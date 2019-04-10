package com.findsdk.library.takephoto

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.findsdk.library.fileprovider.FileUtil
import com.findsdk.library.takephoto.util.PhotoUtil

/**
 * Created by bvb on 2018/12/28.
 */
object TakePhotoUtil {
    /**
     * uri -> bitmap
     * @param context Context
     * @param uri Uri
     * @return Bitmap?
     */
    fun uri2Bitmap(context: Context, uri: Uri): Bitmap? {
        val filePath = FileUtil.getFilePathWithUri(context, uri)
        return PhotoUtil.path2Bitmap(context, filePath!!)

    }

    /**
     * uri -> filePath
     * @param context Context
     * @param uri Uri
     * @return String?
     */
    fun uri2FilePath(context: Context, uri: Uri): String? {
        return FileUtil.getFilePathWithUri(context, uri)
    }


}