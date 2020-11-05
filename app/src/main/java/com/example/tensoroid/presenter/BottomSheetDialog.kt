package com.example.tensoroid.presenter

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tensoroid.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.item_bottom_sheet.*

class BottomSheetDialog : BottomSheetDialogFragment(), View.OnClickListener {

    interface SelectColorListener {
        fun setColor(color: Int)
    }

    private lateinit var selectColorListener: SelectColorListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as? SelectColorListener)?.let {
            selectColorListener = it
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.black -> {
                selectColorListener.setColor(Color.BLACK)
                dismiss()
            }
            R.id.white -> {
                selectColorListener.setColor(Color.WHITE)
                dismiss()
            }
            R.id.darker_gray -> {
                selectColorListener.setColor(Color.DKGRAY)
                dismiss()
            }
            R.id.transparent -> {
                selectColorListener.setColor(Color.TRANSPARENT)
                dismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(
            R.layout.item_bottom_sheet,
            container, false
        )


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        black.setOnClickListener(this)
        white.setOnClickListener(this)
        darker_gray.setOnClickListener(this)
        transparent.setOnClickListener(this)
    }


}