package com.example.csc325_firebase_webview_auth.view;



import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.example.csc325_firebase_webview_auth.model.CurrUser; // Import the helper class
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the registration screen (register_screen.fxml).
 * This class handles new user account creation and input validation.
 */
public class RegisterScreenController implements Initializable {

    // --- FXML Injected UI Components ---
    // The '@FXML' annotation links these variables to the 'fx:id' attributes in the FXML.
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    @FXML private Button registerButton; // The registration button

    // --- Firebase Authentication Instance ---
    private FirebaseAuth fAuth;
    private static final Logger logger = Logger.getLogger(RegisterScreenController.class.getName());

    /**
     * Initializes the controller. This method is automatically called after the FXML is loaded.
     * It sets up the Firebase Auth instance and can perform initial checks.
     * @param url The location of the FXML file.
     * @param rb The resources.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get the Firebase Auth instance from the main App class.
        fAuth = App.fauth;
        if (fAuth == null) {
            logger.log(Level.SEVERE, "Firebase Auth is not initialized. Registration will fail.");
            messageLabel.setText("Application error: Firebase services are unavailable.");
            registerButton.setDisable(true); // Disable the button if auth is not ready.
        }
    }

    /**
     * Handles the registration action when the "Register" button is clicked.
     * It performs input validation and attempts to create a new user account in Firebase.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // --- Input Validation ---
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("All fields are required.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }
        if (password.length() < 6) {
            // Firebase requires a minimum password length of 6 characters.
            messageLabel.setText("Password must be at least 6 characters long.");
            return;
        }

        try {
            // --- Firebase User Creation Logic (using Admin SDK) ---
            // Create a request object with the user's email and password.
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setEmailVerified(false) // Set to true if you've verified the email externally.
                    .setDisabled(false);

            // Create the user in Firebase Authentication. This is a network operation.
            UserRecord userRecord = fAuth.createUser(request);
            logger.log(Level.INFO, "Successfully created new user with UID: " + userRecord.getUid());

            // --- Set the current user's UID globally after successful registration ---
            // This is important for navigating to the main app as a logged-in user.
            CurrUser.setUid(userRecord.getUid());

            messageLabel.setText("Registration successful! Redirecting to dashboard...");

            // --- Redirect to the user dashboard (Viewing) on success ---
            // Use the static App.setRoot() method to switch the scene to the main dashboard.
            App.setRoot("/files/landingscreen.fxml"); // Assuming 'landingscreen.fxml' is the dashboard.

        } catch (FirebaseAuthException e) {
            // --- Error Handling ---
            // Catch specific Firebase Authentication exceptions and provide user-friendly feedback.
            String errorMessage = e.getMessage();
            if (errorMessage.contains("auth/email-already-exists")) {
                messageLabel.setText("This email is already registered.");
            } else if (errorMessage.contains("auth/invalid-email")) {
                messageLabel.setText("Invalid email format.");
            } else if (errorMessage.contains("auth/weak-password")) {
                messageLabel.setText("Password is too weak. Please choose a stronger one.");
            } else {
                messageLabel.setText("Registration failed: " + errorMessage);
                logger.log(Level.SEVERE, "Registration failed with unexpected error", e);
            }
        } catch (IOException ex) {
            // Catch errors if the FXML for the main dashboard cannot be loaded.
            logger.log(Level.SEVERE, "Failed to load dashboard view after registration", ex);
            messageLabel.setText("Application error during redirection.");
        }
    }

    /**
     * Handles the action to navigate back to the login screen.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void goToLogin(ActionEvent event) {
        logger.log(Level.INFO, "Switching back to login screen.");
        try {
            // Use the static App.setRoot() method to change the scene to the login screen FXML.
            App.setRoot("/files/login_screen.fxml"); // This assumes a separate FXML for the login screen.
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to load login view.", ex);
        }
    }
}