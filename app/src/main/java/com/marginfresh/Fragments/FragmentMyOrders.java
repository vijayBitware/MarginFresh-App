package com.marginfresh.Fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.marginfresh.Model.ModelMyOrders;
import com.marginfresh.Model.MyOrderProductData;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityCart;
import com.marginfresh.adapter.AdapterMyOrders;
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
 * Created by bitware on 5/6/17.
 */

public class FragmentMyOrders extends Fragment {

    View view;
    RecyclerView rv_myOrders;
    AdapterMyOrders adapterMyOrders;
    String user_id="";
    boolean isInternetPresent;
    ConnectionDetector cd;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ArrayList<ModelMyOrders> arrMyOrders;
    ArrayList<MyOrderProductData> arrProductData;
    Dialog dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_myorders,container,false);
        init();

        user_id = sharedPreferences.getString("user_id","");
        if (isInternetPresent){
            new MyOrderTask().execute("{\"customerId\":\"" + user_id +  "\"}");
        }else {
            Toast.makeText(getContext(),getResources().getString(R.string.no_internet),Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    private void init() {
        cd = new ConnectionDetector(getContext());
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences =getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor =sharedPreferences.edit();
        rv_myOrders= (RecyclerView) view.findViewById(R.id.rv_myOrders);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rv_myOrders.setLayoutManager(linearLayoutManager);
    }

    class MyOrderTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            dialog = AppUtils.customLoader(getContext());
            dialog.show();
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
                    .url(Config.BASE_URL_SHOPCART+"shopcart/orderhistory.php?"+"customerId="+user_id)
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
            System.out.println(">>> My orders result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
//                        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();

                        arrMyOrders = new ArrayList<>();
                        JSONArray orderData = resObj.getJSONArray("order_data");
                        for (int i=0;i<orderData.length();i++){
                            JSONObject orderObj = orderData.getJSONObject(i);

                            ModelMyOrders myOrders = new ModelMyOrders();
                            myOrders.setOrder_id(orderObj.getString("order_id"));
                            myOrders.setCreated_at(orderObj.getString("created_at"));
                            myOrders.setTotal_qty_ordered(orderObj.getString("total_qty_ordered"));
                            myOrders.setOrderStatus(orderObj.getString("status"));
                            myOrders.setGrand_total(orderObj.getString("grand_total"));
                            myOrders.setReorder(orderObj.getString("reorder"));

                            arrMyOrders.add(myOrders);
                        }
                        adapterMyOrders = new AdapterMyOrders(getContext(),arrMyOrders);
                        rv_myOrders.setAdapter(adapterMyOrders);

                    }else{
                        dialog.dismiss();
                        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.dismiss();
                    Toast.makeText(getContext(),getResources().getString(R.string.network_error),Toast.LENGTH_SHORT).show();
                }
            }else{
                dialog.dismiss();
                Toast.makeText(getContext(), "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Button noticount = (Button) getActivity().findViewById(R.id.btn_notiCount);
        if (sharedPreferences.getString("notiCount","").equals("0") || sharedPreferences.getString("notiCount","").equals("")){
            noticount.setVisibility(View.INVISIBLE);
        }else {
            noticount.setVisibility(View.VISIBLE);
            noticount.setText(sharedPreferences.getString("notiCount",""));
        }

        DatabaseHandler db = new DatabaseHandler(getContext());
        Button btn_cartCount = (Button) getActivity().findViewById(R.id.btn_cartCount);
        String cartCount = db.getProductsInCartCount(sharedPreferences.getString("user_id",""));
        System.out.println("Activity Cart > cart count is " +cartCount);
        if (cartCount.equals("0") || cartCount.equals("")){
            btn_cartCount.setVisibility(View.GONE);
        }else {
            btn_cartCount.setVisibility(View.VISIBLE);
            btn_cartCount.setText(cartCount);
        }
    }
}
