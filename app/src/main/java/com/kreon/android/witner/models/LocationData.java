package com.kreon.android.witner.models;

import android.location.Location;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Arrays;
import java.util.List;

@IgnoreExtraProperties
public class LocationData {

    //region Fields

    private String mProvider;
    private long mTime = 0;
    private long mElapsedRealtimeNanos = 0;
    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private double mAltitude = 0.0f;
    private float mSpeed = 0.0f;
    private float mBearing = 0.0f;
    private float mHorizontalAccuracyMeters = 0.0f;

    //endregion

    //region Properties

    public String getProvider() {
        return mProvider;
    }

    public void setProvider(String provider) {
        mProvider = provider;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public long getElapsedRealtimeNanos() {
        return mElapsedRealtimeNanos;
    }

    public void setElapsedRealtimeNanos(long elapsedRealtimeNanos) {
        mElapsedRealtimeNanos = elapsedRealtimeNanos;
    }

    public List<Double> getCoordinates() {
        return Arrays.asList(mLatitude, mLongitude);
    }

    public void setCoordinates(List<Double> coordinates) {
        mLatitude = coordinates.get(0);
        mLongitude = coordinates.get(1);
    }

    public double getAltitude() {
        return mAltitude;
    }

    public void setAltitude(double altitude) {
        mAltitude = altitude;
    }

    public float getSpeed() {
        return mSpeed;
    }

    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    public float getBearing() {
        return mBearing;
    }

    public void setBearing(float bearing) {
        mBearing = bearing;
    }

    public float getHorizontalAccuracyMeters() {
        return mHorizontalAccuracyMeters;
    }

    public void setHorizontalAccuracyMeters(float horizontalAccuracyMeters) {
        mHorizontalAccuracyMeters = horizontalAccuracyMeters;
    }

    @Exclude
    public double getLatitude() {
        return mLatitude;
    }

    @Exclude
    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    @Exclude
    public double getLongitude() {
        return mLongitude;
    }

    @Exclude
    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    @Exclude
    public LatLng getPosition() {
        return new LatLng(mLatitude, mLongitude);
    }

    @Exclude
    public GeoLocation getGeolocation() {
        return new GeoLocation(mLatitude, mLongitude);
    }

    //endregion

    //region Constructors

    protected LocationData() { }

    public LocationData(Location location)
    {
        mProvider = location.getProvider();
        mTime = location.getTime();
        mElapsedRealtimeNanos = location.getElapsedRealtimeNanos();
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        mAltitude = location.getAltitude();
        mSpeed = location.getSpeed();
        mBearing = location.getBearing();
        mHorizontalAccuracyMeters = location.getAccuracy();
    }

    //endregion
}
