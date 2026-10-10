package com.example.data

enum class AiProvider(
    val id: String,
    val titleArabic: String,
    val descriptionArabic: String,
    val isFreeByDefault: Boolean,
    val defaultModel: String
) {
    GEMINI(
        id = "gemini",
        titleArabic = "Google Gemini Flash (سحابي مجاني)",
        descriptionArabic = "باقة سحابية مجانية فائقة السرعة لمعالجة صور وفيديوهات الكاميرا",
        isFreeByDefault = true,
        defaultModel = "gemini-flash-latest"
    ),
    OPENROUTER(
        id = "openrouter",
        titleArabic = "OpenRouter (نماذج مجانية)",
        descriptionArabic = "منصة مجانية ومفتوحة تدعم نماذج رؤية متعددة بدون اشتراك",
        isFreeByDefault = true,
        defaultModel = "google/gemini-2.0-flash-exp:free"
    ),
    CUSTOM(
        id = "custom",
        titleArabic = "مزود مخصص (OpenAI Compatible)",
        descriptionArabic = "استخدم أي خادم أو عنوان API خاص بك متوافق مع OpenAI",
        isFreeByDefault = false,
        defaultModel = "gpt-4o-mini"
    )
}

enum class ArabicDialect(
    val id: String,
    val titleArabic: String,
    val promptInstruction: String,
    val localeTag: String
) {
    MSA(
        id = "msa",
        titleArabic = "الفصحى المطلقة (المعيارية)",
        promptInstruction = "تحدث باللغة العربية الفصحى المطلقة السليمة والنقية نحوياً ونطقاً، بصياغة موجزة وسلسة تناسب الإلقاء الصوتي.",
        localeTag = "ar"
    ),
    GULF(
        id = "gulf",
        titleArabic = "اللهجة الخليجية",
        promptInstruction = "تحدث باللهجة الخليجية الودودة والواضحة مع المحافظة على الدقة، واستخدم مفردات خليجية لطيفة وموجزة تناسب المساعد الذكي.",
        localeTag = "ar-SA"
    ),
    EGYPTIAN(
        id = "egyptian",
        titleArabic = "اللهجة المصرية",
        promptInstruction = "تحدث باللهجة المصرية الطبيعية السلسة والمحببة، بأسلوب ذكي ومرح وموجز.",
        localeTag = "ar-EG"
    ),
    LEVANTINE(
        id = "levantine",
        titleArabic = "اللهجة الشامية",
        promptInstruction = "تحدث باللهجة الشامية اللطيفة والواضحة، بأسلوب مهذب وموجز ودقيق.",
        localeTag = "ar-LB"
    ),
    MAGHREBI(
        id = "maghrebi",
        titleArabic = "اللهجة المغاربية المبسطة",
        promptInstruction = "تحدث بعربية فصحى ميسرة ومفهومة مع لمسة مغاربية راقية وموجزة.",
        localeTag = "ar-MA"
    )
}

data class AetherSettings(
    val provider: AiProvider = AiProvider.GEMINI,
    val geminiCustomApiKey: String = "",
    val openRouterApiKey: String = "",
    val customBaseUrl: String = "https://api.openai.com/v1/",
    val customApiKey: String = "",
    val customModel: String = "gpt-4o-mini",
    val dialect: ArabicDialect = ArabicDialect.MSA,
    val liveStreamIntervalSec: Int = 6,
    val speechRate: Float = 1.05f,
    val speechPitch: Float = 1.0f,
    val spokenResponseEnabled: Boolean = true,
    val isAutoNarrateEnabled: Boolean = true,
    val hasAcceptedConsent: Boolean = false
)

data class AnalysisResult(
    val id: Long = System.currentTimeMillis(),
    val prompt: String,
    val arabicResponse: String,
    val latencyMs: Long,
    val providerName: String,
    val isStreamTick: Boolean,
    val identifiedObject: String? = null
)

enum class SpeechUiState {
    IDLE,
    LISTENING,
    PROCESSING_VISION,
    SPEAKING
}

data class TelemetryState(
    val fps: Int = 30,
    val latencyMs: Long = 0,
    val framesAnalyzed: Int = 0,
    val isStreaming: Boolean = false,
    val isAutoNarrating: Boolean = true,
    val lastIdentifiedObject: String? = null,
    val isTorchOn: Boolean = false,
    val isFrontCamera: Boolean = false,
    val activeProviderTitle: String = AiProvider.GEMINI.titleArabic,
    val errorMessage: String? = null
)
