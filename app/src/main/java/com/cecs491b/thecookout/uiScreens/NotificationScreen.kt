package com.cecs491b.thecookout.uiScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cecs491b.thecookout.ui.theme.CookoutOrange
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme

// --- data model ---

enum class NotificationType {
    SAVED, RATED, FOLLOWED, COMMENTED
}

data class NotificationItem(
    val id: String,
    val actorName: String,
    val actorInitials: String,
    val type: NotificationType,
    val mainText: String,     // e.g. "saved your recipe"
    val targetText: String,   // e.g. "Classic Carbonara"
    val timeAgo: String,      // "2 hours ago"
    val ratingValue: String? = null,   // e.g. "5 stars"
    val commentPreview: String? = null,
    val showFollowBack: Boolean = false,
    val thumbnailUrl: String? = null
)

// --- screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<NotificationItem>,
    onBackClick: () -> Unit = {},
    onFollowBackClick: (NotificationItem) -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            items(notifications, key = { it.id }) { item ->
                NotificationRow(
                    item = item,
                    onFollowBackClick = { onFollowBackClick(item) }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    thickness = 1.dp
                )
            }
        }
    }
}

// --- row ---

@Composable
private fun NotificationRow(
    item: NotificationItem,
    onFollowBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // LEFT: avatar circle with small icon badge
        Box(
            modifier = Modifier.size(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(CookoutOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.actorInitials,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            SmallTypeBadge(
                type = item.type,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        // CENTER: text
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            // line with name + action + target
            Row {
                Text(
                    text = item.actorName,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = item.mainText,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (item.targetText.isNotBlank()) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = item.targetText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // rating if present
            item.ratingValue?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // comment preview if present
            item.commentPreview?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "\"$it\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = item.timeAgo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(8.dp))

        // RIGHT: either Follow Back button or recipe thumbnail (or nothing)
        when {
            item.showFollowBack -> {
                Button(
                    onClick = onFollowBackClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CookoutOrange),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Follow Back",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item.thumbnailUrl != null -> {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = "Recipe image",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            else -> {
                // nothing
            }
        }
    }
}

@Composable
private fun SmallTypeBadge(
    type: NotificationType,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = when (type) {
        NotificationType.SAVED -> Icons.Filled.BookmarkBorder to Color.White
        NotificationType.RATED -> Icons.Filled.Star to Color.White
        NotificationType.FOLLOWED -> Icons.Filled.PersonAdd to Color.White
        NotificationType.COMMENTED -> Icons.Filled.ChatBubbleOutline to Color.White
    }

    Surface(
        modifier = modifier.size(20.dp),
        shape = CircleShape,
        color = CookoutOrange,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// --- preview with fake data ---

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NotificationsPreview() {
    val sample = listOf(
        NotificationItem(
            id = "1",
            actorName = "Julie",
            actorInitials = "JL",
            type = NotificationType.SAVED,
            mainText = "saved your recipe",
            targetText = "Classic Carbonara",
            timeAgo = "2 hours ago",
            thumbnailUrl = "https://images.unsplash.com/photo-1528715471579-d1bcf0ba5e83"
        ),
        NotificationItem(
            id = "2",
            actorName = "Adie",
            actorInitials = "AD",
            type = NotificationType.RATED,
            mainText = "rated your recipe",
            targetText = "Chocolate Lava Cake",
            timeAgo = "5 hours ago",
            ratingValue = "5 stars",
            thumbnailUrl = "https://images.unsplash.com/photo-1601972599720-36938d4ecd36"
        ),
        NotificationItem(
            id = "3",
            actorName = "Joshua",
            actorInitials = "JH",
            type = NotificationType.FOLLOWED,
            mainText = "started following you",
            targetText = "",
            timeAgo = "1 day ago",
            showFollowBack = true
        ),
        NotificationItem(
            id = "4",
            actorName = "Kim",
            actorInitials = "KM",
            type = NotificationType.COMMENTED,
            mainText = "commented on",
            targetText = "Mediterranean Quinoa Bowl",
            timeAgo = "2 days ago",
            commentPreview = "This looks absolutely delicious! Can't wait to try it!",
            thumbnailUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c"
        ),
        NotificationItem(
            id = "5",
            actorName = "Adie",
            actorInitials = "AD",
            type = NotificationType.SAVED,
            mainText = "saved your recipe",
            targetText = "Mediterranean Quinoa Bowl",
            timeAgo = "3 days ago",
            thumbnailUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c"
        ),
        NotificationItem(
            id = "6",
            actorName = "Julie",
            actorInitials = "JL",
            type = NotificationType.FOLLOWED,
            mainText = "started following you",
            targetText = "",
            timeAgo = "4 days ago",
            showFollowBack = true
        )
    )

    TheCookoutTheme {
        NotificationsScreen(
            notifications = sample
        )
    }
}
