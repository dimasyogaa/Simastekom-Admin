package com.yogadimas.simastekom.adapter.student.identityacademic

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ItemIdentityAcademicBinding
import com.yogadimas.simastekom.model.responses.IdentityAcademicData

class IdentityAcademicAdapter :
    PagingDataAdapter<IdentityAcademicData, IdentityAcademicAdapter.ViewHolder>(DIFF_CALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemIdentityAcademicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }

    }


    class ViewHolder(private val binding: ItemIdentityAcademicBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(
            data: IdentityAcademicData
        ) {

            binding.apply {
                itemView.context.apply {
                    tvId.text =
                        getString(
                            R.string.text_id_format,
                            data.id.toString()
                        )
                    tvStudentIdNumber.text =
                        getString(
                            R.string.text_student_id_number_format,
                            data.studentIdNumber.toString()
                        )
                    tvStudyProgram.text = data.studyProgramName
                    tvBatch.text =
                        getString(
                            R.string.text_batch_format,
                            data.batch?.toDouble().toString()
                        )
                    tvSemester.text =
                        getString(
                            R.string.text_semester_format,
                            data.numberSemester
                        )
                    tvClassSession.text = data.classSessionName
                    tvLectureMethod.text = data.lectureMethodName
                    tvCampus.text = data.campusName
                }
            }

        }
    }


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<IdentityAcademicData>() {
            override fun areItemsTheSame(
                oldItem: IdentityAcademicData,
                newItem: IdentityAcademicData
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: IdentityAcademicData,
                newItem: IdentityAcademicData
            ): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }


}