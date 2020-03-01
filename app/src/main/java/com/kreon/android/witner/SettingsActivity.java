package com.kreon.android.witner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kreon.android.witner.database.RealtimeDatabase;
import com.kreon.android.witner.models.Witneet;
import com.kreon.android.witner.utils.QueryPreferences;
import com.kreon.android.witner.utils.Utils;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, newValue) -> {
        String stringValue = newValue.toString();

        if (preference instanceof EditTextPreference) {
            try
            {
                switch (preference.getKey()) {
                    case "pref_notification_radius_key":
                    case "pref_testing_radius_key":
                        Float.parseFloat(stringValue);
                        preference.setSummary(stringValue + " kilometers");
                        break;

                    case "pref_testing_sample_size_key":
                    case "pref_testing_test_count_key":
                        Integer.parseInt(stringValue);
                        preference.setSummary(stringValue);
                        break;
                }

            }
            catch (Exception ex)
            {
                return false;
            }
        }
        else {
            preference.setSummary(stringValue);
        }

        return true;
    };

    public static Intent newIntent(Context packageContent) {
        return new Intent(packageContent, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotificationsPreferenceFragment())
                .commit();
    }

    //region Types

    public static class NotificationsPreferenceFragment extends PreferenceFragment {

        private static final String TAG = "NotificationsPrefFrag";

        Preference mTestPref;
        Preference mClearTestPref;
        Preference mSignInOutPref;
        Preference mDeleteAccountPref;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_all);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            //region Testing

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_testing_radius_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_testing_sample_size_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_testing_test_count_key)));
            mTestPref = findPreference(getString(R.string.pref_testing_test_key));
            mTestPref.setEnabled(user != null);
            mTestPref.setOnPreferenceClickListener(preference -> {
                if (ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), R.string.location_permission_needed_for_testing_explanation, Toast.LENGTH_LONG)
                        .show();
                }
                else {
                    LocationServices.getFusedLocationProviderClient(getActivity())
                            .getLastLocation()
                            .addOnSuccessListener(getActivity(), location -> {
                                if (location == null) {
                                    Toast.makeText(getActivity(), R.string.current_location_null_error, Toast.LENGTH_LONG)
                                            .show();
                                    return;
                                }

                                dropTestLocations(location);
                            });
                }
                return true;
            });

            mClearTestPref = findPreference(getString(R.string.pref_testing_clear_test_key));
            mClearTestPref.setEnabled(user != null);
            mClearTestPref.setOnPreferenceClickListener(preference -> {
                clearTestLocations();
                return true;
            });

            //endregion

            //region Notifications

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_notification_radius_key)));

            //endregion

            //region Account

            mSignInOutPref = findPreference(getString(R.string.pref_account_sign_in_out_key));
            mDeleteAccountPref = findPreference(getString(R.string.pref_account_delete_key));

            if (user == null) {
                mSignInOutPref.setTitle(R.string.pref_account_sign_in_title);
            }
            else {
                mSignInOutPref.setTitle(R.string.pref_account_sign_out_title);
            }

            mSignInOutPref.setOnPreferenceClickListener(preference -> {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    startActivityForResult(Utils.getSignInIntent(), RC_SIGN_IN);
                }
                else {
                    FirebaseAuth.getInstance().signOut();
                    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                        // Sign out successful.
                        mTestPref.setEnabled(false);
                        mClearTestPref.setEnabled(false);
                        mSignInOutPref.setTitle(R.string.pref_account_sign_in_title);
                        mDeleteAccountPref.setEnabled(false);
                    }
                }

                return true;
            });

            mDeleteAccountPref.setEnabled(user != null);
            mDeleteAccountPref.setOnPreferenceClickListener(preference -> {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User account deleted.");
                            mTestPref.setEnabled(false);
                            mClearTestPref.setEnabled(false);
                            mSignInOutPref.setTitle(R.string.pref_account_sign_in_title);
                            mDeleteAccountPref.setEnabled(false);
                        }
                    });
                }

                return true;
            });

            //endregion
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }

            if (requestCode == RC_SIGN_IN) {
                mTestPref.setEnabled(true);
                mClearTestPref.setEnabled(true);
                mSignInOutPref.setTitle(R.string.pref_account_sign_out_title);
                mDeleteAccountPref.setEnabled(true);
            }
        }

        private void dropTestLocations(Location location) {
            double testingRadius = Float.parseFloat(QueryPreferences.getTestingRadius(getActivity()));
            // Testing is area is taken as double the radius area so that more than half of the
            // generated points will fall into the outer testing area,
            // simulating a more realistic scenario.
            double edgeLength = testingRadius * 2;
            int sampleSize = Integer.parseInt(QueryPreferences.getTestingSampleSize(getActivity()));
            // The testing area is imagined to be a square.
            // In order to deduce how many points falls to each edge of the square,
            // we are going to take the sqrt of the sampleSize.
            long pointsPerEdge = Math.round(Math.sqrt(sampleSize));
            // The 'delta' is the distance between each point.
            double delta = edgeLength / (pointsPerEdge + 1);
            double startingOffset = -testingRadius;
            double startingLat = Utils.addDeltaToLat(location.getLatitude(), startingOffset);
            double startingLng = Utils.addDeltaToLng(startingLat, location.getLongitude(), startingOffset);

            int sampleCounter = 0;
            double lat = startingLat;
            while (sampleCounter < sampleSize) {
                double lng = startingLng;
                for (int i = 0; i < pointsPerEdge && sampleCounter < sampleSize; ++i, ++sampleCounter) {
                    addTestLocation(lat, lng);
                    lng = Utils.addDeltaToLng(lat, lng, delta);
                }
                lat = Utils.addDeltaToLat(lat, delta);
            }

            Toast.makeText(getActivity(), String.format(Locale.US, "<%d> test points successfully added to the database",
                    sampleCounter), Toast.LENGTH_LONG)
                    .show();
        }

        private void clearTestLocations() {
            RealtimeDatabase.getInstance().getTestweetsRef().setValue(null, (error, ref) -> {
                if (error == null) {
                    RealtimeDatabase.getInstance().getTestweetsGeoFireRef().setValue(null, (error1, ref1) -> {
                        if (error1 == null)
                            Toast.makeText(getActivity(), "Test database successfully wiped", Toast.LENGTH_SHORT)
                                    .show();
                        else
                            Toast.makeText(getActivity(), String.format("Test database wipe failed: %s (%s)",
                                    error1.getMessage(), error1.getDetails()), Toast.LENGTH_LONG)
                                    .show();
                    });
                }
                else {
                    Toast.makeText(getActivity(), String.format("Test database wipe failed: %s (%s)",
                            error.getMessage(), error.getDetails()), Toast.LENGTH_LONG)
                            .show();
                }
            });
        }

        private void addTestLocation(double lat, double lng) {
            Witneet testweet = Witneet.getTestWitneet(lat, lng);
            RealtimeDatabase.getInstance()
                    .getTestweetsRef(testweet.getTweetId())
                    .setValue(testweet);
            RealtimeDatabase.getInstance()
                    .getTestweetsGeoFire()
                    .setLocation(testweet.getTweetId(), testweet.getGeolocation(), (key, error) -> { });
        }
    }

    //endregion

    //region Private interface

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        Context context = preference.getContext();
        String value = "";
        switch (preference.getTitleRes()) {
            case R.string.pref_notification_radius_title:
                value = QueryPreferences.getNotificationRadius(context);
                break;
            case R.string.pref_testing_radius_title:
                value = QueryPreferences.getTestingRadius(context);
                break;
            case R.string.pref_testing_sample_size_title:
                value = QueryPreferences.getTestingSampleSize(context);
                break;
            case R.string.pref_testing_test_count_title:
                value = QueryPreferences.getTestCount(context);
                break;
        }

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, value);
    }

    //endregion
}
