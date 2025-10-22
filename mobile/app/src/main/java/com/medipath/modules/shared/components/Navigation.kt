package com.medipath.modules.shared.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.R
import com.medipath.core.models.NavTab
import com.medipath.core.theme.LocalCustomColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(
    content: @Composable (PaddingValues) -> Unit,
    onNotificationsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    firstName: String,
    lastName: String,
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val colors = LocalCustomColors.current

    val tabs = listOf(
        NavTab("Dashboard", Icons.Outlined.Home, colors.dashboardIcon),
        NavTab("Visits", Icons.Outlined.Event, colors.visitsIcon),
        NavTab("Prescriptions", Icons.Outlined.Receipt, colors.prescriptionsIcon),
        NavTab("Referrals", Icons.AutoMirrored.Outlined.Send, colors.referralsIcon),
        NavTab("Medical history", Icons.Outlined.MedicalServices, colors.medicalHistoryIcon),
        NavTab("Comments", Icons.AutoMirrored.Outlined.Comment, colors.commentsIcon),
        NavTab("Reminders", Icons.Outlined.Notifications, colors.remindersIcon)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .background(colors.blue900)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "MediPath logo",
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "MediPath",
                        fontSize = 35.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.background
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                tabs.forEach { tab ->
                    val isSelected = currentTab == tab.name
                    val background = if (isSelected)
                        colors.tabBackground.copy(alpha = 0.15f)
                    else
                        colors.blue900

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTabSelected(tab.name) }
                            .background(background, RoundedCornerShape(10.dp))
                            .padding(horizontal = 16.dp, vertical = 17.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.name,
                            tint = tab.iconTint,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text = tab.name,
                            color = MaterialTheme.colorScheme.background,
                            fontSize = 18.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Thin
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                Text(
                    text = "$firstName $lastName",
                    color = MaterialTheme.colorScheme.background,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* TODO: onProfileClick() */ }
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                )
                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text("LOGOUT")
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Hello, $firstName", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 28.sp, modifier = Modifier.padding(horizontal = 10.dp)) },
                    modifier = Modifier.padding(horizontal = 10.dp),
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(color = MaterialTheme.colorScheme.background, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(onClick = { onNotificationsClick() }) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(color = MaterialTheme.colorScheme.background, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ){
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}