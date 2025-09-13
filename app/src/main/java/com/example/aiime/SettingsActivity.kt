package com.example.aiime

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AlertDialog.Builder(this)
            .setTitle("AI Keyboard Settings")
            .setMessage("Vosk small model is bundled and will unpack on first run. Tap the mic to dictate.")
            .setPositiveButton("OK") { d, _ -> d.dismiss(); finish() }
            .show()
    }
}
