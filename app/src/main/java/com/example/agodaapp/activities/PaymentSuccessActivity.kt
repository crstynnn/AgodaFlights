package com.example.agodaapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agodaapp.databinding.ActivityPaymentSuccessBinding

class PaymentSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val transactionId = intent.getStringExtra("transaction_id") ?: "N/A"
        val amount = intent.getDoubleExtra("amount", 0.0)
        val paymentMethod = intent.getStringExtra("payment_method") ?: "N/A"
        val referenceNumber = intent.getStringExtra("reference_number") ?: "N/A"

        binding.tvAmount.text = String.format("₱%,.2f", amount)
        binding.tvPaymentMethod.text = paymentMethod
        binding.tvReferenceNumber.text = referenceNumber
        binding.tvTransactionId.text = transactionId

        binding.btnViewBooking.setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
            finish()
        }

        binding.btnDownloadReceipt.setOnClickListener {
            Toast.makeText(this, "Receipt saved to Downloads", Toast.LENGTH_LONG).show()
        }

        binding.btnBackToHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}