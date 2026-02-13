package com.university.parking.gui;

import com.university.parking.database.DatabaseManager;
import com.university.parking.logic.FineManager;
import com.university.parking.logic.ParkingService;
import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame {
    private ParkingService service;

    public MainFrame() {
        service = new ParkingService();
        DatabaseManager.initializeDatabase(); 

        setTitle("Parking Lot Management System");
        setSize(900, 700);
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

        // ========== Exit & Payment Panel (Full English) ==========
        JPanel exitPanel = new JPanel(new BorderLayout(10, 10));
        exitPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Search Section
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("1. Vehicle Search"));
        JTextField exitPlateField = new JTextField(15);
        JButton calcButton = new JButton("Calculate Fee");
        searchPanel.add(new JLabel("License Plate:"));
        searchPanel.add(exitPlateField);
        searchPanel.add(calcButton);
        
        // Bill Section
        JPanel billPanel = new JPanel(new BorderLayout());
        billPanel.setBorder(BorderFactory.createTitledBorder("2. Fee Details"));
        JTextArea billArea = new JTextArea(10, 50);
        billArea.setEditable(false);
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        billPanel.add(new JScrollPane(billArea), BorderLayout.CENTER);
        
        // Payment Section
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
        
        cashRadio.addActionListener(e -> {
            cashLabel.setEnabled(true);
            cashField.setEnabled(true);
        });
        cardRadio.addActionListener(e -> {
            cashLabel.setEnabled(false);
            cashField.setEnabled(false);
        });
        
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
        
        gbc2.gridx = 0; gbc2.gridy = 1; paymentPanel.add(cashLabel, gbc2);
        gbc2.gridx = 1; gbc2.gridy = 1; paymentPanel.add(cashField, gbc2);
        
        gbc2.gridx = 0; gbc2.gridy = 2; gbc2.gridwidth = 2;
        paymentPanel.add(payButton, gbc2);
        
        gbc2.gridx = 0; gbc2.gridy = 3; gbc2.gridwidth = 2;
        paymentPanel.add(new JLabel("Receipt:"), gbc2);
        
        gbc2.gridx = 0; gbc2.gridy = 4; gbc2.gridwidth = 2;
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
            
            if (!bill.contains("not found") && !bill.contains("Error") && !bill.contains("not found")) {
                payButton.setEnabled(true);
                cashRadio.setSelected(true);
                cashField.setEnabled(true);
                cashField.setText("0.00");
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
                    if (cashTendered <= 0) {
                        JOptionPane.showMessageDialog(this, "Please enter valid cash amount.");
                        return;
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid cash amount format.");
                return;
            }
            
            String receipt = service.processPayment(plate, paymentMethod, cashTendered);
            receiptArea.setText(receipt);
            
            if (!receipt.contains("Insufficient") && !receipt.contains("❌")) {
                service.completePayment(plate);
                payButton.setEnabled(false);
                JOptionPane.showMessageDialog(this, "Payment Successful! Please take your receipt.", 
                    "Payment Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // ========== Admin Panel (Full English) ==========
        JPanel adminPanel = new JPanel(new BorderLayout(10, 10));
        adminPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Fine Strategy Selection
        JPanel strategyPanel = new JPanel(new GridBagLayout());
        strategyPanel.setBorder(BorderFactory.createTitledBorder("⚙️ Fine Scheme Settings (Admin can switch anytime)"));
        GridBagConstraints gbcAdmin = new GridBagConstraints();
        gbcAdmin.insets = new Insets(10, 10, 10, 10);
        gbcAdmin.anchor = GridBagConstraints.WEST;
        
        JLabel strategyLabel = new JLabel("Current Fine Scheme:");
        JLabel currentStrategyValue = new JLabel(service.getCurrentFineStrategyName());
        currentStrategyValue.setFont(new Font("Arial", Font.BOLD, 14));
        currentStrategyValue.setForeground(new Color(0, 100, 0));
        
        JComboBox<String> strategyCombo = new JComboBox<>(FineManager.getAvailableStrategies());
        JButton applyStrategyBtn = new JButton("Apply Scheme");
        applyStrategyBtn.setBackground(new Color(70, 130, 200));
        applyStrategyBtn.setForeground(Color.WHITE);
        
        JTextArea strategyDesc = new JTextArea(5, 45);
        strategyDesc.setEditable(false);
        strategyDesc.setBackground(new Color(240, 240, 240));
        strategyDesc.setFont(new Font("Monospaced", Font.PLAIN, 12));
        strategyDesc.setText(
            "【Scheme Description】\n" +
            "1. FIXED      - Flat fine RM50 for overstaying (>24 hours)\n" +
            "2. PROGRESSIVE - 24-48h: RM50, 48-72h: +RM100, 72-96h: +RM150, >96h: +RM200\n" +
            "3. HOURLY     - RM20 per hour after 24 hours\n\n" +
            "※ New scheme takes effect immediately for all vehicles exiting after change"
        );
        
        gbcAdmin.gridx = 0; gbcAdmin.gridy = 0; strategyPanel.add(strategyLabel, gbcAdmin);
        gbcAdmin.gridx = 1; gbcAdmin.gridy = 0; strategyPanel.add(currentStrategyValue, gbcAdmin);
        gbcAdmin.gridx = 0; gbcAdmin.gridy = 1; strategyPanel.add(new JLabel("Switch Scheme:"), gbcAdmin);
        gbcAdmin.gridx = 1; gbcAdmin.gridy = 1; strategyPanel.add(strategyCombo, gbcAdmin);
        gbcAdmin.gridx = 2; gbcAdmin.gridy = 1; strategyPanel.add(applyStrategyBtn, gbcAdmin);
        gbcAdmin.gridx = 0; gbcAdmin.gridy = 2; gbcAdmin.gridwidth = 3; 
        strategyPanel.add(new JScrollPane(strategyDesc), gbcAdmin);
        
        applyStrategyBtn.addActionListener(e -> {
            String selected = (String) strategyCombo.getSelectedItem();
            service.setFineStrategy(selected);
            currentStrategyValue.setText(service.getCurrentFineStrategyName());
            JOptionPane.showMessageDialog(this, 
                "Fine scheme switched to: " + service.getCurrentFineStrategyName(), 
                "Setting Updated", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Occupancy Rate
        JPanel occupancyPanel = new JPanel(new BorderLayout());
        occupancyPanel.setBorder(BorderFactory.createTitledBorder("📊 Occupancy Rate"));
        JTextArea occArea = new JTextArea(5, 45);
        occArea.setEditable(false);
        occArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        occArea.setText("Total Spots: 150\nOccupied: 0\nOccupancy Rate: 0.00%\n\n※ Implement ParkingLot.getOccupancyRate()");
        
        occupancyPanel.add(new JScrollPane(occArea), BorderLayout.CENTER);
        
        // Revenue Report
        JPanel revenuePanel = new JPanel(new BorderLayout());
        revenuePanel.setBorder(BorderFactory.createTitledBorder("💰 Revenue Report"));
        JTextArea revenueArea = new JTextArea(4, 45);
        revenueArea.setEditable(false);
        revenueArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        revenueArea.setText("Parking Fee Collected: RM 0.00\nFine Collected: RM 0.00\nTotal Revenue: RM 0.00\n\n※ Implement PaymentDAO statistics");
        revenuePanel.add(new JScrollPane(revenueArea), BorderLayout.CENTER);
        
        JPanel adminCenter = new JPanel(new BorderLayout());
        adminCenter.add(occupancyPanel, BorderLayout.NORTH);
        adminCenter.add(revenuePanel, BorderLayout.CENTER);
        
        adminPanel.add(strategyPanel, BorderLayout.NORTH);
        adminPanel.add(adminCenter, BorderLayout.CENTER);

        // ========== Fine Management Panel (Full English) ==========
        JPanel fineReportPanel = new JPanel(new BorderLayout());
        fineReportPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea fineReportArea = new JTextArea(20, 45);
        fineReportArea.setEditable(false);
        fineReportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JButton refreshFineBtn = new JButton("Refresh Unpaid Fines");
        refreshFineBtn.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel fineTitle = new JLabel("Outstanding Fines List", SwingConstants.CENTER);
        fineTitle.setFont(new Font("Arial", Font.BOLD, 16));
        fineReportPanel.add(fineTitle, BorderLayout.NORTH);
        
        refreshFineBtn.addActionListener(e -> {
            try {
                com.university.parking.database.FineDAO fineDAO = new com.university.parking.database.FineDAO();
                java.util.List<com.university.parking.model.Fine> fines = fineDAO.getAllUnpaidFines();
                
                StringBuilder sb = new StringBuilder();
                sb.append("================================================================\n");
                sb.append("                 OUTSTANDING FINES REPORT                       \n");
                sb.append("================================================================\n");
                sb.append(String.format("%-4s %-12s %-20s %-10s %-20s\n", 
                    "ID", "Plate No.", "Violation Reason", "Amount(RM)", "Issue Date"));
                sb.append("----------------------------------------------------------------\n");
                
                if (fines.isEmpty()) {
                    sb.append("\n                    No outstanding fines!                       \n");
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
                sb.append("Total Unpaid Fines: " + fines.size());
                
                fineReportArea.setText(sb.toString());
                fineReportArea.setCaretPosition(0);
                
                JOptionPane.showMessageDialog(this, "Fine data refreshed!", "Refresh Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Refresh failed: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        fineReportPanel.add(new JScrollPane(fineReportArea), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshFineBtn);
        fineReportPanel.add(buttonPanel, BorderLayout.SOUTH);

        // ========== Add All Tabs ==========
        tabbedPane.addTab("Vehicle Entry", entryPanel);
        tabbedPane.addTab("Payment & Exit", exitPanel);
        tabbedPane.addTab("Admin", adminPanel);
        tabbedPane.addTab("Fine Management", fineReportPanel);

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainFrame().setVisible(true);
        });
    }
}