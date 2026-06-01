# Sprintly

Sprintly is a modern, real-time project management and Kanban board application. It enables agile teams to organize tasks, track project progress, and collaborate seamlessly in real time.

## Key Features

*   **Kanban Board Interface**: Intuitive drag-and-drop task management across columns (To Do, In Progress, Done).
*   **Real-Time Synchronization**: Instant updates across all active clients via WebSockets.
*   **Project & Organization Management**: Group projects under organizations with role-based access control.
*   **Secure Authentication**: JWT-based user authentication and secure API endpoints.
*   **Modern UI/UX**: Responsive, fast, and accessible interface built with modern web technologies.

## Technology Stack

### Frontend
*   React 19 with TypeScript
*   Vite for rapid development and bundling
*   Tailwind CSS for utility-first styling
*   React Query for server state management
*   dnd-kit for accessible drag-and-drop interactions

### Backend
*   Java 23
*   Spring Boot 3.4
*   Spring Security with JWT authentication
*   Spring Data JPA (Hibernate)
*   Spring WebSockets (STOMP protocol)

### Database
*   PostgreSQL 15

---

## Local Development Setup

### Prerequisites
*   Java 23 (JDK)
*   Node.js (v18 or higher)
*   Docker and Docker Compose (for the local database)
*   Maven

### 1. Database Setup

The project uses Docker Compose to spin up a local PostgreSQL 15 instance.

Navigate to the project root and start the database:

```bash
docker-compose up -d
```

The schema and initial tables are automatically initialized on first startup if configured, or you can manually apply `schema.sql` if needed.

### 2. Backend Setup

Navigate to the `backend` directory and start the Spring Boot application.

```bash
cd backend
mvn spring-boot:run
```

The backend server will start on `http://localhost:8080`.

### 3. Frontend Setup

Navigate to the `frontend` directory, install dependencies, and start the Vite development server.

```bash
cd frontend
npm install
npm run dev
```

The frontend application will be accessible at `http://localhost:5173`.

---

## Architecture Overview

The system is designed with a clear separation of concerns:
*   **Client**: A Single Page Application (SPA) that communicates with the backend via RESTful endpoints for CRUD operations and maintains a persistent WebSocket connection for real-time task updates.
*   **API Layer**: Spring Web MVC controllers expose secured REST endpoints. Cross-Origin Resource Sharing (CORS) is configured to allow the frontend to communicate with the backend.
*   **Service Layer**: Contains the core business logic, transaction management, and WebSocket broadcasting mechanisms.
*   **Data Access Layer**: Spring Data JPA repositories interfacing with PostgreSQL. Custom PostgreSQL enums are mapped using Hibernate type configurations to ensure strict database integrity.
