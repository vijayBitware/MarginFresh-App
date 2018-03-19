package com.marginfresh.Fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by bitware on 3/6/17.
 */

public class LatoLightTextView extends TextView {
    public LatoLightTextView(Context context) {
        super(context);
        setFont();
    }

    public LatoLightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }

    public LatoLightTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFont();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LatoLightTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setFont();
    }

    public void setFont(){

        Typeface typedValue = Typeface.createFromAsset(getContext().getAssets(), "lato_light.ttf");
        setTypeface(typedValue);
    }
}
