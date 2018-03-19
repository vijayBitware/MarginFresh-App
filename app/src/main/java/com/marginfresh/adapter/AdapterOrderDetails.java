package com.marginfresh.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.marginfresh.Model.MyOrderProductData;
import com.marginfresh.R;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;

import java.util.ArrayList;

/**
 * Created by bitware on 31/7/17.
 */

public class AdapterOrderDetails  extends ArrayAdapter<MyOrderProductData> {

    Context context;
    ArrayList<MyOrderProductData> arrMyWishList;
    private Typeface Lato_light,Lato_regular;
    LayoutInflater inflater;
    ViewHolder holder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Boolean isInternetPresent = false;
    ConnectionDetector cd;
    String myProductId = "",customerId="";

    public AdapterOrderDetails(Context context, int resource, ArrayList<MyOrderProductData> arrMyWishList) {
        super(context, resource, arrMyWishList);

        this.context = context;
        this.arrMyWishList = arrMyWishList;
        cd = new ConnectionDetector(context);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences= context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }
    public static class ViewHolder {
        TextView tvName ,tvPrice,tvDescription,tvRemoveFromWishlist,tv_qty;
        ImageView ivMyWishList,ivBottles,iv_productImage;
        LinearLayout llViewCart;
        TextView tvQty;
    }
    @Override
    public int getCount() {
        return arrMyWishList.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.row_order_history, null);
            holder = new ViewHolder();

            holder.tvName= (TextView) convertView.findViewById(R.id.tv_productName);
            holder.tv_qty = (TextView) convertView.findViewById(R.id.tv_productQty);
            holder.tvPrice = (TextView) convertView.findViewById(R.id.tv_price);
            holder.iv_productImage = (ImageView) convertView.findViewById(R.id.iv_productImage);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvName.setText(arrMyWishList.get(position).getName());
        holder.tv_qty.setText("Qty :"+Math.round(Float.parseFloat(arrMyWishList.get(position).getQty_ordered())));
        holder.tvPrice.setText("AED "+ AppUtils.getFormattedPrice(Double.parseDouble(arrMyWishList.get(position).getPrice())));
        Glide.with(context).load(arrMyWishList.get(position).getImageUrl()).into(holder.iv_productImage);
        return convertView;
    }
}

