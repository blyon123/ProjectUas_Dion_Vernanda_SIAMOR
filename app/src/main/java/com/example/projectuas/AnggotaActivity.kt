package com.example.projectuas

import android.os.Bundle
import android.widget.ArrayAdapter // Pastikan import ini ada
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projectuas.databinding.ActivityAnggotaBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AnggotaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnggotaBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnggotaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("Anggota")

        // Inisialisasi Dropdown Golongan Darah
        val listGoldar = arrayOf("A", "B", "AB", "O", "Belum Tahu")
        val adapterGoldar = ArrayAdapter(this, android.R.layout.simple_list_item_1, listGoldar)

        binding.btnBackKta.setOnClickListener { finish() }
        binding.btnBackKta.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        // Jika baris bawah ini merah, artinya ID di XML belum 'spGolonganDarah'
        binding.spGolonganDarah.setAdapter(adapterGoldar)

        binding.btnSimpan.setOnClickListener {
            val nama = binding.etNama.text.toString().trim()
            val jabatan = binding.etJabatan.text.toString().trim()
            val unit = binding.etUnit.text.toString().trim()
            val golDarah = binding.spGolonganDarah.text.toString()

            if (nama.isEmpty() || jabatan.isEmpty() || unit.isEmpty() || golDarah.isEmpty()) {
                Toast.makeText(this, "Mohon isi semua data termasuk Golongan Darah", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            simpanKeFirebase(nama, jabatan, unit, golDarah)
        }
    }

    private fun simpanKeFirebase(nama: String, jabatan: String, unit: String, golDarah: String) {
        val anggotaId = database.push().key

        val anggota = HashMap<String, Any>()
        anggota["id"] = anggotaId ?: ""
        anggota["nama"] = nama
        anggota["jabatan"] = jabatan
        anggota["unit"] = unit
        anggota["golonganDarah"] = golDarah

        if (anggotaId != null) {
            database.child(anggotaId).setValue(anggota)
                .addOnSuccessListener {
                    binding.etNama.setText("")
                    binding.etJabatan.setText("")
                    binding.etUnit.setText("")
                    binding.spGolonganDarah.setText("", false) // "" untuk mengosongkan

                    Toast.makeText(this, "Data Anggota Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
