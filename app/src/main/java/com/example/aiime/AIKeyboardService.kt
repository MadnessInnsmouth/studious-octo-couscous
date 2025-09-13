package com.example.aiime

import android.inputmethodservice.InputMethodService
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import com.example.aiime.R

class AIKeyboardService : InputMethodService() {

    private lateinit var rootView: LinearLayout
    private var stt: VoskStt? = null
    private val listening = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    companion object {
        private const val TAG = "AIKeyboardService"
    }

    override fun onCreate() {
        super.onCreate()
        scope.launch(Dispatchers.IO) {
            AssetUnpacker.ensureModelInstalled(this@AIKeyboardService)
        }
    }

    override fun onCreateInputView(): View {
        // Prefer user's keyboard_view.xml if present; else fallback to an empty container to avoid crashes
        val layoutName = "keyboard_view"
        val layoutId = resources.getIdentifier(layoutName, "layout", packageName)
        if (layoutId == 0) {
            Log.w(TAG, "Layout '$layoutName' not found. Returning empty LinearLayout to avoid crash.")
            rootView = LinearLayout(this)
            return rootView
        }

        rootView = LayoutInflater.from(this).inflate(layoutId, null) as LinearLayout
        wireKeys()
        return rootView
    }

    private fun wireKeys() {
        fun setKey(idName: String, text: String) {
            val id = resources.getIdentifier(idName, "id", packageName)
            if (id != 0) {
                val btn = rootView.findViewById<Button>(id)
                btn?.setOnClickListener { currentInputConnection?.commitText(text, 1) }
            }
        }

        val keys = listOf(
            "key_q" to "q","key_w" to "w","key_e" to "e","key_r" to "r","key_t" to "t",
            "key_y" to "y","key_u" to "u","key_i" to "i","key_o" to "o","key_p" to "p",
            "key_a" to "a","key_s" to "s","key_d" to "d","key_f" to "f","key_g" to "g",
            "key_h" to "h","key_j" to "j","key_k" to "k","key_l" to "l",
            "key_z" to "z","key_x" to "x","key_c" to "c","key_v" to "v","key_b" to "b",
            "key_n" to "n","key_m" to "m"
        )
        for (k in keys) setKey(k.first, k.second)

        val spaceId = resources.getIdentifier("key_space", "id", packageName)
        if (spaceId != 0) rootView.findViewById<Button>(spaceId)?.setOnClickListener {
            currentInputConnection?.commitText(" ", 1)
        }

        val backId = resources.getIdentifier("key_backspace", "id", packageName)
        if (backId != 0) rootView.findViewById<Button>(backId)?.setOnClickListener {
            currentInputConnection?.deleteSurroundingText(1, 0)
        }

        val enterId = resources.getIdentifier("key_enter", "id", packageName)
        if (enterId != 0) rootView.findViewById<Button>(enterId)?.setOnClickListener {
            currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_DONE)
            currentInputConnection?.commitText("\n", 1)
        }

        val micId = resources.getIdentifier("btn_mic", "id", packageName)
        if (micId != 0) {
            val micBtn = rootView.findViewById<Button>(micId)
            micBtn?.setOnClickListener {
                if (!listening.get()) startDictation(micBtn) else stopDictation(micBtn)
            }
        }
    }

    private fun startDictation(micBtn: Button?) {
        if (stt == null) stt = VoskStt(this)
        Log.d(TAG, "Starting dictation")
        micBtn?.contentDescription = getString(R.string.accessibility_listening)
        micBtn?.