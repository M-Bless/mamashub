package com.intellisoft.kabarakmhis.new_designs.data_class

data class DbPatient(
    val resourceType: String,
    val id: String,
    val active: Boolean,
    val name: List<DbName>,
    val telecom: List<DbTelecom>,
    val gender: String,
    val birthDate: String,
    val address: List<DbAddress>,
    val contact: List<DbContact>
)
data class DbContact(
    val relationship: ArrayList<DbRshp>?,
    val name: DbName,
    val telecom: List<DbTelecom>
)
data class DbRshp(
    val text: String
)
data class DbName(
    val family: String,
    val given: List<String>
)
data class DbTelecom(
    val system: String,
    val value: String
)
data class DbAddress(
    val text: String,
    val line: List<String>,
    val city: String,
    val district: String,
    val state:String,
    val country:String
)
enum class DbResourceType {
    Patient,
    Encounter,
    Observation
}
enum class DbResourceViews {
    MEDICAL_HISTORY,
    PREGNANCY_DETAILS
}

data class DbPatientSuccess(
    val resourceType: String,
    val id: String
)
data class DbPatientResult(
    val total: Int,
    val entry: ArrayList<DBEntry>?
)
data class DBEntry(
    val resource: DbResourceData
    )
data class DbResourceData(
    val resourceType: String,
    val id: String,
    val active: Boolean,
    val name: List<DbName>,
    val telecom: List<DbTelecom>,
    val gender: String,
    val birthDate: String,
    val address: List<DbAddress>,
    val contact: List<DbContact>?
)
data class DbEncounter(
    val resourceType: String,
    val id : String,
    val subject : DbSubject,
    val reasonCode: List<DbReasonCode>
)
data class DbSubject(
    val reference: String
)
data class DbEncounterData(
    val reference: String
)
data class DbReasonCode(
    val text: String
)
data class DbEncounterList(
    val total: Int,
    val entry: List<DbEncounterEntry>?
)
data class DbEncounterDetailsList(
    val total: Int,
    val entry: List<DbEncounterDataEntry>?
)
data class DbEncounterDataEntry(
    val resource: DbEncounterDataResourceData
)
data class DbEncounterEntry(
    val resource: DbEncounterResourceData
)
data class DbEncounterResourceData(
    val resourceType: String,
    val id: String,
    val reasonCode: List<DbReasonCode>
)
data class DbEncounterDataResourceData(
    val resourceType: String,
    val id: String,
    val code: DbCode?
)
data class DbObservation(
    val resourceType: String,
    val id: String,
    val subject: DbSubject,
    val encounter: DbEncounterData,
    val code: DbCode
)
data class DbCode(
    val coding: List<DbCodingData>,
    val text: String
)
data class DbCodingData(
    val system: String,
    val code: String,
    val display:String
)

data class DbObservationValue(
    val valueList: HashSet<DbObservationData>
)
data class DbObservationData(
    val code: String,
    val valueList: HashSet<String>
)

data class DbObserveValue(
    val title: String,
    val value : String
)