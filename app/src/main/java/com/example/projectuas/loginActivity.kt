package com.example.projectuas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        // Konfigurasi Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // ID ini otomatis ada setelah connect Firebase
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogle = findViewById<Button>(R.id.btnGoogle) // Tambahkan tombol di XML
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Login Manual
        btnLogin.setOnClickListener {
            val inputUsername = etUsername.text.toString().trim()
            val inputPassword = etPassword.text.toString().trim()

            if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(this, "Masukkan Username dan Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (inputUsername == "admin" && inputPassword == "admin123") {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                checkUserInFirebase(inputUsername, inputPassword)
            }
        }

        // Login Google
        btnGoogle.setOnClickListener {
            signInGoogle()
        }
    }

    // --- LOGIKA GOOGLE SIGN IN ---

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // Log detail error ke Logcat untuk mempermudah debugging
            android.util.Log.e("GOOGLE_LOGIN", "Status Code: ${e.statusCode}")

            val msg = when (e.statusCode) {
                10 -> "Developer Error: Pastikan SHA-1 sudah benar di Firebase dan JSON sudah diupdate."
                12500 -> "Sign-in Gagal (12500). Cek Google Play Services di HP."
                else -> "Gagal: ${e.message}"
            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }


    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                Toast.makeText(this, "Login Google Berhasil", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Firebase Auth Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- LOGIKA LOGIN MANUAL DATABASE (Lama) ---
    private fun checkUserInFirebase(user: String, pass: String) {
        database.child(user).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val passwordFromDB = snapshot.child("password").getValue(String::class.java)
                if (passwordFromDB == pass) {
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
        }
    }
}
