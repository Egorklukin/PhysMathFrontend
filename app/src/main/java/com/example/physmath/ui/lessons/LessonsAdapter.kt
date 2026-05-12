package com.example.physmath.ui.lessons

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.physmath.data.entities.LessonEntity
import com.example.physmath.databinding.ItemLessonBinding
import io.noties.markwon.Markwon

class LessonsAdapter(
    private val onLessonClick: (LessonEntity) -> Unit
) : ListAdapter<LessonEntity, LessonsAdapter.LessonViewHolder>(LessonDiffCallback()) {

    private lateinit var markwon: Markwon

    fun setMarkwon(markwon: Markwon) {
        this.markwon = markwon
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemLessonBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LessonViewHolder(
        private val binding: ItemLessonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(lesson: LessonEntity) {
            binding.tvLessonTitle.text = lesson.title

            val preview = lesson.contentMd
                .lines().firstOrNull { it.isNotBlank() && !it.startsWith("#") }
                ?.take(100)
                ?.plus("...") ?: "Нет описания"
            binding.tvLessonPreview.text = preview

            binding.pbProgress.progress = (lesson.progress * 100).toInt()
            binding.tvProgress.text = "${(lesson.progress * 100).toInt()}%"

            binding.tvOfflineBadge.visibility = if (lesson.isDownloaded) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.root.setOnClickListener {
                onLessonClick(lesson)
            }
        }
    }

    class LessonDiffCallback : DiffUtil.ItemCallback<LessonEntity>() {
        override fun areItemsTheSame(oldItem: LessonEntity, newItem: LessonEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: LessonEntity, newItem: LessonEntity) =
            oldItem == newItem
    }
}