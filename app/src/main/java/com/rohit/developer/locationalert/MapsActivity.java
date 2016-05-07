package com.rohit.developer.locationalert;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    String Location_address;
    float Location_distance;
    LatLng alert_location;
    List<Address> addresses = null;
    Marker marker;
    Circle circle;
    boolean stop=true;
    MediaPlayer mediaPlayer=null;
    Marker tapped;
    Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (isNetworkAvailable()) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false)
                    .setTitle("Error")
                    .setIcon(R.drawable.nointernet_icon)
                    .setMessage("No Internet Connection.")
                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }

        Location_address = getIntent().getStringExtra("Loc");
        Location_distance = (float)getIntent().getFloatExtra("Dis", 0);
        Location_distance=(Location_distance*1000);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocationName(Location_address,3);
            if(addresses.isEmpty())
            {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false)
                        .setTitle("Error")
                        .setMessage("We could not find "+Location_address+"\n"+"Make sure your search is spelled correctly.Try adding a city, state, or zip code.")
                        .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();

            }
            else {
                alert_location = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }








    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(MapsActivity.this);
        RadioGroup rgViews = (RadioGroup) findViewById(R.id.rg_views);
        rgViews.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_normal) {

                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (checkedId == R.id.rb_satellite) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (checkedId == R.id.rb_terrain) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                } else if (checkedId == R.id.rb_hybrid) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
            }
        });

        mMap.getUiSettings().setZoomControlsEnabled(true);

        if(!(addresses.isEmpty())) {

            Geocoder geocoder=new Geocoder(getBaseContext(),Locale.getDefault());
            String result_marker=null;
            List<Address> address_marker=null;
            try{
                address_marker=geocoder.getFromLocation((double)alert_location.latitude,(double) alert_location.longitude,3);

                result_marker="";
                if(address_marker.size()>0) {
                    for (int i = 0; i <address_marker.get(0).getMaxAddressLineIndex();i++){
                        result_marker=result_marker+address_marker.get(0).getAddressLine(i)+"\n";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            marker = mMap.addMarker(new MarkerOptions().position(alert_location).title(Location_address).snippet(result_marker));
            circle = mMap.addCircle(new CircleOptions().radius((double) Location_distance).center(alert_location).fillColor(0x553399ff).zIndex(1).strokeWidth(2).strokeColor(Color.rgb(0, 51, 204)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(alert_location));
            mMap.setOnMyLocationChangeListener(myLocationChangeListener);


        }

    }


    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener=new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {

            if(location!=null){

                float distance_location = distFrom(location.getLatitude(),location.getLongitude(), alert_location.latitude, alert_location.longitude);
                polyline=mMap.addPolyline(new PolylineOptions().add(alert_location, new LatLng((double)location.getLatitude(),(double)location.getLongitude())).zIndex(1).width(5).color(Color.RED));
                if (distance_location <= Location_distance) {


                    mediaPlayer = MediaPlayer.create(MapsActivity.this, R.raw.alert_tone);
                    mediaPlayer.start();

                    if(stop) {

                        stop = false;
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                        builder.setCancelable(false)
                                .setMessage("Destination Reached"+"\n"+"Distance Remaining :"+((float)distance_location/1000)+" km")
                                .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mediaPlayer.reset();
                                        marker.remove();
                                        polyline.remove();
                                        circle.remove();
                                        stop = true;
                                        finish();

                                    }
                                })
                                .show();
                    }
                }
            }
        }
    };



    public  float distFrom(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (int) (earthRadius * c);

        return (dist);
    }



    @Override
    public void onMapClick(LatLng latLng) {

        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        Geocoder geocoder=new Geocoder(getBaseContext(),Locale.getDefault());
        String result=null;
        List<Address> address=null;
        try{
            address=geocoder.getFromLocation((double)latLng.latitude,(double) latLng.longitude,3);

            result="";
            if(address.size()>0) {
                for (int i = 0; i <address.get(0).getMaxAddressLineIndex();i++){
                    result=result+address.get(0).getAddressLine(i)+"\n";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

       Toast.makeText(MapsActivity.this, result, Toast.LENGTH_SHORT).show();

    }


}
