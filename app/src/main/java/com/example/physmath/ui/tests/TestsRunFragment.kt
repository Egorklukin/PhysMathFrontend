package com.example.physmath.ui.tests

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.physmath.R
import com.example.physmath.data.entities.Question
import com.example.physmath.data.entities.TestResult
import com.example.physmath.data.network.LessonApi
import com.example.physmath.data.network.RetrofitClient.api
import com.example.physmath.databinding.FragmentTestsBinding
import kotlinx.coroutines.launch

class TestsRunFragment : Fragment(R.layout.fragment_tests) {

    private val args: TestsRunFragmentArgs by navArgs()
    private var _binding: FragmentTestsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TestsViewModel by viewModels()

    private var questions: List<Question> = emptyList()
    private var currentQuestionIndex = 0
    private val userAnswers = mutableMapOf<String, String>()

    private var isShowingExplanation = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTestsBinding.bind(view)

        val lessonId = args.lessonId

        binding.toolbar.apply {
            setNavigationOnClickListener { findNavController().navigateUp() }
            title = "Тестирование"
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.questions.observe(viewLifecycleOwner) { loadedQuestions ->
            if (loadedQuestions.isNotEmpty()) {
                questions = loadedQuestions.sortedBy { it.orderIndex }
                setupUI()
                loadQuestion(currentQuestionIndex)
            } else {
                binding.tvQuestion.text = "❌ Не удалось загрузить вопросы"
                binding.btnNext.isEnabled = false
            }
        }

        viewModel.loadQuestions(lessonId)

        binding.rgOptions.setOnCheckedChangeListener { _, _ ->
            updateNextButtonState()
        }

        binding.btnNext.setOnClickListener {
            handleNextButtonClick()
        }

        binding.btnPrev.setOnClickListener {
            if (currentQuestionIndex > 0) {
                saveCurrentAnswer()
                currentQuestionIndex--
                loadQuestion(currentQuestionIndex)
            }
        }

        binding.btnRestart.setOnClickListener { restartTest(lessonId) }
    }

    private fun handleNextButtonClick() {
        if (isShowingExplanation) {
            Log.d("TestDebug", "пояснение пройдено")
            isShowingExplanation = false
            goToNextQuestion()
        } else {
            saveCurrentAnswer()

            val currentQuestion = questions.getOrNull(currentQuestionIndex) ?: return
            val selectedId = binding.rgOptions.checkedRadioButtonId
            val selectedAnswer = binding.rgOptions.findViewById<RadioButton>(selectedId)?.text?.toString()

            Log.d("TestDebug", "вопрос ${currentQuestionIndex}: выбр='$selectedAnswer', правильный='${currentQuestion.correctAnswer}'")

            if (selectedAnswer != null) {
                val isCorrect = selectedAnswer.trim().equals(currentQuestion.correctAnswer.trim(), ignoreCase = true)

                if (isCorrect) {
                    Log.d("TestDebug", "правильный")
                    goToNextQuestion()
                } else {
                    Log.d("TestDebug", "показ пояснение")
                    showWrongAnswerWithExplanation(currentQuestion, selectedAnswer)
                }
            } else {
                Toast.makeText(requireContext(), "Выберите вариант ответа", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUI() {
        binding.layoutResult.visibility = View.GONE
        binding.tvQuestion.visibility = View.VISIBLE
        binding.rgOptions.visibility = View.VISIBLE
        binding.tvExplanation.visibility = View.GONE
        binding.btnPrev.visibility = View.VISIBLE
        binding.btnNext.visibility = View.VISIBLE
        isShowingExplanation = false
        updateNavigationButtons()
    }

    private fun loadQuestion(index: Int) {
        if (questions.isEmpty() || index !in questions.indices) return
        val question = questions[index]

        binding.tvQuestion.text = "${index + 1}. ${question.questionText}"

        binding.rgOptions.setOnCheckedChangeListener(null)
        binding.rgOptions.removeAllViews()
        binding.rgOptions.clearCheck()
        binding.tvExplanation.visibility = View.GONE
        binding.rgOptions.visibility = View.VISIBLE
        binding.rgOptions.isEnabled = true
        isShowingExplanation = false

        question.getOptions().forEach { option ->
            val radioButton = RadioButton(requireContext()).apply {
                text = option
                setTextColor(requireContext().getColor(R.color.color_text_main))
                buttonTintList = requireContext().getColorStateList(R.color.color_accent)
                textSize = 14f
                setPadding(0, 8, 0, 8)
                isChecked = false
            }
            binding.rgOptions.addView(radioButton)
        }

        userAnswers[question.id]?.let { saved ->
            for (i in 0 until binding.rgOptions.childCount) {
                val rb = binding.rgOptions.getChildAt(i) as RadioButton
                if (rb.text.toString() == saved) {
                    rb.isChecked = true
                    break
                }
            }
        }

        binding.rgOptions.setOnCheckedChangeListener { _, _ ->
            updateNextButtonState()
        }

        updateNavigationButtons()
        updateNextButtonState()
    }

    private fun saveCurrentAnswer() {
        if (questions.isEmpty() || currentQuestionIndex !in questions.indices) return

        val question = questions[currentQuestionIndex]
        val selectedId = binding.rgOptions.checkedRadioButtonId

        if (selectedId == -1) {
            userAnswers.remove(question.id)
            return
        }

        val selectedRadioButton = binding.rgOptions.findViewById<RadioButton>(selectedId)
        val selectedText = selectedRadioButton?.text?.toString()

        if (selectedText != null) {
            userAnswers[question.id] = selectedText
        }
    }

    private fun updateNextButtonState() {
        val hasSelection = binding.rgOptions.checkedRadioButtonId != -1
        binding.btnNext.isEnabled = if (isShowingExplanation) true else hasSelection

        binding.btnNext.text = when {
            isShowingExplanation -> "➡️ Продолжить"
            currentQuestionIndex == questions.size - 1 -> if (hasSelection) "✅ Завершить" else "Выберите ответ"
            else -> "Далее ➡️"
        }
    }

    private fun updateNavigationButtons() {
        binding.btnPrev.isEnabled = currentQuestionIndex > 0 && !isShowingExplanation
    }

    private fun finishTest(lessonId: String) {
        saveCurrentAnswer()

        var correctCount = 0
        questions.forEach { question ->
            val userAnswer = userAnswers[question.id]
            if (userAnswer != null && userAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)) {
                correctCount++
            }
        }

        val progress = if (questions.isNotEmpty()) {
            correctCount.toFloat() / questions.size
        } else {
            0f
        }

        val result = TestResult(
            lessonId = lessonId,
            score = correctCount,
            totalQuestions = questions.size,
            answers = userAnswers.toMap()
        )

        viewModel.saveTestResult(result)
        viewModel.updateLessonProgress(lessonId, progress)
        viewModel.syncResultWithServer(result)
        showResult(result)
    }

    private fun showResult(result: TestResult) {
        binding.tvQuestion.visibility = View.GONE
        binding.rgOptions.visibility = View.GONE
        binding.btnPrev.visibility = View.GONE
        binding.btnNext.visibility = View.GONE
        binding.tvExplanation.visibility = View.GONE
        binding.layoutResult.visibility = View.VISIBLE

        val percentage = (result.score * 100) / result.totalQuestions
        binding.tvResultScore.text = "$percentage%"
        binding.tvResultMessage.text = when {
            percentage >= 80 -> "🎉 Отлично! Вы прекрасно усвоили материал."
            percentage >= 50 -> "👍 Хорошо! Но есть, куда расти."
            else -> "📚 Рекомендуем повторить урок и пройти тест ещё раз."
        }
    }

    private fun restartTest(lessonId: String) {
        currentQuestionIndex = 0
        userAnswers.clear()
        isShowingExplanation = false
        binding.tvQuestion.visibility = View.VISIBLE
        binding.rgOptions.visibility = View.VISIBLE
        binding.btnPrev.visibility = View.VISIBLE
        binding.btnNext.visibility = View.VISIBLE
        binding.layoutResult.visibility = View.GONE
        setupUI()
        loadQuestion(0)
    }
    private fun showWrongAnswerWithExplanation(question: Question, userAnswer: String) {
        Log.d("TestDebug", "пояснение: ${question.questionText.take(50)}...")

        lifecycleScope.launch {
            try {
                binding.rgOptions.isEnabled = false
                isShowingExplanation = true
                updateNextButtonState()

                binding.tvExplanation.apply {
                    text = "⏳ Готовим пояснение..."
                    visibility = View.VISIBLE
                }
                binding.rgOptions.visibility = View.GONE

                Log.d("TestDebug", "запрос пояснения на сервер")
                val response = try {
                    api.getExplanation(
                        lessonId = args.lessonId,
                        request = LessonApi.ExplainRequest(
                            question.questionText,
                            userAnswer,
                            question.correctAnswer
                        )
                    )
                } catch (e: Exception) {
                    Log.e("TestDebug", "ошибка сети: ${e.message}", e)
                    mapOf("explanation" to "Правильный ответ: \"${question.correctAnswer}\". Попробуйте ещё раз! 💪")
                }

                val explanationText = response["explanation"] ?: "Ошибка загрузки"
                Log.d("TestDebug", explanationText.take(100))

                binding.tvExplanation.text = explanationText

            } catch (e: Exception) {
                Log.e("TestDebug", "error: ${e.message}", e)
                Toast.makeText(requireContext(), "Ошибка показа пояснения", Toast.LENGTH_SHORT).show()

                binding.tvExplanation.text = "Правильный ответ: \"${question.correctAnswer}\""
                binding.tvExplanation.visibility = View.VISIBLE
                isShowingExplanation = true
                updateNextButtonState()
            }
        }
    }

    private fun goToNextQuestion() {
        Log.d("TestDebug", "goToNextQuestion- current $currentQuestionIndex, total ${questions.size}")

        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            loadQuestion(currentQuestionIndex)
        } else {
            finishTest(args.lessonId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}