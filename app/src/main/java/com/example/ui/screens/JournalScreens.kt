package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import java.util.Calendar
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.BourbonViewModel
import com.example.viewmodel.BlindPourGuess
import kotlinx.coroutines.launch

// Screen routes constant identifiers
object Routes {
    const val HOME = "home"
    const val REVIEW_WELCOME = "review_welcome"
    const val RE_REVIEW_ROUTER = "re_review_router"
    const val BOTTLE_INFO = "bottle_info"
    const val OWNED = "owned"
    const val POUR = "pour"
    const val NOSE = "nose"
    const val PALATE = "palate"
    const val FINISH = "finish"
    const val PROFILE_WHEEL = "profile_wheel"
    const val OVERALL = "overall"
    const val COLLECTION_LIST = "collection_list"
    const val SPECIFIC_BOTTLE = "specific_bottle"
    const val SPECIFIC_SUB_BOTTLE = "specific_sub_bottle"
    const val READ_ONLY_REVIEW = "read_only_review"
    const val STATS = "stats"
    const val BLIND_SETUP = "blind_setup"
    const val BLIND_POUR_GUESS = "blind_pour_guess"
    const val BLIND_RANKS = "blind_ranks"
    const val BLIND_REVEAL = "blind_reveal"
    const val COMPLETED_BLINDS = "completed_blinds"
}

/**
 * Reusable and beautiful calendar-based DatePicker field.
 */
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year: Int
    val month: Int
    val day: Int

    val parts = value.split("-")
    if (parts.size == 3) {
        year = parts[0].toIntOrNull() ?: calendar.get(Calendar.YEAR)
        month = (parts[1].toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
        day = parts[2].toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)
    } else {
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val formattedMonth = String.format("%02d", selectedMonth + 1)
                val formattedDay = String.format("%02d", selectedDayOfMonth)
                onValueChange("$selectedYear-$formattedMonth-$formattedDay")
            },
            year,
            month,
            day
        )
    }

    LaunchedEffect(value) {
        val currentParts = value.split("-")
        if (currentParts.size == 3) {
            val y = currentParts[0].toIntOrNull() ?: calendar.get(Calendar.YEAR)
            val m = (currentParts[1].toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
            val d = currentParts[2].toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)
            datePickerDialog.updateDate(y, m, d)
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date",
                        tint = PrimaryAmber
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = TextWarmWhite,
                unfocusedTextColor = TextWarmWhite,
                focusedContainerColor = SlateMuted,
                unfocusedContainerColor = DarkCharcoal,
                focusedIndicatorColor = PrimaryAmber,
                unfocusedIndicatorColor = SlateMuted,
                focusedLabelColor = PrimaryAmber,
                unfocusedLabelColor = TextSoftGray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { datePickerDialog.show() }
        )
    }
}

/**
 * Universal Screen Scaffold providing standard Header, symmetric bottom arrows, and Elegant Dark theme containers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreenScaffold(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    onForwardClick: (() -> Unit)? = null,
    forwardText: String = "Next",
    isForwardEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = title,
                            color = TextWarmWhite,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle.uppercase(),
                                color = PrimaryAmber,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PrimaryAmber
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBlack,
                    titleContentColor = TextWarmWhite
                ),
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .background(DarkCharcoal, CircleShape)
                            .border(1.dp, SlateMuted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🥃", fontSize = 16.sp)
                    }
                }
            )
        },
        bottomBar = {
            if (onBackClick != null || onForwardClick != null) {
                Surface(
                    color = ObsidianBlack,
                    border = BorderStroke(1.dp, SlateMuted),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Symmetric Back Arrow
                        if (onBackClick != null) {
                            OutlinedButton(
                                onClick = onBackClick,
                                border = BorderStroke(1.dp, SlateMuted),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = TextSoftGray
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Go Back",
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text("Back", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Symmetric Next/Forward Arrow
                        if (onForwardClick != null) {
                            Button(
                                onClick = onForwardClick,
                                enabled = isForwardEnabled,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryAmber,
                                    contentColor = ObsidianBlack,
                                    disabledContainerColor = SlateMuted,
                                    disabledContentColor = TextSoftGray.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(forwardText, fontWeight = FontWeight.Bold)
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Forward Action",
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        containerColor = ObsidianBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content()
        }
    }
}

/**
 * Main Home App Dashboard
 */
@Composable
fun HomeScreen(viewModel: BourbonViewModel, navController: NavController) {
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianBlack)
                    .padding(top = 28.dp, bottom = 12.dp, start = 20.dp, end = 20.dp)
            ) {
                Text(
                    text = "BOURBON JOURNAL",
                    color = TextWarmWhite,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "AUTHENTIC OFFLINE PERSONAL COMPANION",
                    color = PrimaryAmber,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        },
        containerColor = ObsidianBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Elegant Dashboard Welcome Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateMuted, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("🥃", fontSize = 42.sp)
                    Column {
                        Text(
                            text = "Welcome Back",
                            color = TextWarmWhite,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Review pours, monitor aging stats, and play blind tasting workflows completely offline on your device.",
                            color = TextSoftGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Text(
                text = "JOURNAL OPERATIONS",
                color = TextSoftGray,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Collection Button Card
            DashboardOptionCard(
                title = "Collection",
                description = "Browse your bottle inventory, sub-bottle instances, and detailed average ratings.",
                icon = "📚",
                onClick = { navController.navigate(Routes.COLLECTION_LIST) }
            )

            // Review Button Card
            DashboardOptionCard(
                title = "Review",
                description = "Log physical pours, create profile flavor wheels, and document notes on the fly.",
                icon = "🖊️",
                onClick = {
                    viewModel.clearReviewFlow()
                    // Default unowned flows can redirect
                    navController.navigate(Routes.REVIEW_WELCOME)
                }
            )

            // Stats Card
            DashboardOptionCard(
                title = "Stats",
                description = "Explore mathematical aggregations (favorites, averages) pulled exclusively from your bottle base.",
                icon = "📊",
                onClick = { navController.navigate(Routes.STATS) }
            )

            // Blind
            DashboardOptionCard(
                title = "Blind Tasting",
                description = "Configure and execute curated double-blind tasting, scores comparison, and ranking games.",
                icon = "🔒",
                onClick = { navController.navigate(Routes.BLIND_SETUP) }
            )

            // Completed Blinds
            DashboardOptionCard(
                title = "Completed Blinds",
                description = "Revisit history of blind sessions, past scores, and ranking results.",
                icon = "🏁",
                onClick = { navController.navigate(Routes.COMPLETED_BLINDS) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DashboardOptionCard(
    title: String,
    description: String,
    icon: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, SlateMuted.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SlateMuted, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        color = TextWarmWhite,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("•", color = PrimaryAmber)
                }
                Text(
                    text = description,
                    color = TextSoftGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = PrimaryAmber
            )
        }
    }
}

/**
 * Page 5: Review Welcome & Choice
 */
@Composable
fun ReviewWelcomeScreen(viewModel: BourbonViewModel, navController: NavController) {
    var nameQuery by remember { mutableStateOf("") }
    var nickQuery by remember { mutableStateOf("") }

    val nameSuggestions = viewModel.getBottleNameSuggestions(nameQuery)
    val nickSuggestions = viewModel.getNicknameSuggestions(nickQuery)

    // Button is bolden (enabled) with saved options if user selects from suggests
    val isReReviewEnabled = viewModel.selectedAutoCompleteBottle != null

    JournalScreenScaffold(
        title = "Tasting Review",
        subtitle = "Tasting Session Setup",
        onBackClick = { navController.popBackStack() }
    ) {
        Text(
            text = "Initiate a review session. Search your collection by Bottle Name or Nickname, or press NEW to log a fresh bottle details.",
            color = TextSoftGray,
            style = MaterialTheme.typography.bodyMedium
        )

        PremiumFormCard(title = "Select From Inventory") {
            // Bottle Name autocomplete field
            OutlinedTextField(
                value = nameQuery,
                onValueChange = {
                    nameQuery = it
                    viewModel.reviewBottleName = it
                    // Reset auto-selected state unless matched perfectly
                    viewModel.selectedAutoCompleteBottle = null
                },
                label = { Text("Bottle Name") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedLabelColor = PrimaryAmber,
                    unfocusedLabelColor = TextSoftGray,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Name Matches
            if (nameSuggestions.isNotEmpty()) {
                Surface(
                    color = SlateMuted,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        nameSuggestions.forEach { bottle ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectExistingReviewBottle(bottle)
                                        nameQuery = bottle.name
                                        nickQuery = bottle.nickname
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🎯", fontSize = 14.sp)
                                Text(bottle.name, color = TextWarmWhite, fontWeight = FontWeight.Bold)
                                if (bottle.nickname.isNotEmpty()) {
                                    Text("(${bottle.nickname})", color = TextSoftGray, fontStyle = FontStyle.Italic)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nickname autocomplete field
            OutlinedTextField(
                value = nickQuery,
                onValueChange = {
                    nickQuery = it
                    viewModel.reviewNickname = it
                    viewModel.selectedAutoCompleteBottle = null
                },
                label = { Text("Nickname") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedLabelColor = PrimaryAmber,
                    unfocusedLabelColor = TextSoftGray,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Nickname Matches
            if (nickSuggestions.isNotEmpty()) {
                Surface(
                    color = SlateMuted,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        nickSuggestions.forEach { bottle ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectExistingReviewBottle(bottle)
                                        nameQuery = bottle.name
                                        nickQuery = bottle.nickname
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🏷️", fontSize = 14.sp)
                                Text(bottle.nickname, color = TextWarmWhite, fontWeight = FontWeight.Bold)
                                Text("- ${bottle.name}", color = TextSoftGray, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // Two primary routes: Re-Review or New
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    viewModel.isNewBottleFlow = false
                    navController.navigate(Routes.RE_REVIEW_ROUTER)
                },
                enabled = isReReviewEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryAmber,
                    contentColor = ObsidianBlack,
                    disabledContainerColor = SlateMuted.copy(alpha = 0.5f),
                    disabledContentColor = TextSoftGray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("RE-REVIEW BOTTLE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Button(
                onClick = {
                    // Start new flow: clear autocomplete locks, set isNew = true
                    viewModel.isNewBottleFlow = true
                    viewModel.reReviewType = ""
                    // Transfer the typed queries to preset defaults
                    viewModel.reviewBottleName = nameQuery
                    viewModel.reviewNickname = nickQuery
                    navController.navigate(Routes.BOTTLE_INFO)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkCharcoal,
                    contentColor = TextWarmWhite
                ),
                border = BorderStroke(1.dp, SlateMuted),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("NEW BOTTLE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

/**
 * Page 6: Re-Review routers
 */
@Composable
fun ReReviewRouterScreen(viewModel: BourbonViewModel, navController: NavController) {
    JournalScreenScaffold(
        title = "Re-Review Options",
        subtitle = "Select tasting type",
        onBackClick = { navController.popBackStack() }
    ) {
        Text(
            text = "You are reviewing: ${viewModel.selectedAutoCompleteBottle?.name ?: "Selected Bottle"}",
            color = PrimaryAmber,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Same Bottle Button Row
        MenuOptionButton(
            title = "Same Bottle",
            desc = "Add a brand new review record to the current bottle instance already in your cabinet.",
            icon = "🔄",
            onClick = {
                viewModel.reReviewType = "Same Bottle"
                viewModel.pourPriceReadOnly = true
                viewModel.pourPrice = "0"
                navController.navigate(Routes.POUR)
            }
        )

        // Re-Purchase Button Card
        MenuOptionButton(
            title = "Re-Purchase",
            desc = "Register a new bottle copy. Log the price, date bought, size, and location specs.",
            icon = "🛍️",
            onClick = {
                viewModel.reReviewType = "Re-Purchase"
                viewModel.pourPriceReadOnly = false
                navController.navigate(Routes.OWNED)
            }
        )

        // Single Pour
        MenuOptionButton(
            title = "Single Pour",
            desc = "Review outside a cabinet instance (e.g. at a bar / friend's). Review is saved under unowned Catch-All sequence.",
            icon = "🥃",
            onClick = {
                viewModel.reReviewType = "Single Pour"
                viewModel.pourPriceReadOnly = false
                viewModel.pourPrice = "0"
                navController.navigate(Routes.POUR)
            }
        )
    }
}

@Composable
fun MenuOptionButton(
    title: String,
    desc: String,
    icon: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, SlateMuted, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SlateMuted, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextWarmWhite,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = desc,
                    color = TextSoftGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = PrimaryAmber
            )
        }
    }
}

/**
 * Page 7: Bottle Info inputs
 */
@Composable
fun BottleInfoScreen(viewModel: BourbonViewModel, navController: NavController) {
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    val categories = listOf(
        "American Whisky", "Bourbon", "Irish", "Japanese",
        "Light Whiskey", "Rye", "Scotch", "Wheat"
    )

    JournalScreenScaffold(
        title = "Bottle Specs",
        subtitle = "Bottle Info",
        onBackClick = { navController.popBackStack() }
    ) {
        PremiumFormCard(title = "Vital Details") {
            OutlinedTextField(
                value = viewModel.infoDistillery,
                onValueChange = { viewModel.infoDistillery = it },
                label = { Text("Distillery") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.infoAge,
                onValueChange = { viewModel.infoAge = it },
                label = { Text("Age") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.infoProof,
                onValueChange = { viewModel.infoProof = it },
                label = { Text("Proof") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Category field drop list
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.infoCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextWarmWhite,
                        unfocusedTextColor = TextWarmWhite,
                        focusedContainerColor = SlateMuted,
                        unfocusedContainerColor = DarkCharcoal,
                        focusedIndicatorColor = PrimaryAmber,
                        unfocusedIndicatorColor = SlateMuted
                    ),
                    trailingIcon = {
                        IconButton(onClick = { isCategoryDropdownExpanded = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = PrimaryAmber)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = isCategoryDropdownExpanded,
                    onDismissRequest = { isCategoryDropdownExpanded = false },
                    modifier = Modifier
                        .background(DarkCharcoal)
                        .border(1.dp, SlateMuted, RoundedCornerShape(8.dp))
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, color = TextWarmWhite) },
                            onClick = {
                                viewModel.infoCategory = cat
                                isCategoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.infoFinish,
                onValueChange = { viewModel.infoFinish = it },
                label = { Text("Wood Finish") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.infoOther,
                onValueChange = { viewModel.infoOther = it },
                label = { Text("Other Notes") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    viewModel.isNewBottleOwned = true
                    viewModel.pourPriceReadOnly = false
                    navController.navigate(Routes.OWNED)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkCharcoal, contentColor = TextWarmWhite),
                border = BorderStroke(1.dp, SlateMuted),
                modifier = Modifier.weight(1f)
            ) {
                Text("Owned Bottle", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    viewModel.isNewBottleOwned = false
                    viewModel.pourPriceReadOnly = false
                    viewModel.pourPrice = ""
                    navController.navigate(Routes.POUR)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber, contentColor = ObsidianBlack),
                modifier = Modifier.weight(1f)
            ) {
                Text("Pour Directly", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Page 8: Owned Spec Form
 */
@Composable
fun OwnedScreen(viewModel: BourbonViewModel, navController: NavController) {
    JournalScreenScaffold(
        title = "Purchase Details",
        subtitle = "Owned Info",
        onBackClick = { navController.popBackStack() },
        onForwardClick = {
            viewModel.pourPriceReadOnly = true
            viewModel.pourPrice = "0"
            navController.navigate(Routes.POUR)
        }
    ) {
        PremiumFormCard(title = "Physical Inventory") {
            OutlinedTextField(
                value = viewModel.ownedSizeMl,
                onValueChange = { viewModel.ownedSizeMl = it },
                label = { Text("Size (ml)") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.ownedPrice,
                onValueChange = { viewModel.ownedPrice = it },
                label = { Text("Price Paid ($)") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            DatePickerField(
                value = viewModel.ownedBoughtWhen,
                onValueChange = { viewModel.ownedBoughtWhen = it },
                label = "When (Date)",
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.ownedBoughtWhere,
                onValueChange = { viewModel.ownedBoughtWhere = it },
                label = { Text("Where (Distributor/Store)") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.ownedOther,
                onValueChange = { viewModel.ownedOther = it },
                label = { Text("Cabinet/Storage Notes") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Page 9: Pour details
 */
@Composable
fun PourScreen(viewModel: BourbonViewModel, navController: NavController) {
    var hasOzFocus by remember { mutableStateOf(false) }
    var hasMlFocus by remember { mutableStateOf(false) }

    JournalScreenScaffold(
        title = "Tasting Pour",
        subtitle = "Pour Specifications",
        onBackClick = { navController.popBackStack() },
        onForwardClick = { navController.navigate(Routes.NOSE) }
    ) {
        PremiumFormCard(title = "Tasting Setup") {
            DatePickerField(
                value = viewModel.pourWhen,
                onValueChange = { viewModel.pourWhen = it },
                label = "Pour Date",
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.pourWhere,
                onValueChange = { viewModel.pourWhere = it },
                label = { Text("Pour Location (Bar/Rest)") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.pourPrice,
                onValueChange = { if (!viewModel.pourPriceReadOnly) viewModel.pourPrice = it },
                readOnly = viewModel.pourPriceReadOnly,
                label = { Text("Pour Price") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = if (viewModel.pourPriceReadOnly) TextSoftGray else TextWarmWhite,
                    unfocusedTextColor = if (viewModel.pourPriceReadOnly) TextSoftGray else TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = if (viewModel.pourPriceReadOnly) SlateMuted else PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Drunk Score",
                color = TextWarmWhite,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            CircularRatingRow(
                selectedScore = viewModel.pourDrunkScore,
                onScoreSelected = { viewModel.pourDrunkScore = it }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Liquid Measurement Details",
                color = TextWarmWhite,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            // Dynamic Unit Conversion logic
            // Built with strict focus validation to avoid programmatic triggers loop
            OutlinedTextField(
                value = viewModel.pourAmountOz,
                onValueChange = { newValue ->
                    viewModel.pourAmountOz = newValue
                    if (hasOzFocus) {
                        val ozVal = newValue.toDoubleOrNull()
                        if (ozVal != null) {
                            val converted = ozVal * 29.5735
                            viewModel.pourAmountMl = String.format("%.1f", converted)
                        } else if (newValue.isEmpty()) {
                            viewModel.pourAmountMl = ""
                        }
                    }
                },
                label = { Text("Amount in oz") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { hasOzFocus = it.isFocused }
            )

            OutlinedTextField(
                value = viewModel.pourAmountMl,
                onValueChange = { newValue ->
                    viewModel.pourAmountMl = newValue
                    if (hasMlFocus) {
                        val mlVal = newValue.toDoubleOrNull()
                        if (mlVal != null) {
                            val converted = mlVal / 29.5735
                            viewModel.pourAmountOz = String.format("%.2f", converted)
                        } else if (newValue.isEmpty()) {
                            viewModel.pourAmountOz = ""
                        }
                    }
                },
                label = { Text("Amount in ml") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { hasMlFocus = it.isFocused }
            )

            OutlinedTextField(
                value = viewModel.pourGlass,
                onValueChange = { viewModel.pourGlass = it },
                label = { Text("Glassware Type") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.pourIce = !viewModel.pourIce }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = viewModel.pourIce,
                    onCheckedChange = { viewModel.pourIce = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryAmber,
                        uncheckedColor = TextSoftGray
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("On Ice", color = TextWarmWhite, fontWeight = FontWeight.Medium)
            }

            OutlinedTextField(
                value = viewModel.pourOther,
                onValueChange = { viewModel.pourOther = it },
                label = { Text("Pour Notes") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Pages 10-12, 14: Text assessment + Rating screens (Nose, Palate, Finish, Overall)
 */
@Composable
fun NotesRatingScreen(
    title: String,
    notes: String,
    onNotesChange: (String) -> Unit,
    score: Int,
    onScoreChange: (Int) -> Unit,
    backRoute: String,
    forwardRoute: String,
    navController: NavController,
    stepTitle: String,
    isSaveButton: Boolean = false,
    onSaveClick: (() -> Unit)? = null
) {
    JournalScreenScaffold(
        title = title,
        subtitle = stepTitle,
        onBackClick = { navController.popBackStack() },
        onForwardClick = {
            if (isSaveButton && onSaveClick != null) {
                onSaveClick()
            } else {
                navController.navigate(forwardRoute)
            }
        },
        forwardText = if (isSaveButton) "Save" else "Next"
    ) {
        PremiumFormCard(title = "$title Assessment") {
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Tasting Impressions") },
                placeholder = { Text("Write personal observations here...") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Assign score rating",
                color = TextWarmWhite,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )

            CircularRatingRow(
                selectedScore = score,
                onScoreSelected = onScoreChange
            )
        }
    }
}

/**
 * Page 13: Radar profile wheel
 */
@Composable
fun ProfileWheelScreen(viewModel: BourbonViewModel, navController: NavController) {
    JournalScreenScaffold(
        title = "Flavor Matrix",
        subtitle = "Profile Wheel",
        onBackClick = { navController.popBackStack() },
        onForwardClick = { navController.navigate(Routes.OVERALL) }
    ) {
        Text(
            text = "Fine-tune the individual tasting notes dial below. Drag the hollow outer circles inward down to the absolute center point to represent a 0 (note absent) or outward to 7.",
            color = TextSoftGray,
            style = MaterialTheme.typography.bodySmall
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateMuted, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
            shape = RoundedCornerShape(16.dp)
        ) {
            InteractiveFlavorWheel(
                scores = viewModel.flavorScores.toMap(),
                onScoresChanged = { updated ->
                    updated.forEach { (key, value) ->
                        viewModel.flavorScores[key] = value
                    }
                }
            )
        }
    }
}

/**
 * Page 15: Collection inventory
 */
@Composable
fun CollectionListScreen(viewModel: BourbonViewModel, navController: NavController) {
    val bottles by viewModel.allBottles.collectAsState()

    JournalScreenScaffold(
        title = "Cabinet Collection",
        subtitle = "Sub-Bottle Inventory",
        onBackClick = { navController.popBackStack() }
    ) {
        if (bottles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🥃", fontSize = 48.sp)
                    Text("Cabinet Empty", color = TextWarmWhite, fontWeight = FontWeight.Bold)
                    Text("Go log a tasting review to add a bottle to your catalog.", color = TextSoftGray, textAlign = TextAlign.Center)
                }
            }
        } else {
            // Sort in alphabetical order by bottle names as specified on Page 15
            val sortedBottles = bottles.sortedBy { it.name.lowercase() }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sortedBottles.forEach { bottle ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.activeBottleId = bottle.id
                                navController.navigate("${Routes.SPECIFIC_BOTTLE}/${bottle.id}")
                            }
                            .border(1.dp, SlateMuted, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SlateMuted, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🥃", fontSize = 18.sp)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bottle.name,
                                    color = TextWarmWhite,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (bottle.nickname.isNotEmpty()) {
                                    Text(
                                        text = "\"${bottle.nickname}\"",
                                        color = PrimaryAmber,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                                Text(
                                    text = "${bottle.distillery} • Proof: ${bottle.proof} • ${bottle.category}",
                                    color = TextSoftGray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = PrimaryAmber)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Page 16: Specific Bottle screen containing Averages & list of SubBottles
 */
@Composable
fun SpecificBottleScreen(bottleId: Long, viewModel: BourbonViewModel, navController: NavController) {
    val bottles by viewModel.allBottles.collectAsState()
    val allSubs by viewModel.allSubBottles.collectAsState()
    val allRev by viewModel.allReviews.collectAsState()

    val bottle = bottles.find { it.id == bottleId }
    val subs = allSubs.filter { it.bottleId == bottleId }

    // Map sub-bottle IDs to list
    val subIds = subs.map { it.subBottleId }
    val reviews = allRev.filter { r -> subIds.contains(r.subBottleId) }

    // Aggregated reviews averages
    val avgNose = if (reviews.isNotEmpty()) reviews.map { it.noseScore }.average() else 0.0
    val avgPalate = if (reviews.isNotEmpty()) reviews.map { it.palateScore }.average() else 0.0
    val avgFinish = if (reviews.isNotEmpty()) reviews.map { it.finishScore }.average() else 0.0
    val avgOverall = if (reviews.isNotEmpty()) reviews.map { it.overallScore }.average() else 0.0

    JournalScreenScaffold(
        title = bottle?.name ?: "Bottle Details",
        subtitle = "Averages & Executions",
        onBackClick = { navController.popBackStack() }
    ) {
        if (bottle != null) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header details
                PremiumFormCard(title = "Specs Information") {
                    if (bottle.nickname.isNotEmpty()) {
                        Text(
                            text = "\"${bottle.nickname}\"",
                            color = PrimaryAmber,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text("Distillery: ${bottle.distillery}", color = TextWarmWhite)
                    Text("Age: ${bottle.age}", color = TextWarmWhite)
                    Text("Proof: ${bottle.proof}", color = TextWarmWhite)
                    Text("Category: ${bottle.category}", color = TextWarmWhite)
                    if (bottle.finish.isNotEmpty()) {
                        Text("Finish: ${bottle.finish}", color = TextWarmWhite)
                    }
                    if (bottle.other.isNotEmpty()) {
                        Text("Other: ${bottle.other}", color = TextSoftGray, style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Page 16 aggregated metrics display rows
                PremiumFormCard(title = "Average Aggregated Ratings") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AverageItem("Nose", avgNose)
                        AverageItem("Palate", avgPalate)
                        AverageItem("Finish", avgFinish)
                        AverageItem("Overall", avgOverall)
                    }
                }

                Text(
                    text = "SUB-BOTTLES CABINET ARCHIVE",
                    color = TextSoftGray,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                if (subs.isEmpty()) {
                    Text("No sub-bottles created.", color = TextSoftGray)
                } else {
                    subs.forEach { sub ->
                        val label = if (sub.subBottleNumber == 0) "Single Pours & Unowned Reviews" else "Sub-Bottle #${sub.subBottleNumber}"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.activeSubBottleId = sub.subBottleId
                                    navController.navigate("${Routes.SPECIFIC_SUB_BOTTLE}/${sub.subBottleId}/${bottleId}")
                                }
                                .border(1.dp, SlateMuted, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("🛍️", fontSize = 24.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(label, color = TextWarmWhite, fontWeight = FontWeight.Bold)
                                    if (sub.subBottleNumber > 0) {
                                        Text(
                                            text = "Size: ${sub.sizeMl}ml • Price: $${sub.price} • Bought at ${sub.boughtWhere}",
                                            color = TextSoftGray,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    } else {
                                        Text(
                                            text = "Captures non-cabinet pours (bars / friends / quick-sips)",
                                            color = TextSoftGray,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = PrimaryAmber)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AverageItem(label: String, avg: Double) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(label.uppercase(), color = TextSoftGray, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(SlateMuted, CircleShape)
                .border(1.dp, PrimaryAmber, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (avg == 0.0) "-" else String.format("%.1f", avg),
                color = PrimaryAmber,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }
    }
}

/**
 * Page 17: Specific Sub-Bottle reviews list
 */
@Composable
fun SpecificSubBottleScreen(
    subBottleId: Long,
    bottleId: Long,
    viewModel: BourbonViewModel,
    navController: NavController
) {
    val bottles by viewModel.allBottles.collectAsState()
    val allSubs by viewModel.allSubBottles.collectAsState()
    val allRev by viewModel.allReviews.collectAsState()

    val bottle = bottles.find { it.id == bottleId }
    val sub = allSubs.find { it.subBottleId == subBottleId }
    val reviews = allRev.filter { it.subBottleId == subBottleId }.sortedByDescending { it.timestamp }

    // Aggregates for this sub bottle
    val avgNose = if (reviews.isNotEmpty()) reviews.map { it.noseScore }.average() else 0.0
    val avgPalate = if (reviews.isNotEmpty()) reviews.map { it.palateScore }.average() else 0.0
    val avgFinish = if (reviews.isNotEmpty()) reviews.map { it.finishScore }.average() else 0.0
    val avgOverall = if (reviews.isNotEmpty()) reviews.map { it.overallScore }.average() else 0.0

    val subBottleLabel = if (sub?.subBottleNumber == 0) "Single Pours" else "Sub-Bottle #${sub?.subBottleNumber ?: ""}"

    JournalScreenScaffold(
        title = bottle?.name ?: "Sub-Bottle",
        subtitle = subBottleLabel,
        onBackClick = { navController.popBackStack() }
    ) {
        PremiumFormCard(title = "Sub-Bottle Aggregates") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AverageItem("Nose", avgNose)
                AverageItem("Palate", avgPalate)
                AverageItem("Finish", avgFinish)
                AverageItem("Overall", avgOverall)
            }
        }

        Text(
            text = "REVIEWS SEQUENCE (DATE RECENT TO LEAST)",
            color = TextSoftGray,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        if (reviews.isEmpty()) {
            Text("No review entries recorded for this sub-bottle instance.", color = TextSoftGray)
        } else {
            reviews.forEach { review ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.activeReadOnlyReview = review
                            navController.navigate("${Routes.READ_ONLY_REVIEW}/${review.reviewId}")
                        }
                        .border(1.dp, SlateMuted, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("📝", fontSize = 20.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Date: ${review.whenDate}",
                                color = TextWarmWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Location: ${review.wherePlace}",
                                color = TextSoftGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Overall Score: ",
                                color = TextSoftGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(SlateMuted, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    review.overallScore.toString(),
                                    color = PrimaryAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = PrimaryAmber)
                    }
                }
            }
        }
    }
}

/**
 * Read-Only Walkthrough of a saved review
 */
@Composable
fun ReadOnlyReviewWalkthrough(reviewId: Long, viewModel: BourbonViewModel, navController: NavController) {
    val allReviews by viewModel.allReviews.collectAsState()
    val review = allReviews.find { it.reviewId == reviewId }

    JournalScreenScaffold(
        title = "Tasting Ledger Entry",
        subtitle = "Review Details",
        onBackClick = {
            navController.popBackStack()
        }
    ) {
        if (review != null) {
            PremiumFormCard(title = "Tasting Pour Specs") {
                Text("Date Tried: ${review.whenDate}", color = TextWarmWhite)
                Text("Location: ${review.wherePlace}", color = TextWarmWhite)
                Text("Price: $${review.price}", color = TextWarmWhite)
                Text("Drunk Score: ${review.drunkScore}/7", color = PrimaryAmber, fontWeight = FontWeight.Bold)
                Text("Glassware: ${review.glass}", color = TextWarmWhite)
                Text("On Ice / Rocks: ${if (review.ice) "Yes" else "No"}", color = TextWarmWhite)
                Text("Amount: ${review.amountMl}ml (${review.amountOz}oz)", color = TextWarmWhite)
                if (review.otherPour.isNotEmpty()) {
                    Text("Pour Notes: ${review.otherPour}", color = TextSoftGray, style = MaterialTheme.typography.bodySmall)
                }
            }

            PremiumFormCard(title = "Physical Sensations") {
                Text("👃 Nose Score: ${review.noseScore}/7", color = PrimaryAmber, fontWeight = FontWeight.Bold)
                Text("Notes: ${review.noseNotes}", color = TextWarmWhite, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))

                Text("👅 Palate Score: ${review.palateScore}/7", color = PrimaryAmber, fontWeight = FontWeight.Bold)
                Text("Notes: ${review.palateNotes}", color = TextWarmWhite, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))

                Text("🏁 Finish Score: ${review.finishScore}/7", color = PrimaryAmber, fontWeight = FontWeight.Bold)
                Text("Notes: ${review.finishNotes}", color = TextWarmWhite, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))

                Text("⭐ Overall Score: ${review.overallScore}/7", color = PrimaryAmber, fontWeight = FontWeight.ExtraBold)
                Text("Overall Notes: ${review.overallNotes}", color = TextWarmWhite, style = MaterialTheme.typography.bodySmall)
            }

            Text(
                text = "FLAVOR DIAL MAP",
                color = TextSoftGray,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateMuted, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                shape = RoundedCornerShape(16.dp)
            ) {
                val profileMap = mapOf(
                    "Nutty" to review.nutty,
                    "Woody" to review.woody,
                    "Heat" to review.heat,
                    "Herbal" to review.herbal,
                    "Citrusy" to review.citrusy,
                    "Stone Fruity" to review.stoneFruity,
                    "Smokey" to review.smokey,
                    "Sour" to review.sour,
                    "Sweet" to review.sweet,
                    "Savory" to review.savory
                )
                // Use readonly view by not registering active drags
                InteractiveFlavorWheel(
                    scores = profileMap,
                    onScoresChanged = {},
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

/**
 * Page 4: Stats screen
 */
@Composable
fun StatsScreen(viewModel: BourbonViewModel, navController: NavController) {
    val stats by viewModel.statsState.collectAsState()

    JournalScreenScaffold(
        title = "Analytics Dashboard",
        subtitle = "Collection aggregates Only",
        onBackClick = { navController.popBackStack() }
    ) {
        Text(
            text = "Aggregated metrics drawing exclusively from the Bottles database, completely isolating your Blind lists as specified.",
            color = TextSoftGray,
            style = MaterialTheme.typography.bodySmall
        )

        PremiumFormCard(title = "High Level Cabinet Indexes") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatPanelItem("Bottles Owned", stats.totalBottlesCount.toString())
                StatPanelItem("Reviews Handled", stats.totalReviewsCount.toString())
            }
        }

        PremiumFormCard(title = "Signature Favorites") {
            StatRow("🏆 Highest Ranked Bottle", stats.highestRankedBottle) {
                if (stats.highestRankedAvg > 0) {
                    Text("Average Rating Score: ${String.format("%.1f", stats.highestRankedAvg)}/7", color = PrimaryAmber, style = MaterialTheme.typography.bodySmall)
                }
            }

            StatRow("📝 Most Reviewed", stats.mostReviewedBottle) {
                if (stats.mostReviewedCount > 0) {
                    Text("Total Review Sessions: ${stats.mostReviewedCount}", color = PrimaryAmber, style = MaterialTheme.typography.bodySmall)
                }
            }

            StatRow("📊 Most Filled Category", stats.mostFilledCategory) {
                if (stats.mostFilledCount > 0) {
                    Text("Total Bottles in Category: ${stats.mostFilledCount}", color = PrimaryAmber, style = MaterialTheme.typography.bodySmall)
                }
            }

            StatRow("⭐ Best Rated Category", stats.topCategory) {
                if (stats.topCategoryAverage > 0) {
                    Text("Average Category Rating: ${String.format("%.1f", stats.topCategoryAverage)}/7", color = PrimaryAmber, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun StatPanelItem(key: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(12.dp)
    ) {
        Text(key.uppercase(), color = TextSoftGray, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = PrimaryAmber, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatRow(label: String, value: String, subDetail: @Composable () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(label.uppercase(), color = TextSoftGray, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text(value, color = TextWarmWhite, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        subDetail()
        Divider(color = SlateMuted, modifier = Modifier.padding(top = 8.dp))
    }
}

/**
 * Page 18: Blind Tasting Configuration
 */
@Composable
fun BlindSetupScreen(viewModel: BourbonViewModel, navController: NavController) {
    var errorText by remember { mutableStateOf("") }

    JournalScreenScaffold(
        title = "Blind Tasting",
        subtitle = "Tasting Game Setup",
        onBackClick = { navController.popBackStack() },
        onForwardClick = {
            val num = viewModel.blindNumPours.toIntOrNull()
            if (num == null || num !in 1..10) {
                errorText = "Enter a valid number of pours (Between 1 and 10)."
            } else {
                errorText = ""
                viewModel.startBlindTasting {
                    navController.navigate(Routes.BLIND_POUR_GUESS)
                }
            }
        },
        forwardText = "Pour #1"
    ) {
        Text(
            text = "Guide an interactive double-blind assessment workspace. Setup tasting parameter specs, log guesses, rank glasses and reveal findings step-by-step.",
            color = TextSoftGray,
            style = MaterialTheme.typography.bodySmall
        )

        PremiumFormCard(title = "Tasting Specifications") {
            DatePickerField(
                value = viewModel.blindDate,
                onValueChange = { viewModel.blindDate = it },
                label = "Session Date",
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.blindWhere,
                onValueChange = { viewModel.blindWhere = it },
                label = { Text("Session Location") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.blindNumPours,
                onValueChange = { viewModel.blindNumPours = it },
                label = { Text("Number of Pours (Max 10)") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.blindTheme,
                onValueChange = { viewModel.blindTheme = it },
                label = { Text("Blind Theme (e.g. Wheated Ryes)") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextWarmWhite,
                    unfocusedTextColor = TextWarmWhite,
                    focusedContainerColor = SlateMuted,
                    unfocusedContainerColor = DarkCharcoal,
                    focusedIndicatorColor = PrimaryAmber,
                    unfocusedIndicatorColor = SlateMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorText.isNotEmpty()) {
                Text(errorText, color = AccentError, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Page 19: Blind Pour Assessment Guess Page
 */
@Composable
fun BlindPourGuessScreen(viewModel: BourbonViewModel, navController: NavController) {
    val currentIndex = viewModel.currentTastePourIndex
    val totalPours = viewModel.blindPoursGuess.size
    val currentGuess = viewModel.blindPoursGuess.getOrNull(currentIndex)

    var isDropdownCategoryExpanded by remember { mutableStateOf(false) }
    val categories = listOf(
        "American Whisky", "Bourbon", "Irish", "Japanese",
        "Light Whiskey", "Rye", "Scotch", "Wheat"
    )

    JournalScreenScaffold(
        title = "Glass Pour #${currentIndex + 1}",
        subtitle = "Tasting guessing Specs",
        onBackClick = {
            if (currentIndex > 0) {
                viewModel.currentTastePourIndex = currentIndex - 1
            } else {
                navController.navigate(Routes.BLIND_SETUP)
            }
        },
        onForwardClick = {
            if (currentIndex < totalPours - 1) {
                viewModel.currentTastePourIndex = currentIndex + 1
            } else {
                navController.navigate(Routes.BLIND_RANKS)
            }
        },
        forwardText = if (currentIndex == totalPours - 1) "Rank Pours" else "Pour #${currentIndex + 2}"
    ) {
        if (currentGuess != null) {
            Text(
                text = "Observe glass #${currentIndex + 1} and document guesses on the distillery origin, category, proof and raw tasting score assessments.",
                color = TextSoftGray,
                style = MaterialTheme.typography.bodySmall
            )

            PremiumFormCard(title = "Origin & Class Guessing") {
                OutlinedTextField(
                    value = currentGuess.distillery,
                    onValueChange = { currentGuess.distillery = it },
                    label = { Text("Guess Distillery") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextWarmWhite,
                        unfocusedTextColor = TextWarmWhite,
                        focusedContainerColor = SlateMuted,
                        unfocusedContainerColor = DarkCharcoal,
                        focusedIndicatorColor = PrimaryAmber,
                        unfocusedIndicatorColor = SlateMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = currentGuess.proof,
                    onValueChange = { currentGuess.proof = it },
                    label = { Text("Guess Proof") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextWarmWhite,
                        unfocusedTextColor = TextWarmWhite,
                        focusedContainerColor = SlateMuted,
                        unfocusedContainerColor = DarkCharcoal,
                        focusedIndicatorColor = PrimaryAmber,
                        unfocusedIndicatorColor = SlateMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Domain dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentGuess.category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = TextWarmWhite,
                            unfocusedTextColor = TextWarmWhite,
                            focusedContainerColor = SlateMuted,
                            unfocusedContainerColor = DarkCharcoal,
                            focusedIndicatorColor = PrimaryAmber,
                            unfocusedIndicatorColor = SlateMuted
                        ),
                        trailingIcon = {
                            IconButton(onClick = { isDropdownCategoryExpanded = true }) {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = PrimaryAmber)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = isDropdownCategoryExpanded,
                        onDismissRequest = { isDropdownCategoryExpanded = false },
                        modifier = Modifier
                            .background(DarkCharcoal)
                            .border(1.dp, SlateMuted, RoundedCornerShape(8.dp))
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = TextWarmWhite) },
                                onClick = {
                                    currentGuess.category = cat
                                    isDropdownCategoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            PremiumFormCard(title = "Tasting Ratings (1-7)") {
                Text("👃 Nose Rating", color = TextWarmWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                CircularRatingRow(
                    selectedScore = currentGuess.noseScore,
                    onScoreSelected = { currentGuess.noseScore = it }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text("👅 Palate Rating", color = TextWarmWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                CircularRatingRow(
                    selectedScore = currentGuess.palateScore,
                    onScoreSelected = { currentGuess.palateScore = it }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text("🏁 Finish Rating", color = TextWarmWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                CircularRatingRow(
                    selectedScore = currentGuess.finishScore,
                    onScoreSelected = { currentGuess.finishScore = it }
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = currentGuess.notes,
                    onValueChange = { currentGuess.notes = it },
                    label = { Text("Pour Notes") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextWarmWhite,
                        unfocusedTextColor = TextWarmWhite,
                        focusedContainerColor = SlateMuted,
                        unfocusedContainerColor = DarkCharcoal,
                        focusedIndicatorColor = PrimaryAmber,
                        unfocusedIndicatorColor = SlateMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Page 20: Blind Ranks drag list reordering
 */
@Composable
fun BlindRanksScreen(viewModel: BourbonViewModel, navController: NavController) {
    val total = viewModel.blindPoursGuess.size

    // Handle initialization of standard rank elements
    LaunchedEffect(key1 = total) {
        if (viewModel.blindRankList.isEmpty()) {
            for (i in 1..total) {
                viewModel.blindRankList.add(i)
            }
        }
    }

    JournalScreenScaffold(
        title = "Rank Pours",
        subtitle = "Order preferences",
        onBackClick = {
            viewModel.currentTastePourIndex = total - 1
            navController.navigate(Routes.BLIND_POUR_GUESS)
        },
        onForwardClick = {
            // Set first index for reveals loop
            viewModel.currentTastePourIndex = 0
            navController.navigate(Routes.BLIND_REVEAL)
        },
        forwardText = "Reveal"
    ) {
        Text(
            text = "Move pour glasses up or down in the preferences stack to record preferences rank.",
            color = TextSoftGray,
            style = MaterialTheme.typography.bodySmall
        )

        PremiumFormCard(title = "Preferences Stack (Top is Best)") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.blindRankList.forEachIndexed { index, pourNum ->
                    Surface(
                        color = SlateMuted,
                        border = BorderStroke(1.dp, PrimaryAmber.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(PrimaryAmber, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${index + 1}",
                                        color = ObsidianBlack,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = "Pour Glass #${pourNum}",
                                    color = TextWarmWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Symmetrically aligned arrows representing rank up/down shifts
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val temp = viewModel.blindRankList[index]
                                            viewModel.blindRankList[index] = viewModel.blindRankList[index - 1]
                                            viewModel.blindRankList[index - 1] = temp
                                        }
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = if (index > 0) PrimaryAmber else TextSoftGray.copy(alpha = 0.2f))
                                }

                                IconButton(
                                    onClick = {
                                        if (index < total - 1) {
                                            val temp = viewModel.blindRankList[index]
                                            viewModel.blindRankList[index] = viewModel.blindRankList[index + 1]
                                            viewModel.blindRankList[index + 1] = temp
                                        }
                                    },
                                    enabled = index < total - 1,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = if (index < total - 1) PrimaryAmber else TextSoftGray.copy(alpha = 0.2f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Page 21: Blind Reveal Screen
 */
@Composable
fun BlindRevealScreen(viewModel: BourbonViewModel, navController: NavController) {
    val currentIndex = viewModel.currentTastePourIndex
    val total = viewModel.blindPoursGuess.size
    val currentGuess = viewModel.blindPoursGuess.getOrNull(currentIndex)

    // Calculate final rank
    val rank = viewModel.blindRankList.indexOf(currentIndex + 1) + 1

    var isDropdownCategoryExpanded by remember { mutableStateOf(false) }
    val categories = listOf(
        "American Whisky", "Bourbon", "Irish", "Japanese",
        "Light Whiskey", "Rye", "Scotch", "Wheat"
    )

    JournalScreenScaffold(
        title = "Reveal Glass Pour #${currentIndex + 1}",
        subtitle = "Distillery Origin Disclosure",
        onBackClick = {
            if (currentIndex > 0) {
                viewModel.currentTastePourIndex = currentIndex - 1
            } else {
                navController.navigate(Routes.BLIND_RANKS)
            }
        },
        onForwardClick = {
            if (currentIndex < total - 1) {
                viewModel.currentTastePourIndex = currentIndex + 1
            } else {
                // Save tasting fully to local SQLite
                viewModel.saveBlindTasting {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            }
        },
        forwardText = if (currentIndex == total - 1) "Save session" else "Reveal Pour #${currentIndex + 2}"
    ) {
        if (currentGuess != null) {
            Text(
                text = "Unmask the actual label information and compare with your blind notes & scores. Final Preference Rank is auto-calculated.",
                color = TextSoftGray,
                style = MaterialTheme.typography.bodySmall
            )

            PremiumFormCard(title = "Tasting Preferences & Feedback Correlation") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tasted Preference Rank", color = TextSoftGray)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PrimaryAmber, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(rank.toString(), color = ObsidianBlack, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = SlateMuted)

                Text("Your blind guesses", color = PrimaryAmber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("Guess Brewery/Distillery: ${currentGuess.distillery.ifEmpty { "N/A" }}", color = TextWarmWhite)
                Text("Guess Proof: ${currentGuess.proof.ifEmpty { "N/A" }}", color = TextWarmWhite)
                Text("Guess Category: ${currentGuess.category}", color = TextWarmWhite)
                Text("Scores: Nose ${currentGuess.noseScore}/7 | Palate ${currentGuess.palateScore}/7 | Finish ${currentGuess.finishScore}/7", color = TextWarmWhite)
                if (currentGuess.notes.isNotEmpty()) {
                    Text("Guesses Notes: ${currentGuess.notes}", color = TextSoftGray, style = MaterialTheme.typography.bodySmall)
                }
            }

            PremiumFormCard(title = "Disclose & Input Actual Label details") {
                OutlinedTextField(
                    value = currentGuess.revName,
                    onValueChange = { currentGuess.revName = it },
                    label = { Text("Actual Bottle Name") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextWarmWhite,
                        unfocusedTextColor = TextWarmWhite,
                        focusedContainerColor = SlateMuted,
                        unfocusedContainerColor = DarkCharcoal,
                        focusedIndicatorColor = PrimaryAmber,
                        unfocusedIndicatorColor = SlateMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = currentGuess.revDistillery,
                    onValueChange = { currentGuess.revDistillery = it },
                    label = { Text("Actual Distillery") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextWarmWhite,
                        unfocusedTextColor = TextWarmWhite,
                        focusedContainerColor = SlateMuted,
                        unfocusedContainerColor = DarkCharcoal,
                        focusedIndicatorColor = PrimaryAmber,
                        unfocusedIndicatorColor = SlateMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = currentGuess.revProof,
                    onValueChange = { currentGuess.revProof = it },
                    label = { Text("Actual Proof") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextWarmWhite,
                        unfocusedTextColor = TextWarmWhite,
                        focusedContainerColor = SlateMuted,
                        unfocusedContainerColor = DarkCharcoal,
                        focusedIndicatorColor = PrimaryAmber,
                        unfocusedIndicatorColor = SlateMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Drop level
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentGuess.revCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Actual Category") },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = TextWarmWhite,
                            unfocusedTextColor = TextWarmWhite,
                            focusedContainerColor = SlateMuted,
                            unfocusedContainerColor = DarkCharcoal,
                            focusedIndicatorColor = PrimaryAmber,
                            unfocusedIndicatorColor = SlateMuted
                        ),
                        trailingIcon = {
                            IconButton(onClick = { isDropdownCategoryExpanded = true }) {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = PrimaryAmber)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = isDropdownCategoryExpanded,
                        onDismissRequest = { isDropdownCategoryExpanded = false },
                        modifier = Modifier
                            .background(DarkCharcoal)
                            .border(1.dp, SlateMuted, RoundedCornerShape(8.dp))
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = TextWarmWhite) },
                                onClick = {
                                    currentGuess.revCategory = cat
                                    isDropdownCategoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text("Actual Scores Assessments (If revised after unmasking)", color = TextWarmWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text("👃 Nose Rating Adjusted", color = TextWarmWhite, style = MaterialTheme.typography.bodySmall)
                CircularRatingRow(
                    selectedScore = currentGuess.revNoseScore,
                    onScoreSelected = { currentGuess.revNoseScore = it }
                )

                Text("👅 Palate Rating Adjusted", color = TextWarmWhite, style = MaterialTheme.typography.bodySmall)
                CircularRatingRow(
                    selectedScore = currentGuess.revPalateScore,
                    onScoreSelected = { currentGuess.revPalateScore = it }
                )

                Text("🏁 Finish Rating Adjusted", color = TextWarmWhite, style = MaterialTheme.typography.bodySmall)
                CircularRatingRow(
                    selectedScore = currentGuess.revFinishScore,
                    onScoreSelected = { currentGuess.revFinishScore = it }
                )

                OutlinedTextField(
                    value = currentGuess.revNotes,
                    onValueChange = { currentGuess.revNotes = it },
                    label = { Text("Actual Post-Tasting Notes") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextWarmWhite,
                        unfocusedTextColor = TextWarmWhite,
                        focusedContainerColor = SlateMuted,
                        unfocusedContainerColor = DarkCharcoal,
                        focusedIndicatorColor = PrimaryAmber,
                        unfocusedIndicatorColor = SlateMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Page 22: Completed Blinds list
 */
@Composable
fun CompletedBlindsScreen(viewModel: BourbonViewModel, navController: NavController) {
    val blinds by viewModel.allBlinds.collectAsState()

    JournalScreenScaffold(
        title = "Completed Sessions",
        subtitle = "History Blinds list",
        onBackClick = { navController.popBackStack() }
    ) {
        if (blinds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔒", fontSize = 48.sp)
                    Text("No Completed Sessions", color = TextWarmWhite, fontWeight = FontWeight.Bold)
                    Text("Create a blind session setup on home to log details.", color = TextSoftGray)
                }
            }
        } else {
            blinds.forEach { blind ->
                var expandedRevealList by remember { mutableStateOf(false) }
                val revealsState = viewModel.getRevealsForBlind(blind.blindId).collectAsState(initial = emptyList())

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .border(1.dp, SlateMuted, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedRevealList = !expandedRevealList }
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Theme: ${blind.theme}",
                                color = PrimaryAmber,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Icon(
                                imageVector = if (expandedRevealList) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = PrimaryAmber
                            )
                        }
                        Text("Date: ${blind.date} • Location: ${blind.wherePlace}", color = TextWarmWhite)
                        Text("Size: ${blind.numberOfPours} glasses evaluated.", color = TextSoftGray, style = MaterialTheme.typography.bodySmall)

                        if (expandedRevealList) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = SlateMuted)
                            Spacer(modifier = Modifier.height(8.dp))

                            revealsState.value.sortedBy { it.rank }.forEach { rev ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Rank #${rev.rank} - Pour Glass #${rev.pourNumber}",
                                            color = PrimaryAmber,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(SlateMuted, CircleShape)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(rev.revealedCategory, color = TextWarmWhite, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    Text("revealed: ${rev.revealedName}", color = TextWarmWhite, fontWeight = FontWeight.SemiBold)
                                    Text("Distillery: ${rev.revealedDistillery} • Proof: ${rev.revealedProof}", color = TextSoftGray, style = MaterialTheme.typography.bodySmall)
                                    Text("Assessments: Nose ${rev.revealedNoseScore}/7 | Palate ${rev.revealedPalateScore}/7 | Finish ${rev.revealedFinishScore}/7", color = TextSoftGray, style = MaterialTheme.typography.bodySmall)
                                    if (rev.revealedNotes.isNotEmpty()) {
                                        Text("Overall notes: ${rev.revealedNotes}", color = TextSoftGray, fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Divider(color = SlateMuted.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
