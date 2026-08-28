package com.ehshero.app.data.model

/** Firestore collection: `badges/{badgeId}`. Static catalog, Admin-managed
 * (spec section 8). See data/seed/DefaultConfig.kt for the starter set. */
data class Badge(
    var badgeId: String = "",
    var name: String = "",
    var description: String = "",
    var iconId: String = "shield",
    var criteriaType: String = BadgeCriteriaType.APPROVED_COUNT.name,
    var criteriaValue: Int = 0
)
