package com.marginfresh.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marginfresh.Model.NavDrawerItem;
import com.marginfresh.R;
import com.marginfresh.utils.AppUtils;

import java.util.Collections;
import java.util.List;


public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.MyViewHolder> {

    List<NavDrawerItem> data = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;

    int imgDrawer[] = {R.drawable.icon_home,
           R.drawable.icon_stores,
           R.drawable.icon_budget,
           R.drawable.icon_wallet,
           R.drawable.icon_order,
           R.drawable.icon_wishlist,
           R.drawable.icon_contact,
            R.drawable.nav_livechat,
           R.drawable.icon_setting,
            R.drawable.logout
    };

    public NavigationDrawerAdapter(Context context, List<NavDrawerItem> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    public void delete(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_nav_drawer, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        NavDrawerItem current = data.get(position);
        holder.title.setText(current.getTitle());
        holder.ivNavDrawer.setImageResource(imgDrawer[position]);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView ivNavDrawer;

        public MyViewHolder(View itemView) {

            super(itemView);
             final Typeface Lato_light,Lato_regular;

            title = (TextView) itemView.findViewById(R.id.title);
            ivNavDrawer = (ImageView)itemView.findViewById(R.id.ivNavDrawer);
        }
    }


}
