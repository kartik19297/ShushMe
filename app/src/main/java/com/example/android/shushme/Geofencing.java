package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.GeofencingRequest;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kashish on 14-01-2018.
 */

public class Geofencing implements ResultCallback{

    private static final long GEOFENCE_TIMEOUT = 86400000;
    private final static int GEOFENCE_RADIUS = 50;

    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    public Geofencing(Context context, GoogleApiClient client){
        mContext = context;
        mGoogleApiClient = client;
        mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();
    }

    public void registerAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected() ||
                mGeofenceList == null || mGeofenceList.size() == 0) {
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException) {
            Log.e("SECURITY-BREACH", securityException.getMessage());
        }
    }

    public void unRegisterAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException) {
            Log.e("ERROR", securityException.getMessage());
        }
    }

    public void updateGeofencesList(PlaceBuffer places){
        mGeofenceList = new ArrayList<>();
        if (places == null || places.getCount() == 0){
            for (Place place : places){
                String placeUID = place.getId();
                double placeLat = place.getLatLng().latitude;
                double placeLon = place.getLatLng().longitude;
                Geofence geofence = new Geofence.Builder()
                        .setRequestId(placeUID)
                        .setExpirationDuration(GEOFENCE_TIMEOUT)
                        .setCircularRegion(placeLat,placeLon,GEOFENCE_RADIUS)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build();
                mGeofenceList.add(geofence);
            }
        }
    }

    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent(){
        if (mGeofencePendingIntent!=null){
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext,GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.e("GEOFENCING_ERROR",String.format("Error adding/remving geofence : %s",
                result.getStatus()));
    }
}
