package com.jd.photomapp.view_components.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.jd.photomapp.R;

public class TextViewRegular extends android.support.v7.widget.AppCompatTextView
{
    public TextViewRegular(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    public TextViewRegular(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public TextViewRegular(Context context)
    {
        super(context);
        init();
    }

    private void init()
    {
        if (!isInEditMode())
        {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), getContext().getString(R.string.custom_font_regular));
            setTypeface(tf);
        }
    }
}
