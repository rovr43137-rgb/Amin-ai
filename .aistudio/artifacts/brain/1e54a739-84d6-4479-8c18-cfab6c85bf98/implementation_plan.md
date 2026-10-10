# AETHER AI (أثير) — Cloud-Powered Arabic Vision & Voice Assistant

A futuristic, open-source Android AI vision assistant that connects the smartphone camera directly to free-tier cloud intelligence. Users point the camera at any scene in the real world, converse naturally in pristine Modern Standard Arabic (العربية الفصحى المطلقة), and receive real-time, context-aware spoken and visual answers without requiring costly subscriptions or heavy on-device model downloads.

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural blueprint has been refined according to the project specifications:

- **Cloud-First & Free Tier Prioritized**: Core intelligence runs in the cloud. Primary provider is Google Gemini API (utilizing the generous free tier with `gemini-2.5-flash` / `gemini-3.5-flash`) alongside modular support for OpenRouter free models, user-supplied API keys, and custom endpoints. No multi-gigabyte local models required on the device.
- **Genuine Vision & No Fake Detections**: Every analysis is backed by real frames captured from the CameraX preview and evaluated by actual cloud AI models. No simulated bounding boxes or pre-scripted responses.
- **Arabic-First UTF-8 & Native RTL**: Complete UTF-8 encoding across all resources, natural RTL layout orientation, enhanced Arabic text-to-speech with dialect customization, and speech-to-text recognition.
- **Rate Limit & Bandwidth Conscious**: Live analysis frequency is smartly throttled (configurable from 3s to 15s or manual on-demand trigger) to respect free-tier RPM/TPM limits and conserve mobile data.
- **Privacy & User Consent**: Explicit consent dialog on first launch explaining that camera images and voice queries are transmitted securely to cloud AI providers for processing.

---

## 1. Overview & Core Concept

- **What It Does**: AETHER AI serves as an intelligent visual companion. The user points the camera at an object, document, environment, or activity, speaks in natural Arabic, and the app captures high-quality frames, transmits them to a free-tier cloud vision model, and delivers spoken and written answers in Arabic.
- **Target Audience**: Arabic speakers, students, visually impaired individuals, professionals, and technology enthusiasts seeking a futuristic, accessible, and free vision assistant.
- **Licensing & Extensibility**: Open-source under the MIT License with a modular provider interface allowing developers and users to plug in any vision API.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **First-Launch Privacy & Cloud Consent**:
   - Futuristic holographic briefing explaining that camera frames and audio are processed via cloud AI APIs.
   - User grants camera, audio, and network permissions.
2. **Live Viewfinder & Cybernetic HUD**:
   - Full-screen real CameraX preview.
   - Futuristic glowing HUD overlays: scanline reticle, live telemetry (Active Cloud Provider, Latency ms, Analysis Frequency, Camera FPS, Audio Waveform).
   - Visual radar pulse indicating when an active analysis frame is being sent to the cloud.
3. **Arabic Voice & Analysis Loop**:
   - **Voice Input**: Tap mic or hold to speak in Arabic (*"ما الذي تراه أمامي؟"*, *"اقرأ النص في هذه الورقة"*, *"ما نوع هذا النبات؟"*).
   - **Live Periodic Analysis**: Toggle continuous scanning (default: every 5 seconds) or one-tap instant capture.
   - **Spoken AI Response**: Pristine Modern Standard Arabic voice synthesis via Android TextToSpeech, accompanied by live glowing subtitle transcription.
4. **Provider & Settings Control Drawer**:
   - Seamlessly switch between Gemini Cloud Free Tier, OpenRouter, Groq/Custom API.
   - Enter user-specific API keys securely (stored locally in private DataStore).
   - Dialect selector (فصحى معيارية, خليجي, مصري, شامي, مغاربي) and speech speed/pitch adjustment.
   - Transparent Quota & Free Tier Info banner explaining usage limits and preventing surprise charges.

### Visual Identity & Theme
- **Aesthetic Direction**: Sci-fi aerospace telemetry console with dark cybernetic neon aesthetics.
- **Color Palette**:
  - Deep Space Canvas: `#050811` (with transparent camera pass-through)
  - Neon Cyan / Aether Blue (Primary): `#00F0FF`
  - Cyber Violet (Accent): `#A855F7`
  - Sensor Active Emerald: `#10B981`
  - Radar Amber (Scanning/Busy): `#F59E0B`
  - Error/Limit Alert Red: `#EF4444`
- **Typography & RTL**:
  - Dynamic RTL (`LocalLayoutDirection provides LayoutDirection.Rtl`) with proper Arabic font rendering.
  - High-contrast monospace digits for telemetry metrics.

---

## 3. Key Product Decisions & Trade-Offs

- **Cloud Vision over On-Device Heavy Models**:
  - *Chosen Approach*: Offload multimodal vision understanding to verified free cloud endpoints (Google Gemini 2.5/3.5 Flash free tier, OpenRouter).
  - *Why*: Eliminates 4GB+ model downloads, battery drain, and thermal throttling on mobile devices while providing state-of-the-art vision reasoning.
  - *Mitigation for Free-Tier Limits*: Intelligent frame throttling (never spamming frames faster than API quotas allow), payload compression (JPEG quality 75, max dimension 1024px), and clear error messages when quotas are reached.
- **Genuine Android Speech Services**:
  - *Chosen Approach*: System `SpeechRecognizer` with `Locale("ar")` and Android `TextToSpeech` tuned with Arabic phonemes, with graceful text-only fallback if the device lacks offline Arabic voice packs.
  - *Why*: Completely free, low latency, and zero dependency on paid voice APIs.
- **Modular Cloud AI Provider Architecture**:
  - *Chosen Approach*: Clean interface `VisionCloudProvider` implemented by:
    1. `GeminiCloudProvider`: Direct REST integration using `BuildConfig.GEMINI_API_KEY` or user custom key.
    2. `OpenRouterCloudProvider`: Supports free open vision models.
    3. `CustomCompatibleProvider`: Allows OpenAI/OpenRouter compatible endpoints.
  - *Why*: Ensures the app remains 100% operational if any single provider changes their free tier terms.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                     AETHER COMPOSE UI (RTL ARABIC)                     │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                     CameraX Live Preview                         │  │
│  │  ┌────────────────────────────────────────────────────────────┐  │  │
│  │  │     Cybernetic HUD Overlay (Scan Reticle, Telemetry, FPS)  │  │  │
│  │  │     Interactive Audio Waveform & Arabic Subtitles          │  │  │
│  │  └────────────────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────────────────┐  │  │
│  │  │     Controls: Mic PTT, Live Stream Toggle, Settings Drawer │  │  │
│  │  └────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Frame Snapshot + Arabic Query
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        AETHER VIEWMODEL LAYER                          │
│  - Analysis Scheduler (Interval throttler: 3s - 15s)                   │
│  - Speech Manager (SpeechRecognizer + TTS Arabic Controller)           │
│  - Privacy & Consent State Coordinator                                 │
│  - Network State & Error Recovery Handler                              │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Base64 JPEG + Prompt
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   MODULAR CLOUD AI PROVIDER HUB                        │
│   ├── GeminiCloudProvider (Gemini 2.5 Flash Free Tier)                 │
│   ├── OpenRouterCloudProvider (Free Vision Models)                     │
│   └── CustomEndpointProvider (User-configured Base URL & Key)          │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      LOCAL PREFERENCES & CACHE                         │
│  - Encrypted / DataStore Settings (API Keys, Dialect, Scan Interval)   │
│  - Transient Session History (Vision insights log)                     │
└────────────────────────────────────────────────────────────────────────┘
```

### Build & Verification Milestones
1. **Platform Setup & Manifest**: Configure permissions (`CAMERA`, `RECORD_AUDIO`, `INTERNET`), application name in Arabic and English, and secrets integration.
2. **CameraX & Frame Capture Pipeline**: Implement real camera preview with on-demand and periodic bitmap downsampling.
3. **Arabic Voice Engine**: Configure `TextToSpeech` and `SpeechRecognizer` for Arabic (`ar`) with pitch, rate, and dialect options.
4. **Cloud AI Vision Integration**: Implement `GeminiCloudProvider` with direct REST calls using `gemini-2.5-flash`, handling quotas, timeouts, and JSON responses.
5. **Multi-Provider & Settings UI**: Provider selection drawer, custom API keys input, free tier transparency notice, and scan interval slider.
6. **Futuristic Cybernetic HUD**: Animated reticle, audio visualizer, scanline effect, and Arabic telemetry metrics.
7. **Build & Compilation Verification**: Run `compile_applet` to ensure zero compilation or syntax errors.
