package com.remlocteam.remloc1.Data

data class ActionNotificationData(val placeName: String? = null,
                                  val smsText: String? = null,
                                  val actionType: String? = null,
                                  var latitude: Double? = null,
                                  var longitude: Double? = null,
                                  var turnOn: Boolean = true)
