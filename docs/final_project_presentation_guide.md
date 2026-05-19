# StillNess Final Presentation Video Guide & Script
**Target Duration:** ~8 Minutes (Optimal for the 5-10 min limit)  
**Deliverable:** A comprehensive step-by-step roadmap to achieve a **100% score** on your final video presentation.

---

## 🎬 Part 1: Brief Self-Introduction & System Overview
⏱️ **Time Allocation:** 0:00 – 1:15 (1m 15s)  
🎯 **Rubric Focus:** System Introduction & Overview (10%)

### 🎙️ Talking Points (Script):
> *"Hello! My name is **[Your Name]**, from **[Your Course & Section]**. Today, I am proud to present my individual final project for Systems Integration and Architecture: **StillNess**.*
>
> *StillNess is a premium wellness scheduling platform designed to bridge the gap between wellness seekers and professional mindfulness instructors. It addresses a very common problem: scheduling wellness activities like meditation, yoga, and breathwork is often scattered across platforms, lacks role-based access for instructors, and fails to handle transaction management smoothly.*
>
> *Our goal with StillNess was to build a secure, cross-platform scheduling hub that integrates robust payment processing, live notifications, and deploy-safe, cloud-persistent data pipelines."*

---

## 🏗️ Part 2: Architecture & Codebase Overview
⏱️ **Time Allocation:** 1:15 – 3:00 (1m 45s)  
🎯 **Rubric Focus:** System Architecture & Component Interaction (30%) + Proof of Implementation (20%)

### 🛠️ Visual Action:
Open your IDE (VS Code / Android Studio) and expand the file structure to show where these files live.

### 🎙️ Talking Points & What to Show:
1. **Vertical Slice Architecture Structure**
   * **Show in IDE:** `web/src/features/` directory containing `auth/`, `sessions/`, and `bookings/` as self-contained feature slices.
   * **Script:** *"StillNess implements a **Vertical Slice Architecture** instead of a traditional layered one. As you can see in my frontend structure, each feature—like bookings, authentication, or sessions—is contained inside its own cohesive slice. This increases maintainability, limits cross-dependency issues, and simplifies future system extensions."*
2. **Unified Backend & Mobile Connectivity**
   * **Show in IDE:** `backend/stillness/src/main/.../SessionController.java` & `mobile/app/.../sessions/SessionAdapter.kt`
   * **Script:** *"Our system is completely cross-platform. The React frontend and Kotlin Android app both communicate with our Spring Boot 3 backend REST API via secure token-based authentication."*

---

## 🔧 Part 3: Proof of Technical Integrations
⏱️ **Time Allocation:** 3:00 – 5:00 (2m 00s)  
🎯 **Rubric Focus:** Proof of Implementation (20%)

### 🛠️ Visual Action:
Switch tabs in your IDE to show specific backend config files and classes.

### 🎙️ Talking Points & Code to Highlight:
1. **Deploy-Safe Supabase Base64 Image Storage**
   * **Show in IDE:** `SessionController.java` (`POST /{id}/thumbnail` saving base64 and `GET /{id}/thumbnail` streaming binary) + the Supabase database model.
   * **Script:** *"To solve the problem of ephemeral cloud storage (where images disappear during container redeployment), I designed a custom database-level asset pipeline. Files uploaded by administrators are parsed to safe base64 strings and saved directly in our Supabase PostgreSQL database under a `TEXT` type column. A custom streaming endpoint serves it back with public cache configurations to optimize load times."*
2. **Stripe Payment Gateway Integration**
   * **Show in IDE:** `BookingService.java` or `BookingController.java` where the Stripe sandbox processes transactions.
   * **Script:** *"For secure transactions, we integrated the **Stripe Payment API** in a sandboxed environment. Booking a session successfully creates and verifies a Stripe PaymentIntent before committing the reservation to the database."*
3. **Automated Notification Integration (SMTP)**
   * **Show in IDE:** `EmailService.java` showing standard Spring `JavaMailSender` credentials.
   * **Script:** *"To complete the systems integration flow, any successful booking dynamically triggers our notification service to send confirmation emails via Gmail SMTP to keep the customer informed."*

---

## 💻 Part 4: Live System Walkthrough
⏱️ **Time Allocation:** 5:00 – 7:30 (2m 30s)  
🎯 **Rubric Focus:** Presentation of Main Features (30%) + System Demonstration (10%)

### 🎙️ Action Steps for your Live Demo:
1. **Sign Up / Log In as User**
   * Open the app in your browser at `http://localhost:3000`.
   * Log in or register a new user. Show that you successfully receive a greeting email in your inbox!
2. **Browse & Book a Paid Session**
   * Go to `/sessions`. Look at the cards. Point out the beautiful, dynamic remaining spot progress bars.
   * Click **Reserve Spot** on a paid session. Enter mock credit card details (use `4242 4242 4242 4242` for Stripe Sandbox).
   * Confirm the booking and navigate to the **"My Bookings"** page to show it listed instantly.
3. **Log In as Admin (Instructor) & Upload Thumbnail**
   * Log out, then log in as an Instructor account (e.g. `master@cit.edu`).
   * Navigate to the **Manage Sessions** dashboard.
   * Create or edit a session, select a new beautiful high-resolution image file, and save.
   * Point out the circular avatar preview rendering directly in the table row.
   * Log out, return to the public homepage or sessions list, and show the **gorgeous background card thumbnail rendering flawlessly and persisting over refreshes**.

---

## 👋 Part 5: Conclusion & Q&A Wrap-Up
⏱️ **Time Allocation:** 7:30 – 8:00 (30s)  
🎯 **Rubric Focus:** Presentation Quality & Conclusion (10%)

### 🎙️ Talking Points (Script):
> *"This integration project successfully showcases how multiple complex systems—a Kotlin Android app, a React TS Single Page App, a Spring Boot REST API, a cloud database on Supabase, Stripe payments, and SMTP email services—can be unified into a seamless, robust, and highly secure architecture.*
>
> *Thank you very much for your time, and I am open to any questions!"*

---

## 📋 Final Pre-Recording Verification Checklist
- [ ] Run both the Spring Boot backend server (`mvnw spring-boot:run`) and frontend dev server (`npm run dev`) before starting.
- [ ] Verify you have a mock session in your database with spots left.
- [ ] Keep a browser tab open with your email inbox (e.g., mailtrap or a test Gmail) to show the confirmation emails.
- [ ] Adjust your screen resolution to standard 1080p (1920x1080) and set IDE font scale higher so the code text is highly visible on video.
