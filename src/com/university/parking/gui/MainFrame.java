package com.university.parking.gui;

import com.university.parking.database.DatabaseManager;
import com.university.parking.logic.ParkingService;
import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame {
    private ParkingService service;

    public MainFrame() {
        service = new ParkingService();
        DatabaseManager.initializeDatabase(); 

        setTitle("Parking Lot Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel entryPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JTextField plateField = new JTextField(15);
        
        String[] vehicleTypes = {"CAR", "MOTORCYCLE", "SUV", "TRUCK", "HANDICAPPED"};
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
                entryResultLabel.setText("No suitable spots found.");
                entryResultLabel.setForeground(Color.RED);
            } else if ("ALREADY_PARKED".equals(spot)) {
                entryResultLabel.setText("Vehicle already inside!");
                entryResultLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "Error: Plate " + plate + " is already parked.");
            } else if ("INVALID_TYPE".equals(spot)) {
                entryResultLabel.setText("System Error: Invalid Type.");
            } else {
                entryResultLabel.setText("Assigned Spot: " + spot);
                entryResultLabel.setForeground(Color.BLUE);
                JOptionPane.showMessageDialog(this, "Ticket Generated.\nSpot: " + spot);
            }
        });

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

        JPanel adminPanel = new JPanel();
        adminPanel.add(new JLabel("Admin Panel - Occupancy Views would go here"));

        tabbedPane.addTab("Vehicle Entry", entryPanel);
        tabbedPane.addTab("Payment & Exit", exitPanel);
        tabbedPane.addTab("Admin", adminPanel);

        JPanel fineReportPanel = new JPanel(new BorderLayout());
        fineReportPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea fineReportArea = new JTextArea(20, 40);
        fineReportArea.setEditable(false);
        fineReportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JButton refreshFineBtn = new JButton("Refresh Unpaid Tickets");
        refreshFineBtn.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel fineTitle = new JLabel("List of unpaid fines", SwingConstants.CENTER);
        fineTitle.setFont(new Font("Arial", Font.BOLD, 16));
        fineReportPanel.add(fineTitle, BorderLayout.NORTH);
        
        refreshFineBtn.addActionListener(e -> {
            try {
                com.university.parking.database.FineDAO fineDAO = new com.university.parking.database.FineDAO();
                java.util.List<com.university.parking.model.Fine> fines = fineDAO.getAllUnpaidFines();
                
                StringBuilder sb = new StringBuilder();
                sb.append("================================================================\n");
                sb.append("                     List of Unpaid Fines                    \n");
                sb.append("================================================================\n");
                sb.append(String.format("%-4s %-12s %-20s %-10s %-20s\n", 
                    "ID", "License plate number", "Reason for violation", "Amount (RM)", "Date of ticket"));
                sb.append("----------------------------------------------------------------\n");
                
                if (fines.isEmpty()) {
                    sb.append("\n                     No outstanding fines yet!                      \n");
                } else {
                    for (com.university.parking.model.Fine f : fines) {
                        String reason = f.getReason();
                        if (reason.length() > 18) reason = reason.substring(0, 15) + "...";
                        
                        String dateTime = f.getIssuedAt().toString().replace("T", " ");
                        if (dateTime.length() > 16) dateTime = dateTime.substring(0, 16);
                        
                        sb.append(String.format("%-4d %-12s %-20s RM%-8.2f %-20s\n",
                            f.getFineId(),
                            f.getLicensePlate(),
                            reason,
                            f.getAmount(),
                            dateTime
                        ));
                    }
                }
                
                sb.append("================================================================\n");
                sb.append("Total number of unpaid fines: " + fines.size());
                
                fineReportArea.setText(sb.toString());
                fineReportArea.setCaretPosition(0);
                
                JOptionPane.showMessageDialog(this, "The ticket data has been updated!", "Update successful", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Refresh failed: " + ex.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        fineReportPanel.add(new JScrollPane(fineReportArea), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshFineBtn);
        fineReportPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Fine Management", fineReportPanel);

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}