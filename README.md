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
6. [Technology Stack & Dependency Matrix](#-technology-stack--dependency-matrix)
7. [Step-by-Step Beginner Setup & Run Guide](#-step-by-step-beginner-setup--run-guide)
8. [How to Download & Import the Gemma 2B Model](#-how-to-download--import-the-gemma-2b-model)
9. [Emulator Best Practices & Troubleshooting FAQ](#-emulator-best-practices--troubleshooting-faq)
10. [Future Roadmap & Visionary Scope](#-future-roadmap--visionary-scope)
11. [Medical Disclaimer](#-medical-disclaimer)

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

1. **🐍 Snakebites & Envenomation**: Pressure immobilization bandage technique, keeping bitten limb below heart level, contraindications against tourniquets/cutting/suction.
2. **🩸 Severe Hemorrhage & Wounds**: Direct pressure, wound packing, windlass/commercial tourniquet protocols, shock prevention and blood loss monitoring.
3. **🦴 Fractures & Dislocation**: Musculoskeletal splinting, open fracture sterile dressing, distal pulse, motor, and sensory (PMS) neurological checks.
4. **🏔️ Altitude Sickness (HAPE / HACE)**: Acute Mountain Sickness (AMS), ataxia walking checks, immediate descent protocols, hyperbaric bag guidance.
5. **❄️ Hypothermia & Frostbite**: Active vs. passive rewarming, vapor barrier "hypothermia burrito" wrap, frostbite thawing precautions.
6. **🫀 Acute Cardiac Events**: Wilderness CPR guidelines, aspirin triage, comfortable positioning, emergency satellite signaling.
7. **🧠 Traumatic Head Injury**: Concussion assessment, Glasgow Coma Scale indicators, cervical spine stabilization, intracranial pressure red flags.
8. **🔥 Burns, Heat Stroke & Shock**: Rule of nines assessment, evaporative cooling techniques, sterile non-adherent coverage.

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

## 🛠️ Step-by-Step Beginner Setup & Run Guide

Follow these simple steps to get TrailMedic running on your computer or Android phone in minutes:

### 📋 Prerequisites
1. **Android Studio**: Download and install [Android Studio](https://developer.android.com/studio) (Hedgehog, Ladybug, Iguana, or newer).
2. **JDK 17 or JDK 21**: Included automatically with modern Android Studio installations.
3. **Target Device**:
   - **Option A (Physical Android Phone - Recommended for best AI speed)**: Any phone running Android 8.0+ (API 26+) with 4GB+ RAM.
   - **Option B (Android Virtual Device / Emulator)**: See [Emulator Best Practices](#-emulator-best-practices--troubleshooting-faq) below.

---

### 1️⃣ Clone the Repository
Open your terminal (PowerShell, Command Prompt, or Terminal) and run:
```bash
git clone https://github.com/RushiSatav/TrailMedic.git
cd TrailMedic
```

---

### 2️⃣ Open the Project in Android Studio
1. Launch **Android Studio**.
2. Click **Open** and select the cloned `TrailMedic` folder.
3. Wait 1–2 minutes for Gradle to download dependencies and index the project.

---

### 3️⃣ Run the App on Your Phone or Emulator
1. Select your connected device or emulator from the device dropdown in Android Studio.
2. Click the green **Run (▶)** button (or press `Shift + F10`).
3. Alternatively, build and install from the command line:
   ```powershell
   # Build the debug APK
   .\gradlew.bat assembleDebug

   # Install onto device
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📥 How to Download & Import the Gemma 2B Model

TrailMedic runs **100% out of the box** using its built-in clinical emergency tree. To unlock **neural generative conversational AI**, import the quantized Gemma 2B weights using either method below:

### 1. Download Model Weights (`gemma-2b-it-cpu-int4.bin`)
- Download the CPU quantized Gemma 2B model (~1.28 GB) from Kaggle:
  👉 **[Download Gemma 2B IT CPU INT4 on Kaggle](https://www.kaggle.com/models/google/gemma/tfLite)** (Select `gemma-2b-it-cpu-int4.bin`).
- *(Note: Ensure you download the `cpu-int4.bin` variant for universal compatibility across all Android CPUs and emulators).*

---

### 2. Import the Model into TrailMedic

#### 🟢 Method A: In-App Import (Easiest — No Terminal Required)
1. Transfer or download the `gemma-2b-it-cpu-int4.bin` file to your phone's **Downloads** folder (on an emulator, you can simply **drag and drop** the `.bin` file onto the emulator screen).
2. Open **TrailMedic** on your device.
3. Tap the ⚙️ **Settings** tab in the bottom bar.
4. Scroll down to the **"Offline AI Model"** section.
5. Tap **"Import Model from Device Storage"**.
6. Select your downloaded `gemma-2b-it-cpu-int4.bin` file from the file picker.
7. TrailMedic will validate the model, move it to secure sandboxed storage, and display **"Model Status: READY (1,284 MB)"** with a green badge!

---

#### ⚡ Method B: ADB Push (Fast Command Line Method)
If your phone or emulator is connected via USB / ADB:

```powershell
# Windows PowerShell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" push "C:\path\to\gemma-2b-it-cpu-int4.bin" /data/user/0/com.trailmedic/files/models/gemma-2b-it.bin
```

```bash
# macOS / Linux
adb push path/to/gemma-2b-it-cpu-int4.bin /data/user/0/com.trailmedic/files/models/gemma-2b-it.bin
```

Once pushed, reopen TrailMedic or tap **"Reload Model"** in Settings.

---

### 3. Verify Neural Model
1. In TrailMedic, open **Settings** ➔ tap **"Test Model"**.
2. You will see the local neural model initialize and generate its diagnostic reply directly on-device!

---

## ⚙️ Emulator Best Practices & Troubleshooting FAQ

### 📱 Best Emulator Configuration for Local AI
When creating an Android Virtual Device (AVD) in Android Studio:
- **System Image**: Choose **Android 14 ("UpsideDownCake" / API 34) `x86_64` Google APIs** (Standard 4KB kernel).
  - ⚠️ *Do NOT use preview "16k page size" images, as native C++ libraries require standard 4KB page alignment.*
- **CPU Cores**:
  - If your PC has **4 CPU cores**: Set Multi-Core CPU to **`2` cores** in AVD Advanced Settings (this prevents Windows host contention).
  - If your PC has **6+ CPU cores**: Set Multi-Core CPU to **`4` cores**.
- **RAM**: Set to **`4096 MB`** or **`6144 MB`**.
- **Graphics**: Set to **`Hardware - GLES 2.0`** (offloads UI graphics to your PC's GPU, reserving 100% of CPU for AI tensor operations).

---

### ❓ Frequently Asked Questions

#### Q1: What happens if I don't download or import the Gemma model file?
> **Answer**: TrailMedic is built with a **Dual-Engine Architecture**. If no model is downloaded, the app seamlessly runs on its **embedded deterministic clinical reasoner** covering all 8 trauma categories with zero crashes and millisecond response times.

#### Q2: Why does the first prompt take a few seconds on an emulator CPU?
> **Answer**: On an emulator, ARM-64 neural instructions run via dynamic software translation (`libndk_translation`). On a real physical Android phone (e.g. Snapdragon or MediaTek), execution is direct and hardware accelerated.

#### Q3: Does TrailMedic leak any medical data or telemetry to the cloud?
> **Answer**: **Zero.** TrailMedic does not contain any cloud endpoints, tracking SDKs, or network permissions in its inference pipeline. All conversation logs are stored strictly inside your phone's encrypted Room SQLite database.

---

## 🚀 Future Roadmap & Visionary Scope

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
- Integrate a lightweight **OpenAI Whisper Tiny/Base INT8 model** or **Vosk Offline STT**.
- Incorporate aggressive DSP noise cancellation to filter out high alpine wind noise, rushing rivers, and heavy rain so responders can dictate completely hands-free while performing CPR or wound management.

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
