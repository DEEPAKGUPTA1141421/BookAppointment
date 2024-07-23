import java.sql.*;
import java.util.Date;
import java.util.Scanner;
import java.util.Random;

public class Patient {
    private Connection connection;
    private Scanner scanner;

    public Patient(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    static String login(Scanner sc, Connection connection) {
        System.out.println("\nEnter your patient id");
        String pid = sc.next();
        String query = "select * from logindetails where Patient_id=?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, pid);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("Name");
                System.out.println("Logged in successfully !!\n");
                System.out.println("-----------------------------Welcome " + name.toUpperCase() + "-----------------------------\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pid;
    }

    public static String generatePatientID() {
        int idLength = 8;

        String characters = "0123456789";

        StringBuilder idBuilder = new StringBuilder();

        Random random = new Random();

        for (int i = 0; i < idLength; i++) {

            int randomIndex = random.nextInt(characters.length());
            idBuilder.append(characters.charAt(randomIndex));
        }

        return idBuilder.toString();
    }

    static String signup(Scanner sc, Connection connection) {
        System.out.println("\nEnter patient's name: ");
        String name = sc.next();
        System.out.println("\nEnter patient's age: ");
        int age = sc.nextInt();
        System.out.println("\nEnter patient's gender: ");
        String gender = sc.next();


        String patient_id = generatePatientID();
        try {
            String query = "INSERT INTO logindetails(name,age,gender,patient_id) VALUES (?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, age);
            preparedStatement.setString(3, gender);
            preparedStatement.setString(4, patient_id);


            int affectedRows = preparedStatement.executeUpdate();   // returns number of rows affected

            if (affectedRows > 0) {
                System.out.println("Signed Up Successfully!! Your Patient ID is: " + patient_id + "\n" + " You will need it next time you login." + "\n\n");
            } else {
                System.out.println("Failed to sign up\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return patient_id;
    }

    public boolean getPatientById(String id) {
        String query = "select * from logindetails where Patient_id = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    static void previousAppointments(String pid, Connection connection, Scanner sc, Patient patient, Doctor doctor) {

        String query = "select * from allappointments where patient_id=?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, pid);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Check if there are any previous appointments
            if (resultSet.next()) {
                System.out.println("-----------------------------YOUR PREVIOUS APPOINTMENTS---------------------------------------------");
                System.out.println("+-------------+-------------+----------------+---------------------------+-------------------------+");
                System.out.println("| Patient ID  | Doctor ID   | Doctor Name    | Department                | Appointment Date        |");
                System.out.println("+-------------+-------------+----------------+---------------------------+-------------------------+");

                do {
                    int pat_id = resultSet.getInt("patient_id");
                    int doc_id = resultSet.getInt("doctor_id");
                    String doctorName = resultSet.getString("doctor_name");
                    String department = resultSet.getString("department");
                    Date app_date = resultSet.getDate("appointment_date");

                    System.out.printf("|%-13s|%-13s|%-16s|%-27s|%-25s|\n", pat_id, doc_id, doctorName, department, app_date);
                    System.out.println("+-------------+-------------+----------------+---------------------------+-------------------------+");
                } while (resultSet.next());

            } else {
                System.out.println("No previous appointments found." + "\n\n");
            }
            System.out.println("Want to book an appointment? (Yes/No)");
            String input = sc.next();
            if (input.equalsIgnoreCase("Yes")) {
                HomePage.listOfDepartments(sc, patient, doctor, pid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}


