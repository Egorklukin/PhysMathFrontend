package com.example.physmath.ui.feedback

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.physmath.R
import com.example.physmath.data.network.RetrofitClient
import com.example.physmath.databinding.FragmentFeedbackBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

data class FeedbackRequest(
    val userName: String? = null,
    val userEmail: String? = null,
    val requestType: String,
    val topic: String,
    val description: String? = null,
    val deviceInfo: String? = null
)

class FeedbackFragment : Fragment(R.layout.fragment_feedback) {

    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedbackBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        setupForm()
    }

    private fun setupForm() {
        binding.btnSubmit.setOnClickListener {
            val topic = binding.etTopic.text?.toString()?.trim()

            if (topic.isNullOrEmpty()) {
                binding.etTopic.error = "Укажите тему пожелания"
                binding.etTopic.requestFocus()
                return@setOnClickListener
            }

            val email = binding.etEmail.text?.toString()?.trim()
            if (!email.isNullOrEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Введите корректную почту"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            submitFeedback(
                requestType = if (binding.rbLesson.isChecked) "lesson" else "test",
                topic = topic,
                description = binding.etDescription.text?.toString()?.trim(),
                email = email?.ifEmpty { null }
            )
        }
    }

    private fun submitFeedback(
        requestType: String,
        topic: String,
        description: String?,
        email: String?
    ) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceInfo = "${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE})"

                val request = FeedbackRequest(
                    requestType = requestType,
                    topic = topic,
                    description = description,
                    userEmail = email,
                    deviceInfo = deviceInfo
                )

                RetrofitClient.api.submitFeedback(request)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "✅ Спасибо! Ваше пожелание принято.", Toast.LENGTH_LONG).show()
                    clearForm()
                    findNavController().navigateUp()
                }

            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "❌ Ошибка сервера. Попробуйте позже.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "❌ Нет подключения к интернету", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "❌ Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                }
            }
        }
    }

    private fun clearForm() {
        binding.etTopic.text?.clear()
        binding.etDescription.text?.clear()
        binding.etEmail.text?.clear()
        binding.rbLesson.isChecked = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}