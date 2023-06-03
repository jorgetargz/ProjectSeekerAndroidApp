package com.jorgetargz.projectseeker.framework.main

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.framework.common.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.topAppBar))
        setupNavController()
        setupDrawer()
        configBackButton()
        observeViewModel()
        viewModel.checkNumberOfUnReadMessages()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logoutDone.collect { logOutDone ->
                    logOutDone?.let {
                        if (logOutDone) {
                            navController.navigate(R.id.loginFragment)
                            viewModel.logoutDoneHandled()
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.numberOfUnreadMessages.collect { unReadMessages ->
                    val menuItem =
                        findViewById<NavigationView>(R.id.nav_view).menu.findItem(R.id.channelFragment)
                    if (unReadMessages != 0) {
                        menuItem.title =
                            "${getString(R.string.channels_fragment_title)} ($unReadMessages)"
                    } else {
                        menuItem.title = getString(R.string.channels_fragment_title)
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupNavController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.findNavController()
    }

    private fun setupDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.channelFragment,
                R.id.chatFragment,
                R.id.profileFragment,
                R.id.listProjectsFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                }

                R.id.profileFragment -> {
                    navController.navigate(R.id.profileFragment)
                }

                R.id.listProjectsFragment -> {
                    navController.navigate(R.id.listProjectsFragment)
                }

                R.id.logout_menu -> {
                    logout()
                }

                R.id.logout_everywhere_menu -> {
                    logoutEverywhere()
                }

                R.id.channelFragment -> {
                    val action = R.id.action_global_channelFragment
                    navController.navigate(action)
                }

                R.id.dev_github_menu -> {
                    val myProfileURI = Uri.parse(Constants.DEV_GITHUB_URL)
                    val intent = Intent(Intent.ACTION_VIEW, myProfileURI)
                    startActivity(intent)
                }

                R.id.dev_linkedin_menu -> {
                    val myProfileURI = Uri.parse(Constants.DEV_LINKDIN_URL)
                    val intent = Intent(Intent.ACTION_VIEW, myProfileURI)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun logout() {
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton(getString(R.string.yes_button)) { _, _ ->
            viewModel.logout()
        }
        builder.setNegativeButton(getString(R.string.no_button)) { _, _ -> }
        builder.setTitle(getString(R.string.logout_exit_title))
        builder.setMessage(getString(R.string.logout_exit_message))
        builder.create().show()
    }

    private fun logoutEverywhere() {
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton(getString(R.string.yes_button)) { _, _ ->
            viewModel.logoutEverywhere()
        }
        builder.setNegativeButton(getString(R.string.no_button)) { _, _ -> }
        builder.setTitle(getString(R.string.logout_everywhere_title))
        builder.setMessage(getString(R.string.logout_everywhere_message))
        builder.create().show()
    }

    private fun configBackButton() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else if (navController.currentDestination?.id == R.id.loginFragment ||
                navController.currentDestination?.id == R.id.homeFragment
            ) {
                finish()
            } else {
                navController.navigateUp()
            }
        }
    }
}