package com.example.projectuas

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.projectuas.databinding.ActivityDashboardBinding
import android.widget.Toast
import com.example.projectuas.ProfilActivity
import com.example.projectuas.OrganisasiActivity


class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // OPSI A: Jika ingin menggunakan listener langsung di kode (Rekomendasi)
        binding.btnMenuProfil.setOnClickListener {
            val intent = Intent(this, ProfilActivity::class.java)
            startActivity(intent)
        }

        // Tombol Anggota (Tadinya java . lang . Error di sini)
        binding.btnMenuAnggota.setOnClickListener {
            val intent = Intent(this, AnggotaActivity::class.java)
            startActivity(intent)

            // Tambahkan di dalam onCreate, di bawah binding.btnMenuAnggota
            binding.btnMenuKTA.setOnClickListener {
                val intent = Intent(this, KartuAnggotaActivity::class.java)
                startActivity(intent)
            }

        }
    }


    // OPSI B: Jika Anda menggunakan atribut android:onClick="openProfil" di XML
    fun openProfil(view: View) {
        val intent = Intent(this, ProfilActivity::class.java)
        startActivity(intent)
    }

    // Tambahkan di dalam class DashboardActivity (di luar onCreate)
    fun OpenAnggota(view: android.view.View) {
        val intent = Intent(this, AnggotaActivity::class.java)
        startActivity(intent)
    }


    // Button Pendataan Organisasi
    fun openOrganisasi(view: View) {
        val intent = Intent(this, OrganisasiActivity::class.java)
        startActivity(intent)
    }

    // Button Pelaporan Kegiatan PMI
    fun openKegiatan(view: View) {
        val intent = Intent(this, KegiatanActivity::class.java)
        startActivity(intent)
    }

    fun OpenKTA(view: View) {
        val intent = Intent(this, KartuAnggotaActivity::class.java)
        startActivity(intent)
    }
}



