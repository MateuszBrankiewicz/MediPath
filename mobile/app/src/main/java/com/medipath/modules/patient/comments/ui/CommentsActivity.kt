package com.medipath.modules.patient.comments.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.patient.comments.CommentsViewModel
import com.medipath.modules.patient.comments.ui.components.CommentCard
import com.medipath.modules.patient.home.HomeViewModel
import com.medipath.modules.shared.notifications.NotificationsViewModel
import com.medipath.modules.shared.notifications.ui.NotificationsActivity
import com.medipath.modules.patient.visits.ui.ReviewDetailsActivity
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.shared.components.FilterChipsConfig
import com.medipath.modules.shared.components.GenericFilterChipsSection
import com.medipath.modules.shared.components.GenericFilterToggleRow
import com.medipath.modules.shared.components.GenericSearchBar
import com.medipath.modules.shared.components.Navigation
import com.medipath.modules.shared.profile.ui.EditProfileActivity
import com.medipath.modules.shared.settings.ui.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MediPathTheme {
                CommentsScreen(
                    onLogoutClick = {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val authService = RetrofitInstance.authService
                        val sessionManager = RetrofitInstance.getSessionManager()
                        try {
                            authService.logout()
                        } catch (e: Exception) {
                            Log.e("CommentsActivity", "Logout API error", e)
                        }

                        sessionManager.deleteSessionId()

                        withContext(Dispatchers.Main) {
                            startActivity(
                                Intent(this@CommentsActivity, LoginActivity::class.java)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )
                            finish()
                        }
                    }
                }
                )
            }
        }
    }
}

@Composable
fun CommentsScreen(
    viewModel: HomeViewModel = viewModel(),
    commentsViewModel: CommentsViewModel = viewModel(),
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = LocalCustomColors.current
    
    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val shouldRedirectToLogin by viewModel.shouldRedirectToLogin.collectAsState()
    val canBeDoctor by viewModel.canBeDoctor.collectAsState()

    val comments by commentsViewModel.filteredComments.collectAsState()
    val commentsLoading by commentsViewModel.isLoading.collectAsState()
    val commentsError by commentsViewModel.error.collectAsState()
    val deleteSuccess by commentsViewModel.deleteSuccess.collectAsState()
    val searchQuery by commentsViewModel.searchQuery.collectAsState()
    val sortBy by commentsViewModel.sortBy.collectAsState()
    val sortOrder by commentsViewModel.sortOrder.collectAsState()
    val totalComments by commentsViewModel.totalComments.collectAsState()
    val commentsRedirect by commentsViewModel.shouldRedirectToLogin.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    val notificationsViewModel: NotificationsViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchUserProfile()
                notificationsViewModel.fetchNotifications()
                commentsViewModel.fetchComments()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(commentsError) {
        if (commentsError != null) {
            Toast.makeText(context, commentsError, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            Toast.makeText(context, "Comment deleted successfully", Toast.LENGTH_SHORT).show()
        }
    }

    if (shouldRedirectToLogin || commentsRedirect) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

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
    } else if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Navigation(
            notificationsViewModel = notificationsViewModel,
            screenTitle = "My Comments",
            canSwitchRole = canBeDoctor,
            onNotificationsClick = {
                context.startActivity(Intent(context, NotificationsActivity::class.java))
            },
            onEditProfileClick = {
                context.startActivity(Intent(context, EditProfileActivity::class.java))
            },
            onSettingsClick = {
                context.startActivity(Intent(context, SettingsActivity::class.java))
            },
            content = { innerPadding ->
                LazyColumn (
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(innerPadding)
                ) {
                    item {
                        GenericSearchBar(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { commentsViewModel.updateSearchQuery(it) },
                            placeholder = "Search by doctor, institution or comment...",
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                    }

                    item {
                        GenericFilterToggleRow(
                            totalItems = totalComments,
                            showingItems = comments.size,
                            showFilters = showFilters,
                            onToggleFilters = { showFilters = !showFilters }
                        )
                    }

                    if (showFilters) {
                        item {
                            GenericFilterChipsSection(
                                sortBy = sortBy,
                                sortOrder = sortOrder,
                                onSortByChange = { commentsViewModel.updateSortBy(it) },
                                onSortOrderChange = { commentsViewModel.updateSortOrder(it) },
                                onClearFilters = { commentsViewModel.clearFilters() },
                                config = FilterChipsConfig(
                                    sortByOptions = listOf(
                                        "Date",
                                        "Doctor Rating",
                                        "Institution Rating",
                                        "Doctor Name",
                                        "Institution Name"
                                    )
                                )
                            )
                        }
                    }
                    if (commentsLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (totalComments == 0) "No comments yet" else "No comments match your filters",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        items(comments, key = { it.id }) { comment ->
                            CommentCard(
                                comment = comment,
                                onEdit = { commentId ->
                                    val intent = Intent(context, ReviewDetailsActivity::class.java)
                                    intent.putExtra("COMMENT_ID", commentId)
                                    intent.putExtra("VISIT_ID", "")
                                    context.startActivity(intent)
                                },
                                onDelete = { commentId ->
                                    commentsViewModel.deleteComment(commentId)
                                },
                            )
                        }
                    }
                }
            },
            onLogoutClick = onLogoutClick,
            firstName = firstName,
            lastName = lastName,
            currentTab = "Comments"
        )
    }
}
