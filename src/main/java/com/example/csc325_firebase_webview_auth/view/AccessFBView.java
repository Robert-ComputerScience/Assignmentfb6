package com.example.csc325_firebase_webview_auth.view;

// Make sure this import is present at the top of your file
import com.google.firebase.auth.FirebaseAuth;

import com.example.csc325_firebase_webview_auth.model.Person;
import com.example.csc325_firebase_webview_auth.viewmodel.AccessDataViewModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class AccessFBView {

    @FXML
    private TextField nameField;
    @FXML
    private TextField majorField;
    @FXML
    private TextField ageField;
    @FXML
    private Button writeButton;
    @FXML
    private Button readButton;
    @FXML
    private TextArea outputField;
    private boolean key;
    private ObservableList<Person> listOfUsers = FXCollections.observableArrayList();
    private Person person;

    @FXML
    private TableView<Person> tableView;
    @FXML
    private TableColumn<Person, String> nameColumn;
    @FXML
    private TableColumn<Person, String> majorColumn;
    @FXML
    private TableColumn<Person, Integer> ageColumn;
    public ObservableList<Person> getListOfUsers() {
        return listOfUsers;
    }

    void initialize() {
        AccessDataViewModel accessDataViewModel = new AccessDataViewModel();
        nameField.textProperty().bindBidirectional(accessDataViewModel.userNameProperty());
        majorField.textProperty().bindBidirectional(accessDataViewModel.userMajorProperty());
        writeButton.disableProperty().bind(accessDataViewModel.isWritePossibleProperty().not());
        // --- NEW CODE: Initialize the TableView columns ---
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        majorColumn.setCellValueFactory(new PropertyValueFactory<>("major"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        // --- END NEW CODE ---
    }

    @FXML
    private void addRecord(ActionEvent event) {
        addData();
    }

    @FXML
    private void readRecord(ActionEvent event) {
        readFirebase();
    }

    @FXML
    private void regRecord(ActionEvent event) {
        registerUser();
    }

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("/files/WebContainer.fxml");
    }

    /**
     * Handles the logout action from the menu.
     * Signs out the user and redirects to the splash screen.
     * @param event The action event.
     */
    @FXML
    private void handleLogout(ActionEvent event) throws InterruptedException {
        // Sign out the user from Firebase Authentication
        // This is a synchronous call in the Admin SDK
        App.fauth.wait();
        System.out.println("User signed out.");

        // Redirect to the splash screen
        try {
            App.setRoot("/files/SplashScreen.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleEditUser(ActionEvent event) {
        // Implement logic to edit user data, e.g., open a dialog or enable text fields
        System.out.println("Edit User menu item clicked.");
        // Example: logic to enable editing of fields
        // nameField.setEditable(true);
        // majorField.setEditable(true);
        // ageField.setEditable(true);
    }

    @FXML
    private void handleViewProfile(ActionEvent event) {
        // You can switch to a dedicated profile scene here.
        // For now, let's just print a message.
        System.out.println("View Profile menu item clicked.");
        // Example: App.setRoot("/files/ProfileView.fxml");
    }

    @FXML
    private void handleUploadPicture(ActionEvent event) {
        // You would implement the logic to upload a picture to Firebase Storage here.
        System.out.println("Upload Picture menu item clicked.");
        // Example: open a file chooser and upload the selected file
    }



    public void addData() {
        DocumentReference docRef = App.fstore.collection("References").document(UUID.randomUUID().toString());
        Map<String, Object> data = new HashMap<>();
        data.put("Name", nameField.getText());
        data.put("Major", majorField.getText());
        data.put("Age", Integer.parseInt(ageField.getText()));
        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);
    }

    public boolean readFirebase() {
        key = false;

        // Asynchronously retrieve all documents
        ApiFuture<QuerySnapshot> future = App.fstore.collection("References").get();
        List<QueryDocumentSnapshot> documents;
        try {
            documents = future.get().getDocuments();

            // --- NEW CODE: Clear the list and the TextArea ---
            listOfUsers.clear();
            outputField.setText(""); // Clear the TextArea for a clean output
            // --- END NEW CODE ---

            if (documents.size() > 0) {
                System.out.println("Outing....");
                for (QueryDocumentSnapshot document : documents) {
                    // --- OLD CODE: Commented out to redirect output to TableView ---
                    // outputField.setText(outputField.getText() + document.getData().get("Name") + " , Major: " +
                    //         document.getData().get("Major") + " , Age: " +
                    //         document.getData().get("Age") + " \n ");
                    // --- END OLD CODE ---

                    System.out.println(document.getId() + " => " + document.getData().get("Name"));
                    person = new Person(String.valueOf(document.getData().get("Name")),
                            document.getData().get("Major").toString(),
                            Integer.parseInt(document.getData().get("Age").toString()));
                    listOfUsers.add(person);
                }
            } else {
                System.out.println("No data");
            }
            key = true;

            // --- NEW CODE: Populate the TableView with the list ---
            tableView.setItems(listOfUsers);
            // --- END NEW CODE ---

        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
        return key;
    }

    public void sendVerificationEmail() {
        try {
            UserRecord user = App.fauth.getUser("name");
        } catch (Exception e) {
        }
    }

    public boolean registerUser() {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail("user@example.com")
                .setEmailVerified(false)
                .setPassword("secretPassword")
                .setPhoneNumber("+11234567890")
                .setDisplayName("John Doe")
                .setDisabled(false);

        UserRecord userRecord;
        try {
            userRecord = App.fauth.createUser(request);
            System.out.println("Successfully created new user: " + userRecord.getUid());
            return true;
        } catch (FirebaseAuthException ex) {
            // Logger.getLogger(FirestoreContext.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}