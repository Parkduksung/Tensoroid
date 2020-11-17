package com.example.tensoroid.presenter

import android.content.Context
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import com.example.tensoroid.R

class LoadingDialog(private val context: Context) {


    private var alertDialog: AlertDialog =
        AlertDialog.Builder(context).apply {
            setView(R.layout.dialog_loading)
            setCancelable(true)
        }.create()

    fun showLoading() {
        alertDialog.show()
    }

    fun dismissLoading() {

        Handler().postDelayed({
            alertDialog.dismiss()
        },3000L)
    }

}