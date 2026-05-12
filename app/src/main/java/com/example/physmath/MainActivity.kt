package com.example.physmath

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.physmath.databinding.ActivityMainBinding
import com.example.physmath.utils.ThemeManager
import com.example.physmath.utils.ThemeMode

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val bottomNavMenuIds = setOf(
                R.id.lessonsFragment,
                R.id.testsListFragment,
                R.id.settingsFragment
            )

            binding.bottomNavigation.visibility =
                if (destination.id in bottomNavMenuIds) View.VISIBLE else View.GONE
        }

        binding.bottomNavigation.setupWithNavController(navController)
    }
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)

        if (ThemeManager.getCurrentMode() == ThemeMode.SYSTEM) {
            ThemeManager.applyTheme(this, ThemeMode.SYSTEM)
        }
    }
}