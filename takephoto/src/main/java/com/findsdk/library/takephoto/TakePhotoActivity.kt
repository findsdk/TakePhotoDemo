package com.findsdk.library.takephoto

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.findsdk.library.rxbus.OnEventListener
import com.findsdk.library.rxbus.RxBusHelper
import com.findsdk.library.takephoto.util.ErrorEvent
import com.findsdk.library.takephoto.util.LocaleUtil
import com.findsdk.library.takephoto.util.PhotoHelper
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by bvb on 2018/12/28.
 */
class TakePhotoActivity : Activity() {

    var compositeDisposable: CompositeDisposable? = null
    var progressDialog: Dialog? = null

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleUtil.initLocale(this)
        initView(savedInstanceState)
        initData()
    }

    private fun initView(savedInstanceState: Bundle?) {
        if (intent != null) {
            val key = intent.getIntExtra(KEY_TYPE, -1)
            val width = intent.getIntExtra(KEY_WIDTH, -1)
            val height = intent.getIntExtra(KEY_HEIGHT, -1)
            when (key) {
                PhotoHelper.REQUEST_CODE_TAKE_PHOTO -> PhotoHelper.getInstance(this@TakePhotoActivity).takePhoto(this@TakePhotoActivity)
                PhotoHelper.REQUEST_CODE_TAKE_PHOTO_WITH_CROP -> PhotoHelper.getInstance(this@TakePhotoActivity).takePhotoWithCrop(
                    this@TakePhotoActivity,
                    width,
                    height
                )
                PhotoHelper.REQUEST_CODE_SELECT_FROM_GALLERY -> PhotoHelper.getInstance(this@TakePhotoActivity).pickFromGallery(
                    this@TakePhotoActivity
                )
                PhotoHelper.REQUEST_CODE_SELECT_FROM_GALLERY_WITH_CROP -> PhotoHelper.getInstance(this@TakePhotoActivity).pickFromGalleryWithCrop(
                    this@TakePhotoActivity,
                    width,
                    height
                )
                PhotoHelper.REQUEST_CODE_SELECT_FROM_FILE -> PhotoHelper.getInstance(this@TakePhotoActivity).pickFromFile(
                    this@TakePhotoActivity
                )
                PhotoHelper.REQUEST_CODE_SELECT_FROM_FILE_WITH_CROP -> PhotoHelper.getInstance(this@TakePhotoActivity).pickFromFileWithCrop(
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

    private fun <U> addRxBusSubscribe(eventType: Class<U>, listener: OnEventListener<U>) {
        if (compositeDisposable == null) {
            compositeDisposable = CompositeDisposable()
        }
        RxBusHelper.doOnThreadMode(eventType, compositeDisposable, listener)
    }

    private fun initData() {
        addRxBusSubscribe(Uri::class.java, object : OnEventListener<Uri>() {
            override fun onEvent(uri: Uri) {
                if (uri != null) {
                    getPhoto(uri)
                } else {
                    finish()
                }
            }

            override fun onError(e: Throwable) {
                finish()
            }
        })
        addRxBusSubscribe(ErrorEvent::class.java, object : OnEventListener<ErrorEvent>() {
            override fun onEvent(uri: ErrorEvent) {
                finish()
            }

            override fun onError(e: Throwable) {
                finish()
            }
        })
    }

    private fun showProgressBar() {
        try {
            if (TakePhotoConfig.dialog != null) {
                progressDialog = TakePhotoConfig.dialog
            }
            if (progressDialog == null)
                progressDialog = Dialog(this, R.style.PhotoModuleProgressDialog)
            progressDialog?.let {
                it.show()
            }
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
        PhotoHelper.getInstance(this).compress(this, uri, object : Observer<Uri> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onNext(value: Uri) {
                val intent = Intent()
                intent.data = value
                setResult(Activity.RESULT_OK, intent)
            }

            override fun onError(e: Throwable) {
                showToast(e.message)
                hideProgressBar()
                finish()
            }

            override fun onComplete() {
                hideProgressBar()
                finish()
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        PhotoHelper.getInstance(this).onActivityResult(this, requestCode, resultCode, intent)
        super.onActivityResult(requestCode, resultCode, intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        PhotoHelper.getInstance(this).onRequestPermissionsResult(this, requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideProgressBar()
        if (compositeDisposable != null) {
            compositeDisposable!!.dispose()
            compositeDisposable = null
        }

    }
}