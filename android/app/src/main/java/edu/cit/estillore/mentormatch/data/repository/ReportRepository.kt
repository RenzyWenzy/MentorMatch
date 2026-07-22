package edu.cit.estillore.mentormatch.data.repository

import edu.cit.estillore.mentormatch.data.api.ErrorParser
import edu.cit.estillore.mentormatch.data.api.ReportApi
import edu.cit.estillore.mentormatch.data.model.AdminReport

class ReportRepository(private val reportApi: ReportApi) {

    suspend fun generate(startDate: String, endDate: String): Result<AdminReport> = runCatching {
        val response = reportApi.generate(startDate, endDate)
        if (response.isSuccessful) response.body() ?: error("Empty response from server.")
        else throw IllegalStateException(ErrorParser.parse(response))
    }
}
