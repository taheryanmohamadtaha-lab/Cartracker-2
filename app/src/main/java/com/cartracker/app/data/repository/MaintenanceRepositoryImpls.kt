package com.cartracker.app.data.repository

import com.cartracker.app.data.files.VehiclePhotoStore
import com.cartracker.app.data.local.dao.AttachmentDao
import com.cartracker.app.data.local.dao.ExpenseDao
import com.cartracker.app.data.local.dao.ServiceRecordDao
import com.cartracker.app.data.local.dao.ServiceTypeDao
import com.cartracker.app.data.local.entity.AttachmentEntity
import com.cartracker.app.data.mapper.toDomain
import com.cartracker.app.data.mapper.toEntity
import com.cartracker.app.domain.model.AttachmentOwner
import com.cartracker.app.domain.model.ExpenseRecord
import com.cartracker.app.domain.model.ServiceRecord
import com.cartracker.app.domain.model.ServiceType
import com.cartracker.app.domain.repository.AttachmentRepository
import com.cartracker.app.domain.repository.ExpenseRepository
import com.cartracker.app.domain.repository.ServiceRecordRepository
import com.cartracker.app.domain.repository.ServiceTypeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceTypeRepositoryImpl @Inject constructor(
    private val dao: ServiceTypeDao
) : ServiceTypeRepository {

    override fun observeAll(): Flow<List<ServiceType>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): ServiceType? = dao.getById(id)?.toDomain()

    override suspend fun save(type: ServiceType): Long =
        if (type.id == 0L) {
            dao.insert(type.toEntity())
        } else {
            dao.update(type.toEntity())
            type.id
        }

    override suspend fun deleteOrArchive(id: Long) {
        if (dao.recordCount(id) > 0) {
            val existing = dao.getById(id) ?: return
            dao.update(existing.copy(isArchived = true))
        } else {
            dao.deleteById(id)
        }
    }
}

@Singleton
class ServiceRecordRepositoryImpl @Inject constructor(
    private val dao: ServiceRecordDao,
    private val attachments: AttachmentRepository
) : ServiceRecordRepository {

    override fun observeByVehicle(vehicleId: Long): Flow<List<ServiceRecord>> =
        dao.observeByVehicle(vehicleId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): ServiceRecord? = dao.getById(id)?.toDomain()

    override suspend fun save(record: ServiceRecord): Long =
        if (record.id == 0L) {
            dao.insert(record.toEntity())
        } else {
            dao.update(record.toEntity())
            record.id
        }

    override suspend fun delete(id: Long) {
        attachments.deleteFor(AttachmentOwner.SERVICE_RECORD, id)
        dao.deleteById(id)
    }
}

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao,
    private val attachments: AttachmentRepository
) : ExpenseRepository {

    override fun observeByVehicle(vehicleId: Long): Flow<List<ExpenseRecord>> =
        dao.observeByVehicle(vehicleId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): ExpenseRecord? = dao.getById(id)?.toDomain()

    override suspend fun save(record: ExpenseRecord): Long =
        if (record.id == 0L) {
            dao.insert(record.toEntity())
        } else {
            dao.update(record.toEntity())
            record.id
        }

    override suspend fun delete(id: Long) {
        attachments.deleteFor(AttachmentOwner.EXPENSE_RECORD, id)
        dao.deleteById(id)
    }
}

@Singleton
class AttachmentRepositoryImpl @Inject constructor(
    private val dao: AttachmentDao,
    private val photoStore: VehiclePhotoStore
) : AttachmentRepository {

    override suspend fun get(owner: AttachmentOwner, ownerId: Long): List<String> =
        dao.getFor(owner.name, ownerId).map { it.uri }

    override suspend fun replace(owner: AttachmentOwner, ownerId: Long, paths: List<String>) {
        val existing = dao.getFor(owner.name, ownerId)
        for (item in existing) {
            if (item.uri !in paths) {
                dao.deleteById(item.id)
                photoStore.delete(item.uri)
            }
        }
        val kept = existing.map { it.uri }.toSet()
        for (path in paths) {
            if (path !in kept) {
                dao.insert(
                    AttachmentEntity(
                        ownerType = owner.name,
                        ownerId = ownerId,
                        uri = path,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    override suspend fun deleteFor(owner: AttachmentOwner, ownerId: Long) {
        for (item in dao.getFor(owner.name, ownerId)) {
            dao.deleteById(item.id)
            photoStore.delete(item.uri)
        }
    }

    override suspend fun cleanupOrphans() {
        for (uri in dao.orphanUris()) {
            photoStore.delete(uri)
        }
        dao.deleteOrphans()
    }
}
