package com.marginfresh.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.marginfresh.Fragments.FragmentHome;
import com.marginfresh.Model.GetNearBy;
import com.marginfresh.Model.ModelAllTypeArray;
import com.marginfresh.Model.ModelSubTypeArray;
import com.marginfresh.Model.ModelTopOffers;
import com.marginfresh.R;
import com.marginfresh.activities.DrawerActivity;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class StoreListingAdapter extends RecyclerView.Adapter<StoreListingAdapter.MyViewHolder>
{
    Context context;
    ArrayList<GetNearBy.Nearby_store_array> arrSelectStore;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String user_id,store_id;
    ConnectionDetector cd;
    boolean isInternetPresent;

    public StoreListingAdapter(Context context, ArrayList<GetNearBy.Nearby_store_array> arrSelectStore)
    {
        this.context=context;
        this.arrSelectStore=arrSelectStore;
        sharedPreferences = context.getSharedPreferences("MyPref",MODE_PRIVATE);
        editor= sharedPreferences.edit();
        cd = new ConnectionDetector(context);
        isInternetPresent =cd.isConnectingToInternet();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        ImageView img_store;
        TextView tv_name;
        LinearLayout ll_row;
        public MyViewHolder(View view)
        {
            super(view);
            img_store = (ImageView)view.findViewById(R.id.img_store);
            tv_name = (TextView) view.findViewById(R.id.tv_name);
            ll_row = (LinearLayout) view.findViewById(R.id.ll_row);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.storelist_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position)
    {

        GetNearBy.Nearby_store_array nearby_store_array = arrSelectStore.get(position);
        holder.tv_name.setText(nearby_store_array.getStore_name());
        Glide.with(context).load(nearby_store_array.getStore_image_url()).into(holder.img_store);
        holder.ll_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("store_id",arrSelectStore.get(position).getStore_id());
                editor.putString("storeName",arrSelectStore.get(position).getStore_name());
                editor.putString("NavigationPosition","0");
                editor.putString("comeFrom","selectingStore");
                editor.putString("status","afterStore");
                editor.commit();
                Intent myactivity = new Intent(context, DrawerActivity.class);
                myactivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(myactivity);

            }
        });

    }

    @Override
    public int getItemCount() {
        return arrSelectStore.size();
    }
}
