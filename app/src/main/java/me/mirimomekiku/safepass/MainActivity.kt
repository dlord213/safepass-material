package me.mirimomekiku.safepass

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.autofill.AutofillManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import me.mirimomekiku.safepass.enums.Screens
import me.mirimomekiku.safepass.objects.TinkManager
import me.mirimomekiku.safepass.ui.compositions.LocalNavController
import me.mirimomekiku.safepass.ui.screens.GeneratorScreen
import me.mirimomekiku.safepass.ui.screens.SafeScreen
import me.mirimomekiku.safepass.ui.screens.SearchScreen
import me.mirimomekiku.safepass.ui.screens.SettingsScreen
import me.mirimomekiku.safepass.ui.screens.add.AddAppScreen
import me.mirimomekiku.safepass.ui.screens.add.AddCardScreen
import me.mirimomekiku.safepass.ui.screens.add.AddWebsiteScreen
import me.mirimomekiku.safepass.ui.screens.view.ViewAppCredentialScreen
import me.mirimomekiku.safepass.ui.screens.view.ViewCardCredentialScreen
import me.mirimomekiku.safepass.ui.screens.view.ViewWebsiteCredentialScreen
import me.mirimomekiku.safepass.ui.theme.SafePassTheme

class MainActivity : FragmentActivity() {

    override fun onStart() {
        super.onStart()
        TinkManager.init(applicationContext)

        val autofillManager = getSystemService(AutofillManager::class.java)

        if (autofillManager != null && !autofillManager.hasEnabledAutofillServices()) {
            Toast.makeText(
                applicationContext,
                "If you wanna use the autofill service, simply enable.",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                data = "package:$packageName".toUri()
            }
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            var currentScreen by rememberSaveable { mutableIntStateOf(Screens.Safe.ordinal) }

            SafePassTheme {
                CompositionLocalProvider(LocalNavController provides navController) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(), bottomBar = {

                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route

                            val navBarScreens =
                                listOf(Screens.Safe, Screens.Generator, Screens.Settings)

                            if (currentRoute in navBarScreens.map { it.name }) {
                                NavigationBar(
                                    windowInsets = NavigationBarDefaults.windowInsets,
                                    modifier = Modifier.clip(
                                        RoundedCornerShape(
                                            topStart = 24.dp, topEnd = 24.dp
                                        )
                                    )
                                ) {
                                    Screens.entries.slice(0..1).forEachIndexed { index, screen ->
                                        NavigationBarItem(
                                            selected = currentScreen == index,
                                            onClick = {
                                                navController.navigate(route = screen.name)
                                                currentScreen = index
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = screen.icon,
                                                    contentDescription = screen.name
                                                )
                                            },
                                            label = {
                                                Text(screen.name)
                                            })
                                    }
                                }
                            }
                        }) { innerPadding ->
                        NavHost(
                            navController,
                            Screens.Safe.name,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(
                                route = Screens.Safe.name,
                            ) {
                                SafeScreen()
                            }
                            composable(
                                route = Screens.Generator.name,
                            ) {
                                GeneratorScreen()
                            }
                            composable(
                                route = Screens.Settings.name,
                            ) {
                                SettingsScreen()
                            }
                            composable(route = Screens.AddWebsite.name) {
                                AddWebsiteScreen()
                            }
                            composable(route = Screens.AddCard.name) {
                                AddCardScreen()
                            }
                            composable(route = Screens.AddApps.name) {
                                AddAppScreen()
                            }
                            composable(
                                route = "view_website/{id}",
                            ) { backStackEntry ->
                                ViewWebsiteCredentialScreen(backStackEntry)
                            }
                            composable(
                                route = "view_card/{id}",
                            ) { backStackEntry ->
                                ViewCardCredentialScreen(backStackEntry)
                            }
                            composable(route = "view_app/{id}") { backStackEntry ->
                                ViewAppCredentialScreen(backStackEntry)
                            }
                            composable(route = "search/{query}") { backStackEntry ->
                                SearchScreen(backStackEntry)
                            }
                        }
                    }
                }
            }
        }
    }
}