package com.example.agodaapp.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agodaapp.databinding.ActivityPaymentBinding
import com.example.agodaapp.models.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var bookingId = ""
    private var totalAmount = 0.0
    private var flightDetails = ""
    private var passengerName = ""
    private var bookingReference = ""
    private var flightAirline = ""
    private var flightNumber = ""
    private var flightFrom = ""
    private var flightTo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        bookingId = intent.getStringExtra("booking_id") ?: ""
        totalAmount = intent.getDoubleExtra("total_amount", 0.0)
        flightDetails = intent.getStringExtra("flight_details") ?: ""
        passengerName = intent.getStringExtra("passenger_name") ?: ""
        bookingReference = intent.getStringExtra("booking_reference") ?: ""
        flightAirline = intent.getStringExtra("flight_airline") ?: ""
        flightNumber = intent.getStringExtra("flight_number") ?: ""
        flightFrom = intent.getStringExtra("flight_from") ?: ""
        flightTo = intent.getStringExtra("flight_to") ?: ""

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.tvAmount.text = String.format("₱%,.2f", totalAmount)

        setupPaymentMethods()
        binding.btnPay.setOnClickListener { processPayment() }
    }

    private fun setupPaymentMethods() {
        // Hide optional sections initially
        binding.cardDetailsCard.visibility = View.GONE
        binding.mobilePaymentCard.visibility = View.GONE

        binding.paymentMethodGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.rbCreditCard.id, binding.rbDebitCard.id -> {
                    binding.cardDetailsCard.visibility = View.VISIBLE
                    binding.mobilePaymentCard.visibility = View.GONE
                }
                binding.rbGCash.id -> {
                    binding.cardDetailsCard.visibility = View.GONE
                    binding.mobilePaymentCard.visibility = View.VISIBLE
                    binding.tvMobileInstruction.text = "Enter your GCash registered mobile number"
                }
                binding.rbPayMaya.id -> {
                    binding.cardDetailsCard.visibility = View.GONE
                    binding.mobilePaymentCard.visibility = View.VISIBLE
                    binding.tvMobileInstruction.text = "Enter your PayMaya registered mobile number"
                }
                binding.rbBankTransfer.id -> {
                    binding.cardDetailsCard.visibility = View.GONE
                    binding.mobilePaymentCard.visibility = View.GONE
                }
            }
        }
    }

    private fun processPayment() {
        val checkedId = binding.paymentMethodGroup.checkedRadioButtonId
        if (checkedId == -1) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedPaymentMethod = when (checkedId) {
            binding.rbCreditCard.id -> "Credit Card"
            binding.rbDebitCard.id -> "Debit Card"
            binding.rbGCash.id -> "GCash"
            binding.rbPayMaya.id -> "PayMaya"
            binding.rbBankTransfer.id -> "Bank Transfer"
            else -> {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Validate required fields based on payment method
        when (selectedPaymentMethod) {
            "Credit Card", "Debit Card" -> {
                val cardNumber = binding.etCardNumber.text.toString().trim()
                val cardHolder = binding.etCardHolder.text.toString().trim()
                val expiry = binding.etExpiry.text.toString().trim()
                val cvv = binding.etCVV.text.toString().trim()

                if (cardNumber.isEmpty() || cardHolder.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
                    Toast.makeText(this, "Please enter all card details", Toast.LENGTH_SHORT).show()
                    return
                }
                if (cardNumber.replace(" ", "").length < 12) {
                    Toast.makeText(this, "Invalid card number", Toast.LENGTH_SHORT).show()
                    return
                }
                if (cvv.length < 3) {
                    Toast.makeText(this, "Invalid CVV", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            "GCash", "PayMaya" -> {
                val mobileNumber = binding.etMobileNumber.text.toString().trim()
                if (mobileNumber.isEmpty() || mobileNumber.length < 10) {
                    Toast.makeText(this, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        binding.btnPay.isEnabled = false
        binding.btnPay.text = "Processing..."

        // Simulate payment processing delay
        Handler(Looper.getMainLooper()).postDelayed({
            completePayment(selectedPaymentMethod)
        }, 2000)
    }

    private fun completePayment(paymentMethod: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
            return
        }

        val transactionId = "TXN${System.currentTimeMillis()}"
        val transactionDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val referenceNumber = (1..8).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")

        val transaction = Transaction(
            id = transactionId,
            bookingId = bookingId,
            userId = userId,
            amount = totalAmount,
            currency = "PHP",
            paymentMethod = paymentMethod,
            paymentStatus = "Completed",
            transactionDate = transactionDate,
            referenceNumber = referenceNumber,
            description = "Flight Booking: $flightDetails"
        )

        firestore.collection("transactions").document(transactionId)
            .set(transaction)
            .addOnSuccessListener {
                // Update the booking record with payment info
                firestore.collection("bookings").document(bookingId)
                    .update(
                        mapOf(
                            "paymentStatus" to "Paid",
                            "paymentMethod" to paymentMethod,
                            "transactionId" to transactionId,
                            "paymentDate" to transactionDate
                        )
                    )
                    .addOnSuccessListener {
                        val intent = Intent(this, BookingConfirmationActivity::class.java).apply {
                            putExtra("booking_ref", bookingReference)
                            putExtra("total_amount", totalAmount)
                            putExtra("payment_method", paymentMethod)
                            putExtra("flight_airline", flightAirline)
                            putExtra("flight_number", flightNumber)
                            putExtra("flight_from", flightFrom)
                            putExtra("flight_to", flightTo)
                            // Also pass to PaymentSuccessActivity fields in case needed
                            putExtra("transaction_id", transactionId)
                            putExtra("reference_number", referenceNumber)
                        }
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        resetPayButton()
                        Toast.makeText(this, "Payment recorded but booking update failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                resetPayButton()
                Toast.makeText(this, "Payment failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun resetPayButton() {
        binding.btnPay.isEnabled = true
        binding.btnPay.text = "Pay Now"
    }
}