package com.example.csc325_firebase_webview_auth.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.io.IOException;

public class SplashScreenController {

    /**
     * Handles the action for the Login button.
     * @param event The ActionEvent that triggered this method.
     * @throws IOException
     */
    @FXML
    private void handleLoginButton(ActionEvent event) throws IOException {
        // Here you will switch to the login form scene
        System.out.println("Login button clicked! Switching to login form.");
        App.setRoot("/files/LoginForm.fxml"); // You need to create this FXML file in the next step
    }

    /**
     * Handles the action for the Register button.
     * @param event The ActionEvent that triggered this method.
     * @throws IOException
     */
    @FXML
    private void handleRegisterButton(ActionEvent event) throws IOException {
        // Here you will switch to the registration form scene
        System.out.println("Register button clicked! Switching to registration form.");
        App.setRoot("/files/RegisterForm.fxml"); // You need to create this FXML file in the next step
    }
}