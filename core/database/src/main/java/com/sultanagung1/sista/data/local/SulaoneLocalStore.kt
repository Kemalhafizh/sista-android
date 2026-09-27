package com.sultanagung1.sista.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sultanagung1.sista.data.local.entity.FormDraftRow
import com.sultanagung1.sista.data.local.entity.PendingActionItem
import com.sultanagung1.sista.data.model.*
import java.util.UUID

class SulaoneLocalStore(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "sulaone_offline_store.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_CACHE = "cached_records"
        private const val COL_CACHE_KEY = "cache_key"
        private const val COL_CACHE_DATA = "json_data"
        private const val COL_CACHE_CATEGORY = "category"
        private const val COL_CACHE_UPDATED = "updated_at"

        private const val TABLE_QUEUE = "pending_sync_actions"
        private const val COL_QUEUE_ID = "id"
        private const val COL_QUEUE_ACTION = "action_type"
        private const val COL_QUEUE_PAYLOAD = "payload_json"
        private const val COL_QUEUE_CREATED = "created_at"
        private const val COL_QUEUE_RETRY = "retry_count"
        private const val COL_QUEUE_STATUS = "status"

        // FASE 69.2: local form-draft persistence — survives full app restarts,
        // unlike SavedStateHandle which only survives within the same Activity task.
        private const val TABLE_DRAFTS = "form_drafts"
        private const val COL_DRAFT_FORM_ID = "form_id"
        private const val COL_DRAFT_USER_ID = "user_id"
        private const val COL_DRAFT_PAYLOAD = "payload_json"
        private const val COL_DRAFT_UPDATED = "updated_at"

        @Volatile
        private var INSTANCE: SulaoneLocalStore? = null

        fun getInstance(context: Context): SulaoneLocalStore {
            return INSTANCE ?: synchronized(this) {
                val instance = SulaoneLocalStore(context)
                INSTANCE = instance
                instance
            }
        }
    }

    private val gson = Gson()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_CACHE (
                $COL_CACHE_KEY TEXT PRIMARY KEY,
                $COL_CACHE_DATA TEXT NOT NULL,
                $COL_CACHE_CATEGORY TEXT NOT NULL,
                $COL_CACHE_UPDATED INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_QUEUE (
                $COL_QUEUE_ID TEXT PRIMARY KEY,
                $COL_QUEUE_ACTION TEXT NOT NULL,
                $COL_QUEUE_PAYLOAD TEXT NOT NULL,
                $COL_QUEUE_CREATED INTEGER NOT NULL,
                $COL_QUEUE_RETRY INTEGER DEFAULT 0,
                $COL_QUEUE_STATUS TEXT DEFAULT 'PENDING'
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_DRAFTS (
                $COL_DRAFT_FORM_ID TEXT PRIMARY KEY,
                $COL_DRAFT_USER_ID TEXT,
                $COL_DRAFT_PAYLOAD TEXT NOT NULL,
                $COL_DRAFT_UPDATED INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CACHE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUEUE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DRAFTS")
        onCreate(db)
    }

    // ==========================================
    // 1. Generic Offline Cache Operations
    // ==========================================

    @Synchronized
    fun <T> saveCache(key: String, data: T, category: String = "general") {
        try {
            val json = gson.toJson(data)
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COL_CACHE_KEY, key)
                put(COL_CACHE_DATA, json)
                put(COL_CACHE_CATEGORY, category)
                put(COL_CACHE_UPDATED, System.currentTimeMillis())
            }
            db.insertWithOnConflict(TABLE_CACHE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        } catch (_: Exception) {}
    }

    @Synchronized
    fun <T> getCache(key: String, clazz: Class<T>): T? {
        return try {
            val db = readableDatabase
            val cursor = db.query(
                TABLE_CACHE,
                arrayOf(COL_CACHE_DATA),
                "$COL_CACHE_KEY = ?",
                arrayOf(key),
                null, null, null
            )
            var result: T? = null
            cursor.use {
                if (it.moveToFirst()) {
                    val json = it.getString(0)
                    result = gson.fromJson(json, clazz)
                }
            }
            result
        } catch (_: Exception) {
            null
        }
    }

    @Synchronized
    fun getCacheJson(key: String): String? {
        return try {
            val db = readableDatabase
            val cursor = db.query(
                TABLE_CACHE,
                arrayOf(COL_CACHE_DATA),
                "$COL_CACHE_KEY = ?",
                arrayOf(key),
                null, null, null
            )
            var result: String? = null
            cursor.use {
                if (it.moveToFirst()) {
                    result = it.getString(0)
                }
            }
            result
        } catch (_: Exception) {
            null
        }
    }

    // ==========================================
    // 2. Offline Action Queue (Mutations)
    // ==========================================

    @Synchronized
    fun <T> enqueueAction(actionType: String, payload: T): String {
        val id = UUID.randomUUID().toString()
        try {
            val json = gson.toJson(payload)
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COL_QUEUE_ID, id)
                put(COL_QUEUE_ACTION, actionType)
                put(COL_QUEUE_PAYLOAD, json)
                put(COL_QUEUE_CREATED, System.currentTimeMillis())
                put(COL_QUEUE_RETRY, 0)
                put(COL_QUEUE_STATUS, "PENDING")
            }
            db.insert(TABLE_QUEUE, null, values)
        } catch (_: Exception) {}
        return id
    }

    @Synchronized
    fun getPendingActions(): List<PendingActionItem> {
        val list = mutableListOf<PendingActionItem>()
        try {
            val db = readableDatabase
            val cursor = db.query(
                TABLE_QUEUE,
                null,
                "$COL_QUEUE_STATUS = ?",
                arrayOf("PENDING"),
                null, null, "$COL_QUEUE_CREATED ASC"
            )
            cursor.use {
                while (it.moveToNext()) {
                    list.add(
                        PendingActionItem(
                            id = it.getString(it.getColumnIndexOrThrow(COL_QUEUE_ID)),
                            actionType = it.getString(it.getColumnIndexOrThrow(COL_QUEUE_ACTION)),
                            payloadJson = it.getString(it.getColumnIndexOrThrow(COL_QUEUE_PAYLOAD)),
                            createdAt = it.getLong(it.getColumnIndexOrThrow(COL_QUEUE_CREATED)),
                            retryCount = it.getInt(it.getColumnIndexOrThrow(COL_QUEUE_RETRY)),
                            status = it.getString(it.getColumnIndexOrThrow(COL_QUEUE_STATUS))
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return list
    }

    @Synchronized
    fun getPendingActionsCount(): Int {
        return try {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_QUEUE WHERE $COL_QUEUE_STATUS = 'PENDING'", null)
            var count = 0
            cursor.use {
                if (it.moveToFirst()) count = it.getInt(0)
            }
            count
        } catch (_: Exception) {
            0
        }
    }

    @Synchronized
    fun removeAction(id: String) {
        try {
            val db = writableDatabase
            db.delete(TABLE_QUEUE, "$COL_QUEUE_ID = ?", arrayOf(id))
        } catch (_: Exception) {}
    }

    @Synchronized
    fun incrementRetry(id: String) {
        try {
            val db = writableDatabase
            db.execSQL("UPDATE $TABLE_QUEUE SET $COL_QUEUE_RETRY = $COL_QUEUE_RETRY + 1 WHERE $COL_QUEUE_ID = ?", arrayOf(id))
        } catch (_: Exception) {}
    }

    // ==========================================
    // 3. Local Form Draft Persistence (FASE 69.2)
    // ==========================================

    @Synchronized
    fun saveDraft(formId: String, userId: String?, payloadJson: String) {
        try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COL_DRAFT_FORM_ID, formId)
                put(COL_DRAFT_USER_ID, userId)
                put(COL_DRAFT_PAYLOAD, payloadJson)
                put(COL_DRAFT_UPDATED, System.currentTimeMillis())
            }
            db.insertWithOnConflict(TABLE_DRAFTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        } catch (_: Exception) {}
    }

    @Synchronized
    fun getDraft(formId: String): FormDraftRow? {
        return try {
            val db = readableDatabase
            val cursor = db.query(
                TABLE_DRAFTS,
                arrayOf(COL_DRAFT_PAYLOAD, COL_DRAFT_UPDATED),
                "$COL_DRAFT_FORM_ID = ?",
                arrayOf(formId),
                null, null, null
            )
            var result: FormDraftRow? = null
            cursor.use {
                if (it.moveToFirst()) {
                    result = FormDraftRow(
                        payloadJson = it.getString(0),
                        updatedAt = it.getLong(1)
                    )
                }
            }
            result
        } catch (_: Exception) {
            null
        }
    }

    @Synchronized
    fun deleteDraft(formId: String) {
        try {
            val db = writableDatabase
            db.delete(TABLE_DRAFTS, "$COL_DRAFT_FORM_ID = ?", arrayOf(formId))
        } catch (_: Exception) {}
    }

    // ==========================================
    // 4. Typed Helpers for Modules
    // ==========================================

    fun saveSchedule(schedules: List<ScheduleItem>) {
        saveCache("schedules_cache", schedules, "academic")
    }

    fun getCachedSchedule(): List<ScheduleItem>? {
        val json = getCacheJson("schedules_cache") ?: return null
        return try {
            val type = object : TypeToken<List<ScheduleItem>>() {}.type
            gson.fromJson(json, type)
        } catch (_: Exception) {
            null
        }
    }

    fun saveGrades(grades: List<GradeEntry>) {
        saveCache("grades_cache", grades, "academic")
    }

    fun getCachedGrades(): List<GradeEntry>? {
        val json = getCacheJson("grades_cache") ?: return null
        return try {
            val type = object : TypeToken<List<GradeEntry>>() {}.type
            gson.fromJson(json, type)
        } catch (_: Exception) {
            null
        }
    }

    fun saveMutabaah(activities: List<MutabaahLogItem>) {
        saveCache("mutabaah_cache", activities, "ibadah")
    }

    fun getCachedMutabaah(): List<MutabaahLogItem>? {
        val json = getCacheJson("mutabaah_cache") ?: return null
        return try {
            val type = object : TypeToken<List<MutabaahLogItem>>() {}.type
            gson.fromJson(json, type)
        } catch (_: Exception) {
            null
        }
    }

    fun saveAnnouncements(announcements: List<AnnouncementItem>) {
        saveCache("announcements_cache", announcements, "announcements")
    }

    fun getCachedAnnouncements(): List<AnnouncementItem>? {
        val json = getCacheJson("announcements_cache") ?: return null
        return try {
            val type = object : TypeToken<List<AnnouncementItem>>() {}.type
            gson.fromJson(json, type)
        } catch (_: Exception) {
            null
        }
    }

    // ==========================================
    // 5. Tombstone Handling (Ghost Data Purge - FASE 61.2)
    // ==========================================

    @Synchronized
    fun removeCacheKey(key: String) {
        try {
            val db = writableDatabase
            db.delete(TABLE_CACHE, "$COL_CACHE_KEY = ?", arrayOf(key))
        } catch (_: Exception) {}
    }

    @Synchronized
    fun purgeTombstones(category: String, deletedIds: List<String>) {
        if (deletedIds.isEmpty()) return
        val idSet = deletedIds.toSet()
        when (category.lowercase()) {
            "announcements", "announcement" -> {
                val current = getCachedAnnouncements() ?: return
                val filtered = current.filterNot { it.id in idSet }
                saveAnnouncements(filtered)
            }
            "schedules", "schedule" -> {
                val current = getCachedSchedule() ?: return
                val filtered = current.filterNot { it.id.toString() in idSet }
                saveSchedule(filtered)
            }
        }
    }
}

