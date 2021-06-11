package com.android.arijit.firebase.walker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

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
import com.google.android.gms.maps.model.MapStyleOptions;
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
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        cameraBuilder = new CameraPosition.Builder()
                .zoom(SettingsFragment.CAMERA_ZOOM)
                .tilt(SettingsFragment.CAMERA_TILT)
                .bearing(SettingsFragment.CAMERA_BEARING);

        ForegroundService.distInString.observe(this, this.mDistObserver);
        ForegroundService.curGotPosition.observe(this, this.mTravelCoordinateObserver);
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
    public static String[] wantedPerm = {Manifest.permission.ACCESS_FINE_LOCATION};
    private CameraPosition.Builder cameraBuilder;
    private Marker curMarker;
    private PolylineOptions polylineOptions;
    private static boolean trackState = false;
    private ArrayList<LatLng> travelCoordinates;
    private LatLng initLatLng;

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
        travelCoordinates = new ArrayList<>();

        if(!trackState) {
            tvContainer.setVisibility(View.INVISIBLE);
        }
        else{
            tvDistance.setText(
                    ForegroundService.distInString.getValue()
            );
        }

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
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), wantedPerm, 101);
                }
                startService();
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

                stopService();
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
        try{
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));
            if(!success){
                Log.i(TAG, "onMapReady: parse failed");
            }
        } catch (Resources.NotFoundException e){
            Log.i(TAG, "onMapReady: style not found");
        }

        mMap.setMaxZoomPreference(18);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        polylineOptions = new PolylineOptions();
        providerClient = LocationServices.getFusedLocationProviderClient(getContext());

        if(!trackState && initLatLng != null){
            cameraBuilder.target(initLatLng);
            cameraBuilder.zoom(SettingsFragment.CAMERA_ZOOM);
            cameraBuilder.bearing(SettingsFragment.CAMERA_BEARING);
            cameraBuilder.tilt(SettingsFragment.CAMERA_TILT);

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));
            curMarker = mMap.addMarker(new MarkerOptions()
                    .position(initLatLng));
            return;
        }
        else if(trackState){
            Log.i(TAG, "onMapReady: adding initial "
                    +ForegroundService.curGotPosition.getValue().size());
            travelCoordinates = ForegroundService.curGotPosition.getValue();
            if(travelCoordinates!=null && travelCoordinates.size()>0 ){
                curMarker = mMap.addMarker(new MarkerOptions()
                        .position(travelCoordinates.get(travelCoordinates.size() - 1))
                );
                for (LatLng l:travelCoordinates){
                    polylineOptions.add(l);
                    mMap.addPolyline(polylineOptions);
                }
                initLatLng = travelCoordinates.get(travelCoordinates.size() - 1);
                cameraBuilder.target(travelCoordinates.get(travelCoordinates.size() - 1));
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));
            }

            return;
        }

        setCurrentLocation();

    }

    Observer<String> mDistObserver = s -> {
      if(!trackState)
          return;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                tvDistance.setText(s);
            }
        });

    };

    Observer<ArrayList<LatLng>> mTravelCoordinateObserver = list -> {
        if(!trackState | list.isEmpty()) return;
        LatLng nLatLng = list.get(list.size() - 1);
        if(!travelCoordinates.isEmpty()
                && nLatLng.equals(travelCoordinates.get(travelCoordinates.size() - 1))){
            return;
        }
        travelCoordinates.add(nLatLng);
        initLatLng = travelCoordinates.get(travelCoordinates.size() - 1);
        if(mMap!=null && curMarker != null){
            MarkerAnimation.animateMarkerToGB(mMap, curMarker, nLatLng, new LatLngInterpolator.Spherical());
        }
    };

    private void setCurrentLocation(){
        if(!trackState) {

            new Thread(() -> {
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
                                    Log.i(TAG, "onSuccess: null true");
                                    curMarker = mMap.addMarker(new MarkerOptions()
                                            .position(initLatLng));
                                } else {
                                    Log.i(TAG, "onSuccess: null false " + (mMap==null));
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
            }).start();
        }
        else if(travelCoordinates!=null && travelCoordinates.size()>0){
            cameraBuilder.target(travelCoordinates.get(travelCoordinates.size()-1));
            cameraBuilder.zoom(SettingsFragment.CAMERA_ZOOM);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));
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
        if(mMap !=null) {
            SettingsFragment.CAMERA_ZOOM = mMap.getCameraPosition().zoom;
            SettingsFragment.CAMERA_TILT = mMap.getCameraPosition().tilt;
            SettingsFragment.CAMERA_BEARING = mMap.getCameraPosition().bearing;
        }
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
        ForegroundService.distInString.removeObserver(this.mDistObserver);
        ForegroundService.curGotPosition.removeObserver(this.mTravelCoordinateObserver);
        if(mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
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

    public void startService(){
        Intent serviceIntent = new Intent(getContext(), ForegroundService.class);
        ContextCompat.startForegroundService(getContext(), serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(getContext(), ForegroundService.class);
        getActivity().stopService(serviceIntent);
    }

}