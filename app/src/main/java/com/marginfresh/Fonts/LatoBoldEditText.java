package com.marginfresh.Fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by bitware on 3/6/17.
 */

public class LatoBoldEditText extends EditText {
    public LatoBoldEditText(Context context) {
        super(context);
        setFont();
    }

    public LatoBoldEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }

    public LatoBoldEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFont();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LatoBoldEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setFont();
    }

    public void setFont(){

        Typeface typedValue = Typeface.createFromAsset(getContext().getAssets(), "lato_bold.ttf");
        setTypeface(typedValue);
    }
}
