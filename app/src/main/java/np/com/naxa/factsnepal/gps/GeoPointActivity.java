/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package np.com.naxa.factsnepal.gps;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationListener;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import np.com.naxa.factsnepal.R;
import np.com.naxa.factsnepal.gps.client.LocationClient;
import np.com.naxa.factsnepal.gps.client.LocationClients;


public class GeoPointActivity extends AppCompatActivity implements LocationListener,
        LocationClient.LocationClientListener, GpsStatus.Listener {

    private static final String TAG = "GeoPointActivity";

    // Default values for requesting Location updates.
    private static final long LOCATION_UPDATE_INTERVAL = 100;
    private static final long LOCATION_FASTEST_UPDATE_INTERVAL = 50;

    public static final double DEFAULT_LOCATION_ACCURACY = 5.0;
    public static final String LOCATION_RESULT = "LOCATION_RESULT";



    private static final String LOCATION_COUNT = "locationCount";
    private static final String START_TIME = "startTime";
    private static final String NUMBER_OF_AVAILABLE_SATELLITES = "numberOfAvailableSatellites";

    private ProgressDialog locationDialog;

    private LocationClient locationClient;
    private Location location;

    private double locationAccuracy;

    private int locationCount;
    private int numberOfAvailableSatellites;

    private long startTime = System.currentTimeMillis();

    private String dialogMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (!checkIfLocationPermissionsGranted(this)) {
//            finish();
//            return;
//        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (savedInstanceState != null) {
            locationCount = savedInstanceState.getInt(LOCATION_COUNT);
            startTime = savedInstanceState.getLong(START_TIME);
            numberOfAvailableSatellites = savedInstanceState.getInt(NUMBER_OF_AVAILABLE_SATELLITES);
        }

        Intent intent = getIntent();

        locationAccuracy = DEFAULT_LOCATION_ACCURACY;


        setTitle(R.string.get_location);

        locationClient = LocationClients.clientForContext(this);
        if (locationClient.canSetUpdateIntervals()) {
            locationClient.setUpdateIntervals(LOCATION_UPDATE_INTERVAL, LOCATION_FASTEST_UPDATE_INTERVAL);
        }

        locationClient.setListener(this);

        setupLocationDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationClient.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (locationDialog != null) {
            locationDialog.show();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    updateDialogMessage();
                }
            }, 0, 1000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // We're not using managed dialogs, so we have to dismiss the dialog to prevent it from
        // leaking memory.
        if (locationDialog != null && locationDialog.isShowing()) {
            locationDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        locationClient.stop();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LOCATION_COUNT, locationCount);
        outState.putLong(START_TIME, startTime);
    }

    // LocationClientListener:

    @Override
    public void onClientStart() {
        locationClient.requestLocationUpdates(this);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
                return;
            }
            locationManager.addGpsStatusListener(this);
        }

        if (locationClient.isLocationAvailable()) {
            logLastLocation();

        } else {
            finishOnError();
        }
    }

    @Override
    public void onClientStartFailure() {
        finishOnError();
    }

    @Override
    public void onClientStop() {

    }

    /**
     * Sets up the look and actions for the progress dialog while the GPS is searching.
     */
    @SuppressWarnings("deprecation")
    private void setupLocationDialog() {
        // dialog displayed while fetching gps location
        locationDialog = new ProgressDialog(this);

        locationDialog.setCancelable(false); // taping outside the dialog doesn't cancel
        locationDialog.setIndeterminate(true);
        locationDialog.setIcon(android.R.drawable.ic_dialog_info);
        locationDialog.setTitle(getString(R.string.getting_location));
        dialogMessage = getString(R.string.please_wait_long);

        DialogInterface.OnClickListener geoPointButtonListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                returnLocation();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                location = null;
                                finish();
                                break;
                        }
                    }
                };
        locationDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.save_point),
                geoPointButtonListener);
        locationDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel_location),
                geoPointButtonListener);

        locationDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(30,0,0,0);
                positiveButton.setLayoutParams(params);
            }
        });


    }

    private void logLastLocation() {
        Location loc = locationClient.getLastLocation();

        if (loc != null) {
            Log.i(TAG, "lastKnownLocation() lat: "+loc.getLatitude()+" long: "+loc.getLongitude() +" acc: "+ loc.getAccuracy());

        } else {
            Log.i(TAG, "lastKnownLocation() null location");
        }
    }

    private void returnLocation() {
        if (location != null) {
            Intent i = new Intent();

            i.putExtra(LOCATION_RESULT, getResultStringForLocation(location));

            setResult(RESULT_OK, i);
        }

        finish();
    }

    private void finishOnError() {
        Toast.makeText(this, R.string.provider_disabled_error, Toast.LENGTH_SHORT).show();
        Intent onGPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        startActivity(onGPSIntent);
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;

        if (location != null) {
            // Bug report: cached GeoPoint is being returned as the first value.
            // Wait for the 2nd value to be returned, which is hopefully not cached?
            ++locationCount;
            Log.i(TAG, "onLocationChanged("+locationCount+") "+" location: "+ location);

            if (locationCount > 1) {
                dialogMessage = getProviderAccuracyMessage(location);

                if (location.getAccuracy() <= locationAccuracy) {
                    returnLocation();
                }

            } else {
                dialogMessage = getAccuracyMessage(location);
            }

            updateDialogMessage();
        } else {
            Log.i(TAG, "onLocationChanged ("+locationCount+")");
        }
    }

//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }

    @Override
    public void onGpsStatusChanged(int event) {
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Toast.makeText(this, "Location Permission is Missing", Toast.LENGTH_SHORT).show();
                    return;
                }
                GpsStatus status = locationManager.getGpsStatus(null);
                Iterable<GpsSatellite> satellites = status.getSatellites();
                int satellitesNumber = 0;
                for (GpsSatellite satellite : satellites) {
                    if (satellite.usedInFix()) {
                        satellitesNumber++;
                    }
                }

                numberOfAvailableSatellites = satellitesNumber;
                updateDialogMessage();
            }
        }
    }

    public String getAccuracyMessage(@NonNull Location location) {
        return getString(R.string.location_accuracy, location.getAccuracy());
    }

    public String getProviderAccuracyMessage(@NonNull Location location) {
        return getString(R.string.location_provider_accuracy, "gps".equals(location.getProvider()) ? "GPS" : location.getProvider(), truncateDouble(location.getAccuracy()));
    }

    public String getResultStringForLocation(@NonNull Location location) {
        return String.format("%s %s %s %s", location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy());
    }

    private String truncateDouble(float number) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(number);
    }

    public String getDialogMessage() {
        return dialogMessage;
    }

    private void updateDialogMessage() {
        String timeElapsed = DateUtils.formatElapsedTime((System.currentTimeMillis() - startTime) / 1000);
        String locationMetadata = getString(R.string.location_metadata, numberOfAvailableSatellites, timeElapsed);
        runOnUiThread(() -> locationDialog.setMessage(dialogMessage + "\n\n" + locationMetadata));
    }

}