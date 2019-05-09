package com.findsdk.library.takephoto

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.text.TextUtils
import android.widget.Toast
import com.findsdk.library.takephoto.util.Constants
import com.findsdk.library.takephoto.util.LocaleUtil
import com.findsdk.library.takephoto.util.PhotoHelper

/**
 * Created by bvb on 2018/12/28.
 */
class TakePhotoActivity : Activity() {

    private var progressDialog: Dialog? = null

    companion object {
        private const val KEY_TYPE = "type"
        private const val KEY_WIDTH = "width"
        private const val KEY_HEIGHT = "height"
        private const val DEFAULT_REQUEST_CODE = -1

        /**
         * takePhoto
         * @param context Activity
         * @param requestCode Int
         */
        fun takePhoto(context: Activity, requestCode: Int) {
            val intent = Intent(context, TakePhotoActivity::class.java)
            intent.putExtra(KEY_TYPE, PhotoHelper.REQUEST_CODE_TAKE_PHOTO)
            startActivity(context, intent, requestCode)
        }

        internal fun takePhoto(context: Activity) {
            takePhoto(context, DEFAULT_REQUEST_CODE)
        }

        /**
         * takePhotoWithCrop
         * @param context Activity
         * @param width Int
         * @param height Int
         * @param requestCode Int
         */
        fun takePhotoWithCrop(context: Activity, width: Int, height: Int, requestCode: Int) {
            val intent = Intent(context, TakePhotoActivity::class.java)
            intent.putExtra(KEY_TYPE, PhotoHelper.REQUEST_CODE_TAKE_PHOTO_WITH_CROP)
            intent.putExtra(KEY_WIDTH, width)
            intent.putExtra(KEY_HEIGHT, height)
            startActivity(context, intent, requestCode)
        }

        internal fun takePhotoWithCrop(context: Activity, width: Int, height: Int) {
            takePhotoWithCrop(context, width, height, DEFAULT_REQUEST_CODE)
        }

        /**
         * pickPictureFromGallery
         * @param context Activity
         * @param requestCode Int
         */
        fun pickPictureFromGallery(context: Activity, requestCode: Int) {
            val intent = Intent(context, TakePhotoActivity::class.java)
            intent.putExtra(KEY_TYPE, PhotoHelper.REQUEST_CODE_SELECT_FROM_GALLERY)
            startActivity(context, intent, requestCode)
        }

        internal fun pickPictureFromGallery(context: Activity) {
            pickPictureFromGallery(context, DEFAULT_REQUEST_CODE)
        }

        /**
         * pickPictureFromGalleryWithCrop
         * @param context Activity
         * @param width Int
         * @param height Int
         * @param requestCode Int
         */
        fun pickPictureFromGalleryWithCrop(context: Activity, width: Int, height: Int, requestCode: Int) {
            val intent = Intent(context, TakePhotoActivity::class.java)
            intent.putExtra(KEY_TYPE, PhotoHelper.REQUEST_CODE_SELECT_FROM_GALLERY_WITH_CROP)
            intent.putExtra(KEY_WIDTH, width)
            intent.putExtra(KEY_HEIGHT, height)
            startActivity(context, intent, requestCode)
        }

        internal fun pickPictureFromGalleryWithCrop(context: Activity, width: Int, height: Int) {
            pickPictureFromGalleryWithCrop(context, width, height, DEFAULT_REQUEST_CODE)
        }

        /**
         * pickPictureFromFile
         * @param context Activity
         * @param requestCode Int
         */
        fun pickPictureFromFile(context: Activity, requestCode: Int) {
            val intent = Intent(context, TakePhotoActivity::class.java)
            intent.putExtra(KEY_TYPE, PhotoHelper.REQUEST_CODE_SELECT_FROM_FILE)
            startActivity(context, intent, requestCode)
        }

        internal fun pickPictureFromFile(context: Activity) {
            pickPictureFromFile(context, DEFAULT_REQUEST_CODE)
        }

        /**
         * pickPictureFromFileWithCrop
         * @param context Activity
         * @param width Int
         * @param height Int
         * @param requestCode Int
         */
        fun pickPictureFromFileWithCrop(context: Activity, width: Int, height: Int, requestCode: Int) {
            val intent = Intent(context, TakePhotoActivity::class.java)
            intent.putExtra(KEY_TYPE, PhotoHelper.REQUEST_CODE_SELECT_FROM_FILE_WITH_CROP)
            intent.putExtra(KEY_WIDTH, width)
            intent.putExtra(KEY_HEIGHT, height)
            startActivity(context, intent, requestCode)
        }

        internal fun pickPictureFromFileWithCrop(context: Activity, width: Int, height: Int) {
            pickPictureFromFileWithCrop(context, width, height, DEFAULT_REQUEST_CODE)
        }

        fun takePhotoUseCustomCamera(context: Activity, requestCode: Int, cameraType: Int) {
            val intent = Intent(context, TakePhotoActivity::class.java)
            intent.putExtra(KEY_TYPE, PhotoHelper.REQUEST_CODE_TAKE_CUSTOM_PHOTO)
            intent.putExtra(Constants.INTENT_KEY.CAMERA_FACING_TYPE, cameraType)
            startActivity(context, intent, requestCode)
        }

        internal fun takePhotoUseCustomCamera(context: Activity, cameraType: Int) {
            takePhotoUseCustomCamera(context, DEFAULT_REQUEST_CODE, cameraType)
        }

        fun takePhotoUseCustomCameraWithCrop(
            context: Activity,
            width: Int,
            height: Int,
            requestCode: Int,
            cameraType: Int
        ) {
            val intent = Intent(context, TakePhotoActivity::class.java)
            intent.putExtra(KEY_TYPE, PhotoHelper.REQUEST_CODE_TAKE_CUSTOM_PHOTO_WITH_CROP)
            intent.putExtra(KEY_WIDTH, width)
            intent.putExtra(KEY_HEIGHT, height)
            intent.putExtra(Constants.INTENT_KEY.CAMERA_FACING_TYPE, cameraType)
            startActivity(context, intent, requestCode)
        }

        internal fun takePhotoUseCustomCameraWithCrop(context: Activity, width: Int, height: Int, cameraType: Int) {
            takePhotoUseCustomCameraWithCrop(context, width, height, DEFAULT_REQUEST_CODE, cameraType)
        }

        private fun startActivity(context: Activity, intent: Intent, requestCode: Int = DEFAULT_REQUEST_CODE) {
            if (requestCode == DEFAULT_REQUEST_CODE) {
                context.startActivity(intent)
            } else {
                context.startActivityForResult(intent, requestCode)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        register()
        LocaleUtil.initLocale(this)
        initView()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initView() {
        if (intent != null) {
            val key = intent.getIntExtra(KEY_TYPE, -1)
            val width = intent.getIntExtra(KEY_WIDTH, -1)
            val height = intent.getIntExtra(KEY_HEIGHT, -1)
            when (key) {
                PhotoHelper.REQUEST_CODE_TAKE_PHOTO -> PhotoHelper.takePhoto(this@TakePhotoActivity)
                PhotoHelper.REQUEST_CODE_TAKE_PHOTO_WITH_CROP -> PhotoHelper.takePhotoWithCrop(
                    this@TakePhotoActivity,
                    width,
                    height
                )
                PhotoHelper.REQUEST_CODE_SELECT_FROM_GALLERY -> PhotoHelper.pickFromGallery(
                    this@TakePhotoActivity
                )
                PhotoHelper.REQUEST_CODE_SELECT_FROM_GALLERY_WITH_CROP -> PhotoHelper.pickFromGalleryWithCrop(
                    this@TakePhotoActivity,
                    width,
                    height
                )
                PhotoHelper.REQUEST_CODE_SELECT_FROM_FILE -> PhotoHelper.pickFromFile(
                    this@TakePhotoActivity
                )
                PhotoHelper.REQUEST_CODE_SELECT_FROM_FILE_WITH_CROP -> PhotoHelper.pickFromFileWithCrop(
                    this@TakePhotoActivity,
                    width,
                    height
                )
                PhotoHelper.REQUEST_CODE_TAKE_CUSTOM_PHOTO -> {

                    PhotoHelper.takeCustomPhoto(
                        this@TakePhotoActivity,
                        intent.getIntExtra(
                            Constants.INTENT_KEY.CAMERA_FACING_TYPE,
                            Constants.CAMERA_FACING_TYPE.CAMERA_BACK
                        )
                    )
                }
                else -> finish()

            }
        } else {
            finish()
        }
    }

    private fun register() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.ACTION_PHOTO_RESULT)
        intentFilter.addAction(Constants.ACTION_PHOTO_COMPRESS)
        registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun unRegister() {
        unregisterReceiver(broadcastReceiver)
    }

    private fun showProgressBar() {
        try {
            TakePhotoConfig.dialog?.let { progressDialog = it }
            progressDialog ?: Dialog(this, R.style.PhotoModuleProgressDialog).also {
                progressDialog = it
            }
            progressDialog?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {
        progressDialog?.run {
            if (this.isShowing) this.dismiss()
            progressDialog = null
        }
    }

    private fun showToast(text: String?) {
        if (!TextUtils.isEmpty(text))
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun getPhoto(uri: Uri) {
        showProgressBar()
        PhotoHelper.compress(this, uri)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        PhotoHelper.onActivityResult(this, requestCode, resultCode, intent)
        super.onActivityResult(requestCode, resultCode, intent)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        PhotoHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideProgressBar()
        unRegister()
    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Constants.ACTION_PHOTO_RESULT -> run {
                    val uriString = intent.getStringExtra("uri")
                    if (!TextUtils.isEmpty(uriString)) {
                        Uri.parse(uriString)?.let {
                            getPhoto(it)
                            return@run
                        }
                    }
                    val message = intent.getStringExtra("message")
                    showError(message)
                    finish()
                }
                Constants.ACTION_PHOTO_COMPRESS -> run {
                    val uriString = intent.getStringExtra("uri")
                    if (!TextUtils.isEmpty(uriString)) {
                        Uri.parse(uriString)?.let {
                            val result = Intent()
                            result.data = it
                            setResult(Activity.RESULT_OK, result)
                            TakePhotoHelper.instance.updateResult(it)
                            finish()
                            return@run
                        }
                    }
                    val message = intent.getStringExtra("message")
                    message?.let {
                        showToast(it)
                    }
                    finish()
                }
            }

        }
    }

    private fun showError(message: String?) {
        message?.let {
            showToast(it)
        }
    }
}