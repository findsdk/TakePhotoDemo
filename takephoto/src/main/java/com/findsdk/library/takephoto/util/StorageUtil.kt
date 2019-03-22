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
        try {
            val f = File(dir)
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
        }catch (e:Exception){}
    }
}