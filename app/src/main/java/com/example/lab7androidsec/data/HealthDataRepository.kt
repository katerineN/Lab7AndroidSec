package com.example.lab7androidsec.data

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZoneOffset

class HealthDataRepository(private val applicationContext: Context) : HealthRepository {
        // Клиент для взаимодействия с Health Connect
        private lateinit var healthConnectClient: HealthConnectClient

        // Инициализация клиента Health Connect и проверка доступности SDK
        override fun initClient(): Int {
            // Получение статуса доступности SDK
            val availabilityStatus =
                HealthConnectClient.getSdkStatus(applicationContext, AppRequired.sdkPackageName)

            // Если SDK доступно, создаем экземпляр клиента
            if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
                healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)
            }

            // Возвращаем статус доступности
            return availabilityStatus
        }

        // Проверка наличия необходимых разрешений
        override suspend fun checkPermissions(): Boolean {
            // Получаем список предоставленных разрешений и проверяем наличие необходимых
            return healthConnectClient.permissionController.getGrantedPermissions()
                .containsAll(AppRequired.requiredPermissions.toList())
        }

        // Получение данных за день
        override suspend fun getDayData(startTime: Instant, endTime: Instant): List<StepsRecord>? {
            return try {
                // Чтение записей с использованием Health Connect
                val response = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                response.records
            } catch (e: Exception) {
                Log.e("TAG", "Day fetch failed: $e")
                null
            }
        }

        //Сохранение данных о количестве шагов за определенный период
        override suspend fun saveSteps(
            count: Long,
            startTime: Instant,
            endTime: Instant,
        ): Boolean {
            return try {
                // Создаем объект StepsRecord и вставляем его с использованием Health Connect
                val stepsRecord = StepsRecord(
                    count = count,
                    startTime = startTime,
                    endTime = endTime,
                    startZoneOffset = ZoneOffset.UTC,
                    endZoneOffset = ZoneOffset.UTC
                )
                // Возвращаем результат сохранения
                healthConnectClient.insertRecords(listOf(stepsRecord)).recordIdsList.isNotEmpty()
            } catch (e: Exception) {
                Log.e("TAG", "Steps save failed: $e")
                false
            }
        }

    }

    object AppRequired {
        const val sdkPackageName = "com.google.android.apps.healthdata"

        val requiredPermissions = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getWritePermission(StepsRecord::class)
        )
    }


