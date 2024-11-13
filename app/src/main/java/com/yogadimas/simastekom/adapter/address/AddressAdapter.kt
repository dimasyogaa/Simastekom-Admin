package com.yogadimas.simastekom.adapter.address

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.helper.setStripIfNull
import com.yogadimas.simastekom.databinding.ItemAddressBinding
import com.yogadimas.simastekom.databinding.ItemAddressDetailsViewStubBinding
import com.yogadimas.simastekom.model.responses.AddressData

class AddressAdapter() :
    PagingDataAdapter<AddressData, AddressAdapter.ViewHolder>(DIFF_CALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }

    }

    class ViewHolder(
        private val binding: ItemAddressBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        private var isDetailVisible = false
        private var detailViewBinding: ItemAddressDetailsViewStubBinding? = null

        @SuppressLint("SetTextI18n")
        fun bind(
            data: AddressData,
        ) {
            binding.apply {
                itemView.context.apply {
                    val user = data.studentIdNumber ?: data.lectureIdNumber ?: data.username
                    val labelUser =
                        if (data.studentIdNumber != null) getString(R.string.text_label_student_id_number) else if (data.lectureIdNumber != null) getString(
                            R.string.text_label_lecture_id_number
                        ) else getString(R.string.text_label_id_username)

                    tvLabelUser.text = labelUser
                    tvUser.text = user.toString()

                    btnDetail.text = if (!isDetailVisible) getString(R.string.text_more)
                    else getString(R.string.text_close)
                    btnDetail.setOnClickListener {toggleDetailView(data)}
                }
            }
        }

        private fun toggleDetailView(data: AddressData) {
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
        private fun inflateAndShowDetailViews(data: AddressData) {
            if (detailViewBinding == null) {
                // Inflate the ViewStub jika belum di-inflate
                detailViewBinding =
                    ItemAddressDetailsViewStubBinding.bind(binding.vsItemAddressDetails.inflate())
            }

            // Atur data ke detailView
            detailViewBinding?.apply {
                tvProvince.text = data.province.setStripIfNull()
                tvCityRegency.text = data.cityRegency.setStripIfNull()
                tvDistrict.text = data.district.setStripIfNull()
                tvVillage.text = data.village.setStripIfNull()
                tvRw.text = data.rw.setStripIfNull()
                tvRt.text = data.rt.setStripIfNull()
                tvStreet.text = data.street.setStripIfNull()
                tvOtherDetail.text = data.otherDetailAddress.setStripIfNull()
            }

            // Tampilkan view yang sudah di-inflate
            binding.vsItemAddressDetails.visibility = View.VISIBLE

        }


        // Hide the detail views
        private fun hideDetailViews() {
            binding.vsItemAddressDetails.visibility = View.GONE
        }


    }


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AddressData>() {
            override fun areItemsTheSame(
                oldItem: AddressData,
                newItem: AddressData,
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: AddressData,
                newItem: AddressData,
            ): Boolean {
                return oldItem.userId == newItem.userId
            }

        }
    }


}