package com.cecs491b.thecookout.uiScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import com.cecs491b.thecookout.viewmodels.SavedRecipesViewModel
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment

@Composable
fun SavedPostsScreen(viewModel: SavedRecipesViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("All") }

    val savedIds by viewModel.savedIds.collectAsState()
    val savedRecipes by viewModel.savedRecipes.collectAsState()

    val filteredRecipes = remember(query, selected, savedRecipes){
        savedRecipes.filter{
            (selected == "All" || it.category == selected) &&
                    it.title.contains(query, ignoreCase = true)
        }
    }

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

        if (filteredRecipes.isEmpty()){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = "No saved recipes yet. \n Tap the ❤\uFE0F on a recipe to save it!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp)
        ) {
            items(filteredRecipes) { recipe ->
                RecipeCard(r = recipe,
                    isSaved = recipe.id in savedIds,
                    onSaveClick = {viewModel.toggleSave(recipe.id)},
                    onClick = {
                    // TODO: open recipe detail later
                })
            }
        }

        // space for the bottom nav bar
        Spacer(Modifier.height(56.dp))
    }

}

//@Preview(showBackground = true)
//@Composable
//private fun SavedPostsScreenPreview() {
//    TheCookoutTheme {
//        SavedPostsScreen()
//    }
//}
}
