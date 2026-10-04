package com.endroid.filesjump

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

/**
 * Invisible launcher: opens AOSP DocumentsUI (Files) and finishes immediately.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val intent = Intent(ACTION_BROWSE).apply {
                component = ComponentName(DOCUMENTSUI_PKG, FILES_ACTIVITY)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                )
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback: package-level launch
                val launch = packageManager.getLaunchIntentForPackage(DOCUMENTSUI_PKG)
                if (launch != null) {
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(launch)
                } else {
                    Toast.makeText(this, R.string.files_missing, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (_: Exception) {
            Toast.makeText(this, R.string.files_missing, Toast.LENGTH_SHORT).show()
        } finally {
            finishAndRemoveTask()
        }
    }

    companion object {
        private const val ACTION_BROWSE = "android.provider.action.BROWSE"
        private const val DOCUMENTSUI_PKG = "com.android.documentsui"
        private const val FILES_ACTIVITY = "com.android.documentsui.files.FilesActivity"
    }
}
