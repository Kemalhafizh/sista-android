package com.sultanagung1.sista.core.storage

import com.sultanagung1.sista.data.local.SulaoneLocalStore
import com.sultanagung1.sista.data.local.entity.FormDraftRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FASE 69.2: local draft persistence engine (`form_drafts` SQLite table via
 * [SulaoneLocalStore]). Complements the SavedStateHandle snapshots added in
 * FASE 69.1 — SavedStateHandle only survives as long as the app's task/Activity
 * record does (an OS process-death-and-restore cycle), while a draft written
 * here survives a full app close, a manual swipe from Recents, or the user
 * returning days later, until the form is submitted or the draft is discarded.
 *
 * [autoSave] debounces at 500ms per [formId] so every keystroke in a
 * `SulaoneTextField` doesn't hit SQLite directly.
 */
@Singleton
class FormDraftStore @Inject constructor(
    private val localStore: SulaoneLocalStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pendingSaves = mutableMapOf<String, Job>()

    /** Schedules [payloadJson] to be written for [formId] after 500ms of inactivity, cancelling any prior pending write for the same form. */
    fun autoSave(formId: String, userId: String?, payloadJson: String) {
        pendingSaves[formId]?.cancel()
        pendingSaves[formId] = scope.launch {
            delay(500)
            localStore.saveDraft(formId, userId, payloadJson)
        }
    }

    /** Reads the persisted draft for [formId], if any. Suspends onto IO. */
    suspend fun getDraft(formId: String): FormDraftRow? = withContext(Dispatchers.IO) {
        localStore.getDraft(formId)
    }

    /** Cancels any pending debounced write and permanently deletes the draft — call after a successful submit or an explicit "Buang" (discard). */
    fun clearDraft(formId: String) {
        pendingSaves[formId]?.cancel()
        pendingSaves.remove(formId)
        scope.launch { localStore.deleteDraft(formId) }
    }
}
