package com.example

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room

// Palette definition for customized app theme instances
data class ThemePalette(
    val background: Color,
    val surface: Color,
    val primaryAccent: Color,      // equals / highlighted action
    val secondaryAccent: Color,    // helper indicators / toggles
    val numberButtonBg: Color,
    val numberButtonText: Color,
    val operatorButtonBg: Color,
    val operatorButtonText: Color,
    val specialButtonBg: Color,
    val specialButtonText: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val screenBgGradStart: Color,
    val screenBgGradEnd: Color
)

// Define high-fidelity palettes
val ProfessionalPolishPalette = ThemePalette(
    background = Color(0xFFFDF8F7),
    surface = Color(0xFFF3EDF7),
    primaryAccent = Color(0xFF6750A4), // Equals accent background
    secondaryAccent = Color(0xFFE8DEF8), // Backspace / Operator background
    numberButtonBg = Color(0xFFFDF8F7),
    numberButtonText = Color(0xFF1C1B1F),
    operatorButtonBg = Color(0xFFE8DEF8),
    operatorButtonText = Color(0xFF1D192B),
    specialButtonBg = Color(0xFF6750A4),
    specialButtonText = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF1C1B1F),
    textSecondary = Color(0xFF49454F),
    screenBgGradStart = Color(0xFFFDF8F7),
    screenBgGradEnd = Color(0xFFFDF8F7)
)

val MidnightVelvetPalette = ThemePalette(
    background = Color(0xFF0D0F12),
    surface = Color(0xFF161A22),
    primaryAccent = Color(0xFFD4AF37), // Pure Gold
    secondaryAccent = Color(0xFFE2E8F0),
    numberButtonBg = Color(0xFF1F2430),
    numberButtonText = Color(0xFFF1F5F9),
    operatorButtonBg = Color(0xFF2A3142),
    operatorButtonText = Color(0xFFD4AF37),
    specialButtonBg = Color(0xFFD4AF37),
    specialButtonText = Color(0xFF0D0F12),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    screenBgGradStart = Color(0xFF161C2A),
    screenBgGradEnd = Color(0xFF0D0F12)
)

val EarthyForestPalette = ThemePalette(
    background = Color(0xFFF2EFE9),
    surface = Color(0xFFE3DEC3),
    primaryAccent = Color(0xFF3B5249), // Deep Forest Green
    secondaryAccent = Color(0xFF519872),
    numberButtonBg = Color(0xFFEAE5D9),
    numberButtonText = Color(0xFF23302B),
    operatorButtonBg = Color(0xFFD5CEB9),
    operatorButtonText = Color(0xFF3B5249),
    specialButtonBg = Color(0xFF3B5249),
    specialButtonText = Color(0xFFF2EFE9),
    textPrimary = Color(0xFF1C2C24),
    textSecondary = Color(0xFF5A6B61),
    screenBgGradStart = Color(0xFFE6EBE6),
    screenBgGradEnd = Color(0xFFF2EFE9)
)

val CyberNeonPalette = ThemePalette(
    background = Color(0xFF000000),
    surface = Color(0xFF0D0A16),
    primaryAccent = Color(0xFF00FFFF), // Neon Cyan
    secondaryAccent = Color(0xFFF000FF), // Neon Magenta
    numberButtonBg = Color(0xFF120E25),
    numberButtonText = Color(0xFFFFFFFF),
    operatorButtonBg = Color(0xFF1E143A),
    operatorButtonText = Color(0xFFF000FF),
    specialButtonBg = Color(0xFF00FFFF),
    specialButtonText = Color(0xFF000000),
    textPrimary = Color(0xFF00FFFF),
    textSecondary = Color(0xFFF000FF),
    screenBgGradStart = Color(0xFF1B0330),
    screenBgGradEnd = Color(0xFF000000)
)

val LocalThemePalette = staticCompositionLocalOf { ProfessionalPolishPalette }

class MainActivity : ComponentActivity() {

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            CalculationDatabase::class.java,
            "calculation_database"
        ).fallbackToDestructiveMigration().build()
    }

    private val repository by lazy {
        CalculationRepository(database.historyDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CalculatorViewModel = viewModel(
                factory = CalculatorViewModelFactory(repository)
            )

            val currentThemeState by viewModel.currentTheme.collectAsStateWithLifecycle()
            val basePalette = when (currentThemeState) {
                CalculatorTheme.PROFESSIONAL_POLISH -> ProfessionalPolishPalette
                CalculatorTheme.MIDNIGHT_VELVET -> MidnightVelvetPalette
                CalculatorTheme.EARTHY_FOREST -> EarthyForestPalette
                CalculatorTheme.CYBER_NEON -> CyberNeonPalette
                CalculatorTheme.CLASSIC_M3 -> {
                    // Adapt from default MaterialTheme dynamically
                    ThemePalette(
                        background = MaterialTheme.colorScheme.background,
                        surface = MaterialTheme.colorScheme.surfaceVariant,
                        primaryAccent = MaterialTheme.colorScheme.primary,
                        secondaryAccent = MaterialTheme.colorScheme.secondary,
                        numberButtonBg = MaterialTheme.colorScheme.surface,
                        numberButtonText = MaterialTheme.colorScheme.onSurface,
                        operatorButtonBg = MaterialTheme.colorScheme.primaryContainer,
                        operatorButtonText = MaterialTheme.colorScheme.onPrimaryContainer,
                        specialButtonBg = MaterialTheme.colorScheme.tertiary,
                        specialButtonText = MaterialTheme.colorScheme.onTertiary,
                        textPrimary = MaterialTheme.colorScheme.onBackground,
                        textSecondary = MaterialTheme.colorScheme.onSurfaceVariant,
                        screenBgGradStart = MaterialTheme.colorScheme.surfaceContainerHigh,
                        screenBgGradEnd = MaterialTheme.colorScheme.background
                    )
                }
            }

            CompositionLocalProvider(LocalThemePalette provides basePalette) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LocalThemePalette.current.background
                ) {
                    CalculatorAppContent(viewModel)
                }
            }
        }
    }
}

@Composable
fun CalculatorAppContent(viewModel: CalculatorViewModel) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val palette = LocalThemePalette.current

    // State bindings
    val expression by viewModel.expression.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val isScientificExpanded by viewModel.isScientificExpanded.collectAsStateWithLifecycle()
    val useRadians by viewModel.useRadians.collectAsStateWithLifecycle()
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsStateWithLifecycle()
    val showHistory by viewModel.showHistory.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()

    val view = LocalView.current

    // Combined click feedback handler
    val triggerFeedback = {
        if (isSoundEnabled) {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(palette.screenBgGradStart, palette.screenBgGradEnd)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        if (isLandscape) {
            // Adaptive Landscape split layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel: History and settings
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .background(palette.surface.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "History Logs",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        if (historyList.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    triggerFeedback()
                                    viewModel.clearHistory()
                                },
                                modifier = Modifier.testTag("clear_history_btn_landscape")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear calculation history",
                                    tint = palette.primaryAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (historyList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No calculations yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(historyList) { item ->
                                HistoryRowItem(
                                    item = item,
                                    palette = palette,
                                    onSelect = {
                                        triggerFeedback()
                                        viewModel.selectHistoryItem(item)
                                    },
                                    onDelete = {
                                        triggerFeedback()
                                        viewModel.deleteHistoryItem(item.id)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    ThemeSelectorBar(
                        currentTheme = currentTheme,
                        onSelectTheme = { theme ->
                            triggerFeedback()
                            viewModel.selectTheme(theme)
                        },
                        palette = palette
                    )
                }

                // Right Panel: Main Display and Keypad side-by-side
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                ) {
                    // Header Status Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SoundToggleIcon(
                            isSoundEnabled = isSoundEnabled,
                            onToggle = {
                                triggerFeedback()
                                viewModel.toggleSound()
                            },
                            palette = palette
                        )
                    }

                    // Display Board
                    DisplayBoard(
                        expression = expression,
                        result = result,
                        useRadians = useRadians,
                        isScientificExpanded = isScientificExpanded,
                        palette = palette,
                        modifier = Modifier.weight(0.3f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Combined Landscape keyboard
                    Row(
                        modifier = Modifier
                            .weight(0.7f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Scientific Keys Panel
                        Column(
                            modifier = Modifier.weight(0.4f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScientificKeysGrid(
                                useRadians = useRadians,
                                onKeyPress = { key ->
                                    triggerFeedback()
                                    viewModel.onKeyPress(key)
                                },
                                onToggleRadians = {
                                    triggerFeedback()
                                    viewModel.toggleRadians()
                                },
                                palette = palette
                            )
                        }

                        // Basic Keys Panel
                        Column(
                            modifier = Modifier.weight(0.6f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BasicKeysGrid(
                                onKeyPress = { key ->
                                    triggerFeedback()
                                    viewModel.onKeyPress(key)
                                },
                                palette = palette
                            )
                        }
                    }
                }
            }
        } else {
            // Standard Portrait layout (Optimized for handheld mobile ergonomics)
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Bar with Theme Row, Sound, and History toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(0.7f)) {
                        ThemeSelectorBar(
                            currentTheme = currentTheme,
                            onSelectTheme = { theme ->
                                triggerFeedback()
                                viewModel.selectTheme(theme)
                            },
                            palette = palette
                        )
                    }

                    Row(
                        modifier = Modifier.weight(0.3f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SoundToggleIcon(
                            isSoundEnabled = isSoundEnabled,
                            onToggle = {
                                triggerFeedback()
                                viewModel.toggleSound()
                            },
                            palette = palette
                        )
                        IconButton(
                            onClick = {
                                triggerFeedback()
                                viewModel.toggleHistory()
                            },
                            modifier = Modifier.testTag("history_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (showHistory) Icons.Default.Close else Icons.Default.History,
                                contentDescription = "Toggle history sheet",
                                tint = if (showHistory) palette.primaryAccent else palette.textPrimary
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Display Screen area
                        DisplayBoard(
                            expression = expression,
                            result = result,
                            useRadians = useRadians,
                            isScientificExpanded = isScientificExpanded,
                            palette = palette,
                            modifier = Modifier
                                .weight(0.35f)
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        )

                        // Trigger Mode Toggle (Scientific Slider)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(palette.surface)
                                    .clickable {
                                        triggerFeedback()
                                        viewModel.toggleScientific()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isScientificExpanded) Icons.Default.Science else Icons.Outlined.Science,
                                    contentDescription = "Toggle scientific keyboard",
                                    tint = if (isScientificExpanded) palette.primaryAccent else palette.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isScientificExpanded) "Basic" else "Scientific",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isScientificExpanded) palette.primaryAccent else palette.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Input Keys Keyboard
                        Column(
                            modifier = Modifier
                                .weight(0.65f)
                                .fillMaxWidth()
                                .background(
                                    color = palette.surface,
                                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                                )
                                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 28.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Expandable scientific row list
                            AnimatedVisibility(
                                visible = isScientificExpanded,
                                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    ScientificKeysGrid(
                                        useRadians = useRadians,
                                        onKeyPress = { key ->
                                            triggerFeedback()
                                            viewModel.onKeyPress(key)
                                        },
                                        onToggleRadians = {
                                            triggerFeedback()
                                            viewModel.toggleRadians()
                                        },
                                        palette = palette
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }

                            // Standard keypad grid
                            BasicKeysGrid(
                                onKeyPress = { key ->
                                    triggerFeedback()
                                    viewModel.onKeyPress(key)
                                },
                                palette = palette
                            )
                        }
                    }

                    // Sliding history sheet overlays calculations cleanly on Portrait
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showHistory,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    viewModel.toggleHistory()
                                },
                            color = palette.background.copy(alpha = 0.95f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(palette.surface, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Calculation History",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = palette.textPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row {
                                        if (historyList.isNotEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    triggerFeedback()
                                                    viewModel.clearHistory()
                                                },
                                                modifier = Modifier.testTag("clear_history_btn_portrait")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Clear all logs",
                                                    tint = palette.primaryAccent
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                triggerFeedback()
                                                viewModel.toggleHistory()
                                            },
                                            modifier = Modifier.testTag("close_history_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close logs panel",
                                                tint = palette.textSecondary
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = palette.textSecondary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                                if (historyList.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Your calculation history is empty.\nStart calculating to keep records!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = palette.textSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(historyList) { item ->
                                            HistoryRowItem(
                                                item = item,
                                                palette = palette,
                                                onSelect = {
                                                    triggerFeedback()
                                                    viewModel.selectHistoryItem(item)
                                                },
                                                onDelete = {
                                                    triggerFeedback()
                                                    viewModel.deleteHistoryItem(item.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoundToggleIcon(
    isSoundEnabled: Boolean,
    onToggle: () -> Unit,
    palette: ThemePalette
) {
    IconButton(
        onClick = onToggle,
        modifier = Modifier.testTag("sound_toggle_btn")
    ) {
        Icon(
            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
            contentDescription = "Toggle sound feedback",
            tint = if (isSoundEnabled) palette.primaryAccent else palette.textSecondary
        )
    }
}

@Composable
fun ThemeSelectorBar(
    currentTheme: CalculatorTheme,
    onSelectTheme: (CalculatorTheme) -> Unit,
    palette: ThemePalette
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CalculatorTheme.values().forEach { themeOption ->
            val isSelected = currentTheme == themeOption
            val themeLabel = when (themeOption) {
                CalculatorTheme.PROFESSIONAL_POLISH -> "Polish"
                CalculatorTheme.MIDNIGHT_VELVET -> "Velvet"
                CalculatorTheme.EARTHY_FOREST -> "Forest"
                CalculatorTheme.CYBER_NEON -> "Neon"
                CalculatorTheme.CLASSIC_M3 -> "M3"
            }

            // Small aesthetic preview badge
            val accentColor = when (themeOption) {
                CalculatorTheme.PROFESSIONAL_POLISH -> Color(0xFF6750A4)
                CalculatorTheme.MIDNIGHT_VELVET -> Color(0xFFD4AF37)
                CalculatorTheme.EARTHY_FOREST -> Color(0xFF3B5249)
                CalculatorTheme.CYBER_NEON -> Color(0xFF00FFFF)
                CalculatorTheme.CLASSIC_M3 -> MaterialTheme.colorScheme.primary
            }

            Surface(
                onClick = { onSelectTheme(themeOption) },
                shape = RoundedCornerShape(50),
                color = if (isSelected) palette.surface else Color.Transparent,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) palette.primaryAccent else palette.textSecondary.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .testTag("theme_chip_${themeOption.name.lowercase()}")
                    .minimumInteractiveComponentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Text(
                        text = themeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) palette.textPrimary else palette.textSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun DisplayBoard(
    expression: String,
    result: String,
    useRadians: Boolean,
    isScientificExpanded: Boolean,
    palette: ThemePalette,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("display_board_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, palette.textSecondary.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Formula metrics and status labels (Radian, Scientific indicators)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = palette.background.copy(alpha = 0.5f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = if (useRadians) "RAD" else "DEG",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.primaryAccent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (isScientificExpanded) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = palette.background.copy(alpha = 0.5f),
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = "SCI",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textSecondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Active calculation input string
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = expression.ifEmpty { "0" },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = if (expression.length > 12) 28.sp else 38.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = palette.textPrimary,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Evaluated Result Display
            Text(
                text = result,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = if (result.length > 10) 32.sp else 44.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (result == "Error") Color.Red else palette.primaryAccent,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .semantics { contentDescription = "Evaluation result value" },
                maxLines = 1
            )
        }
    }
}

@Composable
fun ScientificKeysGrid(
    useRadians: Boolean,
    onKeyPress: (String) -> Unit,
    onToggleRadians: () -> Unit,
    palette: ThemePalette
) {
    val rows = listOf(
        listOf("sin", "cos", "tan", "^"),
        listOf("log", "ln", "sqrt", "%"),
        listOf("π", "e", if (useRadians) "DEG" else "RAD", "( )")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    val isSpecialToggle = label == "RAD" || label == "DEG"
                    CalculatorKey(
                        label = label,
                        onClick = {
                            if (isSpecialToggle) {
                                onToggleRadians()
                            } else {
                                onKeyPress(label)
                            }
                        },
                        backgroundColor = if (isSpecialToggle) {
                            palette.primaryAccent
                        } else {
                            if (palette.background == Color(0xFFFDF8F7)) palette.operatorButtonBg else palette.operatorButtonBg.copy(alpha = 0.75f)
                        },
                        contentColor = if (isSpecialToggle) palette.background else palette.operatorButtonText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun BasicKeysGrid(
    onKeyPress: (String) -> Unit,
    palette: ThemePalette
) {
    val rows = listOf(
        listOf("C", "⌫", "±", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf(".", "0", "( )", "=")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    val isAction = label == "C" || label == "⌫" || label == "±" || label == "( )"
                    val isOperator = label == "÷" || label == "×" || label == "-" || label == "+"
                    val isEquals = label == "="

                    val isPolish = palette.background == Color(0xFFFDF8F7)

                    val bgColor = when {
                        isEquals -> palette.specialButtonBg
                        isPolish && label == "C" -> Color(0xFFFFDAD6)
                        isPolish && isAction -> palette.operatorButtonBg
                        isOperator -> palette.operatorButtonBg
                        isAction -> palette.surface
                        else -> palette.numberButtonBg
                    }

                    val textColor = when {
                        isEquals -> palette.specialButtonText
                        isPolish && label == "C" -> Color(0xFF95001D)
                        isPolish && isAction -> palette.operatorButtonText
                        isOperator -> palette.operatorButtonText
                        isAction -> palette.primaryAccent
                        else -> palette.numberButtonText
                    }

                    CalculatorKey(
                        label = label,
                        onClick = { onKeyPress(label) },
                        backgroundColor = bgColor,
                        contentColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorKey(
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .aspectRatio(1.3f)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .testTag("btn_${labelToTag(label)}")
            .semantics { contentDescription = "Calculator button $label" },
        contentAlignment = Alignment.Center
    ) {
        if (label == "⌫") {
            Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Backspace",
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = if (label.length > 3) 14.sp else 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor
            )
        }
    }
}

@Composable
fun HistoryRowItem(
    item: CalculationHistory,
    palette: ThemePalette,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("history_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = palette.surface.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, palette.textSecondary.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.85f)) {
                Text(
                    text = item.expression,
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "= ${item.result}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .weight(0.15f)
                    .testTag("delete_history_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete this calculation history item",
                    tint = palette.textSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Convert button labels to valid snake_case test tag strings
private fun labelToTag(label: String): String {
    return when (label) {
        "+" -> "plus"
        "-" -> "minus"
        "×" -> "multiply"
        "÷" -> "divide"
        "=" -> "equals"
        "." -> "decimal"
        "⌫" -> "backspace"
        "C" -> "clear"
        "±" -> "negate"
        "( )" -> "parens"
        "RAD" -> "rad"
        "DEG" -> "deg"
        "%" -> "percent"
        "^" -> "power"
        else -> label.lowercase()
    }
}
