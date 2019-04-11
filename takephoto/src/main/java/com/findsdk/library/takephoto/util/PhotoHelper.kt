package com.findsdk.library.takephoto.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.text.TextUtils
import android.widget.Toast
import com.findsdk.library.fileprovider.FileUtil
import com.findsdk.library.fileprovider.UriUtil
import com.findsdk.library.takephoto.R
import com.findsdk.library.takephoto.TakePhotoConfig

import java.io.File
import java.util.*

/**
 * Created by bvb on 2018/12/27.
 */
internal object PhotoHelper {


    const val REQUEST_CODE_SELECT_FROM_GALLERY = 555
    const val REQUEST_CODE_SELECT_FROM_GALLERY_WITH_CROP = REQUEST_CODE_SELECT_FROM_GALLERY + 1
    const val REQUEST_CODE_SELECT_FROM_FILE = REQUEST_CODE_SELECT_FROM_GALLERY_WITH_CROP + 1
    const val REQUEST_CODE_SELECT_FROM_FILE_WITH_CROP = REQUEST_CODE_SELECT_FROM_FILE + 1
    const val REQUEST_CODE_TAKE_PHOTO = REQUEST_CODE_SELECT_FROM_FILE_WITH_CROP + 1
    const val REQUEST_CODE_TAKE_PHOTO_WITH_CROP = REQUEST_CODE_TAKE_PHOTO + 1
    const val REQUEST_CODE_CROP = REQUEST_CODE_TAKE_PHOTO_WITH_CROP + 1
    const val REQUEST_CODE_TAKE_CUSTOM_PHOTO = REQUEST_CODE_CROP + 1
    const val REQUEST_CODE_TAKE_CUSTOM_PHOTO_WITH_CROP = REQUEST_CODE_TAKE_CUSTOM_PHOTO + 1


    private const val REQUEST_PERMISSION_CAMERA = 348
    private const val REQUEST_PERMISSION_CUSTOM_CAMERA = 349

//        private var instance: PhotoHelper? = null
//        fun getInstance(context: Context): PhotoHelper {
//            if (instance == null) {
//                instance = PhotoHelper(context)
//            }
//            return instance!!
//        }


    /**
     * 图片文件输出Uri
     */
    private var outputUri: Uri? = null
    /**
     * 临时图片文件Uri
     */
    private var tempUri: Uri? = null
    /**
     * 裁剪完成的图片文件输出Uri
     */
    private var cropUri: Uri? = null
    /**
     * 图片保存路径
     */
    private var path: String? = null
    /**
     * 图片名称
     */
    private var fileName: String? = null
    /**
     * 图片路径
     */
    private var cameraTmpFile: File? = null
    /**
     * 是否裁剪的标志位
     */
    private var isCrop: Boolean = false
    /**
     * 裁剪配置类
     */
    private var cropOptions: CropOptions? = null
    /**
     * 裁剪需求宽度
     */
    private var imgWidth: Int = 0
    /**
     * 裁剪需求高度
     */
    private var imgHeight: Int = 0
    /**
     * 文件选择类型
     */
    private var pickRequestCode: Int = 0

    private var cameraType: Int = 0

    fun compress(context: Context, uri: Uri) {
        val bitmap = PhotoUtil.getRotatedBitmap(
            FileUtil.getFileWithUri(context, uri)!!.absolutePath, Constants.IMAGE_SIZE.UPLOAD_MAX_SIZE,
            Constants.IMAGE_SIZE.UPLOAD_MAX_WIDTH,
            Constants.IMAGE_SIZE.UPLOAD_MAX_HEIGHT
        )
        if (bitmap != null) {
            val file = PhotoUtil.saveBitmapFile(
                StringBuffer()
                    .append(FileUtil.getExternalFilesDirForPic(context))
                    .append(System.currentTimeMillis())
                    .append(".jpg").toString(),
                bitmap
            )
            if (file != null) {
                val uri1 = UriUtil.getUriForFile(context, file)
                sendCompress(context, uri1, null)
            } else {
                sendCompress(context, null, null)
            }
        } else {
            sendCompress(context, null, null)
        }
    }

    /**
     * 获取输出文件对象
     * @return File
     */
//    fun getOutputFile(context: Context): File {
//        val file: File
//        if (isCrop) {
//            if (tempUri != null) {
//                file = FileUtils.getFileWithUri(context, tempUri)
//            } else {
//                file = cameraTmpFile!!
//            }
//        } else {
//            if (outputUri != null) {
//                file = FileUtils.getFileWithUri(context, outputUri)
//            } else {
//                file = cameraTmpFile!!
//            }
//        }
//        return file
//    }

    /**
     * 获取裁剪文件对象
     * @return File
     */
//    fun getCropedFile(context: Context): File {
//        val file: File
//        if (cropUri != null) {
//            file = FileUtils.getFileWithUri(context, cropUri)
//        } else {
//            file = cameraTmpFile!!
//        }
//        return file
//    }

    private fun setSize(width: Int, height: Int) {
        imgWidth = width
        imgHeight = height
    }


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
        //        this.mActivity = activity;
        if (width < 0 || height < 0) {
            takePhoto(activity)
            return
        }
        isCrop = true
        setSize(width, height)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionCamera(activity, REQUEST_PERMISSION_CAMERA)
        } else {
            startCamera(activity)
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun takeCustomPhoto(activity: Activity, cameraType: Int) {
        isCrop = false
        this.cameraType = cameraType
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionCamera(activity, REQUEST_PERMISSION_CUSTOM_CAMERA)
        } else {
            startCustomCamera(activity)
        }
    }

    /**
     * 打开拍照界面
     * @param activity Activity
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun startCamera(activity: Activity) {
        path = PhotoUtil.getTempFile(activity).absolutePath // 图片保存路径
        fileName = Random().nextInt().toString() + "tmp.jpg"
        cameraTmpFile = File(path, fileName)
        if (!StorageUtil.isSDCardEnable()) {
            showToast(activity, TakePhotoConfig.languageNoSDCard)
            return
        }
        val folder = File(path)
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                showToast(activity, TakePhotoConfig.languageDirCreateFailure)
                return
            }
        }
        val imageUri = Uri.fromFile(cameraTmpFile)
        if (isCrop)
            captureWithCrop(activity, imageUri)
        else
            capture(activity, imageUri)
    }

    /**
     * 使用系统相机拍照并裁剪
     * @param activity Activity
     * @param outputUri Uri
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun captureWithCrop(activity: Activity, outputUri: Uri) {
        this.outputUri = outputUri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.tempUri = PhotoUtil.getTempUri(activity)
        } else {
            this.tempUri = outputUri
        }
        try {
            val intent = IntentUtil.getCaptureIntent(this.tempUri!!)
            if (captureBySafely(activity, intent))
                activity.startActivityForResult(
                    intent,
                    Constants.USE_CAMERA_WITH_CROP
                )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 拍照前检查是否有相机
     * @param activity Activity
     * @param intent Intent
     * @return Boolean
     * @throws Exception
     */
    @RequiresApi(Build.VERSION_CODES.M)
    @Throws(Exception::class)
    private fun captureBySafely(activity: Activity, intent: Intent): Boolean {
        val result = activity.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return if (result.isEmpty()) {
            showToast(activity, TakePhotoConfig.languageNoCamera)
            false
        } else {
            true
        }
    }

    /**
     * 使用系统相机拍照
     * @param activity Activity
     * @param outputUri Uri
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun capture(activity: Activity, outputUri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.outputUri = UriUtil.convertFileUriToFileProviderUri(activity, outputUri)
        } else {
            this.outputUri = outputUri
        }
        try {
            val intent = IntentUtil.getCaptureIntent(this.outputUri!!)
            if (captureBySafely(activity, intent))
                activity.startActivityForResult(intent, Constants.USE_CAMERA)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * pickFromGallery
     * @param activity Activity
     */
    fun pickFromGallery(activity: Activity) {
        //        this.mActivity = activity;
        isCrop = false
        pickRequestCode = Constants.TAKE_GALLERY
        startPick(activity)
    }

    /**
     * pickFromGalleryWithCrop
     * @param activity Activity
     * @param width Int
     * @param height Int
     */
    fun pickFromGalleryWithCrop(activity: Activity, width: Int, height: Int) {
        //        this.mActivity = activity;
        if (width < 0 || height < 0) {
            pickFromGallery(activity)
            return
        }
        setSize(width, height)
        isCrop = true
        pickRequestCode = Constants.TAKE_GALLERY_WITH_CROP
        startPick(activity)
    }

    /**
     * pickFromFile
     * @param activity Activity
     */
    fun pickFromFile(activity: Activity) {
        isCrop = false
        pickRequestCode = Constants.TAKE_FILE
        startPick(activity)
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
        setSize(width, height)
        isCrop = true
        pickRequestCode = Constants.TAKE_FILE_WITH_CROP
        startPick(activity)
    }

    private fun showToast(activity: Activity, text: String) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }


    /**
     * 使用系统相机时请求拍照权限
     * @param activity Activity
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermissionCamera(activity: Activity, requestCode: Int) {
        if (PermissionUtil.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                requestCode
            )
        ) {
            when (requestCode) {
                REQUEST_PERMISSION_CAMERA -> {
                    startCamera(activity)
                }
                REQUEST_PERMISSION_CUSTOM_CAMERA -> {
                    startCustomCamera(activity)
                }
            }
        }
    }

    /**
     * 显示请求权限对话框
     * @param activity Activity
     * @param message String
     */
    private fun showPermissionDialog(activity: Activity, message: String) {
        val builder = AlertDialog.Builder(activity, R.style.PhotoModuleAlertDialog)
        builder.setMessage(message)
        builder.setPositiveButton(TakePhotoConfig.languageSetting) { dialogInterface, i ->
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + activity.packageName)
            activity.startActivity(intent)
            activity.finish()
        }
        builder.setNegativeButton(android.R.string.cancel) { dialogInterface, i -> activity.finish() }
        builder.setCancelable(false)
        builder.create().show()
    }

    /**
     * startPick
     * @param activity Activity
     */
    private fun startPick(activity: Activity) {
        if (isCrop) {
            path = PhotoUtil.getTempFile(activity).absolutePath // 图片保存路径
            fileName = Random().nextInt().toString() + "tmp.jpg"
            cameraTmpFile = File(path, fileName)
            //            File file = new File(Environment.getExternalStorageDirectory(), "/.tmp/" + "tmp.jpg");
            if (cameraTmpFile != null && cameraTmpFile!!.parentFile != null && !cameraTmpFile!!.parentFile.exists()) {
                cameraTmpFile!!.parentFile.mkdirs()
            }
            val imageUri = Uri.fromFile(cameraTmpFile)
            onPickFromGalleryWithCrop(
                activity,
                imageUri,
                pickRequestCode,
                PhotoUtil.getCropOptions(imgWidth, imgHeight)
            )
        } else {
            onPickFromGallery(activity, pickRequestCode)
        }
    }

    /**
     * 打开相册选择照片
     * @param activity Activity
     * @param requestCode Int
     */
    private fun onPickFromGallery(activity: Activity, requestCode: Int) {
        selectPicture(activity, requestCode)
    }

    /**
     * 打开相册选择照片并裁剪
     * @param activity Activity
     * @param outPutUri Uri
     * @param requestCode Int
     * @param options CropOptions
     */
    private fun onPickFromGalleryWithCrop(activity: Activity, outPutUri: Uri, requestCode: Int, options: CropOptions) {
        this.cropOptions = options
        this.cropUri = outPutUri
        selectPicture(activity, requestCode)
    }

    /**
     * 选择照片
     * @param activity Activity
     * @param requestCode Int
     */
    private fun selectPicture(activity: Activity, requestCode: Int) {
        var intent: Intent? = null
        when (requestCode) {
            Constants.TAKE_FILE, Constants.TAKE_FILE_WITH_CROP -> intent =
                IntentUtil.getPickIntentWithDocuments()
            Constants.TAKE_GALLERY, Constants.TAKE_GALLERY_WITH_CROP -> intent =
                IntentUtil.getPickIntentWithGallery()
        }
        if (intent != null)
            activity.startActivityForResult(intent, requestCode)
    }


    /**
     * 需要在Activity的onRequestPermissionsResult方法中调用
     * @param activity Activity
     * @param requestCode Int
     * @param permissions Array<String>
     * @param grantResults IntArray?
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun onRequestPermissionsResult(
        activity: Activity,
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray?
    ) {
        if (grantResults != null) {
            when (requestCode) {
                REQUEST_PERMISSION_CAMERA -> {
                    var ret = true
                    for (grantResult in grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            ret = false
                        }
                    }
                    if (ret)
                        startCamera(activity)
                    else {
                        showPermissionDialog(activity, TakePhotoConfig.languageRequestPermissionsCameraTips)
                    }
                }
                REQUEST_PERMISSION_CUSTOM_CAMERA -> {
                    var ret = true
                    for (grantResult in grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            ret = false
                        }
                    }
                    if (ret)
                        startCustomCamera(activity)
                    else {
                        showPermissionDialog(activity, TakePhotoConfig.languageRequestPermissionsCameraTips)
                    }
                }
            }
        }

    }

    private fun startCustomCamera(activity: Activity) {
        path = PhotoUtil.getTempFile(activity).absolutePath // 图片保存路径
        fileName = Random().nextInt().toString() + "tmp.jpg"
        cameraTmpFile = File(path, fileName)
        if (!StorageUtil.isSDCardEnable()) {
            showToast(activity, TakePhotoConfig.languageNoSDCard)
            return
        }
        val folder = File(path)
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                showToast(activity, TakePhotoConfig.languageDirCreateFailure)
                return
            }
        }
        val imageUri = Uri.fromFile(cameraTmpFile)
        if (isCrop)
            captureWithCropByCustom(activity, imageUri, cameraType)
        else
            captureByCustom(activity, imageUri, cameraType)
    }

    /**
     * 使用自定义相机拍照
     * @param activity Activity
     * @param outputUri Uri
     * @param cameraType Int
     */
    private fun captureByCustom(activity: Activity, outputUri: Uri, cameraType: Int) {
        if (Build.VERSION.SDK_INT >= 23) {
            this.outputUri = UriUtil.convertFileUriToFileProviderUri(activity, outputUri)
        } else {
            this.outputUri = outputUri
        }
        try {
            activity.startActivityForResult(
                Intent(TakePhotoConfig.ACTION_CUSTOM_CAMERA)
                    .putExtra(Constants.INTENT_KEY.CAMERA_FACING_TYPE, cameraType)
                    .putExtra(Constants.INTENT_KEY.EXTRA_OUTPUT, this.outputUri),
                Constants.USE_CUSTOM_CAMERA
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    /**
     * 使用自定义相机拍照并裁剪
     *
     * @param activity
     * @param outputUri
     * @param useBackCamera
     */
    private fun captureWithCropByCustom(activity: Activity, outputUri: Uri, cameraType: Int) {
        this.outputUri = outputUri
        if (Build.VERSION.SDK_INT >= 23) {
            this.tempUri = PhotoUtil.getTempUri(activity)
        } else {
            this.tempUri = outputUri
        }
        try {
            activity.startActivityForResult(
                Intent(TakePhotoConfig.ACTION_CUSTOM_CAMERA)
                    .putExtra(
                        Constants.INTENT_KEY.CAMERA_FACING_TYPE, cameraType
                    )
                    .putExtra(Constants.INTENT_KEY.EXTRA_OUTPUT, this.tempUri),
                Constants.USE_CUSTOM_CAMERA_WITH_CROP
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun crop(activity: Activity, targetUri: Uri, outPutUri: Uri) {
        onCrop(activity, targetUri, outPutUri, PhotoUtil.getCropOptions(imgWidth, imgHeight))
    }

    private fun onCrop(activity: Activity, targetUri: Uri, outPutUri: Uri, options: CropOptions) {
        //this.outputUri = outPutUri;
        this.cropUri = outPutUri
        val intent = IntentUtil.getCropIntent(targetUri, outPutUri, options)
        activity.startActivityForResult(intent, Constants.TAKE_PHOTO_WITH_CROP)
    }

    private fun startDefaultCrop(activity: Activity, uri: Uri) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        // 设置裁
        intent.putExtra("crop", "true")
        // aspectX aspectY 是宽高的比例
        //        intent.putExtra("aspectX", 1);
        //        intent.putExtra("aspectY", 1);
        //        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", imgWidth)
        intent.putExtra("outputY", imgHeight)
        intent.putExtra("scale", true)// 去黑边
        intent.putExtra("scaleUpIfNeeded", true)// 去黑边
        intent.putExtra("outputFormat", "JPEG")// 返回格式
        intent.putExtra("return-data", true)
        activity.startActivityForResult(intent, REQUEST_CODE_CROP)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                // 拍照
                Constants.USE_CUSTOM_CAMERA, Constants.USE_CAMERA -> {
                    if (outputUri != null) {
                        val uri = Uri.fromFile(FileUtil.getFileWithUri(activity, outputUri!!))
                        sendResult(activity, uri, null)
                    } else {
                        sendResult(activity, Uri.fromFile(cameraTmpFile), null)
                    }
                }
                //拍照并裁剪
                //                case Constants.USE_CUSTOM_CAMERA_WITH_CROP:
                Constants.USE_CAMERA_WITH_CROP -> {
                    if (outputUri != null) {
                        val uri = Uri.fromFile(File(UriUtil.parseUri(activity, outputUri!!)))
                        crop(activity, tempUri!!, uri)
                        outputUri = null
                    } else {
                        val uri = Uri.fromFile(cameraTmpFile)
                        startDefaultCrop(activity, uri)
                        cameraTmpFile = null
                    }
                }
                // 裁剪返回结果
                Constants.TAKE_PHOTO_WITH_CROP -> if (outputUri != null) {
                    sendResult(activity, outputUri!!, null)
                    outputUri = null
                } else {
                    sendResult(activity, Uri.fromFile(cameraTmpFile), null)
                    cameraTmpFile = null
                }
                //从相册选择照片不裁剪
                Constants.TAKE_GALLERY -> {
                    val galleryUri = data!!.data
                    val uri = PhotoUtil.convertGalleryUriToFileProviderUri(activity, galleryUri)
                    val filePath = FileUtil.getFilePathWithUri(activity, uri)
                    if (!PhotoUtil.isImageFile(activity, filePath)) {
                        notImageFile(activity)
                        return
                    }
                    sendResult(activity, uri, null)
                }
                //从相册选择照片并裁剪
                Constants.TAKE_GALLERY_WITH_CROP -> {
                    val galleryUri = data!!.data
                    val uri = PhotoUtil.convertGalleryUriToFileProviderUri(activity, galleryUri)
                    val filePath = FileUtil.getFilePathWithUri(activity, uri)
                    if (!PhotoUtil.isImageFile(activity, filePath)) {
                        notImageFile(activity)
                        return
                    }
                    onCrop(activity, uri, cropUri!!, cropOptions!!)
                }
                //从文件选择照片不裁剪
                Constants.TAKE_FILE -> {
                    val uri = data!!.data
                    val filePath = FileUtil.getFilePathWithDocumentsUri(activity, uri)
                    if (!PhotoUtil.isImageFile(activity, filePath)) {
                        notImageFile(activity)
                        return
                    }
                    val uri1 = Uri.fromFile(File(filePath))
                    sendResult(activity, uri1, null)
                }
                //从文件选择照片，并裁剪
                Constants.TAKE_FILE_WITH_CROP -> {
                    val uri = data!!.data
                    val filePath = FileUtil.getFilePathWithDocumentsUri(activity, uri)
                    if (!PhotoUtil.isImageFile(activity, filePath)) {
                        notImageFile(activity)
                        return
                    }
                    onCrop(activity, uri, cropUri!!, cropOptions!!)
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            sendResult(activity, null, null)
        }
    }

    private fun notImageFile(activity: Activity) {
        showToast(activity, TakePhotoConfig.languageNotImage)
        sendResult(activity, null, null)
    }


    private fun sendResult(context: Context, uri: Uri?, errorMessage: String?) {
        val intent = Intent(Constants.ACTION_PHOTO_RESULT)
        if (uri != null)
            intent.putExtra("uri", uri.toString())
        if (!TextUtils.isEmpty(errorMessage))
            intent.putExtra(Constants.INTENT_KEY.ERROR_MESSAGE, errorMessage)
        context.sendBroadcast(intent)
    }

    private fun sendCompress(context: Context, uri: Uri?, errorMessage: String?) {
        val intent = Intent(Constants.ACTION_PHOTO_COMPRESS)
        if (uri != null)
            intent.putExtra("uri", uri.toString())
        if (!TextUtils.isEmpty(errorMessage))
            intent.putExtra(Constants.INTENT_KEY.ERROR_MESSAGE, errorMessage)
        context.sendBroadcast(intent)
    }

}