package org.onion.diffusion.ui.navigation.route

import minediffusion.composeapp.generated.resources.Res
import minediffusion.composeapp.generated.resources.home
import minediffusion.composeapp.generated.resources.ic_home


sealed interface DetailRoute {
    data object Home : RoutePage(name = "Home",Res.drawable.ic_home, Res.string.home)
    data object Setting : RoutePage(name = "Setting",Res.drawable.ic_home, Res.string.home)
    data object Mine : RoutePage(name = "Mine",Res.drawable.ic_home, Res.string.home)
    data object BookSource : RoutePage(name = "BookSource",Res.drawable.ic_home, Res.string.home)
}