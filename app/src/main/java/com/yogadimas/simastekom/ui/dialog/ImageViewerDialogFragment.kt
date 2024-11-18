package com.yogadimas.simastekom.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import coil.load
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.enums.ErrorCode
import com.yogadimas.simastekom.common.helper.isLandscape
import com.yogadimas.simastekom.common.helper.showLoadingFade
import com.yogadimas.simastekom.common.interfaces.OnCallbackFromFragmentInterface
import com.yogadimas.simastekom.databinding.DialogFragmentImageViewerBinding

class ImageViewerDialogFragment : DialogFragment() {

    private var _binding: DialogFragmentImageViewerBinding? = null
    private val binding get() = _binding!!

    private lateinit var mCallback: OnCallbackFromFragmentInterface

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallback = activity as OnCallbackFromFragmentInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogFragmentImageViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.background_dialog_rounded)
        showLoadingView(true)
        arguments?.getString(ARG_IMAGE_URL)?.let { imageUrl ->
            binding.ivPicture.load(imageUrl) {
                error(R.drawable.z_ic_placeholder_profile)
                listener(
                    onSuccess = {_, _ -> showLoadingView(false)},
                    onError = {_, _ -> showLoadingView(false)}
                )
            }
        } ?: run {
            showLoadingView(false)
            mCallback.getError(getString(R.string.text_image_not_found), ErrorCode.CLIENT)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.WRAP_CONTENT
        val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT

        binding.scrollView.isScrollbarFadingEnabled = true
        if (isLandscape()) {
            binding.scrollView.isScrollbarFadingEnabled = false
        }
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.background_dialog_rounded)
    }

    private fun showLoadingView(isVisible: Boolean) {
        showLoadingFade(binding.mainProgressBar, isVisible)
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mCallback.getData(ARG_IMAGE_URL)
    }

    override fun onDetach() {
        super.onDetach()
        _binding = null
    }

    companion object {
        const val TAG = "ImageViewerDialog"
        const val ARG_IMAGE_URL = "image_url"

        fun newInstance(imageUrl: String): ImageViewerDialogFragment {
            return ImageViewerDialogFragment().apply {
                arguments = Bundle().apply { putString(ARG_IMAGE_URL, imageUrl) }
            }
        }
    }
}
