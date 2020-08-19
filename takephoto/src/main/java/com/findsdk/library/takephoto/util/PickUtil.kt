package com.findsdk.library.takephoto.util

import android.app.Activity

/**
 * Created by bvb on 2020/7/29.
 */
internal object PickUtil {

    /**
     * 选择照片
     * @param activity Activity
     * @param requestCode Int
     */
    fun pickPicture(activity: Activity, requestCode: Int) {
        val intent = when (requestCode) {
            Constants.PICK_FROM_FILE, Constants.PICK_FROM_FILE_WITH_CROP ->
                IntentUtil.getPickIntentWithDocuments()
            Constants.PICK_FROM_GALLERY, Constants.PICK_FROM_GALLERY_WITH_CROP ->
                IntentUtil.getPickIntentWithGallery()
            else -> null
        }
        intent?.let {
            activity.startActivityForResult(it, requestCode)
        }
    }
}