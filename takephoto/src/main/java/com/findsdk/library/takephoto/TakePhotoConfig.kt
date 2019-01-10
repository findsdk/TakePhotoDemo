package com.findsdk.library.takephoto

import android.app.Dialog
import android.content.Context
import com.findsdk.library.takephoto.util.StorageUtil

/**
 * Created by bvb on 2018/12/27.
 */
object TakePhotoConfig {

    var photoDirectoryName: String = ""

    var languageSetting: String = ""

    var languageRequestPermissionsExternalStorageTips: String = ""

    var languageRequestPermissionsCameraTips: String = ""

    var languageNoSDCard: String = ""

    var languageDirCreateFailure: String = ""

    var languageNoCamera: String = ""

    var languageNotImage: String = ""

    var dialog: Dialog? = null

    fun clearCache(context: Context) {
        if (photoDirectoryName.isNotEmpty()) {
            StorageUtil.deleteCache(context)
        }
    }

}