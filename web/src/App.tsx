/**
 * Root Application Router — Vertical Slice Architecture
 * Each route now lives within its own feature slice folder.
 */
import { Routes, Route, Navigate } from 'react-router-dom'

// Auth slice
import LoginPage from './features/auth/LoginPage'
import RegisterPage from './features/auth/RegisterPage'
import OAuth2CallbackPage from './features/auth/OAuth2CallbackPage'

// Sessions slice
import SessionsPage from './features/sessions/SessionsPage'
import SessionDetailPage from './features/sessions/SessionDetailPage'

// Bookings slice
import BookingCheckoutPage from './features/bookings/BookingCheckoutPage'
import MyBookingsPage from './features/bookings/MyBookingsPage'

// Admin slice
import AdminSessionsPage from './features/admin/AdminSessionsPage'
import AdminAttendeesPage from './features/admin/AdminAttendeesPage'
import AdminPaymentsPage from './features/admin/AdminPaymentsPage'

// Landing / Dashboard
import LandingPage from './features/landing/LandingPage'

// Shared guards
import ProtectedRoute from './shared/components/ProtectedRoute'
import GuestRoute from './shared/components/GuestRoute'
import AdminRoute from './shared/components/AdminRoute'

function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />
      <Route path="/login" element={<GuestRoute><LoginPage /></GuestRoute>} />
      <Route path="/register" element={<GuestRoute><RegisterPage /></GuestRoute>} />
      <Route path="/dashboard" element={<ProtectedRoute><Navigate to="/" replace /></ProtectedRoute>} />
      <Route path="/sessions" element={<ProtectedRoute><SessionsPage /></ProtectedRoute>} />
      <Route path="/sessions/:sessionId" element={<ProtectedRoute><SessionDetailPage /></ProtectedRoute>} />
      <Route path="/sessions/:sessionId/checkout" element={<ProtectedRoute><BookingCheckoutPage /></ProtectedRoute>} />
      <Route path="/bookings" element={<ProtectedRoute><MyBookingsPage /></ProtectedRoute>} />
      <Route path="/admin/sessions" element={<AdminRoute><AdminSessionsPage /></AdminRoute>} />
      <Route path="/admin/sessions/:sessionId/attendees" element={<AdminRoute><AdminAttendeesPage /></AdminRoute>} />
      <Route path="/admin/payments" element={<AdminRoute><AdminPaymentsPage /></AdminRoute>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
