package gui;

import db.DatabaseConnector;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Fox C
 */
public class ViewAllMembers extends javax.swing.JFrame {

    // Table model
    private DefaultTableModel tableModel;

    /**
     * Simple listener to handle text field changes for real-time filtering
     */
    private static abstract class SimpleDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update();
        }

        public abstract void update();
    }

    /**
     * Constructor: initializes UI, listeners, and loads data
     */
    public ViewAllMembers() {
        initComponents();

        // IMPORTANT: Use your JTable name from GUI builder here!
        tableModel = (DefaultTableModel) memberTable.getModel();
        // Let columns keep preferred widths & allow scroll
        memberTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // Set preferred widths (adjust as needed)
        memberTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Full Name
        memberTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Name with Initials

        // Load combobox data
        loadGenders();
        loadMemberTypes();
        loadMemberStatuses();
        loadProvinces();
        loadDistricts();
        loadPollingDivisions();
        loadDivisionalSecretariats();
        loadPaymentYears();
        loadPaymentStatuses();
        loadDeceasedStatus();

        //updateTotalResultsLabel();

        // ComboBox listeners for filtering
        genderComboBox.addActionListener(e -> refreshMembers());
        memberTypeComboBox.addActionListener(e -> refreshMembers());
        memberStatusComboBox.addActionListener(e -> refreshMembers());
        provinceComboBox.addActionListener(e -> {
            loadDistrictsByProvince((String) provinceComboBox.getSelectedItem());
            filterPollingAndDivisional();
            refreshMembers();
        });
        districtComboBox.addActionListener(e -> {
            filterPollingAndDivisional();
            refreshMembers();
        });
        pollingDivComboBox.addActionListener(e -> refreshMembers());
        divisionalSecComboBox.addActionListener(e -> refreshMembers());
        paymentYearComboBox.addActionListener(e -> refreshMembers());
        paymentStatusComboBox.addActionListener(e -> refreshMembers());
        deceasedStatusComboBox.addActionListener(e -> refreshMembers());

        regFromChooser.getDateEditor().addPropertyChangeListener(evt -> refreshMembers());
        regToChooser.getDateEditor().addPropertyChangeListener(evt -> refreshMembers());

        decFromChooser.getDateEditor().addPropertyChangeListener(evt -> refreshMembers());
        decToChooser.getDateEditor().addPropertyChangeListener(evt -> refreshMembers());

        paidFromChooser.getDateEditor().addPropertyChangeListener(evt -> refreshMembers());
        paidToChooser.getDateEditor().addPropertyChangeListener(evt -> refreshMembers());

        dueFromChooser.getDateEditor().addPropertyChangeListener(evt -> refreshMembers());
        dueToChooser.getDateEditor().addPropertyChangeListener(evt -> refreshMembers());

        // Text fields listeners for search
        nicField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            public void update() {
                refreshMembers();
            }
        });
        fullNameField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            public void update() {
                refreshMembers();
            }
        });
        mobileField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            public void update() {
                refreshMembers();
            }
        });
        emailField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            public void update() {
                refreshMembers();
            }
        });
        paidAmountField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            public void update() {
                refreshMembers();
            }
        });
        memberIDField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            public void update() {
                refreshMembers();
            }
        });
        campusIDField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            public void update() {
                refreshMembers();
            }
        });

        // Load all members initially
        refreshMembers();
    }

    private void updateTotalResultsLabel() {
        int rowCount = memberTable.getRowCount();
        totalResultsLabel.setText("Total: " + rowCount);
    }

    //Reload members based on current filters
    private void refreshMembers() {
        Map<String, Object> filters = collectFiltersFromGUI();
        loadAllMembers(filters);
    }

    //Collect filter values from GUI
    private Map<String, Object> collectFiltersFromGUI() {
        Map<String, Object> filters = new HashMap<>();
        if (!nicField.getText().trim().isEmpty()) {
            filters.put("nic", nicField.getText().trim());
        }
        if (!fullNameField.getText().trim().isEmpty()) {
            filters.put("full_name", fullNameField.getText().trim());
        }
        if (!mobileField.getText().trim().isEmpty()) {
            filters.put("mobile", mobileField.getText().trim());
        }
        if (!emailField.getText().trim().isEmpty()) {
            filters.put("email", emailField.getText().trim());
        }
        if (!memberIDField.getText().trim().isEmpty()) {
            filters.put("member_id", memberIDField.getText().trim());
        }
        if (!campusIDField.getText().trim().isEmpty()) {
            filters.put("campus_id", campusIDField.getText().trim());
        }

        if (!"All".equals(genderComboBox.getSelectedItem())) {
            filters.put("gender", genderComboBox.getSelectedItem());
        }
        if (!"All".equals(provinceComboBox.getSelectedItem())) {
            filters.put("province", provinceComboBox.getSelectedItem());
        }
        if (!"All".equals(districtComboBox.getSelectedItem())) {
            filters.put("district", districtComboBox.getSelectedItem());
        }
        if (!"All".equals(pollingDivComboBox.getSelectedItem())) {
            filters.put("polling_div", pollingDivComboBox.getSelectedItem());
        }
        if (!"All".equals(divisionalSecComboBox.getSelectedItem())) {
            filters.put("divisional_sec", divisionalSecComboBox.getSelectedItem());
        }
        if (!"All".equals(memberTypeComboBox.getSelectedItem())) {
            filters.put("member_type", memberTypeComboBox.getSelectedItem());
        }
        if (!"All".equals(memberStatusComboBox.getSelectedItem())) {
            filters.put("status", memberStatusComboBox.getSelectedItem());
        }
        if (!"All".equals(paymentYearComboBox.getSelectedItem())) {
            filters.put("payment_year", paymentYearComboBox.getSelectedItem());
        }
        if (!"All".equals(paymentStatusComboBox.getSelectedItem())) {
            filters.put("payment_status", paymentStatusComboBox.getSelectedItem());
        }
        if (!"All".equals(deceasedStatusComboBox.getSelectedItem())) {
            filters.put("deceased_status", deceasedStatusComboBox.getSelectedItem());
        }

        if (!paidAmountField.getText().trim().isEmpty()) {
            try {
                filters.put("paid_amount", new BigDecimal(paidAmountField.getText().trim()));
            } catch (NumberFormatException ex) {
                /* ignore */ }
        }

        if (regFromChooser.getDate() != null) {
            filters.put("reg_date_from", new java.sql.Date(regFromChooser.getDate().getTime()));
        }
        if (regToChooser.getDate() != null) {
            filters.put("reg_date_to", new java.sql.Date(regToChooser.getDate().getTime()));
        }
        if (decFromChooser.getDate() != null) {
            filters.put("dec_date_from", new java.sql.Date(decFromChooser.getDate().getTime()));
        }
        if (decToChooser.getDate() != null) {
            filters.put("dec_date_to", new java.sql.Date(decToChooser.getDate().getTime()));
        }
        if (paidFromChooser.getDate() != null) {
            filters.put("paid_date_from", new java.sql.Date(paidFromChooser.getDate().getTime()));
        }
        if (paidToChooser.getDate() != null) {
            filters.put("paid_date_to", new java.sql.Date(paidToChooser.getDate().getTime()));
        }
        if (dueFromChooser.getDate() != null) {
            filters.put("due_date_from", new java.sql.Date(dueFromChooser.getDate().getTime()));
        }
        if (dueToChooser.getDate() != null) {
            filters.put("due_date_to", new java.sql.Date(dueToChooser.getDate().getTime()));
        }

        return filters;
    }

    //Load and Members and fill JTable with filtered members     
    //Load and fill JTable with filtered members
    public void loadAllMembers(Map<String, Object> filters) {
        List<Object> params = new ArrayList<>();

        // detect if user added any payment-related filters
        boolean hasPaymentFilter
                = filters.containsKey("payment_year")
                || filters.containsKey("payment_status")
                || filters.containsKey("paid_amount")
                || filters.containsKey("paid_date_from")
                || filters.containsKey("paid_date_to")
                || filters.containsKey("due_date_from")
                || filters.containsKey("due_date_to");

        StringBuilder sql = new StringBuilder(
                "SELECT m.id, m.full_name, m.name_with_initials, m.nic, m.campus_id, m.mobile, m.email, m.birthday, "
                + "mt.name as member_type, s.name as status, g.name as gender, "
                + "a.line1, a.line2, ds.name as divisional_sec, pd.name as polling_div, d.name as district, p.name as province, "
                + "m.reg_date, m.deceased_date, py.year, ps.name as payment_status, pay.amount, pay.due_date, pay.paid_date "
                + "FROM member m "
                + "LEFT JOIN address a ON m.address_id = a.id "
                + "LEFT JOIN divisional_secretariat ds ON a.divisional_secretariat_id = ds.id "
                + "LEFT JOIN polling_division pd ON a.polling_division_id = pd.id "
                + "LEFT JOIN district d ON ds.district_id = d.id "
                + "LEFT JOIN province p ON d.province_id = p.id "
                + "LEFT JOIN member_type mt ON m.member_type_id = mt.id "
                + "LEFT JOIN status s ON m.status_id = s.id "
                + "LEFT JOIN gender g ON m.gender_id = g.id "
        );

        if (hasPaymentFilter) {
            // join ALL payments if user added payment filters
            sql.append(
                    "LEFT JOIN payments pay ON pay.member_id = m.id "
                    + "LEFT JOIN payment_cycle py ON pay.payment_cycle_id = py.id "
                    + "LEFT JOIN payment_status ps ON py.payment_status_id = ps.id "
            );
        } else {
            // default: join ONLY latest payment (paid or unpaid)
            sql.append(
                    "LEFT JOIN ( "
                    + "  SELECT p1.* FROM payments p1 "
                    + "  INNER JOIN ( "
                    + "    SELECT member_id, COALESCE( "
                    + "      MAX(CASE WHEN pc.payment_status_id = (SELECT id FROM payment_status WHERE name='Unpaid') THEN payment_cycle_id END), "
                    + "      MAX(CASE WHEN pc.payment_status_id = (SELECT id FROM payment_status WHERE name='Paid') THEN payment_cycle_id END) "
                    + "    ) AS latest_cycle_id "
                    + "    FROM payments "
                    + "    INNER JOIN payment_cycle pc ON payments.payment_cycle_id = pc.id "
                    + "    GROUP BY member_id "
                    + "  ) latest ON p1.member_id = latest.member_id AND p1.payment_cycle_id = latest.latest_cycle_id "
                    + ") pay ON pay.member_id = m.id "
                    + "LEFT JOIN payment_cycle py ON pay.payment_cycle_id = py.id "
                    + "LEFT JOIN payment_status ps ON py.payment_status_id = ps.id "
            );
        }

        sql.append("WHERE 1=1 ");

        // add dynamic filters (same as before)
        if (filters != null) {
            if (filters.containsKey("nic")) {
                sql.append(" AND m.nic LIKE ? ");
                params.add("%" + filters.get("nic") + "%");
            }
            if (filters.containsKey("full_name")) {
                sql.append(" AND m.full_name LIKE ? ");
                params.add("%" + filters.get("full_name") + "%");
            }
            if (filters.containsKey("mobile")) {
                sql.append(" AND m.mobile LIKE ? ");
                params.add("%" + filters.get("mobile") + "%");
            }
            if (filters.containsKey("email")) {
                sql.append(" AND m.email LIKE ? ");
                params.add("%" + filters.get("email") + "%");
            }
            if (filters.containsKey("member_id")) {
                sql.append(" AND m.id = ? ");
                params.add(filters.get("member_id"));
            }
            if (filters.containsKey("campus_id")) {
                sql.append(" AND m.campus_id LIKE ? ");
                params.add("%" + filters.get("campus_id") + "%");
            }
            if (filters.containsKey("gender")) {
                sql.append(" AND g.name = ? ");
                params.add(filters.get("gender"));
            }
            if (filters.containsKey("province")) {
                sql.append(" AND p.name = ? ");
                params.add(filters.get("province"));
            }
            if (filters.containsKey("district")) {
                sql.append(" AND d.name = ? ");
                params.add(filters.get("district"));
            }
            if (filters.containsKey("polling_div")) {
                sql.append(" AND pd.name = ? ");
                params.add(filters.get("polling_div"));
            }
            if (filters.containsKey("divisional_sec")) {
                sql.append(" AND ds.name = ? ");
                params.add(filters.get("divisional_sec"));
            }
            if (filters.containsKey("member_type")) {
                sql.append(" AND mt.name = ? ");
                params.add(filters.get("member_type"));
            }
            if (filters.containsKey("status")) {
                sql.append(" AND s.name = ? ");
                params.add(filters.get("status"));
            }
            if (filters.containsKey("payment_year")) {
                sql.append(" AND py.year = ? ");
                params.add(filters.get("payment_year"));
            }
            if (filters.containsKey("payment_status")) {
                sql.append(" AND ps.name = ? ");
                params.add(filters.get("payment_status"));
            }
            if (filters.containsKey("deceased_status")) {
                if ("Alive only".equals(filters.get("deceased_status"))) {
                    sql.append(" AND m.deceased_date IS NULL ");
                } else if ("Deceased only".equals(filters.get("deceased_status"))) {
                    sql.append(" AND m.deceased_date IS NOT NULL ");
                }
            }
            if (filters.containsKey("paid_amount")) {
                sql.append(" AND pay.amount = ? ");
                params.add(filters.get("paid_amount"));
            }
            if (filters.containsKey("reg_date_from")) {
                sql.append(" AND m.reg_date >= ? ");
                params.add(filters.get("reg_date_from"));
            }
            if (filters.containsKey("reg_date_to")) {
                sql.append(" AND m.reg_date <= ? ");
                params.add(filters.get("reg_date_to"));
            }
            if (filters.containsKey("dec_date_from")) {
                sql.append(" AND m.deceased_date >= ? ");
                params.add(filters.get("dec_date_from"));
            }
            if (filters.containsKey("dec_date_to")) {
                sql.append(" AND m.deceased_date <= ? ");
                params.add(filters.get("dec_date_to"));
            }
            if (filters.containsKey("paid_date_from")) {
                sql.append(" AND pay.paid_date >= ? ");
                params.add(filters.get("paid_date_from"));
            }
            if (filters.containsKey("paid_date_to")) {
                sql.append(" AND pay.paid_date <= ? ");
                params.add(filters.get("paid_date_to"));
            }
            if (filters.containsKey("due_date_from")) {
                sql.append(" AND pay.due_date >= ? ");
                params.add(filters.get("due_date_from"));
            }
            if (filters.containsKey("due_date_to")) {
                sql.append(" AND pay.due_date <= ? ");
                params.add(filters.get("due_date_to"));
            }
        }

        try (ResultSet rs = DatabaseConnector.executeQueryWithParams(sql.toString(), params.toArray())) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getLong("id"),
                    rs.getString("full_name"),
                    rs.getString("name_with_initials"),
                    rs.getString("nic"),
                    rs.getString("campus_id"),
                    rs.getString("mobile"),
                    rs.getString("email"),
                    rs.getDate("birthday"),
                    rs.getString("line1"),
                    rs.getString("line2"),
                    rs.getString("province"),
                    rs.getString("district"),
                    rs.getString("polling_div"),
                    rs.getString("divisional_sec"),
                    rs.getString("year"),
                    rs.getDate("due_date"),
                    rs.getBigDecimal("amount"),
                    rs.getDate("paid_date"),
                    rs.getString("payment_status"),
                    rs.getString("gender"),
                    rs.getDate("reg_date"),
                    rs.getString("member_type"),
                    rs.getDate("deceased_date"),
                    rs.getString("status")
                });
            }
              updateTotalResultsLabel();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load members: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

// ---------- Loaders ----------
    //Load Genders
    private void loadGenders() {
        try {
            genderComboBox.removeAllItems();
            genderComboBox.addItem("All");
            String sql = "SELECT name FROM gender ORDER BY name";
            try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql)) {
                while (rs.next()) {
                    genderComboBox.addItem(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load genders: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Load Member Types
    private void loadMemberTypes() {
        try {
            memberTypeComboBox.removeAllItems();
            memberTypeComboBox.addItem("All");
            String sql = "SELECT name FROM member_type ORDER BY name";
            try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql)) {
                while (rs.next()) {
                    memberTypeComboBox.addItem(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load member types: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Load Member Statuses
    private void loadMemberStatuses() {
        try {
            memberStatusComboBox.removeAllItems();
            memberStatusComboBox.addItem("All");
            String sql = "SELECT name FROM status ORDER BY name";
            try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql)) {
                while (rs.next()) {
                    memberStatusComboBox.addItem(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load member statuses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Load Districts
    private void loadDistricts() {
        try {
            districtComboBox.removeAllItems();
            districtComboBox.addItem("All");
            String sql = "SELECT name FROM district ORDER BY name";
            try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql)) {
                while (rs.next()) {
                    districtComboBox.addItem(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load districts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDistrictsByProvince(String provinceName) {
        try {
            districtComboBox.removeAllItems();
            districtComboBox.addItem("All");
            String sql = "SELECT d.name FROM district d INNER JOIN province p ON d.province_id = p.id WHERE p.name = ? ORDER BY d.name";
            try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql, provinceName)) {
                while (rs.next()) {
                    districtComboBox.addItem(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProvinces() {
        try {
            provinceComboBox.removeAllItems();
            provinceComboBox.addItem("All");
            String sql = "SELECT name FROM province ORDER BY name";
            try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql)) {
                while (rs.next()) {
                    provinceComboBox.addItem(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load provinces: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPollingDivisions() {
        try {
            pollingDivComboBox.removeAllItems();
            pollingDivComboBox.addItem("All");
            String sql = "SELECT name FROM polling_division ORDER BY name";
            try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql)) {
                while (rs.next()) {
                    pollingDivComboBox.addItem(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load polling divisions: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDivisionalSecretariats() {
        try {
            divisionalSecComboBox.removeAllItems();
            divisionalSecComboBox.addItem("All");
            String sql = "SELECT name FROM divisional_secretariat ORDER BY name";
            try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql)) {
                while (rs.next()) {
                    divisionalSecComboBox.addItem(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load divisional secretariats: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPaymentYears() {
        try {
            paymentYearComboBox.removeAllItems();
            paymentYearComboBox.addItem("All");
            String sql = "SELECT DISTINCT year FROM payment_cycle ORDER BY year";
            try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql)) {
                while (rs.next()) {
                    paymentYearComboBox.addItem(rs.getString("year"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load payment years: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPaymentStatuses() {
        try {
            paymentStatusComboBox.removeAllItems();
            paymentStatusComboBox.addItem("All");
            String sql = "SELECT name FROM payment_status ORDER BY name";
            try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql)) {
                while (rs.next()) {
                    paymentStatusComboBox.addItem(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load payment statuses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDeceasedStatus() {
        deceasedStatusComboBox.removeAllItems();
        deceasedStatusComboBox.addItem("All");
        deceasedStatusComboBox.addItem("Alive only");
        deceasedStatusComboBox.addItem("Deceased only");
    }

    private void filterPollingAndDivisional() {
        String selectedDistrict = (String) districtComboBox.getSelectedItem();
        if (selectedDistrict != null && !"All".equals(selectedDistrict)) {
            loadPollingDivisionsByDistrict(selectedDistrict);
            loadDivisionalSecretariatsByDistrict(selectedDistrict);
        } else {
            loadPollingDivisions();
            loadDivisionalSecretariats();
        }
    }

    private void loadPollingDivisionsByDistrict(String districtName) {
        try {
            pollingDivComboBox.removeAllItems();
            pollingDivComboBox.addItem("All");
            String sql = "SELECT pd.name "
                    + "FROM polling_division pd "
                    + "INNER JOIN district d ON pd.district_id = d.id "
                    + "WHERE d.name = ? ORDER BY pd.name";
            try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql, districtName)) {
                while (rs.next()) {
                    pollingDivComboBox.addItem(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load polling divisions by district: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDivisionalSecretariatsByDistrict(String districtName) {
        try {
            divisionalSecComboBox.removeAllItems();
            divisionalSecComboBox.addItem("All");
            String sql = "SELECT ds.name "
                    + "FROM divisional_secretariat ds "
                    + "INNER JOIN district d ON ds.district_id = d.id "
                    + "WHERE d.name = ? ORDER BY ds.name";
            try (ResultSet rs = db.DatabaseConnector.executeQueryWithParams(sql, districtName)) {
                while (rs.next()) {
                    divisionalSecComboBox.addItem(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load divisional secretariats by district: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        jScrollPane1 = new javax.swing.JScrollPane();
        memberTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        fullNameField = new javax.swing.JTextField();
        nicField = new javax.swing.JTextField();
        memberIDField = new javax.swing.JTextField();
        emailField = new javax.swing.JTextField();
        mobileField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        campusIDField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        memberTypeComboBox = new javax.swing.JComboBox<>();
        genderComboBox = new javax.swing.JComboBox<>();
        memberStatusComboBox = new javax.swing.JComboBox<>();
        pollingDivComboBox = new javax.swing.JComboBox<>();
        districtComboBox = new javax.swing.JComboBox<>();
        paymentStatusComboBox = new javax.swing.JComboBox<>();
        paymentYearComboBox = new javax.swing.JComboBox<>();
        deceasedStatusComboBox = new javax.swing.JComboBox<>();
        clearFieldsButton = new javax.swing.JButton();
        clearComboBoxesButton = new javax.swing.JButton();
        divisionalSecComboBox = new javax.swing.JComboBox<>();
        provinceComboBox = new javax.swing.JComboBox<>();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        regFromChooser = new com.toedter.calendar.JDateChooser();
        regToChooser = new com.toedter.calendar.JDateChooser();
        decToChooser = new com.toedter.calendar.JDateChooser();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        decFromChooser = new com.toedter.calendar.JDateChooser();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        paidFromChooser = new com.toedter.calendar.JDateChooser();
        paidToChooser = new com.toedter.calendar.JDateChooser();
        jLabel24 = new javax.swing.JLabel();
        dueFromChooser = new com.toedter.calendar.JDateChooser();
        jLabel25 = new javax.swing.JLabel();
        dueToChooser = new com.toedter.calendar.JDateChooser();
        clearDateChoosersButton = new javax.swing.JButton();
        paidAmountField = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        totalResultsLabel = new javax.swing.JLabel();
        clearAllButton = new javax.swing.JButton();
        jLabel28 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tempCopyTextArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Alumini System  View Members");

        jLabel1.setBackground(new java.awt.Color(102, 102, 102));
        jLabel1.setFont(new java.awt.Font("Palatino Linotype", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("View Members");

        memberTable.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        memberTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Full Name", "Name with INI", "NIC", "Campus ID", "Mobile", "Email", "Birthday", "Line1 Adr", "Line2 Adr", "Province", "District", "Polling Div", "Divisional Sec", "Pay Cycle Year", "Due Date", "Amount", "Paid Date", "Pay Status", "Gender", "Reg Date", "Type", "Deceased Date", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        memberTable.getTableHeader().setReorderingAllowed(false);
        memberTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                memberTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(memberTable);

        jLabel2.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("NIC");

        jLabel3.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Name");

        jLabel4.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Member ID");

        jLabel5.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Email");

        jLabel6.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Mobile");

        fullNameField.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N

        nicField.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N

        memberIDField.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N

        emailField.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N

        mobileField.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Campus ID");

        campusIDField.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Gender");

        jLabel9.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Member Type");

        jLabel10.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Status");

        jLabel11.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Province");

        jLabel12.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("District");

        jLabel13.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("Polling Div");

        jLabel14.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setText("Divisional Sec");

        jLabel15.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("Payment Year");

        jLabel16.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("Deceased Status");

        jLabel17.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("Payment Status");

        memberTypeComboBox.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        memberTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        genderComboBox.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        genderComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        memberStatusComboBox.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        memberStatusComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        pollingDivComboBox.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        pollingDivComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        districtComboBox.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        districtComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        paymentStatusComboBox.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        paymentStatusComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        paymentYearComboBox.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        paymentYearComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        deceasedStatusComboBox.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        deceasedStatusComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        clearFieldsButton.setBackground(new java.awt.Color(204, 0, 0));
        clearFieldsButton.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        clearFieldsButton.setForeground(new java.awt.Color(255, 255, 255));
        clearFieldsButton.setText("Clear Fields");
        clearFieldsButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        clearFieldsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFieldsButtonActionPerformed(evt);
            }
        });

        clearComboBoxesButton.setBackground(new java.awt.Color(204, 0, 0));
        clearComboBoxesButton.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        clearComboBoxesButton.setForeground(new java.awt.Color(255, 255, 255));
        clearComboBoxesButton.setText("Clear Combo Boxes");
        clearComboBoxesButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        clearComboBoxesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearComboBoxesButtonActionPerformed(evt);
            }
        });

        divisionalSecComboBox.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        divisionalSecComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        provinceComboBox.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        provinceComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel18.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setText("Reg Date From");

        jLabel19.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setText("Reg Date To");

        regFromChooser.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        regFromChooser.setPreferredSize(new java.awt.Dimension(88, 28));

        regToChooser.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        regToChooser.setPreferredSize(new java.awt.Dimension(88, 28));

        decToChooser.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        decToChooser.setPreferredSize(new java.awt.Dimension(88, 28));

        jLabel20.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setText("Deceased Date To");

        jLabel21.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setText("Deceased Date From");

        decFromChooser.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        decFromChooser.setPreferredSize(new java.awt.Dimension(88, 28));

        jLabel22.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setText("Paid Date To");

        jLabel23.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setText("Paid Date From");

        paidFromChooser.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        paidFromChooser.setPreferredSize(new java.awt.Dimension(88, 28));

        paidToChooser.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        paidToChooser.setPreferredSize(new java.awt.Dimension(88, 28));

        jLabel24.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("Due Date From");

        dueFromChooser.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        dueFromChooser.setPreferredSize(new java.awt.Dimension(88, 28));

        jLabel25.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setText("Due Date To");

        dueToChooser.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        dueToChooser.setPreferredSize(new java.awt.Dimension(88, 28));

        clearDateChoosersButton.setBackground(new java.awt.Color(204, 0, 0));
        clearDateChoosersButton.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        clearDateChoosersButton.setForeground(new java.awt.Color(255, 255, 255));
        clearDateChoosersButton.setText("Clear Dates");
        clearDateChoosersButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        clearDateChoosersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearDateChoosersButtonActionPerformed(evt);
            }
        });

        paidAmountField.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N

        jLabel26.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setText("Paid Amount");

        jLabel27.setFont(new java.awt.Font("Monospaced", 1, 18)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 204, 0));
        jLabel27.setText("Total Results :");

        totalResultsLabel.setFont(new java.awt.Font("Monospaced", 1, 24)); // NOI18N
        totalResultsLabel.setForeground(new java.awt.Color(255, 204, 0));
        totalResultsLabel.setText("Total Results");

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel5)
                                            .addComponent(jLabel3))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(fullNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel4)
                                            .addComponent(jLabel7))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(memberIDField, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(campusIDField, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel9)
                                                    .addComponent(jLabel8))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(memberTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(genderComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel10)
                                                .addGap(61, 61, 61)
                                                .addComponent(memberStatusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(23, 23, 23)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel11)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(provinceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel12)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(districtComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(18, 18, 18)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(jLabel14)
                                                            .addComponent(jLabel13))
                                                        .addGap(18, 18, 18)
                                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(pollingDivComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(divisionalSecComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGap(31, 31, 31)
                                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                            .addComponent(jLabel17)
                                                            .addComponent(jLabel15)))
                                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                            .addComponent(jLabel2)
                                                            .addGap(51, 51, 51)
                                                            .addComponent(nicField, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                            .addComponent(jLabel6)
                                                            .addGap(18, 18, 18)
                                                            .addComponent(mobileField, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                            .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(paymentStatusComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(deceasedStatusComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(paymentYearComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel28)
                                                        .addGap(0, 0, Short.MAX_VALUE))
                                                    .addComponent(jScrollPane2))
                                                .addGap(18, 18, 18)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addGap(151, 151, 151)
                                                        .addComponent(paidAmountField, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addComponent(jLabel26))
                                                .addGap(69, 69, 69))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel19)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(regToChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel18)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(regFromChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(54, 54, 54)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel20)
                                                        .addGap(28, 28, 28)
                                                        .addComponent(decToChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel21)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(decFromChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(59, 59, 59)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel23)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(paidFromChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel22)
                                                        .addGap(28, 28, 28)
                                                        .addComponent(paidToChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)))
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel25)
                                                .addGap(28, 28, 28)
                                                .addComponent(dueToChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel27)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(totalResultsLabel))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel24)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(dueFromChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addGap(18, 18, 18)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(clearComboBoxesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(clearFieldsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(clearDateChoosersButton, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                            .addComponent(clearAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE))
                        .addGap(9, 9, 9)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(fullNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(memberIDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(nicField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(clearFieldsButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(campusIDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(mobileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(31, 31, 31)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(paymentYearComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel15))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(paymentStatusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel17))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(deceasedStatusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel16)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(33, 33, 33)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel8)
                                            .addComponent(genderComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel9)
                                            .addComponent(memberTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(clearComboBoxesButton)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel13)
                                            .addComponent(pollingDivComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(provinceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel11))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel14)
                                                .addComponent(districtComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel12))
                                            .addComponent(divisionalSecComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addComponent(memberStatusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(34, 34, 34)
                        .addComponent(jLabel18))
                    .addComponent(regFromChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21)
                    .addComponent(decFromChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearDateChoosersButton)
                    .addComponent(jLabel24)
                    .addComponent(dueFromChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23)
                    .addComponent(paidFromChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel19)
                    .addComponent(regToChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(decToChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22)
                    .addComponent(paidToChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25)
                    .addComponent(dueToChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel26)
                            .addComponent(paidAmountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel27)
                            .addComponent(totalResultsLabel)
                            .addComponent(clearAllButton)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
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

    private void clearFieldsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFieldsButtonActionPerformed
        clearFields();
    }//GEN-LAST:event_clearFieldsButtonActionPerformed

    private void clearComboBoxesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearComboBoxesButtonActionPerformed
        clearComboBoxes();
    }//GEN-LAST:event_clearComboBoxesButtonActionPerformed

    private void clearDateChoosersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearDateChoosersButtonActionPerformed
        clearDateChoosers();
    }//GEN-LAST:event_clearDateChoosersButtonActionPerformed

    private void clearAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAllButtonActionPerformed
        clearAll();
    }//GEN-LAST:event_clearAllButtonActionPerformed

    private void memberTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_memberTableMouseClicked
        if (evt.getClickCount() == 2) {
            int row = memberTable.getSelectedRow();
            if (row != -1) {
                String memberId = memberTable.getValueAt(row, 0).toString();

                PaymentManagement paymentGui = new PaymentManagement(memberId);
                JDialog dialog = new JDialog(ViewAllMembers.this, "Payment Management", true);
                dialog.setContentPane(paymentGui);
                dialog.pack();
                dialog.setLocationRelativeTo(null);

                // Add listener to refresh after closing
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        refreshMembers(); // refresh with current filters
                    }

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        refreshMembers(); // also refresh if window is closed by X
                    }
                });

                dialog.setVisible(true);
            }
        }
    }//GEN-LAST:event_memberTableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField campusIDField;
    private javax.swing.JButton clearAllButton;
    private javax.swing.JButton clearComboBoxesButton;
    private javax.swing.JButton clearDateChoosersButton;
    private javax.swing.JButton clearFieldsButton;
    private com.toedter.calendar.JDateChooser decFromChooser;
    private com.toedter.calendar.JDateChooser decToChooser;
    private javax.swing.JComboBox<String> deceasedStatusComboBox;
    private javax.swing.JComboBox<String> districtComboBox;
    private javax.swing.JComboBox<String> divisionalSecComboBox;
    private com.toedter.calendar.JDateChooser dueFromChooser;
    private com.toedter.calendar.JDateChooser dueToChooser;
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
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
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
    private javax.swing.JTextField memberIDField;
    private javax.swing.JComboBox<String> memberStatusComboBox;
    private javax.swing.JTable memberTable;
    private javax.swing.JComboBox<String> memberTypeComboBox;
    private javax.swing.JTextField mobileField;
    private javax.swing.JTextField nicField;
    private javax.swing.JTextField paidAmountField;
    private com.toedter.calendar.JDateChooser paidFromChooser;
    private com.toedter.calendar.JDateChooser paidToChooser;
    private javax.swing.JComboBox<String> paymentStatusComboBox;
    private javax.swing.JComboBox<String> paymentYearComboBox;
    private javax.swing.JComboBox<String> pollingDivComboBox;
    private javax.swing.JComboBox<String> provinceComboBox;
    private com.toedter.calendar.JDateChooser regFromChooser;
    private com.toedter.calendar.JDateChooser regToChooser;
    private javax.swing.JTextArea tempCopyTextArea;
    private javax.swing.JLabel totalResultsLabel;
    // End of variables declaration//GEN-END:variables

    private void clearFields() {
        fullNameField.setText("");
        nicField.setText("");
        memberIDField.setText("");
        emailField.setText("");
        mobileField.setText("");
        campusIDField.setText("");
        paidAmountField.setText("");
    }

    private void clearComboBoxes() {
        genderComboBox.setSelectedIndex(0);
        memberTypeComboBox.setSelectedIndex(0);
        memberStatusComboBox.setSelectedIndex(0);
        districtComboBox.setSelectedIndex(0);
        provinceComboBox.setSelectedIndex(0);
        pollingDivComboBox.setSelectedIndex(0);
        divisionalSecComboBox.setSelectedIndex(0);
        paymentYearComboBox.setSelectedIndex(0);
        paymentStatusComboBox.setSelectedIndex(0);
        deceasedStatusComboBox.setSelectedIndex(0);
    }

    private void clearDateChoosers() {
        regFromChooser.setDate(null);
        regToChooser.setDate(null);
        decFromChooser.setDate(null);
        decToChooser.setDate(null);
        paidFromChooser.setDate(null);
        paidToChooser.setDate(null);
        dueFromChooser.setDate(null);
        dueToChooser.setDate(null);
    }

    private void clearAll() {
        clearFields();
        clearComboBoxes();
        clearDateChoosers();
        totalResultsLabel.setText("Total Results");
        tempCopyTextArea.setText("");
    }
}
