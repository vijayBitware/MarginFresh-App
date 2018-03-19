package com.marginfresh.Fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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

import com.marginfresh.Model.ModelMyWishlist;
import com.marginfresh.Model.ModelOffer;
import com.marginfresh.R;
import com.marginfresh.adapter.AdapterMyWishlist;
import com.marginfresh.adapter.AdapterOffer;
import com.marginfresh.adapter.AdapterProductList;
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

public class FragmentMyWishlist extends Fragment {

    View view;
    RecyclerView rv_myWishlist;
    AdapterMyWishlist adapterMyWishlist;
    private String response_msg="",user_id="";
    ArrayList<ModelMyWishlist> arrMyWishlist;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    boolean isInternetPresent;
    Button btn_cart;
    DatabaseHandler db;

    private AdapterMyWishlist.OnItemClickListener myItemClickListener=new AdapterMyWishlist.OnItemClickListener() {
        @Override
        public void onClick(View v, String description) {

            if (description.equals("0") || description.equals("")){
                btn_cart.setVisibility(View.GONE);
            }else {

                btn_cart.setVisibility(View.VISIBLE);
                btn_cart.setText(description);
                editor.putString("cartItemCount",description);
                editor.commit();
            }
        }

        @Override
        public void updatePrice(View view, Float price) {
            editor.putString("cartTotal",AppUtils.getFormattedPrice(price));
            editor.commit();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mywishlist,container,false);
        init();

        user_id=sharedPreferences.getString("user_id","");
        if (isInternetPresent){
            new MyWishList().execute("{\"user_id\":\"" + user_id + "\"}");
        }else {
            Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    private void init() {
        sharedPreferences = getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cd = new ConnectionDetector(getContext());
        isInternetPresent = cd.isConnectingToInternet();

        rv_myWishlist= (RecyclerView) view.findViewById(R.id.rv_mywishlist);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rv_myWishlist.setLayoutManager(linearLayoutManager);

        db = new DatabaseHandler(getContext());
        btn_cart = (Button) getActivity().findViewById(R.id.btn_cartCount);
    }

    class MyWishList extends AsyncTask<String, Void, String> {

        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "customerwishlist.php?"+"user_id="+user_id)
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
            System.out.println(">>>Home data result :" + s);
            dialog.dismiss();
            if (s != null) {
//                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if (status.equals("1")) {
//                        Toast.makeText(getContext(), response_msg, Toast.LENGTH_SHORT).show();
                        JSONArray productArray = jsonObject.getJSONArray("product_array");
                        arrMyWishlist=new ArrayList<>();
                        for (int i=0;i<productArray.length();i++){
                            JSONObject productObj = productArray.getJSONObject(i);
                            ModelMyWishlist modelMyWishlist = new ModelMyWishlist();

                            modelMyWishlist.setProduct_id(productObj.getString("product_id"));
                            modelMyWishlist.setProduct_name(productObj.getString("product_name"));
                            modelMyWishlist.setProduct_image(productObj.getString("product_image_url"));
                            modelMyWishlist.setPrice(productObj.getString("price"));
                            modelMyWishlist.setNew_price(productObj.getString("new_price"));
                            modelMyWishlist.setProduct_rating_count(productObj.getString("product_rating_count"));
                            modelMyWishlist.setProduct_isInWislist(productObj.getString("product_isInWislist"));
                            modelMyWishlist.setProductSku(productObj.getString("productSku"));
                            modelMyWishlist.setIsInStock(productObj.getString("isInStock"));
                            if (productObj.getString("new_price").equals("0")){
                                modelMyWishlist.setPriceForStorage(productObj.getString("price"));
                            }else {
                                modelMyWishlist.setPriceForStorage(productObj.getString("new_price"));
                            }

                            arrMyWishlist.add(modelMyWishlist);
                        }
                        adapterMyWishlist =new AdapterMyWishlist(getContext(),arrMyWishlist,myItemClickListener);
                        rv_myWishlist.setAdapter(adapterMyWishlist);

                    } else {
                        dialog.dismiss();
                        Toast.makeText(getContext(), response_msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.dismiss();
                    Toast.makeText(getContext(), getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }
            } else {
                dialog.dismiss();
                Toast.makeText(getContext(),"Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String cartCount = db.getProductsInCartCount(sharedPreferences.getString("user_id",""));
        System.out.println("Activity Cart > cart count is " +cartCount);
        if (cartCount.equals("0") || cartCount.equals("")){
            btn_cart.setVisibility(View.GONE);
        }else {
            btn_cart.setVisibility(View.VISIBLE);
            btn_cart.setText(cartCount);
        }

    }
}
