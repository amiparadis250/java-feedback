import javax.swing.*;
import java.awt.*;

public class ResultPage extends JFrame {
    
    public ResultPage(String firstname, String lastname, String email, String gender, String comment) {
        // Set window title
        setTitle("Submission Details");
        setSize(400, 300);
        setLocationRelativeTo(null);  // Center the window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Create panel with layout
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 1, 5, 5)); // 7 rows, 1 column, spacing 5px
        
        // Greeting message
        JLabel greeting = new JLabel("Hello, Thank you " + firstname + ", here are your details:", JLabel.CENTER);
        greeting.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Labels for user details
        JLabel lblFirstname = new JLabel("📌 Firstname: " + firstname);
        JLabel lblLastname = new JLabel("📌 Lastname: " + lastname);
        JLabel lblEmail = new JLabel("📧 Email: " + email);
        JLabel lblGender = new JLabel("⚥ Gender: " + gender);
        
        // Comment area
        JTextArea commentArea = new JTextArea("📝 Comment: " + comment);
        commentArea.setEditable(false);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setBackground(null); // Matches background
        
        // Scroll for comments
        JScrollPane commentScroll = new JScrollPane(commentArea);
        
        // Back button
        JButton backButton = new JButton("🔙 Back");
        backButton.addActionListener(e -> dispose()); // Close window
        
        // Add components to panel
        panel.add(greeting);
        panel.add(lblFirstname);
        panel.add(lblLastname);
        panel.add(lblEmail);
        panel.add(lblGender);
        panel.add(commentScroll);
        panel.add(backButton);
        
        // Add panel to frame
        add(panel);
    }
    
    // Example main method to test the form
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ResultPage("John", "Doe", "john.doe@example.com", "Male", "This is a test comment.").setVisible(true);
        });
    }
}
