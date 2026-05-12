package com.example.physmath.ui.lessons

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.physmath.R
import com.example.physmath.data.AppDatabase
import com.example.physmath.data.entities.LessonEntity
import com.example.physmath.databinding.FragmentLessonDetailBinding
import com.example.physmath.utils.NetworkUtils
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch

class LessonDetailFragment : Fragment(R.layout.fragment_lesson_detail) {

    private val args: LessonDetailFragmentArgs by navArgs()
    private var _binding: FragmentLessonDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LessonDetailViewModel by viewModels()

    private val dao by lazy { AppDatabase.getInstance(requireContext()).dao() }

    private lateinit var markwon: Markwon

    private var currentLesson: LessonEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        markwon = Markwon.create(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLessonDetailBinding.bind(view)

        setupToolbar()
        loadLesson(args.lessonId)
        setupTestButton()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener { findNavController().navigateUp() }
        }
    }

    private fun loadLesson(lessonId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val localLesson = dao.getLessonById(lessonId)

            if (localLesson != null) {
                if (isAdded && view != null) {
                    displayLesson(localLesson)
                }

                if (NetworkUtils.isOnline(requireContext())) {
                    try {
                        val remoteLesson = viewModel.fetchLessonFromServer(lessonId)
                        dao.insertLesson(remoteLesson.copy(
                            isDownloaded = localLesson.isDownloaded,
                            downloadedAt = localLesson.downloadedAt
                        ))
                        if (isAdded && view != null) {
                            displayLesson(remoteLesson)
                        }
                    } catch (e: Exception) {
                        Log.w("LessonDetail", "ошибка обновления уроков: ${e.message}")
                    }
                }
                return@launch
            }

            if (!NetworkUtils.isOnline(requireContext())) {
                if (isAdded && view != null) {
                    showError("❌ Урок не найден. Подключитесь к интернету.")
                }
                return@launch
            }

            try {
                val lesson = viewModel.fetchLessonFromServer(lessonId)
                dao.insertLesson(lesson.copy(isDownloaded = false))
                if (isAdded && view != null) {
                    displayLesson(lesson)
                }
            } catch (e: Exception) {
                if (isAdded && view != null) {
                    showError("Не удалось загрузить: ${e.message}")
                }
            }
        }
    }

    private fun displayLesson(lesson: LessonEntity) {
        currentLesson = lesson
        binding.tvLessonTitle.text = lesson.title
        markwon.setMarkdown(binding.tvLessonContent, lesson.contentMd)
    }

    private fun setupTestButton() {
        binding.btnStartTest.setOnClickListener {
            currentLesson?.let { lesson ->
                val action = LessonDetailFragmentDirections
                    .actionLessonDetailFragmentToTestsRunFragment(lesson.id)
                findNavController().navigate(action)
            }
        }
    }

    private fun showError(message: String) {
        binding.tvLessonTitle.text = "Ошибка загрузки"
        binding.tvLessonContent.text = message
        binding.pbProgress.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}