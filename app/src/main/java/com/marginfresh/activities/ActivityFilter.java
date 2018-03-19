package com.marginfresh.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.BubbleThumbRangeSeekbar;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.marginfresh.Model.ModelProductList;
import com.marginfresh.R;
import com.marginfresh.domain.Config;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static java.util.Collections.sort;

/**
 * Created by bitware on 5/7/17.
 */

public class ActivityFilter extends AppCompatActivity {

    TextView tv_clear,tvMin,tvMax,tv_apply,tv_minPriceLabel,tv_maxPriceLabel;
    CrystalRangeSeekbar crystalSeekbar;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ImageView iv_back;
    String minPriceAfterFilter,maxPriceAfterFilter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_filter);

        Log.e("TAG","in onCreate");
        init();
        crystalSeekbar.clearAnimation();
        crystalSeekbar.setMinValue(Float.parseFloat(sharedPreferences.getString("minPrice","")));
        crystalSeekbar.setMaxValue(Float.parseFloat(sharedPreferences.getString("maxPrice","")));

        minPriceAfterFilter = sharedPreferences.getString("minRangeAfterFilter","");
        maxPriceAfterFilter = sharedPreferences.getString("maxRangeAfterFilter","");
        System.out.println("Min Range After Filter > "+sharedPreferences.getString("minPrice",""));
        System.out.println("Max Range After Filter > "+sharedPreferences.getString("maxPrice",""));

        tv_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String minRange = tvMin.getText().toString();
                String maxRange = tvMax.getText().toString();
                editor.putString("minRange",minRange.substring(3));
                editor.putString("maxRange",maxRange.substring(3));
                editor.putString("minRangeAfterFilter",minRange.substring(3));
                editor.putString("maxRangeAfterFilter",maxRange.substring(3));
                editor.commit();
                Config.isFiterApply = "yes";
                Intent intent = new Intent(ActivityFilter.this,ActivityProductList.class);
                startActivity(intent);
                finish();
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Config.isFiterApply = "no";
                Intent intent = new Intent(ActivityFilter.this,ActivityProductList.class);
                startActivity(intent);
                finish();
            }
        });

        crystalSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                Log.e("TAG","in seekbar change listner");
                tvMin.setText("AED"+String.valueOf(minValue));
                tvMax.setText("AED"+String.valueOf(maxValue));
            }
        });

    }

    private void init() {
        sharedPreferences=getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        crystalSeekbar = (CrystalRangeSeekbar) findViewById(R.id.rangeSeekbar3);
        tvMin = (TextView)findViewById(R.id.tv_minNew);
        tvMax = (TextView)findViewById(R.id.tv_maxNew);
        tv_apply = (TextView) findViewById(R.id.tv_apply);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        tv_minPriceLabel = (TextView) findViewById(R.id.tv_minPriceLabel);
        tv_maxPriceLabel = (TextView) findViewById(R.id.tv_maxPriceLabel);

        tvMin.setText("AED"+sharedPreferences.getString("minPrice",""));
        tvMax.setText("AED"+sharedPreferences.getString("maxPrice",""));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("TAG","in onResume");
    }

    @Override
    public void onBackPressed() {
        Config.isFiterApply = "no";
        Intent intent = new Intent(ActivityFilter.this,ActivityProductList.class);
        startActivity(intent);
        finish();
    }
}
