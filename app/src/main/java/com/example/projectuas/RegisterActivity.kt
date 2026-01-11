package com.example.projectuas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    // 1. Deklarasi DatabaseReference
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 2. Inisialisasi Firebase Database (Node: "Users")
        database = FirebaseDatabase.getInstance().getReference("Users")

        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Harap isi semua data", Toast.LENGTH_SHORT).show()
            } else {
                // 3. Simpan data ke Firebase
                simpanKeFirebase(username, password)
            }
        }
    }

    private fun simpanKeFirebase(user: String, pass: String) {
        // Kita gunakan username sebagai KEY agar satu username tidak bisa mendaftar dua kali
        val userData = hashMapOf(
            "username" to user,
            "password" to pass
        )

        // Cek dulu apakah username sudah ada atau belum
        database.child(user).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Toast.makeText(this, "Username sudah digunakan, silakan pilih yang lain", Toast.LENGTH_SHORT).show()
            } else {
                // Jika belum ada, simpan data baru
                database.child(user).setValue(userData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Akun berhasil dibuat ", Toast.LENGTH_SHORT).show()
                        finish() // Kembali ke LoginActivity
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal daftar: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Kesalahan koneksi", Toast.LENGTH_SHORT).show()
        }
    }
}
