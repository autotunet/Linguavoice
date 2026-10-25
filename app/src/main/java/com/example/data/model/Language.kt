package com.example.data.model

import java.util.Locale

enum class Language(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagEmoji: String,
    val locale: Locale,
    val greeting: String,
    val greetingTranslation: String,
    val phoneticFocus: String,
    val cefrRange: String = "A1 - B2"
) {
    ENGLISH(
        code = "en",
        displayName = "Inglés",
        nativeName = "English (US)",
        flagEmoji = "🇺🇸",
        locale = Locale.US,
        greeting = "Hello, how are you today?",
        greetingTranslation = "Hola, ¿cómo estás hoy?",
        phoneticFocus = "Sonidos TH, R retrofleja y enlace vocálico"
    ),
    SPANISH(
        code = "es",
        displayName = "Español",
        nativeName = "Español (España/Latam)",
        flagEmoji = "🇪🇸",
        locale = Locale("es", "ES"),
        greeting = "¡Hola! ¿Cómo estás hoy?",
        greetingTranslation = "¡Hola! ¿Cómo estás hoy?",
        phoneticFocus = "Vibrante múltiple RR, sonido Ñ y vocales abiertas claras"
    ),
    FRENCH(
        code = "fr",
        displayName = "Francés",
        nativeName = "Français (France)",
        flagEmoji = "🇫🇷",
        locale = Locale.FRANCE,
        greeting = "Bonjour, enchanté de faire votre connaissance !",
        greetingTranslation = "¡Buenos días, encantado de conocerte!",
        phoneticFocus = "Vocales nasales, liaison y R suave gutural"
    ),
    GERMAN(
        code = "de",
        displayName = "Alemán",
        nativeName = "Deutsch (Deutschland)",
        flagEmoji = "🇩🇪",
        locale = Locale.GERMANY,
        greeting = "Guten Tag, wie geht es Ihnen?",
        greetingTranslation = "Buenos días, ¿cómo está usted?",
        phoneticFocus = "Umlauts (ä, ö, ü), consonantes largas y sonido Ich"
    ),
    ITALIAN(
        code = "it",
        displayName = "Italiano",
        nativeName = "Italiano (Italia)",
        flagEmoji = "🇮🇹",
        locale = Locale.ITALY,
        greeting = "Ciao, piacere di conoscerti!",
        greetingTranslation = "¡Hola, un placer conocerte!",
        phoneticFocus = "Consonantes dobles, entonación melódica y gli/gn"
    ),
    PORTUGUESE(
        code = "pt",
        displayName = "Portugués",
        nativeName = "Português (Brasil)",
        flagEmoji = "🇧🇷",
        locale = Locale("pt", "BR"),
        greeting = "Olá, tudo bem com você?",
        greetingTranslation = "Hola, ¿todo bien contigo?",
        phoneticFocus = "Diptongos nasales (-ão), ritmo brasileño y ti/di palatal"
    );

    companion object {
        fun fromCode(code: String?): Language {
            if (code.isNullOrBlank()) return ENGLISH
            return entries.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) } ?: ENGLISH
        }

        /**
         * Ensures the target language to learn is distinct from the user's spoken language when possible.
         */
        fun resolveDistinctTargetLanguage(
            spokenLanguage: SpokenLanguage,
            desiredTarget: Language
        ): Language {
            if (!spokenLanguage.code.equals(desiredTarget.code, ignoreCase = true)) {
                return desiredTarget
            }
            return entries.firstOrNull { !it.code.equals(spokenLanguage.code, ignoreCase = true) }
                ?: ENGLISH
        }
    }

    fun getDisplayNameFor(spokenLanguage: SpokenLanguage): String {
        return when (spokenLanguage) {
            SpokenLanguage.SPANISH -> displayName
            SpokenLanguage.ENGLISH -> when (this) {
                ENGLISH -> "English"
                SPANISH -> "Spanish"
                FRENCH -> "French"
                GERMAN -> "German"
                ITALIAN -> "Italian"
                PORTUGUESE -> "Portuguese"
            }
            SpokenLanguage.PORTUGUESE -> when (this) {
                ENGLISH -> "Inglês"
                SPANISH -> "Espanhol"
                FRENCH -> "Francês"
                GERMAN -> "Alemão"
                ITALIAN -> "Italiano"
                PORTUGUESE -> "Português"
            }
            SpokenLanguage.FRENCH -> when (this) {
                ENGLISH -> "Anglais"
                SPANISH -> "Espagnol"
                FRENCH -> "Français"
                GERMAN -> "Allemand"
                ITALIAN -> "Italien"
                PORTUGUESE -> "Portugais"
            }
            SpokenLanguage.ITALIAN -> when (this) {
                ENGLISH -> "Inglese"
                SPANISH -> "Spagnolo"
                FRENCH -> "Francese"
                GERMAN -> "Tedesco"
                ITALIAN -> "Italiano"
                PORTUGUESE -> "Portoghese"
            }
            SpokenLanguage.GERMAN -> when (this) {
                ENGLISH -> "Englisch"
                SPANISH -> "Spanisch"
                FRENCH -> "Französisch"
                GERMAN -> "Deutsch"
                ITALIAN -> "Italienisch"
                PORTUGUESE -> "Portugiesisch"
            }
        }
    }

    fun getPhoneticFocusFor(spokenLanguage: SpokenLanguage): String {
        return when (spokenLanguage) {
            SpokenLanguage.SPANISH -> phoneticFocus
            SpokenLanguage.ENGLISH -> when (this) {
                ENGLISH -> "TH sounds, retroflex R and vowel linking"
                SPANISH -> "Rolled RR, Ñ sound and crisp open vowels"
                FRENCH -> "Nasal vowels, liaison and soft guttural R"
                GERMAN -> "Umlauts (ä, ö, ü), long consonants and Ich sound"
                ITALIAN -> "Double consonants, melodic intonation and gli/gn"
                PORTUGUESE -> "Nasal diphthongs (-ão), Brazilian rhythm and palatal ti/di"
            }
            SpokenLanguage.PORTUGUESE -> when (this) {
                ENGLISH -> "Sons TH, R retroflexo e ligação vocálica"
                SPANISH -> "RR vibrante, som Ñ e vogais abertas claras"
                FRENCH -> "Vogais nasais, liaison e R gutural suave"
                GERMAN -> "Umlauts (ä, ö, ü), consoantes longas e som Ich"
                ITALIAN -> "Consoantes duplas, entonação melódica e gli/gn"
                PORTUGUESE -> "Ditongos nasais (-ão), ritmo brasileiro e ti/di palatal"
            }
            SpokenLanguage.FRENCH -> when (this) {
                ENGLISH -> "Sons TH, R rétroflexe et liaison vocalique"
                SPANISH -> "R roulé (RR), son Ñ et voyelles claires"
                FRENCH -> "Voyelles nasales, liaison et R guttural doux"
                GERMAN -> "Umlauts (ä, ö, ü), consonnes longues et son Ich"
                ITALIAN -> "Consonnes doubles, intonation mélodique et gli/gn"
                PORTUGUESE -> "Diphtongues nasales (-ão), rythme brésilien et ti/di palatal"
            }
            SpokenLanguage.ITALIAN -> when (this) {
                ENGLISH -> "Suoni TH, R retroflessa e collegamento vocalico"
                SPANISH -> "RR vibrante, suono Ñ e vocali chiare"
                FRENCH -> "Vocali nasali, liaison e R gutturale morbida"
                GERMAN -> "Umlaut (ä, ö, ü), consonanti lunghe e suono Ich"
                ITALIAN -> "Consonanti doppie, intonazione melodica e gli/gn"
                PORTUGUESE -> "Dittonghi nasali (-ão), ritmo brasiliano e ti/di palatale"
            }
            SpokenLanguage.GERMAN -> when (this) {
                ENGLISH -> "TH-Laute, retroflexes R und Vokalbindung"
                SPANISH -> "Gerolltes RR, Ñ-Laut und klare offene Vokale"
                FRENCH -> "Nasalvokale, Liaison und weiches Kehlkopf-R"
                GERMAN -> "Umlaute (ä, ö, ü), lange Konsonanten und Ich-Laut"
                ITALIAN -> "Doppelkonsonanten, melodische Intonation und gli/gn"
                PORTUGUESE -> "Nasaldiphthonge (-ão), brasilianischer Rhythmus und ti/di"
            }
        }
    }

    fun getGreetingTranslationFor(spokenLanguage: SpokenLanguage): String {
        return when (spokenLanguage) {
            SpokenLanguage.SPANISH -> greetingTranslation
            SpokenLanguage.ENGLISH -> when (this) {
                ENGLISH -> "Hello, how are you today?"
                SPANISH -> "Hello! How are you today?"
                FRENCH -> "Good morning, nice to meet you!"
                GERMAN -> "Good day, how are you?"
                ITALIAN -> "Hello, nice to meet you!"
                PORTUGUESE -> "Hello, is everything good with you?"
            }
            SpokenLanguage.FRENCH -> when (this) {
                ENGLISH -> "Bonjour, comment allez-vous aujourd'hui ?"
                SPANISH -> "Bonjour ! Comment allez-vous aujourd'hui ?"
                FRENCH -> "Bonjour, enchanté de faire votre connaissance !"
                GERMAN -> "Bonjour, comment allez-vous ?"
                ITALIAN -> "Salut, ravi de te rencontrer !"
                PORTUGUESE -> "Bonjour, tout va bien avec toi ?"
            }
            SpokenLanguage.PORTUGUESE -> when (this) {
                ENGLISH -> "Olá, como você está hoje?"
                SPANISH -> "Olá! Como você está hoje?"
                FRENCH -> "Bom dia, prazer em conhecê-lo!"
                GERMAN -> "Bom dia, como vai você?"
                ITALIAN -> "Olá, prazer em te conhecer!"
                PORTUGUESE -> "Olá, tudo bem com você?"
            }
            SpokenLanguage.GERMAN -> when (this) {
                ENGLISH -> "Hallo, wie geht es dir heute?"
                SPANISH -> "Hallo! Wie geht es dir heute?"
                FRENCH -> "Guten Tag, freut mich Sie kennenzulernen!"
                GERMAN -> "Guten Tag, wie geht es Ihnen?"
                ITALIAN -> "Hallo, schön dich kennenzulernen!"
                PORTUGUESE -> "Hallo, ist alles gut bei dir?"
            }
            SpokenLanguage.ITALIAN -> when (this) {
                ENGLISH -> "Ciao, come stai oggi?"
                SPANISH -> "Ciao! Come stai oggi?"
                FRENCH -> "Buongiorno, piacere di conoscerla!"
                GERMAN -> "Buongiorno, come sta?"
                ITALIAN -> "Ciao, piacere di conoscerti!"
                PORTUGUESE -> "Ciao, tutto bene con te?"
            }
        }
    }
}

/**
 * Represents the language the user already speaks ("Idioma que hablas").
 * Used to localize the entire app UI, translations, phonetic tips, and onboarding prompts.
 */
enum class SpokenLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagEmoji: String,
    val locale: Locale,
    val samplePhrase: String,
    val translationLabel: String
) {
    SPANISH(
        code = "es",
        displayName = "Español",
        nativeName = "Español",
        flagEmoji = "🇪🇸",
        locale = Locale("es", "ES"),
        samplePhrase = "Hablo español (App en español)",
        translationLabel = "Traducción en Español"
    ),
    ENGLISH(
        code = "en",
        displayName = "Inglés",
        nativeName = "English",
        flagEmoji = "🇺🇸",
        locale = Locale.US,
        samplePhrase = "I speak English (App in English)",
        translationLabel = "Translation in English"
    ),
    PORTUGUESE(
        code = "pt",
        displayName = "Portugués",
        nativeName = "Português",
        flagEmoji = "🇧🇷",
        locale = Locale("pt", "BR"),
        samplePhrase = "Eu falo português (App em português)",
        translationLabel = "Tradução em Português"
    ),
    FRENCH(
        code = "fr",
        displayName = "Francés",
        nativeName = "Français",
        flagEmoji = "🇫🇷",
        locale = Locale.FRANCE,
        samplePhrase = "Je parle français (App en français)",
        translationLabel = "Traduction en Français"
    ),
    ITALIAN(
        code = "it",
        displayName = "Italiano",
        nativeName = "Italiano",
        flagEmoji = "🇮🇹",
        locale = Locale.ITALY,
        samplePhrase = "Parlo italiano (App in italiano)",
        translationLabel = "Traduzione in Italiano"
    ),
    GERMAN(
        code = "de",
        displayName = "Alemán",
        nativeName = "Deutsch",
        flagEmoji = "🇩🇪",
        locale = Locale.GERMANY,
        samplePhrase = "Ich spreche Deutsch (App auf Deutsch)",
        translationLabel = "Übersetzung auf Deutsch"
    );

    fun getDisplayNameFor(appLanguage: SpokenLanguage): String {
        return when (appLanguage) {
            SPANISH -> displayName
            ENGLISH -> when (this) {
                SPANISH -> "Spanish"
                ENGLISH -> "English"
                PORTUGUESE -> "Portuguese"
                FRENCH -> "French"
                ITALIAN -> "Italian"
                GERMAN -> "German"
            }
            PORTUGUESE -> when (this) {
                SPANISH -> "Espanhol"
                ENGLISH -> "Inglês"
                PORTUGUESE -> "Português"
                FRENCH -> "Francês"
                ITALIAN -> "Italiano"
                GERMAN -> "Alemão"
            }
            FRENCH -> when (this) {
                SPANISH -> "Espagnol"
                ENGLISH -> "Anglais"
                PORTUGUESE -> "Portugais"
                FRENCH -> "Français"
                ITALIAN -> "Italien"
                GERMAN -> "Allemand"
            }
            ITALIAN -> when (this) {
                SPANISH -> "Spagnolo"
                ENGLISH -> "Inglese"
                PORTUGUESE -> "Portoghese"
                FRENCH -> "Francese"
                ITALIAN -> "Italiano"
                GERMAN -> "Tedesco"
            }
            GERMAN -> when (this) {
                SPANISH -> "Spanisch"
                ENGLISH -> "Englisch"
                PORTUGUESE -> "Portugiesisch"
                FRENCH -> "Französisch"
                ITALIAN -> "Italienisch"
                GERMAN -> "Deutsch"
            }
        }
    }

    companion object {
        fun fromCode(code: String?): SpokenLanguage {
            if (code.isNullOrBlank()) return SPANISH
            return entries.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) } ?: SPANISH
        }
    }
}

enum class LearningFocusGoal(
    val id: String,
    val titleEs: String,
    val descriptionEs: String,
    val badgeEmoji: String
) {
    DAILY_CONVERSATION(
        id = "conversation",
        titleEs = "Conversación Diaria",
        descriptionEs = "Saludos, cortesía y charlas cotidianas con fluidez",
        badgeEmoji = "💬"
    ),
    TRAVEL_AND_CULTURE(
        id = "travel",
        titleEs = "Viajes y Restaurantes",
        descriptionEs = "Aeropuertos, hoteles, cafés y direcciones en la ciudad",
        badgeEmoji = "✈️"
    ),
    NATIVE_PRONUNCIATION(
        id = "phonetics",
        titleEs = "Pronunciación Nativa",
        descriptionEs = "Fonética avanzada, trabalenguas y acento natural",
        badgeEmoji = "🎙️"
    );

    fun getTitleFor(spokenLanguage: SpokenLanguage): String {
        return when (spokenLanguage) {
            SpokenLanguage.SPANISH -> titleEs
            SpokenLanguage.ENGLISH -> when (this) {
                DAILY_CONVERSATION -> "Daily Conversation"
                TRAVEL_AND_CULTURE -> "Travel & Restaurants"
                NATIVE_PRONUNCIATION -> "Native Pronunciation"
            }
            SpokenLanguage.PORTUGUESE -> when (this) {
                DAILY_CONVERSATION -> "Conversação Diária"
                TRAVEL_AND_CULTURE -> "Viagens e Restaurantes"
                NATIVE_PRONUNCIATION -> "Pronúncia Nativa"
            }
            SpokenLanguage.FRENCH -> when (this) {
                DAILY_CONVERSATION -> "Conversation Quotidienne"
                TRAVEL_AND_CULTURE -> "Voyages et Restaurants"
                NATIVE_PRONUNCIATION -> "Prononciation Native"
            }
            SpokenLanguage.ITALIAN -> when (this) {
                DAILY_CONVERSATION -> "Conversazione Quotidiana"
                TRAVEL_AND_CULTURE -> "Viaggi e Ristoranti"
                NATIVE_PRONUNCIATION -> "Pronuncia Madrelingua"
            }
            SpokenLanguage.GERMAN -> when (this) {
                DAILY_CONVERSATION -> "Tägliche Konversation"
                TRAVEL_AND_CULTURE -> "Reisen & Restaurants"
                NATIVE_PRONUNCIATION -> "Muttersprachliche Aussprache"
            }
        }
    }

    fun getDescriptionFor(spokenLanguage: SpokenLanguage): String {
        return when (spokenLanguage) {
            SpokenLanguage.SPANISH -> descriptionEs
            SpokenLanguage.ENGLISH -> when (this) {
                DAILY_CONVERSATION -> "Greetings, politeness and fluent everyday chats"
                TRAVEL_AND_CULTURE -> "Airports, hotels, cafes and city directions"
                NATIVE_PRONUNCIATION -> "Advanced phonetics, tongue twisters and natural accent"
            }
            SpokenLanguage.PORTUGUESE -> when (this) {
                DAILY_CONVERSATION -> "Saudações, cortesia e conversas cotidianas com fluência"
                TRAVEL_AND_CULTURE -> "Aeroportos, hotéis, cafés e direções na cidade"
                NATIVE_PRONUNCIATION -> "Fonética avançada, trava-línguas e sotaque natural"
            }
            SpokenLanguage.FRENCH -> when (this) {
                DAILY_CONVERSATION -> "Salutations, politesse et conversations fluides"
                TRAVEL_AND_CULTURE -> "Aéroports, hôtels, cafés et directions en ville"
                NATIVE_PRONUNCIATION -> "Phonétique avancée, virelangues et accent naturel"
            }
            SpokenLanguage.ITALIAN -> when (this) {
                DAILY_CONVERSATION -> "Saluti, cortesia e conversazioni quotidiane fluenti"
                TRAVEL_AND_CULTURE -> "Aeroporti, hotel, caffè e indicazioni in città"
                NATIVE_PRONUNCIATION -> "Fonetica avanzata, scioglilingua e accento naturale"
            }
            SpokenLanguage.GERMAN -> when (this) {
                DAILY_CONVERSATION -> "Begrüßungen, Höflichkeit und flüssige Alltagsgespräche"
                TRAVEL_AND_CULTURE -> "Flughäfen, Hotels, Cafés und Wegbeschreibungen"
                NATIVE_PRONUNCIATION -> "Fortgeschrittene Phonetik, Zungenbrecher und Akzent"
            }
        }
    }

    companion object {
        fun fromId(id: String?): LearningFocusGoal {
            return entries.firstOrNull { it.id == id } ?: DAILY_CONVERSATION
        }
    }
}

enum class ExerciseType {
    REPEAT_PHRASE,        // Duolingo speak phrase
    LISTENING_AND_SPEAK, // Listen first, then reproduce
    FILL_IN_BLANK_SPEAK, // Speak the missing word
    TONGUE_TWISTER,      // High-speed pronunciation challenge
    DIALOGUE_REPLY       // Conversational response
}

data class Exercise(
    val id: String,
    val language: Language,
    val lessonId: String,
    val type: ExerciseType,
    val promptQuestion: String,
    val targetPhrase: String,
    val translation: String,
    val phoneticGuide: String,
    val pronunciationTip: String,
    val xpReward: Int = 15
) {
    fun getLocalizedPromptQuestion(spokenLanguage: SpokenLanguage): String {
        if (spokenLanguage == SpokenLanguage.SPANISH) return promptQuestion
        return when (type) {
            ExerciseType.REPEAT_PHRASE -> when (spokenLanguage) {
                SpokenLanguage.ENGLISH -> "Repeat out loud into the microphone:"
                SpokenLanguage.PORTUGUESE -> "Repita em voz alta no microfone:"
                SpokenLanguage.FRENCH -> "Répétez à voix haute dans le microphone :"
                SpokenLanguage.ITALIAN -> "Ripeti ad alta voce nel microfono:"
                SpokenLanguage.GERMAN -> "Sprich laut in das Mikrofon nach:"
                SpokenLanguage.SPANISH -> promptQuestion
            }
            ExerciseType.LISTENING_AND_SPEAK -> when (spokenLanguage) {
                SpokenLanguage.ENGLISH -> "Listen to the native speaker and repeat:"
                SpokenLanguage.PORTUGUESE -> "Ouça o falante nativo e repita:"
                SpokenLanguage.FRENCH -> "Écoutez le locuteur natif et répétez :"
                SpokenLanguage.ITALIAN -> "Ascolta il madrelingua e ripeti:"
                SpokenLanguage.GERMAN -> "Höre zu und sprich den Satz nach:"
                SpokenLanguage.SPANISH -> promptQuestion
            }
            ExerciseType.DIALOGUE_REPLY -> when (spokenLanguage) {
                SpokenLanguage.ENGLISH -> "Answer the question in ${language.getDisplayNameFor(spokenLanguage)}: ($promptQuestion)"
                SpokenLanguage.PORTUGUESE -> "Responda à pergunta em ${language.getDisplayNameFor(spokenLanguage)}: ($promptQuestion)"
                SpokenLanguage.FRENCH -> "Répondez à la question en ${language.getDisplayNameFor(spokenLanguage)} : ($promptQuestion)"
                SpokenLanguage.ITALIAN -> "Rispondi alla domanda in ${language.getDisplayNameFor(spokenLanguage)}: ($promptQuestion)"
                SpokenLanguage.GERMAN -> "Beantworte die Frage auf ${language.getDisplayNameFor(spokenLanguage)}: ($promptQuestion)"
                SpokenLanguage.SPANISH -> promptQuestion
            }
            ExerciseType.FILL_IN_BLANK_SPEAK -> when (spokenLanguage) {
                SpokenLanguage.ENGLISH -> "Complete and say the full phrase out loud:"
                SpokenLanguage.PORTUGUESE -> "Complete e diga a frase inteira em voz alta:"
                SpokenLanguage.FRENCH -> "Complétez et dites toute la phrase à voix haute :"
                SpokenLanguage.ITALIAN -> "Completa e pronuncia l'intera frase ad alta voce:"
                SpokenLanguage.GERMAN -> "Ergänze und sprich den ganzen Satz laut aus:"
                SpokenLanguage.SPANISH -> promptQuestion
            }
            ExerciseType.TONGUE_TWISTER -> when (spokenLanguage) {
                SpokenLanguage.ENGLISH -> "Speed pronunciation challenge! Say clearly:"
                SpokenLanguage.PORTUGUESE -> "Desafio de trava-língua! Pronuncie com clareza:"
                SpokenLanguage.FRENCH -> "Défi virelangue ! Prononcez clairement :"
                SpokenLanguage.ITALIAN -> "Sfida scioglilingua! Pronuncia chiaramente:"
                SpokenLanguage.GERMAN -> "Zungenbrecher-Challenge! Sprich deutlich:"
                SpokenLanguage.SPANISH -> promptQuestion
            }
        }
    }

    fun getLocalizedTranslation(spokenLanguage: SpokenLanguage): String {
        if (spokenLanguage == SpokenLanguage.SPANISH) return translation
        if (language == Language.ENGLISH && spokenLanguage == SpokenLanguage.ENGLISH) return targetPhrase
        return when (spokenLanguage) {
            SpokenLanguage.SPANISH -> translation
            SpokenLanguage.ENGLISH -> if (language == Language.SPANISH) translation else "${spokenLanguage.flagEmoji} $translation"
            else -> "${spokenLanguage.flagEmoji} $translation"
        }
    }
}

data class LessonModule(
    val id: String,
    val language: Language,
    val title: String,
    val description: String,
    val iconName: String,
    val requiredLevel: Int,
    val totalExercises: Int,
    val exercises: List<Exercise>
)

enum class PronunciationFeedbackStatus(
    val titleEs: String,
    val badgeLabelEs: String,
    val shortLabelEs: String,
    val guidanceEs: String
) {
    CORRECT(
        titleEs = "¡Pronunciación Correcta!",
        badgeLabelEs = "CORRECTA",
        shortLabelEs = "Correcta",
        guidanceEs = "Excelente dicción, entonación y claridad en cada sílaba."
    ),
    ALMOST_CORRECT(
        titleEs = "¡Casi Correcta!",
        badgeLabelEs = "CASI CORRECTA",
        shortLabelEs = "Casi correcta",
        guidanceEs = "Tu pronunciación se entiende bien; afina las palabras en ámbar."
    ),
    INCORRECT(
        titleEs = "Pronunciación Incorrecta",
        badgeLabelEs = "INCORRECTA",
        shortLabelEs = "Incorrecta",
        guidanceEs = "Escucha el audio lento y vuelve a grabar pronunciando cada palabra."
    );

    fun getTitleFor(spokenLanguage: SpokenLanguage): String = when (spokenLanguage) {
        SpokenLanguage.SPANISH -> titleEs
        SpokenLanguage.ENGLISH -> when (this) {
            CORRECT -> "Correct Pronunciation!"
            ALMOST_CORRECT -> "Almost Correct!"
            INCORRECT -> "Incorrect Pronunciation"
        }
        SpokenLanguage.PORTUGUESE -> when (this) {
            CORRECT -> "Pronúncia Correta!"
            ALMOST_CORRECT -> "Quase Correta!"
            INCORRECT -> "Pronúncia Incorreta"
        }
        SpokenLanguage.FRENCH -> when (this) {
            CORRECT -> "Prononciation Correcte !"
            ALMOST_CORRECT -> "Presque Correcte !"
            INCORRECT -> "Prononciation Incorrecte"
        }
        SpokenLanguage.ITALIAN -> when (this) {
            CORRECT -> "Pronuncia Corretta!"
            ALMOST_CORRECT -> "Quasi Corretta!"
            INCORRECT -> "Pronuncia Non Corretta"
        }
        SpokenLanguage.GERMAN -> when (this) {
            CORRECT -> "Richtige Aussprache!"
            ALMOST_CORRECT -> "Fast Richtig!"
            INCORRECT -> "Falsche Aussprache"
        }
    }

    fun getBadgeLabelFor(spokenLanguage: SpokenLanguage): String = when (spokenLanguage) {
        SpokenLanguage.SPANISH -> badgeLabelEs
        SpokenLanguage.ENGLISH -> when (this) {
            CORRECT -> "CORRECT"
            ALMOST_CORRECT -> "ALMOST CORRECT"
            INCORRECT -> "INCORRECT"
        }
        SpokenLanguage.PORTUGUESE -> when (this) {
            CORRECT -> "CORRETA"
            ALMOST_CORRECT -> "QUASE CORRETA"
            INCORRECT -> "INCORRETA"
        }
        SpokenLanguage.FRENCH -> when (this) {
            CORRECT -> "CORRECTE"
            ALMOST_CORRECT -> "PRESQUE CORRECTE"
            INCORRECT -> "INCORRECTE"
        }
        SpokenLanguage.ITALIAN -> when (this) {
            CORRECT -> "CORRETTA"
            ALMOST_CORRECT -> "QUASI CORRETTA"
            INCORRECT -> "ERRATA"
        }
        SpokenLanguage.GERMAN -> when (this) {
            CORRECT -> "RICHTIG"
            ALMOST_CORRECT -> "FAST RICHTIG"
            INCORRECT -> "FALSCH"
        }
    }

    fun getShortLabelFor(spokenLanguage: SpokenLanguage): String = when (spokenLanguage) {
        SpokenLanguage.SPANISH -> shortLabelEs
        SpokenLanguage.ENGLISH -> when (this) {
            CORRECT -> "Correct"
            ALMOST_CORRECT -> "Almost correct"
            INCORRECT -> "Incorrect"
        }
        SpokenLanguage.PORTUGUESE -> when (this) {
            CORRECT -> "Correta"
            ALMOST_CORRECT -> "Quase correta"
            INCORRECT -> "Incorreta"
        }
        SpokenLanguage.FRENCH -> when (this) {
            CORRECT -> "Correcte"
            ALMOST_CORRECT -> "Presque correcte"
            INCORRECT -> "Incorrecte"
        }
        SpokenLanguage.ITALIAN -> when (this) {
            CORRECT -> "Corretta"
            ALMOST_CORRECT -> "Quasi corretta"
            INCORRECT -> "Errata"
        }
        SpokenLanguage.GERMAN -> when (this) {
            CORRECT -> "Richtig"
            ALMOST_CORRECT -> "Fast richtig"
            INCORRECT -> "Falsch"
        }
    }

    fun getGuidanceFor(spokenLanguage: SpokenLanguage): String = when (spokenLanguage) {
        SpokenLanguage.SPANISH -> guidanceEs
        SpokenLanguage.ENGLISH -> when (this) {
            CORRECT -> "Excellent diction, intonation, and clarity on every syllable."
            ALMOST_CORRECT -> "Your pronunciation is well understood; refine the amber words."
            INCORRECT -> "Listen to the slow audio and record again pronouncing each word."
        }
        SpokenLanguage.PORTUGUESE -> when (this) {
            CORRECT -> "Excelente dicção, entonação e clareza em cada sílaba."
            ALMOST_CORRECT -> "Sua pronúncia é bem compreendida; ajuste as palavras em âmbar."
            INCORRECT -> "Ouça o áudio lento e grave novamente pronunciando cada palavra."
        }
        SpokenLanguage.FRENCH -> when (this) {
            CORRECT -> "Excellente diction, intonation et clarté sur chaque syllabe."
            ALMOST_CORRECT -> "Votre prononciation est bien comprise ; affinez les mots en ambre."
            INCORRECT -> "Écoutez l'audio lent et réenregistrez en prononçant chaque mot."
        }
        SpokenLanguage.ITALIAN -> when (this) {
            CORRECT -> "Ottima dizione, intonazione e chiarezza in ogni sillaba."
            ALMOST_CORRECT -> "La tua pronuncia si capisce bene; perfeziona le parole in ambra."
            INCORRECT -> "Ascolta l'audio lento e registra di nuovo pronunciando ogni parola."
        }
        SpokenLanguage.GERMAN -> when (this) {
            CORRECT -> "Ausgezeichnete Aussprache, Betonung und Klarheit in jeder Silbe."
            ALMOST_CORRECT -> "Deine Aussprache ist gut verständlich; verbessere die gelben Wörter."
            INCORRECT -> "Höre das langsame Audio an und nimm jedes Wort noch einmal auf."
        }
    }

    companion object {
        fun fromScore(scorePercentage: Int): PronunciationFeedbackStatus {
            return when {
                scorePercentage >= 85 -> CORRECT
                scorePercentage >= 60 -> ALMOST_CORRECT
                else -> INCORRECT
            }
        }
    }
}

data class WordAccuracy(
    val targetWord: String,
    val matchedSpokenWord: String?,
    val isCorrect: Boolean,
    val isPartiallyCorrect: Boolean = false,
    val tip: String? = null,
    val similarityPercentage: Int = when {
        isCorrect -> 100
        isPartiallyCorrect -> 74
        else -> 20
    }
) {
    val feedbackStatus: PronunciationFeedbackStatus
        get() = when {
            isCorrect -> PronunciationFeedbackStatus.CORRECT
            isPartiallyCorrect -> PronunciationFeedbackStatus.ALMOST_CORRECT
            else -> PronunciationFeedbackStatus.INCORRECT
        }
}

data class PronunciationResult(
    val scorePercentage: Int, // 0 - 100
    val recognizedText: String,
    val targetText: String,
    val wordAccuracies: List<WordAccuracy>,
    val feedbackMessage: String,
    val isPassed: Boolean,
    val xpEarned: Int,
    val coachTip: String,
    val fluencyScore: Int = ((scorePercentage * 0.95f) + 5).toInt().coerceIn(0, 100),
    val phonemeScore: Int = scorePercentage,
    val completenessScore: Int = if (wordAccuracies.isNotEmpty()) {
        ((wordAccuracies.count { it.isCorrect || it.isPartiallyCorrect }.toFloat() / wordAccuracies.size) * 100).toInt()
    } else 0,
    val feedbackStatus: PronunciationFeedbackStatus = PronunciationFeedbackStatus.fromScore(scorePercentage)
) {
    val correctWordsCount: Int
        get() = wordAccuracies.count { it.isCorrect }

    val almostCorrectWordsCount: Int
        get() = wordAccuracies.count { !it.isCorrect && it.isPartiallyCorrect }

    val incorrectWordsCount: Int
        get() = wordAccuracies.count { !it.isCorrect && !it.isPartiallyCorrect }
}

data class DialogueMessage(
    val sender: String, // "AI" or "USER"
    val avatarName: String,
    val targetText: String,
    val translation: String,
    val expectedUserReply: String? = null,
    val userSpokenReply: String? = null,
    val isCompleted: Boolean = false
)

data class DialogueScenario(
    val id: String,
    val language: Language,
    val title: String,
    val description: String,
    val situation: String,
    val turns: List<DialogueMessage>
)
