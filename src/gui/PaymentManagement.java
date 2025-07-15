package gui;

import db.DatabaseConnector;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PaymentManagement extends javax.swing.JPanel {

    private String memberId; // null = normal; not null = double-click view
    private DefaultTableModel tableModel;

    public PaymentManagement() {
        this(null);
    }

    public PaymentManagement(String memberId) {
        this.memberId = memberId;
        initComponents();

        tableModel = (DefaultTableModel) jTable1.getModel();

        // Add this DocumentListener to searchField to refresh payments on every change
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                loadPayments();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                loadPayments();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                loadPayments();
            }
        });

        // Combo box filtering (only in normal mode)
        if (memberId == null) {
            paymentYearComboBox.addActionListener(e -> loadPayments());
        } else {
            paymentYearComboBox.setEnabled(false);
            clearAllButton.setEnabled(false);
            searchField.setEnabled(false);
        }

        loadPaymentYears();

        if (memberId != null) {
            prefillFieldsAndLoadSingleMember();
        } else {
            loadPayments();
        }

        // Only add this click listener in normal mode
        if (memberId == null) {
            jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int row = jTable1.getSelectedRow();
                    if (row != -1) {
                        Object nicObj = tableModel.getValueAt(row, 2);  // NIC column
                        Object yearObj = tableModel.getValueAt(row, 3); // Year column

                        if (nicObj != null) {
                            searchField.setText(nicObj.toString());
                        }
                        if (yearObj != null) {
                            paymentYearComboBox.setSelectedItem(yearObj.toString());
                        }

                        loadPayments(); // reload filtered data

                        jTable1.setRowSelectionInterval(row, row); // keep clicked row selected
                    }
                }
            });
        }
    }

    private static abstract class SimpleDocumentListener implements javax.swing.event.DocumentListener {

        public abstract void update();

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            update();
        }

        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            update();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            update();
        }
    }

    private void prefillFieldsAndLoadSingleMember() {
        new Thread(() -> {
            try {
                String sql = "SELECT m.nic, py.year FROM member m "
                        + "JOIN payments pay ON pay.member_id = m.id "
                        + "JOIN payment_cycle py ON pay.payment_cycle_id = py.id "
                        + "JOIN payment_status ps ON py.payment_status_id = ps.id "
                        + "WHERE ps.name='Unpaid' AND m.id=? ORDER BY pay.due_date ASC LIMIT 1";
                try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql, memberId)) {
                    if (rs.next()) {
                        String nic = rs.getString("nic");
                        String year = rs.getString("year");

                        SwingUtilities.invokeLater(() -> {
                            if (year != null) {
                                paymentYearComboBox.setSelectedItem(year);
                            }
                            if (nic != null) {
                                searchField.setText(nic);
                            }
                            loadPayments(); // finally load payments
                        });
                    } else {
                        SwingUtilities.invokeLater(this::loadPayments); // no data, just load empty
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(()
                        -> JOptionPane.showMessageDialog(this, "Failed to prefill: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    private void loadPayments() {
        new Thread(() -> {
            List<Object[]> rows = new ArrayList<>();
            try {
                StringBuilder sql = new StringBuilder(
                        "SELECT m.id, m.full_name, m.nic, py.year, pay.amount, pay.due_date, pay.paid_date, m.mobile, m.email "
                        + "FROM member m "
                        + "JOIN payments pay ON pay.member_id = m.id "
                        + "JOIN payment_cycle py ON pay.payment_cycle_id = py.id "
                        + "JOIN payment_status ps ON py.payment_status_id = ps.id "
                        + "WHERE ps.name='Unpaid' "
                );
                List<Object> params = new ArrayList<>();

                if (memberId != null) {
                    sql.append("AND m.id=? ");
                    params.add(memberId);
                }

                String selectedYear = (String) paymentYearComboBox.getSelectedItem();
                if (selectedYear != null && !"All".equals(selectedYear)) {
                    sql.append("AND py.year=? ");
                    params.add(selectedYear);
                }

                String search = searchField.getText().trim();
                if (!search.isEmpty()) {
                    sql.append("AND (m.full_name LIKE ? OR m.nic LIKE ? OR m.id LIKE ? OR m.mobile LIKE ?) ");
                    String like = "%" + search + "%";
                    params.add(like);
                    params.add(like);
                    params.add(like);
                    params.add(like);
                }

                sql.append("ORDER BY pay.due_date ASC");

                try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql.toString(), params.toArray())) {
                    while (rs.next()) {
                        rows.add(new Object[]{
                            rs.getString("id"),
                            rs.getString("full_name"),
                            rs.getString("nic"),
                            rs.getString("year"),
                            rs.getBigDecimal("amount"),
                            rs.getDate("due_date"),
                            rs.getDate("paid_date"),
                            rs.getString("mobile"),
                            rs.getString("email")
                        });
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(()
                        -> JOptionPane.showMessageDialog(this, "Failed to load payments: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE));
            }

            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                for (Object[] row : rows) {
                    tableModel.addRow(row);
                }
            });
        }).start();
    }

    private void loadPaymentYears() {
        new Thread(() -> {
            List<String> years = new ArrayList<>();
            years.add("All");
            try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams("SELECT DISTINCT year FROM payment_cycle ORDER BY year")) {
                while (rs.next()) {
                    years.add(rs.getString("year"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(()
                        -> JOptionPane.showMessageDialog(this, "Failed to load years: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE));
            }

            SwingUtilities.invokeLater(() -> {
                paymentYearComboBox.removeAllItems();
                for (String y : years) {
                    paymentYearComboBox.addItem(y);
                }
            });
        }).start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        paymentYearComboBox = new javax.swing.JComboBox<>();
        jLabel13 = new javax.swing.JLabel();
        amountField = new javax.swing.JFormattedTextField();
        clearAllButton = new javax.swing.JButton();
        confirmPaymentButton = new javax.swing.JButton();
        jLabel28 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tempCopyTextArea = new javax.swing.JTextArea();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Full Name", "NIC", "Year", "Amount", "Due Date", "Paid Date", "Mobile", "Email"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setBackground(new java.awt.Color(102, 102, 102));
        jLabel1.setFont(new java.awt.Font("Palatino Linotype", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("View Due Payments");

        searchField.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Name / NIC / Member ID / Mobile");

        jLabel8.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Payment Year :");

        paymentYearComboBox.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        paymentYearComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel13.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("Amount :");

        amountField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        amountField.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N

        clearAllButton.setBackground(new java.awt.Color(204, 0, 0));
        clearAllButton.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        clearAllButton.setForeground(new java.awt.Color(255, 255, 255));
        clearAllButton.setText("Clear All");
        clearAllButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        clearAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAllButtonActionPerformed(evt);
            }
        });

        confirmPaymentButton.setBackground(new java.awt.Color(0, 153, 51));
        confirmPaymentButton.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        confirmPaymentButton.setForeground(new java.awt.Color(255, 255, 255));
        confirmPaymentButton.setText("Confirm Payment");
        confirmPaymentButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        confirmPaymentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmPaymentButtonActionPerformed(evt);
            }
        });

        jLabel28.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 0));
        jLabel28.setText("Temp area to keep copied txt :");

        tempCopyTextArea.setColumns(20);
        tempCopyTextArea.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        tempCopyTextArea.setForeground(new java.awt.Color(255, 255, 0));
        tempCopyTextArea.setLineWrap(true);
        tempCopyTextArea.setRows(5);
        tempCopyTextArea.setWrapStyleWord(true);
        tempCopyTextArea.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane2.setViewportView(tempCopyTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(593, 593, 593))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(clearAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(searchField))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel28)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(jScrollPane2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(confirmPaymentButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(paymentYearComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(amountField, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(amountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(paymentYearComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(confirmPaymentButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(clearAllButton)
                .addGap(8, 8, 8))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void clearAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAllButtonActionPerformed
        reset();
    }//GEN-LAST:event_clearAllButtonActionPerformed

    private void confirmPaymentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmPaymentButtonActionPerformed
        try {
            String nic = searchField.getText().trim();
            String paymentYear = (String) paymentYearComboBox.getSelectedItem();
            String amountText = amountField.getText().trim();

            if (nic.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter or select NIC first.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (paymentYear == null || "All".equals(paymentYear)) {
                JOptionPane.showMessageDialog(this, "Please select a valid payment year.", "Validation", JOptionPane.WARNING_MESSAGE);
                paymentYearComboBox.grabFocus();
                return;
            }
            if (amountText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter the amount.", "Validation", JOptionPane.WARNING_MESSAGE);
                amountField.grabFocus();
                return;
            }

            BigDecimal amount = new BigDecimal(amountText);

            // Find unpaid payment record for this member and year (cycle ids 2, 4, 6)
            String sql = "SELECT p.member_id, p.payment_cycle_id, m.status_id "
                    + "FROM payments p "
                    + "JOIN member m ON m.id = p.member_id "
                    + "JOIN payment_cycle pc ON pc.id = p.payment_cycle_id "
                    + "WHERE m.nic = ? AND pc.year = ? AND pc.payment_status_id = 2 AND p.paid_date IS NULL "
                    + "LIMIT 1";

            try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql, nic, paymentYear)) {
                if (rs.next()) {
                    long memberId = rs.getLong("member_id");
                    long unpaidCycleId = rs.getLong("payment_cycle_id");
                    int statusId = rs.getInt("status_id");

                    // Determine the corresponding paid cycle ID for this unpaid cycle
                    long paidCycleId = -1;
                    long nextUnpaidCycleId = -1;

                    switch ((int) unpaidCycleId) {
                        case 2: // 1st year unpaid → paid is 1, next unpaid 2nd year = 4
                            paidCycleId = 1;
                            nextUnpaidCycleId = 4;
                            break;
                        case 4: // 2nd year unpaid → paid is 3, next unpaid 3rd year = 6
                            paidCycleId = 3;
                            nextUnpaidCycleId = 6;
                            break;
                        case 6: // 3rd year unpaid → paid is 5, no next unpaid
                            paidCycleId = 5;
                            nextUnpaidCycleId = -1;
                            break;
                        default:
                            JOptionPane.showMessageDialog(this, "Invalid unpaid payment cycle id: " + unpaidCycleId, "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                    }

                    // Update existing unpaid payment record: set paid_date, amount, and update cycle id to paidCycleId
                    String updatePaymentSql = "UPDATE payments SET paid_date = CURDATE(), amount = ?, payment_cycle_id = ? "
                            + "WHERE member_id = ? AND payment_cycle_id = ? AND paid_date IS NULL";
                    int updatedRows = db.DatabaseConnector.executeUpdateWithParams(updatePaymentSql, amount, paidCycleId, memberId, unpaidCycleId);

                    // Activate member if status was deactivated (status_id == 2)
                    if (statusId == 2) {
                        db.DatabaseConnector.executeUpdateWithParams("UPDATE member SET status_id = 1 WHERE id = ?", memberId);
                    }

                    // Insert next unpaid payment if applicable
                    if (nextUnpaidCycleId != -1) {
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.MONTH, 12);
                        java.sql.Date dueDate = new java.sql.Date(cal.getTimeInMillis());

                        String insertSql = "INSERT INTO payments (system_user_id, member_id, amount, due_date, paid_date, payment_cycle_id) "
                                + "VALUES (?, ?, ?, ?, NULL, ?)";
                        db.DatabaseConnector.executeUpdateWithParams(insertSql, SignIn.Session.systemUserId, memberId, amount, dueDate, nextUnpaidCycleId);
                    }

                    if (updatedRows > 0) {
                        JOptionPane.showMessageDialog(this, "Payment confirmed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Payment already marked as paid.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }

                    loadPayments();

                } else {
                    JOptionPane.showMessageDialog(this, "Payment record not found (maybe already paid).", "Not Found", JOptionPane.WARNING_MESSAGE);
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to confirm payment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_confirmPaymentButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField amountField;
    private javax.swing.JButton clearAllButton;
    private javax.swing.JButton confirmPaymentButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JComboBox<String> paymentYearComboBox;
    private javax.swing.JTextField searchField;
    private javax.swing.JTextArea tempCopyTextArea;
    // End of variables declaration//GEN-END:variables

    private void reset() {
        searchField.setText("");
        amountField.setText("");
        tempCopyTextArea.setText("");
        paymentYearComboBox.setSelectedIndex(0);
    }
}
