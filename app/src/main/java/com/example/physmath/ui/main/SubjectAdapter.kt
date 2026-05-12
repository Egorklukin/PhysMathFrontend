package com.example.physmath.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.physmath.R
import com.example.physmath.data.entities.Subject
import com.example.physmath.databinding.ItemSubjectCardBinding
import org.commonmark.node.Image

class SubjectAdapter(
    private val onItemClick: (Subject) -> Unit
) : ListAdapter<Subject, SubjectAdapter.SubjectViewHolder>(SubjectDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubjectViewHolder(
        private val binding: ItemSubjectCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subject: Subject) {
            binding.tvSubjectTitle.text = subject.title

            val iconRes = when (subject.icon) {
                "ic_math" -> R.drawable.math
                "ic_physics" -> R.drawable.physics
                else -> R.drawable.ic_default_subject
            }
            binding.ivSubjectIcon.setImageResource(iconRes)

            binding.cardSubject.setCardBackgroundColor(
                android.graphics.Color.parseColor(subject.color)
            )

            binding.root.setOnClickListener { onItemClick(subject) }
        }
    }

    class SubjectDiffCallback : DiffUtil.ItemCallback<Subject>() {
        override fun areItemsTheSame(oldItem: Subject, newItem: Subject) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Subject, newItem: Subject) =
            oldItem == newItem
    }
}