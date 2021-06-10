package com.android.arijit.firebase.walker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public static String TAG = "HomeFragment";

    public HomeFragment() {
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        if(savedInstanceState!=null) {
            initLatLng = (LatLng) savedInstanceState.getParcelable("initLatLng");
        }
    }

    /**
     * Data members
     */

    private MapView mapView;
    private GoogleMap mMap;
    private Animation disReveal, disHide;
    private FloatingActionButton fabAction, fabCurLocation;
    private TextView tvDistance;
    private CardView tvContainer;
    private FusedLocationProviderClient providerClient;
    private String[] wantedPerm = {Manifest.permission.ACCESS_FINE_LOCATION};
    private CameraPosition.Builder cameraBuilder;
    private Marker curMarker;
    private PolylineOptions polylineOptions;
    private boolean trackState = false;
    private ArrayList<LatLng> travelCoordinates = new ArrayList<>();
    private float totDistTravelled = 0f;
    private LatLng initLatLng;
    private String TRACK_STATE = "trackState";

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mapView = root.findViewById(R.id.mapView);
        /**
         * check location enabled
         */
        isLocationEnabled();

        disReveal = AnimationUtils.loadAnimation(getContext(), R.anim.distance_reveal);
        disHide = AnimationUtils.loadAnimation(getContext(), R.anim.distance_hide);
        fabAction = root.findViewById(R.id.fab_action);
        fabCurLocation = root.findViewById(R.id.fab_cur_location);
        tvDistance = root.findViewById(R.id.tv_distance);
        tvContainer = root.findViewById(R.id.tv_container);
        tvContainer.setVisibility(View.INVISIBLE);

        /**
         * init map
         */
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        /**
         * listener
         */

        fabAction.setOnClickListener(v -> {
            if (!trackState) {
                totDistTravelled = 0.00f;
                trackState = true;
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        tvContainer.startAnimation(disReveal);
                        disReveal.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                tvContainer.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                    }
                });

                startTrack();
            } else {
                trackState = false;
                tvContainer.startAnimation(disHide);
                disHide.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        tvContainer.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                stopTrack();
            }
        });

        fabCurLocation.setOnClickListener(v -> {
            setCurrentLocation();
        });

        return root;
    }

    /**
     * map Ready
     * @param googleMap
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.i(TAG, "onMapReady: ");

        mMap = googleMap;
        GoogleMapOptions gm = new GoogleMapOptions();

        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.setMaxZoomPreference(16.5f);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        cameraBuilder = new CameraPosition.Builder().zoom(15);
        polylineOptions = new PolylineOptions();
        providerClient = LocationServices.getFusedLocationProviderClient(getContext());

        if(!trackState && initLatLng != null){
            cameraBuilder.target(initLatLng);
            Log.i(TAG, "onMapReady: state false "+initLatLng.latitude);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));
            curMarker = mMap.addMarker(new MarkerOptions()
                    .position(initLatLng));
            return;
        }
        else if(trackState){
            return;
        }

        setCurrentLocation();

    }

    private void startTrack(){
        /**
         * providerClient to provide location service
         */
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), wantedPerm, 101);
        }
        providerClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
    private void stopTrack(){
        providerClient.removeLocationUpdates(locationCallback);
    }


    //=============
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();
            Log.i(TAG, "onLocationResult: start of list");
            for (Location le:locationList){
                if(le == null)
                    continue;
                double lat = le.getLatitude(), lng = le.getLongitude();
                Log.i(TAG, "onLocationResult: "+lat+"+"+lng);
                LatLng pos = new LatLng(lat, lng);

                if(travelCoordinates.isEmpty()){
                    travelCoordinates.add(pos);
                }
                else{
                    LatLng lastCoor = travelCoordinates.get(travelCoordinates.size()-1);
                    Location lastLoc = new Location("");
                    lastLoc.setLatitude(lastCoor.latitude); lastLoc.setLongitude(lastCoor.longitude);

                    float dist = le.distanceTo(lastLoc);
                    if(dist < 3f) {
                        continue;
                    }
                    totDistTravelled += dist;
                    travelCoordinates.add(pos);
                }
                tvDistance.setText("Distance travelled : "+distanceFormat(totDistTravelled));

                if(curMarker == null)
                    curMarker = mMap.addMarker(new MarkerOptions().position(pos));
                else{
                    MarkerAnimation.animateMarkerToGB(mMap, curMarker, pos, new LatLngInterpolator.Spherical());
                }

                /**
                 * keeping current camera features
                 */

                cameraBuilder.target(pos);
                cameraBuilder.zoom(mMap.getCameraPosition().zoom);
                cameraBuilder.tilt(mMap.getCameraPosition().tilt);
                cameraBuilder.bearing(mMap.getCameraPosition().bearing);

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));

            }
        }
    };

    private void setCurrentLocation(){
        if(!trackState) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), wantedPerm, 101);
            }
            providerClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                @Override
                public boolean isCancellationRequested() {
                    return false;
                }

                @NonNull
                @Override
                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                    return null;
                }
            })
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location == null)
                                return;
                            initLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            if (curMarker == null) {
                                curMarker = mMap.addMarker(new MarkerOptions()
                                        .position(initLatLng));
                            } else {
                                curMarker.setPosition(initLatLng);
                            }
                            cameraBuilder.target(initLatLng);
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
        }
        else if(travelCoordinates!=null && travelCoordinates.size()>0){
            cameraBuilder.target(travelCoordinates.get(travelCoordinates.size()-1));
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));
        }
    }

    private String distanceFormat(float d){
        if(d>1000f){
            d = d/1000f;
            return String.format("%.2f km", d);
        }
        else{
            return String.format("%.2f m", d);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mapView != null)
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);
        Log.i(TAG, "onSaveInstanceState: ");
        outstate.putBoolean(TRACK_STATE, trackState);
        if(!trackState){
            outstate.putParcelable("initLatLng" , initLatLng);
        }
    }

    private void isLocationEnabled(){
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false, netEnabled = false;
        try{
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e){}
        try{
            netEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e){}
        if(!gpsEnabled && !netEnabled){
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.gps_not_enabled)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

}