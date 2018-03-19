package com.marginfresh.adapter;

import android.app.Dialog;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.marginfresh.Model.ModelOffer;
import com.marginfresh.Model.SelectStore;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityProductDetail;
import com.marginfresh.activities.DrawerActivity;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by bitware on 6/6/17.
 */

public class AdapterOffer extends RecyclerView.Adapter<AdapterOffer.MyViewHolder>
{
        Context context;
        ArrayList<ModelOffer> arrOffer;
        boolean isWishlistSelected = false;
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        String user_id="",product_id="";
        ConnectionDetector cd;
        boolean isInternetPresent;
        int currentPosition;
        Dialog dialog;

        public AdapterOffer(Context context,ArrayList<ModelOffer> arrOffer)
        {
            this.context=context;
            this.arrOffer=arrOffer;
            sharedPreferences =context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
            editor =sharedPreferences.edit();
            cd = new ConnectionDetector(context);
            isInternetPresent = cd.isConnectingToInternet();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder
        {
            ImageView iv_wishlist,iv_productImage;
            TextView tv_name,tv_offer,tv_price,tv_newPrice,tv_priceLine;
            LinearLayout ll_row,ll_rating;
            RelativeLayout rl_price,rl_productImage;
            public MyViewHolder(View view)
            {
                super(view);
                iv_wishlist = (ImageView)view.findViewById(R.id.iv_wishlist);
                tv_name = (TextView) view.findViewById(R.id.tv_productName);
                tv_offer = (TextView) view.findViewById(R.id.tv_offer);
                tv_price = (TextView) view.findViewById(R.id.tv_price);
                tv_newPrice = (TextView) view.findViewById(R.id.tv_newPrice);
                iv_productImage = (ImageView) view.findViewById(R.id.iv_productImage);
                ll_row= (LinearLayout) view.findViewById(R.id.ll_row);
                rl_price = (RelativeLayout) view.findViewById(R.id.rl_price);
                tv_priceLine= (TextView) view.findViewById(R.id.tv_priceLine);
                ll_rating = (LinearLayout) view.findViewById(R.id.ll_rating);
               // rl_productImage = (RelativeLayout) view.findViewById(R.id.rl_productImage);

            }
        }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_offer, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AdapterOffer.MyViewHolder holder, final int position) {
        Glide.with(context).load(arrOffer.get(position).getProduct_image()).into(holder.iv_productImage);
        holder.tv_name.setText(arrOffer.get(position).getProduct_name());
        holder.tv_offer.setText(arrOffer.get(position).getOffer()+"%");
        if (arrOffer.get(position).getIsSelected().equalsIgnoreCase("yes")){
            holder.iv_wishlist.setImageResource(R.drawable.wishlist_red);
        }else {
            holder.iv_wishlist.setImageResource(R.drawable.wishlist_inactive);
        }

        holder.ll_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("from","fromOffer");
                editor.putString("product_id",arrOffer.get(position).getProduct_id());
                editor.commit();
                context.startActivity(new Intent(context, ActivityProductDetail.class));
            }
        });
        holder.iv_wishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPosition = position;
                product_id = sharedPreferences.getString("product_id","");
                user_id = sharedPreferences.getString("user_id","");
                if (isInternetPresent){
                    if (arrOffer.get(position).getIsSelected().equalsIgnoreCase("no")){
                        new AddToWishList().execute("{\"user_id\":\"" + user_id + "\",\"product_id\":\"" + product_id + "\"}");
                    }else if (arrOffer.get(position).getIsSelected().equalsIgnoreCase("yes")){
                        new DeleteWishlist().execute("{\"user_id\":\"" + user_id + "\",\"product_id\":\"" + product_id + "\"}");
                    }

                }else {
                    Toast.makeText(context,R.string.no_internet,Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.tv_price.setText("Price: AED "+ AppUtils.getFormattedPrice(Double.parseDouble(arrOffer.get(position).getPrice())));
        if (arrOffer.get(position).getNew_price().equals("0")){
            holder.tv_newPrice.setVisibility(View.GONE);
            setWeight(1.5f,holder.tv_name,null,null);
            setWeight(0.8f,null,null,holder.ll_rating);
            setWeightForPrice(1.5f,holder.rl_price);
            holder.tv_priceLine.setVisibility(View.INVISIBLE);
        }else {
            holder.tv_newPrice.setText("New Price: "+ sharedPreferences.getString("currency","") +" "+ AppUtils.getFormattedPrice(Double.parseDouble(arrOffer.get(position).getNew_price())));
            holder.tv_newPrice.setVisibility(View.VISIBLE);
            setWeight(1.0f,holder.tv_name,null,null);
            setWeight(0.8f,null,null,holder.ll_rating);
            setWeightForPrice(1.0f,holder.rl_price);
            holder.tv_priceLine.setVisibility(View.VISIBLE);
        }
    }

    public void setWeight(Float weight,TextView textView,ImageView imageView,LinearLayout linearLayout){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,0,weight);
        if (!(textView ==null)){
            textView.setLayoutParams(params);
        }
        if (!(imageView ==null)){
            imageView.setLayoutParams(params);
        }
        if (!(linearLayout ==null)){
            linearLayout.setLayoutParams(params);
        }
    }
    public void setWeightForPrice(Float weight,RelativeLayout relativeLayout){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,0,weight);
        if (!(relativeLayout ==null)){
            relativeLayout.setLayoutParams(params);
        }
    }
    @Override
    public int getItemCount() {
        return arrOffer.size();
    }

    class AddToWishList extends AsyncTask<String, Void, String> {

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
                    .url(Config.BASE_URL+"addtowishlist.php?"+"user_id="+user_id + "&product_id="+product_id)
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
            System.out.println(">>> Add to wishlist result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
                        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                        editor.putString("NavigationPosition","6");
                        editor.commit();
                        arrOffer.get(currentPosition).setIsSelected("yes");
                        notifyDataSetChanged();


                    }else{
                        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context,context.getString(R.string.network_error),Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(context, "Something went to wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class DeleteWishlist extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = AppUtils.customLoader(context);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(180, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(180, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "customerdeletewishlistitem.php?"+"user_id="+user_id+"&product_id="+product_id)
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
            // clearEditTextData();
            System.out.println(">>>Delete product from wishlist result :" + s);
            dialog.dismiss();
            if (s != null) {
//                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String status = jsonObject.getString("status");
                    String response_msg = jsonObject.getString("message");

                    if (status.equals("1")) {
                        Toast.makeText(context, response_msg, Toast.LENGTH_SHORT).show();
                        editor.putString("NavigationPosition","6");
                        editor.commit();
                        arrOffer.get(currentPosition).setIsSelected("no");
                        notifyDataSetChanged();
                    } else {
                        dialog.dismiss();
                        Toast.makeText(context, response_msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                dialog.dismiss();
                Toast.makeText(context,"Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


