package edu.cit.estillore.mentormatch.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import edu.cit.estillore.mentormatch.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Base URL notes:
 * - Android emulator reaches the host machine's localhost via 10.0.2.2,
 *   so if Spring Boot runs on your dev machine at http://localhost:8080,
 *   use http://10.0.2.2:8080/ here.
 * - A physical device needs your machine's LAN IP instead, e.g.
 *   http://192.168.1.23:8080/.
 * - cleartext (http) traffic is enabled in AndroidManifest for local dev;
 *   switch to https and remove usesCleartextTraffic for production.
 *
 * provideRetrofit() now caches the Retrofit instance per TokenManager so the
 * growing number of *Api interfaces (Subject, TutorProfile, Availability,
 * Booking, Review, Notification, Report, on top of Auth/User) all share one
 * OkHttpClient instead of each spinning up its own.
 */
object NetworkModule {

    private const val BASE_URL = "http://192.168.1.13:8080"

    private var cachedRetrofit: Retrofit? = null

    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(logging)
            .build()
    }

    fun provideRetrofit(tokenManager: TokenManager): Retrofit {
        cachedRetrofit?.let { return it }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient(tokenManager))
            .addConverterFactory(MoshiConverterFactory.create(provideMoshi()))
            .build()
        cachedRetrofit = retrofit
        return retrofit
    }

    fun provideAuthApi(tokenManager: TokenManager): AuthApi =
        provideRetrofit(tokenManager).create(AuthApi::class.java)

    fun provideUserApi(tokenManager: TokenManager): UserApi =
        provideRetrofit(tokenManager).create(UserApi::class.java)

    fun provideSubjectApi(tokenManager: TokenManager): SubjectApi =
        provideRetrofit(tokenManager).create(SubjectApi::class.java)

    fun provideTutorProfileApi(tokenManager: TokenManager): TutorProfileApi =
        provideRetrofit(tokenManager).create(TutorProfileApi::class.java)

    fun provideAvailabilityApi(tokenManager: TokenManager): AvailabilityApi =
        provideRetrofit(tokenManager).create(AvailabilityApi::class.java)

    fun provideBookingApi(tokenManager: TokenManager): BookingApi =
        provideRetrofit(tokenManager).create(BookingApi::class.java)

    fun provideReviewApi(tokenManager: TokenManager): ReviewApi =
        provideRetrofit(tokenManager).create(ReviewApi::class.java)

    fun provideNotificationApi(tokenManager: TokenManager): NotificationApi =
        provideRetrofit(tokenManager).create(NotificationApi::class.java)

    fun provideReportApi(tokenManager: TokenManager): ReportApi =
        provideRetrofit(tokenManager).create(ReportApi::class.java)
}