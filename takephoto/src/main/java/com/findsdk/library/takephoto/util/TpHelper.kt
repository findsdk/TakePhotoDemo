package com.findsdk.library.takephoto.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import com.findsdk.library.takephoto.TakePhotoConfig
import com.findsdk.library.takephoto.fileprovider.FileUtil
import com.findsdk.library.takephoto.fileprovider.UriUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Created by bvb on 2020/7/29.
 */
internal class TpHelper private constructor() {

    companion object {

        private const val REQUEST_PERMISSION_CAMERA = 348

        val instance: TpHelper by lazy {
            TpHelper()
        }

    }

    /**
     * 拍照临时输出Uri
     */
    private var tmpCameraUri: Uri? = null

    /**
     * 裁剪完成的图片文件输出Uri
     */
    private var tmpCropUri: Uri? = null

    /**
     * 是否裁剪的标志位
     */
    private var isCrop: Boolean = false

    /**
     * 裁剪需求宽度
     */
    private var cropWidth: Int = 0

    /**
     * 裁剪需求高度
     */
    private var cropHeight: Int = 0

    //----------take photo begin-------------

    /**
     * takePhoto
     * @param activity Activity
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun takePhoto(activity: Activity) {
        isCrop = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionCamera(activity, REQUEST_PERMISSION_CAMERA)
        } else {
            startCamera(activity)
        }
    }

    /**
     * 使用自定义相机拍照并裁剪到指定大小
     * @param activity Activity
     * @param width Int
     * @param height Int
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun takePhotoWithCrop(activity: Activity, width: Int, height: Int) {
        if (width <= 0 || height <= 0) {
            takePhoto(activity)
            return
        }
        isCrop = true
        setCropSize(width, height)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionCamera(activity, REQUEST_PERMISSION_CAMERA)
        } else {
            startCamera(activity)
        }
    }

    //----------------take photo end-------------------
    //----------------pick from gallery--------------
    /**
     * pickFromGallery
     * @param activity Activity
     */
    fun pickFromGallery(activity: Activity) {
        isCrop = false
        PickUtil.pickPicture(activity, Constants.PICK_FROM_GALLERY)
    }

    /**
     * pickFromGalleryWithCrop
     * @param activity Activity
     * @param width Int
     * @param height Int
     */
    fun pickFromGalleryWithCrop(activity: Activity, width: Int, height: Int) {
        if (width <= 0 || height <= 0) {
            pickFromGallery(activity)
            return
        }
        isCrop = true
        setCropSize(width, height)
        PickUtil.pickPicture(activity, Constants.PICK_FROM_GALLERY_WITH_CROP)
    }

    //--------------pick from gallery end--------------
    //--------------pick from file begin---------------
    /**
     * pickFromFile
     * @param activity Activity
     */
    fun pickFromFile(activity: Activity) {
        isCrop = false
        PickUtil.pickPicture(activity, Constants.PICK_FROM_FILE)
    }

    /**
     * pickFromFileWithCrop
     * @param activity Activity
     * @param width Int
     * @param height Int
     */
    fun pickFromFileWithCrop(activity: Activity, width: Int, height: Int) {
        if (width < 0 || height < 0) {
            pickFromFile(activity)
            return
        }
        isCrop = true
        setCropSize(width, height)
        PickUtil.pickPicture(activity, Constants.PICK_FROM_FILE_WITH_CROP)
    }

    //----------------pick from file end-----------------
    //----------------crop begin-----------------------
    private fun crop(activity: Activity, targetUri: Uri) {
        onCrop(activity, targetUri, TpUtil.getCropOptions(cropWidth, cropHeight))
    }

    private fun onCrop(activity: Activity, targetUri: Uri, options: CropOptions) {
        val prefix = "tpc_${System.currentTimeMillis()}"
        val suffix = ".jpg"
        tmpCropUri = TpUtil.getTmpUri(activity, prefix, suffix)
        val intent = IntentUtil.getCropIntent(targetUri, tmpCropUri!!, options)
        val resInfoList: List<ResolveInfo> = activity.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName: String = resolveInfo.activityInfo.packageName
            activity.grantUriPermission(packageName, tmpCropUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        activity.startActivityForResult(intent, Constants.CROP_RESULT)
    }

    //---------------crop end------------------

    //-------------request permission begin-----------------------------
    /**
     * 使用系统相机时请求拍照权限
     * @param activity Activity
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermissionCamera(activity: Activity, requestCode: Int) {
        if (PermissionUtil.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), requestCode)) {
            when (requestCode) {
                REQUEST_PERMISSION_CAMERA -> {
                    startCamera(activity)
                }
            }
        }
    }

    /**
     * 需要在Activity的onRequestPermissionsResult方法中调用
     * @param activity Activity
     * @param requestCode Int
     * @param permissions Array<String>
     * @param grantResults IntArray?
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun onRequestPermissionsResult(activity: Activity, requestCode: Int, permissions: Array<String>, grantResults: IntArray?) {
        grantResults?.let {
            when (requestCode) {
                REQUEST_PERMISSION_CAMERA -> {
                    var ret = true
                    for (grantResult in it) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            ret = false
                        }
                    }
                    if (ret)
                        startCamera(activity)
                    else
                        DialogUtil.showPermissionDialog(activity, TakePhotoConfig.languageRequestPermissionsCameraTips)
                }
            }
        }
    }

    //-------------request permission end-----------------------------
    //-------------activity result begin-------------------
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                // 拍照
                Constants.TAKE_PHOTO, Constants.TAKE_PHOTO_USE_CUSTOM_CAMERA -> {
                    tmpCameraUri?.let {
                        sendResult(activity, it, null)
                        tmpCameraUri = null
                    } ?: kotlin.run {
                        sendResult(activity, null, null)
                    }
                }
                //拍照并裁剪
                Constants.TAKE_PHOTO_WITH_CROP, Constants.TAKE_PHOTO_USE_CUSTOM_CAMERA_WITH_CROP -> {
                    tmpCameraUri?.let {
                        crop(activity, it)
                        tmpCameraUri = null
                    } ?: kotlin.run {
                        sendResult(activity, null, null)
                    }
                }
                // 裁剪返回结果
                Constants.CROP_RESULT -> {
                    tmpCropUri?.let {
                        sendResult(activity, it, null)
                        tmpCropUri = null
                    } ?: kotlin.run {
                        sendResult(activity, null, null)
                    }
                }
                //从相册选择照片不裁剪
                Constants.PICK_FROM_GALLERY -> {
                    data?.data?.let {
                        val uri = TpUtil.convertGalleryUriToFileProviderUri(activity, it)
                        val filePath = FileUtil.getFilePathWithUri(activity, uri)
                        if (!TpUtil.isImageFile(activity, filePath)) {
                            notImageFile(activity)
                            return
                        }
                        sendResult(activity, uri, null)
                    } ?: kotlin.run {
                        sendResult(activity, null, null)
                    }
                }
                //从相册选择照片并裁剪
                Constants.PICK_FROM_GALLERY_WITH_CROP -> {
                    data?.data?.let {
                        val uri = TpUtil.convertGalleryUriToFileProviderUri(activity, it)
                        val filePath = FileUtil.getFilePathWithUri(activity, uri)
                        if (!TpUtil.isImageFile(activity, filePath)) {
                            notImageFile(activity)
                            return
                        }
                        crop(activity, it)
                    } ?: kotlin.run {
                        sendResult(activity, null, null)
                    }
                }
                //从文件选择照片不裁剪
                Constants.PICK_FROM_FILE -> {
                    data?.data?.let {
                        val filePath = FileUtil.getFilePathWithDocumentsUri(activity, it)
                        if (!TpUtil.isImageFile(activity, filePath)) {
                            notImageFile(activity)
                            return
                        }
                        val uri1 = Uri.fromFile(File(filePath))
                        sendResult(activity, uri1, null)
                    } ?: kotlin.run {
                        sendResult(activity, null, null)
                    }
                }
                //从文件选择照片，并裁剪
                Constants.PICK_FROM_FILE_WITH_CROP -> {
                    data?.data?.let {
                        val filePath = FileUtil.getFilePathWithDocumentsUri(activity, it)
                        if (!TpUtil.isImageFile(activity, filePath)) {
                            notImageFile(activity)
                            return
                        }
                        crop(activity, it)
                    } ?: kotlin.run {
                        sendResult(activity, null, null)
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            sendResult(activity, null, null)
        }
    }
    //------------activity result end-----------------
    /**
     * 打开拍照界面
     * @param activity Activity
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun startCamera(activity: Activity) {
        if (!StorageUtil.isExternalStorageEnable()) {
            activity.toast(TakePhotoConfig.languageExternalStorageDisable)
            return
        }
        val prefix = "tp_${System.currentTimeMillis()}"
        val suffix = ".jpg"
        val outputUri = TpUtil.getTmpUri(activity, prefix, suffix)
        capture(activity, outputUri, if (isCrop) Constants.TAKE_PHOTO_WITH_CROP else Constants.TAKE_PHOTO)
    }

    /**
     * 使用系统相机拍照
     * @param activity Activity
     * @param outputUri Uri
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun capture(activity: Activity, outputUri: Uri, requestCode: Int) {
        try {
            this.tmpCameraUri = outputUri
            val intent = IntentUtil.getCaptureIntent(outputUri)
            if (IntentUtil.hasIntentActivities(activity, intent))
                activity.startActivityForResult(intent, requestCode)
            else {
                activity.toast(TakePhotoConfig.languageNoCamera)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //----------compress begin------------
    fun compress(context: Context, uri: Uri) {
        MainScope().launch {
            val fileUri = withContext(Dispatchers.IO) {
                val bitmap = CompressUtil.getRotatedBitmap(
                    FileUtil.getFileWithUri(context, uri)!!.absolutePath,
                    Constants.IMAGE_SIZE.UPLOAD_MAX_SIZE,
                    Constants.IMAGE_SIZE.UPLOAD_MAX_WIDTH,
                    Constants.IMAGE_SIZE.UPLOAD_MAX_HEIGHT
                )
                bitmap?.let {
                    TpUtil.saveBitmapFile(context, it)?.let { file ->
                        UriUtil.getUriForFile(context, file)
                    }
                }
            }
            sendCompress(context, fileUri, null)
        }

//        val bitmap = CompressUtil.getRotatedBitmap(
//            FileUtil.getFileWithUri(context, uri)!!.absolutePath,
//            Constants.IMAGE_SIZE.UPLOAD_MAX_SIZE,
//            Constants.IMAGE_SIZE.UPLOAD_MAX_WIDTH,
//            Constants.IMAGE_SIZE.UPLOAD_MAX_HEIGHT
//        )
//        bitmap?.let {
//            TpUtil.saveBitmapFile(context, it)?.let { file ->
//                sendCompress(context, UriUtil.getUriForFile(context, file), null)
//            }
//        } ?: sendCompress(context, null, null)

    }

    //-----------compress end------------
    //-------------send result begin-------------------
    private fun sendResult(context: Context, uri: Uri?, errorMessage: String?) {
        val intent = Intent(Constants.ACTION_PHOTO_RESULT)
        uri?.let {
            intent.putExtra("uri", it)
        }
        if (errorMessage.isNullOrEmpty())
            intent.putExtra(Constants.INTENT_KEY.ERROR_MESSAGE, errorMessage)
        context.sendBroadcast(intent)
    }

    private fun sendCompress(context: Context, uri: Uri?, errorMessage: String?) {
        val intent = Intent(Constants.ACTION_PHOTO_COMPRESS)
        uri?.let {
            intent.putExtra("uri", it)
        }
        if (!TextUtils.isEmpty(errorMessage))
            intent.putExtra(Constants.INTENT_KEY.ERROR_MESSAGE, errorMessage)
        context.sendBroadcast(intent)
    }

    private fun notImageFile(activity: Activity) {
        activity.toast(TakePhotoConfig.languageNotImage)
        sendResult(activity, null, null)
    }
    //--------------send result end--------------

    private fun setCropSize(width: Int, height: Int) {
        cropWidth = width
        cropHeight = height
    }
}
