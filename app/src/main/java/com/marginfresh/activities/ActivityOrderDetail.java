package com.marginfresh.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.marginfresh.Model.ModelMyOrders;
import com.marginfresh.Model.MyOrderProductData;
import com.marginfresh.R;
import com.marginfresh.adapter.AdapterMyOrders;
import com.marginfresh.adapter.AdapterOrderDetails;
import com.marginfresh.db_utils.DatabaseHandler;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by bitware on 31/7/17.
 */

public class ActivityOrderDetail extends AppCompatActivity {

    ListView lv_myOrderProducts;
    AdapterOrderDetails adapterOrderDetails;
    ArrayList<MyOrderProductData> arrProductData;
    TextView tv_orderedOn,tv_item,tv_grandTotal,tv_orderId,tv_status,tv_customerEmail,tv_paymentBy,tv_shippingAmount,tv_shippingAddress;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ImageView iv_back;
    String order_id="";
    ConnectionDetector cd;
    Boolean isInternetPresent;
    RelativeLayout rl_cart,rl_notification;
    Button btn_cartCount,btn_notiCount;
    DatabaseHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderdetail);

        init();

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*editor.putString("NavigationPosition","4");
                editor.commit();
                startActivity(new Intent(ActivityOrderDetail.this,DrawerActivity.class));*/
                finish();
            }
        });

        order_id = sharedPreferences.getString("orderId","");
        if (isInternetPresent){
            new OrderInfoTask().execute("{\"orderId\":\"" + order_id +  "\"}");
        }else {
            Toast.makeText(ActivityOrderDetail.this,getResources().getString(R.string.no_internet),Toast.LENGTH_SHORT).show();
        }

        String cartCount = db.getProductsInCartCount(sharedPreferences.getString("user_id",""));
        System.out.println("Activity Cart > cart count is " +cartCount);
        if (cartCount.equals("0") || cartCount.equals("")){
            btn_cartCount.setVisibility(View.GONE);
        }else {
            btn_cartCount.setVisibility(View.VISIBLE);
            btn_cartCount.setText(cartCount);
        }
        //code to update notification count
        if (sharedPreferences.getString("notiCount","").equals("0") || sharedPreferences.getString("notiCount","").equals("")){
            btn_notiCount.setVisibility(View.INVISIBLE);
        }else {
            btn_notiCount.setVisibility(View.VISIBLE);
            btn_notiCount.setText(sharedPreferences.getString("notiCount",""));
        }
        rl_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityOrderDetail.this,ActivityCart.class));
            }
        });
        rl_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityOrderDetail.this,ActivityNotificationList.class));
            }
        });
    }

    private void init() {
        cd = new ConnectionDetector(ActivityOrderDetail.this);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        lv_myOrderProducts = (ListView) findViewById(R.id.lv_myOrderProducts);
        View header = getLayoutInflater().inflate(R.layout.header_orderhistory, null);
        lv_myOrderProducts.addHeaderView(header);
        tv_orderedOn = (TextView) header.findViewById(R.id.tv_orderedOn);
        tv_item = (TextView) header.findViewById(R.id.tv_items);
        tv_grandTotal = (TextView) header.findViewById(R.id.grandTotal);
        tv_orderId = (TextView) header.findViewById(R.id.tv_orderId);
        tv_status = (TextView) header.findViewById(R.id.tv_status);
        tv_customerEmail = (TextView) header.findViewById(R.id.tv_email);
        tv_paymentBy = (TextView) header.findViewById(R.id.tv_paymentBy);
        tv_shippingAmount = (TextView) header.findViewById(R.id.tv_shippingAmount);
        tv_shippingAddress = (TextView) header.findViewById(R.id.tv_shippingAddress);
        iv_back = (ImageView) header.findViewById(R.id.iv_back);
        rl_cart = (RelativeLayout) header.findViewById(R.id.rlCart);
        rl_notification = (RelativeLayout) header.findViewById(R.id.rl_notification);
        btn_cartCount = (Button) header.findViewById(R.id.btn_cartCount);
        btn_notiCount = (Button) findViewById(R.id.btn_notiCount);

        db = new DatabaseHandler(ActivityOrderDetail.this);
    }

    class OrderInfoTask extends AsyncTask<String, Void, String> {

        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            p = new ProgressDialog(ActivityOrderDetail.this);
            p.setMessage("In Progress..");
            p.setCanceledOnTouchOutside(false);
            p.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(180, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(180, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL_SHOPCART+"shopcart/orderinfo.php?"+"orderId="+order_id)
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
            System.out.println(">>> Order Info result : "+s);
            if (s != null) {
                p.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
//                        Toast.makeText(ActivityOrderDetail.this,message,Toast.LENGTH_SHORT).show();
                        JSONObject orderData = resObj.getJSONObject("orderData");
                        tv_orderedOn.setText(AppUtils.getFormattedDate(orderData.getString("created_at")));

                        String qty= String.valueOf(Math.round(Float.parseFloat(orderData.getString("total_qty_ordered"))));
                        System.out.println("Qty > " +qty);
                        tv_item.setText(qty);
                        tv_grandTotal.setText(sharedPreferences.getString("currency","")+" "+ AppUtils.getFormattedPrice(Double.parseDouble(orderData.getString("grand_total"))));
                        tv_orderId.setText(orderData.getString("order_id"));
                        tv_status.setText(orderData.getString("orderStatus"));
                        tv_customerEmail.setText(sharedPreferences.getString("customerEmail",""));
                        tv_shippingAmount.setText(sharedPreferences.getString("currency","")+" "+AppUtils.getFormattedPrice(Double.parseDouble(orderData.getString("shipping_amount"))));
                        JSONObject payment = orderData.getJSONObject("payment");
                        tv_paymentBy.setText(payment.getString("method"));

                        JSONObject shipping_address = orderData.getJSONObject("shipping_address");
                        String street = shipping_address.getString("street");
                        String city = shipping_address.getString("city");
                        String postcode = shipping_address.getString("postcode");
                        String shippingAddress = street + "," +city ;
                        tv_shippingAddress.setText(shippingAddress);

                        JSONArray productData = orderData.getJSONArray("product_data");
                        arrProductData = new ArrayList<>();
                        for (int j=0;j<productData.length();j++){
                            JSONObject productObj = productData.getJSONObject(j);

                            MyOrderProductData myOrderProductData = new MyOrderProductData();
                            myOrderProductData.setProduct_id(productObj.getString("product_id"));
                            myOrderProductData.setName(productObj.getString("name"));
                            myOrderProductData.setImageUrl(productObj.getString("imageUrl"));
                            myOrderProductData.setQty_ordered(productObj.getString("qty_ordered"));
                            myOrderProductData.setAmount_refunded(productObj.getString("amount_refunded"));
                            myOrderProductData.setPrice(productObj.getString("price"));
                            myOrderProductData.setRow_total(productObj.getString("row_total"));
                            arrProductData.add(myOrderProductData);
                        }
                        adapterOrderDetails = new AdapterOrderDetails(ActivityOrderDetail.this,R.layout.row_order_history,arrProductData);
                        lv_myOrderProducts.setAdapter(adapterOrderDetails);

                    }else{
                        Toast.makeText(ActivityOrderDetail.this,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityOrderDetail.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                Toast.makeText(ActivityOrderDetail.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        /*editor.putString("NavigationPosition","4");
        editor.commit();
        startActivity(new Intent(ActivityOrderDetail.this,DrawerActivity.class));*/
        finish();
    }
}
