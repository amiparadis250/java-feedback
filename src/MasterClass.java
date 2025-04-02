import java.util.regex.Pattern;
import java.time.LocalDate;

public class MasterClass {

    public boolean isValidEmail(String email) {
        return Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email);
    }

    public boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,12}$");
    }
    public String getCurrentDate() {
        return LocalDate.now().toString();
    }
    public int getStringLength(String txt) {
        return txt.length();
    }

    //it was existing in lab 1
    public String changeToUpper(String txt) {
        return txt.toUpperCase();
    }

  
    public String getPasswordValidationError(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        if (password.length() > 12) {
            return "Password must be at most 12 characters long";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one digit";
        }
        return null; 
    }

    public static void main(String[] args) {
        SignUp SignupFrame = new SignUp();
        SignupFrame.setVisible(true);
        SignupFrame.pack();
        SignupFrame.setLocationRelativeTo(null);
    }
}