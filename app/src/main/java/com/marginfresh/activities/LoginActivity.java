package com.marginfresh.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.method.PasswordTransformationMethod;
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
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.marginfresh.R;
import com.marginfresh.db_utils.DBConstants;
import com.marginfresh.db_utils.DatabaseHandler;
import com.marginfresh.domain.Config;
import com.marginfresh.utils.AppUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "GoogleActivity";
    EditText edit_email, edit_password;
    TextView tv_register, tv_login, tv_google, tv_forgotPass, tv_fb;
    String user_email = " ", user_password = " ", isGoogleLogin = "", isFacebookLogin = "", fbEmail = "", fbContact = "", fbFirstName = "", fbLastName = "", fbId = "", fbImgUrl = "";
    Button btn_Login;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private String response_msg = "", googleId = "", googleName = "", googleEmail = "", referalCode = "", userId, devieId, deviceType, fcm_key, googleFirstName, googleLastName;
    Boolean isInternetPresent = false;
    private static final int RC_SIGN_IN = 9001;
    public GoogleApiClient mGoogleApiClient;
    CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;
    LoginButton btn_facebookLogin;
    public int screenWidth, screenHeight;
    SignInButton googleSignInButton;
    GoogleSignInOptions gso;
    String[] permissionsRequired = new String[]{Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION};

    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean allgranted = false;
    Dialog dialogServices;
    GPSTracker gpsTracker;
    double lattitude, longitude;
    boolean isCheckGPS = false;
    Dialog dialog;
    DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        initView();

        db = new DatabaseHandler(this);
        if (AppUtils.IsDatabaseExist(LoginActivity.this,
                DBConstants.DATABASE_NAME)) {
            Log.e("DB EXIST or NOT :", "DB-Exist");
        } else {
            Log.e("ActivityCategory:", "DB-NOT Exist--> So Creating New");
        }

        CheckPermission();

        fcm_key = FirebaseInstanceId.getInstance().getToken();
        callbackManager = CallbackManager.Factory.create();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
                finish();
            }
        });

        tv_login.setOnClickListener(new LoginClickListener());

        btn_facebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginWithFacebook();
            }
        });
        tv_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGoogleLogin = "yes";
                signIn();
            }
        });

        tv_forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ActivityForgotPassword.class);
                startActivity(intent);

            }
        });

        edit_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edit_password.getWindowToken(), 0);

                    user_email = edit_email.getText().toString();
                    user_password = edit_password.getText().toString();

                    if (!user_email.isEmpty() && !user_password.isEmpty()) {
                        if (user_email.matches(Config.EMAIL_REGEX)) {
                            if (!isInternetPresent) {
                                new LoginTask().execute("{\"userEmail\":\"" + user_email + "\",\"userPassword\":\"" + user_password + "\"}");
                            } else {
                                Toast.makeText(LoginActivity.this, "Please Check Internet Connection.!!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Enter Valid Email", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Enter Valid Email or Password", Toast.LENGTH_SHORT).show();
                    }

                }

                return false;
            }
        });

    }

    private void AskLocationCheck() {
        if (gpsTracker.canGetLocation()) {
            lattitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
            System.out.println("Current Location >>> Lattitude-" + lattitude + "Longitude-" + longitude);
            editor.putString("lattitude", String.valueOf(lattitude));
            editor.putString("longitude", String.valueOf(longitude));
            editor.commit();

        } else {
            showLocationDialog();
        }
    }

    private void showLocationDialog() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
            dialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
            dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    isCheckGPS = true;
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });

            dialog.show();
        }
    }

    public void onClick(View v) {
        if (v == tv_fb) {
            btn_facebookLogin.performClick();
        }
    }

    private void signIn() {
        if (!isInternetPresent) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } else {
            Toast.makeText(LoginActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void loginWithFacebook() {
        btn_facebookLogin.setReadPermissions(Arrays.asList("public_profile,email,user_birthday,user_friends"));
        callbackManager = CallbackManager.Factory.create();
        btn_facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                System.out.println("GraphResponse >> " + response);
                                LoginManager.getInstance().logOut();
                                try {
                                    if (!object.getString("id").equals("") || !object.getString("id").equals("null") || !object.getString("id").isEmpty()) {

                                        fbId = object.getString("id");
                                        fbImgUrl = fbImgUrl = "https://graph.facebook.com/" + fbId + "/picture?type=large";
                                        String fbName = object.getString("name");
                                        String fbEmail;
                                        if (object.has("email")) {
                                            fbEmail = object.getString("email");
                                        } else {
                                            fbEmail = " ";
                                        }
                                        isFacebookLogin = "yes";
                                        fbFirstName = object.getString("first_name");
                                        fbLastName = object.getString("last_name");
                                        editor.putString("fbemail", fbEmail);
                                        editor.commit();

                                        if (!isInternetPresent) {
                                            new CheckFacebookUserExits().execute("{\"facebookid\":\"" + fbId + "\"}");
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Please Check Internet Connection.!!", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Log.e("Facebook >> ", "Failed to connect facebook...");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday,first_name,last_name");
                request.setParameters(parameters);
                request.executeAsync();

                accessTokenTracker = new AccessTokenTracker() {
                    @Override
                    protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                               AccessToken currentAccessToken) {
                        if (currentAccessToken == null) {
                        }
                    }
                };
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            googleId = acct.getId();
            googleEmail = acct.getEmail();
            googleName = acct.getDisplayName();
            isGoogleLogin = "yes";
            System.out.println("Google NAME >" + googleName);
            System.out.println("Google email > " + googleEmail);
            if (!acct.getDisplayName().isEmpty() && !(acct.getDisplayName() == null)) {
                String[] str = googleName.split(" ");
                googleFirstName = str[0];
                if (!(str[1] == null)) {
                    googleLastName = str[1];
                }
            } else {
                Toast.makeText(LoginActivity.this, "Not getting user name", Toast.LENGTH_SHORT).show();

            }
            System.out.println("Google First Name > " + googleFirstName + " Google Last Name > " + googleLastName);
            editor.putString("googleEmail", googleEmail);
            editor.commit();

            if (!isInternetPresent) {
                new CheckGoogleUserExits().execute("{\"googleid\":\"" + googleId + "\"}");
            } else {
                Toast.makeText(LoginActivity.this, "Please Check Internet Connection.!!", Toast.LENGTH_SHORT).show();
            }

        } else {
            Log.e("Google Login >", "Failed to connect");

        }
    }

    private void showNumberDialog() {
        dialogServices = new Dialog(LoginActivity.this);
        dialogServices.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogServices.setContentView(R.layout.dialog_email_number);

        final EditText edtEmail = (EditText) dialogServices.findViewById(R.id.edt_email);
        final EditText edtContact = (EditText) dialogServices.findViewById(R.id.edt_contactNumber);
        TextView tvSave = (TextView) dialogServices.findViewById(R.id.tv_save);
        final EditText edt_referalCode = (EditText) dialogServices.findViewById(R.id.edt_referalCode);

        if (isFacebookLogin.equals("yes")) {
            edtEmail.setText(sharedPreferences.getString("fbemail", ""));
        } else if (isGoogleLogin.equals("yes")) {
            edtEmail.setText(sharedPreferences.getString("googleEmail", ""));
        }

        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtContact.getWindowToken(), 0);
                fbEmail = edtEmail.getText().toString();
                fbContact = edtContact.getText().toString();
                referalCode = edt_referalCode.getText().toString();

                if (!fbEmail.isEmpty() && fbEmail.contains(" ")) {
                    String[] arr = fbEmail.split(" ");
                    String strA = arr[0];
                    fbEmail = arr[1];
                }

                System.out.println("FB Email >" + fbEmail);
                if (!fbEmail.isEmpty()) {
                    if (fbEmail.matches(Config.EMAIL_REGEX)) {
                        if (!fbContact.isEmpty()) {
                            if (!(fbContact.length() < 10)) {
                                if (isFacebookLogin.equals("yes")) {
                                    if (!isInternetPresent) {
                                        new LoginWithFacebook().execute("{\"firstName\":\"" + fbFirstName + "\",\"lastName\":\"" + fbLastName + "\",\"emailid\":\"" + fbEmail + "\",\"fbID\":\"" + fbId + "\",\"phoneNO\":\"" + fbContact + "\",\"referalCodeFetched\":\"" + referalCode + "\"}");
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Please Check Internet Connection.!!", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (!isInternetPresent) {
                                        new LoginWithGoogle().execute("{\"firstName\":\"" + googleFirstName + "\",\"lastName\":\"" + googleLastName + "\",\"emailid\":\"" + fbEmail + "\",\"googleid\":\"" + googleId + "\",\"phoneNO\":\"" + fbContact + "\",\"referalCodeFetched\":\"" + referalCode + "\"}");
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Please Check Internet Connection.!!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                edtContact.setError("Please Enter Valid Contact Number");
                                edtContact.requestFocus();
                            }
                        } else {
                            edtContact.setError("Please Enter Contact Number");
                            edtContact.requestFocus();
                        }
                    } else {
                        edtEmail.setError("Please Enter Valid Email");
                        edtEmail.requestFocus();
                    }
                } else {
                    edtEmail.setError("Please Enter Email");
                    edtEmail.requestFocus();
                }
            }
        });

        WindowManager.LayoutParams wmlp = dialogServices.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER_HORIZONTAL;
        dialogServices.show();
        dialogServices.setCanceledOnTouchOutside(false);
        dialogServices.getWindow().setLayout((screenWidth / 2) + 300, (screenHeight / 2) + 50);
    }

    private void initView() {
        tv_fb = (TextView) findViewById(R.id.fb);
        sharedPreferences = getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        tv_register = (TextView) findViewById(R.id.tv_register);
        tv_login = (TextView) findViewById(R.id.btn_Login);
        tv_google = (TextView) findViewById(R.id.tv_google);
        tv_forgotPass = (TextView) findViewById(R.id.tv_forgotPass);
        edit_password = (EditText) findViewById(R.id.edit_password);
        edit_password.setTransformationMethod(new PasswordTransformationMethod());
        edit_email = (EditText) findViewById(R.id.edit_email);
        btn_facebookLogin = (LoginButton) findViewById(R.id.btn_facebookLogin);
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);
        gpsTracker = new GPSTracker(LoginActivity.this);
        dialog = AppUtils.customLoader(LoginActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private class LoginClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edit_password.getWindowToken(), 0);

            user_email = edit_email.getText().toString();
            user_password = edit_password.getText().toString();

            if (!user_email.isEmpty() && !user_password.isEmpty()) {
                if (!user_email.isEmpty()) {
                    if (user_email.matches(Config.EMAIL_REGEX)) {
                        if (!user_password.isEmpty()) {
                            if (!isInternetPresent) {
                                new LoginTask().execute("{\"userEmail\":\"" + user_email + "\",\"userPassword\":\"" + user_password + "\"}");
                            } else {
                                Toast.makeText(LoginActivity.this, "Please Check Internet Connection.!!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            edit_password.setError("Please Enter Password");
                            edit_password.requestFocus();
                        }
                    } else {
                        edit_email.setError("Please Enter Valid Email");
                        edit_email.requestFocus();
                    }
                } else {
                    edit_email.setError("Please Enter Email");
                    edit_email.requestFocus();
                }
            } else {

                Toast.makeText(LoginActivity.this, "Enter Valid Email Or Password", Toast.LENGTH_SHORT).show();
            }
        }

    }

    class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "login.php?" + "userEmail=" + user_email + "&" + "userPassword=" + user_password)
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
            System.out.println(">>> Login result :" + s);
            dialog.dismiss();

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if (status.equals("1")) {
                        Toast.makeText(LoginActivity.this, response_msg, Toast.LENGTH_SHORT).show();
                        String user_id = jsonObject.getString("user_id");
                        String addressAvailable = jsonObject.getString("addressAvailable");
                        editor.putString("addressAvailable", addressAvailable);
                        editor.putString("customerName", jsonObject.getString("name"));
                        editor.putString("customerEmail", jsonObject.getString("email_id"));
                        editor.putString("customerContact", jsonObject.getString("contact_no"));
                        editor.putString("customerFirstName", jsonObject.getString("firstName"));
                        editor.putString("customerLastName", jsonObject.getString("lastName"));
                        editor.putString("loginType", "normalLogin");
                        editor.putString("user_id", user_id);
                        editor.putString("NavigationPosition", "0");
                        editor.putString("status", "isLogin");
                        editor.putString("currency", "AED");
                        editor.commit();
                        userId = sharedPreferences.getString("user_id", "");
                        fcm_key = FirebaseInstanceId.getInstance().getToken();
                        new SaveDeviceId().execute("{\"userId\":\"" + userId + "\",\"deviceId\":\"" + fcm_key + "\",\"deviceType\":\"" + "android" + "\"}");
                    } else {
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, response_msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            } else {
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class CheckFacebookUserExits extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "fb_login.php?" + "facebookid=" + fbId)
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
            System.out.println(">>> Check facebook user result :" + s);
            dialog.dismiss();

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if (status.equals("1")) {
                        String user_id = jsonObject.getString("user_id");
                        String name = jsonObject.getString("name");
                        String email_id = jsonObject.getString("email_id");
                        String contact_no = jsonObject.getString("contact_no");
                        String fbID = jsonObject.getString("fbID");
                        String addressAvailable = jsonObject.getString("addressAvailable");

                        editor.putString("addressAvailable", addressAvailable);
                        editor.putString("loginType", "facebookLogin");
                        editor.putString("user_id", user_id);
                        editor.putString("customerName", name);
                        editor.putString("customerFirstName", jsonObject.getString("firstName"));
                        editor.putString("customerLastName", jsonObject.getString("lastName"));
                        editor.putString("customerContact", contact_no);
                        editor.putString("customerEmail", email_id);
                        editor.putString("status", "isLogin");
                        editor.putString("currency", "AED");
                        editor.commit();

                        userId = sharedPreferences.getString("user_id", "");
                        fcm_key = FirebaseInstanceId.getInstance().getToken();
                        new SaveDeviceId().execute("{\"userId\":\"" + userId + "\",\"deviceId\":\"" + fcm_key + "\",\"deviceType\":\"" + "android" + "\"}");

                    } else {
                        dialog.dismiss();
                        showNumberDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            } else {
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class LoginWithFacebook extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "fb_social.php?" + "firstName=" + fbFirstName + "&" + "lastName=" + fbLastName + "&" + "emailid=" + fbEmail + "&" + "fbID=" + fbId + "&" + "phoneNO=" + fbContact + "&referalCodeFetched=" + referalCode)
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

            System.out.println(">>> Login with facebook result :" + s);
            dialog.dismiss();

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");
                    if (!response_msg.equals("Referral code is not valid")) {
                        dialogServices.dismiss();
                    }
                    if (status.equals("1")) {

                        String contact_no = jsonObject.getString("contact_no");
                        String email_id = jsonObject.getString("email_id");
                        String name = jsonObject.getString("name");
                        String user_id = jsonObject.getString("user_id");

                        String addressAvailable = jsonObject.getString("addressAvailable");

                        editor.putString("addressAvailable", addressAvailable);
                        editor.putString("customerFirstName", jsonObject.getString("firstName"));
                        editor.putString("customerLastName", jsonObject.getString("lastName"));
                        editor.putString("loginType", "facebookLogin");
                        editor.putString("user_id", user_id);
                        editor.putString("customerName", name);
                        editor.putString("customerContact", contact_no);
                        editor.putString("customerEmail", email_id);
                        editor.putString("status", "isLogin");
                        editor.putString("currency", "AED");
                        editor.commit();

                        userId = sharedPreferences.getString("user_id", "");
                        fcm_key = FirebaseInstanceId.getInstance().getToken();
                        new SaveDeviceId().execute("{\"userId\":\"" + userId + "\",\"deviceId\":\"" + fcm_key + "\",\"deviceType\":\"" + "android" + "\"}");

                    } else {
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, response_msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            } else {
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class CheckGoogleUserExits extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "google_login.php?" + "googleid=" + googleId)
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
            System.out.println(">>> Check Google user result :" + s);
            dialog.dismiss();

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if (status.equals("1")) {
                        String user_id = jsonObject.getString("user_id");
                        String name = jsonObject.getString("name");
                        String email_id = jsonObject.getString("email_id");
                        String contact_no = jsonObject.getString("contact_no");
                        String googleId = jsonObject.getString("googleID");

                        String addressAvailable = jsonObject.getString("addressAvailable");

                        editor.putString("addressAvailable", addressAvailable);
                        editor.putString("customerFirstName", jsonObject.getString("firstName"));
                        editor.putString("customerLastName", jsonObject.getString("lastName"));
                        editor.putString("loginType", "googleLogin");
                        editor.putString("user_id", user_id);
                        editor.putString("customerName", name);
                        editor.putString("customerContact", contact_no);
                        editor.putString("customerEmail", email_id);
                        editor.putString("status", "isLogin");
                        editor.putString("currency", "AED");
                        editor.commit();

                        userId = sharedPreferences.getString("user_id", "");
                        fcm_key = FirebaseInstanceId.getInstance().getToken();
                        new SaveDeviceId().execute("{\"userId\":\"" + userId + "\",\"deviceId\":\"" + fcm_key + "\",\"deviceType\":\"" + "android" + "\"}");

                    } else {
                        dialog.dismiss();
                        showNumberDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            } else {
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class LoginWithGoogle extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "google_social.php?" + "firstName=" + googleFirstName + "&" + "lastName=" + googleLastName + "&" + "emailid=" + googleEmail + "&" + "googleid=" + googleId + "&" + "phoneNO=" + fbContact + "&referalCodeFetched=" + referalCode)
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

            System.out.println(">>> Login with google result :" + s);
            dialog.dismiss();

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");
                    if (response_msg.equals("User registered successfully")) {
                        dialogServices.dismiss();
                    }

                    if (status.equals("1")) {

                        String contact_no = jsonObject.getString("contact_no");
                        String email_id = jsonObject.getString("email_id");
                        String name = jsonObject.getString("name");
                        String user_id = jsonObject.getString("user_id");
                        String addressAvailable = jsonObject.getString("addressAvailable");

                        editor.putString("addressAvailable", addressAvailable);
                        editor.putString("loginType", "googleLogin");
                        editor.putString("user_id", user_id);
                        editor.putString("customerName", name);
                        editor.putString("customerContact", contact_no);
                        editor.putString("customerEmail", email_id);
                        editor.putString("customerFirstName", jsonObject.getString("firstName"));
                        editor.putString("customerLastName", jsonObject.getString("lastName"));
                        editor.putString("status", "isLogin");
                        editor.putString("currency", "AED");
                        editor.commit();

                        userId = sharedPreferences.getString("user_id", "");
                        fcm_key = FirebaseInstanceId.getInstance().getToken();
                        new SaveDeviceId().execute("{\"userId\":\"" + userId + "\",\"deviceId\":\"" + devieId + "\",\"deviceType\":\"" + "android" + "\"}");

                    } else {
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, response_msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            } else {
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class SaveDeviceId extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL_SHOPCART + "deviceid/deviceIdCheck.php?" + "userId=" + userId + "&" + "deviceId=" + fcm_key + "&" + "deviceType=" + "android")
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

            System.out.println(">>> Save device id  result :" + s);
            dialog.dismiss();

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if (status.equals("1")) {
                        if (sharedPreferences.getString("isUserRegister", "").equals("yes")) {
                            startActivity(new Intent(LoginActivity.this, SelectStoreActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(LoginActivity.this, HowItWorksActivity.class));
                            finish();
                        }

                    } else {
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, response_msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            } else {
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
        alertDialogBuilder.setTitle("Exit Application");
        alertDialogBuilder
                .setMessage("Are you sure you want to exit ?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                                homeIntent.addCategory(Intent.CATEGORY_HOME);
                                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(homeIntent);
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void CheckPermission() {
        if (ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[1])) {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Location permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(LoginActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this);
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
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(LoginActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.commit();
        } else {

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }
            if (allgranted) {
                TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                String uid = tManager.getDeviceId();
                Log.e("Device id >",uid);
               // AskLocationCheck();

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this,permissionsRequired[1])){
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Camera and Location permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(LoginActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
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
}
