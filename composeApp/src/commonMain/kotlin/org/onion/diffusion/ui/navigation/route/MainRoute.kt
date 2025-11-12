package org.onion.diffusion.ui.navigation.route

import minediffusion.composeapp.generated.resources.Res
import minediffusion.composeapp.generated.resources.home
import minediffusion.composeapp.generated.resources.ic_home


sealed interface MainRoute {
    data object HomeRoute : RoutePage(name = "HomeRoute",Res.drawable.ic_home, Res.string.home)
    data object SettingRoute : RoutePage(name = "SettingRoute",Res.drawable.ic_home, Res.string.home)
    data object MineRoute : RoutePage(name = "MineRoute",Res.drawable.ic_home, Res.string.home)
}
val NAV_BOTTOM_ITEMS = listOf(
    MainRoute.HomeRoute,
    MainRoute.SettingRoute,
    MainRoute.MineRoute
)