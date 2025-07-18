package gui;

import db.DatabaseConnector;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FoxC
 */
public class AddPollingDivisions extends javax.swing.JPanel {

    private String selectedPollingDivisionId = null;

    private static HashMap<String, String> districtMap = new HashMap<>();

    /**
     * Creates new form AddPollingDivisions
     */
    public AddPollingDivisions() {
        initComponents();
        loadDistricts();
        loadPollingDivisions();
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

    private void loadPollingDivisions() {
        try {
            String districtName = String.valueOf(districtComboBox.getSelectedItem());
            String districtId = districtMap.get(districtName);

            String searchText = pollingDivisionField.getText().trim();

            String sql;
            ResultSet resultSet;

            if ((districtId == null || districtName.equals("Select")) && searchText.isEmpty()) {
                // no filter: load all
                sql = "SELECT id, name, district_id FROM polling_division ORDER BY name ASC";
                resultSet = DatabaseConnector.executeQuery(sql);

            } else if ((districtId == null || districtName.equals("Select")) && !searchText.isEmpty()) {
                // only filter by name
                sql = "SELECT id, name, district_id FROM polling_division WHERE name LIKE ? ORDER BY name ASC";
                resultSet = DatabaseConnector.executeQueryWithParams(sql, "%" + searchText + "%");

            } else if (!searchText.isEmpty()) {
                // filter by district AND name
                sql = "SELECT id, name, district_id FROM polling_division WHERE district_id = ? AND name LIKE ? ORDER BY name ASC";
                resultSet = DatabaseConnector.executeQueryWithParams(sql, districtId, "%" + searchText + "%");

            } else {
                // filter only by district
                sql = "SELECT id, name, district_id FROM polling_division WHERE district_id = ? ORDER BY name ASC";
                resultSet = DatabaseConnector.executeQueryWithParams(sql, districtId);
            }

            DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
            dtm.setRowCount(0);

            while (resultSet.next()) {
                Vector<String> vector = new Vector<>();
                vector.add(resultSet.getString("id"));
                vector.add(resultSet.getString("name"));
                vector.add(resultSet.getString("district_id"));
                dtm.addRow(vector);
            }

            if (resultSet != null) {
                resultSet.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
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

        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        pollingDivisionField = new javax.swing.JTextField();
        addButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        clearButton = new javax.swing.JButton();
        districtComboBox = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        jLabel1.setFont(new java.awt.Font("Palatino Linotype", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Add Polling Division");

        jLabel3.setFont(new java.awt.Font("Monospaced", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 0));
        jLabel3.setText("! WARNING !   USE THIS PATTERN   Ex:- Gampaha");

        jLabel2.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Type P.Division");

        pollingDivisionField.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        pollingDivisionField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                pollingDivisionFieldKeyReleased(evt);
            }
        });

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
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Polling Division"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

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

        districtComboBox.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        districtComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        districtComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                districtComboBoxItemStateChanged(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Select District");

        jLabel5.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 0));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(433, 433, 433)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(198, 198, 198)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(pollingDivisionField)
                                    .addComponent(districtComboBox, 0, 266, Short.MAX_VALUE))))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(1, 1, 1)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(districtComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pollingDivisionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        String pollingDivisionName = pollingDivisionField.getText().trim();
        String districtName = String.valueOf(districtComboBox.getSelectedItem());

        try {
            if (districtName.equals("Select")) {
                JOptionPane.showMessageDialog(this, "Please select the District first.", "Warning", JOptionPane.WARNING_MESSAGE);
                districtComboBox.grabFocus();
            } else if (pollingDivisionName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please type the Polling Division name.", "Warning", JOptionPane.WARNING_MESSAGE);
                pollingDivisionField.grabFocus();
            } else {
                String sqlCheck = "SELECT * FROM polling_division WHERE name = ?";
                ResultSet resultSet = DatabaseConnector.executeQueryWithParams(sqlCheck, pollingDivisionName);

                if (resultSet.next()) {
                    JOptionPane.showMessageDialog(this, "This Polling Division has already been added.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else {
                    String districtId = districtMap.get(districtName);
                    String sqlInsert = "INSERT INTO polling_division (name, district_id) VALUES (?, ?)";
                    DatabaseConnector.executeUpdateWithParams(sqlInsert, pollingDivisionName, districtId);

                    reset();
                    JOptionPane.showMessageDialog(this, "Polling Division added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        if (selectedPollingDivisionId == null) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.", "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            String pollingDivisionName = pollingDivisionField.getText().trim();
            String districtName = String.valueOf(districtComboBox.getSelectedItem());

            try {
                if (districtName.equals("Select")) {
                    JOptionPane.showMessageDialog(this, "Please select the District first.", "Warning", JOptionPane.WARNING_MESSAGE);
                    districtComboBox.grabFocus();
                } else if (pollingDivisionName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please type the Polling Division name.", "Warning", JOptionPane.WARNING_MESSAGE);
                    pollingDivisionField.grabFocus();
                } else {
                    String districtId = districtMap.get(districtName);

                    String sqlCheck = "SELECT * FROM polling_division WHERE name = ? AND id != ?";
                    ResultSet resultSet = DatabaseConnector.executeQueryWithParams(sqlCheck, pollingDivisionName, selectedPollingDivisionId);

                    if (resultSet.next()) {
                        JOptionPane.showMessageDialog(this, "Another Polling Division already has this name.", "Warning", JOptionPane.WARNING_MESSAGE);
                    } else {
                        String sqlUpdate = "UPDATE polling_division SET name = ?, district_id = ? WHERE id = ?";
                        DatabaseConnector.executeUpdateWithParams(sqlUpdate, pollingDivisionName, districtId, selectedPollingDivisionId);

                        reset();
                        JOptionPane.showMessageDialog(this, "Polling Division updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }

                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_updateButtonActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        int row = jTable1.getSelectedRow();

        String pollingId = String.valueOf(jTable1.getValueAt(row, 0)); // column 0 = id
        String pollingName = String.valueOf(jTable1.getValueAt(row, 1)); // column 1 = name

        selectedPollingDivisionId = pollingId;

        pollingDivisionField.setText(pollingName);

        try {
            String sql = "SELECT district_id FROM polling_division WHERE id = ?";
            ResultSet rs = DatabaseConnector.executeQueryWithParams(sql, pollingId);

            if (rs.next()) {
                String districtId = rs.getString("district_id");

                String districtName = null;
                for (Map.Entry<String, String> entry : districtMap.entrySet()) {
                    if (entry.getValue().equals(districtId)) {
                        districtName = entry.getKey();
                        break;
                    }
                }
                if (districtName != null) {
                    districtComboBox.setSelectedItem(districtName);
                }
            }
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        addButton.setEnabled(false);
    }//GEN-LAST:event_jTable1MouseClicked

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        reset();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void districtComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_districtComboBoxItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            loadPollingDivisions();
        }
    }//GEN-LAST:event_districtComboBoxItemStateChanged

    private void pollingDivisionFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pollingDivisionFieldKeyReleased
        loadPollingDivisions();
    }//GEN-LAST:event_pollingDivisionFieldKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JComboBox<String> districtComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField pollingDivisionField;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables

    private void reset() {
         districtComboBox.setSelectedItem("Select");
        pollingDivisionField.setText("");
        jTable1.clearSelection();
        addButton.setEnabled(true);
        loadPollingDivisions();
        selectedPollingDivisionId = null;
    }
}
