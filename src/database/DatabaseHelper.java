package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Fixed DatabaseHelper with proper schema updates
 */
public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.dir") + "/telehealth.db";

    // Force SQLite driver loading
    static {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("✓ SQLite JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ SQLite JDBC driver not found: " + e.getMessage());
        }
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        System.out.println("🔄 Initializing TeleHealth Database...");
        System.out.println("Database location: " + System.getProperty("user.dir") + "/telehealth.db");
        
        createUserTable();
        updateUserTableSchema(); // Add this to fix missing role column
        createAppointmentsTable();
        createDiagnosesTable();
        createHospitalReferralsTable();
        insertSampleData();
        System.out.println("✅ Database initialization completed successfully!");
    }

    private static void createUserTable() {
        String createUserTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                created_date TEXT DEFAULT CURRENT_TIMESTAMP
            );
        """;

        executeQuery(createUserTable, "Users table");
    }

    // Add missing columns to existing users table
    private static void updateUserTableSchema() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            // Check if role column exists
            var rs = stmt.executeQuery("PRAGMA table_info(users)");
            boolean hasRole = false;
            boolean hasEmail = false;
            boolean hasPhone = false;
            
            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("role".equals(columnName)) hasRole = true;
                if ("email".equals(columnName)) hasEmail = true;
                if ("phone".equals(columnName)) hasPhone = true;
            }
            
            // Add missing columns
            if (!hasRole) {
                stmt.execute("ALTER TABLE users ADD COLUMN role TEXT DEFAULT 'PATIENT'");
                System.out.println("✓ Added 'role' column to users table");
            }
            if (!hasEmail) {
                stmt.execute("ALTER TABLE users ADD COLUMN email TEXT");
                System.out.println("✓ Added 'email' column to users table");
            }
            if (!hasPhone) {
                stmt.execute("ALTER TABLE users ADD COLUMN phone TEXT");
                System.out.println("✓ Added 'phone' column to users table");
            }
            
        } catch (SQLException e) {
            System.out.println("✗ Schema update failed: " + e.getMessage());
        }
    }

    private static void createAppointmentsTable() {
        String createAppointmentsTable = """
            CREATE TABLE IF NOT EXISTS appointments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_name TEXT NOT NULL,
                specialist_name TEXT NOT NULL,
                appointment_date TEXT NOT NULL,
                time_slot TEXT NOT NULL,
                status TEXT DEFAULT 'SCHEDULED',
                consultation_type TEXT DEFAULT 'VIDEO',
                notes TEXT,
                created_date TEXT DEFAULT CURRENT_TIMESTAMP,
                updated_date TEXT DEFAULT CURRENT_TIMESTAMP
            );
        """;

        executeQuery(createAppointmentsTable, "Appointments table");
    }

    private static void createDiagnosesTable() {
        String createDiagnosesTable = """
            CREATE TABLE IF NOT EXISTS diagnoses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                appointment_id INTEGER,
                patient_name TEXT NOT NULL,
                doctor_name TEXT NOT NULL,
                diagnosis_text TEXT NOT NULL,
                symptoms TEXT,
                prescription_details TEXT,
                treatment_plan TEXT,
                follow_up_instructions TEXT,
                recorded_date TEXT NOT NULL,
                severity TEXT DEFAULT 'MODERATE',
                status TEXT DEFAULT 'ACTIVE',
                created_date TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (appointment_id) REFERENCES appointments (id)
            );
        """;

        executeQuery(createDiagnosesTable, "Diagnoses table");
    }

    private static void createHospitalReferralsTable() {
        String createHospitalReferralsTable = """
            CREATE TABLE IF NOT EXISTS hospital_referrals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_name TEXT NOT NULL,
                referring_doctor_name TEXT NOT NULL,
                hospital_name TEXT NOT NULL,
                department TEXT NOT NULL,
                specialty_required TEXT,
                reason_for_referral TEXT NOT NULL,
                urgency_level TEXT DEFAULT 'MEDIUM',
                referral_date TEXT NOT NULL,
                preferred_appointment_date TEXT,
                status TEXT DEFAULT 'PENDING',
                contact_number TEXT,
                notes TEXT,
                created_date TEXT DEFAULT CURRENT_TIMESTAMP
            );
        """;

        executeQuery(createHospitalReferralsTable, "Hospital referrals table");
    }

    private static void insertSampleData() {
        // Insert sample users for testing
        String insertSampleUsers = """
            INSERT OR IGNORE INTO users (name, username, password, role, email) VALUES 
            ('John Patient', 'patient1', 'password123', 'PATIENT', 'john@email.com'),
            ('Dr. Sarah Smith', 'doctor1', 'doctor123', 'DOCTOR', 'sarah.smith@hospital.com'),
            ('Nurse Mary Johnson', 'staff1', 'staff123', 'STAFF', 'mary.johnson@clinic.com'),
            ('Alice Brown', 'patient2', 'password456', 'PATIENT', 'alice@email.com'),
            ('Dr. Michael Wilson', 'doctor2', 'doctor456', 'DOCTOR', 'michael.wilson@medcenter.com'),
            ('Emma Davis', 'patient3', 'password789', 'PATIENT', 'emma@email.com'),
            ('Dr. Jennifer Lee', 'doctor3', 'doctor789', 'DOCTOR', 'jennifer.lee@hospital.com');
        """;

        executeQuery(insertSampleUsers, "Sample users");

        // Rest of sample data...
        String insertSampleAppointments = """
            INSERT OR IGNORE INTO appointments (patient_name, specialist_name, appointment_date, time_slot, status, consultation_type, notes) VALUES 
            ('John Patient', 'Dr. Sarah Smith', '2025-09-20', '10:00 AM', 'SCHEDULED', 'VIDEO', 'Regular checkup'),
            ('Alice Brown', 'Dr. Michael Wilson', '2025-09-21', '2:00 PM', 'SCHEDULED', 'VIDEO', 'Follow-up consultation'),
            ('John Patient', 'Dr. Sarah Smith', '2025-09-15', '11:00 AM', 'COMPLETED', 'VIDEO', 'Initial consultation'),
            ('Alice Brown', 'Dr. Sarah Smith', '2025-09-18', '4:00 PM', 'SCHEDULED', 'AUDIO', 'Blood pressure check'),
            ('Emma Davis', 'Dr. Jennifer Lee', '2025-09-22', '9:00 AM', 'SCHEDULED', 'VIDEO', 'Cardiology consultation'),
            ('John Patient', 'Dr. Michael Wilson', '2025-09-12', '3:00 PM', 'COMPLETED', 'VIDEO', 'Previous visit'),
            ('Alice Brown', 'Dr. Jennifer Lee', '2025-09-25', '1:00 PM', 'SCHEDULED', 'VIDEO', 'Specialist referral');
        """;

        executeQuery(insertSampleAppointments, "Sample appointments");

        String insertSampleDiagnoses = """
            INSERT OR IGNORE INTO diagnoses (appointment_id, patient_name, doctor_name, diagnosis_text, symptoms, prescription_details, treatment_plan, recorded_date, severity, status) VALUES 
            (3, 'John Patient', 'Dr. Sarah Smith', 'Upper respiratory tract infection', 'Cough, fever, sore throat, runny nose', 'Amoxicillin 500mg - 3 times daily for 7 days. Paracetamol for fever as needed.', 'Rest, fluids, avoid cold environments. Return if symptoms worsen.', '2025-09-15', 'MILD', 'RESOLVED'),
            (6, 'John Patient', 'Dr. Michael Wilson', 'Hypertension Stage 1', 'Elevated blood pressure readings, occasional headaches', 'Lisinopril 10mg - once daily in morning. Monitor blood pressure daily.', 'Lifestyle modifications: low sodium diet, regular exercise, weight management.', '2025-09-12', 'MODERATE', 'ACTIVE'),
            (1, 'Alice Brown', 'Dr. Sarah Smith', 'Seasonal allergies', 'Sneezing, watery eyes, nasal congestion', 'Loratadine 10mg - once daily. Nasal saline spray as needed.', 'Avoid known allergens, keep windows closed during high pollen days.', '2025-09-10', 'MILD', 'ACTIVE');
        """;

        executeQuery(insertSampleDiagnoses, "Sample diagnoses");

        String insertSampleReferrals = """
            INSERT OR IGNORE INTO hospital_referrals (patient_name, referring_doctor_name, hospital_name, department, reason_for_referral, urgency_level, referral_date, status, specialty_required, contact_number) VALUES 
            ('John Patient', 'Dr. Sarah Smith', 'City General Hospital', 'Cardiology', 'Chest pain requiring ECG and stress testing for cardiac evaluation', 'MEDIUM', '2025-09-16', 'PENDING', 'Interventional Cardiology', '+1234567890'),
            ('Alice Brown', 'Dr. Michael Wilson', 'Regional Medical Center', 'Neurology', 'Persistent headaches with visual disturbances, possible migraine evaluation', 'LOW', '2025-09-12', 'CONFIRMED', 'Headache Specialist', '+1234567891'),
            ('Emma Davis', 'Dr. Jennifer Lee', 'Heart Specialist Center', 'Cardiology', 'Irregular heartbeat detected, requires specialized cardiac monitoring', 'HIGH', '2025-09-14', 'PENDING', 'Electrophysiology', '+1234567892'),
            ('John Patient', 'Dr. Michael Wilson', 'University Medical Center', 'Orthopedics', 'Knee pain after injury, possible ligament damage requiring MRI', 'MEDIUM', '2025-09-13', 'CONFIRMED', 'Sports Medicine', '+1234567890');
        """;

        executeQuery(insertSampleReferrals, "Sample hospital referrals");
    }

    private static void executeQuery(String query, String tableName) {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(query);
            System.out.println("✓ " + tableName + " initialized successfully.");
        } catch (SQLException e) {
            System.out.println("✗ " + tableName + " initialization failed: " + e.getMessage());
        }
    }

    // Utility methods
    public static boolean userExists(String username) {
        try (Connection conn = connect()) {
            String query = "SELECT COUNT(*) FROM users WHERE username = ?";
            var pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            var rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getUserRole(String username) {
        try (Connection conn = connect()) {
            String query = "SELECT role FROM users WHERE username = ?";
            var pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "PATIENT";
    }
}