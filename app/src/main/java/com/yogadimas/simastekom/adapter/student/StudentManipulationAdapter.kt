package com.yogadimas.simastekom.adapter.student

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ItemStudentBinding
import com.yogadimas.simastekom.common.interfaces.OnItemClickManipulationCallback
import com.yogadimas.simastekom.model.responses.StudentData

class StudentManipulationAdapter(
    private val itemClickCallback: OnItemClickManipulationCallback<StudentData>
) : PagingDataAdapter<StudentData, StudentManipulationAdapter.ViewHolder>(DIFF_CALLBACK) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data, itemClickCallback)
        }

    }


    class ViewHolder(private val binding: ItemStudentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(
            data: StudentData,
            itemClickCallback: OnItemClickManipulationCallback<StudentData>
        ) {

            binding.apply {
                tvStudentIdNumber.text = data.studentIdNumber
                tvStudentName.text = data.fullName
                tvStudentStudyProgram.text = data.studyProgramName
                tvStudentBatch.text = itemView.context.getString(R.string.text_batch_format, data.batch?.toDouble().toString())
                tvStudentCampus.text = data.campusName
                itemView.setOnClickListener { itemClickCallback.onItemClicked(data) }
                btnDelete.setOnClickListener { itemClickCallback.onDeleteClicked(data) }
            }

        }
    }


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StudentData>() {
            override fun areItemsTheSame(oldItem: StudentData, newItem: StudentData): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StudentData, newItem: StudentData): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }


}