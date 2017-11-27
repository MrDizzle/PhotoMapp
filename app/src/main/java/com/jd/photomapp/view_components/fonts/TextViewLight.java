package com.jd.photomapp.view_components.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.jd.photomapp.R;

public class TextViewLight extends android.support.v7.widget.AppCompatTextView
{
    public TextViewLight(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    public TextViewLight(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public TextViewLight(Context context)
    {
        super(context);
        init();
    }

    private void init()
    {
        if (!isInEditMode())
        {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), getContext().getString(R.string.custom_font_light));
            setTypeface(tf);
        }
    }
}
