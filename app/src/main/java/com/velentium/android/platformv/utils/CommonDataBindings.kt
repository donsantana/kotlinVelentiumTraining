package com.velentium.android.platformv.utils

import android.graphics.Paint
import android.text.Html
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.velentium.android.platformv.uicontrols.ext.makeGrayScale
import java.util.*

@BindingAdapter("imageResourceId")
fun setImageResource(imageView: ImageView, @DrawableRes imageResId: Int) {
    imageView.setImageResource(imageResId)
}

@BindingAdapter("backgroundResourceId")
fun setBackground(imageView: ImageView, @DrawableRes backgroundResId: Int) {
    imageView.setBackgroundResource(backgroundResId)
}

@BindingAdapter("stringResourceId")
fun setStringResource(textView: TextView, @StringRes stringResId: Int) {
    if (stringResId != 0) {
        textView.text = textView.context.getString(stringResId)
    } else {
        textView.text = null
    }
}

@BindingAdapter("drawableEndResourceId")
fun setDrawableEndFromResourceId(textView: TextView, @DrawableRes drawableResId: Int) {
    val existing = textView.compoundDrawables
    if (drawableResId != 0) {
        val drawable = ContextCompat.getDrawable(textView.context, drawableResId) ?: run {
            Log.w("BindingAdapter->setDrawableEndFromResourceId", "Failed to get drawable for resource id: $drawableResId")
            return
        }
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        textView.setCompoundDrawables(existing[0], existing[1], drawable, existing[3])
    } else {
        textView.setCompoundDrawables(existing[0], existing[1], null, existing[3])
    }
}

@BindingAdapter("titleCaseStringResourceId")
fun setTitleCaseStringResource(textView: TextView, @StringRes stringResId: Int) {
    if (stringResId != 0) {
        textView.text = textView.context.getString(stringResId)
                .split(" ")
                .joinToString(" ") { it.capitalize(Locale.ROOT) }
    } else {
        textView.text = null
    }
}

@BindingAdapter("contentDescriptionResourceId")
fun setContentDescriptionResource(textView: TextView, @StringRes stringResId: Int) {
    if (stringResId != 0) {
        textView.contentDescription = textView.context.getString(stringResId)
    } else {
        textView.contentDescription = null
    }
}

//fun setAppCompatTextViewGravity(textView: TextView, )

@BindingAdapter("isImageGreyScale")
fun setIsImageGreyScale(imageView: ImageView, isGreyScale: Boolean) {
    if (isGreyScale) {
        imageView.makeGrayScale()
    } else {
        imageView.colorFilter = null
    }
}

@BindingAdapter("isUnderlinedText")
fun setUnderlinedText(textView: TextView, isUnderlined: Boolean) {
    if (isUnderlined) {
        textView.underline()
    } else {
        textView.removeUnderline()
    }
}

fun TextView.removeUnderline() {
    val textValue = if (text.isNullOrEmpty()) "" else text.toString()
    text = Html.fromHtml(textValue, Html.FROM_HTML_MODE_LEGACY).toString()
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG.inv()
}

fun TextView.underline() {
    var textValue = if (text.isNullOrEmpty()) "" else text.toString()
    if (textValue.contains("<")) {
        // strip existing tags if any
        textValue = Html.fromHtml(textValue, Html.FROM_HTML_MODE_LEGACY).toString()
    }
    text = Html.fromHtml("<u>${textValue}</u>", Html.FROM_HTML_MODE_LEGACY)
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

