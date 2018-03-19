package com.marginfresh.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.marginfresh.Fragments.FragmentHome;
import com.marginfresh.Model.ModelSubTypeArray;
import com.marginfresh.Model.ModelTopOffers;
import com.marginfresh.R;
import com.marginfresh.adapter.AdapterFoodProductList;
import com.marginfresh.adapter.AdapterTopOffers;
import com.marginfresh.adapter.AdapterTopOffersViewMore;
import com.marginfresh.domain.ConnectionDetector;

import java.util.ArrayList;

/**
 * Created by bitware on 28/6/17.
 */

public class ActivityTopOffers extends AppCompatActivity {

    RecyclerView rv_foodProductList;
    AdapterTopOffersViewMore adapterTopOffers;
    ImageView iv_back,iv_storeBanerImage,iv_storeLogo,iv_search;
    TextView tv_categoryname;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    EditText edt_search;
    static ArrayList<ModelTopOffers> arrTopOffers;
    TextView tv_storeName,tv_storeBanerName;
    Button btn_cart;
    String searchString = "";
    ConnectionDetector cd;
    boolean isInternetPresent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topoffer);

        init();
        tv_storeName.setText(sharedPreferences.getString("storeName",""));
        Glide.with(ActivityTopOffers.this).load(sharedPreferences.getString("storeBanerImage","")).into(iv_storeBanerImage);

        adapterTopOffers =new AdapterTopOffersViewMore(ActivityTopOffers.this, FragmentHome.arrTopOfferList);
        rv_foodProductList.setAdapter(adapterTopOffers);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("NavigationPosition","0");
                editor.commit();
                startActivity(new Intent(ActivityTopOffers.this,DrawerActivity.class));
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
                        editor.putString("searchFrom","topOffer");
                        editor.commit();
                        startActivity(new Intent(ActivityTopOffers.this, ActivitySearch.class));
                    }else {
                        Toast.makeText(ActivityTopOffers.this,getResources().getString(R.string.empty_string),Toast.LENGTH_SHORT).show();

                    }

                }

                return false;
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
                    editor.putString("searchFrom","home");
                    editor.commit();
                    startActivity(new Intent(ActivityTopOffers.this, ActivitySearch.class));
                }else {
                    Toast.makeText(ActivityTopOffers.this,getResources().getString(R.string.empty_string),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void init() {
        cd = new ConnectionDetector(ActivityTopOffers.this);
        isInternetPresent = cd.isConnectingToInternet();
        tv_storeName= (TextView) findViewById(R.id.tv_storeName);
        iv_storeBanerImage= (ImageView) findViewById(R.id.iv_storeBanerImage);
        iv_storeLogo = (ImageView) findViewById(R.id.iv_storeLogo);
        sharedPreferences=getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        iv_back= (ImageView) findViewById(R.id.iv_back);
        edt_search = (EditText) findViewById(R.id.edt_search);
        tv_categoryname = (TextView) findViewById(R.id.tv_categoryname);
        rv_foodProductList = (RecyclerView)findViewById(R.id.rv_foodProductList);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(ActivityTopOffers.this,3);
        rv_foodProductList.setLayoutManager(linearLayoutManager);
        btn_cart= (Button) findViewById(R.id.btn_cartCount);
        iv_search = (ImageView) findViewById(R.id.iv_search);
    }

    @Override
    public void onBackPressed() {
        editor.putString("NavigationPosition","0");
        editor.commit();
        startActivity(new Intent(ActivityTopOffers.this,DrawerActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        btn_cart = (Button) findViewById(R.id.btn_cartCount);
        if (sharedPreferences.getString("cartItemCount","").equals("0") || sharedPreferences.getString("cartItemCount","").equals("")){
            btn_cart.setVisibility(View.GONE);
        }else {
            btn_cart.setVisibility(View.VISIBLE);
            btn_cart.setText(sharedPreferences.getString("cartItemCount",""));
        }
    }
}
