package com.rithwik.integrationworkbench.data.db

import androidx.room.TypeConverter
import com.rithwik.integrationworkbench.domain.model.EventType
import com.rithwik.integrationworkbench.domain.model.Status
import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.AdNetwork

class Converters {
    @TypeConverter
    fun eventTypeToString(value: EventType?): String? = value?.name

    @TypeConverter
    fun stringToEventType(value: String?): EventType? =
        value?.let { EventType.valueOf(it) }

    @TypeConverter
    fun statusToString(value: Status?): String? = value?.name

    @TypeConverter
    fun stringToStatus(value: String?): Status? =
        value?.let { Status.valueOf(it) }

    @TypeConverter
    fun adFormatToString(value: AdFormat?): String? = value?.name

    @TypeConverter
    fun stringToAdFormat(value: String?): AdFormat? =
        value?.let { AdFormat.valueOf(it) }

    @TypeConverter
    fun adNetworkToString(value: AdNetwork?): String? = value?.name

    @TypeConverter
    fun stringToAdNetwork(value: String?): AdNetwork? =
        value?.let { AdNetwork.valueOf(it) }
}
