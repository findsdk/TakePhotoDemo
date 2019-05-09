package com.findsdk.library.takephoto

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.findsdk.library.takephoto.util.Constants
import com.findsdk.library.takephoto.util.PhotoHelper

/**
 * Created by bvb on 2019/5/9.
 */
class TakePhotoHelper private constructor() {

    companion object {
        val instance: TakePhotoHelper by lazy {
            TakePhotoHelper()
        }
    }

    internal fun updateResult(data: Uri) {
        if (::mListener.isInitialized) {
            mListener.mTakePhotoAction?.invoke(data)
        }
    }

    /**
     * takephoto
     * @param activity Activity
     * @param resultBuilder ResultBuilder.() -> Unit
     */
    fun takePhoto(activity: Activity, resultBuilder: ResultBuilder.() -> Unit) {
        mListener = ResultBuilder().also(resultBuilder)
        TakePhotoActivity.takePhoto(activity)
    }

    /**
     * takePhotoWithCrop
     * @param activity Activity
     * @param width Int
     * @param height Int
     * @param resultBuilder ResultBuilder.() -> Unit
     */
    fun takePhotoWithCrop(activity: Activity, width: Int, height: Int, resultBuilder: ResultBuilder.() -> Unit) {
        mListener = ResultBuilder().also(resultBuilder)
        TakePhotoActivity.takePhotoWithCrop(activity, width, height)
    }


    /**
     * pickPictureFromGallery
     * @param activity Activity
     * @param resultBuilder ResultBuilder.() -> Unit
     */
    fun pickPictureFromGallery(activity: Activity, resultBuilder: ResultBuilder.() -> Unit) {
        mListener = ResultBuilder().also(resultBuilder)
        TakePhotoActivity.pickPictureFromGallery(activity)
    }

    /**
     *
     * @param activity Activity
     * @param width Int
     * @param height Int
     * @param resultBuilder ResultBuilder.() -> Unit
     */
    fun pickPictureFromGalleryWithCrop(
        activity: Activity,
        width: Int,
        height: Int,
        resultBuilder: ResultBuilder.() -> Unit
    ) {
        mListener = ResultBuilder().also(resultBuilder)
        TakePhotoActivity.pickPictureFromGalleryWithCrop(activity, width, height)
    }

    /**
     * pickPictureFromFile
     * @param activity Activity
     * @param resultBuilder ResultBuilder.() -> Unit
     */
    fun pickPictureFromFile(activity: Activity, resultBuilder: ResultBuilder.() -> Unit) {
        mListener = ResultBuilder().also(resultBuilder)
        TakePhotoActivity.pickPictureFromFile(activity)
    }

    /**
     * pickPictureFromFileWithCrop
     * @param activity Activity
     * @param width Int
     * @param height Int
     * @param resultBuilder ResultBuilder.() -> Unit
     */
    fun pickPictureFromFileWithCrop(
        activity: Activity,
        width: Int,
        height: Int,
        resultBuilder: ResultBuilder.() -> Unit
    ) {
        mListener = ResultBuilder().also(resultBuilder)
        TakePhotoActivity.pickPictureFromFileWithCrop(activity, width, height)
    }

    /**
     * takePhotoUseCustomCamera
     * @param activity Activity
     * @param cameraType Int
     * @param resultBuilder ResultBuilder.() -> Unit
     */
    fun takePhotoUseCustomCamera(activity: Activity, cameraType: Int, resultBuilder: ResultBuilder.() -> Unit) {
        mListener = ResultBuilder().also(resultBuilder)
        TakePhotoActivity.takePhotoUseCustomCamera(activity, cameraType)
    }

    /**
     * takePhotoUseCustomCameraWithCrop
     * @param activity Activity
     * @param width Int
     * @param height Int
     * @param cameraType Int
     * @param resultBuilder ResultBuilder.() -> Unit
     */
    fun takePhotoUseCustomCameraWithCrop(
        activity: Activity,
        width: Int,
        height: Int,
        cameraType: Int,
        resultBuilder: ResultBuilder.() -> Unit
    ) {
        mListener = ResultBuilder().also(resultBuilder)
        TakePhotoActivity.takePhotoUseCustomCameraWithCrop(activity, width, height, cameraType)
    }

    private lateinit var mListener: ResultBuilder

    inner class ResultBuilder {
        internal var mTakePhotoAction: ((data: Uri) -> Unit)? = null

        fun onTakePhoto(action: (data: Uri) -> Unit) {
            mTakePhotoAction = action
        }
    }


}