package com.remlocteam.remloc1.Data

data class ActionsData(val contactName: String? = null,
                       val phoneNumber: String? = null,
                       val smsText: String? = null,
                       val placeName: String? = null,
                       val actionType: String? = null,
                       var latitude: Double? = null,
                       var longitude: Double? = null,
                       var turnOn: Boolean = true
)
