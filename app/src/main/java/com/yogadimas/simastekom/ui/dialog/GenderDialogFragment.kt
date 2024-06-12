package com.yogadimas.simastekom.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.DialogFragmentGenderBinding
import com.yogadimas.simastekom.interfaces.OnOptionDialogListenerInterface
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalEditActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GenderDialogFragment : DialogFragment() {

    private var _binding: DialogFragmentGenderBinding? = null
    private val binding get() = _binding!!

    private var optionDialogListener: OnOptionDialogListenerInterface? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val parentActivity = activity

        if (parentActivity is IdentityPersonalEditActivity) {
            this.optionDialogListener = parentActivity

        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogFragmentGenderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background_rounded)

        binding.apply {

            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    tvMan.setSafeOnClickListener {
                        optionChosen(tvMan.text.toString())
                        dialog?.dismiss()
                    }
                    tvWoman.setSafeOnClickListener {
                        optionChosen(tvWoman.text.toString())
                        dialog?.dismiss()
                    }
                }
            }


        }


    }

    private fun View.setSafeOnClickListener(onSafeClick: suspend (View) -> Unit) {
        setOnClickListener {
            lifecycleScope.launch {
                delay(150)
                onSafeClick(it)
            }
        }
    }

    private fun optionChosen(value: String) {
        optionDialogListener?.onOptionChosen(value, KEY_OPTION_GENDER)
    }

    override fun onDetach() {
        super.onDetach()
        this.optionDialogListener = null
        _binding = null
    }

    companion object {
        const val KEY_OPTION_GENDER = "key_option_gender"
    }
}