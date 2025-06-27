package com.example.csc325_firebase_webview_auth.view;

import com.google.firebase.auth.FirebaseAuth;
// The import for FirebaseUser is no longer needed since we are not using the client SDK.
// import com.google.firebase.auth.FirebaseUser; // REMOVE THIS LINE
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.example.csc325_firebase_webview_auth.model.CurrUser; // Ensure this import is present

/**
 * Controller for the authentication-aware splash screen.
 * This class checks if a user is logged in using a custom session state mechanism
 * and redirects the application to the appropriate view (dashboard or login).
 */
public class SplashScreenController implements Initializable {

    // --- FXML Injected Components ---
    // The statusLabel is used to provide feedback to the user during the check.
    @FXML private Label statusLabel;

    // --- Constants ---
    // The duration the splash screen will be displayed before redirecting.
    private static final int SPLASH_DURATION_SECONDS = 3;
    private static final Logger logger = Logger.getLogger(SplashScreenController.class.getName());

    /**
     * Initializes the controller after the FXML has been loaded.
     * This method sets up a timer to perform the authentication check and redirection.
     * @param url The location of the FXML file.
     * @param rb The resources.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // --- Get the Firebase Auth instance ---
        // This is a singleton instance from the main App class.
        FirebaseAuth fAuth = App.fauth;

        // --- Create a Timeline for a timed redirection ---
        // A Timeline is a simple timer that performs an action after a specified duration.
        // It's used here to ensure the user sees the splash screen before redirection.
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(SPLASH_DURATION_SECONDS), event -> {
            // This code block runs after the 3-second delay.
            try {
                // --- Authentication Check Logic (The Corrected Approach) ---
                // We check the UID stored in our custom global helper class (CurrUser).
                // This UID is set by your login/registration controllers after a successful auth event.
                String currentUserId = CurrUser.getUid();

                if (currentUserId != null) {
                    // --- Case 1: User is Logged In ---
                    // If the UID is not null, it means a user is authenticated.
                    logger.log(Level.INFO, "User is already logged in with UID: " + currentUserId + ". Redirecting to dashboard.");
                    // Redirect to your main app dashboard.
                    App.setRoot("/files/landingscreen.fxml"); // Using your new landingscreen FXML.
                } else {
                    // --- Case 2: No User is Logged In ---
                    // If the UID is null, the user needs to sign in or register.
                    logger.log(Level.INFO, "No user is logged in. Redirecting to login screen.");
                    // Redirect to your login view FXML.
                    App.setRoot("/files/login_screen.fxml");
                }

            } catch (IOException ex) {
                // Log any errors if the target FXML file cannot be loaded.
                logger.log(Level.SEVERE, "Failed to load a scene after authentication check.", ex);
                // Update the status label to inform the user of the error before exiting.
                statusLabel.setText("Failed to load application. Please restart.");
            }
        }));

        // --- Start the animation and check ---
        // This command starts the timer.
        timeline.play();
    }
}