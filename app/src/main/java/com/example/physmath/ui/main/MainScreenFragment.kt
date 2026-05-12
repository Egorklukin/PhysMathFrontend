package com.example.physmath.ui.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.physmath.R
import com.example.physmath.databinding.FragmentMainScreenBinding

class MainScreenFragment : Fragment(R.layout.fragment_main_screen) {

    private var _binding: FragmentMainScreenBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: SubjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainScreenBinding.bind(view)
        binding.btnRefresh.setOnClickListener {
            viewModel.syncWithServer()
        }

        setupToolbar()
        setupRecyclerView()
        setupObservers()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = "PhysMath"
        }
    }

    private fun setupRecyclerView() {
        adapter = SubjectAdapter { subject ->
            val action = MainScreenFragmentDirections
                .actionMainScreenToTopicListFragment(subject.id, subject.title)
            findNavController().navigate(action)
        }

        binding.rvSubjects.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MainScreenFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            adapter.submitList(subjects)
        }

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
            binding.tvPlaceholder.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvSubjects.visibility = if (isEmpty) View.GONE else View.VISIBLE

            if (isEmpty && !viewModel.isLoading.value!!) {
                binding.tvPlaceholder.text = "📚 Предметы не найдены. Нажмите обновить."
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}