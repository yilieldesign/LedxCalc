package com.eliezercruz.ledxcalc.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eliezercruz.ledxcalc.domain.PdfExportData

expect class PlatformContext

@Composable
expect fun VideoBackground(modifier: Modifier = Modifier)

expect fun sharePdf(context: PlatformContext, data: PdfExportData)

expect fun previewPdf(context: PlatformContext, data: PdfExportData)

expect fun closeApp(context: PlatformContext)

expect fun createPlatformContext(): PlatformContext
