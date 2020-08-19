package com.findsdk.library.takephoto.util

import android.content.Context
import com.findsdk.library.takephoto.R
import com.findsdk.library.takephoto.TakePhotoConfig

/**
 * Created by bvb on 2019/1/2.
 */
internal object LocaleUtil {

    fun initLocale(context: Context) {
        if (TakePhotoConfig.languageSetting.isEmpty()) {
            TakePhotoConfig.languageSetting = context.getString(R.string.setting)
        }

        if (TakePhotoConfig.languageRequestPermissionsCameraTips.isEmpty()) {
            TakePhotoConfig.languageRequestPermissionsCameraTips =
                    context.getString(R.string.request_permissions_camera_tips)
        }
        if (TakePhotoConfig.languageExternalStorageDisable.isEmpty()) {
            TakePhotoConfig.languageExternalStorageDisable = context.getString(R.string.no_sd_card)
        }
        if (TakePhotoConfig.languageDirCreateFailure.isEmpty()) {
            TakePhotoConfig.languageDirCreateFailure = context.getString(R.string.dir_create_failure)
        }

        if (TakePhotoConfig.languageNoCamera.isEmpty()) {
            TakePhotoConfig.languageNoCamera = context.getString(R.string.no_camera)
        }

        if (TakePhotoConfig.languageNotImage.isEmpty()) {
            TakePhotoConfig.languageNotImage = context.getString(R.string.not_image)
        }

        if (TakePhotoConfig.photoDirectoryName.isEmpty()) {
            TakePhotoConfig.photoDirectoryName = "tmp_images"
        }
    }

}