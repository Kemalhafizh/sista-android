package com.sultanagung1.sista

import com.sultanagung1.sista.core.util.Constants
import com.sultanagung1.sista.data.api.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.*
import org.junit.Test
import retrofit2.http.*
import java.lang.reflect.Method

class RetrofitUrlNormalizationTest {

    private val allServiceInterfaces: List<Class<*>> = listOf(
        // Services audited & fixed in Fase 49. SchoolOperationsApiService was
        // listed here but no longer exists — the reference stopped this whole
        // test source set from compiling. listMatchesTheServicesInSource() now
        // keeps this list honest.
        AchievementApiService::class.java,
        CapabilitiesApiService::class.java,
        CalendarMobileApiService::class.java,
        CounselingMobileApiService::class.java,
        DailyAssessmentMobileApiService::class.java,
        DisciplineApiService::class.java,
        ERaporApiService::class.java,
        ElearningMobileApiService::class.java,
        EvaluationApiService::class.java,
        ExtracurricularApiService::class.java,
        LibraryApiService::class.java,
        QuestionBankApiService::class.java,
        SpmbMobileApiService::class.java,
        StudentProfileApiService::class.java,
        TeachingJournalMobileApiService::class.java,
        UksMobileApiService::class.java,
        UtbkApiService::class.java,

        // Other API Services in data.api
        AnalyticsApiService::class.java,
        AuthApiService::class.java,
        ChatApiService::class.java,
        NotificationApiService::class.java,
        ContextualHomeApiService::class.java,
        DocumentApiService::class.java,
        GamificationApiService::class.java,
        NotificationPreferencesApiService::class.java,
        ParentExperienceApiService::class.java,
        StudentApiService::class.java,
        AttendanceApiService::class.java,
        CbtApiService::class.java,
        AiApiService::class.java,
        GeneralApiService::class.java,
        TeacherApiService::class.java,
        ParentApiService::class.java,
        AdminApiService::class.java,
        SyncApiService::class.java,
        TahsinApiService::class.java,
        ScannerMobileApiService::class.java,
        MobileConfigApiService::class.java,
        // FASE 77: Sesi Kelas Hidup (backend FASE 117)
        ClassSessionApiService::class.java
    )

    /** Every `interface …ApiService` declared under data/api, from the source files. */
    private fun apiServicesInSource(): Set<String> {
        val candidates = listOf(
            java.io.File("../core/network/src/main/java/com/sultanagung1/sista/data/api"),
            java.io.File("core/network/src/main/java/com/sultanagung1/sista/data/api")
        )
        val dir = candidates.firstOrNull { it.isDirectory } ?: error("data/api source directory not found")
        val declaration = Regex("""^interface\s+(\w+ApiService)\b""", RegexOption.MULTILINE)
        return dir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file -> declaration.findAll(file.readText()).map { it.groupValues[1] } }
            .toSet()
    }

    @Test
    fun listMatchesTheServicesInSource() {
        assertEquals(
            "Every API service in data/api must be audited here, and nothing that no longer exists",
            apiServicesInSource(),
            allServiceInterfaces.map { it.simpleName }.toSet()
        )
    }

    private fun extractPathFromMethod(method: Method): String? {
        method.getAnnotation(GET::class.java)?.let { return it.value }
        method.getAnnotation(POST::class.java)?.let { return it.value }
        method.getAnnotation(PUT::class.java)?.let { return it.value }
        method.getAnnotation(DELETE::class.java)?.let { return it.value }
        method.getAnnotation(PATCH::class.java)?.let { return it.value }
        method.getAnnotation(HEAD::class.java)?.let { return it.value }
        method.getAnnotation(OPTIONS::class.java)?.let { return it.value }
        method.getAnnotation(HTTP::class.java)?.let { return it.path }
        return null
    }

    @Test
    fun verifyNoDuplicateApiV1PrefixInAnyService() {
        var endpointCount = 0

        for (serviceClass in allServiceInterfaces) {
            for (method in serviceClass.methods) {
                val path = extractPathFromMethod(method) ?: continue
                endpointCount++

                assertFalse(
                    "Service [${serviceClass.simpleName}.${method.name}] should not contain 'api/v1/' in path '$path'",
                    path.contains("api/v1")
                )
            }
        }

        assertTrue("Expected to validate multiple endpoints, found: $endpointCount", endpointCount >= 80)
        println("Validated $endpointCount endpoints across ${allServiceInterfaces.size} API service interfaces: 0 contain 'api/v1'.")
    }

    @Test
    fun verifyNoLeadingSlashInAnyServicePath() {
        var endpointCount = 0

        for (serviceClass in allServiceInterfaces) {
            for (method in serviceClass.methods) {
                val path = extractPathFromMethod(method) ?: continue
                endpointCount++

                assertFalse(
                    "Service [${serviceClass.simpleName}.${method.name}] path '$path' must not start with '/' because Retrofit would drop the base URL path prefix!",
                    path.startsWith("/")
                )
            }
        }

        println("Validated $endpointCount endpoints: 0 have leading slash.")
    }

    @Test
    fun verifyAllResolvedUrlsAreValidAndDoNotContainDoublePrefix() {
        val baseUrl = Constants.DEFAULT_BASE_URL.toHttpUrl()
        var endpointCount = 0

        for (serviceClass in allServiceInterfaces) {
            for (method in serviceClass.methods) {
                val path = extractPathFromMethod(method) ?: continue
                endpointCount++

                // Replace path params {param} with dummy value for URL resolution test
                val sanitizedPath = path.replace(Regex("\\{[^}]+\\}"), "test123")
                val resolvedUrl = baseUrl.resolve(sanitizedPath)

                assertNotNull(
                    "Failed to resolve URL for [${serviceClass.simpleName}.${method.name}] with path '$sanitizedPath'",
                    resolvedUrl
                )

                val fullUrl = resolvedUrl.toString()

                assertFalse(
                    "Resolved URL '$fullUrl' contains duplicate '/api/v1/api/v1/'",
                    fullUrl.contains("/api/v1/api/v1/")
                )

                assertTrue(
                    "Resolved URL '$fullUrl' must start with base URL '$baseUrl'",
                    fullUrl.startsWith(baseUrl.toString())
                )
            }
        }

        println("Verified resolution of $endpointCount endpoints against Base URL: All resolved correctly without duplicates.")
    }
}
