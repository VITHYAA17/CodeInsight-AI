import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Sparkles, User, Mail, Lock, CheckCircle2, ArrowRight, ShieldCheck, Zap, Database } from 'lucide-react'
import './Auth.css'

const RegisterPage: React.FC = () => {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const navigate = useNavigate()
  const { register } = useAuth()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (password !== confirmPassword) {
      setError('Passwords do not match')
      return
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters')
      return
    }

    setLoading(true)

    try {
      await register(name, email, password)
      navigate('/')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-bg-decoration">
        <div className="auth-bg-ring-outer"></div>
        <div className="auth-bg-ring-inner"></div>
      </div>
      <div className="auth-card">
        <div className="auth-header">
          <div className="auth-brand-badge">
            <Sparkles size={14} color="#818cf8" />
            <span style={{ fontSize: '11.5px', fontWeight: 700, color: '#a5b4fc', letterSpacing: '0.05em' }}>
              JOIN CODEINSIGHT.AI
            </span>
          </div>
          <h1>Create Account</h1>
          <h2>Start tracking cross-platform coding metrics & AI preparation</h2>
        </div>
        
        {error && <div className="alert alert-error">{error}</div>}
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <User size={13} color="#818cf8" /> Full Name
            </label>
            <input
              id="name"
              type="text"
              placeholder="e.g. Alex Turing"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="email" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Mail size={13} color="#818cf8" /> Email Address
            </label>
            <input
              id="email"
              type="email"
              placeholder="alex@domain.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Lock size={13} color="#818cf8" /> Password (Min. 6 characters)
            </label>
            <input
              id="password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="confirm-password" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <CheckCircle2 size={13} color="#818cf8" /> Confirm Password
            </label>
            <input
              id="confirm-password"
              type="password"
              placeholder="••••••••"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          <button type="submit" className="button button-primary btn-shimmer" disabled={loading} style={{ display: 'flex', gap: '8px', alignItems: 'center', justifyContent: 'center' }}>
            {loading ? (
              <>
                <span className="spinner" style={{ width: '16px', height: '16px', borderWidth: '2px' }}></span>
                <span>Setting up workspace...</span>
              </>
            ) : (
              <>
                <span>Get Started Now</span>
                <ArrowRight size={16} />
              </>
            )}
          </button>
        </form>

        <p className="auth-link">
          Already have an account? <Link to="/login">Sign in here</Link>
        </p>

        <div className="auth-features-footer">
          <span className="badge badge-indigo">
            <Database size={11} /> pgvector RAG
          </span>
          <span className="badge badge-cyan">
            <Zap size={11} /> Real-time SSE
          </span>
          <span className="badge badge-emerald">
            <ShieldCheck size={11} /> Rate Limited
          </span>
        </div>
      </div>
    </div>
  )
}

export default RegisterPage
