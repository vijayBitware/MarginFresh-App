package com.marginfresh.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityProductDetail;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bitware on 1/8/17.
 */

public class AdapterZoomImage extends RecyclerView.Adapter<AdapterZoomImage.MyViewHolder>
{
    Context context;
    ArrayList<String> arrProductImages;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public AdapterZoomImage(Context context, ArrayList<String> arrProductImages)
    {
        this.context=context;
        this.arrProductImages=arrProductImages;

        sharedPreferences = context.getSharedPreferences("MyPref",MODE_PRIVATE);
        editor= sharedPreferences.edit();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        ImageView productImage;
        public MyViewHolder(View view)
        {
            super(view);
            productImage = (ImageView)view.findViewById(R.id.iv_ProductSmallImage);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_zoomimage, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Glide.with(context).load(arrProductImages.get(position)).into(holder.productImage);
        holder.productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getString("navigation","").equals("fromProductDetail")){
                    Glide.with(context).load(arrProductImages.get(position)).into(holder.productImage);
                }else {
                    Glide.with(context).load(arrProductImages.get(position)).into(holder.productImage);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrProductImages.size();
    }
}
