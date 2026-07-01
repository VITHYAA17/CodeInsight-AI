import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'

// Pages - will be created in Phase 6B onwards
const LoginPage = React.lazy(() => import('./pages/LoginPage'))
const RegisterPage = React.lazy(() => import('./pages/RegisterPage'))
const DashboardPage = React.lazy(() => import('./pages/DashboardPage'))

const App: React.FC = () => {
  return (
    <Router>
      <AuthProvider>
        <React.Suspense fallback={<div className="container" style={{ paddingTop: '40px', textAlign: 'center' }}><div className="spinner"></div></div>}>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <DashboardPage />
                </ProtectedRoute>
              }
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </React.Suspense>
      </AuthProvider>
    </Router>
  )
}

export default App
