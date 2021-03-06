package com.findsdk.library.takephoto.util

import android.content.Context
import android.os.Environment
import com.findsdk.library.takephoto.TakePhotoConfig
import com.findsdk.library.takephoto.fileprovider.FileUtil
import java.io.File


/**
 * Created by bvb on 2016/10/26.
 */
internal object StorageUtil {
    /**
     * isSDCardEnable
     *
     * @return
     */
    fun isExternalStorageEnable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun deleteCache(context: Context) {
        val dir = TakePhotoConfig.photoDirectoryName
        val file = FileUtil.getImageDir(context)
        try {
            val f = File(file, dir)
            if (f.exists()) {
                if (f.isDirectory) {
                    val children = f.list()
                    children?.let {
                        for (i in it.indices) {
                            File(f, it[i]).delete()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}