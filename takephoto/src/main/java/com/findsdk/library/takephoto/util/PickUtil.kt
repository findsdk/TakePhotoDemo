package com.findsdk.library.takephoto.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log

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
        try {
            intent?.resolveActivity(activity.packageManager)?.let {
                activity.startActivityForResult(intent, requestCode)
            } ?: kotlin.run {
                activity.startActivityForResult(IntentUtil.getPickIntentWithDocuments(), requestCode)
            }
        } catch (e: Exception) {
            activity.startActivityForResult(IntentUtil.getPickIntentWithDocuments(), requestCode)
        }
//        intent?.let {
//            activity.startActivityForResult(it, requestCode)
//        }
    }

    fun pickPicture1(activity: Activity, requestCode: Int) {
        val intent = when (requestCode) {
            Constants.PICK_FROM_FILE, Constants.PICK_FROM_FILE_WITH_CROP ->
                IntentUtil.getPickIntentWithDocuments()
            Constants.PICK_FROM_GALLERY, Constants.PICK_FROM_GALLERY_WITH_CROP ->
                IntentUtil.getPickIntentWithGallery()
            else -> null
        }
        try {
            intent?.resolveActivity(activity.packageManager)?.let {
                activity.startActivityForResult(intent, requestCode)
            } ?: kotlin.run {
                activity.startActivityForResult(IntentUtil.getPickIntentWithDocuments(), requestCode)
            }
        } catch (e: Exception) {
        }

    }
}