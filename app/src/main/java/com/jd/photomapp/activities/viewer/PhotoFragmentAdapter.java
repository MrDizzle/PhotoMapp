package com.jd.photomapp.activities.viewer;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jd.photomapp.models.ImageSize;
import com.jd.photomapp.models.PhotoModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonathandunn on 26/11/2017.
 */

public class PhotoFragmentAdapter extends FragmentPagerAdapter
{
    private List<PhotoFragmentItem> photoItems = new ArrayList<>();

    PhotoFragmentAdapter(FragmentManager fm, List<PhotoModel> photoObjects)
    {
        super(fm);

        for(int i = 0; i < photoObjects.size(); i++)
        {
            PhotoFragmentItem photoItem = PhotoFragmentItem.newInstance(
                    photoObjects.get(i).getTitle(), photoObjects.get(i).getImageUrl(ImageSize.LARGE));

            photoItems.add(photoItem);
        }
    }

    @Override
    public PhotoFragmentItem getItem(int position)
    {
        return photoItems.get(position);
    }

    @Override
    public int getCount()
    {
        return photoItems.size();
    }
}
