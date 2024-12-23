package com.simpleenergy.rideapplication

object AppConstants {
    const val MOTOR_ON ="MOTOR ON"
    const val TRAVEL_KM  = "2331 Km"
    const val ODO  =  "ODO"
    const val GREATER  =">"
    const val LESSER  = "<"
    const val SIMPLE_ENERGY ="Simple Energy"
    const val TRIP_KM ="044 Kms"
    const val TRIP ="Trip A"
    const val EMPTY_STRING =""

    fun getDataSets(isPortrait:Boolean): List<Float> {
        return if (isPortrait) {
            listOf(45f, 29f, 23f, 20f, 20f) // Portrait data
        }
        else {
            listOf(60f, 40f, 30f, 25f, 25f) // Landscape data
        }
    }
}