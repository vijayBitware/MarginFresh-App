package com.marginfresh.adapter;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.marginfresh.Model.CustomerReview;
import com.marginfresh.Model.ModelAllTypeArray;
import com.marginfresh.Model.ModelFoodCategoryList;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityFoodProductList;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class CustomerReviewAdapter extends ArrayAdapter<CustomerReview> {

    private LayoutInflater inflater;
    Context context;
    ArrayList<CustomerReview> arrCategoryList;
    ViewHolder holder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public CustomerReviewAdapter(Context context, int resource, ArrayList<CustomerReview> arrCategoryList) {
        super(context, resource);
        this.context = context;
        this.arrCategoryList=arrCategoryList;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sharedPreferences = context.getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static class ViewHolder{
        TextView tv_review,tv_raterName;
        ImageView iv_rating;
    }

    @Override
    public int getCount() {
        return arrCategoryList.size();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView==null){
            convertView = inflater.inflate(R.layout.review, null);
            holder = new ViewHolder();

            holder.tv_review = (TextView) convertView.findViewById(R.id.tv_review);
            holder.iv_rating= (ImageView) convertView.findViewById(R.id.iv_rating);
            holder.tv_raterName = (TextView) convertView.findViewById(R.id.tv_raterName);

            convertView.setTag(holder);

        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_review.setText(arrCategoryList.get(position).getRatingDetail());
        holder.tv_raterName.setText(arrCategoryList.get(position).getRatingUser());
        String ratingStar = arrCategoryList.get(position).getRatingStars();
        switch (ratingStar){
            case "0":
                holder.iv_rating.setImageResource(R.drawable.rating0);
                break;
            case "1":
                holder.iv_rating.setImageResource(R.drawable.rating1);
                break;
            case "2":
                holder.iv_rating.setImageResource(R.drawable.rating2);
                break;
            case "3":
                holder.iv_rating.setImageResource(R.drawable.rating3);
                break;
            case "4":
                holder.iv_rating.setImageResource(R.drawable.rating4);
                break;
            case "5":
                holder.iv_rating.setImageResource(R.drawable.rating5);
                break;
        }
        return convertView;
    }
}

