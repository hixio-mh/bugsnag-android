package com.bugsnag.android.mazerunner.scenarios

import android.content.Context
import android.os.Build
import android.os.StrictMode
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.BugsnagThreadViolationListener
import com.bugsnag.android.Configuration
import java.net.HttpURLConnection
import java.net.URL

/**
 * Generates a strictmode exception caused by performing a network request on the main thread
 */
internal class StrictModeNetworkScenario(
    config: Configuration,
    context: Context,
    eventMetadata: String
) : Scenario(config, context, eventMetadata) {

    override fun startScenario() {
        super.startScenario()
        setupBugsnagStrictModeDetection()
        val urlConnection = URL("http://example.com").openConnection() as HttpURLConnection
        urlConnection.doOutput = true
        urlConnection.responseMessage
    }

    private fun setupBugsnagStrictModeDetection() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectNetwork()
                    .penaltyDeath()
                    .build()
            )
        }
        // Android 9 not supported as of yet
        val policy = StrictMode.ThreadPolicy.Builder().detectNetwork()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            policy.penaltyDeath()
        } else {
            val listener = BugsnagThreadViolationListener(Bugsnag.getClient())
            policy.penaltyListener(context.mainExecutor, listener)
        }
        StrictMode.setThreadPolicy(policy.build())
    }
}
