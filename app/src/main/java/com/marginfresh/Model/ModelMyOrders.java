package com.marginfresh.Model;

import java.util.ArrayList;

/**
 * Created by bitware on 6/6/17.
 */

public class ModelMyOrders {

    public String orderStatus,order_id,created_at,total_qty_ordered,grand_total,payment_method,shippingAddress,reorder;
    public ArrayList<MyOrderProductData> arrProductData;

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getTotal_qty_ordered() {
        return total_qty_ordered;
    }

    public void setTotal_qty_ordered(String total_qty_ordered) {
        this.total_qty_ordered = total_qty_ordered;
    }

    public String getGrand_total() {
        return grand_total;
    }

    public void setGrand_total(String grand_total) {
        this.grand_total = grand_total;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public ArrayList<MyOrderProductData> getArrProductData() {
        return arrProductData;
    }

    public void setArrProductData(ArrayList<MyOrderProductData> arrProductData) {
        this.arrProductData = arrProductData;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getReorder() {
        return reorder;
    }

    public void setReorder(String reorder) {
        this.reorder = reorder;
    }
}
