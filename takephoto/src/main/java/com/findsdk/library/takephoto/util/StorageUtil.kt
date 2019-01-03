package com.findsdk.library.takephoto.util

import android.content.Context
import android.os.Environment
import android.util.Log
import com.findsdk.library.takephoto.TakePhotoConfig
import java.io.File
import java.nio.file.Files.isDirectory


/**
 * Created by bvb on 2016/10/26.
 */
internal object StorageUtil {
    /**
     * isSDCardEnable
     *
     * @return
     */
    fun isSDCardEnable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun deleteCache(context: Context) {
        val dir = (Environment
            .getExternalStorageDirectory().toString()
                + "/${TakePhotoConfig.photoDirectoryName}/")

        val f = File(dir)
        if (f != null && f.exists()) {
            if (f.isDirectory) {
                val children = f.list()
                for (i in children.indices) {
                    File(f, children[i]).delete()
                }
            }
        }
    }
}