package com.findsdk.library.takephoto.util

import android.content.Intent
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
    fun getCropIntent(targetUri: Uri, outPutUri: Uri, options: CropOptions): Intent {
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
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri)
        intent.putExtra("return-data", isReturnData)
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        intent.putExtra("noFaceDetection", true) // no face detection
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
     * 获取拍照的Intent
     * @param outPutUri Uri
     * @return Intent
     */
    fun getCaptureIntent(outPutUri: Uri): Intent {
        //        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        intent.action = MediaStore.ACTION_IMAGE_CAPTURE//设置Action为拍照
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri)//将拍取的照片保存到指定URI
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
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.action = Intent.ACTION_OPEN_DOCUMENT
        } else {
            intent.action = Intent.ACTION_GET_CONTENT
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"//从所有图片中进行选择
        return intent
    }

    /**
     * 获取从文件中选择照片的Intent
     * @return Intent
     */
    fun getPickIntentWithDocuments(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        return intent
    }
}