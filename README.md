# TakePhotoDemo

1、拍照、选照片库
包括调用系统相机拍照、剪切、从文件选照片、从相册选照片

    implementation 'com.findsdk.library:takephoto:1.0.6'
    
    //androidx
    implementation 'com.findsdk.library.androidx:takephoto:0.0.1'

2、api:

    fun takePhoto(context: Activity, requestCode: Int)

    fun takePhotoWithCrop(context: Activity, width: Int, height: Int, requestCode: Int)

    fun pickPictureFromGallery(context: Activity, requestCode: Int)

    fun pickPictureFromGalleryWithCrop(context: Activity, width: Int, height: Int, requestCode: Int)

    fun pickPictureFromFile(context: Activity, requestCode: Int)

    fun pickPictureFromFileWithCrop(context: Activity, width: Int, height: Int, requestCode: Int)


3、使用方法：

    方式一（推荐）：
    
    TakePhotoHelper.instance.takePhoto(this) {
          onTakePhoto {
              showImage(it)
          }
     }

    TakePhotoHelper.instance.takePhotoWithCrop(this, width, height) {
                onTakePhoto {
                    showImage(it)
                }
    }
    
    TakePhotoHelper.instance.pickPictureFromGallery(this) {
                onTakePhoto {
                    showImage(it)
                }
     }
     
     TakePhotoHelper.instance.pickPictureFromGalleryWithCrop(this, width, height) {
                onTakePhoto {
                    showImage(it)
                }
     }
    
    TakePhotoHelper.instance.pickPictureFromFile(this) {
                onTakePhoto {
                    showImage(it)
                }
     }
     
     TakePhotoHelper.instance.pickPictureFromFileWithCrop(this, width, height) {
                onTakePhoto {
                    showImage(it)
                }
      }
      
      
   方式二：
   
    private fun takePhoto() {
        TakePhotoActivity.takePhoto(this, 100)
    }

    private fun takePhotoWithCrop() {
        TakePhotoActivity.takePhotoWithCrop(this, width, height, 101)
    }

    private fun pickPictureFromGallery() {
        TakePhotoActivity.pickPictureFromGallery(this, 200)
    }

    private fun pickPictureFromGalleryWithCrop() {
        TakePhotoActivity.pickPictureFromGalleryWithCrop(this, width, height, 201)
    }

    private fun pickPictureFromFile() {
        TakePhotoActivity.pickPictureFromFile(this, 300)
    }

    private fun pickPictureFromFileWithCrop() {
        TakePhotoActivity.pickPictureFromFileWithCrop(this, width, height, 301)
    }


    //获取照片

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                100,101, 200, 201, 300, 301 -> {
                    val uri = data.data
                    bitmap = TakePhotoUtil.uri2Bitmap(this, uri)
                    if (bitmap != null) {
                        image.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }


4、多语言和临时文件目录

    //自定义拍照临时文件目录
    TakePhotoConfig.photoDirectoryName = "tmp"

    //设置多语言
    TakePhotoConfig.languageSetting = "setting"
    TakePhotoConfig.languageDirCreateFailure = "dir create fail"
    TakePhotoConfig.languageNoCamera = "no camera"
    TakePhotoConfig.languageNoSDCard = "no sd card"
    TakePhotoConfig.languageNotImage = "not Image"
    TakePhotoConfig.languageRequestPermissionsCameraTips = "相机权限"

    
    可以在activity退出时 删除拍照的临时文件
    override fun onDestroy() {
        TakePhotoConfig.clearCache(this)
        super.onDestroy()
    }
    
5、混淆

    -keep class com.findsdk.library.**{*;}

6、其他事项

    email：findsdk@gmail.com
