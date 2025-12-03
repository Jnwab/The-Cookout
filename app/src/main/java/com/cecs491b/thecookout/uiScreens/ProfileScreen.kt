package com.cecs491b.thecookout.uiScreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cecs491b.thecookout.ui.theme.CookoutOrange
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import com.cecs491b.thecookout.models.Recipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    displayName: String,
    email: String,
    phoneNumber: String,
    provider: String,
    isLoading: Boolean,
    followerCount: Int,
    followingCount: Int,
    myRecipes: List<Recipe>,
    incomingRequests: List<String>,
    onAcceptRequest: (String) -> Unit,
    onDeclineRequest: (String) -> Unit,
    onEditProfileClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val isPreview = LocalInspectionMode.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Share */ }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "The Cookout",
                            style = MaterialTheme.typography.titleMedium,
                            color = CookoutOrange,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Share your recipes!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(16.dp))

                        Avatar(isPreview = isPreview)

                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = displayName.ifBlank { "Cookout Crew" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Sharing delicious recipes and cooking adventures",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(myRecipes.size.toString(), "Recipes")
                            StatItem(followerCount.toString(), "Followers")
                            StatItem(followingCount.toString(), "Following")
                        }

                        if (incomingRequests.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Follow requests",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))

                            incomingRequests.forEach { requesterUid ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = requesterUid,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Row {
                                        TextButton(onClick = { onAcceptRequest(requesterUid) }) {
                                            Text("Accept")
                                        }
                                        TextButton(onClick = { onDeclineRequest(requesterUid) }) {
                                            Text("Decline")
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = onEditProfileClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CookoutOrange)
                        ) {
                            Text(
                                "Edit Profile",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    text = "My Recipes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )

                Spacer(Modifier.height(12.dp))

//                val sampleDishes = listOf(
//                    "https://images.unsplash.com/photo-1600891964599-f61ba0e24092",
//                    "https://images.unsplash.com/photo-1617196034796-1f84b2b6c8f7",
//                    "https://images.unsplash.com/photo-1627308595229-7830a5c91f9f",
//                    "https://images.unsplash.com/photo-1589307004391-95d2d8b92a4e",
//                    "https://images.unsplash.com/photo-1605478441568-dad29db7f66a",
//                    "https://images.unsplash.com/photo-1504674900247-0877df9cc836"
//                )

                if (myRecipes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recipes yet. Tap + to start cooking one up!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else{
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(myRecipes) { recipe ->
                            RecipeTile(imageUrl = recipe.photoUrl,
                                title = recipe.title,
                                isPreview = isPreview)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "© 2025 The Cookout. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun RecipeTile(imageUrl: String?, title: String, isPreview: Boolean){
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        if (isPreview || imageUrl == null){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun Avatar(isPreview: Boolean) {
    Box(modifier = Modifier.size(110.dp)) {
        if (isPreview) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(56.dp)
                )
            }
        } else {
            AsyncImage(
                model = "https://marketplace.canva.com/8-1Kc/MAGoQJ8-1Kc/1/tl/canva-ginger-cat-with-paws-raised-in-air-MAGoQJ8-1Kc.jpg",
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { /* TODO change avatar */ }
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-6).dp, y = (-6).dp)
                .size(30.dp)
                .clickable { /* TODO change avatar */ },
            shape = CircleShape,
            color = CookoutOrange,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Change Photo",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            count,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CookoutOrange
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

//@Composable
//private fun RecipeTile(imageUrl: String?, isPreview: Boolean) {
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .aspectRatio(1f),
//        shape = RoundedCornerShape(16.dp),
//        color = MaterialTheme.colorScheme.surfaceVariant,
//        border = BorderStroke(
//            1.dp,
//            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
//        )
//    ) {
//        if (isPreview) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(MaterialTheme.colorScheme.surfaceVariant)
//            )
//        } else {
//            AsyncImage(
//                model = imageUrl,
//                contentDescription = "Dish Image",
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.fillMaxSize()
//            )
//        }
//    }
//}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Profile – Preview (placeholders)"
)
@Composable
private fun ProfilePreview() {
    TheCookoutTheme {
        ProfileScreen(
            displayName = "Cookout Crew",
            email = "crew@cookout.app",
            phoneNumber = "",
            provider = "google",
            isLoading = false,
            followerCount = 12,
            followingCount = 5,
            myRecipes = emptyList(),
            incomingRequests = listOf("uid_abc123", "uid_xyz789"),
            onAcceptRequest = {},
            onDeclineRequest = {},
            onEditProfileClick = {},
            onSignOutClick = {},
            onSettingsClick = {}
        )
    }
}
