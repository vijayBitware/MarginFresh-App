package com.marginfresh.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.marginfresh.Model.ModelProductList;
import com.marginfresh.Model.SelectStore;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityProductDetail;
import com.marginfresh.activities.ActivityProductList;
import com.marginfresh.activities.DrawerActivity;
import com.marginfresh.db_utils.DatabaseHandler;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by bitware on 2/6/17.
 */

public class AdapterProductList extends  RecyclerView.Adapter<AdapterProductList.MyViewHolder>
{
    Context context;
    ArrayList<ModelProductList> arrProductList;
    boolean isWishlistSelected = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String user_id="",product_id="",cartTotal="",productSubTotal="";
    ConnectionDetector cd;
    boolean isInternetPresent;
    private String shoppingCartId="",qty,productSku;
    int currentRowPosition,counter=1;
    MyViewHolder viewHolder;
    private OnItemClickListener listener;
    View view;
    Dialog dialog;
    int cnt;
    DatabaseHandler db;

    public interface OnItemClickListener{
        void onClick(View view,String description);
        void updatePrice(View view,Float price);
    }

    public AdapterProductList(Context context, ArrayList<ModelProductList> arrayList,OnItemClickListener listener)
    {
        this.context=context;
        this.arrProductList = arrayList;
        this.listener = listener;
        sharedPreferences =context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        editor =sharedPreferences.edit();
        cd = new ConnectionDetector(context);
        isInternetPresent = cd.isConnectingToInternet();
        dialog = AppUtils.customLoader(context);
        //arrProductIds = new ArrayList<>();
        db = new DatabaseHandler(context);

    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        ImageView iv_wishlist,iv_productImage,iv_rateStar1,iv_rateStar2,iv_rateStar3,iv_rateStar4,iv_rateStar5,iv_add,iv_remove,iv_wishlistInactive;
        TextView tv_productName,tv_Price,tv_newPrice,tv_priceLine,tv_addToCart,tv_lineAfterPrice,tv_lineAfterRating,tv_lineAfterName,tv_count;
        LinearLayout ll_row,ll_rating,ll_addToCart,ll_productName;
        RelativeLayout rl_price,rl_wishlist;
        public MyViewHolder(View view)
        {
            super(view);
            iv_wishlist = (ImageView)view.findViewById(R.id.iv_wishlist);
            ll_row= (LinearLayout) view.findViewById(R.id.ll_row);
            iv_productImage= (ImageView) view.findViewById(R.id.iv_productImage);
            tv_productName= (TextView) view.findViewById(R.id.tv_productName);
            tv_Price = (TextView) view.findViewById(R.id.tv_price);
            tv_newPrice = (TextView) view.findViewById(R.id.tv_newPrice);
            iv_rateStar1 = (ImageView) view.findViewById(R.id.iv_rateStar1);
            iv_rateStar2 = (ImageView) view.findViewById(R.id.iv_rateStar2);
            iv_rateStar3 = (ImageView) view.findViewById(R.id.iv_rateStar3);
            iv_rateStar4 = (ImageView) view.findViewById(R.id.iv_rateStar4);
            iv_rateStar5 = (ImageView) view.findViewById(R.id.iv_rateStar5);
            ll_rating = (LinearLayout) view.findViewById(R.id.ll_rating);
            tv_priceLine = (TextView) view.findViewById(R.id.tv_priceLine);
            rl_price = (RelativeLayout) view.findViewById(R.id.rl_price);
            tv_addToCart = (TextView) view.findViewById(R.id.tv_addToCart);
            ll_addToCart = (LinearLayout) view.findViewById(R.id.ll_addToCart);
            tv_lineAfterPrice = (TextView) view.findViewById(R.id.tv_lineAfterPrice);
            tv_lineAfterRating = (TextView) view.findViewById(R.id.tv_lineAfterRating);
            tv_lineAfterName = (TextView) view.findViewById(R.id.tv_lineAfterName);
            iv_add = (ImageView) view.findViewById(R.id.iv_add);
            iv_remove = (ImageView) view.findViewById(R.id.iv_remove);
            tv_count = (TextView) view.findViewById(R.id.tv_count);
            ll_productName = (LinearLayout) view.findViewById(R.id.ll_productName);
          //  rl_wishlist = (RelativeLayout) view.findViewById(R.id.rl_wishlist);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_productlist, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        currentRowPosition = position;
        viewHolder = holder;
        holder.tv_productName.setText(arrProductList.get(position).getProduct_name());
        Glide.with(context).load(arrProductList.get(position).getProduct_image_url()).into(holder.iv_productImage);
        holder.tv_Price.setText("Price: " + sharedPreferences.getString("currency","")+" "+AppUtils.getFormattedPrice(Double.parseDouble(arrProductList.get(position).getPrice())));
        if (arrProductList.get(position).getNew_price().equals("0")){
            holder.tv_newPrice.setVisibility(View.GONE);
            setWeight(1.8f,null,null,holder.ll_productName);
            setWeight(8.0f,null,holder.iv_productImage,null);
            setWeight(1.3f,null,null,holder.ll_rating);
            setWeightForPrice(1.8f,holder.rl_price);
            setWeight(0.5f,holder.tv_lineAfterPrice,null,null);
            setWeight(1.0f,holder.tv_lineAfterRating,null,null);
            setWeight(0.3f,holder.tv_lineAfterName,null,null);
            holder.tv_priceLine.setVisibility(View.INVISIBLE);
        }else {
            holder.tv_newPrice.setVisibility(View.VISIBLE);
            holder.tv_newPrice.setText("New Price: "+ sharedPreferences.getString("currency","")+" "+AppUtils.getFormattedPrice(Double.parseDouble(arrProductList.get(position).getNew_price())));
            setWeight(1.6f,null,null,holder.ll_productName);
            setWeight(1.6f,holder.tv_newPrice,null,null);
            setWeightForPrice(1.6f,holder.rl_price);
            setWeight(8.0f,null,holder.iv_productImage,null);
            setWeight(1.3f,null,null,holder.ll_rating);
            holder.tv_priceLine.setVisibility(View.VISIBLE);

            setWeight(0.2f,holder.tv_lineAfterPrice,null,null);
            setWeight(0.3f,holder.tv_lineAfterRating,null,null);
            setWeight(0.0f,holder.tv_lineAfterName,null,null);
        }

        if (arrProductList.get(position).getIsSelected().equals("no")){
           holder.iv_wishlist.setImageResource(R.drawable.wishlist_inactive);
        }else {
            holder.iv_wishlist.setImageResource(R.drawable.wishlist_red);
        }
        String rateCount = arrProductList.get(position).getProduct_rating_count();
        switch (rateCount){
            case "1":
                holder.iv_rateStar1.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar2.setImageResource(R.drawable.gray_star);
                holder.iv_rateStar3.setImageResource(R.drawable.gray_star);
                holder.iv_rateStar4.setImageResource(R.drawable.gray_star);
                holder.iv_rateStar5.setImageResource(R.drawable.gray_star);
                break;
            case "2":
                holder.iv_rateStar1.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar2.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar3.setImageResource(R.drawable.gray_star);
                holder.iv_rateStar4.setImageResource(R.drawable.gray_star);
                holder.iv_rateStar5.setImageResource(R.drawable.gray_star);
                break;
            case "3":
                holder.iv_rateStar1.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar2.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar3.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar4.setImageResource(R.drawable.gray_star);
                holder.iv_rateStar5.setImageResource(R.drawable.gray_star);
                break;
            case "4":
                holder.iv_rateStar1.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar2.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar3.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar4.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar5.setImageResource(R.drawable.gray_star);
                break;
            case "5":
                holder.iv_rateStar1.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar2.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar3.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar4.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar5.setImageResource(R.drawable.yellow_star);
                break;

        }

        holder.ll_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("from","fromProductList");
                editor.putString("product_id",arrProductList.get(position).getProductId());
                editor.commit();
                context.startActivity(new Intent(context, ActivityProductDetail.class));
            }
        });
        holder.iv_wishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentRowPosition = position;
                product_id =arrProductList.get(currentRowPosition).getProductId();
                user_id = sharedPreferences.getString("user_id","");
                if (isInternetPresent) {
                    if (arrProductList.get(currentRowPosition).getIsSelected().equals("no")) {
                        new AddToWishList().execute("{\"user_id\":\"" + user_id + "\",\"product_id\":\"" + product_id + "\"}");
                    } else {
                        new DeleteWishlist().execute("{\"user_id\":\"" + user_id + "\",\"product_id\":\"" + product_id + "\"}");

                    }
                }
            }
        });

        holder.ll_addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view = v;
                System.out.println("Cart count from shared preference > " +sharedPreferences.getString("cartItemCount",""));
                String productsPrice = arrProductList.get(position).getPriceForSort();
                Float productsTotalPrice = (Integer.parseInt(holder.tv_count.getText().toString()) * Float.parseFloat(productsPrice));
                product_id  = arrProductList.get(position).getProductId();
                productSubTotal = arrProductList.get(position).getPriceForSort();

                if (arrProductList.get(position).getProductInStock().equalsIgnoreCase("yes")) {

                        if (!db.isProductExitsInDb(product_id,sharedPreferences.getString("user_id",""))) {
                            Log.e("TAG", "Inserting product in cart table");
                            Toast.makeText(context, "Product Added To Cart", Toast.LENGTH_SHORT).show();
                            db.insertProductToCart(sharedPreferences.getString("user_id",""),
                                    arrProductList.get(position).getProductId(),
                                    arrProductList.get(position).getProductSku(),
                                    arrProductList.get(position).getProduct_name(),
                                    arrProductList.get(position).getProduct_image_url(),
                                    arrProductList.get(position).getProductInStock(),
                                    arrProductList.get(position).getProduct_rating_count(),
                                    arrProductList.get(position).getProduct_isInWislist(),
                                    String.valueOf(productsTotalPrice),
                                    holder.tv_count.getText().toString());

                            Log.e("TAG", "Products in cart > " + db.getProductsInCartCount(sharedPreferences.getString("user_id","")));
                            updateCartCount();
                        } else {
                            Log.e("TAG", "Updating product qty in cart table");
                            //updating productQty
                            updateProductQtyInDb(holder.tv_count.getText().toString());
                            updateProductPriceInDB(Integer.parseInt(holder.tv_count.getText().toString()), arrProductList.get(position).getPriceForSort());

                        }

                        if (db.isCartTotalPresentInDB(sharedPreferences.getString("user_id",""))) {
                            Log.e("TAG", "updating cart total");
                            updateCartTotal(Integer.parseInt(holder.tv_count.getText().toString()));
                        } else {
                            Log.e("TAG", "inserting cart total");
                            //updating cart total
                            Log.e("TAG", "Cart total while inserting > " + productSubTotal);
                            db.insertCartTotal(productSubTotal,sharedPreferences.getString("user_id",""));
                            Log.e("TAG", "Cart total after calulaton > " + db.getCartTotal(sharedPreferences.getString("user_id","")));
                        }
                        updatePrice();

                        if (sharedPreferences.getString("shoppingCartId","").equals("")) {
                            user_id=sharedPreferences.getString("user_id","");
                            product_id=arrProductList.get(position).getProductId();
                            productSku = arrProductList.get(position).getProductSku();
                            shoppingCartId = sharedPreferences.getString("shoppingCartId","");
                            qty = holder.tv_count.getText().toString();
                            if (isInternetPresent){
                                if (!qty.equals("0")) {
                                    new AddToCart().execute("{\"customerId\":\"" + user_id + "\",\"productId\":\"" + product_id + "\",\"productQty\":\"" + qty + "\",\"productSku\":\"" + productSku + "\"}");
                                }else {
                                    Toast.makeText(context,"Please Enter Quantity",Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Toast.makeText(context,R.string.no_internet,Toast.LENGTH_SHORT).show();
                            }
                        }
                }else {
                    Toast.makeText(context,"Product Is Out Of Stock",Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.tv_count.setText("1");

        holder.iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter = Integer.parseInt(holder.tv_count.getText().toString());
                counter++;
                holder.tv_count.setText(String.valueOf(counter));
            }
        });

        holder.iv_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int countValue = Integer.parseInt(holder.tv_count.getText().toString());
                if (countValue !=1){
                    countValue--;
                    holder.tv_count.setText(String.valueOf(countValue));
                }
            }
        });
    }

    private void updateProductPriceInDB(int qty,String pPrice) {
        String pPriceFromDB = db.getProductPrice(product_id,sharedPreferences.getString("user_id",""));
        Log.e("TAG","Product Price from db > " +pPriceFromDB);
        Float finalPrice = Float.parseFloat(pPriceFromDB) + (qty * Float.parseFloat(pPrice));
        Log.e("TAG","Product Price while  adding in db > " +pPriceFromDB);
        db.updateProductPrice(String.valueOf(finalPrice),product_id,sharedPreferences.getString("user_id",""));
        Log.e("TAG","Product Price after adding in db > " +db.getProductPrice(product_id,sharedPreferences.getString("user_id","")));
    }

    private void updateProductQtyInDb(String qty) {
        int productQtyFromDb = db.getProductQty(product_id,sharedPreferences.getString("user_id",""));
        Log.e("TAG","Product Qty from db - " +productQtyFromDb);
        int finalQty = productQtyFromDb + Integer.parseInt(qty);
        Log.e("TAG","Product Qty while inserted in db - " +finalQty);
        db.updateProductQty(product_id, String.valueOf(finalQty),sharedPreferences.getString("user_id",""));
        Log.e("TAG","Product Qty after stored in db - " +db.getProductQty(product_id,sharedPreferences.getString("user_id","")));
    }

    private void updateCartTotal(int qty){
        String presentCartTotal = db.getCartTotal(sharedPreferences.getString("user_id",""));
        Log.e("TAG","Cart total from db > " +presentCartTotal);
        Float cartTotal = Float.parseFloat(presentCartTotal) + (qty * Float.parseFloat(productSubTotal));
        Log.e("TAG","Cart total while inserting in db > " +cartTotal);
        db.updateCartTotal(String.valueOf(cartTotal),sharedPreferences.getString("user_id",""));
        Log.e("TAG","Cart total after calulaton > " +db.getCartTotal(sharedPreferences.getString("user_id","")));
    }

    private void updateCartCount() {
        String content = String.valueOf(db.getProductsInCartCount(sharedPreferences.getString("user_id","")));
        if(listener!=null){
            listener.onClick(view,content);
        }
    }

    private void updatePrice(){
        if(listener!=null){
            String addProductPrice = db.getCartTotal(sharedPreferences.getString("user_id",""));
            listener.updatePrice(view, Float.parseFloat(addProductPrice));
        }
    }

    public void setWeight(Float weight,TextView textView,ImageView imageView,LinearLayout linearLayout){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,0,weight);
        if (!(textView ==null)){
            textView.setLayoutParams(params);
        }
        if (!(imageView ==null)){
            imageView.setLayoutParams(params);
        }
        if (!(linearLayout ==null)){
            linearLayout.setLayoutParams(params);
        }
    }
    public void setWeightForPrice(Float weight,RelativeLayout relativeLayout){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,0,weight);
        if (!(relativeLayout ==null)){
            relativeLayout.setLayoutParams(params);
        }
    }
    @Override
    public int getItemCount() {
        return arrProductList.size();
    }
    class AddToWishList extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            com.squareup.okhttp.Response response = null;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"addtowishlist.php?"+"user_id="+user_id + "&product_id="+product_id)
                    .post(body)
                    .build();
            try {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(">>> Add to wishlist result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
                        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                        arrProductList.get(currentRowPosition).setIsSelected("yes");
                        notifyDataSetChanged();
                    }else{
                        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context,context.getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                dialog.dismiss();
                Toast.makeText(context, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
    class DeleteWishlist extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(180, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(180, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "customerdeletewishlistitem.php?"+"user_id="+user_id+"&product_id="+product_id)
                    .post(body)
                    .build();
            try {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // clearEditTextData();
            System.out.println(">>>Delete product from wishlist result :" + s);
            dialog.dismiss();
            if (s != null) {
//                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    String response_msg = jsonObject.getString("message");

                    if (status.equals("1")) {
                        Toast.makeText(context, response_msg, Toast.LENGTH_SHORT).show();
                        arrProductList.get(currentRowPosition).setIsSelected("no");
                        notifyDataSetChanged();
                        //context.startActivity(new Intent(context,ActivityProductList.class));
                    } else {
                        dialog.dismiss();
                        Toast.makeText(context, response_msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context,context.getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            } else {
                dialog.dismiss();
                Toast.makeText(context,"Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
    class AddToCart extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            com.squareup.okhttp.Response response = null;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL_SHOPCART+"shopcart/addtocart.php?"+"customerId="+user_id+"&productId="+product_id+"&productQty="+qty+"&productSku="+productSku)
                    .post(body)
                    .build();
            try {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(">>> Add to cart result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
                        notifyDataSetChanged();
                        shoppingCartId = resObj.getString("cart_id");
                        String cartItemsCount = resObj.getString("cartItemsCount");
                        cartTotal = resObj.getString("cartTotal");
                        //Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                        editor.putString("cartItemCount",cartItemsCount);
                        editor.putString("shoppingCartId",shoppingCartId);
                        editor.commit();
                    }else{
                        updateCartCount();
                        /*Config.productPrice = Float.valueOf(cartTotal);
                        editor.putString("cartTotal",cartTotal);
                        editor.commit();*/
                        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context,context.getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                dialog.dismiss();
                Toast.makeText(context, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }


}

