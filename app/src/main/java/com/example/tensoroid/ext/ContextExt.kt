package com.example.tensoroid.ext

import android.content.Context
import android.widget.Toast


fun Context.showToast(message: String, toastSort: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, toastSort).show()
}