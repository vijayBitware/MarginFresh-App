package com.marginfresh.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.marginfresh.Model.ModelAllTypeArray;
import com.marginfresh.Model.ModelFoodCategoryList;
import com.marginfresh.R;
import com.marginfresh.activities.ActivityFoodProductList;
import com.marginfresh.domain.Config;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bitware on 1/6/17.
 */

public class AdapterCategoryList extends ArrayAdapter<ModelFoodCategoryList> {

    private final LayoutInflater inflater;
    Context context;
    ArrayList<ModelAllTypeArray> arrCategoryList;
    ViewHolder viewHolder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public AdapterCategoryList(Context context, int resource, ArrayList<ModelAllTypeArray> arrCategoryList) {
        super(context, resource);
        this.context = context;
        this.arrCategoryList=arrCategoryList;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sharedPreferences = context.getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static class ViewHolder{
        TextView tv_subCategoryName,tv_viewAll;
        RecyclerView rv_subCategory;
    }

    @Override
    public int getCount() {
        return arrCategoryList.size();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

           Config.arrSubCatArrayList = arrCategoryList.get(position).getArrSubTypeArrayList();
           if (Config.arrSubCatArrayList.size()==0){
               convertView = inflater.inflate(R.layout.no_data_view,null);
               viewHolder = new ViewHolder();
           }else {
               convertView = inflater.inflate(R.layout.row_food_category_list,null);
               viewHolder = new ViewHolder();

               viewHolder.tv_subCategoryName= (TextView) convertView.findViewById(R.id.tv_categoryName);
               viewHolder.rv_subCategory = (RecyclerView) convertView.findViewById(R.id.rv_foodCategory);
               viewHolder.tv_viewAll = (TextView) convertView.findViewById(R.id.tv_viewAll);
               convertView.setTag(viewHolder);

               if (arrCategoryList.get(position).getArrSubTypeArrayList().size() > 3){
                   viewHolder.tv_viewAll.setVisibility(View.VISIBLE);
               }else {
                   viewHolder.tv_viewAll.setVisibility(View.INVISIBLE);
               }
               viewHolder.tv_subCategoryName.setText(arrCategoryList.get(position).getType_name());
               LinearLayoutManager horizontalLayoutManagerCigars = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
               viewHolder.rv_subCategory.setLayoutManager(horizontalLayoutManagerCigars);
               AdapterProductScroll adapterProductScroll = new AdapterProductScroll(context,arrCategoryList.get(position).getArrSubTypeArrayList());
               viewHolder.rv_subCategory.setAdapter(adapterProductScroll);

               viewHolder.tv_viewAll.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       editor.putString("categoryName",arrCategoryList.get(position).getType_name());
                       editor.putString("navigation","fromCategoryList");
                       editor.commit();
                       Config.arrSubCatArrayList = arrCategoryList.get(position).getArrSubTypeArrayList();
                       Intent intent = new Intent(context, ActivityFoodProductList.class);
                       context.startActivity(intent);
                   }
               });
           }

        return convertView;
    }
}

