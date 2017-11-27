package com.jd.photomapp.activities.viewer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.jd.photomapp.R;

/**
 * Created by jonathandunn on 26/11/2017.
 */

public class ViewerActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewer_activity);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();

        ViewerFragment viewerFragment = new ViewerFragment();
        fragmentTransaction.add(R.id.viewer_activity_viewer_fragment, viewerFragment);

        fragmentTransaction.commit();
    }
}
