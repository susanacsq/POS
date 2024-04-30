package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {

    @FXML
    private Button button_signup;
    @FXML
    private Button button_log_in;

    @FXML
    private TextField tf_password;
    @FXML
    private TextField tf_username;
    @FXML
    private TextField tf_mobile;
    @FXML
    private TextField tf_email;
    @FXML
    private TextField tf_firstname;
    @FXML
    private TextField tf_lastname;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        button_signup.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                if (!tf_username.getText().trim().isEmpty() && !tf_password.getText().trim().isEmpty() && !tf_firstname.getText().trim().isEmpty() && !tf_lastname.getText().trim().isEmpty() && !tf_email.getText().trim().isEmpty() && !tf_mobile.getText().trim().isEmpty()) {
                    DBUtils.signUpUser(event, tf_firstname.getText(), tf_lastname.getText(), tf_email.getText(), tf_mobile.getText(), tf_username.getText(), tf_password.getText());
                } else {
                    System.out.println("Please fill in all information");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Please fill in all information to sign up!");
                    alert.show();
                }
            }
        });

        button_log_in.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DBUtils.changeScene(event, "sample.fxml", "Log in", null, null, null, null);
            }
        });

    }
}
