package com.jd.photomapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;

public class PhotoModel implements ClusterItem, Serializable, Parcelable
{
    private static final String IMAGE_URL = "https://farm%s.staticflickr.com/%s/%s_%s_%s.jpg";

    String title;
    long id;
    String secret;
    String server;
    int farm;
    float latitude;
    float longitude;
    String imageURL;

    PhotoModel(Parcel in)
    {
        this.title = in.readString();
        this.id = in.readLong();
        this.secret = in.readString();
        this.server = in.readString();
        this.farm = in.readInt();
        this.latitude = in.readFloat();
        this.longitude = in.readFloat();
        this.imageURL = in.readString();
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    public String getImageUrl(ImageSize size)
    {
        return String.format(IMAGE_URL, farm, server, id, secret, size.getValue());
    }

    @Override
    public String getSnippet()
    {
        return null;
    }

    @Override
    public LatLng getPosition()
    {
        return new LatLng(latitude, longitude);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeString(this.title);
        parcel.writeLong(this.id);
        parcel.writeString(this.secret);
        parcel.writeString(this.server);
        parcel.writeInt(this.farm);
        parcel.writeFloat(this.latitude);
        parcel.writeFloat(this.longitude);
        parcel.writeString(this.getImageUrl(ImageSize.LARGE));
    }

    public static final Parcelable.Creator<PhotoModel> CREATOR = new Parcelable.Creator<PhotoModel>()
    {
        public PhotoModel createFromParcel(Parcel in)
        {
            return new PhotoModel(in);
        }

        public PhotoModel[] newArray(int size)
        {
            return new PhotoModel[size];
        }
    };
}