package com.example.csc325_firebase_webview_auth.view;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.example.csc325_firebase_webview_auth.model.CurrUser; // Import the CurrUser class to set the user ID
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
 * Controller for the LoginScreen.fxml.
 * Manages user authentication (sign-in) logic and navigation to other views.
 */
public class LoginScreenController implements Initializable {

    // --- FXML Injected UI Components ---
    // The '@FXML' annotation is essential for the FXMLLoader to inject the UI components.
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button loginButton; // The login button, injected for potential property binding

    // --- Firebase Authentication Instance ---
    private FirebaseAuth fAuth;

    /**
     * Initializes the controller. This method is called automatically when the FXML is loaded.
     * It sets up the Firebase Auth instance and any initial UI state.
     * @param url The location of the FXML file.
     * @param rb The resources.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get the Firebase Auth instance from the main App class.
        fAuth = App.fauth;
        if (fAuth == null) {
            Logger.getLogger(LoginScreenController.class.getName()).log(Level.SEVERE, "Firebase Auth is not initialized.");
            messageLabel.setText("Application error: Firebase services are unavailable.");
            loginButton.setDisable(true); // Disable the button if auth is not ready.
        }
    }

    /**
     * Handles the login action when the "Sign In" button is clicked.
     * It validates user input and attempts to authenticate with Firebase.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        // --- Input Validation ---
        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both email and password.");
            return;
        }

        try {
            // --- Authentication Logic (using Firebase Admin SDK) ---
            // The Firebase Admin SDK is typically for backend services. For client-side auth,
            // the Firebase Client SDK is used, which has direct `signInWithEmailAndPassword` methods.
            // Here, we simulate the process by checking if the user exists.
            UserRecord userRecord = fAuth.getUserByEmail(email);

            // In a production client app, you would use a client SDK for a secure login flow.
            // For this demo, we'll assume a successful login if the user record is found.
            if (userRecord != null) {
                Logger.getLogger(LoginScreenController.class.getName()).log(Level.INFO, "Login successful for user: " + userRecord.getUid());

                // --- Set the current user's UID globally using the helper class ---
                // This makes the user's information available to other controllers, like your Viewing.java dashboard.
                CurrUser.setUid(userRecord.getUid());

                // --- Redirect to the main application dashboard on success ---
                // We use the static App.setRoot() method to switch the entire scene.
                App.setRoot("/files/landingscreen.fxml"); // Redirect to your dashboard view FXML.
            } else {
                messageLabel.setText("Invalid email or password.");
            }

        } catch (FirebaseAuthException e) {
            // --- Error Handling ---
            // Catch specific Firebase authentication errors and provide user-friendly messages.
            String errorMessage = e.getMessage();
            if (errorMessage.contains("auth/user-not-found") || errorMessage.contains("auth/wrong-password")) {
                messageLabel.setText("Invalid email or password.");
            } else if (errorMessage.contains("auth/invalid-email")) {
                messageLabel.setText("Invalid email format.");
            } else {
                messageLabel.setText("Login failed: " + errorMessage);
                Logger.getLogger(LoginScreenController.class.getName()).log(Level.SEVERE, "Login failed", e);
            }
        } catch (IOException ex) {
            // Catch errors if the FXML for the main view cannot be loaded.
            Logger.getLogger(LoginScreenController.class.getName()).log(Level.SEVERE, "Failed to load dashboard view after login", ex);
            messageLabel.setText("Application error during redirection.");
        }
    }

    /**
     * Handles the action to navigate to the registration screen.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void goToRegister(ActionEvent event) {
        System.out.println("Switching to registration form.");
        try {
            // Redirect to the FXML file for the registration form.
            App.setRoot("/files/registration_form.fxml"); // This assumes a separate FXML exists for registration
        } catch (IOException ex) {
            Logger.getLogger(LoginScreenController.class.getName()).log(Level.SEVERE, "Failed to load registration view", ex);
        }
    }
}