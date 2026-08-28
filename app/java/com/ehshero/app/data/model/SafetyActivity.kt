package com.ehshero.app.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firestore collection: `activities/{activityId}`.
 *
 * Named "SafetyActivity" rather than "Activity" to avoid any confusion with
 * android.app.Activity elsewhere in the codebase.
 *
 * This single flexible shape backs every submission type in spec section 11
 * (TBT, Good Practice, Inspection, Training, Near Miss, Safety Suggestion,
 * Hazard Report, Mock Drill, Safety Meeting, Other) plus the specialised
 * Safety Observation form in section 12 - the observation-only fields are
 * simply left blank for activity types that don't use them.
 *
 * XP is intentionally NOT credited when this document is created. [status]
 * starts at PENDING and [xpValue] records what the activity WOULD be worth;
 * an actual XpTransaction is only written once an HSE user approves it
 * (see GamificationRepository.approveActivity).
 */
data class SafetyActivity(
    var activityId: String = "",
    var userId: String = "",
    var employeeName: String = "",
    var employeeIdText: String = "",
    var projectId: String = "",
    var projectName: String = "",
    var activityType: String = ActivityType.OTHER.name,
    var observationType: String = "",
    var category: String = "",
    var location: String = "",
    var description: String = "",
    var immediateAction: String = "",
    var correctiveAction: String = "",
    var remarks: String = "",
    /** Data-URI / Base64 JPEG thumbnail, kept intentionally small (see
     * PhotoCompressor) so the whole document stays under Firestore's 1 MiB
     * limit. Used instead of Firebase Storage by default so the app runs
     * entirely on Firebase's free Spark plan - see README "Photo storage". */
    var photoBase64: String = "",
    /** Set instead of [photoBase64] when the optional Firebase Storage
     * upgrade path (see README) is enabled. */
    var photoStorageUrl: String = "",
    var status: String = ActivityStatus.PENDING.name,
    var xpValue: Int = 0,
    var missionId: String = "",
    @ServerTimestamp
    var submittedAt: Date? = null,
    var reviewedAt: Date? = null,
    var reviewedByUid: String = "",
    var reviewedByName: String = "",
    var rejectionReason: String = ""
) {
    @get:Exclude
    val activityTypeEnum: ActivityType
        get() = ActivityType.fromNameOrNull(activityType) ?: ActivityType.OTHER

    @get:Exclude
    val statusEnum: ActivityStatus
        get() = runCatching { ActivityStatus.valueOf(status) }.getOrDefault(ActivityStatus.PENDING)

    @get:Exclude
    val hasPhoto: Boolean
        get() = photoBase64.isNotBlank() || photoStorageUrl.isNotBlank()
}
