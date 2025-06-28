package com.example.csc325_firebase_webview_auth.view;

// You need this import for UserRecord
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginFormController {

    @FXML
    private TextField emailField;
    @FXML
    private TextField passwordField;
    @FXML
    private Label errorMessageLabel;

    @FXML
    private void handleLoginButton(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText(); // Note: Admin SDK does not use passwords directly.

        if (email.isEmpty() || password.isEmpty()) {
            errorMessageLabel.setText("Email and password cannot be empty.");
            return;
        }

        try {
            // Use the Admin SDK to get the user by email.
            // This is a synchronous call.
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);

            // Now, you need a way to verify the password.
            // THE ADMIN SDK DOES NOT PROVIDE A METHOD TO VERIFY A PASSWORD DIRECTLY.
            // This is a security feature. You should not be handling user passwords on the server.
            // To properly do this, you would need to use a client-side SDK.

            // Since you are forced to use the Admin SDK, here is a workaround for demonstration purposes.
            // This is NOT secure and should not be used in production.
            // You can generate a custom token and use a client-side SDK to sign in with it.
            // A more direct check is not possible.

            // For this assignment, we will simply check if the user exists by email.
            // A real sign-in would be more complex.
            if (userRecord != null) {
                // If the user exists, assume a successful login for this assignment's purpose.
                System.out.println("User exists: " + userRecord.getUid());

                // For a real app, you would verify the password here.
                // Since there's no Admin SDK API for it, we will just proceed.

                // On successful login, switch to the main application scene
                try {
                    App.setRoot("/files/AccessFBView.fxml");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                errorMessageLabel.setText("Login failed: User not found.");
                System.err.println("User not found for email: " + email);
            }

        } catch (FirebaseAuthException e) {
            // Handle errors like user not found or invalid email
            errorMessageLabel.setText("Login failed: " + e.getMessage());
            System.err.println("Login failed: " + e.getMessage());
        }
    }

    @FXML
    private void switchToRegister() throws IOException {
        App.setRoot("/files/RegisterForm.fxml");
    }

    @FXML
    private void switchToSplash() throws IOException {
        App.setRoot("/files/SplashScreen.fxml");
    }
}