package edu.cit.pangilinan.stillness

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import edu.cit.pangilinan.stillness.api.ApiClient
import edu.cit.pangilinan.stillness.api.SessionApi
import edu.cit.pangilinan.stillness.model.SessionResponse
import edu.cit.pangilinan.stillness.model.SessionDto
import java.util.*

class SessionsActivity : AppCompatActivity() {
    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var allSessions: List<SessionDto> = emptyList()
    
    private lateinit var etSearch: EditText
    private lateinit var spinnerCategory: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessions)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewSessions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        etSearch = findViewById(R.id.etSearch)
        spinnerCategory = findViewById(R.id.spinnerCategory)

        sessionAdapter = SessionAdapter(emptyList()) { session ->
            val intent = Intent(this, SessionDetailActivity::class.java)
            intent.putExtra("SESSION_JSON", com.google.gson.Gson().toJson(session))
            startActivity(intent)
        }
        recyclerView.adapter = sessionAdapter

        setupFilters()

        swipeRefreshLayout.setOnRefreshListener {
            fetchSessions()
        }

        fetchSessions()
    }

    private fun setupFilters() {
        // Setup Category Spinner
        val categories = arrayOf("All Categories", "Yoga", "Meditation", "Fitness", "Mental Health", "Nutrition")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup Search
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyFilters() {
        val query = etSearch.text.toString().lowercase(Locale.ROOT)
        val selectedCategory = spinnerCategory.selectedItem.toString()

        val filteredList = allSessions.filter { session ->
            val matchesSearch = session.title.lowercase(Locale.ROOT).contains(query) || 
                               session.instructorName.lowercase(Locale.ROOT).contains(query)
            
            val matchesCategory = selectedCategory == "All Categories" || 
                                 session.category.equals(selectedCategory, ignoreCase = true)
            
            matchesSearch && matchesCategory
        }
        sessionAdapter.updateData(filteredList)
    }

    private fun fetchSessions() {
        val sharedPrefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        val token = sharedPrefs.getString("token", null)

        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progressBar)
        
        if (!swipeRefreshLayout.isRefreshing) {
            progressBar.visibility = android.view.View.VISIBLE
        }

        SessionApi.getSessions(token, object : ApiClient.ApiCallback<SessionResponse> {
            override fun onSuccess(result: SessionResponse) {
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    swipeRefreshLayout.isRefreshing = false

                    if (result.success && result.data != null) {
                        allSessions = result.data.sessions
                        applyFilters()
                    } else {
                        Toast.makeText(this@SessionsActivity, "No sessions found", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(this@SessionsActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
