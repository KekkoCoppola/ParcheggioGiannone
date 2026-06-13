package com.giannone.parcheggio.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Typeface
import android.graphics.RectF
import androidx.core.content.FileProvider
import com.giannone.parcheggio.ui.viewmodel.ResocontoState
import com.google.firebase.Timestamp
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    private const val PAGE_WIDTH = 595   // A4 width in points (72 dpi)
    private const val PAGE_HEIGHT = 842  // A4 height in points (72 dpi)
    private const val MARGIN = 48f
    private const val PRIMARY_COLOR = 0xFF1A2B4A.toInt()   // Blu scuro brand
    private const val ACCENT_COLOR = 0xFF2563EB.toInt()    // Blu accento
    private const val LIGHT_BG = 0xFFF1F5F9.toInt()        // Sfondo riga
    private const val DIVIDER_COLOR = 0xFFDDE3EE.toInt()   // Divisore

    fun generateAndShare(context: Context, resoconto: ResocontoState) {
        val pdfFile = generate(context, resoconto)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Ricevuta Parcheggio – ${resoconto.nomeCliente}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Condividi ricevuta"))
    }

    private fun generate(context: Context, resoconto: ResocontoState): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        var y = MARGIN

        // ── Header con logo ──────────────────────────────────────────
        val logoBitmap = loadLogo(context)
        val logoHeight = 60f
        val logoWidth = if (logoBitmap != null) {
            val ratio = logoBitmap.width.toFloat() / logoBitmap.height.toFloat()
            logoHeight * ratio
        } else 0f

        if (logoBitmap != null) {
            val srcRect = android.graphics.Rect(0, 0, logoBitmap.width, logoBitmap.height)
            val dstRect = RectF(MARGIN, y, MARGIN + logoWidth, y + logoHeight)
            canvas.drawBitmap(logoBitmap, srcRect, dstRect, null)
        }

        // Data e ora di emissione (allineato a destra)
        val dataPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
        }
        val now = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN).format(Date())
        canvas.drawText("Emesso il $now", PAGE_WIDTH.toFloat() - MARGIN, y + 14f, dataPaint)

        y += logoHeight + 24f

        // Linea separatore header
        drawDivider(canvas, y)
        y += 16f

        // ── Titolo documento ─────────────────────────────────────────
        val titlePaint = Paint().apply {
            color = PRIMARY_COLOR
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("RICEVUTA DI SOSTA", MARGIN, y, titlePaint)
        y += 34f

        // ── Card dati cliente ────────────────────────────────────────
        y = drawSection(canvas, y, "DATI CLIENTE")
        y = drawInfoRow(canvas, y, "Nominativo", resoconto.nomeCliente)
        y = drawInfoRow(canvas, y, "Targa", resoconto.targa, highlight = true)
        y = drawInfoRow(canvas, y, "Piano", resoconto.piano)
        y += 12f

        // ── Card dati sosta ──────────────────────────────────────────
        y = drawSection(canvas, y, "DETTAGLIO SOSTA")
        y = drawInfoRow(canvas, y, "Orario Ingresso", formatTs(resoconto.timestampIngresso))
        y = drawInfoRow(canvas, y, "Orario Uscita", formatTs(resoconto.timestampUscita))
        y = drawInfoRow(canvas, y, "Durata", String.format(Locale.ITALIAN, "%.1f ore", resoconto.totaleOre))
        y = drawInfoRow(canvas, y, "Tariffa", resoconto.tipoTariffa)
        y += 12f

        // ── Totale (box in evidenza) ─────────────────────────────────
        val boxTop = y
        val boxBottom = y + 60f
        val boxPaint = Paint().apply { color = ACCENT_COLOR }
        val boxRect = RectF(MARGIN, boxTop, PAGE_WIDTH - MARGIN, boxBottom)
        canvas.drawRoundRect(boxRect, 12f, 12f, boxPaint)

        val totaleLabelPaint = Paint().apply {
            color = 0xCCFFFFFF.toInt()
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("TOTALE DA PAGARE", MARGIN + 16f, boxTop + 22f, totaleLabelPaint)

        val totaleValuePaint = Paint().apply {
            color = Color.WHITE
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(
            "€ %.2f".format(resoconto.totalePagato),
            PAGE_WIDTH - MARGIN - 16f,
            boxTop + 40f,
            totaleValuePaint
        )
        y = boxBottom + 32f

        // ── Footer ───────────────────────────────────────────────────
        drawDivider(canvas, PAGE_HEIGHT - MARGIN - 20f)
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Parcheggio Giannone • Sicurezza • Comodità • 24/7",
            PAGE_WIDTH / 2f,
            PAGE_HEIGHT - MARGIN,
            footerPaint
        )

        doc.finishPage(page)

        // Salva in cache
        val cacheDir = File(context.cacheDir, "pdf").apply { mkdirs() }
        val file = File(cacheDir, "ricevuta_${resoconto.targa}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()

        return file
    }

    // ── Helpers di disegno ───────────────────────────────────────────

    private fun drawSection(canvas: Canvas, y: Float, title: String): Float {
        val paint = Paint().apply {
            color = PRIMARY_COLOR
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.12f
        }
        canvas.drawText(title, MARGIN, y, paint)
        return y + 18f
    }

    private fun drawInfoRow(
        canvas: Canvas,
        y: Float,
        label: String,
        value: String,
        highlight: Boolean = false
    ): Float {
        val rowHeight = 36f
        if (highlight) {
            val bgPaint = Paint().apply { color = LIGHT_BG }
            canvas.drawRoundRect(
                RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + rowHeight),
                8f, 8f, bgPaint
            )
        }

        val labelPaint = Paint().apply {
            color = Color.GRAY
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val valuePaint = Paint().apply {
            color = PRIMARY_COLOR
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val textY = y + rowHeight / 2f + 4f
        canvas.drawText(label, MARGIN + 8f, textY, labelPaint)
        canvas.drawText(value, PAGE_WIDTH - MARGIN - 8f, textY, valuePaint)

        val dividerPaint = Paint().apply { color = DIVIDER_COLOR }
        canvas.drawLine(MARGIN, y + rowHeight, PAGE_WIDTH - MARGIN, y + rowHeight, dividerPaint)

        return y + rowHeight
    }

    private fun drawDivider(canvas: Canvas, y: Float) {
        val paint = Paint().apply {
            color = DIVIDER_COLOR
            strokeWidth = 1.5f
        }
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
    }

    private fun formatTs(ts: Timestamp?): String {
        if (ts == null) return "--"
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN).format(ts.toDate())
    }

    private fun loadLogo(context: Context): Bitmap? {
        return try {
            context.assets.open("logo_full.png").use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }
}
