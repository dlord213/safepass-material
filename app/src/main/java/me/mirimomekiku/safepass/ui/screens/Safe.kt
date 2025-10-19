package me.mirimomekiku.safepass.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import me.mirimomekiku.safepass.db.AppDatabase
import me.mirimomekiku.safepass.db.entity.AppCredentials
import me.mirimomekiku.safepass.db.entity.CardCredentials
import me.mirimomekiku.safepass.db.entity.WebsiteCredentials
import me.mirimomekiku.safepass.enums.Screens
import me.mirimomekiku.safepass.objects.CryptoUtil
import me.mirimomekiku.safepass.objects.TinkManager
import me.mirimomekiku.safepass.ui.composables.pages.safe.AppsPage
import me.mirimomekiku.safepass.ui.composables.pages.safe.CardsPage
import me.mirimomekiku.safepass.ui.composables.pages.safe.SafeHeading
import me.mirimomekiku.safepass.ui.composables.pages.safe.SafeSearch
import me.mirimomekiku.safepass.ui.composables.pages.safe.WebsitesPage
import me.mirimomekiku.safepass.ui.compositions.LocalNavController

enum class PagerButtons(val icon: ImageVector) {
    Websites(icon = Icons.Outlined.Cloud), Cards(icon = Icons.Outlined.CreditCard),
    Apps(icon = Icons.Outlined.Apps),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeScreen() {
    val context = LocalContext.current
    val activity = LocalActivity.current as FragmentActivity
    val navController = LocalNavController.current
    val executor = ContextCompat.getMainExecutor(context)

    // heading states
    val headingPagerState = rememberPagerState(
        pageCount = { 3 }, initialPage = 0
    )
    val headingPagerScope = rememberCoroutineScope()

    // filter button states
    var selectedFilterButton by rememberSaveable { mutableStateOf("Websites") }
    val filterPagerState = rememberPagerState(
        pageCount = { 3 }, initialPage = 0
    )
    val filterPagerScope = rememberCoroutineScope()
    val filterScrollState = rememberScrollState()

    // credentials
    var websiteCredentials by rememberSaveable { mutableStateOf(emptyList<WebsiteCredentials>()) }
    var cardCredentials by rememberSaveable { mutableStateOf(emptyList<CardCredentials>()) }
    var appCredentials by rememberSaveable { mutableStateOf(emptyList<AppCredentials>()) }

    LaunchedEffect(Unit) {
        suspend fun loadWebsiteCredentials(): List<WebsiteCredentials> {
            val dao = AppDatabase.getDatabase(context).websiteCredentialsDao()
            val aead = TinkManager.getAead()

            return dao.getAllCredentials().map { cred ->
                cred.copy(password = CryptoUtil.decrypt(aead, cred.password, cred.domain))
            }
        }

        suspend fun loadCardCredentials(): List<CardCredentials> {
            val dao = AppDatabase.getDatabase(context).cardCredentialsDao()
            val aead = TinkManager.getAead()

            return dao.getAllCards().map { cred ->
                cred.copy(cardNumber = CryptoUtil.decrypt(aead, cred.cardNumber, cred.cardHolder))
            }
        }

        suspend fun loadAppCredentials(): List<AppCredentials> {
            val dao = AppDatabase.getDatabase(context).appCredentialsDao()
            val aead = TinkManager.getAead()

            return dao.getAllCredentials().map { cred ->
                cred.copy(password = CryptoUtil.decrypt(aead, cred.password, cred.packageName))
            }
        }

        websiteCredentials = loadWebsiteCredentials()
        cardCredentials = loadCardCredentials()
        appCredentials = loadAppCredentials()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HorizontalPager(
                state = headingPagerState, modifier = Modifier.fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> SafeHeading(pagerState = headingPagerState, scope = headingPagerScope)
                    1 -> SafeSearch(pagerState = headingPagerState, scope = headingPagerScope)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(state = filterScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PagerButtons.entries.forEachIndexed { index, button ->
                    if (selectedFilterButton == button.name) {
                        FilledTonalButton(onClick = {
                            selectedFilterButton = button.name
                            filterPagerScope.launch {
                                filterPagerState.animateScrollToPage(index)
                            }
                        }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = button.icon, contentDescription = button.name)
                                Text(
                                    text = button.name, style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    } else {
                        OutlinedButton(onClick = {
                            selectedFilterButton = button.name
                            filterPagerScope.launch {
                                filterPagerState.animateScrollToPage(index)
                            }
                        }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = button.icon, contentDescription = button.name)
                                Text(
                                    text = button.name, style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }

            HorizontalPager(state = filterPagerState, userScrollEnabled = false) { page ->
                when (page) {
                    0 -> WebsitesPage(websiteCredentials, modifier = Modifier.weight(1f))
                    1 -> CardsPage(cardCredentials, modifier = Modifier.weight(1f))
                    2 -> AppsPage(appCredentials, modifier = Modifier.weight(1f))
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                if (selectedFilterButton == "Websites") {
                    navController.navigate(Screens.AddWebsite.name)
                } else if (selectedFilterButton == "Cards") {
                    navController.navigate(Screens.AddCard.name)
                } else if (selectedFilterButton == "Apps") {
                    navController.navigate(Screens.AddApps.name)
                }
            },
            icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
            text = { Text("Add ${selectedFilterButton.lowercase()}") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )
    }
}