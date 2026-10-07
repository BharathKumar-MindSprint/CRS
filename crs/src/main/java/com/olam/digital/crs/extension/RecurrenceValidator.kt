package com.olam.digital.crs.extension

import com.olam.digital.crs.model.mapper.FarmerModuleView
import org.json.JSONArray
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object RecurrenceValidator {

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun getModuleStatus(
        item: FarmerModuleView,
        submittedModulesArray: JSONArray,
        farmerId: Long,
        disabledMonths: List<String> = emptyList(),
        assignmentType: String = "1",
        startDateEpoch: String? = null
    ): ModuleStatus {
        val now = Calendar.getInstance()
        val recurrenceType = item.recurrenceType ?: "MONTHLY"
        val recurrenceFrequency = item.recurrenceFrequency ?: 1
        val startDate = startDateEpoch?.toLongOrNull()?.let { Date(it) }

        var disableEntry = false
        var disableMonth = false
        var hasSubmitted = false
        Timber.d("Evaluating module: ${item.moduleId} for farmer: $farmerId with recurrence type: $recurrenceType and frequency: $recurrenceFrequency")
        Timber.d("Current month: ${now.get(Calendar.MONTH)}, Disabled months: $disabledMonths")
        Timber.d("Submitted modules count: ${submittedModulesArray.length()} for farmer: $farmerId $submittedModulesArray")
//3
        if (disabledMonths.contains(now.get(Calendar.MONTH).toString())) {
            disableMonth = true
        } else {
            for (i in 0 until submittedModulesArray.length()) {
                val submittedModule = submittedModulesArray.getJSONObject(i)
                val submittedDetails = submittedModule.getJSONObject("module")
                Timber.d("Checking submitted module: $submittedDetails against item: ${item.moduleId}")
                Timber.d(
                    "Current time: ${sdf.format(now.time)}, Start date: ${
                        startDate?.let {
                            sdf.format(
                                it
                            )
                        }
                    }, Recurrence type: $recurrenceType, Recurrence frequency: $recurrenceFrequency, Disabled months: $disabledMonths"
                )
                Timber.d("Submitted module created at: ${submittedDetails.optString("created_at")}")
                Timber.d("Submitted module farmer ID: ${submittedModule.optLong("farmer_id")}, Current farmer ID: $farmerId")
                Timber.d("Submitted module ID: ${submittedDetails.optLong("id")}, Current module ID: ${item.moduleId}")

                if (submittedDetails.has("id") && submittedDetails.getLong("id") == item.moduleId) {
                    hasSubmitted = true
                    val createdAt = submittedDetails.optString("created_at")
                    val lastSubmittedDate = Calendar.getInstance()
                    try {
                        lastSubmittedDate.time = sdf.parse(createdAt)!!
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    /*{"module":{"id":1560,"created_at":"2026-04-07 15:32:27","farmId":428518,"plotId":null}}]}*/


                    // Skip if current month is disabled
                    val recurrenceDate = Calendar.getInstance().apply { time = startDate ?: Date() }
                    val calculationDate = calculateRecurrenceDate(
                        now,
                        recurrenceDate,
                        recurrenceType,
                        recurrenceFrequency,
                        assignmentType
                    )

                    Timber.d(
                        "Calculated recurrence date: ${sdf.format(calculationDate.time)}, Last submitted date: ${
                            sdf.format(
                                lastSubmittedDate.time
                            )
                        }"
                    )

                    if (lastSubmittedDate.after(calculationDate) || isSameDay(
                            lastSubmittedDate,
                            calculationDate
                        )
                    ) {
                        disableEntry = true
                    }
                }
            }
        }

        if (disableEntry)
        {
            //need to check if the survey duration is 22, then we can set it to completed, else in active from surveyduration type table
        }

        return when {
            disableMonth -> ModuleStatus.IN_ACTIVE
            item.statusStarted == 1 && item.statusFinished == 1  && !disableEntry-> ModuleStatus.COMPLETED
            item.statusStarted == 1 -> ModuleStatus.IN_PROGRESS
            disableEntry && item.surveyDuration==22 -> ModuleStatus.ACTIVE
            disableEntry -> ModuleStatus.COMPLETED
            hasSubmitted -> ModuleStatus.ACTIVE // new cycle started
            else -> ModuleStatus.ACTIVE
        }
    }

    private fun calculateRecurrenceDate(
        currentDate: Calendar,
        recurrenceDate: Calendar,
        recurrenceType: String,
        recurrenceFrequency: Int,
        assignmentType: String
    ): Calendar {
        val calculationDate = currentDate.clone() as Calendar
        when (recurrenceType.lowercase(Locale.getDefault())) {
            "years" -> {
                // Special case: India spices survey (assignmentType == "48")
                if (assignmentType == "48") {
                    calculationDate.add(Calendar.YEAR, -recurrenceFrequency)
                    calculationDate.set(Calendar.MONTH, Calendar.JULY)
                    calculationDate.set(Calendar.DAY_OF_MONTH, 31)
                } else {
                    val nextRecurrence = recurrenceDate.clone() as Calendar
                    nextRecurrence.add(Calendar.YEAR, recurrenceFrequency)
                    while (!currentDate.before(nextRecurrence)) {
                        recurrenceDate.add(Calendar.YEAR, recurrenceFrequency)
                        nextRecurrence.add(Calendar.YEAR, recurrenceFrequency)
                    }
                    calculationDate.time = recurrenceDate.time
                }
            }

            "months" -> {
                val nextRecurrence = recurrenceDate.clone() as Calendar
                while (!currentDate.before(nextRecurrence)) {
                    recurrenceDate.add(Calendar.MONTH, recurrenceFrequency)
                    nextRecurrence.add(Calendar.MONTH, recurrenceFrequency)
                }
                calculationDate.time = recurrenceDate.time
            }

            "weeks" -> {
                val daysBetween =
                    (currentDate.timeInMillis - recurrenceDate.timeInMillis) / (1000 * 60 * 60 * 24)
                val weekDifference = (daysBetween / 7).toInt()
                val weeksPassed = weekDifference / recurrenceFrequency
                recurrenceDate.add(Calendar.WEEK_OF_YEAR, weeksPassed * recurrenceFrequency)
                calculationDate.time = recurrenceDate.time
            }

            else -> { // daily
                val nextRecurrence = recurrenceDate.clone() as Calendar
                nextRecurrence.add(Calendar.DAY_OF_YEAR, recurrenceFrequency)
                while (!currentDate.before(nextRecurrence)) {
                    recurrenceDate.add(Calendar.DAY_OF_YEAR, recurrenceFrequency)
                    nextRecurrence.add(Calendar.DAY_OF_YEAR, recurrenceFrequency)
                }
                calculationDate.time = recurrenceDate.time
            }
        }
        return calculationDate
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}

enum class ModuleStatus {
    ACTIVE, IN_PROGRESS, COMPLETED, NOT_AVAILABLE,IN_ACTIVE
}
