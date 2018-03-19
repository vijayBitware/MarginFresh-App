package com.marginfresh.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.marginfresh.R;
import com.marginfresh.utils.AppUtils;

/**
 * Created by bitware on 12/9/17.
 */

public class ActivityFAQ extends AppCompatActivity {

    WebView wv_faq;
    ImageView iv_back;
    Dialog dialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        init();
        String faqUrl = "https://www.marginfresh.com/marginfreshfaq/";
        wv_faq.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {}

            @Override
            public void onPageFinished(WebView view, String url) {
                dialog.dismiss();
                super.onPageFinished(view,url);
            }

        });
        wv_faq.loadUrl(faqUrl);
        wv_faq.getSettings().setLoadsImagesAutomatically(true);
        wv_faq.getSettings().setJavaScriptEnabled(true);
        wv_faq.getSettings().setLoadWithOverviewMode(true);
        wv_faq.getSettings().setUseWideViewPort(true);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void init() {
        wv_faq = (WebView) findViewById(R.id.wv_faq);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        dialog = AppUtils.customLoader(ActivityFAQ.this);
        dialog.show();
    }
}
