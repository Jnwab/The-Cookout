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

private val categories = listOf("All", "Breakfast", "Lunch", "Dinner", "Desserts")

// 16 demo items with DIRECT image URLs so Coil can load them.
private val demoRecipes = listOf(
    Recipe("1","Classic Beef Burger","Sarah Kitchen",124,1,15,
        "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=1200&q=80&auto=format&fit=crop","Lunch"),
    // Carbonara
    Recipe("2","Creamy Pasta Carbonara","Chef Marco",256,42,20,
        "https://images.unsplash.com/photo-1521389508051-d7ffb5dc8bbf?w=1200&q=80&auto=format&fit=crop","Dinner"),
    Recipe("3","Fresh Garden Salad","Emma Green",189,23,8,
        "https://images.unsplash.com/photo-1551218808-94e220e084d2?w=1200&q=80&auto=format&fit=crop","Lunch"),
    // Margherita
    Recipe("4","Margherita Pizza","Tony's Kitchen",342,66,25,
        "https://images.unsplash.com/photo-1548365328-9f547fb09550?w=1200&q=80&auto=format&fit=crop","Dinner"),
    Recipe("5","Garlic Butter Steak Bites","Chef Nina",211,18,14,
        "https://images.unsplash.com/photo-1551183053-bf91a1d81141?w=1200&q=80&auto=format&fit=crop","Dinner"),
    Recipe("6","Spicy Chicken Wings","BBQ Pro",289,34,30,
        "https://images.unsplash.com/photo-1544025162-d76694265947?w=1200&q=80&auto=format&fit=crop","Dinner"),
    Recipe("7","Sushi Platter","Hana",412,77,35,
        "https://images.unsplash.com/photo-1553621042-f6e147245754?w=1200&q=80&auto=format&fit=crop","Lunch"),
    Recipe("8","Avocado Toast","Liam",98,9,6,
        "https://images.unsplash.com/photo-1551183053-8f9bdb7b0f91?w=1200&q=80&auto=format&fit=crop","Breakfast"),
    Recipe("9","Blueberry Pancakes","Mila",265,19,18,
        "https://images.unsplash.com/photo-1587731544311-5f87fbb77f89?w=1200&q=80&auto=format&fit=crop","Breakfast"),
    Recipe("10","French Toast Stacks","Andre",233,14,17,
        "https://images.unsplash.com/photo-1533089860892-a7c6f0a88666?w=1200&q=80&auto=format&fit=crop","Breakfast"),
    Recipe("11","Yogurt Parfait","Casey",120,6,5,
        "https://images.unsplash.com/photo-1525351484163-7529414344d8?w=1200&q=80&auto=format&fit=crop","Breakfast"),
    // Desserts
    Recipe("12","Chocolate Lava Cake","Patissier Eloise",501,120,22,
        "https://images.unsplash.com/photo-1606313564200-e75d5e30476f?w=1200&q=80&auto=format&fit=crop","Desserts"),
    Recipe("13","Tiramisu Cups","Giulia",432,90,30,
        "https://images.unsplash.com/photo-1606756790138-261c0b9b1800?w=1200&q=80&auto=format&fit=crop","Desserts"),
    Recipe("14","Strawberry Cheesecake","Baker Zoe",389,64,40,
        "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?w=1200&q=80&auto=format&fit=crop","Desserts"),
    Recipe("15","Macarons Assortment","Maison Pierre",275,38,50,
        "https://images.unsplash.com/photo-1505252585461-04db1eb84625?w=1200&q=80&auto=format&fit=crop","Desserts"),
    Recipe("16","Matcha Ice Cream","Kiko",198,22,10,
        "https://images.unsplash.com/photo-1517256064527-09c73fc73e38?w=1200&q=80&auto=format&fit=crop","Desserts"),
)

@Composable
fun HomeScreen(
    onAddRecipe: () -> Unit = {},
    onOpenRecipe: (Recipe) -> Unit = {},
    onTabChange: (String) -> Unit = {},
    onCreateRecipeClick: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("All") }

    val items = remember(query, selected) {
        demoRecipes.filter {
            (selected == "All" || it.category == selected) &&
                    it.title.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = { BottomNavBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecipe,
                containerColor = CookoutOrange,
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Outlined.Add, contentDescription = "Add") }
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
                items(items) { r -> RecipeCard(r) { onOpenRecipe(r) } }
            }

            Spacer(Modifier.height(56.dp))
        }
    }
}

// ---------- UI Parts ----------
@Composable
private fun TopHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("The Cookout", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = CookoutOrange)
            Text("Share your recipes!", fontSize = 12.sp, color = LightGreyText)
        }
        Text("Share your recipes", fontSize = 12.sp, color = LightGreyText)
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search recipes…") },
        singleLine = true,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun CategoryChipsScrollable(labels: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(labels.size) { idx ->
            val label = labels[idx]
            val active = label == selected
            AssistChip(
                onClick = { onSelect(label) },
                label = { Text(label) },
                shape = RoundedCornerShape(20.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (active) CookoutOrange.copy(0.15f) else Color(0xFFF3F3F3),
                    labelColor = if (active) CookoutOrange else Color.Black
                )
            )
        }
    }
}

@Composable
private fun RecipeCard(r: Recipe, onClick: () -> Unit) {
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
            // Coil image with crossfade & placeholder color
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
            Column(Modifier.padding(12.dp)) {
                Text(r.title, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE9E9E9)),
                        contentAlignment = Alignment.Center
                    ) { Text("👩‍🍳", fontSize = 12.sp) }
                    Spacer(Modifier.width(8.dp))
                    Text(r.chef, fontSize = 12.sp, color = LightGreyText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.FavoriteBorder, null, modifier = Modifier.size(16.dp), tint = CookoutOrange)
                        Spacer(Modifier.width(4.dp)); Text("${r.likes}", fontSize = 12.sp, color = LightGreyText)
                        Spacer(Modifier.width(12.dp))
                        Icon(Icons.Outlined.ChatBubbleOutline, null, modifier = Modifier.size(16.dp), tint = Color(0xFFB3B3B3))
                        Spacer(Modifier.width(4.dp)); Text("${r.comments}", fontSize = 12.sp, color = LightGreyText)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(16.dp), tint = Color(0xFFB3B3B3))
                        Spacer(Modifier.width(4.dp)); Text("${r.mins}m", fontSize = 12.sp, color = LightGreyText)
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavBar() {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(selected = true,  onClick = {}, icon = { Text("🏠") }, label = { Text("Home") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Text("💗") }, label = { Text("Saved") })
        Spacer(Modifier.weight(1f, fill = true)) // space for center FAB
        NavigationBarItem(selected = false, onClick = {}, icon = { Text("🔔") }, label = { Text("Alerts") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Text("👤") }, label = { Text("Profile") })
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    TheCookoutTheme(darkTheme = false, dynamicColor = false) {
        HomeScreen(
            onCreateRecipeClick = {}
        )
    }
}
