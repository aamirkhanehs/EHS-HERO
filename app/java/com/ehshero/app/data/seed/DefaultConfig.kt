package com.ehshero.app.data.seed

import com.ehshero.app.data.model.ActivityType
import com.ehshero.app.data.model.Badge
import com.ehshero.app.data.model.BadgeCriteriaType
import com.ehshero.app.data.model.LevelDef

/**
 * The starter configuration from the spec (sections 6, 7, 8), used two ways:
 *  1. [com.ehshero.app.data.seed.DemoDataSeeder] writes these into Firestore
 *     the first time an Admin sets up a new project.
 *  2. Every repository that reads levels/badges/point-rules falls back to
 *     these constants if Firestore returns an empty collection, so the app
 *     never shows a blank/broken screen on a project that hasn't been
 *     seeded yet.
 *
 * All of it is editable later from the Admin screens - this is only the
 * day-one default, matching spec section 7's "make the level system
 * configurable by Admin".
 */
object DefaultConfig {

    val DEFAULT_LEVELS: List<LevelDef> = listOf(
        LevelDef(1, "Safety Rookie", 0),
        LevelDef(2, "Safety Trainee", 100),
        LevelDef(3, "Safety Scout", 250),
        LevelDef(4, "Safety Defender", 500),
        LevelDef(5, "Safety Protector", 800),
        LevelDef(6, "Safety Warrior", 1200),
        LevelDef(7, "Safety Guardian", 1700),
        LevelDef(8, "Safety Commander", 2500),
        LevelDef(9, "Safety Champion", 3500),
        LevelDef(10, "EHS HERO", 5000)
    )

    val DEFAULT_BADGES: List<Badge> = listOf(
        Badge("badge_safety_guardian", "Safety Guardian", "Log 100 Safety Observations", "eye", BadgeCriteriaType.OBSERVATION_COUNT.name, 100),
        Badge("badge_tbt_master", "TBT Master", "Conduct 50 Toolbox Talks", "megaphone", BadgeCriteriaType.TBT_COUNT.name, 50),
        Badge("badge_hazard_hunter", "Hazard Hunter", "Identify 50 hazards", "hazard", BadgeCriteriaType.HAZARD_COUNT.name, 50),
        Badge("badge_safety_warrior", "Safety Warrior", "Get 100 activities approved", "sword", BadgeCriteriaType.APPROVED_COUNT.name, 100),
        Badge("badge_near_miss_hero", "Near Miss Hero", "Report 10 near misses", "alert", BadgeCriteriaType.NEAR_MISS_COUNT.name, 10),
        Badge("badge_training_champion", "Training Champion", "Complete 25 training activities", "graduation", BadgeCriteriaType.TRAINING_COUNT.name, 25),
        Badge("badge_good_practice_hero", "Good Practice Hero", "Log 25 good practices", "thumb_up", BadgeCriteriaType.GOOD_PRACTICE_COUNT.name, 25),
        Badge("badge_rescue_hero", "Rescue Hero", "Complete 5 rescue trainings", "life_ring", BadgeCriteriaType.RESCUE_COUNT.name, 5),
        Badge("badge_safety_champion", "Safety Champion", "Finish #1 on the monthly leaderboard", "crown", BadgeCriteriaType.MONTHLY_RANK_1.name, 1),
        Badge("badge_ehs_hero", "EHS Hero", "Reach 5,000 total XP", "shield_star", BadgeCriteriaType.TOTAL_XP.name, 5000)
    )

    /** Default XP-per-activity-type, derived from [ActivityType.defaultXp]
     * so the spec's point table (section 6) only has to live in one place. */
    val DEFAULT_POINT_RULES: Map<String, Int> =
        ActivityType.entries.associate { it.name to it.defaultXp }
}
