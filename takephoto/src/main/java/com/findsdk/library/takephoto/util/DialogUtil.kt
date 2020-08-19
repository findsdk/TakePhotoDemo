package com.findsdk.library.takephoto.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.findsdk.library.takephoto.R
import com.findsdk.library.takephoto.TakePhotoConfig

/**
 * Created by bvb on 2020/7/29.
 */
object DialogUtil {

    /**
     * 显示请求权限对话框
     * @param activity Activity
     * @param message String
     */
    fun showPermissionDialog(activity: Activity, message: String) {
        val builder = AlertDialog.Builder(activity, R.style.PhotoModuleAlertDialog)
        builder.setMessage(message)
        builder.setPositiveButton(TakePhotoConfig.languageSetting) { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + activity.packageName)
            activity.startActivity(intent)
            activity.finish()
        }
        builder.setNegativeButton(android.R.string.cancel) { _, _ -> activity.finish() }
        builder.setCancelable(false)
        builder.create().show()
    }
}