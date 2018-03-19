package com.marginfresh.adapter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.marginfresh.Model.ModelBillingInfo;
import com.marginfresh.Model.ModelSavedAddress;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityProductDetail;
import com.marginfresh.activities.ActivitySavedAddress;
import com.marginfresh.activities.DrawerActivity;
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

/**
 * Created by bitware on 4/8/17.
 */

public class AdapterSavedAddress  extends ArrayAdapter<ModelSavedAddress> {

    LayoutInflater inflater;
    ArrayList<ModelSavedAddress> arrSavedAddress;
    Context context;
    ViewHolder viewHolder;
    String addressId="";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    Boolean isInternetPresent;
    int currentPosition;

    public AdapterSavedAddress(Context context, int resource,ArrayList<ModelSavedAddress> arrSavedAddress) {
        super(context, resource);
        this.context = context;
        this.arrSavedAddress=arrSavedAddress;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        cd=new ConnectionDetector(context);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        editor= sharedPreferences.edit();
    }

    public static class ViewHolder {
        TextView tv_address;
    }

    @Override
    public int getCount() {
        return arrSavedAddress.size();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_savedaddress, null);
            viewHolder = new ViewHolder();

            viewHolder.tv_address= (TextView) convertView.findViewById(R.id.tv_savedAddress);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String address="";
        if (!arrSavedAddress.get(position).getAddressStreet().equalsIgnoreCase("null")){
            address = address+""+arrSavedAddress.get(position).getAddressStreet();
        }
        if (!arrSavedAddress.get(position).getAddressCity().equalsIgnoreCase("null")){
            address = address +","+arrSavedAddress.get(position).getAddressCity();
        }
        if (!arrSavedAddress.get(position).getAddressBuildingnumber().equalsIgnoreCase("null")){
            address = address +","+arrSavedAddress.get(position).getAddressBuildingnumber();
        }
        if (!arrSavedAddress.get(position).getAddressFlatnumber().equalsIgnoreCase("null")){
            address = address +","+arrSavedAddress.get(position).getAddressFlatnumber();
        }
        if (!arrSavedAddress.get(position).getAddressMobileNumber().equalsIgnoreCase("null")){
            address = address +","+arrSavedAddress.get(position).getAddressMobileNumber();
        }
        viewHolder.tv_address.setText(address);

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                currentPosition = position;
                showDeleteAddressDialog(position);
                return false;
            }
        });
        return convertView;
    }

    private void showDeleteAddressDialog(final int position) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Delete Address?");
        alertDialogBuilder
                .setMessage("Are you sure you want to delete this address ?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                addressId = arrSavedAddress.get(position).getAddressId();
                                if (isInternetPresent){
                                    new DeleteAddressTask().execute("{\"addressId\":\"" + addressId +  "\"}");
                                }else {
                                    Toast.makeText(context,context.getResources().getString(R.string.no_internet),Toast.LENGTH_SHORT).show();
                                }
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });

        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    class DeleteAddressTask extends AsyncTask<String, Void, String> {
        Dialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = AppUtils.customLoader(context);
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
                    .url(Config.BASE_URL+"customer_address_delete.php?"+"addressId="+addressId)
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
            System.out.println(">>> Delete Adress result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
                        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                        String addressAvailable = resObj.getString("addressAvailable");
                        editor.putString("addressAvailable",addressAvailable);
                        editor.commit();
                        arrSavedAddress.remove(currentPosition);
                        notifyDataSetChanged();
                       // context.startActivity(new Intent(context,ActivitySavedAddress.class));
                    }else{
                        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context,context.getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                Toast.makeText(context, "Something went to wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

