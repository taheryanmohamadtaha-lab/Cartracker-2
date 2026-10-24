package com.cartracker.app.di

import android.content.Context
import androidx.room.Room
import com.cartracker.app.data.local.AppDatabase
import com.cartracker.app.data.local.dao.AttachmentDao
import com.cartracker.app.data.local.dao.ExpenseDao
import com.cartracker.app.data.local.dao.FuelLogDao
import com.cartracker.app.data.local.dao.ServiceRecordDao
import com.cartracker.app.data.local.dao.ServiceTypeDao
import com.cartracker.app.data.local.dao.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "car_tracker.db")
            // در نسخه‌های آزمایشی اگر ساختار جدول‌ها عوض شود، دیتابیس پاک و دوباره ساخته می‌شود.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideVehicleDao(db: AppDatabase): VehicleDao = db.vehicleDao()

    @Provides
    fun provideFuelLogDao(db: AppDatabase): FuelLogDao = db.fuelLogDao()

    @Provides
    fun provideServiceTypeDao(db: AppDatabase): ServiceTypeDao = db.serviceTypeDao()

    @Provides
    fun provideServiceRecordDao(db: AppDatabase): ServiceRecordDao = db.serviceRecordDao()

    @Provides
    fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideAttachmentDao(db: AppDatabase): AttachmentDao = db.attachmentDao()
}
