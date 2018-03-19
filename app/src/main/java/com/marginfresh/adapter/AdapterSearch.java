package com.marginfresh.adapter;

import android.app.Dialog;
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
import com.marginfresh.Model.ModelProductList;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityProductDetail;
import com.marginfresh.activities.ActivityProductList;
import com.marginfresh.activities.ActivitySearch;
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
 * Created by bitware on 3/7/17.
 */

public class AdapterSearch extends  RecyclerView.Adapter<AdapterSearch.MyViewHolder>
{
    Context context;
    ArrayList<ModelProductList> arrProductList;
    boolean isWishlistSelected = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String user_id="",product_id="";
    ConnectionDetector cd;
    boolean isInternetPresent;
    Dialog dialog;

    public AdapterSearch(Context context, ArrayList<ModelProductList> arrayList)
    {
        this.context=context;
        this.arrProductList = arrayList;
        sharedPreferences =context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        editor =sharedPreferences.edit();
        cd = new ConnectionDetector(context);
        isInternetPresent = cd.isConnectingToInternet();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        ImageView iv_wishlist,iv_productImage,iv_rateStar1,iv_rateStar2,iv_rateStar3,iv_rateStar4,iv_rateStar5;
        TextView tv_productName,tv_Price,tv_newPrice,tv_priceLine;
        LinearLayout ll_row,ll_rating;
        RelativeLayout rl_price;
        public MyViewHolder(View view)
        {
            super(view);
            iv_wishlist = (ImageView)view.findViewById(R.id.iv_wishlist);
            ll_row= (LinearLayout) view.findViewById(R.id.ll_row);
            iv_productImage= (ImageView) view.findViewById(R.id.iv_productImage);
            tv_productName= (TextView) view.findViewById(R.id.tv_productName);
            tv_Price = (TextView) view.findViewById(R.id.tv_price);
            tv_newPrice = (TextView) view.findViewById(R.id.tv_newPrice);
            iv_rateStar1 = (ImageView) view.findViewById(R.id.iv_rateStar1);
            iv_rateStar2 = (ImageView) view.findViewById(R.id.iv_rateStar2);
            iv_rateStar3 = (ImageView) view.findViewById(R.id.iv_rateStar3);
            iv_rateStar4 = (ImageView) view.findViewById(R.id.iv_rateStar4);
            iv_rateStar5 = (ImageView) view.findViewById(R.id.iv_rateStar5);
            tv_priceLine = (TextView) view.findViewById(R.id.tv_priceLine);
            ll_rating = (LinearLayout) view.findViewById(R.id.ll_rating);
            rl_price = (RelativeLayout) view.findViewById(R.id.rl_price);

        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.product, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.tv_productName.setText(arrProductList.get(position).getProduct_name());
        Glide.with(context).load(arrProductList.get(position).getProduct_image_url()).into(holder.iv_productImage);
        holder.tv_Price.setText("Price: AED"+ AppUtils.getFormattedPrice(Double.parseDouble(arrProductList.get(position).getPrice())));
        if (arrProductList.get(position).getNew_price().equals("0")){
            holder.tv_newPrice.setVisibility(View.GONE);
            setWeight(1.5f,holder.tv_productName,null,null);
            setWeight(4.0f,null,holder.iv_productImage,null);
            setWeight(0.8f,null,null,holder.ll_rating);
            setWeightForPrice(1.5f,holder.rl_price);
            holder.tv_priceLine.setVisibility(View.INVISIBLE);
        }else {
            holder.tv_newPrice.setVisibility(View.VISIBLE);
            holder.tv_newPrice.setText("New Price: "+ sharedPreferences.getString("currency","")+AppUtils.getFormattedPrice(Double.parseDouble(arrProductList.get(position).getNew_price())));
            setWeight(1.0f,holder.tv_productName,null,null);
            setWeight(1.0f,holder.tv_newPrice,null,null);
            setWeightForPrice(1.0f,holder.rl_price);
            setWeight(4.0f,null,holder.iv_productImage,null);
            setWeight(0.8f,null,null,holder.ll_rating);
            holder.tv_priceLine.setVisibility(View.VISIBLE);
        }
        if (arrProductList.get(position).getProduct_isInWislist().equals("no")){
            holder.iv_wishlist.setImageResource(R.drawable.wishlist_inactive);
        }else {
            holder.iv_wishlist.setImageResource(R.drawable.wishlist_red);
        }
        String rateCount = arrProductList.get(position).getProduct_rating_count();
        switch (rateCount){
            case "1":
                holder.iv_rateStar1.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar2.setImageResource(R.drawable.gray_star);
                holder.iv_rateStar3.setImageResource(R.drawable.gray_star);
                holder.iv_rateStar4.setImageResource(R.drawable.gray_star);
                holder.iv_rateStar5.setImageResource(R.drawable.gray_star);
                break;
            case "2":
                holder.iv_rateStar1.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar2.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar3.setImageResource(R.drawable.gray_star);
                holder.iv_rateStar4.setImageResource(R.drawable.gray_star);
                holder.iv_rateStar5.setImageResource(R.drawable.gray_star);
                break;
            case "3":
                holder.iv_rateStar1.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar2.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar3.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar4.setImageResource(R.drawable.gray_star);
                holder.iv_rateStar5.setImageResource(R.drawable.gray_star);
                break;
            case "4":
                holder.iv_rateStar1.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar2.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar3.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar4.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar5.setImageResource(R.drawable.gray_star);
                break;
            case "5":
                holder.iv_rateStar1.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar2.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar3.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar4.setImageResource(R.drawable.yellow_star);
                holder.iv_rateStar5.setImageResource(R.drawable.yellow_star);
                break;

        }

        holder.ll_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("from","search");
                editor.putString("product_id",arrProductList.get(position).getProductId());
                editor.commit();
                context.startActivity(new Intent(context, ActivityProductDetail.class));
            }
        });
        holder.iv_wishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                product_id = arrProductList.get(position).getProductId();
                user_id = sharedPreferences.getString("user_id","");
                if (isInternetPresent){
                    if (arrProductList.get(position).getProduct_isInWislist().equalsIgnoreCase("no")){
                        new AddToWishList().execute("{\"user_id\":\"" + user_id + "\",\"product_id\":\"" + product_id + "\"}");
                    }else if (arrProductList.get(position).getProduct_isInWislist().equalsIgnoreCase("yes")){
                        new DeleteWishlist().execute("{\"user_id\":\"" + user_id + "\",\"product_id\":\"" + product_id + "\"}");
                    }

                }else {
                    Toast.makeText(context,R.string.no_internet,Toast.LENGTH_SHORT).show();
                }
            }
        });
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
        return arrProductList.size();
    }

    class AddToWishList extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            dialog =AppUtils.customLoader(context);
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
                        context.startActivity(new Intent(context, ActivitySearch.class));

                    }else{
                        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context,context.getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

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
                        context.startActivity(new Intent(context,ActivitySearch.class));
                    } else {
                        dialog.dismiss();
                        Toast.makeText(context, response_msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context,context.getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            } else {
                dialog.dismiss();
                Toast.makeText(context,"Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


