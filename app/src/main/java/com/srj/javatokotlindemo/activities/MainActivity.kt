package com.srj.javatokotlindemo.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import com.srj.javatokotlindemo.R
import com.srj.javatokotlindemo.app.Constants

// import extension to view binding
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.reflect.KClass


class MainActivity : AppCompatActivity() {

    companion object { //  static (1 companion only in one class)
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        // gettexet in chatrsequesnce (title: synthetic property)
        val str = toolbar.title
        toolbar.title = "srj"

        //Editable.text points to Editable object
        val str2 = etName.text.toString()
        //        etName.text = "srj"  will not work
        etName.setText("srj")

    }

    /*
    Save the user in SP
     */
    fun saveName(view: View) {

        if (isEmpty(etName,inputLayoutName)){

            val personName = etName.text.toString()
            val sp = getSharedPreferences(Constants.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            val editor = sp.edit()
            editor.putString(Constants.KEY_PERSON_NAME, personName)
            editor.apply()

        }
    }

    /*
    List github search repository
     */
    fun listRepositories(view: View) {

        if (isEmpty(etRepoName, inputLayoutRepoName)) {
            val repo_name = etRepoName.text.toString()
            val language = etLanguage.text.toString()

            val JClass: Class<DisplayActivity> = DisplayActivity::class.java; // java type reflection
            val KClass: KClass<DisplayActivity> = DisplayActivity::class // Kotlin type reflection
            val intent = Intent(this@MainActivity, JClass) // used 2nd parameter Kotlin Reflection
            //OR Intent(this,DisplayActivity::class.java)
            intent.putExtra(Constants.KEY_QUERY_TYPE, Constants.SEARCH_BY_REPO)
            intent.putExtra(Constants.KEY_REPO_SEARCH, repo_name)
            intent.putExtra(Constants.KEY_LANGUAGE, language)
            startActivity(intent)
        }

    }

    /*
    display user
     */
    fun listUserRepositories(view: View) {

        if (isEmpty(etGithubUser, inputLayoutGithubUser)) {

            val githubUser = etGithubUser.text.toString()
            val intent = Intent(this@MainActivity, DisplayActivity::class.java)
            intent.putExtra(Constants.KEY_QUERY_TYPE, Constants.SEARCH_BY_USER)
            intent.putExtra(Constants.KEY_GITHUB_USER, githubUser)
            startActivity(intent)
        }
    }

    fun isEmpty(et: EditText, txtInputLayout: TextInputLayout): Boolean {
        if (et.text.toString().isEmpty()) {
            txtInputLayout.error = "Cannot be Blank"
            return false
        } else {

            txtInputLayout.isErrorEnabled = false
            return true
        }
    }


}
