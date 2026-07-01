import React from 'react'
import { useAuth } from '../context/AuthContext'
import './Dashboard.css'

const DashboardPage: React.FC = () => {
  const { user, logout } = useAuth()

  const handleLogout = () => {
    logout()
    window.location.href = '/login'
  }

  return (
    <div className="dashboard">
      <nav className="navbar">
        <div className="navbar-content">
          <h1>CodeInsight AI</h1>
          <div className="navbar-right">
            <span className="user-name">Welcome, {user?.name}!</span>
            <button onClick={handleLogout} className="button button-secondary">
              Logout
            </button>
          </div>
        </div>
      </nav>

      <div className="dashboard-container">
        <aside className="sidebar">
          <nav className="sidebar-nav">
            <a href="#/" className="nav-item active">📊 Dashboard</a>
            <a href="#/analytics" className="nav-item">📈 Analytics</a>
            <a href="#/study-plan" className="nav-item">📚 Study Plan</a>
            <a href="#/recommendations" className="nav-item">💡 Recommendations</a>
            <a href="#/platforms" className="nav-item">🔗 Platforms</a>
            <a href="#/settings" className="nav-item">⚙️ Settings</a>
          </nav>
        </aside>

        <main className="main-content">
          <div className="container">
            <h2>Dashboard</h2>
            <p style={{ color: '#666', marginTop: '20px' }}>
              Welcome to CodeInsight AI! This dashboard will show your coding progress, analytics, and personalized recommendations.
            </p>
            <p style={{ color: '#999', marginTop: '10px', fontSize: '14px' }}>
              Phase 6C coming soon with full dashboard metrics and charts...
            </p>
          </div>
        </main>
      </div>
    </div>
  )
}

export default DashboardPage
