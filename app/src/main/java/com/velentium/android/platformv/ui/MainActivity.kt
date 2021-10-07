package com.velentium.android.platformv.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mysecondapp.ui.GameFragment
import com.example.mysecondapp.ui.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.velentium.android.platformv.R
import com.velentium.android.platformv.app.PlatformApplication
import com.velentium.android.platformv.databinding.ActivityMainBinding
import com.velentium.android.platformv.ui.viewmodels.ConnectionViewModel
import com.velentium.android.platformv.uicontrols.common.dialogs.SplashScreenDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    internal lateinit var bottomNavigationView: BottomNavigationView

    private val viewModel by viewModel<ConnectionViewModel>()

    private var splashDialog: SplashScreenDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        splashDialog = SplashScreenDialogFragment.newInstance(
            logoImageResId = com.velentium.android.platformv.uicontrols.R.drawable.ic_platform_logo,
            backgroundColorResId = R.color.accent_color,
            spinnerTintColorResId = R.color.white,
            hasSpinner = true
        )

        bottomNavigationView = findViewById(R.id.bottomNavigation)
        val homeFragment = HomeFragment()
        val gameFragment = GameFragment()

        bottomNavigationView.setOnItemReselectedListener {
            Log.d("item","${it.itemId}")
            when(it.itemId){
                R.id.home_menu->setCurrentFragment(homeFragment)
                R.id.game_menu->setCurrentFragment(gameFragment)
                R.id.flag_menu->navigateToFlagActivity()

            }
            true
        }
    }

    override fun onStart() {
        super.onStart()
        splashDialog?.showSplashScreen(supportFragmentManager)

        viewModel.onServiceInitialized.observe(this) {
            it.unhandledValue?.let {
                binding.root.postDelayed({
                    splashDialog?.dismiss()
                    splashDialog = null
                }, 3000L)
            }
        }
        viewModel.promptForPermissions(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as? PlatformApplication)?.onDestroy()
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView,fragment)
            commit()
        }

    private fun navigateToFlagActivity(){
        val intent = Intent(this,DisplayFlagList::class.java)
        startActivity(intent)
    }
}