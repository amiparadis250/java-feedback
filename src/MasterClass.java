import java.util.regex.Pattern;


public class MasterClass {
    public boolean checkEmail(String email) {
        return Pattern.matches("^[a-zA-Z0-9._%+-]+@(gmail\\.com|yahoo\\.com|hotmail\\.com)$", email);
    }

    public int getStringLength(String txt) {
        return txt.length();
    }

    public String changeToUpper(String txt) {
        return txt.toUpperCase();
    }

    
    public boolean validatePassword(String password) {
        // Check length between 8 and 12
        if (password.length() < 8 || password.length() > 12) {
            return false;
        }
        
        // Check if password contains only uppercase letters
        return password.matches("^[A-Z]+$");
    }

    
    public String getPasswordValidationError(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        
        if (password.length() > 12) {
            return "Password must be at most 12 characters long";
        }
        
        if (!password.matches("^[A-Z]+$")) {
            return "Password must contain only uppercase letters";
        }
        
        return null; // No errors
    }

    public static void main(String[] args) {
        SignUp SignupFrame = new SignUp();
        SignupFrame.setVisible(true);
        SignupFrame.pack();
        SignupFrame.setLocationRelativeTo(null);
    }
}