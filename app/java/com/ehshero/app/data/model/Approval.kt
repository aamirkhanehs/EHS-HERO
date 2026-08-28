package com.ehshero.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firestore collection: `approvals/{approvalId}`.
 *
 * Kept as its own audit-trail collection (rather than just a status field on
 * SafetyActivity) per spec section 19's explicit call for an APPROVALS
 * entity and section 20's auditability requirement: this is the permanent
 * record of *who* decided *what*, separate from the submission itself.
 */
data class Approval(
    var approvalId: String = "",
    var activityId: String = "",
    var userId: String = "",
    var reviewedByUid: String = "",
    var reviewedByName: String = "",
    var decision: String = ActivityStatus.PENDING.name,
    var xpAwarded: Int = 0,
    var rejectionReason: String = "",
    @ServerTimestamp
    var reviewedAt: Date? = null
)
