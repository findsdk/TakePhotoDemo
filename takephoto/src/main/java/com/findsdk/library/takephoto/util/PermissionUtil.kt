package com.findsdk.library.takephoto.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

/**
 * Created by bvb on 2016/10/26.
 */
internal object PermissionUtil {
    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int): Boolean {
        val per = checkPermission(activity, permissions)
        var i = 0
        for (b in per) {
            if (b > 0) i++
        }
        if (i == 0) return true
        val s = arrayOfNulls<String>(i)
        i = 0
        var index = 0
        for (permission in permissions) {
            if (per[i] > 0) {
                s[index] = permission
                index++
            }
            i++
        }
        ActivityCompat.requestPermissions(activity, s, requestCode)
        return false
    }


    private fun checkPermission(activity: Activity, permissions: Array<String>): ByteArray {
        val per = ByteArray(permissions.size)
        for ((i, permission) in permissions.withIndex()) {
            var b: Byte = 0
            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                b = if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    2
                } else {
                    1
                }
            }
            per[i] = b
        }
        return per
    }
}