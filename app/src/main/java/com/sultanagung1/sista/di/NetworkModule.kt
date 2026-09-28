package com.sultanagung1.sista.di

import android.content.Context
import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.network.AuthInterceptor
import com.sultanagung1.sista.core.network.IdempotencyInterceptor
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.util.Constants
import com.sultanagung1.sista.data.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
            redactHeader("Authorization")
            redactHeader("Cookie")
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): AuthInterceptor {
        return AuthInterceptor(sessionManager)
    }

    @Provides
    @Singleton
    fun provideNetworkPerformanceInterceptor(): com.sultanagung1.sista.core.telemetry.NetworkPerformanceInterceptor {
        return com.sultanagung1.sista.core.telemetry.NetworkPerformanceInterceptor()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        networkPerformanceInterceptor: com.sultanagung1.sista.core.telemetry.NetworkPerformanceInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(networkPerformanceInterceptor)
            .addInterceptor(IdempotencyInterceptor())
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiClient(@ApplicationContext context: Context): ApiClient {
        return ApiClient(context)
    }

    // 34 Retrofit API Services
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideCapabilitiesApiService(retrofit: Retrofit): CapabilitiesApiService =
        retrofit.create(CapabilitiesApiService::class.java)

    @Provides
    @Singleton
    fun provideStudentApiService(retrofit: Retrofit): StudentApiService =
        retrofit.create(StudentApiService::class.java)

    @Provides
    @Singleton
    fun provideAttendanceApiService(retrofit: Retrofit): AttendanceApiService =
        retrofit.create(AttendanceApiService::class.java)

    @Provides
    @Singleton
    fun provideCbtApiService(retrofit: Retrofit): CbtApiService =
        retrofit.create(CbtApiService::class.java)

    @Provides
    @Singleton
    fun provideAiApiService(retrofit: Retrofit): AiApiService =
        retrofit.create(AiApiService::class.java)

    @Provides
    @Singleton
    fun provideGeneralApiService(retrofit: Retrofit): GeneralApiService =
        retrofit.create(GeneralApiService::class.java)

    @Provides
    @Singleton
    fun provideTeacherApiService(retrofit: Retrofit): TeacherApiService =
        retrofit.create(TeacherApiService::class.java)

    @Provides
    @Singleton
    fun provideParentApiService(retrofit: Retrofit): ParentApiService =
        retrofit.create(ParentApiService::class.java)

    @Provides
    @Singleton
    fun provideAdminApiService(retrofit: Retrofit): AdminApiService =
        retrofit.create(AdminApiService::class.java)

    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService =
        retrofit.create(ChatApiService::class.java)

    @Provides
    @Singleton
    fun provideNotificationApiService(retrofit: Retrofit): NotificationApiService =
        retrofit.create(NotificationApiService::class.java)

    @Provides
    @Singleton
    fun provideDocumentApiService(retrofit: Retrofit): DocumentApiService =
        retrofit.create(DocumentApiService::class.java)

    @Provides
    @Singleton
    fun provideAnalyticsApiService(retrofit: Retrofit): AnalyticsApiService =
        retrofit.create(AnalyticsApiService::class.java)

    @Provides
    @Singleton
    fun provideDisciplineApiService(retrofit: Retrofit): DisciplineApiService =
        retrofit.create(DisciplineApiService::class.java)

    @Provides
    @Singleton
    fun provideUtbkApiService(retrofit: Retrofit): UtbkApiService =
        retrofit.create(UtbkApiService::class.java)

    @Provides
    @Singleton
    fun provideLibraryApiService(retrofit: Retrofit): LibraryApiService =
        retrofit.create(LibraryApiService::class.java)

    @Provides
    @Singleton
    fun provideExtracurricularApiService(retrofit: Retrofit): ExtracurricularApiService =
        retrofit.create(ExtracurricularApiService::class.java)

    @Provides
    @Singleton
    fun provideAchievementApiService(retrofit: Retrofit): AchievementApiService =
        retrofit.create(AchievementApiService::class.java)

    @Provides
    @Singleton
    fun provideEvaluationApiService(retrofit: Retrofit): EvaluationApiService =
        retrofit.create(EvaluationApiService::class.java)

    @Provides
    @Singleton
    fun provideQuestionBankApiService(retrofit: Retrofit): QuestionBankApiService =
        retrofit.create(QuestionBankApiService::class.java)

    @Provides
    @Singleton
    fun provideRaporApiService(retrofit: Retrofit): ERaporApiService =
        retrofit.create(ERaporApiService::class.java)

    @Provides
    @Singleton
    fun provideDailyAssessmentApiService(retrofit: Retrofit): DailyAssessmentMobileApiService =
        retrofit.create(DailyAssessmentMobileApiService::class.java)

    @Provides
    @Singleton
    fun provideElearningApiService(retrofit: Retrofit): ElearningMobileApiService =
        retrofit.create(ElearningMobileApiService::class.java)

    @Provides
    @Singleton
    fun provideCounselingApiService(retrofit: Retrofit): CounselingMobileApiService =
        retrofit.create(CounselingMobileApiService::class.java)

    @Provides
    @Singleton
    fun provideStudentProfileApiService(retrofit: Retrofit): StudentProfileApiService =
        retrofit.create(StudentProfileApiService::class.java)

    @Provides
    @Singleton
    fun provideCalendarApiService(retrofit: Retrofit): CalendarMobileApiService =
        retrofit.create(CalendarMobileApiService::class.java)

    @Provides
    @Singleton
    fun provideSpmbApiService(retrofit: Retrofit): SpmbMobileApiService =
        retrofit.create(SpmbMobileApiService::class.java)

    @Provides
    @Singleton
    fun provideUksApiService(retrofit: Retrofit): UksMobileApiService =
        retrofit.create(UksMobileApiService::class.java)

    @Provides
    @Singleton
    fun provideTeachingJournalApiService(retrofit: Retrofit): TeachingJournalMobileApiService =
        retrofit.create(TeachingJournalMobileApiService::class.java)

    // FASE 77: Sesi Kelas Hidup (backend FASE 117)
    @Provides
    @Singleton
    fun provideClassSessionApiService(retrofit: Retrofit): ClassSessionApiService =
        retrofit.create(ClassSessionApiService::class.java)

    @Provides
    @Singleton
    fun provideContextualHomeApiService(retrofit: Retrofit): ContextualHomeApiService =
        retrofit.create(ContextualHomeApiService::class.java)

    @Provides
    @Singleton
    fun provideGamificationApiService(retrofit: Retrofit): GamificationApiService =
        retrofit.create(GamificationApiService::class.java)

    @Provides
    @Singleton
    fun provideNotificationPreferencesApiService(retrofit: Retrofit): NotificationPreferencesApiService =
        retrofit.create(NotificationPreferencesApiService::class.java)

    @Provides
    @Singleton
    fun provideParentExperienceApiService(retrofit: Retrofit): ParentExperienceApiService =
        retrofit.create(ParentExperienceApiService::class.java)

    @Provides
    @Singleton
    fun provideSyncApiService(retrofit: Retrofit): SyncApiService =
        retrofit.create(SyncApiService::class.java)

    @Provides
    @Singleton
    fun provideTahsinApiService(retrofit: Retrofit): TahsinApiService =
        retrofit.create(TahsinApiService::class.java)

    @Provides
    @Singleton
    fun provideScannerApiService(retrofit: Retrofit): ScannerMobileApiService =
        retrofit.create(ScannerMobileApiService::class.java)
}
