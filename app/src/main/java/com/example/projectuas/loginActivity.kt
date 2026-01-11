package com.example.projectuas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    // 1. Deklarasi Firebase Database
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        // Inisialisasi Firebase (Node "Users")
        database = FirebaseDatabase.getInstance().getReference("Users")

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // Klik Daftar Akun
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Klik Login
        btnLogin.setOnClickListener {
            val inputUsername = etUsername.text.toString().trim()
            val inputPassword = etPassword.text.toString().trim()

            if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(this, "Masukkan Username dan Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // A. LOGIKA LOGIN ADMIN (Hardcoded)
            if (inputUsername == "admin" && inputPassword == "admin123") {
                Toast.makeText(this, "Login Admin Berhasil", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
            else {
                // B. LOGIKA LOGIN FIREBASE
                checkUserInFirebase(inputUsername, inputPassword)
            }
        }
    }

    private fun checkUserInFirebase(user: String, pass: String) {
        // Mencari username di dalam node "Users"
        database.child(user).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Ambil password dari database
                val passwordFromDB = snapshot.child("password").getValue(String::class.java)

                if (passwordFromDB == pass) {
                    Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()

                    // (Opsional) Simpan session ke SharedPreferences agar tidak perlu login ulang
                    val sharedPref = getSharedPreferences("USER_DATA", MODE_PRIVATE)
                    sharedPref.edit().putString("username", user).apply()

                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Password Salah", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Koneksi Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
