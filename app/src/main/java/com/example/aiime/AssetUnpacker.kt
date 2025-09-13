package com.example.aiime

import android.content.Context
import java.io.File
import java.util.zip.ZipInputStream

object AssetUnpacker {
    fun ensureModelInstalled(ctx: Context) {
        val target = File(ctx.filesDir, "model-en")
        if (target.exists() && target.isDirectory && target.list()?.isNotEmpty() == true) return

        runCatching {
            ctx.assets.open("model-en.zip").use { input ->
                target.mkdirs()
                ZipInputStream(input).use { zis ->
                    var entry = zis.nextEntry
                    val buffer = ByteArray(8192)
                    while (entry != null) {
                        val outFile = File(target, entry.name)
                        if (entry.isDirectory) {
                            outFile.mkdirs()
                        } else {
                            outFile.parentFile?.mkdirs()
                            outFile.outputStream().use { out ->
                                while (true) {
                                    val len = zis.read(buffer)
                                    if (len <= 0) break
                                    out.write(buffer, 0, len)
                                }
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
        }
    }
}
