package com.marginfresh.Fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.marginfresh.R;
import com.marginfresh.db_utils.DatabaseHandler;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bitware on 3/6/17.
 */

public class FragmentBudget extends Fragment {

    View view;
    Spinner spn_monthAddBudget, spn_monthDisplayBudget;
    String[] month ={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    ArrayAdapter monthAdapter;
    TextView tv_save,tv_saving,tv_ifAmountExceeds;
    String response_msg="",user_id="",month_name="",budgetAmount="";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    boolean isInternetPresent;
    EditText edt_BudgetAmount,edt_yourBudgetAmount,edt_actualBillingAmount;
    ArrayList<String> arrMonthList;
    LinearLayout ll_saving,ll_ifAmountExceeds;
    Dialog dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_budget,container,false);

        init();

        //set current month to display customer budget
        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();
        int monthId = Integer.parseInt(dateFormat.format(date))-1;
        spn_monthDisplayBudget.setSelection(monthId);

        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edt_BudgetAmount.getWindowToken(), 0);
                month_name = spn_monthAddBudget.getSelectedItem().toString();
                System.out.println("Month Name >>> " +month_name);
                user_id = sharedPreferences.getString("user_id","");
                budgetAmount = edt_BudgetAmount.getText().toString();
                if (!budgetAmount.isEmpty()) {
                    if (isInternetPresent) {
                        new AddBudget().execute("{\"user_id\":\"" + user_id + "\",\"month\":\"" + month_name + "\",\"budgetamount\":\"" + budgetAmount + "\"}");
                    } else {
                        Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getContext(),"Please Enter Budget Amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

        edt_BudgetAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edt_BudgetAmount.getWindowToken(), 0);
                    month_name = spn_monthAddBudget.getSelectedItem().toString();
                    System.out.println("Month Name >>> " +month_name);
                    user_id = sharedPreferences.getString("user_id","");
                    budgetAmount = edt_BudgetAmount.getText().toString();
                    if (!budgetAmount.isEmpty()) {
                        if (isInternetPresent) {
                            new AddBudget().execute("{\"user_id\":\"" + user_id + "\",\"month\":\"" + month_name + "\",\"budgetamount\":\"" + budgetAmount + "\"}");
                        } else {
                            Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getContext(),"Please Enter Budget Amount", Toast.LENGTH_SHORT).show();
                    }

                }

                    return false;
            }
        });
        spn_monthDisplayBudget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                month_name = spn_monthDisplayBudget.getSelectedItem().toString();
                user_id = sharedPreferences.getString("user_id","");
                if (isInternetPresent){
                    new DisplayUserBudget().execute("{\"user_id\":\"" + user_id + "\",\"month\":\"" + month_name + "\"}");
                }else {
                    Toast.makeText(getContext(),R.string.no_internet,Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }

    private void init() {
        sharedPreferences = getContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        editor= sharedPreferences.edit();
        cd = new ConnectionDetector(getContext());
        isInternetPresent = cd.isConnectingToInternet();
        tv_save= (TextView) view.findViewById(R.id.tv_save);
        edt_BudgetAmount = (EditText) view.findViewById(R.id.edt_BudgetAmount);
        edt_yourBudgetAmount = (EditText) view.findViewById(R.id.edt_yourBudgetAmount);
        edt_actualBillingAmount = (EditText) view.findViewById(R.id.edt_actualBillingAmount);
        tv_saving = (TextView) view.findViewById(R.id.tv_saving);
        ll_saving = (LinearLayout) view.findViewById(R.id.ll_saving);
        ll_ifAmountExceeds = (LinearLayout) view.findViewById(R.id.ll_ifAmountExceeds);
        tv_ifAmountExceeds = (TextView) view.findViewById(R.id.tv_ifAmountExceeds);

        spn_monthAddBudget = (Spinner) view.findViewById(R.id.spn_monthAddBudget);
        spn_monthDisplayBudget = (Spinner) view.findViewById(R.id.spn_monthDisplayBudget);
        monthAdapter = new ArrayAdapter(getContext(),android.R.layout.simple_spinner_item,month);
        spn_monthAddBudget.setAdapter(monthAdapter);
        spn_monthDisplayBudget.setAdapter(monthAdapter);

    }

    class AddBudget extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = AppUtils.customLoader(getContext());
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
                    .url(Config.BASE_URL+"customer_add_budget.php?"+"user_id="+ user_id +"&"+ "month=" +month_name +"&"+ "budgetamount=" +budgetAmount)
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

            System.out.println(">>> Add budget result :" + s);
            dialog.dismiss();

            if(s != null){
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if(status.equals("1")){
                        Toast.makeText(getContext(),response_msg,Toast.LENGTH_SHORT).show();
                        edt_BudgetAmount.setText("");
                        String currentMoth = spn_monthAddBudget.getSelectedItem().toString();
                        String displayMonth = spn_monthDisplayBudget.getSelectedItem().toString();
                        if (currentMoth.equals(displayMonth)){
                            month_name = spn_monthDisplayBudget.getSelectedItem().toString();
                            user_id = sharedPreferences.getString("user_id","");
                            if (isInternetPresent){
                                new DisplayUserBudget().execute("{\"user_id\":\"" + user_id + "\",\"month\":\"" + month_name + "\"}");
                            }else {
                                Toast.makeText(getContext(),R.string.no_internet,Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                    else {
                        dialog.dismiss();
                        Toast.makeText(getContext(),response_msg,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    dialog.dismiss();
                    Toast.makeText(getContext(),getResources().getString(R.string.network_error),Toast.LENGTH_SHORT).show();
                }
            }
            else {
                dialog.dismiss();
                Toast.makeText(getContext(), "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class DisplayUserBudget extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = AppUtils.customLoader(getContext());
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
                    .url(Config.BASE_URL+"customerbudget.php?"+"user_id="+ user_id +"&"+ "month=" +month_name)
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

            System.out.println(">>> Display budget result :" + s);
            dialog.dismiss();

            if(s != null){
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    response_msg = jsonObject.getString("message");

                    if(status.equals("1")){
                       // Toast.makeText(getContext(),response_msg,Toast.LENGTH_SHORT).show();
                        int budgetAmount = Integer.parseInt(jsonObject.getString("budget_amount"));
                        edt_yourBudgetAmount.setText(sharedPreferences.getString("currency","")+" "+jsonObject.getString("budget_amount"));
                        edt_actualBillingAmount.setText(sharedPreferences.getString("currency","")+" "+jsonObject.getString("expense_amount"));
                        tv_saving.setText(sharedPreferences.getString("currency","")+" "+jsonObject.getString("saving"));
                        float saving = Float.parseFloat(jsonObject.getString("saving"));
                        if ( saving > 0){
                            ll_saving.setVisibility(View.VISIBLE);
                            ll_ifAmountExceeds.setVisibility(View.INVISIBLE);

                        }else {
                            ll_saving.setVisibility(View.INVISIBLE);
                            ll_ifAmountExceeds.setVisibility(View.VISIBLE);
                            tv_ifAmountExceeds.setText("AED "+jsonObject.getString("budget_amount"));
                        }
                    }
                    else {
                        dialog.dismiss();
                        Toast.makeText(getContext(),response_msg,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    dialog.dismiss();
                    Toast.makeText(getContext(),getResources().getString(R.string.network_error),Toast.LENGTH_SHORT).show();
                }
            }
            else {
                dialog.dismiss();
                Toast.makeText(getContext(), "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Button noticount = (Button) getActivity().findViewById(R.id.btn_notiCount);
        if (sharedPreferences.getString("notiCount","").equals("0") || sharedPreferences.getString("notiCount","").equals("")){
            noticount.setVisibility(View.INVISIBLE);
        }else {
            noticount.setVisibility(View.VISIBLE);
            noticount.setText(sharedPreferences.getString("notiCount",""));
        }

        DatabaseHandler db = new DatabaseHandler(getContext());
        Button btn_cartCount = (Button) getActivity().findViewById(R.id.btn_cartCount);
        String cartCount = db.getProductsInCartCount(sharedPreferences.getString("user_id",""));
        System.out.println("Activity Cart > cart count is " +cartCount);
        if (cartCount.equals("0") || cartCount.equals("")){
            btn_cartCount.setVisibility(View.GONE);
        }else {
            btn_cartCount.setVisibility(View.VISIBLE);
            btn_cartCount.setText(cartCount);
        }
    }
}
