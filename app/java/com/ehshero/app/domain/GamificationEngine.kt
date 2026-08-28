package com.ehshero.app.domain

import com.ehshero.app.data.model.Badge
import com.ehshero.app.data.model.BadgeCriteriaType
import com.ehshero.app.data.model.LevelDef
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * Pure, stateless gamification rules - no Firebase, no Android framework
 * classes. This is the one place that defines "what does approving an
 * activity actually do to a user's level / streak / badges", so repository
 * code should always go through here rather than re-deriving the rules.
 *
 * SAFETY ACTIVITY -> APPROVAL -> XP -> LEVEL -> BADGE -> RANK -> REWARD
 * (spec section 31's core philosophy, implemented literally as a pipeline.)
 */
object GamificationEngine {

    /** The level a user with [totalXp] currently sits at, given a level
     * table. Falls back to a sane Level 1 default if [levels] is empty
     * (e.g. a fresh Firebase project that hasn't been seeded yet). */
    fun currentLevel(totalXp: Int, levels: List<LevelDef>): LevelDef {
        if (levels.isEmpty()) return LevelDef(1, "Safety Rookie", 0)
        val sorted = levels.sortedBy { it.levelNumber }
        return sorted.lastOrNull { totalXp >= it.xpRequired } ?: sorted.first()
    }

    /** Everything a level/XP progress bar needs to render itself. */
    fun progress(totalXp: Int, levels: List<LevelDef>): LevelProgress {
        val sorted = levels.sortedBy { it.levelNumber }
        val current = currentLevel(totalXp, sorted)
        val next = sorted.firstOrNull { it.levelNumber == current.levelNumber + 1 }
        val xpIntoLevel = (totalXp - current.xpRequired).coerceAtLeast(0)
        val xpSpanOfLevel = ((next?.xpRequired ?: current.xpRequired) - current.xpRequired).coerceAtLeast(0)
        val fraction = if (next == null || xpSpanOfLevel <= 0) {
            1f
        } else {
            (xpIntoLevel.toFloat() / xpSpanOfLevel.toFloat()).coerceIn(0f, 1f)
        }
        return LevelProgress(
            currentLevel = current,
            nextLevel = next,
            xpIntoLevel = xpIntoLevel,
            xpForNextLevel = xpSpanOfLevel,
            progress = fraction
        )
    }

    /** Returns the new [LevelDef] if crediting XP pushed the user into a
     * higher level than before, so the caller can trigger the full-screen
     * "LEVEL UP!" animation - null if the level didn't change. */
    fun levelUpIfAny(oldXp: Int, newXp: Int, levels: List<LevelDef>): LevelDef? {
        val before = currentLevel(oldXp, levels)
        val after = currentLevel(newXp, levels)
        return if (after.levelNumber > before.levelNumber) after else null
    }

    /**
     * Streak logic (spec section 10). Compares calendar days - using
     * java.time, natively available from API 26+ without desugaring - so a
     * submission just after midnight doesn't wrongly reset a streak, unlike
     * naive epoch-millis-division bucketing.
     *
     * - Same calendar day as the last approved activity -> streak unchanged.
     * - Exactly one day later -> streak increments.
     * - Any bigger gap (or no prior activity) -> streak restarts at 1.
     */
    fun nextStreakCount(
        previousStreak: Int,
        lastActivityMillis: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): Int {
        if (lastActivityMillis <= 0L) return 1
        val zone = java.time.ZoneId.systemDefault()
        val lastDay = Date(lastActivityMillis).toInstant().atZone(zone).toLocalDate()
        val today = Date(nowMillis).toInstant().atZone(zone).toLocalDate()
        val daysBetween = ChronoUnit.DAYS.between(lastDay, today)
        return when {
            daysBetween == 0L -> previousStreak.coerceAtLeast(1)
            daysBetween == 1L -> previousStreak + 1
            else -> 1
        }
    }

    /** +50 XP every 7-day streak milestone, per spec section 10. Configurable
     * cadence/reward could move to Firestore settings later; kept as a
     * constant for the MVP and called out as such in the README. */
    fun streakBonusXp(newStreak: Int, streakIntervalDays: Int = 7, bonusXp: Int = 50): Int =
        if (newStreak > 0 && newStreak % streakIntervalDays == 0) bonusXp else 0

    /**
     * Given a user's updated activity counters, returns any badges from
     * [allBadges] whose criteria are now satisfied and that aren't already
     * unlocked. MONTHLY_RANK_1 is deliberately excluded - see
     * [isMonthlyChampion], which needs the whole leaderboard rather than one
     * user's counters and is evaluated separately.
     */
    fun newlyUnlockedBadges(
        counts: UserActivityCounts,
        allBadges: List<Badge>,
        alreadyUnlockedBadgeIds: Set<String>
    ): List<Badge> = allBadges.filter { badge ->
        if (badge.badgeId in alreadyUnlockedBadgeIds) return@filter false
        val criteria = runCatching { BadgeCriteriaType.valueOf(badge.criteriaType) }.getOrNull()
            ?: return@filter false
        when (criteria) {
            BadgeCriteriaType.OBSERVATION_COUNT -> counts.approvedObservationCount >= badge.criteriaValue
            BadgeCriteriaType.TBT_COUNT -> counts.tbtCount >= badge.criteriaValue
            BadgeCriteriaType.HAZARD_COUNT -> counts.hazardCount >= badge.criteriaValue
            BadgeCriteriaType.APPROVED_COUNT -> counts.approvedTotalCount >= badge.criteriaValue
            BadgeCriteriaType.NEAR_MISS_COUNT -> counts.nearMissCount >= badge.criteriaValue
            BadgeCriteriaType.TRAINING_COUNT -> counts.trainingCount >= badge.criteriaValue
            BadgeCriteriaType.GOOD_PRACTICE_COUNT -> counts.goodPracticeCount >= badge.criteriaValue
            BadgeCriteriaType.RESCUE_COUNT -> counts.rescueCount >= badge.criteriaValue
            BadgeCriteriaType.TOTAL_XP -> counts.totalXp >= badge.criteriaValue
            BadgeCriteriaType.MONTHLY_RANK_1 -> false
        }
    }

    /** Called from the monthly-champion flow (HSE dashboard) once a
     * leaderboard winner for the period is known, rather than per-activity. */
    fun isMonthlyChampion(isRankOneThisMonth: Boolean): Boolean = isRankOneThisMonth
}

data class LevelProgress(
    val currentLevel: LevelDef,
    val nextLevel: LevelDef?,
    val xpIntoLevel: Int,
    val xpForNextLevel: Int,
    /** 0f..1f fill amount for the current level's XP bar/ring. */
    val progress: Float
)

/** Snapshot of one user's APPROVED activity counters, used purely to check
 * badge eligibility. Computed by GamificationRepository from xpTransactions
 * / activities, never stored as its own Firestore document. */
data class UserActivityCounts(
    val approvedObservationCount: Int = 0,
    val tbtCount: Int = 0,
    val hazardCount: Int = 0,
    val approvedTotalCount: Int = 0,
    val nearMissCount: Int = 0,
    val trainingCount: Int = 0,
    val goodPracticeCount: Int = 0,
    val rescueCount: Int = 0,
    val totalXp: Int = 0
)
