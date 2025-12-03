package com.cecs491b.thecookout.uiScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cecs491b.thecookout.ui.theme.CookoutOrange
import com.cecs491b.thecookout.ui.theme.LightGreyText
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme

@Composable
fun SavedPostsScreen(
    onAddRecipe: () -> Unit = {},
    onOpenRecipe: (Recipe) -> Unit = {},
    onTabChange: (String) -> Unit = {},
    onCreateRecipeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onSavedClick: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("All") }

    // replace items with call to db
    /*val items = remember(query, selected) {
        demoRecipes.filter {
            (selected == "All" || it.category == selected) &&
                    it.title.contains(query, ignoreCase = true)
        }
    }*/

    Scaffold(
        containerColor = Color.White,
        bottomBar = { BottomNavBar("saved", onProfileClick, onSavedClick, onHomeClick) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRecipeClick,
                containerColor = CookoutOrange,
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Outlined.Add, contentDescription = "Create Recipe") }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            TopHeader()
            Spacer(Modifier.height(8.dp))
            SearchField(query) { query = it }
            Spacer(Modifier.height(12.dp))
            CategoryChipsScrollable(
                labels = categories,
                selected = selected,
                onSelect = { selected = it; onTabChange(it) }
            )
            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                //items(items) { r -> RecipeCard(r) { onOpenRecipe(r) } }
            }

            Spacer(Modifier.height(56.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SavedPostsScreenPreview() {
    TheCookoutTheme(darkTheme = false, dynamicColor = false) {
        SavedPostsScreen(
            onCreateRecipeClick = {}
        )
    }
}
