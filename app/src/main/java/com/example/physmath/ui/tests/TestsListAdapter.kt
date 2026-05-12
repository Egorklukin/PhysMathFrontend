package com.example.physmath.ui.tests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.physmath.databinding.ItemTestBinding

data class TestListItem(
    val lessonId: String,
    val title: String,
    val description: String,
    val questionsCount: Int,
    val isUnlocked: Boolean = true
)

class TestsListAdapter(
    private val onStartTest: (String) -> Unit
) : ListAdapter<TestListItem, TestsListAdapter.TestViewHolder>(TestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val binding = ItemTestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TestViewHolder(
        private val binding: ItemTestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(test: TestListItem) {
            binding.tvTestTitle.text = test.title
            binding.tvTestDescription.text = test.description
            binding.tvQuestionsCount.text = "${test.questionsCount} вопросов"

            binding.root.isEnabled = test.isUnlocked
            binding.btnStart.isEnabled = test.isUnlocked
            binding.root.alpha = if (test.isUnlocked) 1f else 0.5f

            val onClick: (android.view.View) -> Unit = { _ ->
                onStartTest(test.lessonId)
            }

            binding.root.setOnClickListener(onClick)
            binding.btnStart.setOnClickListener(onClick)
        }
    }

    class TestDiffCallback : DiffUtil.ItemCallback<TestListItem>() {
        override fun areItemsTheSame(oldItem: TestListItem, newItem: TestListItem) =
            oldItem.lessonId == newItem.lessonId

        override fun areContentsTheSame(oldItem: TestListItem, newItem: TestListItem) =
            oldItem == newItem
    }
}