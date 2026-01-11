package com.example.projectuas

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.projectuas.databinding.ActivityKartuanggotaBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException
import java.util.*
import java.io.File
import java.io.FileOutputStream
import android.os.Environment


class KartuAnggotaActivity : AppCompatActivity() {

    private fun cetakKePdf() {
        // 1. Update data ke label kartu sebelum dicetak
        updatePreviewKartu()

        // 2. Inisialisasi PdfDocument
        val pdfDocument = PdfDocument()

        // 3. Ambil View Kartu (Ganti cardKTA dengan ID CardView desain kartu Anda di XML)
        val view = binding.cardKTA

        // 4. MEMASTIKAN VIEW DIUKUR ULANG SEBELUM DICETAK (Agar tidak terpotong)
        view.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(view.width, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val pageInfo = PdfDocument.PageInfo.Builder(view.measuredWidth, view.measuredHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        // 5. Gambar view ke dalam PDF
        val canvas: Canvas = page.canvas
        view.draw(canvas)
        pdfDocument.finishPage(page)

        // 6. Tentukan lokasi penyimpanan (Folder Downloads Publik)
        val fileName = "KTA_${binding.etNik.text}.pdf"
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadDir, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF Berhasil! Cek folder Downloads", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal mencetak PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }


    private fun updatePreviewKartu() {
        // Mengisi TextView di dalam kartu dengan data dari EditText
        // Sesuaikan ID (txtKtaNama, dsb) dengan ID yang ada di desain kartu Anda
        binding.txtKtaNama.text = " : ${binding.etTempatLahir.text}" // Anggap etTempatLahir itu input Nama
        binding.txtKtaNik.text = " : ${binding.etNik.text}"
        binding.txtKtaProv.text = " : ${binding.spProvinsi.text}"

        // Pastikan foto juga terpasang di kartu
        imageUri?.let {
            binding.imgFotoKtaPreview.setImageURI(it)
        }
    }

    private lateinit var binding: ActivityKartuanggotaBinding
    private var imageUri: Uri? = null

    // Inisialisasi Firebase Database
    private lateinit var database: DatabaseReference

    // Di dalam class KartuAnggotaActivity
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            // Memberikan izin akses permanen ke URI (opsional tapi disarankan)
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // Simpan URI ke variabel global
            imageUri = it

            // 1. Tampilkan di Area Upload (yang Anda tanyakan)
            binding.imgPreviewKta.setImageURI(it)

            // 2. Hilangkan filter warna (tint) agar foto terlihat asli (PENTING)
            binding.imgPreviewKta.imageTintList = null

            // 3. Tampilkan juga di desain kartu KTA di bawahnya
            binding.imgFotoKtaPreview.setImageURI(it)

            Toast.makeText(this, "Foto berhasil dimuat", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKartuanggotaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hubungkan ke node "Anggota" di Firebase
        database = FirebaseDatabase.getInstance().getReference("Anggota")

        setupDropdowns()
        setupDatePicker()

        binding.btnBackKta.setOnClickListener { finish() }
        binding.btnBackKta.setOnClickListener {

            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnUploadFoto.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        binding.btnSimpanKta.setOnClickListener {
            val nik = binding.etNik.text.toString().trim()
            if (nik.isEmpty()) {
                binding.etNik.error = "NIK wajib diisi"
                return@setOnClickListener
            }

            // 1. Simpan ke Firebase
            simpanDataKeFirebase()

            // 2. Beri jeda 500ms agar data muncul di kartu, lalu cetak
            binding.root.postDelayed({
                cetakKePdf()
            }, 500)

            // Hapus finish() di sini. Biarkan user melihat Toast "PDF Berhasil" dulu.
        }

    }

    private fun simpanDataKeFirebase() {
        val nik = binding.etNik.text.toString().trim()
        val nama = binding.etTempatLahir.text.toString().trim() // Contoh: Anda bisa menambah field Nama
        val tglLahir = binding.etTglLahir.text.toString().trim()
        val agama = binding.spAgama.text.toString()
        val gender = binding.spGender.text.toString()
        val hp = binding.etHpKta.text.toString().trim()
        val provinsi = binding.spProvinsi.text.toString()
        val kabupaten = binding.spKabupaten.text.toString()

        // Validasi sederhana
        if (nik.isEmpty()) {
            binding.etNik.error = "NIK tidak boleh kosong"
            return
        }

        // Buat objek data (Map)
        val anggotaData = HashMap<String, Any>()
        anggotaData["nik"] = nik
        anggotaData["tglLahir"] = tglLahir
        anggotaData["agama"] = agama
        anggotaData["gender"] = gender
        anggotaData["noHp"] = hp
        anggotaData["provinsi"] = provinsi
        anggotaData["kabupaten"] = kabupaten
        // Catatan: Untuk foto, biasanya URL hasil upload dari Firebase Storage yang disimpan di sini.

        // Simpan ke Firebase menggunakan NIK sebagai Key
        database.child(nik).setValue(anggotaData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal simpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Deklarasikan variabel data di tingkat class (di bawah private lateinit var database...)
    private val wilayahData = HashMap<String, Array<String>>()

    private fun setupDropdowns() {
        // 1. Data Wilayah (Contoh beberapa provinsi, Anda bisa melengkapinya)
        wilayahData["Aceh"] = arrayOf(
            "Banda Aceh","Sabang","Langsa","Lhokseumawe","Subulussalam",
            "Aceh Besar","Aceh Barat","Aceh Barat Daya","Aceh Selatan","Aceh Tengah",
            "Aceh Tenggara","Aceh Timur","Aceh Utara","Bener Meriah","Bireuen",
            "Gayo Lues","Nagan Raya","Pidie","Pidie Jaya","Simeulue"
        )

        wilayahData["Sumatera Utara"] = arrayOf(
            "Medan","Binjai","Pematangsiantar","Tanjungbalai","Tebing Tinggi",
            "Sibolga","Padangsidimpuan","Gunungsitoli",
            "Asahan","Batubara","Dairi","Deli Serdang","Humbang Hasundutan","Karo",
            "Labuhanbatu","Labuhanbatu Selatan","Labuhanbatu Utara","Langkat",
            "Mandailing Natal","Nias","Nias Barat","Nias Selatan","Nias Utara",
            "Padang Lawas","Padang Lawas Utara","Samosir","Serdang Bedagai",
            "Simalungun","Tapanuli Selatan","Tapanuli Tengah","Tapanuli Utara","Toba"
        )

        wilayahData["Sumatera Barat"] = arrayOf(
            "Padang","Bukittinggi","Padang Panjang","Pariaman","Payakumbuh","Sawahlunto",
            "Agam","Dharmasraya","Kepulauan Mentawai","Lima Puluh Kota","Padang Pariaman",
            "Pasaman","Pasaman Barat","Pesisir Selatan","Sijunjung","Solok",
            "Solok Selatan","Tanah Datar"
        )

        wilayahData["Riau"] = arrayOf(
            "Pekanbaru","Dumai",
            "Bengkalis","Indragiri Hilir","Indragiri Hulu","Kampar",
            "Kepulauan Meranti","Kuantan Singingi","Pelalawan",
            "Rokan Hilir","Rokan Hulu","Siak"
        )

        wilayahData["Kepulauan Riau"] = arrayOf(
            "Batam","Tanjungpinang",
            "Bintan","Karimun","Lingga","Natuna","Kepulauan Anambas"
        )

        wilayahData["Jambi"] = arrayOf(
            "Jambi","Sungai Penuh",
            "Batanghari","Bungo","Kerinci","Merangin","Muaro Jambi",
            "Sarolangun","Tanjung Jabung Barat","Tanjung Jabung Timur","Tebo"
        )

        wilayahData["Sumatera Selatan"] = arrayOf(
            "Palembang","Prabumulih","Lubuk Linggau","Pagar Alam",
            "Banyuasin","Empat Lawang","Lahat","Muara Enim","Musi Banyuasin",
            "Musi Rawas","Musi Rawas Utara","Ogan Ilir","Ogan Komering Ilir",
            "Ogan Komering Ulu","Ogan Komering Ulu Selatan","Ogan Komering Ulu Timur",
            "Penukal Abab Lematang Ilir"
        )

        wilayahData["Bengkulu"] = arrayOf(
            "Kota Bengkulu","Bengkulu Selatan","Bengkulu Tengah","Bengkulu Utara",
            "Kaur","Kepahiang","Lebong","Mukomuko","Rejang Lebong","Seluma"
        )

        wilayahData["Lampung"] = arrayOf(
            "Bandar Lampung","Metro",
            "Lampung Barat","Lampung Selatan","Lampung Tengah","Lampung Timur",
            "Lampung Utara","Mesuji","Pesawaran","Pesisir Barat","Pringsewu",
            "Tanggamus","Tulang Bawang","Tulang Bawang Barat","Way Kanan"
        )

        wilayahData["Kepulauan Bangka Belitung"] = arrayOf(
            "Pangkalpinang",
            "Bangka","Bangka Barat","Bangka Selatan","Bangka Tengah",
            "Belitung","Belitung Timur"
        )

        wilayahData["DKI Jakarta"] = arrayOf(
            "Jakarta Pusat","Jakarta Barat","Jakarta Timur",
            "Jakarta Utara","Jakarta Selatan","Kepulauan Seribu"
        )

        wilayahData["Jawa Barat"] = arrayOf(
            "Bandung","Bekasi","Bogor","Cimahi","Cirebon","Depok","Sukabumi","Tasikmalaya","Banjar",
            "Bandung Barat","Bekasi","Bogor","Ciamis","Cianjur","Garut","Indramayu","Karawang",
            "Kuningan","Majalengka","Pangandaran","Purwakarta","Subang",
            "Sumedang","Sukabumi","Tasikmalaya"
        )

        wilayahData["Jawa Tengah"] = arrayOf(
            "Semarang","Surakarta","Magelang","Pekalongan","Salatiga","Tegal",
            "Banjarnegara","Banyumas","Batang","Blora","Boyolali","Brebes","Cilacap",
            "Demak","Grobogan","Jepara","Karanganyar","Kebumen","Kendal","Klaten",
            "Kudus","Magelang","Pati","Pemalang","Purbalingga","Purworejo",
            "Rembang","Semarang","Sragen","Sukoharjo","Tegal","Temanggung",
            "Wonogiri","Wonosobo"
        )

        wilayahData["DI Yogyakarta"] = arrayOf(
            "Yogyakarta","Sleman","Bantul","Kulon Progo","Gunungkidul"
        )

        wilayahData["Jawa Timur"] = arrayOf(
            "Surabaya","Malang","Batu","Blitar","Kediri","Madiun","Mojokerto",
            "Pasuruan","Probolinggo",
            "Bangkalan","Banyuwangi","Blitar","Bojonegoro","Bondowoso","Gresik",
            "Jember","Jombang","Lamongan","Lumajang","Magetan","Malang","Nganjuk",
            "Ngawi","Pacitan","Pamekasan","Ponorogo","Sampang","Sidoarjo",
            "Situbondo","Sumenep","Trenggalek","Tuban","Tulungagung"
        )

        wilayahData["Banten"] = arrayOf(
            "Serang","Cilegon","Tangerang","Tangerang Selatan","Lebak","Pandeglang"
        )

        wilayahData["Kalimantan Barat"] = arrayOf(
            "Pontianak","Singkawang",
            "Bengkayang","Kapuas Hulu","Kayong Utara","Ketapang",
            "Kubu Raya","Landak","Melawi","Mempawah",
            "Sambas","Sanggau","Sekadau","Sintang"
        )

        wilayahData["Kalimantan Tengah"] = arrayOf(
            "Palangkaraya",
            "Barito Selatan","Barito Timur","Barito Utara",
            "Gunung Mas","Kapuas","Katingan","Kotawaringin Barat",
            "Kotawaringin Timur","Lamandau","Murung Raya",
            "Pulang Pisau","Seruyan","Sukamara"
        )

        wilayahData["Kalimantan Selatan"] = arrayOf(
            "Banjarmasin","Banjarbaru",
            "Balangan","Banjar","Barito Kuala",
            "Hulu Sungai Selatan","Hulu Sungai Tengah","Hulu Sungai Utara",
            "Kotabaru","Tabalong","Tanah Bumbu","Tanah Laut","Tapin"
        )

        wilayahData["Kalimantan Timur"] = arrayOf(
            "Samarinda","Balikpapan","Bontang",
            "Berau","Kutai Barat","Kutai Kartanegara",
            "Kutai Timur","Mahakam Ulu","Paser","Penajam Paser Utara"
        )

        wilayahData["Kalimantan Utara"] = arrayOf(
            "Tarakan",
            "Bulungan","Malinau","Nunukan","Tana Tidung"
        )

        wilayahData["Sulawesi Utara"] = arrayOf(
            "Manado","Bitung","Kotamobagu","Tomohon",
            "Bolaang Mongondow","Bolaang Mongondow Selatan",
            "Bolaang Mongondow Timur","Bolaang Mongondow Utara",
            "Kepulauan Sangihe","Kepulauan Talaud","Minahasa",
            "Minahasa Selatan","Minahasa Tenggara","Minahasa Utara"
        )

        wilayahData["Gorontalo"] = arrayOf(
            "Gorontalo",
            "Boalemo","Bone Bolango","Gorontalo Utara","Pohuwato"
        )

        wilayahData["Sulawesi Tengah"] = arrayOf(
            "Palu",
            "Banggai","Banggai Kepulauan","Banggai Laut",
            "Buol","Donggala","Morowali","Morowali Utara",
            "Parigi Moutong","Poso","Sigi","Tojo Una-Una","Tolitoli"
        )

        wilayahData["Sulawesi Barat"] = arrayOf(
            "Mamuju",
            "Majene","Mamasa","Pasangkayu","Polewali Mandar","Mamuju Tengah"
        )

        wilayahData["Sulawesi Selatan"] = arrayOf(
            "Makassar","Parepare","Palopo",
            "Bantaeng","Barru","Bone","Bulukumba","Enrekang",
            "Gowa","Jeneponto","Kepulauan Selayar","Luwu",
            "Luwu Timur","Luwu Utara","Maros","Pangkajene dan Kepulauan",
            "Pinrang","Sidenreng Rappang","Sinjai","Soppeng",
            "Takalar","Tana Toraja","Toraja Utara","Wajo"
        )

        wilayahData["Sulawesi Tenggara"] = arrayOf(
            "Kendari","Baubau",
            "Bombana","Buton","Buton Selatan","Buton Tengah","Buton Utara",
            "Kolaka","Kolaka Timur","Kolaka Utara",
            "Konawe","Konawe Kepulauan","Konawe Selatan","Konawe Utara",
            "Muna","Muna Barat","Wakatobi"
        )

        wilayahData["Maluku"] = arrayOf(
            "Ambon","Tual",
            "Buru","Buru Selatan","Kepulauan Aru",
            "Maluku Barat Daya","Maluku Tengah","Maluku Tenggara","Seram Bagian Barat","Seram Bagian Timur"
        )

        wilayahData["Maluku Utara"] = arrayOf(
            "Ternate","Tidore Kepulauan",
            "Halmahera Barat","Halmahera Tengah","Halmahera Timur",
            "Halmahera Selatan","Halmahera Utara",
            "Kepulauan Sula","Pulau Morotai","Pulau Taliabu"
        )

        wilayahData["Papua"] = arrayOf(
            "Jayapura",
            "Biak Numfor","Jayapura","Keerom","Kepulauan Yapen",
            "Mamberamo Raya","Sarmi","Waropen"
        )

        wilayahData["Papua Barat"] = arrayOf(
            "Manokwari",
            "Fakfak","Kaimana","Manokwari Selatan","Pegunungan Arfak",
            "Teluk Bintuni","Teluk Wondama"
        )

        wilayahData["Papua Selatan"] = arrayOf(
            "Merauke",
            "Asmat","Boven Digoel","Mappi"
        )

        wilayahData["Papua Tengah"] = arrayOf(
            "Nabire",
            "Deiyai","Dogiyai","Intan Jaya","Mimika",
            "Paniai","Puncak","Puncak Jaya"
        )

        wilayahData["Papua Pegunungan"] = arrayOf(
            "Jayawijaya",
            "Lanny Jaya","Mamberamo Tengah","Nduga",
            "Pegunungan Bintang","Tolikara","Yahukimo","Yalimo"
        )

        wilayahData["Papua Barat Daya"] = arrayOf(
            "Sorong",
            "Maybrat","Raja Ampat","Sorong Selatan","Tambrauw"
        )

        wilayahData["Bali"] = arrayOf(
            "Denpasar",
            "Badung","Bangli","Buleleng","Gianyar",
            "Jembrana","Karangasem","Klungkung","Tabanan"
        )

        wilayahData["Nusa Tenggara Barat"] = arrayOf(
            "Mataram","Bima",
            "Bima","Dompu","Lombok Barat","Lombok Tengah",
            "Lombok Timur","Lombok Utara","Sumbawa","Sumbawa Barat"
        )

        wilayahData["Nusa Tenggara Timur"] = arrayOf(
            "Kupang",
            "Alor","Belu","Ende","Flores Timur","Kupang",
            "Lembata","Malaka","Manggarai","Manggarai Barat",
            "Manggarai Timur","Nagekeo","Ngada",
            "Rote Ndao","Sabu Raijua","Sikka",
            "Sumba Barat","Sumba Barat Daya","Sumba Tengah","Sumba Timur","Timor Tengah Selatan","Timor Tengah Utara"
        )

        // 2. Setup Adapter Provinsi
        val listProvinsi = wilayahData.keys.toTypedArray().sortedArray()
        val adapterProv = ArrayAdapter(this, android.R.layout.simple_list_item_1, listProvinsi)
        binding.spProvinsi.setAdapter(adapterProv)

        // 3. Logika: Saat Provinsi Dipilih, Update Kabupaten
        binding.spProvinsi.setOnItemClickListener { parent, _, position, _ ->
            val selectedProv = parent.getItemAtPosition(position).toString()

            // Reset teks kabupaten saat provinsi ganti
            binding.spKabupaten.setText("")

            // Ambil data kabupaten berdasarkan kunci provinsi yang dipilih
            val listKabupaten = wilayahData[selectedProv] ?: arrayOf()
            val adapterKab = ArrayAdapter(this, android.R.layout.simple_list_item_1, listKabupaten)
            binding.spKabupaten.setAdapter(adapterKab)

            // Tampilkan notifikasi kecil
            Toast.makeText(this, "Provinsi dipilih: $selectedProv", Toast.LENGTH_SHORT).show()
        }

        // --- Dropdown Lainnya (Agama & Gender) ---
        val listAgama = arrayOf("Islam", "Kristen", "Katolik", "Hindu", "Budha", "Konghucu")
        binding.spAgama.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, listAgama))

        val itemsGender = arrayOf("Laki-laki", "Perempuan")
        binding.spGender.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, itemsGender))
    }


    private fun setupDatePicker() {
        binding.etTglLahir.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                binding.etTglLahir.setText("$day/${month + 1}/$year")
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
}
