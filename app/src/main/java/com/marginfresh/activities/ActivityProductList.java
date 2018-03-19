package com.marginfresh.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.marginfresh.Model.ModelProductList;
import com.marginfresh.R;
import com.marginfresh.adapter.AdapterProductList;
import com.marginfresh.db_utils.DatabaseHandler;
import com.marginfresh.domain.Config;
import com.marginfresh.domain.ConnectionDetector;
import com.marginfresh.utils.AppUtils;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.nfc.tech.MifareUltralight.PAGE_SIZE;
import static java.util.Collections.*;

/**
 * Created by bitware on 2/6/17.
 */

public class ActivityProductList extends AppCompatActivity{

    RecyclerView rv_productList;
    AdapterProductList adapterProductList;
    ImageView iv_back,iv_cart,iv_close;
    LinearLayout ll_sort,ll_filter,ll_search,ll_main,ll_searchBar;
    String store_id="",type_id="",user_id="",whichSortSelected="",searchString="";
    ArrayList<ModelProductList> arrProductList;
    ConnectionDetector cd;
    boolean isInternetPresent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView tv_storeName;
    public int screenWidth, screenHeight;
    private Button btn_cartCount,btn_notiCount;
    RelativeLayout rlCart,rl_notification,rl_wallet;
    boolean isAZSelected=false,isZASelected=false,isHtoLSelected=false,isLtoHSelected=false;
    ArrayList<ModelProductList> arrProductsAfterFilter,arrProductListToSort;
    RelativeLayout rl_cart;
    TextView tv_cartTotal,tv_cartTotalLable,tv_pullToRefresh;
    LinearLayoutManager linearLayoutManager;
    int start_limit=0,pageCount=1;
    SwipyRefreshLayout swipyRefreshLayout;
    private Parcelable recyclerViewState;
    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    DatabaseHandler db;
    SearchView search_product;

    private AdapterProductList.OnItemClickListener myItemClickListener=new AdapterProductList.OnItemClickListener() {

        @Override
        public void onClick(View v, String description) {
            // Do what you like when individual item gets clicked.

            if (description.equals("0") || description.equals("")){
                btn_cartCount.setVisibility(View.GONE);
            }else {

                btn_cartCount.setVisibility(View.VISIBLE);
                btn_cartCount.setText(description);
            }
        }

        @Override
        public void updatePrice(View view, Float price) {
            System.out.println("product price on activity product list > ");
            tv_cartTotalLable.setVisibility(View.VISIBLE);
            tv_cartTotal.setVisibility(View.VISIBLE);

            tv_cartTotal.setText("AED "+AppUtils.getFormattedPrice(price));

        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        init();
        tv_storeName.setText(sharedPreferences.getString("storeName",""));
        rl_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityProductList.this,ActivityNotificationList.class));
            }
        });

        if (sharedPreferences.getString("navigation","").equals("fromHome")){
            editor.putString("comeToProductListFrom","fromHome");
            editor.commit();
        }else if (sharedPreferences.getString("navigation","").equals("fromFoodProductList")){
            editor.putString("comeToProductListFrom","fromFoodProductList");
            editor.commit();
        }else if (sharedPreferences.getString("navigation","").equals("fromCategoryList")){
            editor.putString("comeToProductListFrom","fromCategoryList");
            editor.commit();
        }

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        rl_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("cartFrom","categoryList");
                editor.commit();
                startActivity(new Intent(ActivityProductList.this,ActivityCart.class));
            }
        });

        rl_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityProductList.this,ActivityWallet.class));
                finish();
            }
        });

        rv_productList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy>0) {

                    if (dy > 0) //check for scroll down
                    {
                        visibleItemCount = linearLayoutManager.getChildCount();
                        totalItemCount = linearLayoutManager.getItemCount();
                        pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                        if (loading) {
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                loading = false;
                                start_limit = start_limit + 20;
                                if (searchString.isEmpty()) {
                                    callApi();
                                }else {
                                    //callApiForSearch();
                                }
                            }
                        }
                    }

                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }
        });

        search_product.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchString = query;
                callApiForSearch();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length()==0){
                    //Toast.makeText(ActivityProductList.this,"Query closed",Toast.LENGTH_SHORT).show();
                    start_limit=0;
                    searchString="";
                    arrProductList = new ArrayList<>();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(search_product.getWindowToken(), 0);
                    callApi();
                }
                return false;
            }
        });


    }

    @Override
    public void onBackPressed() {
        finish();
    }
    private void init() {
        sharedPreferences=getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        cd = new ConnectionDetector(ActivityProductList.this);
        isInternetPresent = cd.isConnectingToInternet();
        iv_back= (ImageView) findViewById(R.id.iv_back);
        rv_productList= (RecyclerView) findViewById(R.id.rv_productList);
        linearLayoutManager = new LinearLayoutManager(this);
        rv_productList.setLayoutManager(linearLayoutManager);
        rv_productList.setNestedScrollingEnabled(false);
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();

        ll_filter= (LinearLayout) findViewById(R.id.ll_filter);
        ll_sort= (LinearLayout) findViewById(R.id.ll_sortBy);
        tv_storeName = (TextView) findViewById(R.id.tv_storeName);

        btn_cartCount = (Button) findViewById(R.id.btn_cartCount);
        rl_wallet = (RelativeLayout) findViewById(R.id.rl_wallet);
        rl_notification = (RelativeLayout) findViewById(R.id.rl_notification);
        iv_cart = (ImageView) findViewById(R.id.iv_cart);
        btn_notiCount = (Button) findViewById(R.id.btn_notiCount);
        rl_cart = (RelativeLayout) findViewById(R.id.rl_cart);
        tv_cartTotal = (TextView) findViewById(R.id.tv_cartTotal);
        tv_cartTotalLable = (TextView) findViewById(R.id.tv_cartTotalLable);
        tv_pullToRefresh = (TextView) findViewById(R.id.tv_pullToRefresh);

        arrProductList = new ArrayList<>();
        arrProductListToSort = new ArrayList<>();

        recyclerViewState = rv_productList.getLayoutManager().onSaveInstanceState();
        rv_productList.getLayoutManager().onRestoreInstanceState(recyclerViewState);


        store_id = sharedPreferences.getString("store_id","");
        type_id=sharedPreferences.getString("type_id","");
        user_id=sharedPreferences.getString("user_id","");

        db = new DatabaseHandler(ActivityProductList.this);

        search_product = (SearchView) findViewById(R.id.search_product);
        ll_main = (LinearLayout) findViewById(R.id.ll_main);
        iv_close = (ImageView) findViewById(R.id.iv_close);
        ll_searchBar = (LinearLayout) findViewById(R.id.ll_searchBar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Config.isFiterApply.equals("no")){
            arrProductList = new ArrayList<>();
            arrProductListToSort = new ArrayList<>();
            if (Config.isNeedToService==1) {
                start_limit = 0;
                callApi();
            }else {
                start_limit = 0;
                callApi();
            }
        }else if (Config.isFiterApply.equals("yes")){
            rv_productList.setAdapter(new AdapterProductList(ActivityProductList.this,getArrayAfterFilter(),myItemClickListener));
        }

        //showing cart count from db
        String cartCount = db.getProductsInCartCount(sharedPreferences.getString("user_id",""));
        System.out.println("Activity Cart > cart count is " +cartCount);
        if (cartCount.equals("0") || cartCount.equals("")){
            btn_cartCount.setVisibility(View.GONE);
        }else {
            btn_cartCount.setVisibility(View.VISIBLE);
            btn_cartCount.setText(""+cartCount);
        }

        //code to update notification count
        if (sharedPreferences.getString("notiCount","").equals("0") || sharedPreferences.getString("notiCount","").equals("")){
            btn_notiCount.setVisibility(View.INVISIBLE);
        }else {
            btn_notiCount.setVisibility(View.VISIBLE);
            btn_notiCount.setText(sharedPreferences.getString("notiCount",""));
        }

        //code to update cartTotalPrice
        String cartTotal = db.getCartTotal(sharedPreferences.getString("user_id",""));
        if (cartTotal.equals(0.0)){
            tv_cartTotalLable.setVisibility(View.INVISIBLE);
            tv_cartTotal.setVisibility(View.INVISIBLE);
        }else {
            if (cartTotal.equals("")){
                tv_cartTotalLable.setVisibility(View.INVISIBLE);
                tv_cartTotal.setVisibility(View.INVISIBLE);
            }else {
                tv_cartTotalLable.setVisibility(View.VISIBLE);
                tv_cartTotal.setVisibility(View.VISIBLE);
                tv_cartTotal.setText("AED "+AppUtils.getFormattedPrice(Double.parseDouble(cartTotal)));
            }
        }

        ll_sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrProductList.size() == 0){
                    Toast.makeText(ActivityProductList.this,"No Products Available",Toast.LENGTH_SHORT).show();
                }else {
                    showSortDialog();
                }
            }
        });

        ll_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrProductList.size() == 0){
                    Toast.makeText(ActivityProductList.this,"No Products Available",Toast.LENGTH_SHORT).show();
                }else {
                    if (Config.isFiterApply.equals("yes")){
                        startActivity(new Intent(ActivityProductList.this,ActivityFilter.class));
                        finish();
                    }else {
                        getMaxPrice();
                        getMinPrice();
                        startActivity(new Intent(ActivityProductList.this,ActivityFilter.class));
                        finish();
                    }


                }

            }
        });
    }

    public void callApi(){

        if (isInternetPresent){
            new ProductListTask().execute("{\"store_id\":\"" + store_id + "\",\"type_id\":\"" + type_id + "\",\"user_id\":\"" + user_id + "\",\"start_limit\":\"" + start_limit + "\",\"end_limit\":\"" + "20" +  "\"}");
        }else {
            Toast.makeText(ActivityProductList.this,R.string.no_internet,Toast.LENGTH_SHORT).show();
        }
    }
    public void showSortDialog(){

        final BottomSheetDialog dialogService = new BottomSheetDialog(this);
        dialogService.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogService.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogService.setContentView(R.layout.dialog_sort);

        LinearLayout ll_sortAZ = (LinearLayout) dialogService.findViewById(R.id.ll_sortAZ);
        LinearLayout ll_sortZA = (LinearLayout) dialogService.findViewById(R.id.ll_sortZA);
        LinearLayout ll_sortHL = (LinearLayout) dialogService.findViewById(R.id.ll_sortHL);
        LinearLayout ll_sortLH = (LinearLayout) dialogService.findViewById(R.id.ll_sortLH);

        final ImageView iv_sortAZ = (ImageView) dialogService.findViewById(R.id.iv_sortAZ);
        ImageView iv_sortZA = (ImageView) dialogService.findViewById(R.id.iv_sortZA);
        ImageView iv_sortHL = (ImageView) dialogService.findViewById(R.id.iv_sortHL);
        ImageView iv_sortLH = (ImageView) dialogService.findViewById(R.id.iv_sortLH);

        ll_sortAZ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichSortSelected = "AZ";
                if (arrProductList.size() > 1) {
                    sort(arrProductList, new Comparator<ModelProductList>() {
                        @Override
                        public int compare(ModelProductList lhs, ModelProductList rhs) {
                            dialogService.dismiss();
                            return lhs.getProduct_name().compareTo(rhs.getProduct_name());
                        }
                    });
                    rv_productList.setAdapter(new AdapterProductList(ActivityProductList.this, arrProductList,myItemClickListener));

                }else {
                    dialogService.dismiss();
                }
            }
        });

        ll_sortZA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichSortSelected = "ZA";
                if (arrProductList.size() > 1) {
                    sort(arrProductList, new Comparator<ModelProductList>() {
                        @Override
                        public int compare(ModelProductList lhs, ModelProductList rhs) {
                            dialogService.dismiss();
                            return rhs.getProduct_name().compareTo(lhs.getProduct_name());
                        }
                    });
                    rv_productList.setAdapter(new AdapterProductList(ActivityProductList.this, arrProductList,myItemClickListener));
                }else {
                    dialogService.dismiss();
                }
            }
        });

        ll_sortLH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichSortSelected = "LH";
                if (arrProductList.size() > 1) {
                    Collections.sort(arrProductList, new Comparator<ModelProductList>() {
                        @Override
                        public int compare(ModelProductList o1, ModelProductList o2) {
                            dialogService.dismiss();
                            return Float.compare(Float.parseFloat(o1.getPriceForSort()),Float.parseFloat(o2.getPriceForSort()));
                        }
                    });
                    rv_productList.setAdapter(new AdapterProductList(ActivityProductList.this, arrProductList,myItemClickListener));
                }else {
                    dialogService.dismiss();
                }
            }
        });

        ll_sortHL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichSortSelected = "HL";
                if (arrProductList.size() > 1) {
                    Collections.sort(arrProductList, new Comparator<ModelProductList>() {
                        @Override
                        public int compare(ModelProductList o1, ModelProductList o2) {
                            dialogService.dismiss();
                            return Float.compare(Float.parseFloat(o2.getPriceForSort()),Float.parseFloat(o1.getPriceForSort()));
                        }
                    });
                    rv_productList.setAdapter(new AdapterProductList(ActivityProductList.this, arrProductList,myItemClickListener));
                }else{
                    dialogService.dismiss();
                }
            }
        });

        switch (whichSortSelected){
            case "AZ":
                iv_sortAZ.setImageResource(R.drawable.sort_check);
                break;
            case "ZA":
                iv_sortZA.setImageResource(R.drawable.sort_check);
                break;
            case "HL":
                iv_sortHL.setImageResource(R.drawable.sort_check);
                break;
            case "LH":
                iv_sortLH.setImageResource(R.drawable.sort_check);
                break;

        }
        dialogService.show();
        dialogService.setCanceledOnTouchOutside(true);

    }

    class ProductListTask extends AsyncTask<String, Void, String> {

        Dialog dialog;
        @Override
        protected void onPreExecute() {
           dialog = AppUtils.customLoader(ActivityProductList.this);
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
                    .url(Config.BASE_URL+"listofproducts_all.php?"+"store_id="+store_id + "&type_id="+type_id+"&user_id="+user_id+"&start_limit="+start_limit+"&end_limit="+"20")
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
            System.out.println(">>> Product list result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){

                        loading = true;
                        //arrProductList.clear();
                        JSONArray productListArray = resObj.getJSONArray("product_array");

                            for (int i = 0; i < productListArray.length(); i++) {
                                JSONObject productObj = productListArray.getJSONObject(i);

                                ModelProductList modelProductList = new ModelProductList();
                                modelProductList.setProductId(productObj.getString("productId"));
                                String productName = productObj.getString("product_name").substring(0,1).toUpperCase() + productObj.getString("product_name").substring(1);
                                modelProductList.setProduct_name(productName);
                                modelProductList.setProduct_image_url(productObj.getString("product_image_url"));
                                modelProductList.setPrice(productObj.getString("price"));
                                modelProductList.setNew_price(productObj.getString("new_price"));
                                modelProductList.setProduct_rating_count(productObj.getString("product_rating_count"));
                                modelProductList.setProduct_isInWislist(productObj.getString("product_isInWislist"));
                                modelProductList.setProductInStock(productObj.getString("product_is_in_stock"));
                                if (productObj.getString("product_isInWislist").equalsIgnoreCase("no")){
                                    modelProductList.setIsSelected("no");
                                }else if (productObj.getString("product_isInWislist").equalsIgnoreCase("yes")){
                                    modelProductList.setIsSelected("yes");
                                }
                                modelProductList.setProductSku(productObj.getString("productSku"));
                                if (productObj.getString("new_price").equals("0")){
                                    modelProductList.setPriceForSort(productObj.getString("price"));
                                }else {
                                    modelProductList.setPriceForSort(productObj.getString("new_price"));
                                }
                                arrProductList.add(modelProductList);
                            }
                        Config.arrayListToFilter = arrProductList;
                        adapterProductList=new AdapterProductList(ActivityProductList.this,arrProductList,myItemClickListener);
                        rv_productList.scrollToPosition(start_limit-2);
                        rv_productList.setAdapter(adapterProductList);
                    }else{
                        arrProductList = new ArrayList<>();
                        arrProductListToSort = new ArrayList<>();
                        Toast.makeText(ActivityProductList.this,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityProductList.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }
            }else{
                dialog.dismiss();
                Toast.makeText(ActivityProductList.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getMinPrice(){
        Collections.sort(Config.arrayListToFilter, new Comparator<ModelProductList>() {
            @Override
            public int compare(ModelProductList o1, ModelProductList o2) {
                return Float.compare(Float.parseFloat(o1.getPriceForSort()),Float.parseFloat(o2.getPriceForSort()));
            }
        });
        System.out.println("Min Price > " +Config.arrayListToFilter.get(0).getPriceForSort());
        editor.putString("minPrice", Config.arrayListToFilter.get(0).getPriceForSort());
        editor.commit();
    }

    public void getMaxPrice(){
        Collections.sort(Config.arrayListToFilter, new Comparator<ModelProductList>() {
            @Override
            public int compare(ModelProductList o1, ModelProductList o2) {
                return Float.compare(Float.parseFloat(o2.getPriceForSort()),Float.parseFloat(o1.getPriceForSort()));
            }
        });
        System.out.println("Max Price > " +Config.arrayListToFilter.get(0).getPriceForSort());
        editor.putString("maxPrice", Config.arrayListToFilter.get(0).getPriceForSort());
        editor.commit();
    }

    public ArrayList<ModelProductList> getArrayAfterFilter(){
        arrProductList = new ArrayList<>();
        Float minRange = Float.parseFloat(sharedPreferences.getString("minRange",""));
        Float maxRange = Float.parseFloat(sharedPreferences.getString("maxRange",""));

        for (int i=0;i<Config.arrayListToFilter.size();i++){
            String price = Config.arrayListToFilter.get(i).getPriceForSort();
            Float priceInFloat = Float.valueOf(price);
            System.out.println(">>> Product Price : "+priceInFloat);
            if (priceInFloat >= minRange && priceInFloat <=maxRange){
                arrProductList.add(Config.arrayListToFilter.get(i));
            }
        }
        return arrProductList;
    }

    class SearchProductTask extends AsyncTask<String, Void, String> {

        Dialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = AppUtils.customLoader(ActivityProductList.this);
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
                    .url(Config.BASE_URL+"searchproduct.php?"+"store_id="+store_id + "&searchStringValue="+searchString+"&user_id="+user_id)
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
            System.out.println(">>> Product list result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){

                        loading = true;
                        arrProductList.clear();
                        JSONArray productListArray = resObj.getJSONArray("product_array");

                        for (int i = 0; i < productListArray.length(); i++) {
                            JSONObject productObj = productListArray.getJSONObject(i);

                            ModelProductList modelProductList = new ModelProductList();
                            modelProductList.setProductId(productObj.getString("productId"));
                            String productName = productObj.getString("product_name").substring(0,1).toUpperCase() + productObj.getString("product_name").substring(1);
                            modelProductList.setProduct_name(productName);
                            modelProductList.setProduct_image_url(productObj.getString("product_image_url"));
                            modelProductList.setPrice(productObj.getString("price"));
                            modelProductList.setNew_price(productObj.getString("new_price"));
                            modelProductList.setProduct_rating_count(productObj.getString("product_rating_count"));
                            modelProductList.setProduct_isInWislist(productObj.getString("product_isInWislist"));
                            modelProductList.setProductInStock(productObj.getString("product_is_in_stock"));
                            if (productObj.getString("product_isInWislist").equalsIgnoreCase("no")){
                                modelProductList.setIsSelected("no");
                            }else if (productObj.getString("product_isInWislist").equalsIgnoreCase("yes")){
                                modelProductList.setIsSelected("yes");
                            }
                            modelProductList.setProductSku(productObj.getString("productSku"));
                            if (productObj.getString("new_price").equals("0")){
                                modelProductList.setPriceForSort(productObj.getString("price"));
                            }else {
                                modelProductList.setPriceForSort(productObj.getString("new_price"));
                            }
                            arrProductList.add(modelProductList);
                        }
                        Config.arrayListToFilter = arrProductList;
                        adapterProductList=new AdapterProductList(ActivityProductList.this,arrProductList,myItemClickListener);
                        rv_productList.scrollToPosition(start_limit-2);
                        rv_productList.setAdapter(adapterProductList);


                    }else{
                        arrProductList = new ArrayList<>();
                        arrProductListToSort = new ArrayList<>();
                        adapterProductList=new AdapterProductList(ActivityProductList.this,arrProductList,myItemClickListener);
                        rv_productList.setAdapter(adapterProductList);
                        Toast.makeText(ActivityProductList.this,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityProductList.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                dialog.dismiss();
                Toast.makeText(ActivityProductList.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void callApiForSearch(){
        if (isInternetPresent){
            new SearchProductTask().execute("{\"store_id\":\"" + store_id + "\",\"user_id\":\"" + user_id + "\",\"searchStringValue\":\"" + searchString +  "\"}");
        }else {
            Toast.makeText(ActivityProductList.this,R.string.no_internet,Toast.LENGTH_SHORT).show();
        }
    }
}
