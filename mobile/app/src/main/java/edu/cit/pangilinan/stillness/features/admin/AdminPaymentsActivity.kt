package edu.cit.pangilinan.stillness.features.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.pangilinan.stillness.R
import edu.cit.pangilinan.stillness.model.AdminPaymentsResponse
import edu.cit.pangilinan.stillness.shared.api.ApiClient
import edu.cit.pangilinan.stillness.shared.auth.SessionManager

class AdminPaymentsActivity : AppCompatActivity() {

    private lateinit var rvPayments: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvRevenue: TextView
    private lateinit var tvPaid: TextView
    private lateinit var tvFailed: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_payments)

        rvPayments = findViewById(R.id.rv_payments)
        progressBar = findViewById(R.id.progress_bar)
        tvRevenue = findViewById(R.id.tv_revenue)
        tvPaid = findViewById(R.id.tv_paid)
        tvFailed = findViewById(R.id.tv_failed)

        rvPayments.layoutManager = LinearLayoutManager(this)

        loadPayments()
    }

    private fun loadPayments() {
        val token = SessionManager.getToken(this)
        if (token == null) {
            // Not logged in
            return
        }

        progressBar.visibility = View.VISIBLE
        AdminApi.getAdminPayments(token, object : ApiClient.ApiCallback<AdminPaymentsResponse> {
            override fun onSuccess(result: AdminPaymentsResponse) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    
                    tvRevenue.text = String.format("$%.2f", result.summary.totalRevenue)
                    tvPaid.text = result.summary.paidTransactions.toString()
                    tvFailed.text = result.summary.failedTransactions.toString()
                    
                    val adapter = AdminPaymentAdapter(result.records)
                    rvPayments.adapter = adapter
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                }
            }
        })
    }
}
