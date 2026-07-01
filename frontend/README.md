# CodeInsight AI - Frontend

React + TypeScript + Vite frontend for CodeInsight AI interview preparation platform.

## Features

- ✅ User authentication (Login/Register)
- ✅ Protected routes with JWT
- ✅ Dashboard with metrics overview
- ✅ Analytics and performance charts
- ✅ Study plan tracker
- ✅ AI-powered recommendations
- ✅ Company readiness scores

## Tech Stack

- **React 18** - UI framework
- **TypeScript** - Type safety
- **Vite** - Lightning-fast build tool
- **React Router v6** - Client-side routing
- **Axios** - HTTP client
- **Recharts** - Data visualization

## Setup

### Prerequisites

- Node.js 16+
- npm or yarn

### Installation

```bash
# Install dependencies
npm install

# Create .env file (copy from .env.example)
cp .env.example .env

# Update .env with your backend URL
VITE_API_BASE_URL=http://localhost:8080/api
```

### Development

```bash
# Start development server
npm run dev

# The app will be available at http://localhost:5173
```

### Build

```bash
# Build for production
npm run build

# Preview production build
npm run preview
```

## Project Structure

```
src/
├── components/          # Reusable components
│   └── ProtectedRoute.tsx
├── context/            # React Context (Auth state)
│   └── AuthContext.tsx
├── pages/              # Page components
│   ├── LoginPage.tsx
│   ├── RegisterPage.tsx
│   └── DashboardPage.tsx
├── services/           # API calls
│   └── api.ts
├── App.tsx             # Main app component
├── main.tsx            # Entry point
└── index.css          # Global styles
```

## API Integration

All API calls are handled through `src/services/api.ts`:

- **Auth API** - Login, Register, Current User
- **Platform API** - Connect accounts, sync stats
- **Analytics API** - Metrics, Performance, Insights
- **AI API** - Recommendations, Study Plans

Authentication uses JWT tokens stored in localStorage.

## Environment Variables

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=CodeInsight AI
```

## Deployment

### Vercel (Recommended)

```bash
# Push to GitHub, connect repository to Vercel
# Set environment variables in Vercel dashboard
# Auto-deploy on push
```

### Docker

```dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM node:18-alpine
WORKDIR /app
RUN npm install -g serve
COPY --from=builder /app/dist ./dist
EXPOSE 3000
CMD ["serve", "-s", "dist", "-l", "3000"]
```

## Development Phases

- **Phase 6A** ✅ - Frontend Setup & Project Structure
- **Phase 6B** - Authentication Pages (Login/Register)
- **Phase 6C** - Dashboard Overview & Metrics
- **Phase 6D** - Analytics & Charts
- **Phase 6E** - Study Plan Tracker
- **Phase 6F** - Recommendations & Company Readiness
