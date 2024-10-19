package com.yogadimas.simastekom.ui.dialog


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.DialogFragmentReligionBinding
import com.yogadimas.simastekom.common.helper.isLandscape
import com.yogadimas.simastekom.common.interfaces.OnOptionDialogListenerInterface
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalEditActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ReligionDialogFragment : DialogFragment() {

    private var _binding: DialogFragmentReligionBinding? = null
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
        _binding = DialogFragmentReligionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.apply {

            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                   optionChosen(tvBuddhism)
                   optionChosen(tvHinduism)
                   optionChosen(tvIslam)
                   optionChosen(tvProtestant)
                   optionChosen(tvCatholic)
                   optionChosen(tvConfucianism)
                }
            }


        }


    }

    private fun optionChosen(textView: TextView) {
        textView.setSafeOnClickListener {
            optionChosen(textView.text.toString())
            dialog?.dismiss()
        }
    }



    private fun optionChosen(value: String) {
        optionDialogListener?.onOptionChosen(value, KEY_OPTION_RELIGION)
    }

    private fun View.setSafeOnClickListener(onSafeClick: suspend (View) -> Unit) {
        setOnClickListener {
            lifecycleScope.launch {
                delay(150)
                onSafeClick(it)
            }
        }
    }



    override fun onStart() {
        super.onStart()
        val width = resources.getDimensionPixelSize(R.dimen.popup_width)
        var height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.scrollView.isScrollbarFadingEnabled = true
        if (isLandscape()) {
            height = resources.getDimensionPixelSize(R.dimen.popup_height_300)
            binding.scrollView.isScrollbarFadingEnabled = false
        }
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.background_dialog_rounded)
    }

    override fun onDetach() {
        super.onDetach()
        this.optionDialogListener = null
        _binding = null
    }

    companion object {
        const val KEY_OPTION_RELIGION = "key_option_religion"
    }


}