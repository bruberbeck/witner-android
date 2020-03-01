package com.kreon.android.witner.utils;

import android.content.Context;
import android.preference.PreferenceManager;

public final class QueryPreferences {

    //region Fields

    private static final String PREF_ZOOM_FACTOR = "zoomFactor";
    private static final String PREF_IS_FIRST_RUN = "isFirstRun";
    private static final String PREF_LAST_NOTIFICATION_SERVER_TIME_STAMP = "lastNotificationServerTimeStamp";
    private static final String PREF_NOTIFICATION_RADIUS = "pref_notification_radius_key";
    private static final String PREF_TESTING_RADIUS = "pref_testing_radius_key";
    private static final String PREF_TESTING_SAMPLE_SIZE = "pref_testing_sample_size_key";
    private static final String PREF_TESTING_TEST_COUNT = "pref_testing_test_count_key";
    private static final String PREF_TESTING_IS_TESTING = "pref_testing_is_testing_key";
    private static final String PREF_TESTING_IS_SHOW_MARKERS = "pref_testing_is_show_markers_key";

    private static final float ZOOM_FACTOR_DEFAULT = 18.0F;
    private static final boolean IS_FIRST_RUN_DEFAULT = true;
    private static final int LAST_NOTIFICATION_SERVER_TIME_STAMP = 0;
    private static final String NOTIFICATION_RADIUS_DEFAULT = "5"; // kilometers
    private static final String TESTING_RADIUS_DEFAULT = "20"; // kilometers
    private static final String TESTING_SAMPLE_SIZE_DEFAULT = "100"; // samples
    private static final String TESTING_TEST_COUNT_DEFAULT = "105"; // times
    private static final boolean TESTING_IS_TESTING_DEFAULT = false;
    private static final boolean TESTING_IS_SHOW_MARKERS_DEFAULT = false;

    //endregion

    //
    //
    //region Constructors

    private QueryPreferences() { }

    //endregion

    //
    //
    //region Settings

    public static float getZoomFactor(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getFloat(PREF_ZOOM_FACTOR, ZOOM_FACTOR_DEFAULT);
    }

    public static void setZoomFactor(Context c, float zoomFactor) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putFloat(PREF_ZOOM_FACTOR, zoomFactor).apply();
    }

    public static boolean isFirstRun(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(PREF_IS_FIRST_RUN, IS_FIRST_RUN_DEFAULT);
    }

    public static void setFirstRun(Context c, boolean isFirstRun) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean(PREF_IS_FIRST_RUN, isFirstRun).apply();
    }

    // Here, the update time stored is SERVER SIDE update time, not local update time.
    public static long getLastNotificationServerTimeStamp(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getLong(PREF_LAST_NOTIFICATION_SERVER_TIME_STAMP, LAST_NOTIFICATION_SERVER_TIME_STAMP);
    }

    public static void setLastNotificationServerTimeStamp(Context c, long serverTime) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putLong(PREF_LAST_NOTIFICATION_SERVER_TIME_STAMP, serverTime).apply();
    }

    public static String getNotificationRadius(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(PREF_NOTIFICATION_RADIUS, NOTIFICATION_RADIUS_DEFAULT);
    }

    public static void setNotificationRadius(Context c, String radius) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putString(PREF_NOTIFICATION_RADIUS, radius).apply();
    }

    public static String getTestingRadius(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(PREF_TESTING_RADIUS, TESTING_RADIUS_DEFAULT);
    }

    public static void setTestingRadius(Context c, String radius) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putString(PREF_TESTING_RADIUS, radius).apply();
    }

    public static String getTestingSampleSize(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(PREF_TESTING_SAMPLE_SIZE, TESTING_SAMPLE_SIZE_DEFAULT);
    }

    public static void setTestingSampleSize(Context c, String sampleSize) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putString(PREF_TESTING_SAMPLE_SIZE, sampleSize).apply();
    }

    public static String getTestCount(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(PREF_TESTING_TEST_COUNT, TESTING_TEST_COUNT_DEFAULT);
    }

    public static void setTestCount(Context c, String testCount) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putString(PREF_TESTING_TEST_COUNT, testCount).apply();
    }

    public static boolean isTesting(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(PREF_TESTING_IS_TESTING, TESTING_IS_TESTING_DEFAULT);
    }

    public static void setTesting(Context c, boolean isTesting) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean(PREF_TESTING_IS_TESTING, isTesting).apply();
    }

    public static boolean isShowMarkers(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(PREF_TESTING_IS_SHOW_MARKERS, TESTING_IS_SHOW_MARKERS_DEFAULT);
    }

    public static void setShowMarkers(Context c, boolean isShowMarkers) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean(PREF_TESTING_IS_SHOW_MARKERS, isShowMarkers).apply();
    }
}
