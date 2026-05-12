package com.example.physmath.ui.topics

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.physmath.R
import com.example.physmath.databinding.FragmentTopicListBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TopicListFragment : Fragment(R.layout.fragment_topic_list) {

    private val args: TopicListFragmentArgs by navArgs()
    private var _binding: FragmentTopicListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TopicViewModel by viewModels {
        TopicViewModelFactory(requireActivity().application)
    }
    private lateinit var adapter: TopicAdapter
    private var searchJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTopicListBinding.bind(view)
        binding.btnRefresh.setOnClickListener {
            viewModel.refreshTopics(args.subjectId)
        }

        setupToolbar()
        setupSearch()
        setupRecyclerView()
        setupObservers()
        loadTopics()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = args.subjectTitle
            setNavigationOnClickListener { findNavController().navigateUp() }
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300)
                    viewModel.searchTopics(args.subjectId, newText.orEmpty())
                }
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = TopicAdapter { topic ->
            val action = TopicListFragmentDirections
                .actionTopicListToLessonListFragment(
                    topicId = topic.id,
                    topicTitle = topic.title,
                    subjectId = args.subjectId
                )
            findNavController().navigate(action)
        }

        binding.rvTopics.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TopicListFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRefresh.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                binding.btnRefresh.visibility = View.VISIBLE
            } else {
                binding.btnRefresh.visibility = View.GONE
            }
        }

        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.tvEmpty.text = if (isEmpty) "🔍 Темы не найдены" else ""
        }

        viewModel.topics.observe(viewLifecycleOwner) { topics ->
            adapter.submitList(topics)
        }
    }

    private fun loadTopics() {
        lifecycleScope.launch {
            try {
                viewModel.loadTopics(args.subjectId)
            } catch (e: Exception) {

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchJob?.cancel()
    }
}