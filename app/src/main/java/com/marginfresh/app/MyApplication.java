package com.marginfresh.app;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class MyApplication extends Application{
    private static MyApplication mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    public Context appcContext;
    public static int DAY_PLAN;
    public static int WEEK_PLAN;
    public static int TWO_WEEK_PLAN;
    public static int SHOP_PLAN;
    public static int CATERING_PLAN;
    /**
     * Log or request TAG
     */
    public static final String TAG = "VolleyPatterns";

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the singleton
        mInstance = this;
    }

    /**
     * @return MyApplication singleton instance
     */
    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    /**
            * @return The Volley Request queue, the queue will be created if it is null
            */
    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }
    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        VolleyLog.d("Adding request to queue: %s", req.getUrl());
        getRequestQueue().add(req);
    }
    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     * @param
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }
    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public void cancelAll() {
        try {
            mRequestQueue.cancelAll(null);
        } catch (Exception e) {

        }
    }

    public ImageLoader getImageLoader() {

        getRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
        return mImageLoader;
    }
}
