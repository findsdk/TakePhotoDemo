package com.findsdk.library.fileprovider

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.webkit.MimeTypeMap
import java.io.File

/**
 * Created by bvb on 2019/4/9.
 */
internal object MimeTypeUtil {

    /**
     * 获取扩展名
     * @param context Context
     * @param uri Uri
     * @return String
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        var extension: String?
        //Check uri format to avoid null
        if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            //If scheme is a content
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(uri))
            if (TextUtils.isEmpty(extension))
                extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
            if (TextUtils.isEmpty(extension))
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(uri))
        }
        if (TextUtils.isEmpty(extension)) {
            extension = getMimeTypeByFileName(FileUtil.getFileWithUri(context, uri)!!.name)
        }
        return extension
    }

    /**
     * 通过文件名获取扩展名
     * @param fileName String
     * @return String
     */
    private fun getMimeTypeByFileName(fileName: String): String {
        return fileName.substring(fileName.lastIndexOf("."), fileName.length)
    }
}