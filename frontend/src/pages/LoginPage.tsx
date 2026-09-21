import React, { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { authAPI } from '../services/api'
import { Sparkles, Mail, Lock, ArrowRight, ShieldCheck, Zap, Database, Activity } from 'lucide-react'
import './Auth.css'

const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [elapsedSeconds, setElapsedSeconds] = useState(0)
  const [serverStatus, setServerStatus] = useState<'checking' | 'awake' | 'waking'>('checking')
  const [error, setError] = useState<string | null>(null)
  const navigate = useNavigate()
  const { login } = useAuth()

  // Eager pre-warming on page load
  useEffect(() => {
    let isMounted = true
    authAPI.pingHealth()
      .then(() => {
        if (isMounted) setServerStatus('awake')
      })
      .catch(() => {
        if (isMounted) setServerStatus('waking')
      })

    const statusTimer = setTimeout(() => {
      if (isMounted) {
        setServerStatus((curr) => curr === 'awake' ? 'awake' : 'waking')
      }
    }, 2500)

    return () => {
      isMounted = false
      clearTimeout(statusTimer)
    }
  }, [])

  // Timer during authentication if server cold starts
  useEffect(() => {
    let interval: any
    if (loading) {
      setElapsedSeconds(0)
      interval = setInterval(() => {
        setElapsedSeconds((s) => s + 1)
      }, 1000)
    } else {
      setElapsedSeconds(0)
    }
    return () => clearInterval(interval)
  }, [loading])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)

    try {
      await login(email, password)
      navigate('/')
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed. Please check your credentials.')
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
              AI DEVELOPER INTELLIGENCE
            </span>
          </div>
          <h1>CodeInsight.AI</h1>
          <h2>Welcome back! Sign in to view your algorithmic intelligence</h2>

          <div className="server-status-pill">
            <span className={`status-dot ${serverStatus === 'awake' ? 'online' : 'waking'}`}></span>
            <span>
              {serverStatus === 'awake' 
                ? 'API Server Online (<100ms)' 
                : 'Server Standby (Pre-warming...)'}
            </span>
          </div>
        </div>
        
        {error && <div className="alert alert-error">{error}</div>}

        {loading && elapsedSeconds >= 3 && (
          <div className="auth-server-wakeup-banner">
            <div className="wakeup-banner-header">
              <Activity size={15} color="#fbbf24" className="animate-spin" />
              <span className="wakeup-title">
                Waking Up Cloud Server ({elapsedSeconds}s elapsed)
              </span>
            </div>
            <p className="wakeup-text">
              Render free tier puts idle servers to sleep after 15m. The Java Spring Boot container and PostgreSQL connection pool are starting up. 
              Subsequent logins will be instantaneous!
            </p>
            <div className="wakeup-progress-track">
              <div 
                className="wakeup-progress-bar" 
                style={{ width: `${Math.min(96, Math.max(12, elapsedSeconds * 2.2))}%` }}
              />
            </div>
          </div>
        )}
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Mail size={13} color="#818cf8" /> Email Address
            </label>
            <input
              id="email"
              type="email"
              placeholder="developer@codeinsight.ai"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Lock size={13} color="#818cf8" /> Password
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

          <button type="submit" className="button button-primary btn-shimmer" disabled={loading} style={{ display: 'flex', gap: '8px', alignItems: 'center', justifyContent: 'center' }}>
            {loading ? (
              <>
                <span className="spinner" style={{ width: '16px', height: '16px', borderWidth: '2px' }}></span>
                <span>
                  {elapsedSeconds >= 3 
                    ? `Waking Server (${elapsedSeconds}s)...` 
                    : 'Authenticating...'}
                </span>
              </>
            ) : (
              <>
                <span>Sign In to Dashboard</span>
                <ArrowRight size={16} />
              </>
            )}
          </button>
        </form>

        <p className="auth-link">
          Don't have an account? <Link to="/register">Create an account</Link>
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

export default LoginPage
