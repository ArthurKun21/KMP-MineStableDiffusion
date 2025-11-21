package org.onion.diffusion.native

expect class DiffusionLoader(){
    suspend fun getModelFilePath():String
    fun loadModel(modelPath: String)
}