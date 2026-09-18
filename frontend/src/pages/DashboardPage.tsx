import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { platformAPI, analyticsAPI, aiAPI } from '../services/api'
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Legend
} from 'recharts'
import {
  LayoutDashboard,
  TrendingUp,
  BookOpen,
  Sparkles,
  Link2,
  Settings,
  LogOut,
  RefreshCw,
  Flame,
  Trophy,
  Target,
  Award,
  CheckCircle2,
  ExternalLink,
  Zap,
  Database,
  Code2,
  Terminal,
  Cpu,
  User,
  GraduationCap,
  Phone,
  FileText,
  Clock,
  ArrowRight,
  Check
} from 'lucide-react'
import './Dashboard.css'

interface CodingAccount {
  leetcodeUsername?: string | null
  codeforcesUsername?: string | null
  codechefUsername?: string | null
  geeksforgeeksUsername?: string | null
  githubUsername?: string | null
}

interface PlatformMetric {
  platform: string
  totalSolved: number
  easySolved: number
  mediumSolved: number
  hardSolved: number
  acceptanceRate: number | null
  contestRating: number | null
  currentStreak: number | null
}

interface Metrics {
  totalProblems: number
  easyCount: number
  mediumCount: number
  hardCount: number
  easyPercentage: number
  mediumPercentage: number
  hardPercentage: number
  averageAcceptanceRate: number
  averageContestRating: number
  maxCurrentStreak: number
  platformBreakdown: Record<string, PlatformMetric>
}

interface TopicPerformance {
  topicName: string
  strengthScore: number
  problemsSolved: number
}

interface Performance {
  strongestTopics: TopicPerformance[]
  weakestTopics: TopicPerformance[]
  improvementAreas: string[]
  platformComparison: Record<string, number>
  strongestPlatform: string | null
  totalContests: number
  averageContestRank: number | null
}

interface SkillGap {
  topic: string
  currentScore: number
  targetScore: number
  estimatedDaysToTarget: number
  priority: string
}

interface Insights {
  interviewReadinessScore: number
  companyMatchingScores: Record<string, number>
  topicStrengths: string[]
  skillGaps: SkillGap[]
  performanceLevel: string
  nextMilestone: string
}

interface StudyPlanTask {
  id: number
  weekNumber: number
  topicName: string
  taskDescription: string
  status: string
  createdAt: string
  completedAt?: string | null
}

interface Recommendation {
  id: number
  targetCompany: string
  recommendationText: string
  interviewReadiness: number
  generatedAt: string
}

const DashboardPage: React.FC = () => {
  const { user, logout, updateProfile } = useAuth()
  const [activeTab, setActiveTab] = useState<string>('dashboard')
  const [loadingMessage, setLoadingMessage] = useState('Loading CodeInsight.AI Dashboard...')

  useEffect(() => {
    const timer = setTimeout(() => {
      setLoadingMessage("Waking up the database server container... Since we are on Render's free tier, this initial boot-up takes about 50 seconds. Thanks for your patience! Once awake, all subsequent dashboard elements will open instantly.")
    }, 3500)
    return () => clearTimeout(timer)
  }, [])

  // Profile settings inputs
  const [profileName, setProfileName] = useState<string>('')
  const [collegeName, setCollegeName] = useState<string>('')
  const [contactNumber, setContactNumber] = useState<string>('')
  const [profilePhoto, setProfilePhoto] = useState<string>('')
  const [resumeUrl, setResumeUrl] = useState<string>('')
  const [profileLoading, setProfileLoading] = useState<boolean>(false)
  const [profileMessage, setProfileMessage] = useState<string | null>(null)

  useEffect(() => {
    if (user) {
      setProfileName(user.name || '')
      setCollegeName(user.collegeName || '')
      setContactNumber(user.contactNumber || '')
      setProfilePhoto(user.profilePhoto || '')
      setResumeUrl(user.resumeUrl || '')
    }
  }, [user])

  // Data states
  const [accounts, setAccounts] = useState<CodingAccount | null>(null)
  const [metrics, setMetrics] = useState<Metrics | null>(null)
  const [performance, setPerformance] = useState<Performance | null>(null)
  const [insights, setInsights] = useState<Insights | null>(null)
  const [studyPlans, setStudyPlans] = useState<StudyPlanTask[]>([])
  const [recommendations, setRecommendations] = useState<Recommendation[]>([])

  // Loading / Error states
  const [loading, setLoading] = useState<boolean>(true)
  const [syncing, setSyncing] = useState<boolean>(false)
  const [formError, setFormError] = useState<string | null>(null)
  const [formSuccess, setFormSuccess] = useState<string | null>(null)

  // Forms inputs
  const [connectPlatform, setConnectPlatform] = useState<string>('leetcode')
  const [connectUsername, setConnectUsername] = useState<string>('')
  
  const [planCompany, setPlanCompany] = useState<string>('Google')
  const [planWeeks, setPlanWeeks] = useState<number>(4)
  const [planLoading, setPlanLoading] = useState<boolean>(false)
  const [streamingPlan, setStreamingPlan] = useState<boolean>(false)
  const [streamedPlanText, setStreamedPlanText] = useState<string>('')

  const [recCompany, setRecCompany] = useState<string>('Core DSA Mastery')
  const [recLoading, setRecLoading] = useState<boolean>(false)
  const [streamingRec, setStreamingRec] = useState<boolean>(false)
  const [streamedRecText, setStreamedRecText] = useState<string>('')

  useEffect(() => {
    fetchData()
  }, [])

  const formatDate = (dateInput: any) => {
    if (!dateInput) return 'N/A';
    if (Array.isArray(dateInput)) {
      const [year, month, day, hour = 0, minute = 0, second = 0] = dateInput;
      return new Date(year, month - 1, day, hour, minute, second).toLocaleDateString();
    }
    const d = new Date(dateInput);
    return isNaN(d.getTime()) ? 'N/A' : d.toLocaleDateString();
  }

  const renderFormattedMarkdown = (text: string) => {
    if (!text) return null;
    const lines = text.split('\n');
    return lines.map((line, index) => {
      let trimmed = line.trim();
      if (!trimmed) return <div key={index} style={{ height: '12px' }} />;
      
      const parseInlineStyles = (txt: string) => {
        const parts = [];
        let currentIndex = 0;
        const boldItalicRegex = /(\*\*.*?\*\*|\*.*?\*)/g;
        let match;
        
        while ((match = boldItalicRegex.exec(txt)) !== null) {
          const matchIndex = match.index;
          const matchStr = match[0];
          
          if (matchIndex > currentIndex) {
            parts.push(txt.substring(currentIndex, matchIndex));
          }
          
          if (matchStr.startsWith('**') && matchStr.endsWith('**')) {
            parts.push(
              <strong key={matchIndex} style={{ fontWeight: 700, color: '#ffffff' }}>
                {matchStr.slice(2, -2)}
              </strong>
            );
          } else if (matchStr.startsWith('*') && matchStr.endsWith('*')) {
            parts.push(
              <em key={matchIndex} style={{ fontStyle: 'italic', color: '#94a3b8' }}>
                {matchStr.slice(1, -1)}
              </em>
            );
          }
          
          currentIndex = boldItalicRegex.lastIndex;
        }
        
        if (currentIndex < txt.length) {
          parts.push(txt.substring(currentIndex));
        }
        
        return parts;
      };

      if (trimmed.startsWith('### ')) {
        return (
          <h4 key={index} style={{ fontSize: '20px', fontWeight: 700, color: '#ffffff', marginTop: '28px', marginBottom: '14px', borderBottom: '1px solid rgba(255, 255, 255, 0.08)', paddingBottom: '8px' }}>
            {parseInlineStyles(trimmed.substring(4))}
          </h4>
        );
      }
      
      if (trimmed.startsWith('#### ')) {
        return (
          <h5 key={index} style={{ fontSize: '16px', fontWeight: 700, color: '#a5b4fc', marginTop: '20px', marginBottom: '10px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            {parseInlineStyles(trimmed.substring(5))}
          </h5>
        );
      }
      
      if (trimmed.startsWith('- ') || trimmed.startsWith('* ')) {
        const content = trimmed.substring(2);
        return (
          <div key={index} style={{ display: 'flex', gap: '8px', marginLeft: '12px', marginBottom: '8px', lineHeight: 1.7 }}>
            <span style={{ color: '#6366f1', fontSize: '15.5px' }}>•</span>
            <span style={{ fontSize: '15.5px', color: '#cbd5e1' }}>{parseInlineStyles(content)}</span>
          </div>
        );
      }
      
      const numMatch = trimmed.match(/^(\d+)\.\s(.*)/);
      if (numMatch) {
        const num = numMatch[1];
        const content = numMatch[2];
        return (
          <div key={index} style={{ display: 'flex', gap: '8px', marginLeft: '12px', marginBottom: '12px', alignItems: 'flex-start', lineHeight: 1.7 }}>
            <span style={{ color: '#818cf8', fontWeight: 600, minWidth: '18px', fontSize: '15.5px' }}>{num}.</span>
            <span style={{ fontSize: '15.5px', color: '#cbd5e1' }}>{parseInlineStyles(content)}</span>
          </div>
        );
      }
      
      return (
        <p key={index} style={{ fontSize: '15.5px', color: '#cbd5e1', lineHeight: 1.7, marginBottom: '16px' }}>
          {parseInlineStyles(trimmed)}
        </p>
      );
    });
  };

  const fetchData = async () => {
    setLoading(true)
    try {
      // Step 1: Fetch primary account & metrics first to unblock UI rendering instantly
      const [accountsRes, metricsRes] = await Promise.all([
        platformAPI.getAccounts(),
        analyticsAPI.getMetrics()
      ])

      setAccounts(accountsRes.data)
      setMetrics(metricsRes.data)
      setLoading(false) // Dashboard loads instantly!

      // Step 2: Fetch remaining analytics & AI recommendations asynchronously in background
      Promise.all([
        analyticsAPI.getPerformance(),
        analyticsAPI.getInsights(),
        aiAPI.getStudyPlan(),
        aiAPI.getRecommendations()
      ]).then(([performanceRes, insightsRes, studyPlanRes, recommendationRes]) => {
        setPerformance(performanceRes.data)
        setInsights(insightsRes.data)
        setStudyPlans(studyPlanRes.data.data || [])
        setRecommendations(recommendationRes.data.data || [])
      }).catch(err => console.error('Error fetching secondary analytics:', err))

    } catch (err) {
      console.error('Error fetching dashboard data:', err)
      setLoading(false)
    }
  }

  const handleLogout = () => {
    logout()
    window.location.href = '/login'
  }

  const handleConnect = async (e: React.FormEvent) => {
    e.preventDefault()
    setFormError(null)
    setFormSuccess(null)
    if (!connectUsername.trim()) {
      setFormError('Username is required')
      return
    }

    try {
      await platformAPI.connectAccount(connectPlatform, connectUsername)
      setFormSuccess(`${connectPlatform} account connected and synced successfully!`)
      setConnectUsername('')
      fetchData()
    } catch (err: any) {
      setFormError(err.response?.data?.message || 'Failed to connect platform')
    }
  }

  const handleSyncAll = async () => {
    setSyncing(true)
    setFormError(null)
    setFormSuccess(null)
    try {
      await platformAPI.refreshStats()
      setFormSuccess('All connected accounts re-synced successfully!')
      fetchData()
    } catch (err: any) {
      setFormError('Failed to refresh data from platforms')
    } finally {
      setSyncing(false)
    }
  }

  const handleGenerateStudyPlan = async (e: React.FormEvent) => {
    e.preventDefault()
    setPlanLoading(true)
    setStreamingPlan(true)
    setStreamedPlanText('')
    setFormError(null)
    setFormSuccess(null)
    setActiveTab('study-plan')

    try {
      const url = aiAPI.getStreamStudyPlanUrl(planCompany, planWeeks)
      const eventSource = new EventSource(url)

      eventSource.addEventListener('chunk', (event: MessageEvent) => {
        setStreamedPlanText((prev) => prev + event.data)
      })

      eventSource.addEventListener('complete', async () => {
        eventSource.close()
        setStreamingPlan(false)
        setPlanLoading(false)
        setFormSuccess('Study plan generated with Neon pgvector RAG!')
        try {
          const studyPlanRes = await aiAPI.getStudyPlan()
          setStudyPlans(studyPlanRes.data.data || [])
        } catch (err) {
          console.error('Failed to reload study plan', err)
        }
      })

      eventSource.addEventListener('error', (event) => {
        console.warn('SSE stream error or fallback, closing stream', event)
        eventSource.close()
        setStreamingPlan(false)
        setPlanLoading(false)
        aiAPI.getStudyPlan().then((res) => {
          setStudyPlans(res.data.data || [])
        }).catch(() => {})
      })
    } catch (err: any) {
      setFormError(err.message || 'Failed to start study plan stream')
      setStreamingPlan(false)
      setPlanLoading(false)
    }
  }

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault()
    setProfileLoading(true)
    setProfileMessage(null)
    try {
      await updateProfile({
        name: profileName,
        collegeName,
        contactNumber,
        profilePhoto,
        resumeUrl
      })
      setProfileMessage('Profile settings updated successfully!')
      setTimeout(() => setProfileMessage(null), 4000)
    } catch (err: any) {
      setProfileMessage('Error: ' + (err.response?.data?.message || 'Failed to update profile settings'))
    } finally {
      setProfileLoading(false)
    }
  }

  const handleGenerateRecommendations = async (e: React.FormEvent) => {
    e.preventDefault()
    setRecLoading(true)
    setStreamingRec(true)
    setStreamedRecText('')
    setFormError(null)
    setFormSuccess(null)
    setActiveTab('recommendations')

    try {
      const url = aiAPI.getStreamRecommendationsUrl(recCompany)
      const eventSource = new EventSource(url)

      eventSource.addEventListener('chunk', (event: MessageEvent) => {
        setStreamedRecText((prev) => prev + event.data)
      })

      eventSource.addEventListener('complete', async () => {
        eventSource.close()
        setStreamingRec(false)
        setRecLoading(false)
        setFormSuccess('AI Recommendations generated with Neon pgvector RAG!')
        try {
          const recommendationRes = await aiAPI.getRecommendations()
          setRecommendations(recommendationRes.data.data || [])
        } catch (err) {
          console.error('Failed to reload recommendations', err)
        }
      })

      eventSource.addEventListener('error', (event) => {
        console.warn('SSE stream error or fallback, closing stream', event)
        eventSource.close()
        setStreamingRec(false)
        setRecLoading(false)
        aiAPI.getRecommendations().then((res) => {
          setRecommendations(res.data.data || [])
        }).catch(() => {})
      })
    } catch (err: any) {
      setFormError(err.message || 'Failed to start recommendations stream')
      setStreamingRec(false)
      setRecLoading(false)
    }
  }

  const handleToggleTask = async (id: number) => {
    try {
      await aiAPI.toggleStudyPlanTask(id)
      // Update local state state to be snappy
      setStudyPlans(prev =>
        prev.map(task => {
          if (task.id === id) {
            const nextStatus = task.status === 'COMPLETED' ? 'PENDING' : 'COMPLETED'
            return { ...task, status: nextStatus }
          }
          return task
        })
      )
    } catch (err) {
      console.error('Failed to toggle study plan task status', err)
    }
  }

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

  // Render sub-components
  const renderDashboardOverview = () => {
    if (!metrics) return <p>No statistics connected. Please connect accounts in the "Platforms" tab.</p>

    const hasAccounts = accounts && (accounts.leetcodeUsername || accounts.geeksforgeeksUsername || accounts.codechefUsername || accounts.githubUsername)

    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
        {/* Welcome Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <h3 style={{ fontSize: '24px', fontWeight: 800, margin: 0, letterSpacing: '-0.02em' }}>
              Welcome back, <span className="gradient-text">{user?.name || 'Developer'}</span> 👋
            </h3>
            <p style={{ color: '#94a3b8', fontSize: '14px', marginTop: '4px' }}>
              Unified cross-platform algorithmic intelligence & AI preparation metrics.
            </p>
          </div>
          {hasAccounts && (
            <button
              className="button button-secondary"
              onClick={handleSyncAll}
              disabled={syncing}
              style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13.5px', padding: '9px 18px' }}
            >
              <RefreshCw size={15} className={syncing ? 'spinner' : ''} style={syncing ? { border: 'none', animation: 'spin 1s linear infinite' } : {}} />
              <span>{syncing ? 'Syncing Profiles...' : 'Sync All Profiles'}</span>
            </button>
          )}
        </div>

        {formSuccess && <div className="alert alert-success">{formSuccess}</div>}

        {!hasAccounts ? (
          <div className="card" style={{ textAlign: 'center', padding: '50px 24px', background: 'rgba(15, 23, 42, 0.7)' }}>
            <div style={{ width: '56px', height: '56px', borderRadius: '16px', background: 'rgba(99, 102, 241, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px auto' }}>
              <Link2 size={26} color="#818cf8" />
            </div>
            <h4 style={{ fontSize: '20px', fontWeight: 700, marginBottom: '8px' }}>No coding platforms connected yet</h4>
            <p style={{ color: '#94a3b8', fontSize: '14.5px', maxWidth: '460px', margin: '0 auto 24px auto' }}>
              Connect your LeetCode, GeeksforGeeks, CodeChef, or GitHub accounts to activate live algorithmic analytics and trigger AI study roadmaps.
            </p>
            <button className="button button-primary btn-shimmer" onClick={() => setActiveTab('platforms')} style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
              <span>Connect Platforms Now</span>
              <ArrowRight size={16} />
            </button>
          </div>
        ) : (
          <>
            {/* Stats Summary Cards Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '20px' }}>
              {/* Card 1: Total Solved */}
              <div className="stat-card-modern">
                <div className="stat-card-header">
                  <span className="stat-card-title">Total Solved</span>
                  <div className="stat-icon-wrapper" style={{ background: 'rgba(16, 185, 129, 0.15)' }}>
                    <Trophy size={20} color="#10b981" />
                  </div>
                </div>
                <div className="stat-value gradient-text-emerald">{metrics.totalProblems}</div>
                <div className="stat-subtext">
                  <span className="badge badge-emerald">
                    <CheckCircle2 size={11} /> Verified Solves
                  </span>
                </div>
              </div>

              {/* Card 2: Current Streak */}
              <div className="stat-card-modern">
                <div className="stat-card-header">
                  <span className="stat-card-title">Current Streak</span>
                  <div className="stat-icon-wrapper" style={{ background: 'rgba(245, 158, 11, 0.15)' }}>
                    <Flame size={20} color="#f59e0b" />
                  </div>
                </div>
                <div className="stat-value gradient-text-amber">
                  {metrics.maxCurrentStreak}{' '}
                  <span style={{ fontSize: '16px', fontWeight: 600, color: '#94a3b8' }}>days</span>
                </div>
                <div className="stat-subtext">
                  <span className="badge badge-amber">🔥 Active Streak</span>
                </div>
              </div>

              {/* Card 3: Acceptance Rate */}
              <div className="stat-card-modern">
                <div className="stat-card-header">
                  <span className="stat-card-title">Acceptance Rate</span>
                  <div className="stat-icon-wrapper" style={{ background: 'rgba(6, 182, 212, 0.15)' }}>
                    <Target size={20} color="#06b6d4" />
                  </div>
                </div>
                <div className="stat-value">{metrics.averageAcceptanceRate}%</div>
                <div className="stat-subtext">
                  <span className="badge badge-cyan">
                    <TrendingUp size={11} /> Average Accuracy
                  </span>
                </div>
              </div>

              {/* Card 4: Readiness Score */}
              <div className="stat-card-modern">
                <div className="stat-card-header">
                  <span className="stat-card-title">Readiness Score</span>
                  <div className="stat-icon-wrapper" style={{ background: 'rgba(168, 85, 247, 0.15)' }}>
                    <Award size={20} color="#a855f7" />
                  </div>
                </div>
                <div className="stat-value gradient-text">
                  {insights?.interviewReadinessScore || 0}
                  <span style={{ fontSize: '16px', fontWeight: 600, color: '#94a3b8' }}>/100</span>
                </div>
                <div className="stat-subtext">
                  <span className="badge badge-purple">
                    <Sparkles size={11} /> {insights?.performanceLevel || 'Beginner'}
                  </span>
                </div>
              </div>
            </div>

            {/* Platform Profiles & Strengths */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))', gap: '20px' }}>
              <div className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Code2 size={18} color="#818cf8" />
                    <h4 style={{ fontSize: '16px', margin: 0, fontWeight: 700 }}>Connected Accounts</h4>
                  </div>
                  <span className="badge badge-indigo" style={{ fontSize: '10px' }}>Live Sync</span>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {accounts?.leetcodeUsername && (
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 14px', background: 'rgba(255, 255, 255, 0.03)', borderRadius: '10px', border: '1px solid rgba(255, 255, 255, 0.06)' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{ width: '32px', height: '32px', borderRadius: '8px', background: 'rgba(255, 161, 22, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#ffa116', fontWeight: 800, fontSize: '14px' }}>
                          LC
                        </div>
                        <div>
                          <span style={{ fontSize: '14px', fontWeight: 600, color: '#ffffff', display: 'block' }}>LeetCode</span>
                          <span style={{ fontSize: '12px', color: '#94a3b8' }}>@{accounts.leetcodeUsername}</span>
                        </div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <span style={{ fontSize: '14px', fontWeight: 700, color: '#ffffff', display: 'block' }}>
                          {metrics?.platformBreakdown?.['leetcode']?.totalSolved ?? 0} solved
                        </span>
                        <span style={{ fontSize: '11px', color: '#f59e0b', fontWeight: 600 }}>
                          🔥 {metrics?.platformBreakdown?.['leetcode']?.currentStreak ?? 0} day streak
                        </span>
                      </div>
                    </div>
                  )}

                  {accounts?.geeksforgeeksUsername && (
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 14px', background: 'rgba(255, 255, 255, 0.03)', borderRadius: '10px', border: '1px solid rgba(255, 255, 255, 0.06)' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{ width: '32px', height: '32px', borderRadius: '8px', background: 'rgba(16, 185, 129, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#10b981', fontWeight: 800, fontSize: '14px' }}>
                          GFG
                        </div>
                        <div>
                          <span style={{ fontSize: '14px', fontWeight: 600, color: '#ffffff', display: 'block' }}>GeeksforGeeks</span>
                          <span style={{ fontSize: '12px', color: '#94a3b8' }}>@{accounts.geeksforgeeksUsername}</span>
                        </div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <span style={{ fontSize: '14px', fontWeight: 700, color: '#ffffff', display: 'block' }}>
                          {metrics?.platformBreakdown?.['geeksforgeeks']?.totalSolved ?? 0} solved
                        </span>
                        <span style={{ fontSize: '11px', color: '#f59e0b', fontWeight: 600 }}>
                          🔥 {metrics?.platformBreakdown?.['geeksforgeeks']?.currentStreak ?? 0} day streak
                        </span>
                      </div>
                    </div>
                  )}

                  {accounts?.codechefUsername && (
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 14px', background: 'rgba(255, 255, 255, 0.03)', borderRadius: '10px', border: '1px solid rgba(255, 255, 255, 0.06)' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{ width: '32px', height: '32px', borderRadius: '8px', background: 'rgba(168, 85, 247, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#c084fc', fontWeight: 800, fontSize: '14px' }}>
                          CC
                        </div>
                        <div>
                          <span style={{ fontSize: '14px', fontWeight: 600, color: '#ffffff', display: 'block' }}>CodeChef</span>
                          <span style={{ fontSize: '12px', color: '#94a3b8' }}>@{accounts.codechefUsername}</span>
                        </div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <span style={{ fontSize: '14px', fontWeight: 700, color: '#ffffff', display: 'block' }}>
                          {metrics?.platformBreakdown?.['codechef']?.totalSolved ?? 0} solved
                        </span>
                        <span style={{ fontSize: '11px', color: '#f59e0b', fontWeight: 600 }}>
                          🔥 {metrics?.platformBreakdown?.['codechef']?.currentStreak ?? 0} day streak
                        </span>
                      </div>
                    </div>
                  )}

                  {accounts?.githubUsername && (
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 14px', background: 'rgba(255, 255, 255, 0.03)', borderRadius: '10px', border: '1px solid rgba(255, 255, 255, 0.06)' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{ width: '32px', height: '32px', borderRadius: '8px', background: 'rgba(99, 102, 241, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#a5b4fc', fontWeight: 800, fontSize: '14px' }}>
                          GH
                        </div>
                        <div>
                          <span style={{ fontSize: '14px', fontWeight: 600, color: '#ffffff', display: 'block' }}>GitHub</span>
                          <span style={{ fontSize: '12px', color: '#94a3b8' }}>@{accounts.githubUsername}</span>
                        </div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <span style={{ fontSize: '14px', fontWeight: 700, color: '#ffffff', display: 'block' }}>
                          {metrics?.platformBreakdown?.['github']?.totalSolved ?? 0} repos
                        </span>
                        <span style={{ fontSize: '11px', color: '#f59e0b', fontWeight: 600 }}>
                          🔥 {metrics?.platformBreakdown?.['github']?.currentStreak ?? 0} day streak
                        </span>
                      </div>
                    </div>
                  )}
                </div>
              </div>

              <div className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Sparkles size={18} color="#a855f7" />
                    <h4 style={{ fontSize: '16px', margin: 0, fontWeight: 700 }}>Coach Recommendations</h4>
                  </div>
                  <span className="badge badge-purple" style={{ fontSize: '10px' }}>AI Advisor</span>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {insights?.topicStrengths && insights.topicStrengths.map((strength, idx) => (
                    <div key={idx} style={{ fontSize: '13.5px', padding: '12px 14px', backgroundColor: 'rgba(99, 102, 241, 0.05)', borderRadius: '10px', border: '1px solid rgba(99, 102, 241, 0.2)', borderLeft: '4px solid #6366f1', color: '#f8fafc', fontWeight: 500 }}>
                      {strength}
                    </div>
                  ))}
                  {insights?.nextMilestone && (
                    <div style={{ marginTop: '12px', padding: '12px 14px', borderRadius: '10px', background: 'rgba(245, 158, 11, 0.06)', border: '1px solid rgba(245, 158, 11, 0.2)', fontSize: '13.5px', color: '#fcd34d' }}>
                      <strong style={{ color: '#ffffff' }}>Target Milestone:</strong> {insights.nextMilestone}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </>
        )}
      </div>
    )
  }

  const renderAnalytics = () => {
    if (!metrics || !performance) return <p>Please connect coding platforms first to view graphs.</p>

    const difficultyData = [
      { name: 'Easy', value: metrics.easyCount, color: '#10b981' },
      { name: 'Medium', value: metrics.mediumCount, color: '#f59e0b' },
      { name: 'Hard', value: metrics.hardCount, color: '#ef4444' }
    ]

    const platformComparisonData = Object.entries(performance.platformComparison || {}).map(([platform, solved]) => ({
      name: platform.toUpperCase(),
      Solved: solved
    }))

    const strongestTopicsData = (performance.strongestTopics || []).map(t => ({
      name: `${t.topicName} (${t.problemsSolved || 0} solved)`,
      Score: t.strengthScore
    }))

    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
        <div>
          <h3 style={{ fontSize: '22px', fontWeight: 800, margin: 0, letterSpacing: '-0.02em' }}>
            Algorithmic Analytics & Strengths
          </h3>
          <p style={{ color: '#94a3b8', fontSize: '14px', marginTop: '4px' }}>
            Deep-dive into difficulty distribution, cross-platform volume, and DSA pattern mastery.
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '20px' }}>
          {/* Difficulty breakdown */}
          <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h4 style={{ fontSize: '15px', fontWeight: 700, margin: 0 }}>Difficulty Breakdown</h4>
              <span className="badge badge-emerald" style={{ fontSize: '10px' }}>Categorized</span>
            </div>
            <div style={{ height: '220px' }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={difficultyData}
                    cx="50%"
                    cy="50%"
                    innerRadius={65}
                    outerRadius={88}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {difficultyData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} stroke="rgba(0,0,0,0.3)" strokeWidth={2} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.95)', border: '1px solid rgba(99, 102, 241, 0.3)', borderRadius: '10px', color: '#ffffff' }}
                    formatter={(value) => [`${value} problems`, 'Solved']}
                  />
                  <Legend verticalAlign="bottom" height={36} iconType="circle" />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Platform comparison */}
          <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h4 style={{ fontSize: '15px', fontWeight: 700, margin: 0 }}>Solved Counts by Platform</h4>
              <span className="badge badge-indigo" style={{ fontSize: '10px' }}>Aggregated</span>
            </div>
            <div style={{ height: '220px' }}>
              {platformComparisonData.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={platformComparisonData}>
                    <XAxis dataKey="name" stroke="#94a3b8" fontSize={11} tickLine={false} />
                    <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} />
                    <Tooltip
                      cursor={{ fill: 'rgba(99, 102, 241, 0.08)' }}
                      contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.95)', border: '1px solid rgba(99, 102, 241, 0.3)', borderRadius: '10px', color: '#ffffff' }}
                    />
                    <Bar dataKey="Solved" fill="#6366f1" radius={[6, 6, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div style={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}>No platform data available</div>
              )}
            </div>
          </div>
        </div>

        {/* Skill areas */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '20px' }}>
          <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h4 style={{ fontSize: '15px', fontWeight: 700, margin: 0 }}>DSA Topic Strengths</h4>
              <span className="badge badge-cyan" style={{ fontSize: '10px' }}>Strength Index</span>
            </div>
            <div style={{ height: '360px' }}>
              {strongestTopicsData.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={strongestTopicsData} layout="vertical">
                    <XAxis type="number" domain={[0, 100]} stroke="#94a3b8" fontSize={11} tickLine={false} />
                    <YAxis dataKey="name" type="category" stroke="#94a3b8" fontSize={11} width={150} tickLine={false} />
                    <Tooltip contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.95)', border: '1px solid rgba(16, 185, 129, 0.3)', borderRadius: '10px', color: '#ffffff' }} />
                    <Bar dataKey="Score" fill="#10b981" radius={[0, 6, 6, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div style={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}>No topic statistics tracked yet</div>
              )}
            </div>
          </div>
 
          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <h4 style={{ fontSize: '15px', fontWeight: 700, margin: 0 }}>Topic Gaps & Priorities</h4>
              <span className="badge badge-rose" style={{ fontSize: '10px' }}>Action Items</span>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {insights?.skillGaps && insights.skillGaps.length > 0 ? (
                insights.skillGaps.map((gap, index) => (
                  <div key={index} style={{ display: 'flex', flexDirection: 'column', gap: '6px', padding: '12px 14px', borderRadius: '12px', background: 'rgba(255, 255, 255, 0.02)', border: '1px solid rgba(255, 255, 255, 0.06)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <span style={{ fontSize: '14.5px', fontWeight: 700, color: '#ffffff' }}>{gap.topic}</span>
                        <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginTop: '4px' }}>
                          <span className={gap.priority === 'HIGH' ? 'badge badge-rose' : gap.priority === 'MEDIUM' ? 'badge badge-amber' : 'badge badge-emerald'} style={{ fontSize: '9px', padding: '1px 6px' }}>
                            {gap.priority} PRIORITY
                          </span>
                          <span style={{ fontSize: '11px', color: '#94a3b8' }}>Target: {gap.estimatedDaysToTarget} days</span>
                        </div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <span style={{ fontSize: '14px', fontWeight: 800, color: '#ffffff' }}>{gap.currentScore} / {gap.targetScore}</span>
                        <span style={{ fontSize: '10.5px', color: '#94a3b8', display: 'block' }}>Score</span>
                      </div>
                    </div>
                    
                    {/* Visual Progress Bar */}
                    <div style={{ width: '100%', height: '6px', backgroundColor: 'rgba(255, 255, 255, 0.08)', borderRadius: '9999px', overflow: 'hidden', marginTop: '6px' }}>
                      <div style={{ 
                        width: `${Math.min(100, Math.round((gap.currentScore / gap.targetScore) * 100))}%`, 
                        height: '100%', 
                        background: gap.priority === 'HIGH' ? 'linear-gradient(90deg, #ef4444, #f43f5e)' : gap.priority === 'MEDIUM' ? 'linear-gradient(90deg, #f59e0b, #fbbf24)' : 'linear-gradient(90deg, #10b981, #34d399)',
                        borderRadius: '9999px',
                        transition: 'width 0.5s ease-in-out'
                      }} />
                    </div>
                  </div>
                ))
              ) : (
                <div style={{ padding: '30px', textAlign: 'center', color: '#94a3b8' }}>
                  <CheckCircle2 size={32} color="#10b981" style={{ margin: '0 auto 10px auto' }} />
                  <p style={{ fontSize: '14px' }}>Excellent work! No critical skill gaps registered.</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    )
  }

  const renderStudyPlan = () => {
    // Group study plans by week
    const weeks: Record<number, StudyPlanTask[]> = {}
    studyPlans.forEach(task => {
      if (!weeks[task.weekNumber]) {
        weeks[task.weekNumber] = []
      }
      weeks[task.weekNumber].push(task)
    })

    return (
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px', gap: '28px', alignItems: 'start' }}>
        {/* Left Side: Tasks */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <h3 style={{ fontSize: '22px', fontWeight: 800, margin: 0, letterSpacing: '-0.02em' }}>
              Adaptive Preparation Roadmap
            </h3>
            <p style={{ color: '#94a3b8', fontSize: '14px', marginTop: '4px' }}>
              Personalized syllabus grounded in your detected weaknesses via PostgreSQL pgvector.
            </p>
          </div>

          {formSuccess && <div className="alert alert-success">{formSuccess}</div>}

          {(streamingPlan || streamedPlanText) && (
            <div className="ai-stream-box" style={{ marginBottom: '10px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', borderBottom: '1px solid rgba(255, 255, 255, 0.08)', paddingBottom: '10px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <span className="pulse-dot"></span>
                  <strong style={{ color: '#a5b4fc', fontSize: '13px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                    {streamingPlan ? 'Neon pgvector RAG + Real-Time SSE Stream (Sub-50ms)' : 'Generated Study Plan Stream'}
                  </strong>
                </div>
                <span className="badge badge-purple" style={{ fontSize: '10px' }}>
                  <Database size={10} /> Ground Truth RAG
                </span>
              </div>
              <div style={{ fontSize: '15px', lineHeight: 1.7, color: '#f8fafc' }}>
                {renderFormattedMarkdown(streamedPlanText)}
                {streamingPlan && <span style={{ display: 'inline-block', width: '2px', height: '18px', background: '#818cf8', marginLeft: '4px', verticalAlign: 'middle', animation: 'blink 1s infinite' }}>|</span>}
              </div>
            </div>
          )}

          {Object.keys(weeks).length === 0 ? (
            <div className="card" style={{ padding: '50px 24px', textAlign: 'center', background: 'rgba(15, 23, 42, 0.65)' }}>
              <div style={{ width: '54px', height: '54px', borderRadius: '16px', background: 'rgba(99, 102, 241, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px auto' }}>
                <BookOpen size={24} color="#818cf8" />
              </div>
              <h4 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '6px' }}>No active study plan</h4>
              <p style={{ color: '#94a3b8', fontSize: '14px', maxWidth: '420px', margin: '0 auto 16px auto' }}>
                Select your target company and weeks on the generator panel to generate a syllabus.
              </p>
            </div>
          ) : (
            Object.entries(weeks).map(([week, tasks]) => {
              const completedCount = tasks.filter(t => t.status === 'COMPLETED').length
              const progressPct = tasks.length > 0 ? Math.round((completedCount / tasks.length) * 100) : 0

              return (
                <div key={week} className="card" style={{ padding: '22px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid rgba(255, 255, 255, 0.08)', paddingBottom: '12px', marginBottom: '16px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <span className="badge badge-indigo">Week {week}</span>
                      <span style={{ fontSize: '13.5px', color: '#94a3b8' }}>{completedCount} of {tasks.length} tasks completed</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <div style={{ width: '80px', height: '6px', background: 'rgba(255,255,255,0.08)', borderRadius: '999px', overflow: 'hidden' }}>
                        <div style={{ width: `${progressPct}%`, height: '100%', background: '#10b981', borderRadius: '999px' }} />
                      </div>
                      <span style={{ fontSize: '12px', fontWeight: 700, color: progressPct === 100 ? '#10b981' : '#a5b4fc' }}>{progressPct}%</span>
                    </div>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {tasks.map(task => (
                      <div
                        key={task.id}
                        onClick={() => handleToggleTask(task.id)}
                        style={{
                          display: 'flex',
                          gap: '14px',
                          alignItems: 'flex-start',
                          cursor: 'pointer',
                          padding: '10px 12px',
                          borderRadius: '10px',
                          background: task.status === 'COMPLETED' ? 'rgba(16, 185, 129, 0.05)' : 'rgba(255, 255, 255, 0.02)',
                          border: task.status === 'COMPLETED' ? '1px solid rgba(16, 185, 129, 0.2)' : '1px solid rgba(255, 255, 255, 0.04)',
                          transition: 'all 0.2s ease'
                        }}
                      >
                        <div style={{
                          width: '20px',
                          height: '20px',
                          borderRadius: '6px',
                          marginTop: '2px',
                          border: task.status === 'COMPLETED' ? '2px solid #10b981' : '2px solid rgba(255, 255, 255, 0.25)',
                          background: task.status === 'COMPLETED' ? '#10b981' : 'transparent',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          flexShrink: 0
                        }}>
                          {task.status === 'COMPLETED' && <Check size={13} color="#ffffff" strokeWidth={3} />}
                        </div>
                        <div style={{ textDecoration: task.status === 'COMPLETED' ? 'line-through' : 'none', opacity: task.status === 'COMPLETED' ? 0.7 : 1 }}>
                          <span style={{ fontSize: '12.5px', fontWeight: 700, color: '#a5b4fc', display: 'block', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                            {task.topicName}
                          </span>
                          <p style={{ fontSize: '14px', color: '#f8fafc', margin: '2px 0 0 0', lineHeight: 1.5 }}>
                            {task.taskDescription}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )
            })
          )}
        </div>

        {/* Right Side: Generator Form */}
        <div>
          <div className="card" style={{ position: 'sticky', top: '90px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
              <Zap size={18} color="#818cf8" />
              <h4 style={{ fontSize: '16px', margin: 0, fontWeight: 700 }}>Generate Plan</h4>
            </div>
            <form onSubmit={handleGenerateStudyPlan} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label htmlFor="planCompany">Target Company Tier</label>
                <select
                  id="planCompany"
                  value={planCompany}
                  onChange={(e) => setPlanCompany(e.target.value)}
                >
                  <option value="Google">Google (Graph & DP Focus)</option>
                  <option value="Amazon">Amazon (Trees & Scalability)</option>
                  <option value="Microsoft">Microsoft (Arrays & Strings)</option>
                  <option value="Meta">Meta (Speed & Core Algorithms)</option>
                  <option value="Apple">Apple (Concurrency & Systems)</option>
                </select>
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label htmlFor="planWeeks">Duration (Weeks)</label>
                <input
                  id="planWeeks"
                  type="number"
                  min="2"
                  max="24"
                  value={planWeeks}
                  onChange={(e) => setPlanWeeks(parseInt(e.target.value))}
                  required
                />
              </div>

              <button type="submit" className="button button-primary btn-shimmer" disabled={planLoading} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', height: '46px' }}>
                {planLoading ? (
                  <>
                    <span className="spinner" style={{ width: '16px', height: '16px', borderWidth: '2px' }}></span>
                    <span>Streaming RAG Plan...</span>
                  </>
                ) : (
                  <>
                    <Sparkles size={16} />
                    <span>Generate AI Plan (SSE)</span>
                  </>
                )}
              </button>
            </form>
          </div>
        </div>
      </div>
    )
  }

  const renderRecommendations = () => {
    const latestRecommendation = recommendations[0]

    return (
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px', gap: '28px', alignItems: 'start' }}>
        {/* Left Side: Recommendations Detail */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <h3 style={{ fontSize: '22px', fontWeight: 800, margin: 0, letterSpacing: '-0.02em' }}>
              AI Problem Recommendations
            </h3>
            <p style={{ color: '#94a3b8', fontSize: '14px', marginTop: '4px' }}>
              Cosine similarity vector search against 30+ ground-truth problems via Neon pgvector.
            </p>
          </div>

          {formSuccess && <div className="alert alert-success">{formSuccess}</div>}

          {(streamingRec || streamedRecText) && (
            <div className="ai-stream-box" style={{ marginBottom: '10px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', borderBottom: '1px solid rgba(255, 255, 255, 0.08)', paddingBottom: '10px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <span className="pulse-dot"></span>
                  <strong style={{ color: '#a5b4fc', fontSize: '13px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                    {streamingRec ? 'Neon pgvector RAG + Real-Time AI Stream Active...' : 'Latest Live Generation'}
                  </strong>
                </div>
                <span className="badge badge-indigo" style={{ fontSize: '10px' }}>
                  <Database size={10} /> 1536-dim Embedding
                </span>
              </div>
              <div style={{ fontSize: '15.5px', lineHeight: 1.7, color: '#f8fafc' }}>
                {renderFormattedMarkdown(streamedRecText)}
                {streamingRec && <span style={{ display: 'inline-block', width: '2px', height: '18px', background: '#818cf8', marginLeft: '4px', verticalAlign: 'middle', animation: 'blink 1s infinite' }}>|</span>}
              </div>
            </div>
          )}

          {!latestRecommendation && !streamingRec && !streamedRecText ? (
            <div className="card" style={{ padding: '50px 24px', textAlign: 'center', background: 'rgba(15, 23, 42, 0.65)' }}>
              <div style={{ width: '54px', height: '54px', borderRadius: '16px', background: 'rgba(168, 85, 247, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px auto' }}>
                <Sparkles size={24} color="#a855f7" />
              </div>
              <h4 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '6px' }}>No recommendations active</h4>
              <p style={{ color: '#94a3b8', fontSize: '14px', maxWidth: '420px', margin: '0 auto 16px auto' }}>
                Trigger grounded AI insights using the focus selector panel on the right.
              </p>
            </div>
          ) : (
            <div className="card" style={{ padding: '28px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid rgba(255, 255, 255, 0.08)', paddingBottom: '16px', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                    <span className="badge badge-purple">Focus Goal</span>
                    <h4 style={{ fontSize: '19px', fontWeight: 800, margin: 0, color: '#ffffff' }}>{latestRecommendation.targetCompany}</h4>
                  </div>
                  <span style={{ fontSize: '12px', color: '#94a3b8' }}>Synthesized on {formatDate(latestRecommendation.generatedAt)}</span>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <span style={{ fontSize: '11px', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.04em', display: 'block' }}>DSA Readiness</span>
                  <span style={{ fontSize: '24px', fontWeight: 800, color: '#67e8f9' }}>{latestRecommendation.interviewReadiness}%</span>
                </div>
              </div>
              <div style={{ fontSize: '15.5px', lineHeight: 1.7, color: '#f8fafc' }}>
                {renderFormattedMarkdown(latestRecommendation.recommendationText)}
              </div>
            </div>
          )}
        </div>

        {/* Right Side: Generator Form */}
        <div>
          <div className="card" style={{ position: 'sticky', top: '90px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
              <Sparkles size={18} color="#c084fc" />
              <h4 style={{ fontSize: '16px', margin: 0, fontWeight: 700 }}>Focus Analysis</h4>
            </div>
            <form onSubmit={handleGenerateRecommendations} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label htmlFor="recCompany">Select Goal / Pattern</label>
                <select
                  id="recCompany"
                  value={recCompany}
                  onChange={(e) => setRecCompany(e.target.value)}
                >
                  <option value="Core DSA Mastery">Core DSA Mastery</option>
                  <option value="Dynamic Programming & Graphs">Dynamic Programming & Graphs</option>
                  <option value="Problem Solving Speed & Accuracy">Speed & Accuracy</option>
                  <option value="Competitive Programming & Contests">Competitive Programming</option>
                  <option value="Advanced LLD & OOP Design">LLD & OOP Design</option>
                </select>
              </div>

              <button type="submit" className="button button-primary btn-shimmer" disabled={recLoading} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', height: '46px' }}>
                {recLoading ? (
                  <>
                    <span className="spinner" style={{ width: '16px', height: '16px', borderWidth: '2px' }}></span>
                    <span>Querying pgvector...</span>
                  </>
                ) : (
                  <>
                    <Zap size={16} />
                    <span>Run AI Recommendation</span>
                  </>
                )}
              </button>
            </form>
          </div>
        </div>
      </div>
    )
  }

  const renderPlatforms = () => {
    const platformList = [
      { id: 'leetcode', name: 'LeetCode', username: accounts?.leetcodeUsername, color: '#ffa116', badge: 'LC', solved: metrics?.platformBreakdown?.['leetcode']?.totalSolved, streak: metrics?.platformBreakdown?.['leetcode']?.currentStreak },
      { id: 'geeksforgeeks', name: 'GeeksforGeeks', username: accounts?.geeksforgeeksUsername, color: '#10b981', badge: 'GFG', solved: metrics?.platformBreakdown?.['geeksforgeeks']?.totalSolved, streak: metrics?.platformBreakdown?.['geeksforgeeks']?.currentStreak },
      { id: 'codechef', name: 'CodeChef', username: accounts?.codechefUsername, color: '#c084fc', badge: 'CC', solved: metrics?.platformBreakdown?.['codechef']?.totalSolved, streak: metrics?.platformBreakdown?.['codechef']?.currentStreak },
      { id: 'github', name: 'GitHub', username: accounts?.githubUsername, color: '#a5b4fc', badge: 'GH', solved: metrics?.platformBreakdown?.['github']?.totalSolved, streak: metrics?.platformBreakdown?.['github']?.currentStreak }
    ]

    return (
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px', gap: '28px', alignItems: 'start' }}>
        {/* Left Side: Connected Platforms Listing */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <h3 style={{ fontSize: '22px', fontWeight: 800, margin: 0, letterSpacing: '-0.02em' }}>
              Platform Integrations
            </h3>
            <p style={{ color: '#94a3b8', fontSize: '14px', marginTop: '4px' }}>
              Connect and aggregate your competitive programming and development profiles.
            </p>
          </div>

          {formSuccess && <div className="alert alert-success">{formSuccess}</div>}
          {formError && <div className="alert alert-error">{formError}</div>}

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px' }}>
            {platformList.map((p) => (
              <div key={p.id} className="card" style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <div style={{ width: '38px', height: '38px', borderRadius: '10px', background: `rgba(255, 255, 255, 0.06)`, border: `1px solid rgba(255, 255, 255, 0.1)`, display: 'flex', alignItems: 'center', justifyContent: 'center', color: p.color, fontWeight: 800, fontSize: '14px' }}>
                      {p.badge}
                    </div>
                    <div>
                      <span style={{ fontSize: '15px', fontWeight: 700, color: '#ffffff', display: 'block' }}>{p.name}</span>
                      <span style={{ fontSize: '12px', color: p.username ? '#a5b4fc' : '#64748b' }}>
                        {p.username ? `@${p.username}` : 'Not connected'}
                      </span>
                    </div>
                  </div>
                  <span className={p.username ? 'badge badge-emerald' : 'badge'} style={!p.username ? { background: 'rgba(255,255,255,0.05)', color: '#64748b', fontSize: '9px' } : { fontSize: '9px' }}>
                    {p.username ? 'CONNECTED' : 'UNLINKED'}
                  </span>
                </div>

                {p.username ? (
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: '10px', borderTop: '1px solid rgba(255, 255, 255, 0.06)', fontSize: '12.5px' }}>
                    <span style={{ color: '#94a3b8' }}>
                      Solved: <strong style={{ color: '#ffffff' }}>{p.solved ?? 0}</strong>
                    </span>
                    <span style={{ color: '#f59e0b', fontWeight: 600 }}>
                      🔥 {p.streak ?? 0}d streak
                    </span>
                  </div>
                ) : (
                  <div style={{ paddingTop: '10px', borderTop: '1px solid rgba(255, 255, 255, 0.06)' }}>
                    <span style={{ fontSize: '12px', color: '#64748b' }}>Use the connect form to link this account.</span>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Right Side: Connect Form */}
        <div>
          <div className="card" style={{ position: 'sticky', top: '90px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
              <Link2 size={18} color="#818cf8" />
              <h4 style={{ fontSize: '16px', margin: 0, fontWeight: 700 }}>Link Coding Account</h4>
            </div>
            <form onSubmit={handleConnect} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label htmlFor="connectPlatform">Select Platform</label>
                <select
                  id="connectPlatform"
                  value={connectPlatform}
                  onChange={(e) => setConnectPlatform(e.target.value)}
                >
                  <option value="leetcode">LeetCode</option>
                  <option value="geeksforgeeks">GeeksforGeeks</option>
                  <option value="codechef">CodeChef</option>
                  <option value="github">GitHub</option>
                </select>
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label htmlFor="connectUsername">Profile Username</label>
                <input
                  id="connectUsername"
                  type="text"
                  placeholder="e.g. touring_alex"
                  value={connectUsername}
                  onChange={(e) => setConnectUsername(e.target.value)}
                  required
                />
              </div>

              <button type="submit" className="button button-primary btn-shimmer" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', height: '46px' }}>
                <CheckCircle2 size={16} />
                <span>Link & Sync Profile</span>
              </button>
            </form>
          </div>
        </div>
      </div>
    )
  }

  const renderSettings = () => {
    return (
      <div style={{ display: 'grid', gridTemplateColumns: '360px 1fr', gap: '28px', alignItems: 'start' }}>
        <div className="card" style={{ padding: '28px' }}>
          <h4 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '20px', borderBottom: '1px solid rgba(255, 255, 255, 0.08)', paddingBottom: '10px' }}>
            Profile Overview
          </h4>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '14px', textAlign: 'center', marginBottom: '24px' }}>
            <div style={{ position: 'relative' }}>
              <img
                src={profilePhoto || "https://api.dicebear.com/7.x/bottts/svg?seed=" + (user?.name || "default")}
                alt="Profile Avatar"
                style={{ width: '96px', height: '96px', borderRadius: '50%', objectFit: 'cover', border: '3px solid #6366f1', boxShadow: '0 0 20px rgba(99, 102, 241, 0.35)', backgroundColor: '#0f172a' }}
                onError={(e) => {
                  (e.target as HTMLImageElement).src = "https://api.dicebear.com/7.x/bottts/svg?seed=" + (user?.name || "default")
                }}
              />
            </div>
            <div>
              <h4 style={{ fontSize: '18px', fontWeight: 700, margin: '2px 0', color: '#ffffff' }}>{user?.name}</h4>
              <span style={{ fontSize: '13px', color: '#94a3b8' }}>{user?.email}</span>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', borderTop: '1px solid rgba(255, 255, 255, 0.08)', paddingTop: '18px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <GraduationCap size={16} color="#818cf8" />
              <div>
                <span style={{ fontSize: '11px', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.04em', display: 'block' }}>College</span>
                <span style={{ fontSize: '13.5px', fontWeight: 600, color: user?.collegeName ? '#ffffff' : '#64748b' }}>
                  {user?.collegeName || 'Not configured'}
                </span>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Phone size={16} color="#818cf8" />
              <div>
                <span style={{ fontSize: '11px', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.04em', display: 'block' }}>Contact</span>
                <span style={{ fontSize: '13.5px', fontWeight: 600, color: user?.contactNumber ? '#ffffff' : '#64748b' }}>
                  {user?.contactNumber || 'Not configured'}
                </span>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <FileText size={16} color="#818cf8" />
              <div>
                <span style={{ fontSize: '11px', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.04em', display: 'block' }}>Resume Link</span>
                {user?.resumeUrl ? (
                  <a href={user.resumeUrl} target="_blank" rel="noopener noreferrer" style={{ fontSize: '13.5px', fontWeight: 600, color: '#38bdf8', display: 'inline-flex', alignItems: 'center', gap: '4px', textDecoration: 'none' }}>
                    <span>View Resume</span>
                    <ExternalLink size={12} />
                  </a>
                ) : (
                  <span style={{ fontSize: '13.5px', color: '#64748b' }}>Not linked</span>
                )}
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Clock size={16} color="#818cf8" />
              <div>
                <span style={{ fontSize: '11px', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.04em', display: 'block' }}>Member Since</span>
                <span style={{ fontSize: '13.5px', fontWeight: 600, color: '#ffffff' }}>
                  {user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'Active'}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className="card" style={{ padding: '28px' }}>
          <h4 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '20px', borderBottom: '1px solid rgba(255, 255, 255, 0.08)', paddingBottom: '10px' }}>
            Edit Profile Information
          </h4>
          
          {profileMessage && (
            <div className={profileMessage.startsWith('Error') ? 'alert alert-error' : 'alert alert-success'} style={{ padding: '10px 14px', fontSize: '13.5px' }}>
              {profileMessage}
            </div>
          )}

          <form onSubmit={handleUpdateProfile} style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label htmlFor="profileName" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <User size={13} color="#818cf8" /> Full Name
              </label>
              <input
                id="profileName"
                type="text"
                value={profileName}
                onChange={(e) => setProfileName(e.target.value)}
                placeholder="Your Full Name"
                required
              />
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label htmlFor="collegeName" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <GraduationCap size={13} color="#818cf8" /> College / University
              </label>
              <input
                id="collegeName"
                type="text"
                value={collegeName}
                onChange={(e) => setCollegeName(e.target.value)}
                placeholder="Enter College or University"
              />
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label htmlFor="contactNumber" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Phone size={13} color="#818cf8" /> Phone / Contact Number
              </label>
              <input
                id="contactNumber"
                type="text"
                value={contactNumber}
                onChange={(e) => setContactNumber(e.target.value)}
                placeholder="+1 (555) 000-0000"
              />
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label htmlFor="profilePhoto" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Sparkles size={13} color="#818cf8" /> Profile Photo URL (or Leave for DiceBear avatar)
              </label>
              <input
                id="profilePhoto"
                type="text"
                value={profilePhoto}
                onChange={(e) => setProfilePhoto(e.target.value)}
                placeholder="https://..."
              />
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label htmlFor="resumeUrl" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <FileText size={13} color="#818cf8" /> Resume Link (Google Drive / Dropbox)
              </label>
              <input
                id="resumeUrl"
                type="text"
                value={resumeUrl}
                onChange={(e) => setResumeUrl(e.target.value)}
                placeholder="https://drive.google.com/..."
              />
            </div>

            <button type="submit" className="button button-primary btn-shimmer" disabled={profileLoading} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', height: '46px', marginTop: '6px' }}>
              {profileLoading ? (
                <>
                  <span className="spinner" style={{ width: '16px', height: '16px', borderWidth: '2px' }}></span>
                  <span>Saving Changes...</span>
                </>
              ) : (
                <>
                  <CheckCircle2 size={16} />
                  <span>Save Profile Changes</span>
                </>
              )}
            </button>
          </form>
        </div>
      </div>
    )
  }

  const renderContent = () => {
    switch (activeTab) {
      case 'dashboard':
        return renderDashboardOverview()
      case 'analytics':
        return renderAnalytics()
      case 'study-plan':
        return renderStudyPlan()
      case 'recommendations':
        return renderRecommendations()
      case 'platforms':
        return renderPlatforms()
      case 'settings':
        return renderSettings()
      default:
        return renderDashboardOverview()
    }
  }

  return (
    <div className="dashboard">
      <nav className="navbar">
        <div className="navbar-content">
          <div className="brand-container">
            <div className="brand-icon">
              <Terminal size={20} color="#ffffff" />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <h1 style={{ margin: 0, lineHeight: 1.1 }}>CodeInsight.AI</h1>
                <span className="navbar-badge" style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                  <span className="pulse-dot"></span>
                  pgvector RAG
                </span>
              </div>
            </div>
          </div>
          <div className="navbar-right">
            <div className="user-badge">
              <img
                src={profilePhoto || "https://api.dicebear.com/7.x/bottts/svg?seed=" + (user?.name || "default")}
                alt="Avatar"
                className="user-avatar-mini"
                onError={(e) => {
                  (e.target as HTMLImageElement).src = "https://api.dicebear.com/7.x/bottts/svg?seed=" + (user?.name || "default")
                }}
              />
              <span className="user-name">{user?.name || 'Developer'}</span>
            </div>
            <button onClick={handleLogout} className="button button-secondary" style={{ padding: '7px 14px', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <LogOut size={14} />
              <span>Logout</span>
            </button>
          </div>
        </div>
      </nav>

      <div className="dashboard-container">
        <aside className="sidebar">
          <nav className="sidebar-nav">
            <div className="nav-section-title">INTELLIGENCE</div>
            <button
              onClick={() => { setActiveTab('dashboard'); setFormSuccess(null); setFormError(null); }}
              className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`}
              style={{ border: 'none', background: 'none', width: '100%', textAlign: 'left', cursor: 'pointer' }}
            >
              <div className="nav-item-content">
                <LayoutDashboard size={17} />
                <span>Dashboard</span>
              </div>
            </button>
            <button
              onClick={() => { setActiveTab('analytics'); setFormSuccess(null); setFormError(null); }}
              className={`nav-item ${activeTab === 'analytics' ? 'active' : ''}`}
              style={{ border: 'none', background: 'none', width: '100%', textAlign: 'left', cursor: 'pointer' }}
            >
              <div className="nav-item-content">
                <TrendingUp size={17} />
                <span>Analytics</span>
              </div>
            </button>

            <div className="nav-section-title" style={{ marginTop: '14px' }}>AI ACCELERATOR</div>
            <button
              onClick={() => { setActiveTab('study-plan'); setFormSuccess(null); setFormError(null); }}
              className={`nav-item ${activeTab === 'study-plan' ? 'active' : ''}`}
              style={{ border: 'none', background: 'none', width: '100%', textAlign: 'left', cursor: 'pointer' }}
            >
              <div className="nav-item-content">
                <BookOpen size={17} />
                <span>Study Plan</span>
              </div>
              <span className="badge badge-indigo" style={{ fontSize: '9.5px', padding: '2px 6px' }}>AI</span>
            </button>
            <button
              onClick={() => { setActiveTab('recommendations'); setFormSuccess(null); setFormError(null); }}
              className={`nav-item ${activeTab === 'recommendations' ? 'active' : ''}`}
              style={{ border: 'none', background: 'none', width: '100%', textAlign: 'left', cursor: 'pointer' }}
            >
              <div className="nav-item-content">
                <Sparkles size={17} />
                <span>Recommendations</span>
              </div>
              <span className="badge badge-purple" style={{ fontSize: '9.5px', padding: '2px 6px' }}>RAG</span>
            </button>

            <div className="nav-section-title" style={{ marginTop: '14px' }}>CONFIGURATION</div>
            <button
              onClick={() => { setActiveTab('platforms'); setFormSuccess(null); setFormError(null); }}
              className={`nav-item ${activeTab === 'platforms' ? 'active' : ''}`}
              style={{ border: 'none', background: 'none', width: '100%', textAlign: 'left', cursor: 'pointer' }}
            >
              <div className="nav-item-content">
                <Link2 size={17} />
                <span>Platforms</span>
              </div>
            </button>
            <button
              onClick={() => { setActiveTab('settings'); setFormSuccess(null); setFormError(null); }}
              className={`nav-item ${activeTab === 'settings' ? 'active' : ''}`}
              style={{ border: 'none', background: 'none', width: '100%', textAlign: 'left', cursor: 'pointer' }}
            >
              <div className="nav-item-content">
                <Settings size={17} />
                <span>Settings</span>
              </div>
            </button>
          </nav>

          <div className="sidebar-status-card">
            <div className="sidebar-status-header">
              <Cpu size={13} color="#818cf8" />
              <span>Engine Status</span>
            </div>
            <div className="sidebar-status-item">
              <span>pgvector RAG</span>
              <span className="badge badge-emerald" style={{ fontSize: '9px', padding: '1px 6px' }}>ONLINE</span>
            </div>
            <div className="sidebar-status-item">
              <span>SSE Stream</span>
              <span className="badge badge-cyan" style={{ fontSize: '9px', padding: '1px 6px' }}>ACTIVE</span>
            </div>
            <div className="sidebar-status-item">
              <span>Bucket4j Rate Limit</span>
              <span className="badge badge-indigo" style={{ fontSize: '9px', padding: '1px 6px' }}>SECURE</span>
            </div>
          </div>
        </aside>

        <main className="main-content">
          {renderContent()}
        </main>
      </div>
    </div>
  )
}

export default DashboardPage
