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

        /**
         * takePhoto
         * @param context Activity
         * @param requestCode Int
         */
        fun takePhoto(context: Activity, requestCode: Int) {
            val intent = Intent(context, TakePhotoActivity::class.java)
            intent.putExtra(KEY_TYPE, PhotoHelper.REQUEST_CODE_TAKE_PHOTO)
            context.startActivityForResult(intent, requestCode)
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
            context.startActivityForResult(intent, requestCode)
        }

        /**
         * pickPictureFromGallery
         * @param context Activity
         * @param requestCode Int
         */
        fun pickPictureFromGallery(context: Activity, requestCode: Int) {
            val intent = Intent(context, TakePhotoActivity::class.java)
            intent.putExtra(KEY_TYPE, PhotoHelper.REQUEST_CODE_SELECT_FROM_GALLERY)
            context.startActivityForResult(intent, requestCode)
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
            context.startActivityForResult(intent, requestCode)
        }

        /**
         * pickPictureFromFile
         * @param context Activity
         * @param requestCode Int
         */
        fun pickPictureFromFile(context: Activity, requestCode: Int) {
            val intent = Intent(context, TakePhotoActivity::class.java)
            intent.putExtra(KEY_TYPE, PhotoHelper.REQUEST_CODE_SELECT_FROM_FILE)
            context.startActivityForResult(intent, requestCode)
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
            context.startActivityForResult(intent, requestCode)
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
            if (TakePhotoConfig.dialog != null) {
                progressDialog = TakePhotoConfig.dialog
            }
            if (progressDialog == null)
                progressDialog = Dialog(this, R.style.PhotoModuleProgressDialog)
            progressDialog?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun hideProgressBar() {
        if (progressDialog != null) {
            if (progressDialog!!.isShowing) progressDialog!!.dismiss()
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
                Constants.ACTION_PHOTO_RESULT -> {
                    val uriString = intent.getStringExtra("uri")
                    if (!TextUtils.isEmpty(uriString)) {
                        val uri = Uri.parse(uriString)
                        if (uri != null) {
                            getPhoto(uri)
                            return
                        }
                    }
                    val message = intent.getStringExtra("message")
                    showError(message)
                    finish()


                }
                Constants.ACTION_PHOTO_COMPRESS -> {
                    val uriString = intent.getStringExtra("uri")
                    if (!TextUtils.isEmpty(uriString)) {
                        val uri = Uri.parse(uriString)
                        if (uri != null) {
                            val result = Intent()
                            result.data = uri
                            setResult(Activity.RESULT_OK, result)
                            finish()
                            return
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