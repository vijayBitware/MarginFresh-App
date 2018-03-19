package com.marginfresh.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.marginfresh.Model.ModelCart;
import com.marginfresh.Model.ModelNotificationList;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityCart;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by bitware on 5/8/17.
 */

public class AdapterNotification extends ArrayAdapter<ModelNotificationList> {

    Context context;
    LayoutInflater inflater;
    ViewHolder holder;
    ArrayList<ModelNotificationList> arrNotification;
    boolean isInternetPresent;
    ConnectionDetector cd;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public AdapterNotification(Context context, int resource,ArrayList<ModelNotificationList> arrNotification) {
        super(context,resource);
        this.context = context;
        this.arrNotification=arrNotification;
        cd = new ConnectionDetector(context);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences =getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor =sharedPreferences.edit();
    }

    public static class ViewHolder {
        TextView tv_title,tv_content;
        ImageView iv_notificationImage;
    }
    @Override
    public int getCount() {
        return arrNotification.size();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView==null){
            convertView = inflater.inflate(R.layout.row_notification, null);
            holder = new ViewHolder();

            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_notiTitle);
            holder.tv_content = (TextView) convertView.findViewById(R.id.tv_notiContent);
            holder.iv_notificationImage = (ImageView) convertView.findViewById(R.id.iv_notiImage);
            convertView.setTag(holder);

        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_title.setText(arrNotification.get(position).getNotify_title());
        holder.tv_content.setText(arrNotification.get(position).getNotify_content());
        if (arrNotification.get(position).getNotify_logo().equals("") || arrNotification.get(position).getNotify_logo().equals("null")){
            holder.iv_notificationImage.setImageResource(R.drawable.placeholder);
        }else {
            Glide.with(context).load(arrNotification.get(position).getNotify_logo()).into(holder.iv_notificationImage);
        }
        return convertView;
    }
}

