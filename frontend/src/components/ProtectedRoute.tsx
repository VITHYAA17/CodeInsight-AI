import React, { useState, useEffect } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

interface ProtectedRouteProps {
  children: React.ReactNode
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, loading } = useAuth()
  const [loadingMessage, setLoadingMessage] = useState('Verifying credentials...')

  useEffect(() => {
    const timer = setTimeout(() => {
      setLoadingMessage("Waking up the backend server container... Since we are on Render's free tier, this initial boot-up takes about 50 seconds. Thanks for your patience! Once awake, all subsequent page reloads will open instantly.")
    }, 3500)
    return () => clearTimeout(timer)
  }, [])

  if (loading) {
    return (
      <div style={{ display: 'flex', height: '100vh', justifyContent: 'center', alignItems: 'center', backgroundColor: 'var(--bg-app)', padding: '24px' }}>
        <div style={{ textAlign: 'center', maxWidth: '480px' }}>
          <div className="spinner" style={{ width: '40px', height: '40px', borderWidth: '3px', margin: '0 auto 16px auto' }}></div>
          <p style={{ color: '#94a3b8', fontSize: '14px', lineHeight: 1.6, fontWeight: 500 }}>{loadingMessage}</p>
        </div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}

export default ProtectedRoute
