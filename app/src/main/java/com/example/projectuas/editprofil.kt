package com.example.projectuas // Ganti sesuai package name Anda

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.projectuas.databinding.ActivityEditprofilBinding
import android.content.Intent
import com.example.projectuas.R
class EditProfilActivity : AppCompatActivity() {

    // Inisialisasi ViewBinding
    private lateinit var binding: ActivityEditprofilBinding

    // Variabel untuk menyimpan URI foto yang dipilih (opsional jika ingin dikirim ke database)
    private var imageUri: Uri? = null

    // Launcher untuk membuka Galeri dan mengambil gambar
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                // Minta izin akses permanen agar foto tidak crash saat dibuka di halaman lain
                val contentResolver = applicationContext.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(it, takeFlags)

                binding.imgEditProfile.setImageURI(it)
                imageUri = it
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditprofilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Di onCreate EditProfilActivity
        val sharedPref = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        binding.etEditNama.setText(sharedPref.getString("nama", ""))
        binding.etEditEmail.setText(sharedPref.getString("email", ""))


        // 1. Logika Klik Foto Profil (untuk ganti foto)
        binding.imgEditProfile.setOnClickListener {
            openGallery()
        }


        // 2. Logika Tombol Simpan
        binding.btnSimpanProfil.setOnClickListener {
            saveChanges()
        }

        // 3. Opsi: Isi data awal (jika diperlukan)
        // loadCurrentData()
    }

    private fun openGallery() {
        // Meluncurkan pemilih gambar galeri
        pickImageLauncher.launch(arrayOf("image/*"))
        // atau jika ingin memilih gambar spesifik:
        //arayOf("image/*")
    }

    private fun saveChanges() {
        val nama = binding.etEditNama.text.toString()
        val email = binding.etEditEmail.text.toString()
        val telepon = binding.etEditTelepon.text.toString()
        val unit = binding.etEditUnit.text.toString()

        if (nama.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nama dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Simpan semua data sekaligus
        editor.putString("nama", nama)
        editor.putString("email", email)
        editor.putString("telepon", telepon)
        editor.putString("unit", unit)

        // Simpan URI foto jika ada
        imageUri?.let {
            editor.putString("foto_uri", it.toString())
        }

        editor.apply() // Panggil apply sekali saja di akhir

        Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_LONG).show()
        finish()
    }



    /*
    private fun loadCurrentData() {
        // Contoh jika ingin set data default saat halaman dibuka
        binding.etEditNama.setText("Ahmad Relawan")
        binding.etEditEmail.setText("ahmad@pmi.or.id")
    }
    */
}
