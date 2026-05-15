package com.rudra.hisab.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val INVENTORY = "inventory"
    const val SALE = "sale"
    const val CUSTOMERS = "customers"
    const val CUSTOMER_DETAIL = "customer_detail/{customerId}"
    const val EXPENSES = "expenses"
    const val DAILY_CLOSE = "daily_close"
    const val SETTINGS = "settings"
    const val ANALYTICS = "analytics"
    const val MORE = "more"
    const val ADD_PRODUCT = "add_product"
    const val SPLASH = "splash"
    const val LOCK_SCREEN = "lock_screen"

    fun customerDetail(customerId: Long) = "customer_detail/$customerId"
}
