package com.sultanagung1.sista

import com.sultanagung1.sista.di.NetworkModule
import com.sultanagung1.sista.di.RepositoryModule
import com.sultanagung1.sista.di.StorageModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.*
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton

class HiltDependencyInjectionTest {

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
        com.sultanagung1.sista.ui.announcements.AnnouncementFeedViewModel::class.java,
        com.sultanagung1.sista.ui.announcements.AnnouncementDetailViewModel::class.java,
        com.sultanagung1.sista.ui.notifications.NotificationViewModel::class.java,
        com.sultanagung1.sista.ui.gamification.GamificationViewModel::class.java,
        com.sultanagung1.sista.ui.scanner.ScannerViewModel::class.java,
        com.sultanagung1.sista.ui.attendance.FaceEnrollmentViewModel::class.java,
        com.sultanagung1.sista.ui.analytics.StudentAnalyticsViewModel::class.java,
        com.sultanagung1.sista.ui.analytics.ClassAnalyticsViewModel::class.java,
        com.sultanagung1.sista.ui.analytics.ChildProgressViewModel::class.java,
        com.sultanagung1.sista.ui.analytics.ExecutiveAnalyticsViewModel::class.java,
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
        com.sultanagung1.sista.ui.profile.ProfileViewModel::class.java,
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
    fun testAllViewModelsHaveHiltViewModelAnnotation() {

        for (vmClass in allViewModelClasses) {
            // Hilt menghasilkan class <ViewModel>_HiltModules saat @HiltViewModel diproses
            val hiltModuleClassName = "${vmClass.name}_HiltModules"
            val hiltModuleClass = try {
                Class.forName(hiltModuleClassName)
            } catch (e: ClassNotFoundException) {
                null
            }
            assertNotNull(
                "ViewModel ${vmClass.simpleName} harus menghasilkan HiltModule ($hiltModuleClassName)",
                hiltModuleClass
            )
        }
    }

    @Test
    fun testAllViewModelsHaveInjectConstructors() {
        for (vmClass in allViewModelClasses) {
            val constructors = vmClass.constructors
            val hasInjectConstructor = constructors.any { it.isAnnotationPresent(Inject::class.java) }
            assertTrue(
                "ViewModel ${vmClass.simpleName} harus memiliki konstruktor yang dianotasi @Inject",
                hasInjectConstructor
            )
        }
    }

    @Test
    fun testHiltModulesHaveCorrectAnnotations() {
        val modules = listOf(
            NetworkModule::class.java,
            StorageModule::class.java,
            RepositoryModule::class.java
        )

        for (module in modules) {
            assertTrue(
                "Module ${module.simpleName} harus memiliki anotasi @Module",
                module.isAnnotationPresent(Module::class.java)
            )

            // Hilt meng-aggregate module ke hilt_aggregated_deps
            val aggregatedDepClassName = "hilt_aggregated_deps._com_sultanagung1_sista_di_${module.simpleName}"
            val aggregatedDepClass = try {
                Class.forName(aggregatedDepClassName)
            } catch (e: ClassNotFoundException) {
                null
            }
            assertNotNull(
                "Module ${module.simpleName} harus di-aggregate oleh Hilt ($aggregatedDepClassName)",
                aggregatedDepClass
            )
        }
    }

    @Test
    fun testApplicationAndActivityHaveHiltAnnotations() {
        // Hilt menghasilkan Hilt_SulaoneApplication dan Dagger Component untuk @HiltAndroidApp
        val appHiltClass = try {
            Class.forName("com.sultanagung1.sista.Hilt_SulaoneApplication")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("SulaoneApplication harus menghasilkan Hilt_SulaoneApplication", appHiltClass)

        // Hilt menghasilkan Hilt_MainActivity untuk @AndroidEntryPoint
        val activityHiltClass = try {
            Class.forName("com.sultanagung1.sista.Hilt_MainActivity")
        } catch (e: ClassNotFoundException) {
            null
        }
        assertNotNull("MainActivity harus menghasilkan Hilt_MainActivity", activityHiltClass)
    }

    @Test
    fun testNetworkModuleProvidesAllServices() {
        val methods = NetworkModule::class.java.declaredMethods
        val returnTypes = methods.map { it.returnType.simpleName }

        assertTrue("NetworkModule harus menyediakan Retrofit", returnTypes.contains("Retrofit"))
        assertTrue("NetworkModule harus menyediakan OkHttpClient", returnTypes.contains("OkHttpClient"))
        assertTrue("NetworkModule harus menyediakan ApiClient", returnTypes.contains("ApiClient"))
        assertTrue("NetworkModule harus menyediakan AuthApiService", returnTypes.contains("AuthApiService"))
        assertTrue("NetworkModule harus menyediakan StudentApiService", returnTypes.contains("StudentApiService"))
        assertTrue("NetworkModule harus menyediakan CbtApiService", returnTypes.contains("CbtApiService"))
        assertTrue("NetworkModule harus menyediakan ERaporApiService", returnTypes.contains("ERaporApiService"))
        assertTrue("NetworkModule harus menyediakan SpmbMobileApiService", returnTypes.contains("SpmbMobileApiService"))
        assertTrue("NetworkModule harus menyediakan UksMobileApiService", returnTypes.contains("UksMobileApiService"))
    }

    @Test
    fun testStorageModuleProvidesCoreServices() {
        val methods = StorageModule::class.java.declaredMethods
        val returnTypes = methods.map { it.returnType.simpleName }

        assertTrue("StorageModule harus menyediakan SessionManager", returnTypes.contains("SessionManager"))
        assertTrue("StorageModule harus menyediakan SulaoneLocalStore", returnTypes.contains("SulaoneLocalStore"))
        assertTrue("StorageModule harus menyediakan LanguageManager", returnTypes.contains("LanguageManager"))
        assertTrue("StorageModule harus menyediakan FontScaleManager", returnTypes.contains("FontScaleManager"))
        assertTrue("StorageModule harus menyediakan ThemeManager", returnTypes.contains("ThemeManager"))
        assertTrue("StorageModule harus menyediakan SyncManager", returnTypes.contains("SyncManager"))
    }

    @Test
    fun testRepositoryModuleProvidesAllRepositories() {
        val methods = RepositoryModule::class.java.declaredMethods
        val returnTypes = methods.map { it.returnType.simpleName }

        assertTrue("RepositoryModule harus menyediakan AuthRepository", returnTypes.contains("AuthRepository"))
        assertTrue("RepositoryModule harus menyediakan StudentRepository", returnTypes.contains("StudentRepository"))
        assertTrue("RepositoryModule harus menyediakan AttendanceRepository", returnTypes.contains("AttendanceRepository"))
        assertTrue("RepositoryModule harus menyediakan CbtRepository", returnTypes.contains("CbtRepository"))
        assertTrue("RepositoryModule harus menyediakan TeacherRepository", returnTypes.contains("TeacherRepository"))
        assertTrue("RepositoryModule harus menyediakan ParentRepository", returnTypes.contains("ParentRepository"))
        assertTrue("RepositoryModule harus menyediakan AdminRepository", returnTypes.contains("AdminRepository"))
        assertTrue("RepositoryModule harus menyediakan RaporRepository", returnTypes.contains("RaporRepository"))
        assertTrue("RepositoryModule harus menyediakan QuestionBankRepository", returnTypes.contains("QuestionBankRepository"))
        assertTrue("RepositoryModule harus menyediakan DailyAssessmentRepository", returnTypes.contains("DailyAssessmentRepository"))
    }
}
