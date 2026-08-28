package com.ehshero.app.data.model

/** Firestore doc: `settings/pointRules`. Admin-editable overrides of
 * [ActivityType.defaultXp] (spec section 6: "Make the level/point system
 * configurable by Admin"). Keys are ActivityType names, values are XP. */
data class PointRulesSettings(
    var xpByActivityType: Map<String, Long> = emptyMap()
)
