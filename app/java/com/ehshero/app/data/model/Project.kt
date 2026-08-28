package com.ehshero.app.data.model

/** Firestore collection: `projects/{projectId}`. */
data class Project(
    var projectId: String = "",
    var name: String = "",
    var location: String = "",
    var type: String = "Transmission Line",
    var active: Boolean = true
)
