package com.university.parking.gui;

import com.university.parking.database.DatabaseManager;
import com.university.parking.logic.ParkingService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private ParkingService service;

    public MainFrame() {
        service = new ParkingService();
        DatabaseManager.initializeDatabase(); // Ensure DB exists

        setTitle("Parking Lot Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- Tab 1: Entry ---
        JPanel entryPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JTextField plateField = new JTextField(15);
        String[] vehicleTypes = {"Car", "Motorcycle", "SUV", "Truck", "Handicapped"};
        JComboBox<String> typeCombo = new JComboBox<>(vehicleTypes);
        JButton parkButton = new JButton("Park Vehicle");
        JLabel entryResultLabel = new JLabel("Ready");

        gbc.gridx=0; gbc.gridy=0; entryPanel.add(new JLabel("License Plate:"), gbc);
        gbc.gridx=1; entryPanel.add(plateField, gbc);
        gbc.gridx=0; gbc.gridy=1; entryPanel.add(new JLabel("Vehicle Type:"), gbc);
        gbc.gridx=1; entryPanel.add(typeCombo, gbc);
        gbc.gridx=1; gbc.gridy=2; entryPanel.add(parkButton, gbc);
        gbc.gridx=1; gbc.gridy=3; entryPanel.add(entryResultLabel, gbc);

        parkButton.addActionListener(e -> {
            String plate = plateField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            if(plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a plate number.");
                return;
            }
            
            String spot = service.parkVehicle(plate, type);
            if("NO_SPOTS".equals(spot)) {
                entryResultLabel.setText("Full for this type!");
                entryResultLabel.setForeground(Color.RED);
            } else {
                entryResultLabel.setText("Assigned Spot: " + spot);
                entryResultLabel.setForeground(Color.BLUE);
                JOptionPane.showMessageDialog(this, "Ticket Generated.\nSpot: " + spot + "\nTime: " + new java.util.Date());
            }
        });

        // --- Tab 2: Exit ---
        JPanel exitPanel = new JPanel(new GridBagLayout());
        JTextField exitPlateField = new JTextField(15);
        JButton calcButton = new JButton("Calculate Fee");
        JTextArea billArea = new JTextArea(8, 30);
        billArea.setEditable(false);
        JButton payButton = new JButton("Pay & Exit");
        payButton.setEnabled(false);

        gbc.gridx=0; gbc.gridy=0; exitPanel.add(new JLabel("Search Plate:"), gbc);
        gbc.gridx=1; exitPanel.add(exitPlateField, gbc);
        gbc.gridx=2; exitPanel.add(calcButton, gbc);
        gbc.gridx=0; gbc.gridy=1; gbc.gridwidth=3; exitPanel.add(new JScrollPane(billArea), gbc);
        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=3; exitPanel.add(payButton, gbc);

        calcButton.addActionListener(e -> {
            String plate = exitPlateField.getText().trim();
            if(plate.isEmpty()) return;
            
            String bill = service.processExit(plate);
            billArea.setText(bill);
            if (!bill.contains("not found") && !bill.contains("Error")) {
                payButton.setEnabled(true);
            }
        });

        payButton.addActionListener(e -> {
            String plate = exitPlateField.getText().trim();
            service.completePayment(plate);
            billArea.append("\n\nPAYMENT SUCCESSFUL. GATE OPEN.");
            payButton.setEnabled(false);
        });

        // --- Tab 3: Admin (Stub) ---
        JPanel adminPanel = new JPanel();
        adminPanel.add(new JLabel("Admin Panel - Occupancy Views would go here"));

        tabbedPane.addTab("Vehicle Entry", entryPanel);
        tabbedPane.addTab("Payment & Exit", exitPanel);
        tabbedPane.addTab("Admin", adminPanel);

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}