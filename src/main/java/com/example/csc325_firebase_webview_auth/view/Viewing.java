package com.example.csc325_firebase_webview_auth.view;

import com.example.csc325_firebase_webview_auth.model.CurrUser;
import com.example.csc325_firebase_webview_auth.model.Person;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Controller for the user's main dashboard after successful sign-in.
 * It handles CRUD operations (Create, Read, Update, Delete) for user-specific data
 * displayed in a TableView.
 */
public class Viewing implements Initializable {

    // --- FXML Injected UI Components ---
    // These variables are linked to fx:id attributes in the FXML.
    @FXML
    TextField name, age, major;

    // This field holds the UID of the currently logged-in user.
    String currUser;

    // TableView and TableColumns for displaying the user's records.
    @FXML
    private TableView<Person> tv;

    @FXML
    private TableColumn<Person, Integer> tv_age;

    @FXML
    private TableColumn<Person, String> tv_fn, tv_major; // NOTE: tv_fn is typically for 'first name'

    // ObservableList to hold the data displayed in the TableView.
    // This list automatically updates the TableView when its content changes.
    ObservableList<Person> data = FXCollections.observableList(new ArrayList<Person>());

    // ImageView for displaying a user's profile picture.
    @FXML
    ImageView img_view;

    /**
     * Initializes the controller after the FXML file has been loaded.
     * This is where you set up the TableView, fetch initial data, and get the current user's ID.
     *
     * @param url The location used to resolve relative paths for the root object.
     * @param resourceBundle The resources used to localize the root object.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Get the UID of the currently logged-in user.
        // This is crucial for filtering data to show only the logged-in user's records.
        currUser = CurrUser.getUid();

        // --- TableColumn Initialization ---
        // These lines link the columns in the TableView (defined by fx:id) to the properties
        // of the 'Person' model class (via its getter methods like 'getName()').
        tv_fn.setCellValueFactory(new PropertyValueFactory<>("name")); // Mapped to 'name' property in Person.
        tv_age.setCellValueFactory(new PropertyValueFactory<>("age")); // Mapped to 'age' property in Person.
        tv_major.setCellValueFactory(new PropertyValueFactory<>("major")); // Mapped to 'major' property in Person.

        // --- Asynchronously Read User Data from Firestore ---
        // This process fetches documents from the "References" collection.
        ApiFuture<QuerySnapshot> future = App.fstore.collection("References").get();

        // This try-catch block waits for the Firestore query to complete.
        try {
            // Get the list of documents from the query result.
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.size() > 0) {
                System.out.println("Found documents in Firestore.");

                // Iterate through each document found.
                for (QueryDocumentSnapshot document : documents) {
                    System.out.println("Processing document with Age: " + document.getData().get("Age"));

                    // Create a new Person object from the Firestore document data.
                    Person person = new Person(
                            String.valueOf(document.getData().get("Name")),
                            document.getData().get("Major").toString(),
                            Integer.parseInt(document.getData().get("Age").toString())
                    );

                    // --- Filter data to show only the current user's records ---
                    // This is a key step to personalize the dashboard.
                    if(document.getData().get("User").equals(currUser)) {
                        data.add(person); // Add the person object to the ObservableList.
                    }
                }
            } else {
                System.out.println("No data found in the database.");
            }

        } catch (InterruptedException | ExecutionException ex) {
            // Print the stack trace if an error occurs during fetching data.
            ex.printStackTrace();
        }

        // --- Bind the ObservableList to the TableView ---
        // This displays the filtered data in the table.
        tv.setItems(data);
    }


    /**
     * Handles adding a new record to Firestore and updating the TableView.
     */
    @FXML
    protected void addNewRecord() {
        // Add the new record to the local ObservableList first for immediate UI update.
        data.add(new Person(name.getText(), major.getText(), Integer.parseInt(age.getText())));

        // Create a new document reference with a random UUID as the document ID.
        DocumentReference docRef = App.fstore.collection("References").document(UUID.randomUUID().toString());

        // Prepare the data to be written to Firestore.
        Map<String, Object> datas = new HashMap<>();
        datas.put("Name", name.getText());
        datas.put("Major", major.getText());
        datas.put("Age", Integer.parseInt(age.getText()));
        datas.put("User", currUser); // Add the current user's UID to the document.

        // Asynchronously write the data to Firestore.
        ApiFuture<WriteResult> result = docRef.set(datas);

        // Note: The result.get() is missing here, so the write is asynchronous and not waited on.
        // You may want to add a try-catch block with result.get() to confirm completion.
    }

    /**
     * Clears the input fields in the form.
     */
    @FXML
    protected void clearForm() {
        name.setText("");
        age.setText("");
        major.setText("");
    }

    /**
     * Closes the application.
     */
    @FXML
    protected void closeApplication() {
        System.exit(0);
    }


    /**
     * Edits a selected record in the TableView and updates it in Firestore.
     * This method is inefficient as it reads all documents from the database to find the one to delete.
     */
    @FXML
    protected void editRecord() {
        // Get the selected Person from the TableView.
        Person p = tv.getSelectionModel().getSelectedItem();

        if (p == null) {
            // Add a check to ensure an item is selected.
            System.out.println("Please select a record to edit.");
            return;
        }

        // --- INEFFICIENCY WARNING ---
        // This approach fetches ALL documents from Firestore to find the record's ID.
        // For a large database, this is very inefficient. A better approach would be
        // to store the Firestore Document ID as a property of your Person model.
        ApiFuture<QuerySnapshot> future = App.fstore.collection("References").get();
        List<QueryDocumentSnapshot> documents;
        try {
            documents = future.get().getDocuments();
            if (!documents.isEmpty()) {
                // Iterate to find the document that matches the selected Person.
                for (QueryDocumentSnapshot document : documents) {
                    if (document.getData().get("User").equals(currUser) &&
                            document.getData().get("Name").equals(p.getName()) &&
                            document.getData().get("Major").equals(p.getMajor()) &&
                            Integer.parseInt(document.getData().get("Age").toString()) == p.getAge()) {

                        // Delete the old document.
                        DocumentReference docRef = App.fstore.collection("References").document(document.getId());
                        docRef.delete();
                        System.out.println("Old record deleted for editing.");
                        break; // Exit the loop once the document is found and deleted.
                    }
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }

        // --- Update the local ObservableList ---
        int c = data.indexOf(p);
        Person p2 = new Person(name.getText(), major.getText(), Integer.parseInt(age.getText()));
        data.remove(c);
        data.add(c, p2);

        // --- Add the updated record as a new document to Firestore ---
        // This creates a new document instead of updating the old one.
        DocumentReference docRef = App.fstore.collection("References").document(UUID.randomUUID().toString());
        Map<String, Object> datas = new HashMap<>();
        datas.put("Name", name.getText());
        datas.put("Major", major.getText());
        datas.put("Age", Integer.parseInt(age.getText()));
        datas.put("User", currUser);
        ApiFuture<WriteResult> result = docRef.set(datas);

        tv.getSelectionModel().select(c); // Re-select the edited item in the TableView.

        // This method should be improved to perform a direct update to avoid a read and delete.
    }

    /**
     * Deletes a selected record from the TableView and Firestore.
     * This method is inefficient as it reads all documents from the database to find the one to delete.
     */
    @FXML
    protected void deleteRecord() {
        // Get the selected Person from the TableView.
        Person p = tv.getSelectionModel().getSelectedItem();

        if (p == null) {
            System.out.println("Please select a record to delete.");
            return;
        }

        // --- INEFFICIENCY WARNING ---
        // This method also fetches ALL documents just to find one ID for deletion.
        // It should be optimized by storing the document ID in the Person object.
        ApiFuture<QuerySnapshot> future = App.fstore.collection("References").get();
        List<QueryDocumentSnapshot> documents;
        try {
            documents = future.get().getDocuments();

            if (!documents.isEmpty()) {
                System.out.println("Searching for record to delete...");
                for (QueryDocumentSnapshot document : documents) {
                    // Find the matching document based on user, name, major, and age.
                    if (document.getData().get("User").equals(currUser) &&
                            document.getData().get("Name").equals(p.getName()) &&
                            document.getData().get("Major").equals(p.getMajor()) &&
                            Integer.parseInt(document.getData().get("Age").toString()) == p.getAge()) {

                        // Delete the document from Firestore.
                        DocumentReference docRef = App.fstore.collection("References").document(document.getId());
                        docRef.delete();
                        System.out.println("Record deleted from Firestore.");
                        break; // Exit the loop.
                    }
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }

        // Remove the selected item from the local ObservableList to update the UI.
        data.remove(p);
    }

    /**
     * Fills the input form fields with the data from the selected row in the TableView.
     *
     * @param mouseEvent The MouseEvent that triggered this method (e.g., a mouse click on a row).
     */
    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            // Set the TextField values from the selected Person object.
            name.setText(p.getName());
            age.setText(String.valueOf(p.getAge()));
            major.setText(p.getMajor());
        }
    }

    /**
     * Closes the application when the close button is clicked.
     */
    @FXML
    protected void close() {
        System.exit(0);
    }

    /**
     * Opens a file chooser dialog to select an image and displays it in the ImageView.
     */
    @FXML
    protected void showImage() {
        // Create a new FileChooser dialog.
        FileChooser fileChooser = new FileChooser();
        // Show the dialog and get the selected file.
        File file= fileChooser.showOpenDialog(img_view.getScene().getWindow());

        if(file != null){
            // If a file is selected, set the image in the ImageView from its URI.
            img_view.setImage(new Image(file.toURI().toString()));
        }
    }
}