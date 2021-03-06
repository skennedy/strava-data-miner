package com.github.skennedy.stravadataminer

import kiambogo.scrava.models.{PersonalActivitySummary, Polyline}

trait StravaApiStubs {
  
  def stubPersonalActivitySummary(
    id: Int,
    resource_state: Int = 0,
    external_id: Option[String] = None,
    upload_id: Option[Int] = None,
    athlete: Map[String, Int] = Map(),
    name: String = "",
    distance: Float = 0,
    moving_time: Int = 0,
    elapsed_time: Int = 0,
    total_elevation_gain: Float = 0,
    `type`: String = "",
    start_date: String = "",
    start_date_local: String = "",
    timezone: String = "",
    start_latlng: List[Float] = List(),
    end_latlng: Option[List[Float]] = None,
    location_city: Option[String] = None,
    location_state: Option[String] = None,
    location_country: Option[String] = None,
    achievement_count: Int = 0,
    kudos_count: Int = 0,
    comment_count: Int = 0,
    athlete_count: Int = 0,
    photo_count: Int = 0,
    map: Polyline = Polyline("", None, None, 0),
    trainer: Boolean = false,
    commute: Boolean = false,
    manual: Boolean = false,
    `private`: Boolean = false,
    flagged: Boolean = false,
    gear_id: Option[String] = None,
    average_speed: Float = 0,
    max_speed: Float = 0,
    average_cadence: Option[Float] = None,
    average_temp: Option[Int] = None,
    average_watts: Option[Float] = None,
    kilojoules: Option[Float] = None,
    device_watts: Option[Boolean] = None,
    average_heartrate: Option[Float] = None,
    max_heartrate: Option[Float] = None,
    total_photo_count: Int = 0,
    has_kudoed: Boolean = false,
    workout_type: Option[Int] = None) =
    PersonalActivitySummary(
      id,
      resource_state,
      external_id,
      upload_id,
      athlete,
      name,
      distance,
      moving_time,
      elapsed_time,
      total_elevation_gain,
      `type`,
      start_date,
      start_date_local,
      timezone,
      start_latlng,
      end_latlng,
      location_city,
      location_state,
      location_country,
      achievement_count,
      kudos_count,
      comment_count,
      athlete_count,
      photo_count,
      map,
      trainer,
      commute,
      manual,
      `private`,
      flagged,
      gear_id,
      average_speed,
      max_speed,
      average_cadence,
      average_temp,
      average_watts,
      kilojoules,
      device_watts,
      average_heartrate,
      max_heartrate,
      total_photo_count,
      has_kudoed,
      workout_type)
}
