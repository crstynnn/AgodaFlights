package com.example.agodaapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agodaapp.databinding.ActivityRegisterBinding
import com.example.agodaapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val name = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            binding.etFullName.error = "Name is required"
            binding.etFullName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            binding.etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Please enter a valid email address"
            binding.etEmail.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            binding.etPhone.error = "Phone number is required"
            binding.etPhone.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            binding.etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            binding.etPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            binding.etConfirmPassword.requestFocus()
            return
        }

        // Show loading
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Creating account..."
        binding.tvError.visibility = android.view.View.GONE

        Log.d(TAG, "Attempting to create user with email: $email")

        // Create user in Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    Log.d(TAG, "User created successfully with UID: $userId")

                    // Get current timestamp
                    val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                    // Create user object with additional fields
                    val user = User(
                        id = userId,
                        email = email,
                        name = name,
                        phone = phone
                    )

                    // Additional data to save in Firestore
                    val userData = hashMapOf(
                        "id" to userId,
                        "email" to email,
                        "name" to name,
                        "phone" to phone,
                        "createdAt" to currentDate,
                        "userType" to "customer",
                        "isActive" to true,
                        "totalBookings" to 0
                    )

                    Log.d(TAG, "Saving user data to Firestore: $userData")

                    // Save user to Firestore with more details
                    firestore.collection("users")
                        .document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            Log.d(TAG, "User data successfully saved to Firestore")

                            // Also save user preferences (optional)
                            saveUserPreferences(name, email)

                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error saving user to Firestore: ${e.message}", e)
                            binding.tvError.text = "Error saving user data: ${e.message}"
                            binding.tvError.visibility = android.view.View.VISIBLE
                            binding.btnRegister.isEnabled = true
                            binding.btnRegister.text = "Sign Up"

                            // Optional: Delete the auth user if Firestore save fails
                            auth.currentUser?.delete()
                        }
                } else {
                    val errorMessage = task.exception?.message ?: "Registration failed"
                    Log.e(TAG, "Registration failed: $errorMessage", task.exception)
                    binding.tvError.text = "Registration failed: $errorMessage"
                    binding.tvError.visibility = android.view.View.VISIBLE
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Sign Up"
                }
            }
    }

    private fun saveUserPreferences(name: String, email: String) {
        // Save to SharedPreferences for quick access
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("userName", name)
        editor.putString("userEmail", email)
        editor.putBoolean("isLoggedIn", true)
        editor.apply()

        Log.d(TAG, "User preferences saved")
    }
}