package com.yogadimas.simastekom.adapter.student.identitypersonal

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
import coil.load
import com.yogadimas.simastekom.BuildConfig
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.enums.FieldType
import com.yogadimas.simastekom.common.enums.Scale
import com.yogadimas.simastekom.common.enums.SpecialCharacter
import com.yogadimas.simastekom.common.interfaces.OnItemClickIdentityPersonalCallback
import com.yogadimas.simastekom.databinding.ItemIdentityPersonalBinding
import com.yogadimas.simastekom.databinding.ItemIdentityPersonalDetailsViewStubBinding
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IdentityPersonalAdapter(private val itemClickCallback: OnItemClickIdentityPersonalCallback) :
    PagingDataAdapter<IdentityPersonalData, IdentityPersonalAdapter.ViewHolder>(DIFF_CALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemIdentityPersonalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, itemClickCallback)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }

    }

    class ViewHolder(
        private val binding: ItemIdentityPersonalBinding,
        private val itemClickCallback: OnItemClickIdentityPersonalCallback,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        private var isDetailVisible = false
        private var detailViewBinding: ItemIdentityPersonalDetailsViewStubBinding? = null

        @SuppressLint("SetTextI18n")
        fun bind(
            data: IdentityPersonalData,
        ) {
            binding.apply {
                itemView.context.apply {
                    val user = data.studentIdNumber ?: data.lectureIdNumber ?: data.username
                    val labelUser =
                        if (data.studentIdNumber != null) getString(R.string.text_label_student_id_number) else if (data.lectureIdNumber != null) getString(
                            R.string.text_label_lecture_id_number
                        ) else getString(R.string.text_label_id_username)


                    ivProfile.load(BuildConfig.BASE_URL + data.profilePicture) {
                        crossfade(true)
                        placeholder(R.drawable.z_ic_placeholder_profile)
                        error(R.drawable.z_ic_placeholder_profile)
                    }

                    tvLabelUser.text = labelUser
                    tvUser.text = user.toString()

                    btnDetail.text = itemView.context.getString(R.string.text_more)
                    btnDetail.setOnClickListener {
                        toggleDetailView(data)
                    }
                }
            }
        }

        private fun toggleDetailView(data: IdentityPersonalData) {
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
        private fun inflateAndShowDetailViews(data: IdentityPersonalData) {
            if (detailViewBinding == null) {
                // Inflate the ViewStub jika belum di-inflate
                detailViewBinding =
                    ItemIdentityPersonalDetailsViewStubBinding.bind(binding.vsItemIdentityPersonalDetails.inflate())
            }

            // Atur data ke detailView
            detailViewBinding?.apply {
                tvIdCardNumber.text = data.idCardNumber.setValueIfNull()
                tvGender.text = data.gender.setValueIfNull()
                tvAddress.text = data.address.setValueIfNull()
                tvPlaceDateBirth.text = data.placeDateBirth.setValueIfNull()
                tvReligion.text = data.religion.setValueIfNull()
                CoroutineScope(Dispatchers.Main).launch {
                    configureEmailView(data)
                    configurePhoneView(data)
                }

            }

            // Tampilkan view yang sudah di-inflate
            binding.vsItemIdentityPersonalDetails.visibility = View.VISIBLE

        }


        // Hide the detail views
        private fun hideDetailViews() {
            binding.vsItemIdentityPersonalDetails.visibility = View.GONE
        }

        @SuppressLint("ResourceType")
        private suspend fun configureEmailView(data: IdentityPersonalData) {
            detailViewBinding?.tvEmail?.apply {
                if (!data.email.isNullOrEmpty()) {
                    isClickable = true
                    isFocusable = true
                    stateListAnimator = setAnimationOnClick(itemView.rootView)
                    setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
                    (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = 20.dpToPx()
                    background = ContextCompat.getDrawable(
                        context,
                        R.drawable.background_rounded_solid_blue
                    )
                    setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    setOnClickListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(100);
                            itemClickCallback.onItemClicked(data, FieldType.EMAIL)
                        }
                    }


                } else {
                    setPadding(4.dpToPx(), 2.dpToPx(), 4.dpToPx(), 2.dpToPx())
                    (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = 10.dpToPx()
                    background = null
                }

                text = data.email.setValueIfNull()

            }
        }

        private fun configurePhoneView(data: IdentityPersonalData) {
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
                            itemClickCallback.onItemClicked(data, FieldType.PHONE)
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
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<IdentityPersonalData>() {
            override fun areItemsTheSame(
                oldItem: IdentityPersonalData,
                newItem: IdentityPersonalData,
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: IdentityPersonalData,
                newItem: IdentityPersonalData,
            ): Boolean {
                return oldItem.userId == newItem.userId
            }

        }
    }


}