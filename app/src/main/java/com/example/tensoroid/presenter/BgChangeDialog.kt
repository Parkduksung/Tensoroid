package com.example.tensoroid.presenter

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

class BgChangeDialog : BottomSheetDialogFragment() {

    private lateinit var binding: ItemBottomSheetBinding

    private val tensoroidViewModel by sharedViewModel<TensoroidViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.item_bottom_sheet, container, false)
        binding.lifecycleOwner = this
        binding.run {
            vm = tensoroidViewModel
        }
        return binding.root
    }

    companion object{
        const val TAG = "BackgroundChangeBottomSheetDialog"
    }
}