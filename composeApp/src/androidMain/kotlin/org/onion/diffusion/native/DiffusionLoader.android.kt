package org.onion.diffusion.native

import androidx.core.net.toUri
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.context
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import java.io.File
import java.io.FileOutputStream

actual class DiffusionLoader actual constructor() {

    init {
        System.loadLibrary("sdloader")
    }

    actual suspend fun getModelFilePath(): String {
        val androidFile = FileKit.openFilePicker(type = FileKitType.File(listOf(
            "safetensors","ckpt","pt","bin","gguf"
        )))
        FileKit.context.contentResolver.openInputStream((androidFile?.absolutePath() ?: return "").toUri()).use { inputStream ->
            FileOutputStream(File(FileKit.context.filesDir, androidFile.name)).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
        }
        return File(FileKit.context.filesDir, androidFile.name).absolutePath
    }

    actual fun loadModel(modelPath: String) {
    }
}