package com.marginfresh.Model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by bitware on 10/10/17.
 */

public class ModelProductsInCart {

    public String productId,productQty,productSku;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductQty() {
        return productQty;
    }

    public void setProductQty(String productQty) {
        this.productQty = productQty;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

}
