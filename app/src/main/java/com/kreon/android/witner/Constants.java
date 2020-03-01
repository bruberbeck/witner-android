package com.kreon.android.witner;

import com.firebase.ui.auth.AuthUI;

import java.util.Arrays;
import java.util.List;

public final class Constants {

    public static final List<AuthUI.IdpConfig> AUTH_PROVIDERS = Arrays.asList(
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            new AuthUI.IdpConfig.TwitterBuilder().build());

    //
    //
    //region Request codes

    public static final int REQUEST_CODE_SIGN_IN = 1;

    //endregion

    private Constants() { }
}
