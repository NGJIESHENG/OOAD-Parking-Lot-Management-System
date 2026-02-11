package com.university.parking.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:parking_system.db";

    public static Connection connect(){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Connection Error: " + e.getMessage());
        }
        return conn;
    }

    public static void createNewTable(){
        String sql = "CREATE TABLE IF NOT EXISTS parking_spots ("
                        + "spot_id TEXT PRIMARY KEY,"
                        + " type TEXT NOT NULL,"
                        + " is_occupied INTEGER NOT NULL,"
                        + " hourly_rate REAL NOT NULL"
                        + ");";

        try (Connection conn = connect();
            Statement stmt = conn.createStatement()){
                stmt.execute(sql);
                System.out.println("Database and Table initialized successfully.");
            }catch (SQLException e){
                System.out.println("Table Creation Error: " + e.getMessage());
            }
    }
}
