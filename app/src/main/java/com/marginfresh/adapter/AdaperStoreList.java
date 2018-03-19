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

import com.marginfresh.Model.SelectStore;
import com.marginfresh.R;
import com.marginfresh.activities.DrawerActivity;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static java.security.AccessController.getContext;

/**
 * Created by bitware on 6/6/17.
 */

public class AdaperStoreList extends RecyclerView.Adapter<AdaperStoreList.MyViewHolder>
{
    Context context;
    ArrayList<SelectStore> arrSelectStore;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public AdaperStoreList(Context context, ArrayList<SelectStore> arrSelectStore)
    {
        this.context=context;
        this.arrSelectStore=arrSelectStore;

        sharedPreferences = context.getSharedPreferences("MyPref",MODE_PRIVATE);
        editor= sharedPreferences.edit();
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
            tv_name = (TextView) view.findViewById(R.id.tv_name);
            ll_row = (LinearLayout) view.findViewById(R.id.ll_row);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.storelist_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.tv_name.setText(arrSelectStore.get(position).getName());
        holder.img_store.setImageResource(arrSelectStore.get(position).getImage());

        holder.ll_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(">> Store is selected");
                editor.putString("NavigationPosition","0");
                editor.putString("navigation","selectStore");
                editor.commit();
                context.startActivity(new Intent(context, DrawerActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrSelectStore.size();
    }
}

