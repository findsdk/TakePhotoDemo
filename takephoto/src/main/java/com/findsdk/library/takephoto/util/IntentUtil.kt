package com.findsdk.library.takephoto.util

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils


/**
 * Created by bvb on 2018/12/27.
 */
internal object IntentUtil {

    /**
     * 获取裁剪照片的Intent
     * @param targetUri Uri
     * @param outPutUri Uri
     * @param options CropOptions
     * @return Intent
     */
    fun getCropIntent(targetUri: Uri, outputUri: Uri, options: CropOptions): Intent {
        val isReturnData = isReturnData()
        //        Log.w(TAG, "getCaptureIntentWithCrop:isReturnData:" + (isReturnData ? "true" : "false"));
        val intent = Intent("com.android.camera.action.CROP")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(targetUri, "image/*")
        intent.putExtra("crop", "true")
        if (options.aspectX * options.aspectY > 0) {
            intent.putExtra("aspectX", options.aspectX)
            intent.putExtra("aspectY", options.aspectY)
        }
        if (options.outputX * options.outputY > 0) {
            intent.putExtra("outputX", options.outputX)
            intent.putExtra("outputY", options.outputY)
        }
        intent.putExtra("scale", true)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
        intent.putExtra("return-data", isReturnData)
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        intent.putExtra("noFaceDetection", true) // no face detection
        return intent
    }

    fun getPickWithCropIntent(outPutUri: Uri, options: CropOptions): Intent {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        intent.type = "image/*"
        intent.putExtra("crop", "true")
        if (options.aspectX * options.aspectY > 0) {
            intent.putExtra("aspectX", options.aspectX)
            intent.putExtra("aspectY", options.aspectY)
        }
        if (options.outputX * options.outputY > 0) {
            intent.putExtra("outputX", options.outputX)
            intent.putExtra("outputY", options.outputY)
        }
        intent.putExtra("scale", true)
        intent.putExtra("scaleUpIfNeeded", true) //learn it from gallery2 source code

        intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri)
        intent.putExtra(
            "outputFormat",
            Bitmap.CompressFormat.JPEG.toString()
        )
        return intent
    }

    /**
     * 是否裁剪之后返回数据
     * @return Boolean
     */
    private fun isReturnData(): Boolean {
        val manufacturer = Build.MANUFACTURER
        if (!TextUtils.isEmpty(manufacturer)) {
            if (manufacturer.toLowerCase().contains("lenovo")) {//对于联想的手机返回数据
                return true
            }
        }
        return false
    }

    /**
     * AAAAAAAAAAAAA
     * 获取拍照的Intent
     * @param outPutUri Uri
     * @return Intent
     */
    fun getCaptureIntent(outPutUri: Uri): Intent {
        return Intent().apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            action = MediaStore.ACTION_IMAGE_CAPTURE
            putExtra(MediaStore.EXTRA_OUTPUT, outPutUri)
        }
    }

    /**
     * 获取选择照片的Intent
     * @return Intent
     */
    fun getPickIntentWithGallery(): Intent {
        return Intent().apply {
            action = Intent.ACTION_OPEN_DOCUMENT
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"//从所有图片中进行选择
        }
    }

    /**
     * 获取从文件中选择照片的Intent
     * @return Intent
     */
    fun getPickIntentWithDocuments(): Intent {
        return Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"//从所有图片中进行选择
        }
    }

    /**
     * @param activity Activity
     * @param intent Intent
     * @return Boolean
     */
    fun hasIntentActivities(activity: Activity, intent: Intent): Boolean {
        val result = activity.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return result.isNotEmpty()
    }
}