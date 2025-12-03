package com.cecs491b.thecookout.uiScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme

@Composable
fun SavedPostsScreen() {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("All") }

    // TODO: replace this with real saved recipes list from Firestore later
    val items = remember(query, selected) {
        emptyList<Recipe>()   // <- no dependency on demoRecipes
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        TopHeader()

        Spacer(Modifier.height(8.dp))

        SearchField(value = query) { query = it }

        Spacer(Modifier.height(12.dp))

        CategoryChipsScrollable(
            labels = categories,
            selected = selected,
            onSelect = { selected = it }
        )

        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp)
        ) {
            items(items) { recipe ->
                RecipeCard(recipe) {
                    // TODO: open recipe detail later
                }
            }
        }

        // space for the bottom nav bar
        Spacer(Modifier.height(56.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun SavedPostsScreenPreview() {
    TheCookoutTheme {
        SavedPostsScreen()
    }
}
