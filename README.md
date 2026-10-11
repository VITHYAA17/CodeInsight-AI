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

- **Backend**: Java 21, Spring Boot 3.x, Spring Data JPA, Hibernate, Spring Security with JWT tokens, PostgreSQL with `pgvector`, Redis (caching & Bucket4j rate limiting).
- **AI & RAG Engine**: Spring AI, OpenAI GPT-4.1, PostgreSQL `pgvector` cosine similarity search, Server-Sent Events (SSE) real-time streaming.
- **Frontend**: React 18, TypeScript, Vite, Recharts for analytics data visualization, and custom CSS for a dark glassmorphic UI.
- **DevOps & Cloud Infrastructure**: Docker, Kubernetes (GKE), Terraform (IaC on GCP), Jenkins CI/CD pipeline.

---

## ☁️ Cloud Infrastructure & DevOps (Terraform & Kubernetes)

The application includes enterprise-grade Cloud Infrastructure as Code (IaC) and container orchestration:

- **Terraform ([`terraform/`](terraform/))**: Complete GCP modular infrastructure provisioning:
  - Custom VPC with secondary IP ranges for Pods (`10.20.0.0/16`) and Services (`10.30.0.0/20`), with Cloud NAT for private node egress.
  - Private **Google Kubernetes Engine (GKE)** cluster with Workload Identity and autoscaling node pools.
  - Managed **Cloud SQL PostgreSQL 15** with `cloudsql.enable_pgvector = on` for AI semantic vector search.
  - Managed **Cloud Memorystore for Redis** for distributed rate limiting and caching.
  - **Google Artifact Registry** for private Docker image storage.
- **Kubernetes ([`k8s/`](k8s/))**: Production declarative manifests featuring Deployments, ClusterIP Services, ConfigMaps, Secrets, and Ingress with rolling zero-downtime updates and health probes.
- **CI/CD Pipeline ([`Jenkinsfile`](Jenkinsfile))**: Declarative Jenkins pipeline automating Maven build & test, React Vite packaging, Docker image creation, Artifact Registry push, and zero-downtime GKE deployment rollout.

---

## Styling and Themes

The UI uses a custom-built, modern glassmorphic dark theme. All design tokens (colors, gradients, paddings, and borders) are defined in:
- Global styles: `frontend/src/index.css`
- Layout spacing: `frontend/src/pages/Dashboard.css`

I styled this using CSS variables so it is easy to customize. If you want to change the primary accent colors or adjust typography sizes, you can modify the tokens block inside the `index.css` file.

