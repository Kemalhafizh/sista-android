package com.sultanagung1.sista.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.data.model.AttendanceStudentStatus
import com.sultanagung1.sista.data.model.StudentAttendanceInputItem
import com.sultanagung1.sista.data.model.SubmitClassAttendanceRequest
import com.sultanagung1.sista.data.repository.TeacherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherAttendanceUiState(
    val loading: Boolean = true,
    val students: List<StudentAttendanceInputItem> = emptyList(),
    /** Why the roster could not be loaded: the server's message (a 403 for another teacher's class). */
    val loadError: String? = null,
    val submitting: Boolean = false,
    val submitError: String? = null,
    /** How many students the server recorded; set once the attendance is saved. */
    val recordedCount: Int? = null,
)

/**
 * Class attendance for one lesson: the roster from `teacher/classes/{id}/students`,
 * sent to `POST teacher/attendance`. Separate from [TeacherViewModel], which loads
 * the whole home (classes, schedule, journals, sessions) this screen does not need.
 */
@HiltViewModel
class TeacherAttendanceViewModel @Inject constructor(
    private val repository: TeacherRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherAttendanceUiState())
    val uiState: StateFlow<TeacherAttendanceUiState> = _uiState.asStateFlow()

    private var classroomId: Long? = null

    fun load(classroomId: Long) {
        this.classroomId = classroomId
        _uiState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            repository.getClassStudents(classroomId).collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.update { state ->
                        // Keep what the teacher already marked when the roster is reloaded.
                        val marked = state.students.associate { it.studentId to it.status }
                        state.copy(
                            loading = false,
                            students = result.data.map { s ->
                                StudentAttendanceInputItem(
                                    studentId = s.id,
                                    name = s.name,
                                    nis = s.nis,
                                    nisn = s.nisn,
                                    status = marked[s.id] ?: AttendanceMarks.PRESENT,
                                )
                            },
                        )
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(loading = false, loadError = result.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun setStatus(studentId: Long, code: String) {
        _uiState.update { state ->
            state.copy(students = state.students.map { if (it.studentId == studentId) it.copy(status = code) else it })
        }
    }

    fun markAllPresent() {
        _uiState.update { state -> state.copy(students = state.students.map { it.copy(status = AttendanceMarks.PRESENT) }) }
    }

    /** [scheduleId] is the lesson the attendance belongs to, or null when opened without one. */
    fun submit(scheduleId: Long?) {
        val classroom = classroomId ?: return
        val state = _uiState.value
        if (state.students.isEmpty() || state.submitting) return
        _uiState.update { it.copy(submitting = true, submitError = null) }
        viewModelScope.launch {
            val request = SubmitClassAttendanceRequest(
                classroomId = classroom,
                date = DateUtils.todayIso(),
                scheduleId = scheduleId,
                students = state.students.map { AttendanceStudentStatus(studentId = it.studentId, status = it.status) },
            )
            repository.submitClassAttendance(request).collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.update { it.copy(submitting = false, recordedCount = result.data.recordedCount) }
                    is NetworkResult.Error -> _uiState.update { it.copy(submitting = false, submitError = result.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }
}

/** The attendance codes `teacher/attendance` stores, and how many students have each. */
object AttendanceMarks {
    const val PRESENT = "H"
    const val PERMIT = "I"
    const val SICK = "S"
    const val ABSENT = "A"

    /** In the order the screen offers them. */
    val ALL = listOf(PRESENT, PERMIT, SICK, ABSENT)

    fun tally(students: List<StudentAttendanceInputItem>): Map<String, Int> =
        ALL.associateWith { code -> students.count { it.status == code } }
}
