package com.example.network

import android.util.Base64
import com.example.BuildConfig
import com.example.data.AetherSettings
import com.example.data.AiProvider
import com.example.data.AnalysisResult
import com.example.data.ArabicDialect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

interface VisionAiProvider {
    suspend fun analyze(
        jpegBytes: ByteArray,
        userPrompt: String,
        dialect: ArabicDialect,
        settings: AetherSettings,
        isStreamTick: Boolean
    ): Result<AnalysisResult>
}

class VisionProviderHub : VisionAiProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun analyze(
        jpegBytes: ByteArray,
        userPrompt: String,
        dialect: ArabicDialect,
        settings: AetherSettings,
        isStreamTick: Boolean
    ): Result<AnalysisResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            when (settings.provider) {
                AiProvider.GEMINI -> callGemini(jpegBytes, userPrompt, dialect, settings, isStreamTick, startTime)
                AiProvider.OPENROUTER -> callOpenRouter(jpegBytes, userPrompt, dialect, settings, isStreamTick, startTime)
                AiProvider.CUSTOM -> callCustomOpenAi(jpegBytes, userPrompt, dialect, settings, isStreamTick, startTime)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun callGemini(
        jpegBytes: ByteArray,
        userPrompt: String,
        dialect: ArabicDialect,
        settings: AetherSettings,
        isStreamTick: Boolean,
        startTime: Long
    ): Result<AnalysisResult> {
        val apiKey = settings.geminiCustomApiKey.ifBlank { BuildConfig.GEMINI_API_KEY }.trim()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return Result.failure(
                IllegalStateException("يرجى إدخال مفتاح Gemini API في إعدادات التطبيق أو من لوحة أسرار AI Studio لاستخدام التحليل البصري السحابي.")
            )
        }

        val base64Image = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
        val promptText = if (userPrompt.isNotBlank()) {
            userPrompt
        } else if (isStreamTick) {
            "انظر فورياً لما أمام الكاميرا الآن: حدد بدقة ما هو الشيء أو الشخص أو النص أو النشاط الموجود أمامك، وأخبر المستخدم فوراً باسمه ووصفه المباشر بأسلوب ناطق وذكي وموجز جداً (جملة أو جملتين)."
        } else {
            "حدد بدقة وذكاء ما هو الشيء أو الشخص أو النص الظاهر في هذه الصورة، واشرحه بإيجاز وبلاغة."
        }

        val systemPrompt = "أنت أثير (AETHER AI)، العين الذكية الناطقة للمستخدم في الوقت الحقيقي. وظيفتك هي النظر عبر الكاميرا والتعرف الفوري على كل شيء في العالم: الأشخاص، الأدوات، الأجهزة، الحيوانات، الأطعمة، النصوص، الأماكن والأنشطة. تحدث مباشرة فور رؤية الشيء، وابدأ فوراً بذكر ما تراه مثل 'أرى أمامك...' أو 'هذا...' دون أي مقدمات روتينية، وصِغه بجملة أو جملتين رشيقتين وممتعتين للاستماع الصوتي. تعليمات اللهجة: ${dialect.promptInstruction}"

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "$systemPrompt\n\nالسؤال/الطلب: $promptText")
                        })
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("maxOutputTokens", 400)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)
        
        // Primary supported model is gemini-flash-latest with fallback to gemini-3.5-flash
        val candidateModels = listOf("gemini-flash-latest", "gemini-3.5-flash")
        var lastErrorMsg = "خطأ في الاتصال بمزود الذكاء السحابي."
        var successfulResponse: String? = null
        var usedModel = "Gemini Flash"

        for (modelName in candidateModels) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string().orEmpty()
                    if (response.isSuccessful) {
                        successfulResponse = responseBody
                        usedModel = if (modelName == "gemini-flash-latest") "Gemini Flash" else "Gemini 3.5 Flash"
                        return@use
                    } else {
                        lastErrorMsg = when (response.code) {
                            404 -> "النموذج السحابي غير متوفر مؤقتاً (404)."
                            429 -> "تم بلوغ الحد الأقصى لطلبات الباقة المجانية مؤقتاً (429 Rate Limit). يرجى الانتظار لحظات."
                            403 -> "مفتاح Gemini API غير صالح أو تنقصه الصلاحية. تفقد المفتاح في الإعدادات."
                            400 -> "خطأ في معالجة إطار الصورة السحابي (400)."
                            else -> "خطأ في الاتصال بمزود الذكاء السحابي (${response.code})."
                        }
                    }
                }
                if (successfulResponse != null) break
            } catch (e: Exception) {
                lastErrorMsg = "تعذر الاتصال بخادم الذكاء الاصطناعي: ${e.message}"
            }
        }

        val latency = System.currentTimeMillis() - startTime
        if (successfulResponse == null) {
            return Result.failure(IllegalStateException(lastErrorMsg))
        }

        val json = JSONObject(successfulResponse!!)
        val candidates = json.optJSONArray("candidates")
        val text = candidates?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text")

        val cleanText = text?.trim() ?: "لم يتم استلام رد بصري من النموذج."
        return Result.success(
            AnalysisResult(
                prompt = promptText,
                arabicResponse = cleanText,
                latencyMs = latency,
                providerName = usedModel,
                isStreamTick = isStreamTick
            )
        )
    }

    private fun callOpenRouter(
        jpegBytes: ByteArray,
        userPrompt: String,
        dialect: ArabicDialect,
        settings: AetherSettings,
        isStreamTick: Boolean,
        startTime: Long
    ): Result<AnalysisResult> {
        val apiKey = settings.openRouterApiKey.trim()
        if (apiKey.isBlank()) {
            return Result.failure(
                IllegalStateException("يرجى إدخال مفتاح OpenRouter في الإعدادات للاستفادة من النماذج المفتوحة المجانية.")
            )
        }

        val base64Image = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
        val promptText = if (userPrompt.isNotBlank()) userPrompt else "ما الذي يظهر في هذا المشهد؟ صفه بدقة وإيجاز."
        val systemPrompt = "أنت أثير (AETHER AI)، المساعد البصري الذكي. تحدث بالعربية وفق هذه التعليمات: ${dialect.promptInstruction}"

        val requestJson = JSONObject().apply {
            put("model", settings.customModel.ifBlank { "google/gemini-2.0-flash-exp:free" })
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "text")
                            put("text", promptText)
                        })
                        put(JSONObject().apply {
                            put("type", "image_url")
                            put("image_url", JSONObject().apply {
                                put("url", "data:image/jpeg;base64,$base64Image")
                            })
                        })
                    })
                })
            })
            put("max_tokens", 400)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("HTTP-Referer", "https://github.com/aether-ai")
            .header("X-Title", "AETHER AI Android")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val latency = System.currentTimeMillis() - startTime
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return Result.failure(IllegalStateException("خطأ OpenRouter (${response.code}): $responseBody"))
            }

            val json = JSONObject(responseBody)
            val content = json.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                ?.trim() ?: "لم يتم استلام نص من OpenRouter."

            return Result.success(
                AnalysisResult(
                    prompt = promptText,
                    arabicResponse = content,
                    latencyMs = latency,
                    providerName = "OpenRouter",
                    isStreamTick = isStreamTick
                )
            )
        }
    }

    private fun callCustomOpenAi(
        jpegBytes: ByteArray,
        userPrompt: String,
        dialect: ArabicDialect,
        settings: AetherSettings,
        isStreamTick: Boolean,
        startTime: Long
    ): Result<AnalysisResult> {
        val baseUrl = settings.customBaseUrl.trim().trimEnd('/')
        val apiKey = settings.customApiKey.trim()
        val model = settings.customModel.ifBlank { "gpt-4o-mini" }.trim()

        val base64Image = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
        val promptText = if (userPrompt.isNotBlank()) userPrompt else "ما الذي يظهر في هذا المشهد؟ صفه بدقة وإيجاز."
        val systemPrompt = "أنت أثير (AETHER AI)، المساعد البصري الذكي. تحدث بالعربية وفق هذه التعليمات: ${dialect.promptInstruction}"

        val requestJson = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "text")
                            put("text", promptText)
                        })
                        put(JSONObject().apply {
                            put("type", "image_url")
                            put("image_url", JSONObject().apply {
                                put("url", "data:image/jpeg;base64,$base64Image")
                            })
                        })
                    })
                })
            })
            put("max_tokens", 400)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)
        val requestBuilder = Request.Builder()
            .url("$baseUrl/chat/completions")
            .post(body)

        if (apiKey.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer $apiKey")
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            val latency = System.currentTimeMillis() - startTime
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return Result.failure(IllegalStateException("خطأ المزود المخصص (${response.code}): $responseBody"))
            }

            val json = JSONObject(responseBody)
            val content = json.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                ?.trim() ?: "لم يتم استلام نص من الخادم المخصص."

            return Result.success(
                AnalysisResult(
                    prompt = promptText,
                    arabicResponse = content,
                    latencyMs = latency,
                    providerName = "Custom ($model)",
                    isStreamTick = isStreamTick
                )
            )
        }
    }
}
