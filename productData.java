package sample;

import java.util.Date;

public class productData {

    private Integer id;
    private String productID;
    private String name;
    private Integer stock;
    private Double price;
    private String image;
    private Date date;
    private String category;
    private String username;

    public productData(Integer id, String productID, String name, int stock, Double price, String image, java.sql.Date date, String category) {
        this.id = id;
        this.productID = productID;
        this.name = name;
        this.stock = stock;
        this.price = price;
        this.image = image;
        this.date = date;
        this.category = category;
        this.username = username;
    }


    public productData(Integer id, String productID, String name, Double price, String image) {
        this.id = id;
        this.productID = productID;
        this.name = name;
        this.price = price;
        this.image = image;
    }

    public Integer getId() {
        return id;
    }


    public String getProductID() {
        return productID;
    }

    public String getName() {
        return name;
    }

    public Integer getStock() {
        return stock;
    }

    public Double getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public Date getDate() {
        return date;
    }

    public String getCategory() {
        return category != null ? category : "";
    }

    public String getUsername() {
        return username;
    }
}
