# 🏥 TeleHealth System (THS-Enhanced)

**Course:** COIT20258 – Software Engineering
**Team:** G1 – TeleHealth System
**Institution:** CQUniversity
**Language & Tools:** JavaFX, MySQL, NetBeans, MVC Architecture

---

## Overview

The **TeleHealth System (THS-Enhanced)** is a distributed desktop application developed in JavaFX using the MVC pattern.
It allows **patients, doctors, and administrators** to interact through a centralized health service platform.

The key goal is to **enhance digital healthcare accessibility** through secure login, online appointment booking, health record sharing, and doctor-to-hospital referrals.

---

## Team Members

| Name                   | Role                                             | Responsibilities                                                                                                                      |
| ---------------------- | ------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------- |
| **Sobiha (Team Leader)** | Patient Module Developer & Dashboard Enhancement | Designed database schema and ERD, developed Health Report and Prescription modules, managed SQL integration and team milestones.      |
| **Gihani De Silva**    | Signup, login, and booking appointment & Tester   | Implemented Login/Signup, Book Consultation, linked GUI with MySQL backend, performed testing & documentation. |
| **Ajay**                | Doctor & Admin Module Developer                  | Implemented Doctor Diagnosis and Hospital Referral modules, Admin staff management, contributed to integration and debugging.         |

---

## Project Structure

```
TeleHealthSystem/
│
├── src/
│   ├── application/            # Main JavaFX entry point
│   ├── controllers/            # MVC Controllers (Login, Dashboard, BookConsultation, etc.)
│   ├── models/                 # POJOs (User, Booking, Diagnosis, HealthReport, HospitalReferral)
│   ├── services/               # Database interaction (DiagnosisService, BookingService)
│   ├── utils/                  # Helper classes (DatabaseHelper)
│   └── views/                  # FXML and CSS files
│
├── db/
│   ├── Telehealth_System.sql              # Database structure
│ 
│
|
│
└── README.md                   # Project setup
```

---

##  Installation & Setup

### 1. Prerequisites

* Java **JDK 20+**
* **MySQL 8+**
* **NetBeans 19+**
* JavaFX SDK properly configured (set `--module-path` and `--add-modules javafx.controls,javafx.fxml`)

### 2. Database Setup
# Run the existing SQL schema
db/Telehealth_System.sql


### 3. Update Database Configuration

Edit **DatabaseHelper.java** if needed:

```java
private static final String URL = "jdbc:mysql://localhost:3306/telehealth_system";
private static final String USER = "root";
private static final String PASSWORD = "yourpassword";
```

### 4. Run the Application

1. Open project in **NetBeans**
2. Clean & Build
3. Run `application.Main`

---

## Test Accounts

| Role        | Username | Password  |
| ----------- | ---------| --------- |
| **Admin**   | Admin    | 12345     |
| **Doctor**  | Emily    | 12345     |
| **Patient** | Alice    | 12345     |

---

## Features

### Core Functionalities

| Module                  | Description                                                                                    |
| ----------------------- | ---------------------------------------------------------------------------------------------- |
| **Login/Signup**        | Secure authentication for Admin, Doctor, and Patient.                                          |
| **Dashboard**           | Role-based navigation (Admin – Staff; Doctor – Diagnosis/Referral; Patient – Booking/Reports). |
| **Book Consultation**   | Patients book appointments with available doctors.                                             |
| **Doctor Diagnosis**    | Doctors record patient diagnoses, prescriptions, and referrals.                                |
| **Health Report**       | Patients can view diagnosis history with doctor details.                                       |
| **Prescription Refill** | Patients can request prescription renewals.                                                    |
| **Vitals Record**       | Patients can log and visualize health metrics (BMI, BP, etc.).                                 |

---

## Additional (THS-Enhanced) Features

### 1**Hospital Referral**

Doctors can directly refer patients to hospitals for critical conditions.
Each referral includes hospital name, urgency level, reason, and timestamp.

### 2️**Patient Health Report**

Patients can access an auto-generated report consolidating diagnoses, prescriptions, and referrals from all visits.

---
