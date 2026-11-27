package id.dhautogiveaway.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 48, 32, 48)
        }
        val title = TextView(this).apply {
            text = "DH Auto Giveaway\n\nDetektor giveaway Donghive"
            textSize = 22f
            gravity = Gravity.CENTER
        }
        val info = TextView(this).apply {
            text = "Aktifkan layanan aksesibilitas untuk mendeteksi kartu giveaway, jumlah slot, dan tombol Klaim Sekarang. Aplikasi hanya memberi notifikasi; klaim tetap dilakukan dan dikonfirmasi oleh pengguna."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 28, 0, 28)
        }
        val settings = Button(this).apply {
            text = "Buka Pengaturan Aksesibilitas"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        layout.addView(title)
        layout.addView(info)
        layout.addView(settings)
        setContentView(layout)
    }
}
