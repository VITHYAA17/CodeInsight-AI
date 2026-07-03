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
import './Dashboard.css'

interface CodingAccount {
  leetcodeUsername?: string | null
  codeforcesUsername?: string | null
  codechefUsername?: string | null
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

  const [recCompany, setRecCompany] = useState<string>('Core DSA Mastery')
  const [recLoading, setRecLoading] = useState<boolean>(false)

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
          <h4 key={index} style={{ fontSize: '18px', fontWeight: 700, color: '#ffffff', marginTop: '20px', marginBottom: '10px' }}>
            {parseInlineStyles(trimmed.substring(4))}
          </h4>
        );
      }
      
      if (trimmed.startsWith('#### ')) {
        return (
          <h5 key={index} style={{ fontSize: '15px', fontWeight: 700, color: '#a5b4fc', marginTop: '16px', marginBottom: '8px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            {parseInlineStyles(trimmed.substring(5))}
          </h5>
        );
      }
      
      if (trimmed.startsWith('- ') || trimmed.startsWith('* ')) {
        const content = trimmed.substring(2);
        return (
          <div key={index} style={{ display: 'flex', gap: '8px', marginLeft: '12px', marginBottom: '6px', lineHeight: 1.6 }}>
            <span style={{ color: '#6366f1' }}>•</span>
            <span style={{ fontSize: '14px', color: '#cbd5e1' }}>{parseInlineStyles(content)}</span>
          </div>
        );
      }
      
      const numMatch = trimmed.match(/^(\d+)\.\s(.*)/);
      if (numMatch) {
        const num = numMatch[1];
        const content = numMatch[2];
        return (
          <div key={index} style={{ display: 'flex', gap: '8px', marginLeft: '12px', marginBottom: '10px', alignItems: 'flex-start', lineHeight: 1.6 }}>
            <span style={{ color: '#818cf8', fontWeight: 600, minWidth: '18px' }}>{num}.</span>
            <span style={{ fontSize: '14px', color: '#cbd5e1' }}>{parseInlineStyles(content)}</span>
          </div>
        );
      }
      
      return (
        <p key={index} style={{ fontSize: '14px', color: '#cbd5e1', lineHeight: 1.6, marginBottom: '12px' }}>
          {parseInlineStyles(trimmed)}
        </p>
      );
    });
  };

  const fetchData = async () => {
    setLoading(true)
    try {
      const [
        accountsRes,
        metricsRes,
        performanceRes,
        insightsRes,
        studyPlanRes,
        recommendationRes
      ] = await Promise.all([
        platformAPI.getAccounts(),
        analyticsAPI.getMetrics(),
        analyticsAPI.getPerformance(),
        analyticsAPI.getInsights(),
        aiAPI.getStudyPlan(),
        aiAPI.getRecommendations()
      ])

      setAccounts(accountsRes.data)
      setMetrics(metricsRes.data)
      setPerformance(performanceRes.data)
      setInsights(insightsRes.data)
      setStudyPlans(studyPlanRes.data.data || [])
      setRecommendations(recommendationRes.data.data || [])
    } catch (err) {
      console.error('Error fetching dashboard data:', err)
    } finally {
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
    setFormError(null)
    setFormSuccess(null)
    try {
      const response = await aiAPI.generateStudyPlan(planCompany, planWeeks)
      setFormSuccess(response.data.message || 'Study plan generated!')
      // Refresh study plans
      const studyPlanRes = await aiAPI.getStudyPlan()
      setStudyPlans(studyPlanRes.data.data || [])
      setActiveTab('study-plan')
    } catch (err: any) {
      setFormError(err.response?.data?.message || 'Failed to generate study plan')
    } finally {
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
    setFormError(null)
    setFormSuccess(null)
    try {
      const response = await aiAPI.generateRecommendations(recCompany)
      setFormSuccess(response.data.message || 'Recommendations generated!')
      // Refresh recommendations
      const recommendationRes = await aiAPI.getRecommendations()
      setRecommendations(recommendationRes.data.data || [])
      setActiveTab('recommendations')
    } catch (err: any) {
      setFormError(err.response?.data?.message || 'Failed to generate recommendations')
    } finally {
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
      <div style={{ display: 'flex', height: '100vh', justifyContent: 'center', alignItems: 'center', backgroundColor: 'var(--bg-app)' }}>
        <div style={{ textAlign: 'center' }}>
          <div className="spinner" style={{ width: '40px', height: '40px', borderWidth: '3px' }}></div>
          <p style={{ marginTop: '16px', color: '#94a3b8', fontSize: '14px', fontWeight: 500 }}>Loading CodeInsight.AI Dashboard...</p>
        </div>
      </div>
    )
  }

  // Render sub-components
  const renderDashboardOverview = () => {
    if (!metrics) return <p>No statistics connected. Please connect accounts in the "Platforms" tab.</p>

    const hasAccounts = accounts && (accounts.leetcodeUsername || accounts.codeforcesUsername || accounts.codechefUsername || accounts.githubUsername)

    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h3 style={{ fontSize: '20px', fontWeight: 600 }}>Overview</h3>
            <p style={{ color: '#64748b', fontSize: '14px' }}>Aggregated statistics across all connected coding environments.</p>
          </div>
          {hasAccounts && (
            <button className="button button-secondary" onClick={handleSyncAll} disabled={syncing}>
              {syncing ? 'Syncing...' : '🔄 Sync Profiles'}
            </button>
          )}
        </div>

        {formSuccess && <div className="alert alert-success">{formSuccess}</div>}

        {!hasAccounts ? (
          <div className="card" style={{ textAlign: 'center', padding: '40px 20px' }}>
            <h4 style={{ fontSize: '18px', marginBottom: '10px' }}>No platforms connected yet</h4>
            <p style={{ color: '#64748b', fontSize: '14px', marginBottom: '20px' }}>Connect your profiles to aggregate your analytics and trigger AI plans.</p>
            <button className="button button-primary" onClick={() => setActiveTab('platforms')}>Connect Platforms</button>
          </div>
        ) : (
          <>
            {/* Stats Summary Cards Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px' }}>
              <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <span style={{ fontSize: '13px', color: '#64748b', fontWeight: 500 }}>Total Solved</span>
                <span style={{ fontSize: '28px', fontWeight: 700 }}>{metrics.totalProblems}</span>
                <span style={{ fontSize: '12px', color: '#10b981', fontWeight: 500 }}>Active solved count</span>
              </div>
              <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <span style={{ fontSize: '13px', color: '#64748b', fontWeight: 500 }}>Current Streak</span>
                <span style={{ fontSize: '28px', fontWeight: 700 }}>{metrics.maxCurrentStreak} days</span>
                <span style={{ fontSize: '12px', color: '#64748b' }}>Longest coding streak</span>
              </div>
              <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <span style={{ fontSize: '13px', color: '#64748b', fontWeight: 500 }}>Acceptance Rate</span>
                <span style={{ fontSize: '28px', fontWeight: 700 }}>{metrics.averageAcceptanceRate}%</span>
                <span style={{ fontSize: '12px', color: '#64748b' }}>Average solve ratio</span>
              </div>
              <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <span style={{ fontSize: '13px', color: '#64748b', fontWeight: 500 }}>Readiness Score</span>
                <span style={{ fontSize: '28px', fontWeight: 700 }}>{insights?.interviewReadinessScore || 0}/100</span>
                <span style={{ fontSize: '12px', color: '#3b82f6', fontWeight: 500 }}>{insights?.performanceLevel || 'Beginner'}</span>
              </div>
            </div>

            {/* Platform Profiles & Strengths */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))', gap: '20px' }}>
              <div className="card">
                <h4 style={{ fontSize: '16px', marginBottom: '16px' }}>Connected Accounts</h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {accounts?.leetcodeUsername && (
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9', paddingBottom: '8px' }}>
                      <div>
                        <span style={{ fontSize: '14px', fontWeight: 500, display: 'block' }}>LeetCode</span>
                        <span style={{ fontSize: '12px', color: '#64748b' }}>{accounts.leetcodeUsername}</span>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <span style={{ fontSize: '14px', fontWeight: 600, color: '#0f172a', display: 'block' }}>
                          {metrics?.platformBreakdown?.['leetcode']?.totalSolved ?? 0} solved
                        </span>
                        <span style={{ fontSize: '11px', color: '#10b981', fontWeight: 500 }}>
                          🔥 {metrics?.platformBreakdown?.['leetcode']?.currentStreak ?? 0} day streak
                        </span>
                      </div>
                    </div>
                  )}
                  {accounts?.codeforcesUsername && (
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9', paddingBottom: '8px' }}>
                      <div>
                        <span style={{ fontSize: '14px', fontWeight: 500, display: 'block' }}>Codeforces</span>
                        <span style={{ fontSize: '12px', color: '#64748b' }}>{accounts.codeforcesUsername}</span>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <span style={{ fontSize: '14px', fontWeight: 600, color: '#0f172a', display: 'block' }}>
                          {metrics?.platformBreakdown?.['codeforces']?.totalSolved ?? 0} solved
                        </span>
                        <span style={{ fontSize: '11px', color: '#10b981', fontWeight: 500 }}>
                          🔥 {metrics?.platformBreakdown?.['codeforces']?.currentStreak ?? 0} day streak
                        </span>
                      </div>
                    </div>
                  )}
                  {accounts?.codechefUsername && (
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9', paddingBottom: '8px' }}>
                      <div>
                        <span style={{ fontSize: '14px', fontWeight: 500, display: 'block' }}>CodeChef</span>
                        <span style={{ fontSize: '12px', color: '#64748b' }}>{accounts.codechefUsername}</span>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <span style={{ fontSize: '14px', fontWeight: 600, color: '#0f172a', display: 'block' }}>
                          {metrics?.platformBreakdown?.['codechef']?.totalSolved ?? 0} solved
                        </span>
                        <span style={{ fontSize: '11px', color: '#10b981', fontWeight: 500 }}>
                          🔥 {metrics?.platformBreakdown?.['codechef']?.currentStreak ?? 0} day streak
                        </span>
                      </div>
                    </div>
                  )}
                  {accounts?.githubUsername && (
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9', paddingBottom: '8px' }}>
                      <div>
                        <span style={{ fontSize: '14px', fontWeight: 500, display: 'block' }}>GitHub</span>
                        <span style={{ fontSize: '12px', color: '#64748b' }}>{accounts.githubUsername}</span>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <span style={{ fontSize: '14px', fontWeight: 600, color: '#0f172a', display: 'block' }}>
                          {metrics?.platformBreakdown?.['github']?.totalSolved ?? 0} repos
                        </span>
                        <span style={{ fontSize: '11px', color: '#10b981', fontWeight: 500 }}>
                          🔥 {metrics?.platformBreakdown?.['github']?.currentStreak ?? 0} day streak
                        </span>
                      </div>
                    </div>
                  )}
                </div>
              </div>

              <div className="card">
                <h4 style={{ fontSize: '16px', marginBottom: '16px' }}>Coach Recommendations</h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {insights?.topicStrengths && insights.topicStrengths.map((strength, idx) => (
                    <div key={idx} style={{ fontSize: '14px', padding: '12px 16px', backgroundColor: 'rgba(255, 255, 255, 0.03)', borderRadius: '8px', border: '1px solid rgba(255, 255, 255, 0.06)', borderLeft: '4px solid #3b82f6', color: '#f8fafc', fontWeight: 500 }}>
                      {strength}
                    </div>
                  ))}
                  {insights?.nextMilestone && (
                    <div style={{ marginTop: '12px', fontSize: '14px', color: '#94a3b8' }}>
                      <strong>Next milestone:</strong> {insights.nextMilestone}
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
          <h3 style={{ fontSize: '20px', fontWeight: 600 }}>Analytics</h3>
          <p style={{ color: '#64748b', fontSize: '14px' }}>Deep dive into your strengths and category distribution.</p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px' }}>
          {/* Difficulty breakdown */}
          <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <h4 style={{ fontSize: '15px' }}>Problems Solved by Difficulty</h4>
            <div style={{ height: '200px' }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={difficultyData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {difficultyData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value) => [`${value} problems`]} />
                  <Legend verticalAlign="bottom" height={36} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Platform comparison */}
          <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <h4 style={{ fontSize: '15px' }}>Solved Counts by Platform</h4>
            <div style={{ height: '200px' }}>
              {platformComparisonData.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={platformComparisonData}>
                    <XAxis dataKey="name" stroke="#64748b" fontSize={12} tickLine={false} />
                    <YAxis stroke="#64748b" fontSize={12} tickLine={false} />
                    <Tooltip cursor={{ fill: 'rgba(241, 245, 249, 0.5)' }} />
                    <Bar dataKey="Solved" fill="#2563eb" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div style={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}>No data</div>
              )}
            </div>
          </div>
        </div>

        {/* Skill areas */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px' }}>
          <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <h4 style={{ fontSize: '15px' }}>DSA Topics Strength Breakdown</h4>
            <div style={{ height: '350px' }}>
              {strongestTopicsData.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={strongestTopicsData} layout="vertical">
                    <XAxis type="number" domain={[0, 100]} stroke="#64748b" fontSize={11} />
                    <YAxis dataKey="name" type="category" stroke="#64748b" fontSize={11} width={150} />
                    <Tooltip />
                    <Bar dataKey="Score" fill="#10b981" radius={[0, 4, 4, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div style={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}>No topic statistics tracked yet</div>
              )}
            </div>
          </div>
 
          <div className="card">
            <h4 style={{ fontSize: '15px', marginBottom: '16px' }}>Topic Gaps Priority</h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {insights?.skillGaps && insights.skillGaps.length > 0 ? (
                insights.skillGaps.map((gap, index) => (
                  <div key={index} style={{ display: 'flex', flexDirection: 'column', gap: '6px', borderBottom: '1px solid rgba(255, 255, 255, 0.06)', paddingBottom: '12px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <span style={{ fontSize: '14px', fontWeight: 600, color: '#ffffff' }}>{gap.topic}</span>
                        <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginTop: '2px' }}>
                          <span style={{
                            fontSize: '9px',
                            padding: '1px 5px',
                            borderRadius: '4px',
                            fontWeight: 700,
                            backgroundColor: gap.priority === 'HIGH' ? '#fef2f2' : gap.priority === 'MEDIUM' ? '#fffbeb' : '#f0fdf4',
                            color: gap.priority === 'HIGH' ? '#ef4444' : gap.priority === 'MEDIUM' ? '#d97706' : '#16a34a',
                            border: `1px solid ${gap.priority === 'HIGH' ? '#fee2e2' : gap.priority === 'MEDIUM' ? '#fde68a' : '#bbf7d0'}`
                          }}>
                            {gap.priority} PRIORITY
                          </span>
                          <span style={{ fontSize: '11px', color: '#94a3b8' }}>Target: {gap.estimatedDaysToTarget} days</span>
                        </div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <span style={{ fontSize: '13px', fontWeight: 700, color: '#ffffff' }}>{gap.currentScore} / {gap.targetScore}</span>
                        <span style={{ fontSize: '11px', color: '#94a3b8', display: 'block' }}>Score</span>
                      </div>
                    </div>
                    
                    {/* Visual Progress Bar */}
                    <div style={{ width: '100%', height: '6px', backgroundColor: 'rgba(255, 255, 255, 0.08)', borderRadius: '3px', overflow: 'hidden', marginTop: '4px' }}>
                      <div style={{ 
                        width: `${Math.min(100, Math.round((gap.currentScore / gap.targetScore) * 100))}%`, 
                        height: '100%', 
                        backgroundColor: gap.priority === 'HIGH' ? '#ef4444' : gap.priority === 'MEDIUM' ? '#f59e0b' : '#10b981',
                        borderRadius: '3px',
                        transition: 'width 0.5s ease-in-out'
                      }} />
                    </div>
                  </div>
                ))
              ) : (
                <p style={{ color: '#94a3b8', fontSize: '13px' }}>Excellent work! No significant skill gaps registered.</p>
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
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '28px' }}>
        {/* Left Side: Tasks */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <h3 style={{ fontSize: '20px', fontWeight: 600 }}>Preparation Study Plan</h3>
            <p style={{ color: '#64748b', fontSize: '14px' }}>Manage and complete week-by-week customized tasks.</p>
          </div>

          {formSuccess && <div className="alert alert-success">{formSuccess}</div>}

          {Object.keys(weeks).length === 0 ? (
            <div className="card" style={{ padding: '40px', textAlign: 'center' }}>
              <h4 style={{ fontSize: '16px', marginBottom: '8px' }}>No study plan active</h4>
              <p style={{ color: '#64748b', fontSize: '14px' }}>Provide target details in the generator form on the right to start.</p>
            </div>
          ) : (
            Object.entries(weeks).map(([week, tasks]) => (
              <div key={week} className="card" style={{ padding: '20px' }}>
                <h4 style={{ fontSize: '16px', borderBottom: '1px solid rgba(255, 255, 255, 0.08)', paddingBottom: '8px', marginBottom: '14px' }}>Week {week}</h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  {tasks.map(task => (
                    <label key={task.id} style={{ display: 'flex', gap: '12px', alignItems: 'flex-start', cursor: 'pointer' }}>
                      <input
                        type="checkbox"
                        checked={task.status === 'COMPLETED'}
                        onChange={() => handleToggleTask(task.id)}
                        style={{ marginTop: '4px', cursor: 'pointer' }}
                      />
                      <div style={{ textDecoration: task.status === 'COMPLETED' ? 'line-through' : 'none' }}>
                        <span style={{ fontSize: '13px', fontWeight: 600, color: '#94a3b8', display: 'block' }}>{task.topicName}</span>
                        <p style={{ fontSize: '14px', color: task.status === 'COMPLETED' ? '#64748b' : '#ffffff' }}>{task.taskDescription}</p>
                      </div>
                    </label>
                  ))}
                </div>
              </div>
            ))
          )}
        </div>

        {/* Right Side: Generator Form */}
        <div>
          <div className="card">
            <h4 style={{ fontSize: '16px', marginBottom: '16px' }}>Generate New Plan</h4>
            <form onSubmit={handleGenerateStudyPlan} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label htmlFor="planCompany">Target Company</label>
                <select
                  id="planCompany"
                  value={planCompany}
                  onChange={(e) => setPlanCompany(e.target.value)}
                >
                  <option value="Google">Google</option>
                  <option value="Amazon">Amazon</option>
                  <option value="Microsoft">Microsoft</option>
                  <option value="Meta">Meta</option>
                  <option value="Apple">Apple</option>
                </select>
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label htmlFor="planWeeks">Preparation Weeks</label>
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

              <button type="submit" className="button button-primary" disabled={planLoading}>
                {planLoading ? 'Generating Plan...' : 'Generate Plan'}
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
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '28px' }}>
        {/* Left Side: Recommendations Detail */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <h3 style={{ fontSize: '20px', fontWeight: 600 }}>AI DSA Insights & Recommendations</h3>
            <p style={{ color: '#94a3b8', fontSize: '14px' }}>Actionable advice based on your solved problem statistics, strengths, and gaps.</p>
          </div>

          {formSuccess && <div className="alert alert-success">{formSuccess}</div>}

          {!latestRecommendation ? (
            <div className="card" style={{ padding: '40px', textAlign: 'center' }}>
              <h4 style={{ fontSize: '16px', marginBottom: '8px' }}>No insights active</h4>
              <p style={{ color: '#94a3b8', fontSize: '14px' }}>Generate tailored guidance using the panel on the right.</p>
            </div>
          ) : (
            <div className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid rgba(255, 255, 255, 0.08)', paddingBottom: '12px', marginBottom: '20px' }}>
                <div>
                  <h4 style={{ fontSize: '18px' }}>Focus Goal: {latestRecommendation.targetCompany}</h4>
                  <span style={{ fontSize: '12px', color: '#94a3b8' }}>Generated on {formatDate(latestRecommendation.generatedAt)}</span>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <span style={{ fontSize: '12px', color: '#94a3b8', display: 'block' }}>DSA Strength Index</span>
                  <span style={{ fontSize: '20px', fontWeight: 700, color: '#60a5fa' }}>{latestRecommendation.interviewReadiness}%</span>
                </div>
              </div>
              <div style={{ fontSize: '14px', lineHeight: 1.6, color: '#ffffff' }}>
                {renderFormattedMarkdown(latestRecommendation.recommendationText)}
              </div>
            </div>
          )}
        </div>

        {/* Right Side: Generator Form */}
        <div>
          <div className="card">
            <h4 style={{ fontSize: '16px', marginBottom: '16px' }}>Generate Skill Analysis</h4>
            <form onSubmit={handleGenerateRecommendations} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label htmlFor="recCompany">Select Focus Goal</label>
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

              <button type="submit" className="button button-primary" disabled={recLoading}>
                {recLoading ? 'Analyzing Solved Data...' : 'Get Insights & Recommendations'}
              </button>
            </form>
          </div>
        </div>
      </div>
    )
  }

  const renderPlatforms = () => {
    return (
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px', gap: '28px' }}>
        {/* Left Side: Connected Platforms Listing */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <h3 style={{ fontSize: '20px', fontWeight: 600 }}>Coding Platforms</h3>
            <p style={{ color: '#64748b', fontSize: '14px' }}>Connect profiles to start tracking data.</p>
          </div>

          {formSuccess && <div className="alert alert-success">{formSuccess}</div>}
          {formError && <div className="alert alert-error">{formError}</div>}

          <div className="card" style={{ padding: 0 }}>
            <div style={{ padding: '16px 20px', borderBottom: '1px solid #e2e8f0', fontWeight: 600, fontSize: '14px', color: '#475569' }}>
              Connected Accounts Status
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '16px 20px', borderBottom: '1px solid #f1f5f9' }}>
                <span style={{ fontSize: '14px', fontWeight: 600 }}>LeetCode</span>
                <span style={{ fontSize: '14px', color: accounts?.leetcodeUsername ? '#0f172a' : '#94a3b8' }}>
                  {accounts?.leetcodeUsername || 'Not connected'}
                </span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '16px 20px', borderBottom: '1px solid #f1f5f9' }}>
                <span style={{ fontSize: '14px', fontWeight: 600 }}>Codeforces</span>
                <span style={{ fontSize: '14px', color: accounts?.codeforcesUsername ? '#0f172a' : '#94a3b8' }}>
                  {accounts?.codeforcesUsername || 'Not connected'}
                </span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '16px 20px', borderBottom: '1px solid #f1f5f9' }}>
                <span style={{ fontSize: '14px', fontWeight: 600 }}>CodeChef</span>
                <span style={{ fontSize: '14px', color: accounts?.codechefUsername ? '#0f172a' : '#94a3b8' }}>
                  {accounts?.codechefUsername || 'Not connected'}
                </span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '16px 20px' }}>
                <span style={{ fontSize: '14px', fontWeight: 600 }}>GitHub</span>
                <span style={{ fontSize: '14px', color: accounts?.githubUsername ? '#0f172a' : '#94a3b8' }}>
                  {accounts?.githubUsername || 'Not connected'}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Right Side: Connect Form */}
        <div>
          <div className="card">
            <h4 style={{ fontSize: '16px', marginBottom: '16px' }}>Link Coding Account</h4>
            <form onSubmit={handleConnect} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label htmlFor="connectPlatform">Select Platform</label>
                <select
                  id="connectPlatform"
                  value={connectPlatform}
                  onChange={(e) => setConnectPlatform(e.target.value)}
                >
                  <option value="leetcode">LeetCode</option>
                  <option value="codeforces">Codeforces</option>
                  <option value="codechef">CodeChef</option>
                  <option value="github">GitHub</option>
                </select>
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label htmlFor="connectUsername">Username</label>
                <input
                  id="connectUsername"
                  type="text"
                  placeholder="Enter username"
                  value={connectUsername}
                  onChange={(e) => setConnectUsername(e.target.value)}
                  required
                />
              </div>

              <button type="submit" className="button button-primary">
                Connect Profile
              </button>
            </form>
          </div>
        </div>
      </div>
    )
  }

  const renderSettings = () => {
    return (
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.2fr', gap: '24px', alignItems: 'start' }}>
        <div className="card">
          <h3 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '20px', borderBottom: '1px solid #e2e8f0', paddingBottom: '8px' }}>Profile Card</h3>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px', textAlign: 'center', marginBottom: '20px' }}>
            <div style={{ position: 'relative' }}>
              <img
                src={profilePhoto || "https://api.dicebear.com/7.x/bottts/svg?seed=" + (user?.name || "default")}
                alt="Profile Avatar"
                style={{ width: '100px', height: '100px', borderRadius: '50%', objectFit: 'cover', border: '3px solid #3b82f6', backgroundColor: '#f8fafc' }}
                onError={(e) => {
                  (e.target as HTMLImageElement).src = "https://api.dicebear.com/7.x/bottts/svg?seed=" + (user?.name || "default")
                }}
              />
            </div>
            <div>
              <h4 style={{ fontSize: '18px', fontWeight: 600, margin: '4px 0' }}>{user?.name}</h4>
              <span style={{ fontSize: '14px', color: '#64748b' }}>{user?.email}</span>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', borderTop: '1px solid #e2e8f0', paddingTop: '16px' }}>
            <div>
              <span style={{ fontSize: '12px', color: '#64748b', display: 'block' }}>College Name</span>
              <span style={{ fontSize: '14px', fontWeight: 500, color: user?.collegeName ? '#0f172a' : '#94a3b8' }}>
                {user?.collegeName || 'Not configured'}
              </span>
            </div>
            <div>
              <span style={{ fontSize: '12px', color: '#64748b', display: 'block' }}>Contact Number</span>
              <span style={{ fontSize: '14px', fontWeight: 500, color: user?.contactNumber ? '#0f172a' : '#94a3b8' }}>
                {user?.contactNumber || 'Not configured'}
              </span>
            </div>
            <div>
              <span style={{ fontSize: '12px', color: '#64748b', display: 'block' }}>Resume Link</span>
              {user?.resumeUrl ? (
                <a href={user.resumeUrl} target="_blank" rel="noopener noreferrer" style={{ fontSize: '14px', fontWeight: 500, color: '#3b82f6', display: 'flex', alignItems: 'center', gap: '4px', textDecoration: 'none' }}>
                  📄 View Resume
                </a>
              ) : (
                <span style={{ fontSize: '14px', fontWeight: 500, color: '#94a3b8' }}>Not configured</span>
              )}
            </div>
            <div>
              <span style={{ fontSize: '12px', color: '#64748b', display: 'block' }}>Account Created</span>
              <span style={{ fontSize: '14px', fontWeight: 500 }}>
                {user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}
              </span>
            </div>
          </div>
        </div>

        <div className="card">
          <h3 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '20px', borderBottom: '1px solid #e2e8f0', paddingBottom: '8px' }}>Edit Profile Information</h3>
          
          {profileMessage && (
            <div style={{
              padding: '12px',
              borderRadius: '6px',
              marginBottom: '16px',
              fontSize: '14px',
              backgroundColor: profileMessage.startsWith('Error') ? '#fef2f2' : '#ecfdf5',
              color: profileMessage.startsWith('Error') ? '#991b1b' : '#065f46',
              border: profileMessage.startsWith('Error') ? '1px solid #fca5a5' : '1px solid #6ee7b7'
            }}>
              {profileMessage}
            </div>
          )}

          <form onSubmit={handleUpdateProfile} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div className="form-group" style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <label htmlFor="profileName" style={{ fontSize: '13px', fontWeight: 500, color: '#334155' }}>Name</label>
              <input
                id="profileName"
                type="text"
                value={profileName}
                onChange={(e) => setProfileName(e.target.value)}
                placeholder="Your Full Name"
                required
              />
            </div>

            <div className="form-group" style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <label htmlFor="collegeName" style={{ fontSize: '13px', fontWeight: 500, color: '#334155' }}>College Name</label>
              <input
                id="collegeName"
                type="text"
                value={collegeName}
                onChange={(e) => setCollegeName(e.target.value)}
                placeholder="Enter College/University Name"
              />
            </div>

            <div className="form-group" style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <label htmlFor="contactNumber" style={{ fontSize: '13px', fontWeight: 500, color: '#334155' }}>Contact Number</label>
              <input
                id="contactNumber"
                type="text"
                value={contactNumber}
                onChange={(e) => setContactNumber(e.target.value)}
                placeholder="Enter Phone/Contact Number"
              />
            </div>

            <div className="form-group" style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <label htmlFor="profilePhoto" style={{ fontSize: '13px', fontWeight: 500, color: '#334155' }}>Profile Photo URL</label>
              <input
                id="profilePhoto"
                type="text"
                value={profilePhoto}
                onChange={(e) => setProfilePhoto(e.target.value)}
                placeholder="Enter avatar image link (e.g. https://...)"
              />
            </div>

            <div className="form-group" style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <label htmlFor="resumeUrl" style={{ fontSize: '13px', fontWeight: 500, color: '#334155' }}>Resume Link (Google Drive / Dropbox)</label>
              <input
                id="resumeUrl"
                type="text"
                value={resumeUrl}
                onChange={(e) => setResumeUrl(e.target.value)}
                placeholder="Enter link to view resume"
              />
            </div>

            <button type="submit" className="button button-primary" disabled={profileLoading} style={{ marginTop: '10px' }}>
              {profileLoading ? 'Updating Profile...' : 'Save Profile Changes'}
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
          <h1>CodeInsight.AI</h1>
          <div className="navbar-right">
            <span className="user-name">Welcome, {user?.name}!</span>
            <button onClick={handleLogout} className="button button-secondary" style={{ padding: '8px 14px', fontSize: '13px' }}>
              Logout
            </button>
          </div>
        </div>
      </nav>

      <div className="dashboard-container">
        <aside className="sidebar">
          <nav className="sidebar-nav">
            <button
              onClick={() => { setActiveTab('dashboard'); setFormSuccess(null); setFormError(null); }}
              className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`}
              style={{ border: 'none', background: 'none', width: '100%', textAlign: 'left', cursor: 'pointer' }}
            >
              📊 Dashboard
            </button>
            <button
              onClick={() => { setActiveTab('analytics'); setFormSuccess(null); setFormError(null); }}
              className={`nav-item ${activeTab === 'analytics' ? 'active' : ''}`}
              style={{ border: 'none', background: 'none', width: '100%', textAlign: 'left', cursor: 'pointer' }}
            >
              📈 Analytics
            </button>
            <button
              onClick={() => { setActiveTab('study-plan'); setFormSuccess(null); setFormError(null); }}
              className={`nav-item ${activeTab === 'study-plan' ? 'active' : ''}`}
              style={{ border: 'none', background: 'none', width: '100%', textAlign: 'left', cursor: 'pointer' }}
            >
              📚 Study Plan
            </button>
            <button
              onClick={() => { setActiveTab('recommendations'); setFormSuccess(null); setFormError(null); }}
              className={`nav-item ${activeTab === 'recommendations' ? 'active' : ''}`}
              style={{ border: 'none', background: 'none', width: '100%', textAlign: 'left', cursor: 'pointer' }}
            >
              💡 Recommendations
            </button>
            <button
              onClick={() => { setActiveTab('platforms'); setFormSuccess(null); setFormError(null); }}
              className={`nav-item ${activeTab === 'platforms' ? 'active' : ''}`}
              style={{ border: 'none', background: 'none', width: '100%', textAlign: 'left', cursor: 'pointer' }}
            >
              🔗 Platforms
            </button>
            <button
              onClick={() => { setActiveTab('settings'); setFormSuccess(null); setFormError(null); }}
              className={`nav-item ${activeTab === 'settings' ? 'active' : ''}`}
              style={{ border: 'none', background: 'none', width: '100%', textAlign: 'left', cursor: 'pointer' }}
            >
              ⚙️ Settings
            </button>
          </nav>
        </aside>

        <main className="main-content">
          {renderContent()}
        </main>
      </div>
    </div>
  )
}

export default DashboardPage
