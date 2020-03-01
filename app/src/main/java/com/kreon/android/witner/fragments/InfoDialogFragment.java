package com.kreon.android.witner.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class InfoDialogFragment extends DialogFragment {

    //region Fields

    private static final String TAG = "RationaleFragment";

    public static final String ARG_TITLE_ID = "title_id";
    private static final String ARG_MESSAGE_ID = "message_id";
    private static final String ARG_POSITIVE_BUTTON_TEXT_ID = "positive_button_text_id";
    private static final String ARG_NEUTRAL_BUTTON_TEXT_ID = "neutral_button_text_id";
    private static final String ARG_TAG = "tag";

    public static final String EXTRA_WHICH = "com.kreon.android.witner.which";

    private int mTitleId;

    //endregion

    public interface OnResultListener {
        /**
         * This method will be invoked when a button in the dialog is clicked.
         *
         * @param dialog the dialog that received the click
         * @param titleId the titleId of this InfoDialog's title.
         *                It is used to distinguish varying
         *                instances of InfoDialogFragment.
         * @param which the button that was clicked (ex.
         *              {@link DialogInterface#BUTTON_POSITIVE}) or the position
         *              of the item clicked
         */
        void onResult(DialogInterface dialog, int titleId, int which);
    }

    public static InfoDialogFragment newInstance(@StringRes int titleId, @StringRes int messageId,
                                                 @StringRes int neutralButtonTextId) {
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE_ID, titleId);
        args.putInt(ARG_MESSAGE_ID, messageId);
        args.putInt(ARG_NEUTRAL_BUTTON_TEXT_ID, neutralButtonTextId);

        InfoDialogFragment frag = new InfoDialogFragment();
        frag.setArguments(args);

        return frag;
    }

    public static InfoDialogFragment newInstance(@StringRes int titleId, @StringRes int messageId,
                                                 @StringRes int positiveButtonTextId, @StringRes int neutralButtonTextId) {
        InfoDialogFragment frag = newInstance(titleId, messageId, neutralButtonTextId);
        frag.getArguments().putInt(ARG_POSITIVE_BUTTON_TEXT_ID, positiveButtonTextId);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mTitleId = getArguments().getInt(ARG_TITLE_ID);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(mTitleId)
                .setMessage(getArguments().getInt(ARG_MESSAGE_ID))
                .setNeutralButton(getArguments().getInt(ARG_NEUTRAL_BUTTON_TEXT_ID), this::onButtonClick);

        if (getArguments().getInt(ARG_POSITIVE_BUTTON_TEXT_ID) != 0) {
            builder.setPositiveButton(getArguments().getInt(ARG_POSITIVE_BUTTON_TEXT_ID), this::onButtonClick);
        }

        return  builder.create();
    }

    private void onButtonClick(DialogInterface dialog, int which) {
        if (getTargetFragment() != null) {
            Intent i = new Intent();
            i.putExtra(EXTRA_WHICH, which);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
        }
        else if (getActivity() instanceof OnResultListener) {
            ((OnResultListener) getActivity()).onResult(dialog, mTitleId, which);
        }
    }
}
