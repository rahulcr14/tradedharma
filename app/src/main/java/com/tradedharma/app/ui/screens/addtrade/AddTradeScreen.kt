package com.tradedharma.app.ui.screens.addtrade

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tradedharma.app.data.local.SavedExchangeEntity
import com.tradedharma.app.data.local.SavedLotSizeEntity
import com.tradedharma.app.data.local.SavedSymbolEntity
import com.tradedharma.app.data.local.TradeEntity
import com.tradedharma.app.domain.calculation.TradeCalculator
import com.tradedharma.app.domain.model.*
import com.tradedharma.app.domain.repository.TradeRepository
import com.tradedharma.app.ui.components.money
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTradeScreen(
    repository: TradeRepository,
    tradeId: Long?,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val symbols by repository.observeSymbols().collectAsStateWithLifecycle(emptyList())
    val exchanges by repository.observeExchanges().collectAsStateWithLifecycle(emptyList())
    val lotSizes by repository.observeLotSizes().collectAsStateWithLifecycle(emptyList())

    LaunchedEffect(Unit) { repository.seedCatalogDefaults() }

    var existing by remember { mutableStateOf<TradeEntity?>(null) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(tradeId) {
        loaded = false
        existing = tradeId?.takeIf { it >= 0 }?.let { repository.get(it) }
    }

    var instrument by rememberSaveable { mutableStateOf(InstrumentType.OPTIONS) }
    var direction by rememberSaveable { mutableStateOf(TradeDirection.BUY) }
    var symbol by rememberSaveable { mutableStateOf("") }
    var exchange by rememberSaveable { mutableStateOf("NSE") }
    var optionType by rememberSaveable { mutableStateOf(OptionType.PUT) }
    var strike by rememberSaveable { mutableStateOf("") }
    var expiry by rememberSaveable { mutableStateOf("") }
    var lotSizeText by rememberSaveable { mutableStateOf("") }
    var lotsText by rememberSaveable { mutableStateOf("1") }
    var quantityText by rememberSaveable { mutableStateOf("") }
    var entry by rememberSaveable { mutableStateOf("") }
    var exit by rememberSaveable { mutableStateOf("") }
    var stopLoss by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableStateOf("") }
    var charges by rememberSaveable { mutableStateOf("0") }
    var strategy by rememberSaveable { mutableStateOf("") }
    var tags by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var screenshotPath by rememberSaveable { mutableStateOf<String?>(null) }
    var tradeDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(existing) {
        existing?.let { t ->
            if (!loaded) {
                instrument = t.instrumentType
                direction = t.direction
                symbol = t.symbol
                exchange = t.exchange
                optionType = t.optionType ?: OptionType.CALL
                strike = t.strikePrice?.formatNumber().orEmpty()
                expiry = normalizeExpiry(t.expiryDate.orEmpty())
                lotSizeText = t.lotSize?.formatNumber().orEmpty()
                quantityText = t.quantity.formatNumber()
                lotsText = if (t.instrumentType == InstrumentType.OPTIONS && t.lotSize != null && t.lotSize > 0) {
                    (t.quantity / t.lotSize).formatNumber()
                } else t.quantity.formatNumber()
                entry = t.entryPrice.formatNumber()
                exit = t.exitPrice.formatNumber()
                stopLoss = t.stopLoss?.formatNumber().orEmpty()
                target = t.target?.formatNumber().orEmpty()
                charges = t.charges.formatNumber()
                strategy = t.strategy.orEmpty()
                tags = t.tagsCsv
                notes = t.notes
                screenshotPath = t.screenshotPath
                tradeDate = t.tradeDate ?: Instant.ofEpochMilli(t.openedAtEpochMs).atZone(ZoneId.systemDefault()).toLocalDate().toString()
                loaded = true
            }
        }
    }

    var showSymbolPicker by remember { mutableStateOf(false) }
    var showExchangePicker by remember { mutableStateOf(false) }
    var showLotSizePicker by remember { mutableStateOf(false) }
    var showExpiryPicker by remember { mutableStateOf(false) }
    var showTradeDatePicker by remember { mutableStateOf(false) }
    var showCustomSymbol by remember { mutableStateOf(false) }
    var showCustomExchange by remember { mutableStateOf(false) }
    var showCustomLotSize by remember { mutableStateOf(false) }

    val effectiveLotSize = lotSizeText.toDoubleOrNull()
    val effectiveLots = lotsText.toDoubleOrNull()
    val totalQuantity = if (instrument == InstrumentType.OPTIONS && effectiveLotSize != null && effectiveLots != null) {
        effectiveLotSize * effectiveLots
    } else quantityText.toDoubleOrNull()

    val calcPreview = remember(direction, totalQuantity, entry, exit, charges, stopLoss, target) {
        runCatching {
            val q = totalQuantity ?: return@runCatching null
            val e = entry.toDoubleOrNull() ?: return@runCatching null
            val x = exit.toDoubleOrNull() ?: return@runCatching null
            TradeCalculator.calculate(
                direction = direction,
                quantity = q,
                entry = e,
                exit = x,
                charges = charges.toDoubleOrNull() ?: 0.0,
                stopLoss = stopLoss.toDoubleOrNull(),
                target = target.toDoubleOrNull()
            )
        }.getOrNull()
    }

    val screenshotPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) scope.launch { screenshotPath = copyToInternal(uri, context) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (tradeId == null) "Add Trade" else "Edit Trade") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.Close, "Close") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SelectorField(
                    label = "Trade Date",
                    value = expiryDisplay(tradeDate),
                    onClick = { showTradeDatePicker = true },
                    trailingIcon = Icons.Default.CalendarMonth
                )
            }
            item {
                Text("Instrument", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                InstrumentSelector(instrument) { instrument = it }
            }
            item {
                SelectorField(
                    label = "Symbol",
                    value = symbol.ifBlank { "Select symbol" },
                    onClick = { showSymbolPicker = true }
                )
            }
            item {
                ExchangeInputField(
                    value = exchange,
                    onValueChange = { exchange = it.uppercase(Locale.ENGLISH) },
                    onSelect = { showExchangePicker = true }
                )
            }
            item { BuySellSelector(direction) { direction = it } }

            if (instrument == InstrumentType.OPTIONS) {
                item { CallPutSelector(optionType) { optionType = it } }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NumericField("Strike Price", strike, { strike = it }, Modifier.weight(1f))
                        SelectorField(
                            label = "Expiry Date",
                            value = expiryDisplay(expiry).ifBlank { "Select expiry date" },
                            onClick = { showExpiryPicker = true },
                            modifier = Modifier.weight(1f),
                            trailingIcon = Icons.Default.CalendarMonth
                        )
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SelectorField(
                            label = "Lot Size",
                            value = lotSizeText.ifBlank { "Select lot size" },
                            onClick = { showLotSizePicker = true },
                            modifier = Modifier.weight(1f)
                        )
                        NumericField(
                            "Lots / Quantity",
                            lotsText,
                            { lotsText = it },
                            Modifier.weight(1f),
                            supporting = totalQuantity?.let { "Total quantity: ${it.formatNumber()}" }
                        )
                    }
                }
                item { SectionDivider() }
            } else {
                item { NumericField("Quantity", quantityText, { quantityText = it }, Modifier.fillMaxWidth()) }
                item { SectionDivider() }
            }

            item {
                TwoColumn {
                    NumericField("Entry Price", entry, { entry = it }, Modifier.weight(1f))
                    NumericField("Exit Price", exit, { exit = it }, Modifier.weight(1f))
                }
            }
            item { SectionDivider() }
            item {
                TwoColumn {
                    NumericField("Stop Loss", stopLoss, { stopLoss = it }, Modifier.weight(1f))
                    NumericField("Target", target, { target = it }, Modifier.weight(1f))
                }
            }
            item { SectionDivider() }
            item { NumericField("Charges", charges, { charges = it }, Modifier.fillMaxWidth()) }
            item { TextFieldWide("Strategy", strategy, { strategy = it }) }
            item { TextFieldWide("Tags (comma separated)", tags, { tags = it }) }
            item { TextFieldWide("Notes", notes, { notes = it }, singleLine = false, minLines = 4) }
            item {
                OutlinedButton(
                    onClick = { screenshotPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (screenshotPath == null) "Add chart screenshot" else "Screenshot attached")
                }
            }
            calcPreview?.let { calc ->
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Live P&L preview", style = MaterialTheme.typography.titleMedium)
                            Text("Gross ${money(calc.grossPnl)}")
                            Text("Net ${money(calc.netPnl)}")
                            Text("R:R ${calc.plannedRr?.let { "%.2f".format(Locale.ENGLISH, it) } ?: "—"}")
                        }
                    }
                }
            }
            error?.let { msg -> item { Text(msg, color = MaterialTheme.colorScheme.error) } }
            item {
                Button(
                    enabled = tradeId == null || existing != null,
                    onClick = {
                        scope.launch {
                            try {
                                error = null
                                require(symbol.isNotBlank()) { "Symbol is required" }
                                require(exchange.isNotBlank()) { "Exchange is required" }
                                val quantity = if (instrument == InstrumentType.OPTIONS) {
                                    require(effectiveLotSize != null && effectiveLotSize > 0) { "Select a lot size" }
                                    require(effectiveLots != null && effectiveLots > 0) { "Lots must be greater than zero" }
                                    effectiveLotSize * effectiveLots
                                } else {
                                    require(quantityText.toDoubleOrNull()?.let { it > 0 } == true) { "Quantity must be greater than zero" }
                                    quantityText.toDouble()
                                }
                                val entryValue = requireNotNull(entry.toDoubleOrNull()) { "Entry price is required" }
                                val exitValue = requireNotNull(exit.toDoubleOrNull()) { "Exit price is required" }
                                val chargesValue = charges.toDoubleOrNull() ?: 0.0
                                val calc = TradeCalculator.calculate(direction, quantity, entryValue, exitValue, chargesValue, stopLoss.toDoubleOrNull(), target.toDoubleOrNull())
                                val entity = TradeEntity(
                                    id = existing?.id ?: 0,
                                    symbol = symbol.trim().uppercase(Locale.ENGLISH),
                                    exchange = exchange.trim().uppercase(Locale.ENGLISH),
                                    instrumentType = instrument,
                                    direction = direction,
                                    quantity = quantity,
                                    entryPrice = entryValue,
                                    exitPrice = exitValue,
                                    stopLoss = stopLoss.toDoubleOrNull(),
                                    target = target.toDoubleOrNull(),
                                    charges = chargesValue,
                                    strategy = strategy.trim().ifBlank { null },
                                    tagsCsv = tags,
                                    notes = notes,
                                    optionType = if (instrument == InstrumentType.OPTIONS) optionType else null,
                                    strikePrice = if (instrument == InstrumentType.OPTIONS) strike.toDoubleOrNull() else null,
                                    expiryDate = if (instrument == InstrumentType.OPTIONS) expiry.ifBlank { null } else null,
                                    lotSize = if (instrument == InstrumentType.OPTIONS) effectiveLotSize else null,
                                    screenshotPath = screenshotPath,
                                    tradeDate = tradeDate,
                                    openedAtEpochMs = existing?.openedAtEpochMs ?: System.currentTimeMillis(),
                                    closedAtEpochMs = System.currentTimeMillis(),
                                    grossPnl = calc.grossPnl,
                                    netPnl = calc.netPnl,
                                    initialRisk = calc.initialRisk,
                                    plannedReward = calc.plannedReward,
                                    plannedRr = calc.plannedRr,
                                    actualRMultiple = calc.actualRMultiple
                                )
                                if (existing == null) repository.insert(entity) else repository.update(entity)
                                onSaved()
                            } catch (t: Throwable) {
                                error = t.message ?: "Unable to save trade"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (tradeId == null) "Save Trade" else "Save Changes") }
            }
            item { OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Cancel") } }
        }
    }

    if (showSymbolPicker) {
        CatalogPickerDialog(
            title = "Select Symbol",
            searchPlaceholder = "Search symbol or exchange",
            items = symbols,
            itemText = { "${it.symbol} • ${it.exchange}" },
            subtitle = { it.exchange },
            isDefault = { it.isDefault },
            onDismiss = { showSymbolPicker = false },
            onSelect = { symbol = it.symbol; exchange = it.exchange; showSymbolPicker = false },
            onDelete = { scope.launch { repository.deleteCustomSymbol(it.id) } },
            addLabel = "Add custom symbol",
            onAdd = { showSymbolPicker = false; showCustomSymbol = true }
        )
    }

    if (showExchangePicker) {
        CatalogPickerDialog(
            title = "Select Exchange",
            searchPlaceholder = "Search code or exchange name",
            items = exchanges,
            itemText = { "${it.code} • ${it.name}" },
            subtitle = { it.name },
            isDefault = { it.isDefault },
            onDismiss = { showExchangePicker = false },
            onSelect = { exchange = it.code; showExchangePicker = false },
            onDelete = { scope.launch { repository.deleteCustomExchange(it.id) } },
            addLabel = "Add custom exchange",
            onAdd = { showExchangePicker = false; showCustomExchange = true }
        )
    }

    if (showLotSizePicker) {
        CatalogPickerDialog(
            title = "Select Lot Size",
            searchPlaceholder = "Search lot size",
            items = lotSizes,
            itemText = { it.value.formatNumber() },
            subtitle = { if (it.isDefault) "Journal default" else "Custom" },
            isDefault = { it.isDefault },
            onDismiss = { showLotSizePicker = false },
            onSelect = { lotSizeText = it.value.formatNumber(); showLotSizePicker = false },
            onDelete = { scope.launch { repository.deleteCustomLotSize(it.id) } },
            addLabel = "Add custom lot size",
            onAdd = { showLotSizePicker = false; showCustomLotSize = true }
        )
    }

    if (showExpiryPicker) {
        val initial = runCatching { LocalDate.parse(expiry) }.getOrElse { LocalDate.now() }
        val state = rememberDatePickerState(initialSelectedDateMillis = initial.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showExpiryPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { expiry = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toString() }
                    showExpiryPicker = false
                }) { Text("Done") }
            },
            dismissButton = { TextButton(onClick = { showExpiryPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    if (showTradeDatePicker) {
        val initial = runCatching { LocalDate.parse(tradeDate) }.getOrElse { LocalDate.now() }
        val state = rememberDatePickerState(initialSelectedDateMillis = initial.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showTradeDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { tradeDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toString() }
                    showTradeDatePicker = false
                }) { Text("Done") }
            },
            dismissButton = { TextButton(onClick = { showTradeDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    if (showCustomSymbol) {
        CustomSymbolDialog(
            exchanges = exchanges,
            onDismiss = { showCustomSymbol = false },
            onAdd = { newSymbol, ex ->
                scope.launch {
                    val cleanedSymbol = newSymbol.trim().uppercase(Locale.ENGLISH)
                    val cleanedExchange = ex.trim().uppercase(Locale.ENGLISH)
                    val id = repository.addSymbol(cleanedSymbol, cleanedExchange)
                    if (id > 0) { symbol = cleanedSymbol; exchange = cleanedExchange }
                    showCustomSymbol = false
                }
            },
            onAddExchange = { showCustomSymbol = false; showCustomExchange = true }
        )
    }

    if (showCustomExchange) {
        CustomExchangeDialog(
            onDismiss = { showCustomExchange = false },
            onAdd = { code, name ->
                scope.launch {
                    val cleaned = code.trim().uppercase(Locale.ENGLISH)
                    val id = repository.addExchange(cleaned, name.trim())
                    if (id > 0) exchange = cleaned
                    showCustomExchange = false
                }
            }
        )
    }

    if (showCustomLotSize) {
        CustomLotSizeDialog(
            onDismiss = { showCustomLotSize = false },
            onAdd = { value ->
                scope.launch {
                    val id = repository.addLotSize(value)
                    if (id > 0) lotSizeText = value.formatNumber()
                    showCustomLotSize = false
                }
            }
        )
    }
}

@Composable
private fun TwoColumn(content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), content = content)
}

@Composable
private fun InstrumentSelector(value: InstrumentType, onChange: (InstrumentType) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        InstrumentType.values().forEachIndexed { index, type ->
            SegmentedButton(
                selected = value == type,
                onClick = { onChange(type) },
                shape = SegmentedButtonDefaults.itemShape(index, InstrumentType.values().size)
            ) { Text(type.label()) }
        }
    }
}

@Composable
private fun BuySellSelector(direction: TradeDirection, onDirectionChange: (TradeDirection) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DirectionButton("Buy", direction == TradeDirection.BUY, MaterialTheme.colorScheme.primary, { onDirectionChange(TradeDirection.BUY) })
        DirectionButton("Sell", direction == TradeDirection.SELL, MaterialTheme.colorScheme.error, { onDirectionChange(TradeDirection.SELL) })
    }
}

@Composable
private fun DirectionButton(label: String, selected: Boolean, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color), modifier = Modifier.width(120.dp)) {
            Icon(Icons.Default.Check, null)
            Spacer(Modifier.width(6.dp))
            Text(label)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.width(120.dp)) { Text(label) }
    }
}

@Composable
private fun CallPutSelector(optionType: OptionType, onChange: (OptionType) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = optionType == OptionType.CALL, onClick = { onChange(OptionType.CALL) }, label = { Text("Call") })
        FilterChip(selected = optionType == OptionType.PUT, onClick = { onChange(OptionType.PUT) }, label = { Text("Put") })
    }
}

@Composable
private fun NumericField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier, supporting: String? = null) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val filtered = buildString {
                var dot = false
                input.forEach { c ->
                    when {
                        c.isDigit() -> append(c)
                        c == '.' && !dot -> { append(c); dot = true }
                    }
                }
            }
            onValueChange(filtered)
        },
        modifier = modifier,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        supportingText = supporting?.let { { Text(it) } }
    )
}

@Composable
private fun TextFieldWide(label: String, value: String, onValueChange: (String) -> Unit, singleLine: Boolean = true, minLines: Int = 1) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines
    )
}

@Composable
private fun SelectorField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.ExpandMore
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(3.dp))
                Text(value, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
            }
            Icon(trailingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ExchangeInputField(value: String, onValueChange: (String) -> Unit, onSelect: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Exchange") },
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = onSelect) { Icon(Icons.Default.ExpandMore, "Select exchange") }
        },
        supportingText = { Text("Type an exchange code or use the selector") }
    )
}

@Composable
private fun SectionDivider() { HorizontalDivider(Modifier.padding(vertical = 2.dp)) }

@Composable
private fun <T> CatalogPickerDialog(
    title: String,
    searchPlaceholder: String,
    items: List<T>,
    itemText: (T) -> String,
    subtitle: (T) -> String,
    isDefault: (T) -> Boolean,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
    onDelete: (T) -> Unit,
    addLabel: String,
    onAdd: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<T?>(null) }
    val filtered = items.filter { itemText(it).contains(search, true) || subtitle(it).contains(search, true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text(searchPlaceholder) },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 400.dp)) {
                    items(filtered, key = { itemText(it) }) { item ->
                        ListItem(
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(item) },
                            headlineContent = { Text(itemText(item)) },
                            supportingContent = { Text(subtitle(item)) },
                            leadingContent = { if (isDefault(item)) Icon(Icons.Default.Check, null) else Icon(Icons.Default.Edit, null) },
                            trailingContent = {
                                if (!isDefault(item)) {
                                    IconButton(onClick = { pendingDelete = item }) { Icon(Icons.Default.Delete, "Delete custom entry") }
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                    item {
                        FilledTonalButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(6.dp))
                            Text(addLabel)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete custom entry?") },
            text = { Text("This removes it from the selector only. Existing trades are not deleted.") },
            confirmButton = {
                TextButton(onClick = { onDelete(item); pendingDelete = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CustomSymbolDialog(
    exchanges: List<SavedExchangeEntity>,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit,
    onAddExchange: () -> Unit
) {
    var symbol by remember { mutableStateOf("") }
    var exchange by remember { mutableStateOf(exchanges.firstOrNull()?.code ?: "NSE") }
    var openExchangeMenu by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add custom symbol") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(symbol, { symbol = it.uppercase(Locale.ENGLISH) }, Modifier.fillMaxWidth(), label = { Text("Symbol") }, singleLine = true)
                Box {
                    SelectorField("Exchange", exchange, { openExchangeMenu = true })
                    DropdownMenu(expanded = openExchangeMenu, onDismissRequest = { openExchangeMenu = false }) {
                        exchanges.forEach { ex ->
                            DropdownMenuItem(text = { Text("${ex.code} • ${ex.name}") }, onClick = { exchange = ex.code; openExchangeMenu = false })
                        }
                        DropdownMenuItem(leadingIcon = { Icon(Icons.Default.Add, null) }, text = { Text("Add custom exchange") }, onClick = { openExchangeMenu = false; onAddExchange() })
                    }
                }
            }
        },
        confirmButton = { TextButton(enabled = symbol.isNotBlank() && exchange.isNotBlank(), onClick = { onAdd(symbol, exchange) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun CustomExchangeDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add custom exchange") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(code, { code = it.uppercase(Locale.ENGLISH) }, Modifier.fillMaxWidth(), label = { Text("Exchange code") }, singleLine = true)
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Exchange name") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(enabled = code.isNotBlank() && name.isNotBlank(), onClick = { onAdd(code, name) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun CustomLotSizeDialog(onDismiss: () -> Unit, onAdd: (Double) -> Unit) {
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add custom lot size") },
        text = {
            OutlinedTextField(
                value,
                { value = it.filter { c -> c.isDigit() || c == '.' } },
                Modifier.fillMaxWidth(),
                label = { Text("Lot size") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        },
        confirmButton = { TextButton(enabled = value.toDoubleOrNull()?.let { it > 0 } == true, onClick = { onAdd(value.toDouble()) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun InstrumentType.label(): String = name.lowercase(Locale.ENGLISH).replaceFirstChar { it.uppercase(Locale.ENGLISH) }
private fun Double.formatNumber(): String = if (this % 1.0 == 0.0) this.toLong().toString() else "%.4f".format(Locale.ENGLISH, this).trimEnd('0').trimEnd('.')
private fun normalizeExpiry(value: String): String = runCatching { LocalDate.parse(value).toString() }.getOrElse { runCatching { LocalDate.parse(value, DISPLAY_DATE).toString() }.getOrDefault("") }
private fun expiryDisplay(iso: String): String = runCatching { LocalDate.parse(iso).format(DISPLAY_DATE) }.getOrDefault(iso)
private fun copyToInternal(uri: Uri, context: Context): String? = runCatching {
    val dir = File(context.filesDir, "trade_attachments").apply { mkdirs() }
    val file = File(dir, "chart_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(file).use { output -> input.copyTo(output) } }
    file.absolutePath
}.getOrNull()
