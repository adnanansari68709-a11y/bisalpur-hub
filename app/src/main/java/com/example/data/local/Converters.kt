package com.example.data.local

import androidx.room.TypeConverter
import com.example.domain.model.Role

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromRole(role: Role): String {
        return role.name
    }

    @TypeConverter
    fun toRole(role: String): Role {
        return Role.valueOf(role)
    }
}
