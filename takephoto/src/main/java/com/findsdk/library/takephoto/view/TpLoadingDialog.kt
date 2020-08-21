package com.findsdk.library.takephoto.view

import android.app.Activity
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import com.findsdk.library.takephoto.R

/**
 * Created by bvb on 2019/1/8.
 */
internal object TpLoadingDialog {

    fun makeLoadingDialog(activity: Activity): Dialog {
        val dialog = Dialog(activity, R.style.Theme_AppCompat_Dialog_Alert)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setDimAmount(0f)
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        dialog.setContentView(R.layout.tp_loading_view)
        return dialog
    }
}