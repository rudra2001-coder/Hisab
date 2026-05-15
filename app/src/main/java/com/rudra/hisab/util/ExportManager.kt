package com.rudra.hisab.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.rudra.hisab.data.local.entity.ExpenseCategory
import com.rudra.hisab.data.local.entity.ExpenseEntity
import com.rudra.hisab.data.repository.CustomerRepository
import com.rudra.hisab.data.repository.DailySnapshotRepository
import com.rudra.hisab.data.repository.ExpenseRepository
import com.rudra.hisab.data.repository.ProductRepository
import com.rudra.hisab.data.repository.SaleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportManager @Inject constructor(
    private val context: Context
) {

    suspend fun generateReport(
        startDate: Long,
        endDate: Long,
        includeSales: Boolean,
        includeInventory: Boolean,
        includeCustomers: Boolean,
        includeExpenses: Boolean,
        format: ExportFormat,
        saleRepository: SaleRepository,
        expenseRepository: ExpenseRepository,
        customerRepository: CustomerRepository,
        productRepository: ProductRepository,
        dailySnapshotRepository: DailySnapshotRepository
    ): String = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fileName = "Hisab_Report_${dateFormat.format(Date())}"

        when (format) {
            ExportFormat.CSV -> generateCsv(
                fileName, startDate, endDate, includeSales, includeInventory,
                includeCustomers, includeExpenses,
                saleRepository, expenseRepository, customerRepository, productRepository
            )
            ExportFormat.EXCEL -> generateExcel(
                fileName, startDate, endDate, includeSales, includeInventory,
                includeCustomers, includeExpenses,
                saleRepository, expenseRepository, customerRepository, productRepository
            )
            ExportFormat.PDF -> generatePdf(
                fileName, startDate, endDate, includeSales, includeInventory,
                includeCustomers, includeExpenses,
                saleRepository, expenseRepository, customerRepository, productRepository
            )
        }
    }

    private suspend fun generateCsv(
        baseName: String,
        startDate: Long,
        endDate: Long,
        includeSales: Boolean,
        includeInventory: Boolean,
        includeCustomers: Boolean,
        includeExpenses: Boolean,
        saleRepository: SaleRepository,
        expenseRepository: ExpenseRepository,
        customerRepository: CustomerRepository,
        productRepository: ProductRepository
    ): String {
        val file = File(context.cacheDir, "$baseName.csv")
        FileWriter(file).use { writer ->
            if (includeSales) {
                writer.appendLine("=== SALES REPORT ===")
                writer.appendLine("ID,Customer,Total,Paid,Due,Method,Date")
                val sales = saleRepository.getSalesByDateRange(startDate, endDate).first()
                sales.forEach { sale ->
                    writer.appendLine(
                        "${sale.id},${sale.customerName.ifEmpty { "Walk-in" }},${sale.totalAmount}," +
                            "${sale.paidAmount},${sale.dueAmount},${sale.paymentMethod}," +
                            "${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(sale.createdAt))}"
                    )
                }
                writer.appendLine()
            }

            if (includeInventory) {
                writer.appendLine("=== INVENTORY REPORT ===")
                writer.appendLine("ID,Name,Buy Price,Sell Price,Stock,Low Stock Threshold")
                val products = productRepository.getAllProducts().first()
                products.forEach { p ->
                    writer.appendLine("${p.id},${p.name},${p.buyPrice},${p.sellPrice},${p.currentStock},${p.lowStockThreshold}")
                }
                writer.appendLine()
            }

            if (includeCustomers) {
                writer.appendLine("=== CUSTOMER REPORT ===")
                writer.appendLine("ID,Name,Phone,Total Due")
                val customers = customerRepository.getAllCustomers().first()
                customers.forEach { c ->
                    writer.appendLine("${c.id},${c.name},${c.phone},${c.totalDue}")
                }
                writer.appendLine()
            }

            if (includeExpenses) {
                writer.appendLine("=== EXPENSE REPORT ===")
                writer.appendLine("ID,Category,Amount,Description,Date")
                val expenses = expenseRepository.getExpensesByDateRange(startDate, endDate).first()
                expenses.forEach { e ->
                    writer.appendLine(
                        "${e.id},${e.categoryId},${e.amount},${e.description}," +
                            "${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(e.date))}"
                    )
                }
            }
        }
        shareFile(file, "text/csv", "$baseName.csv")
        return file.absolutePath
    }

    private suspend fun generateExcel(
        baseName: String,
        startDate: Long,
        endDate: Long,
        includeSales: Boolean,
        includeInventory: Boolean,
        includeCustomers: Boolean,
        includeExpenses: Boolean,
        saleRepository: SaleRepository,
        expenseRepository: ExpenseRepository,
        customerRepository: CustomerRepository,
        productRepository: ProductRepository
    ): String {
        val workbook = XSSFWorkbook()
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.DARK_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            bottomBorderColor = IndexedColors.BLACK.index
            topBorderColor = IndexedColors.BLACK.index
            leftBorderColor = IndexedColors.BLACK.index
            rightBorderColor = IndexedColors.BLACK.index
            alignment = HorizontalAlignment.CENTER
        }
        val bold = workbook.createFont().apply { bold = true }
        headerStyle.setFont(bold)

        fun createHeaderRow(sheet: org.apache.poi.ss.usermodel.Sheet, vararg headers: String) {
            val row = sheet.createRow(0)
            headers.forEachIndexed { i, h ->
                val cell = row.createCell(i)
                cell.setCellValue(h)
                cell.cellStyle = headerStyle
            }
        }

        if (includeSales) {
            val sheet = workbook.createSheet("Sales")
            createHeaderRow(sheet, "ID", "Customer", "Total", "Paid", "Due", "Method", "Date")
            var rowIdx = 1
            val sales = saleRepository.getSalesByDateRange(startDate, endDate).first()
            sales.forEach { sale ->
                val row = sheet.createRow(rowIdx++)
                row.createCell(0).setCellValue(sale.id.toDouble())
                row.createCell(1).setCellValue(sale.customerName.ifEmpty { "Walk-in" })
                row.createCell(2).setCellValue(sale.totalAmount)
                row.createCell(3).setCellValue(sale.paidAmount)
                row.createCell(4).setCellValue(sale.dueAmount)
                row.createCell(5).setCellValue(sale.paymentMethod.name)
                row.createCell(6).setCellValue(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(sale.createdAt)))
            }
            sheet.setColumnWidth(0, 10)
            sheet.setColumnWidth(1, 25)
            sheet.setColumnWidth(2, 15)
            sheet.setColumnWidth(3, 15)
            sheet.setColumnWidth(4, 15)
            sheet.setColumnWidth(5, 18)
            sheet.setColumnWidth(6, 22)
        }

        if (includeInventory) {
            val sheet = workbook.createSheet("Inventory")
            createHeaderRow(sheet, "ID", "Name", "Buy Price", "Sell Price", "Stock", "Low Stock Alert")
            var rowIdx = 1
            val products = productRepository.getAllProducts().first()
            products.forEach { p ->
                val row = sheet.createRow(rowIdx++)
                row.createCell(0).setCellValue(p.id.toDouble())
                row.createCell(1).setCellValue(p.name)
                row.createCell(2).setCellValue(p.buyPrice)
                row.createCell(3).setCellValue(p.sellPrice)
                row.createCell(4).setCellValue(p.currentStock)
                row.createCell(5).setCellValue(p.lowStockThreshold)
            }
        }

        if (includeCustomers) {
            val sheet = workbook.createSheet("Customers")
            createHeaderRow(sheet, "ID", "Name", "Phone", "Total Due")
            var rowIdx = 1
            val customers = customerRepository.getAllCustomers().first()
            customers.forEach { c ->
                val row = sheet.createRow(rowIdx++)
                row.createCell(0).setCellValue(c.id.toDouble())
                row.createCell(1).setCellValue(c.name)
                row.createCell(2).setCellValue(c.phone)
                row.createCell(3).setCellValue(c.totalDue)
            }
        }

        if (includeExpenses) {
            val sheet = workbook.createSheet("Expenses")
            createHeaderRow(sheet, "ID", "Category", "Amount", "Description", "Date")
            var rowIdx = 1
            val expenses = expenseRepository.getExpensesByDateRange(startDate, endDate).first()
            expenses.forEach { e ->
                val row = sheet.createRow(rowIdx++)
                row.createCell(0).setCellValue(e.id.toDouble())
                row.createCell(1).setCellValue(e.categoryId.name)
                row.createCell(2).setCellValue(e.amount)
                row.createCell(3).setCellValue(e.description)
                row.createCell(4).setCellValue(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(e.date)))
            }
        }

        val file = File(context.cacheDir, "$baseName.xlsx")
        workbook.write(file.outputStream())
        workbook.close()
        shareFile(file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "$baseName.xlsx")
        return file.absolutePath
    }

    private suspend fun generatePdf(
        baseName: String,
        startDate: Long,
        endDate: Long,
        includeSales: Boolean,
        includeInventory: Boolean,
        includeCustomers: Boolean,
        includeExpenses: Boolean,
        saleRepository: SaleRepository,
        expenseRepository: ExpenseRepository,
        customerRepository: CustomerRepository,
        productRepository: ProductRepository
    ): String {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint().apply { textSize = 12f }

        var y = 40f
        val lineHeight = 20f
        val margin = 40f
        val pageWidth = 595 - margin * 2

        fun drawTitle(text: String) {
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText(text, margin, y, paint)
            y += lineHeight * 1.5f
            paint.textSize = 12f
            paint.isFakeBoldText = false
        }

        fun drawLine(label: String, value: String) {
            canvas.drawText("$label: $value", margin, y, paint)
            y += lineHeight
        }

        fun drawSection(title: String) {
            y += lineHeight * 0.5f
            paint.isFakeBoldText = true
            drawTitle(title)
            paint.isFakeBoldText = false
        }

        fun checkNewPage() {
            if (y > 800f) {
                pdfDocument.finishPage(page)
                val newPage = pdfDocument.startPage(pageInfo)
                y = 40f
            }
        }

        drawTitle("Hisab Report")
        drawLine("From", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(startDate)))
        drawLine("To", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(endDate)))

        if (includeSales) {
            drawSection("Sales Report")
            var totalSales = 0.0
            val sales = saleRepository.getSalesByDateRange(startDate, endDate).first()
            sales.forEach { sale ->
                checkNewPage()
                drawLine("Sale #${sale.id}", "${sale.totalAmount} (${sale.paymentStatus})")
                totalSales += sale.totalAmount
            }
            checkNewPage()
            drawLine("Total Sales", totalSales.toString())
        }

        if (includeCustomers) {
            drawSection("Customer Dues")
            val customers = customerRepository.getAllCustomers().first()
            customers.filter { it.totalDue > 0 }.forEach { c ->
                checkNewPage()
                drawLine(c.name, "Due: ${c.totalDue}")
            }
        }

        if (includeExpenses) {
            drawSection("Expenses")
            var totalExp = 0.0
            val expenses = expenseRepository.getExpensesByDateRange(startDate, endDate).first()
            expenses.forEach { e ->
                checkNewPage()
                drawLine(e.categoryId.name, "${e.amount} - ${e.description}")
                totalExp += e.amount
            }
            checkNewPage()
            drawLine("Total Expenses", totalExp.toString())
        }

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "$baseName.pdf")
        pdfDocument.writeTo(file.outputStream())
        pdfDocument.close()
        shareFile(file, "application/pdf", "$baseName.pdf")
        return file.absolutePath
    }

    private fun shareFile(file: File, mimeType: String, fileName: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }
}
