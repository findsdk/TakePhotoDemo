package com.findsdk.demo.takephoto

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.DialogCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.fragment.app.DialogFragment

import com.findsdk.library.takephoto.TakePhotoActivity
import com.findsdk.library.takephoto.TakePhotoConfig
import com.findsdk.library.takephoto.TakePhotoHelper
import com.findsdk.library.takephoto.TakePhotoUtil
import com.findsdk.library.takephoto.util.DialogUtil
import com.venus.library.permission.PermissionManager
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.security.AccessController.getContext


class MainActivity : AppCompatActivity() {

    var bitmap: Bitmap? = null
    val width = 400
    val height = 300
    var useResultListener = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        takephoto.setOnClickListener {
            takePhoto()
//            test()
        }
        takephoto_crop.setOnClickListener {
//            takePhotoWithCrop()
            test()
        }

        pick_from_gallery.setOnClickListener {
            pickPictureFromGallery()
        }
        pick_from_gallery_crop.setOnClickListener {
            pickPictureFromGalleryWithCrop()
        }

        pick_from_file.setOnClickListener {
            pickPictureFromFile()
        }
        pick_from_file_crop.setOnClickListener {
            pickPictureFromFileWithCrop()
        }
        delete_tmp_dir.setOnClickListener {
            TakePhotoConfig.clearCache(this)
        }
//        val dialog = AlertDialog.Builder(this).setMessage("loading").create()
//        TakePhotoConfig.dialog = dialog
        TakePhotoConfig.photoDirectoryName = "tmp"
        TakePhotoConfig.languageSetting = "setting"
        TakePhotoConfig.languageDirCreateFailure = "dir create fail"
        TakePhotoConfig.languageNoCamera = "no camera"
        TakePhotoConfig.languageExternalStorageDisable = "no sd card"
        TakePhotoConfig.languageNotImage = "not image"
        TakePhotoConfig.languageRequestPermissionsCameraTips = "相机权限"
    }

    private fun takePhoto() {
        if (useResultListener) {
            TakePhotoHelper.instance.takePhoto(this) {
                onTakePhoto {
                    showImage(it)
                }
            }
        } else {
            TakePhotoActivity.takePhoto(this, 100)
        }
    }

    private fun takePhotoWithCrop() {
        if (useResultListener) {
            TakePhotoHelper.instance.takePhotoWithCrop(this, width, height) {
                onTakePhoto {
                    showImage(it)
                }
            }
        } else {
            TakePhotoActivity.takePhotoWithCrop(this, width, height, 101)
        }
    }

    private fun pickPictureFromGallery() {
        if (useResultListener) {
            TakePhotoHelper.instance.pickPictureFromGallery(this) {
                onTakePhoto {
                    showImage(it)
                }
            }
        } else {
            TakePhotoActivity.pickPictureFromGallery(this, 200)
        }
    }

    private fun pickPictureFromGalleryWithCrop() {
        if (useResultListener) {
            TakePhotoHelper.instance.pickPictureFromGalleryWithCrop(this, width, height) {
                onTakePhoto {
                    showImage(it)
                }
            }
        } else {
            TakePhotoActivity.pickPictureFromGalleryWithCrop(this, width, height, 201)
        }
    }

    private fun pickPictureFromFile() {
        if (useResultListener) {
            TakePhotoHelper.instance.pickPictureFromFile(this) {
                onTakePhoto {
                    showImage(it)
                }
            }
        } else {
            TakePhotoActivity.pickPictureFromFile(this, 300)
        }
    }

    private fun pickPictureFromFileWithCrop() {
        if (useResultListener) {
            TakePhotoHelper.instance.pickPictureFromFileWithCrop(this, width, height) {
                onTakePhoto {
                    showImage(it)
                }
            }
        } else {
            TakePhotoActivity.pickPictureFromFileWithCrop(this, width, height, 301)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                100,
                101, 200, 201, 300, 301 -> {
                    val uri = data.data
                    uri?.let { showImage(it) }
                }
            }
        }
    }

    override fun onDestroy() {
        TakePhotoConfig.clearCache(this)
        super.onDestroy()
    }

    private fun showImage(uri: Uri) {
        TakePhotoUtil.uri2Bitmap(this@MainActivity, uri)?.let {
            image1.setImageBitmap(it)
        }
    }

    private fun test() {

        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        PermissionManager.instance.requestPermission(this,permissions){
            onGranted {
                Log.e("===","===onGranted===")
            }
            onDenied {
                Log.e("===","===onDenied===")
            }
        }
    }
}
