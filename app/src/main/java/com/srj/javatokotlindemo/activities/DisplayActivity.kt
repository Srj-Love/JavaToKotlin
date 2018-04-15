package com.srj.javatokotlindemo.activities

import android.content.Context
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import com.srj.javatokotlindemo.R
import com.srj.javatokotlindemo.adapters.DisplayAdapter
import com.srj.javatokotlindemo.app.Constants
import com.srj.javatokotlindemo.app.Util
import com.srj.javatokotlindemo.models.Repository
import com.srj.javatokotlindemo.models.SearchResponse
import com.srj.javatokotlindemo.retrofit.GithubAPIService
import com.srj.javatokotlindemo.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.activity_display.*
import kotlinx.android.synthetic.main.header.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class DisplayActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mRecyclerView: RecyclerView? = null
    private var mDisplayAdapter: DisplayAdapter? = null
    private var browsedRepositories: List<Repository>? = null
    private var mService: GithubAPIService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Showing Browsed Results"

        setUpUserName()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView!!.layoutManager = layoutManager

        mService = RetrofitClient.getGithubAPIService()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        drawerLayout!!.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        val intent = intent
        if (intent.getIntExtra(Constants.KEY_QUERY_TYPE, -1) == Constants.SEARCH_BY_REPO) {
            val queryRepo = intent.getStringExtra(Constants.KEY_REPO_SEARCH)
            val repoLanguage = intent.getStringExtra(Constants.KEY_LANGUAGE)
            fetchRepositories(queryRepo, repoLanguage)
        } else {
            val githubUser = intent.getStringExtra(Constants.KEY_GITHUB_USER)
            fetchUserRepositories(githubUser)
        }
    }

    private fun setUpUserName() {
        val sp = getSharedPreferences(Constants.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val personName = sp.getString(Constants.KEY_PERSON_NAME,"User")
        val headerView = navigatinView.getHeaderView(0)
        headerView.txvName.text = personName

    }

    private fun fetchUserRepositories(githubUser: String) {

        mService!!.searchRepositoriesByUser(githubUser).enqueue(object: Callback<List<Repository>> {
            override fun onFailure(call: Call<List<Repository>>?, t: Throwable) {
                Util.showMessage(this@DisplayActivity,t.message)

            }

            override fun onResponse(call: Call<List<Repository>>?, response: Response<List<Repository>>) {

                if (response.isSuccessful){
                    Log.i(TAG," Post loaded from API "+ response)
                    browsedRepositories = response.body()
                    if (browsedRepositories!!.isNotEmpty()){
                        setupRecyclerView(browsedRepositories)
                    }else{
                        Util.showMessage(this@DisplayActivity,"No Data found ")
                    }
                }else{
                    Util.showErrorMessage(this@DisplayActivity,response.errorBody()!!)
                }
            }

        })
    }

    private fun fetchRepositories(queryRepo: String, repoLanguage: String?) {
        var queryRepo = queryRepo

        val query = HashMap<String, String>()

        if (repoLanguage != null && !repoLanguage.isEmpty())
            queryRepo += " language:$repoLanguage"
        query["q"] = queryRepo

        mService!!.searchRepositories(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    Log.i(TAG, "posts loaded from API $response")

                    browsedRepositories = response.body()!!.items

                    if (browsedRepositories!!.size > 0)
                        setupRecyclerView(browsedRepositories)
                    else
                        Util.showMessage(this@DisplayActivity, "No Items Found")

                } else {
                    Log.i(TAG, "error $response")
                    Util.showErrorMessage(this@DisplayActivity, response.errorBody())
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Util.showMessage(this@DisplayActivity, t.toString())
            }
        })
    }

    private fun setupRecyclerView(items: List<Repository>?) {
        mDisplayAdapter = DisplayAdapter(this, items)
        mRecyclerView!!.adapter = mDisplayAdapter
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {

        menuItem.isChecked = true
        closeDrawer()

        when (menuItem.itemId) {

            R.id.item_bookmark -> {
                showBookmarks()
                supportActionBar!!.title = "Showing Bookmarks"
            }

            R.id.item_browsed_results -> {
                showBrowsedResults()
                supportActionBar!!.title = "Showing Browsed Results"
            }
        }

        return true
    }

    private fun showBrowsedResults() {
        mDisplayAdapter!!.swap(browsedRepositories)
    }

    private fun showBookmarks() {

    }

    private fun closeDrawer() {
        drawerLayout!!.closeDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START))
            closeDrawer()
        else {
            super.onBackPressed()
        }
    }

    companion object {

        private val TAG = DisplayActivity::class.java.simpleName
    }
}
