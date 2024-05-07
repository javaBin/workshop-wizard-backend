package no.javabin.dto

import kotlinx.serialization.Serializable

@Serializable
data class WorkshopListImportDTO(
  val sessions: List<WorkshopImport>,
)

@Serializable
data class WorkshopImport(
  val intendedAudience: String,
  val length: String,
  val format: String,
  val language: String,
  val abstract: String,
  val title: String,
  val room: String,
  val startTime: String,
  val endTime: String,
  val video: String? = "",
  val startTimeZulu: String,
  val endTimeZulu: String,
  val id: String,
  val sessionId: String,
  val conferenceId: String,
  val startSlot: String,
  val startSlotZulu: String,
  val speakers: List<Speaker>,
  val workshopPrerequisites: String? = "",
  val registerLoc: String? = "",
)

@Serializable
data class Speaker(
  val name: String,
  val twitter: String? = "",
  val bio: String,
)
