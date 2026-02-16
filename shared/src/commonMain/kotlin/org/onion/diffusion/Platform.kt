package org.onion.diffusion

interface Platform {
    val name: String
    val isMacOS: Boolean get() = false
}

expect fun getPlatform(): Platform