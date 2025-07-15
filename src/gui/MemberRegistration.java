package gui;

import db.DatabaseConnector;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Fox C
 */
public class MemberRegistration extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MemberRegistration.class.getName());

    private static HashMap<String, String> genderMap = new HashMap<>();
    private static HashMap<String, String> memberTypeMap = new HashMap<>();

    /**
     * Creates new form MemberRegistration
     */
    public MemberRegistration() {
        initComponents();
        loadGender();
        loadMemberType();
        loadMembers();
        generateMemberID();
    }

    private void generateMemberID() {
        long memId = System.currentTimeMillis();
        memberIdField.setText(String.valueOf(memId));
        districtField.setText("Not Set");
    }

    private void loadGender() {
        try {
            String sql = "SELECT * FROM `gender`";
            ResultSet resultSet = DatabaseConnector.executeQuery(sql);

            Vector<String> vector = new Vector<>();
            vector.add("Select");

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String id = resultSet.getString("id");
                vector.add(name);
                genderMap.put(name, id);
            }

            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(vector);
            genderComboBox.setModel(model);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMemberType() {
        try {
            String sql = "SELECT * FROM `member_type`";
            ResultSet resultSet = DatabaseConnector.executeQuery(sql);

            Vector<String> vector = new Vector<>();
            vector.add("Select");

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String id = resultSet.getString("id");
                vector.add(name);
                memberTypeMap.put(name, id);
            }

            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(vector);
            memberTypeComboBox.setModel(model);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadMembers() {
        try {
            String sql = "SELECT m.id, m.full_name, m.name_with_initials, m.campus_id, m.nic, m.birthday, "
                    + "m.email, m.mobile, mt.name AS member_type, s.name AS status, g.name AS gender "
                    + "FROM member m "
                    + "INNER JOIN member_type mt ON m.member_type_id = mt.id "
                    + "INNER JOIN status s ON m.status_id = s.id "
                    + "INNER JOIN gender g ON m.gender_id = g.id "
                    + "ORDER BY m.full_name ASC";

            ResultSet rs = DatabaseConnector.executeQuery(sql);

            DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
            dtm.setRowCount(0);

            while (rs.next()) {
                Vector<String> v = new Vector<>();
                v.add(rs.getString("id"));
                v.add(rs.getString("full_name"));
                v.add(rs.getString("name_with_initials"));
                v.add(rs.getString("campus_id"));
                v.add(rs.getString("nic"));
                v.add(rs.getString("birthday"));
                v.add(rs.getString("email"));
                v.add(rs.getString("mobile"));
                v.add(rs.getString("member_type"));
                v.add(rs.getString("status"));
                v.add(rs.getString("gender"));

                dtm.addRow(v);
            }
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchMembersByNic() {
        String searchText = searchField.getText().trim();

        try {
            String sql;
            ResultSet rs;

            if (searchText.isEmpty()) {
                // no filter, load all
                sql = "SELECT m.id, m.full_name, m.name_with_initials, m.campus_id, m.nic, m.birthday, "
                        + "m.email, m.mobile, mt.name AS member_type, s.name AS status, g.name AS gender "
                        + "FROM member m "
                        + "INNER JOIN member_type mt ON m.member_type_id = mt.id "
                        + "INNER JOIN status s ON m.status_id = s.id "
                        + "INNER JOIN gender g ON m.gender_id = g.id "
                        + "ORDER BY m.full_name ASC";
                rs = DatabaseConnector.executeQuery(sql);
            } else {
                // filter by NIC like
                sql = "SELECT m.id, m.full_name, m.name_with_initials, m.campus_id, m.nic, m.birthday, "
                        + "m.email, m.mobile, mt.name AS member_type, s.name AS status, g.name AS gender "
                        + "FROM member m "
                        + "INNER JOIN member_type mt ON m.member_type_id = mt.id "
                        + "INNER JOIN status s ON m.status_id = s.id "
                        + "INNER JOIN gender g ON m.gender_id = g.id "
                        + "WHERE m.nic LIKE ? "
                        + "ORDER BY m.full_name ASC";
                rs = DatabaseConnector.executeQueryWithParams(sql, "%" + searchText + "%");
            }

            DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
            dtm.setRowCount(0);

            while (rs.next()) {
                Vector<String> v = new Vector<>();
                v.add(rs.getString("id"));
                v.add(rs.getString("full_name"));
                v.add(rs.getString("name_with_initials"));
                v.add(rs.getString("campus_id"));
                v.add(rs.getString("nic"));
                v.add(rs.getString("birthday"));
                v.add(rs.getString("email"));
                v.add(rs.getString("mobile"));
                v.add(rs.getString("member_type"));
                v.add(rs.getString("status"));
                v.add(rs.getString("gender"));

                dtm.addRow(v);
            }

            if (rs != null) {
                rs.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMemberDistrictName(String memberId) {
        String districtName = "";
        try {
            String sql = "SELECT d.name AS district_name "
                    + "FROM member m "
                    + "INNER JOIN address a ON m.address_id = a.id "
                    + "INNER JOIN divisional_secretariat ds ON a.divisional_secretariat_id = ds.id "
                    + "INNER JOIN district d ON ds.district_id = d.id "
                    + "WHERE m.id = ?";

            ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql, memberId);
            if (rs.next()) {
                districtName = rs.getString("district_name");
            }

            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this, "Error loading district: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return districtName;
    }

    private void refreshMemberDistrict(String memberId) {
        try {
            String districtName = getMemberDistrictName(memberId);
            if (districtName != null && !districtName.isEmpty()) {
                districtField.setText(districtName);
            } else {
                districtField.setText("Not set");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            districtField.setText("Error");
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
        fullNameField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        withIniField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        campusIdField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        nicField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        mobileField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        districtField = new javax.swing.JTextField();
        registerButton = new javax.swing.JButton();
        genderComboBox = new javax.swing.JComboBox<>();
        memberTypeComboBox = new javax.swing.JComboBox<>();
        bDayChooser = new com.toedter.calendar.JDateChooser();
        updateButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tempCopyTextArea = new javax.swing.JTextArea();
        jLabel15 = new javax.swing.JLabel();
        memberIdField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        paidAmountField = new javax.swing.JFormattedTextField();
        viewFullMembersButton = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Alumini System  Member Registration");
        setBackground(new java.awt.Color(153, 153, 153));
        setResizable(false);

        fullNameField.setFont(new java.awt.Font("Verdana", 1, 24)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Full Name :");

        jLabel1.setFont(new java.awt.Font("Palatino Linotype", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Member Registration");

        jTable1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Full Name", "Name with IN", "Campus ID", "NIC", "Birthday", "Email", "Mobile", "Member Type", "Status", "Gender"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false
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

        withIniField.setFont(new java.awt.Font("Verdana", 1, 24)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("With Initils :");

        jLabel4.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Campus ID :");

        campusIdField.setFont(new java.awt.Font("Verdana", 1, 24)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("NIC :");

        nicField.setFont(new java.awt.Font("Verdana", 1, 24)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Birth Day :");

        jLabel7.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Email :");

        emailField.setFont(new java.awt.Font("Verdana", 1, 24)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Mobile :");

        mobileField.setFont(new java.awt.Font("Verdana", 1, 24)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Gender :");

        jLabel10.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Member Type :");

        jLabel11.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Address :");

        districtField.setEditable(false);
        districtField.setFont(new java.awt.Font("Verdana", 1, 24)); // NOI18N
        districtField.setFocusable(false);

        registerButton.setBackground(new java.awt.Color(0, 153, 51));
        registerButton.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        registerButton.setForeground(new java.awt.Color(255, 255, 255));
        registerButton.setText("Register Member");
        registerButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        registerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registerButtonActionPerformed(evt);
            }
        });

        genderComboBox.setFont(new java.awt.Font("Verdana", 1, 24)); // NOI18N
        genderComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        memberTypeComboBox.setFont(new java.awt.Font("Verdana", 1, 24)); // NOI18N
        memberTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        bDayChooser.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N

        updateButton.setBackground(new java.awt.Color(0, 153, 153));
        updateButton.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        updateButton.setForeground(new java.awt.Color(255, 255, 255));
        updateButton.setText("Update Member");
        updateButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        clearButton.setBackground(new java.awt.Color(204, 0, 0));
        clearButton.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        clearButton.setForeground(new java.awt.Color(255, 255, 255));
        clearButton.setText("Clear All");
        clearButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        searchField.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchFieldKeyReleased(evt);
            }
        });

        searchButton.setBackground(new java.awt.Color(0, 153, 51));
        searchButton.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        searchButton.setForeground(new java.awt.Color(255, 255, 255));
        searchButton.setText("Search");
        searchButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("Search by NIC :");

        tempCopyTextArea.setColumns(20);
        tempCopyTextArea.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        tempCopyTextArea.setForeground(new java.awt.Color(255, 255, 0));
        tempCopyTextArea.setLineWrap(true);
        tempCopyTextArea.setRows(5);
        tempCopyTextArea.setWrapStyleWord(true);
        tempCopyTextArea.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane2.setViewportView(tempCopyTextArea);

        jLabel15.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 0));
        jLabel15.setText("Temp area to keep copied txt :");

        memberIdField.setEditable(false);
        memberIdField.setBackground(new java.awt.Color(102, 102, 102));
        memberIdField.setFont(new java.awt.Font("Verdana", 1, 24)); // NOI18N
        memberIdField.setForeground(new java.awt.Color(255, 204, 0));
        memberIdField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        memberIdField.setText("Member ID");

        jLabel13.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("1st Payment :");

        paidAmountField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        paidAmountField.setFont(new java.awt.Font("Verdana", 1, 24)); // NOI18N

        viewFullMembersButton.setBackground(new java.awt.Color(204, 204, 0));
        viewFullMembersButton.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        viewFullMembersButton.setForeground(new java.awt.Color(255, 255, 255));
        viewFullMembersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/records.png"))); // NOI18N
        viewFullMembersButton.setPreferredSize(new java.awt.Dimension(75, 31));
        viewFullMembersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewFullMembersButtonActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Monospaced", 1, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 0));
        jLabel14.setText("View Full");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(updateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel6)
                            .addComponent(jLabel2)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(genderComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(mobileField)
                            .addComponent(emailField)
                            .addComponent(fullNameField)
                            .addComponent(campusIdField)
                            .addComponent(nicField)
                            .addComponent(bDayChooser, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(withIniField, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addComponent(registerButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel13)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel14)
                                    .addComponent(viewFullMembersButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jLabel11)))
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(paidAmountField)
                            .addComponent(memberIdField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                            .addComponent(memberTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(districtField, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(searchField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1))
                        .addGap(6, 6, 6))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addGap(607, 607, 607))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 649, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6))))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(searchButton)
                            .addComponent(jLabel12)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fullNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(withIniField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(campusIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nicField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6)
                            .addComponent(bDayChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(mobileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(genderComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(memberTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(paidAmountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(districtField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(55, 55, 55)
                                .addComponent(memberIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(viewFullMembersButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(0, 35, Short.MAX_VALUE)
                                .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(registerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void registerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerButtonActionPerformed
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String memberID = memberIdField.getText();
        String fullName = fullNameField.getText().trim();
        String nameWithIni = withIniField.getText().trim();
        String uniId = campusIdField.getText().trim();
        String nic = nicField.getText().trim();
        Date dateOfBirth = bDayChooser.getDate();
        String email = emailField.getText().trim();
        String mobile = mobileField.getText().trim();
        String gender = String.valueOf(genderComboBox.getSelectedItem());
        String memberType = String.valueOf(memberTypeComboBox.getSelectedItem());
        String paidAmount = paidAmountField.getText().trim();
        Date currentDate = new Date();

        if (memberID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Something went wrong. Contact developers.", "System Error", JOptionPane.ERROR_MESSAGE);
        } else if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the Full Name.", "Warning", JOptionPane.WARNING_MESSAGE);
            fullNameField.grabFocus();
        } else if (nameWithIni.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the Name with Initials.", "Warning", JOptionPane.WARNING_MESSAGE);
            withIniField.grabFocus();
        } else if (uniId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the Campus ID.", "Warning", JOptionPane.WARNING_MESSAGE);
            campusIdField.grabFocus();
        } else if (nic.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the NIC number.", "Warning", JOptionPane.WARNING_MESSAGE);
            nicField.grabFocus();
        } else if (!nic.matches("^([0-9]{9}[vVxX]|[1-2]{1}[0-9]{11})$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid NIC number.", "Warning", JOptionPane.WARNING_MESSAGE);
            nicField.grabFocus();
        } else if (dateOfBirth == null) {
            JOptionPane.showMessageDialog(this, "Please select the Date of Birth.", "Warning", JOptionPane.WARNING_MESSAGE);
            bDayChooser.grabFocus();
        } else if (dateOfBirth.after(currentDate)) {
            JOptionPane.showMessageDialog(this, "Date of Birth cannot be in the future.", "Warning", JOptionPane.WARNING_MESSAGE);
            bDayChooser.grabFocus();
        } else if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the Email address.", "Warning", JOptionPane.WARNING_MESSAGE);
            emailField.grabFocus();
        } else if (!email.matches("^[\\w\\.-]+@[\\w\\.-]+\\.\\w{2,}$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Email address.", "Warning", JOptionPane.WARNING_MESSAGE);
            emailField.grabFocus();
        } else if (mobile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the Mobile number.", "Warning", JOptionPane.WARNING_MESSAGE);
            mobileField.grabFocus();
        } else if (!mobile.matches("^07[01245678][0-9]{7}$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Sri Lankan Mobile number.", "Warning", JOptionPane.WARNING_MESSAGE);
            mobileField.grabFocus();
        } else if (gender.equals("Select")) {
            JOptionPane.showMessageDialog(this, "Please select the Gender.", "Warning", JOptionPane.WARNING_MESSAGE);
            genderComboBox.grabFocus();
        } else if (memberType.equals("Select")) {
            JOptionPane.showMessageDialog(this, "Please select the Member Type.", "Warning", JOptionPane.WARNING_MESSAGE);
            memberTypeComboBox.grabFocus();
        } else if (paidAmount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the Paid Amount.", "Warning", JOptionPane.WARNING_MESSAGE);
            paidAmountField.grabFocus();
        } else if (!paidAmount.matches("\\d+(\\.\\d{1,2})?")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric Paid Amount.", "Warning", JOptionPane.WARNING_MESSAGE);
            paidAmountField.grabFocus();
        } else {
            try {
                String sqlCheck = "SELECT * FROM member WHERE nic = ? OR mobile = ? OR email = ?";
                ResultSet rs = DatabaseConnector.executeQueryWithParams(sqlCheck, nic, mobile, email);

                if (rs.next()) {
                    if (rs.getString("nic").equals(nic)) {
                        JOptionPane.showMessageDialog(this, "This NIC is already used.", "Warning", JOptionPane.WARNING_MESSAGE);
                        nicField.grabFocus();
                    } else if (rs.getString("mobile").equals(mobile)) {
                        JOptionPane.showMessageDialog(this, "This Mobile number is already used.", "Warning", JOptionPane.WARNING_MESSAGE);
                        mobileField.grabFocus();
                    } else if (rs.getString("email").equals(email)) {
                        JOptionPane.showMessageDialog(this, "This Email address is already used.", "Warning", JOptionPane.WARNING_MESSAGE);
                        emailField.grabFocus();
                    }
                } else {
                    String genderId = genderMap.get(gender);
                    String memberTypeId = memberTypeMap.get(memberType);

                    String sqlInsert = "INSERT INTO member (id, full_name, name_with_initials, campus_id, nic, birthday, email, mobile, reg_date, member_type_id, status_id, gender_id) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?)";
                    int rows = DatabaseConnector.executeUpdateWithParams(
                            sqlInsert,
                            memberID,
                            fullName,
                            nameWithIni,
                            uniId,
                            nic,
                            new java.sql.Date(dateOfBirth.getTime()),
                            email,
                            mobile,
                            memberTypeId,
                            "1",
                            genderId
                    );

                    if (rows > 0) {
                        // ✅ Insert first payment as PAID
                        String sqlPayment1 = "INSERT INTO payments (system_user_id, member_id, amount, due_date, paid_date, payment_cycle_id) "
                                + "VALUES (?, ?, ?, NULL, NOW(), ?)";
                        int paymentRows1 = DatabaseConnector.executeUpdateWithParams(
                                sqlPayment1,
                                SignIn.Session.systemUserId,
                                memberID,
                                paidAmount,
                                "1"
                        );

                        if (paymentRows1 > 0) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(new Date()); // paid_date is now
                            cal.add(Calendar.MONTH, 12);
                            java.sql.Date secondYearDueDate = new java.sql.Date(cal.getTimeInMillis());

                            String sqlPayment2 = "INSERT INTO payments (system_user_id, member_id, amount, due_date, paid_date, payment_cycle_id) "
                                    + "VALUES (?, ?, ?, ?, NULL, ?)";
                            int paymentRows2 = DatabaseConnector.executeUpdateWithParams(
                                    sqlPayment2,
                                    SignIn.Session.systemUserId,
                                    memberID,
                                    paidAmount,
                                    secondYearDueDate,
                                    "4"
                            );

                            if (paymentRows2 > 0) {
                                JOptionPane.showMessageDialog(this, "Member registered and first & second year payments saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(this, "Member registered, first payment saved but failed to add second year payment.", "Warning", JOptionPane.WARNING_MESSAGE);
                            }

                            reset();
                        } else {
                            JOptionPane.showMessageDialog(this, "Member registered but failed to save first payment.", "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to register member.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

    }//GEN-LAST:event_registerButtonActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        int row = jTable1.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a Member to update.", "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            String memberID = memberIdField.getText();
            String fullName = fullNameField.getText().trim();
            String nameWithIni = withIniField.getText().trim();
            String uniId = campusIdField.getText().trim();
            String nic = nicField.getText().trim();
            Date dateOfBirth = bDayChooser.getDate();
            String email = emailField.getText().trim();
            String mobile = mobileField.getText().trim();
            String gender = String.valueOf(genderComboBox.getSelectedItem());
            String memberType = String.valueOf(memberTypeComboBox.getSelectedItem());
            Date currentDate = new Date();

            try {
                if (memberID.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Something went wrong. Contact developers.", "System Error", JOptionPane.ERROR_MESSAGE);
                } else if (fullName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter the Full Name.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else if (nameWithIni.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter the Name with Initials.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else if (uniId.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter the Campus ID.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else if (nic.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter the NIC number.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else if (!nic.matches("^([0-9]{9}[vVxX]|[1-2]{1}[0-9]{11})$")) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid NIC number.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else if (dateOfBirth == null) {
                    JOptionPane.showMessageDialog(this, "Please select the Date of Birth.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else if (dateOfBirth.after(currentDate)) {
                    JOptionPane.showMessageDialog(this, "Date of Birth cannot be in the future.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else if (email.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter the Email address.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else if (!email.matches("^[\\w\\.-]+@[\\w\\.-]+\\.\\w{2,}$")) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid Email address.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else if (mobile.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter the Mobile number.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else if (!mobile.matches("^07[01245678][0-9]{7}$")) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid Sri Lankan Mobile number.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else if (gender.equals("Select")) {
                    JOptionPane.showMessageDialog(this, "Please select the Gender.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else if (memberType.equals("Select")) {
                    JOptionPane.showMessageDialog(this, "Please select the Member Type.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else {
                    int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to update this member?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {

                        String genderId = genderMap.get(gender);
                        String memberTypeId = memberTypeMap.get(memberType);

                        String sqlCheck = "SELECT * FROM member WHERE (nic = ? OR email = ? OR mobile = ?) AND id != ?";
                        ResultSet rs = DatabaseConnector.executeQueryWithParams(sqlCheck, nic, email, mobile, memberID);

                        if (rs.next()) {
                            if (nic.equals(rs.getString("nic"))) {
                                JOptionPane.showMessageDialog(this, "This NIC is already used by another member.", "Warning", JOptionPane.WARNING_MESSAGE);
                            } else if (email.equals(rs.getString("email"))) {
                                JOptionPane.showMessageDialog(this, "This Email is already used by another member.", "Warning", JOptionPane.WARNING_MESSAGE);
                            } else if (mobile.equals(rs.getString("mobile"))) {
                                JOptionPane.showMessageDialog(this, "This Mobile number is already used by another member.", "Warning", JOptionPane.WARNING_MESSAGE);
                            }
                        } else {
                            String sqlUpdate = "UPDATE member SET full_name = ?, name_with_initials = ?, campus_id = ?, nic = ?, birthday = ?, email = ?, mobile = ?, member_type_id = ?, gender_id = ? WHERE id = ?";
                            int rows = DatabaseConnector.executeUpdateWithParams(
                                    sqlUpdate, fullName, nameWithIni, uniId, nic, new java.sql.Date(dateOfBirth.getTime()), email, mobile, memberTypeId, genderId, memberID
                            );

                            if (rows > 0) {
                                JOptionPane.showMessageDialog(this, "Member updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                                reset();
                                loadMembers();
                                generateMemberID();
                            } else {
                                JOptionPane.showMessageDialog(this, "Failed to update member.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        if (rs != null) {
                            rs.close();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_updateButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        reset();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void searchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchFieldKeyReleased
        searchMembersByNic();
    }//GEN-LAST:event_searchFieldKeyReleased

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        searchField.grabFocus();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        registerButton.setEnabled(false);
        int row = jTable1.getSelectedRow();

        if (row != -1) {
            try {
                String memberId = String.valueOf(jTable1.getValueAt(row, 0));
                String fullName = String.valueOf(jTable1.getValueAt(row, 1));
                String nameWithIni = String.valueOf(jTable1.getValueAt(row, 2));
                String campusId = String.valueOf(jTable1.getValueAt(row, 3));
                String nic = String.valueOf(jTable1.getValueAt(row, 4));
                String birthday = String.valueOf(jTable1.getValueAt(row, 5));
                String email = String.valueOf(jTable1.getValueAt(row, 6));
                String mobile = String.valueOf(jTable1.getValueAt(row, 7));
                String memberType = String.valueOf(jTable1.getValueAt(row, 8));
                String status = String.valueOf(jTable1.getValueAt(row, 9));
                String gender = String.valueOf(jTable1.getValueAt(row, 10));

                memberIdField.setText(memberId);
                fullNameField.setText(fullName);
                withIniField.setText(nameWithIni);
                campusIdField.setText(campusId);
                nicField.setText(nic);
                emailField.setText(email);
                mobileField.setText(mobile);
                genderComboBox.setSelectedItem(gender);
                memberTypeComboBox.setSelectedItem(memberType);

                // Load and set district
                refreshMemberDistrict(memberId);

                // Load latest paid amount
                try {
                    String paidSql = "SELECT amount FROM payments WHERE member_id = ? ORDER BY paid_date DESC LIMIT 1";
                    ResultSet paidRs = DatabaseConnector.executeQueryWithParams(paidSql, memberId);
                    if (paidRs.next()) {
                        String paidAmount = paidRs.getString("amount");
                        paidAmountField.setText(paidAmount);
                    } else {
                        paidAmountField.setText("0");
                    }
                    if (paidRs != null) {
                        paidRs.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    paidAmountField.setText("0");
                }

                if (birthday != null && !birthday.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(birthday);
                    bDayChooser.setDate(date);
                } else {
                    bDayChooser.setDate(null);
                }

                if (evt.getClickCount() == 2) {
                    AddAddress addressGui = new AddAddress(memberId);
                    addressGui.setVisible(true);
                    addressGui.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                    this.setEnabled(false);

                    addressGui.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                            MemberRegistration.this.setEnabled(true);
                            refreshMemberDistrict(memberId); // refresh after closing
                        }

                        @Override
                        public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                            MemberRegistration.this.setEnabled(true);
                            refreshMemberDistrict(memberId); // refresh after closing
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to load member data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jTable1MouseClicked

    private void viewFullMembersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewFullMembersButtonActionPerformed
        ViewAllMembers viewAllMembers = new ViewAllMembers();
        viewAllMembers.setVisible(true);

        viewAllMembers.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        this.setEnabled(false);

        viewAllMembers.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                MemberRegistration.this.setEnabled(true);
            }

            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                MemberRegistration.this.setEnabled(true);
            }
        });
    }//GEN-LAST:event_viewFullMembersButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.toedter.calendar.JDateChooser bDayChooser;
    private javax.swing.JTextField campusIdField;
    private javax.swing.JButton clearButton;
    private javax.swing.JTextField districtField;
    private javax.swing.JTextField emailField;
    private javax.swing.JTextField fullNameField;
    private javax.swing.JComboBox<String> genderComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField memberIdField;
    private javax.swing.JComboBox<String> memberTypeComboBox;
    private javax.swing.JTextField mobileField;
    private javax.swing.JTextField nicField;
    private javax.swing.JFormattedTextField paidAmountField;
    private javax.swing.JButton registerButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JTextArea tempCopyTextArea;
    private javax.swing.JButton updateButton;
    private javax.swing.JButton viewFullMembersButton;
    private javax.swing.JTextField withIniField;
    // End of variables declaration//GEN-END:variables

    private void reset() {
        searchField.setText("");
        fullNameField.setText("");
        withIniField.setText("");
        campusIdField.setText("");
        nicField.setText("");
        bDayChooser.setDate(null);
        emailField.setText("");
        mobileField.setText("");
        genderComboBox.setSelectedIndex(0);
        memberTypeComboBox.setSelectedIndex(0);
        districtField.setText("");
        registerButton.setEnabled(true);
        tempCopyTextArea.setText("");
        paidAmountField.setText("");
        loadMembers();
        generateMemberID();
    }
}
