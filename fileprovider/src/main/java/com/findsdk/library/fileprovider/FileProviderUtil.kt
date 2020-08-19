package com.findsdk.library.fileprovider

import android.content.Context

/**
 * Created by bvb on 2019/4/9.
 */
internal object FileProviderUtil {
    const val IMAGES_PATH = "tp_images"

    fun getFileProviderName(context: Context): String {
        return context.packageName + ".fileprovider"
    }

}