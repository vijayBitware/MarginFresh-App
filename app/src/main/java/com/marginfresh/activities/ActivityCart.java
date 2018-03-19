package com.marginfresh.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.marginfresh.Model.ModelCart;
import com.marginfresh.Model.ModelProductsInCart;
import com.marginfresh.R;
import com.marginfresh.adapter.AdapterCart;
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


public class ActivityCart extends AppCompatActivity {

    ListView lv_cart;
    AdapterCart adapterCart;
    ImageView iv_back;
    TextView tv_proccedToCheckout,tv_screenTitle,tv_subtotal,tv_deliveryCharge,tv_grandTotal,tv_storeName,
            tv_applyCoupon,tv_discount,tv_removeCoupon;
    boolean isInternetPresent,isProductAddedToServer =false;
    ConnectionDetector cd;
    String user_id="",productSku="",shoppingCartId="",coupenCode="",subtotalWithDiscount,disount,grandtotal,deliveryCharge,subtotalWithOutDiscount;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ArrayList<ModelCart> arrModelCart;
    Button btn_cart,btn_notiCount;
    EditText edt_applyCode;
    RelativeLayout rl_notification;
    View footer;
    LinearLayout ll_removeCoupon;
    String discount;
    Dialog dialog;
    DatabaseHandler db;

    private AdapterCart.OnItemClickListener onItemClickListener = new AdapterCart.OnItemClickListener() {
        @Override
        public void onClick(View view, String description) {
            Toast.makeText(ActivityCart.this, description, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void updatePrice(View view, Float price) {
            tv_grandTotal.setText("Your Cart Total Is AED " +AppUtils.getFormattedPrice(price));
            tv_screenTitle.setText(db.getProductsInCartCount(sharedPreferences.getString("user_id","")) + " items in your cart");
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cart);

        init();
        tv_storeName.setText(sharedPreferences.getString("storeName",""));
        tv_screenTitle.setText(db.getProductsInCartCount(sharedPreferences.getString("user_id","")) + " items in your cart");

        arrModelCart = new ArrayList<>();
        arrModelCart = db.getProductsInCart(sharedPreferences.getString("user_id", ""));
        if (arrModelCart.size() > 0) {
            lv_cart.removeFooterView(footer);
            lv_cart.addFooterView(footer);
        }
        adapterCart = new AdapterCart(ActivityCart.this, R.layout.row_cartproduct, arrModelCart, onItemClickListener);
        lv_cart.setAdapter(adapterCart);
        uploadProductToServer();

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_proccedToCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!shoppingCartId.equals("")) {
                    if (!(arrModelCart.size() == 0)) {

                            if (sharedPreferences.getString("addressAvailable", "").equals("0")) {
                                android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(ActivityCart.this);
                                alertDialogBuilder
                                        .setMessage("Please Enter Address To Checkout")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        editor.putString("myAddress", "cart");
                                                        editor.commit();
                                                        startActivity(new Intent(ActivityCart.this, ActivityMyAddress.class));
                                                        finish();
                                                    }
                                                });

                                android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            } else if (sharedPreferences.getString("addressAvailable", "").equals("1")) {
                                    startActivity(new Intent(ActivityCart.this, ActivityCheckout.class));
                                    finish();

                            }

                    } else {
                        Toast.makeText(ActivityCart.this, "Please add product to your cart", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    uploadProductToServer();
                }
            }
        });

        rl_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityCart.this, ActivityNotificationList.class));
            }
        });

    }

    private void uploadProductToServer() {
        ArrayList<ModelProductsInCart> arrProductsIncart = db.getListOfProductsIncart(sharedPreferences.getString("user_id",""));
        System.out.println("Activity Cart > Products in cart > " +arrProductsIncart.size());
        JSONArray productArray = new JSONArray();
        for (int i=0;i<arrProductsIncart.size();i++){
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("productId",arrProductsIncart.get(i).getProductId());
                jsonObject.put("productQty",arrProductsIncart.get(i).getProductQty());
                jsonObject.put("productSku",arrProductsIncart.get(i).getProductSku());

                productArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        if (productArray.length()> 0) {
            callApi(productArray);
        }
    }

    private void callApi(JSONArray cartProductObj) {
        user_id = sharedPreferences.getString("user_id","");
        shoppingCartId = sharedPreferences.getString("shoppingCartId","");
        if (isInternetPresent){
            if (!shoppingCartId.equals("")) {
                new UploadProductToServer().execute("{\"customerId\":\"" + user_id + "\",\"shoppingCartId\":\"" + shoppingCartId + "\",\"productarr\":" + cartProductObj + "}");
            }else {
                Toast.makeText(ActivityCart.this,"Something Went Wrong",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(ActivityCart.this,getResources().getString(R.string.no_internet),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void init() {
        cd = new ConnectionDetector(ActivityCart.this);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences =getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor =sharedPreferences.edit();
        tv_proccedToCheckout= (TextView) findViewById(R.id.tv_proccedToCheckout);
        iv_back= (ImageView) findViewById(R.id.iv_back);
        lv_cart= (ListView) findViewById(R.id.lv_cartProduct);
        tv_storeName = (TextView) findViewById(R.id.tv_storeName);
        tv_screenTitle= (TextView) findViewById(R.id.tv_screenTitle);
        rl_notification = (RelativeLayout) findViewById(R.id.rl_notification);

        LayoutInflater inflater = LayoutInflater.from(ActivityCart.this);
        footer = inflater.inflate(R.layout.footer_cart, null);
        tv_grandTotal = (TextView) footer.findViewById(R.id.tv_grandTotal);
        btn_notiCount = (Button) findViewById(R.id.btn_notiCount);

        db = new DatabaseHandler(ActivityCart.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db.isCartTotalPresentInDB(sharedPreferences.getString("user_id",""))) {
            tv_grandTotal.setText("Your Cart Total Is AED " + AppUtils.getFormattedPrice(Double.parseDouble(db.getCartTotal(sharedPreferences.getString("user_id","")))));
            tv_screenTitle.setText(db.getProductsInCartCount(sharedPreferences.getString("user_id","")) + " items in your cart");
        }else {

        }
        //code to update notification count
        if (sharedPreferences.getString("notiCount","").equals("0") || sharedPreferences.getString("notiCount","").equals("")){
            btn_notiCount.setVisibility(View.INVISIBLE);
        }else {
            btn_notiCount.setVisibility(View.VISIBLE);
            btn_notiCount.setText(sharedPreferences.getString("notiCount",""));
        }

    }
    class UploadProductToServer extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            dialog = AppUtils.customLoader(ActivityCart.this);
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
            System.out.println(">>> add multiple product  result : "+s);
            if (s != null) {
               // dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
                       // Toast.makeText(ActivityCart.this,message, Toast.LENGTH_SHORT).show();
                        isProductAddedToServer = true;

                    }else {
                        isProductAddedToServer = false;
                        Toast.makeText(ActivityCart.this,message, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.dismiss();
                    Toast.makeText(ActivityCart.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }
            }else{
               // dialog.dismiss();
                Toast.makeText(ActivityCart.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SetData() {
        tv_screenTitle.setText(arrModelCart.size() + " Item In Your Cart");
        tv_grandTotal.setText("Your Cart Toal Is AED " +AppUtils.getFormattedPrice(Double.parseDouble(grandtotal)));
    }
}
