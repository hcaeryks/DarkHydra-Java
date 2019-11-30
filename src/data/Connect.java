package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {
    private static Connect ourInstance = new Connect();

    public static Connect getInstance() {
        return ourInstance;
    }

    public Connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("JDBC is working.");
        } catch (ClassNotFoundException ex) {
            System.out.println("JDBC failed to start or not found.");
        }

        try {
            conn = DriverManager.getConnection("jdbc:mysql://:3306/?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC","", "");
            System.out.println("Connected succesfully.");
        } catch (SQLException ex) {
            System.out.println("Failed to connect to database.");
            ex.printStackTrace();
        }
    }

    public Connection conn = null;
}
