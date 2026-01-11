package com.example.projectuas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.projectuas.databinding.ActivityProfilBinding
import com.example.projectuas.R

class ProfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilBinding
    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    // Pindahkan pengambilan data ke sini agar lebih aman
    private fun loadProfileData() {
        val sharedPref = getSharedPreferences("USER_DATA", MODE_PRIVATE)

        // Ambil data dari memori (Gunakan default value yang sesuai)
        val nama = sharedPref.getString("nama", "Ahmad Relawan")
        val email = sharedPref.getString("email", "ahmad@pmi.or.id")
        val telepon = sharedPref.getString("telepon", "+62 812 3456 7890")
        val unit = sharedPref.getString("unit", "Markas PMI, Jl. Gatot Subroto")
        val fotoUriString = sharedPref.getString("foto_uri", null)

        // Pastikan binding sudah di-inflate sebelum mengakses view
        // Jika ID tvEmailProfil dkk belum ada di XML, aplikasi akan crash di sini.
        try {
            binding.tvNamaProfil.text = nama
            binding.tvEmailProfil.text = email
            binding.tvTeleponProfil.text = telepon
            binding.tvUnitProfil.text = unit

            fotoUriString?.let {
                try {
                    val uri = android.net.Uri.parse(it)
                    binding.imgProfile.setImageURI(uri)
                } catch (e: Exception) {
                    // Jika gagal (karena izin), gunakan gambar default agar tidak crash
                    binding.imgProfile.setImageResource(android.R.drawable.ic_menu_gallery)
                    e.printStackTrace()
                }
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }


            override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Inisialisasi Data Dummy
        setupProfileData()

        // 2. Logika Tombol Kembali (Header)
        binding.headerProfil.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 3. Logika Tombol Pindah ke Edit Profil
        // PASTIKAN di activity_profil.xml tombol Anda memiliki id: android:id="@+id/btnGoToEdit"
        binding.btnGoToEdit.setOnClickListener {
            val intent = Intent(this, EditProfilActivity::class.java)
            startActivity(intent)
        }

        // 4. Logika Tombol Logout
        setupLogoutButton()

    } // TUTUP KURUNG ONCREATE HARUS DI SINI

    private fun setupProfileData() {
        binding.tvNamaProfil.text = "Ahmad Relawan"
        // Tambahkan binding lain di sini jika ada ID-nya di XML
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            // 1. (Opsional) Jika menggunakan Firebase Auth, tambahkan:
            // FirebaseAuth.getInstance().signOut()

            // 2. Berpindah ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)

            // 3. Clear Task agar user tidak bisa kembali ke profil dengan tombol back HP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            // 4. Tampilkan pesan
            Toast.makeText(this, "Berhasil Logout", Toast.LENGTH_SHORT).show()

            // 5. Tutup activity profil
            finish()
        }
    }
}
