package com.jd.photomapp.activities.viewer;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.jd.photomapp.R;
import com.jd.photomapp.view_components.pager.AutoSpeedScroller;
import com.jd.photomapp.view_components.pager.CustomViewPager;
import com.jd.photomapp.view_components.pager.ManualSpeedScroller;
import com.jd.photomapp.models.PhotoModel;

import org.apmem.tools.layouts.FlowLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonathandunn on 26/11/2017.
 */

public class ViewerFragment extends Fragment
{
    public ViewPager photoPager;
    public List<PhotoModel> photoObjects = new ArrayList<>();
    public PhotoFragmentAdapter photoFragmentAdapter;
    public List<ImageView> indicators = new ArrayList<>();

    private Handler handler = new Handler();
    public static final int DELAY = 6000;

    public boolean disableAutoSwitch = false;

    AccelerateInterpolator autoInterpolator;
    Interpolator manualInterpolator;
    FlowLayout indicatorLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View root = inflater.inflate(R.layout.photo_fragment_layout, container, false);

        Bundle photoBundle = getActivity().getIntent().getExtras();
        photoObjects = photoBundle.getParcelableArrayList("photos");

        photoPager = (CustomViewPager) root.findViewById(R.id.photo_fragment_pager);
        photoPager.setOffscreenPageLimit(1);
        photoPager.setPageMargin(48);
        photoPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                for (int i = 0; i < indicators.size(); i++)
                {
                    if (i == position)
                    {
                        indicators.get(i).setImageResource(R.drawable.holo_circle_full);
                    }

                    else
                    {
                        indicators.get(i).setImageResource(R.drawable.holo_circle_empty);
                    }
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        photoPager.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                disableAutoSwitch();

                return false;
            }
        });

        photoFragmentAdapter = new PhotoFragmentAdapter(getFragmentManager(), photoObjects);
        photoPager.setAdapter(photoFragmentAdapter);

        indicatorLayout = root.findViewById(R.id.photo_fragment_pager_indicator_layout);

        for (int i = 0; i < photoObjects.size(); i++)
        {
            final ImageView indicatorDot = new ImageView(getActivity());

            if (i == 0)
            {
                indicatorDot.setImageResource(R.drawable.holo_circle_full);
            }

            else
            {
                indicatorDot.setImageResource(R.drawable.holo_circle_empty);
            }

            indicatorDot.setPadding(10, 10, 10, 10);
            indicatorDot.setId(i);
            indicatorDot.setAdjustViewBounds(true);
            indicatorDot.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    disableAutoSwitch();

                    for (int i = 0; i < photoObjects.size(); i++)
                    {
                        ImageView clickImageView = root.findViewById(i);

                        if (clickImageView.getId() == indicatorDot.getId())
                        {
                            clickImageView.setImageResource(R.drawable.holo_circle_full);
                        }

                        else
                        {
                            clickImageView.setImageResource(R.drawable.holo_circle_empty);
                        }
                    }

                    photoPager.setCurrentItem(indicatorDot.getId());
                }
            });

            indicators.add(indicatorDot);
            indicatorLayout.addView(indicatorDot);
        }

        try
        {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            AutoSpeedScroller scroller = new AutoSpeedScroller(photoPager.getContext(), autoInterpolator);
            mScroller.set(photoPager, scroller);
        }

        catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {e.printStackTrace();}

        autoSwitchPage.run();

        photoPager.setCurrentItem(0);

        return root;
    }

    public Runnable autoSwitchPage = new Runnable()
    {
        @Override
        public void run()
        {
            int num = photoObjects.size();

            if ((photoPager.getCurrentItem() + 1) >= num)
            {
                photoPager.setCurrentItem(0);
            }

            else
            {
                photoPager.setCurrentItem(photoPager.getCurrentItem() + 1);
            }

            handler.postDelayed(autoSwitchPage, DELAY);
        }
    };

    public void disableAutoSwitch()
    {
        if (!disableAutoSwitch)
        {
            try
            {
                Field mScroller;
                mScroller = ViewPager.class.getDeclaredField("mScroller");
                mScroller.setAccessible(true);
                ManualSpeedScroller scroller = new ManualSpeedScroller(photoPager.getContext(), manualInterpolator);
                mScroller.set(photoPager, scroller);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
            }

            handler.removeCallbacks(autoSwitchPage);

            disableAutoSwitch = true;
        }
    }
}
