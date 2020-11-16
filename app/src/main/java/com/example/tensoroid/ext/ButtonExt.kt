package com.example.tensoroid.ext

import android.graphics.Color
import android.widget.Button
import androidx.databinding.BindingAdapter

@BindingAdapter("bind:colorType", "bind:onChangeBg")
fun Button.onChangeBg(colorType: BgColor, f: (color: Int) -> Unit) {
    setOnClickListener {
        when (colorType) {
            BgColor.BLACK -> {
                f(Color.BLACK)
            }
            BgColor.WHITE -> {
                f(Color.WHITE)
            }
            BgColor.DK_GRAY -> {
                f(Color.DKGRAY)
            }
            BgColor.TRANSPARENT -> {
                f(Color.TRANSPARENT)
            }
        }
    }
}

enum class BgColor {
    BLACK, WHITE, DK_GRAY, TRANSPARENT
}