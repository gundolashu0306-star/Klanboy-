package com.example.data.model

/**
 * South African Higher Education Institutions (Universities and TVET Campuses).
 * Representing authentic South African student life across all 9 provinces.
 */
enum class SouthAfricanCampus(
    val code: String,
    val institutionName: String,
    val shortName: String,
    val province: String,
    val primaryCity: String
) {
    UCT("uct", "University of Cape Town", "UCT", "Western Cape", "Cape Town"),
    WITS("wits", "University of the Witwatersrand", "Wits", "Gauteng", "Johannesburg"),
    UP("up", "University of Pretoria", "UP (Tuks)", "Gauteng", "Pretoria"),
    STELLENBOSCH("su", "Stellenbosch University", "Maties", "Western Cape", "Stellenbosch"),
    UJ("uj", "University of Johannesburg", "UJ", "Gauteng", "Johannesburg"),
    UKZN("ukzn", "University of KwaZulu-Natal", "UKZN", "KwaZulu-Natal", "Durban"),
    RHODES("ru", "Rhodes University", "Rhodes", "Eastern Cape", "Makhanda"),
    NMU("nmu", "Nelson Mandela University", "Mandela Uni", "Eastern Cape", "Gqeberha"),
    UWC("uwc", "University of the Western Cape", "UWC", "Western Cape", "Bellville"),
    NWU("nwu", "North-West University", "NWU", "North West", "Potchefstroom"),
    CPUT("cput", "Cape Peninsula University of Technology", "CPUT", "Western Cape", "Cape Town"),
    TUT("tut", "Tshwane University of Technology", "TUT", "Gauteng", "Pretoria"),
    CUT("cut", "Central University of Technology", "CUT", "Free State", "Bloemfontein"),
    DUT("dut", "Durban University of Technology", "DUT", "KwaZulu-Natal", "Durban"),
    UFS("ufs", "University of the Free State", "Kovsies", "Free State", "Bloemfontein"),
    UNIVEN("univen", "University of Venda", "Univen", "Limpopo", "Thohoyandou"),
    UL("ul", "University of Limpopo", "Turfloop", "Limpopo", "Polokwane"),
    WSU("wsu", "Walter Sisulu University", "WSU", "Eastern Cape", "Mthatha"),
    UNIZULU("unizulu", "University of Zululand", "UniZulu", "KwaZulu-Natal", "KwaDlangezwa"),
    SMU("smu", "Sefako Makgatho Health Sciences University", "SMU", "Gauteng", "Pretoria"),
    VUT("vut", "Vaal University of Technology", "VUT", "Gauteng", "Vanderbijlpark"),
    MUT("mut", "Mangosuthu University of Technology", "MUT", "KwaZulu-Natal", "Umlazi"),
    TVET_COLLEGES("tvet", "South African TVET College System", "TVET SA", "National", "All Campuses");

    companion object {
        fun fromCode(code: String): SouthAfricanCampus =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: UJ
    }
}
