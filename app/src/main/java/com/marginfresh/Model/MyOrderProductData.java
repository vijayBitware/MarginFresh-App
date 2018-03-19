package com.marginfresh.Model;

import java.io.Serializable;

/**
 * Created by bitware on 31/7/17.
 */

public class MyOrderProductData implements Serializable{

    public String product_id,name,imageUrl,qty_ordered,price,row_total,amount_refunded;

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getQty_ordered() {
        return qty_ordered;
    }

    public void setQty_ordered(String qty_ordered) {
        this.qty_ordered = qty_ordered;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRow_total() {
        return row_total;
    }

    public void setRow_total(String row_total) {
        this.row_total = row_total;
    }

    public String getAmount_refunded() {
        return amount_refunded;
    }

    public void setAmount_refunded(String amount_refunded) {
        this.amount_refunded = amount_refunded;
    }
}
