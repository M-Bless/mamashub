package com.kabarak.kabarakmhis.pnc.data_class

data class ChildPnc(
    val id: String,
    val visitTime: String,
    val generalCondition: String,
    val nextVisitDate: String,
)


data class ChildPncData(
    val id: String,
    val visitTime: String,
    val generalCondition: String,
    val nextVisitDate: String,
    val temperature: String,
    val breathsPerMinute: String,
    val feedingMethod: String,
    val umbilicalCordStatus: String,
    val clinicalNotes: String,

)