package com.rudra.hisab.data.repository

/**
 * Contract that SettingsViewModel uses for cross-cutting data operations.
 *
 * Implement this in your concrete Room/DataStore repository and bind it via Hilt:
 *
 * ```kotlin
 * @Module @InstallIn(SingletonComponent::class)
 * abstract class RepositoryModule {
 *     @Binds @Singleton
 *     abstract fun bindDataRepository(impl: DataRepositoryImpl): DataRepository
 * }
 * ```
 */
interface DataRepository {

    /**
     * Collect every exportable entity from the database and return an
     * [ExportPayload] ready to be serialised.
     */
    suspend fun exportAllData(): ExportPayload

    /**
     * Parse [json] (a valid v1 export document) and upsert all contained
     * entities back into the database.  Throws on malformed input.
     */
    suspend fun importAllData(json: String)

    /**
     * Delete every row from every user-data table.
     * Called before [AppPreferences.clearAllData] when the user chooses
     * "Delete all data" in Settings.
     */
    suspend fun clearAllData()
}

// ─────────────────────────────────────────────────────────────────────────────
// Export payload
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Top-level wrapper for a full database export.
 *
 * Add your own domain lists (sales, customers, products, …) as fields below
 * and update [toJsonString] / [toCsvString] accordingly.
 *
 * The [version] field is checked on import; increment it whenever the schema
 * changes so old exports are rejected gracefully.
 */
data class ExportPayload(
    val version: Int = 1,
    val exportDate: String = java.text.SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US
    ).format(java.util.Date()),

    // ── Add your domain lists here ───────────────────────────────────────────
    // val sales: List<SaleEntity> = emptyList(),
    // val customers: List<CustomerEntity> = emptyList(),
    // val products: List<ProductEntity> = emptyList(),
) {

    /** Serialise to a pretty-printed JSON string. */
    fun toJsonString(): String {
        // Replace this stub with your preferred serialiser
        // (Gson, Moshi, kotlinx.serialization, etc.)
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"version\": $version,\n")
        sb.append("  \"exportDate\": \"$exportDate\"\n")
        // sb.append("  ,\"sales\": ${Gson().toJson(sales)}\n")
        sb.append("}")
        return sb.toString()
    }

    /**
     * Serialise to CSV.
     *
     * A single export often contains heterogeneous tables, so the recommended
     * approach is one section per entity type separated by blank lines:
     *
     * ```
     * ## sales
     * id,date,amount
     * 1,2024-01-01,500
     *
     * ## customers
     * id,name,phone
     * 1,Rahim,017…
     * ```
     */
    fun toCsvString(): String {
        val sb = StringBuilder()
        sb.appendLine("## export_meta")
        sb.appendLine("version,exportDate")
        sb.appendLine("$version,$exportDate")
        // sb.appendLine()
        // sb.appendLine("## sales")
        // sb.appendLine("id,date,amount,customerName")
        // sales.forEach { sb.appendLine("${it.id},${it.date},${it.amount},${it.customerName}") }
        return sb.toString()
    }
}