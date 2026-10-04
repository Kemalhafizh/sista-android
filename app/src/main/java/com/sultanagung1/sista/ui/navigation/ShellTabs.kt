package com.sultanagung1.sista.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import com.sultanagung1.sista.core.ui.component.NavEntry
import com.sultanagung1.sista.data.model.CapabilityState

/**
 * The app's top-level places. Identical for every account: Beranda, Layanan,
 * Notifikasi, Profil. The key of each entry is the route it opens.
 */
object ShellTabs {

    /** Tab names in the app's language (nav_* in res/values, values-en, values-ar). */
    data class Labels(val home: String, val services: String, val notifications: String, val profile: String)

    fun entries(homeRoute: String, state: CapabilityState, labels: Labels): List<NavEntry> = buildList {
        // An account without a dashboard of its own lands on Layanan; one
        // tab for it is enough.
        if (homeRoute != Screen.ServicesHub.route) {
            add(NavEntry(homeRoute, labels.home, Icons.Outlined.Home, Icons.Filled.Home))
        }
        add(NavEntry(Screen.ServicesHub.route, labels.services, Icons.Outlined.Apps, Icons.Filled.Apps))
        if (state.canOpen(Screen.NotificationCenter.route)) {
            add(NavEntry(Screen.NotificationCenter.route, labels.notifications, Icons.Outlined.Notifications, Icons.Filled.Notifications))
        }
        add(NavEntry(Screen.Profile.route, labels.profile, Icons.Outlined.Person, Icons.Filled.Person))
    }
}
