package com.example.tensoroid.presenter

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.tensoroid.R
import com.example.tensoroid.databinding.ItemBottomSheetBinding
import com.example.tensoroid.presenter.viewmodel.TensoroidViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class BackgroundChangeBottomSheetDialog : BottomSheetDialogFragment(), View.OnClickListener {


    private lateinit var binding: ItemBottomSheetBinding

    private val tensoroidViewModel by sharedViewModel<TensoroidViewModel>()


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.black -> {
                tensoroidViewModel.setBgColor(Color.BLACK)
                dismiss()
            }
            R.id.white -> {
                tensoroidViewModel.setBgColor(Color.WHITE)
                dismiss()
            }
            R.id.darker_gray -> {
                tensoroidViewModel.setBgColor(Color.DKGRAY)
                dismiss()
            }
            R.id.transparent -> {
                tensoroidViewModel.setBgColor(Color.TRANSPARENT)
                dismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.item_bottom_sheet, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.black.setOnClickListener(this)
        binding.white.setOnClickListener(this)
        binding.darkerGray.setOnClickListener(this)
        binding.transparent.setOnClickListener(this)
    }


}