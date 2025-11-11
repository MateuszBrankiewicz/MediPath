package com.medipath.modules.shared.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.MediPathApplication
import com.medipath.R
import com.medipath.core.models.NavTab
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.theme.LocalCustomColors
import com.medipath.modules.patient.codes.ui.CodesActivity
import com.medipath.modules.patient.home.ui.HomeActivity
import com.medipath.modules.patient.visits.ui.VisitsActivity
import com.medipath.modules.patient.medical_history.ui.MedicalHistoryActivity
import com.medipath.modules.patient.comments.ui.CommentsActivity
import com.medipath.modules.shared.notifications.NotificationsViewModel
import com.medipath.modules.shared.reminders.ui.RemindersActivity
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.shared.profile.ui.EditProfileActivity
import com.medipath.modules.doctor.dashboard.ui.DoctorDashboardActivity
import com.medipath.modules.doctor.schedule.ui.DoctorScheduleActivity
import com.medipath.modules.doctor.visits.ui.DoctorVisitsActivity
import com.medipath.modules.doctor.patients.ui.DoctorPatientsActivity
import kotlinx.coroutines.launch

object NavigationRouter {
    private var isNavigating = false
    
    fun navigateToTab(context: Context, tab: String, currentTab: String, isDoctor: Boolean = false) {
        if (isNavigating) return
        if (tab == currentTab) return
        
        val activityClass = if (isDoctor) {
            when (tab) {
                "Dashboard" -> DoctorDashboardActivity::class.java
                "Schedule" -> DoctorScheduleActivity::class.java
                "Visits" -> DoctorVisitsActivity::class.java
                "Patients" -> DoctorPatientsActivity::class.java
                "Reminders" -> RemindersActivity::class.java
                else -> return
            }
        } else {
            when (tab) {
                "Dashboard" -> HomeActivity::class.java
                "Visits" -> VisitsActivity::class.java
                "Prescriptions" -> CodesActivity::class.java
                "Referrals" -> CodesActivity::class.java
                "Medical history" -> MedicalHistoryActivity::class.java
                "Comments" -> CommentsActivity::class.java
                "Reminders" -> RemindersActivity::class.java
                else -> return
            }
        }
        
        isNavigating = true
        try {
            val intent = Intent(context, activityClass)
            
            if (tab == "Reminders") {
                intent.putExtra("isDoctor", isDoctor)
            }
            
            if (!isDoctor) {
                when (tab) {
                    "Prescriptions" -> intent.putExtra("code_type", "PRESCRIPTION")
                    "Referrals" -> intent.putExtra("code_type", "REFERRAL")
                }
            }
            
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
            (context as? ComponentActivity)?.overridePendingTransition(0, 0)
        } finally {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                isNavigating = false
            }, 500)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(
    notificationsViewModel: NotificationsViewModel,
    content: @Composable (PaddingValues) -> Unit,
    screenTitle: String? = null,
    onNotificationsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    firstName: String,
    lastName: String,
    currentTab: String,
    isDoctor: Boolean = false,
    canSwitchRole: Boolean = false
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val colors = LocalCustomColors.current
    var showUserMenu by remember { mutableStateOf(false) }
    var showRoleMenu by remember { mutableStateOf(false) }
    var isNavigating by remember { mutableStateOf(false) }

    val shouldRedirect by notificationsViewModel.shouldRedirectToLogin.collectAsState()
    if (shouldRedirect) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            val sessionManager = RetrofitInstance.getSessionManager()
            sessionManager.deleteSessionId()
            context.startActivity(
                Intent(context, LoginActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            (context as? ComponentActivity)?.finish()
        }
    }

    val notifications by notificationsViewModel.notifications.collectAsState()
    val unreadNotificationsCount by remember {
        derivedStateOf { notifications.count { !it.read } }
    }

    val tabs = if (isDoctor) {
        listOf(
            NavTab("Dashboard", Icons.Outlined.Home, colors.dashboardIcon),
            NavTab("Schedule", Icons.Outlined.CalendarMonth, colors.visitsIcon),
            NavTab("Visits", Icons.Outlined.Event, colors.prescriptionsIcon),
            NavTab("Patients", Icons.Outlined.People, colors.referralsIcon),
            NavTab("Reminders", Icons.Outlined.Notifications, colors.remindersIcon)
        )
    } else {
        listOf(
            NavTab("Dashboard", Icons.Outlined.Home, colors.dashboardIcon),
            NavTab("Visits", Icons.Outlined.Event, colors.visitsIcon),
            NavTab("Prescriptions", Icons.Outlined.Receipt, colors.prescriptionsIcon),
            NavTab("Referrals", Icons.AutoMirrored.Outlined.Send, colors.referralsIcon),
            NavTab("Medical history", Icons.Outlined.MedicalServices, colors.medicalHistoryIcon),
            NavTab("Comments", Icons.AutoMirrored.Outlined.Comment, colors.commentsIcon),
            NavTab("Reminders", Icons.Outlined.Notifications, colors.remindersIcon)
        )
    }

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
                            .clickable(enabled = !isNavigating) {
                                if (isNavigating) return@clickable
                                isNavigating = true
                                
                                scope.launch { 
                                    drawerState.close()
                                    kotlinx.coroutines.delay(300)
                                    NavigationRouter.navigateToTab(context, tab.name, currentTab, isDoctor)
                                    kotlinx.coroutines.delay(200)
                                    isNavigating = false
                                }
                            }
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
                        .clickable(enabled = !isNavigating) {
                            context.startActivity(Intent(context, EditProfileActivity::class.java))
                        }
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            screenTitle ?: "Hello, $firstName",
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.primary, 
                            fontSize = 28.sp, 
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) 
                    },
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
                                BadgedBox(
                                    badge = {
                                        if (unreadNotificationsCount > 0) {
                                            Badge(
                                                containerColor = colors.orange800,
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    text = if (unreadNotificationsCount > 99) "99+" else unreadNotificationsCount.toString(),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        
                        Box {
                            IconButton(onClick = { showUserMenu = !showUserMenu }) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(color = MaterialTheme.colorScheme.background, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ){
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "User menu",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            DropdownMenu(
                                expanded = showUserMenu,
                                onDismissRequest = { 
                                    showUserMenu = false
                                    showRoleMenu = false
                                },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface)
                                    .width(200.dp)
                            ) {

                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Outlined.SwapHoriz,
                                                    contentDescription = "Select role",
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    "Select role",
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Icon(
                                                    if (showRoleMenu) Icons.Outlined.ArrowDropUp else Icons.Outlined.ArrowDropDown,
                                                    contentDescription = "Expand",
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        },
                                        onClick = { showRoleMenu = !showRoleMenu },
                                        enabled = true
                                    )

                                    if (showRoleMenu) {
                                        DropdownMenuItem(
                                            text = { Text("Patient") },
                                            onClick = {
                                                showRoleMenu = false
                                                showUserMenu = false
                                                scope.launch {
                                                    try {
                                                        RetrofitInstance.userService.updateDefaultPanel(1)
                                                        val intent = Intent(context, HomeActivity::class.java).apply {
                                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        }
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Error switching to patient: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.padding(start = 40.dp)
                                        )
                                        if(canSwitchRole) {
                                            DropdownMenuItem(
                                                text = { Text("Doctor") },
                                                onClick = {
                                                    showRoleMenu = false
                                                    showUserMenu = false
                                                    scope.launch {
                                                        try {
                                                            RetrofitInstance.userService.updateDefaultPanel(
                                                                2
                                                            )
                                                            val intent = Intent(
                                                                context,
                                                                DoctorDashboardActivity::class.java
                                                            ).apply {
                                                                flags =
                                                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                            }
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            Toast.makeText(
                                                                context,
                                                                "Error switching to doctor: ${e.message}",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.padding(start = 40.dp)
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                )
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Outlined.Edit,
                                                contentDescription = "Edit profile",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Edit profile", color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    },
                                    onClick = {
                                        showUserMenu = false
                                        onEditProfileClick()
                                    }
                                )
                                
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Outlined.Settings,
                                                contentDescription = "Settings",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Settings", color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    },
                                    onClick = {
                                        showUserMenu = false
                                        onSettingsClick()
                                    }
                                )
                                
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                )
                                
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Outlined.Logout,
                                                contentDescription = "Logout",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Logout", color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    onClick = {
                                        showUserMenu = false
                                        (context.applicationContext as MediPathApplication).disconnectWebSocket()
                                        onLogoutClick()
                                    }
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