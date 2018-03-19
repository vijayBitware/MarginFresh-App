package com.marginfresh.db_utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.marginfresh.Model.ModelCart;
import com.marginfresh.Model.ModelProductsInCart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bitware on 9/10/17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    SQLiteDatabase db;
    public DatabaseHandler(Context context) {
        super(context, DBConstants.DATABASE_NAME, null,
                DBConstants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("DatabaseHandler:", "Creating DB");
        db.execSQL(DBConstants.CREATE_USER_CART_TABLE);
        db.execSQL(DBConstants.CREATE_CART_TOTAL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("DatabaseHandler:", "DRopping Table in DB");
        db.execSQL("DROP TABLE IF EXISTS " + DBConstants.TABLE_USER_CART);
        db.execSQL("DROP TABLE IF EXISTS " + DBConstants.TABLE_CART_TOTAL);
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    public void insertProductToCart(String userId,String pId,String pSku,String pName,String pImage,String pInStock,String pratingCount,String pInWishlist,String pPriceCalculation,String pQty){
        db = this.getWritableDatabase();

        String sql =   "INSERT INTO "+DBConstants.TABLE_USER_CART + "( "+DBConstants.USER_ID +","
                +DBConstants.PRODUCT_ID +","
                +DBConstants.PRODUCT_SKU +","
                +DBConstants.PRODUCT_NAME +","
                +DBConstants.PRODUCT_IMAGE +","
                +DBConstants.PRODUCT_INSTOCK +","
                +DBConstants.PRODUCT_RATING_COUNT +","
                +DBConstants.PRODUCT_INWISHLIST +","
                +DBConstants.PRODUCT_PRICE_FOR_CALCULATION +","
                +DBConstants.PRODUCT_QTY
                +") VALUES(?,?,?,?,?,?,?,?,?,?)";

        SQLiteStatement insertStmt =   db.compileStatement(sql);
        insertStmt.bindString(1,userId);
        insertStmt.bindString(2,pId);
        insertStmt.bindString(3,pSku);
        insertStmt.bindString(4,pName);
        insertStmt.bindString(5,pImage);
        insertStmt.bindString(6,pInStock);
        insertStmt.bindString(7,pratingCount);
        insertStmt.bindString(8,pInWishlist);
        insertStmt.bindString(9,pPriceCalculation);
        insertStmt.bindString(10,pQty);
        insertStmt.executeInsert();
    }

    public ArrayList<ModelCart> getProductsInCart(String userId){
        db = this.getReadableDatabase();
        ArrayList<ModelCart> arrCart = new ArrayList();
        String selectQuery = "SELECT * FROM " +DBConstants.TABLE_USER_CART+ " WHERE "+DBConstants.USER_ID + "=" +userId;
        Log.e("selectQuery", selectQuery);
        Cursor cursor = db.rawQuery(selectQuery,null);
        if (cursor.moveToFirst()){
            do {
                ModelCart modelCart = new ModelCart();
                modelCart.setProduct_id(cursor.getString(cursor.getColumnIndex(DBConstants.PRODUCT_ID)));
                modelCart.setProduct_name(cursor.getString(cursor.getColumnIndex(DBConstants.PRODUCT_NAME)));
                modelCart.setProductPrice(cursor.getString(cursor.getColumnIndex(DBConstants.PRODUCT_PRICE_FOR_CALCULATION)));
                modelCart.setProductImageUrl(cursor.getString(cursor.getColumnIndex(DBConstants.PRODUCT_IMAGE)));
                modelCart.setProductSku(cursor.getString(cursor.getColumnIndex(DBConstants.PRODUCT_SKU)));
                modelCart.setQty_count(cursor.getString(cursor.getColumnIndex(DBConstants.PRODUCT_QTY)));
                arrCart.add(modelCart);
            } while (cursor.moveToNext());
        }
        return arrCart;
    }

    public String getProductsInCartCount(String userId) {
        String countQuery = "SELECT  * FROM " + DBConstants.TABLE_USER_CART+ " WHERE "+DBConstants.USER_ID + "=" +userId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return String.valueOf(cnt);
    }

    public void removeProductFromCart(String pId,String userId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DBConstants.TABLE_USER_CART,DBConstants.USER_ID + " = ? AND " + DBConstants.PRODUCT_ID + " = ?",new String[]{userId,pId});
    }

    public boolean isProductExitsInDb(String pId,String userId){
        Cursor cursor;
        SQLiteDatabase db = this.getWritableDatabase();
        String sql ="SELECT "+DBConstants.PRODUCT_ID+ " FROM "+DBConstants.TABLE_USER_CART+" WHERE "+DBConstants.USER_ID +"="+userId+ " AND "+ DBConstants.PRODUCT_ID+ "="+pId;
        cursor= db.rawQuery(sql,null);
        Log.e("Cursor Count : " , String.valueOf(cursor.getCount()));

        if(cursor.getCount()>0){
            Log.e("Product id > ","Found.! This product id exits in db");
            return true;
        }else{
            //PID Not Found
            Log.e("product id > ","Not Found");
        }
        return false;
    }
    public int getProductQty(String pId,String userId){
        int pQty=0;
        String selectQuery = "SELECT " +DBConstants.PRODUCT_QTY +" FROM " +DBConstants.TABLE_USER_CART
                +" WHERE " +DBConstants.PRODUCT_ID + "=" +pId+ " AND "+DBConstants.USER_ID + "=" +userId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()){
            do {
                pQty = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBConstants.PRODUCT_QTY)));
            }while (cursor.moveToNext());
        }
        return pQty;
    }

    public void updateProductQty(String pId,String pQTY,String userId){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.PRODUCT_QTY,pQTY);
        db.update(DBConstants.TABLE_USER_CART,cv,DBConstants.PRODUCT_ID + " = ? AND " + DBConstants.USER_ID + " = ?",new String[]{pId,userId});
    }

    public String getProductPrice(String pId,String userId){
        String pQty="";
        String selectQuery = "SELECT " +DBConstants.PRODUCT_PRICE_FOR_CALCULATION +" FROM " +DBConstants.TABLE_USER_CART
                +" WHERE " +DBConstants.PRODUCT_ID + "=" +pId+ " AND "+DBConstants.USER_ID + "=" +userId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()){
            do {
                pQty = cursor.getString(cursor.getColumnIndex(DBConstants.PRODUCT_PRICE_FOR_CALCULATION));
            }while (cursor.moveToNext());
        }
        return pQty;
    }

    public void updateProductPrice(String pPrice,String pId,String userId){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.PRODUCT_PRICE_FOR_CALCULATION,pPrice);
        db.update(DBConstants.TABLE_USER_CART,cv,DBConstants.PRODUCT_ID + " = ? AND " + DBConstants.USER_ID + " = ?",new String[]{pId, userId});
    }

    public boolean isCartTotalPresentInDB(String userId){
        Cursor cursor;
        SQLiteDatabase db = this.getWritableDatabase();
        String sql ="SELECT "+DBConstants.CART_TOTAL+ " FROM "+DBConstants.TABLE_CART_TOTAL +" WHERE " +DBConstants.USER_ID + "=" +userId;
        cursor= db.rawQuery(sql,null);
        Log.e("Cursor Count : " , String.valueOf(cursor.getCount()));

        if(cursor.getCount()>0){
            Log.e("TAG","Cart total present in db");
            return true;
        }else{
            //PID Not Found
            Log.e("TAG","Cart total not found");
        }
        return false;
    }

    public String getCartTotal(String userId){
        String carTotal="";
        String selectQuery = "SELECT " +DBConstants.CART_TOTAL +" FROM " +DBConstants.TABLE_CART_TOTAL +" WHERE " +DBConstants.USER_ID + "=" +userId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.e("TAG","Carsor count of cart total table> "+cursor.getCount());
        if (cursor.moveToFirst()){
            do {
                carTotal = cursor.getString(cursor.getColumnIndex(DBConstants.CART_TOTAL));
            }while (cursor.moveToNext());
        }
        return carTotal;
    }

    public void insertCartTotal(String cartTotal,String userId){
        db = this.getWritableDatabase();
        Log.e("DatabaseHandler","cart total is >" +cartTotal);
        String sql =   "INSERT INTO "+DBConstants.TABLE_CART_TOTAL + "( " +DBConstants.USER_ID +","+DBConstants.CART_TOTAL +") VALUES(?,?)";
        SQLiteStatement insertStmt =   db.compileStatement(sql);
        insertStmt.bindString(1,userId);
        insertStmt.bindString(2,cartTotal);
        insertStmt.executeInsert();
    }

    public void updateCartTotal(String cartTotal,String userId){
        Log.e("DatabaseHandler","cart total when update is >" +cartTotal);
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBConstants.CART_TOTAL,cartTotal);
        db.update(DBConstants.TABLE_CART_TOTAL,cv,DBConstants.USER_ID +"="+userId,null);
    }

    public ArrayList<ModelProductsInCart> getListOfProductsIncart(String userId){
        ArrayList<ModelProductsInCart> arrProductInCart = new ArrayList<>();
        String selectQuery = "SELECT * FROM " +DBConstants.TABLE_USER_CART +" WHERE " +DBConstants.USER_ID + "=" +userId;
        Log.e("selectQuery", selectQuery);
        Cursor cursor = db.rawQuery(selectQuery,null);
        if (cursor.moveToFirst()){
            do {
                ModelProductsInCart modelProductsInCart = new ModelProductsInCart();
                modelProductsInCart.setProductId(cursor.getString(cursor.getColumnIndex(DBConstants.PRODUCT_ID)));
                modelProductsInCart.setProductQty(cursor.getString(cursor.getColumnIndex(DBConstants.PRODUCT_QTY)));
                modelProductsInCart.setProductSku(cursor.getString(cursor.getColumnIndex(DBConstants.PRODUCT_SKU)));

                arrProductInCart.add(modelProductsInCart);
            } while (cursor.moveToNext());
        }
        return arrProductInCart;
    }

    public void deleteRecordsFromDB(String user_id){
        db= this.getWritableDatabase();
        String sqlQuery = "DELETE FROM " +DBConstants.TABLE_USER_CART +" WHERE " +DBConstants.USER_ID +"="+user_id;
        db.execSQL(sqlQuery);
       // Log.e("DBHANDLER :Table count after deleting records > " ,getProductsInCartCount());
    }

    public void deletCartTotalFromDb(String userId){
        db= this.getWritableDatabase();
        String sqlQuery = "DELETE FROM " +DBConstants.TABLE_CART_TOTAL +" WHERE " +DBConstants.USER_ID +"="+userId;
        db.execSQL(sqlQuery);
       //Log.e("DBHANDLER :Table count after deleting records > " ,getCartTotal());
    }
}
