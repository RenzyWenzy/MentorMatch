package edu.cit.estillore.mentormatch.data.model

/** Mirrors BookingController.java's confirm/decline/cancel/complete actions. */
enum class BookingStatus {
    PENDING,
    CONFIRMED,
    DECLINED,
    COMPLETED,
    CANCELLED
}
