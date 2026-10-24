package com.cartracker.app.domain.repository

import com.cartracker.app.domain.model.AttachmentOwner
import com.cartracker.app.domain.model.ExpenseRecord
import com.cartracker.app.domain.model.ServiceRecord
import com.cartracker.app.domain.model.ServiceType
import kotlinx.coroutines.flow.Flow

interface ServiceTypeRepository {
    /** همه انواع سرویس (شامل بایگانی‌شده‌ها). */
    fun observeAll(): Flow<List<ServiceType>>
    suspend fun getById(id: Long): ServiceType?

    /** اگر id صفر باشد ثبت جدید است. نام تکراری باعث خطا می‌شود. */
    suspend fun save(type: ServiceType): Long

    /** اگر برای این نوع سرویس سابقه‌ای ثبت شده باشد بایگانی می‌شود، وگرنه حذف. */
    suspend fun deleteOrArchive(id: Long)
}

interface ServiceRecordRepository {
    fun observeByVehicle(vehicleId: Long): Flow<List<ServiceRecord>>
    suspend fun getById(id: Long): ServiceRecord?
    suspend fun save(record: ServiceRecord): Long
    suspend fun delete(id: Long)
}

interface ExpenseRepository {
    fun observeByVehicle(vehicleId: Long): Flow<List<ExpenseRecord>>
    suspend fun getById(id: Long): ExpenseRecord?
    suspend fun save(record: ExpenseRecord): Long
    suspend fun delete(id: Long)
}

/** عکس فاکتورها. مسیر فایل‌ها در حافظه خود برنامه است. */
interface AttachmentRepository {
    suspend fun get(owner: AttachmentOwner, ownerId: Long): List<String>

    /** فهرست پیوست‌ها را با paths یکسان می‌کند (موارد حذف‌شده را از دیسک هم پاک می‌کند). */
    suspend fun replace(owner: AttachmentOwner, ownerId: Long, paths: List<String>)
    suspend fun deleteFor(owner: AttachmentOwner, ownerId: Long)

    /** پیوست‌های رکوردهایی که دیگر وجود ندارند (مثلاً بعد از حذف خودرو) را پاک می‌کند. */
    suspend fun cleanupOrphans()
}
