package com.university.parking.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:parking_system.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Connection Error: " + e.getMessage());
        }
        return conn;
    }

    public static void initializeDatabase() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            // Existing Spots Table
            String sqlSpots = "CREATE TABLE IF NOT EXISTS parking_spots ("
                    + "spot_id TEXT PRIMARY KEY,"
                    + " type TEXT NOT NULL,"
                    + " is_occupied INTEGER NOT NULL,"
                    + " hourly_rate REAL NOT NULL"
                    + ");";
            stmt.execute(sqlSpots);

            // NEW: Tickets Table for Entry/Exit tracking
            String sqlTickets = "CREATE TABLE IF NOT EXISTS tickets ("
                    + "ticket_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "spot_id TEXT,"
                    + "license_plate TEXT,"
                    + "vehicle_type TEXT,"
                    + "entry_time LONG," // Storing as timestamp (milliseconds)
                    + "is_paid INTEGER DEFAULT 0"
                    + ");";
            stmt.execute(sqlTickets);

            String sqlFines = "CREATE TABLE IF NOT EXISTS fines ("
                    + "fine_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "license_plate TEXT NOT NULL,"
                    + "reason TEXT NOT NULL,"
                    + "amount REAL NOT NULL,"
                    + "issued_at TEXT NOT NULL,"
                    + "is_paid INTEGER DEFAULT 0,"
                    + "ticket_id TEXT"
                    + ");";
            stmt.execute(sqlFines);
            
            System.out.println("Database initialized.");
        } catch (SQLException e) {
            System.out.println("DB Init Error: " + e.getMessage());
        }
    }
}