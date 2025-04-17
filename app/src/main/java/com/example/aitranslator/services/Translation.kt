package com.example.aitranslator.services

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale


fun getAllLanguages(): List<String> = TranslateLanguage.getAllLanguages()

fun getLanguageName(languageCode: String): String {
    return Locale(languageCode).displayLanguage
}

fun languageIdentifier(lang: String, output: (String) -> Unit) {
    val identifier: LanguageIdentifier = LanguageIdentification.getClient()

    identifier.identifyLanguage(lang).addOnSuccessListener { languageCode ->
        output(languageCode)
    }.addOnFailureListener {
        output("Error")
    }

}

fun translation(
    text: String,
    targetLanguage: String = "hi",
    sourceLanguage: String = TranslateLanguage.ENGLISH,
    output: (String) -> Unit = {}
) {
    val translator = TranslatorOptions.Builder().setSourceLanguage(sourceLanguage)
        .setTargetLanguage(targetLanguage).build()

    val client = Translation.getClient(translator)

    client.downloadModelIfNeeded(
        DownloadConditions.Builder().requireWifi().requireCharging().build()
    ).addOnSuccessListener {
        client.translate(text).addOnSuccessListener { output(it.orEmpty()) }
    }.addOnFailureListener {
        output("Error")
    }
}

//
//fun correctGrammar(inputText: String): String {
//    val languageTool = JLanguageTool( BritishEnglish())
//    val matches: List<RuleMatch> = languageTool.check(inputText)
//
//    var correctedText = inputText
//    for (match in matches.reversed()) {
//        correctedText = correctedText.substring(0, match.fromPos) +
//                match.suggestedReplacements.firstOrNull().orEmpty() +
//                correctedText.substring(match.toPos)
//    }
//    return correctedText
//}
