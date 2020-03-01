package com.kreon.android.witner.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

public class RationaleFragment extends DialogFragment {

    private static final String TAG = "RationaleFragment";

    @StringRes private int mTitleId;
    @StringRes private int mMessageId;
    @StringRes private int mButtonTextId;

    public static RationaleFragment newInstance(Fragment targetFragment, int requestCode,
             @StringRes int titleId, @StringRes int messageId, @StringRes int buttonTextId) {
        RationaleFragment rationaleFragment = new RationaleFragment();
        rationaleFragment.mTitleId = titleId;
        rationaleFragment.mMessageId = messageId;
        rationaleFragment.mButtonTextId = buttonTextId;
        rationaleFragment.setTargetFragment(targetFragment, requestCode);

        return rationaleFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(mTitleId)
                .setMessage(mMessageId)
                .setNeutralButton(mButtonTextId, (dialog, which) -> dialog.cancel())
                .create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        if (getTargetFragment() instanceof DialogInterface.OnCancelListener) {
            ((DialogInterface.OnCancelListener) getTargetFragment()).onCancel(dialog);
        }
    }
}
