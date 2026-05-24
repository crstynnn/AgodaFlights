package com.example.agodaapp.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agodaapp.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnVerify.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val fullName = binding.etFullName.text.toString().trim()

            binding.tilEmail.error = null
            binding.tilFullName.error = null

            if (email.isEmpty() || fullName.isEmpty()) {
                if (email.isEmpty()) binding.tilEmail.error = "Required"
                if (fullName.isEmpty()) binding.tilFullName.error = "Required"
                return@setOnClickListener
            }

            // Verify existence and match
            firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        binding.tilEmail.error = "Email not found"
                    } else {
                        val userDoc = querySnapshot.documents[0]
                        val dbFullName = userDoc.getString("name")
                        
                        if (dbFullName.equals(fullName, ignoreCase = true)) {
                            // Match found, send reset email
                            auth.sendPasswordResetEmail(email)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG).show()
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            binding.tilFullName.error = "Name does not match records"
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}