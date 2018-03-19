package com.marginfresh.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.marginfresh.Fonts.LatoMediunItalicEdit;
import com.marginfresh.R;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.security.AccessController.getContext;


public class RegistrationActivity extends AppCompatActivity {
    TextView tv_login;
    TextView btn_Register;
    String userFirstname = "", userContact = "", userLocality = "", userBuilding = "", userEmail = "", userPassword = "",referalCode="",userLastName="";
    ConnectionDetector cd;
    Boolean isInternetPresent,isTermsAgree= false;
    SharedPreferences sharedPreferences;
    private String response_msg = "";
    SharedPreferences.Editor editor;
    EditText edtRegFirstName,edtRegContact,edtRegLocality,edtRegBuilding,edtRegEmail,edtRegPassword,edit_referalCode,edtLastName;
    ImageView iv_locationIcon,iv_check,iv_uncheck;
    String colored = "*";
    String firstName = "First Name";
    TextInputLayout tv_firstName,tl_password;
    CheckBox cb_terms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        tv_login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        iv_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTermsAgree){
                    iv_check.setImageResource(R.drawable.checkterms);
                    isTermsAgree=true;
                }else {
                    iv_check.setImageResource(R.drawable.uncheck);
                    isTermsAgree=false;
                }
            }
        });
        /*edtRegPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                tl_password.setPasswordVisibilityToggleEnabled(true);
                edtRegPassword.requestFocus();
            }
        });*/
        edtRegPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tl_password.setPasswordVisibilityToggleEnabled(true);
                edtRegPassword.requestFocus();
            }
        });
        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtRegPassword.getWindowToken(), 0);
                userFirstname = edtRegFirstName.getText().toString();
                userContact = edtRegContact.getText().toString();
                userEmail = edtRegEmail.getText().toString();
                userPassword = edtRegPassword.getText().toString();
                referalCode = edit_referalCode.getText().toString();
                userLastName = edtLastName.getText().toString();

                if(userFirstname.isEmpty()&& userLastName.isEmpty() && userContact.isEmpty() && userEmail.isEmpty() && userPassword.isEmpty()){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RegistrationActivity.this);
                    alertDialogBuilder
                            .setMessage("Please Fill All Fields")
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }else {
                    if (!userFirstname.isEmpty()){
                        if (!userLastName.isEmpty()) {
                            if (!userContact.isEmpty()) {
                                if ((userContact.length() >= 10) && userContact.length() <=12) {
                                    if (!userEmail.isEmpty()) {
                                        if (userEmail.matches(Config.EMAIL_REGEX)) {
                                            if (!userPassword.isEmpty()) {
                                                if (!userPassword.contains(" ")) {
                                                    if (!(userPassword.length() < 6)) {
                                                        //service call
                                                        if (isTermsAgree==true) {
                                                            if (isInternetPresent) {
                                                                new SignUpTask().execute("{\"firstname\":\"" + userFirstname + "\",\"lastname\":\"" + userLastName + "\",\"contact_no\":\"" + userContact + "\",\"locality\":\"" + userLocality + "\",\"building_no\":\"" + userBuilding + "\",\"email_id\":\"" + userEmail + "\",\"password\":\"" + userPassword + "\",\"referalCodeFetched\":\"" + referalCode + "\"}");
                                                            } else {
                                                                Toast.makeText(RegistrationActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                                                            }
                                                        }else {
                                                            Toast.makeText(RegistrationActivity.this,"Please Agree Terms And Conditions",Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        tl_password.setPasswordVisibilityToggleEnabled(false);
                                                        edtRegPassword.setError("Password should be min 6 chars");
                                                        edtRegPassword.requestFocus();
                                                      //  tl_password.setPasswordVisibilityToggleEnabled(false);
                                                    }
                                                }else{
                                                    tl_password.setPasswordVisibilityToggleEnabled(false);
                                                    edtRegPassword.setError("Space Is Not Allowed In Password");
                                                    edtRegPassword.requestFocus();
                                                 //   tl_password.setPasswordVisibilityToggleEnabled(false);
                                                }
                                            } else {
                                                tl_password.setPasswordVisibilityToggleEnabled(false);
                                                edtRegPassword.setError("Please Enter Password");
                                                edtRegPassword.requestFocus();
                                               // tl_password.setPasswordVisibilityToggleEnabled(false);
                                            }
                                        } else {
                                            edtRegEmail.setError("Please Enter Valid Email Id");
                                            edtRegEmail.requestFocus();
                                        }
                                    } else {
                                        edtRegEmail.setError("Please Enter Email Id");
                                        edtRegEmail.requestFocus();
                                    }
                                } else {
                                    edtRegContact.setError("Please Enter Valid Contact Number");
                                    edtRegContact.requestFocus();
                                }
                            } else {
                                edtRegContact.setError("Please Enter Contact Number");
                                edtRegContact.requestFocus();
                            }
                        }else {
                            edtLastName.setError("Please Enter Last Name");
                            edtLastName.requestFocus();
                        }

                    }else {
                        edtRegFirstName.setError("Please Enter First Name");
                        edtRegFirstName.requestFocus();
                    }
                }
            }
        });

    }

    private void initView() {
        cd = new ConnectionDetector(RegistrationActivity.this);
        isInternetPresent = cd.isConnectingToInternet();

        tv_login = (TextView) findViewById(R.id.tv_login);
        btn_Register = (TextView) findViewById(R.id.btn_Register);

        sharedPreferences = getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        edtRegFirstName = (EditText) findViewById(R.id.edit_firstName);
        edtRegContact = (EditText) findViewById(R.id.edit_phone1);

        edtRegEmail = (EditText) findViewById(R.id.edit_registeremail);
        edtRegPassword = (EditText) findViewById(R.id.edit_registerpass);
        edit_referalCode = (EditText) findViewById(R.id.edit_referalCode);

        btn_Register = (TextView) findViewById(R.id.btn_Register);
        iv_locationIcon = (ImageView) findViewById(R.id.iv_locationIcon);
        edtLastName = (EditText) findViewById(R.id.edit_lastName);

        edtRegPassword.setTransformationMethod(new PasswordTransformationMethod());
        tv_firstName = (TextInputLayout) findViewById(R.id.tv_firstName);
        tl_password = (TextInputLayout) findViewById(R.id.tv_input_password);
        iv_check = (ImageView) findViewById(R.id.iv_terms);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(firstName);
        int start = builder.length();
        builder.append(colored);
        int end = builder.length();
        builder.setSpan(new ForegroundColorSpan(Color.RED), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edtRegFirstName.setHint(builder);

        String lastName = "Last Name";
        SpannableStringBuilder builderL = new SpannableStringBuilder();
        builderL.append(lastName);
        int startL = builderL.length();
        builderL.append(colored);
        int endL = builderL.length();
        builderL.setSpan(new ForegroundColorSpan(Color.RED), startL, endL,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edtLastName.setHint(builderL);

        String contact = "Contact No";
        SpannableStringBuilder contactBulder = new SpannableStringBuilder();
        contactBulder.append(contact);
        int contactstart = contactBulder.length();
        contactBulder.append(colored);
        int contactEnd = contactBulder.length();
        contactBulder.setSpan(new ForegroundColorSpan(Color.RED), contactstart, contactEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edtRegContact.setHint(contactBulder);

        String email = "Email Id";
        SpannableStringBuilder emailbuilder = new SpannableStringBuilder();
        emailbuilder.append(email);
        int emailStart = emailbuilder.length();
        emailbuilder.append(colored);
        int emailEnd = emailbuilder.length();
        emailbuilder.setSpan(new ForegroundColorSpan(Color.RED), emailStart, emailEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edtRegEmail.setHint(emailbuilder);

        String password = "Password";
        SpannableStringBuilder passwordBuilder = new SpannableStringBuilder();
        passwordBuilder.append(password);
        int passStart = passwordBuilder.length();
        passwordBuilder.append(colored);
        int passEnd = passwordBuilder.length();
        passwordBuilder.setSpan(new ForegroundColorSpan(Color.RED), passStart, passEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edtRegPassword.setHint(passwordBuilder);

        String referalCode = "Referral Code";
        SpannableStringBuilder referalBuilder = new SpannableStringBuilder();
        referalBuilder.append(referalCode);
        int referStart = referalBuilder.length();
       // referalBuilder.append(colored);
        int referEnd = referalBuilder.length();
        referalBuilder.setSpan(new ForegroundColorSpan(Color.RED), referStart, referEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        edit_referalCode.setHint(referalBuilder);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // retrive the data by using getPlace() method.
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.e("Tag", "Place: " + place.getAddress() + place.getPhoneNumber());

                edtRegLocality
                        .setText(""+place.getAddress() + place.getPhoneNumber());
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
    }

    class SignUpTask extends AsyncTask<String, Void, String> {

        Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = AppUtils.customLoader(RegistrationActivity.this);
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
                    .url(Config.BASE_URL+"signup.php?"+"firstname="+ userFirstname +"&"+ "lastname=" +userLastName+"&"+ "contact_no=" +userContact+"&"+ "locality=" +userLocality+"&"+ "building_no=" +userBuilding+"&"+ "email_id=" +userEmail+"&"+ "password=" +userPassword+"&"+ "referalCodeFetched=" +referalCode)
                    .post(body)
                    .build();

            try
            {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // clearEditTextData();
            System.out.println(">>> Signup result :" + s);
            dialog.dismiss();

            if(s != null){
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if(status.equals("1")){
                        Toast.makeText(RegistrationActivity.this,response_msg,Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(RegistrationActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        dialog.dismiss();
                        Toast.makeText(RegistrationActivity.this,response_msg,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(RegistrationActivity.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }
            else {
                dialog.dismiss();
                Toast.makeText(RegistrationActivity.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
