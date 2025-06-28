package com.example.csc325_firebase_webview_auth.view;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.IOException;

public class RegisterFormController {

    @FXML
    private TextField emailField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField confirmPasswordField;
    @FXML
    private Label errorMessageLabel;

    @FXML
    private void handleRegisterButton(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorMessageLabel.setText("All fields are required.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            errorMessageLabel.setText("Passwords do not match.");
            return;
        }
        if (password.length() < 6) {
            errorMessageLabel.setText("Password must be at least 6 characters.");
            return;
        }

        // Use Firebase Authentication to create a new user
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password);

        try {
            UserRecord userRecord = App.fauth.createUser(request);
            System.out.println("Successfully created new user: " + userRecord.getUid());
            errorMessageLabel.setText("Registration successful!");
            // Optionally, switch to the login scene after successful registration
            App.setRoot("/files/LoginForm.fxml");
        } catch (FirebaseAuthException ex) {
            errorMessageLabel.setText("Registration failed: " + ex.getMessage());
            System.err.println("Registration failed: " + ex.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("/files/LoginForm.fxml");
    }

    @FXML
    private void switchToSplash() throws IOException {
        App.setRoot("/files/SplashScreen.fxml");
    }
}