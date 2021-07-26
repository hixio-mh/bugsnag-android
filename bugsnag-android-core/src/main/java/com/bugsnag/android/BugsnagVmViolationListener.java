package com.bugsnag.android;

import android.os.Build;

import android.os.StrictMode.OnVmViolationListener;
import android.os.strictmode.Violation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.List;

// TODO add docs
@RequiresApi(api = Build.VERSION_CODES.P)
public class BugsnagVmViolationListener implements OnVmViolationListener {

    private final Client client;
    private final OnVmViolationListener listener;

    public BugsnagVmViolationListener(@NonNull Client client) {
        this(client, null);
    }

    public BugsnagVmViolationListener(@NonNull Client client,
                                      @Nullable OnVmViolationListener listener) {
        this.client = client;
        this.listener = listener;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onVmViolation(@NonNull Violation violation) {
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
                    err.setErrorMessage("StrictMode policy violation detected: VmPolicy");
                }
                return true;
            }
        });
        if (listener != null) {
            listener.onVmViolation(violation);
        }
    }
}
