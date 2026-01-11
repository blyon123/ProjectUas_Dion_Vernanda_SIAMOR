package com.example.projectuas

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projectuas.databinding.ActivityKegiatanBinding

class KegiatanActivity : AppCompatActivity() {

    // 1. Deklarasi binding di DALAM class
    private lateinit var binding: ActivityKegiatanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Inisialisasi binding
        binding = ActivityKegiatanBinding.inflate(layoutInflater)

        // 3. Gunakan binding.root
        setContentView(binding.root)

        val db = DBHELPER(this).writableDatabase

        // Tombol Kembali
        binding.btnBackKta.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Tombol Simpan (Gunakan binding agar tidak perlu findViewById)
        binding.btnSimpanOrg.setOnClickListener {
            val nama = binding.etKetua.text.toString().trim()
            val tgl = binding.etUnit.text.toString().trim()
            val ket = binding.etKeterangan.text.toString().trim()

            if (nama.isEmpty() || tgl.isEmpty() || ket.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                db.execSQL(
                    "INSERT INTO kegiatan (nama_kegiatan, tanggal, keterangan) VALUES (?,?,?)",
                    arrayOf(nama, tgl, ket)
                )

                Toast.makeText(this, "Laporan kegiatan tersimpan", Toast.LENGTH_SHORT).show()

                // Reset Form
                binding.etKetua.setText("")
                binding.etUnit.setText("")
                binding.etKeterangan.setText("")

            } catch (e: Exception) {
                Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
