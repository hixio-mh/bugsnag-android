package com.bugsnag.android;

import android.os.Build;

import android.os.StrictMode.OnThreadViolationListener;
import android.os.strictmode.Violation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.List;

// TODO add docs
@RequiresApi(api = Build.VERSION_CODES.P)
public class BugsnagThreadViolationListener implements OnThreadViolationListener {

    private final Client client;
    private final OnThreadViolationListener listener;

    // TODO document client parameter enforces Bugsnag.start()
    //  must happen before setting up strictmode
    public BugsnagThreadViolationListener(@NonNull Client client) {
        this(client, null);
    }

    public BugsnagThreadViolationListener(@NonNull Client client,
                                          @Nullable OnThreadViolationListener listener) {
        this.client = client;
        this.listener = listener;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onThreadViolation(@NonNull Violation violation) {
        if (client == null || violation == null) { // bomb out early if supplied client was null
            return;
        }
        client.notify(violation, new OnErrorCallback() {
            @Override
            public boolean onError(@NonNull Event event) {
                event.updateSeverityInternal(Severity.INFO);
                List<Error> errors = event.getErrors();

                if (!errors.isEmpty()) {
                    Error err = errors.get(0);
                    err.setErrorMessage("StrictMode policy violation detected: ThreadPolicy");
                }
                return true;
            }
        });
        if (listener != null) {
            listener.onThreadViolation(violation);
        }
    }
}
