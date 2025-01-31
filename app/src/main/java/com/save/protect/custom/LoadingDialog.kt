package com.save.protect.custom

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.save.protect.R
import com.save.protect.databinding.DialogLoadingBinding

class LoadingDialog : DialogFragment() {

    lateinit var binding: DialogLoadingBinding

    private var isLoading = false


    override fun getTheme() = R.style.AppDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_loading,
            container,
            false
        )

        binding.lifecycleOwner = this

        binding.loadingImageView.let {
            Glide.with(this).load(R.raw.load).into(it)
        }

        return binding.root
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        setCancelable(false)

        return super.onCreateDialog(savedInstanceState)
    }


    override fun show(manager: FragmentManager, tag: String?) {
        if (!isLoading) {
            isLoading = true

            try {
                super.show(manager, tag)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }
    }

    override fun dismiss() {
        if (isLoading) {
            isLoading = false
            super.dismissAllowingStateLoss()
        }
    }

}