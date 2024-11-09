package com.yogadimas.simastekom.adapter.student.identityparent

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.StateListAnimator
import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.enums.Scale
import com.yogadimas.simastekom.common.enums.SpecialCharacter
import com.yogadimas.simastekom.common.interfaces.OnItemClickCallback
import com.yogadimas.simastekom.databinding.ItemIdentityParentBinding
import com.yogadimas.simastekom.databinding.ItemIdentityParentDetailsViewStubBinding
import com.yogadimas.simastekom.model.responses.AddressData
import com.yogadimas.simastekom.model.responses.StudentIdentityParentData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StudentIdentityParentAdapter(private val itemClickCallback: OnItemClickCallback<StudentIdentityParentData>) :
    PagingDataAdapter<StudentIdentityParentData, StudentIdentityParentAdapter.ViewHolder>(DIFF_CALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemIdentityParentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, itemClickCallback)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }

    }

    class ViewHolder(
        private val binding: ItemIdentityParentBinding,
        private val itemClickCallback: OnItemClickCallback<StudentIdentityParentData>,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        private var isDetailVisible = false
        private var detailViewBinding: ItemIdentityParentDetailsViewStubBinding? = null

        @SuppressLint("SetTextI18n")
        fun bind(
            data: StudentIdentityParentData,
        ) {
            binding.apply {
                itemView.context.apply {
                    tvStudentIdNumber.text = data.studentIdNumber

                    btnDetail.text = if (!isDetailVisible) getString(R.string.text_more)
                    else getString(R.string.text_close)
                    btnDetail.setOnClickListener {
                        toggleDetailView(data)
                    }
                }
            }
        }

        private fun toggleDetailView(data: StudentIdentityParentData) {
            if (isDetailVisible) {
                hideDetailViews()
                binding.btnDetail.text = itemView.context.getString(R.string.text_more)
                binding.btnDetail.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.md_theme_primary
                    )
                )
            } else {
                binding.btnDetail.text = itemView.context.getString(R.string.text_close)
                binding.btnDetail.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.md_theme_error
                    )
                )
                inflateAndShowDetailViews(data)
            }
            isDetailVisible = !isDetailVisible
        }


        // Inflate and show the detail views using ViewStub
        private fun inflateAndShowDetailViews(data: StudentIdentityParentData) {
            if (detailViewBinding == null) {
                // Inflate the ViewStub jika belum di-inflate
                detailViewBinding =
                    ItemIdentityParentDetailsViewStubBinding.bind(binding.vsItemIdentityParentDetails.inflate())
            }

            // Atur data ke detailView
            detailViewBinding?.apply {
                tvFatherIdCardNumber.text = data.idCardNumberFather.setValueIfNull()
                tvFatherName.text = data.nameFather.setValueIfNull()
                tvMotherIdCardNumber.text = data.idCardNumberMother.setValueIfNull()
                tvMotherName.text = data.nameMother.setValueIfNull()
                tvOccupation.text = data.occupation.setValueIfNull()
                tvAddress.text = AddressData.getAddressData(data.address).setValueIfNull()
                CoroutineScope(Dispatchers.Main).launch {
                    configurePhoneView(data)
                }

            }

            // Tampilkan view yang sudah di-inflate
            binding.vsItemIdentityParentDetails.visibility = View.VISIBLE

        }


        // Hide the detail views
        private fun hideDetailViews() {
            binding.vsItemIdentityParentDetails.visibility = View.GONE
        }


        private fun configurePhoneView(data: StudentIdentityParentData) {
            detailViewBinding?.tvPhone?.apply {
                if (!data.phone.isNullOrEmpty()) {
                    isClickable = true
                    isFocusable = true
                    stateListAnimator = setAnimationOnClick(itemView.rootView)

                    setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
                    (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = 32.dpToPx()
                    background = ContextCompat.getDrawable(
                        context,
                        R.drawable.background_rounded_solid_green
                    )
                    setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    setOnClickListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(50);
                            itemClickCallback.onItemClicked(data)
                        }
                    }
                } else {
                    setPadding(4.dpToPx(), 2.dpToPx(), 4.dpToPx(), 2.dpToPx())
                    (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = 10.dpToPx()
                    background = null
                }

                text = data.phone.setValueIfNull()

            }
        }

        private fun setAnimationOnClick(view: View): StateListAnimator {

            val stateListAnimator = StateListAnimator()
            val scaleX = Scale.X.value
            val scaleY = Scale.Y.value

            val pressedAnimator = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(scaleX, 0.85f),
                PropertyValuesHolder.ofFloat(scaleY, 0.85f)
            ).apply {
                duration = 15
            }

            val defaultAnimator = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(scaleX, 1.0f),
                PropertyValuesHolder.ofFloat(scaleY, 1.0f)
            ).apply {
                duration = 15
            }

            stateListAnimator.addState(intArrayOf(android.R.attr.state_pressed), pressedAnimator)
            stateListAnimator.addState(intArrayOf(), defaultAnimator) // State default

            return stateListAnimator
        }

        private fun String?.setValueIfNull(): String {
            return if (!this.isNullOrEmpty()) this else SpecialCharacter.STRIP.symbol.toString()

        }

        private fun Int.dpToPx(): Int {
            return (this * Resources.getSystem().displayMetrics.density).toInt()
        }

    }


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StudentIdentityParentData>() {
            override fun areItemsTheSame(
                oldItem: StudentIdentityParentData,
                newItem: StudentIdentityParentData,
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: StudentIdentityParentData,
                newItem: StudentIdentityParentData,
            ): Boolean {
                return oldItem.userId == newItem.userId
            }

        }
    }


}