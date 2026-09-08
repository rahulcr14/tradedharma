package com.tradedharma.app.ui.screens.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tradedharma.app.data.local.ReviewEntity
import com.tradedharma.app.domain.analytics.Analytics
import com.tradedharma.app.domain.model.ReviewRating
import com.tradedharma.app.domain.repository.TradeRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReviewScreen(repository: TradeRepository, onBack: () -> Unit) {
    ReviewEditor(repository, "Daily Review", onBack, false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReviewScreen(repository: TradeRepository, onBack: () -> Unit) {
    ReviewEditor(repository, "Weekly Review", onBack, true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewEditor(repository: TradeRepository, title: String, onBack: () -> Unit, weekly: Boolean) {
    val trades = repository.observeTrades().collectAsStateWithLifecycle(emptyList()).value
    val scope = rememberCoroutineScope()
    val now = Calendar.getInstance()
    val start = Calendar.getInstance().apply {
        timeInMillis = now.timeInMillis
        if (weekly) {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        } else {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
    }.timeInMillis
    val end = Calendar.getInstance().apply {
        timeInMillis = now.timeInMillis
        if (weekly) add(Calendar.DAY_OF_YEAR, 7) else add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val periodTrades = trades.filter { (it.closedAtEpochMs ?: it.openedAtEpochMs) in start until end }
    val key = SimpleDateFormat(if (weekly) "yyyy-'W'ww" else "yyyy-MM-dd", Locale.getDefault()).format(Date())
    var rating by remember { mutableStateOf(ReviewRating.PARTIAL) }
    var reflection by remember { mutableStateOf("") }
    var improvement by remember { mutableStateOf("") }
    LaunchedEffect(key) { repository.getReview(key)?.let { rating = it.rating; reflection = it.reflection; improvement = it.improvement } }
    Scaffold(topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(if (weekly) "This week's summary" else "Today's summary", style = MaterialTheme.typography.headlineSmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(Modifier.weight(1f)) { Column(Modifier.padding(14.dp)) { Text("Trades"); Text(periodTrades.size.toString(), style = MaterialTheme.typography.titleLarge) } }
                Card(Modifier.weight(1f)) { Column(Modifier.padding(14.dp)) { Text("Net P&L"); Text("₹%.2f".format(Analytics.netPnl(periodTrades)), style = MaterialTheme.typography.titleLarge) } }
            }
            Text("Did you follow your plan?", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                ReviewRating.values().forEachIndexed { index, r -> SegmentedButton(selected = rating == r, onClick = { rating = r }, shape = SegmentedButtonDefaults.itemShape(index, ReviewRating.values().size)) { Text(r.name.lowercase().replaceFirstChar { it.uppercase() }) } }
            }
            OutlinedTextField(reflection, { reflection = it }, Modifier.fillMaxWidth().height(130.dp), label = { Text("What happened?") })
            OutlinedTextField(improvement, { improvement = it }, Modifier.fillMaxWidth().height(130.dp), label = { Text("What will I improve?") })
            Button(onClick = { scope.launch { repository.saveReview(ReviewEntity(key, rating, reflection, improvement)); onBack() } }, Modifier.fillMaxWidth()) { Text("Save Review") }
        }
    }
}
