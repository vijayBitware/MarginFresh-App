package com.marginfresh.adapter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.marginfresh.Model.ModelMyOrders;
import com.marginfresh.Model.SelectStore;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityCart;
import com.marginfresh.activities.ActivityCheckout;
import com.marginfresh.activities.ActivityOrderDetail;
import com.marginfresh.activities.ActivityProductDetail;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
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

/**
 * Created by bitware on 5/6/17.
 */

public class AdapterMyOrders extends RecyclerView.Adapter<AdapterMyOrders.MyViewHolder>
{
        Context context;
        ArrayList<ModelMyOrders> arrMyOrder;
        boolean isWishlistSelected = false;
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        String customerId,orderId;
        boolean isInternetPresent;
        ConnectionDetector cd;

        public AdapterMyOrders(Context context,ArrayList<ModelMyOrders> arrMyOrder)
        {
            this.context=context;
            this.arrMyOrder = arrMyOrder;
            cd = new ConnectionDetector(context);
            isInternetPresent = cd.isConnectingToInternet();
            sharedPreferences = context.getSharedPreferences("MyPref",Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder
        {
            LinearLayout ll_row;
            TextView tv_status,tv_date,tv_orderId,tv_price,tv_orderstatus,tv_reorder;
            ImageView iv_orderstatus;

        public MyViewHolder(View view)
        {
            super(view);
            tv_status= (TextView) view.findViewById(R.id.tv_status);
            tv_date= (TextView) view.findViewById(R.id.tv_date);
            tv_price = (TextView) view.findViewById(R.id.tv_price);
            tv_orderId = (TextView) view.findViewById(R.id.tv_orderId);
            ll_row= (LinearLayout) view.findViewById(R.id.ll_row);
            iv_orderstatus = (ImageView) view.findViewById(R.id.iv_orderstatus);
            tv_reorder = (TextView) view.findViewById(R.id.tv_reorder);
        }
        }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_myorders, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.tv_orderId.setText("Order Id: "+arrMyOrder.get(position).getOrder_id());
        holder.tv_price.setText("Price: "+ sharedPreferences.getString("currency","")+" " + AppUtils.getFormattedPrice(Double.parseDouble(arrMyOrder.get(position).getGrand_total())));
        String orderStatus = arrMyOrder.get(position).getOrderStatus().substring(0,1).toUpperCase() + arrMyOrder.get(position).getOrderStatus().substring(1);
        holder.tv_status.setText(orderStatus);
        holder.tv_date.setText(AppUtils.getFormattedDate(arrMyOrder.get(position).getCreated_at()));
        System.out.println("Product data size in Adapter > " +arrMyOrder.get(position).getArrProductData());
        holder.ll_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("orderId",arrMyOrder.get(position).getOrder_id());
                editor.commit();
                Intent intent = new Intent(context,ActivityOrderDetail.class);
                intent.putExtra("productData",arrMyOrder.get(position).getArrProductData());
                context.startActivity(intent);
            }
        });

        String orderstatus = arrMyOrder.get(position).getOrderStatus();
        switch (orderstatus){
            case "pending":
                holder.iv_orderstatus.setImageResource(R.drawable.status_pending);
                break;
            case "holded":
                holder.iv_orderstatus.setImageResource(R.drawable.status_holded);
                break;
            case "processing":
                holder.iv_orderstatus.setImageResource(R.drawable.status_processing);
                break;
            case "canceled":
                holder.iv_orderstatus.setImageResource(R.drawable.status_canceled);
                break;
            case "closed":
                holder.iv_orderstatus.setImageResource(R.drawable.status_closed);
                break;
            case "complete":
                holder.iv_orderstatus.setImageResource(R.drawable.status_complete);
                break;
        }

        holder.tv_reorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrMyOrder.get(position).getReorder().equals("0")){
                    // holder.tv_reorder.setVisibility(View.INVISIBLE);
                    Toast.makeText(context,"Product(s) Not Available",Toast.LENGTH_SHORT).show();
                }else {

                    showReorderDialog(position);
                }
            }
        });
    }

    public void showReorderDialog(final int position){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Margin Fresh");
        alertDialogBuilder
                .setMessage("Do you want to reorder?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                customerId = sharedPreferences.getString("user_id","");
                                orderId = arrMyOrder.get(position).getOrder_id();
                                if (isInternetPresent){
                                    new ReorderTask().execute("{\"orderId\":\"" + orderId + "\",\"customerId\":\"" + customerId + "\"}");
                                }else {
                                    Toast.makeText(context,context.getResources().getString(R.string.no_internet),Toast.LENGTH_SHORT).show();
                                }
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    @Override
    public int getItemCount() {
        return arrMyOrder.size();
    }

    class ReorderTask extends AsyncTask<String, Void, String> {

        Dialog dialog;
        @Override
        protected void onPreExecute() {
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
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"reorder_products.php?"+"orderId="+orderId+"&customerId="+customerId)
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
            System.out.println(">>>Reorder result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
                        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                        editor.putString("fromReorder","yes");
                        editor.commit();
                        context.startActivity(new Intent(context, ActivityCheckout.class));

                    }else{
                        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context,context.getResources().getString(R.string.network_error),Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(context, "Something went to wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

