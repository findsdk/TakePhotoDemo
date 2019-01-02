package com.findsdk.library.takephoto.util

import android.os.Environment

/**
 * Created by bvb on 2016/10/26.
 */
internal object SDCardUtil {
    /**
     * isSDCardEnable
     *
     * @return
     */
    fun isSDCardEnable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}