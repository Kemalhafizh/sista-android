package com.sultanagung1.sista.core.network

import android.content.Context
import com.sultanagung1.sista.core.network.BuildConfig
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.util.Constants
import com.sultanagung1.sista.data.api.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(private val context: Context) {

    private val sessionManager = SessionManager(context)

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
        redactHeader("Authorization")
        redactHeader("Cookie")
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(IdempotencyInterceptor())
        .addInterceptor(AuthInterceptor(sessionManager))
        .addInterceptor(LanguageInterceptor { com.sultanagung1.sista.core.accessibility.AppLocale.headerValue(context) })
        .addInterceptor(loggingInterceptor)
        .apply {
            if (BuildConfig.DEBUG) {
                try {
                    val debugInterceptor = Class.forName(
                        "com.sultanagung1.sista.core.debug.SistaNetworkInterceptor"
                    ).getDeclaredConstructor().newInstance() as okhttp3.Interceptor
                    addInterceptor(debugInterceptor)
                } catch (_: Exception) { /* debug class not available in release */ }
            }
        }
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val studentApi: StudentApiService by lazy { retrofit.create(StudentApiService::class.java) }
    val attendanceApi: AttendanceApiService by lazy { retrofit.create(AttendanceApiService::class.java) }
    val cbtApi: CbtApiService by lazy { retrofit.create(CbtApiService::class.java) }
    val aiApi: AiApiService by lazy { retrofit.create(AiApiService::class.java) }
    val generalApi: GeneralApiService by lazy { retrofit.create(GeneralApiService::class.java) }
    val teacherApi: TeacherApiService by lazy { retrofit.create(TeacherApiService::class.java) }
    val parentApi: ParentApiService by lazy { retrofit.create(ParentApiService::class.java) }
    val adminApi: AdminApiService by lazy { retrofit.create(AdminApiService::class.java) }
    val chatApi: ChatApiService by lazy { retrofit.create(ChatApiService::class.java) }
    val notificationApi: NotificationApiService by lazy { retrofit.create(NotificationApiService::class.java) }
    val documentApi: DocumentApiService by lazy { retrofit.create(DocumentApiService::class.java) }
    val analyticsApi: AnalyticsApiService by lazy { retrofit.create(AnalyticsApiService::class.java) }
    val disciplineApi: DisciplineApiService by lazy { retrofit.create(DisciplineApiService::class.java) }
    val utbkApi: UtbkApiService by lazy { retrofit.create(UtbkApiService::class.java) }
    val libraryApi: LibraryApiService by lazy { retrofit.create(LibraryApiService::class.java) }
    val extracurricularApi: ExtracurricularApiService by lazy { retrofit.create(ExtracurricularApiService::class.java) }
    val achievementApi: AchievementApiService by lazy { retrofit.create(AchievementApiService::class.java) }
    val evaluationApi: EvaluationApiService by lazy { retrofit.create(EvaluationApiService::class.java) }
    val questionBankApi: QuestionBankApiService by lazy { retrofit.create(QuestionBankApiService::class.java) }
    val raporApi: ERaporApiService by lazy { retrofit.create(ERaporApiService::class.java) }
    val dailyAssessmentApi: DailyAssessmentMobileApiService by lazy { retrofit.create(DailyAssessmentMobileApiService::class.java) }
    val elearningApi: ElearningMobileApiService by lazy { retrofit.create(ElearningMobileApiService::class.java) }
    val counselingApi: CounselingMobileApiService by lazy { retrofit.create(CounselingMobileApiService::class.java) }
    val studentProfileApi: StudentProfileApiService by lazy { retrofit.create(StudentProfileApiService::class.java) }
    val calendarApi: CalendarMobileApiService by lazy { retrofit.create(CalendarMobileApiService::class.java) }
    val spmbApi: SpmbMobileApiService by lazy { retrofit.create(SpmbMobileApiService::class.java) }
    val uksApi: UksMobileApiService by lazy { retrofit.create(UksMobileApiService::class.java) }
    val teachingJournalApi: TeachingJournalMobileApiService by lazy { retrofit.create(TeachingJournalMobileApiService::class.java) }
    val contextualHomeApi: ContextualHomeApiService by lazy { retrofit.create(ContextualHomeApiService::class.java) }
    val gamificationApi: GamificationApiService by lazy { retrofit.create(GamificationApiService::class.java) }
    val parentExperienceApi: ParentExperienceApiService by lazy { retrofit.create(ParentExperienceApiService::class.java) }
    val syncApi: SyncApiService by lazy { retrofit.create(SyncApiService::class.java) }
    val mobileConfigApi: MobileConfigApiService by lazy { retrofit.create(MobileConfigApiService::class.java) }
    val tahsinApi: TahsinApiService by lazy { retrofit.create(TahsinApiService::class.java) }
    val scannerApi: ScannerMobileApiService by lazy { retrofit.create(ScannerMobileApiService::class.java) }
}

