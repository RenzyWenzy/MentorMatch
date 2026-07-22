package edu.cit.estillore.mentormatch

import android.content.Context
import edu.cit.estillore.mentormatch.data.api.NetworkModule
import edu.cit.estillore.mentormatch.data.local.TokenManager
import edu.cit.estillore.mentormatch.data.repository.AuthRepository
import edu.cit.estillore.mentormatch.data.repository.AvailabilityRepository
import edu.cit.estillore.mentormatch.data.repository.BookingRepository
import edu.cit.estillore.mentormatch.data.repository.NotificationRepository
import edu.cit.estillore.mentormatch.data.repository.ReportRepository
import edu.cit.estillore.mentormatch.data.repository.ReviewRepository
import edu.cit.estillore.mentormatch.data.repository.SubjectRepository
import edu.cit.estillore.mentormatch.data.repository.TutorProfileRepository
import edu.cit.estillore.mentormatch.data.repository.UserRepository

/**
 * Lightweight hand-rolled dependency container (no Hilt/Dagger needed for
 * an app this size). Created once in MainActivity and threaded down to
 * the ViewModels via their factories.
 */
class AppContainer(context: Context) {

    val tokenManager = TokenManager(context.applicationContext)

    private val authApi = NetworkModule.provideAuthApi(tokenManager)
    private val userApi = NetworkModule.provideUserApi(tokenManager)
    private val subjectApi = NetworkModule.provideSubjectApi(tokenManager)
    private val tutorProfileApi = NetworkModule.provideTutorProfileApi(tokenManager)
    private val availabilityApi = NetworkModule.provideAvailabilityApi(tokenManager)
    private val bookingApi = NetworkModule.provideBookingApi(tokenManager)
    private val reviewApi = NetworkModule.provideReviewApi(tokenManager)
    private val notificationApi = NetworkModule.provideNotificationApi(tokenManager)
    private val reportApi = NetworkModule.provideReportApi(tokenManager)

    val authRepository = AuthRepository(authApi, tokenManager)
    val userRepository = UserRepository(userApi)
    val subjectRepository = SubjectRepository(subjectApi)
    val tutorProfileRepository = TutorProfileRepository(tutorProfileApi)
    val availabilityRepository = AvailabilityRepository(availabilityApi)
    val bookingRepository = BookingRepository(bookingApi)
    val reviewRepository = ReviewRepository(reviewApi)
    val notificationRepository = NotificationRepository(notificationApi)
    val reportRepository = ReportRepository(reportApi)
}
