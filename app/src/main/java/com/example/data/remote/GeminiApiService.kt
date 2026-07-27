package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun queryCopilot(userQuery: String, currentStreet: String, isVtcMode: Boolean): String {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Local fallback Copilot intelligence when API Key isn't provided in UI secrets
            return generateLocalFallbackResponse(userQuery, currentStreet, isVtcMode)
        }

        val systemPrompt = """
            Tu es Gemini Live, le copilote vocal intelligent de Drive AI pour les chauffeurs VTC et conducteurs en Côte d'Ivoire (Abidjan).
            Réponds toujours en français parlé, de manière concise (max 2 à 3 phrases) pour ne pas distraire le conducteur.
            Tu connais parfaitement la géographie d'Abidjan : Plateau, Cocody, Marcory, Zone 4, Yopougon, Treichville, Adjamé, Pont Henri Konan Bédié, Boulevard Mitterrand, Voie Express.
            Position actuelle du véhicule : $currentStreet, Abidjan.
            Mode Chauffeur VTC actif : $isVtcMode.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = userQuery)),
                    role = "user"
                )
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.6f, maxOutputTokens = 300)
        )

        return try {
            val response = service.generateContent(apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!replyText.isNullOrBlank()) {
                replyText
            } else {
                generateLocalFallbackResponse(userQuery, currentStreet, isVtcMode)
            }
        } catch (e: Exception) {
            generateLocalFallbackResponse(userQuery, currentStreet, isVtcMode)
        }
    }

    private fun generateLocalFallbackResponse(query: String, street: String, isVtcMode: Boolean): String {
        val q = query.lowercase()
        return when {
            q.contains("station") || q.contains("carburant") || q.contains("essence") ->
                "Station Petroci à 300m sur le Boulevard Mitterrand (Zone $street). Prix Super: 875 FCFA. Voulez-vous y faire une étape ?"

            q.contains("embouteillage") || q.contains("bouchon") || q.contains("trafic") ->
                "Trafic modéré vers le Plateau. Je vous suggère le Pont HKB pour gagner 12 minutes vers Marcory."

            q.contains("manger") || q.contains("restaurant") || q.contains("maquis") ->
                "Maquis Chez Tantine à 500m près de votre position à $street. Spécialité Garba et poisson frais."

            q.contains("course") || q.contains("client") || q.contains("vtc") || isVtcMode ->
                "Prochain arrêt VTC : Dépôt passager au Plateau Immeuble CCIA. Durée estimée 14 minutes, distance 6.8 km."

            else ->
                "Compris ! Suivez le guidage vocal vers la direction $street. Je surveille le trafic et les raccourcis pour vous."
        }
    }
}
