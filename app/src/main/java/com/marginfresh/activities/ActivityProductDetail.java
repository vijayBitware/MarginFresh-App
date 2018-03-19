package com.marginfresh.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.marginfresh.Model.ModelProductList;
import com.marginfresh.Model.SelectStore;
import com.marginfresh.R;
import com.marginfresh.adapter.AdaperStoreList;
import com.marginfresh.adapter.AdapterMyWishlist;
import com.marginfresh.adapter.AdapterProductList;
import com.marginfresh.adapter.AdapterZoomImage;
import com.marginfresh.db_utils.DatabaseHandler;
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
 * Created by bitware on 2/6/17.
 */

public class ActivityProductDetail extends AppCompatActivity {

    View view;
    TextView tv_count,tv_addToCart,tv_submitReview,tv_productName,
            tv_inStock,tv_price,tv_newPrice,tv_productDescripton,tv_storeName,tv_priceLine,tv_customerReview;
    int pos,counter=0;
    private int[] counters;
    ImageView iv_back,iv_wishlist,iv_rate1,iv_rate2,iv_rate3,iv_rate4,iv_rate5,iv_productImage, tv_add,tv_remove;
    boolean isWishlistSelected = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String store_id="",type_id="",user_id="",product_id="",productName="",productInStock="",ratingCount="",productPrice="",inWishlist="",productType="";
    String productNewPrice="",productDescription="",productImageUrl="",qty="",rating="",review,shoppingCartId="",productSku="",cartTotal="";
    boolean isInternetPresent,isAddToCartClicked=false;
    ConnectionDetector cd;
    RecyclerView rv_productImages;
    ArrayList<String> arrProductImages;
    private Dialog dialogService;
    TouchImageView touchViewImage ;
    public int screenWidth, screenHeight;
    RelativeLayout rl_notification,rl_cart;
    EditText edt_review;
    ImageView iv_showRate1,iv_showRate2,iv_showrate3,iv_showRate4,iv_showRate5;
    LinearLayout ll_newPrice,ll_more;
    Button btn_cart,btn_notiCount;
    Dialog dialog;
    String productPriceForStorage="";
    DatabaseHandler db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_productdetail);

        init();
        tv_storeName.setText(sharedPreferences.getString("storeName",""));
        store_id=sharedPreferences.getString("store_id","");
        type_id=sharedPreferences.getString("type_id","");
        user_id=sharedPreferences.getString("user_id","");
        product_id=sharedPreferences.getString("product_id","");
        if (isInternetPresent){
            new ProductDetailTask().execute("{\"store_id\":\"" + store_id + "\",\"type_id\":\"" + type_id + "\",\"user_id\":\"" + user_id + "\",\"product_id\":\"" + product_id +  "\"}");
        }else {
            Toast.makeText(ActivityProductDetail.this,R.string.no_internet,Toast.LENGTH_SHORT).show();
        }
        tv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter = Integer.parseInt(tv_count.getText().toString());
                counter++;
                tv_count.setText(String.valueOf(counter));
            }
        });

        tv_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int countValue = Integer.parseInt(tv_count.getText().toString());
                if (countValue !=1){
                    countValue--;
                    tv_count.setText(String.valueOf(countValue));
                }
            }
        });

        tv_addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (productInStock.equalsIgnoreCase("No")){
                    Toast.makeText(ActivityProductDetail.this,"Product Is Out Of Stock",Toast.LENGTH_SHORT).show();
                }else {

                    if (!db.isProductExitsInDb(product_id,sharedPreferences.getString("user_id",""))) {
                        Log.e("TAG","Inserting product in cart table");
                        Toast.makeText(ActivityProductDetail.this, "Product Added To Cart", Toast.LENGTH_SHORT).show();
                        db.insertProductToCart(sharedPreferences.getString("user_id",""),product_id,productSku,productName,productImageUrl,productInStock,"",inWishlist,productPriceForStorage,tv_count.getText().toString());

                        Log.e("TAG", "Products in cart > " + db.getProductsInCartCount(sharedPreferences.getString("user_id","")));
                        updateCartCount();
                    }else {
                        Log.e("TAG","Updating product qty in cart table");
                        //updating productQty
                        updateProductQtyInDb(tv_count.getText().toString());
                        updateProductPriceInDB(Integer.parseInt(tv_count.getText().toString()),productPriceForStorage);

                    }

                    if (db.isCartTotalPresentInDB(sharedPreferences.getString("user_id",""))){
                        Log.e("TAG","updating cart total");
                        updateCartTotal(Integer.parseInt(tv_count.getText().toString()));
                    }else {
                        Log.e("TAG","inserting cart total");
                        //updating cart total
                        Log.e("TAG","Cart total while inserting > " +productPriceForStorage);
                        db.insertCartTotal(productPriceForStorage,sharedPreferences.getString("user_id",""));
                        Log.e("TAG","Cart total after calulaton > " +db.getCartTotal(sharedPreferences.getString("user_id","")));
                    }

                    if (sharedPreferences.getString("shoppingCartId","").equals("")) {
                        qty=tv_count.getText().toString();
                        user_id=sharedPreferences.getString("user_id","");
                        product_id=sharedPreferences.getString("product_id","");
                        productSku = sharedPreferences.getString("productSku","");
                        shoppingCartId = sharedPreferences.getString("shoppingCartId","");
                        if (!qty.equals("0")){
                            if (isInternetPresent){
                                new AddToCart().execute("{\"customerId\":\"" + user_id + "\",\"productId\":\"" + product_id + "\",\"productQty\":\"" + qty + "\",\"productSku\":\"" + productSku  +  "\"}");
                            }else {
                                Toast.makeText(ActivityProductDetail.this,R.string.no_internet,Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(ActivityProductDetail.this,"Please Enter Quantity",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        iv_wishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isWishlistSelected){
                    iv_wishlist.setImageResource(R.drawable.favorite);
                    isWishlistSelected=true;
                    Toast.makeText(ActivityProductDetail.this,"User can add product to wishlist",Toast.LENGTH_SHORT).show();
                }else {
                    iv_wishlist.setImageResource(R.drawable.wishlist_inactive);
                    isWishlistSelected=false;
                    Toast.makeText(ActivityProductDetail.this,"User can remove product to wishlist",Toast.LENGTH_SHORT).show();
                }
            }
        });

        rateProduct();

        tv_submitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                review =edt_review.getText().toString();
                if (!rating.isEmpty()){
                    if (!review.isEmpty()) {
                        if (isInternetPresent) {
                            new SubmitReview().execute("{\"customerId\":\"" + user_id + "\",\"productId\":\"" + product_id + "\",\"rating_value\":\"" + rating + "\",\"description\":\"" + review + "\",\"summaryreview\":\"" + "Review" + "\"}");
                        } else {
                            Toast.makeText(ActivityProductDetail.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(ActivityProductDetail.this,"Please Enter Review",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(ActivityProductDetail.this,"Please Give Rating",Toast.LENGTH_SHORT).show();
                }
            }
        });

        iv_productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageZoomDialog();
            }
        });
        rl_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityProductDetail.this,ActivityNotificationList.class));
            }
        });
        rl_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("cartFrom","productDetail");
                editor.commit();
                startActivity(new Intent(ActivityProductDetail.this,ActivityCart.class));
               // finish();
            }
        });

        iv_wishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                product_id = sharedPreferences.getString("product_id","");
                user_id = sharedPreferences.getString("user_id","");
                if (isInternetPresent){
                    if (inWishlist.equalsIgnoreCase("no")){
                        new AddToWishList().execute("{\"user_id\":\"" + user_id + "\",\"product_id\":\"" + product_id + "\"}");
                    }else if (inWishlist.equalsIgnoreCase("yes")){
                        new DeleteWishlist().execute("{\"user_id\":\"" + user_id + "\",\"product_id\":\"" + product_id + "\"}");
                    }

                }else {
                    Toast.makeText(ActivityProductDetail.this,R.string.no_internet,Toast.LENGTH_SHORT).show();
                }

            }
        });

        tv_customerReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityProductDetail.this,ActivityCustomerReview.class));
            }
        });
    }

    private void updateProductQtyInDb(String qty) {
        int productQtyFromDb = db.getProductQty(product_id,sharedPreferences.getString("user_id",""));
        Log.e("TAG","Product Qty from db - " +productQtyFromDb);
        int finalQty = productQtyFromDb + Integer.parseInt(qty);
        Log.e("TAG","Product Qty while inserted in db - " +finalQty);
        db.updateProductQty(product_id, String.valueOf(finalQty),sharedPreferences.getString("user_id",""));
        Log.e("TAG","Product Qty after stored in db - " +db.getProductQty(product_id,sharedPreferences.getString("user_id","")));
    }

    private void updateProductPriceInDB(int qty,String pPrice) {
        String pPriceFromDB = db.getProductPrice(product_id,sharedPreferences.getString("user_id",""));
        Log.e("TAG","Product Price from db > " +pPriceFromDB);
        Float finalPrice = Float.parseFloat(pPriceFromDB) + (qty * Float.parseFloat(pPrice));
        Log.e("TAG","Product Price while  adding in db > " +pPriceFromDB);
        db.updateProductPrice(String.valueOf(finalPrice),product_id,sharedPreferences.getString("user_id",""));
        Log.e("TAG","Product Price after adding in db > " +db.getProductPrice(product_id,sharedPreferences.getString("user_id","")));
    }

    private void updateCartTotal(int qty){
        String presentCartTotal = db.getCartTotal(sharedPreferences.getString("user_id",""));
        Log.e("TAG","Cart total from db > " +presentCartTotal);
        Float cartTotal = Float.parseFloat(presentCartTotal) + (qty * Float.parseFloat(productPriceForStorage));
        Log.e("TAG","Cart total while inserting in db > " +cartTotal);
        db.updateCartTotal(String.valueOf(cartTotal),sharedPreferences.getString("user_id",""));
        Log.e("TAG","Cart total after calulaton > " +db.getCartTotal(sharedPreferences.getString("user_id","")));
    }

    private void showImageZoomDialog() {
        dialogService = new Dialog(ActivityProductDetail.this);
        dialogService.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogService.setContentView(R.layout.dialog_zoomimage);

        touchViewImage = (TouchImageView) dialogService.findViewById(R.id.iv_zoomImage);
        touchViewImage.setImageDrawable(iv_productImage.getDrawable());

        ImageView iv_close = (ImageView) dialogService.findViewById(R.id.iv_close);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogService.dismiss();
            }
        });

        RecyclerView rv_productImages = (RecyclerView) dialogService.findViewById(R.id.rv_productImageDialog);
        LinearLayoutManager horizontalLayoutManagerCigars = new LinearLayoutManager(ActivityProductDetail.this, LinearLayoutManager.HORIZONTAL, false);
        rv_productImages.setLayoutManager(horizontalLayoutManagerCigars);
        editor.putString("navigation","fromDialog");
        editor.commit();
        rv_productImages.setAdapter(new AdapterZoomImage(ActivityProductDetail.this,arrProductImages));

        WindowManager.LayoutParams wmlp = dialogService.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER_HORIZONTAL;
        dialogService.show();
        dialogService.setCanceledOnTouchOutside(true);
        dialogService.getWindow().setLayout((screenWidth/2) + 280,(screenHeight/2)+300);
    }

    private void rateProduct() {
        iv_rate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating ="1";
                iv_rate1.setImageResource(R.drawable.yellow_star);
                iv_rate2.setImageResource(R.drawable.gray_star);
                iv_rate3.setImageResource(R.drawable.gray_star);
                iv_rate4.setImageResource(R.drawable.gray_star);
                iv_rate5.setImageResource(R.drawable.gray_star);
            }
        });

        iv_rate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating ="2";
                iv_rate1.setImageResource(R.drawable.yellow_star);
                iv_rate2.setImageResource(R.drawable.yellow_star);
                iv_rate3.setImageResource(R.drawable.gray_star);
                iv_rate4.setImageResource(R.drawable.gray_star);
                iv_rate5.setImageResource(R.drawable.gray_star);
            }
        });

        iv_rate3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating="3";
                iv_rate1.setImageResource(R.drawable.yellow_star);
                iv_rate2.setImageResource(R.drawable.yellow_star);
                iv_rate3.setImageResource(R.drawable.yellow_star);
                iv_rate4.setImageResource(R.drawable.gray_star);
                iv_rate5.setImageResource(R.drawable.gray_star);
            }
        });
        iv_rate4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating ="4";
                iv_rate1.setImageResource(R.drawable.yellow_star);
                iv_rate2.setImageResource(R.drawable.yellow_star);
                iv_rate3.setImageResource(R.drawable.yellow_star);
                iv_rate4.setImageResource(R.drawable.yellow_star);
                iv_rate5.setImageResource(R.drawable.gray_star);
            }
        });

        iv_rate5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating="5";
                iv_rate1.setImageResource(R.drawable.yellow_star);
                iv_rate2.setImageResource(R.drawable.yellow_star);
                iv_rate3.setImageResource(R.drawable.yellow_star);
                iv_rate4.setImageResource(R.drawable.yellow_star);
                iv_rate5.setImageResource(R.drawable.yellow_star);
            }
        });



    }

    private void init() {
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        cd = new ConnectionDetector(ActivityProductDetail.this);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences =getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor =sharedPreferences.edit();
        iv_wishlist= (ImageView) findViewById(R.id.iv_wishlist);
        iv_back= (ImageView) findViewById(R.id.iv_back);
        tv_add= (ImageView) findViewById(R.id.tv_add);
        tv_remove= (ImageView) findViewById(R.id.tv_remove);
        tv_count = (TextView) findViewById(R.id.tv_count);
        tv_addToCart= (TextView) findViewById(R.id.tv_addToCart);
        iv_rate1= (ImageView) findViewById(R.id.iv_rate1);
        iv_rate2= (ImageView) findViewById(R.id.iv_rate2);
        iv_rate3= (ImageView) findViewById(R.id.iv_rate3);
        iv_rate4= (ImageView) findViewById(R.id.iv_rate4);
        iv_rate5= (ImageView) findViewById(R.id.iv_rate5);
        tv_submitReview = (TextView) findViewById(R.id.tv_submitReview);
        tv_productName = (TextView) findViewById(R.id.tv_productName);
        tv_inStock = (TextView) findViewById(R.id.tv_inStock);
        tv_price = (TextView) findViewById(R.id.tv_pPrice);
        tv_newPrice= (TextView) findViewById(R.id.tv_pNewPrice);
        iv_productImage = (ImageView) findViewById(R.id.iv_producImage);
        tv_productDescripton = (TextView) findViewById(R.id.tv_description);
        tv_storeName = (TextView) findViewById(R.id.tv_storeName);
        rv_productImages = (RecyclerView) findViewById(R.id.rv_productImages);
        LinearLayoutManager horizontalLayoutManagerCigars = new LinearLayoutManager(ActivityProductDetail.this, LinearLayoutManager.HORIZONTAL, false);
        rv_productImages.setLayoutManager(horizontalLayoutManagerCigars);
        arrProductImages = new ArrayList<>();
        rl_notification= (RelativeLayout) findViewById(R.id.rl_notification);
        rl_cart = (RelativeLayout) findViewById(R.id.rlCart);
        edt_review = (EditText) findViewById(R.id.edt_review);
        iv_showRate1= (ImageView) findViewById(R.id.iv_showRating1);
        iv_showRate2= (ImageView) findViewById(R.id.iv_showRating2);
        iv_showrate3= (ImageView) findViewById(R.id.iv_showRating3);
        iv_showRate4= (ImageView) findViewById(R.id.iv_showRating4);
        iv_showRate5= (ImageView) findViewById(R.id.iv_showRating5);
        btn_cart = (Button) findViewById(R.id.btn_cart);
        tv_priceLine = (TextView) findViewById(R.id.tv_priceLine);
        ll_newPrice = (LinearLayout) findViewById(R.id.ll_newPrice);

        btn_cart = (Button) findViewById(R.id.btn_cart);
        rl_cart = (RelativeLayout) findViewById(R.id.rlCart);

        ll_more = (LinearLayout) findViewById(R.id.ll_more);
        tv_customerReview = (TextView) findViewById(R.id.tv_customerReview);
        btn_notiCount = (Button) findViewById(R.id.btn_notiCount);
        dialog = AppUtils.customLoader(ActivityProductDetail.this);

        db = new DatabaseHandler(ActivityProductDetail.this);

    }

    @Override
    public void onBackPressed() {
        finish();

    }

    class ProductDetailTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
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
                    .url(Config.BASE_URL+"productdetailpage.php?"+"store_id="+store_id + "&type_id="+type_id+"&user_id="+user_id+"&product_id="+product_id)
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
            System.out.println(">>> Product detail result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
//                        Toast.makeText(ActivityProductDetail.this,message,Toast.LENGTH_SHORT).show();
                        product_id = resObj.getString("product_id");
                        productName = resObj.getString("product_name");
                        productInStock = resObj.getString("product_is_in_stock");
                        ratingCount = resObj.getString("product_rating_count");
                        productPrice = resObj.getString("price");
                        productNewPrice = resObj.getString("new_price");
                        productDescription = resObj.getString("product_description");
                        inWishlist = resObj.getString("product_isInWislist");
                        productSku = resObj.getString("sku");
                        productType = resObj.getString("productType");

                        if (productNewPrice.equals("0.00")){
                            productPriceForStorage = productPrice;
                        }else {
                            productPriceForStorage = productNewPrice;
                        }
                        JSONArray productImagesArray = resObj.getJSONArray("product_image_url");
                        for (int i=0;i<productImagesArray.length();i++){
                            JSONObject imageObj = productImagesArray.getJSONObject(i);
                            arrProductImages.add(imageObj.getString("url"));
                        }
                        System.out.println("Images Array size >>> " +arrProductImages.size());
                        editor.putString("navigation","fromProductDetail");
                        editor.putString("product_id",product_id);
                        editor.putString("productSku",productSku);
                        editor.putString("productType",productType);
                        editor.commit();
                        rv_productImages.setAdapter(new AdapterProductImage(ActivityProductDetail.this,arrProductImages));
                        setData();

                    }else{
                        Toast.makeText(ActivityProductDetail.this,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityProductDetail.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                dialog.dismiss();
                Toast.makeText(ActivityProductDetail.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setData() {
        tv_productName.setText(productName);
        Glide.with(ActivityProductDetail.this).load(arrProductImages.get(0)).into(iv_productImage);
        if (productInStock.equalsIgnoreCase("yes")){
            tv_inStock.setText("In Stock");
        }else {
            tv_inStock.setText("Out Of Stock");
            tv_inStock.setTextColor(getResources().getColor(R.color.shape_bg));
        }

        if (inWishlist.equals("no")){
            iv_wishlist.setImageResource(R.drawable.wishlist_inactive);
        }else {
            iv_wishlist.setImageResource(R.drawable.wishlist_red);
        }
        if (productNewPrice.equals("0.00")){
            ll_newPrice.setVisibility(View.GONE);
            tv_price.setText("PRICE: "+sharedPreferences.getString("currency","")+" "+ AppUtils.getFormattedPrice(Double.parseDouble(productPrice)));
        }else {
            ll_newPrice.setVisibility(View.VISIBLE);
            tv_price.setText("PRICE: "+sharedPreferences.getString("currency","")+" "+AppUtils.getFormattedPrice(Double.parseDouble(productPrice)));
            tv_priceLine.setVisibility(View.VISIBLE);
            tv_newPrice.setText("NEW PRICE: "+sharedPreferences.getString("currency","")+" "+AppUtils.getFormattedPrice(Double.parseDouble(productNewPrice)));
        }
        tv_productDescripton.setText(productDescription);
        Layout l = tv_productDescripton.getLayout();
        if (l !=null){
            int lines = l.getLineCount();
            if (lines==3){
                if (l.getEllipsisCount(lines-1) > 0){
                    ll_more.setVisibility(View.VISIBLE);
                    ll_more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDiscriptionDialog(productDescription);
                        }
                    });
                }
            }
        }
        switch (ratingCount){
            case "1":
                iv_showRate1.setImageResource(R.drawable.yellow_star);
                iv_showRate2.setImageResource(R.drawable.gray_star);
                iv_showrate3.setImageResource(R.drawable.gray_star);
                iv_showRate4.setImageResource(R.drawable.gray_star);
                iv_showRate5.setImageResource(R.drawable.gray_star);
                break;
            case "2":
                iv_showRate1.setImageResource(R.drawable.yellow_star);
                iv_showRate2.setImageResource(R.drawable.yellow_star);
                iv_showrate3.setImageResource(R.drawable.gray_star);
                iv_showRate4.setImageResource(R.drawable.gray_star);
                iv_showRate5.setImageResource(R.drawable.gray_star);
                break;
            case "3":
                iv_showRate1.setImageResource(R.drawable.yellow_star);
                iv_showRate2.setImageResource(R.drawable.yellow_star);
                iv_showrate3.setImageResource(R.drawable.yellow_star);
                iv_showRate4.setImageResource(R.drawable.gray_star);
                iv_showRate5.setImageResource(R.drawable.gray_star);
                break;
            case "4":
                iv_showRate1.setImageResource(R.drawable.yellow_star);
                iv_showRate2.setImageResource(R.drawable.yellow_star);
                iv_showrate3.setImageResource(R.drawable.yellow_star);
                iv_showRate4.setImageResource(R.drawable.yellow_star);
                iv_showRate5.setImageResource(R.drawable.gray_star);
                break;
            case "5":
                iv_showRate1.setImageResource(R.drawable.yellow_star);
                iv_showRate2.setImageResource(R.drawable.yellow_star);
                iv_showrate3.setImageResource(R.drawable.yellow_star);
                iv_showRate4.setImageResource(R.drawable.yellow_star);
                iv_showRate5.setImageResource(R.drawable.yellow_star);
                break;
            default:
                break;
        }
    }

    private void showDiscriptionDialog(String msg) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(ActivityProductDetail.this);
        alertDialogBuilder
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    class AdapterProductImage extends RecyclerView.Adapter<AdapterProductImage.MyViewHolder>{
        Context context;
        ArrayList<String> arrSelectStore;
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;

        public AdapterProductImage(Context context, ArrayList<String> arrSelectStore)
        {
            this.context=context;
            this.arrSelectStore=arrSelectStore;

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
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_productimage, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            Glide.with(context).load(arrSelectStore.get(position)).into(holder.productImage);
            holder.productImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sharedPreferences.getString("navigation","").equals("fromProductDetail")){
                        Glide.with(ActivityProductDetail.this).load(arrProductImages.get(position)).into(iv_productImage);
                    }else {
                        Glide.with(ActivityProductDetail.this).load(arrProductImages.get(position)).into(touchViewImage);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrSelectStore.size();
        }
    }
    class AddToWishList extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
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
                        Toast.makeText(ActivityProductDetail.this,message,Toast.LENGTH_SHORT).show();
                        inWishlist = "yes";
                        Config.isNeedToService = 1;
                        iv_wishlist.setImageResource(R.drawable.wishlist_red);

                    }else{
                        Toast.makeText(ActivityProductDetail.this,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityProductDetail.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                dialog.dismiss();
                Toast.makeText(ActivityProductDetail.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
    class DeleteWishlist extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                        Toast.makeText(ActivityProductDetail.this, response_msg, Toast.LENGTH_SHORT).show();
                        inWishlist = "no";
                        Config.isNeedToService = 1;
                        iv_wishlist.setImageResource(R.drawable.wishlist_inactive);

                    } else {
                        dialog.dismiss();
                        Toast.makeText(ActivityProductDetail.this, response_msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityProductDetail.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            } else {
                dialog.dismiss();
                Toast.makeText(ActivityProductDetail.this,"Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
    class AddToCart extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
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
                    .url(Config.BASE_URL_SHOPCART+"shopcart/addtocart.php?"+"customerId="+user_id+"&productId="+product_id+"&productQty="+qty+"&productSku="+productSku)
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
            System.out.println(">>> Add to cart result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
                       // Toast.makeText(ActivityProductDetail.this,message,Toast.LENGTH_SHORT).show();
                        shoppingCartId = resObj.getString("cart_id");
                        cartTotal = resObj.getString("cartTotal");
                        String cartItemsCount = resObj.getString("cartItemsCount");
                        editor.putString("cartItemCount",cartItemsCount);
                        editor.putString("shoppingCartId",shoppingCartId);
                        editor.commit();
                    }else{
                        Toast.makeText(ActivityProductDetail.this,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityProductDetail.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                Toast.makeText(ActivityProductDetail.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        }
    }

    class SubmitReview extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
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
                    .url(Config.BASE_URL+"customer_rate_product.php?"+"customerId="+user_id+"&productId="+product_id+"&rating_value="+rating +"&description="+review+"&summaryreview=Review")
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
            System.out.println(">>> Submit Review result : "+s);
            if (s != null) {
                dialog.dismiss();
                try {
                    JSONObject resObj = new JSONObject(s);
                    String status = resObj.getString("status");
                    String message = resObj.getString("message");
                    if(status.equals("1")){
                        Toast.makeText(ActivityProductDetail.this,message,Toast.LENGTH_SHORT).show();
                        edt_review.setText("");
                        iv_rate1.setImageResource(R.drawable.gray_star);
                        iv_rate2.setImageResource(R.drawable.gray_star);
                        iv_rate3.setImageResource(R.drawable.gray_star);
                        iv_rate4.setImageResource(R.drawable.gray_star);
                        iv_rate5.setImageResource(R.drawable.gray_star);

                    }else{
                        Toast.makeText(ActivityProductDetail.this,message,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityProductDetail.this,getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

                }
            }else{
                Toast.makeText(ActivityProductDetail.this, "Something went wrong.Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //showing cart count from db
        updateCartCount();

        //code to update notification count
        if (sharedPreferences.getString("notiCount","").equals("0") || sharedPreferences.getString("notiCount","").equals("")){
            btn_notiCount.setVisibility(View.INVISIBLE);
        }else {
            btn_notiCount.setVisibility(View.VISIBLE);
            btn_notiCount.setText(sharedPreferences.getString("notiCount",""));
        }
    }

    public void updateCartCount(){
        String cartCount = db.getProductsInCartCount(sharedPreferences.getString("user_id",""));
        System.out.println("Activity Cart > cart count is " +cartCount);
        if (cartCount.equals("0") || cartCount.equals("")){
            btn_cart.setVisibility(View.GONE);
        }else {
            btn_cart.setVisibility(View.VISIBLE);
            btn_cart.setText(cartCount);
        }
    }
}
