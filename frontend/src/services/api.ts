import axios, { AxiosInstance } from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Add JWT token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Handle response errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('jwt_token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// Auth APIs
export const authAPI = {
  register: (data: { name: string; email: string; password: string }) =>
    api.post('/auth/register', data),
  login: (data: { email: string; password: string }) =>
    api.post('/auth/login', data),
  getCurrentUser: () =>
    api.get('/auth/me')
}

// Platform APIs
export const platformAPI = {
  connectAccount: (platform: string, username: string) =>
    api.post('/platforms/connect', { platform, username }),
  refreshStats: () =>
    api.post('/platforms/refresh'),
  getStats: () =>
    api.get('/platforms/stats'),
  getAccounts: () =>
    api.get('/platforms/accounts')
}

// Analytics APIs
export const analyticsAPI = {
  getMetrics: () =>
    api.get('/analytics/metrics'),
  getPerformance: () =>
    api.get('/analytics/performance'),
  getInsights: () =>
    api.get('/analytics/insights')
}

// AI APIs
export const aiAPI = {
  generateRecommendations: (targetCompany: string) =>
    api.post('/ai/generate-recommendations', null, {
      params: { targetCompany }
    }),
  generateStudyPlan: (targetCompany: string, weeksAvailable: number) =>
    api.post('/ai/generate-study-plan', null, {
      params: { targetCompany, weeksAvailable }
    })
}

export default api
