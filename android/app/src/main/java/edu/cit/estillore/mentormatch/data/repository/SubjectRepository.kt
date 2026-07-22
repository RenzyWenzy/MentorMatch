package edu.cit.estillore.mentormatch.data.repository

import edu.cit.estillore.mentormatch.data.api.ErrorParser
import edu.cit.estillore.mentormatch.data.api.SubjectApi
import edu.cit.estillore.mentormatch.data.model.Subject
import edu.cit.estillore.mentormatch.data.model.SubjectRequest

class SubjectRepository(private val subjectApi: SubjectApi) {

    suspend fun list(): Result<List<Subject>> = runCatching {
        val response = subjectApi.listSubjects()
        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw IllegalStateException(ErrorParser.parse(response))
        }
    }

    suspend fun create(name: String, description: String?): Result<Subject> = runCatching {
        val response = subjectApi.createSubject(SubjectRequest(name, description))
        if (response.isSuccessful) {
            response.body() ?: error("Empty response from server.")
        } else {
            throw IllegalStateException(ErrorParser.parse(response))
        }
    }

    suspend fun update(id: Long, name: String, description: String?): Result<Subject> = runCatching {
        val response = subjectApi.updateSubject(id, SubjectRequest(name, description))
        if (response.isSuccessful) {
            response.body() ?: error("Empty response from server.")
        } else {
            throw IllegalStateException(ErrorParser.parse(response))
        }
    }

    suspend fun delete(id: Long): Result<Unit> = runCatching {
        val response = subjectApi.deleteSubject(id)
        if (!response.isSuccessful) {
            throw IllegalStateException(ErrorParser.parse(response))
        }
    }
}