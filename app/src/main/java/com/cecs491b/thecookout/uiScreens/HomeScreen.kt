package com.cecs491b.thecookout.uiScreens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.cecs491b.thecookout.viewmodels.SavedRecipesViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.scale

// ---------- DATA ----------
data class Recipe(
    val id: String,
    val title: String,
    val chef: String,
    val likes: Int,
    val comments: Int,
    val mins: Int,
    val imageUrl: String,
    val category: String
)

val categories = listOf("All", "Breakfast", "Lunch", "Dinner", "Desserts")

internal val demoRecipes = listOf(
    Recipe(
        "1", "Classic Beef Burger", "Sarah Kitchen", 676, 1, 15,
        "https://www.allrecipes.com/thmb/5JVfA7MxfTUPfRerQMdF-nGKsLY=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/25473-the-perfect-basic-burger-DDMFS-4x3-56eaba3833fd4a26a82755bcd0be0c54.jpg",
        "Lunch"
    ),
    Recipe(
        "2", "Creamy Pasta Carbonara", "Chef Marco", 256, 42, 20,
        "https://therecipecritic.com/wp-content/uploads/2012/07/creamy_bacon_carbonara.jpg",
        "Dinner"
    ),
    Recipe(
        "3", "Fresh Garden Salad", "Emma Green", 189, 23, 8,
        "https://feelgoodfoodie.net/wp-content/uploads/2023/03/Everyday-Garden-Salad-07.jpg",
        "Lunch"
    ),
    Recipe(
        "4", "Margherita Pizza", "Tony's Kitchen", 342, 66, 25,
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRoD0mNU4MlbDRw_NgpU-q8gw799bMypX73iA&s",
        "Dinner"
    ),
    Recipe(
        "5", "Garlic Butter Steak Bites", "Chef Nina", 211, 18, 14,
        "https://www.modernhoney.com/wp-content/uploads/2022/09/Garlic-Butter-Steak-Bites-9-scaled.jpg",
        "Dinner"
    ),
    Recipe(
        "6", "Spicy Chicken Wings", "BBQ Pro", 289, 34, 30,
        "https://bakerbynature.com/wp-content/uploads/2015/02/Sweet-and-Spicy-Sriracha-Chicken-Wings-0-6.jpg",
        "Dinner"
    ),
    Recipe(
        "7", "Sushi Platter", "Hana", 412, 77, 35,
        "https://cdn.foodstorm.com/e5184b75632349358c9031c2ef988e6b/images/0ac13014da6f4fd1adee7eb7fc2f70eb_1080w.jpg",
        "Lunch"
    ),
    Recipe(
        "8", "Avocado Toast", "Liam", 98, 9, 6,
        "https://gratefulgrazer.com/wp-content/uploads/2025/01/avocado-toast-square.jpg",
        "Breakfast"
    ),
    Recipe(
        "9", "Blueberry Pancakes", "Mila", 265, 19, 18,
        "https://www.thespruceeats.com/thmb/9IRYWPZ9ydGFZFtxthmtAR150VM=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/blueberry-ricotta-pancakes-541279742-5ab2d59d04d1cf0036f81d80.jpg",
        "Breakfast"
    ),
    Recipe(
        "10", "French Toast Stacks", "Andre", 233, 14, 17,
        "https://altonbrown.com/wp-content/uploads/2020/08/French-Toast-Stack_Lynne_resized.jpg",
        "Breakfast"
    ),
    Recipe(
        "11", "Yogurt Parfait", "Casey", 120, 6, 5,
        "https://spicecravings.com/wp-content/uploads/2023/09/Greek-Yogurt-Parfait-Featured.jpg",
        "Breakfast"
    ),
    Recipe(
        "12", "Chocolate Lava Cake", "Patissier Eloise", 501, 120, 22,
        "https://www.melskitchencafe.com/wp-content/uploads/2023/01/updated-lava-cakes7-500x500.jpg",
        "Desserts"
    ),
    Recipe(
        "13", "Tiramisu Cups", "Giulia", 432, 90, 30,
        "https://bakerstable.net/wp-content/uploads/2024/08/tiramisu-cups-4-scaled.jpg",
        "Desserts"
    ),
    Recipe(
        "14", "Strawberry Cheesecake", "Baker Zoe", 389, 64, 40,
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSZLcX5NeFrqrgb6Y7XRZ4loW2X7xhlRn56Tw&s",
        "Desserts"
    ),
    Recipe(
        "15", "Macarons Assortment", "Maison Pierre", 275, 38, 50,
        "https://www.jordanwinery.com/wp-content/uploads/2020/04/French-Macaron-Cookie-Recipe-WebHero-6435.jpg",
        "Desserts"
    ),
    Recipe(
        "16", "Matcha Ice Cream", "Kiko", 198, 22, 10,
        "https://www.allrecipes.com/thmb/totJUia-TjrmF6VnYGHOM5hVjqQ=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/241759-matcha-green-tea-ice-cream-VAT-003-4x3-01closeup-692d327cc2174abb84b440568f61e29a.jpg",
        "Desserts"
    )
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: SavedRecipesViewModel = viewModel(),
    onOpenRecipe: (Recipe) -> Unit = {},
    onTabChange: (String) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("All") }
    val savedIds  by viewModel.savedIds.collectAsState()

    val items = remember(query, selected) {
        demoRecipes.filter {
            (selected == "All" || it.category == selected) &&
                    it.title.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
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
            onSelect = {
                selected = it
                onTabChange(it)
            }
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
            items(items) { r -> RecipeCard(
                r = r,
                isSaved = r.id in savedIds,
                onSaveClick = {viewModel.toggleSave(r.id)},
                onClick = { onOpenRecipe(r) }) }
        }

        Spacer(Modifier.height(56.dp))
    }
}

// ---------- UI Parts ----------
@Composable
fun TopHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "The Cookout",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = CookoutOrange
            )
            Text("Share your recipes!", fontSize = 12.sp, color = LightGreyText)
        }
        Text("Share your recipes", fontSize = 12.sp, color = LightGreyText)
    }
}

@Composable
fun SearchField(value: String, onChange: (String) -> Unit) {
    androidx.compose.material3.OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search recipes…") },
        singleLine = true,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun CategoryChipsScrollable(labels: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(labels.size) { idx ->
            val label = labels[idx]
            val active = label == selected
            androidx.compose.material3.AssistChip(
                onClick = { onSelect(label) },
                label = { Text(label) },
                shape = RoundedCornerShape(20.dp),
                colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                    containerColor = if (active) CookoutOrange.copy(0.15f) else Color(0xFFF3F3F3),
                    labelColor = if (active) CookoutOrange else Color.Black
                )
            )
        }
    }
}

@Composable
fun RecipeCard(r: Recipe, isSaved: Boolean, onSaveClick: () -> Unit, onClick: () -> Unit) {
    var isAnimating by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = 0.4f,
            stiffness = 400f
        ),
        finishedListener = {isAnimating = false},
        label = "heartScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box{
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(r.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = r.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0xFFF0F0F0))
                )

                IconButton(
                    onClick = {
                        onSaveClick()
                        isAnimating = true
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ){
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isSaved) "Unsave" else "Save",
                        tint = if (isSaved) CookoutOrange else Color.White,
                        modifier = Modifier
                            .scale(scale)
                            .size(24.dp)
                    )
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(
                    r.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE9E9E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👩‍🍳", fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        r.chef,
                        fontSize = 12.sp,
                        color = LightGreyText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = CookoutOrange
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${r.likes}", fontSize = 12.sp, color = LightGreyText)
                        Spacer(Modifier.width(12.dp))
                        androidx.compose.material3.Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFB3B3B3)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${r.comments}", fontSize = 12.sp, color = LightGreyText)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFB3B3B3)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${r.mins}m", fontSize = 12.sp, color = LightGreyText)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    TheCookoutTheme(darkTheme = false, dynamicColor = false) {
        HomeScreen()
    }
}
