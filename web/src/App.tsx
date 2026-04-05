import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import LandingPage from './pages/LandingPage'
import SessionsPage from './pages/SessionsPage'
import SessionDetailPage from './pages/SessionDetailPage'
import BookingCheckoutPage from './pages/BookingCheckoutPage'
import MyBookingsPage from './pages/MyBookingsPage'
import AdminSessionsPage from './pages/AdminSessionsPage'
import AdminAttendeesPage from './pages/AdminAttendeesPage'
import AdminPaymentsPage from './pages/AdminPaymentsPage'
import OAuth2CallbackPage from './pages/OAuth2CallbackPage'
import ProtectedRoute from './components/ProtectedRoute'
import GuestRoute from './components/GuestRoute'
import AdminRoute from './components/AdminRoute'

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
