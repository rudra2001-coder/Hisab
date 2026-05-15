package com.rudra.hisab.data.local.entity

/** How a sale/transaction is settled: cash, on credit, or partial payment. */
enum class SalePaymentType {
    CASH,
    CREDIT,
    PARTIAL
}

/** Settlement state of a sale record (derived from paid vs due amounts). */
enum class PaymentStatus {
    PAID,
    PARTIAL,
    DUE
}
