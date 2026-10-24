package com.cartracker.app.di

import com.cartracker.app.data.repository.AttachmentRepositoryImpl
import com.cartracker.app.data.repository.ExpenseRepositoryImpl
import com.cartracker.app.data.repository.ServiceRecordRepositoryImpl
import com.cartracker.app.data.repository.ServiceTypeRepositoryImpl
import com.cartracker.app.domain.repository.AttachmentRepository
import com.cartracker.app.domain.repository.ExpenseRepository
import com.cartracker.app.domain.repository.ServiceRecordRepository
import com.cartracker.app.domain.repository.ServiceTypeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MaintenanceModule {

    @Binds
    @Singleton
    abstract fun bindServiceTypeRepository(impl: ServiceTypeRepositoryImpl): ServiceTypeRepository

    @Binds
    @Singleton
    abstract fun bindServiceRecordRepository(impl: ServiceRecordRepositoryImpl): ServiceRecordRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindAttachmentRepository(impl: AttachmentRepositoryImpl): AttachmentRepository
}
