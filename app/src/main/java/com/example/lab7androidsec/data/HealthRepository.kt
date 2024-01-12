package com.example.lab7androidsec.data

import androidx.health.connect.client.records.StepsRecord
import java.time.Instant

interface HealthRepository {
    fun initClient(): Int
    suspend fun checkPermissions(): Boolean

    suspend fun getDayData(startTime: Instant, endTime: Instant): List<StepsRecord>?
    suspend fun saveSteps(count: Long, startTime: Instant, endTime: Instant): Boolean

}