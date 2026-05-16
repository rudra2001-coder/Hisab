package com.rudra.hisab.ui.screens.reports

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.hisab.data.local.entity.ExpenseCategory
import com.rudra.hisab.data.preferences.AppPreferences
import com.rudra.hisab.data.repository.CustomerRepository
import com.rudra.hisab.data.repository.DailySnapshotRepository
import com.rudra.hisab.data.repository.ExpenseRepository
import com.rudra.hisab.data.repository.ProductRepository
import com.rudra.hisab.data.repository.TransactionRepository
import com.rudra.hisab.util.CurrencyFormatter
import com.rudra.hisab.util.ExportFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SalesReportData(
    val totalSales: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalDue: Double = 0.0,
    val saleCount: Int = 0,
    val avgSaleValue: Double = 0.0
)

data class ExpenseReportData(
    val totalExpenses: Double = 0.0,
    val breakdown: Map<ExpenseCategory, Double> = emptyMap()
)

data class ProfitLossData(
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMargin: Double = 0.0
)

data class ReportsUiState(
    val startDate: Long = 0L,
    val endDate: Long = System.currentTimeMillis(),
    val salesReport: SalesReportData = SalesReportData(),
    val expenseReport: ExpenseReportData = ExpenseReportData(),
    val profitLoss: ProfitLossData = ProfitLossData(),
    val totalDues: Double = 0.0,
    val dueCustomerCount: Int = 0,
    val lowStockCount: Int = 0,
    val totalStockValue: Double = 0.0,
    val isLoading: Boolean = false,
    val selectedFormat: ExportFormat = ExportFormat.CSV,
    val isExporting: Boolean = false,
    val isBangla: Boolean = true
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val expenseRepository: ExpenseRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val dailySnapshotRepository: DailySnapshotRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = appPreferences.settings.first()
            _uiState.value = _uiState.value.copy(isBangla = settings.languageCode == "bn")
        }
    }

    fun setDateRange(startDate: Long, endDate: Long) {
        _uiState.value = _uiState.value.copy(startDate = startDate, endDate = endDate)
        loadReports()
    }

    private fun loadReports() {
        val start = _uiState.value.startDate
        val end = _uiState.value.endDate
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            // Sales — suspend snapshot from transactions table
            val totalSales = transactionRepository.getTodaySalesTotal(start, end)
            val saleCount = transactionRepository.getTodaySaleCount(start, end)
            val totalDueInRange = transactionRepository.getTotalDueInRange(start, end)

            // Expenses — collect once
            val expenseList = expenseRepository.getExpensesByDateRange(start, end)
                .first()   // take the latest emitted list, then return
            val totalExpenses = expenseList.sumOf { it.amount }
            val breakdown = expenseList.groupBy { it.categoryId }
                .mapValues { (_, list) -> list.sumOf { it.amount } }

            val netProfit = totalSales - totalExpenses
            val margin = if (totalSales > 0) (netProfit / totalSales) * 100 else 0.0

            // Customer dues — suspend snapshot
            val allCustomers = customerRepository.getAllCustomers().first()
            val dues = allCustomers.sumOf { it.totalDue }
            val dueCount = allCustomers.count { it.totalDue > 0 }

            // Low stock — suspend snapshot
            val lowStock = productRepository.getLowStockProductsOnce()
            val stockValue = productRepository.getTotalStockValue().first() ?: 0.0

            _uiState.value = _uiState.value.copy(
                salesReport = SalesReportData(
                    totalSales = totalSales,
                    totalPaid = totalSales - totalDueInRange,
                    totalDue = totalDueInRange,
                    saleCount = saleCount,
                    avgSaleValue = if (saleCount > 0) totalSales / saleCount else 0.0
                ),
                expenseReport = ExpenseReportData(
                    totalExpenses = totalExpenses,
                    breakdown = breakdown
                ),
                profitLoss = ProfitLossData(
                    totalRevenue = totalSales,
                    totalExpenses = totalExpenses,
                    netProfit = netProfit,
                    profitMargin = margin
                ),
                totalDues = dues,
                dueCustomerCount = dueCount,
                lowStockCount = lowStock.size,
                totalStockValue = stockValue,
                isLoading = false
            )
        }
    }

    fun setFormat(format: ExportFormat) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }

    fun exportData(context: Context) {
        val state = _uiState.value
        _uiState.value = state.copy(isExporting = true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = when (state.selectedFormat) {
                    ExportFormat.CSV -> generateCsv(context)
                    ExportFormat.EXCEL -> generateExcel(context)
                    ExportFormat.PDF -> generatePdf(context)
                }
                shareFile(context, file)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.value = _uiState.value.copy(isExporting = false)
            }
        }
    }

    private fun generateCsv(context: Context): File {
        val state = _uiState.value
        val dir = File(context.cacheDir, "exports").also { it.mkdirs() }
        val file = File(dir, "report_${System.currentTimeMillis()}.csv")
        FileWriter(file).use { writer ->
            writer.write("Metric,Value\n")
            writer.write("Total Sales,${state.salesReport.totalSales}\n")
            writer.write("Total Paid,${state.salesReport.totalPaid}\n")
            writer.write("Total Due,${state.salesReport.totalDue}\n")
            writer.write("Sale Count,${state.salesReport.saleCount}\n")
            writer.write("Avg Sale Value,${state.salesReport.avgSaleValue}\n")
            writer.write("Total Expenses,${state.expenseReport.totalExpenses}\n")
            writer.write("Total Revenue,${state.profitLoss.totalRevenue}\n")
            writer.write("Net Profit,${state.profitLoss.netProfit}\n")
            writer.write("Profit Margin (%),${state.profitLoss.profitMargin}\n")
            writer.write("Total Dues,${state.totalDues}\n")
            writer.write("Due Customers,${state.dueCustomerCount}\n")
            writer.write("Low Stock Items,${state.lowStockCount}\n")
            writer.write("Total Stock Value,${state.totalStockValue}\n")
        }
        return file
    }

    private fun generateExcel(context: Context): File {
        val state = _uiState.value
        val dir = File(context.cacheDir, "exports").also { it.mkdirs() }
        val file = File(dir, "report_${System.currentTimeMillis()}.xlsx")
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet("Report")
        val headerStyle = wb.createCellStyle().apply {
            setFillForegroundColor(IndexedColors.DARK_BLUE.index)
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            borderBottom = BorderStyle.THIN
        }
        val headerFont = wb.createFont().apply { color = IndexedColors.WHITE.index; bold = true }
        headerStyle.setFont(headerFont)
        val dataStyle = wb.createCellStyle().apply {
            alignment = HorizontalAlignment.LEFT
            borderBottom = BorderStyle.THIN
        }
        listOf("Metric", "Value").forEachIndexed { i, h ->
            sheet.createRow(0).createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
        }
        listOf(
            "Total Sales" to state.salesReport.totalSales,
            "Total Paid" to state.salesReport.totalPaid,
            "Total Due" to state.salesReport.totalDue,
            "Sale Count" to state.salesReport.saleCount.toDouble(),
            "Avg Sale Value" to state.salesReport.avgSaleValue,
            "Total Expenses" to state.expenseReport.totalExpenses,
            "Total Revenue" to state.profitLoss.totalRevenue,
            "Net Profit" to state.profitLoss.netProfit,
            "Profit Margin (%)" to state.profitLoss.profitMargin,
            "Total Dues" to state.totalDues,
            "Due Customers" to state.dueCustomerCount.toDouble(),
            "Low Stock Items" to state.lowStockCount.toDouble(),
            "Total Stock Value" to state.totalStockValue
        ).forEachIndexed { i, (label, value) ->
            val row = sheet.createRow(i + 1)
            row.createCell(0).apply { setCellValue(label); cellStyle = dataStyle }
            row.createCell(1).apply { setCellValue(value); cellStyle = dataStyle }
        }
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
        FileOutputStream(file).use { wb.write(it) }
        wb.close()
        return file
    }

    private fun generatePdf(context: Context): File {
        val state = _uiState.value
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply { textSize = 28f; isFakeBoldText = true; color = Color.DKGRAY }
        val headerPaint = Paint().apply { textSize = 18f; isFakeBoldText = true; color = Color.DKGRAY }
        val bodyPaint = Paint().apply { textSize = 14f; color = Color.DKGRAY }
        val greenPaint = Paint().apply { textSize = 16f; isFakeBoldText = true; color = Color.parseColor("#2E7D32") }
        val redPaint = Paint().apply { textSize = 16f; isFakeBoldText = true; color = Color.parseColor("#C62828") }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        var y = 50f
        val leftMargin = 50f
        val rightMargin = 545f
        canvas.drawText("Hisab Report", leftMargin, y, titlePaint)
        y += 30f
        canvas.drawText("${state.startDate} - ${state.endDate}", leftMargin, y, headerPaint)
        y += 20f
        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
        y += 30f
        canvas.drawText("Summary", leftMargin, y, headerPaint)
        y += 25f
        fun drawRow(label: String, amount: Double, paint: Paint) {
            canvas.drawText(label, leftMargin + 10f, y, bodyPaint)
            canvas.drawText("৳${CurrencyFormatter.format(amount)}", rightMargin - 100f, y, paint)
            y += 22f
        }
        drawRow("Total Sales", state.salesReport.totalSales, greenPaint)
        drawRow("Total Paid", state.salesReport.totalPaid, greenPaint)
        drawRow("Total Due", state.salesReport.totalDue, redPaint)
        drawRow("Total Expenses", state.expenseReport.totalExpenses, redPaint)
        y += 5f
        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
        y += 8f
        val profitPaint = if (state.profitLoss.netProfit >= 0) greenPaint else redPaint
        canvas.drawText("Net Profit", leftMargin + 10f, y + 18f, headerPaint)
        canvas.drawText("৳${CurrencyFormatter.format(state.profitLoss.netProfit)}", rightMargin - 100f, y + 18f, profitPaint)
        y += 35f
        canvas.drawText("Sale Count: ${state.salesReport.saleCount}", leftMargin, y, bodyPaint)
        document.finishPage(page)
        val dir = File(context.cacheDir, "exports").also { it.mkdirs() }
        val file = File(dir, "report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val mimeType = when {
            file.name.endsWith(".csv") -> "text/csv"
            file.name.endsWith(".xlsx") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else -> "application/pdf"
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Report"))
    }
}


