package com.example.agodaapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agodaapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // If already logged in, check role and skip to appropriate activity
        if (auth.currentUser != null) {
            checkUserRoleAndNavigate()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            binding.tilEmail.error = null
            binding.tilPassword.error = null

            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty()) binding.tilEmail.error = "Email is required"
                if (password.isEmpty()) binding.tilPassword.error = "Password is required"
                return@setOnClickListener
            }

            binding.btnLogin.isEnabled = false
            binding.btnLogin.text = "Logging in..."

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        checkUserRoleAndNavigate()
                    } else {
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = "Login"
                        binding.tilPassword.error = "Invalid email or password"
                    }
                }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun checkUserRoleAndNavigate() {
        val userId = auth.currentUser?.uid ?: return
        
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Checking role..."
        
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userType = document.getString("userType") ?: "customer"
                    if (userType == "admin") {
                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    finish()
                } else {
                    // Fallback if user doc doesn't exist yet
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Login"
                // On failure, default to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }
}