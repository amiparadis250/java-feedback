import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
public class Feedback extends javax.swing.JFrame {
     private Connection connection;
    private DefaultTableModel tableModel;

    public Feedback() {
        initComponents();
        initDatabase();
        setupTableListener();
    }
     private void initDatabase() {
        try {
            connection = DatabaseConnection.connect();
            loadDataToTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
        }
    }
     private void loadDataToTable() {
        try {
            tableModel = (DefaultTableModel) Table.getModel();
            tableModel.setRowCount(0); // Clear existing rows

            String query = "SELECT firstname, email, gender, comment FROM feedbacks";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getString("firstname"),
                        rs.getString("email"),
                        rs.getString("gender"),
                        rs.getString("comment")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
     private void exportFeedbacks() {
    try {
        // Open file chooser to select export location
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Feedbacks to CSV");
        fileChooser.setSelectedFile(new File("feedbacks_export.csv"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            // Establish database connection
            Connection conn = DatabaseConnection.connect();
            
            // Prepare SQL statement to retrieve all feedbacks
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT firstname, email, gender, comment FROM feedbacks");
            
            // Execute query
            ResultSet rs = pstmt.executeQuery();
            
            // Create FileWriter for CSV export
            try (FileWriter csvWriter = new FileWriter(fileToSave)) {
                // Write CSV Header
                csvWriter.append("First Name,Email,Gender,Comment\n");
                
                // Iterate through result set and write to CSV
                while (rs.next()) {
                    csvWriter.append(
                        cleanField(rs.getString("firstname")) + "," +
                        cleanField(rs.getString("email")) + "," +
                        cleanField(rs.getString("gender")) + "," +
                        cleanField(rs.getString("comment")) + "\n"
                    );
                }
                
                // Show success message
                JOptionPane.showMessageDialog(this, 
                    "Feedbacks exported successfully to " + fileToSave.getAbsolutePath());
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Database error during export: " + e.getMessage(), 
            "Export Error", 
            JOptionPane.ERROR_MESSAGE);
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, 
            "File error during export: " + e.getMessage(), 
            "Export Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}

// Comprehensive field cleaning method
private String cleanField(String field) {
    if (field == null) return "";
    
    // Remove special characters and sanitize
    String cleaned = field
        .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "") // Remove control characters except line breaks
        .replace("\"", "'")  // Replace double quotes with single quotes
        .replace(',', ';')   // Replace commas with semicolons
        .trim();             // Remove leading/trailing whitespace
    
    // Limit field length if needed
    if (cleaned.length() > 500) {
        cleaned = cleaned.substring(0, 500);
    }
    
    // Wrap in quotes to handle potential commas or special characters
    return "\"" + cleaned + "\"";
}
 private void setupTableListener() {
    Table.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting() && Table.getSelectedRow() != -1) {
            int selectedRow = Table.getSelectedRow();
            
            // Load data into form fields
            String firstname = (String) Table.getValueAt(selectedRow, 0);
            String email = (String) Table.getValueAt(selectedRow, 1);
            String gender = (String) Table.getValueAt(selectedRow, 2);
            String comment = (String) Table.getValueAt(selectedRow, 3);

            // Fetch lastname separately from database
            try {
                String lastnameQuery = "SELECT lastname FROM feedbacks WHERE email = ?";
                PreparedStatement pstmt = connection.prepareStatement(lastnameQuery);
                pstmt.setString(1, email);
                ResultSet rs = pstmt.executeQuery();
                
                String lastname = "";
                if (rs.next()) {
                    lastname = rs.getString("lastname");
                }
                
                // Populate form fields
                jTextField3.setText(firstname);
                Last.setText(lastname);
                jTextField2.setText(email);
                jComboBox1.setSelectedItem(gender);
                jTextArea1.setText(comment);

                // Store the current email for potential updates
                String currentEditEmail = email;

                // Enable delete and view buttons
                Delete.setEnabled(true);
                view.setEnabled(true);

                // Check if email and lastname are not empty, then enable submit (Edit) button
                if (!email.isEmpty() && !lastname.isEmpty()) {
                    Edit.setEnabled(true);  // Enable submit button
                } else {
                    Edit.setEnabled(false); // Disable if required fields are empty
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error fetching lastname: " + ex.getMessage());
            }
        }
    });
}

  private void submitFeedback() {
        String lastname = Last.getText().trim();
        String firstname = jTextField3.getText().trim();
        String email = jTextField2.getText().trim();
        String gender = (String) jComboBox1.getSelectedItem();
        String comment = jTextArea1.getText().trim();

        // Validate input
        if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || comment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields");
            return;
        }

        try {
            String query = "INSERT INTO feedbacks (firstname, lastname, email, gender, comment) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, firstname);
                pstmt.setString(2, lastname);
                pstmt.setString(3, email);
                pstmt.setString(4, gender);
                pstmt.setString(5, comment);
                
                pstmt.executeUpdate();
                
                // Refresh table
                loadDataToTable();
                
                // Clear input fields
                clearInputFields();
                
                JOptionPane.showMessageDialog(this, "Feedback submitted successfully!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error submitting feedback: " + e.getMessage());
        }
    }

    private void clearInputFields() {
        Last.setText("");
        jTextField3.setText("");
        jTextField2.setText("");
        jComboBox1.setSelectedIndex(0);
        jTextArea1.setText("");
    }
    private void deleteFeedback() {
        int selectedRow = Table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete");
            return;
        }

        String email = (String) Table.getValueAt(selectedRow, 1);

        try {
            String query = "DELETE FROM feedbacks WHERE email = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, email);
                
                int result = pstmt.executeUpdate();
                
                if (result > 0) {
                    
                    loadDataToTable();
                    JOptionPane.showMessageDialog(this, "Feedback deleted successfully!");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting feedback: " + e.getMessage());
        }
    }
 private void viewFeedbackDetails() {
    int selectedRow = Table.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row to view");
        return;
    }

    // Create a dialog for details
    JDialog detailsDialog = new JDialog(this, "Feedback Details", true);
    detailsDialog.setLayout(new BorderLayout());

    JPanel contentPanel = new JPanel(new GridBagLayout());
    contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(2, 5, 2, 10); // Reduce spacing between labels and values
    gbc.anchor = GridBagConstraints.WEST; 
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;

    String[] labels = {"Firstname:", "Email:", "Gender:", "Feedback:"};
    
    for (int i = 0; i < labels.length; i++) {
        gbc.gridx = 0;
        gbc.gridy = i;
        JLabel label = new JLabel(labels[i]);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        contentPanel.add(label, gbc);

        gbc.gridx = 1;
        JLabel valueLabel = new JLabel("<html>" + Table.getValueAt(selectedRow, i).toString() + "</html>");
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        contentPanel.add(valueLabel, gbc);
    }

    detailsDialog.add(contentPanel, BorderLayout.CENTER);

    // Keep width 1000px, reduce height if needed
    detailsDialog.setSize(1000, 200);
    detailsDialog.setResizable(false);
    detailsDialog.setLocationRelativeTo(this);
    detailsDialog.setVisible(true);
}
 
private void updateRecord() {
    int selectedRow = Table.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row to edit");
        return;
    }

    // Retrieve original email for database lookup
    String originalEmail = (String) Table.getValueAt(selectedRow, 1);

    // Get updated values from form fields
    String updatedFirstname = jTextField3.getText().trim();
    String updatedEmail = jTextField2.getText().trim();
    String updatedGender = (String) jComboBox1.getSelectedItem();
    String updatedComment = jTextArea1.getText().trim();

    try {
        Connection conn = DatabaseConnection.connect();
        PreparedStatement pstmt = conn.prepareStatement(
            "UPDATE feedbacks SET firstname = ?, email = ?, gender = ?, comment = ? WHERE email = ?");
        
        pstmt.setString(1, updatedFirstname);
        pstmt.setString(2, updatedEmail);
        pstmt.setString(3, updatedGender);
        pstmt.setString(4, updatedComment);
        pstmt.setString(5, originalEmail);

        int updatedRows = pstmt.executeUpdate();
        if (updatedRows > 0) {
            JOptionPane.showMessageDialog(this, "Record updated successfully");
            // Refresh table data or update specific row
            Table.setValueAt(updatedFirstname, selectedRow, 0);
            Table.setValueAt(updatedEmail, selectedRow, 1);
            Table.setValueAt(updatedGender, selectedRow, 2);
            Table.setValueAt(updatedComment, selectedRow, 3);
        } else {
            JOptionPane.showMessageDialog(this, "No record found to update");
        }

        // Removed automatic connection closing
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage());
    }
}

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Feedback = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        Last = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        ResultPage = new javax.swing.JButton();
        Edit = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        patching = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        view = new javax.swing.JButton();
        Delete = new javax.swing.JButton();
        Export = new javax.swing.JButton();
        jTextField3 = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        Table = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Feedback.setBackground(new java.awt.Color(204, 204, 204));
        Feedback.setPreferredSize(new java.awt.Dimension(1600, 1000));
        Feedback.setLayout(null);

        jPanel1.setBackground(new java.awt.Color(0, 102, 102));

        jLabel1.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("We would like to hear from you? provide your feedback ");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Feedback");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Lastname");

        Last.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LastActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female", "Lesbian", "Gay", "other" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Email");

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Firstname");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Gender");

        ResultPage.setBackground(new java.awt.Color(51, 0, 255));
        ResultPage.setForeground(new java.awt.Color(255, 255, 255));
        ResultPage.setText("Result page");
        ResultPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ResultPageActionPerformed(evt);
            }
        });

        Edit.setBackground(new java.awt.Color(51, 0, 255));
        Edit.setForeground(new java.awt.Color(255, 255, 255));
        Edit.setText("Submit");
        Edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditActionPerformed(evt);
            }
        });

        jPanel3.setBackground(new java.awt.Color(0, 102, 102));
        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 51, 255), 1, true));

        patching.setText("Update");
        patching.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                patchingActionPerformed(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(0, 102, 102));
        jPanel4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 51, 255), 1, true));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Controls");

        view.setBackground(new java.awt.Color(0, 255, 102));
        view.setForeground(new java.awt.Color(255, 255, 255));
        view.setText("View");
        view.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewActionPerformed(evt);
            }
        });

        Delete.setBackground(new java.awt.Color(255, 51, 51));
        Delete.setForeground(new java.awt.Color(255, 255, 255));
        Delete.setText("Delete");
        Delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteActionPerformed(evt);
            }
        });

        Export.setText("Export");
        Export.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(view)
                        .addGap(30, 30, 30)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Export, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Delete, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(62, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(view)
                    .addComponent(Delete))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Export)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(patching)
                .addContainerGap(186, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(73, Short.MAX_VALUE)
                .addComponent(patching)
                .addGap(22, 22, 22))
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 477, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Last, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Edit, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ResultPage, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                            .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(56, 56, 56)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(48, 48, 48)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Last, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(Edit, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(ResultPage, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(35, Short.MAX_VALUE))
        );

        Feedback.add(jPanel1);
        jPanel1.setBounds(10, 0, 600, 500);

        Table.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0))));
        Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Firstname", "Email", "Gender", "Feedback"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        Table.setColumnSelectionAllowed(true);
        jScrollPane2.setViewportView(Table);

        Feedback.add(jScrollPane2);
        jScrollPane2.setBounds(620, 0, 870, 500);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Feedback, javax.swing.GroupLayout.DEFAULT_SIZE, 1105, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(Feedback, javax.swing.GroupLayout.PREFERRED_SIZE, 507, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(41, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void LastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LastActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_LastActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void ResultPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ResultPageActionPerformed
      String firstName = jTextField3.getText();
    String lastName = Last.getText();
    String emailText = jTextField2.getText();
    String genderText = (String) jComboBox1.getSelectedItem();
    String commentText = jTextArea1.getText();

   
    if (firstName.isEmpty() || lastName.isEmpty() || emailText.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill in all required fields before proceeding.");
        return;
    }
    Result Result = new Result(firstName, lastName, emailText, genderText, commentText);
    System.out.print(Result);
    Result.setVisible(true); 
    Result.pack();
    Result.setLocationRelativeTo(null);
    }//GEN-LAST:event_ResultPageActionPerformed

    private void EditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditActionPerformed
       submitFeedback();        // TODO add your handling code here:
    }//GEN-LAST:event_EditActionPerformed

    private void viewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewActionPerformed
       viewFeedbackDetails();        // TODO add your handling code here:
    }//GEN-LAST:event_viewActionPerformed

    private void DeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteActionPerformed
       deleteFeedback();                 // TODO add your handling code here:
    }//GEN-LAST:event_DeleteActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void patchingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_patchingActionPerformed
      updateRecord();              // TODO add your handling code here:
    }//GEN-LAST:event_patchingActionPerformed

    private void ExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExportActionPerformed
   exportFeedbacks();        // TODO add your handling code here:
    }//GEN-LAST:event_ExportActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Feedback.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Feedback.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Feedback.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Feedback.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Feedback().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Delete;
    private javax.swing.JButton Edit;
    private javax.swing.JButton Export;
    private javax.swing.JPanel Feedback;
    private javax.swing.JTextField Last;
    private javax.swing.JButton ResultPage;
    private javax.swing.JTable Table;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JButton patching;
    private javax.swing.JButton view;
    // End of variables declaration//GEN-END:variables
}
