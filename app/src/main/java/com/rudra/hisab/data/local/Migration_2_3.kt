package com.rudra.hisab.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ── New tables ────────────────────────────────────────────────
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `suppliers` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `phone` TEXT NOT NULL DEFAULT '',
                `address` TEXT NOT NULL DEFAULT '',
                `companyName` TEXT NOT NULL DEFAULT '',
                `totalDue` REAL NOT NULL DEFAULT 0.0,
                `createdAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL DEFAULT 0
            )"""
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_suppliers_phone` ON `suppliers` (`phone`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_suppliers_name` ON `suppliers` (`name`)")

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `stock_transactions` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `productId` INTEGER NOT NULL,
                `type` TEXT NOT NULL,
                `quantity` REAL NOT NULL,
                `previousStock` REAL NOT NULL,
                `newStock` REAL NOT NULL,
                `note` TEXT NOT NULL DEFAULT '',
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`productId`) REFERENCES `products`(`id`) ON DELETE CASCADE
            )"""
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_transactions_productId` ON `stock_transactions` (`productId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_transactions_createdAt` ON `stock_transactions` (`createdAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_transactions_type` ON `stock_transactions` (`type`)")

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `ledger_entries` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `accountType` TEXT NOT NULL,
                `customerId` INTEGER,
                `supplierId` INTEGER,
                `entryType` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `description` TEXT NOT NULL DEFAULT '',
                `referenceId` INTEGER,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`customerId`) REFERENCES `customers`(`id`) ON DELETE SET NULL,
                FOREIGN KEY(`supplierId`) REFERENCES `suppliers`(`id`) ON DELETE SET NULL
            )"""
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ledger_entries_customerId` ON `ledger_entries` (`customerId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ledger_entries_supplierId` ON `ledger_entries` (`supplierId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ledger_entries_createdAt` ON `ledger_entries` (`createdAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ledger_entries_accountType` ON `ledger_entries` (`accountType`)")

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `payments` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `type` TEXT NOT NULL,
                `customerId` INTEGER,
                `supplierId` INTEGER,
                `amount` REAL NOT NULL,
                `paymentMethod` TEXT NOT NULL DEFAULT 'CASH',
                `description` TEXT NOT NULL DEFAULT '',
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`customerId`) REFERENCES `customers`(`id`) ON DELETE SET NULL,
                FOREIGN KEY(`supplierId`) REFERENCES `suppliers`(`id`) ON DELETE SET NULL
            )"""
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_customerId` ON `payments` (`customerId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_supplierId` ON `payments` (`supplierId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_createdAt` ON `payments` (`createdAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_type` ON `payments` (`type`)")

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `sales` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `customerId` INTEGER,
                `customerName` TEXT NOT NULL DEFAULT '',
                `totalAmount` REAL NOT NULL,
                `discountAmount` REAL NOT NULL DEFAULT 0.0,
                `paidAmount` REAL NOT NULL DEFAULT 0.0,
                `dueAmount` REAL NOT NULL DEFAULT 0.0,
                `paymentMethod` TEXT NOT NULL DEFAULT 'CASH',
                `paymentStatus` TEXT NOT NULL DEFAULT 'PAID',
                `note` TEXT NOT NULL DEFAULT '',
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`customerId`) REFERENCES `customers`(`id`) ON DELETE SET NULL
            )"""
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_customerId` ON `sales` (`customerId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_createdAt` ON `sales` (`createdAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_paymentStatus` ON `sales` (`paymentStatus`)")

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `sale_items` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `saleId` INTEGER NOT NULL,
                `productId` INTEGER,
                `productName` TEXT NOT NULL,
                `quantity` REAL NOT NULL,
                `unitPrice` REAL NOT NULL,
                `totalPrice` REAL NOT NULL,
                `profit` REAL NOT NULL DEFAULT 0.0,
                FOREIGN KEY(`saleId`) REFERENCES `sales`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`productId`) REFERENCES `products`(`id`) ON DELETE SET NULL
            )"""
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sale_items_saleId` ON `sale_items` (`saleId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sale_items_productId` ON `sale_items` (`productId`)")

        // ── New columns on products ───────────────────────────────────
        db.execSQL("ALTER TABLE `products` ADD COLUMN `supplierId` INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE `products` ADD COLUMN `barcode` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `products` ADD COLUMN `batchNumber` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `products` ADD COLUMN `expiryDate` INTEGER DEFAULT NULL")

        // ── New columns on customers ──────────────────────────────────
        db.execSQL("ALTER TABLE `customers` ADD COLUMN `creditLimit` REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE `customers` ADD COLUMN `email` TEXT NOT NULL DEFAULT ''")
    }
}
