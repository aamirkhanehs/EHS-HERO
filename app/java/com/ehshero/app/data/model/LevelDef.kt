package com.ehshero.app.data.model

/** Firestore collection: `levels/{levelNumber}` (doc id = level number).
 * Admin-configurable per spec section 7. */
data class LevelDef(
    var levelNumber: Int = 1,
    var title: String = "Safety Rookie",
    var xpRequired: Int = 0
)
