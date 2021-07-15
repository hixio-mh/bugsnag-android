package com.bugsnag.android.ndk

import org.junit.Test

class NativeStructMigrationTest {

    companion object {
        init {
            System.loadLibrary("bugsnag-ndk")
            System.loadLibrary("bugsnag-ndk-test")
        }
    }

    external fun run(): Int

    @Test
    fun testPassesNativeSuite() {
        verifyNativeRun(run())
    }
}
