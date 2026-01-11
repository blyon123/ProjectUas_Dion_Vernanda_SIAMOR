package com.example.projectuas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.projectuas.AnggotaActivity
import com.example.projectuas.OrganisasiActivity
import com.example.projectuas.R


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ambil CardView dari layout
        val cardAnggota = findViewById<CardView>(R.id.cardAnggota)
        val cardOrganisasi = findViewById<CardView>(R.id.cardOrganisasi)

        // Klik menu Data Anggota
        cardAnggota.setOnClickListener {
            startActivity(Intent(this, AnggotaActivity::class.java))
        }

        // Klik menu Data Organisasi
        cardOrganisasi.setOnClickListener {
            startActivity(Intent(this, OrganisasiActivity::class.java))
        }
    }
}
