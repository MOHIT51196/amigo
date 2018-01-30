package amigo.atom.team.amigo.views.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import amigo.atom.team.amigo.R;
import amigo.atom.team.amigo.adapters.CustomInfoWindowAdapter;


public class FindFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, android.location.LocationListener {

    public static final int PERMISSION_REQ_CODE_LOCATION = 100;

    private View view;
    private SupportMapFragment mapView;
    private GoogleMap gMap;
//    private FusedLocationProviderClient fusedLocClient;
    private GoogleApiClient apiClient;
    private LocationManager locationManager;
    private Marker currentLocMarker;


    ArrayList<LatLng> getLatLangsDummy(){
        ArrayList<LatLng> latLangs = new ArrayList<>();
        latLangs.add(new LatLng(28.1217087, 77.1217087));
        latLangs.add(new LatLng(28.6996789, 77.1217087));
        latLangs.add(new LatLng(28.7020868, 77.1170674));
        latLangs.add(new LatLng(28.6994810, 77.1170580));
        latLangs.add(new LatLng(28.7040795, 77.1273969));

        return latLangs;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_find, container, false);

        this.locationManager = (LocationManager) view.getContext().getSystemService(Context.LOCATION_SERVICE);

        mapView = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView);

        checkLocationPermission();

        initMap();



        return this.view;
    }

    private void initMap() {

        try {
            MapsInitializer.initialize(getActivity());

        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setRotateGesturesEnabled(true);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            initLocationOnMap();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQ_CODE_LOCATION);
        }


        initOtherClients();

    }

    public synchronized void buildApiClient(){
        Toast.makeText(view.getContext(),"buildGoogleApiClient", Toast.LENGTH_SHORT).show();

        apiClient = new GoogleApiClient.Builder(view.getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void initOtherClients() {
        ArrayList<LatLng> latLangs = getLatLangsDummy();
        for(int i=0;i<latLangs.size();i++)
        {
            gMap.addMarker(LocationMarkerFactory.getMapMarker(getActivity(), latLangs.get(i), i));
        }

        gMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getActivity(), latLangs));
    }

    private void initLocationOnMap() throws SecurityException {

        gMap.setMyLocationEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(true);
        buildApiClient();
        apiClient.connect();

        Location location = LocationServices.FusedLocationApi.getLastLocation(apiClient);
        if(location != null) {
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);

//        fusedLocClient = LocationServices.getFusedLocationProviderClient(getActivity());
//
//        // init location request for the map location
//        final LocationRequest locationRequest = new LocationRequest();
//        locationRequest.setInterval(1000 * 60); // in millisec
//        locationRequest.setFastestInterval(1000 * 60);
//        locationRequest.setSmallestDisplacement(400);   // in metres
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        fusedLocClient.requestLocationUpdates(locationRequest, new LocationCallback(){
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                android.location.Location location = locationResult.getLastLocation();
//                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//
//                gMap.addMarker(LocationMarkerFactory.getMapMarker(getActivity(),latLng, "Me"));
//
//                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 26));
//            }
//
//        }, Looper.myLooper());
    }

    private void renderLocation(Location location) {

        if(currentLocMarker != null){
            currentLocMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // creating and setting up the marker
        MarkerOptions markerOptions = LocationMarkerFactory.getMapMarker(getActivity(), latLng, "Me");
        currentLocMarker = gMap.addMarker(markerOptions);

    }

    private void showGPSAlert() {

        //if(gpsAlertDialog == null) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(view.getContext());

        alertBuilder.setTitle("GPS Provider Alert");
        Log.d("LocationFragment", "inside showGPSAlert");
        alertBuilder.setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(view.getContext(), "GPS permission is denied", Toast.LENGTH_SHORT).show();

            }
        });

        //gpsAlertDialog = alertBuilder.create();
        //}
        alertBuilder.show();
        //gpsAlertDialog.show();
    }

    private void checkLocationPermission() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                        PERMISSION_REQ_CODE_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQ_CODE_LOCATION);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQ_CODE_LOCATION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED
                    || grantResults[1] == PackageManager.PERMISSION_GRANTED){
                showGPSAlert();
                initMap();
            }
        }
    }


//    --------------- Google APi interface IMPLEMENTATIONS --------------

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        LatLng latLng = null;


        try {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);

            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                showGPSAlert();
            }

            if (lastLocation != null) {
                latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

                // creating and setting up the marker
                MarkerOptions markerOptions = LocationMarkerFactory.getMapMarker(getActivity(), latLng, "Me");

                currentLocMarker = gMap.addMarker(markerOptions);

                renderLocation(lastLocation);
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            }

            LocationRequest locRequest = new LocationRequest();
            locRequest.setInterval(1000 * 60); // in millisec
            locRequest.setFastestInterval(1000 * 60);
            locRequest.setSmallestDisplacement(400);   // in metres
            locRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locRequest, this);



        } catch (SecurityException e){
            e.printStackTrace();
        }

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {}


//    --------------- GOOGLE LOCATION LISTENER IMPLEMENTATIONS --------------
    @Override
    public void onLocationChanged(Location location) {
        renderLocation(location);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        Toast.makeText(view.getContext(), "Loaction Changed", Toast.LENGTH_SHORT).show();
    }


//    --------------- ANDROID LOCATION LISTENER IMPLEMENTATIONS --------------

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {
        initMap();
    }

    @Override
    public void onProviderDisabled(String provider) {
        initMap();
    }

}

abstract class LocationMarkerFactory{

    public static MarkerOptions getMapMarker(Context context, LatLng latLng, int position){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.snippet(String.valueOf(position + 1));//Here we are saving the position of marker (converted into string).
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(LocationMarkerFactory.getMarkerBitmapFromView(context, "Person" + position)));

        return markerOptions;
    }

    public static MarkerOptions getMapMarker(Context context, LatLng latLng, String title){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
//        markerOptions.snippet("0");
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(LocationMarkerFactory.getMarkerBitmapFromView(context, title)));

        return markerOptions;
    }

    public static Bitmap getMarkerBitmapFromView(Context context, String name) {

        //HERE YOU CAN ADD YOUR CUSTOM VIEW
        View customMarkerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.map_marker, null);

        //IN THIS EXAMPLE WE ARE TAKING TEXTVIEW BUT YOU CAN ALSO TAKE ANY KIND OF VIEW LIKE IMAGEVIEW, BUTTON ETC.
//        TextView textView = (TextView) customMarkerView.findViewById(R.id.txt_name);
//        textView.setText(name);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }
}
