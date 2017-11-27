package com.jd.photomapp.activities.viewer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.jd.photomapp.R;
import com.jd.photomapp.view_components.SquareImageView;

/**
 * Created by jonathandunn on 26/11/2017.
 */

public class PhotoFragmentItem extends Fragment
{
    public String title;
    public String imageURL;
    public static int value;

    SquareImageView photoImageV;

    public PhotoFragmentItem()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.photo_fragment_item_layout, container, false);

        title = getArguments().getString("title");
        imageURL = getArguments().getString("image");

        TextView photoTitleV = view.findViewById(R.id.photo_fragment_item_text);
        photoTitleV.setText(title);

        photoImageV = view.findViewById(R.id.photo_fragment_item_image);
        Glide.with(getActivity())
        .load(imageURL)
        .thumbnail(0.5f)
        .into(new SimpleTarget<GlideDrawable>()
        {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation)
            {
                photoImageV.setImageDrawable(resource);
            }
        });


        return view;
    }

    public static PhotoFragmentItem newInstance(String title, String imageURL)
    {
        PhotoFragmentItem f = new PhotoFragmentItem();
        Bundle b = new Bundle();
        b.putString("title", title);
        b.putString("image", imageURL);

        Log.d("Value", String.valueOf(value));

        value++;

        f.setArguments(b);

        return f;
    }
}
