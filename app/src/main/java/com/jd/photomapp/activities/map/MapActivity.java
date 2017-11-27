package com.jd.photomapp.activities.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.jd.photomapp.BuildConfig;
import com.jd.photomapp.R;
import com.jd.photomapp.activities.base.BaseActivity;
import com.jd.photomapp.activities.viewer.ViewerActivity;
import com.jd.photomapp.activities.map.cluster.PhotoClusterRenderer;
import com.jd.photomapp.models.PhotoModel;
import com.jd.photomapp.models.PlaceModel;
import com.shawnlin.numberpicker.NumberPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonathandunn on 25/11/2017.
 */

public class MapActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener
{
    GoogleMap mGoogleMap;
    SupportMapFragment mapFragment;

    SwitchCompat locationSwitch;
    NumberPicker numOfResultsPicker;

    ClusterManager<PhotoModel> clusterManager;
    List<PhotoModel> pictureList;
    List<PlaceModel> placeList;

    boolean locationBased = false;

    final String API_KEY = "e28de5f3b02436001f54bc62695fc4ab";
    final String URL_PHOTOS_BASE = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=" + API_KEY;
    final String URL_PLACES_BASE = "https://api.flickr.com/services/rest/?method=flickr.places.find&api_key=" + API_KEY;
    final String QUERY = "&query=";
    final String FORMAT = "&page=&format=json&nojsoncallback=1";
    final String GEO = "&has_geo=true&extras=geo";
    final String TAGS = "&tags=";
    final String LAT = "&lat=";
    final String LON = "&lon=";
    final String RADIUS = "&radius=20";
    final String PERPAGE = "&per_page=";

    final String COLUMN_PHOTO = "photo";
    final String COLUMN_PHOTOS = "photos";
    final String COLUMN_PLACE = "place";
    final String COLUMN_PLACES = "places";

    int numberOfResults = 100;

    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        queue = Volley.newRequestQueue(getApplicationContext());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_activity_map_fragment);
        mapFragment.getMapAsync(this);

        numOfResultsPicker = findViewById(R.id.map_activity_num_of_results_picker);
        numberOfResults = numOfResultsPicker.getValue();
        numOfResultsPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                numberOfResults = newVal;
            }
        });

        locationSwitch = findViewById(R.id.map_activity_location_switch);
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked)
            {
                locationBased = checked;

                if (checked && lastLocation != null) // When user switches location mode on, map camera zooms to users current location
                {
                    CameraUpdate zoomTo = CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 3);
                    mGoogleMap.animateCamera(zoomTo, 1500, null);

                    search("");
                }
            }
        });

        final EditText searchEditText = findViewById(R.id.map_activity_search_edit_text);
        searchEditText.setImeActionLabel("Enter", KeyEvent.KEYCODE_ENTER);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                boolean handled = false;

                if (actionId == android.view.KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE)
                {
                    search(searchEditText.getText().toString());
                    handled = true;
                }

                return handled;
            }
        });

        final TextWatcher textWatcher = new TextWatcher()
        {
            final android.os.Handler handler = new android.os.Handler();
            Runnable runnable;

            public void onTextChanged(final CharSequence s, int start, final int before, int count)
            {
                handler.removeCallbacks(runnable);
            }

            @Override
            public void afterTextChanged(final Editable searchInput)
            {
                runnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        search(searchInput.toString());
                    }
                };
                handler.postDelayed(runnable, 1000);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        };

        searchEditText.addTextChangedListener(textWatcher);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap = googleMap;

        clusterManager = new ClusterManager<>(this, mGoogleMap);
        clusterManager.setRenderer(new PhotoClusterRenderer(getApplicationContext(), mGoogleMap, clusterManager));
        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<PhotoModel>()
        {
            @Override
            public boolean onClusterClick(Cluster<PhotoModel> cluster)
            {
                closeKeyboard();

                Intent intent = new Intent(MapActivity.this, ViewerActivity.class);
                List<PhotoModel> photoList = (List<PhotoModel>) cluster.getItems();
                intent.putParcelableArrayListExtra("photos", (ArrayList<? extends Parcelable>) photoList);

                startActivity(intent);

                return false;
            }
        });

        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<PhotoModel>()
        {
            @Override
            public boolean onClusterItemClick(PhotoModel photoModel)
            {
                closeKeyboard();

                List<PhotoModel> photoList = new ArrayList<>();
                photoList.add(photoModel);
                Intent intent = new Intent(MapActivity.this, ViewerActivity.class);
                intent.putParcelableArrayListExtra("photos", (ArrayList<? extends Parcelable>) photoList);

                startActivity(intent);

                return false;
            }
        });

        mGoogleMap.setOnCameraIdleListener(clusterManager);
        mGoogleMap.setOnMarkerClickListener(clusterManager);
        mGoogleMap.setOnInfoWindowClickListener(clusterManager);
        mGoogleMap.setOnMarkerClickListener(clusterManager);

        if(checkLocationPermission())
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
    }

    public void search(String searchTerm)
    {
        closeKeyboard();

        try
        {
            if(locationBased)
            {
                searchByLocation(searchTerm);
            }

            else
            {
                searchByTag(searchTerm);
            }
        }
        catch (IOException | AuthFailureError e) {e.printStackTrace();}
    }

    public void searchByTag(String searchTerm) throws IOException, AuthFailureError
    {
        String url = URL_PHOTOS_BASE + TAGS + searchTerm + GEO + PERPAGE + numberOfResults + FORMAT;

        CameraUpdate zoomOut = CameraUpdateFactory.zoomTo(1);
        mGoogleMap.animateCamera(zoomOut, 3000, null);

        getPicturesRequest(url);
    }

    public void searchByLocation(String searchTerm)
    {
        if(searchTerm.equals(""))
        {
            CameraUpdate zoomTo = CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 10);
            mGoogleMap.animateCamera(zoomTo, 3000, null);

            String currentLatitude = String.valueOf(lastLocation.getLatitude());
            String currentLongitude = String.valueOf(lastLocation.getLongitude());
            String url = URL_PHOTOS_BASE + GEO + LAT + currentLatitude + LON + currentLongitude + RADIUS + PERPAGE + numberOfResults + FORMAT;

            getPicturesRequest(url);
        }

        else
        {
            String url = URL_PLACES_BASE + QUERY + searchTerm + FORMAT;

            getLocationRequest(url);
        }
    }

    public void getLocationRequest(String url)
    {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    try
                    {
                        Log.d("JSON", response);

                        JSONArray jsonArray = new JSONObject(response).getJSONObject(COLUMN_PLACES).getJSONArray(COLUMN_PLACE);
                        Type listType = new TypeToken<List<PlaceModel>>(){}.getType();
                        placeList = new GsonBuilder().create().fromJson(jsonArray.toString(), listType);

                        if(placeList.size() > 0)
                        {
                            // Unfortunately the Flickr API endpoint for querying places does not allow for specifying the number of results you want to return, so we just take the first result.
                            LatLng coord = new LatLng(placeList.get(0).getLatitude(), placeList.get(0).getLongitude());

                            CameraUpdate zoomTo = CameraUpdateFactory.newLatLngZoom(coord, 10);
                            mGoogleMap.animateCamera(zoomTo, 3000, null);

                            String resultLatitude = String.valueOf(coord.latitude);
                            String resultLongitude = String.valueOf(coord.longitude);

                            String url = URL_PHOTOS_BASE + GEO + LAT + resultLatitude + LON + resultLongitude + RADIUS + PERPAGE + numberOfResults + FORMAT;

                            getPicturesRequest(url);
                        }
                    }

                    catch (JSONException error)
                    {
                        error.printStackTrace();

                        if (BuildConfig.DEBUG)
                        {
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            },
            new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    error.printStackTrace();

                    if (BuildConfig.DEBUG)
                    {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

        queue.add(stringRequest);
    }

    public void getPicturesRequest(String url)
    {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    try
                    {
                        Log.d("JSON", response);

                        JSONArray jsonArray = new JSONObject(response).getJSONObject(COLUMN_PHOTOS).getJSONArray(COLUMN_PHOTO);
                        Type listType = new TypeToken<List<PhotoModel>>(){}.getType();
                        pictureList = new GsonBuilder().create().fromJson(jsonArray.toString(), listType);

                        refreshResults();
                    }

                    catch (JSONException error)
                    {
                        error.printStackTrace();

                        if (BuildConfig.DEBUG)
                        {
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            },
            new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    error.printStackTrace();

                    if (BuildConfig.DEBUG)
                    {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

        queue.add(stringRequest);
    }

    public void refreshResults()
    {
        clusterManager.clearItems();

        for(final PhotoModel pic: pictureList)
        {
            clusterManager.addItem(pic);
        }

        clusterManager.cluster();
    }

    @Override
    public void onCameraMove()
    {
        refreshResults();
    }
}
