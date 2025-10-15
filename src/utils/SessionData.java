package utils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class SessionData {
    // Booking Data
    public static String patientName;
    public static String specialistName;
    public static LocalDate appointmentDate;
    public static String appointmentTime;

    // Prescription Refill
    public static String medicationName;
    public static String medicationQuantity;

    // Vitals
    public static Map<String, String> vitals = new HashMap<>();
}
