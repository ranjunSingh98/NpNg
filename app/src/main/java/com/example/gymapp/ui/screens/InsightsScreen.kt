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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.gymapp.ui.components.WorkoutHeatmap
import com.example.gymapp.ui.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch
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
    val workoutDaysByMonth by viewModel.workoutDaysByMonth.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val graphicsLayer = rememberGraphicsLayer()
    
    // Create a list of 24 months ending with the current month
    val months = remember {
        val list = mutableListOf<Pair<Int, Int>>()
        val cal = Calendar.getInstance()
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
                                    if (pagerState.currentPage > 0) {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                }
                            },
                            enabled = pagerState.currentPage > 0
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
                        }

                        val currentMonthData = months[pagerState.currentPage]
                        val monthName = remember(currentMonthData) {
                            val cal = Calendar.getInstance()
                            cal.set(Calendar.YEAR, currentMonthData.first)
                            cal.set(Calendar.MONTH, currentMonthData.second)
                            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
                        }

                        Text(
                            text = monthName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (pagerState.currentPage < months.size - 1) {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            },
                            enabled = pagerState.currentPage < months.size - 1
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
                        val workoutDays = workoutDaysByMonth["$year-$month"] ?: emptySet()
                        
                        Box(modifier = Modifier
                            .drawWithContent {
                                graphicsLayer.record {
                                    this@drawWithContent.drawContent()
                                }
                                drawLayer(graphicsLayer)
                            }
                        ) {
                            WorkoutHeatmap(
                                workoutDays = workoutDays,
                                year = year,
                                month = month,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            val allSessions by viewModel.allSessions.collectAsState(initial = emptyList())
            val currentMonthData = months[pagerState.currentPage]
            
            val monthStats = remember(allSessions, currentMonthData) {
                val year = currentMonthData.first
                val month = currentMonthData.second
                
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
                
                Triple(allSessions.size, workoutDays, avgPerWeek)
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
