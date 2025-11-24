package org.onion.diffusion.native

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import org.onion.diffusion.utils.NativeLibraryLoader

actual class DiffusionLoader actual constructor() {

    init {
        NativeLibraryLoader.loadFromResources("sdloader")
    }

    actual suspend fun getModelFilePath(): String {
        return FileKit.openFilePicker()?.file?.absolutePath ?: ""
    }

    actual fun loadModel(modelPath: String) {
    }
}