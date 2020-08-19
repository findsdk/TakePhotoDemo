package com.findsdk.library.fileprovider

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by bvb on 2019/4/9.
 */
object UriUtil {
    const val TAG = "FileProvider.UriUtil"

    /**
     * 获取一个临时的Uri, 文件名随机生成
     * @param context Context
     * @return Uri
     */
    fun getTempUri(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.externalCacheDir, "/images/$timeStamp")
        if (!file.parentFile.exists())
            file.parentFile.mkdirs()
        return getUriForFile(context, file)
    }

    /**
     * 获取URI path
     * @param context Context
     * @param uri Uri
     * @return String
     */
    fun parseUri(context: Context, uri: Uri): String? {
        return if (TextUtils.equals(uri.authority, FileProviderUtil.getFileProviderName(context))) {
            File(uri.path!!.replace("file_path/", "")).absolutePath
        } else {
            uri.path
        }
    }

    /**
     * uri from file
     * @param context Context
     * @param file File
     * @return Uri
     */
    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, FileProviderUtil.getFileProviderName(context), file)
    }

    /**
     * AAAAAAAAAAAAA
     * 将scheme为file的uri转成FileProvider 提供的content uri
     * @param context Context
     * @param uri Uri
     * @return Uri
     */
    fun convertFileUriToFileProviderUri(context: Context, uri: Uri): Uri {
        return if (ContentResolver.SCHEME_FILE == uri.scheme) {
            getUriForFile(context, File(uri.path))
        } else uri

    }

    /**
     * 将Uri转换为绝对路径，适用于4.4以下版本
     * @param context Context
     * @param uri Uri
     * @return String
     */
    fun convertUriToPathBelowKitKat(context: Context, uri: Uri): String {
        var currentImagePath = ""
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                currentImagePath = cursor.getString(index)
            }
        } catch (e: IllegalArgumentException) {

        } finally {
            cursor?.close()
        }

        return currentImagePath
    }

    /**
     * 将Uri转换为绝对路径，适用于4.4及以上版本
     *
     * @param context, uri
     * @return String
     */

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun convertUriToPath(context: Context, uri: Uri): String? {

        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (FileUtil.isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (FileUtil.isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))

                return FileUtil.getDataColumn(context, contentUri, null, null)
            } else if (FileUtil.isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return FileUtil.getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {

            // Return the remote address
            return if (FileUtil.isGooglePhotosUri(uri)) uri.lastPathSegment else FileUtil.getDataColumn(context, uri, null, null)

        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }
}