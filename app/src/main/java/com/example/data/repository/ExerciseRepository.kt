package com.example.data.repository

import com.example.data.model.DialogueMessage
import com.example.data.model.DialogueScenario
import com.example.data.model.Exercise
import com.example.data.model.ExerciseType
import com.example.data.model.Language
import com.example.data.model.LessonModule

object ExerciseRepository {

    fun getModulesGroupedByLevel(language: Language): Map<Int, List<LessonModule>> {
        return getModulesForLanguage(language)
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
        }
    }
}
