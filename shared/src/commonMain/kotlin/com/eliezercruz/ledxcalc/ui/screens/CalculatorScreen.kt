package com.eliezercruz.ledxcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eliezercruz.ledxcalc.data.AppRepository
import com.eliezercruz.ledxcalc.domain.BreakerRating
import com.eliezercruz.ledxcalc.domain.CalculationHistoryEntry
import com.eliezercruz.ledxcalc.domain.LedCalculator
import com.eliezercruz.ledxcalc.domain.MeasurementUnit
import com.eliezercruz.ledxcalc.domain.ModuleCatalog
import com.eliezercruz.ledxcalc.domain.ModulePhysicalCategory
import com.eliezercruz.ledxcalc.domain.ModuleSpec
import com.eliezercruz.ledxcalc.domain.SketchDefaults
import com.eliezercruz.ledxcalc.domain.SketchKind
import com.eliezercruz.ledxcalc.domain.SketchSelection
import com.eliezercruz.ledxcalc.domain.StructureMounting
import com.eliezercruz.ledxcalc.domain.SupplyVoltage
import com.eliezercruz.ledxcalc.platform.PlatformContext
import com.eliezercruz.ledxcalc.platform.closeApp
import com.eliezercruz.ledxcalc.platform.previewPdf
import com.eliezercruz.ledxcalc.platform.sharePdf
import com.eliezercruz.ledxcalc.ui.components.BasesDistributionList
import com.eliezercruz.ledxcalc.ui.components.Button3D
import com.eliezercruz.ledxcalc.ui.components.ElectricalPowerCard
import com.eliezercruz.ledxcalc.ui.components.LedPanel
import com.eliezercruz.ledxcalc.ui.components.LedTitleBar
import com.eliezercruz.ledxcalc.ui.components.ModuleDropdownSelector
import com.eliezercruz.ledxcalc.ui.components.SketchOptionsSelector
import com.eliezercruz.ledxcalc.ui.components.StructureMountingSelector
import com.eliezercruz.ledxcalc.ui.sketches.BasesSketch
import com.eliezercruz.ledxcalc.ui.sketches.HoleSketch
import com.eliezercruz.ledxcalc.ui.sketches.ModuleSketch
import com.eliezercruz.ledxcalc.ui.sketches.SketchSection
import com.eliezercruz.ledxcalc.ui.sketches.TrussSketch
import com.eliezercruz.ledxcalc.ui.formatUiText
import com.eliezercruz.ledxcalc.ui.rememberKeyboardDismissHandler
import com.eliezercruz.ledxcalc.ui.theme.LedColors
import com.eliezercruz.ledxcalc.resources.Res
import com.eliezercruz.ledxcalc.resources.logo_calculadora
import com.eliezercruz.ledxcalc.resources.logo_g
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.Image
import com.eliezercruz.ledxcalc.util.currentTimeMillis
import kotlin.random.Random

@Composable
fun ResolutionCalculatorScreen(
    repository: AppRepository,
    platformContext: PlatformContext,
    modifier: Modifier = Modifier
) {
    var widthInput by rememberSaveable { mutableStateOf("") }
    var heightInput by rememberSaveable { mutableStateOf("") }
    var ghostModulesInput by rememberSaveable { mutableStateOf("") }
    var selectedUnitKey by rememberSaveable { mutableStateOf(MeasurementUnit.METERS.name) }
    var selectedCategoryKey by rememberSaveable { mutableStateOf(ModulePhysicalCategory.SIZE_500x500.name) }
    var selectedModuleId by rememberSaveable { mutableStateOf(ModuleCatalog.defaultModuleId(ModulePhysicalCategory.SIZE_500x500)) }
    var customSpec by remember { mutableStateOf<ModuleSpec?>(null) }
    var useCustomModule by rememberSaveable { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showSavedModules by remember { mutableStateOf(false) }
    var savedModules by remember { mutableStateOf(repository.loadSavedModules()) }
    var history by remember { mutableStateOf(repository.loadHistory()) }
    var structureMountingKey by rememberSaveable { mutableStateOf(StructureMounting.FLOOR_BASES.name) }
    var sketchEnabledKeys by rememberSaveable {
        mutableStateOf(SketchSelection.encodeEnabled(SketchDefaults.forMounting(StructureMounting.FLOOR_BASES)))
    }

    val sketchSelection = SketchSelection.decode(structureMountingKey, sketchEnabledKeys)

    val selectedUnit = MeasurementUnit.valueOf(selectedUnitKey)
    val selectedCategory = ModulePhysicalCategory.valueOf(selectedCategoryKey)
    val widthMeters = widthInput.toDoubleOrNull()?.let { LedCalculator.convertToMeters(selectedUnit, it) }
    val heightMeters = heightInput.toDoubleOrNull()?.let { LedCalculator.convertToMeters(selectedUnit, it) }
    val ghostModules = ghostModulesInput.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val catalogModule = ModuleCatalog.findById(selectedModuleId)
        ?: ModuleCatalog.forCategory(selectedCategory).firstOrNull()
    val activeSpec = if (useCustomModule) customSpec else catalogModule
    val dismissKeyboard = rememberKeyboardDismissHandler()
    val scrollState = rememberScrollState()
    val heightFocusRequester = remember { FocusRequester() }
    val dismissKeyboardOnScroll = remember(dismissKeyboard) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available != Offset.Zero) {
                    dismissKeyboard()
                }
                return Offset.Zero
            }
        }
    }
    val hasValidDimensions = widthMeters != null && heightMeters != null && activeSpec != null

    LaunchedEffect(hasValidDimensions) {
        if (hasValidDimensions) {
            dismissKeyboard()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(dismissKeyboardOnScroll)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(Res.drawable.logo_calculadora),
                contentDescription = "Logo",
                modifier = Modifier.height(80.dp).shadow(16.dp)
            )
        }

        LedTitleBar("LedxCalc — Calculadora LED", accentColor = LedColors.NeonCyan)

        if (!useCustomModule) {
            ModuleDropdownSelector(
                selectedCategory = selectedCategory,
                selectedModule = catalogModule,
                onCategorySelected = { category ->
                    dismissKeyboard()
                    selectedCategoryKey = category.name
                    selectedModuleId = ModuleCatalog.defaultModuleId(category)
                },
                onModuleSelected = { module ->
                    dismissKeyboard()
                    selectedModuleId = module.id
                    useCustomModule = false
                }
            )
        } else {
            customSpec?.let { custom ->
                Text(
                    text = formatUiText("📐 Módulo personalizado: ${custom.dropdownLabel}"),
                    color = LedColors.NeonCyan,
                    modifier = Modifier.fillMaxWidth()
                        .background(LedColors.Panel.copy(alpha = 0.9f), MaterialTheme.shapes.medium)
                        .border(1.dp, LedColors.NeonCyan.copy(alpha = 0.35f), MaterialTheme.shapes.medium)
                        .padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                Button3D("➕ Crear Módulo", true) { showCreateDialog = true }
                if (useCustomModule) {
                    Button3D("📋 Catálogo", false) { useCustomModule = false }
                }
                Button3D("💾 Guardados", false) { showSavedModules = true }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button3D("📋 Historial", false) { showHistory = true }
            }
        }

        UnitSelector(selectedUnit) {
            dismissKeyboard()
            selectedUnitKey = it.name
        }

        LedPanel(accentColor = LedColors.NeonBlue) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InputField(
                    label = "📏 Ancho (${selectedUnit.shortLabel})",
                    value = widthInput,
                    onValueChange = { widthInput = LedCalculator.decimalSanitized(it) },
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next,
                    onImeNext = { heightFocusRequester.requestFocus() }
                )
                Text("×", style = MaterialTheme.typography.titleLarge, color = LedColors.NeonCyan)
                InputField(
                    label = "📐 Alto (${selectedUnit.shortLabel})",
                    value = heightInput,
                    onValueChange = { heightInput = LedCalculator.decimalSanitized(it) },
                    modifier = Modifier.weight(1f).focusRequester(heightFocusRequester),
                    imeAction = ImeAction.Done
                )
            }
        }

        LedPanel(accentColor = LedColors.NeonPurple) {
            InputField(
                label = "Módulos fantasma (opcional, bajo pantalla)",
                value = ghostModulesInput,
                onValueChange = { ghostModulesInput = LedCalculator.integerSanitized(it) },
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number
            )
            Text(
                text = "Espacio vacío de 1–3 gabinetes bajo la pantalla LED. Afecta bases, escaleras y cobertura de montaje.",
                color = LedColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }

        if (activeSpec != null && widthMeters != null && heightMeters != null) {
            val result = LedCalculator.calculate(
                activeSpec, widthMeters, heightMeters, selectedUnit, ghostModules = ghostModules
            )
            if (result != null) {
                ResolutionResults(
                    moduleSpec = activeSpec,
                    result = result,
                    selectedUnit = selectedUnit,
                    widthInput = widthInput,
                    heightInput = heightInput,
                    widthMeters = widthMeters,
                    heightMeters = heightMeters,
                    platformContext = platformContext,
                    sketchSelection = sketchSelection,
                    onSketchSelectionChange = { updated ->
                        sketchEnabledKeys = SketchSelection.encodeEnabled(updated.enabled)
                        structureMountingKey = updated.mounting.name
                    },
                    onStructureMountingChange = { mounting ->
                        structureMountingKey = mounting.name
                        sketchEnabledKeys = SketchSelection.encodeEnabled(SketchDefaults.forMounting(mounting))
                    },
                    onSaveToHistory = {
                        val entry = CalculationHistoryEntry(
                            id = Random.nextLong().toString(),
                            timestampMillis = currentTimeMillis(),
                            widthInput = widthInput,
                            heightInput = heightInput,
                            unit = selectedUnit,
                            moduleTitle = activeSpec.title,
                            totalModules = result.totalModules,
                            resolution = "${result.widthPixels}×${result.heightPixels}"
                        )
                        repository.addHistoryEntry(entry)
                        history = repository.loadHistory()
                    }
                )
            }
        } else if (widthInput.isNotBlank() || heightInput.isNotBlank()) {
            Text(
                formatUiText("⚠️ Ingresa valores válidos para ancho y alto."),
                color = LedColors.TextSecondary,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.Center) {
            Button3D("🗑️ Limpiar", false) {
                dismissKeyboard()
                widthInput = ""
                heightInput = ""
                ghostModulesInput = ""
            }
            Spacer(Modifier.width(12.dp))
            Button3D("❌ Cerrar", false) { closeApp(platformContext) }
        }

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Image(painterResource(Res.drawable.logo_g), "Logo G", Modifier.height(50.dp))
        }
        Text(
            formatUiText("✨ Esta app fue creada por Eliezer Cruz ✨"),
            color = LedColors.Result.Footer,
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
    }

    if (showCreateDialog) {
        CreateModuleDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { spec ->
                customSpec = spec
                repository.saveModule(spec)
                savedModules = repository.loadSavedModules()
                useCustomModule = true
                showCreateDialog = false
            }
        )
    }

    if (showHistory) {
        HistoryDialog(
            entries = history,
            onDismiss = { showHistory = false },
            onClear = {
                repository.clearHistory()
                history = emptyList()
            },
            onSelect = { entry ->
                widthInput = entry.widthInput
                heightInput = entry.heightInput
                selectedUnitKey = entry.unit.name
                showHistory = false
            }
        )
    }

    if (showSavedModules) {
        SavedModulesDialog(
            modules = savedModules,
            onDismiss = { showSavedModules = false },
            onSelect = { module ->
                customSpec = module
                useCustomModule = true
                showSavedModules = false
            },
            onDelete = { id ->
                repository.deleteModule(id)
                savedModules = repository.loadSavedModules()
                if (customSpec?.id == id) customSpec = null
            }
        )
    }
}

@Composable
private fun ResolutionResults(
    moduleSpec: ModuleSpec,
    result: com.eliezercruz.ledxcalc.domain.LedCalculationResult,
    selectedUnit: MeasurementUnit,
    widthInput: String,
    heightInput: String,
    widthMeters: Double,
    heightMeters: Double,
    platformContext: PlatformContext,
    sketchSelection: SketchSelection,
    onSketchSelectionChange: (SketchSelection) -> Unit,
    onStructureMountingChange: (StructureMounting) -> Unit,
    onSaveToHistory: () -> Unit
) {
    var pdfVoltageKey by rememberSaveable { mutableStateOf(SupplyVoltage.V110.name) }
    var pdfBreakerKey by rememberSaveable { mutableStateOf(BreakerRating.A20.name) }
    val pdfVoltage = SupplyVoltage.valueOf(pdfVoltageKey)
    val pdfBreaker = BreakerRating.valueOf(pdfBreakerKey)

    LedPanel(accentColor = LedColors.NeonMagenta, glowElevation = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(formatUiText("🔲 ${moduleSpec.title}"), color = LedColors.NeonCyan, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            if (moduleSpec.pitch > 0) {
                ResultLine("📐 Gabinete: ${moduleSpec.physicalLabel} · Pitch ${moduleSpec.pitch}", LedColors.NeonTeal, MaterialTheme.typography.bodyMedium)
            }

            ResultLine(
                "📏 Unidad: ${selectedUnit.displayName} · Entrada: $widthInput × $heightInput ${selectedUnit.shortLabel}",
                LedColors.Result.Input,
                MaterialTheme.typography.bodyLarge
            )
            ResultLine(
                "📺 Resolución: ${result.widthPixels} × ${result.heightPixels} px",
                LedColors.Result.Resolution,
                MaterialTheme.typography.headlineSmall
            )
            ResultLine(
                "✨ Total gabinetes: ${result.totalModules}",
                LedColors.Result.Total,
                MaterialTheme.typography.headlineSmall
            )
            ResultLine("↔ Columnas: ${result.modulesAcross}", LedColors.Result.Total, MaterialTheme.typography.titleMedium)
            ResultLine("↕ Filas LED: ${result.modulesHigh}", LedColors.Result.Total, MaterialTheme.typography.titleMedium)
            if (result.ghostModules > 0) {
                ResultLine(
                    "Módulos fantasma (bajo pantalla): ${result.ghostModules}",
                    LedColors.NeonPurple,
                    MaterialTheme.typography.titleMedium
                )
                ResultLine(
                    "Estructura montaje: ${result.modulesAcross} × ${result.structureModulesHigh} gabinetes",
                    LedColors.BasesAccent,
                    MaterialTheme.typography.bodyMedium
                )
            }
            ResultLine("📐 Cobertura: ${result.displayWidth} × ${result.displayHeight} ${result.displayUnitLabel}", LedColors.Result.Coverage)
            ResultLine("🕳️ Hueco de pantalla: ${result.holeWidthFormatted} × ${result.holeHeightFormatted} ft", LedColors.Result.Hole)
            ResultLine("con paneles", LedColors.Result.Hole, MaterialTheme.typography.bodyMedium)
            ResultLine("🌐 Líneas de señal: ${result.signalLinesNeeded} (cada ${result.groupSizeForSpec} módulos)", LedColors.Result.Signal)

            SketchSection("🖥️ Boceto de pantalla") {
                ModuleSketch(
                    columns = result.modulesAcross,
                    rows = result.modulesHigh,
                    groupSize = result.groupSizeForSpec,
                    isLarge = true
                )
            }

            StructureMountingSelector(
                selected = sketchSelection.mounting,
                onSelect = onStructureMountingChange
            )

            if (sketchSelection.mounting == StructureMounting.FLOOR_BASES) {
                SketchSection("🏗️ Bases de piso y escaleras") {
                    BasesDistributionList(
                        support = result.supportCalc,
                        ghostModules = result.ghostModules
                    )
                    BasesSketch(
                        modulesAcross = result.modulesAcross,
                        modulesHigh = result.structureModulesHigh,
                        ledModulesHigh = result.modulesHigh,
                        ghostModules = result.ghostModules,
                        support = result.supportCalc
                    )
                }
                SketchSection("🕳️ Hueco de pantalla") {
                    HoleSketch(
                        screenWidthFeet = result.screenWidthFeet,
                        screenHeightFeet = result.screenHeightFeet,
                        holeWidthFeet = result.holeWidthFeet,
                        holeHeightFeet = result.holeHeightFeet,
                        holeWidthLabel = result.holeWidthFormatted,
                        holeHeightLabel = result.holeHeightFormatted,
                        modulesAcross = result.modulesAcross,
                        modulesHigh = result.modulesHigh
                    )
                }
            } else {
                SketchSection("🔩 Estructura truss") {
                    TrussSketch(
                        trussWidthFeet = result.trussWidthFeet,
                        trussHeightFeet = result.trussHeightFeet,
                        screenWidthFeet = result.screenWidthFeet,
                        screenHeightFeet = result.screenHeightFeet,
                        modulesAcross = result.modulesAcross,
                        modulesHigh = result.modulesHigh
                    )
                }
                SketchSection("🕳️ Hueco de pantalla") {
                    HoleSketch(
                        screenWidthFeet = result.screenWidthFeet,
                        screenHeightFeet = result.screenHeightFeet,
                        holeWidthFeet = result.holeWidthFeet,
                        holeHeightFeet = result.holeHeightFeet,
                        holeWidthLabel = result.holeWidthFormatted,
                        holeHeightLabel = result.holeHeightFormatted,
                        modulesAcross = result.modulesAcross,
                        modulesHigh = result.modulesHigh
                    )
                }
            }

            ElectricalPowerCard(
                modelLabel = moduleSpec.model.ifBlank { moduleSpec.title },
                totalGabinetes = result.totalModules,
                wattsPromedioPorGabinete = result.wattsPerModule,
                wattsMaxPorGabinete = result.wattsPerModuleMax,
                selectedVoltage = pdfVoltage,
                onVoltageChange = { pdfVoltageKey = it.name },
                selectedBreaker = pdfBreaker,
                onBreakerChange = { pdfBreakerKey = it.name }
            )

            if (result.electricalNote.isNotBlank()) {
                ResultLine("ℹ️ ${result.electricalNote}", LedColors.Result.Note, MaterialTheme.typography.bodySmall)
            }

            SketchOptionsSelector(
                selection = sketchSelection,
                onSelectionChange = onSketchSelectionChange
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button3D("💾 Guardar en historial", true, onClick = onSaveToHistory)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                val pdfData = LedCalculator.toPdfExportData(
                    moduleSpec = moduleSpec,
                    result = result,
                    widthMeters = widthMeters,
                    heightMeters = heightMeters,
                    voltage = pdfVoltage,
                    breaker = pdfBreaker,
                    sketchSelection = sketchSelection
                )
                Button3D("👁️ Vista Previa PDF", true) { previewPdf(platformContext, pdfData) }
                Spacer(Modifier.width(12.dp))
                Button3D("📄 Compartir PDF", true) { sharePdf(platformContext, pdfData) }
            }
        }
    }
}

@Composable
private fun ResultLine(text: String, color: Color = LedColors.Result.Default, style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium) {
    Text(formatUiText(text), style = style, color = color, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), textAlign = TextAlign.Center)
}

@Composable
private fun UnitSelector(selectedUnit: MeasurementUnit, onSelect: (MeasurementUnit) -> Unit) {
    LedPanel(accentColor = LedColors.NeonGreen) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Unidades:", color = LedColors.TextPrimary)
            Button3D("Metros", selectedUnit == MeasurementUnit.METERS) { onSelect(MeasurementUnit.METERS) }
            Button3D("Pies", selectedUnit == MeasurementUnit.FEET) { onSelect(MeasurementUnit.FEET) }
        }
    }
}

@Composable
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
    onImeNext: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    val dismissKeyboard = rememberKeyboardDismissHandler()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(formatUiText(label), color = LedColors.TextSecondary) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { dismissKeyboard() },
            onNext = { onImeNext?.invoke() ?: dismissKeyboard() }
        ),
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = LedColors.Panel.copy(alpha = 0.9f),
            unfocusedContainerColor = LedColors.Panel.copy(alpha = 0.7f),
            focusedTextColor = LedColors.TextPrimary,
            unfocusedTextColor = LedColors.TextPrimary,
            focusedIndicatorColor = LedColors.NeonCyan,
            unfocusedIndicatorColor = LedColors.GridLine,
            cursorColor = LedColors.NeonCyan
        )
    )
}

@Composable
private fun CreateModuleDialog(onDismiss: () -> Unit, onConfirm: (ModuleSpec) -> Unit) {
    var wPx by remember { mutableStateOf("") }
    var hPx by remember { mutableStateOf("") }
    var wM by remember { mutableStateOf("") }
    var hM by remember { mutableStateOf("") }
    var modulesPerLine by remember { mutableStateOf("") }
    var watts by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear módulo personalizado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InputField("Ancho (px)", wPx, { wPx = it.filter { c -> c.isDigit() } })
                InputField("Alto (px)", hPx, { hPx = it.filter { c -> c.isDigit() } })
                InputField("Ancho (m)", wM, { wM = LedCalculator.decimalSanitized(it) })
                InputField("Alto (m)", hM, { hM = LedCalculator.decimalSanitized(it) })
                InputField("Módulos/línea señal", modulesPerLine, { modulesPerLine = it.filter { c -> c.isDigit() } })
                InputField("Watts por módulo", watts, { watts = LedCalculator.decimalSanitized(it) })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val spec = ModuleSpec(
                    title = "Módulo ${wPx}x${hPx} px (${wM}×${hM} m)",
                    widthPx = wPx.toIntOrNull() ?: return@TextButton,
                    heightPx = hPx.toIntOrNull() ?: return@TextButton,
                    widthMeters = wM.toDoubleOrNull() ?: return@TextButton,
                    heightMeters = hM.toDoubleOrNull() ?: return@TextButton,
                    modulesPerSignalLine = modulesPerLine.toIntOrNull() ?: 36,
                    wattsPerModule = watts.toDoubleOrNull() ?: 0.0,
                    isCustom = true
                )
                onConfirm(spec)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun HistoryDialog(
    entries: List<CalculationHistoryEntry>,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onSelect: (CalculationHistoryEntry) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Historial de cálculos") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (entries.isEmpty()) Text("Sin cálculos guardados.")
                entries.forEach { entry ->
                    TextButton(onClick = { onSelect(entry) }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "${entry.widthInput}×${entry.heightInput} ${entry.unit.shortLabel} — ${entry.moduleTitle}\n${entry.totalModules} módulos, ${entry.resolution} px",
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClear) { Text("Limpiar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun SavedModulesDialog(
    modules: List<ModuleSpec>,
    onDismiss: () -> Unit,
    onSelect: (ModuleSpec) -> Unit,
    onDelete: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Módulos guardados") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (modules.isEmpty()) Text("No hay módulos guardados.")
                modules.forEach { module ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { onSelect(module) }) { Text(module.title, textAlign = TextAlign.Start) }
                        TextButton(onClick = { onDelete(module.id) }) { Text(formatUiText("🗑")) }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}
