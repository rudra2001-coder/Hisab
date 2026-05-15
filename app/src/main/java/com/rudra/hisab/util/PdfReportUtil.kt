package com.rudra.hisab.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.rudra.hisab.data.local.entity.DailySnapshotEntity
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.local.entity.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReportData(
    val shopName: String,
    val startDate: String,
    val endDate: String,
    val totalSales: Double,
    val totalExpenses: Double,
    val totalPurchases: Double,
    val netProfit: Double,
    val cashReceived: Double,
    val creditGiven: Double,
    val saleCount: Int,
    val topProducts: List<Pair<String, Double>>,
    val dailyBreakdown: List<DailySnapshotEntity>,
    val generatedAt: Long = System.currentTimeMillis()
)

object PdfReportUtil {

    fun generateReport(context: Context, data: ReportData): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        val titlePaint = Paint().apply {
            textSize = 28f
            isFakeBoldText = true
            color = Color.DKGRAY
        }
        val headerPaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
            color = Color.DKGRAY
        }
        val bodyPaint = Paint().apply {
            textSize = 14f
            color = Color.DKGRAY
        }
        val greenPaint = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
            color = Color.parseColor("#2E7D32")
        }
        val redPaint = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
            color = Color.parseColor("#C62828")
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = 50f
        val leftMargin = 50f
        val rightMargin = 545f

        // Title
        canvas.drawText(data.shopName.ifEmpty { "Hisab" }, leftMargin, y, titlePaint)
        y += 30f
        canvas.drawText("রিপোর্ট: ${data.startDate} - ${data.endDate}", leftMargin, y, headerPaint)
        y += 25f
        canvas.drawText("তৈরি: ${dateFormat.format(Date(data.generatedAt))}", leftMargin, y, bodyPaint)
        y += 20f
        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
        y += 30f

        // Summary section
        canvas.drawText("সারসংক্ষেপ", leftMargin, y, headerPaint)
        y += 25f

        fun drawSummaryRow(label: String, amount: Double, paint: Paint) {
            canvas.drawText(label, leftMargin + 10f, y, bodyPaint)
            canvas.drawText("৳${CurrencyFormatter.format(amount)}", rightMargin - 100f, y, paint)
            y += 22f
        }

        drawSummaryRow("মোট বিক্রয়", data.totalSales, greenPaint)
        drawSummaryRow("নগদ প্রাপ্তি", data.cashReceived, greenPaint)
        drawSummaryRow("বাকি বিক্রয়", data.creditGiven, redPaint)
        drawSummaryRow("মোট খরচ", data.totalExpenses, redPaint)
        drawSummaryRow("ক্রয়", data.totalPurchases, redPaint)

        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
        y += 8f
        val profitPaint = if (data.netProfit >= 0) greenPaint else redPaint
        canvas.drawText("নিট মুনাফা", leftMargin + 10f, y + 18f, headerPaint)
        canvas.drawText("৳${CurrencyFormatter.format(data.netProfit)}", rightMargin - 100f, y + 18f, profitPaint)
        y += 35f

        canvas.drawText("মোট লেনদেন: ${data.saleCount} টি", leftMargin, y, bodyPaint)
        y += 30f

        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
        y += 25f

        // Top products
        if (data.topProducts.isNotEmpty()) {
            canvas.drawText("সেরা পণ্য", leftMargin, y, headerPaint)
            y += 25f
            data.topProducts.forEachIndexed { i, (name, revenue) ->
                canvas.drawText("${i + 1}. $name", leftMargin + 10f, y, bodyPaint)
                canvas.drawText("৳${CurrencyFormatter.format(revenue)}", rightMargin - 100f, y, greenPaint)
                y += 22f
            }
            y += 15f
            canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
            y += 25f
        }

        // Daily breakdown
        if (data.dailyBreakdown.isNotEmpty()) {
            canvas.drawText("দৈনিক বিবরণ", leftMargin, y, headerPaint)
            y += 25f

            canvas.drawText("তারিখ", leftMargin + 10f, y, bodyPaint)
            canvas.drawText("বিক্রয়", rightMargin - 180f, y, bodyPaint)
            canvas.drawText("খরচ", rightMargin - 100f, y, bodyPaint)
            y += 5f
            canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
            y += 20f

            data.dailyBreakdown.sortedBy { it.date }.forEach { snapshot ->
                val date = dateFormat.format(Date(snapshot.date))
                canvas.drawText(date, leftMargin + 10f, y, bodyPaint)
                canvas.drawText("৳${CurrencyFormatter.format(snapshot.totalSales)}", rightMargin - 180f, y, bodyPaint)
                canvas.drawText("৳${CurrencyFormatter.format(snapshot.totalExpenses)}", rightMargin - 100f, y, bodyPaint)
                y += 20f
            }
        }

        document.finishPage(page)

        val dir = File(context.cacheDir, "reports")
        dir.mkdirs()
        val file = File(dir, "hisab_report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    fun shareReport(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
    }
}
