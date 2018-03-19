package com.marginfresh.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.marginfresh.R;
import com.marginfresh.adapter.AdapterCart;
import com.marginfresh.db_utils.DatabaseHandler;
import com.marginfresh.utils.AppUtils;

/**
 * Created by bitware on 1/8/17.
 */

public class ActivityCheckout extends AppCompatActivity{

    WebView wv_checkout;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ImageView iv_back;
    TextView tv_storeName;
    Dialog dialog;
    DatabaseHandler db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        init();
        tv_storeName.setText(sharedPreferences.getString("storeName",""));
        String user_id = sharedPreferences.getString("user_id","");
        String url = "https://www.marginfresh.com/bitware-checkoutapp/?userId=" +user_id;
        Log.e("Checkout url",url);

        wv_checkout.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {}

            @Override
            public void onPageFinished(WebView view, String url) {
                dialog.dismiss();
                super.onPageFinished(view,url);
                if (url.equals("https://www.marginfresh.com/checkout/onepage/success/")){
                    showPlaceOrderDialog();
                }

            }

        });

        wv_checkout.loadUrl(url);
        wv_checkout.getSettings().setLoadsImagesAutomatically(true);
        wv_checkout.getSettings().setJavaScriptEnabled(true);
        wv_checkout.getSettings().setLoadWithOverviewMode(true);
        wv_checkout.getSettings().setUseWideViewPort(true);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sharedPreferences.getString("fromReorder","").equals("yes")) {
                    editor.putString("NavigationPosition","4");
                    editor.commit();
                    startActivity(new Intent(ActivityCheckout.this, DrawerActivity.class));
                    finish();
                }else
                {
                    startActivity(new Intent(ActivityCheckout.this, ActivityCart.class));
                    finish();
                }
            }
        });
    }

    private void init() {
        tv_storeName = (TextView) findViewById(R.id.tv_storeName);
        wv_checkout = (WebView) findViewById(R.id.wv_checkout);
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        iv_back = (ImageView) findViewById(R.id.iv_back);
        dialog = AppUtils.customLoader(ActivityCheckout.this);
        dialog.show();

        db = new DatabaseHandler(ActivityCheckout.this);

    }

    private void showPlaceOrderDialog() {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityCheckout.this);
            alertDialogBuilder.setTitle("Margin Fresh");
            alertDialogBuilder
                    .setMessage("Your Order Has Been Received!")
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
//                                    positio 4
                                    db.deleteRecordsFromDB(sharedPreferences.getString("user_id",""));
                                    db.deletCartTotalFromDb(sharedPreferences.getString("user_id",""));
                                    editor.putString("NavigationPosition","0");
                                    editor.putString("cartItemCount","0");
                                    editor.putString("shoppingCartId","");
                                    editor.commit();
                                    startActivity(new Intent(ActivityCheckout.this,DrawerActivity.class));
                                }
                            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        if(sharedPreferences.getString("fromReorder","").equals("yes")) {
            editor.putString("NavigationPosition","4");
            editor.commit();
            startActivity(new Intent(ActivityCheckout.this, DrawerActivity.class));
            finish();
        }else
        {
            startActivity(new Intent(ActivityCheckout.this, ActivityCart.class));
            finish();
        }
    }
}
