package com.marginfresh.Model;

import java.util.ArrayList;

/**
 * Created by bitware on 1/6/17.
 */

public class ModelFoodCategoryList {

    public  String categoryName;
    public ArrayList<ModelCategoryListScroll> arrProductList;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public ArrayList<ModelCategoryListScroll> getArrProductList() {
        return arrProductList;
    }

    public void setArrProductList(ArrayList<ModelCategoryListScroll> arrProductList) {
        this.arrProductList = arrProductList;
    }
}
