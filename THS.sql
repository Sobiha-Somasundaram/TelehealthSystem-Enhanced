-- MySQL dump 10.13  Distrib 8.0.42, for macos15 (arm64)
--
-- Host: localhost    Database: telehealth_system
-- ------------------------------------------------------
-- Server version	8.4.5

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `appointments`
--

DROP TABLE IF EXISTS `appointments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appointments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `patient_name` varchar(255) NOT NULL,
  `doctor_name` varchar(255) DEFAULT NULL,
  `appointment_date` date NOT NULL,
  `appointment_time` time NOT NULL,
  `status` enum('SCHEDULED','COMPLETED','CANCELLED') DEFAULT 'SCHEDULED',
  `booking_id` int DEFAULT NULL,
  `appointment_type` varchar(50) DEFAULT 'General',
  `notes` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appointments`
--

LOCK TABLES `appointments` WRITE;
/*!40000 ALTER TABLE `appointments` DISABLE KEYS */;
INSERT INTO `appointments` VALUES (1,'John Doe','Dr. Smith','2025-10-20','09:00:00','SCHEDULED',NULL,'General',NULL),(2,'Jane Roe','Dr. Brown','2025-10-21','09:00:00','COMPLETED',NULL,'General',NULL),(3,'Alice Johnson','Dr. Smith','2025-10-22','08:00:00','SCHEDULED',NULL,'General',NULL),(4,'Bob Williams','Dr. Taylor','2025-10-23','11:00:00','CANCELLED',NULL,'General',NULL),(5,'Mary Davis','Dr. Brown','2025-10-24','10:00:00','COMPLETED',NULL,'General',NULL);
/*!40000 ALTER TABLE `appointments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `booking_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `doctor_id` int NOT NULL,
  `appointment_date` date NOT NULL,
  `appointment_time` time NOT NULL,
  `symptoms` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('Pending','Approved','Cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'Pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `consultation_mode` enum('Video','Audio') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Video',
  PRIMARY KEY (`booking_id`),
  KEY `patient_id` (`patient_id`),
  KEY `doctor_id` (`doctor_id`),
  CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
INSERT INTO `bookings` VALUES (3,6,4,'2025-10-14','11:00:00','Heada','Pending','2025-10-11 00:20:38','Video'),(4,6,3,'2025-10-16','12:00:00','Cold','Pending','2025-10-11 00:25:34','Video'),(5,6,4,'2025-10-14','11:00:00','Cold','Pending','2025-10-11 02:01:55','Video'),(6,6,4,'2025-10-15','10:00:00','Cold','Pending','2025-10-12 01:41:48','Video'),(7,6,3,'2025-10-19','11:00:00','Blood test','Pending','2025-10-14 13:01:40','Video'),(8,6,7,'2025-10-15','16:00:00','visar','Pending','2025-10-14 13:24:46','Video'),(9,6,4,'2025-10-21','09:00:00','Flue','Pending','2025-10-14 13:55:07','Video'),(10,6,3,'2025-10-15','12:00:00','Audio','Pending','2025-10-14 13:56:05','Video'),(11,6,4,'2025-10-15','10:00:00','A','Pending','2025-10-14 14:00:55','Audio'),(12,10,3,'2025-10-16','10:00:00','Fever','Pending','2025-10-14 14:24:02','Audio'),(13,10,4,'2025-10-17','10:00:00','Fever','Pending','2025-10-14 14:54:50','Audio'),(14,6,4,'2025-10-17','11:00:00','test','Pending','2025-10-15 11:38:01','Audio'),(15,6,3,'2025-10-15','10:00:00','Fever','Pending','2025-10-16 11:40:52','Video');
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diagnoses`
--

DROP TABLE IF EXISTS `diagnoses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diagnoses` (
  `id` int NOT NULL AUTO_INCREMENT,
  `appointment_id` int DEFAULT NULL,
  `patient_name` varchar(255) NOT NULL,
  `doctor_name` varchar(255) NOT NULL,
  `diagnosis_text` text NOT NULL,
  `symptoms` text,
  `prescription_details` text,
  `treatment_plan` text,
  `follow_up_instructions` text,
  `recorded_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `severity` enum('MILD','MODERATE','SEVERE') DEFAULT 'MODERATE',
  `status` enum('ACTIVE','ONGOING','RESOLVED') DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  KEY `appointment_id` (`appointment_id`),
  CONSTRAINT `diagnoses_ibfk_1` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diagnoses`
--

LOCK TABLES `diagnoses` WRITE;
/*!40000 ALTER TABLE `diagnoses` DISABLE KEYS */;
INSERT INTO `diagnoses` VALUES (1,1,'John Doe','Dr. Smith','Flu-like symptoms','Fever, cough, fatigue','Paracetamol 500mg, rest','Stay hydrated, monitor temperature','Follow-up in 1 week','2025-10-17 00:46:59','MODERATE','ACTIVE'),(2,2,'Jane Roe','Dr. Brown','Sprained ankle','Swelling, pain','Ice packs, pain relief','Rest, elevate leg, physiotherapy exercises','Follow-up in 2 weeks','2025-10-17 00:46:59','MILD','RESOLVED'),(3,3,'Alice Johnson','Dr. Smith','Migraine','Headache, nausea','Ibuprofen 400mg','Avoid triggers, rest in dark room','Follow-up if persistent','2025-10-17 00:46:59','SEVERE','ONGOING'),(4,5,'Mary Davis','Dr. Brown','Allergic reaction','Rash, itching','Antihistamines','Avoid allergens, monitor symptoms','Follow-up if symptoms worsen','2025-10-17 00:46:59','MODERATE','RESOLVED'),(5,2,'Jane Roe','Dr. System','dfwd','sd','dcdw','cd','dd','2025-10-17 00:00:00','MODERATE','ONGOING');
/*!40000 ALTER TABLE `diagnoses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `doctor_diagnoses`
--

DROP TABLE IF EXISTS `doctor_diagnoses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctor_diagnoses` (
  `diagnosis_id` int NOT NULL AUTO_INCREMENT,
  `patient_name` varchar(255) NOT NULL,
  `doctor_name` varchar(255) NOT NULL,
  `diagnosis` text NOT NULL,
  `treatment` text,
  `notes` text,
  `diagnosis_date` date DEFAULT (curdate()),
  `status` enum('ONGOING','RESOLVED','REFERRED') DEFAULT 'ONGOING',
  PRIMARY KEY (`diagnosis_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctor_diagnoses`
--

LOCK TABLES `doctor_diagnoses` WRITE;
/*!40000 ALTER TABLE `doctor_diagnoses` DISABLE KEYS */;
/*!40000 ALTER TABLE `doctor_diagnoses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `health_reports`
--

DROP TABLE IF EXISTS `health_reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `health_reports` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `generated_by` int DEFAULT NULL,
  `summary` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `latest_vitals_id` int DEFAULT NULL,
  `latest_booking_id` int DEFAULT NULL,
  `latest_prescription_id` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`report_id`),
  KEY `user_id` (`user_id`),
  KEY `fk_health_reports_vitals` (`latest_vitals_id`),
  KEY `fk_health_reports_booking` (`latest_booking_id`),
  KEY `fk_health_reports_prescription` (`latest_prescription_id`),
  CONSTRAINT `fk_health_reports_booking` FOREIGN KEY (`latest_booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_health_reports_prescription` FOREIGN KEY (`latest_prescription_id`) REFERENCES `prescription_refills` (`refill_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_health_reports_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_health_reports_vitals` FOREIGN KEY (`latest_vitals_id`) REFERENCES `vitals_records` (`vitals_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `health_reports`
--

LOCK TABLES `health_reports` WRITE;
/*!40000 ALTER TABLE `health_reports` DISABLE KEYS */;
INSERT INTO `health_reports` VALUES (5,6,4,'? TELEHEALTH SYSTEM - HEALTH REPORT\n------------------------------------------\nPatient Name     : Gihani\nDoctor/Specialist: Dr. Dr. Emily Brown\nAppointment Date : 2025-10-15\nAppointment Time : 10:00:00\nReport Generated : 2025-10-14T23:16:42.008132\n\n? PRESCRIPTION REFILL\n• Medication Name : Parasitamol\n• Quantity        : 7\n• Status          : Pending\n\n? VITAL SIGNS\n• Pulse: 12 (60–100 bpm) → Low Alert\n• Temperature: 12.0 (36.0–37.5 °C) → Low Alert\n• Respiration: 12 (12–20 breaths/min) → Normal\n• Blood Pressure: 120 (120/80 mmHg) → Normal\n• Oxygen: 2.0 (95–100%) → Low Alert\n\n? DOCTOR\'S ADVICE\nNo vitals submitted.',3,6,3,'2025-10-14 12:16:49'),(6,6,3,'? TELEHEALTH SYSTEM - HEALTH REPORT\n------------------------------------------\nPatient Name     : Gihani\nDoctor/Specialist: Dr. Dr. John Smith\nAppointment Date : 2025-10-19\nAppointment Time : 11:00:00\nReport Generated : 2025-10-15T00:03:29.351375\n\n? PRESCRIPTION REFILL\n• Medication Name : vit D\n• Quantity        : 10\n• Status          : Pending\n\n? VITAL SIGNS\n• Pulse: 30 (60–100 bpm) → Low Alert\n• Temperature: 30.0 (36.0–37.5 °C) → Low Alert\n• Respiration: 30 (12–20 breaths/min) → High Alert\n• Blood Pressure: 30/80 (120/80 mmHg) → Normal\n• Oxygen: 30.0 (95–100%) → Low Alert\n\n? DOCTOR\'S ADVICE\nNo vitals submitted.',4,7,4,'2025-10-14 13:03:41'),(7,6,7,'? TELEHEALTH SYSTEM - HEALTH REPORT\n------------------------------------------\nPatient Name     : Gihani\nDoctor/Specialist: Dr. Sobi\nAppointment Date : 2025-10-15\nAppointment Time : 16:00:00\nReport Generated : 2025-10-15T00:25:42.549236\n\n? PRESCRIPTION REFILL\n• Medication Name : visar kulusa\n• Quantity        : 2\n• Status          : Pending\n\n? VITAL SIGNS\n• Pulse: 3 (60–100 bpm) → Low Alert\n• Temperature: 3.0 (36.0–37.5 °C) → Low Alert\n• Respiration: 3 (12–20 breaths/min) → Low Alert\n• Blood Pressure: 3 (120/80 mmHg) → Normal\n• Oxygen: 3.0 (95–100%) → Low Alert\n\n? DOCTOR\'S ADVICE\nNo vitals submitted.',9,8,NULL,'2025-10-14 13:25:51'),(8,10,4,'? TELEHEALTH SYSTEM - HEALTH REPORT\n------------------------------------------\nPatient Name     : Rathi\nDoctor/Specialist: Dr. Dr. Emily Brown\nAppointment Date : 2025-10-17\nAppointment Time : 10:00:00\nReport Generated : 2025-10-15T01:56:04.916303\n\n? PRESCRIPTION REFILL\n• Medication Name : Panadol\n• Quantity        : 20\n• Status          : Pending\n\n? VITAL SIGNS\n• Pulse: 30 (60–100 bpm) → Low Alert\n• Temperature: 32.0 (36.0–37.5 °C) → Low Alert\n• Respiration: 32 (12–20 breaths/min) → High Alert\n• Blood Pressure: 120/80 (120/80 mmHg) → Normal\n• Oxygen: 32.0 (95–100%) → Low Alert\n\n? DOCTOR\'S ADVICE\nNo vitals submitted.',10,13,7,'2025-10-14 14:56:23'),(9,6,4,'? TELEHEALTH SYSTEM - HEALTH REPORT\n------------------------------------------\nPatient Name     : Gihani\nDoctor/Specialist: Dr. Dr. Emily Brown\nAppointment Date : 2025-10-17\nAppointment Time : 11:00:00\nReport Generated : 2025-10-15T22:38:58.172292\n\n? PRESCRIPTION REFILL\n• Medication Name : Panadol\n• Quantity        : 10\n• Status          : Pending\n\n? VITAL SIGNS\n• Pulse: 10 (60–100 bpm) → Low Alert\n• Temperature: 10.0 (36.0–37.5 °C) → Low Alert\n• Respiration: 10 (12–20 breaths/min) → Low Alert\n• Blood Pressure: 10 (120/80 mmHg) → Normal\n• Oxygen: 10.0 (95–100%) → Low Alert\n\n? DOCTOR\'S ADVICE\nNo vitals submitted.',11,14,8,'2025-10-15 11:39:37');
/*!40000 ALTER TABLE `health_reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hospital_referrals`
--

DROP TABLE IF EXISTS `hospital_referrals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hospital_referrals` (
  `referral_id` int NOT NULL AUTO_INCREMENT,
  `patient_name` varchar(255) NOT NULL,
  `referring_doctor_name` varchar(255) DEFAULT NULL,
  `hospital_name` varchar(255) DEFAULT NULL,
  `department` varchar(255) DEFAULT NULL,
  `specialty_required` varchar(255) DEFAULT NULL,
  `reason_for_referral` text,
  `urgency_level` enum('LOW','MEDIUM','HIGH','EMERGENCY') DEFAULT 'MEDIUM',
  `referral_date` date DEFAULT (curdate()),
  `preferred_appointment_date` date DEFAULT NULL,
  `status` enum('PENDING','CONFIRMED','COMPLETED','CANCELLED') DEFAULT 'PENDING',
  `contact_number` varchar(50) DEFAULT NULL,
  `notes` text,
  PRIMARY KEY (`referral_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hospital_referrals`
--

LOCK TABLES `hospital_referrals` WRITE;
/*!40000 ALTER TABLE `hospital_referrals` DISABLE KEYS */;
INSERT INTO `hospital_referrals` VALUES (1,'Jane Roe','Dr. System','City General Hospital','Neurology','dfdf','cdsvd','HIGH','2025-10-17','2025-10-25','PENDING','435436788','dsfd');
/*!40000 ALTER TABLE `hospital_referrals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prescription_refills`
--

DROP TABLE IF EXISTS `prescription_refills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescription_refills` (
  `refill_id` int NOT NULL AUTO_INCREMENT,
  `patient_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `medication_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int NOT NULL,
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'Pending',
  `request_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`refill_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `prescription_refills_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prescription_refills`
--

LOCK TABLES `prescription_refills` WRITE;
/*!40000 ALTER TABLE `prescription_refills` DISABLE KEYS */;
INSERT INTO `prescription_refills` VALUES (1,'Gihani','Panadol',2,'once per week','Pending','2025-10-11 00:39:00',6),(2,'Gihani','Panadol',12,'Cold','Pending','2025-10-12 01:42:32',6),(3,'Gihani','Parasitamol',7,'Cold','Pending','2025-10-12 02:03:52',6),(4,'Gihani','vit D',10,'low vitamin','Pending','2025-10-14 13:02:25',6),(6,'Gihani','elevit',20,'reguler use','Pending','2025-10-14 14:42:24',6),(7,'Rathi','Panadol',20,'fever','Pending','2025-10-14 14:55:10',10),(8,'Gihani','Panadol',10,'fever','Pending','2025-10-15 11:38:26',6),(9,'Gihani','Amoxicillin',10,'Pain','Pending','2025-10-16 11:51:49',6);
/*!40000 ALTER TABLE `prescription_refills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('Patient','Doctor','Admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Alice Patient','Alice','12345','Patient','2025-10-10 01:39:04'),(2,'Bob Patient','Bob','12345','Patient','2025-10-10 01:39:04'),(3,'Dr. John Smith','John','12345','Doctor','2025-10-10 01:39:04'),(4,'Dr. Emily Brown','Emily','12345','Doctor','2025-10-10 01:39:04'),(5,'Admin User','Admin','12345','Admin','2025-10-10 01:39:04'),(6,'Gihani','G','123','Patient','2025-10-10 02:24:54'),(7,'Sobi','Sobi','123','Doctor','2025-10-10 02:27:10'),(8,'Aja','Aja','123','Admin','2025-10-10 02:28:14'),(9,'So','Sobiha','1234','Patient','2025-10-13 16:34:55'),(10,'Rathi','Rathi','123','Patient','2025-10-14 14:23:41');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vitals_records`
--

DROP TABLE IF EXISTS `vitals_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vitals_records` (
  `vitals_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `pulse` int DEFAULT NULL,
  `temperature` decimal(4,1) DEFAULT NULL,
  `respiration` int DEFAULT NULL,
  `blood_pressure` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `weight` decimal(5,2) DEFAULT NULL,
  `height` decimal(5,2) DEFAULT NULL,
  `oxygen` decimal(4,1) DEFAULT NULL,
  `recorded_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`vitals_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `vitals_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vitals_records`
--

LOCK TABLES `vitals_records` WRITE;
/*!40000 ALTER TABLE `vitals_records` DISABLE KEYS */;
INSERT INTO `vitals_records` VALUES (1,6,85,7.0,52,'120/85',55.00,150.00,5.0,'2025-10-11 00:43:12'),(2,6,52,4.0,25,'12/80',12.00,150.00,25.0,'2025-10-12 01:43:20'),(3,6,12,12.0,12,'120',15.00,140.00,2.0,'2025-10-12 02:04:34'),(4,6,30,30.0,30,'30/80',30.00,130.00,30.0,'2025-10-14 13:03:08'),(5,6,20,20.0,20,'20',20.00,20.00,20.0,'2025-10-14 13:04:27'),(6,6,12,12.0,12,'12',12.00,12.00,12.0,'2025-10-14 13:09:18'),(7,6,1,2.0,3,'4',5.00,6.00,7.0,'2025-10-14 13:11:25'),(8,6,1,1.0,1,'1',1.00,1.00,1.0,'2025-10-14 13:23:56'),(9,6,3,3.0,3,'3',3.00,3.00,3.0,'2025-10-14 13:25:39'),(10,10,30,32.0,32,'120/80',54.00,156.00,32.0,'2025-10-14 14:55:45'),(11,6,10,10.0,10,'10',10.00,10.00,10.0,'2025-10-15 11:38:45'),(12,6,72,30.0,12,'120/80',65.00,172.00,6.0,'2025-10-16 11:58:24'),(13,6,72,30.0,12,'120/80',65.00,172.00,100.0,'2025-10-16 11:59:41');
/*!40000 ALTER TABLE `vitals_records` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-17  1:35:40
