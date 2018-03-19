package com.marginfresh.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
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

public class AdapterFoodProductList extends RecyclerView.Adapter<AdapterFoodProductList.MyViewHolder>{
        Context context;
        ArrayList<ModelSubTypeArray> arrSelectStore;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

        public AdapterFoodProductList(Context context, ArrayList<ModelSubTypeArray> arrSelectStore)
        {
            this.context=context;
            this.arrSelectStore=arrSelectStore;
            sharedPreferences=context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
            editor=sharedPreferences.edit();
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
        ll_row= (LinearLayout) view.findViewById(R.id.ll_row);
    }
}
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_product_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.tv_name.setText(arrSelectStore.get(position).getType_name());
        Glide.with(context).load(arrSelectStore.get(position).getSub_type_image()).into(holder.img_store);
        holder.ll_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("store_id",sharedPreferences.getString("store_id",""));
                editor.putString("type_id",arrSelectStore.get(position).getType_id());
                editor.putString("navigation","fromFoodProductList");
                editor.commit();
                Config.isFiterApply = "no";
                context.startActivity(new Intent(context, ActivityProductList.class));
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrSelectStore.size();
    }
}

