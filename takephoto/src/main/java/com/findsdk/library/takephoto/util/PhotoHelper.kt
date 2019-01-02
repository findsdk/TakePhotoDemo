package com.findsdk.library.takephoto.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.findsdk.library.fileprovider.FileUtils
import com.findsdk.library.rxbus.RxBusHelper
import com.findsdk.library.takephoto.R
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*

/**
 * Created by bvb on 2018/12/27.
 */
internal class PhotoHelper(var context: Context) {

    companion object {
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
        private const val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 350

        private var instance: PhotoHelper? = null
        fun getInstance(context: Context): PhotoHelper {
            if (instance == null) {
                instance = PhotoHelper(context)
            }
            return instance!!
        }
    }

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
    private var cameraTmpFile: File?=null
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

    fun compress(context: Context, uri: Uri, observer: Observer<Uri>) {
        Observable.just(uri)
            .map { uri -> FileUtils.getFileWithUri(context, uri) }
            .subscribeOn(Schedulers.io())
            .map(Function<File, Bitmap> { file ->
                PhotoUtil.getRotatedBitmap(
                    file.absolutePath,
                    Constants.IMAGE_SIZE.UPLOAD_MAX_SIZE,
                    Constants.IMAGE_SIZE.UPLOAD_MAX_WIDTH,
                    Constants.IMAGE_SIZE.UPLOAD_MAX_HEIGHT
                )
            })
            .map(Function<Bitmap, File> { bitmap ->
                PhotoUtil.saveBitmapFile(
                    StringBuffer()
                        //.append(FileUtils.getExternalStoragePublicDirectoryForPic(mContext))
                        .append(FileUtils.getExternalFilesDirForPic(context))
                        .append(System.currentTimeMillis())
                        .append(".jpg").toString(),
                    bitmap
                )
            })
            .filter(object : Predicate<File> {
                @Throws(Exception::class)
                override fun test(file: File): Boolean {
                    return file != null
                }
            })
            .map { file -> FileUtils.getUriForFile(context, file) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(observer)

    }

    /**
     * 获取输出文件对象
     * @return File
     */
    fun getOutputFile(): File {
        val file: File
        if (isCrop) {
            if (tempUri != null) {
                file = FileUtils.getFileWithUri(context, tempUri)
            } else {
                file = cameraTmpFile!!
            }
        } else {
            if (outputUri != null) {
                file = FileUtils.getFileWithUri(context, outputUri)
            } else {
                file = cameraTmpFile!!
            }
        }
        return file
    }

    /**
     * 获取裁剪文件对象
     * @return File
     */
    fun getCropedFile(): File {
        val file: File
        if (cropUri != null) {
            file = FileUtils.getFileWithUri(context, cropUri)
        } else {
            file = cameraTmpFile!!
        }
        return file
    }

    private fun setSize(width: Int, height: Int) {
        imgWidth = width
        imgHeight = height
    }


    /**
     * takePhoto
     * @param activity Activity
     */
    fun takePhoto(activity: Activity) {
        isCrop = false
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissionCamera(activity)
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
    fun takePhotoWithCrop(activity: Activity, width: Int, height: Int) {
        //        this.mActivity = activity;
        if (width < 0 || height < 0) {
            takePhoto(activity)
            return
        }
        isCrop = true
        setSize(width, height)
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissionCamera(activity)
        } else {
            startCamera(activity)
        }
    }


    /**
     * 打开拍照界面
     * @param activity Activity
     */
    private fun startCamera(activity: Activity) {
        path = FileUtils.getBasePath(activity).absolutePath // 图片保存路径
        fileName = Random().nextInt().toString() + "tmp.jpg"
        cameraTmpFile = File(path, fileName)
        if (!SDCardUtil.isSDCardEnable()) {
            showToast(activity, activity.getString(R.string.no_sd_card))
            return
        }
        val folder = File(path)
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                showToast(activity, activity.getString(R.string.dir_create_failure))
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
    private fun captureWithCrop(activity: Activity, outputUri: Uri) {
        this.outputUri = outputUri
        if (Build.VERSION.SDK_INT >= 23) {
            this.tempUri = FileUtils.getTempUri(activity)
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
    @Throws(Exception::class)
    private fun captureBySafely(activity: Activity, intent: Intent): Boolean {
        val result = activity.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        if (result.isEmpty()) {
            showToast(activity, activity.getString(R.string.no_camera))
            return false
        } else {
            return true
        }
    }

    /**
     * 使用系统相机拍照
     * @param activity Activity
     * @param outputUri Uri
     */
    private fun capture(activity: Activity, outputUri: Uri) {
        if (Build.VERSION.SDK_INT >= 23) {
            this.outputUri = FileUtils.convertFileUriToFileProviderUri(activity, outputUri)
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
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissionExternalStorage(activity)
        } else {
            startPick(activity)
        }
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
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissionExternalStorage(activity)
        } else {
            startPick(activity)
        }
    }

    /**
     * pickFromFile
     * @param activity Activity
     */
    fun pickFromFile(activity: Activity) {
        isCrop = false
        pickRequestCode = Constants.TAKE_FILE
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissionExternalStorage(activity)
        } else {
            startPick(activity)
        }
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
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissionExternalStorage(activity)
        } else {
            startPick(activity)
        }
    }

    private fun showToast(activity: Activity, text: String) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }


    /**
     * 使用系统相机时请求拍照权限
     * @param activity Activity
     */
    private fun requestPermissionCamera(activity: Activity) {
        if (PermissionUtil.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                REQUEST_PERMISSION_CAMERA
            )
        ) {
            startCamera(activity)
        }
    }

    /**
     * 请求访问内部存储权限
     * @param activity Activity
     */
    private fun requestPermissionExternalStorage(activity: Activity) {
        if (PermissionUtil.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE
            )
        ) {
            startPick(activity)
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
        builder.setPositiveButton(R.string.setting) { dialogInterface, i ->
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
            path = FileUtils.getBasePath(activity).absolutePath // 图片保存路径
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
                        showPermissionDialog(activity, activity.getString(R.string.request_permissions_camera_tips))
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
                        startCustomCamera(activity, false)
                    else {
                        showPermissionDialog(activity, activity.getString(R.string.request_permissions_camera_tips))
                    }
                }
                REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE -> {
                    var ret = true
                    for (grantResult in grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            ret = false
                        }
                    }
                    if (ret)
                        startPick(activity)
                    else {
                        showPermissionDialog(
                            activity,
                            activity.getString(R.string.request_permissions_external_storage_tips)
                        )
                    }
                }
            }
        }

    }

    private fun startCustomCamera(activity: Activity, useBackCamera: Boolean) {
        path = FileUtils.getBasePath(activity).absolutePath // 图片保存路径
        fileName = Random().nextInt().toString() + "tmp.jpg"
        cameraTmpFile = File(path, fileName)
        if (!SDCardUtil.isSDCardEnable()) {
            showToast(activity, activity.getString(R.string.no_sd_card))
            return
        }
        val folder = File(path)
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                showToast(activity, activity.getString(R.string.dir_create_failure))
                return
            }
        }
        val imageUri = Uri.fromFile(cameraTmpFile)
        if (isCrop)
            captureWithCropByCustom(activity, imageUri, useBackCamera)
        else
            captureByCustom(activity, imageUri, useBackCamera)
    }

    /**
     * 使用自定义相机拍照
     * @param activity Activity
     * @param outputUri Uri
     * @param useBackCamera Boolean
     */
    private fun captureByCustom(activity: Activity, outputUri: Uri, useBackCamera: Boolean) {
        if (Build.VERSION.SDK_INT >= 23) {
            this.outputUri = FileUtils.convertFileUriToFileProviderUri(activity, outputUri)
        } else {
            this.outputUri = outputUri
        }
        try {
            activity.startActivityForResult(
                Intent(Constants.ACTION.ACTION_CAMERA)
                    .putExtra(
                        Constants.INTENT_KEY.CAMERA_FACING_TYPE,
                        if (useBackCamera) Constants.CAMERA_FACING_TYPE.CAMERA_BACK else Constants.CAMERA_FACING_TYPE.CAMERA_FRONT
                    )
                    .putExtra(Constants.INTENT_KEY.EXTRA_OUTPUT, this.outputUri),
                Constants.USE_CUSTOM_CAMERA
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 使用自定义相机拍照
     * @param activity Activity
     * @param outputFile File
     * @param useBackCamera Boolean
     */
    private fun captureByCustom(activity: Activity, outputFile: File, useBackCamera: Boolean) {
        val uri = Uri.fromFile(outputFile)
        if (Build.VERSION.SDK_INT >= 23) {
            this.outputUri = FileUtils.convertFileUriToFileProviderUri(activity, uri)
        } else {
            this.outputUri = uri
        }
        try {
            activity.startActivityForResult(
                Intent("com.common.photomodule.camera").putExtra("outputFile", outputFile.absolutePath),
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
    private fun captureWithCropByCustom(activity: Activity, outputUri: Uri, useBackCamera: Boolean) {
        this.outputUri = outputUri
        if (Build.VERSION.SDK_INT >= 23) {
            this.tempUri = FileUtils.getTempUri(activity)
        } else {
            this.tempUri = outputUri
        }
        try {
            activity.startActivityForResult(
                Intent(Constants.ACTION.ACTION_CAMERA)
                    .putExtra(
                        Constants.INTENT_KEY.CAMERA_FACING_TYPE,
                        if (useBackCamera) Constants.CAMERA_FACING_TYPE.CAMERA_BACK else Constants.CAMERA_FACING_TYPE.CAMERA_FRONT
                    )
                    .putExtra(Constants.INTENT_KEY.EXTRA_OUTPUT, this.tempUri),
                Constants.USE_CUSTOM_CAMERA_WITH_CROP
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun crop(activity: Activity, targetUri: Uri, outPutUri: Uri) {
        onCrop(activity, targetUri, outPutUri, PhotoUtil.getCropOptions(imgWidth, imgHeight))
    }

    fun onCrop(activity: Activity, targetUri: Uri, outPutUri: Uri, options: CropOptions) {
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

    fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                // 拍照
                Constants.USE_CUSTOM_CAMERA, Constants.USE_CAMERA -> {
                    if (outputUri != null) {
                        val uri = Uri.fromFile(FileUtils.getFileWithUri(context, outputUri))
                        postResult(uri)
                    } else {
                        postResult(Uri.fromFile(cameraTmpFile))
                    }
                }
                //拍照并裁剪
                //                case Constants.USE_CUSTOM_CAMERA_WITH_CROP:
                Constants.USE_CAMERA_WITH_CROP -> {
                    if (outputUri != null) {
                        val uri = Uri.fromFile(File(FileUtils.parseUri(context, outputUri)))
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
                    postResult(outputUri!!)
                    outputUri = null
                } else {
                    postResult(Uri.fromFile(cameraTmpFile))
                    cameraTmpFile = null
                }
                //从相册选择照片并裁剪
                Constants.TAKE_GALLERY_WITH_CROP -> onCrop(activity, data!!.data, cropUri!!, cropOptions!!)
                //从相册选择照片不裁剪
                Constants.TAKE_GALLERY -> {
                    val uri = Uri.fromFile(FileUtils.getFileWithUri(activity, data!!.data!!))
                    postResult(uri)
                }
                //从文件选择照片不裁剪
                Constants.TAKE_FILE -> {
                    val filePath = FileUtils.getFilePathWithDocumentsUri(activity, data!!.data)
                    val uri = Uri.fromFile(File(filePath))
                    postResult(uri)
                }
                //从文件选择照片，并裁剪
                Constants.TAKE_FILE_WITH_CROP -> {
                    onCrop(activity, data!!.data, cropUri!!, cropOptions!!)
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            postResult(Uri.parse(""))
        }
    }

    private fun postResult(uri: Uri) {
        //        Log.e("PhotoHelper Post Uri : ", uri.toString());
        RxBusHelper.post(uri)
    }
}