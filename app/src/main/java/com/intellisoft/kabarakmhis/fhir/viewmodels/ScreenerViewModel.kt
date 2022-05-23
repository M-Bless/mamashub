package com.intellisoft.kabarakmhis.fhir.viewmodels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.gson.Gson
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.screens.ScreenerFragment
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.helperclass.QuestionnaireHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.codesystems.RiskProbability
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*


const val TAG = "ScreenerViewModel"

/** ViewModel for screener questionnaire screen {@link ScreenerEncounterFragment}. */
class ScreenerViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {
    val questionnaire: String
        get() = getQuestionnaireJson()
    val isResourcesSaved = MutableLiveData<Boolean>()
//    val apgarScore = MutableLiveData<ApGar>() 
    var isSafe = false

    private val questionnaireResource: Questionnaire
        get() = FhirContext.forR4().newJsonParser().parseResource(questionnaire) as Questionnaire
    private var questionnaireJson: String? = null
    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

    private val user = FhirApplication.getProfile(application.applicationContext)
    private val gson = Gson()
    private var frequency: Int = 0
    private var volume: String = "0"


    fun saveScreenerEncounter(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    getApplication(),
                    questionnaireResource,
                    questionnaireResponse
                )
            val context = FhirContext.forR4()

            Log.e(
                "Questionnaire Res::: " , context.newJsonParser()
                    .encodeResourceToString(questionnaireResponse)
            )


            val subjectReference = Reference("Patient/$patientId")
            val encounterId = generateUuid()
            if (isRequiredFieldMissing(bundle)) {
                isResourcesSaved.value = false
                return@launch
            }

            saveResources(bundle, subjectReference, encounterId)
//            generateRiskAssessmentResource(bundle, subjectReference, encounterId)
            isResourcesSaved.value = true
        }
    }


//    private suspend fun getEDD(itemsList1: MutableList<QuestionnaireResponse.QuestionnaireResponseItemComponent>): Triple<String, String, String> {
//
//        val formatHelper = FormatterClass()
//
//        var basicThree = Triple("", "", "")
//
//        for (mainItem in itemsList1) {
//
//            val mainLinkId = mainItem.linkId
//            val subItemList = mainItem.item
//            if (mainLinkId == "2.0.0") {
//
//                for (subItem in subItemList) {
//
//                    val subItemLinkId = subItem.linkId
//                    val subSubItemList = subItem.item
//
//                    val job = Job()
//
//                    CoroutineScope(Dispatchers.IO + job).launch {
//                        if (subItemLinkId == "2.1.2") {
//
//                            for (subSubItem in subSubItemList) {
//
//                                val pregnancyDetailsList = subSubItem.item
//
//                                for (pregnancyDetails in pregnancyDetailsList) {
//
//                                    val menstrualAnswerList = pregnancyDetails.answer
//
//                                    for (pregnancyDetailsItem in menstrualAnswerList) {
//                                        val lmp = pregnancyDetailsItem.value.dateTimeValue().value
//                                        val eddStr = formatHelper.getCalculations(lmp.toString())
//                                        val ges = formatHelper.calculateGestation(lmp.toString())
//                                        basicThree = Triple(
//                                            formatHelper.refineLMP(lmp.toString()),
//                                            eddStr,
//                                            ges
//                                        )
//                                    }
//
//                                }
//
//
//                            }
//
//                        }
//                    }.join()
//
//
//                }
//
//            }
//
//        }
//
//        return basicThree
//    }

//    fun saveRelatedPerson(questionnaireResponse: QuestionnaireResponse, patientId: String) {
//        viewModelScope.launch {
//            val bundle =
//                ResourceMapper.extract(
//                    getApplication(),
//                    questionnaireResource,
//                    questionnaireResponse
//                ).entryFirstRep
//            if (bundle.resource !is Patient) return@launch
//            val relatedPerson = bundle.resource as Patient
//
//            if (relatedPerson.hasBirthDate() && relatedPerson.hasGender()
//            ) {
//                val birthDate = relatedPerson.birthDate.toString()
//                val todayDate = FormatterClass().getTodayDate()
//                val isDateValid = FormatterClass().checkDate(birthDate, todayDate)
//
//                if (isDateValid) {
//                    val subjectReference = Reference("Patient/$patientId")
//                    relatedPerson.active = true
//                    relatedPerson.id = generateUuid()
//                    relatedPerson.linkFirstRep.other = subjectReference
//                    fhirEngine.create(relatedPerson)
//                    isResourcesSaved.value = true
//                    return@launch
//                } else {
//                    isResourcesSaved.value = false
//                }
//
//            }
//            isResourcesSaved.value = false
//        }
//    }

//    fun saveMaternity(questionnaireResponse: QuestionnaireResponse, patientId: String) {
//        viewModelScope.launch {
//            val bundle =
//                ResourceMapper.extract(
//                    getApplication(),
//                    questionnaireResource,
//                    questionnaireResponse
//                )
//            val context = FhirContext.forR4()
//
//
//            val questionnaire =
//                context.newJsonParser().encodeResourceToString(questionnaireResponse)
//            try {
//                if (isRequiredFieldMissing(bundle)) {
//                    isResourcesSaved.value = false
//                    return@launch
//                }
//
//                val qh = QuestionnaireHelper()
//                val value = extractStatus(questionnaire)
//
//                if (value.isNotEmpty()) {
//                    bundle.addEntry()
//                        .setResource(
//                            qh.codingQuestionnaire(
//                                "Mother's Health",
//                                value,
//                                value
//                            )
//                        )
//                        .request.url = "Observation"
//                }
//                val itemsList1 = questionnaireResponse.item
//                val basicThree = getEDD(itemsList1)
//
//                if (basicThree.first.isNotEmpty() && basicThree.second.isNotEmpty() && basicThree.third.isNotEmpty()) {
//                    bundle.addEntry()
//                        .setResource(
//                            qh.codingQuestionnaire(
//                                "Expected Date of Delivery",
//                                "Expected Date of Delivery",
//                                basicThree.first
//                            )
//                        )
//                        .request.url = "Observation"
//                    bundle.addEntry()
//                        .setResource(
//                            qh.codingQuestionnaire(
//                                "Last Menstrual Period",
//                                "Last Menstrual Period",
//                                basicThree.second
//                            )
//                        )
//                        .request.url = "Observation"
//
//                    bundle.addEntry()
//                        .setResource(
//                            qh.codingQuestionnaire(
//                                "Gestation",
//                                "Gestation",
//                                basicThree.third
//                            )
//                        )
//                        .request.url = "Observation"
//                }
//
//
//                val subjectReference = Reference("Patient/$patientId")
//                val encounterId = generateUuid()
//                saveResources(bundle, subjectReference, encounterId)
////                generateRiskAssessmentResource(bundle, subjectReference, encounterId)
//                isResourcesSaved.value = true
//                // return@launch
//
//
//            } catch (e: Exception) {
//
//                isResourcesSaved.value = false
//                return@launch
//            }
//
//
//        }
//    }

//    private fun extractStatus(questionnaire: String): String {
//        val json = JSONObject(questionnaire)
//        val common = json.getJSONArray("item")
//        var value = ""
//        for (i in 0 until common.length()) {
//
//            val item = common.getJSONObject(i)
//            val parent = item.getJSONArray("item")
//            for (j in 0 until parent.length()) {
//
//                val itemChild = parent.getJSONObject(j)
//                val child = itemChild.getJSONArray("item")
//                for (k in 0 until child.length()) {
//                    val inner = child.getJSONObject(k)
//                    val childChild = inner.getString("linkId")
//
//                    if (childChild == "Mothers-Status") {
//
//                        value = extractValueString(inner)
//
//                    }
//                }
//            }
//        }
//        return value
//    }



//    private fun extractValueString(inner: JSONObject): String {
//
//        val childAnswer = inner.getJSONArray("item")
//        val ans = childAnswer.getJSONObject(0).getJSONArray("answer")
//
//        return ans.getJSONObject(0).getString("valueString")
//    }

    fun saveAssessment(questionnaireResponse: QuestionnaireResponse, patientId: String) {

        viewModelScope.launch {
            val bundle =
                ResourceMapper.extract(
                    getApplication(),
                    questionnaireResource,
                    questionnaireResponse
                )
            val context = FhirContext.forR4()
            val qh = QuestionnaireHelper()

            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)

            try {

                val json = JSONObject(questionnaire)
                val common = json.getJSONArray("item")
                for (i in 0 until common.length()) {

                    val item = common.getJSONObject(i)

                    val parent = item.getJSONArray("item")


                    for (j in 0 until parent.length()) {

                        val child1 = parent.getJSONObject(j)
                        val childChild = child1.getString("linkId")

                        Log.e("----1 ", childChild.toString())

                        if (childChild == "kinPhone") {

                            val childAnswer = child1.getJSONArray("answer")
                            val value = childAnswer.getJSONObject(0).getString("valueString")

                            bundle.addEntry()
                                .setResource(
                                    qh.codingQuestionnaire(
                                        "Next of Kin Phone Number",
                                        value,
                                        value
                                    )
                                )
                                .request.url = "Observation"
                        }
                        if (childChild == "kinName") {

                            val childAnswer = child1.getJSONArray("answer")
                            val value = childAnswer.getJSONObject(0).getString("valueString")

                            bundle.addEntry()
                                .setResource(
                                    qh.codingQuestionnaire(
                                        "Next of Kin Name",
                                        value,
                                        value
                                    )
                                )
                                .request.url = "Observation"
                        }

                    }
                }

                val subjectReference = Reference("Patient/$patientId")
                val encounterId = generateUuid()
                saveResources(bundle, subjectReference, encounterId)



                isResourcesSaved.value = true

            } catch (e: Exception) {



                isResourcesSaved.value = false
                return@launch
            }
        }
    }



    private suspend fun saveResources(
        bundle: Bundle,
        subjectReference: Reference,
        encounterId: String
    ) {
        val encounterReference = Reference("Encounter/$encounterId")
        bundle.entry.forEach {
            when (val resource = it.resource) {
                is Observation -> {

                    if (resource.hasCode()) {
                        resource.id = generateUuid()
                        resource.subject = subjectReference
                        resource.encounter = encounterReference
                        saveResourceToDatabase(resource)
                    }
                }
                is Condition -> {

                    if (resource.hasCode()) {
                        resource.id = generateUuid()
                        resource.subject = subjectReference
                        resource.encounter = encounterReference
                        saveResourceToDatabase(resource)
                    }
                }
                is Encounter -> {

                    resource.subject = subjectReference
                    resource.id = encounterId
                    saveResourceToDatabase(resource)
                }
            }
        }
    }

    private fun isRequiredFieldMissing(bundle: Bundle): Boolean {
        bundle.entry.forEach {
            val resource = it.resource
            when (resource) {
                is Observation -> {
                    if (resource.hasValueQuantity() && !resource.valueQuantity.hasValueElement()) {
                        return true
                    }

                }


                // TODO check other resources inputs
            }
        }
        return false
    }

    private suspend fun saveResourceToDatabase(resource: Resource) {
        fhirEngine.create(resource)

    }

    private fun getQuestionnaireJson(): String {
        questionnaireJson?.let {
            return it
        }
        questionnaireJson =
            readFileFromAssets(state[ScreenerFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
        return questionnaireJson!!
    }

    private fun readFileFromAssets(filename: String): String {
        return getApplication<Application>().assets.open(filename).bufferedReader().use {
            it.readText()
        }
    }

    private fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }

    private suspend fun generateApgarAssessmentResource(
        bundle: Bundle,
        subjectReference: Reference,
        encounterId: String,
        total: Int
    ) {

        val riskProbability = getProbability(total)
        riskProbability?.let { rProbability ->
            val riskAssessment =
                RiskAssessment().apply {
                    id = generateUuid()
                    subject = subjectReference
                    encounter = Reference("Encounter/$encounterId")
                    addPrediction().apply {
                        qualitativeRisk =
                            CodeableConcept().apply {
                                addCoding().updateRiskProbability(
                                    rProbability
                                )
                            }
                    }
                    occurrence = DateTimeType.now()
                }
            saveResourceToDatabase(riskAssessment)
        }

    }

    private fun getProbability(
        total: Int
    ): RiskProbability? {
        if (total <= 3) return RiskProbability.HIGH else if (total in 4..6) return RiskProbability.MODERATE else if (total > 6) return RiskProbability.LOW
        return null
    }

//    private suspend fun generateRiskAssessmentResource(
//        bundle: Bundle,
//        subjectReference: Reference,
//        encounterId: String
//    ) {
//        val spO2 = getSpO2(bundle)
//        spO2?.let {
//            val isSymptomPresent = isSymptomPresent(bundle)
//            val isComorbidityPresent = isComorbidityPresent(bundle)
//            val riskProbability = getRiskProbability(isSymptomPresent, isComorbidityPresent, it)
//            riskProbability?.let { rProbability ->
//                val riskAssessment =
//                    RiskAssessment().apply {
//                        id = generateUuid()
//                        subject = subjectReference
//                        encounter = Reference("Encounter/$encounterId")
//                        addPrediction().apply {
//                            qualitativeRisk =
//                                CodeableConcept().apply {
//                                    addCoding().updateRiskProbability(
//                                        rProbability
//                                    )
//                                }
//                        }
//                        occurrence = DateTimeType.now()
//                    }
//                saveResourceToDatabase(riskAssessment)
//            }
//        }
//    }

    private fun getRiskProbability(
        isSymptomPresent: Boolean,
        isComorbidityPresent: Boolean,
        spO2: BigDecimal
    ): RiskProbability? {
        if (spO2 < BigDecimal(90)) {
            return RiskProbability.HIGH
        } else if (spO2 >= BigDecimal(90) && spO2 < BigDecimal(94)) {
            return RiskProbability.MODERATE
        } else if (isSymptomPresent) {
            return RiskProbability.MODERATE
        } else if (spO2 >= BigDecimal(94) && isComorbidityPresent) {
            return RiskProbability.MODERATE
        } else if (spO2 >= BigDecimal(94) && !isComorbidityPresent) {
            return RiskProbability.LOW
        }
        return null
    }

    private fun Coding.updateRiskProbability(riskProbability: RiskProbability) {
        code = riskProbability.toCode()
        display = riskProbability.display
    }

//    private fun getSpO2(bundle: Bundle): BigDecimal? {
//        return bundle
//            .entry
//            .asSequence()
//            .filter { it.resource is Observation }
//            .map { it.resource as Observation }
//            .filter {
//                it.hasCode() && it.code.hasCoding() && it.code.coding.first().code.equals(
//                    Logics.SPO2
//                )
//            }
//            .map { it.valueQuantity.value }
//            .firstOrNull()
//    }
//
//    private fun isSymptomPresent(bundle: Bundle): Boolean {
//        val count =
//            bundle
//                .entry
//                .filter { it.resource is Observation }
//                .map { it.resource as Observation }
//                .filter { it.hasCode() && it.code.hasCoding() }
//                .flatMap { it.code.coding }
//                .map { it.code }
//                .filter { isSymptomPresent(it) }
//                .count()
//        return count > 0
//    }
//
//    private fun isSymptomPresent(symptom: String): Boolean {
//        return Logics.symptoms.contains(symptom)
//    }
//
//    private fun isComorbidityPresent(bundle: Bundle): Boolean {
//        val count =
//            bundle
//                .entry
//                .filter { it.resource is Condition }
//                .map { it.resource as Condition }
//                .filter { it.hasCode() && it.code.hasCoding() }
//                .flatMap { it.code.coding }
//                .map { it.code }
//                .filter { isComorbidityPresent(it) }
//                .count()
//        return count > 0
//    }
//
//    private fun isComorbidityPresent(comorbidity: String): Boolean {
//        return Logics.comorbidities.contains(comorbidity)
//    }

    /***
     * apgar score
     * ***/


}

private operator fun String.div(divisor: Int): Int {
    return divisor

}

private operator fun String.rem(divisor: Int): Int {
    return divisor

}

