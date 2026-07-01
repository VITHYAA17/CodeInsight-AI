import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { authAPI } from '../services/api'

export interface User {
  id: number
  name: string
  email: string
  createdAt: string
  updatedAt: string
}

interface AuthContextType {
  user: User | null
  loading: boolean
  error: string | null
  login: (email: string, password: string) => Promise<void>
  register: (name: string, email: string, password: string) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Check if user is already logged in
  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem('jwt_token')
      if (token) {
        try {
          const response = await authAPI.getCurrentUser()
          setUser(response.data.data)
        } catch (err) {
          localStorage.removeItem('jwt_token')
          setUser(null)
        }
      }
      setLoading(false)
    }

    checkAuth()
  }, [])

  const login = async (email: string, password: string) => {
    setError(null)
    try {
      const response = await authAPI.login({ email, password })
      const { token, user: userData } = response.data.data
      localStorage.setItem('jwt_token', token)
      setUser(userData)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed')
      throw err
    }
  }

  const register = async (name: string, email: string, password: string) => {
    setError(null)
    try {
      const response = await authAPI.register({ name, email, password })
      const { token, user: userData } = response.data.data
      localStorage.setItem('jwt_token', token)
      setUser(userData)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed')
      throw err
    }
  }

  const logout = () => {
    localStorage.removeItem('jwt_token')
    setUser(null)
    setError(null)
  }

  const value: AuthContextType = {
    user,
    loading,
    error,
    login,
    register,
    logout,
    isAuthenticated: !!user
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
