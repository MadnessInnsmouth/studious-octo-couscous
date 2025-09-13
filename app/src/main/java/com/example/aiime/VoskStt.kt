package com.example.aiime

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import org.json.JSONObject

class VoskStt(private val ctx: Context) {
    private var recognizer: Recognizer? = null
    private var model: Model? = null
    private var job: Job? = null
    private val running = AtomicBoolean(false)

    fun start(
        onPartial: (String) -> Unit,
        onFinal: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (running.get()) return
        running.set(true)

        job = CoroutineScope(Dispatchers.Default).launch {
            try {
                if (model == null) {
                    val modelDir = File(ctx.filesDir, "model-en")
                    if (!modelDir.exists() || modelDir.list()?.isEmpty() != false) {
                        withContext(Dispatchers.Main) { onError("Model not installed.") }
                        running.set(false)
                        return@launch
                    }
                    model = Model(modelDir.absolutePath)
                }
                recognizer = Recognizer(model, 16000f)

                val minBuf = AudioRecord.getMinBufferSize(
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val recorder = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuf * 2
                )
                val buf = ByteArray(4096)
                recorder.startRecording()

                while (running.get()) {
                    val n = recorder.read(buf, 0, buf.size)
                    if (n > 0) {
                        if (recognizer!!.acceptWaveForm(buf, n)) {
                            val res = recognizer!!.result
                            val text = JSONObject(res).optString("text")
                            if (text.isNotBlank()) withContext(Dispatchers.Main) { onFinal(text) }
                        } else {
                            val part = JSONObject(recognizer!!.partialResult).optString("partial")
                            if (part.isNotBlank()) withContext(Dispatchers.Main) { onPartial(part) }
                        }
                    }
                }

                recorder.stop()
                recorder.release()

                val last = JSONObject(recognizer!!.finalResult).optString("text")
                if (last.isNotBlank()) withContext(Dispatchers.Main) { onFinal(last) }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Unknown error") }
            }
        }
    }

    fun stop() {
        running.set(false)
        job?.cancel()
    }

    fun release() {
        stop()
        recognizer?.close()
        model?.close()
    }
}
