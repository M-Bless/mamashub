package com.kabarak.kabarakmhis.pnc.data_class

data class Child(
    val id: String,
    val name: String,
    val birthDate: String,
)

data class CivilRegistration(
    val id: String,
    val name: String,
    val sexOfChild: String,
    val birthDate: String,
)
data class IPV(
    val id: String,
    val dateGiven: String,
    val nextVisit: String,
)