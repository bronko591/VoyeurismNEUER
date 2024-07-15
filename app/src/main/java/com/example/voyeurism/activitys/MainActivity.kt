package com.example.voyeurism.activitys

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.mediarouter.app.MediaRouteButton
import androidx.preference.PreferenceManager
import com.example.voyeurism.R
import com.example.voyeurism.fragments.ChaturbateFragment
import com.example.voyeurism.fragments.FavoritesFragment
import com.google.android.gms.cast.framework.AppVisibilityListener
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.cast.framework.CastStateListener
import com.google.android.gms.cast.framework.IntroductoryOverlay
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.material.navigation.NavigationView

@Suppress("UNUSED_EXPRESSION")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    // drawerNavigation Controls
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var toolBar: Toolbar
    // extra loaderContent

    // chromeCast Controls
    private lateinit var sessionManager: SessionManager
    private lateinit var castContext: CastContext
    private lateinit var mediaRouteButton: MediaRouteButton
    private lateinit var mediaRouterButton: MenuItem
    private lateinit var castSession: CastSession
    private lateinit var introductoryOverlay : IntroductoryOverlay
    private lateinit var castStateListener: CastStateListener
    private val appVisibilityListener = object: AppVisibilityListener {
        override fun onAppEnteredBackground() {
            TODO("Not yet implemented")
        }

        override fun onAppEnteredForeground() {
            TODO("Not yet implemented")
        }
    }
    private val sessionManagerListener: SessionManagerListenerImpl = SessionManagerListenerImpl()
    private inner class SessionManagerListenerImpl : SessionManagerListener<CastSession?> {
        override fun onSessionEnding(p0: CastSession) {
            TODO("Not yet implemented")
        }
        override fun onSessionResumeFailed(p0: CastSession, p1: Int) {
            TODO("Not yet implemented")
        }
        override fun onSessionResuming(p0: CastSession, p1: String) {
            TODO("Not yet implemented")
        }
        override fun onSessionStartFailed(p0: CastSession, p1: Int) {
            TODO("Not yet implemented")
        }
        override fun onSessionStarting(p0: CastSession) {
            TODO("Not yet implemented")
        }
        override fun onSessionSuspended(p0: CastSession, p1: Int) {
            TODO("Not yet implemented")
        }
        override fun onSessionStarted(p0: CastSession, p1: String) {
            TODO("Not yet implemented")
        }
        override fun onSessionEnded(p0: CastSession, p1: Int) {
            TODO("Not yet implemented")
        }
        override fun onSessionResumed(p0: CastSession, p1: Boolean) {
            TODO("Not yet implemented")
        }
    }

    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mediaRouteButton = findViewById(R.id.media_route_button)
        castContext = CastContext.getSharedInstance(this)
        sessionManager = CastContext.getSharedInstance(this).sessionManager
        CastButtonFactory.setUpMediaRouteButton(this, mediaRouteButton)
        initDrawer()
        initDarkMode()
    }

    // navigationItemSelection ----->
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.setChecked(true)
        when(item.itemId){
            R.id.favorite -> {
                supportFragmentManager.commit {
                    replace(R.id.fragmentContainerView, FavoritesFragment.newInstance())
                    setReorderingAllowed(true)
                    addToBackStack(null)
                }
                setActionBarTitle("Favorites", "")
                writeToast("Open Favorites")
            }
            R.id.chaturbate_featured -> {
                setActionBarTitle("Featured Cams", "")
                replaceFragment("gender","Featured")
                writeToast("Open Featured Cams")
            }
            R.id.chaturbate_couple -> {
                setActionBarTitle("Couple Cams", "")
                replaceFragment("gender", "Couple")
                writeToast("Open Couple Cams")
            }
            R.id.chaturbate_trans -> {
                setActionBarTitle("Transe Cams", "")
                replaceFragment("gender", "Transe")
                writeToast("Open Transe Cams")
            }
            R.id.chaturbate_female -> {
                setActionBarTitle("Female Cams", "")
                replaceFragment("gender", "Female")
                writeToast("Open Female Cams")
            }
            R.id.chaturbate_male -> {
                setActionBarTitle("Male Cams", "")
                replaceFragment("gender","Male")
                writeToast("Open Male Cams")
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                setActionBarTitle("Settings", "")
                //title = "Settings"
                writeToast("Open Settings")
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // onOptions
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //menuInflater.inflate(R.menu.main_menu, menu)
        when (item.itemId) {
            //R.id.app_bar_search -> return false
            else -> {}
        }
         if (actionBarDrawerToggle.onOptionsItemSelected(item)) { true }
        //searchView.setOnQueryTextListener(queryTextListener)
        return super.onOptionsItemSelected(item)
    }

    // utilitys --> actonBar title, replace Fragment, write Toast
    private fun setActionBarTitle(title:String, subtitle:String){
        supportActionBar?.apply {
            setTitle(title)
            setSubtitle(subtitle)
        }
    }

    private fun replaceFragment(modus: String, parameter:String) {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainerView, ChaturbateFragment.newInstance(modus, parameter))
            setReorderingAllowed(true)
            //supportActionBar?.title = parameter
            addToBackStack(null)
        }
    }

    private fun writeToast(message: String) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }

    // override´s ------>
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes")
                { _: DialogInterface, _: Int -> super.onBackPressed()
                }.setNegativeButton("No", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Define the listener.
        val expandListener = object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Do something when the action item collapses.
                return true // Return true to collapse the action view.
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // Do something when it expands.
                return true // Return true to expand the action view.
            }
        }
        // Get the MenuItem for the action item.
        //val actionMenuItem = menu.findItem(R.id.app_bar_search)

        // Assign the listener to that action item.
        //actionMenuItem?.setOnActionExpandListener(expandListener)

        // For anything else you have to do when creating the options menu,
        // do the following:
        return super.onCreateOptionsMenu(menu)


    }

    // init´s --> drawer, navigationView, toolbar
    private fun initDrawer() {
        initToolbar()
        initNavigationView()
        drawerLayout = findViewById(R.id.drawerLayout)
        drawerLayout.apply { addDrawerListener(
            ActionBarDrawerToggle(this@MainActivity, drawerLayout, toolBar,0,0).apply
            {
                syncState()
                drawerLayout.isClickable = true
                isDrawerIndicatorEnabled = true
                drawerArrowDrawable.color = ContextCompat.getColor(this@MainActivity, R.color.white) })
            isClickable = true
        }
        navigationView.apply { itemIconTintList = null }.setNavigationItemSelectedListener(this)
        setActionBarTitle("Featured Cams", "")
    }

    private fun initNavigationView() { navigationView = findViewById(R.id.navigationView) }

    private fun initToolbar(){
        toolBar = findViewById(R.id.toolBar)
        setSupportActionBar(toolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    // init´s --> darkMode initiation
    private fun initDarkMode() {
        val darkModeValues = resources.getStringArray(R.array.dark_mode_values)
        val pref: String = PreferenceManager.getDefaultSharedPreferences(this).apply {
            getString(R.string.dark_mode)
            getString(R.string.dark_mode_def_value)
        }.toString()
        if (pref == darkModeValues[0]) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        if (pref == darkModeValues[1]) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        if (pref == darkModeValues[2]) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    // chromeCast
    override fun onDestroy() {
        super.onDestroy()
      //  CastContext.getSharedInstance(this).removeAppVisibilityListener(appVisibilityListener)
       // CastContext.getSharedInstance(this).removeCastStateListener(castStateListener)
        //CastContext.getSharedInstance(this).sessionManager.removeSessionManagerListener(sessionManager)
    }

    private fun showIntroductoryOverlay() {
        introductoryOverlay.remove()
        if (mediaRouterButton.isVisible) {
            Handler().post(Runnable() {
                fun run() {
                    introductoryOverlay = IntroductoryOverlay.Builder(
                        MainActivity(),
                        mediaRouterButton
                    )
                        .setTitleText("Introduction text")
                        .setOverlayColor(R.color.purple_700)
                        .setSingleTime()
                        .setOnOverlayDismissedListener { introductoryOverlay = null!! }.build();
                    introductoryOverlay.show()
                }
            })
        }
    }

    private fun onCastStateChanged(newState: Int) {
        if (newState != CastState.NO_DEVICES_AVAILABLE) {
            showIntroductoryOverlay()
        }
    }

    override fun onResume() {
        super.onResume()
        //castSession = sessionManager.currentCastSession!!
        //sessionManager.addSessionManagerListener(sessionManagerListener, CastSession::class.java)
    }

    override fun onPause() {
        super.onPause()
        //sessionManager.removeSessionManagerListener(sessionManagerListener, CastSession<SessionManager>::class.java)
    //    castSession = null!!
    }

}
