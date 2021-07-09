package com.example.bugsnag.android

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process.myPid
import com.bugsnag.android.BreadcrumbType
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import com.bugsnag.android.OnErrorCallback

class ExampleApplication : Application() {

    companion object {
        init {
//            if you support API <= 17 you should uncomment this to load the bugsnag library
//            before any libraries that link to it
//            https://docs.bugsnag.com/platforms/android/#initialize-the-bugsnag-client
//
//            System.loadLibrary("bugsnag-ndk")
//            System.loadLibrary("bugsnag-plugin-android-anr")

            System.loadLibrary("entrypoint")
        }
    }

    private external fun performNativeBugsnagSetup()

    override fun onCreate() {
        super.onCreate()

        val config = Configuration.load(this)
        config.setUser("123456", "joebloggs@example.com", "Joe Bloggs")
        config.addMetadata("user", "age", 31)

        // Configure the persistence directory when running MultiProcessActivity in a separate
        // process to ensure the two Bugsnag clients are independent
//        val processName = findCurrentProcessName()
//        if (processName.endsWith("secondaryprocess")) {
//            config.persistenceDirectory = File(filesDir, processName)
//        }

        config.addOnError(OnErrorCallback { event ->
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            val processInfo = ActivityManager.RunningAppProcessInfo()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ActivityManager.getMyMemoryState(processInfo)

                val memoryInfo = ActivityManager.MemoryInfo()
                am.getMemoryInfo(memoryInfo)

                val pids = intArrayOf(myPid())
                val dbgMemoryInfo = am.getProcessMemoryInfo(pids).single()

                event.addMetadata(
                    "memoryInfo", mapOf(
                        "memoryClass" to am.memoryClass,
                        "largeMemoryClass" to am.largeMemoryClass,
                        "getMyMemoryState" to mapOf(
                            "importance" to processInfo.importance,
                            "importanceReasonCode" to processInfo.importanceReasonCode,
                            "lastTrimLevel" to processInfo.lastTrimLevel,
                            "lru" to processInfo.lru
                        ),
                        "getMemoryInfo" to mapOf(
                            "availMem" to memoryInfo.availMem,
                            "lowMemory" to memoryInfo.lowMemory,
                            "threshold" to memoryInfo.threshold,
                            "totalMem" to memoryInfo.totalMem
                        ),
                        "runtime" to mapOf(
                            "maxMemory" to Runtime.getRuntime().maxMemory(),
                            "totalMemory" to Runtime.getRuntime().totalMemory(),
                            "freeMemory" to Runtime.getRuntime().freeMemory()
                        ),
                        "dbgMemoryInfoPages" to mapOf(
                            "dalvikPrivateDirty" to dbgMemoryInfo.dalvikPrivateDirty,
                            "dalvikSharedDirty" to dbgMemoryInfo.dalvikSharedDirty,
                            "dalvikPss" to dbgMemoryInfo.dalvikPss,
                            "nativePrivateDirty" to dbgMemoryInfo.nativePrivateDirty,
                            "nativeSharedDirty" to dbgMemoryInfo.nativeSharedDirty,
                            "nativePss" to dbgMemoryInfo.nativePss,
                            "otherPrivateDirty" to dbgMemoryInfo.otherPrivateDirty,
                            "otherSharedDirty" to dbgMemoryInfo.otherSharedDirty,
                            "otherPss" to dbgMemoryInfo.otherPss
                        ),
                        "dbgMemoryInfo" to mapOf(
                            "memoryStats" to dbgMemoryInfo.memoryStats,
                            "totalPrivateClean" to dbgMemoryInfo.totalPrivateClean,
                            "totalPrivateDirty" to dbgMemoryInfo.totalPrivateDirty,
                            "totalPss" to dbgMemoryInfo.totalPss,
                            "totalSharedClean" to dbgMemoryInfo.totalSharedClean,
                            "totalSharedDirty" to dbgMemoryInfo.totalSharedDirty,
                            "totalSwappablePss" to dbgMemoryInfo.totalSwappablePss
                        )
                    )
                )

                val exitReason = am.getHistoricalProcessExitReasons(
                    packageName,
                    0,
                    1
                ).singleOrNull()
                exitReason?.let {
                    event.addMetadata(
                        "historicalExitReason", mapOf(
                            "importance" to it.importance,
                            "reason" to it.reason,
                            "pss" to it.pss,
                            "rss" to it.rss
                        )
                    )
                }
            }
            true
        })


        Bugsnag.start(this, config)

        // Initialise native callbacks
        performNativeBugsnagSetup()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Bugsnag.leaveBreadcrumb(
            "onTrimMemory", mutableMapOf<String, Any>(
                "level" to level
            ), BreadcrumbType.STATE
        )
    }

}
