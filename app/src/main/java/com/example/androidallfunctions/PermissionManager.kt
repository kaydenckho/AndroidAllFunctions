package com.example.androidallfunctions

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject


class PermissionManager @Inject constructor(@ActivityContext val context: Context) {

    val startActivityLauncher =
        (context as ComponentActivity).registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    val requestMultiplePermissionsLauncher =
        (context as ComponentActivity).registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: Map<String, Boolean> ->
            permissions.entries.forEach {}
        }

    val PERMISSIONS = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_SMS,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NOTIFICATION_POLICY,
        Settings.ACTION_MANAGE_WRITE_SETTINGS
    )

    /** This API will clear all app data and kill the process **/
    fun clearPermissions() {
        (context.getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
    }

    fun checkNormalPermissions() {
        requestMultiplePermissionsLauncher.launch(PERMISSIONS)
    }

    fun checkSystemWriteSettings(): Boolean {
        if (!Settings.System.canWrite(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:" + context.packageName)
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivityLauncher.launch(intent)
            return false
        }
        return true
    }

    @Synchronized fun checkNotificationAccess(): Boolean {
        val nm =
            context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivityLauncher.launch(intent)
            return false
        }
        return true
    }
}