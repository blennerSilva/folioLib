package com.folioreader.model.event;


import android.support.v4.app.DialogFragment;

/**
 * Created by blennersilva on 11/12/17.
 */

public class OpenTOC {
   DialogFragment dialogFragment;

    public OpenTOC(DialogFragment dialogFragment) {
        this.dialogFragment = dialogFragment;
    }

    public DialogFragment getDialogFragment() {
        return dialogFragment;
    }
}
