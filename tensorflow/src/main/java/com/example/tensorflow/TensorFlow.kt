package com.example.tensorflow

import android.content.Context
import android.widget.Toast

class CustomTensorFlow() {

    fun toastText(context: Context, string: String) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT)
    }
}