package com.findsdk.library.takephoto.util

import android.app.Activity
import android.widget.Toast

/**
 * Created by bvb on 2020/7/29.
 */

fun Activity.toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()