package com.example.physmath.ui.lessons

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.physmath.R
import com.example.physmath.databinding.FragmentLessonsBinding
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch

class LessonsFragment : Fragment(R.layout.fragment_lessons) {

    private val args: LessonsFragmentArgs by navArgs()
    private var _binding: FragmentLessonsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LessonsViewModel by navGraphViewModels(R.id.nav_graph)

    private lateinit var adapter: LessonsAdapter
    private lateinit var markwon: Markwon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        markwon = Markwon.create(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLessonsBinding.bind(view)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        loadLessons()
    }

    private fun loadLessons() {
        lifecycleScope.launch {
            if (!args.topicId.isNullOrEmpty()) {
                viewModel.loadLessonsByTopic(args.topicId!!, args.topicTitle)
            } else {
                viewModel.syncWithServer()
            }
        }
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.lessons.observe(viewLifecycleOwner) { lessons ->
            adapter.submitList(lessons)
            updateEmptyState(lessons.isEmpty())
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRefresh.isEnabled = !isLoading
        }

        binding.btnRefresh.setOnClickListener {
            if (!args.topicId.isNullOrEmpty()) {
                viewModel.reloadCurrentLessons()
            } else {
                viewModel.syncWithServer()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = LessonsAdapter(
            onLessonClick = { lesson ->
                val action = LessonsFragmentDirections
                    .actionLessonsFragmentToLessonDetailFragment(lesson.id)
                findNavController().navigate(action)
            }
        )

        binding.rvLessons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LessonsFragment.adapter
        }

        adapter.setMarkwon(markwon)
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.rvLessons.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.tvPlaceholder.visibility = if (isEmpty) View.VISIBLE else View.GONE

        if (isEmpty) {
            binding.tvPlaceholder.text = "📚 Уроков пока нет. Нажмите обновить."
        }
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = viewModel.getCurrentTopicTitle() ?: args.topicTitle ?: "Конспекты"

            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}