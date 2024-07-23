import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Doctor {

    private static Connection connection;
    private static Scanner sc;
    public Doctor(Connection connection,Scanner sc){
        this.connection = connection;
        this.sc = sc;

    }

    static void displayDoctors(String departmentName, Patient patient, Doctor doctor, String pid){
        System.out.println("\nDoctors in " + departmentName + " department");
        System.out.println("+-------------+--------------------------+");
        System.out.println("| Doctor ID   | Name                     |");
        System.out.println("+-------------+--------------------------+");

        String query = "select * from doctors where specialization=?";

        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, departmentName);
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()){
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                System.out.printf("|%-13s|%-26s|\n",id, name);
                System.out.println("+-------------+--------------------------+");
            }


            System.out.println("\n\nStart Booking appointment:");
            HomePage.bookAppointment(connection,sc,patient,doctor,departmentName,pid);

        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    public boolean getDoctorByName(String name,String department,int id){
        String query = "select * from doctors where id = ? AND specialization= ?";
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1,id);
            preparedStatement.setString(2,department);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String doctorNameInDB = resultSet.getString("name");
                return doctorNameInDB.toLowerCase().contains(name.toLowerCase());
            }
            return false;
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

//    static boolean checkDoctorAvailability(String doctor_name, String appointment_date, Connection connection){
//        String query = "select COUNT(*) from allappointments where doctor_name=? AND appointment_date=?";
//        try{
//            PreparedStatement preparedStatement = connection.prepareStatement(query);
//            preparedStatement.setString(1, doctor_name);
//            preparedStatement.setString(2, appointment_date);
//            ResultSet resultSet = preparedStatement.executeQuery();
//            if(resultSet.next()){
//                int count = resultSet.getInt(1);
//                if(count<20){
//                    return true;
//                }
//                else{
//                    return false;
//                }
//            }
//        }
//        catch(SQLException e){
//            e.printStackTrace();
//        }
//        return false;
//
//    }

}
