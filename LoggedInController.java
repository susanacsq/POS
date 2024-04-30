package sample;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.security.spec.ECField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

import static sample.data.cID;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;

public class LoggedInController implements Initializable {

    @FXML
    private Button button_sign_out;
    @FXML
    private Button button_cart;
    @FXML
    private Button button_burger;
    @FXML
    private Button button_pizza;
    @FXML
    private Button button_sides;
    @FXML
    private Button button_beverage;
    @FXML
    private Button button_pay;
    @FXML
    private AnchorPane pane_cart;
    @FXML
    private AnchorPane pane_burger;
    @FXML
    private AnchorPane pane_pizza;
    @FXML
    private AnchorPane pane_sides;
    @FXML
    private AnchorPane pane_beverage;
    @FXML
    private GridPane gp_burger;
    @FXML
    private GridPane gp_pizza;
    @FXML
    private GridPane gp_sides;
    @FXML
    private GridPane gp_beverage;
    @FXML
    private TableView<CartItem> cartTable;
    @FXML
    private TableColumn<CartItem, String> table_name;
    @FXML
    private TableColumn<CartItem, Integer> table_quantity;
    @FXML
    private TableColumn<CartItem, Double> table_price;
    @FXML
    private TableColumn<CartItem, Double> table_totalprice;
    @FXML
    private Label tf_total;

    private ObservableList<productData> cardListData = FXCollections.observableArrayList();

    private static LoggedInController instance;
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    private double totalP;

    public LoggedInController() {
        instance = this;
    }

    public void initialize(URL location, ResourceBundle resources) {
        populateTableView();
        connect = database.connectDB();
        instance = this;

        button_sign_out.setOnAction(event -> DBUtils.changeScene(event, "sample.fxml", "Log in!", null, null, null, null));

        button_cart.setOnAction(event -> {
            switchToPane(pane_cart);
        });

        button_burger.setOnAction(event -> {
            switchToPane(pane_burger);
            handleCategorySelection("burger");
        });

        button_pizza.setOnAction(event -> {
            switchToPane(pane_pizza);
            handleCategorySelection("pizza");
        });

        button_sides.setOnAction(event -> {
            switchToPane(pane_sides);
            handleCategorySelection("sides");
        });

        button_beverage.setOnAction(event -> {
            switchToPane(pane_beverage);
            handleCategorySelection("beverage");
        });

        button_pay.setOnAction(event -> {
            String insertPay = "INSERT INTO receipt (customer_id, total, date, em_username)" + "VALUES(?,?,?,?)";
            connect = database.connectDB();

            try{
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Message");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure?");
                Optional<ButtonType> option = alert.showAndWait();

                if (option.get().equals(ButtonType.OK)) {
                    customerID();
                    prepare = connect.prepareStatement(insertPay);
                    prepare.setString(1, String.valueOf(cID));
                    prepare.setString(2, String.valueOf(totalP));
                    Date date = new Date();
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    prepare.setString(3, String.valueOf(sqlDate));
                    prepare.setString(4, data.username);

                    prepare.executeUpdate();
                }else {
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Cancelled.");
                    alert.showAndWait();
                }

            }catch (Exception e){e.printStackTrace();}

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Payment Successful");
            alert.setHeaderText(null);
            alert.setContentText("Paid successfully!");

            ButtonType okButton = new ButtonType("OK");
            ButtonType printReceiptButton = new ButtonType("Print Receipt");

            alert.getButtonTypes().setAll(okButton, printReceiptButton);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == okButton) {
                    System.out.println("User clicked OK");
                    alert.close();
                    instance.cartTable.getItems().clear();
                    totalP = 0;
                    updateTotalAmount();
                } else if (result.get() == printReceiptButton) {
                    System.out.println("User clicked Print Receipt");
                    customerID();
                    HashMap map = new HashMap<>();
                    map.put("getReceipt", cID);
                    try {
                        JasperDesign jDesign = JRXmlLoader.load("C:\\Users\\Amily\\IdeaProjects\\POS\\src\\main\\resources\\sample\\report.jrxml");
                        JasperReport jReport = JasperCompileManager.compileReport(jDesign);
                        JasperPrint jPrint = JasperFillManager.fillReport(jReport, map, connect);

                        JasperViewer.viewReport(jPrint, false);

                        instance.cartTable.getItems().clear();
                        totalP = 0;
                        updateTotalAmount();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        table_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        table_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        table_price.setCellValueFactory(new PropertyValueFactory<>("price"));
        table_totalprice.setCellValueFactory(new PropertyValueFactory<>("total"));

        switchToPane(pane_burger);
        handleCategorySelection("burger");

        updateTotalAmount();
    }

    private void updateTotalAmount() {
        double totalAmount = 0.0;
        for (CartItem item : cartTable.getItems()) {
            totalAmount += item.getTotal();
        }
        tf_total.setText("RM " + String.format("%.2f", totalAmount));
    }

    private void switchToPane(AnchorPane selectedPane) {
        pane_cart.setVisible(false);
        pane_burger.setVisible(false);
        pane_pizza.setVisible(false);
        pane_sides.setVisible(false);
        pane_beverage.setVisible(false);

        selectedPane.setVisible(true);
    }

    public ObservableList<productData> menuGetData() {
        String sql = "SELECT * FROM product";

        ObservableList<productData> cardListData = FXCollections.observableArrayList();
        try (
                Connection connect = database.connectDB();
                PreparedStatement prepare = connect.prepareStatement(sql);
                ResultSet result = prepare.executeQuery()
        ) {
            while (result.next()) {
                productData prod = new productData(result.getInt("id"), result.getString("prod_id"), result.getString("name"), result.getInt("stock"), result.getDouble("price"), result.getString("image"), result.getDate("date"), result.getString("category"));
                cardListData.add(prod);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cardListData;
    }

    public void menuDisplayCard(GridPane gridPane, String selectedCategory) {
        cardListData.clear();
        cardListData.addAll(menuGetData());

        gridPane.getChildren().clear();

        int columnCount = 3;
        int columnIndex = 0;
        int rowIndex = 0;

        for (productData data : cardListData) {
            if (data.getCategory() != null && data.getCategory().equalsIgnoreCase(selectedCategory)) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("cardProduct.fxml"));
                    AnchorPane pane = loader.load();
                    cardProductController cardController = loader.getController();
                    cardController.setData(data);
                    cardController.setCustomerID(cID);

                    gridPane.add(pane, columnIndex, rowIndex);

                    columnIndex++;

                    if (columnIndex >= columnCount) {
                        columnIndex = 0;
                        rowIndex++;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void handleCategorySelection(String selectedCategory) {
        switch (selectedCategory) {
            case "burger":
                menuDisplayCard(gp_burger, selectedCategory);
                break;
            case "pizza":
                menuDisplayCard(gp_pizza, selectedCategory);
                break;
            case "sides":
                menuDisplayCard(gp_sides, selectedCategory);
                break;
            case "beverage":
                menuDisplayCard(gp_beverage, selectedCategory);
                break;
            default:
                break;
        }
    }

    public class CustomerData {
        private final StringProperty productName;
        private final IntegerProperty quantity;
        private final DoubleProperty price;
        private final DoubleProperty totalPrice;

        public CustomerData(String productName, int quantity, double price, double totalPrice) {
            this.productName = new SimpleStringProperty(productName);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.price = new SimpleDoubleProperty(price);
            this.totalPrice = new SimpleDoubleProperty(totalPrice);
        }

        public String getProductName() {
            return productName.get();
        }

        public void setProductName(String productName) {
            this.productName.set(productName);
        }

        public StringProperty productNameProperty() {
            return productName;
        }

        public int getQuantity() {
            return quantity.get();
        }

        public void setQuantity(int quantity) {
            this.quantity.set(quantity);
        }

        public IntegerProperty quantityProperty() {
            return quantity;
        }

        public double getPrice() {
            return price.get();
        }

        public void setPrice(double price) {
            this.price.set(price);
        }

        public DoubleProperty priceProperty() {
            return price;
        }

        public double getTotalPrice() {
            return totalPrice.get();
        }

        public void setTotalPrice(double totalPrice) {
            this.totalPrice.set(totalPrice);
        }

        public DoubleProperty totalPriceProperty() {
            return totalPrice;
        }
    }


    public ObservableList<CartItem> fetchCustomerData() {
        customerID();
        ObservableList<CartItem> customerDataList = FXCollections.observableArrayList();

        // Check if the user is new
        if (isNewUser()) {
            // If the user is new, return an empty list
            return customerDataList;
        }

        String sql = "SELECT prod_name, quantity, price FROM customer WHERE customer_id = ?";

        try (Connection connection = database.connectDB();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, String.valueOf(cID));
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String prodName = resultSet.getString("prod_name");
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price");
                double totalPrice = quantity * price;

                CartItem cartItem = new CartItem(prodName, quantity, price, totalPrice);
                customerDataList.add(cartItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customerDataList;
    }

    // Method to check if the user is new
    private boolean isNewUser() {
        // Implement logic to check if the user is new, for example, check if the cart is empty
        return instance.cartTable.getItems().isEmpty();
    }


    public void populateTableView() {
        ObservableList<CartItem> customerDataList = fetchCustomerData();
        cartTable.setItems(customerDataList); // Set items list to retrieved data

        // Refresh table view
        cartTable.refresh();
    }


    public static void addToCart(Label itemName, int quantity, Label price) {
        if (instance != null) {
            String itemNameText = itemName.getText();
            double priceValue = Double.parseDouble(price.getText());
            double total = quantity * priceValue;

            boolean itemFound = false;
            for (CartItem item : instance.cartTable.getItems()) {
                if (item.getName().equals(itemNameText)) {
                    item.setQuantity(item.getQuantity() + quantity);
                    item.setTotal(item.getTotal() + total);
                    itemFound = true;
                    break;
                }
            }

            if (!itemFound) {
                CartItem item = new CartItem(itemNameText, quantity, priceValue, total);
                instance.cartTable.getItems().add(item);
            }

            // Update total amount
            instance.updateTotalAmount();

            // Refresh cart table view
            instance.populateTableView();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Item added to cart successfully.");
            alert.showAndWait();
        }
    }

    public static class CartItem {
        private final StringProperty name;
        private final IntegerProperty quantity;
        private final DoubleProperty price;
        private final DoubleProperty total;

        public DoubleProperty priceProperty() {
            return price;
        }

        public CartItem(String name, int quantity, double price, double total) {
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.price = new SimpleDoubleProperty(price);
            this.total = new SimpleDoubleProperty(total);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public StringProperty nameProperty() {
            return name;
        }

        public int getQuantity() {
            return quantity.get();
        }

        public void setQuantity(int quantity) {
            this.quantity.set(quantity);
        }

        public IntegerProperty quantityProperty() {
            return quantity;
        }

        public double getTotal() {
            return total.get();
        }

        public void setTotal(double total) {
            this.total.set(total);
        }

        public DoubleProperty totalProperty() {
            return total;
        }
    }

    public void setUserInformation(String firstName, String lastName, String email, String username) {
    }

    private int cID;

    public void customerID() {
        String sql = "SELECT MAX(customer_id) AS max_id FROM customer";
        connect = database.connectDB();

        try {
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            if (result.next()) {
                cID = result.getInt("max_id");
            }

            String checkCID = "SELECT MAX(customer_id) AS max_id FROM receipt";
            prepare = connect.prepareStatement(checkCID);
            result = prepare.executeQuery();
            int checkID = 0;
            if (result.next()) {
                checkID = result.getInt("max_id");
            }

            if (cID == 0) {
                cID += 1;
            } else if (cID == checkID) {
                cID += 1;
            }

            data.cID = cID;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
