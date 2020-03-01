package com.kreon.android.witner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.kreon.android.witner.database.RealtimeDatabase;
import com.kreon.android.witner.fragments.InfoDialogFragment;
import com.kreon.android.witner.models.Config;
import com.kreon.android.witner.models.LocationData;
import com.kreon.android.witner.models.Witneet;
import com.kreon.android.witner.utils.QueryPreferences;
import com.kreon.android.witner.utils.Utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.kreon.android.witner.utils.WitnerApplication.getContext;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, InfoDialogFragment.OnResultListener, GeoQueryEventListener,
        GoogleMap.OnInfoWindowClickListener {

    //region Fields

    private static String TAG = "MapsActivity";
    private static final String DIALOG_SIGN_IN = "DialogSignIn";
    private static final String DIALOG_SIGN_IN_INFO = "DialogSignInInfo";
    private static final String BUNDLE_CAMERA_ZOOM_KEY = "is_new_instance_key";
    private static final long LOCATION_REQUEST_INTERVAL = 200L;    // Milliseconds.
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    private static final int RC_SIGN_IN = 1;
    private static final float ZOOM_FACTOR_DEFAULT = 18.0F;

    private View mMainView;
    private FloatingActionButton mPositionFab;
    private GoogleMap mMap;
    private float mZoomFactor;
    private boolean mIsTesting;
    private boolean mIsFirstKeyEntered;
    private boolean mShowMarkers;
    private long mKeyEnteredStopWatch;
    private long mGeoQueryEnteredStopWatch;
    private int mTestCount;
    private int mTestIndex;
    private boolean mIsLocationPermissionGranted;
    private boolean mIsGpsOn;
    private Config mConfig;
    private Location mCurrentLocation;
    private Circle mAccuracyCircle;
    private Marker mUserMarker;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest = new LocationRequest()
            .setInterval(LOCATION_REQUEST_INTERVAL)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    private FirebaseUser mCurrentUser;
    private Map<String, Marker> mGeoKeyMarkers = new HashMap<>();
    private GeoQuery mGeoQuery;
    private ValueEventListener mConfigListener = new ValueEventListener() {
        // In case of a change in 'replyTracks' configuration,
        // traverse through all markers.
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            mConfig = dataSnapshot.getValue(Config.class);
            RealtimeDatabase.getInstance().getWitneetsRef().addChildEventListener(mWitneetListener);
            for (Marker marker : mGeoKeyMarkers.values()) {
                Witneet eet = (Witneet) marker.getTag();
                if (eet != null)
                    recycleMarker(marker, eet);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    };
    private ChildEventListener mWitneetListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Witneet eet = dataSnapshot.getValue(Witneet.class);
            if (eet == null || !mGeoKeyMarkers.containsKey(eet.getTweetId()))
                return;

            recycleMarker(mGeoKeyMarkers.get(eet.getTweetId()), eet);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    };
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null)
                return;

            mCurrentLocation = locationResult.getLastLocation();
            // Log user's location for safety.
            if (!mIsTesting)
                trackUsersLocation(mCurrentUser, mCurrentLocation);

            if (mGeoQuery == null) {
                mGeoQuery = Utils.getGeoQuery(MapsActivity.this, mCurrentLocation, mIsTesting);
                if (mIsTesting && !mShowMarkers)
                    mGeoQuery.addGeoQueryEventListener(mTestGeoListener);
                else
                    mGeoQuery.addGeoQueryEventListener(MapsActivity.this);
                if (mIsTesting) {
                    ++mTestIndex;
                    mIsFirstKeyEntered = false;
                    mKeyEnteredStopWatch = System.currentTimeMillis();
                    mGeoQueryEnteredStopWatch = System.currentTimeMillis();
                }
            }
            else {
                mGeoQuery.setCenter(new GeoLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
            }

            updateGoogleMap();
        }
    };
    private GeoQueryEventListener mTestGeoListener = new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            if (mIsTesting && !mIsFirstKeyEntered && mTestIndex < mTestCount) {
                logKeyEntered();
            }
        }

        @Override
        public void onKeyExited(String key) { }

        @Override
        public void onKeyMoved(String key, GeoLocation location) { }

        @Override
        public void onGeoQueryReady() {
            if (mIsTesting && mTestIndex < mTestCount) {
                logQueryReady();
                restartTest();
            }
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {
            geoQueryError(error);
        }
    };

    //endregion

    //region Lifecycle Callbacks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mZoomFactor = QueryPreferences.getZoomFactor(getContext());

        // Populate member variables.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mMainView = findViewById(android.R.id.content);
        mPositionFab = findViewById(R.id.fab_position);
        mPositionFab.setOnClickListener(v -> {
            if (mCurrentLocation == null) {
                Toast.makeText(getContext(), R.string.current_location_null_error, Toast.LENGTH_LONG).show();
                return;
            }

            animateMoveTo(mCurrentLocation);
        });
        mPositionFab.setOnLongClickListener(v -> {
            if (mCurrentLocation == null) {
                Toast.makeText(getContext(), R.string.current_location_null_error, Toast.LENGTH_LONG).show();
                return false;
            }

            animateMoveToAndZoomInOn(mCurrentLocation);
            return true;
        });

        FloatingActionButton tweetFab = findViewById(R.id.fab_tweet);
        tweetFab.setOnClickListener(v -> {
            String tweetUrl = "https://twitter.com/intent/tweet";
            Uri uri = Uri.parse(tweetUrl);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        });

        // Action bar.
        setSupportActionBar(findViewById(R.id.maps_toolbar));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_maps_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsLocationPermissionGranted = checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mIsGpsOn = lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        getLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mZoomFactor = QueryPreferences.getZoomFactor(getContext());
        mIsTesting = QueryPreferences.isTesting(getContext());
        mShowMarkers = QueryPreferences.isShowMarkers(getContext());
        mTestCount = Integer.parseInt(QueryPreferences.getTestCount(getContext()));
        mTestIndex = -1;
        checkForFeatures();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        QueryPreferences.setZoomFactor(getContext(), mMap.getCameraPosition().zoom);
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGeoQuery != null) {
            mGeoQuery.removeAllListeners();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Witneet witneet = (Witneet) marker.getTag();
                if (witneet == null)
                    return null;

                View v = getLayoutInflater().inflate(R.layout.contents_info, null);

                TextView idView = v.findViewById(R.id.tweet_id);
                TextView hashtagsView = v.findViewById(R.id.tweet_hashtags);
                TextView userNameView = v.findViewById(R.id.tweet_user_name);
                TextView dateView = v.findViewById(R.id.tweet_date);
                TextView textView = v.findViewById(R.id.tweet_text);
                TextView geoparsedView = v.findViewById(R.id.geoparsed);

                idView.setText(witneet.getTweetId());
                hashtagsView.setText(TextUtils.join(", ", witneet.getHashtags()));
                userNameView.setText(witneet.getUser().getName());
                dateView.setText(witneet.getDateString());
                textView.setText(witneet.getText());
                geoparsedView.setText(witneet.getGeoparsed() ? "True" : "False");

                return v;
            }
        });
        mMap.setOnInfoWindowClickListener(this);
        updateGoogleMap();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(SettingsActivity.newIntent(this));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mIsLocationPermissionGranted = false;
        // If request is cancelled, the result arrays are empty.
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mIsLocationPermissionGranted = true;
            }
        }

        updateGoogleMap();

        if (QueryPreferences.isFirstRun(getContext())) {
            // Since this is the first run, ask for user to log in.
            InfoDialogFragment infoDialog = InfoDialogFragment.newInstance(R.string.sign_in, R.string.sign_in_message,
                    R.string.sign_in, R.string.not_now);
            infoDialog.show(getSupportFragmentManager(), DIALOG_SIGN_IN);
            QueryPreferences.setFirstRun(getContext(), false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == RC_SIGN_IN) {
            mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        }
    }

    //endregion

    //region Interface Implementations

    @Override
    public void onResult(DialogInterface dialog, int titleId, int which) {
        switch (titleId) {
            case R.string.sign_in:
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        startActivityForResult(Utils.getSignInIntent(), RC_SIGN_IN);
                        break;

                    case DialogInterface.BUTTON_NEUTRAL:
                        InfoDialogFragment.newInstance(R.string.sign_in_declined_title, R.string.sign_in_declined_message, R.string.ok)
                                .show(getSupportFragmentManager(), DIALOG_SIGN_IN_INFO);
                        break;
                }
                break;

            case R.string.sign_in_declined_message:
                break;
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker.getTag() != null)
            startActivity(Utils.getTweetIntent((Witneet) marker.getTag()));
    }

    //region GeoQuery Event Listeners

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        if (mIsTesting && !mIsFirstKeyEntered && mTestIndex < mTestCount) {
            logKeyEntered();
        }

        // First fetch its details location from "witneets" ref.
        //..
        // Then add a marker for it.
        DatabaseReference ref = mIsTesting ?
                RealtimeDatabase.getInstance().getTestweetsRef(key) :
                RealtimeDatabase.getInstance().getWitneetRef(key);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Witneet witneet = dataSnapshot.getValue(Witneet.class);
                if (witneet != null) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .icon(mConfig.getDescriptor(witneet))
                            .position(new LatLng(location.latitude, location.longitude))
                            .title(witneet.getTweetId()));
                    marker.setTag(witneet);
                    mGeoKeyMarkers.put(key, marker);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, String.format(Locale.US, "Database error: %s (%s)",
                        databaseError.getMessage(),
                        databaseError.getDetails()));
            }
        });
    }

    @Override
    public void onKeyExited(String key) {
        if (mGeoKeyMarkers.containsKey(key)) {
            mGeoKeyMarkers.get(key).remove();
            mGeoKeyMarkers.remove(key);
        }
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        // This can never be the case.
    }

    @Override
    public void onGeoQueryReady() {
        if (mIsTesting && mTestIndex < mTestCount) {
            logQueryReady();
            restartTest();
        }
    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        geoQueryError(error);
    }

    //endregion

    //endregion

    //region Private Interface

    private void logKeyEntered() {
        long stopWatch = System.currentTimeMillis();
        Log.e(TAG, String.format("onKeyEntered: i and elapsed times, %d, %d",
                mTestIndex,
                stopWatch - mKeyEnteredStopWatch));
        mIsFirstKeyEntered = true;
    }

    private void logQueryReady() {
        long stopWatch = System.currentTimeMillis();
        Log.e(TAG, String.format("onGeoQueryReady: i and elapsed times, %d, %d",
                mTestIndex,
                stopWatch - mGeoQueryEnteredStopWatch));
    }

    private void checkForFeatures() {
        mPositionFab.setEnabled(false);
        if (!mIsLocationPermissionGranted) {
            showSnackbar(R.string.location_permission_denied_explanation, Snackbar.LENGTH_INDEFINITE, R.string.settings,
                v -> startActivity(Utils.getPermissionIntent()));
        }
        else if (mCurrentUser == null) {
            showSnackbar(R.string.no_user_signed_in_explanation, Snackbar.LENGTH_INDEFINITE, R.string.sign_in,
                    v -> startActivityForResult(Utils.getSignInIntent(), RC_SIGN_IN));
        }
        else if (!mIsGpsOn) {
            showSnackbar(R.string.gps_is_off_explanation, Snackbar.LENGTH_INDEFINITE, R.string.settings,
                    v -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
        }
        else {
            mPositionFab.setEnabled(true);
        }
    }

    private void trackUsersLocation(FirebaseUser user, Location location) {
        LocationData data = new LocationData(location);
        DatabaseReference ref = RealtimeDatabase.getInstance().getUserTrackRef(user).push();
        ref.setValue(data);
        RealtimeDatabase.getInstance().getUserTrackGeoFire(user).setLocation(ref.getKey(),
                data.getGeolocation(), (key, error) -> { });
    }

    private void startLocationUpdates() {
        if (checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
            && mCurrentUser != null
            && mIsGpsOn) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            RealtimeDatabase.getInstance().getConfigRef().addValueEventListener(mConfigListener);
        }
        else {
            mPositionFab.setEnabled(false);
        }
    }

    private void stopLocationUpdates() {
        RealtimeDatabase.getInstance().getConfigRef().removeEventListener(mConfigListener);
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        if (mGeoQuery != null) {
            mGeoQuery.removeAllListeners();
            for (Marker marker : mGeoKeyMarkers.values()) {
                marker.remove();
            }
            mGeoKeyMarkers.clear();
            mGeoQuery = null;
        }
    }

    private void updateGoogleMap() {
        if (mMap == null || mCurrentLocation == null) {
            return;
        }

        if (mIsLocationPermissionGranted) {
            if (mAccuracyCircle == null) {
                //region First run since last closing

                // Build accuracy circle
                CircleOptions accOps = new CircleOptions()
                        .strokeWidth(4f)
                        .strokeColor(ContextCompat.getColor(getContext(), R.color.accuracyCircleStroke))
                        .fillColor(ContextCompat.getColor(getContext(), R.color.accuracyCircleFill))
                        .center(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                        .radius(mCurrentLocation.getAccuracy())
                        .zIndex(Float.MAX_VALUE);
                mAccuracyCircle = mMap.addCircle(accOps);

                // Build user showing icon
                // But first, getInstance the icon from a shape drawable.
                Drawable d = getResources().getDrawable(R.drawable.user_circle, null);
                Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                d.draw(canvas);

                MarkerOptions userOps = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .anchor(0.5f, 0.5f)
                        .position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                mUserMarker = mMap.addMarker(userOps);

                if (mZoomFactor == Float.MIN_VALUE) {
                    moveToAndZoomInOn(mCurrentLocation);
                }
                else {
                    moveToAndZoomInOn(mCurrentLocation, mZoomFactor);
                }

                //endregion
            }
            else {
                mAccuracyCircle.setCenter(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                mAccuracyCircle.setRadius(mCurrentLocation.getAccuracy());
                mUserMarker.setPosition(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
            }
        }
        else {
            if (mAccuracyCircle != null)
                mAccuracyCircle.remove();
            if (mUserMarker != null)
                mUserMarker.remove();
            mCurrentLocation = null;
        }
    }

    private void restartTest() {
        // Check to see if we have reached our testing limit.
        // If we have, just restart the query process as if
        // we are restarting the activity.
        stopLocationUpdates();
        startLocationUpdates();
    }

    private void geoQueryError(DatabaseError error) {
        showSnackbar(
                String.format(Locale.US, "%s (%s). Error code: %d", error.getMessage(), error.getDetails(), error.getCode()),
                Snackbar.LENGTH_INDEFINITE,
                R.string.ok,
                null
        );
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                            new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private Snackbar showSnackbar(int textId, int duration, int actionTextId, View.OnClickListener listener) {
       return showSnackbar(getString(textId), duration, actionTextId, listener);
    }

    private Snackbar showSnackbar(String text, int duration, int actionTextId, View.OnClickListener listener) {
        Snackbar bar = Snackbar.make(mMainView, text, duration).setAction(getString(actionTextId), listener);
        bar.show();
        return bar;
    }

    private void moveToAndZoomInOn(@NonNull Location location) {
        moveToAndZoomInOn(new LatLng(location.getLatitude(), location.getLongitude()), mZoomFactor);
    }

    private void moveToAndZoomInOn(@NonNull Location location, float zoom) {
        moveToAndZoomInOn(new LatLng(location.getLatitude(), location.getLongitude()), zoom);
    }

    private void moveToAndZoomInOn(@NonNull LatLng position, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
    }

    private void animateMoveTo(@NonNull Location location) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(
                new LatLng(location.getLatitude(), location.getLongitude())));
    }

    private void animateMoveToAndZoomInOn(@NonNull Location location) {
        float zoomLevel = mMap.getCameraPosition().zoom > ZOOM_FACTOR_DEFAULT ?
                mMap.getCameraPosition().zoom : ZOOM_FACTOR_DEFAULT;
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder()
                        .zoom(zoomLevel)
                        .target(new LatLng(location.getLatitude(), location.getLongitude())).build()));
    }

    private void recycleMarker(Marker marker, Witneet eet) {
        marker.setIcon(mConfig.getDescriptor(eet));
    }

    //endregion
}
