package sample;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.Date;

public class cardProductController implements Initializable {

    @FXML
    private AnchorPane card_form;

    @FXML
    private Label prod_name;

    @FXML
    private Label prod_price;

    @FXML
    private ImageView prod_imageView;

    @FXML
    private Spinner<Integer> prod_spinner;

    @FXML
    private Button prod_addBtn;

    private productData prodData;
    private Image image;
    private int qty;
    private double pr;
    private double totalP;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private Alert alert;

    private String prodID;
    private String category;
    private String prod_image;
    private String prod_date;

    private int cID;

    public void setCustomerID(int cID) {
        this.cID = cID;
    }


    private SpinnerValueFactory<Integer> spin;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setQuantity();
    }

    private void setQuantity() {
        spin = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
        prod_spinner.setValueFactory(spin);
    }

    @FXML
    private void addBtn() {
        LoggedInController loggedInController = new LoggedInController();
        loggedInController.customerID();

        qty = prod_spinner.getValue();

        try {
            connect = database.connectDB();

            int checkStck = 0;
            String checkStock = "SELECT stock FROM product WHERE prod_id = ?";
            prepare = connect.prepareStatement(checkStock);
            prepare.setString(1, prodID);
            result = prepare.executeQuery();

            if (result.next()) {
                checkStck = result.getInt("stock");
            }

            if (checkStck < qty) {
                showAlert(Alert.AlertType.ERROR, "Error", "Out of Stock");
            } else {
                String insertData = "INSERT INTO customer (customer_id, prod_name, quantity, price, date, em_username) VALUES (?, ?, ?, ?, ?, ?)";
                prepare = connect.prepareStatement(insertData);
                prepare.setInt(1, data.cID);
                prepare.setString(2, prod_name.getText());
                prepare.setInt(3, qty);
                totalP = (qty * pr);
                prepare.setDouble(4, totalP);

                java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
                prepare.setDate(5, sqlDate);

                if (data.username != null) { // Check if data.username is not null
                    prepare.setString(6, data.username);
                } else {
                    // Handle the case when data.username is null
                    prepare.setString(6, ""); // Assuming em_username is of type VARCHAR
                }

                prepare.executeUpdate();


                showAlert(Alert.AlertType.INFORMATION, "Information", "Successfully Added!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    private void showAlert(Alert.AlertType alertType, String title, String content) {
        alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setData(productData prodData) {
        this.prodData = prodData;

        prod_image = prodData.getImage();
        prod_date = String.valueOf(prodData.getDate());
        category = prodData.getCategory();
        prodID = prodData.getProductID();
        prod_name.setText(prodData.getName());
        prod_price.setText(String.valueOf(prodData.getPrice()));
        String path = "File:" + prodData.getImage();
        image = new Image(path, 140, 120, false, true);
        prod_imageView.setImage(image);
        pr = prodData.getPrice();
    }

    @FXML
    private void handleAddButtonClick() {
        int quantity = prod_spinner.getValue();
        LoggedInController.addToCart(prod_name, quantity, prod_price);
    }
}
