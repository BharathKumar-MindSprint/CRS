package com.olam.digital.crs.extension

import com.olam.digital.crs.room.ConditionalQuestionDetailEntity
import com.olam.digital.crs.room.ConditionalTypeEntity
import timber.log.Timber
import kotlin.collections.get

class CrsSkipLogicEvaluator(
    details: List<ConditionalQuestionDetailEntity>,
    private val conditionTypeById: Map<Long, ConditionalTypeEntity>
) {
    private val rulesByTrigger: Map<Long, List<ConditionalQuestionDetailEntity>> =
        details.filter { it.isDeleted != true }.groupBy { it.questionId }

    /** questionIds whose conditional rule is currently satisfied, given answers keyed by questionId */
    /*fun triggeredQuestionIds(answers: Map<Long, Any?>): Set<Long> {
        val result = mutableSetOf<Long>()
        rulesByTrigger.forEach { (triggerId, rules) ->
            val answer = answers[triggerId]
            rules.forEach { rule ->
                if (ruleFires(rule, answer)) result += parseIds(rule.skipQuestionIds ?: rule.skipQuestions)
            }
        }
        return result
    }*/
    fun triggeredQuestionIds(answers: Map<Long, Any?>): Set<Long> {
        val result = mutableSetOf<Long>()
        Timber.d("SKIP_EVAL: answers=$answers")
        Timber.d("SKIP_EVAL: rulesByTrigger keys=${rulesByTrigger.keys}")
        rulesByTrigger.forEach { (triggerId, rules) ->
            val answer = answers[triggerId]
            rules.forEach { rule ->
                val fires = ruleFires(rule, answer)
                Timber.d("SKIP_EVAL: trigger=$triggerId answer=$answer isAnswered=${rule.isAnswered} condText=${rule.conditionalText} lookupIds=${rule.lookUpValueIds ?: rule.lookupIds} fires=$fires → skips=${rule.skipQuestionIds ?: rule.skipQuestions}")
                if (fires) result += parseIds(rule.skipQuestionIds ?: rule.skipQuestions)
            }
        }
        Timber.d("SKIP_EVAL: triggered=$result")
        return result
    }


    /** True if any rule for this trigger question is satisfied by the given answer. */
    fun isSatisfied(questionId: Long, answer: Any?): Boolean =
        rulesByTrigger[questionId]?.any { ruleFires(it, answer) } == true
  /*  private fun ruleFires(rule: ConditionalQuestionDetailEntity, answer: Any?): Boolean = when {
        rule.isAnswered == true -> !isEmpty(answer)
        !rule.conditionalText.isNullOrEmpty() -> matchesCondition(rule, answer)   // handles text + numeric
        !rule.lookUpValueIds.isNullOrEmpty() || !rule.lookupIds.isNullOrEmpty() -> {
            val ids = parseIds(rule.lookUpValueIds ?: rule.lookupIds)
            selectedOptionIds(answer).any { it in ids }
        }
        else -> false
    }*/

    private fun ruleFires(rule: ConditionalQuestionDetailEntity, answer: Any?): Boolean {
        val type = conditionTypeById[rule.conditionalId] ?: return false
        val op = (type.operator ?: type.value).orEmpty().trim().lowercase()

        return when {
            // "Is answered" type
            op == "is answered" -> !isEmpty(answer)

            // Lookup option match
            !rule.lookUpValueIds.isNullOrEmpty() || !rule.lookupIds.isNullOrEmpty() -> {
                val ids = parseIds(rule.lookUpValueIds ?: rule.lookupIds)
                selectedOptionIds(answer).any { it in ids }
            }

            // Numeric/text comparison
            !rule.conditionalText.isNullOrEmpty() -> matchesCondition(rule,  answer)

            else -> false
        }
    }


    private fun matchesCondition(rule: ConditionalQuestionDetailEntity, answer: Any?): Boolean {
        val type = conditionTypeById[rule.conditionalId] ?: return false
        val op = (type.operator ?: type.value).orEmpty().trim().lowercase()
        val expected = rule.conditionalText?.trim() ?: return false

        val answerNum = numericValue(answer)
        val expectedNum = expected.toDoubleOrNull()


        Timber.d("Comparing values for rule ${rule.id}: answer='$answer' expected='$expected' op='$op' answerNum=$answerNum expectedNum=$expectedNum")
        // Numeric comparison when both sides are numeric
        if (answerNum != null && expectedNum != null) {
            return when (op) {
                ">",  "greater than"                 -> answerNum > expectedNum
                "<",  "lesser than", "less than"     -> answerNum < expectedNum
                "=",  "==", "equal to", "equals"     -> answerNum == expectedNum
                ">=", "greater than equal to"        -> answerNum >= expectedNum
                "<=", "lesser than equal to"         -> answerNum <= expectedNum
                "!=", "<>", "not equal to"           -> answerNum != expectedNum
                else -> false
            }
        }

        // Text comparison otherwise (this is the "test" case)
        val answerStr = answer?.toString()?.trim() ?: return false
        return when (op) {
            "=", "==", "equal to", "equals"  -> answerStr.equals(expected, ignoreCase = true)
            "!=", "<>", "not equal to"       -> !answerStr.equals(expected, ignoreCase = true)
            "contains"                       -> answerStr.contains(expected, ignoreCase = true)
            else -> answerStr.equals(expected, ignoreCase = true)   // safe default for text rules
        }
    }

    private fun numericValue(a: Any?): Double? = when (a) {
        is Number -> a.toDouble(); is String -> a.toDoubleOrNull(); else -> null
    }
    private fun selectedOptionIds(a: Any?): List<Long> = when (a) {
        is Long -> listOf(a); is Int -> listOf(a.toLong())
        is String -> a.toLongOrNull()?.let(::listOf) ?: emptyList()
        is List<*> -> a.mapNotNull { when (it) { is Long -> it; is Int -> it.toLong(); is String -> it.toLongOrNull(); else -> null } }
        else -> emptyList()
    }
    private fun isEmpty(a: Any?): Boolean = when (a) {
        null -> true; is String -> a.isBlank(); is List<*> -> a.isEmpty(); else -> false
    }
    private fun parseIds(raw: String?): Set<Long> =
        raw?.replace("[", "")?.replace("]", "")
            ?.split(",")?.mapNotNull { it.trim().toLongOrNull() }?.toSet() ?: emptySet()
}