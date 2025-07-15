package gui;

import com.sun.jdi.connect.spi.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import db.DatabaseConnector;
import static db.DatabaseConnector.getConnection;
import javax.swing.JOptionPane;

/**
 *
 * @author FoxC
 */
public class AddAddress extends javax.swing.JFrame {

    private String selectedDistrictId = null;
    private String memberId;

    private static HashMap<String, String> districtMap = new HashMap<>();
    private static HashMap<String, String> pollingDivisionMap = new HashMap<>();
    private static HashMap<String, String> divisionalSecretariatsMap = new HashMap<>();

    /**
     * Creates new form AddAddress
     */
    public AddAddress(String memberId) {
        initComponents();
        this.memberId = memberId;
        loadDistricts();
        filterPollingDivisionsAndDivisionalSecretariats();
        loadMemberNIC();
        loadMemberAddressIfExists();
    }

    private void loadMemberNIC() {
        try {
            String sql = "SELECT nic FROM member WHERE id = ?";
            ResultSet rs = DatabaseConnector.executeQueryWithParams(sql, memberId);

            if (rs.next()) {
                String nic = rs.getString("nic");
                memberNICLabel.setText(nic);
            } else {
                memberNICLabel.setText("NIC not found");
            }

            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading NIC: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDistricts() {
        try {
            String sql = "SELECT district.id, district.name FROM district INNER JOIN province "
                    + "ON district.province_id = province.id ORDER BY district.name ASC";
            ResultSet resultSet = DatabaseConnector.executeQuery(sql);

            Vector<String> vector = new Vector<>();
            vector.add("Select");

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String id = resultSet.getString("id");
                vector.add(name);
                districtMap.put(name, id);
            }

            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(vector);
            districtComboBox.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterPollingDivisionsAndDivisionalSecretariats() {
        String districtName = String.valueOf(districtComboBox.getSelectedItem());
        String districtId = districtMap.get(districtName);

        try {
            // ------------------------------
            // Filter Polling Divisions
            // ------------------------------
            Vector<String> pollingVector = new Vector<>();
            pollingVector.add("Select Polling Division");

            ResultSet rsPolling;
            if (districtId == null || districtName.equals("Select")) {
                String sql = "SELECT id, name FROM polling_division ORDER BY name ASC";
                rsPolling = DatabaseConnector.executeQuery(sql);
            } else {
                String sql = "SELECT id, name FROM polling_division WHERE district_id = ? ORDER BY name ASC";
                rsPolling = DatabaseConnector.executeQueryWithParams(sql, districtId);
            }

            pollingDivisionMap.clear();
            while (rsPolling.next()) {
                String name = rsPolling.getString("name");
                String id = rsPolling.getString("id");
                pollingVector.add(name);
                pollingDivisionMap.put(name, id);
            }
            pollingDivisionComboBox.setModel(new DefaultComboBoxModel<>(pollingVector));

            if (rsPolling != null) {
                rsPolling.close();
            }

            // ------------------------------
            // Filter Divisional Secretariats
            // ------------------------------
            Vector<String> dsVector = new Vector<>();
            dsVector.add("Select Divisional Secretariat");

            ResultSet rsDS;
            if (districtId == null || districtName.equals("Select")) {
                String sql = "SELECT id, name FROM divisional_secretariat ORDER BY name ASC";
                rsDS = DatabaseConnector.executeQuery(sql);
            } else {
                String sql = "SELECT id, name FROM divisional_secretariat WHERE district_id = ? ORDER BY name ASC";
                rsDS = DatabaseConnector.executeQueryWithParams(sql, districtId);
            }

            divisionalSecretariatsMap.clear();
            while (rsDS.next()) {
                String name = rsDS.getString("name");
                String id = rsDS.getString("id");
                dsVector.add(name);
                divisionalSecretariatsMap.put(name, id);
            }
            divisionalSecretariatsComboBox.setModel(new DefaultComboBoxModel<>(dsVector));

            if (rsDS != null) {
                rsDS.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMemberAddressIfExists() {
        try {
            String sql = "SELECT a.id, a.line1, a.line2, a.divisional_secretariat_id, a.polling_division_id, "
                    + "ds.name AS ds_name, pd.name AS pd_name, d.id AS district_id, d.name AS district_name "
                    + "FROM member m "
                    + "INNER JOIN address a ON m.address_id = a.id "
                    + "INNER JOIN divisional_secretariat ds ON a.divisional_secretariat_id = ds.id "
                    + "INNER JOIN district d ON ds.district_id = d.id "
                    + "INNER JOIN polling_division pd ON a.polling_division_id = pd.id "
                    + "WHERE m.id = ?";

            ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql, memberId);

            if (rs.next()) {
                String line1 = rs.getString("line1");
                String line2 = rs.getString("line2");
                String districtName = rs.getString("district_name");
                String dsName = rs.getString("ds_name");
                String pdName = rs.getString("pd_name");

                addButton.setEnabled(false);
                clearButton.setEnabled(false);
                line1TextArea.setText(line1);
                line2TextArea.setText(line2);

                districtComboBox.setSelectedItem(districtName);

                filterPollingDivisionsAndDivisionalSecretariats();

                pollingDivisionComboBox.setSelectedItem(pdName);
                divisionalSecretariatsComboBox.setSelectedItem(dsName);
            }

            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this, "Error loading existing address: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        memberNICLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        line1TextArea = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        line2TextArea = new javax.swing.JTextField();
        addPollingDivisionButton = new javax.swing.JButton();
        divisionalSecretariatsComboBox = new javax.swing.JComboBox<>();
        pollingDivisionComboBox = new javax.swing.JComboBox<>();
        addDivisionalSecretariatButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        districtComboBox = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Add Member Address");
        setResizable(false);

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Palatino Linotype", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Add Member Address");

        addButton.setBackground(new java.awt.Color(0, 153, 51));
        addButton.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        addButton.setForeground(new java.awt.Color(255, 255, 255));
        addButton.setText("Add");
        addButton.setPreferredSize(new java.awt.Dimension(86, 31));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        updateButton.setBackground(new java.awt.Color(0, 153, 153));
        updateButton.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        updateButton.setForeground(new java.awt.Color(255, 255, 255));
        updateButton.setText("Update");
        updateButton.setPreferredSize(new java.awt.Dimension(86, 31));

        clearButton.setBackground(new java.awt.Color(204, 0, 0));
        clearButton.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        clearButton.setForeground(new java.awt.Color(255, 255, 255));
        clearButton.setText("Clear");
        clearButton.setPreferredSize(new java.awt.Dimension(86, 31));
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        memberNICLabel.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        memberNICLabel.setForeground(new java.awt.Color(255, 255, 255));
        memberNICLabel.setText("Member NIC");

        jLabel4.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Address Line 1");

        line1TextArea.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Address Line 2");

        line2TextArea.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N

        addPollingDivisionButton.setBackground(new java.awt.Color(0, 153, 51));
        addPollingDivisionButton.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        addPollingDivisionButton.setForeground(new java.awt.Color(255, 255, 255));
        addPollingDivisionButton.setText("+");
        addPollingDivisionButton.setPreferredSize(new java.awt.Dimension(86, 31));
        addPollingDivisionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPollingDivisionButtonActionPerformed(evt);
            }
        });

        divisionalSecretariatsComboBox.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        divisionalSecretariatsComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        pollingDivisionComboBox.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        pollingDivisionComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        addDivisionalSecretariatButton.setBackground(new java.awt.Color(0, 153, 51));
        addDivisionalSecretariatButton.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        addDivisionalSecretariatButton.setForeground(new java.awt.Color(255, 255, 255));
        addDivisionalSecretariatButton.setText("+");
        addDivisionalSecretariatButton.setPreferredSize(new java.awt.Dimension(86, 31));
        addDivisionalSecretariatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDivisionalSecretariatButtonActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("District");

        jLabel6.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 0));
        jLabel6.setText("⚠️ Click '+' to add unavailable items.");

        districtComboBox.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        districtComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        districtComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                districtComboBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(18, 18, 18)
                                .addComponent(line1TextArea))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(18, 18, 18)
                                .addComponent(line2TextArea, javax.swing.GroupLayout.PREFERRED_SIZE, 499, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(memberNICLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(districtComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(addDivisionalSecretariatButton, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                                    .addComponent(addPollingDivisionButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(pollingDivisionComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(divisionalSecretariatsComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(133, 133, 133)
                .addComponent(jLabel6)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(memberNICLabel)
                    .addComponent(jLabel3)
                    .addComponent(districtComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(line1TextArea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(line2TextArea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addPollingDivisionButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pollingDivisionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addDivisionalSecretariatButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(divisionalSecretariatsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void addPollingDivisionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPollingDivisionButtonActionPerformed
        AddPollingDivisions polling = new AddPollingDivisions();

        JDialog pollingDivision = new JDialog(AddAddress.this, "Polling Divisions", true);
        pollingDivision.setContentPane(polling);
        pollingDivision.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pollingDivision.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                filterPollingDivisionsAndDivisionalSecretariats();
            }
        });

        pollingDivision.pack();
        pollingDivision.setLocationRelativeTo(null);
        pollingDivision.setVisible(true);
    }//GEN-LAST:event_addPollingDivisionButtonActionPerformed

    private void addDivisionalSecretariatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDivisionalSecretariatButtonActionPerformed
        AddDivisionalSecretariats divisional = new AddDivisionalSecretariats();

        JDialog divisionalSecretariats = new JDialog(AddAddress.this, "Divisional Secretariats", true);
        divisionalSecretariats.setContentPane(divisional);
        divisionalSecretariats.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        divisionalSecretariats.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                filterPollingDivisionsAndDivisionalSecretariats();
            }
        });

        divisionalSecretariats.pack();
        divisionalSecretariats.setLocationRelativeTo(null);
        divisionalSecretariats.setVisible(true);
    }//GEN-LAST:event_addDivisionalSecretariatButtonActionPerformed

    private void districtComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_districtComboBoxItemStateChanged
        filterPollingDivisionsAndDivisionalSecretariats();
    }//GEN-LAST:event_districtComboBoxItemStateChanged

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        reset();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        String memberDistrict = String.valueOf(districtComboBox.getSelectedItem());
        String line1 = line1TextArea.getText().trim();
        String line2 = line2TextArea.getText().trim();
        String divisionalName = String.valueOf(divisionalSecretariatsComboBox.getSelectedItem());
        String pollingName = String.valueOf(pollingDivisionComboBox.getSelectedItem());

        if (memberDistrict.equals("Select")) {
            JOptionPane.showMessageDialog(this, "Please select District first.", "Warning", JOptionPane.WARNING_MESSAGE);
            districtComboBox.grabFocus();
        } else if (line1.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Address Line 1.", "Warning", JOptionPane.WARNING_MESSAGE);
            line1TextArea.grabFocus();
        } else if (line2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Address Line 2.", "Warning", JOptionPane.WARNING_MESSAGE);
            line2TextArea.grabFocus();
        } else if (pollingName.equals("Select Polling Division")) {
            JOptionPane.showMessageDialog(this, "Please select a Polling Division.", "Warning", JOptionPane.WARNING_MESSAGE);
            pollingDivisionComboBox.grabFocus();
        } else if (divisionalName.equals("Select Divisional Secretariat")) {
            JOptionPane.showMessageDialog(this, "Please select a Divisional Secretariat.", "Warning", JOptionPane.WARNING_MESSAGE);
            divisionalSecretariatsComboBox.grabFocus();
        } else {
            try {
                String pollingId = pollingDivisionMap.get(pollingName);
                String divisionalId = divisionalSecretariatsMap.get(divisionalName);

                String sqlInsert = "INSERT INTO address (line1, line2, divisional_secretariat_id, polling_division_id) VALUES (?, ?, ?, ?)";
                long addressId = DatabaseConnector.executeInsertAndReturnId(sqlInsert, line1, line2, divisionalId, pollingId);

                if (addressId > 0) {
                    String sqlUpdate = "UPDATE member SET address_id = ? WHERE id = ?";
                    int updated = DatabaseConnector.executeUpdateWithParams(sqlUpdate, String.valueOf(addressId), memberId);

                    if (updated > 0) {
                        JOptionPane.showMessageDialog(this, "Address saved and linked to member successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadMemberAddressIfExists();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to link address to member.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save address.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_addButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addDivisionalSecretariatButton;
    private javax.swing.JButton addPollingDivisionButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JComboBox<String> districtComboBox;
    private javax.swing.JComboBox<String> divisionalSecretariatsComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField line1TextArea;
    private javax.swing.JTextField line2TextArea;
    private javax.swing.JLabel memberNICLabel;
    private javax.swing.JComboBox<String> pollingDivisionComboBox;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables

    private void reset() {
        districtComboBox.setSelectedIndex(0);
        pollingDivisionComboBox.setSelectedIndex(0);
        divisionalSecretariatsComboBox.setSelectedIndex(0);
        addButton.setEnabled(true);
        filterPollingDivisionsAndDivisionalSecretariats();
    }
}
