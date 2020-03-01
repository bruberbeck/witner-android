package com.kreon.android.witner.models;

import com.google.firebase.database.Exclude;

import java.util.Arrays;
import java.util.List;

public class Poi {

    //region Fields

    private String mFullName;
    private double mLatitude;
    private double mLongitude;

    //endregion

    //region Properties

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String fullName) {
        this.mFullName = fullName;
    }

    public List<Double> getCoordinates() {
        return Arrays.asList(mLatitude, mLongitude);
    }

    public void setCoordinates(List<Double> coordinates) {
        mLatitude = coordinates.get(0);
        mLongitude = coordinates.get(1);
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

    //endregion

    //region Constructor

    public Poi() { }

    //endregion
}
