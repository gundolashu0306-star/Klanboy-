package com.example.data.model

/**
 * Verification state for South African students on Campus Plug SA.
 * Must only be transitioned to VERIFIED by authorised administrators.
 */
enum class VerificationStatus(val label: String) {
    NOT_SUBMITTED("Unverified"),
    PENDING("Verification Pending"),
    VERIFIED("Verified Student"),
    REJECTED("Verification Rejected");

    companion object {
        fun fromString(value: String?): VerificationStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NOT_SUBMITTED
    }
}
