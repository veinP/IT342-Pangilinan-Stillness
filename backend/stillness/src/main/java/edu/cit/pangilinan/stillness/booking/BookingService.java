package edu.cit.pangilinan.stillness.booking;

import edu.cit.pangilinan.stillness.booking.dto.CreateBookingRequest;
import edu.cit.pangilinan.stillness.booking.dto.BookingDto;
import edu.cit.pangilinan.stillness.shared.model.Booking;
import edu.cit.pangilinan.stillness.shared.model.Payment;
import edu.cit.pangilinan.stillness.shared.model.Session;
import edu.cit.pangilinan.stillness.shared.model.User;
import edu.cit.pangilinan.stillness.booking.event.BookingCreatedEvent;
import edu.cit.pangilinan.stillness.auth.EmailService;
import edu.cit.pangilinan.stillness.shared.repository.BookingRepository;
import edu.cit.pangilinan.stillness.shared.repository.PaymentRepository;
import edu.cit.pangilinan.stillness.shared.repository.SessionRepository;
import edu.cit.pangilinan.stillness.shared.repository.UserRepository;
import edu.cit.pangilinan.stillness.session.dto.SessionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("unchecked")
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private EmailService emailService;

    private User resolveCurrentUser(User user) {
        if (user != null && user.getId() != null) {
            return user;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String email = auth.getName();
        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            return null;
        }

        return userRepository.findByEmail(email).orElse(null);
    }

    public BookingDto createBooking(CreateBookingRequest request, User user) {
        User resolvedUser = resolveCurrentUser(user);
        if (resolvedUser == null) {
            throw new IllegalStateException("Authenticated user is required to create a booking");
        }

        Optional<Session> sessionOpt = sessionRepository.findById(request.getSessionId());
        if (sessionOpt.isEmpty()) {
            return null;
        }

        Session session = sessionOpt.get();

        // 1. Prevent duplicate booking for the same session
        if (bookingRepository.existsByUserAndSessionAndStatusNot(resolvedUser, session, "CANCELLED")) {
            throw new IllegalStateException("You have already booked this session");
        }

        // 2. Prevent booking cancelled session
        if ("CANCELLED".equals(session.getStatus())) {
            throw new IllegalStateException("Cannot book a cancelled session");
        }

        // 3. Prevent booking past sessions
        if (session.getStartTime() != null && session.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot book a session that has already started");
        }

        // 4. Prevent booking if capacity is reached
        long bookedCount = getSessionBookingCount(session.getId());
        if (session.getCapacity() != null && bookedCount >= session.getCapacity()) {
            throw new IllegalStateException("Session is fully booked");
        }

        BigDecimal amount = session.getPrice() != null ? session.getPrice() : BigDecimal.ZERO;

        // Generate unique booking number
        String bookingNumber = "STN-" + System.currentTimeMillis();
        String paymentIntentId = (amount.compareTo(BigDecimal.ZERO) == 0 ? "free-" : "pi-") + bookingNumber;
        String paymentStatus = amount.compareTo(BigDecimal.ZERO) == 0 ? "PAID" : "PENDING";

        Booking booking = Booking.builder()
                .bookingNumber(bookingNumber)
            .user(resolvedUser)
                .session(session)
                .status("CONFIRMED")
                .bookedAt(LocalDateTime.now())
                .attendeeNotes(request.getAttendeeNotes())
                .build();

        Booking saved = bookingRepository.save(booking);

        eventPublisher.publishEvent(new BookingCreatedEvent(this, saved, resolvedUser.getEmail()));

        Payment payment = Payment.builder()
                .booking(saved)
                .user(resolvedUser)
                .amount(amount)
                .currency("PHP")
                .paymentMethod(amount.compareTo(BigDecimal.ZERO) == 0 ? "FREE" : "SANDBOX_CARD")
                .paymentIntentId(paymentIntentId)
                .transactionId(amount.compareTo(BigDecimal.ZERO) == 0 ? "FREE-" + bookingNumber : null)
                .status(paymentStatus)
                .paymentDate(LocalDateTime.now())
                .build();

            paymentRepository.save(payment);

            // Send booking confirmation email
            emailService.sendBookingConfirmation(
                resolvedUser.getEmail(),
                resolvedUser.getFullName(),
                session.getTitle(),
                session.getStartTime(),
                session.getLocation()
            );

            BookingDto dto = convertToDto(saved);
            dto.setAmount(amount);
            dto.setPaymentStatus(paymentStatus);
            dto.setPaymentIntentId(paymentIntentId);
            return dto;
    }

    public BookingDto getBookingById(UUID id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        return booking.map(this::convertToDto).orElse(null);
    }

    public boolean hasActiveBooking(User user, UUID sessionId) {
        User resolvedUser = resolveCurrentUser(user);
        if (resolvedUser == null) return false;
        
        Optional<Session> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) return false;
        
        return bookingRepository.existsByUserAndSessionAndStatusNot(resolvedUser, sessionOpt.get(), "CANCELLED");
    }

    public List<BookingDto> getUserBookings(User user) {
        User resolvedUser = resolveCurrentUser(user);
        if (resolvedUser == null) {
            return List.of();
        }

        return bookingRepository.findByUser(resolvedUser).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getSessionBookings(Session session) {
        return bookingRepository.findBySession(session).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BookingDto cancelBooking(UUID id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return null;
        }

        Booking booking = bookingOpt.get();
        booking.setStatus("CANCELLED");
        booking.setCancelledAt(LocalDateTime.now());

        Booking updated = bookingRepository.save(booking);
        
        // Send cancellation confirmation email
        if (booking.getUser() != null && booking.getSession() != null) {
            emailService.sendCancellationConfirmation(
                booking.getUser().getEmail(),
                booking.getUser().getFullName(),
                booking.getSession().getTitle()
            );
        }
        
        return convertToDto(updated);
    }

    public long getSessionBookingCount(UUID sessionId) {
        Optional<Session> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return 0;
        }
        return bookingRepository.countBySessionAndStatus(sessionOpt.get(), "CONFIRMED");
    }

    private BookingDto convertToDto(Booking booking) {
        SessionDto sessionDto = null;
        BigDecimal amount = BigDecimal.ZERO;
        String paymentStatus = null;
        String paymentIntentId = null;

        Optional<Payment> paymentOpt = paymentRepository.findByBooking(booking);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            amount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
            paymentStatus = payment.getStatus();
            paymentIntentId = payment.getPaymentIntentId();
        }

        if (booking.getSession() != null) {
            Session s = booking.getSession();
            SessionDto.InstructorDto instructorDto = null;
            if (s.getInstructor() != null) {
                String instFullName = s.getInstructor().getUser() != null ? s.getInstructor().getUser().getFullName() : "Instructor";
                instructorDto = SessionDto.InstructorDto.builder()
                        .id(s.getInstructor().getId())
                        .fullName(instFullName)
                        .profileImageUrl(s.getInstructor().getProfileImageUrl())
                        .build();
            }
            sessionDto = SessionDto.builder()
                    .id(s.getId())
                    .title(s.getTitle())
                    .description(s.getDescription())
                    .type(s.getSessionType())
                    .startTime(s.getStartTime())
                    .endTime(s.getEndTime())
                    .capacity(s.getCapacity())
                    .price(s.getPrice())
                    .instructor(instructorDto)
                    .thumbnailUrl(s.getThumbnailUrl())
                    .location(s.getLocation())
                    .status(s.getStatus())
                    .createdAt(s.getCreatedAt())
                    .build();
        }

        return BookingDto.builder()
                .id(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .userId(booking.getUser() != null ? booking.getUser().getId() : null)
                .session(sessionDto)
                .status(booking.getStatus())
                .bookedAt(booking.getBookedAt())
                .cancelledAt(booking.getCancelledAt())
                .cancellationReason(booking.getCancellationReason())
                .attendeeNotes(booking.getAttendeeNotes())
                .amount(amount)
                .paymentStatus(paymentStatus)
                .paymentIntentId(paymentIntentId)
                .build();
    }
}
