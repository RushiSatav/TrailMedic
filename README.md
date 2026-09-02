# 🏔️ MediTrail — Offline Wilderness Emergency First Aid Assistant

<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![MediaPipe LLM](https://img.shields.io/badge/Local%20AI-GGUF%20%2F%20Gemma--2B%20INT4-FF6F00.svg?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev/edge/mediapipe)
[![Offline First](https://img.shields.io/badge/Offline%20First-100%25%20Zero%20Signal%20Required-2E8B57.svg?style=for-the-badge)](https://developer.android.com)
[![Voice Enabled](https://img.shields.io/badge/Speech-STT%20%26%20Concise%20TTS-F6B93B.svg?style=for-the-badge)](https://developer.android.com)

**MediTrail** is an intelligent, **100% offline, on-device AI emergency first aid and triage assistant** built for mountaineers, backpackers, search-and-rescue teams, and wilderness travelers operating in remote backcountry areas with **zero cellular signal, internet access, or cloud connectivity**.

</div>

---

## 📑 Table of Contents
1. [Overview & Key Features](#-overview--key-features)
2. [System Architecture](#-system-architecture)
3. [Dual-Dataset Knowledge Engine](#-dual-dataset-knowledge-engine)
4. [🎙️ Hands-Free Speech Pipeline (STT & TTS)](#️-hands-free-speech-pipeline-stt--tts)
5. [🧠 On-Device Neural Model Support (.GGUF / .BIN / .TASK)](#-on-device-neural-model-support-gguf--bin--task)
6. [Emergency Protocols Covered](#-emergency-protocols-covered)
7. [Technology Stack](#-technology-stack)
8. [Getting Started & Installation](#-getting-started--installation)
9. [Project Structure](#-project-structure)
10. [Medical Disclaimer](#-medical-disclaimer)

---

## 🌟 Overview & Key Features

When accidents happen in deep wilderness trails, cloud-dependent assistants (ChatGPT, Gemini, Claude) fail completely due to lack of network connection. **MediTrail** turns any standard Android phone into an **offline emergency medical assistant**:

- **100% Offline-First Architecture**: Runs fully in airplane mode with zero external network dependencies.
- **Plain, Easy-to-Understand Language**: Instructions avoid complex medical jargon and deliver clear, life-saving steps in simple, direct language.
- **Dual Clinical Knowledge Base**: Combines an 8-category Wilderness Emergency Tree with a 44-condition First Aid Intent Dataset.
- **Typo-Tolerant Clinical Extractor (RAG)**: Automatically resolves misspelled search queries (e.g. `sanke bite`, `chokng`, `bleding`) and grounds responses on verified protocols.
- **Hands-Free Voice Interaction**:
  - **Speech-to-Text (STT)**: Offline voice dictation so responders can describe injuries while tending to a patient.
  - **Concise Text-to-Speech (TTS)**: Spoken voice guidance that vocalizes only the top 2 actionable steps and the triage question for fast, non-distracting audio assistance.
- **Battery-Aware Resource Management**: Automatically throttles token length and optimizes inference under low battery conditions (<15% and <5%).
- **Interactive Checklists & Structured Reports**: Check off completed first-aid actions, monitor red-flag warning signs, and export structured `.txt` reports formatted for Satellite SOS (Garmin inReach, Apple Emergency SOS, 406MHz PLB).

---

## 🏗️ System Architecture

```
                    ┌────────────────────────────────────────────────────────┐
                    │               EMERGENCY USER ENCOUNTER                 │
                    │         "My friend fell and is bleeding heavily"       │
                    └───────────────────────────┬────────────────────────────┘
                                                │
                                ┌───────────────┴───────────────┐
                                │                               │
                       [🎙️ Voice Input (STT)]           [⌨️ Text Input]
                                │                               │
                                └───────────────┬───────────────┘
                                                │
                                                ▼
                    ┌────────────────────────────────────────────────────────┐
                    │             CLINICAL KNOWLEDGE EXTRACTOR               │
                    │   • Jaccard Token Overlap & Fuzzy Intent Scoring       │
                    │   • Typo Tolerance ('sanke' -> 'snake', 'bleding')     │
                    │   • Dual-Dataset Indexing (44+ First-Aid Conditions)   │
                    └───────────────────────────┬────────────────────────────┘
                                                │
                                                ▼
                    ┌────────────────────────────────────────────────────────┐
                    │             STRUCTURED RAG GROUNDING                   │
                    │   • Verified Plain-Language First Aid Steps            │
                    │   • Red-Flag Warning Signs (Shock, Numbness, Cold)     │
                    │   • Multi-Turn Triage Questions                        │
                    │   • Satellite SOS & Evacuation Guidelines              │
                    └───────────────────────────┬────────────────────────────┘
                                                │
                                                ▼
                                    ┌───────────────────────┐
                                    │ Is Local AI Model     │
                                    │ (.gguf/.bin) Enabled? │
                                    └───────────┬───────────┘
                                                │
                                ┌───────────────┴───────────────┐
                                │ YES                           │ NO (or Low RAM)
                                ▼                               ▼
                  ┌───────────────────────────┐   ┌───────────────────────────┐
                  │   Local Neural LLM        │   │   Deterministic Clinical  │
                  │   (.gguf / Gemma-2B INT4) │   │   Wilderness Reasoner     │
                  │   • MediaPipe GenAI Ops   │   │   • Verified Decision Tree│
                  │   • Strict Turn Templates │   │   • Millisecond Latency   │
                  │   • Battery-Aware Tokens  │   │   • 100% Reliable Steps   │
                  └─────────────┬─────────────┘   └─────────────┬─────────────┘
                                │                               │
                                └───────────────┬───────────────┘
                                                │
                                                ▼
                    ┌────────────────────────────────────────────────────────┐
                    │               CONVERSATIONAL RESPONSE                  │
                    │  1. Immediate Action Checklist (Numbered steps)        │
                    │  2. Red-Flag Warning Signs (Bullet points)             │
                    │  3. Adaptive Triage Question                           │
                    │  4. Evacuation & Satellite SOS Recommendation          │
                    └───────────────────────────┬────────────────────────────┘
                                                │
                                ┌───────────────┴───────────────┐
                                │                               │
                                ▼                               ▼
                    ┌───────────────────────┐       ┌───────────────────────┐
                    │ 🔊 Spoken Voice (TTS) │       │ 💾 Offline DB Logging │
                    │ Concise spoken steps  │       │ Encrypted Room SQLite │
                    │ (Top 2 points + Q)    │       │ Session report export │
                    └───────────────────────┘       └───────────────────────┘
```

---

## 📚 Dual-Dataset Knowledge Engine

MediTrail indexes two complementary clinical datasets out of the box:

1. **Wilderness Emergency Symptom Tree (`symptom_tree.json`)**:
   Deep decision trees for severe outdoor trauma:
   - *Heavy Bleeding & Arterial Trauma*
   - *Fractures, Sprains & Dislocations*
   - *Venomous Snake & Insect Bites*
   - *High-Altitude Sickness (HAPE / HACE) & Breathing Emergencies*
   - *Severe Hypothermia & Frostbite*
   - *Cardiac Emergencies & CPR*
   - *Head Trauma & Concussion*
   - *Heat Stroke & Exhaustion*

2. **First-Aid Intent Dataset (`first_aid_intents.json`)**:
   44 categorized conditions covering standard medical protocols:
   - *Cuts, Abrasions, Bee/Wasp Stings, Splinters, Ankle Sprains, Muscle Strains, Fever, Nasal Congestion, Cough, Sore Throat, Gastrointestinal Upset, Skin Allergies, Abdominal Pain, Bruises, Broken Toes, Choking (Heimlich), Open Wounds, Diarrhea & Dehydration, Frostbite, Heat Exhaustion, Heat Stroke, Insect Bites, Nosebleeds, Pulled Muscles, Sunburn, Eye Injuries, Chemical Burns, Poisoning, Broken Teeth, Seizures, Fainting, Tension Headaches, Animal Bites, Drowning Rescue, CPR Protocol, Fractures, and more.*

---

## 🎙️ Hands-Free Speech Pipeline (STT & TTS)

In emergencies, rescuers' hands are occupied applying pressure on wounds, splinting limbs, or performing CPR:

- **Offline Speech-To-Text (STT)**: Uses Android's speech framework (`createOnDeviceSpeechRecognizer` on Android 13+) with `PREFER_OFFLINE` support for hands-free dictation. Partial results are preserved automatically.
- **Concise Text-To-Speech (TTS)**: Built-in `formatForConciseSpeech` filter vocalizes only the top 2 immediate action points and the question (under 40 words), preventing long audio output.
- **Dynamic Speech Rate**: Adjustable from **0.5x to 1.5x** in Settings.

---

## 🧠 On-Device Neural Model Support (.GGUF / .BIN / .TASK)

Users can optionally run local quantized models for generative conversational responses:

- **Supported Formats**: `.gguf`, `.bin`, `.task`, `.onnx` (e.g. Gemma 2B INT4, Phi-2, TinyLlama).
- **In-App Importer**: Select model weights directly from device storage.
- **Extraction Test Sandbox**: Test query extraction and responses with pre-built test chips (`Cuts`, `Snake Bite`, `CPR`, `Sprains`) directly in Settings.

---

## 🚑 Emergency Protocols Covered

| Emergency | Common Scenarios | Plain-Language Protocol Focus |
| :--- | :--- | :--- |
| **🩸 Severe Bleeding** | Deep cuts, puncture wounds, spurting blood | Firm continuous pressure, wound packing, tight tourniquet 2-3" above cut, shock legs elevation |
| **🦴 Broken Bones & Sprains** | Ridge falls, snapped limbs, joint twists | Do not straighten crooked bone, rigid splint with trekking poles, check warm fingers/toes |
| **🐍 Bites & Stings** | Snake bites, bee/wasp stings, spider bites | Keep calm and still, remove rings/shoes, firm elastic wrap, draw swelling border with time |
| **🏔️ Altitude & Breathing** | High mountain pass, HAPE, asthma, choking | Descend immediately (>1,500 ft), Heimlich 5 back slaps & 5 belly thrusts, rescue inhaler puffs |
| **❄️ Cold & Hypothermia** | River soaking, freezing weather, frostbite | Take off wet clothes, burrito sleeping bag wrap, warm sweet drinks, never rub frozen skin |
| **🫀 Heart & Chest Pain** | Chest tightness, radiating arm pain | Sit with knees bent, chew 1 regular Aspirin (325mg), continuous CPR chest pushes if breathing stops |
| **🧠 Head Trauma** | Rock fall impact, concussion, blackouts | Keep head and neck completely still, roll body to side if vomiting, watch pupil reaction |

---

## 💻 Technology Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Kotlin 1.9.22 | 100% null-safe Kotlin |
| **UI Framework** | Jetpack Compose (M3) | HealthTech visual design, light surfaces, dynamic typography |
| **AI Runtime** | Google MediaPipe GenAI | Local execution of quantized LLMs (`.gguf`, `.bin`, `.task`) |
| **Speech Engine** | Android STT & TTS | Offline voice input and concise speech synthesis |
| **Architecture** | Clean Architecture + MVVM | Strict separation of Data, Domain, and Presentation |
| **Dependency Injection** | Dagger Hilt 2.50 | Application-wide DI |
| **Local Database** | Room DB 2.6.1 | Offline encrypted emergency session logging |
| **Preferences** | Jetpack DataStore | User settings (TTS speech rate, emergency contacts, display) |
| **Asynchronous Engine** | Coroutines & StateFlow | Reactive non-blocking token streaming and timers |

---

## 🛠️ Getting Started & Installation

### Prerequisites
- **Android Studio** (Hedgehog / Ladybug or newer)
- **JDK 17**
- **Android Device or Emulator** running Android 8.0+ (API 26+)

### 1. Clone the Repository
```bash
git clone https://github.com/RushiSatav/TrailMedic.git
cd TrailMedic
```

### 2. Run Unit Tests
```powershell
.\gradlew.bat testDebugUnitTest
```

### 3. Build the Debug APK
```powershell
.\gradlew.bat assembleDebug
```
The compiled APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 4. Running Without Local Model (Instant Setup)
You do not need to download the 1.5 GB neural weights to use MediTrail:
1. Open the app on your phone.
2. On the download screen, tap **"Skip for now (Use Offline Symptom Engine)"**.
3. The app opens immediately with full offline first-aid coverage.

---

## 📁 Project Structure

```
com.trailmedic/
├── data/
│   ├── local/            # Room Database (SessionEntity, SessionDao)
│   ├── llm/              # MediaPipe LLM Engine & ConversationManager
│   └── repository/       # Repository implementations (Chat, SymptomTree)
├── domain/
│   ├── ai/               # WildernessClinicalAIReasoner & ClinicalKnowledgeExtractor
│   ├── model/            # Domain models (Emergency, Message, Session)
│   └── usecase/          # Use cases (SaveSession, RunEmergencyInterview)
├── ui/
│   ├── components/       # SOSButton, OfflineBadge, ChatBubble, TypingIndicator
│   ├── emergency/        # ChatScreen, ResultScreen, ChatViewModel
│   ├── history/          # HistoryScreen, SessionDetailScreen, HistoryViewModel
│   ├── home/             # HomeScreen, HomeViewModel
│   ├── onboarding/       # OnboardingScreen, ModelDownloadScreen
│   ├── settings/         # SettingsScreen, SettingsViewModel
│   ├── splash/           # SplashScreen, SplashViewModel
│   └── theme/            # Color tokens, Typography, MediTrailTheme
└── utils/                # BatteryAwareManager, TTSManager, VoiceInputManager
```

---

## ⚖️ Medical Disclaimer

> **IMPORTANT**: *MediTrail provides emergency first aid guidance for situations where professional medical facilities and communication networks are unavailable. It is designed to assist in stabilizing casualties during remote expeditions. It is NOT a substitute for certified medical professionals, hospital triage, or formal wilderness emergency training (WFR/WFA). Always evacuate injured persons to professional medical care as rapidly as possible.*

---

<div align="center">

**MediTrail — Emergency First Aid. Anywhere. No Signal Needed.**  
Developed by **Rushi Satav**

</div>
