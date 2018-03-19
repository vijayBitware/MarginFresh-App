package com.marginfresh.adapter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.marginfresh.Model.ModelReferFriend;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityCart;
import com.marginfresh.activities.ActivityInviteFriend;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by bitware on 14/8/17.
 */

public class AdapterInviteFriend extends ArrayAdapter<ModelReferFriend> {

    LayoutInflater inflater;
    ArrayList<ModelReferFriend> arrFriendList;
    Context context;
    ViewHolder viewHolder;
    private String referedToEmail,email,referredByUserId,referredByName;
    ConnectionDetector cd;
    Boolean isInternetPresent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public AdapterInviteFriend(Context context, int resource,ArrayList<ModelReferFriend> arrReferFriend) {
        super(context, resource);
        this.context = context;
        this.arrFriendList = arrReferFriend;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        sharedPreferences = context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        editor= sharedPreferences.edit();
        cd = new ConnectionDetector(context);
        isInternetPresent =cd.isConnectingToInternet();
    }

    public static class ViewHolder {
        TextView tv_friendName,tv_emaailAddress,tv_inviteFriend;
    }

    @Override
    public int getCount() {
        return arrFriendList.size();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_invitefriend, null);
            viewHolder = new ViewHolder();

            viewHolder.tv_friendName= (TextView) convertView.findViewById(R.id.tv_friendName);
            viewHolder.tv_emaailAddress = (TextView) convertView.findViewById(R.id.tv_emailAddress);
            viewHolder.tv_inviteFriend = (TextView) convertView.findViewById(R.id.tv_inviteFriend);
//
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tv_friendName.setText(arrFriendList.get(position).getFriendName());
        viewHolder.tv_emaailAddress.setText(arrFriendList.get(position).getFriendEmail());
        viewHolder.tv_inviteFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email =sharedPreferences.getString("customerEmail","");
                referredByUserId= sharedPreferences.getString("user_id","");
                referredByName=sharedPreferences.getString("customerName","");
                referedToEmail = arrFriendList.get(position).getFriendEmail();
                if (isInternetPresent){
                    new InviteFriend().execute("{\"referredByEmail\":\"" + email + "\",\"referredToEmail\":\"" + referedToEmail + "\",\"referredByUserId\":\"" + referredByUserId+ "\",\"referredByName\":\"" + referredByName + "\"}");
                }else {
                    Toast.makeText(context,R.string.no_internet,Toast.LENGTH_SHORT).show();
                }
            }
        });
        return convertView;
    }

    class InviteFriend extends AsyncTask<String, Void, String> {
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
                        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context,context.getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                Toast.makeText(context, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

