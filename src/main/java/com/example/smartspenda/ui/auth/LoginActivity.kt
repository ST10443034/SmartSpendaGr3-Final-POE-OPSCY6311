package com.example.smartspenda.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartspenda.MainActivity
import com.example.smartspenda.R
import com.example.smartspenda.data.database.AppDatabase
import com.example.smartspenda.data.entities.Category
import com.example.smartspenda.databinding.ActivityLoginBinding
import com.example.smartspenda.utils.SecurityHelper
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.example.smartspenda.R.style.Theme_SmartSpenda) // Revert to main theme after splash
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        // Check if already logged in
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (prefs.contains("userId")) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val user = db.userDao().login(email, SecurityHelper.hashPassword(password))
                if (user != null) {
                    getSharedPreferences("app_prefs", MODE_PRIVATE).edit()
                        .putLong("userId", user.userId)
                        .putString("userName", user.fullName)
                        .putString("userEmail", user.email)
                        .apply()

                    // Pre-insert default categories if none exist
                    val cats = db.categoryDao().getCategoriesForUser(user.userId)
                    if (cats.isEmpty()) {
                        val defaultNames = listOf("Groceries", "Transport", "Entertainment", "Bills", "Dining Out", "Shopping")
                        defaultNames.forEach { name ->
                            db.categoryDao().insertCategory(Category(userId = user.userId, name = name))
                        }
                    }

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}