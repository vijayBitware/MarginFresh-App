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

import com.marginfresh.Model.ModelAllTypeArray;
import com.marginfresh.Model.ModelOffer;
import com.marginfresh.Model.ModelSubTypeArray;
import com.marginfresh.Model.ModelTopOffers;
import com.marginfresh.R;
import com.marginfresh.adapter.AdapterOffer;
import com.marginfresh.adapter.AdapterProductList;
import com.marginfresh.adapter.AdapterStoreProductHome;
import com.marginfresh.adapter.AdapterTopOffers;
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
 * Created by bitware on 6/6/17.
 */

public class FragmentOffer extends Fragment {

    View view;
    RecyclerView rv_offer;
    AdapterOffer adapterOffer;
    private String response_msg="",store_id="",user_id="";
    ArrayList<ModelOffer> arrOffer;
    boolean isInternetPresent;
    ConnectionDetector cd;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_offer,container,false);

        init();

        store_id=sharedPreferences.getString("store_id","");
        user_id=sharedPreferences.getString("user_id","");
        if (isInternetPresent){
            new OfferListTask().execute("{\"store_id\":\"" + store_id + "\",\"user_id\":\"" + user_id +  "\"}");
        }else {
            Toast.makeText(getContext(),R.string.no_internet,Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    private void init() {
        sharedPreferences = getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cd = new ConnectionDetector(getContext());
        isInternetPresent = cd.isConnectingToInternet();
        rv_offer= (RecyclerView) view.findViewById(R.id.rv_offer);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(),2);
        rv_offer.setLayoutManager(linearLayoutManager);
    }

    class OfferListTask extends AsyncTask<String, Void, String> {
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
                    .url(Config.BASE_URL + "offer_list.php?"+"store_id="+store_id+"&user_id="+user_id)
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
                        Toast.makeText(getContext(), response_msg, Toast.LENGTH_SHORT).show();
                        arrOffer=new ArrayList<>();
                        JSONArray productArray = jsonObject.getJSONArray("product_array");
                        for (int i=0;i<productArray.length();i++){
                            JSONObject productObj = productArray.getJSONObject(i);
                            ModelOffer modelOffer = new ModelOffer();

                            modelOffer.setProduct_id(productObj.getString("product_id"));
                            modelOffer.setProduct_name(productObj.getString("product_name"));
                            modelOffer.setProduct_image(productObj.getString("product_image"));
                            modelOffer.setPrice(productObj.getString("price"));
                            modelOffer.setNew_price(productObj.getString("new_price"));
                            modelOffer.setProduct_rating_count(productObj.getString("product_rating_count"));
                            modelOffer.setProduct_isInWislist(productObj.getString("product_isInWislist"));
                            modelOffer.setOffer(productObj.getString("offer"));
                            if (productObj.getString("product_isInWislist").equalsIgnoreCase("no")){
                                modelOffer.setIsSelected("no");
                            }else if (productObj.getString("product_isInWislist").equalsIgnoreCase("yes")){
                                modelOffer.setIsSelected("yes");
                            }
                            arrOffer.add(modelOffer);
                        }
                        adapterOffer =new AdapterOffer(getContext(),arrOffer);
                        rv_offer.setAdapter(adapterOffer);

                    } else {
                        dialog.dismiss();
                        Toast.makeText(getContext(), response_msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.dismiss();
                    Toast.makeText(getContext(),getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
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
