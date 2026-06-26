package com.example

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "calculation_history")
data class CalculationHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface CalculationHistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllHistory(): Flow<List<CalculationHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: CalculationHistory)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAllHistory()

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)
}

@Database(entities = [CalculationHistory::class], version = 1, exportSchema = false)
abstract class CalculationDatabase : RoomDatabase() {
    abstract fun historyDao(): CalculationHistoryDao
}

class CalculationRepository(private val dao: CalculationHistoryDao) {
    val allHistory: Flow<List<CalculationHistory>> = dao.getAllHistory()

    suspend fun insert(expression: String, result: String) {
        if (expression.isNotBlank() && result.isNotBlank()) {
            dao.insertHistory(CalculationHistory(expression = expression, result = result))
        }
    }

    suspend fun clear() {
        dao.clearAllHistory()
    }

    suspend fun deleteById(id: Int) {
        dao.deleteHistoryById(id)
    }
}
