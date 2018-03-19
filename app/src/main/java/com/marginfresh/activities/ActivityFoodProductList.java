package com.marginfresh.activities;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.marginfresh.Model.ModelCategoryListScroll;
import com.marginfresh.Model.ModelSubTypeArray;
import com.marginfresh.R;
import com.marginfresh.adapter.AdapterFoodProductList;
import com.marginfresh.db_utils.DatabaseHandler;
import com.marginfresh.domain.Config;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by bitware on 2/6/17.
 */

public class ActivityFoodProductList extends AppCompatActivity {

    RecyclerView rv_foodProductList;
    AdapterFoodProductList adapterFoodProductList;
    ImageView iv_back,iv_storeBanerImage,iv_storeLogo,iv_search;
    TextView tv_categoryname;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    EditText edt_search;
    TextView tv_storeName,tv_storeBanerName;
    RelativeLayout rl_notification,rl_cart;
    private Button btn_cartCount,btn_notiCount;
    DatabaseHandler db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_food_product_list);

        init();
        tv_categoryname.setText(sharedPreferences.getString("categoryName",""));
        tv_storeName.setText(sharedPreferences.getString("storeName",""));
        Glide.with(ActivityFoodProductList.this).load(sharedPreferences.getString("storeBanerImage","")).into(iv_storeBanerImage);

        System.out.println("Sub type arraylist size >> " +Config.arrSubCatArrayList);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        rl_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityFoodProductList.this,ActivityNotificationList.class));
            }
        });
        rl_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("cartFrom","foodProductList");
                editor.commit();
                startActivity(new Intent(ActivityFoodProductList.this,ActivityCart.class));
            }
        });

        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edt_search.getWindowToken(), 0);
                String searchString = edt_search.getText().toString();
                if (!searchString.isEmpty()){
                    editor.putString("searchString",searchString);
                    editor.putString("searchFrom","foodProductList");
                    editor.commit();
                    Intent intent = new Intent(ActivityFoodProductList.this,ActivitySearch.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(ActivityFoodProductList.this,getResources().getString(R.string.empty_string),Toast.LENGTH_SHORT).show();
                }
            }
        });

        edt_search.setOnEditorActionListener(new TextView.OnEditorActionListener(){

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edt_search.getWindowToken(), 0);
                    String searchString = edt_search.getText().toString();
                    if (!searchString.isEmpty()){
                        editor.putString("searchString",searchString);
                        editor.putString("searchFrom","foodProductList");
                        editor.commit();
                        startActivity(new Intent(ActivityFoodProductList.this, ActivitySearch.class));
                    }else {
                        Toast.makeText(ActivityFoodProductList.this,getResources().getString(R.string.empty_string),Toast.LENGTH_SHORT).show();
                    }

                }

                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void init() {
        rl_notification= (RelativeLayout) findViewById(R.id.rl_notification);
        rl_cart = (RelativeLayout) findViewById(R.id.rlCart);
        tv_storeName= (TextView) findViewById(R.id.tv_storeName);
        iv_storeBanerImage= (ImageView) findViewById(R.id.iv_storeBanerImage);
        iv_storeLogo = (ImageView) findViewById(R.id.iv_storeLogo);
        sharedPreferences=getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        iv_back= (ImageView) findViewById(R.id.iv_back);
        edt_search = (EditText) findViewById(R.id.edt_search);
        tv_categoryname = (TextView) findViewById(R.id.tv_categoryname);
        rv_foodProductList = (RecyclerView)findViewById(R.id.rv_foodProductList);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(ActivityFoodProductList.this,3);
        rv_foodProductList.setLayoutManager(linearLayoutManager);
        iv_search = (ImageView) findViewById(R.id.iv_search);

        btn_cartCount = (Button) findViewById(R.id.btn_cartCount);
        rl_cart = (RelativeLayout) findViewById(R.id.rlCart);
        btn_notiCount = (Button) findViewById(R.id.btn_notiCount);

        db = new DatabaseHandler(ActivityFoodProductList.this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        adapterFoodProductList =new AdapterFoodProductList(ActivityFoodProductList.this,Config.arrSubCatArrayList);
        rv_foodProductList.setAdapter(adapterFoodProductList);

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
    }
}
