package com.intellisoft.kabarakmhis.helperclass

import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Quantity
import org.hl7.fhir.r4.model.Questionnaire

class QuestionnaireHelper {

    fun codingQuestionnaire(
        code: String,
        display: String,
        text: String
    ): Observation {
        val observation = Observation()
        observation
            .code
            .addCoding()
            .setSystem("http://snomed.info/sct")
            .setCode(code).display = display
        observation.code.text = display
        observation.valueStringType.value = text
        return observation
    }

    fun quantityQuestionnaire(
        code: String,
        display: String,
        text: String,
        quantity: String,
        units: String
    ): Observation? {

        try{
            val observation = Observation()
            observation
                .code
                .addCoding()
                .setSystem("http://snomed.info/sct")
                .setCode(code).display = display
            observation.code.text = display
            observation.value = Quantity()
                .setValue(quantity.toBigDecimal())
                .setUnit(units)
                .setSystem("http://unitsofmeasure.org")
            return observation

        } catch(e: NumberFormatException){ // handle your exception
            return null
        }


    }
}