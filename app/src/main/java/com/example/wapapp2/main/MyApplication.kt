package com.example.wapapp2.main

import android.app.Application
import androidx.collection.arrayMapOf
import com.example.wapapp2.R
import com.example.wapapp2.model.BankDTO

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase

import net.danlew.android.joda.JodaTimeAndroid

class MyApplication : Application() {
    companion object {
        val BANK_MAPS = arrayMapOf<String, BankDTO>()
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)

        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
        JodaTimeAndroid.init(applicationContext)
        initBanks()
    }

    private fun initBanks() {
        val bankNamesList = resources.getStringArray(R.array.bank_name_list)
        val bankIconsList = resources.getIntArray(R.array.bank_icon_list)

        for ((index, value) in bankNamesList.withIndex()) {
            BANK_MAPS[index.toString()] = BankDTO(value, bankIconsList[index], index.toString())
        }
    }

}