package com.findsdk.library.takephoto.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.text.TextUtils
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.roundToInt

/**
 * Created by bvb on 2020/7/29.
 */
object CompressUtil {

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
        val bm = getBitmapAfterCompress(filePath, maxSize, reqWidth, reqHeight) ?: return null
        val degree = readPictureDegree(filePath)
        // 根据旋转角度，生成旋转矩阵
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
        } catch (e: Exception) {
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
     * 获取压缩后的图片
     * @param filePath String
     * @param maxSize Long
     * @param reqWidth Int
     * @param reqHeight Int
     * @return Bitmap?
     */
    private fun getBitmapAfterCompress(filePath: String, maxSize: Long, reqWidth: Int, reqHeight: Int): Bitmap? {
        var resultBitmap: Bitmap? = null
        val options = BitmapFactory.Options()
        val bitmap = BitmapFactory.decodeFile(filePath, options)

        try {
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            // output
            ByteArrayOutputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)//如果签名是png的话，则不管quality是多少，都不会进行质量的压缩
                var quality = 100
                while (output.toByteArray().size > maxSize) {
                    quality -= 16
                    output.reset()
                    if (quality <= 0) {
                        break
                    }
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
                }
                ByteArrayInputStream(output.toByteArray()).use { inputStream ->
                    resultBitmap = BitmapFactory.decodeStream(inputStream)
                }
            }
        } catch (e: OutOfMemoryError) {
        } finally {
            bitmap?.recycle()
            return resultBitmap
        }
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
            val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }
}