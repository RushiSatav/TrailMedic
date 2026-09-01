# 🏔️ TrailMedic — On-Device AI Wilderness Emergency First Aid Assistant

<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![MediaPipe LLM](https://img.shields.io/badge/Local%20AI-Gemma--2B--IT%20INT4-FF6F00.svg?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev/edge/mediapipe)
[![Network](https://img.shields.io/badge/Offline%20First-100%25%20Zero%20Signal%20Required-success.svg?style=for-the-badge)](https://developer.android.com)

**TrailMedic** is an intelligent, **100% offline, on-device AI emergency first aid assistant** engineered for mountaineers, backpackers, search-and-rescue teams, and remote wilderness guides operating in extreme environments with **zero cellular signal, internet access, or cloud connectivity**.

</div>

---

## 📑 Table of Contents

1. [Executive Summary & Problem Statement](#-executive-summary--problem-statement)
2. [Why TrailMedic Wins (Hackathon Highlights)](#-why-trailmedic-wins-hackathon-highlights)
3. [Dual-Engine Intelligence Pipeline](#-dual-engine-intelligence-pipeline)
4. [System Architecture & Data Flow](#-system-architecture--data-flow)
5. [Wilderness Medical Protocols Covered](#-wilderness-medical-protocols-covered)
6. [Core Features & Innovation](#-core-features--innovation)
7. [Technology Stack & Dependency Matrix](#-technology-stack--dependency-matrix)
8. [Setup, Build & Execution Guide](#-setup-build--execution-guide)
9. [Future Roadmap & Visionary Scope](#-future-roadmap--visionary-scope)
10. [Medical Disclaimer & License](#-medical-disclaimer)

---

## 🧭 Executive Summary & Problem Statement

### ❌ The Wilderness Emergency Dilemma
- **Zero Cellular Signal (0 Bars)**: Backcountry trails, deep valleys, and high-altitude peaks have zero cellular coverage. Cloud-dependent assistants (ChatGPT, Gemini API, Claude) fail immediately.
- **Panic & Cognitive Overload**: In acute emergencies (arterial bleeding, open fractures, venomous snakebites, hypothermia), untrained responders panic and forget critical triage steps.
- **Bulky Medical Handbooks**: Paper field manuals are hard to read in the dark, heavy to carry, and impossible to search quickly under high stress.
- **Battery Scarcity**: Remote expeditions last days on portable power banks. Unoptimized mobile applications drain crucial survival battery.

### ✅ The TrailMedic Solution
TrailMedic transforms a standard Android device into a **self-contained emergency medical command station**:
- **100% On-Device Neural Model**: Runs **Gemma-2B-IT (INT4 Quantized)** locally via **Google MediaPipe Tasks GenAI** with zero cloud telemetry.
- **Dual-Engine Failsafe**: Instant fallback to a curated 8-category clinical knowledge tree if the device is on ultra-low memory.
- **Structured Multi-Turn Clinical Triage**: Performs iterative patient assessment before providing numbered first aid steps, red-flag warnings, and satellite evacuation notes.
- **Hands-Free Operation**: Offline Text-to-Speech (TTS) guidance and voice dictation allow rescuers to receive step-by-step instructions without removing their hands from a wound.
- **Battery-Aware Intelligence**: Dynamically adapts token limits and model execution based on battery thresholds (<15% and <5%).

---

## 🏆 Why TrailMedic Wins (Hackathon Highlights)

| Feature / Dimension | Traditional First Aid Apps | Cloud AI Solutions | **TrailMedic (Our System)** |
| :--- | :--- | :--- | :--- |
| **Connectivity** | Static text / PDF viewers | Requires 4G/5G / WiFi | **100% On-Device & Zero Cloud Dependencies** |
| **Intelligence Engine** | Rigid keyword search | High intelligence (Cloud only) | **Quantized Gemma-2B-IT Neural Model** |
| **Fail-Safe Reliability** | No dynamic reasoning | Completely fails offline | **Dual-Engine (Neural LLM + Clinical Tree)** |
| **Hands-Free Assistance** | Touch screen only | Cloud speech services | **On-Device Android TTS + Offline Dictation** |
| **Power Conservation** | Fixed power profile | Massive cloud drain | **Dynamic Battery-Aware Token Throttling** |
| **Rescue Handover** | None | Requires cloud sync | **Room Database + Instant .TXT Evac Report** |

---

## 🧠 Dual-Engine Intelligence Pipeline

TrailMedic utilizes a resilient **Dual-Engine Architecture** that guarantees medical advice is always delivered instantly:

```
                            ┌─────────────────────────────────────────┐
                            │        User Emergency Encounter         │
                            │  "He fell off a ledge, bone exposed"    │
                            └────────────────────┬────────────────────┘
                                                 │
                                                 ▼
                            ┌─────────────────────────────────────────┐
                            │      Dynamic Category Classifier        │
                            │  (Regex / Semantic Trauma Tokenizer)    │
                            └────────────────────┬────────────────────┘
                                                 │
                                                 ▼
                                     ┌───────────────────────┐
                                     │ Is Gemma-2B Model     │
                                     │ Available & BatteryOK?│
                                     └───────────┬───────────┘
                                                 │
                                 ┌───────────────┴───────────────┐
                                 │ YES                           │ NO (or Low RAM)
                                 ▼                               ▼
                   ┌───────────────────────────┐   ┌───────────────────────────┐
                   │  MediaPipe Tasks GenAI    │   │  Deterministic Clinical   │
                   │  Gemma-2B-IT (INT4) Engine│   │  Wilderness Reasoner      │
                   │  • Strict Chat Template   │   │  • 8 Trauma Branches      │
                   │  • 512 Sequence Buffer    │   │  • Clinical Checklist     │
                   │  • Streaming Token Output │   │  • Millisecond Latency    │
                   └─────────────┬─────────────┘   └─────────────┬─────────────┘
                                 │                               │
                                 └───────────────┬───────────────┘
                                                 │
                                                 ▼
                            ┌─────────────────────────────────────────┐
                            │       Unified Clinical Output Flow      │
                            │  1. Clarifying Diagnostic Question      │
                            │  2. Actionable Numbered Procedures      │
                            │  3. Critical Red-Flag Warning Signs     │
                            │  4. Satellite SOS Evac Handover Record  │
                            └─────────────────────────────────────────┘
```

---

## 🏛️ System Architecture & Data Flow

TrailMedic is architected with **Clean Architecture** and **MVI/MVVM design patterns** powered by Kotlin Coroutines and StateFlow:

```
┌────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                            │
│   • Jetpack Compose Material 3 · Edge-to-Edge Dark Mode UI             │
│   • ViewModels (HomeViewModel, ChatViewModel, SettingsViewModel)       │
│   • Unidirectional Data Flow (StateFlow / SharedFlow)                  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                            DOMAIN LAYER                                │
│   • UseCases: RunEmergencyInterviewUseCase, SaveSessionUseCase,        │
│               ExportEvacuationReportUseCase, GetSessionHistoryUseCase  │
│   • Clinical AI Reasoner: WildernessClinicalAIReasoner                 │
│   • Domain Models: Message, Session, EmergencyCategory, TraumaData     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                             DATA LAYER                                 │
│   • LLM Engine: MediaPipe GenAI (LlmInference, ConversationManager)    │
│   • Database: Room DB (SessionEntity, MessageEntity, TypeConverters)   │
│   • Preferences: Jetpack DataStore (SettingsManager)                   │
│   • Device Integrations: BatteryAwareManager, Offline TTSManager       │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 🚑 Wilderness Medical Protocols Covered

TrailMedic covers 8 comprehensive wilderness medical categories based on Wilderness First Responder (WFR) and Wilderness First Aid (WFA) standards:

1. **🐍 Snakebites & Envenomation**: Pressure immobilization, limb positioning below heart, contraindications against tourniquets/cutting/suction.
2. **🩸 Severe Hemorrhage & Wounds**: Direct pressure, wound packing, windlass/commercial tourniquet protocols, shock prevention.
3. **🦴 Fractures & Dislocation**: Musculoskeletal splinting, open fracture dressing, distal pulse & neurological checks.
4. **🏔️ Altitude Sickness (HAPE / HACE)**: Acute Mountain Sickness (AMS), ataxia checks, immediate descent protocols, hyperbaric guidance.
5. **❄️ Hypothermia & Frostbite**: Active vs. passive rewarming, vapor barrier wrapping, cold diuresis management, frostbite thawing precautions.
6. **🫀 Acute Cardiac Events**: Wilderness CPR, aspirin triage, rest position, emergency evacuation signaling.
7. **🧠 Traumatic Head Injury**: Concussion assessment, Glasgow Coma Scale indicators, cervical spine immobilization, intracranial pressure signs.
8. **🔥 Burns, Heat Stroke & Shock**: Rule of nines assessment, hydration strategies, evaporative cooling, sterile non-adherent coverage.

---

## 💻 Technology Stack & Dependency Matrix

| Category | Technology | Purpose |
| :--- | :--- | :--- |
| **Language** | Kotlin 1.9.22 | 100% Modern Kotlin codebase |
| **UI Framework** | Jetpack Compose (M3) | Declarative reactive UI with dark theme support |
| **AI Inference** | Google MediaPipe Tasks GenAI (`0.10.20`) | Native on-device execution of quantized LLMs |
| **Model** | Gemma-2B-IT (INT4 Quantized) | 2 Billion parameter neural assistant running on-device |
| **Architecture** | Clean Architecture + MVVM | Scalable domain isolation and testability |
| **Dependency Injection** | Dagger Hilt 2.50 | Application-wide singleton lifecycle management |
| **Persistence** | AndroidX Room DB + DataStore | Offline session logs and user settings |
| **Asynchronous** | Kotlin Coroutines & StateFlow | Reactive non-blocking token streaming |
| **Speech** | Android TTS Engine | Offline hands-free audio playback |

---

## 🛠️ Setup, Build & Execution Guide

### Prerequisites
- **Android Studio** (Hedgehog, Ladybug, Iguana, or newer)
- **JDK 17 or JDK 21**
- **Android Device / Emulator**: Running Android 8.0+ (API 26+) with ARM64 support.

### 1. Clone the Repository
```bash
git clone https://github.com/RushiSatav/TrailMedic.git
cd TrailMedic
```

### 2. Build the Debug APK
```powershell
.\gradlew.bat assembleDebug
```

### 3. Install on Connected Device
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 4. Sideload Gemma 2B Model (Optional for LLM Mode)
```bash
# Push the quantized CPU model to the app internal storage
adb push gemma-2b-it-cpu-int4.bin /data/user/0/com.trailmedic/files/models/gemma-2b-it.bin
```

---

## 🚀 Future Roadmap & Visionary Scope

The future development plan for TrailMedic focuses on expanding offline multimodal capabilities for extreme field operations:

```
                  ┌──────────────────────────────────────────────────┐
                  │             TrailMedic Future Scope              │
                  └────────────────────────┬─────────────────────────┘
                                           │
         ┌──────────────────┬──────────────┴─────┬──────────────────┐
         ▼                  ▼                    ▼                  ▼
┌─────────────────┐┌─────────────────┐ ┌──────────────────┐┌──────────────────┐
│ 🎙️ On-Device    ││ 📸 Computer     │ │ 📡 BLE Mesh &    ││ 🛸 Drone &      │
│ Whisper STT     ││ Vision Wound    │ │ LoRa Off-Grid    ││ Satellite SOS    │
│ Noise Filtering ││ Classification  │ │ Peer-to-Peer     ││ Dispatch Protocol│
└─────────────────┘└─────────────────┘ └──────────────────┘└──────────────────┘
```

### 1. 🎙️ On-Device Speech-to-Text (STT) via Whisper.tflite
- Integrate a lightweight, on-device **OpenAI Whisper Tiny/Base INT8 model** or **Vosk Offline STT**.
- Incorporate aggressive DSP noise-cancellation to filter out high alpine wind noise, rushing rivers, and heavy rain so responders can dictate completely hands-free while performing CPR or wound management.

### 2. 📸 Computer Vision Wound & Fracture Classification
- Deploy an on-device **MobileNet / MediaPipe Vision classifier**.
- Responders can photograph an injury in the dark or field; the CV engine automatically assesses **burn depth (1st, 2nd, 3rd degree)**, **wound infection signs**, **pupil dilation / concussion**, and **fracture angulation**.

### 3. 📡 Off-Grid BLE Mesh & LoRa Peer-to-Peer Relay
- Implement an ad-hoc **Bluetooth Low Energy (BLE) Mesh** and **LoRa (Long Range) 915/868 MHz** relay layer.
- Allows TrailMedic instances across different hiking groups within a 5–15 km radius to automatically relay emergency SOS telemetry and GPS coordinates from phone to phone until reaching a ranger outpost without cellular networks.

### 4. 🛰️ Direct Satellite SOS & Drone Beacon Handover
- Integrate formatted exports for **Garmin inReach**, **ZOLEO**, and **Android 15 / Apple Satellite Emergency SOS APIs**.
- Direct transmission of compact medical triage reports to search-and-rescue UAVs and medevac helicopters.

### 5. 🌍 Multi-Language Offline Speech Packs
- Provide quantized bilingual models and localized voice packs for **Spanish, French, German, Hindi, Japanese, and Mandarin** to assist international mountaineering expeditions.

---

## ⚖️ Medical Disclaimer

> **IMPORTANT**: *TrailMedic provides wilderness first aid guidance for emergency scenarios when professional medical facilities and communication networks are unavailable. It is designed to assist trained or untrained responders in stabilizing casualties during remote expeditions. It is NOT a replacement for certified medical professionals or formal wilderness emergency training (WFR/WFA). Always evacuate injured persons to professional medical care as rapidly as possible.*

---

<div align="center">

**TrailMedic — Saving Lives Where Connectivity Ends.**  
Developed by **Rushi Satav** | Built with Kotlin, Jetpack Compose, and Google Gemma AI.

</div>
