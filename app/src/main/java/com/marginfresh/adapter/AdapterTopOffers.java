package com.marginfresh.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.marginfresh.Model.ModelTopOffers;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityCategoryList;
import com.marginfresh.activities.ActivityProductDetail;

import java.util.ArrayList;

/**
 * Created by bitware on 1/6/17.
 */

public class AdapterTopOffers extends RecyclerView.Adapter<AdapterTopOffers.MyViewHolder> {

    Context context;
    ArrayList<ModelTopOffers> arrTopOffers;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public AdapterTopOffers(Context context, ArrayList<ModelTopOffers> arrayList)
    {
        this.context=context;
        this.arrTopOffers=arrayList;

        sharedPreferences = context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        editor =sharedPreferences.edit();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        ImageView img_store,iv_productImage;
        TextView tv_name,tv_offer;
        LinearLayout ll_row;
        public MyViewHolder(View view)
        {
            super(view);
            tv_name = (TextView) view.findViewById(R.id.tv_pname);
            tv_offer= (TextView) view.findViewById(R.id.tv_offer);
            ll_row= (LinearLayout) view.findViewById(R.id.ll_row);
            iv_productImage = (ImageView) view.findViewById(R.id.iv_productImage);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_top_offer, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.tv_name.setText(arrTopOffers.get(position).getProduct_name());
        holder.tv_offer.setText(arrTopOffers.get(position).getOffer() + "% Off");
        Glide.with(context).load(arrTopOffers.get(position).getStore_image_url()).into(holder.iv_productImage);

        holder.ll_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("product_id", arrTopOffers.get(position).getProduct_id());
                editor.putString("topOfferFrom","home");
                editor.commit();
                context.startActivity(new Intent(context, ActivityProductDetail.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrTopOffers.size();
    }
}
