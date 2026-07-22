package edu.cit.estillore.mentormatch.data.api

import edu.cit.estillore.mentormatch.data.model.AdminReport
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/** Matches AdminReportController.java. ADMIN-only server-side. */
interface ReportApi {

    @GET("api/admin/reports")
    suspend fun generate(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<AdminReport>
}