package com.example.csc325_firebase_webview_auth.view;

// Make sure this import is present at the top of your file
// ADD THIS LINE
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.StorageOptions;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

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
    private void readRecord(ActionEvent event) {
        Task<Boolean> readTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // This runs on a background thread
                return readFirebase();
            }
        };

        readTask.setOnSucceeded(e -> {
            System.out.println("Records loaded successfully.");
            // The readFirebase() method already updates the TableView
        });

        readTask.setOnFailed(e -> {
            System.err.println("Failed to read records.");
            readTask.getException().printStackTrace();
            // You could show an error Alert to the user here
        });

        new Thread(readTask).start();
    }

    // Add this new method to your AccessFBView.java file

    /**
     * Creates a background task to write the current data from the text fields to Firestore.
     * This method is triggered by the "Write Record" button.
     * After a successful write, it clears the input fields and refreshes the table.
     * @param event The action event from the button click.
     */
    @FXML
    private void writeRecord(ActionEvent event) {
        Task<Void> writeTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // This runs on a background thread to prevent UI freezing
                addData();
                return null;
            }
        };

        // This runs on the UI thread after the task successfully completes
        writeTask.setOnSucceeded(e -> {
            System.out.println("Record written successfully.");
            // Clear the input fields for the next entry
            nameField.clear();
            majorField.clear();
            ageField.clear();
            // Automatically refresh the table to show the new record
            readRecord(null); // We can pass null since the event isn't used in readRecord
        });

        // This runs if the background task fails
        writeTask.setOnFailed(e -> {
            System.err.println("Failed to write record.");
            writeTask.getException().printStackTrace();
            // Consider showing an error dialog to the user here
        });

        // Start the background task
        new Thread(writeTask).start();
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
    private void handleLogout(ActionEvent event) {
        // 1. In a desktop app, "logging out" is primarily about changing the UI state.
        //    There is no user session to clear on the server in this simple context.
        System.out.println("User logout initiated. Redirecting to splash screen.");

        // 2. Redirect to the splash screen.
        try {
            App.setRoot("/files/SplashScreen.fxml");
        } catch (IOException e) {
            // It's good practice to print the stack trace if an error occurs.
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

    // Inside your controller class...
    @FXML
    private void handleUploadPicture(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Picture");
        // Set extension filters to allow only image files
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.bmp")
        );

        // Show the file open dialog
        File selectedFile = fileChooser.showOpenDialog(null); // Pass a Stage if available

        if (selectedFile != null) {
            // Run the upload in a background thread
            uploadFile(selectedFile);
        } else {
            System.out.println("No file selected.");
        }
    }

    private void uploadFile(File fileToUpload) {
        Task<Void> uploadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // --- This runs on a background thread ---

                // IMPORTANT: Replace with your Firebase Storage bucket name
                String bucketName = "csc325firebase-2daab.appspot.com";

                // This is the name the file will have in Firebase Storage
                String objectName = "profile-pictures/" + fileToUpload.getName();

                Storage storage = StorageOptions.newBuilder()
                        .setProjectId("csc325firebase-2daab")
                        .build()
                        .getService();

                BlobId blobId = BlobId.of(bucketName, objectName);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

                System.out.println("Uploading file " + fileToUpload.getName() + " to gs://" + bucketName + "/" + objectName);

                // Upload the file
                storage.create(blobInfo, Files.readAllBytes(fileToUpload.toPath()));

                System.out.println("File uploaded successfully.");
                return null;
            }
        };

        // Handle success
        uploadTask.setOnSucceeded(e -> {
            System.out.println("Upload task finished.");
            // You can show a confirmation dialog to the user here
        });

        // Handle failure
        uploadTask.setOnFailed(e -> {
            System.err.println("Upload failed.");
            uploadTask.getException().printStackTrace();
            // You can show an error dialog here
        });

        // Start the task
        new Thread(uploadTask).start();
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
                    person = new Person(document.getId(), String.valueOf(document.getData().get("Name")),
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

    @FXML
    private void handleDeleteRecord(ActionEvent event) {
        // Get the currently selected item from the TableView
        Person selectedPerson = tableView.getSelectionModel().getSelectedItem();

        // Check if an item was actually selected
        if (selectedPerson == null) {
            System.out.println("No record selected to delete.");
            // Optionally, show an alert to the user that they need to select a row first.
            return;
        }

        // Create a background task for the deletion to avoid freezing the UI
        Task<Void> deleteTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // This runs on a background thread
                System.out.println("Deleting record: " + selectedPerson.getId());

                // Use the person's unique ID to delete the document from Firestore
                App.fstore.collection("References").document(selectedPerson.getId()).delete();

                return null;
            }
        };

        // This runs on the UI thread after the background task successfully completes
        deleteTask.setOnSucceeded(e -> {
            System.out.println("Deletion successful. Refreshing table.");
            // Refresh the table to show the updated data
            readFirebase();
        });

        // This runs if the background task fails
        deleteTask.setOnFailed(e -> {
            System.err.println("Error deleting record.");
            deleteTask.getException().printStackTrace();
        });

        // Start the deletion task
        new Thread(deleteTask).start();
    }
}