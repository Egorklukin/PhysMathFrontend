package com.example.physmath.ui.topics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.physmath.data.entities.Topic
import com.example.physmath.databinding.ItemTopicBinding

class TopicAdapter(
    private val onItemClick: (Topic) -> Unit
) : ListAdapter<Topic, TopicAdapter.TopicViewHolder>(TopicDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = ItemTopicBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TopicViewHolder(private val binding: ItemTopicBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(topic: Topic) {
            binding.tvTopicTitle.text = topic.title
            binding.root.setOnClickListener { onItemClick(topic) }
        }
    }

    class TopicDiffCallback : DiffUtil.ItemCallback<Topic>() {
        override fun areItemsTheSame(oldItem: Topic, newItem: Topic) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Topic, newItem: Topic) =
            oldItem == newItem
    }
}