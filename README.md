# CodeInsight.AI

CodeInsight.AI is a premium, feature-rich developer analytics dashboard and interview preparation coach. It dynamically aggregates data from coding platforms like **LeetCode**, **CodeChef**, **Codeforces**, and **GitHub** to provide comprehensive performance insights, identify DSA skill gaps, generate customized AI preparation roadmaps, and track progress using interactive study plans.

---

## Live Deployments 🌐

- **Frontend App**: [https://codeinsight-frontend-d22s.onrender.com](https://codeinsight-frontend-d22s.onrender.com)
- **Backend API**: [https://codeinsight-ai-backend-ncxn.onrender.com](https://codeinsight-ai-backend-ncxn.onrender.com)

---

## Key Features 

- **Dynamic Profile Synchronization**: 
  - Real-time scraping of LeetCode submission calendars to calculate actual active coding streaks.
  - Automatic synchronization of coding contest participation, contest rankings, and problems solved.
- **Deep Analytics & Gaps Prioritization**:
  - Interactive charts displaying problem-solving distributions (Easy, Medium, Hard).
  - DSA Topic performance trackers indicating percentage mastery in covered topics.
  - Priority Gaps lists identifying critical topics that require focus based on company targets.
- **AI Coach Roadmaps**:
  - Tailored interview roadmaps generated dynamically for top companies (Google, Meta, Apple, Amazon, Microsoft).
  - Detailed recommendations indicating preparation readiness scores, areas of focus, and behavioral preparation tips.
- **Interactive Study Plans**:
  - Week-by-week personalized task checklists built to guide candidates systematically through preparation milestones.
- **Sleek Profile Settings**:
  - Personalize account details including Name, College/University, Contact Number, Resume Links, and customizable real-time profile avatars.

---

## Technology Stack 

- **Backend**: Spring Boot 3.x, Spring Data JPA, Spring Security (JWT-based), Hibernate, PostgreSQL
- **Frontend**: React 18, TypeScript, Vite, Recharts, Custom HSL Glassmorphic Dark-Mode UI Theme

---

## Getting Started 

Follow these instructions to run the application locally on your machine.

### Prerequisites
- Java Development Kit (JDK) 21 or later
- Node.js (v18 or later)
- PostgreSQL Server (running on port `5432` with a database named `codeinsight`)

### 1. Configure the Database
Verify your connection credentials inside the backend's property configuration file:
- File: [application.yaml](file:///c:/users/victus/OneDrive/Desktop/CodeInsight-AI/backend/src/main/resources/application.yaml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/codeinsight
    username: postgres
    password: YourPostgresPassword
```

### 2. Start the Backend Service
Navigate to the `backend` folder and run the Spring Boot server:
```cmd
cd backend
mvnw.cmd spring-boot:run
```
The server will initialize the PostgreSQL tables automatically and begin listening on **`http://localhost:8080`**.

### 3. Start the Frontend Application
Navigate to the `frontend` folder, install dependencies, and launch the Vite development server:
```cmd
cd frontend
npm install
npm run dev
```
Open **[http://localhost:5173/](http://localhost:5173/)** in your browser to experience the application!

---

## Design System & Customization 

The user interface uses a high-end **Glassmorphism Dark Theme** configured globally:
- Global Styles: [index.css](file:///c:/users/victus/OneDrive/Desktop/CodeInsight-AI/frontend/src/index.css)
- Layout Overrides: [Dashboard.css](file:///c:/users/victus/OneDrive/Desktop/CodeInsight-AI/frontend/src/pages/Dashboard.css)

Feel free to customize the primary theme accent or adjust typography details inside the `index.css` design tokens block!
