package com.marginfresh.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.marginfresh.Fragments.FragmentHome;
import com.marginfresh.Model.ModelAllTypeArray;
import com.marginfresh.Model.ModelCategoryListScroll;
import com.marginfresh.Model.ModelFoodCategoryList;
import com.marginfresh.R;
import com.marginfresh.adapter.AdapterCategoryList;
import com.marginfresh.db_utils.DatabaseHandler;
import com.marginfresh.domain.Config;

import java.util.ArrayList;

/**
 * Created by bitware on 2/6/17.
 */

public class ActivityCategoryList extends AppCompatActivity {

    ListView lv_categoryList;
    AdapterCategoryList adapterCategoryList;
    ImageView iv_back,iv_storeBannerImage,iv_storeLogo,iv_search;
    private EditText edt_search;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ArrayList<ModelAllTypeArray> arrAllTypeArray;
    TextView tv_storeName,tv_storeBanerName;
    RelativeLayout rl_notification,rl_cart;
    Button btn_cartCount,btn_notiCount;
    DatabaseHandler db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_food_category_list);

        init();
        tv_storeName.setText(sharedPreferences.getString("storeName",""));
        Glide.with(ActivityCategoryList.this).load(sharedPreferences.getString("storeBanerImage","")).into(iv_storeBannerImage);


        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        rl_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityCategoryList.this,ActivityNotificationList.class));
            }
        });
        rl_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("cartFrom","categoryList");
                editor.commit();
                startActivity(new Intent(ActivityCategoryList.this,ActivityCart.class));
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
                    editor.putString("searchFrom","categoryList");
                    editor.commit();
                    startActivity(new Intent(ActivityCategoryList.this, ActivitySearch.class));
                }else {
                    Toast.makeText(ActivityCategoryList.this,getResources().getString(R.string.empty_string),Toast.LENGTH_SHORT).show();
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
                        editor.putString("searchFrom","categoryList");
                        editor.commit();
                        startActivity(new Intent(ActivityCategoryList.this, ActivitySearch.class));
                    }else {
                        Toast.makeText(ActivityCategoryList.this,getResources().getString(R.string.empty_string),Toast.LENGTH_SHORT).show();
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
        rl_notification = (RelativeLayout) findViewById(R.id.rl_notification);
        rl_cart = (RelativeLayout) findViewById(R.id.rlCart);
        tv_storeName = (TextView) findViewById(R.id.tv_storeName);
        iv_storeBannerImage = (ImageView) findViewById(R.id.iv_bannerImage);
        iv_storeLogo = (ImageView) findViewById(R.id.iv_storeLogo);
        tv_storeBanerName = (TextView) findViewById(R.id.tv_storeNameBanner);
        arrAllTypeArray = new ArrayList<>();
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        iv_back = (ImageView) findViewById(R.id.iv_back);
        edt_search= (EditText) findViewById(R.id.edt_search);
        lv_categoryList= (ListView) findViewById(R.id.lv_categoryList);
        iv_search = (ImageView) findViewById(R.id.iv_search);
        btn_cartCount = (Button) findViewById(R.id.btn_cartCount);
        btn_notiCount = (Button) findViewById(R.id.btn_notiCount);

        db = new DatabaseHandler(ActivityCategoryList.this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        arrAllTypeArray = new ArrayList<>();
        for (int i=1;i<Config.arrAllTypeArrayList.size();i++){
            arrAllTypeArray.add(Config.arrAllTypeArrayList.get(i));
        }
        adapterCategoryList = new AdapterCategoryList(ActivityCategoryList.this,R.layout.row_food_category_list,arrAllTypeArray);
        lv_categoryList.setAdapter(adapterCategoryList);

        //code to update notification count
        if (sharedPreferences.getString("notiCount","").equals("0") || sharedPreferences.getString("notiCount","").equals("")){
            btn_notiCount.setVisibility(View.INVISIBLE);
        }else {
            btn_notiCount.setVisibility(View.VISIBLE);
            btn_notiCount.setText(sharedPreferences.getString("notiCount",""));
        }
    }
}
