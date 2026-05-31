package com.example.gymapp.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.gymapp.ui.WorkoutCategory
import com.example.gymapp.ui.components.WorkoutHeatmap
import com.example.gymapp.ui.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val workoutStatsByMonth by viewModel.workoutStatsByMonth.collectAsState()
    var highlightedWorkoutType by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val currentCalendar = Calendar.getInstance()
    val currentYear = currentCalendar.get(Calendar.YEAR)
    val currentMonth = currentCalendar.get(Calendar.MONTH)
    
    // Create a list of 24 months ending with the current month
    val months = remember(currentYear, currentMonth) {
        val list = mutableListOf<Pair<Int, Int>>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, currentYear)
        cal.set(Calendar.MONTH, currentMonth)
        for (i in 0 until 24) {
            list.add(cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH))
            cal.add(Calendar.MONTH, -1)
        }
        list.reversed() // So current month is at the end
    }

    val pagerState = rememberPagerState(
        initialPage = months.size - 1,
        pageCount = { months.size }
    )
    var displayedPage by rememberSaveable(currentYear, currentMonth) {
        mutableIntStateOf(months.lastIndex)
    }

    LaunchedEffect(currentYear, currentMonth) {
        displayedPage = months.lastIndex
        pagerState.scrollToPage(months.lastIndex)
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collectLatest { page ->
                displayedPage = page
            }
    }

    val selectedMonthData = months[displayedPage.coerceIn(0, months.lastIndex)]
    val selectedMonthLabel = run {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, selectedMonthData.first)
        cal.set(Calendar.MONTH, selectedMonthData.second)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                            val file = File(context.cacheDir, "heatmap_share.png")
                            FileOutputStream(file).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                            val uri: Uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Heatmap"))
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Month Navigation Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with Arrows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (displayedPage > 0) {
                                        val nextPage = displayedPage - 1
                                        displayedPage = nextPage
                                        pagerState.animateScrollToPage(nextPage)
                                    }
                                }
                            },
                            enabled = displayedPage > 0
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
                        }

                        Text(
                            text = selectedMonthLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (displayedPage < months.size - 1) {
                                        val nextPage = displayedPage + 1
                                        displayedPage = nextPage
                                        pagerState.animateScrollToPage(nextPage)
                                    }
                                }
                            },
                            enabled = displayedPage < months.size - 1
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
                        }
                    }

                    // Horizontal Pager for Heatmap
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        val (year, month) = months[page]
                        val workoutStats = workoutStatsByMonth["$year-$month"] ?: emptyMap()
                        
                        Box(modifier = Modifier
                            .drawWithContent {
                                graphicsLayer.record {
                                    this@drawWithContent.drawContent()
                                }
                                drawLayer(graphicsLayer)
                            }
                        ) {
                            WorkoutHeatmap(
                                workoutStats = workoutStats,
                                year = year,
                                month = month,
                                highlightedWorkoutType = highlightedWorkoutType,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            val allSessions by viewModel.allSessions.collectAsState(initial = emptyList())
            val availableWorkoutTypes = remember(selectedMonthData, workoutStatsByMonth) {
                val monthKey = "${selectedMonthData.first}-${selectedMonthData.second}"
                val presentTypes = workoutStatsByMonth[monthKey]
                    ?.values
                    ?.flatten()
                    ?.distinct()
                    .orEmpty()

                val categoryOrder = WorkoutCategory.categories.map { it.name }
                val knownTypes = categoryOrder.filter { ordered ->
                    presentTypes.any { it.equals(ordered, ignoreCase = true) }
                }
                val customTypes = presentTypes
                    .filterNot { type ->
                        knownTypes.any { it.equals(type, ignoreCase = true) }
                    }
                    .sorted()

                knownTypes + customTypes
            }

            LaunchedEffect(availableWorkoutTypes) {
                if (highlightedWorkoutType != null &&
                    availableWorkoutTypes.none { it.equals(highlightedWorkoutType, ignoreCase = true) }
                ) {
                    highlightedWorkoutType = null
                }
            }
            
            val monthStats = remember(allSessions, selectedMonthData) {
                val year = selectedMonthData.first
                val month = selectedMonthData.second
                
                val cal = Calendar.getInstance()
                val isCurrentMonth = cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
                
                val monthSessions = allSessions.filter {
                    val sCal = Calendar.getInstance()
                    sCal.timeInMillis = it.timestamp
                    sCal.get(Calendar.YEAR) == year && sCal.get(Calendar.MONTH) == month
                }
                
                val workoutDays = monthSessions.map {
                    val sCal = Calendar.getInstance()
                    sCal.timeInMillis = it.timestamp
                    sCal.get(Calendar.DAY_OF_YEAR)
                }.distinct().size
                
                val totalDaysInCalculation = if (isCurrentMonth) {
                    cal.get(Calendar.DAY_OF_MONTH)
                } else {
                    val mCal = Calendar.getInstance()
                    mCal.set(Calendar.YEAR, year)
                    mCal.set(Calendar.MONTH, month)
                    mCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                }
                
                val avgPerWeek = if (totalDaysInCalculation > 0) {
                    (workoutDays.toDouble() / (totalDaysInCalculation.toDouble() / 7.0))
                } else 0.0
                
                Triple(monthSessions.size, workoutDays, avgPerWeek)
            }

            if (availableWorkoutTypes.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Heatmap Focus",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (highlightedWorkoutType == null) {
                                    "Showing all workout days"
                                } else {
                                    "Highlighting $highlightedWorkoutType"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FocusCircleButton(
                                label = "All",
                                selected = highlightedWorkoutType == null,
                                accentColor = MaterialTheme.colorScheme.primary,
                                onClick = { highlightedWorkoutType = null }
                            )
                            availableWorkoutTypes.forEach { workoutType ->
                                val category = WorkoutCategory.getByName(workoutType)
                                FocusCircleButton(
                                    label = workoutType.take(2).uppercase(),
                                    selected = highlightedWorkoutType == workoutType,
                                    accentColor = category?.accentColor ?: MaterialTheme.colorScheme.primary,
                                    iconRes = category?.iconRes,
                                    onClick = { highlightedWorkoutType = workoutType }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Total Workouts",
                    value = monthStats.first.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Workouts This Month",
                    value = monthStats.second.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            StatCard(
                label = "Avg Per Week",
                value = String.format(Locale.getDefault(), "%.1f", monthStats.third),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FocusCircleButton(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    iconRes: Int? = null
) {
    Surface(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(21.dp)),
        shape = RoundedCornerShape(21.dp),
        color = if (selected) {
            accentColor.copy(alpha = 0.22f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
        },
        tonalElevation = if (selected) 2.dp else 0.dp,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
