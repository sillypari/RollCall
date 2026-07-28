package com.simpleattendance.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.PathInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.simpleattendance.R
import com.simpleattendance.databinding.ActivityMainBinding
import com.simpleattendance.ui.createclass.CreateClassActivity
import com.simpleattendance.util.HeroTransitionLauncher
import com.simpleattendance.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

import com.simpleattendance.ui.classlist.ClassListViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ClassListViewModel by viewModels()
    
    @Inject
    lateinit var hapticUtils: HapticUtils
    
    private var currentPage = 0
    private var searchInteractionActive = false
    private val emphasizedInterpolator = PathInterpolator(0.2f, 0f, 0f, 1f)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewPager()
        setupBottomNavigation()
        setupFab()
        observeViewModel()
        binding.brandWordmark.post { revealWordmark() }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Pulse FAB when class list is empty and we are on first page
                    if (state.isEmpty && !state.isLoading && binding.viewPager.currentItem == 0) {
                        com.simpleattendance.util.AnimationUtils.startPulsing(binding.fab)
                    } else {
                        com.simpleattendance.util.AnimationUtils.stopPulsing(binding.fab)
                    }
                }
            }
        }
    }
    
    private fun setupViewPager() {
        val pagerAdapter = MainPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.setPageTransformer { page, position ->
            val distance = abs(position).coerceIn(0f, 1f)
            page.alpha = if (abs(position) >= 1f) 0f else 1f - (distance * 0.12f)
            page.scaleX = 1f - (distance * 0.025f)
            page.scaleY = 1f - (distance * 0.025f)
            page.translationX = 0f
        }
        
        // Sync ViewPager with BottomNavigation
        binding.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position != currentPage) {
                    hapticUtils.lightTap()
                }
                currentPage = position
                if (position != 1) {
                    setSearchInteractionActive(false)
                }
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
                    binding.viewPager.setCurrentItem(0, true)
                    true
                }
                R.id.nav_history -> {
                    binding.viewPager.setCurrentItem(1, true)
                    true
                }
                R.id.nav_settings -> {
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
            com.simpleattendance.util.AnimationUtils.applySpringScale(it)
            HeroTransitionLauncher.start(
                activity = this,
                intent = Intent(this, CreateClassActivity::class.java),
                sourceView = it
            )
        }
    }
    
    private fun animateToolbarTitle(position: Int) {
        binding.brandWordmark.animate().cancel()
        binding.pageTitle.animate().cancel()
        if (position == 0) {
            binding.pageTitle.animate()
                .alpha(0f)
                .translationY(-6f * resources.displayMetrics.density)
                .setDuration(150L)
                .withEndAction {
                    binding.pageTitle.visibility = View.GONE
                    binding.brandWordmark.visibility = View.VISIBLE
                    revealWordmark()
                }
                .start()
        } else {
            val title = if (position == 1) getString(R.string.nav_history) else getString(R.string.settings)
            binding.brandWordmark.animate()
                .alpha(0f)
                .translationY(-6f * resources.displayMetrics.density)
                .setDuration(150L)
                .withEndAction {
                    binding.brandWordmark.visibility = View.GONE
                    binding.pageTitle.text = title
                    binding.pageTitle.visibility = View.VISIBLE
                    binding.pageTitle.alpha = 0f
                    binding.pageTitle.translationY = 9f * resources.displayMetrics.density
                    binding.pageTitle.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(280L)
                        .setInterpolator(emphasizedInterpolator)
                        .start()
                }
                .start()
        }
    }

    fun setSearchInteractionActive(active: Boolean) {
        if (searchInteractionActive == active) return
        searchInteractionActive = active
        binding.viewPager.isUserInputEnabled = !active
        binding.bottomNavigation.animate().cancel()
        binding.bottomDockScrim.animate().cancel()

        if (active) {
            binding.bottomNavigation.visibility = View.GONE
            binding.bottomDockScrim.visibility = View.GONE
        } else {
            val offset = 18f * resources.displayMetrics.density
            binding.bottomDockScrim.visibility = View.VISIBLE
            binding.bottomDockScrim.alpha = 0f
            binding.bottomDockScrim.animate()
                .alpha(1f)
                .setDuration(180L)
                .start()

            binding.bottomNavigation.visibility = View.VISIBLE
            binding.bottomNavigation.alpha = 0f
            binding.bottomNavigation.translationY = offset
            binding.bottomNavigation.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(240L)
                .setInterpolator(emphasizedInterpolator)
                .start()
        }
    }

    private fun revealWordmark() {
        val density = resources.displayMetrics.density
        binding.brandWordmark.visibility = View.VISIBLE
        binding.brandWordmark.alpha = 1f
        binding.brandWordmark.translationY = 0f
        binding.brandRoll.alpha = 0f
        binding.brandRoll.scaleX = 0.94f
        binding.brandRoll.scaleY = 0.94f
        binding.brandCall.alpha = 0f
        binding.brandCall.translationX = -14f * density

        binding.brandRoll.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(260L)
            .setInterpolator(emphasizedInterpolator)
            .start()
        binding.brandCall.animate()
            .alpha(1f)
            .setDuration(180L)
            .start()

        SpringAnimation(binding.brandCall, DynamicAnimation.TRANSLATION_X, 0f).apply {
            spring = SpringForce(0f).apply {
                dampingRatio = 0.78f
                stiffness = 520f
            }
        }.start()
    }
    
    private fun updateFabVisibility(position: Int) {
        // Hide FAB on History and Settings tabs
        if (position == 0) {
            binding.fab.show()
            // Re-check empty state to restart pulsing if needed
            val state = viewModel.uiState.value
            if (state.isEmpty && !state.isLoading) {
                com.simpleattendance.util.AnimationUtils.startPulsing(binding.fab)
            }
        } else {
            binding.fab.hide()
            com.simpleattendance.util.AnimationUtils.stopPulsing(binding.fab)
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
