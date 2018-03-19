package com.marginfresh.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.marginfresh.R;

public class HowItWorksActivity extends AppCompatActivity {

    RelativeLayout rv_selectStore;
    private boolean gps_enabled=false,network_enabled=false;
    private boolean isLocationEnabled=false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_howitworks);
        initializeView();

        rv_selectStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  showSettingAlert();
                startActivity(new Intent(HowItWorksActivity.this,SelectStoreActivity.class));
                finish();
            }
        });

    }

    private void initializeView() {
        rv_selectStore= (RelativeLayout) findViewById(R.id.rv_selectStore);
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor= sharedPreferences.edit();
        editor.putString("status","howItWorks");
        editor.commit();
    }

    public void showSettingAlert(){
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        }catch (Exception ex){

        }

        if(!gps_enabled && !network_enabled){
//            isLocationEnabled=false;
            AlertDialog.Builder dialog = new AlertDialog.Builder(HowItWorksActivity.this);
            dialog.setMessage("Allow  Margin Fresh to access your location while you use the app?");
            dialog.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    isLocationEnabled=true;
                }
            });
            dialog.setNegativeButton("Don't Allow", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }else {
            isLocationEnabled=true;
            startActivity(new Intent(HowItWorksActivity.this,SelectStoreActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(HowItWorksActivity.this,LoginActivity.class));
    }
}
