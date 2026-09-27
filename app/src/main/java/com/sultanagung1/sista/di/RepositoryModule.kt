package com.sultanagung1.sista.di

import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.widget.WidgetSnapshotStore
import com.sultanagung1.sista.data.api.*
import com.sultanagung1.sista.data.local.SistaDatabase
import com.sultanagung1.sista.data.local.SulaoneLocalStore
import com.sultanagung1.sista.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiClient: ApiClient,
        sessionManager: SessionManager,
        widgetSnapshots: WidgetSnapshotStore
    ): AuthRepository = AuthRepository(apiClient, sessionManager, widgetSnapshots = widgetSnapshots)

    @Provides
    @Singleton
    fun provideFeatureUsageRepository(
        database: SistaDatabase,
        sessionManager: SessionManager
    ): FeatureUsageRepository = FeatureUsageRepository(database.featureUsageDao(), sessionManager)

    @Provides
    @Singleton
    fun provideStudentRepository(
        apiClient: ApiClient,
        localStore: SulaoneLocalStore,
        widgetSnapshots: WidgetSnapshotStore
    ): StudentRepository = StudentRepository(apiClient, localStore, widgetSnapshots)

    @Provides
    @Singleton
    fun provideAttendanceRepository(
        apiClient: ApiClient,
        widgetSnapshots: WidgetSnapshotStore
    ): AttendanceRepository = AttendanceRepository(apiClient, widgetSnapshots)

    @Provides
    @Singleton
    fun provideCbtRepository(apiClient: ApiClient): CbtRepository =
        CbtRepository(apiClient)

    @Provides
    @Singleton
    fun provideAiRepository(apiClient: ApiClient): AiRepository =
        AiRepository(apiClient)

    @Provides
    @Singleton
    fun provideTeacherRepository(apiClient: ApiClient): TeacherRepository =
        TeacherRepository(apiClient)

    @Provides
    @Singleton
    fun provideParentRepository(
        apiClient: ApiClient,
        widgetSnapshots: WidgetSnapshotStore
    ): ParentRepository = ParentRepository(apiClient, widgetSnapshots)

    @Provides
    @Singleton
    fun provideAdminRepository(apiClient: ApiClient): AdminRepository =
        AdminRepository(apiClient)

    @Provides
    @Singleton
    fun provideChatRepository(apiClient: ApiClient, sessionManager: SessionManager): ChatRepository =
        ChatRepository(apiClient, sessionManager)

    @Provides
    @Singleton
    fun provideNotificationRepository(apiClient: ApiClient): NotificationRepository =
        NotificationRepository(apiClient)

    @Provides
    @Singleton
    fun provideAnalyticsRepository(apiClient: ApiClient): AnalyticsRepository =
        AnalyticsRepository(apiClient)

    @Provides
    @Singleton
    fun provideDocumentRepository(apiClient: ApiClient): DocumentRepository =
        DocumentRepository(apiClient)

    @Provides
    @Singleton
    fun provideDisciplineRepository(disciplineApi: DisciplineApiService): DisciplineRepository =
        DisciplineRepository(disciplineApi)

    @Provides
    @Singleton
    fun provideUtbkRepository(utbkApi: UtbkApiService): UtbkRepository =
        UtbkRepository(utbkApi)

    @Provides
    @Singleton
    fun provideLibraryRepository(libraryApi: LibraryApiService): LibraryRepository =
        LibraryRepository(libraryApi)

    @Provides
    @Singleton
    fun provideScannerRepository(scannerApi: ScannerMobileApiService): ScannerRepository =
        ScannerRepository(scannerApi)

    @Provides
    @Singleton
    fun provideExtracurricularRepository(extracurricularApi: ExtracurricularApiService): ExtracurricularRepository =
        ExtracurricularRepository(extracurricularApi)

    @Provides
    @Singleton
    fun provideAchievementRepository(achievementApi: AchievementApiService): AchievementRepository =
        AchievementRepository(achievementApi)

    @Provides
    @Singleton
    fun provideEvaluationRepository(evaluationApi: EvaluationApiService): EvaluationRepository =
        EvaluationRepository(evaluationApi)

    @Provides
    @Singleton
    fun provideQuestionBankRepository(questionBankApi: QuestionBankApiService): QuestionBankRepository =
        QuestionBankRepository(questionBankApi)

    @Provides
    @Singleton
    fun provideRaporRepository(raporApi: ERaporApiService): RaporRepository =
        RaporRepository(raporApi)

    @Provides
    @Singleton
    fun provideDailyAssessmentRepository(dailyAssessmentApi: DailyAssessmentMobileApiService): DailyAssessmentRepository =
        DailyAssessmentRepository(dailyAssessmentApi)

    @Provides
    @Singleton
    fun provideElearningMobileRepository(elearningApi: ElearningMobileApiService): ElearningMobileRepository =
        ElearningMobileRepository(elearningApi)

    @Provides
    @Singleton
    fun provideCounselingRepository(counselingApi: CounselingMobileApiService): CounselingRepository =
        CounselingRepository(counselingApi)

    @Provides
    @Singleton
    fun provideStudentProfileRepository(studentProfileApi: StudentProfileApiService): StudentProfileRepository =
        StudentProfileRepository(studentProfileApi)

    @Provides
    @Singleton
    fun provideUksRepository(uksApi: UksMobileApiService): UksRepository =
        UksRepository(uksApi)

    @Provides
    @Singleton
    fun provideSpmbRepository(spmbApi: SpmbMobileApiService): SpmbRepository =
        SpmbRepository(spmbApi)

    @Provides
    @Singleton
    fun provideCalendarRepository(calendarApi: CalendarMobileApiService): CalendarRepository =
        CalendarRepository(calendarApi)

    @Provides
    @Singleton
    fun provideTeachingJournalRepository(journalApi: TeachingJournalMobileApiService): TeachingJournalRepository =
        TeachingJournalRepository(journalApi)

    // FASE 77: Sesi Kelas Hidup — guru, siswa, dan admin/Waka Kurikulum/TU.
    @Provides
    @Singleton
    fun provideClassSessionRepository(classSessionApi: ClassSessionApiService): ClassSessionRepository =
        ClassSessionRepository(classSessionApi)

    @Provides
    @Singleton
    fun provideGamificationRepository(gamificationApi: GamificationApiService): GamificationRepository =
        GamificationRepository(gamificationApi)

    @Provides
    @Singleton
    fun provideTahsinRepository(tahsinApi: TahsinApiService): TahsinRepository =
        TahsinRepository(tahsinApi)
}
