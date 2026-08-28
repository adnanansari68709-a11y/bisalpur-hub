package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE (email = :identifier OR phone = :identifier) LIMIT 1")
    suspend fun getUserByIdentifier(identifier: String): UserEntity?

    @Query("""
        SELECT * FROM users 
        WHERE (email IS NOT NULL AND email != '' AND (LOWER(email) = :lowercaseIdentifier OR LOWER(email) = :identifier))
           OR (phone IS NOT NULL AND phone != '' AND (
               phone = :identifier 
               OR phone = :normalizedPhone 
               OR phone = :tenDigits 
               OR phone = '+91' || :tenDigits
               OR phone = '91' || :tenDigits
               OR phone = '0' || :tenDigits
               OR (length(:tenDigits) = 10 AND phone LIKE '%' || :tenDigits)
               OR (length(:tenDigits) = 10 AND REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(phone, ' ', ''), '-', ''), '+', ''), '(', ''), ')', '') LIKE '%' || :tenDigits)
           ))
        LIMIT 1
    """)
    suspend fun findUserByIdentifier(
        identifier: String,
        lowercaseIdentifier: String,
        normalizedPhone: String,
        tenDigits: String
    ): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?
    
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserFlow(userId: Long): Flow<UserEntity?>

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE id = :userId")
    suspend fun updatePassword(userId: Long, newPasswordHash: String)

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE email = :email")
    suspend fun updatePasswordByEmail(email: String, newPasswordHash: String)

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE phone = :phone")
    suspend fun updatePasswordByPhone(phone: String, newPasswordHash: String)

    @Query("""
        UPDATE users 
        SET passwordHash = :newPasswordHash 
        WHERE phone = :phone 
           OR phone = :normalizedPhone 
           OR phone = :tenDigits 
           OR (length(:tenDigits) >= 10 AND phone LIKE '%' || :tenDigits)
    """)
    suspend fun updatePasswordByPhoneMulti(
        phone: String,
        normalizedPhone: String,
        tenDigits: String,
        newPasswordHash: String
    )
}

