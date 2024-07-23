import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class HomePage {
    private static final String url ="jdbc:mysql://localhost:3306/bookyourappointment";
    private static final String username = "root";
    private static final String password = "Supergirl7)";
    private static final String[] DEFAULT_TIME_SLOTS = { "9-10", "10-11", "11-12", "12-1", "3-4", "4-5" };

    public static void main(String args[]){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }

        Scanner sc = new Scanner(System.in);

        try{
            Connection connection = DriverManager.getConnection(url,username,password);
            Patient patient = new Patient(connection,sc);
            Doctor doctor = new Doctor(connection,sc);
            System.out.println("**************************************** WELCOME !! BOOK YOUR APPOINTMENT NOW ************************************");
            System.out.println("------------------------------------- Enter 1 to login if your account already exists------------------------------");
            System.out.println("--------------------------------------------- Enter 2 to sign up---------------------------------------------------");
            int choice = sc.nextInt();
            switch (choice){
                case 1:
                    String id = Patient.login(sc,connection);
                    System.out.println("Enter 1 for booking appointment");
                    System.out.println("Enter 2 to view previous appointments");
                    int input = sc.nextInt();
                    if(input == 1){
                        listOfDepartments(sc,patient,doctor,id);
                    }
                    else{
                        Patient.previousAppointments(id,connection,sc,patient,doctor);
                    }

                    break;
                case 2:
                    String pid = Patient.signup(sc,connection);
                    System.out.println("Enter 1 for booking appointment");
                    System.out.println("Enter 2 to view previous appointments");
                    int choose = sc.nextInt();
                    if(choose == 1){
                        listOfDepartments(sc,patient,doctor,pid);
                    }
                    else{
                        Patient.previousAppointments(pid,connection,sc,patient,doctor);
                    }

                    break;
                default:
                    System.out.println("Invalid choice");
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    static void listOfDepartments(Scanner sc, Patient patient, Doctor doctor, String id){
        System.out.println("Book your appointment in any of the following departments:");
        System.out.println("-----------------Enter 1 for Cardiology-----------------");
        System.out.println("-----------------Enter 2 for Neurology------------------");
        System.out.println("-----------------Enter 3 for Pediatrics-----------------");
        System.out.println("-----------------Enter 4 for Radiology------------------");
        System.out.println("-----------------Enter 5 for Internal Medicine----------");
        System.out.println("-----------------Enter 6 for Gastroenterology-----------");
        System.out.println("-----------------Enter 7 for Nephrology-----------------");
        System.out.println("-----------------Enter 8 for Psychiatry-----------------");
        System.out.println("-----------------Enter 9 for Urology--------------------");
        System.out.println("-----------------Enter 10 for Dermatology---------------");

        int choice = sc.nextInt();

        switch (choice){
            case 1:
                Doctor.displayDoctors("Cardiology",patient,doctor,id);
                break;
            case 2:
                Doctor.displayDoctors("Neurology",patient,doctor,id);
                break;
            case 3:
                Doctor.displayDoctors("Pediatrics",patient,doctor,id);
                break;
            case 4:
                Doctor.displayDoctors("Radiology",patient,doctor,id);
                break;
            case 5:
                Doctor.displayDoctors("Internal Medicine",patient,doctor,id);
                break;
            case 6:
                Doctor.displayDoctors("Gastroenterology",patient,doctor,id);
                break;
            case 7:
                Doctor.displayDoctors("Nephrology",patient,doctor,id);
                break;
            case 8:
                Doctor.displayDoctors("Psychiatry",patient,doctor,id);
                break;
            case 9:
                Doctor.displayDoctors("Urology",patient,doctor,id);
                break;
            case 10:
                Doctor.displayDoctors("Dermatology",patient,doctor,id);
                break;
        }

    }

    static List<String> getAvailableTimeSlots(Connection connection, String department, String appointment_date) {
        List<String> availableTimeSlots = new ArrayList<>(Arrays.asList(DEFAULT_TIME_SLOTS));

        try {
            String query = "SELECT time_slot, COUNT(*) AS num_appointments FROM allappointments WHERE department = ? AND appointment_date = ? GROUP BY time_slot";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, department);
            preparedStatement.setString(2, appointment_date);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String timeSlot = resultSet.getString("time_slot");
                int numAppointments = resultSet.getInt("num_appointments");

                if (numAppointments >= 3) {
                    availableTimeSlots.remove(timeSlot); // Remove unavailable time slots
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableTimeSlots;
    }

    static synchronized void bookAppointment(Connection connection, Scanner sc, Patient patient, Doctor doctor, String department, String pid)
    {
            String patient_id = "";
            boolean validPatientId = false;

            // Loop until a valid patient id is entered
            while (!validPatientId) {
                System.out.println("\nEnter patient id");
                patient_id = sc.next();

                if (!patient_id.equals(pid)) {
                    System.out.println("Enter your own patient id");
                } else {
                    validPatientId = true;
                }
            }

            boolean validDoctorInfo = false;

            String doctor_name = null;
            int doctor_id=0;

            // Loop until a valid doctor id is entered
            while (!validDoctorInfo) {
                sc.nextLine(); // Consume the newline character before reading the doctor name
                System.out.println("\nEnter name of doctor (note: enter the same name as shown)");
                doctor_name = sc.nextLine();

                System.out.println("\nEnter doctor id");
                doctor_id = sc.nextInt();

                if (!doctor.getDoctorByName(doctor_name, department, doctor_id)) {
                    System.out.println("You entered wrong doctor id or doctor name");
                } else {
                    validDoctorInfo = true;
                }
            }

        boolean validAppointmentDate = false;
        List<String> availableTimeSlots = null;
        LocalDate appointment_date = null;
        String appointment_date_str = null;

        while (!validAppointmentDate) {
            // Prompt the user to enter the appointment date
            while (!validAppointmentDate) {
                System.out.println("\nEnter appointment date (YYYY-MM-DD):");
                appointment_date_str = sc.next();

                try {
                    appointment_date = LocalDate.parse(appointment_date_str);
                    LocalDate now = LocalDate.now();

                    if (appointment_date.isAfter(now)) {
                        validAppointmentDate = true;
                    } else {
                        System.out.println("Enter a date that is after today's date.");
                    }
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Please enter the date in the format (YYYY-MM-DD).");
                }
            }

            // Get available time slots for the selected date and department
            availableTimeSlots = getAvailableTimeSlots(connection, department, appointment_date_str);

            // If no time slots are available, prompt the user to choose another appointment date
            if (availableTimeSlots.isEmpty()) {
                System.out.println("No available slots on this date. Choose another date.");
                validAppointmentDate = false; // Set to false to repeat the loop
            } else {
                // Display available time slots to the user
                System.out.println("Available time slots:");
                for (int i = 0; i < availableTimeSlots.size(); i++) {
                    System.out.println((i + 1) + ". " + availableTimeSlots.get(i) + " am");
                }

                // Proceed to select a time slot
                validAppointmentDate = true;
            }
        }

        // Ask the user to choose a time slot
        System.out.println("Choose a time slot number:");
        int slotNumber = sc.nextInt();

        if (slotNumber >= 1 && slotNumber <= availableTimeSlots.size()) {
            String selectedTimeSlot = availableTimeSlots.get(slotNumber - 1);

            // Insert the appointment into the database
            if (patient.getPatientById(patient_id)) {
                // check if doctor is available or not
                if (doctor.getDoctorByName(doctor_name, department, doctor_id)) {
//                    if (Doctor.checkDoctorAvailability(doctor_name, appointment_date_str, connection)) {

                        if(!checkAppointmentOnTheGivenDate(patient_id,appointment_date_str,department,connection)) {

                            String appointment_query = "INSERT INTO allappointments(patient_id,doctor_id,appointment_date, department,doctor_name,time_slot) VALUES (?,?,?,?,?,?)";
                            try {
                                PreparedStatement preparedStatement = connection.prepareStatement(appointment_query);
                                preparedStatement.setString(1, patient_id);
                                preparedStatement.setInt(2, doctor_id);
                                preparedStatement.setString(3, appointment_date_str);
                                preparedStatement.setString(4, department);
                                preparedStatement.setString(5, doctor_name);
                                preparedStatement.setString(6,selectedTimeSlot);
                                int rowsAffected = preparedStatement.executeUpdate();

                                if (rowsAffected > 0) {
                                    System.out.println("\n\n------------------------------Appointment Booked-----------------------------");
                                    System.out.println("View previous appointments? (Yes/No)");

                                    String input = sc.next();
                                    if (input.equalsIgnoreCase("Yes")) {
                                        Patient.previousAppointments(patient_id, connection, sc, patient, doctor);
                                    }

                                } else {
                                    System.out.println("Failed to book appointment");
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            System.out.println("You already have an appointment booked on this date in this department. Try booking on some other date.");
                        }
//                    } else {
//                        System.out.println("\nOops!! Doctor is not available on this date. Try on some other date.");
//                    }
                } else {
                    System.out.println("Doctor does not exist");
                }
            } else {
                System.out.println("Patient does not exist");
            }
            // Display success message or handle errors
        } else {
            System.out.println("Invalid slot number.");
        }


        // check if patient and doctor exist

    }

    private static boolean checkAppointmentOnTheGivenDate(String patient_id, String appointment_date, String department, Connection connection){
        String query = "select * from allappointments where patient_id=? and appointment_date=? and department=?";
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1,patient_id);
            preparedStatement.setString(2, appointment_date);
            preparedStatement.setString(3,department);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                return true;
            }
            return false;
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        return false;
    }

}
