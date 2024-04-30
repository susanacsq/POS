package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class DBUtils {

    public static void changeScene(ActionEvent event, String fxmlFile, String title, String username, String firstName, String lastName, String email) {
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(DBUtils.class.getResource(fxmlFile));
        try {
            root = loader.load();
            Object controller = loader.getController();

            // Handle different controller types using instanceof checks
            if (controller instanceof LoggedInController) {
                LoggedInController loggedInController = (LoggedInController) controller;
                loggedInController.setUserInformation(firstName, lastName, email, username);
            } else if (controller instanceof AdminController) {
                AdminController adminController = (AdminController) controller;
                adminController.setUserInformation(firstName, lastName, email, username);
            } else {
                // Handle other controller types here if needed
                System.out.println("Unhandled controller type: " + controller.getClass().getName());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set up the scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(new Scene(root, 600, 400));
        stage.show();
    }


    public static void signUpUser(ActionEvent event, String firstName, String lastName, String email, String mobile, String username, String password) {
        Connection connection = null;
        PreparedStatement psInsert = null;
        PreparedStatement psCheckUserExists = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx", "root", "toor");
            psCheckUserExists = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            psCheckUserExists.setString(1, username);
            resultSet = psCheckUserExists.executeQuery();

            if (resultSet.isBeforeFirst()) {
                System.out.println("User already exists!");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("You cannot use this username.");
                alert.show();
            } else {
                psInsert = connection.prepareStatement("INSERT INTO users (firstName, lastName, email, mobile, username, password) VALUES (?, ?, ?, ?, ?, ?)");
                psInsert.setString(1, firstName);
                psInsert.setString(2, lastName);
                psInsert.setString(3, email);
                psInsert.setString(4, mobile);
                psInsert.setString(5, username);
                psInsert.setString(6, password);
                psInsert.executeUpdate();

                changeScene(event, "logged-in.fxml", "Menu", username, firstName, lastName, email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (psCheckUserExists != null) {
                try {
                    psCheckUserExists.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void logInUser(ActionEvent event, String username, String password) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx", "root", "toor");
            preparedStatement = connection.prepareStatement("SELECT password, firstName, lastName, email FROM users WHERE username = ?");
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                System.out.println("User not found in the database!");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("User not found in the database!");
                alert.show();
            } else {
                while (resultSet.next()) {
                    String retrievedPassword = resultSet.getString("password");
                    String firstName = resultSet.getString("firstName");
                    String lastName = resultSet.getString("lastName");
                    String email = resultSet.getString("email");

                    if (retrievedPassword.equals(password)) {
                        // Show choice dialog
                        showChoiceDialog(event, username, firstName, lastName, email);
                    } else {
                        System.out.println("Password did not match!");
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("The provided credentials are incorrect!");
                        alert.show();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(resultSet, preparedStatement, connection);
        }
    }

    private static void showChoiceDialog(ActionEvent event, String username, String firstName, String lastName, String email) {
        Alert choiceDialog = new Alert(Alert.AlertType.CONFIRMATION);
        choiceDialog.setTitle("Choose Role");
        choiceDialog.setHeaderText(null);
        choiceDialog.setContentText("Please select your role:");

        ButtonType userButton = new ButtonType("User", ButtonData.OK_DONE);
        ButtonType adminButton = new ButtonType("Administrator", ButtonData.OTHER);
        choiceDialog.getButtonTypes().setAll(userButton, adminButton);

        Optional<ButtonType> result = choiceDialog.showAndWait();
        if (result.isPresent()) {
            if (result.get() == userButton) {
                // Change scene to logged-in.fxml (User view)
                changeScene(event, "logged-in.fxml", "Menu", username, firstName, lastName, email);
            } else if (result.get() == adminButton) {
                // Change scene to admin.fxml (Admin view)
                changeScene(event, "admin.fxml", "Admin Panel", username, firstName, lastName, email);
            }
        }
    }

    private static void closeResources(ResultSet resultSet, PreparedStatement preparedStatement, Connection connection) {
        // Closing resources
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}