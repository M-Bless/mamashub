package com.kabarak.kabarakmhis.pnc.data_class

data class Child(
    val id: String,
    val name: String,
    val birthDate: String,
)

data class ChildDetail(
    val question: String,
    val answer: String,
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

data class QuestionnaireDetails(
    val detailQuestion: String,
    val detailAnswer: String,
)


data class CancerScreening(
    val id: String,
    val type: String,
    val date: String,
    val responseId: String,

)

data class EyeProblems(
    val id: String,
    val VisitType: String,
    val VisitDate: String,
)
