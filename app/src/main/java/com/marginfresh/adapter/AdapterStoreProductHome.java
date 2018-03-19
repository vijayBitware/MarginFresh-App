package com.marginfresh.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.marginfresh.Fragments.FragmentHome;
import com.marginfresh.Model.ModelAllTypeArray;
import com.marginfresh.Model.ModelTopOffers;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityCategoryList;
import com.marginfresh.activities.ActivityFoodProductList;
import com.marginfresh.activities.ActivityProductList;
import com.marginfresh.domain.Config;

import java.util.ArrayList;

/**
 * Created by bitware on 1/6/17.
 */

public class AdapterStoreProductHome extends RecyclerView.Adapter<AdapterStoreProductHome.MyViewHolder> {

    Context context;
    ArrayList<ModelAllTypeArray> arrSelectStore;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public AdapterStoreProductHome(Context context,ArrayList<ModelAllTypeArray> arrayList)
    {
        this.context=context;
        this.arrSelectStore=arrayList;

        sharedPreferences=context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        System.out.println("Array list size in adapter store home >>> " +arrSelectStore.size());
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        ImageView img_store;
        TextView tv_name;
        LinearLayout ll_row;
        public MyViewHolder(View view)
        {
            super(view);
            img_store = (ImageView)view.findViewById(R.id.iv_productTypeImage);
            tv_name = (TextView) view.findViewById(R.id.tv_categoryName);
            ll_row= (LinearLayout) view.findViewById(R.id.ll_row);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_store_product, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.tv_name.setText(arrSelectStore.get(position).getType_name());
        System.out.println("Category Image Urls > "+arrSelectStore.get(position).getType_image_url());
        Glide.with(context).load(arrSelectStore.get(position).getType_image_url()).into(holder.img_store);

        holder.ll_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrSelectStore.get(position).getType_name().equals("All Items")){
                    editor.putString("type_id",arrSelectStore.get(position).getType_id());
                    editor.putString("navigation","fromHome");
                    Config.isFiterApply = "no";
                    editor.commit();
                    Intent intent = new Intent(context, ActivityProductList.class);
                    context.startActivity(intent);
                }else {
                    editor.putString("categoryName",arrSelectStore.get(position).getType_name());
                    editor.commit();
                    Config.arrSubCatArrayList = arrSelectStore.get(position).getArrSubTypeArrayList();
                    if (Config.arrSubCatArrayList.size()==0){
                        editor.putString("type_id",arrSelectStore.get(position).getType_id());
                        editor.putString("navigation","fromHome");
                        Config.isFiterApply = "no";
                        editor.commit();
                        Intent intent = new Intent(context, ActivityProductList.class);
                        context.startActivity(intent);
                    }else {
                        editor.putString("navigation","fromHome");
                        editor.commit();
                        Config.arrSubCatArrayList = arrSelectStore.get(position).getArrSubTypeArrayList();
                        Intent intent = new Intent(context, ActivityFoodProductList.class);
                        context.startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrSelectStore.size();
    }
}
