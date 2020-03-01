package com.kreon.android.witner.utils;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.ui.auth.AuthUI;
import com.kreon.android.witner.BuildConfig;
import com.kreon.android.witner.Constants;
import com.kreon.android.witner.R;
import com.kreon.android.witner.database.RealtimeDatabase;
import com.kreon.android.witner.models.Witneet;

public final class Utils {

    private static final double RADIAN_TO_DEGREES_CONST = 180.0 / Math.PI;
    private static final double DEGREES_TO_RADIAN_CONST = Math.PI / 180.0;
    private static final double SPHERICAL_CONST = 6378.1370;

    //
    //
    //region Public Interface

    public static Intent getPermissionIntent() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent getSignInIntent() {
        return AuthUI.getInstance().createSignInIntentBuilder()
                .setTheme(R.style.AppTheme_NoActionBar)
                .setAvailableProviders(Constants.AUTH_PROVIDERS)
                .build();
    }

    public static Intent getTweetIntent(Witneet witneet) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(
                String.format("twitter://status?status_id=%s", witneet.getTweetId())));
    }

    public static GeoQuery getGeoQuery(Context context, Location location, boolean testing) {
        GeoFire geoFire = testing ?
            RealtimeDatabase.getInstance().getTestweetsGeoFire() :
            RealtimeDatabase.getInstance().getWitneetsGeoFire();
        return geoFire.queryAtLocation(
                new GeoLocation(location.getLatitude(), location.getLongitude()),
                Float.parseFloat(QueryPreferences.getNotificationRadius(context)));
    }

    // deltaY is in kilometers.
    public static double addDeltaToLat(double lat, double deltaY) {
        return lat + RADIAN_TO_DEGREES_CONST * deltaY / SPHERICAL_CONST;
    }

    // deltaX is in kilometers.
    public static double addDeltaToLng(double lat, double lng, double deltaY) {
        return lng + RADIAN_TO_DEGREES_CONST * deltaY / SPHERICAL_CONST / Math.cos(lat * DEGREES_TO_RADIAN_CONST);
    }

    //endregion
}
