package com.maxlab.data.db

import androidx.room.TypeConverter
import com.maxlab.domain.model.AdFormat
import com.maxlab.domain.model.EventCategory

class Converters {
    @TypeConverter
    fun eventCategoryToString(value: EventCategory?): String? = value?.name

    @TypeConverter
    fun stringToEventCategory(value: String?): EventCategory? =
        value?.let { EventCategory.valueOf(it) }

    @TypeConverter
    fun adFormatToString(value: AdFormat?): String? = value?.name

    @TypeConverter
    fun stringToAdFormat(value: String?): AdFormat? =
        value?.let { AdFormat.valueOf(it) }
}
