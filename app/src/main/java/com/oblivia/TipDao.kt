```kotlin
package com.oblivia

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TipDao {
    @Insert
    suspend fun insert(tip: Tip)

    @Query("SELECT * FROM tips ORDER BY id DESC")
    suspend fun getAllTips(): List<Tip>
}
```
