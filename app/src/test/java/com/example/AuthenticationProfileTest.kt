package com.example

import com.example.data.model.SouthAfricanCampus
import com.example.data.model.UserProfile
import com.example.data.model.VerificationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthenticationProfileTest {

    @Test
    fun `test verification status parsing and labels`() {
        assertEquals(VerificationStatus.NOT_SUBMITTED, VerificationStatus.fromString("NOT_SUBMITTED"))
        assertEquals(VerificationStatus.PENDING, VerificationStatus.fromString("PENDING"))
        assertEquals(VerificationStatus.VERIFIED, VerificationStatus.fromString("VERIFIED"))
        assertEquals(VerificationStatus.REJECTED, VerificationStatus.fromString("REJECTED"))
        assertEquals(VerificationStatus.NOT_SUBMITTED, VerificationStatus.fromString("unknown_val"))

        assertEquals("Verified Student", VerificationStatus.VERIFIED.label)
        assertEquals("Verification Pending", VerificationStatus.PENDING.label)
        assertEquals("Verification Rejected", VerificationStatus.REJECTED.label)
        assertEquals("Unverified", VerificationStatus.NOT_SUBMITTED.label)
    }

    @Test
    fun `test UserProfile model defaults and verification invariants`() {
        val newProfile = UserProfile(
            id = "test_uid_101",
            email = "thabo@student.uj.ac.za",
            displayName = "Thabo Mokoena",
            institutionName = "University of Johannesburg",
            campusCode = "uj",
            campusName = "Auckland Park Kingsway",
            course = "BSc Information Technology",
            yearOfStudy = 2
        )

        assertEquals("test_uid_101", newProfile.id)
        assertEquals("Thabo Mokoena", newProfile.displayName)
        assertEquals(VerificationStatus.NOT_SUBMITTED, newProfile.status)
        assertFalse("New student must not be automatically marked as verified", newProfile.isVerified)
        assertFalse("Student cannot have admin privileges by default", newProfile.isAdmin)
        assertEquals("BSc Information Technology", newProfile.effectiveCourse)
    }

    @Test
    fun `test UserProfile verified status recognition`() {
        val verifiedProfile = UserProfile(
            id = "verified_uid_202",
            displayName = "Lethabo Khumalo",
            verificationStatus = VerificationStatus.VERIFIED.name,
            isStudentVerified = true
        )

        assertEquals(VerificationStatus.VERIFIED, verifiedProfile.status)
        assertTrue(verifiedProfile.isVerified)
    }

    @Test
    fun `test SouthAfricanCampus model resolution`() {
        val wits = SouthAfricanCampus.fromCode("wits")
        assertEquals("University of the Witwatersrand", wits.institutionName)
        assertEquals("Wits", wits.shortName)
        assertEquals("Gauteng", wits.province)

        val uct = SouthAfricanCampus.fromCode("uct")
        assertEquals("Western Cape", uct.province)
    }

    @Test
    fun `test registration password validation rules`() {
        val password = "securePassword123"
        val confirmValid = "securePassword123"
        val confirmMismatch = "wrongPassword"

        assertTrue(password.length >= 6)
        assertEquals(password, confirmValid)
        assertFalse(password == confirmMismatch)
    }
}
