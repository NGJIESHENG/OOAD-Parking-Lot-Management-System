package com.university.parking.gui;

import com.university.parking.database.DatabaseManager;
import com.university.parking.logic.FineManager;
import com.university.parking.logic.ParkingService;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MainFrame extends JFrame {
    private ParkingService service;

    public MainFrame() {
        service = new ParkingService();
        DatabaseManager.initializeDatabase(); 

        setTitle("Parking Lot Management System");
        setSize(1000, 800); // Slightly wider to accommodate the table
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // ========== Entry Panel ==========
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

        // ========== Exit & Payment Panel ==========
        JPanel exitPanel = new JPanel(new BorderLayout(10, 10));
        exitPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("1. Vehicle Search"));
        JTextField exitPlateField = new JTextField(15);
        JButton calcButton = new JButton("Calculate Fee");
        searchPanel.add(new JLabel("License Plate:"));
        searchPanel.add(exitPlateField);
        searchPanel.add(calcButton);

        JPanel billPanel = new JPanel(new BorderLayout());
        billPanel.setBorder(BorderFactory.createTitledBorder("2. Fee Details"));
        JTextArea billArea = new JTextArea(10, 50);
        billArea.setEditable(false);
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        billPanel.add(new JScrollPane(billArea), BorderLayout.CENTER);

        JPanel paymentPanel = new JPanel(new GridBagLayout());
        paymentPanel.setBorder(BorderFactory.createTitledBorder("3. Payment Method"));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(5, 5, 5, 5);
        gbc2.anchor = GridBagConstraints.WEST;

        JRadioButton cashRadio = new JRadioButton("Cash", true);
        JRadioButton cardRadio = new JRadioButton("Credit/Debit Card");
        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(cashRadio);
        paymentGroup.add(cardRadio);

        JLabel cashLabel = new JLabel("Cash Tendered (RM):");
        JTextField cashField = new JTextField(10);
        cashField.setText("0.00");

        JCheckBox payFinesCheckBox = new JCheckBox("Pay outstanding fines", true);
        payFinesCheckBox.setVisible(false); 

        JButton payButton = new JButton("💰 Pay & Exit");
        payButton.setFont(new Font("Arial", Font.BOLD, 14));
        payButton.setBackground(new Color(70, 130, 200));
        payButton.setForeground(Color.WHITE);
        payButton.setEnabled(false);

        JTextArea receiptArea = new JTextArea(8, 50);
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setBackground(new Color(245, 245, 245));

        gbc2.gridx = 0; gbc2.gridy = 0; paymentPanel.add(new JLabel("Select Payment:"), gbc2);
        gbc2.gridx = 1; gbc2.gridy = 0; 
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        radioPanel.add(cashRadio);
        radioPanel.add(cardRadio);
        paymentPanel.add(radioPanel, gbc2);

        gbc2.gridx = 0; gbc2.gridy = 1; gbc2.gridwidth = 2;
        paymentPanel.add(payFinesCheckBox, gbc2);

        gbc2.gridx = 0; gbc2.gridy = 2; gbc2.gridwidth = 1;
        paymentPanel.add(cashLabel, gbc2);
        gbc2.gridx = 1; gbc2.gridy = 2;
        paymentPanel.add(cashField, gbc2);

        gbc2.gridx = 0; gbc2.gridy = 3; gbc2.gridwidth = 2;
        paymentPanel.add(payButton, gbc2);

        gbc2.gridx = 0; gbc2.gridy = 4; gbc2.gridwidth = 2;
        paymentPanel.add(new JLabel("Receipt:"), gbc2);

        gbc2.gridx = 0; gbc2.gridy = 5; gbc2.gridwidth = 2;
        paymentPanel.add(new JScrollPane(receiptArea), gbc2);

        JPanel exitNorthPanel = new JPanel(new BorderLayout());
        exitNorthPanel.add(searchPanel, BorderLayout.NORTH);
        exitNorthPanel.add(billPanel, BorderLayout.CENTER);

        exitPanel.add(exitNorthPanel, BorderLayout.NORTH);
        exitPanel.add(paymentPanel, BorderLayout.CENTER);

        calcButton.addActionListener(e -> {
            String plate = exitPlateField.getText().trim();
            if(plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter license plate number.");
                return;
            }
            String bill = service.processExit(plate);
            billArea.setText(bill);
            if (!bill.contains("not found") && !bill.contains("Error")) {
                payButton.setEnabled(true);
                receiptArea.setText("");
            } else {
                payButton.setEnabled(false);
            }
        });

        payButton.addActionListener(e -> {
            String plate = exitPlateField.getText().trim();
            String paymentMethod = cardRadio.isSelected() ? "CARD" : "CASH";
            double cashTendered = 0.0;
            try {
                if (paymentMethod.equals("CASH")) {
                    cashTendered = Double.parseDouble(cashField.getText().trim());
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid cash amount.");
                return;
            }
            
            String receipt = service.processPayment(plate, paymentMethod, cashTendered, payFinesCheckBox.isSelected());
            receiptArea.setText(receipt);
            
            if (!receipt.contains("Insufficient") && !receipt.contains("❌")) {
                service.completePayment(plate);
                payButton.setEnabled(false);
                JOptionPane.showMessageDialog(this, "Payment Successful!");
            }
        });

        // ========== Admin Panel (Requirement Fulfillment) ==========
        JPanel adminPanel = new JPanel(new BorderLayout(10, 10));
        adminPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Strategy Settings
        JPanel strategyPanel = new JPanel(new GridBagLayout());
        strategyPanel.setBorder(BorderFactory.createTitledBorder("⚙️ Fine Scheme Settings"));
        GridBagConstraints gbcAdmin = new GridBagConstraints();
        gbcAdmin.insets = new Insets(5, 5, 5, 5);
        gbcAdmin.anchor = GridBagConstraints.WEST;

        JLabel strategyLabel = new JLabel("Current Scheme:");
        JLabel currentStrategyValue = new JLabel(service.getCurrentFineStrategyName());
        currentStrategyValue.setFont(new Font("Arial", Font.BOLD, 12));
        
        JComboBox<String> strategyCombo = new JComboBox<>(FineManager.getAvailableStrategies());
        JButton applyStrategyBtn = new JButton("Apply Scheme");

        gbcAdmin.gridx = 0; gbcAdmin.gridy = 0; strategyPanel.add(strategyLabel, gbcAdmin);
        gbcAdmin.gridx = 1; strategyPanel.add(currentStrategyValue, gbcAdmin);
        gbcAdmin.gridx = 0; gbcAdmin.gridy = 1; strategyPanel.add(new JLabel("Switch To:"), gbcAdmin);
        gbcAdmin.gridx = 1; strategyPanel.add(strategyCombo, gbcAdmin);
        gbcAdmin.gridx = 2; strategyPanel.add(applyStrategyBtn, gbcAdmin);

        applyStrategyBtn.addActionListener(e -> {
            service.setFineStrategy((String) strategyCombo.getSelectedItem());
            currentStrategyValue.setText(service.getCurrentFineStrategyName());
        });

        // 2. Stats & Table
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        JTextArea occArea = new JTextArea(5, 20);
        JTextArea revenueArea = new JTextArea(5, 20);
        occArea.setEditable(false);
        revenueArea.setEditable(false);
        statsPanel.add(new JScrollPane(occArea));
        statsPanel.add(new JScrollPane(revenueArea));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("🚗 Vehicles Currently Parked (All Floors)"));
        String[] columnNames = {"Spot ID", "Plate Number", "Type", "Entry Time"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable parkedTable = new JTable(tableModel);
        tablePanel.add(new JScrollPane(parkedTable), BorderLayout.CENTER);

        Runnable refreshAdminData = () -> {
            int total = service.getTotalSpotsCount();
            int occupied = service.getOccupiedSpotsCount();
            occArea.setText(String.format("OCCUPANCY\nTotal: %d\nOccupied: %d\nRate: %.2f%%", 
                total, occupied, total > 0 ? (occupied * 100.0) / total : 0));
            
            revenueArea.setText(String.format("REVENUE\nParking: RM %.2f\nFines: RM %.2f\nTotal: RM %.2f", 
                service.getTotalParkingRevenue(), service.getTotalFineRevenue(), 
                service.getTotalParkingRevenue() + service.getTotalFineRevenue()));

            tableModel.setRowCount(0);
            for (String[] row : service.getCurrentlyParkedVehicles()) {
                tableModel.addRow(row);
            }
        };

        JButton refreshBtn = new JButton("🔄 Refresh Dashboard");
        refreshBtn.addActionListener(e -> refreshAdminData.run());

        JPanel adminCenter = new JPanel(new BorderLayout(10, 10));
        adminCenter.add(statsPanel, BorderLayout.NORTH);
        adminCenter.add(tablePanel, BorderLayout.CENTER);
        adminCenter.add(refreshBtn, BorderLayout.SOUTH);

        adminPanel.add(strategyPanel, BorderLayout.NORTH);
        adminPanel.add(adminCenter, BorderLayout.CENTER);

        // ========== Fine Management Panel ==========
        JPanel fineReportPanel = new JPanel(new BorderLayout());
        JTextArea fineReportArea = new JTextArea();
        JButton refreshFineBtn = new JButton("Refresh Unpaid Fines");
        refreshFineBtn.addActionListener(e -> {
            List<com.university.parking.model.Fine> fines = new com.university.parking.database.FineDAO().getAllUnpaidFines();
            StringBuilder sb = new StringBuilder("ID | Plate | Reason | Amount | Date\n---\n");
            for (com.university.parking.model.Fine f : fines) {
                sb.append(String.format("%d | %s | %s | RM%.2f | %s\n", 
                    f.getFineId(), f.getLicensePlate(), f.getReason(), f.getAmount(), f.getIssuedAt()));
            }
            fineReportArea.setText(sb.toString());
        });
        fineReportPanel.add(new JScrollPane(fineReportArea), BorderLayout.CENTER);
        fineReportPanel.add(refreshFineBtn, BorderLayout.SOUTH);

        tabbedPane.addTab("Vehicle Entry", entryPanel);
        tabbedPane.addTab("Payment & Exit", exitPanel);
        tabbedPane.addTab("Admin", adminPanel);
        tabbedPane.addTab("Fine Management", fineReportPanel);

        add(tabbedPane);
        SwingUtilities.invokeLater(refreshAdminData);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}