package com.simpleattendance.ui.main

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.simpleattendance.R
import com.simpleattendance.databinding.ActivityMainBinding
import com.simpleattendance.ui.createclass.CreateClassActivity
import com.simpleattendance.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    @Inject
    lateinit var hapticUtils: HapticUtils
    
    private var currentTitle = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewPager()
        setupBottomNavigation()
        setupFab()
        
        // Set initial title
        currentTitle = getString(R.string.app_name)
    }
    
    private fun setupViewPager() {
        val pagerAdapter = MainPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        
        // Sync ViewPager with BottomNavigation
        binding.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                hapticUtils.lightTap()
                binding.bottomNavigation.menu.getItem(position).isChecked = true
                animateToolbarTitle(position)
                updateFabVisibility(position)
            }
        })
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_attendance -> {
                    hapticUtils.lightTap()
                    binding.viewPager.setCurrentItem(0, true)
                    true
                }
                R.id.nav_history -> {
                    hapticUtils.lightTap()
                    binding.viewPager.setCurrentItem(1, true)
                    true
                }
                R.id.nav_settings -> {
                    hapticUtils.lightTap()
                    binding.viewPager.setCurrentItem(2, true)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupFab() {
        binding.fab.setOnClickListener {
            hapticUtils.mediumImpact()
            startActivity(Intent(this, CreateClassActivity::class.java))
        }
    }
    
    private fun animateToolbarTitle(position: Int) {
        val newTitle = when (position) {
            0 -> getString(R.string.app_name)
            1 -> "History"
            2 -> "Settings"
            else -> getString(R.string.app_name)
        }
        
        if (newTitle == currentTitle) return
        currentTitle = newTitle
        
        // Fade out, change text, fade in animation
        ObjectAnimator.ofFloat(binding.toolbar, "alpha", 1f, 0f).apply {
            duration = 100
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        
        binding.toolbar.postDelayed({
            binding.toolbar.title = newTitle
            ObjectAnimator.ofFloat(binding.toolbar, "alpha", 0f, 1f).apply {
                duration = 150
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }, 100)
    }
    
    private fun updateFabVisibility(position: Int) {
        // Hide FAB on History and Settings tabs
        if (position == 0) {
            binding.fab.show()
        } else {
            binding.fab.hide()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Ensure correct tab is selected
        binding.bottomNavigation.selectedItemId = when (binding.viewPager.currentItem) {
            0 -> R.id.nav_attendance
            1 -> R.id.nav_history
            2 -> R.id.nav_settings
            else -> R.id.nav_attendance
        }
    }
}