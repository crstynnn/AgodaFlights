package com.example.agodaapp.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.agodaapp.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupFormatters()

        binding.btnPay.setOnClickListener {
            val cardNumber = binding.etCardNumber.text.toString().replace(" ", "").trim()
            val expiry = binding.etExpiry.text.toString().trim()
            val cvv = binding.etCvv.text.toString().trim()

            if (cardNumber.length < 16 || expiry.length < 5 || cvv.length < 3) {
                Toast.makeText(this, "Please enter valid card details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun setupFormatters() {
        // Card number formatting: 0000 0000 0000 0000
        binding.etCardNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etCardNumber.removeTextChangedListener(this)
                val str = s.toString().replace(" ", "")
                val formatted = StringBuilder()
                for (i in str.indices) {
                    if (i > 0 && i % 4 == 0) formatted.append(" ")
                    formatted.append(str[i])
                }
                binding.etCardNumber.setText(formatted.toString())
                binding.etCardNumber.setSelection(formatted.length)
                binding.etCardNumber.addTextChangedListener(this)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Expiry formatting: MM/YY
        binding.etExpiry.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etExpiry.removeTextChangedListener(this)
                val str = s.toString().replace("/", "")
                if (str.length >= 2) {
                    val formatted = "${str.substring(0, 2)}/${str.substring(2)}"
                    binding.etExpiry.setText(formatted)
                    binding.etExpiry.setSelection(formatted.length)
                } else {
                    binding.etExpiry.setText(str)
                    binding.etExpiry.setSelection(str.length)
                }
                binding.etExpiry.addTextChangedListener(this)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}