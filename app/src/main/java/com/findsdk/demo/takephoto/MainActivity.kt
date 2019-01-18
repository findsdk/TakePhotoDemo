package com.findsdk.demo.takephoto

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.findsdk.library.takephoto.TakePhotoActivity
import com.findsdk.library.takephoto.TakePhotoConfig
import com.findsdk.library.takephoto.TakePhotoUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var bitmap: Bitmap? = null
    val width = 300
    val height = 300
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        takephoto.setOnClickListener {
            takePhoto()
        }
        takephoto_crop.setOnClickListener {
            takePhotoWithCrop()
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

        TakePhotoConfig.photoDirectoryName = "tmp"
        TakePhotoConfig.languageSetting = "setting"
        TakePhotoConfig.languageDirCreateFailure = "dir create fail"
        TakePhotoConfig.languageNoCamera = "no camera"
        TakePhotoConfig.languageNoSDCard = "no sd card"
        TakePhotoConfig.languageNotImage = "not image"
        TakePhotoConfig.languageRequestPermissionsCameraTips = "相机权限"
        TakePhotoConfig.languageRequestPermissionsExternalStorageTips = "文件权限"
    }

    private fun takePhoto() {
        TakePhotoActivity.takePhoto(this, 100)
    }

    private fun takePhotoWithCrop() {
        TakePhotoActivity.takePhotoWithCrop(this, width, height, 101)
    }

    private fun pickPictureFromGallery() {
        TakePhotoActivity.pickPictureFromGallery(this, 200)
    }

    private fun pickPictureFromGalleryWithCrop() {
        TakePhotoActivity.pickPictureFromGalleryWithCrop(this, width, height, 201)
    }

    private fun pickPictureFromFile() {
        TakePhotoActivity.pickPictureFromFile(this, 300)
    }

    private fun pickPictureFromFileWithCrop() {
        TakePhotoActivity.pickPictureFromFileWithCrop(this, width, height, 301)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                100,
                101, 200, 201, 300, 301 -> {
                    val uri = data.data
                    Log.e("===", "===uri ${uri}")
                    bitmap = TakePhotoUtil.uri2Bitmap(this, uri)
                    if (bitmap != null) {
                        image1.setImageBitmap(bitmap)
                    }
                }
//                200, 201,300, 301 -> {
//                    val path = data.getStringExtra("file")
//                    val bitmap = PhotoUtil.path2Bitmap(this@MainActivity, path)
//                    if (bitmap != null) {
//                        image1.setImageBitmap(bitmap)
//                    }
//                }
            }
        }
    }

    override fun onDestroy() {
        TakePhotoConfig.clearCache(this)
        super.onDestroy()
    }
}
