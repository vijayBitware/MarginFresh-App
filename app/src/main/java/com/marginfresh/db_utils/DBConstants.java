package com.marginfresh.db_utils;

/**
 * Created by bitware on 9/10/17.
 */

public class DBConstants {

    // Database Version
    public static final int DATABASE_VERSION = 1;
    // Database Name
    public static final String DATABASE_NAME = "marginfresh_db";

    //table name for cart count
    public static final String TABLE_USER_CART_COUNT = "user_cart_table_count";

    //colums names for user_cart table
    public static final String KEY_CART_COUNT = "user_cart_count";

    //creating user cart count table
    public static final String CREATE_USER_CART_COUNT_TABLE = "CREATE TABLE "
            + DBConstants.TABLE_USER_CART_COUNT + "("
            + DBConstants.KEY_CART_COUNT + " INTEGER"
            + ")";
    /*------------------for products in cart ---------------*/

    // /table for cart products
    public static final String TABLE_USER_CART = "user_cart_table";

    //columns for user cart table
    public static final String USER_ID = "user_id";
    public static final String PRODUCT_ID = "product_id";
    public static final String PRODUCT_SKU = "productSku";
    public static final String PRODUCT_NAME = "product_name";
    public static final String PRODUCT_IMAGE = "product_image_url";
    public static final String PRODUCT_PRICE = "price";
    public static final String PRODUCT_NEW_PRICE = "new_price";
    public static final String PRODUCT_PRICE_FOR_CALCULATION = "product_price_for_calculation";
    public static final String PRODUCT_INSTOCK = "product_is_in_stock";
    public static final String PRODUCT_RATING_COUNT = "product_rating_count";
    public static final String PRODUCT_INWISHLIST = "product_isInWislist";
    public static final String PRODUCT_QTY ="product_qty";

    //creating user car table
    public static final String CREATE_USER_CART_TABLE = "CREATE TABLE "
            +DBConstants.TABLE_USER_CART + "("
            +DBConstants.USER_ID + " TEXT,"
            +DBConstants.PRODUCT_ID + " TEXT,"
            +DBConstants.PRODUCT_SKU + " TEXT,"
            +DBConstants.PRODUCT_NAME + " TEXT,"
            +DBConstants.PRODUCT_IMAGE + " TEXT,"
            +DBConstants.PRODUCT_PRICE + " TEXT,"
            +DBConstants.PRODUCT_NEW_PRICE + " TEXT,"
            +DBConstants.PRODUCT_PRICE_FOR_CALCULATION + " TEXT,"
            +DBConstants.PRODUCT_INSTOCK + " TEXT,"
            +DBConstants.PRODUCT_RATING_COUNT + " TEXT,"
            +DBConstants.PRODUCT_INWISHLIST + " TEXT,"
            +DBConstants.PRODUCT_QTY + " TEXT"
            + ")";

    /*---------For cart total-------*/
    public static final String CART_TOTAL = "cart_total";
    public static final String CART_TOTAL_ID = "cart_total_id";

    public static final String TABLE_CART_TOTAL = "cart_total_table";

    //creating cart total table
    public static final String CREATE_CART_TOTAL_TABLE = "CREATE TABLE "
            +DBConstants.TABLE_CART_TOTAL + "("
            +DBConstants.USER_ID + " TEXT,"
            +DBConstants.CART_TOTAL + " TEXT"
            + ")";
}
