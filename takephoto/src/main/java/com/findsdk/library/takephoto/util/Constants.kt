package com.findsdk.library.takephoto.util

/**
 * Created by bvb on 2016/10/26.
 */
internal object Constants {


    /**
     * 裁剪结果
     */
    const val CROP_RESULT = 1

    /**
     * 拍照
     */
    const val TAKE_PHOTO = CROP_RESULT + 1

    /**
     * 拍照并裁剪
     */
    const val TAKE_PHOTO_WITH_CROP = TAKE_PHOTO + 1

    /**
     * 从相册选择
     */
    const val PICK_FROM_GALLERY = TAKE_PHOTO_WITH_CROP + 1

    /**
     * 从相册选择并裁剪
     */
    const val PICK_FROM_GALLERY_WITH_CROP = PICK_FROM_GALLERY + 1

    /**
     * 从文件中选择
     */
    const val PICK_FROM_FILE = PICK_FROM_GALLERY_WITH_CROP + 1

    /**
     * 从文件中选择并裁剪
     */
    const val PICK_FROM_FILE_WITH_CROP = PICK_FROM_FILE + 1

    /**
     * 使用自定义相机
     */
    const val TAKE_PHOTO_USE_CUSTOM_CAMERA = PICK_FROM_FILE_WITH_CROP + 1

    /**
     * 使用自定义相机并裁剪
     */
    const val TAKE_PHOTO_USE_CUSTOM_CAMERA_WITH_CROP = TAKE_PHOTO_USE_CUSTOM_CAMERA + 1


    /**
     * 广播action
     */
    const val ACTION_PHOTO_RESULT = "com.findsdk.library.takephoto.result"

    const val ACTION_PHOTO_COMPRESS = "com.findsdk.library.takephoto.compress"

    /**
     * 图片尺寸
     */
    interface IMAGE_SIZE {
        companion object {
            const val UPLOAD_MAX_SIZE = (512 * 1024).toLong()
            const val UPLOAD_MAX_HEIGHT = 800
            const val UPLOAD_MAX_WIDTH = 600
        }
    }

    /**
     * intent key
     */
    interface INTENT_KEY {
        companion object {
            const val CAMERA_FACING_TYPE = "camera_facing_type"
            const val EXTRA_OUTPUT = "output"

            const val ERROR_MESSAGE = "error_message"
        }
    }

    /**
     * 摄像头类型，包括后置摄像头和前置摄像头
     */
    interface CAMERA_FACING_TYPE {
        companion object {
            const val CAMERA_BACK = 0
            const val CAMERA_FRONT = 1
        }
    }

}