package com.university.parking.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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

            String checkFinesTable = "SELECT name FROM sqlite_master WHERE type='table' AND name='fines'";
            ResultSet rs = stmt.executeQuery(checkFinesTable);
            if (rs.next()) {
             
                String checkColumn = "PRAGMA table_info(fines)";
                ResultSet columnRs = stmt.executeQuery(checkColumn);
                boolean hasSchemeUsed = false;
                while (columnRs.next()) {
                    if ("scheme_used".equals(columnRs.getString("name"))) {
                        hasSchemeUsed = true;
                        break;
                    }
                }
                columnRs.close();
                
                if (!hasSchemeUsed) {
                    stmt.execute("ALTER TABLE fines RENAME TO fines_old");
               
                    String sqlFines = "CREATE TABLE fines ("
                            + "fine_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "license_plate TEXT NOT NULL,"
                            + "reason TEXT NOT NULL,"
                            + "amount REAL NOT NULL,"
                            + "issued_at TEXT NOT NULL,"
                            + "is_paid INTEGER DEFAULT 0,"
                            + "ticket_id TEXT,"
                            + "scheme_used TEXT"
                            + ");";
                    stmt.execute(sqlFines);
                
                    stmt.execute("INSERT INTO fines (fine_id, license_plate, reason, amount, issued_at, is_paid, ticket_id) "
                            + "SELECT fine_id, license_plate, reason, amount, issued_at, is_paid, ticket_id FROM fines_old");
            
                    stmt.execute("DROP TABLE fines_old");
                    
                    System.out.println("Updated fines table with scheme_used column");
                }
            } else {
                
                String sqlFines = "CREATE TABLE fines ("
                        + "fine_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "license_plate TEXT NOT NULL,"
                        + "reason TEXT NOT NULL,"
                        + "amount REAL NOT NULL,"
                        + "issued_at TEXT NOT NULL,"
                        + "is_paid INTEGER DEFAULT 0,"
                        + "ticket_id TEXT,"
                        + "scheme_used TEXT"
                        + ");";
                stmt.execute(sqlFines);
            }

            String sqlPayments = "CREATE TABLE IF NOT EXISTS payments (" +
                "payment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "license_plate TEXT NOT NULL," +
                "amount REAL NOT NULL," +
                "payment_method TEXT NOT NULL," +
                "payment_time TEXT NOT NULL," +
                "cash_tendered REAL DEFAULT 0," +
                "change_amount REAL DEFAULT 0," +
                "ticket_id INTEGER" +
                ");";
            stmt.execute(sqlPayments);
            
            System.out.println("Database initialized.");
        } catch (SQLException e) {
            System.out.println("DB Init Error: " + e.getMessage());
        }
    }
}