# 🚀 Online Exam System – Java (Spring Boot)

<p align="center">
<img src="https://img.shields.io/badge/Java-17-blue?style=for-the-badge&logo=java&logoColor=white" alt="Java 17">
<img src="https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.x">
<img src="https://img.shields.io/badge/JPA%20%2F%20Hibernate-red?style=for-the-badge" alt="JPA / Hibernate">
<img src="https://img.shields.io/badge/MySQL-blue?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL">
</p>

A comprehensive **Smart Coding Exam System** built using **Spring Boot, Spring Security, Thymeleaf, Bootstrap 5**, and **JPA/Hibernate**.  
The platform provides a secure and user-friendly environment for **Admins** and **Students** to manage and take coding exams effectively.


✔️ Completely Free  
✔️ Full Source Code Included

---


<p align="center">
<a href="https://www.youtube.com/c/LazyCoderOnline?sub_confirmation=1">
<img src="https://img.shields.io/badge/Subscribe-LazyCoder-red?style=for-the-badge&logo=youtube" >
</a>

<a href="https://wa.me/919572181024">
<img src="https://img.shields.io/badge/WhatsApp-Chat%20Now-green?style=for-the-badge&logo=whatsapp" >
</a>
</p>

---

# 📸 Screenshots


<table width="100%">


<tr>
<td align="center"><b>Exam Page (with Pagination)</b></td>
</tr>
<tr>
<td align="center"><img src="https://github.com/sumitkumar1503/online-exam-system/blob/master/screenshots/exampage.png" width="90%"></td>
</tr>

<tr>
<td align="center"><b>Admin Dashboard</b></td>
</tr>
<tr>
<td align="center"><img src="https://github.com/sumitkumar1503/online-exam-system/blob/master/screenshots/admindashboard.png?raw=true" width="90%"></td>
</tr>

<tr>
<td align="center"><b>Manage Exam</b></td>
</tr>
<tr>
<td align="center"><img src="https://github.com/sumitkumar1503/online-exam-system/blob/master/screenshots/adminmanageexam.png" width="90%"></td>
</tr>

<tr>
<td align="center"><b>Manage Question</b></td>
</tr>
<tr>
<td align="center"><img src="https://github.com/sumitkumar1503/online-exam-system/blob/master/screenshots/adminmanagequestion.png" width="90%"></td>
</tr>
</table>

---

# ✨ Features

## 👨‍💻 Admin Features
- Secure Admin Login
- Stats Dashboard (Total Students, Exams, Questions, Submissions)
- **Exam CRUD** (title, duration, description)
- **Question CRUD** per exam
- Cascade deletes for exams → questions → results
- Protect answered questions from accidental delete
- Manage Students
- Reset Student Password
- Delete Student Account (cascade all related data)
- View all submissions for any exam

---

## 🧑‍🎓 Student Features
- Student Registration (Full Name, Email, Mobile, Profile Picture)
- Secure Login
- Dashboard with KPIs + Performance Chart
- Take Exam (paginated interface + question palette)
- Live Timer (auto submit)
- Instant Results (score, percentage, pass/fail)
- Detailed Review Page (correct vs incorrect answers)
- Profile Update
- Upload New Profile Picture
- Change Password
- View All Previous Exam Results

---

# 🛠️ Tech Stack

| Layer | Technology                                 |
|------|--------------------------------------------|
| Backend | Spring Boot 3, Spring Security 6           |
| Frontend | Thymeleaf, Html, Bootstrap 5, Chart.js     |
| Database | MySQL |
| ORM | Hibernate / JPA                            |
| Build | Maven                                      |
| Storage | Local File System for images               |

---

## 🚀 How to Run the Project (Dev & Prod)

### ✔️ Prerequisites
- Java 17+ (or matching your local JDK)
- Node.js + npm (for frontend dev/build)
- Git, Maven

### ✔️ Development (run backend + frontend)
You can run the backend (Spring Boot) and the frontend (Vite) separately during development.

From project root, start both in separate PowerShell windows using the helper script:

```powershell
.\scripts\run-dev.ps1
```

Or start individually:

Backend (from project root):
```powershell
.\mvnw.cmd spring-boot:run
```

Frontend (from project root):
```powershell
cd frontend
npm install
npm run dev
```

The backend runs on http://localhost:5000 by default. Vite dev server runs on http://localhost:5173 and proxies `/api` to the backend.

### ✔️ Production (single server)
Build the frontend and let Spring Boot serve the static files:

```powershell
cd frontend
npm install
npm run build
cd ..
.\mvnw.cmd -DskipTests package
java -jar target/online-exam-0.0.1-SNAPSHOT.jar
```

After a production build the frontend bundle is placed under `backend/main/resources/static` and will be served by Spring Boot.

### 🗄️ Database (MySQL)

Create the schema before first run:

```powershell
mysql -u root -p < database/schema_mysql.sql
```

Default connection:
```
Host: localhost:3306
Database: smart_coding_exam
User: root
Password: (empty)
```

Configure via environment variables if needed:
- `MYSQL_URL`
- `MYSQL_USER`
- `MYSQL_PASSWORD`

---

# 📜 License

This project is **open-source** under the **MIT License**.

---

<p align="center">
<strong>Happy Coding ❤️</strong>
</p>
