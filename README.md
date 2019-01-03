# TakePhotoDemo
TakePhotoDemo

TakePhotoActivity.Companion.takePhoto()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                100,
                101, 200, 201, 300, 301 -> {
                    val uri = data.data
                    bitmap = TakePhotoUtil.uri2Bitmap(this, uri)
                    if (bitmap != null) {
                        image1.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }
