package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.HolidayCategory
import com.example.data.model.HolidayItem
import com.example.data.repository.HolidayRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HolidayViewModel(
    application: Application,
    private val repository: HolidayRepository
) : AndroidViewModel(application) {

    // Real-time ticking epoch millis (updated every 1000ms)
    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    private val _selectedCategory = MutableStateFlow(HolidayCategory.ALL)
    val selectedCategory: StateFlow<HolidayCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear: StateFlow<Int?> = _selectedYear.asStateFlow()

    private val _selectedHolidayForDetail = MutableStateFlow<HolidayItem?>(null)
    val selectedHolidayForDetail: StateFlow<HolidayItem?> = _selectedHolidayForDetail.asStateFlow()

    // Base holidays flow combining static/official + Room custom holidays
    private val customHolidaysFlow = repository.getCustomHolidaysFlow()

    // Combined list of holidays
    val allHolidays: StateFlow<List<HolidayItem>> = combine(
        customHolidaysFlow,
        _currentTimeMillis
    ) { customList, currentMillis ->
        val official = repository.getOfficialAndCalendarHolidays(currentMillis)
        (official + customList).distinctBy { it.id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered holidays by category, search query, sorted by upcoming date
    val filteredUpcomingHolidays: StateFlow<List<HolidayItem>> = combine(
        allHolidays,
        _selectedCategory,
        _searchQuery,
        _currentTimeMillis
    ) { holidays, category, query, currentMillis ->
        holidays.filter { item ->
            val matchesCategory = when (category) {
                HolidayCategory.ALL -> true
                else -> item.category == category
            }
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.subtitle.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)

            // Include holidays that are upcoming or currently ongoing
            val durationMillis = (item.durationDays * 24 * 3600 * 1000).toLong()
            val isNotPassed = (item.targetEpochMillis + durationMillis) >= currentMillis

            matchesCategory && matchesQuery && isNotPassed
        }.sortedBy { it.targetEpochMillis }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // The very next imminent holiday
    val imminentHoliday: StateFlow<HolidayItem?> = combine(
        allHolidays,
        _currentTimeMillis
    ) { holidays, currentMillis ->
        holidays
            .filter { item ->
                val durationMillis = (item.durationDays * 24 * 3600 * 1000).toLong()
                (item.targetEpochMillis + durationMillis) >= currentMillis
            }
            .minByOrNull { it.targetEpochMillis }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Bridge holidays list (official or custom holidays that have strategic leave opportunities)
    val bridgeOpportunities: StateFlow<List<HolidayItem>> = combine(
        allHolidays,
        _currentTimeMillis
    ) { holidays, currentMillis ->
        holidays.filter { item ->
            val durationMillis = (item.durationDays * 24 * 3600 * 1000).toLong()
            val isUpcoming = (item.targetEpochMillis + durationMillis) >= currentMillis
            val hasTip = !item.bridgeTip.isNullOrBlank() && !item.bridgeTip.contains("Hafta sonuna")
            isUpcoming && hasTip
        }.sortedBy { it.targetEpochMillis }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Start live ticker coroutine: emits every second (1000ms)
        viewModelScope.launch {
            while (isActive) {
                _currentTimeMillis.value = System.currentTimeMillis()
                delay(1000L)
            }
        }
    }

    fun setCategory(category: HolidayCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectHolidayForDetail(holiday: HolidayItem?) {
        _selectedHolidayForDetail.value = holiday
    }

    fun addCustomHoliday(
        title: String,
        subtitle: String,
        targetEpochMillis: Long,
        durationDays: Double,
        description: String,
        location: String,
        iconKey: String,
        colorHex: Long
    ) {
        viewModelScope.launch {
            repository.addCustomHoliday(
                title = title,
                subtitle = subtitle,
                targetEpochMillis = targetEpochMillis,
                durationDays = durationDays,
                description = description,
                location = location,
                iconKey = iconKey,
                colorHex = colorHex
            )
        }
    }

    fun deleteHoliday(holiday: HolidayItem) {
        viewModelScope.launch {
            if (holiday.isCustom) {
                repository.deleteCustomHoliday(holiday.id)
            }
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(application)
                    val repo = HolidayRepository(db.customHolidayDao())
                    return HolidayViewModel(application, repo) as T
                }
            }
    }
}
