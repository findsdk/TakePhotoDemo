package com.findsdk.library.takephoto.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.findsdk.library.fileprovider.FileUtil
import com.findsdk.library.fileprovider.UriUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by bvb on 2020/7/29.
 */
internal object TpUtil {

    /**
     * 获取裁剪输出URI
     */
    fun getTmpUri(context: Context, prefix: String, suffix: String): Uri {
        val file = getTmpImageFile(context, prefix, suffix)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UriUtil.convertFileUriToFileProviderUri(context, Uri.fromFile(file))
        } else {
            Uri.fromFile(file)
        }

    }

    /**
     * 通过FileProvider获取临时图片文件
     */
    private fun getTmpImageFile(context: Context, prefix: String, suffix: String): File {
        val directory = FileUtil.getImageDir(context)
        return File.createTempFile(
            prefix, /* prefix */
            suffix, /* suffix */
            directory /* directory */
        )
    }

    /**
     * CropOptions
     * @param width Int
     * @param height Int
     * @return CropOptions
     */
    fun getCropOptions(width: Int, height: Int): CropOptions {
        val builder = CropOptions.Builder()
        builder.setAspectX(width).setAspectY(height).setOutputX(width).setOutputY(height)
        return builder.create()
    }

    /**
     * 保存图片文件
     * @param picPath String
     * @param bitmap Bitmap?
     * @return File?
     */
    fun saveBitmapFile(context: Context, bitmap: Bitmap): File? {
        try {
            val prefix = "b_${System.currentTimeMillis()}"
            val suffix = ".jpg"
            val f = getTmpImageFile(context, prefix, suffix)
            FileOutputStream(f).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                it.flush()
            }
            return f
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class)
    fun convertGalleryUriToFileProviderUri(context: Context, uri: Uri): Uri {
        val inputStream = context.contentResolver.openInputStream(uri)
        val prefix = "pg_${System.currentTimeMillis()}"
        val suffix = ".jpg"
        val tmpFile = getTmpImageFile(context, prefix, suffix)
        FileUtil.inputStreamToFile(inputStream, tmpFile)
        return UriUtil.getUriForFile(context, tmpFile)
    }

    fun isImageFile(context: Context, filePath: String?): Boolean {
        if (filePath == null) return false
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        return options.outWidth !== -1
    }

    /**
     * filepath -> bitmap
     * @param context Context
     * @param filePath String
     * @return Bitmap?
     */
    fun path2Bitmap(context: Context, filePath: String): Bitmap? {
        val uri = UriUtil.getUriForFile(context, File(filePath))
        return try {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (e: Exception) {
            null
        }
    }
}