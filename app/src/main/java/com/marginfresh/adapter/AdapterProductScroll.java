package com.marginfresh.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.marginfresh.Model.ModelCategoryListScroll;
import com.marginfresh.Model.ModelSubTypeArray;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityProductList;
import com.marginfresh.domain.Config;

import java.util.ArrayList;

/**
 * Created by bitware on 1/6/17.
 */

public class AdapterProductScroll extends RecyclerView.Adapter<AdapterProductScroll.MyViewHolder>
{
    Context context;
    ArrayList<ModelSubTypeArray> arrSubCategoryList;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public AdapterProductScroll(Context context, ArrayList<ModelSubTypeArray> arrSubCategoryList)
    {
        this.context=context;
        this.arrSubCategoryList=arrSubCategoryList;

        sharedPreferences=context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
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
            tv_name = (TextView) view.findViewById(R.id.tv_productName);
            ll_row = (LinearLayout) view.findViewById(R.id.ll_row);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_category_product, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.tv_name.setText(arrSubCategoryList.get(position).getType_name());
        Glide.with(context).load(arrSubCategoryList.get(position).getSub_type_image()).into(holder.img_store);
        holder.ll_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("type_id",arrSubCategoryList.get(position).getType_id());
                editor.putString("navigation","fromCategoryList");
                editor.commit();
                Config.isFiterApply = "no";
                context.startActivity(new Intent(context, ActivityProductList.class));

            }
        });
    }

    @Override
    public int getItemCount() {
        return arrSubCategoryList.size();
    }
}

