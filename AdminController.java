package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML
    private Button button_db;

    @FXML
    private Button button_inventory;

    @FXML
    private Button button_customers;

    @FXML
    private Button button_sign_out;

    @FXML
    private AnchorPane pane_db;

    @FXML
    private AnchorPane pane_inventory;

    @FXML
    private AnchorPane pane_customers;

    @FXML
    private TableView<productData> table_inventory;

    @FXML
    private TableColumn<productData, String> table_productID;

    @FXML
    private TableColumn<productData, String> table_name;

    @FXML
    private TableColumn<productData, Integer> table_stock;

    @FXML
    private TableColumn<productData, Double> table_price;

    @FXML
    private TableColumn<productData, Date> table_date;

    @FXML
    private TextField tf_productID;

    @FXML
    private TextField tf_product_name;

    @FXML
    private Label db_NC;

    @FXML
    private Label db_NPS;

    @FXML
    private Button button_Add;

    @FXML
    private Button button_update;

    @FXML
    private Button button_delete;

    @FXML
    private Button button_clear;

    @FXML
    private TextField tf_stock;

    @FXML
    private TextField tf_price;

    @FXML
    private ComboBox<String> cb_category;

    @FXML
    private AnchorPane AP_image;

    @FXML
    private AnchorPane management_system;

    @FXML
    private ImageView image_import;

    @FXML
    private Button button_import;

    @FXML
    private TableView<customersData> tableView_customers;

    @FXML
    private TableColumn<customersData, String> col_customerID;

    @FXML
    private TableColumn<customersData, String> col_total;

    @FXML
    private TableColumn<customersData, String> col_date;

    @FXML
    private TableColumn<customersData, String> col_cashier;

    @FXML
    private AreaChart<?,?> db_IC;

    private Alert alert;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    private Image image;

    private LoggedInController loggedInController;

    public void dashboardDisplayNC() {

        String sql = "SELECT COUNT(id) FROM receipt";

        connect = database.connectDB();

        try {
            int nc = 0;
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            if(result.next()) {
                nc = result.getInt("COUNT(id)");
            }
            db_NC.setText(String.valueOf(nc));

        }catch (Exception e) {e.printStackTrace();}
    }

    public void dashboardDisplayNPS(){
        String sql = "SELECT COUNT(quantity) FROM customer";

        connect = database.connectDB();

        try {
            int q = 0;
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            if (result.next()) {
                q = result.getInt("COUNT(quantity)");
            }
            db_NPS.setText(String.valueOf(q));
        }catch (Exception e) {e.printStackTrace();}
    }

    public void dashboardIncomeChart(){
        db_IC.getData().clear();
        String sql = "SELECT date, SUM(total) FROM receipt GROUP BY date ORDER BY TIMESTAMP(date)";

        connect = database.connectDB();
        XYChart.Series chart = new XYChart.Series();

        try {
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            while (result.next()) {
                chart.getData().add(new XYChart.Data<>(result.getString(1), result.getFloat(2)));
            }

            db_IC.getData().add(chart);

        }catch (Exception e) {e.printStackTrace();}
    }

    public void setLoggedInController(LoggedInController loggedInController) {
        this.loggedInController = loggedInController;
    }

    public void onCategorySelected() {
        String selectedCategory = cb_category.getValue();
        if (loggedInController != null) {
            loggedInController.handleCategorySelection(selectedCategory);
        } else {
            System.out.println("Error: loggedInController is null");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        button_db.setOnAction(event -> showDashboard());
        button_inventory.setOnAction(event -> showInventory());
        button_customers.setOnAction(event -> showCustomers());
        button_sign_out.setOnAction(event -> DBUtils.changeScene(event, "sample.fxml", "Log in!", null, null, null, null));

        table_productID.setCellValueFactory(new PropertyValueFactory<>("productID"));
        table_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        table_stock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        table_price.setCellValueFactory(new PropertyValueFactory<>("price"));
        table_date.setCellValueFactory(new PropertyValueFactory<>("date"));

        ObservableList<String> categories = FXCollections.observableArrayList("burger", "pizza", "sides", "beverage");
        cb_category.setItems(categories);

        inventoryShowData();
        inventoryClearBtn();
        dashboardDisplayNC();
        dashboardDisplayNPS();
        dashboardIncomeChart();
    }

    public void inventoryAddBtn() {
        if (tf_productID.getText().isEmpty() || tf_product_name.getText().isEmpty() || tf_stock.getText().isEmpty() || tf_price.getText().isEmpty() || data.path == null) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill in all blank fields");
        } else {
            String checkProdID = "SELECT prod_id FROM product WHERE prod_id = ?";

            try {
                connect = database.connectDB();
                prepare = connect.prepareStatement(checkProdID);
                prepare.setString(1, tf_productID.getText());
                result = prepare.executeQuery();

                if (result.next()) {
                    showAlert(Alert.AlertType.ERROR, "Error Message", tf_productID.getText() + " is already taken");
                } else {
                    String insertData = "INSERT INTO product (prod_id, name, stock, price, image, date, category) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    prepare = connect.prepareStatement(insertData);
                    prepare.setString(1, tf_productID.getText());
                    prepare.setString(2, tf_product_name.getText());
                    prepare.setString(3, tf_stock.getText());
                    prepare.setString(4, tf_price.getText());
                    prepare.setString(5, data.path.replace("\\", "\\\\"));
                    prepare.setDate(6, new java.sql.Date(new Date().getTime()));
                    prepare.setString(7, cb_category.getValue());
                    prepare.executeUpdate();

                    inventoryShowData();
                    showAlert(Alert.AlertType.INFORMATION, "Information", "Successfully Added!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void inventoryUpdateBtn() {
        if (tf_productID.getText().isEmpty() || tf_product_name.getText().isEmpty() || tf_stock.getText().isEmpty() || tf_price.getText().isEmpty() || data.path == null || data.id == 0) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill in all blank fields");
        } else {
            String path = data.path != null ? data.path.replace("\\", "\\\\") : null;
            String updateData = "UPDATE product SET prod_id = ?, name = ?, stock = ?, price = ?, image = ?, date = ?, category = ? WHERE id = ?";

            connect = database.connectDB();

            try {
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to update product ID: " + tf_productID.getText() + "?");
                Optional<ButtonType> option = alert.showAndWait();

                if (option.isPresent() && option.get() == ButtonType.OK) {
                    prepare = connect.prepareStatement(updateData);
                    prepare.setString(1, tf_productID.getText());
                    prepare.setString(2, tf_product_name.getText());
                    prepare.setString(3, tf_stock.getText());
                    prepare.setString(4, tf_price.getText());
                    prepare.setString(5, path);
                    prepare.setDate(6, new java.sql.Date(new Date().getTime()));
                    prepare.setString(7, cb_category.getValue());
                    prepare.setInt(8, data.id);
                    prepare.executeUpdate();

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
                    inventoryShowData();
                    updateImageView(path);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error Message", "Cancelled.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateImageView(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                image_import.setImage(new Image(file.toURI().toString()));
            }
        }
    }

    public void inventoryDeleteBtn() {

        if (tf_productID.getText().isEmpty() || tf_product_name.getText().isEmpty() || tf_stock.getText().isEmpty() || tf_price.getText().isEmpty() || data.path == null || data.id == 0) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill in all blank fields");
        } else {
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete product ID: " + tf_productID.getText() + "?");
            Optional<ButtonType> option = alert.showAndWait();

            if (option.isPresent() && option.get() == ButtonType.OK) {
                String deleteData = "DELETE FROM product WHERE id = ?";
                try {
                    prepare = connect.prepareStatement(deleteData);
                    prepare.setInt(1, data.id);
                    prepare.executeUpdate();

                    showAlert(Alert.AlertType.ERROR, "Error Message", "Successfully Deleted!");

                    inventoryShowData();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error Message", "Cancelled");
            }
        }
    }

    public void inventoryImportBtn() {
        FileChooser openFile = new FileChooser();
        openFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("Open Image File", "*png", "*jpg"));
        File file = openFile.showOpenDialog(management_system.getScene().getWindow());

        if (file != null) {
            data.path = file.getAbsolutePath();
            image = new Image(file.toURI().toString(), 116, 112, false, true);
            image_import.setImage(image);
        }
    }

    private Optional<ButtonType> showAlert(Alert.AlertType alertType, String title, String content) {
        alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
        return null;
    }

    public void inventoryClearBtn() {
        tf_productID.setText("");
        tf_product_name.setText("");
        tf_stock.setText("");
        tf_price.setText("");

        cb_category.setValue(null);
        image_import.setImage(null);
    }

    private void inventoryShowData() {
        ObservableList<productData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM product";

        try {
            connect = database.connectDB();
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            while (result.next()) {
                listData.add(new productData(
                        result.getInt("id"),
                        result.getString("prod_id"),
                        result.getString("name"),
                        result.getInt("stock"),
                        result.getDouble("price"),
                        result.getString("image"),
                        result.getDate("date"),
                        result.getString("category")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        table_inventory.setItems(listData);
    }

    public void inventorySelectData() {

        productData prodData = table_inventory.getSelectionModel().getSelectedItem();
        int num = table_inventory.getSelectionModel().getSelectedIndex();

        if ((num - 1) < -1) return;

        tf_productID.setText(prodData.getProductID());
        tf_product_name.setText(prodData.getName());
        tf_stock.setText(String.valueOf(prodData.getStock()));
        tf_price.setText(String.valueOf(prodData.getPrice()));

        cb_category.setValue(prodData.getCategory());

        data.path = prodData.getImage();

        String path = "File:" + prodData.getImage();
        data.date = String.valueOf(prodData.getDate());
        data.id = prodData.getId();

        image = new Image(path, 116, 112, false, true);
        image_import.setImage(image);

    }

    public ObservableList<customersData> customersDataList () {

        ObservableList<customersData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM receipt";
        connect = database.connectDB();

        try {

            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            customersData cData;

            while (result.next())
                cData = new customersData(result.getInt("id"), result.getInt("customer_id"), result.getDouble("total"), result.getDate("date"), result.getString("emUsername"));
        } catch (Exception e) {e.printStackTrace();}
        return listData;
    }

    private void showDashboard() {
        pane_inventory.setVisible(false);
        pane_db.setVisible(true);
        pane_customers.setVisible(false);

        dashboardDisplayNC();
        dashboardDisplayNPS();
    }

    private void showInventory() {
        pane_db.setVisible(false);
        pane_inventory.setVisible(true);
        pane_customers.setVisible(false);
    }

    private void showCustomers() {
        pane_db.setVisible(false);
        pane_inventory.setVisible(false);
        pane_customers.setVisible(true);
    }



    public void setUserInformation(String firstName, String lastName, String email, String username) {
    }
}
