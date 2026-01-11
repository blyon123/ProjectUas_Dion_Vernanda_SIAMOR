package com.example.projectuas

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projectuas.databinding.ActivityOrganisasiBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class OrganisasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrganisasiBinding

    // 1. Deklarasi DatabaseReference
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrganisasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Inisialisasi Firebase Database (Node: "Organisasi")
        database = FirebaseDatabase.getInstance().getReference("Organisasi")

        binding.btnBackKta.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSimpanOrg.setOnClickListener {
            val unit = binding.etUnit.text.toString().trim()
            val ketua = binding.etKetua.text.toString().trim()

            if (unit.isEmpty() || ketua.isEmpty()) {
                Toast.makeText(this, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Simpan data ke Firebase
            simpanKeFirebase(unit, ketua)
        }
    }

    private fun simpanKeFirebase(unit: String, ketua: String) {
        // Membuat ID unik berdasarkan waktu atau bisa menggunakan nama unit sebagai key
        // Di sini kita gunakan push().key agar data tidak menimpa satu sama lain
        val id = database.push().key ?: return

        val organisasiData = hashMapOf(
            "id" to id,
            "nama_unit" to unit,
            "ketua" to ketua
        )

        database.child(id).setValue(organisasiData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data organisasi tersimpan di Firebase", Toast.LENGTH_SHORT).show()

                // Kosongkan input setelah berhasil
                binding.etUnit.setText("")
                binding.etKetua.setText("")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal simpan ke Firebase: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
