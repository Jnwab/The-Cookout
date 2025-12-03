package com.cecs491b.thecookout.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import com.cecs491b.thecookout.uiScreens.NotificationItem
import com.cecs491b.thecookout.uiScreens.NotificationType
import com.cecs491b.thecookout.uiScreens.NotificationsScreen

class AlertsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: later, replace this with real data from Firestore
        val sampleNotifications: List<NotificationItem> = listOf(
            NotificationItem(
                id = "1",
                actorName = "Julie",
                actorInitials = "JT",
                type = NotificationType.SAVED,
                mainText = "saved your recipe",
                targetText = "Classic Carbonara",
                timeAgo = "2 hours ago",
                thumbnailUrl = "https://images.unsplash.com/photo-1528715471579-d1bcf0ba5e83"
            ),
            NotificationItem(
                id = "2",
                actorName = "Adie",
                actorInitials = "AR",
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
                actorInitials = "JN",
                type = NotificationType.FOLLOWED,
                mainText = "started following you",
                targetText = "",
                timeAgo = "1 day ago",
                showFollowBack = true
            ),
            NotificationItem(
                id = "4",
                actorName = "Kim",
                actorInitials = "KL",
                type = NotificationType.COMMENTED,
                mainText = "commented on",
                targetText = "Mediterranean Quinoa Bowl",
                timeAgo = "2 days ago",
                commentPreview = "This looks absolutely delicious! Can't wait to try it!",
                thumbnailUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c"
            ),
            NotificationItem(
                id = "5",
                actorName = "Arnav",
                actorInitials = "AM",
                type = NotificationType.SAVED,
                mainText = "saved your recipe",
                targetText = "Mediterranean Quinoa Bowl",
                timeAgo = "3 days ago",
                thumbnailUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c"
            ),
            NotificationItem(
                id = "6",
                actorName = "Julie",
                actorInitials = "JT",
                type = NotificationType.FOLLOWED,
                mainText = "started following you",
                targetText = "",
                timeAgo = "4 days ago",
                showFollowBack = true
            )
        )

        setContent {
            TheCookoutTheme {
                NotificationsScreen(
                    notifications = sampleNotifications,
                    onBackClick = { finish() },
                    onFollowBackClick = { /* TODO: follow-back logic later */ }
                )
            }
        }
    }
}
