package com.example.lab7androidsec.home


import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab7androidsec.data.HealthRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class HomeViewModel(private val healthRepository: HealthRepository) : ViewModel() {
    val uiState = mutableStateOf(UiState())
    val currentDate: LocalDate = LocalDate.now()
    val yesterdayDate: LocalDate = currentDate.minusDays(1)

    init {
        launchChecks()
    }

    private fun launchChecks() {
        viewModelScope.launch {
            when (healthRepository.initClient()) {
                HealthConnectClient.SDK_UNAVAILABLE -> {
                    uiState.value = uiState.value.copy(abortDialogRequired = true)
                    return@launch
                }

                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    uiState.value = uiState.value.copy(installSdkDialogRequired = true)
                    return@launch
                }
            }
            if (!healthRepository.checkPermissions()) {
                uiState.value = uiState.value.copy(permissionDialogRequired = true)
                return@launch
            }
            uiState.value = uiState.value.copy(checksFinished = true)
            updateAggregationDataForDay(yesterdayDate)
        }
    }

    private fun updateAggregationDataForDay(selectedDate: LocalDate) {
        viewModelScope.launch {
            val startTime = selectedDate.atStartOfDay().toInstant(ZoneOffset.UTC)
            val endTime = selectedDate.atStartOfDay().plusDays(1).minusSeconds(1).toInstant(ZoneOffset.UTC)

            healthRepository.getDayData(startTime, endTime)?.let { data ->
                uiState.value = uiState.value.copy(totalSteps = data.sumOf { it.count }, stepsRecords = data)
            } ?: run {

            }
        }
    }
    fun onChecksFinished() {
        uiState.value = uiState.value.copy(checksFinished = true)
        updateAggregationDataForDay(yesterdayDate)
    }

    fun showAddDialog(shouldShowDialog: Boolean){
        uiState.value = uiState.value.copy(isAddEntryDialogRequired = shouldShowDialog)
    }

    fun saveEntry(interval: Pair<Int, Int>, steps: Long){
        val dateTimeStart = uiState.value.dayTime.toLocalDate().atStartOfDay(ZoneId.systemDefault()).plusMinutes(interval.first.toLong()).toInstant()
        val dateTimeEnd = uiState.value.dayTime.toLocalDate().atStartOfDay(ZoneId.systemDefault()).plusMinutes(interval.second.toLong()).toInstant()
        viewModelScope.launch {
            healthRepository.saveSteps(steps, dateTimeStart, dateTimeEnd)
        }
        updateAggregationDataForDay(yesterdayDate)
    }

    fun showPermissionDialog(shouldShowDialog: Boolean) {
        uiState.value = uiState.value.copy(permissionDialogRequired = shouldShowDialog)
    }

    data class UiState(
        val installSdkDialogRequired: Boolean = false,
        val permissionDialogRequired: Boolean = false,
        val abortDialogRequired: Boolean = false,
        val checksFinished: Boolean = false,
        val isAddEntryDialogRequired: Boolean = false,
        val dayTime: LocalDateTime = LocalDateTime.now().minusDays(1),
        val totalSteps: Long = 0,
        val stepsRecords: List<StepsRecord> = listOf()
    )
    {
        val day: String
            get() = dayTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }
}