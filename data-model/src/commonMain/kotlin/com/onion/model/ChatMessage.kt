package com.onion.model

data class ChatMessage(
        val message: String,
        val isUser: Boolean,
        val image: ByteArray? = null,
        val videoFrames: List<ByteArray>? = null,
        val metadata: Map<String, String>? = null
)