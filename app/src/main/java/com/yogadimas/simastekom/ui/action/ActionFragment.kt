package com.yogadimas.simastekom.ui.action

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yogadimas.simastekom.databinding.FragmentActionBinding
import com.yogadimas.simastekom.interfaces.OnCallbackFromFragmentInterface
import com.yogadimas.simastekom.ui.student.StudentActivity
import com.yogadimas.simastekom.ui.student.StudentActivityMenu


class ActionFragment : Fragment() {


    private var _binding: FragmentActionBinding? = null
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
        _binding = FragmentActionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCallback.getData(NAME_FRAGMENT)

        binding.apply {
            btnStudent.setOnClickListener {startActivity(Intent(requireActivity(), StudentActivityMenu::class.java))}
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val NAME_FRAGMENT = "ActionFragment"
    }


}