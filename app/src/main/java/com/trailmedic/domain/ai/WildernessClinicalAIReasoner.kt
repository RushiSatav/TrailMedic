package com.trailmedic.domain.ai

import com.trailmedic.domain.model.ClinicalExtractionResult
import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.EmergencyCategory
import com.trailmedic.domain.model.Message
import com.trailmedic.domain.model.SymptomEmergencyData
import com.trailmedic.domain.repository.SymptomTreeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WildernessClinicalAIReasoner @Inject constructor(
    private val symptomTreeRepository: SymptomTreeRepository,
    private val clinicalKnowledgeExtractor: ClinicalKnowledgeExtractor
) {

    /**
     * Dynamically detects the medical emergency topic from user conversation,
     * leveraging clinical datasets, keyword recognition, and typo tolerance.
     */
    fun detectEmergencyCategory(messages: List<Message>, defaultCategory: EmergencyCategory): EmergencyCategory {
        val userTexts = messages.filter { it.isUser }.joinToString(" ") { it.content.lowercase() }
        if (userTexts.isBlank()) return defaultCategory

        return clinicalKnowledgeExtractor.mapToCategory(userTexts, defaultCategory)
    }

    /**
     * Extracts precise structured knowledge for the current conversation context.
     */
    fun extractRelevantKnowledge(messages: List<Message>): ClinicalExtractionResult? {
        val userTexts = messages.filter { it.isUser }.joinToString(" ") { it.content }
        return clinicalKnowledgeExtractor.extractKnowledgeForPrompt(userTexts)
    }

    /**
     * Generates a contextually intelligent clinical response in simple, easy-to-understand language.
     */
    fun generateDynamicResponse(
        category: EmergencyCategory,
        messages: List<Message>,
        phase: ConversationPhase,
        questionIndex: Int = 0
    ): String {
        val activeCategory = detectEmergencyCategory(messages, category)
        val userMessages = messages.filter { it.isUser }
        val userInputs = userMessages.map { it.content }
        val latestInput = userInputs.lastOrNull()?.trim() ?: ""

        val extract = extractRelevantKnowledge(messages)
        val data = if (extract != null) {
            SymptomEmergencyData(
                id = extract.conditionTag.lowercase().replace(" ", "_"),
                name = extract.conditionName,
                triggerKeywords = listOf(extract.conditionTag),
                questions = listOf(extract.triageQuestion),
                firstAidSteps = extract.firstAidSteps,
                warningSigns = extract.warningSigns,
                evacuationNote = extract.evacuationNote
            )
        } else {
            symptomTreeRepository.getCategoryFallback(activeCategory)
        }

        val turnIndex = (userInputs.size - 1).coerceAtLeast(0)

        return when (phase) {
            ConversationPhase.INTERVIEWING -> generateInterviewQuestion(
                category = activeCategory,
                data = data,
                extract = extract,
                userInputs = userInputs,
                latestInput = latestInput,
                turnIndex = turnIndex
            )
            ConversationPhase.DIAGNOSING -> generateDiagnosisAndTreatment(activeCategory, data, extract, userInputs)
        }
    }

    private fun generateInterviewQuestion(
        category: EmergencyCategory,
        data: SymptomEmergencyData,
        extract: ClinicalExtractionResult?,
        userInputs: List<String>,
        latestInput: String,
        turnIndex: Int
    ): String {
        // 0. Natural greetings
        if (isGreeting(latestInput) && userInputs.size == 1) {
            return "Hello! I am MediTrail, your offline wilderness first aid helper. Please tell me what happened — is someone hurt, bleeding, bitten, having trouble breathing, or feeling sick?"
        }

        val allUserText = userInputs.joinToString(" ").lowercase()
        val latestLower = latestInput.lowercase()

        return when (category) {
            EmergencyCategory.BLEEDING -> handleBleedingTurn(turnIndex, latestLower)
            EmergencyCategory.FRACTURE -> handleFractureTurn(turnIndex, latestLower)
            EmergencyCategory.BITE -> handleBiteTurn(turnIndex, latestLower, allUserText)
            EmergencyCategory.BREATHING -> handleBreathingTurn(turnIndex, latestLower, allUserText)
            EmergencyCategory.HYPOTHERMIA -> handleHypothermiaTurn(turnIndex, latestLower)
            EmergencyCategory.CARDIAC -> handleCardiacTurn(turnIndex)
            EmergencyCategory.HEAD -> handleHeadTurn(turnIndex, latestLower)
            EmergencyCategory.GENERAL -> handleGeneralOrIntentTurn(turnIndex, data, extract)
        }
    }

    // ==========================================
    // 1. BLEEDING & WOUND TRIAGE (Plain, Easy Words)
    // ==========================================
    private fun handleBleedingTurn(
        turnIndex: Int,
        latest: String
    ): String {
        return when (turnIndex) {
            0 -> """
IMMEDIATE FIRST AID ACTIONS:
1. Press down firmly right on the wound using a clean cloth, bandage, or your hands. Do not let go.
2. Keep pressing hard for 5 to 10 minutes without lifting the cloth to check.
3. If the arm or leg is not broken, raise it up higher than their chest to slow down the blood flow.
4. Have the person lie down or sit comfortably so they do not feel dizzy or faint.

TRIAGE ASSESSMENT:
Is the blood shooting/spurting out fast like a water pump (arterial bleeding), or is it flowing steadily / leaking slowly from the cut?
            """.trimIndent()

            1 -> {
                val isSpurting = isAffirmative(latest) || containsAny(latest, "spurt", "artery", "heavy", "gush", "lot of blood", "pulsing", "fast", "high pressure")
                if (isSpurting) {
                    """
⚠️ CRITICAL TOURNIQUET & HEAVY BLEEDING STEPS:
1. TIE A TIGHT TOURNIQUET: If heavy blood is spurting from an arm or leg, wrap a tight belt, cloth strap, or tourniquet 2 to 3 inches above the cut (never directly over an elbow or knee joint).
2. Twist and tighten it with a stick until the bleeding stops completely, and write the exact time on their forehead (e.g. 'TK 14:30').
3. Keep pressing firmly on the wound with a clean cloth.
4. Lay the person flat on their back, raise their feet slightly, and wrap them in a warm jacket or emergency blanket.

TRIAGE ASSESSMENT:
Where on the body is the injury (arm, leg, head, chest), and is there anything stuck inside the cut (like a rock, wood, or glass)?
                    """.trimIndent()
                } else {
                    """
STEADY BLEEDING FIRST AID:
1. Keep pressing hard on the cut with a clean cloth for at least 10 full minutes without letting go.
2. Keep the hurt arm or leg raised up higher than their chest to reduce bleeding.
3. Once the bleeding slows down, wrap a clean bandage firmly around the cloth to hold it tightly in place.
4. Do NOT wash, wipe, or rub the cut while it is trying to clot and seal.

TRIAGE ASSESSMENT:
Is there any object (rock, wood, glass, metal) stuck inside the cut, or did they break a bone from the fall?
                    """.trimIndent()
                }
            }

            2 -> {
                val hasObject = containsAny(latest, "object", "rock", "stick", "wood", "glass", "metal", "stone", "stuck", "inside", "impaled") ||
                        (isAffirmative(latest) && !isNegative(latest))
                if (hasObject) {
                    """
OBJECT STUCK IN WOUND STEPS:
1. DO NOT pull out the stuck rock, wood, or glass — pulling it out will cause massive dangerous bleeding.
2. Place rolled-up cloths on BOTH sides of the object to support it and keep it from moving.
3. Wrap a bandage firmly around the cloths to hold them in place, leaving the object sticking out safely.
4. Keep the injured arm or leg completely still.

TRIAGE ASSESSMENT:
Is the person awake and talking clearly, and are they feeling dizzy, cold, pale, or shivering?
                    """.trimIndent()
                } else {
                    """
BANDAGE & REST GUIDANCE:
1. Keep the clean bandage tied firmly over the cut.
2. If their arm or leg hurts from the fall, support it with rolled clothes or trekking poles so it stays still.
3. Check their fingers or toes below the bandage: make sure they stay warm and are not turning blue, cold, or numb.
4. Let the person rest quietly and give small sips of clean water if they are awake.

TRIAGE ASSESSMENT:
Is the person awake, breathing normally, and are they feeling dizzy, pale, or confused?
                    """.trimIndent()
                }
            }

            else -> {
                val hasShock = isAffirmative(latest) || containsAny(latest, "shock", "pale", "cold", "dizzy", "faint", "shiver", "confus", "sweat", "fast pulse")
                if (hasShock) {
                    """
SHOCK RELIEF STEPS:
1. Lay the person flat on their back right away.
2. Raise their legs up about 12 inches (30 cm) to help blood flow to their brain and heart.
3. Wrap them completely in warm jackets, sleeping bags, or an emergency foil blanket to keep them warm.
4. Loosen any tight clothes around their neck, chest, and waist.
5. Do NOT give food or big gulps of water if they feel sick or sleepy.

TRIAGE ASSESSMENT:
First aid steps are ready. Are you ready to see the full simple summary and rescue signaling steps?
                    """.trimIndent()
                } else {
                    """
KEEPING THE PERSON STABLE:
1. The person is steady. Check their pulse and breathing every 10 minutes to be safe.
2. Keep the clean bandage wrapped on the cut and let them rest comfortably.
3. Protect them from cold wind and wet ground using sleeping pads and warm jackets.

TRIAGE ASSESSMENT:
First aid steps are ready. Are you ready to see the full simple summary and rescue signaling steps?
                    """.trimIndent()
                }
            }
        }
    }

    // ==========================================
    // 2. FRACTURE & FALL TRIAGE (Plain, Easy Words)
    // ==========================================
    private fun handleFractureTurn(
        turnIndex: Int,
        latest: String
    ): String {
        return when (turnIndex) {
            0 -> """
IMMEDIATE FIRST AID ACTIONS:
1. DO NOT try to push the bone back or straighten a crooked arm or leg.
2. Keep the hurt area completely still in the exact position you found it.
3. If there is bleeding near the bone, press gently around the sides with a clean cloth (never push on the bone itself).
4. Keep the person completely still so the broken bone does not hurt nearby nerves or blood vessels.

TRIAGE ASSESSMENT:
Is the bone poking out through the skin (open break), or is the skin closed with bad swelling and pain?
            """.trimIndent()

            1 -> {
                val isOpen = isAffirmative(latest) || containsAny(latest, "open", "sticking out", "protruding", "through skin", "bone out", "bleeding bone", "torn skin")
                if (isOpen && !containsAny(latest, "closed", "skin is closed", "not open", "not sticking out")) {
                    """
OPEN BONE INJURY FIRST AID:
1. Cover the exposed bone loosely with a clean, damp cloth or sterile gauze.
2. Never push or force the bone back under the skin.
3. Make a stiff splint using trekking poles, straight branches, or a rolled sleeping pad along both sides of the limb.
4. Tie the splint firmly ABOVE and BELOW the break, but do not tie right over the open wound.

TRIAGE ASSESSMENT:
Can the person feel you touching their fingers or toes below the break, and do their hands/feet feel warm?
                    """.trimIndent()
                } else {
                    """
CLOSED BROKEN BONE & SPRAIN FIRST AID:
1. Make a stiff splint using trekking poles, rolled-up foam pad, or straight sticks.
2. Put soft cloths or bandanas in hollow spots (like ankles, knees, or wrists) so the splint is comfortable.
3. Tie the splint securely above and below the injured joint so it cannot bend or move.
4. Put a cold pack or snow wrapped in a cloth on the swollen area for 15 minutes to ease pain.

TRIAGE ASSESSMENT:
Can the person wiggle their fingers or toes below the break, and do they feel warm to touch?
                    """.trimIndent()
                }
            }

            2 -> {
                val impaired = isNegative(latest) || containsAny(latest, "can't feel", "numb", "cold", "blue", "pale", "no pulse", "pins and needles")
                if (impaired) {
                    """
⚠️ TIGHT BANDAGE WARNING:
1. Loosen any tight ties, tape, or bandages right now — blood flow is getting squeezed off.
2. Re-check if their fingers or toes feel warm again and if you can feel a pulse at their wrist or foot.
3. Gently support the limb in the most comfortable, natural position.
4. Urgent rescue is needed to protect the limb's blood circulation.

TRIAGE ASSESSMENT:
Can the person stand up or put any weight on it, and do they feel dizzy, pale, or cold?
                    """.trimIndent()
                } else {
                    """
BLOOD FLOW IS HEALTHY:
1. Good, blood flow and feeling are normal. Re-check their fingertips or toes every 15 minutes.
2. Make sure the splint is tied firmly so the limb cannot bend.
3. Give pain relief pills (like Ibuprofen or Paracetamol/Tylenol) if they are awake and not bleeding internally.
4. Do NOT let the person stand or walk on a hurt leg.

TRIAGE ASSESSMENT:
Did they hit their head, neck, back, or chest during the fall?
                    """.trimIndent()
                }
            }

            else -> """
REST & RESCUE PREPARATION:
1. Keep the person resting in a dry, sheltered spot, insulated from the cold ground with sleeping pads.
2. Wrap them in a warm emergency blanket or sleeping bag so they stay warm.
3. Prepare to help carry them or call emergency satellite SOS for helicopter rescue.

TRIAGE ASSESSMENT:
First aid steps are ready. Are you ready to see the full simple summary and rescue signaling steps?
            """.trimIndent()
        }
    }

    // ==========================================
    // 3. BITE & STING TRIAGE (Plain, Easy Words)
    // ==========================================
    private fun handleBiteTurn(
        turnIndex: Int,
        latest: String,
        allUserText: String
    ): String {
        return when (turnIndex) {
            0 -> """
IMMEDIATE FIRST AID ACTIONS:
1. Move safely away from the snake, spider, or insect so no one gets bitten again.
2. Keep the person completely calm, sitting, and still — moving around spreads venom faster in the body.
3. Take off rings, watches, bracelets, and tight boots right away before swelling starts.
4. Keep the bitten arm or leg resting level with their chest.
5. DO NOT cut the skin, DO NOT suck out the venom, and DO NOT use ice or tight tourniquets.

TRIAGE ASSESSMENT:
Did a snake bite them (two small fang puncture holes), or was it a bee, wasp, or spider sting? Describe any redness or swelling.
            """.trimIndent()

            1 -> {
                val isSnake = containsAny(latest, "snake", "viper", "cobra", "rattle", "fang", "puncture", "two holes", "bitten") ||
                        (isAffirmative(latest) && containsAny(allUserText, "snake", "fang", "bite"))
                if (isSnake) {
                    """
SNAKE BITE FIRST AID:
1. Wrap a firm elastic bandage over the entire bitten arm or leg (firm like an elastic wrap for a sprained ankle, but not cutting off blood pulse).
2. Splint the arm or leg with sticks or trekking poles so they cannot bend the joints.
3. Use a pen to draw a circle around the edge of the swelling and write the current time next to it to see if it spreads.
4. Keep the person completely still; carry them if you need to move them.

TRIAGE ASSESSMENT:
Is the person feeling dizzy, sick to their stomach (nausea), numb around their mouth, sweating heavily, or having trouble breathing?
                    """.trimIndent()
                } else {
                    """
BEE / INSECT STING FIRST AID:
1. If a bee stinger is left in the skin: scrape it off sideways with a card or fingernail (do not pinch the venom pouch).
2. Wash the sting spot gently with clean water and soap.
3. Put a cold, wet cloth on it for 15 minutes to reduce pain and swelling.
4. Give an allergy pill (like Cetirizine, Benadryl, or Allegra) to stop itching and swelling.

TRIAGE ASSESSMENT:
Are there any signs of a dangerous allergy: swelling of lips/tongue, tight throat, wheezing, or hives all over the body?
                    """.trimIndent()
                }
            }

            2 -> {
                val isSevere = isAffirmative(latest) || containsAny(latest, "throat", "breath", "dizzy", "nause", "vomit", "lip", "hive", "faint", "sweat")
                if (isSevere) {
                    """
⚠️ CRITICAL SEVERE ALLERGY / VENOM STEPS:
1. If an allergy auto-injector (EpiPen) is available: press it firmly into the outer thigh muscle and hold for 3 seconds.
2. Keep the person sitting upright if breathing is hard, or lay them flat with feet raised if feeling dizzy or faint.
3. Loosen tight shirts and collars around their neck.
4. Trigger Satellite Emergency SOS immediately — this requires urgent hospital care.

TRIAGE ASSESSMENT:
Is the person awake and breathing, and have you started emergency satellite rescue / 911 contact?
                    """.trimIndent()
                } else {
                    """
LOCAL STING CARE:
1. The reaction is only in that one spot right now. Keep resting and keep the limb still.
2. Put soothing cream (like Hydrocortisone or Calamine lotion) on the itchy skin.
3. Draw a circle around the swelling every 15 minutes to make sure it is not spreading fast.
4. Give Ibuprofen or Paracetamol for the stinging pain.

TRIAGE ASSESSMENT:
Are their breathing and alertness completely normal?
                    """.trimIndent()
                }
            }

            else -> """
MONITORING & GETTING READY:
1. Keep the person resting quietly and give small sips of water.
2. Check their breathing and pulse every 15 minutes to make sure they stay steady.
3. Keep them warm and off the cold ground with sleeping pads and blankets.

TRIAGE ASSESSMENT:
First aid steps are ready. Are you ready to see the full simple summary and rescue signaling steps?
            """.trimIndent()
        }
    }

    // ==========================================
    // 4. BREATHING & ALTITUDE TRIAGE (Plain, Easy Words)
    // ==========================================
    private fun handleBreathingTurn(
        turnIndex: Int,
        latest: String,
        allUserText: String
    ): String {
        return when (turnIndex) {
            0 -> """
IMMEDIATE FIRST AID ACTIONS:
1. Sit the person upright leaning slightly forward with hands on their knees — do NOT let them lie flat.
2. Loosen all tight clothing, backpack straps, and collars around their neck and chest.
3. If they have an asthma rescue inhaler, help them take 2 to 4 puffs right away.
4. Tell them to take slow, calm breaths: breathe in through the nose for 2 seconds, and blow out gently through pursed lips for 4 seconds.

TRIAGE ASSESSMENT:
Are you high in the mountains (above 8,000 feet / 2,500 meters), or is this an asthma attack, food choking, or allergy?
            """.trimIndent()

            1 -> {
                val isAltitude = containsAny(latest, "altitude", "mountain", "high", "pass", "peak", "elevation", "hike") ||
                        (isAffirmative(latest) && containsAny(allUserText, "altitude", "mountain"))
                val isChoking = containsAny(latest, "chok", "food", "throat", "swallow")

                when {
                    isAltitude -> """
⚠️ HIGH MOUNTAIN ALTITUDE SICKNESS STEPS:
1. START GOING DOWN IMMEDIATELY: Descend to lower ground right away (going down even 1,500 to 3,000 feet can save their life).
2. Do NOT hike any higher under any circumstances.
3. Give bottled oxygen if you have it, and keep them warm while moving down.
4. Do not let them carry heavy packs while descending.

TRIAGE ASSESSMENT:
Are they coughing up wet/pink bubbly spit, or are they stumbling and walking like they are drunk?
                    """.trimIndent()

                    isChoking -> """
CHOKING FIRST AID STEPS:
1. If the person can cough loudly: let them keep coughing hard — do not stop them.
2. If they CANNOT speak, breathe, or make sound: Stand behind them and give 5 firm slaps between their shoulder blades with the heel of your hand.
3. Next, place your fist just above their belly button and pull hard inward and upward 5 times (belly thrusts).
4. Keep repeating 5 back slaps and 5 belly thrusts until the stuck food pops out.

TRIAGE ASSESSMENT:
Has their airway cleared now, and are they able to breathe and speak?
                    """.trimIndent()

                    else -> """
BREATHING TROUBLE FIRST AID:
1. Give 2 to 4 puffs of their rescue asthma inhaler; repeat after 15 minutes if wheezing continues.
2. Keep the person sitting upright and talk to them calmly to stop panic and fast breathing.
3. If the mountain air is freezing cold, cover their mouth loosely with a scarf to warm the air.
4. Loosen all belts and upper clothing layers.

TRIAGE ASSESSMENT:
Are their lips or fingernails turning blue or grey, and can they speak full sentences without gasping for air?
                    """.trimIndent()
                }
            }

            2 -> {
                val isSevere = isAffirmative(latest) || containsAny(latest, "blue", "grey", "froth", "cough", "pink", "stumble", "confus", "gasp")
                if (isSevere) {
                    """
⚠️ CRITICAL BREATHING EMERGENCY:
1. Immediate fast descent to lower altitude or emergency rescue is required right now.
2. Keep the person sitting upright and give continuous oxygen if you have it.
3. If the person passes out and stops breathing, start CPR chest pushes right away (push hard and fast in the center of the chest).

TRIAGE ASSESSMENT:
Is the person awake, and is satellite emergency rescue / SOS active?
                    """.trimIndent()
                } else {
                    """
BREATHING IS IMPROVING:
1. Breathing is getting easier. Keep doing slow nose-in and mouth-out breathing cycles.
2. Keep sitting upright and stay warm in dry clothes.
3. Avoid hard walking or hiking uphill.

TRIAGE ASSESSMENT:
Are their breathing and pulse steady now?
                    """.trimIndent()
                }
            }

            else -> """
REST & RESCUE PREPARATION:
1. Check their breathing rate and how awake they are every 10 minutes.
2. Keep the person warm, sheltered, and calm.
3. Prepare to help them walk down to lower elevation or wait for rescue.

TRIAGE ASSESSMENT:
First aid steps are ready. Are you ready to see the full simple summary and rescue signaling steps?
            """.trimIndent()
        }
    }

    // ==========================================
    // 5. HYPOTHERMIA & COLD INJURY (Plain, Easy Words)
    // ==========================================
    private fun handleHypothermiaTurn(
        turnIndex: Int,
        latest: String
    ): String {
        return when (turnIndex) {
            0 -> """
IMMEDIATE FIRST AID ACTIONS:
1. Move the person out of the wind, rain, and snow into a tent or shelter right now.
2. Take off all wet clothes and put on dry, warm layers, a warm hat, and a windproof jacket.
3. Put sleeping pads, backpacks, or dry leaves under them so they are not touching the frozen ground.
4. Wrap them up tightly in warm blankets or sleeping bags like a burrito.

TRIAGE ASSESSMENT:
Are they shivering hard, or have they STOPPED shivering while acting very sleepy, slow, or confused?
            """.trimIndent()

            1 -> {
                val stoppedShivering = containsAny(latest, "stopped", "no shivering", "confus", "slur", "stumbl", "sleepy", "unconscious") ||
                        (isAffirmative(latest) && containsAny(latest, "stopped"))

                if (stoppedShivering && !containsAny(latest, "still shivering", "shivering violently")) {
                    """
⚠️ SEVERE COLD EMERGENCY:
1. Their body is dangerously cold — handle them very gently without sudden rough movements.
2. Put warm (not boiling) water bottles wrapped in cloths on their chest, armpits, and groin.
3. Cover their head and neck with a warm hat, leaving only their face open to breathe.
4. Do NOT give food or drinks, and do NOT rub their cold arms or legs.

TRIAGE ASSESSMENT:
Are any fingers, toes, ears, or nose frozen hard, pale white, and completely numb (frostbite)?
                    """.trimIndent()
                } else {
                    """
MILD COLD RECOVERY FIRST AID:
1. Give them warm, sweet drinks (warm tea, soup, or warm sugary water) to help their body create heat.
2. Feed high-energy snacks like chocolate, energy bars, or dried fruit.
3. Keep their head, neck, and hands covered with warm dry gear.
4. Let them rest inside the dry shelter.

TRIAGE ASSESSMENT:
Are any fingers, toes, nose, or ears frozen white, hard, and numb?
                    """.trimIndent()
                }
            }

            2 -> {
                val hasFrostbite = isAffirmative(latest) || containsAny(latest, "white", "hard", "numb", "waxy", "frozen", "frostbite")
                if (hasFrostbite && !isNegative(latest)) {
                    """
FROSTBITE FIRST AID:
1. DO NOT rub, scratch, or put snow on frozen skin — rubbing damages frozen skin cells.
2. Wrap the cold fingers/toes loosely in a soft, clean, dry cloth.
3. Do NOT thaw frozen feet if the person still needs to walk for help (re-freezing causes permanent loss of toes).
4. Keep the rest of their body warm with blankets.

TRIAGE ASSESSMENT:
Is the person awake and able to talk clearly, and is rescue signaling ready?
                    """.trimIndent()
                } else {
                    """
WARMING UP & REST:
1. Keep warming them up with dry sleeping bags, body heat, and warm sweet drinks.
2. Keep them completely off the cold ground and away from wind.
3. Watch how warm they feel and check their shivering.

TRIAGE ASSESSMENT:
Are their breathing and alertness steady?
                    """.trimIndent()
                }
            }

            else -> """
COLD RECOVERY & RESCUE PREPARATION:
1. Keep them wrapped in warm blankets until help arrives or they feel warm again.
2. Check their pulse, breathing, and alertness every 15 minutes.
3. Call satellite SOS or blow a whistle 3 times every minute to signal rescuers.

TRIAGE ASSESSMENT:
First aid steps are ready. Are you ready to see the full simple summary and rescue signaling steps?
            """.trimIndent()
        }
    }

    // ==========================================
    // 6. CARDIAC & CHEST PAIN (Plain, Easy Words)
    // ==========================================
    private fun handleCardiacTurn(
        turnIndex: Int
    ): String {
        return when (turnIndex) {
            0 -> """
IMMEDIATE FIRST AID ACTIONS:
1. Stop all walking and moving immediately. Have the person sit comfortably with knees bent.
2. Loosen tight shirts, collars, and belts.
3. If they are not allergic and have no stomach bleeding: have them chew 1 regular Aspirin (325mg) or 4 low-dose baby aspirins.
4. If they have their own prescribed heart medicine (Nitroglycerin), help them take one dose under the tongue.

TRIAGE ASSESSMENT:
Is the chest pain crushing or tight, does it spread to their left arm, neck, or jaw, and do they feel sweaty or short of breath?
            """.trimIndent()

            1 -> """
HEART ATTACK FIRST AID:
1. Keep the person completely quiet, rested, and calm — do not let them walk even short distances.
2. Keep them warm with jackets (cold weather puts extra strain on the heart).
3. Watch their breathing closely. If they pass out and stop breathing, start chest CPR pushes right away (push hard and fast in the center of the chest, 2 pushes per second).

TRIAGE ASSESSMENT:
Is the person awake, talking, and responding clearly?
            """.trimIndent()

            else -> """
CALL EMERGENCY SOS:
1. Call satellite emergency rescue / SOS immediately.
2. Keep the person resting in a comfortable sitting position and keep them calm.
3. If they stop breathing: start continuous chest compressions (CPR) immediately.

TRIAGE ASSESSMENT:
First aid steps are ready. Are you ready to see the full simple summary and rescue signaling steps?
            """.trimIndent()
        }
    }

    // ==========================================
    // 7. HEAD TRAUMA & CONCUSSION (Plain, Easy Words)
    // ==========================================
    private fun handleHeadTurn(
        turnIndex: Int,
        latest: String
    ): String {
        return when (turnIndex) {
            0 -> """
IMMEDIATE FIRST AID ACTIONS:
1. Keep the person lying flat and hold their head and neck still — do not let them twist or turn their head.
2. Do NOT move them unless there is immediate danger (like falling rocks).
3. If their scalp is bleeding, press gently with a clean cloth (do not press hard if the skull feels soft).
4. If they vomit, carefully roll their whole body together onto their side so they do not choke.

TRIAGE ASSESSMENT:
Did they pass out or lose consciousness (even for a few seconds), or are they confused and repeating questions?
            """.trimIndent()

            1 -> {
                val isSevere = isAffirmative(latest) || containsAny(latest, "unconscious", "blackout", "passed out", "dizzy", "confus", "vomit", "amnesia")
                if (isSevere) {
                    """
⚠️ HEAD INJURY EMERGENCY STEPS:
1. Keep their head and neck completely still.
2. Check if their eye pupils are equal in size and react to light.
3. Look for any clear fluid or blood coming from their ears or nose.
4. Keep them warm and watch them closely so they do not choke if they throw up.

TRIAGE ASSESSMENT:
Are they throwing up repeatedly, complaining of a severe headache, or having trouble moving their arms or legs?
                    """.trimIndent()
                } else {
                    """
MILD HEAD HIT / CONCUSSION FIRST AID:
1. Let the person rest quietly in a shady, peaceful spot.
2. No hiking, reading, or screen time right now.
3. Check on them every 15 minutes to make sure they know their name and where they are.
4. Give Paracetamol / Tylenol for headache if needed (avoid Aspirin or Ibuprofen right after a head strike).

TRIAGE ASSESSMENT:
Are they feeling sick to their stomach (nausea), dizzy, or sensitive to light?
                    """.trimIndent()
                }
            }

            else -> """
HEAD INJURY MONITORING & GETTING HELP:
1. Call emergency rescue if they passed out, threw up repeatedly, or feel very confused.
2. Keep their head and neck still during all movement.
3. Watch their alertness and breathing closely.

TRIAGE ASSESSMENT:
First aid steps are ready. Are you ready to see the full simple summary and rescue signaling steps?
            """.trimIndent()
        }
    }

    // ==========================================
    // 8. GENERAL & INTENT-MATCHED TRIAGE (Plain, Easy Words)
    // ==========================================
    private fun handleGeneralOrIntentTurn(
        turnIndex: Int,
        data: SymptomEmergencyData,
        extract: ClinicalExtractionResult?
    ): String {
        val steps = data.firstAidSteps.ifEmpty {
            listOf(
                "Make sure the area is safe from falling rocks, bad weather, or animals.",
                "Check if the person is awake, breathing normally, and can speak to you.",
                "Press firmly on any bleeding wounds with a clean cloth to stop blood flow.",
                "Keep them warm: place sleeping pads under them and wrap them in an emergency blanket.",
                "Keep the person calm, resting, and give small sips of water if they are awake and not sick."
            )
        }

        val stepList = steps.mapIndexed { idx, s -> "${idx + 1}. $s" }.joinToString("\n")

        return when (turnIndex) {
            0 -> {
                val triageQ = if (extract != null && extract.triageQuestion.isNotBlank()) {
                    extract.triageQuestion
                } else {
                    data.questions.firstOrNull() ?: "Is the person awake, breathing normally, and able to talk to you?"
                }

                """
IMMEDIATE FIRST AID ACTIONS:
$stepList

TRIAGE ASSESSMENT:
$triageQ
                """.trimIndent()
            }

            1 -> {
                val qList = data.questions
                val nextQ = if (qList.size > 1) qList[1] else "Are there any warning signs such as severe pain, fast swelling, dizziness, or numbness?"

                """
FIRST AID FOLLOW-UP GUIDANCE:
1. Continue with the initial first aid steps above.
2. Keep the hurt area supported and protected from further injury.
3. Watch the person closely for any changes in how alert they are.
4. Make sure they stay warm, sheltered, and resting comfortably.

TRIAGE ASSESSMENT:
$nextQ
                """.trimIndent()
            }

            else -> """
PATIENT CARE & RESCUE PREPARATION:
1. Keep all bandages clean and tightly in place.
2. Keep them warm and off the cold ground with sleeping pads and blankets.
3. Check their breathing, pulse, and alertness every 15 minutes.

TRIAGE ASSESSMENT:
First aid steps are ready. Are you ready to see the full simple summary and rescue signaling steps?
            """.trimIndent()
        }
    }

    /**
     * Generates comprehensive diagnosis and treatment protocol, strictly formatted in simple, easy-to-understand points.
     */
    private fun generateDiagnosisAndTreatment(
        category: EmergencyCategory,
        data: SymptomEmergencyData,
        extract: ClinicalExtractionResult?,
        userInputs: List<String>
    ): String {
        val stepsFormatted = data.firstAidSteps.mapIndexed { idx, step -> "${idx + 1}. $step" }.joinToString("\n")
        val warningsFormatted = if (data.warningSigns.isNotEmpty()) {
            data.warningSigns.joinToString("\n") { "• $it" }
        } else "• Spreading redness, fever, severe swelling, numbness, or falling unconscious"

        val summaryTitle = if (extract != null) {
            "EMERGENCY ASSESSMENT: ${extract.conditionName.uppercase()} PLAN"
        } else {
            when (category) {
                EmergencyCategory.BITE -> "EMERGENCY ASSESSMENT: Bite & Sting First Aid Plan"
                EmergencyCategory.BLEEDING -> "EMERGENCY ASSESSMENT: Bleeding & Wound Care Plan"
                EmergencyCategory.FRACTURE -> "EMERGENCY ASSESSMENT: Broken Bone & Sprain First Aid Plan"
                EmergencyCategory.BREATHING -> "EMERGENCY ASSESSMENT: Breathing & Altitude First Aid Plan"
                EmergencyCategory.HYPOTHERMIA -> "EMERGENCY ASSESSMENT: Cold Injury & Hypothermia First Aid Plan"
                EmergencyCategory.CARDIAC -> "EMERGENCY ASSESSMENT: Heart Emergency First Aid Plan"
                EmergencyCategory.HEAD -> "EMERGENCY ASSESSMENT: Head Injury & Concussion First Aid Plan"
                EmergencyCategory.GENERAL -> "EMERGENCY ASSESSMENT: First Aid Plan (${data.name})"
            }
        }

        val allUserText = userInputs.joinToString(" ").lowercase()
        val shockNote = if (containsAny(allUserText, "shock", "pale", "dizzy", "cold skin", "faint", "spurting", "heavy bleeding")) {
            "⚠️ WARNING: PERSON SHOWS SIGNS OF SHOCK:\n• Lay them flat and raise their feet 12 inches (unless neck/spine is hurt).\n• Wrap them tightly in an emergency blanket or sleeping bag to stay warm.\n• Check their pulse and alertness every 5 minutes.\n\n"
        } else ""

        val evacuationFormatted = if (data.evacuationNote.isNotBlank()) {
            data.evacuationNote
        } else {
            "1. Activate Satellite SOS (Garmin inReach, Apple Emergency SOS, or 406MHz beacon).\n2. Write down your GPS coordinates.\n3. Blow your whistle 3 loud times every minute so rescue teams can find you."
        }

        return """
$summaryTitle

$shockNote ACTION PROTOCOL:
$stepsFormatted

CRITICAL WARNING SIGNS (Situation Worsening):
$warningsFormatted

EVACUATION & SATELLITE SOS:
$evacuationFormatted
        """.trimIndent()
    }

    private fun isGreeting(text: String): Boolean {
        val trimmed = text.trim().lowercase()
        return trimmed in listOf("hi", "hii", "hiii", "hello", "hey", "heyy", "greetings", "good morning", "good evening", "who are you", "what can you do", "help")
    }

    private fun isAffirmative(text: String): Boolean {
        val clean = text.trim().lowercase()
        return clean in listOf("yes", "yep", "yeah", "yup", "sure", "true", "correct", "it is", "spurting", "a lot", "severe", "artery", "heavy", "open", "sticking out", "broken") ||
                clean.startsWith("yes") || clean.startsWith("yeah") || clean.startsWith("yep")
    }

    private fun isNegative(text: String): Boolean {
        val clean = text.trim().lowercase()
        return clean in listOf("no", "nope", "nah", "not really", "steady", "slow", "none", "negative", "clear", "intact", "no object", "closed", "not spurting") ||
                clean.startsWith("no") || clean.startsWith("nope")
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { text.contains(it, ignoreCase = true) }
    }
}
