package com.marginfresh.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.marginfresh.Model.ModelCart;
import com.marginfresh.Model.ModelReferFriend;
import com.marginfresh.R;
import com.marginfresh.adapter.AdapterCart;
import com.marginfresh.adapter.AdapterInviteFriend;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by bitware on 14/8/17.
 */

public class ActivityInviteFriend extends AppCompatActivity {

    ListView lv_inviteFriend;
    String[] permissionsRequired = new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS};
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean allgranted=false;
    ArrayList<ModelReferFriend> arrFriendList;
    AdapterInviteFriend adapterInviteFriend;
    View header;
    TextView tv_directInvite;
    EditText edt_directEmail;
    ConnectionDetector cd;
    Boolean isInternetPresent;
    String email,referedToEmail,referredByUserId,referredByName;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ImageView iv_back;
    String displayName="", emailAddress="", phoneNumber="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitefriend);

        init();
        CheckPermission();
        tv_directInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                referedToEmail = edt_directEmail.getText().toString();
                if (!referedToEmail.isEmpty()){
                    if (referedToEmail.matches(Config.EMAIL_REGEX)) {
                        email = sharedPreferences.getString("customerEmail", "");
                        referredByUserId = sharedPreferences.getString("user_id", "");
                        referredByName = sharedPreferences.getString("customerName", "");
                        if (isInternetPresent) {
                            new InviteFriend().execute("{\"referredByEmail\":\"" + email + "\",\"referredToEmail\":\"" + referedToEmail + "\",\"referredByUserId\":\"" + referredByUserId + "\",\"referredByName\":\"" + referredByName + "\"}");
                        } else {
                            Toast.makeText(ActivityInviteFriend.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(ActivityInviteFriend.this,"Please Enter Valid Email Id",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(ActivityInviteFriend.this,"Please Enter Email Id",Toast.LENGTH_SHORT).show();

                }

            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*editor.putString("NavigationPosition","7");
                editor.commit();
                startActivity(new Intent(ActivityInviteFriend.this,DrawerActivity.class));*/
                finish();
            }
        });


    }

    private void init() {
        lv_inviteFriend = (ListView) findViewById(R.id.lv_inviteFriend);
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);
        cd = new ConnectionDetector(ActivityInviteFriend.this);
        isInternetPresent =cd.isConnectingToInternet();
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor= sharedPreferences.edit();
        arrFriendList = new ArrayList<>();

        LayoutInflater inflater = LayoutInflater.from(ActivityInviteFriend.this);
        header = inflater.inflate(R.layout.header_referfriend, null);
        lv_inviteFriend.addHeaderView(header);
        tv_directInvite = (TextView) header.findViewById(R.id.tv_directInvite);
        edt_directEmail = (EditText) header.findViewById(R.id.edt_directEmail);
        iv_back = (ImageView) findViewById(R.id.iv_back);

    }


    private void readContacts()
    {
        ContentResolver cr =getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext())
        {
            displayName="";emailAddress="";
            displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor emails = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id, null, null);
            while (emails.moveToNext())
            {
                emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                break;
            }
            emails.close();
            if (!emailAddress.equals("")){
                ModelReferFriend modelReferFriend = new ModelReferFriend();
                modelReferFriend.setFriendName(displayName);
                modelReferFriend.setFriendEmail(emailAddress);
                arrFriendList.add(modelReferFriend);
            }

        }
        System.out.println("Friend List Size > " +arrFriendList.size());
        adapterInviteFriend = new AdapterInviteFriend(ActivityInviteFriend.this,R.layout.row_invitefriend,arrFriendList);
        lv_inviteFriend.setAdapter(adapterInviteFriend);
        cursor.close();
    }
    public void CheckPermission(){
        if(ActivityCompat.checkSelfPermission(ActivityInviteFriend.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(ActivityInviteFriend.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(ActivityInviteFriend.this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityInviteFriend.this,permissionsRequired[1])){
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivityInviteFriend.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Location permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ActivityInviteFriend.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0],false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivityInviteFriend.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Location permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant  Camera and Location", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }  else {
                //just request the permission
                ActivityCompat.requestPermissions(ActivityInviteFriend.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0],true);
            editor.commit();
        } else {
            readContacts();
        }

    }

    @Override
    public void onBackPressed() {
        /*editor.putString("NavigationPosition","7");
        editor.commit();
        startActivity(new Intent(ActivityInviteFriend.this,DrawerActivity.class));*/
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        if(requestCode == PERMISSION_CALLBACK_CONSTANT){
            //check if all permissions are granted
            boolean allgranted = false;
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if(allgranted){
//                proceedAfterPermission();

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(ActivityInviteFriend.this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityInviteFriend.this,permissionsRequired[1])){
//                txtPermissions.setText("Permissions Required");
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivityInviteFriend.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ActivityInviteFriend.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(),"Unable to get Permission",Toast.LENGTH_LONG).show();
            }
        }
    }

    class InviteFriend extends AsyncTask<String, Void, String> {
        Dialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = AppUtils.customLoader(ActivityInviteFriend.this);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            com.squareup.okhttp.Response response = null;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL_SHOPCART+"referafriend/referafriend.php?"+"referredByEmail="+email+"&referredToEmail="+referedToEmail+"&referredByUserId="+referredByUserId+"&referredByName="+referredByName)
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
            System.out.println(">>>Invite Friend Result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
                        Toast.makeText(ActivityInviteFriend.this,message,Toast.LENGTH_SHORT).show();
                        edt_directEmail.setText("");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityInviteFriend.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                Toast.makeText(ActivityInviteFriend.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
