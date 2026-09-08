package com.tradedharma.app.data.local

import androidx.room.TypeConverter
import com.tradedharma.app.domain.model.InstrumentType
import com.tradedharma.app.domain.model.OptionType
import com.tradedharma.app.domain.model.TradeDirection

class Converters {
    @TypeConverter fun instrumentTypeToString(value: InstrumentType): String = value.name
    @TypeConverter fun stringToInstrumentType(value: String): InstrumentType = InstrumentType.valueOf(value)
    @TypeConverter fun directionToString(value: TradeDirection): String = value.name
    @TypeConverter fun stringToDirection(value: String): TradeDirection = TradeDirection.valueOf(value)
    @TypeConverter fun optionTypeToString(value: OptionType?): String? = value?.name
    @TypeConverter fun stringToOptionType(value: String?): OptionType? = value?.let { OptionType.valueOf(it) }
}
