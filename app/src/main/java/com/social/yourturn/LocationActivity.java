package com.social.yourturn;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.social.yourturn.asynctasks.FetchPlaceAddressTask;
import com.social.yourturn.asynctasks.FetchPlaceTask;
import com.social.yourturn.models.Contact;
import com.social.yourturn.models.Place;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class LocationActivity extends AppCompatActivity implements FetchPlaceTask.PlaceListener, FetchPlaceAddressTask.AddressListener {

    private static final String TAG = LocationActivity.class.getSimpleName();
    private static final String BASE_LOCATION_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String BASE_PlACE_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 0;
    private static final int REQUEST_CHECK_SETTINGS = 34;
    public static final String CURRENT_PLACE = "current_place";
    public static final String PLACE_URL = "place_url";
    private String url = "";
    private ArrayList<Contact> mContactList;
    private  View dialogView = null;
    private String placeUrl = null;

    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        if(getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            mContactList = bundle.getParcelableArrayList(MainActivity.ALL_CONTACTS);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            displayLocationSettingsRequest(this);
        }
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    Log.i(TAG, "All location settings are satisfied.");
                    getLastLocation();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(LocationActivity.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        Log.i(TAG, "PendingIntent unable to execute request.");
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                    break;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK) getLastLocation();
        else showSnackbar(getString(R.string.no_location_detected));
    }


    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        mLastLocation = task.getResult();
                        StringBuilder urlBuilder = new StringBuilder(BASE_LOCATION_URL);
                        urlBuilder.append("location=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
                        urlBuilder.append("&radius=0.2");
                        urlBuilder.append("&key=" + getString(R.string.google_places_api_key));
                        url = urlBuilder.toString();
                        FetchPlaceTask placeTask = new FetchPlaceTask(LocationActivity.this);
                        placeTask.execute(url);
                        Log.d(TAG, url);
                    } else {
                        Log.w(TAG, "getLastLocation:exception", task.getException());
                        showSnackbar(getString(R.string.no_location_detected));
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS_REQUEST_CODE:
                displayLocationSettingsRequest(this);
                break;
            default:
                return;
        }
    }

    private void showSnackbar(final String text) {
        View container = findViewById(R.id.container);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean checkPermissions() {
        int coarsePermissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        int finePermissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        return coarsePermissionState == PackageManager.PERMISSION_GRANTED && finePermissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(LocationActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.location_permission_rationale, android.R.string.ok,
                    view -> {
                        // Request permission
                        startLocationPermissionRequest();
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            startLocationPermissionRequest();
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    @Override
    public void onComplete(Place place) {
        if (place != null) {
            Log.d(TAG, place.getName());
            Log.d(TAG, place.getId());
            Log.d(TAG, place.getUrl());
            showLocationBox(place);
            FetchPlaceAddressTask fetchPlaceAddressTask = new FetchPlaceAddressTask(this);
            StringBuilder placeBuilder = new StringBuilder(BASE_PlACE_URL);
            placeBuilder.append("placeid=" + place.getId());
            placeBuilder.append("&key=" + getString(R.string.google_places_api_key));
            fetchPlaceAddressTask.execute(placeBuilder.toString());
        }
    }

    private void showLocationBox(final Place place){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Current Location Information");
        LayoutInflater inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.location_layout, null);
        dialogBuilder.setView(dialogView);

        final TextView placeText = (TextView) dialogView.findViewById(R.id.placeNameText);
        placeText.setText(place.getName());

        final ImageView iconView = (ImageView) dialogView.findViewById(R.id.placeIcon);
        Log.d(TAG, place.getIcon());
        Glide.with(this).load(place.getIcon()).into(iconView);

        final CircleImageView placeImageView = (CircleImageView) dialogView.findViewById(R.id.placeUrl);
        placeUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=2048&photoreference=" + place.getUrl() + "&key=" + getString(R.string.google_places_api_key);
        Glide.with(this).load(placeUrl).into(placeImageView);

        dialogBuilder.setPositiveButton(R.string.proceed_text, (dialog, which) -> {
            Intent contactIntent = new Intent(LocationActivity.this, ContactActivity.class);
            contactIntent.putParcelableArrayListExtra(MainActivity.ALL_CONTACTS, mContactList);
            contactIntent.putExtra(LocationActivity.CURRENT_PLACE, place);
            contactIntent.putExtra(LocationActivity.PLACE_URL,  placeUrl);
            startActivity(contactIntent);
        });

        dialogBuilder.setNegativeButton(R.string.cancel_text, (dialog, which) -> finish());

        dialogBuilder.setOnDismissListener(dialog -> finish());

        dialogBuilder.create().show();
    }

    @Override
    public void onAddressComplete(String address) {
        final TextView placeText = (TextView) dialogView.findViewById(R.id.addressText);
        placeText.setText(address);
    }
}