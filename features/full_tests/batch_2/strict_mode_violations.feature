Feature: Reporting Strict Mode Violations

# TODO alter the violations here as needed. enhance test assertions similarly to the smoke tests.

@skip_below_android_9
Scenario: StrictMode DiscWrite violation
    When I run "StrictModeDiscScenario" and relaunch the app
    And I configure Bugsnag for "StrictModeDiscScenario"
    And I wait to receive an error
    Then the error is valid for the error reporting API version "4.0" for the "Android Bugsnag Notifier" notifier
    And the exception "errorClass" equals "android.os.StrictMode$Foo"
    And the event "metaData.StrictMode.Violation" equals "DiskWrite"

@skip_below_android_9
Scenario: StrictMode Network on Main Thread violation
    When I run "StrictModeNetworkScenario" and relaunch the app
    And I configure Bugsnag for "StrictModeNetworkScenario"
    And I wait to receive an error
    Then the error is valid for the error reporting API version "4.0" for the "Android Bugsnag Notifier" notifier
    And the exception "errorClass" equals "android.os.StrictMode$StrictMode$Foo"
    And the event "metaData.StrictMode.Violation" equals "NetworkOperation"

@skip_below_android_9
Scenario: StrictMode Activity leak violation
    When I run "StrictModeFileUriExposeScenario" and relaunch the app
    And I configure Bugsnag for "StrictModeFileUriExposeScenario"
    And I wait to receive an error
    Then the error is valid for the error reporting API version "4.0" for the "Android Bugsnag Notifier" notifier
    And the exception "errorClass" equals "android.os.StrictMode$StrictMode$Foo"
    And the event "metaData.StrictMode.Violation" equals "NetworkOperation"
