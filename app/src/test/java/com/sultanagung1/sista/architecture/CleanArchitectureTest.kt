package com.sultanagung1.sista.architecture

import com.sultanagung1.sista.core.mvi.MviViewModel
import com.sultanagung1.sista.core.mvi.UiEffect
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.di.RepositoryModule
import dagger.Module
import dagger.Provides
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class CleanArchitectureTest {

    private fun resolveDir(relativePath: String): File {
        val f1 = File(relativePath)
        if (f1.exists()) return f1
        val f2 = File("app", relativePath)
        if (f2.exists()) return f2
        return f1
    }

    private val allViewModelClasses: List<Class<*>> = listOf(
        com.sultanagung1.sista.ui.auth.LoginViewModel::class.java,
        com.sultanagung1.sista.ui.navigation.CapabilitiesViewModel::class.java,
        com.sultanagung1.sista.ui.home.HomeViewModel::class.java,
        com.sultanagung1.sista.ui.attendance.AttendanceViewModel::class.java,
        com.sultanagung1.sista.ui.academic.AcademicViewModel::class.java,
        com.sultanagung1.sista.ui.cbt.CbtViewModel::class.java,
        com.sultanagung1.sista.ui.ibadah.IbadahViewModel::class.java,
        com.sultanagung1.sista.ui.ai.AiViewModel::class.java,
        com.sultanagung1.sista.ui.teacher.TeacherViewModel::class.java,
        com.sultanagung1.sista.ui.parent.ParentViewModel::class.java,
        com.sultanagung1.sista.ui.admin.AdminViewModel::class.java,
        com.sultanagung1.sista.ui.chat.ChatViewModel::class.java,
        com.sultanagung1.sista.ui.announcements.AnnouncementViewModel::class.java,
        com.sultanagung1.sista.ui.notifications.NotificationViewModel::class.java,
        com.sultanagung1.sista.ui.gamification.GamificationViewModel::class.java,
        com.sultanagung1.sista.ui.scanner.ScannerViewModel::class.java,
        com.sultanagung1.sista.ui.attendance.FaceEnrollmentViewModel::class.java,
        com.sultanagung1.sista.ui.analytics.AnalyticsViewModel::class.java,
        com.sultanagung1.sista.ui.settings.SettingsViewModel::class.java,
        com.sultanagung1.sista.ui.discipline.DisciplineViewModel::class.java,
        com.sultanagung1.sista.ui.utbk.UtbkViewModel::class.java,
        com.sultanagung1.sista.ui.library.LibraryViewModel::class.java,
        com.sultanagung1.sista.ui.extracurricular.ExtracurricularViewModel::class.java,
        com.sultanagung1.sista.ui.achievement.AchievementViewModel::class.java,
        com.sultanagung1.sista.ui.evaluation.EvaluationViewModel::class.java,
        com.sultanagung1.sista.ui.teacher.QuestionBankViewModel::class.java,
        com.sultanagung1.sista.ui.academic.RaporViewModel::class.java,
        com.sultanagung1.sista.ui.teacher.DailyAssessmentViewModel::class.java,
        com.sultanagung1.sista.ui.elearning.ElearningViewModel::class.java,
        com.sultanagung1.sista.ui.counseling.CounselingViewModel::class.java,
        com.sultanagung1.sista.ui.profile.StudentProfileViewModel::class.java,
        com.sultanagung1.sista.ui.calendar.CalendarViewModel::class.java,
        com.sultanagung1.sista.ui.spmb.SpmbViewModel::class.java,
        com.sultanagung1.sista.ui.uks.UksViewModel::class.java,
        com.sultanagung1.sista.ui.teacher.JournalMobileViewModel::class.java,
        com.sultanagung1.sista.core.websocket.WebSocketSessionViewModel::class.java,
        com.sultanagung1.sista.ui.document.DocumentScannerViewModel::class.java,
        com.sultanagung1.sista.ui.document.SignatureViewModel::class.java,
        com.sultanagung1.sista.ui.finance.BillingViewModel::class.java,
        com.sultanagung1.sista.ui.ibadah.TahsinRecorderViewModel::class.java,
        com.sultanagung1.sista.ui.ibadah.TahsinViewModel::class.java,
        com.sultanagung1.sista.ui.portal.ModuleCatalogViewModel::class.java,
        com.sultanagung1.sista.ui.teacher.CbtProctorViewModel::class.java,
        com.sultanagung1.sista.ui.teacher.TeacherCreateExamViewModel::class.java,
        com.sultanagung1.sista.ui.teacher.TeacherProctorExamsViewModel::class.java,
        // FASE 77: Sesi Kelas Hidup
        com.sultanagung1.sista.ui.teacher.sessions.TeacherTodaySessionsViewModel::class.java,
        com.sultanagung1.sista.ui.teacher.sessions.TeacherActiveSessionViewModel::class.java,
        com.sultanagung1.sista.ui.teacher.sessions.TeacherAttendanceListViewModel::class.java,
        com.sultanagung1.sista.ui.attendance.StudentSessionQrScanViewModel::class.java,
        com.sultanagung1.sista.ui.admin.sessions.AdminSessionManagementViewModel::class.java,
        com.sultanagung1.sista.ui.admin.sessions.AdminAttendanceOverrideViewModel::class.java
    )
    /**
     * Every `@HiltViewModel class` in the main source sets of all modules. The
     * list above used to be kept by hand: it named SchoolOperationsViewModel,
     * which no longer exists (so this source set didn't compile), and missed
     * ten real ViewModels.
     */
    private fun hiltViewModelsInSource(): Set<String> {
        val roots = listOf("app", "core", "feature").map { dir ->
            listOf(java.io.File("../$dir"), java.io.File(dir)).firstOrNull { it.isDirectory } ?: error("$dir not found")
        }
        val pkg = Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE)
        val vm = Regex("""@HiltViewModel\s+class\s+(\w+)""")
        return roots.flatMap { root ->
            root.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .filter { f -> f.path.replace('\\', '/').let { "/src/main/" in it && "/build/" !in it } }
                .flatMap { f ->
                    val text = f.readText()
                    val p = pkg.find(text)?.groupValues?.get(1) ?: ""
                    vm.findAll(text).map { "$p.${it.groupValues[1]}" }
                }
                .toList()
        }.toSet()
    }

    @Test
    fun testViewModelListMatchesSource() {
        assertEquals(
            "Every @HiltViewModel must be listed here, and nothing that no longer exists",
            hiltViewModelsInSource(),
            allViewModelClasses.map { it.name }.toSet()
        )
    }


    @Test
    fun testNoRetrofitOrOkHttpImportsInUiLayer() {
        val uiDir = resolveDir("src/main/java/com/sultanagung1/sista/ui")
        assertTrue("UI source directory must exist", uiDir.exists() && uiDir.isDirectory)

        val forbiddenImports = listOf("import retrofit2.", "import okhttp3.")
        val violatingFiles = mutableListOf<String>()

        uiDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val lines = file.readLines()
                for (line in lines) {
                    val trimmed = line.trim()
                    for (forbidden in forbiddenImports) {
                        if (trimmed.startsWith(forbidden)) {
                            violatingFiles.add("${file.name}: $trimmed")
                        }
                    }
                }
            }

        assertTrue(
            "Clean Architecture Violation: UI layer must NEVER import Retrofit or OkHttp directly!\nViolations:\n${violatingFiles.joinToString("\n")}",
            violatingFiles.isEmpty()
        )
    }

    @Test
    fun testNoApiServiceInViewModelConstructors() {
        for (vmClass in allViewModelClasses) {
            for (constructor in vmClass.constructors) {
                for (paramType in constructor.parameterTypes) {
                    assertFalse(
                        "Clean Architecture Violation: ViewModel ${vmClass.simpleName} cannot directly inject ApiService ${paramType.simpleName}. Use Repository pattern instead!",
                        paramType.simpleName.endsWith("ApiService")
                    )
                }
            }
        }
    }

    @Test
    fun testNoApiClientInViewModelConstructors() {
        for (vmClass in allViewModelClasses) {
            for (constructor in vmClass.constructors) {
                for (paramType in constructor.parameterTypes) {
                    assertFalse(
                        "Clean Architecture Violation: ViewModel ${vmClass.simpleName} cannot directly inject ApiClient. Use Repository pattern instead!",
                        paramType.simpleName == "ApiClient"
                    )
                }
            }
        }
    }

    @Test
    fun testMviCoreContractsPresent() {
        assertNotNull("UiState interface must be defined", UiState::class.java)
        assertNotNull("UiEvent interface must be defined", UiEvent::class.java)
        assertNotNull("UiEffect interface must be defined", UiEffect::class.java)
        assertNotNull("MviViewModel abstract class must be defined", MviViewModel::class.java)
    }

    @Test
    fun testNewRepositoriesRegisteredInRepositoryModule() {
        val repositoryModuleClass = RepositoryModule::class.java
        assertTrue("RepositoryModule must be annotated with @Module", repositoryModuleClass.isAnnotationPresent(Module::class.java))

        val providerMethodReturnTypes = repositoryModuleClass.declaredMethods
            .filter { it.isAnnotationPresent(Provides::class.java) }
            .map { it.returnType.simpleName }

        val expectedRepositories = listOf(
            "UksRepository",
            "SpmbRepository",
            "CalendarRepository",
            "TeachingJournalRepository",
            "GamificationRepository",
            "ClassSessionRepository"
        )

        for (expected in expectedRepositories) {
            assertTrue(
                "RepositoryModule must provide $expected",
                providerMethodReturnTypes.contains(expected)
            )
        }
    }
}
