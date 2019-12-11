package com.example.studypal

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){
    private lateinit var db:FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.visibility = View.GONE
        topToolbar.visibility = View.GONE
        setSupportActionBar(topToolbar)
        db = FirebaseFirestore.getInstance()

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        setupBottomNavMenu(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->

            // Fragments that we do not want the navigation elements to show up
            val navigationVisibleIds = setOf<Int>( R.id.navigation_home,R.id.navigation_profile,
                R.id.navigation_settings, R.id.navigation_social)
            if(!navigationVisibleIds.contains(destination.id)) {
            bottom_navigation.visibility = View.GONE
            topToolbar.visibility = View.GONE
            }
            else{
                bottom_navigation.visibility = View.VISIBLE
                topToolbar.visibility = View.VISIBLE
            }
        }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Verify the action and get the query
        handleIntent(intent!!)
    }
    private fun search(query:String) {
        Log.d("Main", query)
        val matchingUserList:MutableList<User> = mutableListOf()
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if(document.data["username"].toString().contains(query,ignoreCase = true)) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        matchingUserList.add(document.toObject(User::class.java))
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
        Log.d(TAG, matchingUserList.toString())
    }
    private fun setupBottomNavMenu(navController: NavController) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.setupWithNavController(navController)
    }

    //setting menu in action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu,menu)

        // Get the SearchView and set the searchable configuration
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        (menu!!.findItem(R.id.menu_search).actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isIconifiedByDefault = false // Do not iconify the widget; expand it by default
        }

        return super.onCreateOptionsMenu(menu)
    }
    // actions on click menu items
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.signOut -> {
            val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
            val action = HomeFragmentDirections.actionNavigationHomeToMainFragment("signOut")
            navController.navigate(action)
            // User chose the "Print" item
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            if (query != null) {
                search(query)
            }
        }
    }
    companion object {
        private const val TAG = "MainActivity"
    }

}
