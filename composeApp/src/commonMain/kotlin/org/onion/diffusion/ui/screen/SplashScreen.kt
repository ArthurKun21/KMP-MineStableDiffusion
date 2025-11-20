package org.onion.diffusion.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.onion.theme.style.splashBackgroundEnd
import com.onion.theme.style.splashBackgroundStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import minediffusion.composeapp.generated.resources.Res
import minediffusion.composeapp.generated.resources.splash_logo
import org.jetbrains.compose.resources.painterResource
import org.onion.diffusion.ui.navigation.route.RootRoute


fun NavGraphBuilder.splashScreen(autoToMainPage: () -> Unit){
    composable(RootRoute.Splash.name) {
        val scale = remember { Animatable(0.5f) }
        val alpha = remember { Animatable(0f) }

        LaunchedEffect(true) {
            launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1000)
                )
            }
            delay(2000)
            autoToMainPage()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(splashBackgroundStart, splashBackgroundEnd)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize(0.5f)
                    .scale(scale.value)
                    .alpha(alpha.value),
                painter = painterResource(Res.drawable.splash_logo),
                contentScale = ContentScale.Fit,
                contentDescription = "Splash Logo",
            )
        }
    }
}