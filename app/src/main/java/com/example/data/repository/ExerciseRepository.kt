package com.example.data.repository

import com.example.data.model.DialogueMessage
import com.example.data.model.DialogueScenario
import com.example.data.model.Exercise
import com.example.data.model.ExerciseType
import com.example.data.model.Language
import com.example.data.model.LearningFocusGoal
import com.example.data.model.LessonModule

object ExerciseRepository {

    fun getModulesGroupedByLevel(language: Language): Map<Int, List<LessonModule>> {
        return getModulesForLanguage(language)
            .groupBy { it.requiredLevel }
            .toSortedMap()
    }

    /**
     * Returns the personalized list of lesson modules for the chosen target [language]
     * and [learningFocus] stored in Room.
     */
    fun getPersonalizedModulesForLanguage(
        language: Language,
        learningFocus: LearningFocusGoal = LearningFocusGoal.DAILY_CONVERSATION
    ): List<LessonModule> {
        val baseModules = getModulesForLanguage(language)
        return when (learningFocus) {
            LearningFocusGoal.DAILY_CONVERSATION -> baseModules
            LearningFocusGoal.TRAVEL_AND_CULTURE -> {
                baseModules.sortedWith(
                    compareBy<LessonModule> {
                        when (it.iconName) {
                            "flight", "restaurant" -> 0
                            "waving_hand" -> 1
                            else -> 2
                        }
                    }.thenBy { it.requiredLevel }
                )
            }
            LearningFocusGoal.NATIVE_PRONUNCIATION -> {
                baseModules.sortedWith(
                    compareBy<LessonModule> {
                        when (it.iconName) {
                            "local_fire_department" -> 0
                            "waving_hand" -> 1
                            else -> 2
                        }
                    }.thenBy { it.requiredLevel }
                )
            }
        }
    }

    fun getPersonalizedModulesGroupedByLevel(
        language: Language,
        learningFocus: LearningFocusGoal = LearningFocusGoal.DAILY_CONVERSATION
    ): Map<Int, List<LessonModule>> {
        return getPersonalizedModulesForLanguage(language, learningFocus)
            .groupBy { it.requiredLevel }
            .toSortedMap()
    }

    fun getModulesForLanguage(language: Language): List<LessonModule> {
        val exercises = getExercisesForLanguage(language)
        return when (language) {
            Language.ENGLISH -> listOf(
                LessonModule(
                    id = "module_en_1",
                    language = language,
                    title = "Saludos y Encuentros",
                    description = "Aprende a presentarte y saludar con acento natural.",
                    iconName = "waving_hand",
                    requiredLevel = 1,
                    totalExercises = 4,
                    exercises = exercises.filter { it.lessonId == "lesson_en_1" }
                ),
                LessonModule(
                    id = "module_en_1_2",
                    language = language,
                    title = "Cortesía y Conversación Diaria",
                    description = "Expresiones esenciales de bienestar y despedidas cordiales.",
                    iconName = "waving_hand",
                    requiredLevel = 1,
                    totalExercises = 2,
                    exercises = exercises.filter { it.lessonId == "lesson_en_1" }.takeLast(2)
                ),
                LessonModule(
                    id = "module_en_2",
                    language = language,
                    title = "En el Café y Restaurante",
                    description = "Ordena café, comida y pide la cuenta con fluidez.",
                    iconName = "restaurant",
                    requiredLevel = 2,
                    totalExercises = 4,
                    exercises = exercises.filter { it.lessonId == "lesson_en_2" }
                ),
                LessonModule(
                    id = "module_en_2_2",
                    language = language,
                    title = "Pedidos Especiales y Cuenta",
                    description = "Personaliza tu orden y agradece el servicio en inglés.",
                    iconName = "restaurant",
                    requiredLevel = 2,
                    totalExercises = 2,
                    exercises = exercises.filter { it.lessonId == "lesson_en_2" }.takeLast(2)
                ),
                LessonModule(
                    id = "module_en_3",
                    language = language,
                    title = "Viajes y Direcciones",
                    description = "Muévete por aeropuertos y pide ayuda en la ciudad.",
                    iconName = "flight",
                    requiredLevel = 3,
                    totalExercises = 3,
                    exercises = exercises.filter { it.lessonId == "lesson_en_3" }
                ),
                LessonModule(
                    id = "module_en_3_2",
                    language = language,
                    title = "Transporte y Estaciones",
                    description = "Compra boletos y consulta rutas hacia el centro.",
                    iconName = "flight",
                    requiredLevel = 3,
                    totalExercises = 2,
                    exercises = exercises.filter { it.lessonId == "lesson_en_3" }.takeLast(2)
                ),
                LessonModule(
                    id = "module_en_4",
                    language = language,
                    title = "Trabalenguas y Desafío Pro",
                    description = "Domina sonidos difíciles como 'th', 'sh' y 'r'.",
                    iconName = "local_fire_department",
                    requiredLevel = 4,
                    totalExercises = 3,
                    exercises = exercises.filter { it.lessonId == "lesson_en_4" }
                ),
                LessonModule(
                    id = "module_en_4_2",
                    language = language,
                    title = "Agilidad Fonética Avanzada",
                    description = "Entrena velocidad y precisión articular con trabalenguas.",
                    iconName = "local_fire_department",
                    requiredLevel = 4,
                    totalExercises = 2,
                    exercises = exercises.filter { it.lessonId == "lesson_en_4" }.takeLast(2)
                )
            )
            Language.FRENCH -> listOf(
                LessonModule(
                    id = "module_fr_1",
                    language = language,
                    title = "Premiers Pas",
                    description = "Saludos y cortesía francesa con pronunciación elegante.",
                    iconName = "waving_hand",
                    requiredLevel = 1,
                    totalExercises = 3,
                    exercises = exercises.filter { it.lessonId == "lesson_fr_1" }
                ),
                LessonModule(
                    id = "module_fr_2",
                    language = language,
                    title = "Au Bistro Parisien",
                    description = "Práctica de sonidos nasales y pedidos en cafeterías.",
                    iconName = "restaurant",
                    requiredLevel = 2,
                    totalExercises = 3,
                    exercises = exercises.filter { it.lessonId == "lesson_fr_2" }
                ),
                LessonModule(
                    id = "module_fr_3",
                    language = language,
                    title = "Voyage à Paris",
                    description = "Direcciones en el metro, museos y estaciones de tren.",
                    iconName = "flight",
                    requiredLevel = 3,
                    totalExercises = 2,
                    exercises = exercises.filter { it.lessonId == "lesson_fr_3" }
                ),
                LessonModule(
                    id = "module_fr_4",
                    language = language,
                    title = "Phonétique & Virelangues",
                    description = "Domina la R francesa y las vocales nasales avanzadas.",
                    iconName = "local_fire_department",
                    requiredLevel = 4,
                    totalExercises = 2,
                    exercises = exercises.filter { it.lessonId == "lesson_fr_4" }
                )
            )
            Language.GERMAN -> listOf(
                LessonModule(
                    id = "module_de_1",
                    language = language,
                    title = "Hallo und Willkommen",
                    description = "Primeras frases y articulación clara en alemán.",
                    iconName = "waving_hand",
                    requiredLevel = 1,
                    totalExercises = 3,
                    exercises = exercises.filter { it.lessonId == "lesson_de_1" }
                ),
                LessonModule(
                    id = "module_de_2",
                    language = language,
                    title = "Im Restaurant & Markt",
                    description = "Expresiones cotidianas con umlauts (ä, ö, ü).",
                    iconName = "restaurant",
                    requiredLevel = 2,
                    totalExercises = 3,
                    exercises = exercises.filter { it.lessonId == "lesson_de_2" }
                ),
                LessonModule(
                    id = "module_de_3",
                    language = language,
                    title = "Reisen in Berlin",
                    description = "Estaciones de tren, boletos y direcciones urbanas.",
                    iconName = "flight",
                    requiredLevel = 3,
                    totalExercises = 2,
                    exercises = exercises.filter { it.lessonId == "lesson_de_3" }
                ),
                LessonModule(
                    id = "module_de_4",
                    language = language,
                    title = "Zungenbrecher Pro",
                    description = "Agilidad articulatoria con sonidos 'sch', 'ch' y 'sp'.",
                    iconName = "local_fire_department",
                    requiredLevel = 4,
                    totalExercises = 2,
                    exercises = exercises.filter { it.lessonId == "lesson_de_4" }
                )
            )
            Language.ITALIAN -> listOf(
                LessonModule(
                    id = "module_it_1",
                    language = language,
                    title = "Primi Passi",
                    description = "Entonación italiana viva y sonidos vocálicos claros.",
                    iconName = "waving_hand",
                    requiredLevel = 1,
                    totalExercises = 3,
                    exercises = exercises.filter { it.lessonId == "lesson_it_1" }
                ),
                LessonModule(
                    id = "module_it_2",
                    language = language,
                    title = "Al Ristorante Italiano",
                    description = "Pide pasta, café espresso y postres con encanto.",
                    iconName = "restaurant",
                    requiredLevel = 2,
                    totalExercises = 3,
                    exercises = exercises.filter { it.lessonId == "lesson_it_2" }
                ),
                LessonModule(
                    id = "module_it_3",
                    language = language,
                    title = "Viaggio a Roma e Firenze",
                    description = "Pregunta por monumentos, trenes y plazas históricas.",
                    iconName = "flight",
                    requiredLevel = 3,
                    totalExercises = 2,
                    exercises = exercises.filter { it.lessonId == "lesson_it_3" }
                ),
                LessonModule(
                    id = "module_it_4",
                    language = language,
                    title = "Scioglilingua & Dizione",
                    description = "Domina consonantes dobles y los sonidos 'gli' y 'gn'.",
                    iconName = "local_fire_department",
                    requiredLevel = 4,
                    totalExercises = 2,
                    exercises = exercises.filter { it.lessonId == "lesson_it_4" }
                )
            )
            Language.PORTUGUESE -> listOf(
                LessonModule(
                    id = "module_pt_1",
                    language = language,
                    title = "Primeiros Passos",
                    description = "Sons nasais e ritmo melódico brasileiro.",
                    iconName = "waving_hand",
                    requiredLevel = 1,
                    totalExercises = 3,
                    exercises = exercises.filter { it.lessonId == "lesson_pt_1" }
                ),
                LessonModule(
                    id = "module_pt_2",
                    language = language,
                    title = "Na Praia e na Cidade",
                    description = "Peça sucos, comida típica e converse com locais.",
                    iconName = "restaurant",
                    requiredLevel = 2,
                    totalExercises = 3,
                    exercises = exercises.filter { it.lessonId == "lesson_pt_2" }
                ),
                LessonModule(
                    id = "module_pt_3",
                    language = language,
                    title = "Viagem pelo Brasil",
                    description = "Aeroportos, transporte e passeios turísticos.",
                    iconName = "flight",
                    requiredLevel = 3,
                    totalExercises = 2,
                    exercises = exercises.filter { it.lessonId == "lesson_pt_3" }
                ),
                LessonModule(
                    id = "module_pt_4",
                    language = language,
                    title = "Trava-Línguas & Fluência",
                    description = "Domine sons nasais (-ão, -em) e ritmo acelerado.",
                    iconName = "local_fire_department",
                    requiredLevel = 4,
                    totalExercises = 2,
                    exercises = exercises.filter { it.lessonId == "lesson_pt_4" }
                )
            )
            Language.SPANISH -> listOf(
                LessonModule(
                    id = "module_es_1",
                    language = language,
                    title = "Saludos y Presentaciones",
                    description = "Saluda, preséntate y conversa en español con vocales claras.",
                    iconName = "waving_hand",
                    requiredLevel = 1,
                    totalExercises = 4,
                    exercises = exercises.filter { it.lessonId == "lesson_es_1" }
                ),
                LessonModule(
                    id = "module_es_1_2",
                    language = language,
                    title = "Preguntas y Respuestas Diarias",
                    description = "Responde preguntas frecuentes hablando en el micrófono.",
                    iconName = "waving_hand",
                    requiredLevel = 1,
                    totalExercises = 2,
                    exercises = exercises.filter { it.lessonId == "lesson_es_1" }.takeLast(2)
                ),
                LessonModule(
                    id = "module_es_2",
                    language = language,
                    title = "Tapas, Café y Restaurante",
                    description = "Pide comida, café y la cuenta en España y Latinoamérica.",
                    iconName = "restaurant",
                    requiredLevel = 2,
                    totalExercises = 3,
                    exercises = exercises.filter { it.lessonId == "lesson_es_2" }
                ),
                LessonModule(
                    id = "module_es_3",
                    language = language,
                    title = "Viajes, Hotel y Direcciones",
                    description = "Pregunta direcciones, transporte y reservas en español.",
                    iconName = "flight",
                    requiredLevel = 3,
                    totalExercises = 3,
                    exercises = exercises.filter { it.lessonId == "lesson_es_3" }
                ),
                LessonModule(
                    id = "module_es_4",
                    language = language,
                    title = "Trabalenguas: RR y Sonido Ñ",
                    description = "Domina la vibrante múltiple RR y el sonido palatal Ñ.",
                    iconName = "local_fire_department",
                    requiredLevel = 4,
                    totalExercises = 2,
                    exercises = exercises.filter { it.lessonId == "lesson_es_4" }
                )
            )
        }
    }

    fun getExercisesForLanguage(language: Language): List<Exercise> {
        return when (language) {
            Language.ENGLISH -> listOf(
                Exercise(
                    id = "en_1_1",
                    language = language,
                    lessonId = "lesson_en_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pronuncia esta frase para saludar:",
                    targetPhrase = "Hello, nice to meet you!",
                    translation = "¡Hola, un gusto conocerte!",
                    phoneticGuide = "/həˈloʊ, naɪs tu miːt juː/",
                    pronunciationTip = "Asegúrate de aspirar suavemente la 'H' al inicio de 'Hello'.",
                    xpReward = 15
                ),
                Exercise(
                    id = "en_1_2",
                    language = language,
                    lessonId = "lesson_en_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pregunta cómo está la otra persona:",
                    targetPhrase = "How are you doing today?",
                    translation = "¿Cómo estás hoy?",
                    phoneticGuide = "/haʊ ɑːr juː ˈduːɪŋ təˈdeɪ/",
                    pronunciationTip = "Une 'How are' de forma fluida: suena como 'Haw-ahr'.",
                    xpReward = 15
                ),
                Exercise(
                    id = "en_1_3",
                    language = language,
                    lessonId = "lesson_en_1",
                    type = ExerciseType.FILL_IN_BLANK_SPEAK,
                    promptQuestion = "Di la frase completa diciendo tu bienestar:",
                    targetPhrase = "I am doing great, thank you!",
                    translation = "¡Estoy genial, muchas gracias!",
                    phoneticGuide = "/aɪ æm ˈduːɪŋ ɡreɪt, θæŋk juː/",
                    pronunciationTip = "Para 'thank', coloca la punta de la lengua entre los dientes.",
                    xpReward = 20
                ),
                Exercise(
                    id = "en_1_4",
                    language = language,
                    lessonId = "lesson_en_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Despídete cordialmente:",
                    targetPhrase = "Have a wonderful day ahead!",
                    translation = "¡Que tengas un día maravilloso!",
                    phoneticGuide = "/hæv ə ˈwʌndərfəl deɪ əˈhɛd/",
                    pronunciationTip = "En 'wonderful', la 'w' se pronuncia redondeando los labios suavemente.",
                    xpReward = 20
                ),
                Exercise(
                    id = "en_2_1",
                    language = language,
                    lessonId = "lesson_en_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pide un café cortésmente:",
                    targetPhrase = "Could I have a hot coffee, please?",
                    translation = "¿Podría darme un café caliente, por favor?",
                    phoneticGuide = "/kʊd aɪ hæv ə hɒt ˈkɔːfi, pliːz/",
                    pronunciationTip = "En 'Could', la letra 'l' es muda, suena como 'Kud'.",
                    xpReward = 15
                ),
                Exercise(
                    id = "en_2_2",
                    language = language,
                    lessonId = "lesson_en_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Elige leche de almendras:",
                    targetPhrase = "With almond milk and no sugar.",
                    translation = "Con leche de almendras y sin azúcar.",
                    phoneticGuide = "/wɪð ˈɑːmənd mɪlk ænd noʊ ˈʃʊɡər/",
                    pronunciationTip = "En 'sugar', la 's' suena como 'sh' (/ʃ/).",
                    xpReward = 15
                ),
                Exercise(
                    id = "en_2_3",
                    language = language,
                    lessonId = "lesson_en_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pide la cuenta:",
                    targetPhrase = "Can we get the check, please?",
                    translation = "¿Nos puede traer la cuenta, por favor?",
                    phoneticGuide = "/kæn wiː ɡɛt ðə tʃɛk, pliːz/",
                    pronunciationTip = "Pronuncia 'check' con un sonido claro 'ch' al inicio.",
                    xpReward = 15
                ),
                Exercise(
                    id = "en_2_4",
                    language = language,
                    lessonId = "lesson_en_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Agradece por el servicio:",
                    targetPhrase = "Everything was absolutely delicious!",
                    translation = "¡Todo estuvo absolutamente delicioso!",
                    phoneticGuide = "/ˈɛvriˌθɪŋ wəz ˌæbsəˈluːtli dɪˈlɪʃəs/",
                    pronunciationTip = "Acentúa la segunda sílaba en 'de-LI-cious'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "en_3_1",
                    language = language,
                    lessonId = "lesson_en_3",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pregunta en el aeropuerto:",
                    targetPhrase = "Where is the boarding gate?",
                    translation = "¿Dónde está la puerta de embarque?",
                    phoneticGuide = "/wɛər ɪz ðə ˈbɔːrdɪŋ ɡeɪt/",
                    pronunciationTip = "Une 'Where is' como 'Wehr-iz'.",
                    xpReward = 15
                ),
                Exercise(
                    id = "en_3_2",
                    language = language,
                    lessonId = "lesson_en_3",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pide indicaciones en la calle:",
                    targetPhrase = "Excuse me, which way to the station?",
                    translation = "Disculpe, ¿por dónde se va a la estación?",
                    phoneticGuide = "/ɪkˈskjuːz miː, wɪtʃ weɪ tuː ðə ˈsteɪʃən/",
                    pronunciationTip = "Sonido suave 'z' al final de 'Excuse'.",
                    xpReward = 15
                ),
                Exercise(
                    id = "en_3_3",
                    language = language,
                    lessonId = "lesson_en_3",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pregunta por el precio de un billete:",
                    targetPhrase = "How much is a ticket to downtown?",
                    translation = "¿Cuánto cuesta un boleto al centro?",
                    phoneticGuide = "/haʊ mʌtʃ ɪz ə ˈtɪkɪt tuː ˈdaʊnˌtaʊn/",
                    pronunciationTip = "Enfatiza 'down-town' con ritmo claro.",
                    xpReward = 20
                ),
                Exercise(
                    id = "en_4_1",
                    language = language,
                    lessonId = "lesson_en_4",
                    type = ExerciseType.TONGUE_TWISTER,
                    promptQuestion = "🔥 Desafío Trabalenguas - Sonido TH:",
                    targetPhrase = "Three free throws for the team.",
                    translation = "Tres tiros libres para el equipo.",
                    phoneticGuide = "/θriː friː θroʊz fɔːr ðə tiːm/",
                    pronunciationTip = "Alterna entre 'th' (lengua en dientes) y 'f' (labio inferior).",
                    xpReward = 30
                ),
                Exercise(
                    id = "en_4_2",
                    language = language,
                    lessonId = "lesson_en_4",
                    type = ExerciseType.TONGUE_TWISTER,
                    promptQuestion = "🔥 Desafío Trabalenguas - Sonido S y SH:",
                    targetPhrase = "She sells seashells on the seashore.",
                    translation = "Ella vende conchas marinas en la orilla del mar.",
                    phoneticGuide = "/ʃiː sɛlz ˈsiːˌʃɛlz ɒn ðə ˈsiːˌʃɔːr/",
                    pronunciationTip = "Contrasta bien 'sh' (soplo suave) con 's' (silbido agudo).",
                    xpReward = 30
                ),
                Exercise(
                    id = "en_4_3",
                    language = language,
                    lessonId = "lesson_en_4",
                    type = ExerciseType.TONGUE_TWISTER,
                    promptQuestion = "🔥 Desafío Velocidad de articulación:",
                    targetPhrase = "Red leather, yellow leather, red leather.",
                    translation = "Cuero rojo, cuero amarillo, cuero rojo.",
                    phoneticGuide = "/rɛd ˈlɛðər, ˈjɛloʊ ˈlɛðər, rɛd ˈlɛðər/",
                    pronunciationTip = "Entrena la agilidad de tu lengua tocando el paladar superior.",
                    xpReward = 35
                )
            )

            Language.FRENCH -> listOf(
                Exercise(
                    id = "fr_1_1",
                    language = language,
                    lessonId = "lesson_fr_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Saluda cordialmente:",
                    targetPhrase = "Bonjour, enchanté de faire votre connaissance.",
                    translation = "Buenos días, encantado de conocerle.",
                    phoneticGuide = "/bɔ̃.ʒuʁ, ɑ̃.ʃɑ̃.te də fɛʁ vɔtʁ kɔ.nɛ.sɑ̃s/",
                    pronunciationTip = "La 'r' francesa se articula suavemente desde el fondo del paladar.",
                    xpReward = 15
                ),
                Exercise(
                    id = "fr_1_2",
                    language = language,
                    lessonId = "lesson_fr_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pregunta cómo está:",
                    targetPhrase = "Comment allez-vous aujourd'hui ?",
                    translation = "¿Cómo está usted hoy?",
                    phoneticGuide = "/kɔ.mɑ̃.t‿a.le vu o.ʒuʁ.dɥi/",
                    pronunciationTip = "Haz la liaison entre 'Comment' y 'allez': suena 'koman-tale-vu'.",
                    xpReward = 15
                ),
                Exercise(
                    id = "fr_1_3",
                    language = language,
                    lessonId = "lesson_fr_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Agradece amablemente:",
                    targetPhrase = "Merci beaucoup pour votre aide !",
                    translation = "¡Muchas gracias por su ayuda!",
                    phoneticGuide = "/mɛʁ.si bo.ku puʁ vɔtʁ ɛd/",
                    pronunciationTip = "En 'beaucoup', la 'p' final es completamente silenciosa.",
                    xpReward = 20
                ),
                Exercise(
                    id = "fr_2_1",
                    language = language,
                    lessonId = "lesson_fr_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pide en un café parisino:",
                    targetPhrase = "Un croissant et un café s'il vous plaît.",
                    translation = "Un croissant y un café, por favor.",
                    phoneticGuide = "/œ̃ kʁwa.sɑ̃ e œ̃ ka.fe sil vu plɛ/",
                    pronunciationTip = "El sonido 'un' es nasal: deja salir el aire por la nariz.",
                    xpReward = 20
                ),
                Exercise(
                    id = "fr_2_2",
                    language = language,
                    lessonId = "lesson_fr_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pregunta por la cuenta:",
                    targetPhrase = "L'addition, s'il vous plaît !",
                    translation = "¡La cuenta, por favor!",
                    phoneticGuide = "/la.di.sjɔ̃ sil vu plɛ/",
                    pronunciationTip = "La terminación '-tion' suena como '-sió' nasal.",
                    xpReward = 15
                ),
                Exercise(
                    id = "fr_2_3",
                    language = language,
                    lessonId = "lesson_fr_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Expresa satisfacción:",
                    targetPhrase = "C'était vraiment délicieux, merci !",
                    translation = "¡Estuvo realmente delicioso, gracias!",
                    phoneticGuide = "/s‿e.tɛ vʁɛ.mɑ̃ de.li.sjø mɛʁ.si/",
                    pronunciationTip = "Redondea los labios para 'délicieux'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "fr_3_1",
                    language = language,
                    lessonId = "lesson_fr_3",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pregunta por la estación de metro:",
                    targetPhrase = "Excusez-moi, où est la station de métro ?",
                    translation = "Disculpe, ¿dónde está la estación de metro?",
                    phoneticGuide = "/ɛk.sky.ze mwa, u ɛ la sta.sjɔ̃ də me.tʁo/",
                    pronunciationTip = "Enlaza 'où est' suavemente como 'u-e'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "fr_3_2",
                    language = language,
                    lessonId = "lesson_fr_3",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Compra una entrada para el museo:",
                    targetPhrase = "Je voudrais deux billets pour le musée.",
                    translation = "Quisiera dos entradas para el museo.",
                    phoneticGuide = "/ʒə vu.dʁɛ dø bi.jɛ puʁ lə my.ze/",
                    pronunciationTip = "En 'billets', la doble 'll' suena como una 'y' suave.",
                    xpReward = 20
                ),
                Exercise(
                    id = "fr_4_1",
                    language = language,
                    lessonId = "lesson_fr_4",
                    type = ExerciseType.TONGUE_TWISTER,
                    promptQuestion = "🔥 Desafío Trabalenguas Francés:",
                    targetPhrase = "Un chasseur sachant chasser sans son chien.",
                    translation = "Un cazador que sabe cazar sin su perro.",
                    phoneticGuide = "/œ̃ ʃa.sœʁ sa.ʃɑ̃ ʃa.se sɑ̃ sɔ̃ ʃjɛ̃/",
                    pronunciationTip = "Alterna con precisión entre 'ch' (/ʃ/) y 's' (/s/).",
                    xpReward = 30
                ),
                Exercise(
                    id = "fr_4_2",
                    language = language,
                    lessonId = "lesson_fr_4",
                    type = ExerciseType.TONGUE_TWISTER,
                    promptQuestion = "🔥 Desafío Vocales Nasales:",
                    targetPhrase = "Cinq chiens chassent six chats.",
                    translation = "Cinco perros cazan seis gatos.",
                    phoneticGuide = "/sɛ̃k ʃjɛ̃ ʃas si ʃa/",
                    pronunciationTip = "Mantén el flujo de aire constante en las consonantes fricativas.",
                    xpReward = 30
                )
            )

            Language.GERMAN -> listOf(
                Exercise(
                    id = "de_1_1",
                    language = language,
                    lessonId = "lesson_de_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Saluda en alemán:",
                    targetPhrase = "Guten Tag, wie geht es Ihnen?",
                    translation = "Buenos días, ¿cómo está usted?",
                    phoneticGuide = "/ˌɡuːtn̩ ˈtaːk, viː ɡeːt ɛs ˈiːnən/",
                    pronunciationTip = "La 'g' final en 'Tag' suena similar a una 'k'.",
                    xpReward = 15
                ),
                Exercise(
                    id = "de_1_2",
                    language = language,
                    lessonId = "lesson_de_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Preséntate:",
                    targetPhrase = "Ich freue mich, Sie kennenzulernen.",
                    translation = "Me alegro de conocerle.",
                    phoneticGuide = "/ɪç ˈfʁɔɪ̯ə mɪç, ziː ˈkɛnən.tsuːˌlɛʁnən/",
                    pronunciationTip = "El sonido 'ch' en 'Ich' es suave como una brisa entre dientes.",
                    xpReward = 20
                ),
                Exercise(
                    id = "de_1_3",
                    language = language,
                    lessonId = "lesson_de_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Agradece cordialmente:",
                    targetPhrase = "Vielen Dank für Ihre Hilfe!",
                    translation = "¡Muchas gracias por su ayuda!",
                    phoneticGuide = "/ˈfiːlən daŋk fyːr ˈiːʁə ˈhɪlfə/",
                    pronunciationTip = "La 'V' en 'Vielen' suena como una 'F' española.",
                    xpReward = 15
                ),
                Exercise(
                    id = "de_2_1",
                    language = language,
                    lessonId = "lesson_de_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pide agua en el restaurante:",
                    targetPhrase = "Ich möchte bitte ein Glas Wasser.",
                    translation = "Quisiera por favor un vaso de agua.",
                    phoneticGuide = "/ɪç ˈmœçtə ˈbɪtə aɪn ɡlaːs ˈvasɐ/",
                    pronunciationTip = "Para 'ö' en 'möchte', redondea los labios pronunciando 'e'.",
                    xpReward = 15
                ),
                Exercise(
                    id = "de_2_2",
                    language = language,
                    lessonId = "lesson_de_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pide la cuenta en alemán:",
                    targetPhrase = "Die Rechnung, bitte schön!",
                    translation = "¡La cuenta, por favor!",
                    phoneticGuide = "/diː ˈʁɛçnʊŋ ˈbɪtə ʃøːn/",
                    pronunciationTip = "Pronuncia 'schön' con labios redondeados y tono amable.",
                    xpReward = 20
                ),
                Exercise(
                    id = "de_2_3",
                    language = language,
                    lessonId = "lesson_de_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pregunta por la estación:",
                    targetPhrase = "Entschuldigung, wo ist der Bahnhof?",
                    translation = "Disculpe, ¿dónde está la estación?",
                    phoneticGuide = "/ɛntˈʃʊldɪɡʊŋ, voː ɪst deːɐ̯ ˈbaːnˌhoːf/",
                    pronunciationTip = "Articula con claridad cada sílaba en 'Ent-schul-di-gung'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "de_3_1",
                    language = language,
                    lessonId = "lesson_de_3",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Compra un boleto de tren en Berlín:",
                    targetPhrase = "Eine Fahrkarte nach Berlin, bitte.",
                    translation = "Un boleto hacia Berlín, por favor.",
                    phoneticGuide = "/ˈaɪ.nə ˈfaːɐ̯ˌkaʁ.tə naːx bɛʁˈliːn ˈbɪ.tə/",
                    pronunciationTip = "En 'nach', la 'ch' se pronuncia desde la garganta suave.",
                    xpReward = 20
                ),
                Exercise(
                    id = "de_3_2",
                    language = language,
                    lessonId = "lesson_de_3",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pregunta a qué hora sale el tren:",
                    targetPhrase = "Wann fährt der nächste Zug ab?",
                    translation = "¿Cuándo sale el próximo tren?",
                    phoneticGuide = "/van fɛːɐ̯t deːɐ̯ ˈnɛːç.stə tsuːk ap/",
                    pronunciationTip = "La 'Z' en 'Zug' suena fuerte como 'ts'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "de_4_1",
                    language = language,
                    lessonId = "lesson_de_4",
                    type = ExerciseType.TONGUE_TWISTER,
                    promptQuestion = "🔥 Desafío Trabalenguas Alemán:",
                    targetPhrase = "Fischers Fritze fischt frische Fische.",
                    translation = "Fritz el pescador pesca pescado fresco.",
                    phoneticGuide = "/ˈfɪ.ʃɐs ˈfʁɪ.tsə fɪʃt ˈfʁɪ.ʃə ˈfɪ.ʃə/",
                    pronunciationTip = "Articula con nitidez el grupo 'fr' y 'sch'.",
                    xpReward = 30
                ),
                Exercise(
                    id = "de_4_2",
                    language = language,
                    lessonId = "lesson_de_4",
                    type = ExerciseType.TONGUE_TWISTER,
                    promptQuestion = "🔥 Desafío Sonido SCH y ST:",
                    targetPhrase = "Zehn zahme Ziegen zogen zehn Zentner Zucker.",
                    translation = "Diez cabras mansas tiraron diez quintales de azúcar.",
                    phoneticGuide = "/tseːn ˈtsaː.mə ˈtsiː.ɡn̩ ˈtsoː.ɡn̩ tseːn ˈtsɛnt.nɐ ˈtsʊ.kɐ/",
                    pronunciationTip = "Haz sonar cada 'Z' inicial con ataque dental 'ts'.",
                    xpReward = 30
                )
            )

            Language.ITALIAN -> listOf(
                Exercise(
                    id = "it_1_1",
                    language = language,
                    lessonId = "lesson_it_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Saluda con calidez italiana:",
                    targetPhrase = "Ciao, piacere di conoscerti!",
                    translation = "¡Hola, un placer conocerte!",
                    phoneticGuide = "/ˈtʃaː.o, pjaˈtʃe.re di koˈnoʃ.ʃer.ti/",
                    pronunciationTip = "'Ciao' se pronuncia 'Chao' con energía.",
                    xpReward = 15
                ),
                Exercise(
                    id = "it_1_2",
                    language = language,
                    lessonId = "lesson_it_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pregunta cómo está todo:",
                    targetPhrase = "Come stai? Tutto bene oggi?",
                    translation = "¿Cómo estás? ¿Todo bien hoy?",
                    phoneticGuide = "/ˈko.me ˈstai, ˈtut.to ˈbɛ.ne ˈɔd.dʒi/",
                    pronunciationTip = "Haz sonar la doble 'tt' en 'Tutto' con una pausa breve.",
                    xpReward = 15
                ),
                Exercise(
                    id = "it_1_3",
                    language = language,
                    lessonId = "lesson_it_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Agradece calurosamente:",
                    targetPhrase = "Grazie mille per il tuo aiuto!",
                    translation = "¡Muchísimas gracias por tu ayuda!",
                    phoneticGuide = "/ˈɡrat.tsje ˈmil.le per il ˈtu.o aˈju.to/",
                    pronunciationTip = "La 'z' en 'Grazie' suena vibrante como 'ts'.",
                    xpReward = 15
                ),
                Exercise(
                    id = "it_2_1",
                    language = language,
                    lessonId = "lesson_it_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pide una pizza:",
                    targetPhrase = "Vorrei una pizza margherita, per favore.",
                    translation = "Quisiera una pizza margarita, por favor.",
                    phoneticGuide = "/vorˈrɛi ˈu.na ˈpit.tsa marɡeˈri.ta, per faˈvo.re/",
                    pronunciationTip = "Haz vibrar la doble 'rr' en 'Vorrei'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "it_2_2",
                    language = language,
                    lessonId = "lesson_it_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pide la cuenta:",
                    targetPhrase = "Il conto, per favore!",
                    translation = "¡La cuenta, por favor!",
                    phoneticGuide = "/il ˈkon.to, per faˈvo.re/",
                    pronunciationTip = "Sonido vocal limpio y abierto en 'con-to'.",
                    xpReward = 15
                ),
                Exercise(
                    id = "it_2_3",
                    language = language,
                    lessonId = "lesson_it_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Elogia la comida:",
                    targetPhrase = "La cucina italiana è buonissima!",
                    translation = "¡La comida italiana es buenísima!",
                    phoneticGuide = "/la kuˈtʃi.na itaˈlja.na ɛ bwoˈnis.si.ma/",
                    pronunciationTip = "Enfatiza con entusiasmo la doble 'ss' en 'buonissima'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "it_3_1",
                    language = language,
                    lessonId = "lesson_it_3",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pregunta cómo llegar al Coliseo:",
                    targetPhrase = "Scusi, per andare al Colosseo?",
                    translation = "Disculpe, ¿para ir al Coliseo?",
                    phoneticGuide = "/ˈsku.zi, per anˈda.re al kolosˈsɛ.o/",
                    pronunciationTip = "Mantén una entonación ascendente al final de la pregunta.",
                    xpReward = 20
                ),
                Exercise(
                    id = "it_3_2",
                    language = language,
                    lessonId = "lesson_it_3",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Compra un billete de tren a Florencia:",
                    targetPhrase = "Un biglietto per Firenze, grazie.",
                    translation = "Un boleto para Florencia, gracias.",
                    phoneticGuide = "/un biʎˈʎet.to per fiˈrɛn.tse ˈɡrat.tsje/",
                    pronunciationTip = "El grupo 'gli' en 'biglietto' suena palatal como 'll' suave.",
                    xpReward = 20
                ),
                Exercise(
                    id = "it_4_1",
                    language = language,
                    lessonId = "lesson_it_4",
                    type = ExerciseType.TONGUE_TWISTER,
                    promptQuestion = "🔥 Desafío Trabalenguas Italiano:",
                    targetPhrase = "Trentatré trentini entrarono a Trento.",
                    translation = "Treinta y tres trentinos entraron a Trento.",
                    phoneticGuide = "/trentaˈtre trenˈti.ni enˈtra.ro.no a ˈtren.to/",
                    pronunciationTip = "Haz vibrar con fuerza cada grupo 'tr' sin perder el ritmo.",
                    xpReward = 30
                ),
                Exercise(
                    id = "it_4_2",
                    language = language,
                    lessonId = "lesson_it_4",
                    type = ExerciseType.TONGUE_TWISTER,
                    promptQuestion = "🔥 Desafío Consonantes Dobles:",
                    targetPhrase = "Sopra la panca la capra campa.",
                    translation = "Sobre la banca la cabra vive.",
                    phoneticGuide = "/ˈso.pra la ˈpaŋ.ka la ˈka.pra ˈkam.pa/",
                    pronunciationTip = "Abre bien las vocales 'a' y 'o' al más puro estilo italiano.",
                    xpReward = 30
                )
            )

            Language.PORTUGUESE -> listOf(
                Exercise(
                    id = "pt_1_1",
                    language = language,
                    lessonId = "lesson_pt_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Saluda cordialmente:",
                    targetPhrase = "Olá, tudo bem com você?",
                    translation = "Hola, ¿todo bien contigo?",
                    phoneticGuide = "/oˈla, ˈtu.du bẽj kõ voˈse/",
                    pronunciationTip = "'Tudo bem' termina en una suave resonancia nasal.",
                    xpReward = 15
                ),
                Exercise(
                    id = "pt_1_2",
                    language = language,
                    lessonId = "lesson_pt_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Preséntate:",
                    targetPhrase = "Muito prazer em conhecê-lo!",
                    translation = "¡Mucho gusto en conocerte!",
                    phoneticGuide = "/ˈmũj.tu pɾaˈzeɾ ẽj ko.ɲeˈse.lu/",
                    pronunciationTip = "En 'muito', la 'ui' es fuertemente nasal: 'muin-tu'.",
                    xpReward = 15
                ),
                Exercise(
                    id = "pt_1_3",
                    language = language,
                    lessonId = "lesson_pt_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Agradece:",
                    targetPhrase = "Muito obrigado pela sua atenção!",
                    translation = "¡Muchas gracias por su atención!",
                    phoneticGuide = "/ˈmũj.tu o.bɾiˈɡa.du ˈpɛ.lɐ ˈsu.ɐ a.tẽˈsɐ̃w/",
                    pronunciationTip = "El diptongo final '-ão' suena como '-ang' por la nariz.",
                    xpReward = 20
                ),
                Exercise(
                    id = "pt_2_1",
                    language = language,
                    lessonId = "lesson_pt_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pide un jugo tropical:",
                    targetPhrase = "Eu quero um suco de maracujá, por favor.",
                    translation = "Quiero un jugo de maracuyá, por favor.",
                    phoneticGuide = "/ew ˈkɛ.ɾu ũ ˈsu.ku dʒi ma.ɾa.kuˈʒa puʁ faˈvoʁ/",
                    pronunciationTip = "La 'de' en portugués de Brasil suena suavemente como 'dji'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "pt_2_2",
                    language = language,
                    lessonId = "lesson_pt_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pide la cuenta:",
                    targetPhrase = "A conta, por gentileza!",
                    translation = "¡La cuenta, por favor!",
                    phoneticGuide = "/a ˈkõ.tɐ, poʁ ʒẽ.tʃiˈle.zɐ/",
                    pronunciationTip = "En 'gentileza', la 'ti' suena como 'tchi'.",
                    xpReward = 15
                ),
                Exercise(
                    id = "pt_2_3",
                    language = language,
                    lessonId = "lesson_pt_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Despídete amistosamente:",
                    targetPhrase = "Até logo, tenha um ótimo dia!",
                    translation = "¡Hasta luego, que tengas un excelente día!",
                    phoneticGuide = "/aˈtɛ ˈlɔ.ɡu, ˈtẽ.ɲɐ ũ ˈɔ.tʃi.mu ˈdʒi.ɐ/",
                    pronunciationTip = "Acentúa abiertamente 'ó-timo'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "pt_3_1",
                    language = language,
                    lessonId = "lesson_pt_3",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pregunta cómo llegar a la playa:",
                    targetPhrase = "Com licença, como chego à praia de Ipanema?",
                    translation = "Con permiso, ¿cómo llego a la playa de Ipanema?",
                    phoneticGuide = "/kõ liˈsẽ.sɐ, ˈko.mu ˈʃe.ɡu a ˈpɾa.jɐ dʒi i.paˈnẽ.mɐ/",
                    pronunciationTip = "La 'ch' en 'chego' suena suave como 'sh' (/ʃ/).",
                    xpReward = 20
                ),
                Exercise(
                    id = "pt_3_2",
                    language = language,
                    lessonId = "lesson_pt_3",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pide información en el aeropuerto:",
                    targetPhrase = "Onde fica o portão de embarque?",
                    translation = "¿Dónde queda la puerta de embarque?",
                    phoneticGuide = "/ˈõ.dʒi ˈfi.kɐ u poʁˈtɐ̃w dʒi ẽˈbaʁ.ki/",
                    pronunciationTip = "Pronuncia 'Onde' y 'de' con el sonido palatal 'dji'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "pt_4_1",
                    language = language,
                    lessonId = "lesson_pt_4",
                    type = ExerciseType.TONGUE_TWISTER,
                    promptQuestion = "🔥 Desafío Trabalenguas Portugués:",
                    targetPhrase = "O rato roeu a roupa do rei de Roma.",
                    translation = "El ratón royó la ropa del rey de Roma.",
                    phoneticGuide = "/u ˈʁa.tu ʁoˈew a ˈʁo.pɐ du ˈʁej dʒi ˈʁo.mɐ/",
                    pronunciationTip = "En portugués brasileño, la 'r' inicial suena aspirada como una 'j' suave.",
                    xpReward = 30
                ),
                Exercise(
                    id = "pt_4_2",
                    language = language,
                    lessonId = "lesson_pt_4",
                    type = ExerciseType.TONGUE_TWISTER,
                    promptQuestion = "🔥 Desafío Ritmo Brasileño:",
                    targetPhrase = "Três pratos de trigo para três tigres tristes.",
                    translation = "Tres platos de trigo para tres tigres tristes.",
                    phoneticGuide = "/tɾejs ˈpɾa.tus dʒi ˈtɾi.ɡu ˈpa.ɾɐ tɾejs ˈtʃi.ɡɾis ˈtɾis.tʃis/",
                    pronunciationTip = "Observa cómo 'ti-gres' y 'tris-tes' palatalizan la 't' antes de 'i'.",
                    xpReward = 30
                )
            )
            Language.SPANISH -> listOf(
                Exercise(
                    id = "es_1_1",
                    language = language,
                    lessonId = "lesson_es_1",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Saluda cordialmente en español:",
                    targetPhrase = "¡Hola! Mucho gusto en conocerte.",
                    translation = "Hello! Nice to meet you.",
                    phoneticGuide = "/ˈo.la ˈmu.tʃo ˈɡus.to en ko.noˈθeɾ.te/",
                    pronunciationTip = "La 'H' en 'Hola' es completamente muda; abre bien las vocales O y A.",
                    xpReward = 15
                ),
                Exercise(
                    id = "es_1_2",
                    language = language,
                    lessonId = "lesson_es_1",
                    type = ExerciseType.DIALOGUE_REPLY,
                    promptQuestion = "La app pregunta: «¿Cómo estás hoy?» — Responde en el micrófono:",
                    targetPhrase = "Estoy muy bien, muchas gracias.",
                    translation = "I am doing very well, thank you very much.",
                    phoneticGuide = "/esˈtoj muj ˈβjen, ˈmu.tʃas ˈɣɾa.sjas/",
                    pronunciationTip = "Pronuncia claramente 'mu-chas gra-cias' marcando cada sílaba.",
                    xpReward = 20
                ),
                Exercise(
                    id = "es_1_3",
                    language = language,
                    lessonId = "lesson_es_1",
                    type = ExerciseType.DIALOGUE_REPLY,
                    promptQuestion = "La app pregunta: «¿Qué idioma estás aprendiendo?» — Responde:",
                    targetPhrase = "Estoy aprendiendo a hablar español todos los días.",
                    translation = "I am learning to speak Spanish every day.",
                    phoneticGuide = "/esˈtoj a.pɾenˈdjen.do a aˈβlaɾ es.paˈɲol ˈto.ðos los ˈdi.as/",
                    pronunciationTip = "En 'español', apoya el dorso de la lengua contra el paladar para la 'ñ'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "es_1_4",
                    language = language,
                    lessonId = "lesson_es_1",
                    type = ExerciseType.LISTENING_AND_SPEAK,
                    promptQuestion = "Escucha al hablante nativo y repite la despedida:",
                    targetPhrase = "¡Que tengas un excelente día, hasta pronto!",
                    translation = "Have an excellent day, see you soon!",
                    phoneticGuide = "/ke ˈteŋ.ɡas un ek.seˈlen.te ˈdi.a ˈas.ta ˈpɾon.to/",
                    pronunciationTip = "Recuerda que la 'h' de 'hasta' no suena; enlaza 'hasta pronto'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "es_2_1",
                    language = language,
                    lessonId = "lesson_es_2",
                    type = ExerciseType.DIALOGUE_REPLY,
                    promptQuestion = "El camarero pregunta: «¿Qué desea tomar?» — Pide en el micrófono:",
                    targetPhrase = "Quisiera un café con leche y dos churros, por favor.",
                    translation = "I would like a coffee with milk and two churros, please.",
                    phoneticGuide = "/kiˈsje.ɾa un kaˈfe kon ˈle.tʃe i ðos ˈtʃu.ros poɾ faˈβoɾ/",
                    pronunciationTip = "Haz vibrar con fuerza la doble 'rr' en 'chu-rros'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "es_2_2",
                    language = language,
                    lessonId = "lesson_es_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pide la recomendación del restaurante:",
                    targetPhrase = "¿Cuál es el plato tradicional de la casa?",
                    translation = "What is the traditional house dish?",
                    phoneticGuide = "/ˈkwal es el ˈpla.to tɾa.ði.sjoˈnal de la ˈka.sa/",
                    pronunciationTip = "Eleva la entonación al inicio y al final de la pregunta.",
                    xpReward = 20
                ),
                Exercise(
                    id = "es_2_3",
                    language = language,
                    lessonId = "lesson_es_2",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Pide la cuenta amablemente:",
                    targetPhrase = "La comida estuvo deliciosa, ¿nos trae la cuenta?",
                    translation = "The food was delicious, could you bring us the bill?",
                    phoneticGuide = "/la koˈmi.ða esˈtu.βo ðe.liˈsjo.sa nos ˈtɾa.e la ˈkwen.ta/",
                    pronunciationTip = "La 'd' entre vocales en 'comida' suena suave.",
                    xpReward = 20
                ),
                Exercise(
                    id = "es_3_1",
                    language = language,
                    lessonId = "lesson_es_3",
                    type = ExerciseType.DIALOGUE_REPLY,
                    promptQuestion = "Preguntas en la calle cómo llegar a la estación:",
                    targetPhrase = "Disculpe, ¿dónde está la estación de tren más cercana?",
                    translation = "Excuse me, where is the nearest train station?",
                    phoneticGuide = "/disˈkul.pe ˈdon.de esˈta la es.taˈsjon de ˈtɾen mas seɾˈka.na/",
                    pronunciationTip = "Acentúa con fuerza la última sílaba en 'es-ta-CIÓN'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "es_3_2",
                    language = language,
                    lessonId = "lesson_es_3",
                    type = ExerciseType.REPEAT_PHRASE,
                    promptQuestion = "Confirma tu reserva en el hotel:",
                    targetPhrase = "Buenas tardes, tengo una habitación reservada para hoy.",
                    translation = "Good afternoon, I have a room reserved for today.",
                    phoneticGuide = "/ˈbwe.nas ˈtaɾ.ðes ˈteŋ.ɡo ˈu.na a.βi.taˈsjon re.seɾˈβa.ða ˈpa.ɾa ˈoj/",
                    pronunciationTip = "Haz vibrar la 'r' inicial de 'reservada'.",
                    xpReward = 20
                ),
                Exercise(
                    id = "es_3_3",
                    language = language,
                    lessonId = "lesson_es_3",
                    type = ExerciseType.FILL_IN_BLANK_SPEAK,
                    promptQuestion = "Pregunta a qué hora sale el vuelo:",
                    targetPhrase = "¿A qué hora sale nuestro próximo vuelo a Madrid?",
                    translation = "What time does our next flight to Madrid leave?",
                    phoneticGuide = "/a ˈke ˈo.ɾa ˈsa.le ˈnwes.tɾo ˈpɾok.si.mo ˈbwe.lo a maˈðɾið/",
                    pronunciationTip = "Pronuncia claramente el diptongo 'ue' en 'nuestro' y 'vuelo'.",
                    xpReward = 25
                ),
                Exercise(
                    id = "es_4_1",
                    language = language,
                    lessonId = "lesson_es_4",
                    type = ExerciseType.TONGUE_TWISTER,
                    promptQuestion = "🔥 Desafío Vibrante RR en Español:",
                    targetPhrase = "El perro de San Roque no tiene rabo porque Ramón Ramírez se lo ha cortado.",
                    translation = "San Roque's dog has no tail because Ramon Ramirez cut it off.",
                    phoneticGuide = "/el ˈpe.ro ðe san ˈro.ke no ˈtje.ne ˈra.βo/",
                    pronunciationTip = "Haz vibrar la punta de la lengua en 'perro', 'Roque', 'rabo' y 'Ramón'.",
                    xpReward = 30
                ),
                Exercise(
                    id = "es_4_2",
                    language = language,
                    lessonId = "lesson_es_4",
                    type = ExerciseType.TONGUE_TWISTER,
                    promptQuestion = "🔥 Desafío de Fluidez y Ritmo en Español:",
                    targetPhrase = "Tres tristes tigres tragaban trigo en un trigal.",
                    translation = "Three sad tigers swallowed wheat in a wheat field.",
                    phoneticGuide = "/ˈtɾes ˈtɾis.tes ˈti.ɣɾes tɾaˈɣa.βan ˈtɾi.ɣo en un tɾiˈɣal/",
                    pronunciationTip = "Articula el grupo consonántico 'tr' con precisión en cada palabra.",
                    xpReward = 30
                )
            )
        }
    }

    fun getDialogueScenarios(language: Language): List<DialogueScenario> {
        return when (language) {
            Language.ENGLISH -> listOf(
                DialogueScenario(
                    id = "dial_en_cafe",
                    language = language,
                    title = "☕ En la Cafetería en Nueva York",
                    description = "Conversa con el barista y pide tu bebida favorita.",
                    situation = "Entras a una cafetería en Manhattan. El barista te saluda cordialmente.",
                    turns = listOf(
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Barista Sam",
                            targetText = "Hi there! Welcome to Central Brew. What can I get started for you today?",
                            translation = "¡Hola! Bienvenido a Central Brew. ¿Qué puedo prepararte hoy?",
                            expectedUserReply = "Hi, I would like an iced latte with oat milk, please."
                        ),
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Barista Sam",
                            targetText = "Great choice! Would you like any sweet pastry or croissant to go with that?",
                            translation = "¡Gran elección! ¿Te gustaría algún pastel dulce o croissant para acompañar?",
                            expectedUserReply = "Just a chocolate croissant, thank you very much!"
                        ),
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Barista Sam",
                            targetText = "Perfect! That will be six dollars. Will that be cash or card?",
                            translation = "¡Perfecto! Serán seis dólares. ¿Pagas con efectivo o tarjeta?",
                            expectedUserReply = "I will pay with card, please."
                        )
                    )
                ),
                DialogueScenario(
                    id = "dial_en_hotel",
                    language = language,
                    title = "🏨 Check-in en el Hotel",
                    description = "Llegas a la recepción y confirmas tu reserva.",
                    situation = "Llegas al mostrador del hotel tras un largo viaje.",
                    turns = listOf(
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Recepcionista Emma",
                            targetText = "Good evening! Welcome to Grand Palace Hotel. Do you have a reservation with us?",
                            translation = "¡Buenas noches! Bienvenido al Hotel Grand Palace. ¿Tiene una reserva con nosotros?",
                            expectedUserReply = "Yes, I have a reservation under the name Garcia."
                        ),
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Recepcionista Emma",
                            targetText = "I found your booking for three nights. Here is your room key on the fifth floor.",
                            translation = "Encontré su reserva por tres noches. Aquí tiene la llave de su habitación en el quinto piso.",
                            expectedUserReply = "Thank you! What time is breakfast served in the morning?"
                        )
                    )
                )
            )
            Language.FRENCH -> listOf(
                DialogueScenario(
                    id = "dial_fr_bistro",
                    language = language,
                    title = "🥐 Dans un Café à Paris",
                    description = "Pratique la conversation avec le serveur.",
                    situation = "Tu es assis en terrasse à Saint-Germain.",
                    turns = listOf(
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Serveur Pierre",
                            targetText = "Bonjour monsieur-dame, désirez-vous prendre quelque chose à boire ?",
                            translation = "Buenos días, ¿desea tomar algo de beber?",
                            expectedUserReply = "Bonjour ! Je voudrais un café noir et de l'eau, s'il vous plaît."
                        ),
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Serveur Pierre",
                            targetText = "Très bien, et avec ceci, souhaitez-vous une viennoiserie ?",
                            translation = "Muy bien, y con esto, ¿desea algún bollo?",
                            expectedUserReply = "Oui, un pain au chocolat, merci beaucoup !"
                        )
                    )
                )
            )
            Language.GERMAN -> listOf(
                DialogueScenario(
                    id = "dial_de_cafe",
                    language = language,
                    title = "🥨 Im Berliner Café",
                    description = "Bestelle Kaffee und Kuchen auf Deutsch.",
                    situation = "Du bist in einem gemütlichen Café in Berlin.",
                    turns = listOf(
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Kellner Lukas",
                            targetText = "Guten Tag! Was darf ich Ihnen heute Schönes bringen?",
                            translation = "¡Buenos días! ¿Qué cosas ricas le puedo traer hoy?",
                            expectedUserReply = "Guten Tag! Ich möchte einen Cappuccino und Apfelstrudel, bitte."
                        ),
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Kellner Lukas",
                            targetText = "Sehr gerne! Kommt sofort zu Ihnen an den Tisch.",
                            translation = "¡Con mucho gusto! Se lo traigo enseguida a su mesa.",
                            expectedUserReply = "Vielen herzlichen Dank!"
                        )
                    )
                )
            )
            Language.ITALIAN -> listOf(
                DialogueScenario(
                    id = "dial_it_bar",
                    language = language,
                    title = "☕ Al Bar di Roma",
                    description = "Ordina la colazione all'italiana.",
                    situation = "Sei al bancone di un tipico bar romano la mattina.",
                    turns = listOf(
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Barista Marco",
                            targetText = "Buongiorno! Cosa ti preparo di buono stamattina?",
                            translation = "¡Buenos días! ¿Qué te preparo de rico esta mañana?",
                            expectedUserReply = "Un cappuccino e un cornetto alla crema, per favore."
                        ),
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Barista Marco",
                            targetText = "Ecco a te, appena sfornato! Buon appetito!",
                            translation = "¡Aquí tienes, recién horneado! ¡Buen provecho!",
                            expectedUserReply = "Grazie mille, sembra delizioso!"
                        )
                    )
                )
            )
            Language.PORTUGUESE -> listOf(
                DialogueScenario(
                    id = "dial_pt_praia",
                    language = language,
                    title = "🥥 No Quiosque em Copacabana",
                    description = "Peça uma água de coco gelada na praia.",
                    situation = "Você está curtindo o sol no calçadão do Rio.",
                    turns = listOf(
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Atendente Beto",
                            targetText = "E aí, beleza? Quer uma água de coco bem geladinha?",
                            translation = "¿Qué tal, todo bien? ¿Quieres un agua de coco bien fría?",
                            expectedUserReply = "Sim, por favor! Uma água de coco e um pastel de queijo."
                        ),
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Atendente Beto",
                            targetText = "Com certeza, sai caprichado na hora pra você!",
                            translation = "¡Por supuesto, te lo preparo con todo el cariño ahora mismo!",
                            expectedUserReply = "Valeu demais, muito obrigado!"
                        )
                    )
                )
            )
            Language.SPANISH -> listOf(
                DialogueScenario(
                    id = "dial_es_cafe",
                    language = language,
                    title = "🥘 En una Cafetería en Madrid",
                    description = "Responde las preguntas del camarero hablando en español.",
                    situation = "Estás en una terraza soleada en la Plaza Mayor de Madrid.",
                    turns = listOf(
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Camarero Diego",
                            targetText = "¡Hola, buenos días! Bienvenido a Café Mayor. ¿Qué te gustaría tomar hoy?",
                            translation = "Hello, good morning! Welcome to Cafe Mayor. What would you like to have today?",
                            expectedUserReply = "Hola, buenos días. Quisiera un café con leche y una tostada, por favor."
                        ),
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Camarero Diego",
                            targetText = "¡Excelente elección! ¿Prefieres el café caliente o con hielo?",
                            translation = "Excellent choice! Do you prefer hot coffee or iced coffee?",
                            expectedUserReply = "Prefiero el café caliente, muchas gracias."
                        ),
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Camarero Diego",
                            targetText = "¡Perfecto! Enseguida te lo llevo a la mesa.",
                            translation = "Perfect! I will bring it to your table right away.",
                            expectedUserReply = "Muchas gracias, eres muy amable."
                        )
                    )
                ),
                DialogueScenario(
                    id = "dial_es_hotel",
                    language = language,
                    title = "🏨 Recepción de Hotel en Barcelona",
                    description = "Practica preguntas y respuestas reales al registrarte en un hotel.",
                    situation = "Acabas de llegar al hotel en Barcelona y hablas con la recepcionista.",
                    turns = listOf(
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Recepcionista Lucía",
                            targetText = "¡Buenas tardes! Bienvenido al Hotel Mediterráneo. ¿Tiene una reserva?",
                            translation = "Good afternoon! Welcome to Hotel Mediterraneo. Do you have a reservation?",
                            expectedUserReply = "Buenas tardes, sí, tengo una reserva para dos noches."
                        ),
                        DialogueMessage(
                            sender = "AI",
                            avatarName = "Recepcionista Lucía",
                            targetText = "Aquí está su llave de la habitación trescientos cuatro. ¿Necesita ayuda con su equipaje?",
                            translation = "Here is your key for room 304. Do you need help with your luggage?",
                            expectedUserReply = "No gracias, ¿a qué hora sirven el desayuno mañana?"
                        )
                    )
                )
            )
        }
    }
}
