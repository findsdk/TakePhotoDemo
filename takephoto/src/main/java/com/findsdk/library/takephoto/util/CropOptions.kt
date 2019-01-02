package com.findsdk.library.takephoto.util

/**
 * Created by bvb on 2016/10/26.
 */
internal class CropOptions {
    var aspectX: Int = 0

    var aspectY: Int = 0

    var outputX: Int = 0

    var outputY: Int = 0

    constructor()

    class Builder {
        private val options: CropOptions

        init {
            options = CropOptions()
        }

        fun setAspectX(aspectX: Int): Builder {
            options.aspectX = aspectX
            return this
        }

        fun setAspectY(aspectY: Int): Builder {
            options.aspectY = aspectY
            return this
        }

        fun setOutputX(outputX: Int): Builder {
            options.outputX = outputX
            return this
        }

        fun setOutputY(outputY: Int): Builder {
            options.outputY = outputY
            return this
        }

        fun create(): CropOptions {
            return options
        }
    }
}