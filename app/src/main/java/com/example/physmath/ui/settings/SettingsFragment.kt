package com.example.physmath.ui.settings

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.physmath.R
import com.example.physmath.databinding.FragmentSettingsBinding
import com.example.physmath.databinding.FragmentSettingsBinding.*
import com.example.physmath.utils.ThemeManager
import com.example.physmath.utils.ThemeMode
import com.example.physmath.utils.ThemePreferences
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportProgress(it) }
    }
    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importProgress(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = bind(view)

        setupThemeIcons()

        viewModel.toast.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        binding.btnExport.setOnClickListener {
            exportLauncher.launch("physmath_backup_${System.currentTimeMillis()}.json")
        }

        binding.btnImport.setOnClickListener {
            importLauncher.launch(arrayOf("application/json"))
        }

        binding.btnFeedback.setOnClickListener {
            val action = SettingsFragmentDirections
                .actionSettingsFragmentToFeedbackFragment()
            findNavController().navigate(action)
        }
        setupThemeIcons()
        setupOfflineCard()
    }

    private fun applyTheme(mode: ThemeMode) {
        Log.d("ThemeDebug", "тема: $mode")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                ThemePreferences.setThemeMode(requireContext(), mode)
                Log.d("ThemeDebug", "соранено в ДС")

                ThemeManager.applyTheme(requireContext(), mode)
                Log.d("ThemeDebug", "тема сохранена")


            } catch (e: Exception) {
                Log.e("ThemeDebug", "ошибка: ${e.message}", e)
            }
        }
    }

    private fun setupThemeIcons() {
        viewLifecycleOwner.lifecycleScope.launch {

            ThemePreferences.getThemeMode(requireContext()).collect { savedMode ->
                updateThemeIconTints(savedMode)
            }
        }

        binding.btnThemeSystem.setOnClickListener {
            Log.d("ThemeDebug", "SYSTEM")
            applyTheme(ThemeMode.SYSTEM)
        }
        binding.btnThemeLight.setOnClickListener {
            Log.d("ThemeDebug", "LIGHT")
            applyTheme(ThemeMode.LIGHT)
        }
        binding.btnThemeDark.setOnClickListener {
            Log.d("ThemeDebug", "DARK")
            applyTheme(ThemeMode.DARK)
        }
    }

    private fun updateThemeIconTints(activeMode: ThemeMode) {
        if (_binding == null) return

        val activeColor = ContextCompat.getColor(requireContext(), R.color.accent)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.text_hint)

        binding.btnThemeSystem.imageTintList = ColorStateList.valueOf(
            if (activeMode == ThemeMode.SYSTEM) activeColor else inactiveColor
        )
        binding.btnThemeLight.imageTintList = ColorStateList.valueOf(
            if (activeMode == ThemeMode.LIGHT) activeColor else inactiveColor
        )
        binding.btnThemeDark.imageTintList = ColorStateList.valueOf(
            if (activeMode == ThemeMode.DARK) activeColor else inactiveColor
        )
    }
    private fun setupOfflineCard() {
        // Прогресс
        viewModel.downloadProgress.observe(viewLifecycleOwner) { progress ->
            binding.pbDownload.visibility = if (progress.current in 0..100) View.VISIBLE else View.GONE
            binding.tvDownloadStatus.visibility = View.VISIBLE
            binding.pbDownload.progress = progress.current
            binding.tvDownloadStatus.text = progress.message
            binding.btnDownloadAll.isEnabled = progress.current != 100 && progress.current >= 0
        }

        // Статус "всё скачано"
        viewModel.isAllDownloaded.observe(viewLifecycleOwner) { isDownloaded ->
            binding.btnDownloadAll.text = if (isDownloaded) {
                "✅ Всё загружено"
            } else {
                "📥 Загрузить всё для офлайн"
            }
            binding.btnDownloadAll.isEnabled = !isDownloaded
        }

        // Кнопка "Загрузить всё"
        binding.btnDownloadAll.setOnClickListener {
            viewModel.downloadAllContent()
        }

        // Кнопка "Очистить кэш"
        binding.btnDeleteAll.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("🗑️ Очистить кэш?")
                .setMessage("Удалить все скачанные уроки? Сетевой трафик не будет экономиться.")
                .setPositiveButton("Удалить") { _, _ ->
                    viewModel.deleteAllDownloadedContent()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}