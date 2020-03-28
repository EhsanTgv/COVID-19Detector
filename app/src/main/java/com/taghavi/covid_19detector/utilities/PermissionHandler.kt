package com.taghavi.covid_19detector.utilities

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHandler {
    companion object {
        fun checkPermission(context: Context, permission: String): Boolean {
            val result: Int = ContextCompat.checkSelfPermission(context, permission)
            return result == PackageManager.PERMISSION_GRANTED
        }

        fun requestForPermission(activity: Activity, permissionId:Int ,vararg permissions: String) {
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                permissionId
            )
        }
    }
}