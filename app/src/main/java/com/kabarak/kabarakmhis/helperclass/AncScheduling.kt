package com.kabarak.kabarakmhis.helperclass

class AncSchedulingCalculator {


    var count = 0

    fun ancSchedule(input: Int):MutableList<Any>{

        val theList = mutableListOf<Any>()
        calculate(input, theList)

        return theList
    }

    private fun calculate(input: Int, thelist: MutableList<Any>) {

        if (count <= 9) {

            if (input < 20) {
                count += 1
                val weeks = 8
                thelist.add(input + weeks)
                calculate(input + weeks, thelist)
            } else if (input in 20..25) {
                count += 1
                val weeks = 6
                thelist.add(input + weeks)
                calculate(input + weeks, thelist)
            } else if (input in 26..33) {
                count += 1
                val weeks = 4
                thelist.add(input + weeks)
                calculate(input + weeks, thelist)
            } else if (input in 34..38) {
                count += 1
                val weeks = 2
                thelist.add(input + weeks)
                calculate(input + weeks, thelist)
            } else if (input in 39..40) {
                count += 1
                val weeks = 1
                thelist.add(input + weeks)
                calculate(input + weeks, thelist)
            }

        }
    }

}