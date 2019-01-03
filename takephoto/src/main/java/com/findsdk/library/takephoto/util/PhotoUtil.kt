package com.findsdk.library.takephoto.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.CameraProfile
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import com.findsdk.library.fileprovider.FileUtils
import com.findsdk.library.takephoto.TakePhotoConfig
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by bvb on 2016/10/26.
 */
internal object PhotoUtil {

    fun getTempUri(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file =
            File(Environment.getExternalStorageDirectory(), "/${TakePhotoConfig.photoDirectoryName}/$timeStamp.jpg")
        if (file != null && file.parentFile != null && !file.parentFile.exists())
            file.parentFile.mkdirs()
        return FileUtils.getUriForFile(context, file)
    }

    fun getTempPath(context: Context): File {
        val dir = (Environment
            .getExternalStorageDirectory().toString()
                + "/${TakePhotoConfig.photoDirectoryName}/")

        val f = File(dir)
        if (f != null && !f.exists()) {
            f.mkdirs()
        }
        return f
    }

    /**
     * 保存图片文件
     * @param picPath String
     * @param bitmap Bitmap?
     * @return File?
     */
    fun saveBitmapFile(picPath: String, bitmap: Bitmap?): File? {
        if (TextUtils.isEmpty(picPath) || bitmap == null) {
            return null
        }
        val f: File
        try {
            f = File(picPath)
            if (f.exists()) {
                f.delete()
            }
            f.createNewFile()
            val fOut = FileOutputStream(f)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()
            return f
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * 将图片转换为字节流
     * @param bmp Bitmap?
     * @return ByteArray?
     */
    fun bmpToByteArray(bmp: Bitmap?): ByteArray? {
        val output = ByteArrayOutputStream()
        var result: ByteArray? = null

        try {
            bmp!!.compress(Bitmap.CompressFormat.PNG, 100, output)
            result = output.toByteArray()
            output.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bmp?.recycle()

            return result
        }
    }

    /**
     *
     * @param filePath String
     * @param cameraId Int
     * @param degree Int
     */
    fun savePictureExif(filePath: String, cameraId: Int, degree: Int) {
        try {
            val exifInterface = ExifInterface(filePath)
            // 修正图片的旋转角度，设置其不旋转。这里也可以设置其旋转的角度，可以传值过去，
            // 例如旋转90度，传值ExifInterface.ORIENTATION_ROTATE_90，需要将这个值转换为String类型的
            var orientation = 1
            if (cameraId == CameraCharacteristics.LENS_FACING_FRONT) {
//            if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                when (degree) {
                    0 -> orientation = ExifInterface.ORIENTATION_ROTATE_180
                    90 -> orientation = ExifInterface.ORIENTATION_ROTATE_270
                    180 -> orientation = ExifInterface.ORIENTATION_NORMAL
                    270 -> orientation = ExifInterface.ORIENTATION_ROTATE_90
                }
            } else {
                when (degree) {
                    0 -> orientation = ExifInterface.ORIENTATION_NORMAL
                    90 -> orientation = ExifInterface.ORIENTATION_ROTATE_90
                    180 -> orientation = ExifInterface.ORIENTATION_ROTATE_180
                    270 -> orientation = ExifInterface.ORIENTATION_ROTATE_270
                }
            }
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
            exifInterface.saveAttributes()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    /**
     * CropOptions
     * @param width Int
     * @param height Int
     * @return CropOptions
     */
    fun getCropOptions(width: Int, height: Int): CropOptions {
        var builder = CropOptions.Builder()
        builder.setAspectX(width).setAspectY(height).setOutputX(width).setOutputY(height)
        return builder.create()
    }

    /**
     * uri -> bitmap
     * @param uri Uri
     * @return Bitmap
     */
    fun uri2Bitmap(context: Context, uri: Uri): Bitmap? {
        var filePath = FileUtils.getFilePathWithUri(context, uri)
        return path2Bitmap(context, filePath)
    }

    /**
     * filepath -> bitmap
     * @param context Context
     * @param filePath String
     * @return Bitmap?
     */
    fun path2Bitmap(context: Context, filePath: String): Bitmap? {
        var uri = FileUtils.getUriForFile(context, File(filePath))
        if (uri != null) {
            return MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            return null
        }
    }

    /**
     * decodeUri
     * @param context Context
     * @param selectedImage Uri
     * @return Bitmap?
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    fun decodeUri(context: Context, selectedImage: Uri): Bitmap? {

        // Decode image size
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(context.contentResolver.openInputStream(selectedImage), null, o)

        // The new size we want to scale to
        val REQUIRED_SIZE = 140

        // Find the correct scale value. It should be the power of 2.
        var width_tmp = o.outWidth
        var height_tmp = o.outHeight
        var scale = 1
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break
            }
            width_tmp /= 2
            height_tmp /= 2
            scale *= 2
        }

        // Decode with inSampleSize
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        return BitmapFactory.decodeStream(context.contentResolver.openInputStream(selectedImage), null, o2)
    }

    /**
     * createScaledBitmap
     * @param bm Bitmap
     * @param newWidth Int
     * @param newHeight Int
     * @return Bitmap
     */
    fun createScaledBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        // 获得图片的宽高
        val width = bm.width
        val height = bm.height
        // 计算缩放比例
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // 取得想要缩放的matrix参数
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        // 得到新的图片
        return Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix,
            true
        )
    }

    /**
     * resource -> bitmap
     * @param context Context
     * @param uri Uri
     * @param reqWidth Int
     * @param reqHeight Int
     * @return Bitmap
     */
    fun decodeSampledBitmapFromResource(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(FileUtils.getFilePathWithUri(context, uri), options)
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(FileUtils.getFilePathWithUri(context, uri), options)
    }

    /**
     *
     * @param options BitmapFactory.Options
     * @param reqWidth Int
     * @param reqHeight Int
     * @return Int
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // 源图片的高度和宽度
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    /**
     * 获取压缩并旋转后的图片
     * @param filePath String
     * @param cameraId Int
     * @return Bitmap?
     */
    fun rotatedBitmap(filePath: String, cameraId: Int): Bitmap? {
        var returnBm: Bitmap? = null
        if (TextUtils.isEmpty(filePath)) {
            return null
        }
        val bm = BitmapFactory.decodeFile(filePath)
        val degree = readPictureDegree(filePath)

        // 根据旋转角度，生成旋转矩阵
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        //        //如果使用的是前置摄像头，需要翻转
        if (cameraId == CameraCharacteristics.LENS_FACING_FRONT) {
//        if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            matrix.postScale(-1f, 1f)
        }
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        if (returnBm == null) {
            returnBm = bm
        }
        if (bm != returnBm) {
            bm.recycle()
        }
        return returnBm
    }

    /**
     * 读取图片旋转角度
     * @param path String
     * @return Int
     */
    private fun readPictureDegree(path: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        return degree
    }

    /**
     * 获取压缩并旋转后的图片
     * @param filePath String
     * @param maxSize Long
     * @param reqWidth Int
     * @param reqHeight Int
     * @return Bitmap?
     */
    fun getRotatedBitmap(filePath: String, maxSize: Long, reqWidth: Int, reqHeight: Int): Bitmap? {
        var returnBm: Bitmap? = null
        if (TextUtils.isEmpty(filePath)) {
            return null
        }
        val bm = getBitmapAfterCompress(filePath, maxSize, reqWidth, reqHeight)
            ?: return null
        val degree = readPictureDegree(filePath)
        // 根据旋转角度，生成旋转矩阵
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm!!, 0, 0, bm!!.getWidth(), bm!!.getHeight(), matrix, true)
        } catch (e: Exception) {
        }

        if (returnBm == null) {
            returnBm = bm
        }
        if (bm != returnBm) {
            bm!!.recycle()
        }
        return returnBm
    }


    /**
     * 获取压缩后的图片
     * @param filePath String
     * @param maxSize Long
     * @param reqWidth Int
     * @param reqHeight Int
     * @return Bitmap?
     */
    fun getBitmapAfterCompress(filePath: String, maxSize: Long, reqWidth: Int, reqHeight: Int): Bitmap? {
        var resultBitmap: Bitmap? = null
        var outputStream: ByteArrayOutputStream? = null
        var inputStream: ByteArrayInputStream? = null

        val options = BitmapFactory.Options()
        var bitmap: Bitmap? = null

        try {
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            options.inJustDecodeBounds = false
            bitmap = BitmapFactory.decodeFile(filePath, options)

            // output
            outputStream = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)//如果签名是png的话，则不管quality是多少，都不会进行质量的压缩
            var quality = 100
            while (outputStream.toByteArray().size > maxSize) {
                quality -= 16
                outputStream.reset()
                if (quality <= 0) {
                    break
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }
            inputStream = ByteArrayInputStream(outputStream.toByteArray())
            resultBitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: OutOfMemoryError) {
        } finally {
            bitmap?.recycle()
            try {
                outputStream?.close()
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return resultBitmap
        }
    }

    /**
     * 获取压缩后的图片
     * @param oriBitmap Bitmap
     * @param maxSize Long
     * @return ByteArray
     */
    fun getBitmapAfterCompress(oriBitmap: Bitmap, maxSize: Long): ByteArray {
        var bitmapBytes = ByteArray(0)
        //Bitmap resultBitmap = null;
        var outputStream: ByteArrayOutputStream? = null
        val inputStream: ByteArrayInputStream? = null

        try {

            // output
            outputStream = ByteArrayOutputStream()
            oriBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)//如果签名是png的话，则不管quality是多少，都不会进行质量的压缩
            var quality = 100
            while (outputStream.toByteArray().size > maxSize) {
                quality -= 16
                outputStream.reset()
                if (quality <= 0) {
                    break
                }
                oriBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }
            bitmapBytes = outputStream.toByteArray()
            //            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            //            resultBitmap = BitmapFactory.decodeStream(inputStream, null, null);
        } catch (e: OutOfMemoryError) {
        } finally {
            oriBitmap.recycle()
            try {
                outputStream?.close()
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            //return resultBitmap;
            return bitmapBytes
        }
    }


}