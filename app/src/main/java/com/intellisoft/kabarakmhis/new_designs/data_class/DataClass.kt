package com.intellisoft.kabarakmhis.new_designs.data_class

data class DbPatient(
    val resourceType: String,
    val id: String,
    val active: Boolean,
    val name: List<DbName>,
    val telecom: List<DbTelecom>,
    val gender: String,
    val birthDate: String,
    val address: List<DbAddress>
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
    Patient
}

data class DbPatientSuccess(
    val resourceType: String,
    val id: String
)
data class DbPatientResult(
    val total: Int,
    val entry: List<DBEntry>
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
    val address: List<DbAddress>
)