package com.example.projectuas
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHELPER(context: Context) :
    SQLiteOpenHelper(context, "siamo_pmi.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE kegiatan (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama_kegiatan TEXT,
                tanggal TEXT,
                keterangan TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS kegiatan")
        onCreate(db)
    }
}
