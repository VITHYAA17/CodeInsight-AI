# CodeInsight.AI

I built CodeInsight.AI to solve a personal pain point: keeping track of coding progress across multiple platforms (like LeetCode, CodeChef, Codeforces, and GitHub) and getting actionable, personalized advice on what to study next for technical interviews. This application aggregates all of those developer stats into a single dark-themed dashboard, identifies DSA skill gaps, generates study roadmaps, and keeps track of progress via interactive task checklists.

---

## Live Deployments

You can check out the live builds of the application hosted on Render:

- **Frontend App**: [https://codeinsight-frontend-d22s.onrender.com](https://codeinsight-frontend-d22s.onrender.com)
- **Backend API**: [https://codeinsight-ai-backend-ncxn.onrender.com](https://codeinsight-ai-backend-ncxn.onrender.com)

Note: Since the services are hosted on Render's free tier, there might be a 50-90 second spin-up delay (cold start) when loading the URL for the first time. Once active, it runs fast.

---

## What the App Does

- **Syncs Coding Profiles**: It scrapes public statistics and submission calendars from LeetCode, Codeforces, CodeChef, and GitHub so you can see your active daily streak and contest history in one place.
- **Identifies Skill Gaps**: Based on the difficulty distribution and topics of the problems you have solved, it highlights your weakest DSA areas (e.g. Dynamic Programming, Graphs) and lists what you need to focus on next.
- **Generates Roadmaps**: You can select a target company (like Google or Amazon) and request custom roadmaps. If an LLM is not configured, it falls back to curated resources (like Striver, Aditya Verma, and Concept & Coding) tailored directly to your skill gaps.
- **Keeps You Organized**: The app generates interactive, checkable task lists to help you stay on track day-by-day.
- **Sleek Profile Management**: You can manage your personal details, contact info, university name, resume links, and choose custom developer avatars.

---

## Tech Stack I Used

- **Backend**: Java 21, Spring Boot 3.x, Spring Data JPA, Hibernate, Spring Security with JWT tokens, and PostgreSQL.
- **Frontend**: React 18, TypeScript, Vite, Recharts for charts, and custom CSS for a dark glassmorphic UI.

---

## How to Set It Up Locally

If you want to run this project on your local machine, here are the steps to get both the backend and frontend up and running.

### Prerequisites
Make sure you have these installed on your system:
- Java Development Kit (JDK) 21 or later
- Node.js (v18 or later)
- PostgreSQL Server running on port 5432 (create a database named `codeinsight`)

### 1. Set Up the Database
Open the backend configuration file at `backend/src/main/resources/application.yaml` and update the database credentials to match your local PostgreSQL setup:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/codeinsight
    username: postgres
    password: YourPostgresPassword
```

### 2. Run the Spring Boot Backend
Open your terminal, navigate to the `backend` folder, and start the Spring Boot server using the Maven wrapper:
```cmd
cd backend
mvnw.cmd spring-boot:run
```
The server will boot up, automatically generate the database schema tables, and start listening for API requests on http://localhost:8080.

### 3. Run the React Frontend
Open a separate terminal window, navigate to the `frontend` folder, install the package dependencies, and start the Vite development server:
```cmd
cd frontend
npm install
npm run dev
```
Once it starts, open http://localhost:5173 in your web browser to view the application.

---

## Styling and Themes

The UI uses a custom-built, modern glassmorphic dark theme. All design tokens (colors, gradients, paddings, and borders) are defined in:
- Global styles: `frontend/src/index.css`
- Layout spacing: `frontend/src/pages/Dashboard.css`

I styled this using CSS variables so it is easy to customize. If you want to change the primary accent colors or adjust typography sizes, you can modify the tokens block inside the `index.css` file.
