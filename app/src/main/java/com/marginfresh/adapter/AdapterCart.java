package com.marginfresh.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.marginfresh.Model.ModelCart;
import com.marginfresh.Model.ModelProductsInCart;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityCart;
import com.marginfresh.activities.DrawerActivity;
import com.marginfresh.db_utils.DatabaseHandler;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by bitware on 2/6/17.
 */

public class AdapterCart extends ArrayAdapter<ModelCart> {

    Context context;
    LayoutInflater inflater;
    ViewHolder holder;
    ArrayList<ModelCart> arrCart;
    String productId,productType,productSku,productQty,shoppingCartId,user_id,productSubTotal;
    boolean isInternetPresent;
    ConnectionDetector cd;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private OnItemClickListener listener;
    View view;
    int currentPosition;
    DatabaseHandler db;

    public interface OnItemClickListener{
        void onClick(View view,String description);
        void updatePrice(View view,Float price);
    }
    public AdapterCart(Context context, int resource,ArrayList<ModelCart> arrCart,OnItemClickListener listener) {
        super(context,resource);
        this.context = context;
        this.arrCart=arrCart;
        this.listener = listener;

        cd = new ConnectionDetector(context);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences =getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor =sharedPreferences.edit();
        db = new DatabaseHandler(context);
    }

    public static class ViewHolder {
        TextView tv_cartPName,tv_PSubtotal,tv_PQty;
        ImageView iv_cartPImage,iv_delete;
        LinearLayout llViewCart;
    }
    @Override
    public int getCount() {
        return arrCart.size();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView==null){
            convertView = inflater.inflate(R.layout.row_cartproduct, null);
            holder = new ViewHolder();

            holder.iv_cartPImage = (ImageView) convertView.findViewById(R.id.iv_cartPImage);
            holder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
            holder.tv_cartPName = (TextView) convertView.findViewById(R.id.tv_cartPName);
            holder.tv_PSubtotal = (TextView) convertView.findViewById(R.id.tv_PSubtotal);
            holder.tv_PQty = (TextView) convertView.findViewById(R.id.tv_PQty);

            convertView.setTag(holder);

        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_cartPName.setText(arrCart.get(position).getProduct_name());
        holder.tv_PSubtotal.setText("Subtotal: "+ sharedPreferences.getString("currency","")+" " + AppUtils.getFormattedPrice(Double.parseDouble(arrCart.get(position).getProductPrice())));
        holder.tv_PQty.setText("Qty: " +arrCart.get(position).getQty_count());
        Glide.with(context).load(arrCart.get(position).getProductImageUrl()).into(holder.iv_cartPImage);
        holder.iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view = v;
                productSubTotal = arrCart.get(position).getProductPrice();
                productId = arrCart.get(position).getProduct_id();
                currentPosition = position;
                showRemoveDialog(position);
            }
        });
        return convertView;
    }

    public void showRemoveDialog(final int position){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Margin Fresh");
        alertDialogBuilder
                .setMessage("Do you want to remove this product from  cart ?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                if (Integer.parseInt(db.getProductsInCartCount(sharedPreferences.getString("user_id",""))) > 0) {
                                    db.removeProductFromCart(arrCart.get(currentPosition).getProduct_id(),sharedPreferences.getString("user_id",""));
                                    notifyDataSetChanged();
                                    Log.e("TAG", "Cart count after removing product > " + db.getProductsInCartCount(sharedPreferences.getString("user_id","")));
                                    updateCartTotal();
                                    user_id = sharedPreferences.getString("user_id","");
                                    productId = arrCart.get(currentPosition).getProduct_id();
                                    if (isInternetPresent){
                                        new RemoveFromCart().execute("{\"productId\":\"" + productId + "\",\"user_id\":\"" + user_id + "}");
                                    }else {
                                        Toast.makeText(context,context.getResources().getString(R.string.no_internet),Toast.LENGTH_SHORT).show();
                                    }
                                   // uploadProductToServer();
                                    arrCart.remove(currentPosition);
                                }
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void updateCartTotal() {
        String presentCartTotal = db.getCartTotal(sharedPreferences.getString("user_id",""));
        Log.e("TAG","Cart total from db > " +presentCartTotal);
        if (Float.parseFloat(presentCartTotal) > 0.0f){
            Float cartTotal = Float.parseFloat(presentCartTotal) - Float.parseFloat(productSubTotal);
            Log.e("TAG","Cart total while inserting in db > " +cartTotal);
            db.updateCartTotal(String.valueOf(cartTotal), sharedPreferences.getString("user_id", ""));
            Log.e("TAG","Cart total after calulaton > " +db.getCartTotal(sharedPreferences.getString("user_id","")));
        }
        if(listener!=null){
            listener.updatePrice(view, Float.valueOf(db.getCartTotal(sharedPreferences.getString("user_id",""))));
        }
    }

    private void callApi(JSONArray cartProductObj) {
        user_id = sharedPreferences.getString("user_id","");
        shoppingCartId = sharedPreferences.getString("shoppingCartId","");
        if (isInternetPresent){
            if (!shoppingCartId.equals("")) {
                new UploadProductToServer().execute("{\"customerId\":\"" + user_id + "\",\"shoppingCartId\":\"" + shoppingCartId + "\",\"productarr\":" + cartProductObj + "}");
            }else {
                Toast.makeText(context,"Something Went Wrong",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(context,context.getResources().getString(R.string.no_internet),Toast.LENGTH_SHORT).show();
        }
    }

    class UploadProductToServer extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
//            dialog = AppUtils.customLoader(ActivityCart.this);
            //  dialog.show();
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
                    .url(Config.BASE_URL_SHOPCART+"shopcart/addtocart_multiple.php")
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
            System.out.println(">>> MY cart result : "+s);
            if (s != null) {
                // dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
                        // Toast.makeText(ActivityCart.this,message, Toast.LENGTH_SHORT).show();

                    }else {
                        Toast.makeText(context,message, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                   // dialog.dismiss();
                    Toast.makeText(context,context.getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }
            }else{
                // dialog.dismiss();
                Toast.makeText(context, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }


    class RemoveFromCart extends AsyncTask<String, Void, String> {
        Dialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = AppUtils.customLoader(context);
         //   dialog.show();
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
                    .url(Config.BASE_URL_SHOPCART+"shopcart/delete_from_cart.php?"+"productId="+productId+"&user_id="+user_id)
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
            System.out.println(">>> Remove from cart result : "+s);
            if (s != null) {
             //   dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
                       // Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                        String cartItemsCount=resObj.getString("cartItemsCount");
                        //Config.cartCounter = Integer.parseInt(cartItemsCount);
                        editor.putString("cartItemCount",cartItemsCount);
                        editor.commit();

//                        arrCart.remove(currentPosition);
                        notifyDataSetChanged();
                        if (Config.arrProductIds.contains(productId)){
                            System.out.println("In if condition from cart");
                            Config.arrProductIds.remove(productId);
                        }

                        String cartTotal = sharedPreferences.getString("cartTotal","");
                        Float cartTotl = Float.parseFloat(cartTotal);
                        System.out.println("Cart total in adapter cart > " +cartTotl);
                        if (!(cartTotl < 0)){
                            System.out.println("Product subtotal>" + productSubTotal);
                            Float reducedPrice = cartTotl - Float.parseFloat(productSubTotal);
                            System.out.println("Product price > " +reducedPrice);
                            editor.putString("cartTotal",AppUtils.getFormattedPrice(reducedPrice));
                            editor.commit();
                        }

                        /*context.startActivity(new Intent(context,ActivityCart.class));
                        ((Activity)context).finish();*/
                    }else{
                        //dialog.dismiss();
                        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                   // dialog.dismiss();
                    Toast.makeText(context,context.getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                //dialog.dismiss();
                Toast.makeText(context, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
