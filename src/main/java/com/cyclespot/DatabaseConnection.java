
package com.cyclespot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/cyclespot";

    private static final String USER =
            System.getenv("CYCLESPOT_DB_USER");

    private static final String PASSWORD =
            System.getenv("CYCLESPOT_DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        if (USER == null || PASSWORD == null) {
            throw new IllegalStateException(
                    "Database credentials are missing. Check your environment variables."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("Connected to CycleSpot database!");
            System.out.println("Database: " + connection.getCatalog());
        } catch (Exception e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
        }
    }
}
