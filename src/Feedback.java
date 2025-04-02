import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
public class Feedback extends javax.swing.JFrame {
     private Connection connection;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    

    public Feedback() {
        initComponents();
        initDatabase();
        setupTableListener();
        Bduttons();
        initializeTableSorter();
        setupValidation();
    }
     private void initDatabase() {
        try {
            connection = DatabaseConnection.connect();
            loadDataToTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
        }
    }
     public void Bduttons(){
      ButtonGroup satisfactionGroup = new ButtonGroup();
    satisfactionGroup.add(verysatisfied);
    satisfactionGroup.add(satisfied);
    satisfactionGroup.add(neutral);
    satisfactionGroup.add(dissatisfied);
     }
    private void initializeTableSorter() {
    sorter = new TableRowSorter<>((DefaultTableModel) Table.getModel());
    Table.setRowSorter(sorter);
}
   private void setupValidation() {
        Edit.setEnabled(false);
        
        jTextField2.getDocument().addDocumentListener(new ValidationListener()); // Email
        jTextArea1.getDocument().addDocumentListener(new ValidationListener()); // Comment
        jTextField3.getDocument().addDocumentListener(new ValidationListener()); // First Name
        Last.getDocument().addDocumentListener(new ValidationListener()); // Last Name
    }

    private boolean isValidEmail(String email) {
        return Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email);
    }

    private boolean isValidComment(String comment) {
        return comment.length() >= 50 && comment.length() <= 100;
    }

    private boolean areFieldsFilled() {
        return !jTextField3.getText().trim().isEmpty() &&
               !Last.getText().trim().isEmpty() &&
               !jTextField2.getText().trim().isEmpty() &&
               !jTextArea1.getText().trim().isEmpty();
    }

    private void validateForm() {
        boolean valid = isValidEmail(jTextField2.getText().trim()) &&
                        isValidComment(jTextArea1.getText().trim()) &&
                        areFieldsFilled();
        Edit.setEnabled(valid);
    }

    private class ValidationListener implements javax.swing.event.DocumentListener {
        public void insertUpdate(javax.swing.event.DocumentEvent e) { validateForm(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { validateForm(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { validateForm(); }
    }

 private void submitFeedback() {
    ButtonGroup satisfactionGroup = new ButtonGroup();
    satisfactionGroup.add(verysatisfied);
    satisfactionGroup.add(satisfied);
    satisfactionGroup.add(neutral);
    satisfactionGroup.add(dissatisfied);
    
    String lastname = Last.getText().trim();
    String firstname = jTextField3.getText().trim();
    String email = jTextField2.getText().trim();
    String gender = (String) jComboBox1.getSelectedItem();
    String comment = jTextArea1.getText().trim();
    
    // Get selected satisfaction level from radio buttons
    String satisfaction = null;
    if (verysatisfied.isSelected()) {
        satisfaction = "Very Satisfied";
    } else if (satisfied.isSelected()) {
        satisfaction = "Satisfied";
    } else if (neutral.isSelected()) {
        satisfaction = "Neutral";
    } else if (dissatisfied.isSelected()) {
        satisfaction = "Dissatisfied";
    }
    
    // Validate input
    if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || comment.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill in all fields");
        return;
    }
    
    // Validate comment length (50-100 characters)
    if (comment.length() < 50 || comment.length() > 100) {
        JOptionPane.showMessageDialog(this, "Comment must be between 50 and 100 characters");
        return;
    }
    
    // Validate satisfaction selection
    if (satisfaction == null) {
        JOptionPane.showMessageDialog(this, "Please select a satisfaction level");
        return;
    }
    
    try {
        String query = "INSERT INTO feedbacks (first_name, last_name, email, gender, comment, satisfaction) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, firstname);
            pstmt.setString(2, lastname);
            pstmt.setString(3, email);
            pstmt.setString(4, gender);
            pstmt.setString(5, comment);
            pstmt.setString(6, satisfaction);
            
            pstmt.executeUpdate();
            
            loadDataToTable();
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
    
    // Clear radio button selection
    ButtonGroup satisfactionGroup = new ButtonGroup();
    satisfactionGroup.add(verysatisfied);
    satisfactionGroup.add(satisfied);
    satisfactionGroup.add(neutral);
    satisfactionGroup.add(dissatisfied);
    satisfactionGroup.clearSelection();
}
private void loadDataToTable() {
    try {
        tableModel = (DefaultTableModel) Table.getModel();
        tableModel.setRowCount(0); 
        
        
        String query = "SELECT first_name, last_name, email, gender, comment, satisfaction FROM feedbacks";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                
                String comment = rs.getString("comment");
                String displayComment = comment;
                
              
                final int MAX_DISPLAY_LENGTH = 30; 
                if (comment != null && comment.length() > MAX_DISPLAY_LENGTH) {
                    displayComment = comment.substring(0, MAX_DISPLAY_LENGTH) + "...";
                }
                
                tableModel.addRow(new Object[]{
                    rs.getString("first_name"),
//                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("gender"),
                    displayComment, 
                    rs.getString("satisfaction")
                });
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
    }
}
 private void exportFeedbacks() {
    try {
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Feedbacks to CSV");
        fileChooser.setSelectedFile(new File("feedbacks_export.csv"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
        
            PreparedStatement pstmt = connection.prepareStatement(
                "SELECT first_name, last_name, email, gender, comment, satisfaction FROM feedbacks");
            
            // Execute query
            ResultSet rs = pstmt.executeQuery();
            
            // Create FileWriter for CSV export
            try (FileWriter csvWriter = new FileWriter(fileToSave)) {
                // Write CSV Header
                csvWriter.append("First Name,Last Name,Email,Gender,Comment,Satisfaction\n");
                
                // Iterate through result set and write to CSV
                while (rs.next()) {
                    csvWriter.append(
                        cleanField(rs.getString("first_name")) + "," +
                        cleanField(rs.getString("last_name")) + "," +
                        cleanField(rs.getString("email")) + "," +
                        cleanField(rs.getString("gender")) + "," +
                        cleanField(rs.getString("comment")) + "," +
                        cleanField(rs.getString("satisfaction")) + "\n"
                    );
                }
                
                // Close resources
                rs.close();
                pstmt.close();
                
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

    private void deleteFeedback() {
    int selectedRow = Table.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row to delete");
        return;
    }
    
    // Get email from selected row
    String email = (String) Table.getValueAt(selectedRow, 1);
    
    // Confirm deletion with user
    int confirmed = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to delete this feedback?",
        "Confirm Deletion",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE
    );
    
    if (confirmed != JOptionPane.YES_OPTION) {
        return;  // User cancelled the operation
    }
    
    try {
        String query = "DELETE FROM feedbacks WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, email);
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // Clear form fields after successful deletion
                clearFields();
                
                // Reload table data
                loadDataToTable();
                
                JOptionPane.showMessageDialog(this, "Feedback deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "No feedback found with the specified email.");
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error deleting feedback: " + e.getMessage());
    }
}

// Helper method to clear all form fields
private void clearFields() {
    jTextField3.setText("");  // First name
    Last.setText("");         // Last name
    jTextField2.setText("");  // Email
    jComboBox1.setSelectedIndex(0);  // Gender
    jTextArea1.setText("");   // Comment
    
    // Clear radio button selection
    ButtonGroup group = new ButtonGroup();
    group.add(verysatisfied);
    group.add(satisfied);
    group.add(neutral);
    group.add(dissatisfied);
    group.clearSelection();
    
    // Disable action buttons
  
}
private void viewFeedbackDetails() {
    int selectedRow = Table.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row to view");
        return;
    }
    
   
    String email = (String) Table.getValueAt(selectedRow, 1); 
    
   
    try {
        String query = "SELECT first_name, last_name, email, gender, comment, satisfaction FROM feedbacks WHERE email = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, email);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
           
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            String gender = rs.getString("gender");
            String comment = rs.getString("comment");
            String satisfaction = rs.getString("satisfaction");
            
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
            
            // Create labels with appropriate data
            String[] labelNames = {"First Name:", "Last Name:", "Email:", "Gender:", "Feedback:", "Satisfaction:"};
            String[] values = {firstName, lastName, email, gender, comment, satisfaction};
            
            for (int i = 0; i < labelNames.length; i++) {
                gbc.gridx = 0;
                gbc.gridy = i;
                JLabel label = new JLabel(labelNames[i]);
                label.setFont(new Font("Arial", Font.BOLD, 14));
                contentPanel.add(label, gbc);
                
                gbc.gridx = 1;
                // Use HTML for potential wrapping of long text like comments
                JLabel valueLabel = new JLabel("<html>" + values[i] + "</html>");
                valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                contentPanel.add(valueLabel, gbc);
            }
            
            detailsDialog.add(contentPanel, BorderLayout.CENTER);
            // Keep width 1000px, adjust height to accommodate all fields
            detailsDialog.setSize(1000, 300);
            detailsDialog.setResizable(false);
            detailsDialog.setLocationRelativeTo(this);
            detailsDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No data found for the selected record");
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error fetching feedback details: " + ex.getMessage());
    }
}
private void updateRecord() {
    ButtonGroup satisfactionGroup = new ButtonGroup();
    satisfactionGroup.add(verysatisfied);
    satisfactionGroup.add(satisfied);
    satisfactionGroup.add(neutral);
    satisfactionGroup.add(dissatisfied);
    
    
    int selectedRow = Table.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row to edit");
        return;
    }
    
    // Retrieve original email for database lookup
    String originalEmail = (String) Table.getValueAt(selectedRow, 1); 
    
    // Get updated values from form fields
    String updatedFirstName = jTextField3.getText().trim();
    String updatedLastName = Last.getText().trim();
    String updatedEmail = jTextField2.getText().trim();
    String updatedGender = (String) jComboBox1.getSelectedItem();
    String updatedComment = jTextArea1.getText().trim();
    
    // Get satisfaction from radio buttons
    String updatedSatisfaction = "";
    if (verysatisfied.isSelected()) {
        updatedSatisfaction = "Very Satisfied";
    } else if (satisfied.isSelected()) {
        updatedSatisfaction = "Satisfied";
    } else if (neutral.isSelected()) {
        updatedSatisfaction = "Neutral";
    } else if (dissatisfied.isSelected()) {
        updatedSatisfaction = "Dissatisfied";
    }
    
    // Validate comment length (50-100 characters) as per database constraint
    if (updatedComment.length() < 50 || updatedComment.length() > 100) {
        JOptionPane.showMessageDialog(this, 
            "Comment must be between 50 and 100 characters. Current length: " + updatedComment.length());
        return;
    }
    
    try {
        // Updated to match database column names
        PreparedStatement pstmt = connection.prepareStatement(
            "UPDATE feedbacks SET first_name = ?, last_name = ?, email = ?, gender = ?, comment = ?, satisfaction = ? WHERE email = ?");
        
        pstmt.setString(1, updatedFirstName);
        pstmt.setString(2, updatedLastName);
        pstmt.setString(3, updatedEmail);
        pstmt.setString(4, updatedGender);
        pstmt.setString(5, updatedComment);
        pstmt.setString(6, updatedSatisfaction);
        pstmt.setString(7, originalEmail);
        
        int updatedRows = pstmt.executeUpdate();
        if (updatedRows > 0) {
            JOptionPane.showMessageDialog(this, "Record updated successfully");
            // Refresh table data with all fields
            Table.setValueAt(updatedFirstName, selectedRow, 0);
//            Table.setValueAt(updatedLastName, selectedRow, 1);
            Table.setValueAt(updatedEmail, selectedRow, 1);
            Table.setValueAt(updatedGender, selectedRow, 2);
            // May need to trim comment for table display
            String displayComment = updatedComment.length() > 30 ? 
                                   updatedComment.substring(0, 27) + "..." : 
                                   updatedComment;
            Table.setValueAt(displayComment, selectedRow, 3);
            Table.setValueAt(updatedSatisfaction, selectedRow, 4);
        } else {
            JOptionPane.showMessageDialog(this, "No record found to update");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage());
    }
}
private void setupTableListener() {
    // Configure table selection behavior
    Table.setRowSelectionAllowed(true);
    Table.setColumnSelectionAllowed(false);
    
    // Add selection listener
    Table.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting() && Table.getSelectedRow() != -1) {
            // Get selected row data
            int selectedRow = Table.getSelectedRow();
            String email = (String) Table.getValueAt(selectedRow, 1); // Email in column 1
            
            try {
                // Prepare and execute query using existing connection
                String query = "SELECT first_name, last_name, gender, comment, satisfaction " +
                               "FROM feedbacks WHERE email = ?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, email);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    // Populate form fields
                    jTextField3.setText(rs.getString("first_name"));
                    Last.setText(rs.getString("last_name"));
                    jTextField2.setText(email); // Use email from table
                    jComboBox1.setSelectedItem(rs.getString("gender"));
                    jTextArea1.setText(rs.getString("comment"));
                    
                    // Handle satisfaction radio buttons
                    String satisfaction = rs.getString("satisfaction");
                    if (satisfaction != null) {
                        switch (satisfaction) {
                            case "Very Satisfied": verysatisfied.setSelected(true); break;
                            case "Satisfied": satisfied.setSelected(true); break;
                            case "Neutral": neutral.setSelected(true); break;
                            case "Dissatisfied": dissatisfied.setSelected(true); break;
                            default: clearRadioSelection();
                        }
                    }
                    
                    // Enable action buttons
                    Delete.setEnabled(true);
                    view.setEnabled(true);
                    Edit.setEnabled(true);
                }
                
                // Close resources
                rs.close();
                pstmt.close();
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading feedback data: " + ex.getMessage(),
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
                
                // Disable buttons on error
                Delete.setEnabled(false);
                view.setEnabled(false);
                Edit.setEnabled(false);
            }
        }
    });
}

private void clearRadioSelection() {
    ButtonGroup group = new ButtonGroup();
    group.add(verysatisfied);
    group.add(satisfied);
    group.add(neutral);
    group.add(dissatisfied);
    group.clearSelection();
}
 
private void search() {
    JDialog searchDialog = new JDialog(this, "Search Feedbacks", true);
    searchDialog.setLayout(new BorderLayout(10, 10));
    
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    JPanel inputPanel = new JPanel(new GridLayout(3, 1, 5, 5));
    JLabel instructionLabel = new JLabel("Enter search text to filter the table:");
    JTextField searchField = new JTextField(20);
    JLabel statusLabel = new JLabel(" ");
    
    inputPanel.add(instructionLabel);
    inputPanel.add(searchField);
    inputPanel.add(statusLabel);
    
    JPanel buttonsPanel = new JPanel();
    JButton filterButton = new JButton("Filter");
    JButton clearButton = new JButton("Clear Filter");
    JButton closeButton = new JButton("Close");
    
    buttonsPanel.add(filterButton);
    buttonsPanel.add(clearButton);
    buttonsPanel.add(closeButton);
    
    mainPanel.add(inputPanel, BorderLayout.CENTER);
    mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
    
    searchDialog.add(mainPanel);
    
    filterButton.addActionListener(e -> applyFilter(searchField, statusLabel));
    clearButton.addActionListener(e -> clearFilter(searchField, statusLabel));
    closeButton.addActionListener(e -> searchDialog.dispose());
    
    searchField.addKeyListener(new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            applyFilter(searchField, statusLabel);
        }
    });
    
    searchDialog.setSize(400, 200);
    searchDialog.setLocationRelativeTo(this);
    searchDialog.setResizable(false);
    searchDialog.setVisible(true);
}

private void applyFilter(JTextField searchField, JLabel statusLabel) {
    String searchText = searchField.getText().trim();
    if (searchText.isEmpty()) {
        sorter.setRowFilter(null);
        statusLabel.setText("Filter cleared");
    } else {
        try {
            RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + searchText);
            sorter.setRowFilter(rf);
            int rowCount = Table.getRowCount();
            statusLabel.setText(rowCount == 0 ? "No matching records found" : rowCount + " matching records found");
        } catch (java.util.regex.PatternSyntaxException ex) {
            statusLabel.setText("Invalid search pattern");
        }
    }
}

private void clearFilter(JTextField searchField, JLabel statusLabel) {
    searchField.setText("");
    sorter.setRowFilter(null);
    statusLabel.setText("Filter cleared");
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
        jTextField3 = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        view = new javax.swing.JButton();
        Delete = new javax.swing.JButton();
        Export = new javax.swing.JButton();
        patching = new javax.swing.JButton();
        search = new javax.swing.JButton();
        verysatisfied = new javax.swing.JRadioButton();
        satisfied = new javax.swing.JRadioButton();
        neutral = new javax.swing.JRadioButton();
        dissatisfied = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
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

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
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

        patching.setText("Update");
        patching.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                patchingActionPerformed(evt);
            }
        });

        search.setText("Search");
        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
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
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(view)
                            .addComponent(patching))
                        .addGap(30, 30, 30)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Export, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(Delete, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(search)))))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(view)
                    .addComponent(Delete)
                    .addComponent(search))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Export)
                    .addComponent(patching))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        verysatisfied.setForeground(new java.awt.Color(255, 255, 255));
        verysatisfied.setText("Very Satisfied,");
        verysatisfied.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verysatisfiedActionPerformed(evt);
            }
        });

        satisfied.setForeground(new java.awt.Color(255, 255, 255));
        satisfied.setText("Satisfied");

        neutral.setForeground(new java.awt.Color(255, 255, 255));
        neutral.setText("Neutral");
        neutral.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                neutralActionPerformed(evt);
            }
        });

        dissatisfied.setForeground(new java.awt.Color(204, 255, 255));
        dissatisfied.setText("Dissatisfied");
        dissatisfied.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dissatisfiedActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Please provide your satisfaction level?");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 477, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Last, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE))
                        .addGap(56, 56, 56)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(verysatisfied, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(satisfied, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dissatisfied, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(neutral, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ResultPage, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Edit, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(21, Short.MAX_VALUE))
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
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addGap(25, 25, 25)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(verysatisfied)
                    .addComponent(neutral))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(satisfied)
                    .addComponent(dissatisfied))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(Edit, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ResultPage, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );

        Feedback.add(jPanel1);
        jPanel1.setBounds(20, 0, 600, 590);

        Table.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0))));
        Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Firstname", "Email", "Gender", "Feedback", "Satisfication"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        Table.setColumnSelectionAllowed(true);
        jScrollPane2.setViewportView(Table);

        Feedback.add(jScrollPane2);
        jScrollPane2.setBounds(640, 10, 880, 590);

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
                .addComponent(Feedback, javax.swing.GroupLayout.PREFERRED_SIZE, 602, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(82, Short.MAX_VALUE))
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

    private void verysatisfiedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verysatisfiedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_verysatisfiedActionPerformed

    private void neutralActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_neutralActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_neutralActionPerformed

    private void dissatisfiedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dissatisfiedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dissatisfiedActionPerformed

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
      search();         // TODO add your handling code here:
    }//GEN-LAST:event_searchActionPerformed

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
    private javax.swing.JRadioButton dissatisfied;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JRadioButton neutral;
    private javax.swing.JButton patching;
    private javax.swing.JRadioButton satisfied;
    private javax.swing.JButton search;
    private javax.swing.JRadioButton verysatisfied;
    private javax.swing.JButton view;
    // End of variables declaration//GEN-END:variables
}
