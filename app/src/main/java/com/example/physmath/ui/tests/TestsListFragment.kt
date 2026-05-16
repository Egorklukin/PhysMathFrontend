package com.example.physmath.ui.tests

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.physmath.R
import com.example.physmath.databinding.FragmentTestsListBinding
import kotlinx.coroutines.launch

class TestsListFragment : Fragment(R.layout.fragment_tests_list) {

    private var _binding: FragmentTestsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestsViewModel by viewModels()
    private lateinit var adapter: TestsListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTestsListBinding.bind(view)

        setupRecyclerView()
        loadAvailableTests()
    }

    private fun setupRecyclerView() {
        adapter = TestsListAdapter { lessonId ->
            val action = TestsListFragmentDirections
                .actionTestsListFragmentToTestsRunFragment(lessonId)
            findNavController().navigate(action)
        }

        binding.rvTestsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TestsListFragment.adapter
        }
    }

    private fun loadAvailableTests() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getDownloadedLessons().collect { lessons ->

                val testItems = lessons.map { lesson ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        val count = viewModel.getQuestionsCount(lesson.id)
                        val testItem = TestListItem(
                            lessonId = lesson.id,
                            title = lesson.title,
                            description = "Тест по уроку: ${lesson.title}",
                            questionsCount = count,
                            isUnlocked = lesson.isDownloaded
                        )
                        val currentList = (adapter.currentList).toMutableList()
                        val index = currentList.indexOfFirst { it.lessonId == lesson.id }
                        if (index >= 0) {
                            currentList[index] = testItem
                            adapter.submitList(currentList)
                        } else {
                            adapter.submitList(currentList + testItem)
                        }
                    }
                    TestListItem(
                        lessonId = lesson.id,
                        title = lesson.title,
                        description = "Тест по уроку: ${lesson.title}",
                        questionsCount = 0,
                        isUnlocked = lesson.isDownloaded
                    )
                }

                adapter.submitList(testItems)
                updateEmptyState(testItems.isEmpty())
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.rvTestsList.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.tvPlaceholder.visibility = if (isEmpty) View.VISIBLE else View.GONE

        if (isEmpty) {
            binding.tvPlaceholder.text = "📚 Тестов пока нет. Скачайте урок, чтобы разблокировать тест."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}