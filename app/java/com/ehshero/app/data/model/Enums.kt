package com.ehshero.app.data.model

/** The three roles from the spec. Stored on the User doc as a plain string
 * (Firestore-friendly) and parsed back into this enum via [User.roleEnum]. */
enum class UserRole {
    STAFF, HSE, ADMIN
}

enum class UserStatus {
    ACTIVE, DISABLED
}

/**
 * Every submittable HSE activity type, with its default XP value from the
 * spec's point system (section 6). Admins can override these defaults at
 * runtime via the `settings/pointRules` document - [defaultXp] is only the
 * seed/fallback value.
 */
enum class ActivityType(val displayName: String, val defaultXp: Int) {
    TBT("Toolbox Talk (TBT)", 10),
    SAFETY_OBSERVATION("Safety Observation", 15),
    GOOD_PRACTICE("Good Practice", 20),
    INSPECTION("Inspection", 15),
    TRAINING("Safety Training", 25),
    UNSAFE_ACT("Unsafe Act Report", 10),
    UNSAFE_CONDITION("Unsafe Condition Report", 10),
    NEAR_MISS("Near Miss Report", 30),
    SAFETY_SUGGESTION("Safety Suggestion", 20),
    EMERGENCY_DRILL("Emergency Drill Participation", 25),
    MOCK_DRILL("Mock Drill Conducted", 30),
    SAFETY_MEETING("Safety Meeting", 15),
    HAZARD_IDENTIFICATION("Hazard Identification", 20),
    FIRST_AID_AWARENESS("First Aid Awareness", 15),
    RESCUE_TRAINING("Rescue Training", 30),
    HSE_CAMPAIGN("HSE Campaign Participation", 20),
    OTHER("Other", 10);

    companion object {
        fun fromNameOrNull(value: String?): ActivityType? =
            entries.firstOrNull { it.name == value }
    }
}

enum class ActivityStatus {
    PENDING, APPROVED, REJECTED
}

/** Used specifically by the Safety Observation form (spec section 12). */
enum class ObservationType(val displayName: String) {
    UNSAFE_ACT("Unsafe Act"),
    UNSAFE_CONDITION("Unsafe Condition"),
    GOOD_PRACTICE("Good Practice"),
    NEAR_MISS("Near Miss")
}

/** Categories shared by the Safety Observation form and general activity
 * tagging - merges spec sections 12 and 24 into one list. */
enum class HseCategory(val displayName: String) {
    WORK_AT_HEIGHT("Work at Height"),
    ELECTRICAL_SAFETY("Electrical Safety"),
    LIFTING_RIGGING("Lifting & Rigging"),
    EXCAVATION("Excavation"),
    FOUNDATION("Foundation"),
    TOWER_ERECTION("Tower Erection"),
    STRINGING("Stringing"),
    LOTO("LOTO (Lock-Out Tag-Out)"),
    EARTHING("Earthing"),
    LIVE_LINE_CROSSING("Live Line Crossing"),
    HOUSEKEEPING("Housekeeping"),
    PPE("PPE"),
    VEHICLE_SAFETY("Vehicle Safety"),
    MATERIAL_HANDLING("Material Handling"),
    MATERIAL_STACKING("Material Stacking"),
    FIRE_SAFETY("Fire Safety"),
    ENVIRONMENTAL("Environmental Safety"),
    EMERGENCY_PREPAREDNESS("Emergency Preparedness"),
    HEAT_STRESS("Heat Stress"),
    SNAKE_BITE_AWARENESS("Snake Bite Awareness"),
    FIRST_AID("First Aid"),
    RESCUE("Rescue"),
    OTHER("Other")
}

enum class MissionStatus {
    NOT_STARTED, IN_PROGRESS, COMPLETED, APPROVED, EXPIRED
}

enum class NotificationType {
    ACTIVITY_APPROVED,
    ACTIVITY_REJECTED,
    XP_RECEIVED,
    LEVEL_UP,
    BADGE_UNLOCKED,
    MISSION_AVAILABLE,
    CHALLENGE_STARTED,
    RANK_CHANGED,
    MONTHLY_WINNER
}

/** Drives how [com.ehshero.app.domain.GamificationEngine] evaluates whether a
 * badge should unlock for a user - see spec section 8. */
enum class BadgeCriteriaType {
    OBSERVATION_COUNT,
    TBT_COUNT,
    HAZARD_COUNT,
    APPROVED_COUNT,
    NEAR_MISS_COUNT,
    TRAINING_COUNT,
    GOOD_PRACTICE_COUNT,
    RESCUE_COUNT,
    MONTHLY_RANK_1,
    TOTAL_XP
}

enum class LeaderboardPeriod {
    DAILY, WEEKLY, MONTHLY, YEARLY, ALL_TIME
}
