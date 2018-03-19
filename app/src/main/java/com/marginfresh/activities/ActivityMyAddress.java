package com.marginfresh.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.marginfresh.R;
import com.marginfresh.db_utils.DatabaseHandler;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by bitware on 6/6/17.
 */

public class ActivityMyAddress extends AppCompatActivity {

    ImageView iv_back,iv_locationIcon;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Button btn_addAddress;
    EditText edt_name,edt_contact,edt_email,edt_address,edt_streetName,edt_building,edt_flatNo,edt_lastName;
    String user_id="",name="",email="",contact="",address="",streetName="",flatNo="",buildingNo="",state="",country="",
            telephoneNumber="",city="",postCode="",lastName="";
    TextView tv_address,edit_locality;
    ConnectionDetector cd;
    Boolean isInternetPresent,isLocationClicked =true;
    Button btn_cart,btn_notiCount;
    public int screenWidth, screenHeight;
    RelativeLayout rl_cart,rl_notification;
    String colored = "*";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_address);
        init();

        edt_name.setText(sharedPreferences.getString("customerFirstName",""));
        edt_lastName.setText(sharedPreferences.getString("customerLastName",""));
        edt_contact.setText(sharedPreferences.getString("customerContact",""));

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getString("myAddress","").equals("cart")){
                    editor.putString("fromMyAddress","yes");
                    editor.commit();
                    startActivity(new Intent(ActivityMyAddress.this,ActivityCart.class));
                    finish();
                }else if(sharedPreferences.getString("myAddress","").equals("saveAddress")){
                    startActivity(new Intent(ActivityMyAddress.this,ActivitySavedAddress.class));
                    finish();
                }
            }
        });

        edt_contact.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT){
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edt_contact.getWindowToken(), 0);
                    try {
                        if (isLocationClicked) {
                            isLocationClicked = false;
                            Intent intent = new PlaceAutocomplete
                                    .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(ActivityMyAddress.this);
                            startActivityForResult(intent, 1);
                        }
                    } catch (GooglePlayServicesRepairableException e) {
                        // TODO: Handle the error.
                    } catch (GooglePlayServicesNotAvailableException e) {
                        // TODO: Handle the error.
                    }
                }
                return false;
            }
        });
        btn_addAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name =  edt_name.getText().toString();
                lastName = edt_lastName.getText().toString();
                telephoneNumber = edt_contact.getText().toString();
                buildingNo =edt_building.getText().toString();
                flatNo = edt_flatNo.getText().toString();
                user_id = sharedPreferences.getString("user_id","");
              //  streetName = edit_locality.getText().toString();
                System.out.println("Street Name > " +streetName);
                if (!name.isEmpty()){
                    if (!lastName.isEmpty()) {
                        if (!telephoneNumber.isEmpty()) {
                            if (!(telephoneNumber.length() < 10)) {
                                if (!streetName.isEmpty()) {
                                    if (!buildingNo.isEmpty()) {
                                        if (!flatNo.isEmpty()) {
                                            if (isInternetPresent) {
                                                new AddAddress().execute("{\"customerId\":\"" + user_id + "\",\"firstName\":\"" + name + "\",\"lastName\":\""
                                                        + lastName + "\",\"street\":\"" + streetName + "\",\"city\":\"" + city + "\",\"state\":\"" + state
                                                        + "\",\"postcode\":\"" + postCode + "\",\"country_id\":\"" + country + "\",\"telephone\":\"" + telephoneNumber
                                                        + "\",\"buildingnumber\":\"" + buildingNo + "\",\"flatnumber\":\"" + flatNo
                                                        + "\",\"addressId\":\"" + "" + "\",\"addressUpdate\":\"" + "" + "\"}");
                                            } else {
                                                Toast.makeText(ActivityMyAddress.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            edt_flatNo.setError("Enter Flat Number");
                                            edt_flatNo.requestFocus();
                                        }
                                    } else {
                                        edt_building.setError("Enter Building Number");
                                        edt_building.requestFocus();
                                    }
                                } else {
                                    iv_locationIcon.setVisibility(View.GONE);
                                    edit_locality.setError("Enter Address");
                                    edit_locality.requestFocus();
                                }
                            } else {
                                edt_contact.setError("Enter Valid Number");
                                edt_contact.requestFocus();
                            }
                        } else {
                            edt_contact.setError("Enter Contact Number");
                            edt_contact.requestFocus();
                        }
                    }else {
                        edt_lastName.setError("Enter Last Name");
                        edt_lastName.requestFocus();
                    }
                }else {
                    edt_name.setError("Enter First Name");
                    edt_name.requestFocus();
                }

            }
        });

        edit_locality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isLocationClicked) {
                        isLocationClicked = false;
                        Intent intent = new PlaceAutocomplete
                                .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .build(ActivityMyAddress.this);
                        startActivityForResult(intent, 1);
                        edit_locality.setError(null);
                    }
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        rl_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityMyAddress.this,ActivityNotificationList.class));
            }
        });

        rl_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityMyAddress.this,ActivityCart.class));
            }
        });
    }

    private void init() {
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        cd = new ConnectionDetector(ActivityMyAddress.this);
        isInternetPresent = cd.isConnectingToInternet();
        iv_back= (ImageView) findViewById(R.id.iv_back);
        btn_addAddress= (Button) findViewById(R.id.btn_addAddress);
        edt_contact = (EditText) findViewById(R.id.edt_contact);
        edt_email = (EditText) findViewById(R.id.edt_email);
        edt_building = (EditText) findViewById(R.id.edt_building);
        edt_flatNo = (EditText) findViewById(R.id.edt_flat);
        edit_locality = (TextView) findViewById(R.id.edit_locality);
        edt_name = (EditText) findViewById(R.id.edt_firstname);
        iv_locationIcon = (ImageView) findViewById(R.id.iv_locationIcon);
        edt_lastName = (EditText) findViewById(R.id.edt_lastname);
        btn_cart = (Button) findViewById(R.id.btn_cart);
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        rl_cart= (RelativeLayout) findViewById(R.id.rlCart);
        rl_notification = (RelativeLayout) findViewById(R.id.rl_notification);
        btn_notiCount = (Button) findViewById(R.id.btn_notiCount);

        String firstName = "First Name";
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(firstName);
        int start = builder.length();
        builder.append(colored);
        int end = builder.length();
        builder.setSpan(new ForegroundColorSpan(Color.RED), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edt_name.setHint(builder);

        String lastName = "Last Name";
        SpannableStringBuilder builderL = new SpannableStringBuilder();
        builderL.append(lastName);
        int startL = builderL.length();
        builderL.append(colored);
        int endL = builderL.length();
        builderL.setSpan(new ForegroundColorSpan(Color.RED), startL, endL,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edt_lastName.setHint(builderL);

        String contact = "Contact No";
        SpannableStringBuilder contactBulder = new SpannableStringBuilder();
        contactBulder.append(contact);
        int contactstart = contactBulder.length();
        contactBulder.append(colored);
        int contactEnd = contactBulder.length();
        contactBulder.setSpan(new ForegroundColorSpan(Color.RED), contactstart, contactEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edt_contact.setHint(contactBulder);

        String buildingNo = "Building No.";
        SpannableStringBuilder buildingBuilder = new SpannableStringBuilder();
        buildingBuilder.append(buildingNo);
        int buildingStart = buildingBuilder.length();
        buildingBuilder.append(colored);
        int buildingEnd = buildingBuilder.length();
        buildingBuilder.setSpan(new ForegroundColorSpan(Color.RED), buildingStart, buildingEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edt_building.setHint(buildingBuilder);

        String flatNo = "Flat No.";
        SpannableStringBuilder flatBuilder = new SpannableStringBuilder();
        flatBuilder.append(flatNo);
        int flatStart = flatBuilder.length();
        flatBuilder.append(colored);
        int flatEnd = flatBuilder.length();
        flatBuilder.setSpan(new ForegroundColorSpan(Color.RED), flatStart, flatEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edt_flatNo.setHint(flatBuilder);

        String locality = "Locality/Street Name";
        SpannableStringBuilder locationBuilder = new SpannableStringBuilder();
        locationBuilder.append(locality);
        int locationStart = locationBuilder.length();
        locationBuilder.append(colored);
        int locationEnd = locationBuilder.length();
        locationBuilder.setSpan(new ForegroundColorSpan(Color.RED), locationStart, locationEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edit_locality.setText(locationBuilder);

    }

    class AddAddress extends AsyncTask<String, Void, String> {
        Dialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = AppUtils.customLoader(ActivityMyAddress.this);
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
                    .url(Config.BASE_URL+"customeraddressupdate.php?"+"customerId="+user_id+"&firstName="+name+"&lastName="+lastName+"&street="+streetName
                            +"&city="+city+"&state="+state+"&postcode="+postCode+"&country_id="+country
                            +"&telephone="+telephoneNumber+"&buildingnumber="+buildingNo+"&flatnumber="+flatNo+"&addressId="+""+"&addressUpdate="+"")
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
            System.out.println(">>> Update Address result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
                        Toast.makeText(ActivityMyAddress.this,message,Toast.LENGTH_SHORT).show();
                        editor.putString("addressAvailable","1");
                        editor.commit();
                        if (sharedPreferences.getString("myAddress","").equals("cart")){
                            startActivity(new Intent(ActivityMyAddress.this,ActivityCheckout.class));
                        }else {
                            startActivity(new Intent(ActivityMyAddress.this,ActivitySavedAddress.class));
                        }
                    }else{
                        dialog.dismiss();
                        Toast.makeText(ActivityMyAddress.this,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.dismiss();
                    Toast.makeText(ActivityMyAddress.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                dialog.dismiss();
                Toast.makeText(ActivityMyAddress.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // retrive the data by using getPlace() method.
                Place place = PlaceAutocomplete.getPlace(this, data);
                String address = String.valueOf(place.getAddress());
                System.out.println("Address : " +address);
                edit_locality.setText(address);
                edit_locality.setTextColor(getResources().getColor(R.color.black));
                streetName = address;
                getLocationFromAddress(ActivityMyAddress.this,address);
                iv_locationIcon.setVisibility(View.GONE);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.e("Tag", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public void getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        String pinCode="";

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 1);
            if (!(address == null)) {
                Address location = address.get(0);
                System.out.println("Place Country: " + location.getCountryCode());
                System.out.println("PLace Postal Code : " +location.getPostalCode());
                System.out.println("Place Locality :" +location.getLocality());
                System.out.println("Place State :"   +location.getLocale());
                System.out.println("PLace Admin Area :"  +location.getAdminArea());
                System.out.println("Place SubAdmin Area:" +location.getSubAdminArea());
//                streetName = strAddress;
                city = location.getSubAdminArea();
                country = location.getCountryCode();
                state = location.getAdminArea();
                pinCode = location.getPostalCode();

            }


        } catch (IOException ex) {

            ex.printStackTrace();
        }
        System.out.println("PinCode > " +pinCode);
        if (pinCode==null){
            //showNumberDialog();
            pinCode = "";
        }else {
            postCode = pinCode;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (sharedPreferences.getString("myAddress","").equals("cart")){
            editor.putString("fromMyAddress","yes");
            editor.commit();
            startActivity(new Intent(ActivityMyAddress.this,ActivityCart.class));
            finish();
        }else if(sharedPreferences.getString("myAddress","").equals("saveAddress")){
//            startActivity(new Intent(ActivityMyAddress.this,ActivitySavedAddress.class));
            finish();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        isLocationClicked=true;
        DatabaseHandler db = new DatabaseHandler(ActivityMyAddress.this);
        String cartCount = db.getProductsInCartCount(sharedPreferences.getString("user_id",""));
        System.out.println("Activity Cart > cart count is " +cartCount);
        if (cartCount.equals("0") || cartCount.equals("")){
            btn_cart.setVisibility(View.GONE);
        }else {
            btn_cart.setVisibility(View.VISIBLE);
            btn_cart.setText(cartCount);
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
