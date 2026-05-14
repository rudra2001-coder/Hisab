package com.rudra.hisab.data.repository

import com.rudra.hisab.data.local.dao.CategoryDao
import com.rudra.hisab.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(id: Long): CategoryEntity? = categoryDao.getCategoryById(id)

    suspend fun insert(category: CategoryEntity): Long = categoryDao.insert(category)

    suspend fun insertAll(categories: List<CategoryEntity>) = categoryDao.insertAll(categories)

    suspend fun update(category: CategoryEntity) = categoryDao.update(category)

    suspend fun delete(category: CategoryEntity) = categoryDao.delete(category)
}
