@file:Suppress("unused")

package com.velentium.android.platformv.uicontrols.ext

import android.content.res.ColorStateList
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun ImageView.makeGrayScale() {
    val matrix = ColorMatrix()
    matrix.setSaturation(0f)
    colorFilter = ColorMatrixColorFilter(matrix)
}

fun ImageView.clearGrayScale() {
    colorFilter = null
}

fun ImageView.tintImage(@ColorInt tintColor: Int) {
    imageTintMode = PorterDuff.Mode.SRC_ATOP
    imageTintList = ColorStateList.valueOf(tintColor)
}

fun ImageView.tintImageWithColor(@ColorRes tintColorResId: Int) {
    tintImage(ContextCompat.getColor(context, tintColorResId))
}