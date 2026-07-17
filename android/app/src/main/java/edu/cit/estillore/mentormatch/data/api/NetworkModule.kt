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
 */
object NetworkModule {

    private const val BASE_URL = "http://192.168.1.16:8080"

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

    fun provideRetrofit(tokenManager: TokenManager): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(provideOkHttpClient(tokenManager))
        .addConverterFactory(MoshiConverterFactory.create(provideMoshi()))
        .build()

    fun provideAuthApi(tokenManager: TokenManager): AuthApi =
        provideRetrofit(tokenManager).create(AuthApi::class.java)

    fun provideUserApi(tokenManager: TokenManager): UserApi =
        provideRetrofit(tokenManager).create(UserApi::class.java)
}
