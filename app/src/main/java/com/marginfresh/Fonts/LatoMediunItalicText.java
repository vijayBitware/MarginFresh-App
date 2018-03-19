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

public class LatoMediunItalicText extends TextView {

    public LatoMediunItalicText(Context context) {
        super(context);
        setFont();
    }

    public LatoMediunItalicText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }

    public LatoMediunItalicText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFont();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LatoMediunItalicText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setFont();
    }

    public void setFont(){

        Typeface typedValue = Typeface.createFromAsset(getContext().getAssets(), "lato_mediunitalic.ttf");
        setTypeface(typedValue);
    }
}
