# Learning Progress Tracker

## 🚀 Live Demo

- Frontend: https://learning-tracker-smoky.vercel.app/login
- Backend: https://learning-tracker-tdd2.onrender.com

A small web app for tracking training progress in a cohort.

- **Coordinators** sign in, manage learning activities, create candidate accounts, assign activities, reset passwords, and see cohort progress.
- **Candidates** sign in with a one-time temporary password, set their own password, and view and update the status of **only their own** training.

All names, emails and data in this project are fictional.

## Tech stack

| Part | Technology |
|---|---|
| Backend | Java, Spring Boot 4, Spring Web, Spring Data JPA (Hibernate), Spring Security, Bean Validation, Maven |
| Database | H2, stored in a file (no database install needed) |
| Frontend | React with Next.js 16 (App Router), plain CSS |
| Tests | JUnit 5 with Spring Boot Test |

## Repository layout

```
learning-tracker/
├── api/learning-tracker-api/   Spring Boot backend (REST API on port 8080)
└── web/                        Next.js frontend (port 3000)
```

Backend packages (`com.example.learning_tracker_api`): `controller`, `service`, `repository`, `entity`, `dto`, `security`, `exception`, `config`.

## Prerequisites

- **JDK**: use the version set as `java.version` in `api/learning-tracker-api/pom.xml` (developed on JDK 27; JDK 21 or newer is recommended).
- **Node.js 20.9 or newer** and npm (check with `node -v`).
- **Git**.
- Free ports **8080** (backend) and **3000** (frontend).

## Configuration

Nothing secret is stored in the repository. Credentials are read from environment variables.

### Backend

| Variable | Required | Purpose |
|---|---|---|
| `COORDINATOR_EMAIL` | Yes, on first start | Email of the initial coordinator account |
| `COORDINATOR_PASSWORD` | Yes, on first start | Password of the initial coordinator account (choose your own strong password) |
| `SESSION_COOKIE_SECURE` | Only on HTTPS | Set to `true` whenever the app is served over HTTPS. Leave unset on localhost. |

The coordinator is created **once**, when the database has no coordinator yet.

### Frontend

| Variable | Default | Purpose |
|---|---|---|
| `API_URL` | `http://localhost:8080` | Where Next.js forwards `/api/*` requests |

## Setup and run

### 1. Start the backend

macOS / Linux:

```bash
cd api/learning-tracker-api
export COORDINATOR_EMAIL="coordinator@example.com"
export COORDINATOR_PASSWORD="<choose-a-strong-password>"
./mvnw spring-boot:run
```

Windows (PowerShell):

```powershell
cd api\learning-tracker-api
$env:COORDINATOR_EMAIL="coordinator@example.com"
$env:COORDINATOR_PASSWORD="<choose-a-strong-password>"
.\mvnw.cmd spring-boot:run
```

On first start the console shows `Initial coordinator account created.` The API is then available at `http://localhost:8080`.

You can also run `LearningTrackerApiApplication` from an IDE. Add the two variables to its run configuration.

### 2. Start the frontend

In a second terminal:

```bash
cd web
npm install
npm run dev
```

Open **http://localhost:3000**.

The browser only talks to port 3000. Next.js forwards `/api/*` to the backend, so session and CSRF cookies work without any CORS setup.

## Database and seed data

- The H2 database file is created automatically at `api/learning-tracker-api/data/` and tables are created on start.
- On an empty database the app seeds:
  - the coordinator (from the environment variables above)
  - 4 activities: React Basics, REST APIs, SQL Foundations, Spring Boot Basics
  - 3 fictional learners: Jordan Lee, Priya Nair, Sam Carter
- Seeded candidates have no usable password. To let one sign in, open **Learners → View progress → Reset password** as the coordinator and hand over the new one-time password. You can also create a new candidate.
- **Start fresh:** stop the backend, delete the `data/` folder, and start it again.

## Using the app

**Coordinator**

1. Open http://localhost:3000, choose **Coordinator**, and sign in.
2. **Dashboard:** see the totals, then add or edit activities. Title, topic and planned hours (greater than zero) are required.
3. **Learners → Add candidate:** enter name, email and cohort. The temporary password is shown **once**. Copy it and hand it over privately. It expires after 24 hours. A duplicate email is rejected.
4. **Learners → View progress:** assign an activity (a duplicate assignment is rejected), see each learner's progress, or reset their password.

**Candidate**

1. Choose **Candidate** and sign in with the email and temporary password.
2. Set a new password (required before anything else).
3. **My Training:** change the status of each assigned activity (Not Started, In Progress, Completed).

To try both roles at once, use a normal window and a private/incognito window.

## Running the tests

**Backend** (uses an in-memory database, so your real data is untouched):

```bash
cd api/learning-tracker-api
./mvnw test
```

They cover:

- activity validation: required fields and hours greater than zero
- assignments: duplicate prevention, missing learner or activity, status updates, cohort summary counts
- candidate creation: temporary password, 24-hour expiry, duplicate email (case-insensitive), first sign-in
- authentication: generic error for wrong password and unknown email, expired temporary password rejected
- session rules: first-login enforcement, old sessions rejected after a reset
- access rules: a candidate cannot update another candidate's assignment, and "my assignments" shows only the caller's own
- login rate limiting

**Frontend:**

```bash
cd web
npm run lint
npm run build
```

## API summary

All paths start with `/api/v1`. Errors use the format `{ "message": "..." }`.

| Who | Endpoint | Notes |
|---|---|---|
| Public | `GET /auth/csrf` | Sets the `XSRF-TOKEN` cookie |
| Public | `POST /auth/login` | Returns `role` and `mustChangePassword`; rate-limited |
| Signed in | `POST /auth/change-password` | `204`; rotates the session |
| Signed in | `POST /auth/logout` | `204` |
| Coordinator | `GET`, `POST /activities`; `PUT /activities/{id}` | |
| Coordinator | `GET`, `POST /learners` | `POST` creates a candidate and returns one-time credentials |
| Coordinator | `GET /learners/{id}/assignments` | |
| Coordinator | `POST /learners/{id}/reset-password` | One-time credentials; ends the candidate's sessions |
| Coordinator | `POST /assignments` | `409` if already assigned |
| Coordinator | `GET /cohort-summary` | Learners, assignments, completed |
| Candidate | `GET /me/assignments` | Only the caller's own training |
| Candidate | `PATCH /assignments/{id}/status` | Only the caller's own assignment |

Status codes: `400` invalid input, `401` not signed in, `403` not allowed (wrong role, someone else's data, or password change still required), `404` not found, `409` duplicate, `429` too many login attempts.

Calling the API by hand: first `GET /auth/csrf`, then send the `XSRF-TOKEN` cookie value in an `X-XSRF-TOKEN` header on every `POST`, `PUT` and `PATCH`.

## Data model

- **Account:** id, unique lowercase email, password hash, role (Coordinator or Candidate), must-change-password flag, temporary-password expiry, session version
- **Learner:** id, name, cohort, account
- **Activity:** id, title, topic, planned hours
- **Assignment:** id, learner, activity, status. One assignment per learner and activity pair.

Password hashes are never returned by the API.

## Security notes

- Passwords are stored as salted BCrypt hashes.
- Sessions use HttpOnly, SameSite cookies and expire after 30 minutes of inactivity. Set `SESSION_COOKIE_SECURE=true` on HTTPS.
- CSRF protection is enabled for every `POST`, `PUT` and `PATCH`.
- Login is rate-limited, and every failed login returns the same generic message.
- Temporary passwords are random, shown once, and expire after 24 hours. A signed-in user with a temporary password can only change the password or sign out.
- Roles and ownership are enforced on the server for every request. The frontend only redirects for convenience.
- The H2 console is disabled. No credentials or secrets are committed.

## Troubleshooting

- **"Invalid email or password" for the coordinator:** the coordinator is only created once. Stop the app, delete `api/learning-tracker-api/data/`, check the two environment variables, and start again.
- **`429 Too many login attempts`:** wait 15 minutes or restart the backend.
- **`403` when calling the API by hand:** the CSRF header is missing (see the API summary).
- **Port already in use:** stop the other process using 8080 or 3000.

## Known limitations

- The login rate limiter is kept in memory, so it resets when the backend restarts and works for a single instance.
- Progress bars show 0%, 50% or 100% based on the status, because only three statuses are stored.
- Credentials are handed over manually. There is no email delivery.
