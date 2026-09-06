# 🚀 CodeExam – Smart Coding Exam System

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-blue?style=for-the-badge&logo=java&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.2">
  <img src="https://img.shields.io/badge/Spring%20Security-6.x-green?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security">
  <img src="https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React 19">
  <img src="https://img.shields.io/badge/TypeScript-5.x-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript">
  <img src="https://img.shields.io/badge/Vite-7.x-646CFF?style=for-the-badge&logo=vite&logoColor=white" alt="Vite">
  <img src="https://img.shields.io/badge/MySQL-blue?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
  <img src="https://img.shields.io/badge/AWS-EC2-orange?style=for-the-badge&logo=amazonaws&logoColor=white" alt="AWS EC2">
</p>

<p align="center">
  <b>An AI-powered online coding examination platform built with React, TypeScript, Spring Boot, MySQL and Docker.</b>
</p>

<p align="center">
  <a href="http://13.60.52.103">
    <img src="https://img.shields.io/badge/🚀_Live_Demo-CodeExam-success?style=for-the-badge" alt="Live Demo">
  </a>
</p>

---

## 📌 Table of Contents

* [🌐 Live Demo](#-live-demo)
* [📖 About the Project](#-about-the-project)
* [✨ Key Features](#-key-features)
* [👥 User Roles](#-user-roles)
* [📸 Screenshots](#-screenshots)
* [👨‍💼 Admin Features](#-admin-features)
* [👨‍🎓 Student Features](#-student-features)
* [🤖 AI Question Generator](#-ai-question-generator)
* [📂 AI File Upload Generation](#-ai-file-upload-generation)
* [💻 Online Coding Exam](#-online-coding-exam)
* [🏆 Results & Leaderboard](#-results--leaderboard)
* [🛠️ Tech Stack](#️-tech-stack)
* [🏗️ System Architecture](#️-system-architecture)
* [📁 Project Structure](#-project-structure)
* [🚀 How to Run](#-how-to-run)
* [🐳 Docker Deployment](#-docker-deployment)
* [⚙️ Configuration](#️-configuration)
* [🔐 Authentication & Security](#-authentication--security)
* [🧪 Testing](#-testing)
* [🚧 Future Enhancements](#-future-enhancements)
* [📄 License](#-license)
* [👨‍💻 Developer](#-developer)

---

# 🌐 Live Demo

<p align="center">

### 🚀 Try CodeExam Online

<a href="http://13.60.52.103">
  <img src="https://img.shields.io/badge/OPEN_LIVE_PREVIEW-13.60.52.103-success?style=for-the-badge" alt="Open Live Preview">
</a>

</p>

> **Live Application:** `http://13.60.52.103`

The application is deployed on **AWS EC2** and can be accessed through the live preview above.

---

# 📖 About the Project

**CodeExam** is a smart online coding examination platform designed to conduct programming assessments in a secure and interactive environment.

The platform provides dedicated interfaces for both **Administrators** and **Students**.

### 👨‍💼 Administrators can

* Create and manage examinations
* Add programming questions manually
* Generate questions using AI
* Generate multiple questions at once
* Upload documents for AI-powered question generation
* Manage students
* Configure exam duration and marks
* Manage test cases
* Monitor examination results
* View the global leaderboard

### 👨‍🎓 Students can

* Register and securely log in
* Browse available examinations
* Attempt timed coding exams
* Write code using the Monaco Editor
* Run and test their solutions
* Submit coding answers
* View examination results
* Track performance
* Compare rankings on the global leaderboard

The application uses a modern **React + TypeScript frontend**, **Spring Boot backend**, and **MySQL database**, with **Docker** and **AWS EC2** support for deployment.

---

# ✨ Key Features

| Feature                 | Description                                  |
| ----------------------- | -------------------------------------------- |
| 🔐 Authentication       | Secure registration and login                |
| 🛡️ JWT Security        | Token-based authentication and authorization |
| 👥 Role Management      | Separate Admin and Student access            |
| 📊 Admin Dashboard      | Centralized examination management           |
| 📝 Exam Management      | Create, edit, publish and manage exams       |
| 📚 Question Management  | Create and manage programming questions      |
| 🤖 AI Generation        | Automatically generate coding questions      |
| 📂 File-Based AI        | Generate questions from uploaded documents   |
| 💻 Online Coding        | Browser-based programming environment        |
| 🧑‍💻 Monaco Editor     | Modern VS Code-like code editor              |
| ⏱️ Live Timer           | Timed examination experience                 |
| ▶️ Code Execution       | Run and test submitted code                  |
| 🧪 Test Cases           | Validate programming solutions               |
| 📈 Automatic Evaluation | Evaluate coding submissions                  |
| 🏆 Leaderboard          | Compare student performance                  |
| 📊 Results              | View examination scores and performance      |
| 🐳 Docker               | Containerized application support            |
| ☁️ AWS EC2              | Cloud deployment support                     |

---

# 👥 User Roles

## 👨‍💼 Admin

Administrators have complete control over the examination platform.

```text
Admin Login
     │
     ▼
Admin Dashboard
     │
     ├── Manage Exams
     │
     ├── Manage Questions
     │
     ├── Generate AI Questions
     │
     ├── Upload Question Files
     │
     ├── Manage Students
     │
     └── View Results & Leaderboard
```

## 👨‍🎓 Student

Students use the platform to participate in coding examinations.

```text
Student Registration
        │
        ▼
      Login
        │
        ▼
Student Dashboard
        │
        ├── Available Exams
        │
        ├── Start Coding Exam
        │
        ├── Write & Run Code
        │
        ├── Submit Exam
        │
        ├── View Results
        │
        └── View Leaderboard
```

---

# 📸 Screenshots

## 🏠 Landing Page

<p align="center">
  <img src="./Documents/screenshots/home.png" width="95%" alt="CodeExam Landing Page">
</p>

---

## 🔐 Login & Registration

<table>
<tr>
<td width="50%" align="center">

### Login

<img src="./Documents/screenshots/login.png" width="100%" alt="Login">

</td>

<td width="50%" align="center">

### Registration

<img src="./Documents/screenshots/register.png" width="100%" alt="Registration">

</td>
</tr>
</table>

---

## 👨‍💼 Admin Dashboard

<p align="center">
  <img src="./Documents/screenshots/admin-dashboard.png" width="95%" alt="Admin Dashboard">
</p>

---

## 📝 Exam Management

<table>
<tr>
<td width="50%" align="center">

### Create Exam

<img src="./Documents/screenshots/create-exam.png" width="100%" alt="Create Exam">

</td>

<td width="50%" align="center">

### Manage Exams

<img src="./Documents/screenshots/manage-exams.png" width="100%" alt="Manage Exams">

</td>
</tr>
</table>

---

## 🤖 AI Question Generator

<p align="center">
  <img src="./Documents/screenshots/ai-question-generator.png" width="95%" alt="AI Question Generator">
</p>

---

## ✍️ Manual Question Creation

<p align="center">
  <img src="./Documents/screenshots/manual-add-question.png" width="95%" alt="Manual Question Creation">
</p>

---

## 📂 Upload Question File

<p align="center">
  <img src="./Documents/screenshots/upload-question-file.png" width="95%" alt="Upload Question File">
</p>

---

## 📚 Manage Questions

<p align="center">
  <img src="./Documents/screenshots/manage-questions.png" width="95%" alt="Manage Questions">
</p>

---

## 👨‍🎓 Manage Students

<p align="center">
  <img src="./Documents/screenshots/manage-students.png" width="95%" alt="Manage Students">
</p>

---

## 🏆 Global Leaderboard

<p align="center">
  <img src="./Documents/screenshots/global-leaderboard.png" width="95%" alt="Global Leaderboard">
</p>

---

## 👨‍🎓 Student Dashboard

<p align="center">
  <img src="./Documents/screenshots/student-dashboard.png" width="95%" alt="Student Dashboard">
</p>

---

## 💻 Online Coding Exam

<p align="center">
  <img src="./Documents/screenshots/coding-exam.png" width="95%" alt="Coding Exam">
</p>

---

## ✅ Exam Submission

<p align="center">
  <img src="./Documents/screenshots/exam-submitted.png" width="95%" alt="Exam Submitted">
</p>

---

# 👨‍💼 Admin Features

## 🔐 Authentication

* Secure Admin Login
* JWT-based Authentication
* Role-based Authorization

## 📊 Dashboard

The Admin Dashboard provides a centralized overview of the platform.

* Total Exams
* Total Students
* Total Questions
* Examination Statistics
* Quick Management Actions

## 📝 Exam Management

Administrators can:

* Create examinations
* Edit examinations
* Delete examinations
* Publish examinations
* Unpublish examinations
* Configure examination duration
* Configure total marks

## 📚 Question Management

Administrators can:

* Add questions manually
* Edit questions
* Delete questions
* View questions
* Manage test cases
* Assign questions to examinations

## 🤖 AI Question Generation

Administrators can:

* Generate programming questions using AI
* Generate multiple questions
* Select programming language
* Select difficulty
* Configure marks
* Generate questions from a topic
* Generate questions from uploaded documents

## 👨‍🎓 Student Management

Administrators can:

* View registered students
* View student details
* Manage student accounts
* Monitor participation
* Track examination activity
* View student performance

## 🏆 Results & Leaderboard

Administrators can:

* View examination results
* Monitor student scores
* View the global leaderboard

---

# 👨‍🎓 Student Features

## 🔐 Authentication

* Student Registration
* Student Login
* Secure Authentication

## 📊 Student Dashboard

Students can access:

* Available examinations
* Upcoming examinations
* Completed examinations
* Examination results
* Leaderboard
* Performance information

## 💻 Coding Examination

The online coding environment allows students to:

* Read programming problems
* Select programming language
* Write code directly in the browser
* Run code
* Compile code
* Execute test cases
* Reset code
* Navigate between questions
* Use the question palette
* Track the live examination timer
* Submit answers

The platform uses **Monaco Editor** to provide a modern browser-based coding experience.

---

# 🤖 AI Question Generator

CodeExam includes an AI-powered question generation system that helps administrators create programming questions automatically.

## Generate Questions From a Topic

Administrators can provide parameters such as:

```text
Topic:
Java Arrays

Programming Language:
Java

Difficulty:
Medium

Marks:
10

Number of Questions:
5
```

The generated questions contain:

* Problem Statement
* Input Format
* Output Format
* Constraints
* Sample Input
* Sample Output
* Test Cases

## 🔄 AI Processing Pipeline

```text
┌─────────────────────┐
│     AI Request      │
└──────────┬──────────┘
           ▼
┌─────────────────────┐
│ Generate Questions  │
└──────────┬──────────┘
           ▼
┌─────────────────────┐
│ Receive AI Response │
└──────────┬──────────┘
           ▼
┌─────────────────────┐
│    Parse JSON       │
└──────────┬──────────┘
           ▼
┌─────────────────────┐
│   Validate Data     │
└──────────┬──────────┘
           ▼
┌─────────────────────┐
│ Create Questions    │
└──────────┬──────────┘
           ▼
┌─────────────────────┐
│   Save to Database  │
└──────────┬──────────┘
           ▼
┌─────────────────────┐
│ Assign to Exam      │
└─────────────────────┘
```

---

# 📂 AI File Upload Generation

Administrators can upload programming-related documents and use their content to generate coding questions.

## 📄 Supported File Formats

```text
PDF
DOC
DOCX
TXT
MD
```

## 🔄 Processing Flow

```text
Upload Document
       │
       ▼
Extract Document Content
       │
       ▼
Send Content to AI
       │
       ▼
Generate Questions
       │
       ▼
Parse JSON Response
       │
       ▼
Validate Questions
       │
       ▼
Save to Database
       │
       ▼
Assign Questions to Exam
```

---

# 💻 Online Coding Exam

The coding examination interface provides students with an interactive programming environment.

### Students can:

```text
┌─────────────────────────────┐
│       Coding Problem        │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│       Monaco Editor         │
│                             │
│       Write Code            │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│       Run / Compile         │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│       Test Cases            │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│       Submit Solution       │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│    Automatic Evaluation     │
└─────────────────────────────┘
```

---

# 🏆 Results & Leaderboard

## 📊 Examination Results

After submitting an examination, students can view their performance.

The result system provides:

* Total Marks
* Obtained Marks
* Questions Attempted
* Correct Answers
* Incorrect Answers
* Final Score
* Performance Summary

## 🏆 Global Leaderboard

The global leaderboard allows students to compare their performance with other participants.

```text
Rank     Student        Score
────────────────────────────
🥇 1     Student A      95
🥈 2     Student B      90
🥉 3     Student C      87
```

---

# 🛠️ Tech Stack

## 🎨 Frontend

| Technology            | Purpose                        |
| --------------------- | ------------------------------ |
| React 19              | UI development                 |
| TypeScript            | Type-safe frontend development |
| Vite                  | Frontend build tool            |
| React Router / Wouter | Client-side routing            |
| Axios                 | API communication              |
| Monaco Editor         | Online code editor             |
| Chart.js              | Data visualization             |
| HTML5                 | Application structure          |
| CSS3                  | Styling                        |

## ⚙️ Backend

| Technology      | Purpose                        |
| --------------- | ------------------------------ |
| Java 17         | Backend programming language   |
| Spring Boot 3.2 | Backend framework              |
| Spring Web      | REST APIs                      |
| Spring Data JPA | Database interaction           |
| Spring Security | Authentication & authorization |
| JWT             | Token-based authentication     |
| Hibernate       | ORM                            |

## 🗄️ Database

| Technology | Usage               |
| ---------- | ------------------- |
| MySQL      | Production database |
| H2         | Development/testing |

## 🔧 Additional Technologies

* Maven
* npm
* Docker
* Apache PDFBox
* Apache POI
* Gson
* Git
* GitHub
* AWS EC2

---

# 🏗️ System Architecture

```text
                       ┌──────────────────┐
                       │      User        │
                       │ Browser / Client │
                       └────────┬─────────┘
                                │
                                ▼
                  ┌──────────────────────────┐
                  │        Frontend          │
                  │     React + TypeScript   │
                  │       + Vite             │
                  └────────────┬─────────────┘
                               │
                         REST API / HTTP
                               │
                               ▼
                  ┌──────────────────────────┐
                  │        Backend           │
                  │ Spring Boot + Java 17    │
                  │ Spring Security + JWT    │
                  └────────────┬─────────────┘
                               │
                    ┌──────────┴──────────┐
                    │                     │
                    ▼                     ▼
          ┌─────────────────┐   ┌─────────────────┐
          │     MySQL       │   │   AI Services   │
          │    Database     │   │ Question Engine │
          └─────────────────┘   └─────────────────┘
```

---

# 📁 Project Structure

```text
CodeExam/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── ...
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   └── ...
│
├── frontend/
│   └── frontend/
│       ├── src/
│       │   ├── components/
│       │   ├── pages/
│       │   ├── services/
│       │   ├── types/
│       │   └── ...
│       ├── package.json
│       ├── vite.config.ts
│       └── ...
│
├── database/
│   └── ...
│
├── Documents/
│   └── screenshots/
│       ├── home.png
│       ├── login.png
│       ├── register.png
│       ├── admin-dashboard.png
│       ├── create-exam.png
│       ├── manage-exams.png
│       ├── ai-question-generator.png
│       ├── manual-add-question.png
│       ├── upload-question-file.png
│       ├── manage-questions.png
│       ├── manage-students.png
│       ├── global-leaderboard.png
│       ├── student-dashboard.png
│       ├── coding-exam.png
│       └── exam-submitted.png
│
├── docker-compose.yml
├── start-system.bat
├── README.md
└── TODO.md
```

---

# 🚀 How to Run

## 1️⃣ Clone the Repository

```bash
git clone https://github.com/Sumit-Kadam-07/CodeExam.git

cd CodeExam
```

---

## 2️⃣ Configure MySQL

Make sure MySQL is installed and running.

Create the database:

```sql
CREATE DATABASE codeexam;
```

Configure the database connection:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/codeexam
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

---

## 3️⃣ Run the Backend

Navigate to the backend:

```bash
cd backend
```

Run Spring Boot:

```bash
mvn spring-boot:run
```

The backend will start and provide the REST APIs required by the frontend.

---

## 4️⃣ Run the Frontend

Open another terminal:

```bash
cd frontend/frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Open the URL provided by Vite in your browser.

---

# 🐳 Docker Deployment

CodeExam includes Docker configuration for simplified application deployment.

## ▶️ Start Containers

From the project root:

```bash
docker-compose up --build
```

## 🚀 Run in Background

```bash
docker-compose up -d --build
```

## 🔍 Check Containers

```bash
docker ps
```

## 📋 View Logs

```bash
docker-compose logs
```

Follow logs continuously:

```bash
docker-compose logs -f
```

## 🛑 Stop Application

```bash
docker-compose down
```

## 🔄 Rebuild Containers

```bash
docker-compose up -d --build
```

---

# 🐳 Docker Architecture

```text
                  ┌─────────────────────┐
                  │       Browser       │
                  └──────────┬──────────┘
                             │
                             ▼
                  ┌─────────────────────┐
                  │ Frontend Container  │
                  │    React + Vite     │
                  └──────────┬──────────┘
                             │
                             ▼
                  ┌─────────────────────┐
                  │ Backend Container   │
                  │ Spring Boot + Java  │
                  └──────────┬──────────┘
                             │
                             ▼
                  ┌─────────────────────┐
                  │ Database Container  │
                  │       MySQL         │
                  └─────────────────────┘
```

---

# ⚙️ Configuration

## 🗄️ Database Configuration

CodeExam uses MySQL as its primary database.

Create the database:

```sql
CREATE DATABASE codeexam;
```

Configure `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/codeexam
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

---

## 🔐 JWT Configuration

Configure your JWT secret:

```properties
jwt.secret=YOUR_SECRET_KEY
```

> ⚠️ Use a strong secret key for production deployments.

---

## 🌐 Frontend API Configuration

For local development:

```env
VITE_API_URL=http://localhost:8080
```

For production:

```env
VITE_API_URL=https://your-backend-domain.com
```

---

## 🔑 Environment Variables

Example:

```env
DB_USERNAME=root
DB_PASSWORD=YOUR_PASSWORD
JWT_SECRET=YOUR_SECRET_KEY
VITE_API_URL=http://localhost:8080
```

> ⚠️ **Security:** Never commit passwords, API keys, JWT secrets, or other sensitive credentials to GitHub.

### ✅ Configuration Checklist

```text
✓ MySQL is installed and running
✓ codeexam database is created
✓ Database credentials are configured
✓ JWT secret is configured
✓ Backend API URL is configured
✓ Required environment variables are available
✓ Frontend dependencies are installed
✓ Backend dependencies are installed
```

---

# 🔐 Authentication & Security

CodeExam provides authentication and role-based authorization to protect users and application resources.

## 🔑 Authentication Flow

```text
User Login
    │
    ▼
Authenticate Credentials
    │
    ▼
Generate JWT Token
    │
    ▼
Store / Send Token
    │
    ▼
Authenticated API Requests
    │
    ▼
Validate JWT
    │
    ▼
Role-Based Authorization
    │
    ▼
Protected Resources
```

## 👨‍💼 Admin Authorization

Admins can:

* Create and manage examinations
* Add and manage questions
* Generate AI questions
* Upload files for question generation
* Manage students
* View examination data
* Monitor results
* Access leaderboard information

## 👨‍🎓 Student Authorization

Students can:

* Access available examinations
* Attempt coding questions
* Submit solutions
* View results
* View leaderboard rankings

---

# 🧪 Testing

CodeExam can be tested at both backend and frontend levels.

## Backend Testing

Navigate to:

```bash
cd backend
```

Run tests:

```bash
mvn test
```

Run a clean test build:

```bash
mvn clean test
```

---

## Frontend Build Testing

Navigate to:

```bash
cd frontend/frontend
```

Install dependencies:

```bash
npm install
```

Build the application:

```bash
npm run build
```

A successful build verifies that the React and TypeScript application can be compiled successfully.

---

## 🧪 Manual Testing Flow

```text
Authentication
      ↓
Registration & Login
      ↓
Admin Dashboard
      ↓
Create Examination
      ↓
Add / Generate Questions
      ↓
Manage Students
      ↓
Student Dashboard
      ↓
Attempt Coding Exam
      ↓
Run & Test Code
      ↓
Submit Examination
      ↓
View Results
      ↓
Leaderboard
```

## ✅ Functional Testing Checklist

```text
✓ User Registration
✓ User Login
✓ JWT Authentication
✓ Admin Dashboard
✓ Create Examination
✓ Edit Examination
✓ Manage Examination
✓ Add Questions Manually
✓ Generate Questions Using AI
✓ Upload Files for AI Generation
✓ Manage Questions
✓ Manage Students
✓ Student Dashboard
✓ Coding Examination
✓ Code Execution
✓ Test Cases
✓ Exam Timer
✓ Exam Submission
✓ Result Calculation
✓ Leaderboard
```

---

# 📦 Production Build

## Backend

```bash
cd backend

mvn clean package
```

The generated JAR will be available inside:

```text
backend/target/
```

## Frontend

```bash
cd frontend/frontend

npm run build
```

The production files will be generated inside:

```text
dist/
```

---

# 🚧 Future Enhancements

The platform can be further expanded with advanced features.

### 🤖 AI & Question Management

* Advanced AI-powered question generation
* Large question bank
* Topic and category filtering
* Automated examination creation
* Randomized questions for every examination

### 📊 Analytics

* Detailed student performance analytics
* Advanced admin analytics dashboard
* Performance trends
* Personalized learning insights

### 🔔 Communication

* Email notifications
* Examination reminders
* Result notifications

### 🏆 Gamification

* Advanced ranking system
* Achievements
* Badges
* Competitive coding statistics

### 🌐 Platform Improvements

* Support for additional programming languages
* Improved mobile responsiveness
* Cloud scalability
* Performance optimization
* Additional security improvements

---

# 🎯 Long-Term Vision

```text
       🤖 AI Question Generation
                │
                ▼
       📚 Large Question Bank
                │
                ▼
     📝 Automated Exam Creation
                │
                ▼
      💻 Online Coding Exam
                │
                ▼
       ⚡ Automated Evaluation
                │
                ▼
        📊 Performance Analytics
                │
                ▼
     🎯 Personalized Insights
```

The long-term goal is to evolve **CodeExam** into a complete and scalable platform for programming assessments, technical examinations, automated evaluation, and performance analysis.

---

# 📄 License

This project is developed for **educational and demonstration purposes**.

The source code is available on GitHub for learning, development, and reference.

---

# 👨‍💻 Developer

<p align="center">

### **Sumit Kadam**

<a href="https://github.com/Sumit-Kadam-07/CodeExam">
  <img src="https://img.shields.io/badge/GitHub-CodeExam-181717?style=for-the-badge&logo=github" alt="GitHub Repository">
</a>

</p>

---

<p align="center">

⭐ **If you find CodeExam useful, consider giving the repository a star!**

<br>

<b>Built with ❤️ using React, TypeScript, Java, Spring Boot and MySQL.</b>

</p>
