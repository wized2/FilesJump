package com.endroid.filesjump

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast

/**
 * Invisible launcher: opens AOSP / OEM DocumentsUI (Files) and finishes.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val opened = tryOpenFiles()
        if (!opened) {
            Toast.makeText(this, R.string.files_missing, Toast.LENGTH_SHORT).show()
        }
        finishAndRemoveTask()
    }

    private fun tryOpenFiles(): Boolean {
        // 1) Standard “Files” app category (works on most AOSP / Pixel / many OEMs)
        if (startSafe(Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_FILES)
                addFlags(FLAGS)
            })
        ) return true

        // 2) BROWSE roots (DocumentsUI)
        if (startSafe(Intent(ACTION_BROWSE).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(FLAGS)
            })
        ) return true

        // 3) Explicit known components (AOSP + Google DocumentsUI)
        for ((pkg, cls) in KNOWN_ACTIVITIES) {
            if (startSafe(Intent(ACTION_BROWSE).apply {
                    component = ComponentName(pkg, cls)
                    addFlags(FLAGS)
                })
            ) return true
            if (startSafe(Intent().apply {
                    component = ComponentName(pkg, cls)
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    addFlags(FLAGS)
                })
            ) return true
        }

        // 4) Launch by package (launcher activity)
        for (pkg in KNOWN_PACKAGES) {
            val launch = packageManager.getLaunchIntentForPackage(pkg) ?: continue
            launch.addFlags(FLAGS)
            if (startSafe(launch)) return true
        }

        // 5) Open primary storage root via DocumentsContract
        val rootUri = DocumentsContract.buildRootUri(EXTERNAL_STORAGE_AUTHORITY, "primary")
        if (startSafe(Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(rootUri, DocumentsContract.Document.MIME_TYPE_DIR)
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(FLAGS)
            })
        ) return true

        // 6) Query anything that handles BROWSE / APP_FILES
        val candidates = queryHandlers()
        for (intent in candidates) {
            if (startSafe(intent)) return true
        }

        return false
    }

    private fun queryHandlers(): List<Intent> {
        val out = ArrayList<Intent>()
        val pm = packageManager
        val browse = Intent(ACTION_BROWSE).addCategory(Intent.CATEGORY_DEFAULT)
        for (ri in pm.queryIntentActivities(browse, PackageManager.MATCH_DEFAULT_ONLY)) {
            out += Intent(ACTION_BROWSE).apply {
                component = ComponentName(ri.activityInfo.packageName, ri.activityInfo.name)
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(FLAGS)
            }
        }
        val files = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_FILES)
        for (ri in pm.queryIntentActivities(files, PackageManager.MATCH_DEFAULT_ONLY)) {
            out += Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName(ri.activityInfo.packageName, ri.activityInfo.name)
                addCategory(Intent.CATEGORY_APP_FILES)
                addFlags(FLAGS)
            }
        }
        return out
    }

    private fun startSafe(intent: Intent): Boolean {
        return try {
            // Prefer resolve when possible, but still try start — OEMs hide packages from resolve
            startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        private const val ACTION_BROWSE = "android.provider.action.BROWSE"
        private const val EXTERNAL_STORAGE_AUTHORITY = "com.android.externalstorage.documents"
        private const val FLAGS =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED

        private val KNOWN_PACKAGES = listOf(
            "com.android.documentsui",
            "com.google.android.documentsui",
        )

        private val KNOWN_ACTIVITIES = listOf(
            "com.android.documentsui" to "com.android.documentsui.files.FilesActivity",
            "com.android.documentsui" to "com.android.documentsui.LauncherActivity",
            "com.google.android.documentsui" to "com.android.documentsui.files.FilesActivity",
            "com.google.android.documentsui" to "com.android.documentsui.LauncherActivity",
        )
    }
}
