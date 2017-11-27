package com.jd.photomapp.activities.map.cluster;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.jd.photomapp.R;
import com.jd.photomapp.models.ImageSize;
import com.jd.photomapp.models.PhotoModel;

public class PhotoClusterRenderer extends DefaultClusterRenderer<PhotoModel>
{
    final IconGenerator markerIconGenerator;
    final IconGenerator clusterIconGenerator;
    final ImageView markerImageView;
    final TextView clusterTextView;

    Bitmap markerIcon;

    Context context;

    public PhotoClusterRenderer(Context context, GoogleMap map, ClusterManager<PhotoModel> clusterManager)
    {
        super(context, map, clusterManager);

        this.context = context;

        clusterIconGenerator = new IconGenerator(context.getApplicationContext());
        View clusterView = LayoutInflater.from(context).inflate(R.layout.cluster_layout, null);
        clusterIconGenerator.setContentView(clusterView);
        clusterTextView = clusterView.findViewById(R.id.cluster_layout_item_num_text);

        markerIconGenerator = new IconGenerator(context.getApplicationContext());
        View markerView = LayoutInflater.from(context).inflate(R.layout.marker_layout, null);
        markerIconGenerator.setContentView(markerView);
        markerImageView = markerView.findViewById(R.id.marker_layout_image);
    }

    @Override
    protected void onBeforeClusterItemRendered(PhotoModel pic, MarkerOptions markerOptions)
    {
        markerIcon = markerIconGenerator.makeIcon(pic.getTitle());
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerIcon)).title(pic.getTitle());
    }

    @Override
    protected void onClusterItemRendered(PhotoModel clusterItem, final Marker marker)
    {
        marker.setTag("active");

        Glide.with(context)
        .load(clusterItem.getImageUrl(ImageSize.MEDIUM))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .thumbnail(0.5f)
        .into(new SimpleTarget<GlideDrawable>()
        {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation)
            {
                markerImageView.setImageDrawable(resource);
                markerIcon = markerIconGenerator.makeIcon();

                if(marker.getTag() != null) // A hack to handle marker reference being lost - Problem described here: https://stackoverflow.com/questions/41902478/illegalargumentexception-unmanaged-descriptor-using-gms-maps-model-marker-setic
                {
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(markerIcon));
                }
            }
        });
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<PhotoModel> cluster, MarkerOptions markerOptions)
    {
        clusterTextView.setText(String.valueOf(cluster.getSize()));
        Bitmap icon = clusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster)
    {
        return cluster.getSize() > 1;
    }
}
