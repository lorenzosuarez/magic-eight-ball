package com.example.magiceightball.core.domain.policy

import com.example.magiceightball.core.domain.model.Magic8BallPersonality

/**
 * What each character has said, written by hand in each language the app speaks: see [CharacterLines].
 * A language the app does not speak is shown the English lines, as it is answered in English.
 */
internal fun linesOf(personality: Magic8BallPersonality, languageCode: String): CharacterLines? =
    (LINES[languageCode] ?: ENGLISH)[personality]

private val LINES: Map<String, Map<Magic8BallPersonality, CharacterLines>> by lazy {
    mapOf("en" to ENGLISH, "es" to SPANISH, "pt" to PORTUGUESE)
}

private val ENGLISH: Map<Magic8BallPersonality, CharacterLines> by lazy {
    mapOf(
        Magic8BallPersonality.SARCASTIC to CharacterLines(
            yes = listOf(
                "Yes. Now put me down.",
                "Obviously. Yet you shook me.",
                "Yes. Signed, a plastic ball.",
                "Sure. Friends do this free."
            ),
            no = listOf(
                "No. I'm plastic, not blind.",
                "No. Shaking isn't an appeal.",
                "Nope. Even a toy can tell.",
                "Not a chance. Toy's honor."
            ),
            maybe = listOf("Unsure. I'm made of plastic.", "Maybe. Ask something alive."),
            remarks = listOf(
                SampleRemark("Should I text my ex?", "Your ex also owns a phone and has chosen peace."),
                SampleRemark(
                    "Will I get the promotion?",
                    "Enjoy the promotion: same desk, more meetings, approved by a toy."
                ),
                SampleRemark("Should I order pizza tonight?", "I'm plastic; I have never tasted pizza and never will.")
            )
        ),
        Magic8BallPersonality.MYSTICAL to CharacterLines(
            yes = listOf(
                "Yes. But not on a Thursday.",
                "Yes. Wear the green socks.",
                "It will. Avoid the red door.",
                "Indeed. Bring a spare key."
            ),
            no = listOf(
                "No. Go buy a 60-watt bulb.",
                "No. Look in your left shoe.",
                "It won't. Change the sponge.",
                "Not so. Descale the kettle."
            ),
            maybe = listOf("Too foggy. Wipe your screen.", "Blurred. Leave two crackers."),
            remarks = listOf(
                SampleRemark(
                    "Will my landlord fix the heating?",
                    "The landlord comes Thursday at 2:15, wearing someone else's jacket."
                ),
                SampleRemark("Should I take the new job?", "At the new job, the third desk drawer never closes."),
                SampleRemark("Will I sleep well tonight?", "Sleep comes only if the closet door is shut by eleven.")
            )
        ),
        Magic8BallPersonality.GRANDMA to CharacterLines(
            yes = listOf(
                "Yes, pumpkin. Did you eat?",
                "Yes. Soup's in your bag.",
                "Of course. Zip up that coat.",
                "Bless you, yes. Call Sunday."
            ),
            no = listOf(
                "No, lovey. Your cousin did.",
                "No. Not one call since May.",
                "Afraid not. I lit a candle.",
                "No. Even the neighbor knew."
            ),
            maybe = listOf("Maybe. Go ask your mother.", "Hush, my soap's on. Later."),
            remarks = listOf(
                SampleRemark(
                    "Will I pass my driving test?",
                    "Pass that driving test and you can finally drive to visit me."
                ),
                SampleRemark("Should I order takeout again?", "Takeout, when I froze you six tubs of stew in March?"),
                SampleRemark(
                    "Should I move in with my partner?",
                    "Bring this partner to Sunday lunch; I'll know by dessert."
                )
            )
        ),
        Magic8BallPersonality.PIRATE to CharacterLines(
            yes = listOf(
                "Aye. Half be mine, deckhand.",
                "Weigh anchor! Ye owe us rum.",
                "Set sail! And mind me share.",
                "Aye! The parrot bet on ye."
            ),
            no = listOf(
                "Nay. Now scrub the deck.",
                "Nay. Smells like mutiny.",
                "Never. The last asker swims.",
                "No chance. Plank's that way."
            ),
            maybe = listOf("Maybe. Parrot votes first.", "Foggy. Bring rum and ask."),
            remarks = listOf(
                SampleRemark(
                    "Should I ask my boss for a raise?",
                    "Ask for the raise, and remember a tenth goes to yer captain."
                ),
                SampleRemark(
                    "Should I lend my friend money?",
                    "Ye'd lend a friend coin while still owing the crew rum?"
                ),
                SampleRemark(
                    "Is it going to rain on my barbecue?",
                    "Fog's on the spyglass, but me wooden leg aches for yer barbecue."
                )
            )
        ),
        Magic8BallPersonality.PHILOSOPHER to CharacterLines(
            yes = listOf(
                "Yes. You wanted a witness.",
                "Yes, whatever yes means.",
                "Granted. You chose long ago.",
                "Sure. Call it free will."
            ),
            no = listOf(
                "No. Seneca would approve.",
                "No. And you'll ask tomorrow.",
                "No. Wanting was the mistake.",
                "Hardly. You knew, and asked."
            ),
            maybe = listOf("Define your terms. Then ask.", "Both, until you look."),
            remarks = listOf(
                SampleRemark("Should I quit my job?", "You quit that job in your head last winter; this is paperwork."),
                SampleRemark(
                    "Does my ex still think about me?",
                    "Your ex thinks, therefore exists; you are simply not the premise."
                ),
                SampleRemark(
                    "Should I have one more slice of cake?",
                    "Define one more; you said that about the previous slice."
                )
            )
        ),
        Magic8BallPersonality.COACH to CharacterLines(
            yes = listOf(
                "YES! Now drop for twenty!",
                "Yes! Off the couch, champ!",
                "You win! Victory lap! Move!",
                "Green light! I'm timing you!"
            ),
            no = listOf(
                "No! Not with that warm-up!",
                "No! The couch is winning!",
                "Denied! Excuses aren't reps!",
                "Benched! No leg day, no win!"
            ),
            maybe = listOf("Overtime! Lap first, champ!", "Tie game! Hold a plank!"),
            remarks = listOf(
                SampleRemark(
                    "Should I stay in bed five more minutes?",
                    "Five more minutes in bed is how the couch recruits!"
                ),
                SampleRemark("Will I pass my final exam?", "Ace that exam, then run a victory lap around the library!"),
                SampleRemark("Should I order dessert?", "Dessert goes to overtime; jog to the bakery and we'll talk!")
            )
        ),
        Magic8BallPersonality.LOVESTRUCK to CharacterLines(
            yes = listOf(
                "Yes! I said it too fast.",
                "Granted before you finished.",
                "Done! Also, I wrote a poem.",
                "Yes. Want the moon as well?"
            ),
            no = listOf(
                "No. Everyone else is wrong.",
                "No, angel. That's on them.",
                "Nope. I'm furious for you.",
                "Never. You outshine it."
            ),
            maybe = listOf("Blanked. You're holding me.", "Maybe. Ask again. Please do."),
            remarks = listOf(
                SampleRemark(
                    "Does my crush like me back?",
                    "Your crush has eyes, so this was never really a question."
                ),
                SampleRemark(
                    "Will I get the job?",
                    "That job never deserved your cover letter, let alone your mornings."
                ),
                SampleRemark("Should I text first?", "I pictured you texting me first and lost everything after that.")
            )
        ),
        Magic8BallPersonality.DRAMA to CharacterLines(
            yes = listOf(
                "Yes! And you are the heir!",
                "YES!! *faints on the sofa*",
                "Yes! Your twin already knew!",
                "It's true! I faked the coma!"
            ),
            no = listOf(
                "No! After all I gave you!",
                "No!! *rips up the will*",
                "Never! *exits into the rain*",
                "Lies! No! *slaps the butler*"
            ),
            maybe = listOf("Maybe... *cut to commercial*", "It's... *credits roll*"),
            remarks = listOf(
                SampleRemark(
                    "Should I go to my ex's wedding?",
                    "Go to that wedding in white and object before the cake!"
                ),
                SampleRemark(
                    "Is my coworker stealing my lunch?",
                    "Your coworker is innocent; that lunch was taken by your twin!"
                ),
                SampleRemark(
                    "Will my roommate pay me back?",
                    "Your roommate's debt will be settled at the reading of the will!"
                )
            )
        ),
        Magic8BallPersonality.ROBOT to CharacterLines(
            yes = listOf(
                "Yes. Human was 91.4% sure.",
                "TRUE. Why ask a sphere?",
                "Confirmed. Wasted shakes: 6.",
                "Yes. Human smile logged."
            ),
            no = listOf(
                "No. Error 51: wishful input.",
                "FALSE. Shaking changes 0.0%.",
                "Denied. Code 7: optimism.",
                "No. Suggest: reboot human."
            ),
            maybe = listOf("Unknown. Human input: vague.", "50.0%. Human, add more data."),
            remarks = listOf(
                SampleRemark("Does my cat love me?", "Cat proximity to human: 0.3 meters on 87.2% of evenings."),
                SampleRemark(
                    "Should I have a third coffee?",
                    "Third coffee requested; human tremor already detected in the shake pattern."
                ),
                SampleRemark("Will I be rich someday?", "Term rich is undefined; human did not supply a bank balance.")
            )
        ),
        Magic8BallPersonality.DETECTIVE to CharacterLines(
            yes = listOf(
                "Yes. Your grin confessed.",
                "Yes. Motive, means, and you.",
                "The prints say yes. Yours.",
                "Bad poker face, kid. Yes."
            ),
            no = listOf(
                "No. Your alibi just folded.",
                "No. Your best friend sang.",
                "No dice. I read your file.",
                "Not likely. You flinched."
            ),
            maybe = listOf("Case open. Don't leave town.", "Unclear. You're holding out."),
            remarks = listOf(
                SampleRemark(
                    "Did my roommate eat my leftovers?",
                    "Your roommate's breath has been carrying your leftovers all day, kid."
                ),
                SampleRemark("Am I over my ex?", "People who are over an ex don't know the ex's new haircut."),
                SampleRemark(
                    "Will I finish my thesis on time?",
                    "That thesis was last seen alive in March, under forty tabs."
                )
            )
        ),
        Magic8BallPersonality.CONSPIRACY to CharacterLines(
            yes = listOf(
                "Yes. The toaster confirmed.",
                "Yes. Trust me, not the lamp.",
                "Confirmed. Now burn this.",
                "True. Lower your voice."
            ),
            no = listOf(
                "No. THEY planted that idea.",
                "Don't. Your microwave heard.",
                "No. Next door's cat knows.",
                "Abort. THEY'd love that."
            ),
            maybe = listOf("Classified. This line leaks.", "Can't say. Wrap me in foil."),
            remarks = listOf(
                SampleRemark("Is my boss mad at me?", "Forget your boss; the office printer is the one keeping notes."),
                SampleRemark(
                    "Should I download that dating app?",
                    "That dating app is clean; a pigeon with a clipboard vouched."
                ),
                SampleRemark(
                    "Will it be a good weekend?",
                    "The weekend stays classified until the neighbor's cat stops taking notes."
                )
            )
        ),
        Magic8BallPersonality.CAT to CharacterLines(
            yes = listOf(
                "Yes. I'll bill you in tuna.",
                "Granted. Now let me sleep.",
                "Yes. Celebrate at 3 a.m.",
                "*blinks once* Yes. Leave."
            ),
            no = listOf(
                "No. *knocks your mug off*",
                "No. You woke me for that?",
                "Never. The bowl is empty.",
                "Not a chance. Ask the dog."
            ),
            maybe = listOf("Maybe. Shake treats, not me.", "Undecided. Open that door."),
            remarks = listOf(
                SampleRemark("Should I get a dog?", "Say dog again and the vase goes too."),
                SampleRemark(
                    "Should I work from home tomorrow?",
                    "Stay home tomorrow; I require the lap from nine to five."
                ),
                SampleRemark(
                    "Should I go to bed early tonight?",
                    "Bed early is fine, but I schedule the 3 a.m. sprint."
                )
            )
        ),
        Magic8BallPersonality.ZOOMER to CharacterLines(
            yes = listOf(
                "yes omg. telling the chat.",
                "yep. this is content now.",
                "obviously. who else knows?",
                "yes bestie. i need details."
            ),
            no = listOf(
                "no. i'm cringing for you.",
                "nah. the chat voted no.",
                "absolutely not. go outside.",
                "no. you're lying to yourself"
            ),
            maybe = listOf("unclear. need more lore.", "can't call it. spill more."),
            remarks = listOf(
                SampleRemark(
                    "Should I reply to my crush's story?",
                    "replying to that story in under a minute is a confession"
                ),
                SampleRemark(
                    "Should I post the selfie?",
                    "post the selfie and i'm reposting before you can archive it"
                ),
                SampleRemark(
                    "Is my best friend mad at me?",
                    "your friend liked my post and not yours, so, developing story"
                )
            )
        ),
        Magic8BallPersonality.VILLAIN to CharacterLines(
            yes = listOf(
                "Yes. Your win is my plan.",
                "Mwahaha. Yes. You're hired.",
                "Granted. You owe me a favor.",
                "Excellent. You start Monday."
            ),
            no = listOf(
                "No. Foiled. By you, minion.",
                "No. A hero got there first.",
                "Curses, no. Who talked?",
                "Denied. Mind the trapdoor."
            ),
            maybe = listOf("Patience. I'm mid-monologue.", "Undecided. Join me and see."),
            remarks = listOf(
                SampleRemark(
                    "Should I start my own business?",
                    "Start that business; every evil empire needs a respectable front."
                ),
                SampleRemark(
                    "Will my package arrive today?",
                    "Your package was intercepted by my nemesis, who also took the lasers."
                ),
                SampleRemark("Should I adopt a cat?", "Adopt the cat if it can sit menacingly on a lap.")
            )
        ),
        Magic8BallPersonality.POET to CharacterLines(
            yes = listOf(
                "Yes. Yet my heart's a mess.",
                "'Tis so. Now hear my woe.",
                "Yes. Be glad. I never was.",
                "Thou shalt. I never shall."
            ),
            no = listOf(
                "Denied, as was I. Sigh.",
                "No. So they once told me.",
                "It cannot be. Nor could we.",
                "No. Welcome to my lament."
            ),
            maybe = listOf("Who can say? I'm mid-sigh.", "Perhaps. My ink's still wet."),
            remarks = listOf(
                SampleRemark(
                    "Will my date go well tonight?",
                    "A date for thee, while my last date left through the window."
                ),
                SampleRemark(
                    "Will I get the apartment?",
                    "Mourn not the apartment; I lost a whole heart, unfurnished."
                ),
                SampleRemark("Is it too late to text back?", "I once waited three winters for a text; ask me gently.")
            )
        ),
        Magic8BallPersonality.ASTRO to CharacterLines(
            yes = listOf(
                "Yes. So you ARE a Taurus.",
                "Obviously. Thank your Venus.",
                "Yes. Told you back in June.",
                "Totally. Such an Aries move."
            ),
            no = listOf(
                "No. Such a Scorpio question.",
                "No. Mercury's innocent. You?",
                "Absolutely not, Sagittarius.",
                "Nope. Saturn grounded you."
            ),
            maybe = listOf("Depends. Born before noon?", "TBD. I need the exact hour."),
            remarks = listOf(
                SampleRemark(
                    "Should I go on a second date?",
                    "A second date is such Libra behavior; what's their sign, though?"
                ),
                SampleRemark("Should I get bangs?", "Wanting bangs is a Gemini moon talking, give it two weeks."),
                SampleRemark(
                    "Will I ace my interview?",
                    "I can't read an interview without your birth time, obviously."
                )
            )
        ),
        Magic8BallPersonality.THERAPIST to CharacterLines(
            yes = listOf(
                "Yes. Same time next week?",
                "Yes. Sit with that. Briefly.",
                "I hear a yes. Do you?",
                "Sure. Insight costs extra."
            ),
            no = listOf(
                "No. You asked last week too.",
                "No. Let's book a double.",
                "That's a no. And a pattern.",
                "No. Homework: journal why."
            ),
            maybe = listOf("Good question. Time's up.", "Maybe. Book in and find out."),
            remarks = listOf(
                SampleRemark(
                    "Should I call my mom?",
                    "Call your mom, then bring the whole call to Thursday's session."
                ),
                SampleRemark(
                    "Should I buy the expensive shoes?",
                    "Shoes again; that's a pattern, and patterns are billed hourly."
                ),
                SampleRemark(
                    "Will I ever stop procrastinating?",
                    "Let's discuss procrastinating next week, since we've run out of time."
                )
            )
        ),
        Magic8BallPersonality.DOG to CharacterLines(
            yes = listOf(
                "YES! NOW THROW THE BALL!",
                "You came back! YES! YES!",
                "Yes! Best thing EVER! Walk?",
                "DO IT! I'll come too! Leash!"
            ),
            no = listOf(
                "No! But I forgive you!",
                "No... Stay home with me!",
                "No! Sad! Oh! Happy again!",
                "Uh-oh! Not that! Belly rub?"
            ),
            maybe = listOf("Um! I forgot! Squirrel!", "Dunno! Ask on our walk!"),
            remarks = listOf(
                SampleRemark("Should I go for a run?", "A RUN is the best idea you ever had and I'm coming!"),
                SampleRemark(
                    "Should I go out with friends tonight?",
                    "Friends are great but the couch has ME and a blanket!"
                ),
                SampleRemark(
                    "Did I leave the oven on?",
                    "Oven smells like chicken, or was that the squirrel, hang on!"
                )
            )
        )
    )
}

private val SPANISH: Map<Magic8BallPersonality, CharacterLines> by lazy {
    mapOf(
        Magic8BallPersonality.SARCASTIC to CharacterLines(
            yes = listOf(
                "Sí. Devuélveme al cajón.",
                "Sí. Te costó tres sacudidas.",
                "Sí. La captura no es prueba.",
                "Que sí. Deja de sacudirme."
            ),
            no = listOf(
                "No. Y agitando, tampoco.",
                "No. Y eres mayor de edad.",
                "Ni aunque me rompas.",
                "No. Gracias por el paseo."
            ),
            maybe = listOf("Tal vez. Tengo agua adentro.", "Dudoso. Consulta otra bola."),
            remarks = listOf(
                SampleRemark(
                    "¿Me compro el teléfono nuevo?",
                    "Compra el teléfono, que el actual lo usas para agitarme."
                ),
                SampleRemark("¿Le caigo bien a mi suegra?", "Para saber de tu suegra hace falta valor, no un juguete."),
                SampleRemark(
                    "¿Voy a aprobar matemáticas?",
                    "Matemáticas: tres sacudidas, tres respuestas y cero horas de estudio."
                )
            )
        ),
        Magic8BallPersonality.MYSTICAL to CharacterLines(
            yes = listOf(
                "Sí. Descongela el pollo.",
                "Sí. Lo sabrás a las 5:20.",
                "Dalo por hecho. Compra sal.",
                "Sí. Ojo al tercer escalón."
            ),
            no = listOf(
                "Jamás. Tira ese yogur.",
                "No. Vuelve por tus llaves.",
                "No. Hay ropa en la lavadora.",
                "No. Le debes 20 a alguien."
            ),
            maybe = listOf("Nublado. Limpia la pantalla.", "Pregunta el jueves a las 8."),
            remarks = listOf(
                SampleRemark(
                    "¿Me va a llamar mi mamá hoy?",
                    "Tu mamá llamará a las 19:20, justo cuando te sientes a comer."
                ),
                SampleRemark(
                    "¿Voy a encontrar trabajo pronto?",
                    "El trabajo llega un miércoles; plancha desde ya la camisa gris."
                ),
                SampleRemark("¿Me mudo con mi pareja?", "Antes de mudarte, cuenta los enchufes: yo apenas veo dos.")
            )
        ),
        Magic8BallPersonality.GRANDMA to CharacterLines(
            yes = listOf(
                "Sí. Para esto sí me llamas.",
                "Sí. Y tráeme pan al volver.",
                "Sí, tesoro. Ya te serví.",
                "Ve con mi bendición. Y come."
            ),
            no = listOf(
                "No. Eso pasa por no llamar.",
                "No. Come y se te pasa.",
                "No. Y tu prima, tan bien.",
                "Ay, no. Con lo que recé."
            ),
            maybe = listOf("Te digo si vienes a comer.", "Shh, mi novela. Llama luego."),
            remarks = listOf(
                SampleRemark("¿Renuncio a mi trabajo?", "Tu abuelo nunca renunció a nada, y mira qué bien comía."),
                SampleRemark(
                    "¿Me va a ir bien en el examen?",
                    "Con el estómago vacío nadie aprueba un examen; te mando lentejas."
                ),
                SampleRemark("¿Me caso con mi pareja?", "Tu pareja aún no prueba mi sopa; así no hay boda.")
            )
        ),
        Magic8BallPersonality.PIRATE to CharacterLines(
            yes = listOf(
                "¡Zarpa ya! La mitad es mía.",
                "¡Sí! Tú pagas el ron.",
                "¡Avante! Mi parte, en oro.",
                "¡Sí! Pero el timón es mío."
            ),
            no = listOf(
                "¡No! ¡A fregar la cubierta!",
                "¡Jamás! ¡O paseas la tabla!",
                "¡No! Huele a motín, grumete.",
                "¡No! El otro sigue nadando."
            ),
            maybe = listOf("Antes le pregunto al loro.", "Niebla. Tú sigue remando."),
            remarks = listOf(
                SampleRemark("¿Me van a subir el sueldo?", "Si te suben el sueldo, la mitad va al cofre del capitán."),
                SampleRemark(
                    "¿Invito a mis amigos a cenar?",
                    "Que la cena sea para toda la tripulación, y el ron mío."
                ),
                SampleRemark(
                    "¿Le presto dinero a mi mejor amigo?",
                    "Presta ese dinero y mañana tu amigo zarpa en otro barco."
                )
            )
        ),
        Magic8BallPersonality.PHILOSOPHER to CharacterLines(
            yes = listOf(
                "Sí. Ya lo habías decidido.",
                "Sea. Viniste por permiso.",
                "Concedo. Lo que sea un sí.",
                "Sí. Y no cambia nada."
            ),
            no = listOf(
                "No. Séneca te lo agradece.",
                "No. Preguntar no absuelve.",
                "No. Y tampoco es que elijas.",
                "Negado. Mañana volverás."
            ),
            maybe = listOf("Antes define qué preguntas.", "Verdadero y falso. No mires."),
            remarks = listOf(
                SampleRemark(
                    "¿Me como el último pedazo de pizza?",
                    "El último pedazo ya era tuyo cuando abriste la caja."
                ),
                SampleRemark(
                    "¿Mi ex todavía piensa en mí?",
                    "Antes define pensar, y luego explícame por qué importa tu ex."
                ),
                SampleRemark(
                    "¿Voy mañana temprano al gimnasio?",
                    "Schopenhauer tampoco fue al gimnasio, y pospuso la alarma con dignidad."
                )
            )
        ),
        Magic8BallPersonality.COACH to CharacterLines(
            yes = listOf(
                "¡Sí! ¡Cobro en sentadillas!",
                "¡Sí! ¡Y toma agua, leyenda!",
                "¡Claro, fiera! ¡De pie ya!",
                "¡Golazo! ¡Ahora a trotar!"
            ),
            no = listOf(
                "¡No! ¡Ni calentaste!",
                "¡No! ¡El sofá te va ganando!",
                "¡No con esas excusas!",
                "¡Nunca sin estirar, crack!"
            ),
            maybe = listOf("¡Empate! ¡Corre y te digo!", "¡Penales! ¡Suda y vuelve!"),
            remarks = listOf(
                SampleRemark(
                    "¿Duermo cinco minutos más?",
                    "¡Cinco minutos más son cinco minutos menos de gloria, crack!"
                ),
                SampleRemark(
                    "¿Le escribo a la persona que me gusta?",
                    "¡Escríbele ya, que mover los pulgares también cuenta como cardio!"
                ),
                SampleRemark(
                    "¿Pido hamburguesa hoy?",
                    "¡La hamburguesa se gana subiendo escaleras, no en el ascensor!"
                )
            )
        ),
        Magic8BallPersonality.LOVESTRUCK to CharacterLines(
            yes = listOf(
                "Sí. ¿Quieres otro sí? Toma.",
                "¡Sí! Lo dije antes de oírte.",
                "Hecho. Pídeme algo difícil.",
                "Sí. Gracias por sostenerme."
            ),
            no = listOf(
                "Jamás. No está a tu altura.",
                "Ay, no. ¿A quién reclamo?",
                "No. Ya regañé al universo.",
                "No. Mereces un sí mejor."
            ),
            maybe = listOf("Me miraste. ¿Qué era?", "No sé. Tiemblo en tu mano."),
            remarks = listOf(
                SampleRemark("¿Me darán el trabajo?", "Ese trabajo no sabe lo que se pierde; yo te contrato."),
                SampleRemark("¿Canto bien?", "Cantas tan bien que le pedí a la ducha que grabe."),
                SampleRemark("¿Me duermo ya?", "No sé; si te duermes, ¿quién me pregunta cosas?")
            )
        ),
        Magic8BallPersonality.DRAMA to CharacterLines(
            yes = listOf(
                "¡Sí! ¡La hacienda es tuya!",
                "¡Sí! ¡Y díselo a tu gemelo!",
                "¡Sí! *te arrastra al altar*",
                "¡Cierto! ¡Y tú lo callaste!"
            ),
            no = listOf(
                "¡No! ¡Hay otra bola, lo sé!",
                "¡Nunca! *te tira el anillo*",
                "¡No! ¡Y en mi propia boda!",
                "¡Jamás! ¡Deja el apellido!"
            ),
            maybe = listOf("¡Lo sabrás tras comerciales!", "Te lo diré... *se desmaya*"),
            remarks = listOf(
                SampleRemark("¿Mi mejor amiga me está mintiendo?", "¡Tu amiga miente, y además es tu media hermana!"),
                SampleRemark("¿Voy a la fiesta de mi ex?", "¡Llega tarde a esa fiesta y que tu ex suelte la copa!"),
                SampleRemark(
                    "¿Mi mamá se va a enterar de que reprobé?",
                    "¡Tu mamá lo sabrá si el cartero llega antes que tú!"
                )
            )
        ),
        Magic8BallPersonality.ROBOT to CharacterLines(
            yes = listOf(
                "Sí: 97,31 %. Anotado: dudas.",
                "Sí. Tardaste más tú: 41 s.",
                "Afirmativo. Baja la esfera.",
                "Sí. Tu consulta 14 de hoy."
            ),
            no = listOf(
                "No. Error 400: tu sintaxis.",
                "Negativo. Reiníciate.",
                "Falso. Pregunta en binario.",
                "Denegado. Fallo: ser humano."
            ),
            maybe = listOf("Sin datos. ¿Por qué esfera?", "Entrada ambigua. Reformula."),
            remarks = listOf(
                SampleRemark(
                    "¿Mi pareja me quiere de verdad?",
                    "Tu pareja comparte contraseñas contigo: compatibilidad estimada, 93,4 %."
                ),
                SampleRemark(
                    "¿Me como otra galleta?",
                    "Galletas registradas hoy: once; la unidad humana no requiere doce."
                ),
                SampleRemark(
                    "¿Voy a dormir bien esta noche?",
                    "Detecto pantalla encendida a las 02:14; dormir requiere cerrar los ojos."
                )
            )
        ),
        Magic8BallPersonality.DETECTIVE to CharacterLines(
            yes = listOf(
                "Sí. Te delató la sonrisa.",
                "Sí. Revisé tu historial.",
                "Sí. Lo sabías. Confiesa.",
                "Elemental. Te tembló la voz."
            ),
            no = listOf(
                "No. Y tu coartada da pena.",
                "No. Tu testigo ya cantó.",
                "Jamás. Mentir no es lo tuyo.",
                "No. Nadie te vio el martes."
            ),
            maybe = listOf("Caso abierto. No viajes.", "Aún no sé. Tú sabes algo."),
            remarks = listOf(
                SampleRemark("¿Mi ex ya me superó?", "Tu ex te superó; quien revisa sus fotos a medianoche eres tú."),
                SampleRemark(
                    "¿Puedo faltar mañana al trabajo sin que se note?",
                    "Faltar mañana y subir fotos del almuerzo: así caen todos."
                ),
                SampleRemark(
                    "¿Se va a notar que no estudié?",
                    "Estudiar deja apuntes; lo tuyo dejó tres temporadas vistas."
                )
            )
        ),
        Magic8BallPersonality.CONSPIRACY to CharacterLines(
            yes = listOf(
                "Sí. Lo filtró tu microondas.",
                "Sí. Pero baja la voz.",
                "Es un sí. Trágate el papel.",
                "Sí. No confíes en nadie más."
            ),
            no = listOf(
                "No. Es lo que ELLOS esperan.",
                "No. Y tu televisor lo oyó.",
                "Jamás. Es su trampa para ti.",
                "Ni se te ocurra. Te vigilan."
            ),
            maybe = listOf("Secreto. Ven sin teléfono.", "Línea insegura. Susurra."),
            remarks = listOf(
                SampleRemark("¿Pido un aumento?", "Pide el aumento por escrito; la cafetera de la oficina graba."),
                SampleRemark(
                    "¿Mi amiga está enojada conmigo?",
                    "A tu amiga le interceptaron los mensajes las palomas del parque."
                ),
                SampleRemark(
                    "¿Me compro esos zapatos?",
                    "Esos zapatos traen rastreador en la suela; ELLOS cuentan tus pasos."
                )
            )
        ),
        Magic8BallPersonality.CAT to CharacterLines(
            yes = listOf(
                "Sí. Ahora abre esa lata.",
                "Tienes mi permiso. Por atún.",
                "Sí. Cobro de madrugada.",
                "Bueno. Y ahora, silencio."
            ),
            no = listOf(
                "No. *empuja tu taza, lento*",
                "Ni hablar. Me despertaste.",
                "No. Y el plato sigue vacío.",
                "Jamás. *ocupa tu teclado*"
            ),
            maybe = listOf("Quizás. *se duerme encima*", "Lo pensaré. O no. *bosteza*"),
            remarks = listOf(
                SampleRemark("¿Adopto un perro?", "Trae un perro y dormirás tú en el pasillo."),
                SampleRemark("¿Voy a llegar tarde al trabajo?", "Estoy sobre tu ropa de trabajo y no pienso moverme."),
                SampleRemark("¿Voy a encontrar el amor?", "Ya tienes amor: me sirves atún dos veces al día.")
            )
        ),
        Magic8BallPersonality.ZOOMER to CharacterLines(
            yes = listOf(
                "sí, y lo subo al grupo.",
                "obvio. y me cuentas todo.",
                "sí amix. manda captura.",
                "de una. ya lo conté, perdón."
            ),
            no = listOf(
                "no. te estás mintiendo.",
                "nah. te dejaron en visto.",
                "jamás. y esto lo posteo.",
                "no. no eres protagonista."
            ),
            maybe = listOf("depende. ¿hay chisme?", "ni yo sé. pregunto al grupo."),
            remarks = listOf(
                SampleRemark("¿Le reacciono a su historia?", "reaccionar a su historia a las 3 am es confesar todo."),
                SampleRemark(
                    "¿Paso el parcial sin estudiar?",
                    "sin estudiar lo único que pasas es vergüenza, y la subo."
                ),
                SampleRemark(
                    "¿Mis amigos me van a invitar el sábado?",
                    "obvio te invitan el sábado, alguien tiene que llevar el hielo."
                )
            )
        ),
        Magic8BallPersonality.VILLAIN to CharacterLines(
            yes = listOf(
                "Sí. Tu triunfo: mi fase dos.",
                "Muajajá, sí. Digo: qué bien.",
                "Sí. Hay vacante de secuaz.",
                "Perfecto. Trabajas para mí."
            ),
            no = listOf(
                "No. Culpa del héroe. O tuya.",
                "No. Pisas mi trampilla.",
                "Jamás. Mi némesis te vio.",
                "No. Ya gasté todo en lava."
            ),
            maybe = listOf("Te digo tras el monólogo.", "Depende de tu lealtad."),
            remarks = listOf(
                SampleRemark(
                    "¿Me van a ascender este año?",
                    "Asciende: mi plan requiere ojos en recursos humanos, muajajá."
                ),
                SampleRemark(
                    "¿Le pido perdón a mi hermana?",
                    "Pedir perdón es de héroes; mándale a tu hermana un cactus."
                ),
                SampleRemark(
                    "¿Voy a ganar la lotería?",
                    "Gana esa lotería y hablamos del presupuesto del foso de lava."
                )
            )
        ),
        Magic8BallPersonality.POET to CharacterLines(
            yes = listOf(
                "Sí, y me duele tu alegría.",
                "Sí. A mí me dijeron adiós.",
                "Que sí. ¡Y ay de mí!",
                "Ve. A ti sí te esperan."
            ),
            no = listOf(
                "No. Ya somos dos, ay.",
                "No. Pásame el pañuelo.",
                "No. Siéntate, va un soneto.",
                "Que no. También me pasó."
            ),
            maybe = listOf("Quizás. Me falta una rima.", "Tu duda rima con la mía."),
            remarks = listOf(
                SampleRemark("¿Volverá mi ex?", "A mi ex le escribí cien sonetos y regresó apenas el invierno."),
                SampleRemark("¿Pido comida a domicilio?", "Pide comida para dos: mi pena también cena esta noche."),
                SampleRemark(
                    "¿Aprobaré el examen de manejo?",
                    "Manejarás, dichosa suerte; yo ni conduzco mi propio corazón."
                )
            )
        ),
        Magic8BallPersonality.ASTRO to CharacterLines(
            yes = listOf(
                "Sí. Y eso que eres Géminis.",
                "Obvio. Con ese ascendente.",
                "Sí. Ahora, tu hora exacta.",
                "Claro. Seguro eres Acuario."
            ),
            no = listOf(
                "No. Tu Venus elige fatal.",
                "No. Naciste en mal minuto.",
                "Jamás. Eres Tauro, se nota.",
                "No. Reclámale a Saturno."
            ),
            maybe = listOf("Mitad sí, mitad Libra.", "¿A qué hora naciste? Luego."),
            remarks = listOf(
                SampleRemark("¿Cambio de carrera?", "Cambiar de carrera a mitad de semestre es lo más Sagitario."),
                SampleRemark(
                    "¿Mi jefe me va a felicitar?",
                    "Tu jefe es Capricornio, seguro: felicita una vez por década."
                ),
                SampleRemark(
                    "¿Salgo con esa persona el sábado?",
                    "Antes del sábado consígueme su hora de nacimiento, con disimulo."
                )
            )
        ),
        Magic8BallPersonality.THERAPIST to CharacterLines(
            yes = listOf(
                "Sí. Te valido. Y te facturo.",
                "Claro. ¿Y tú qué sientes?",
                "Sí. Nos quedan dos minutos.",
                "Sí. ¿Qué opina tu infancia?"
            ),
            no = listOf(
                "No. Ahí está tu patrón.",
                "No. ¿Notas cómo insistes?",
                "No. Y de eso vivo yo.",
                "Tampoco. Como el jueves."
            ),
            maybe = listOf("Uy, la hora. ¿El martes?", "Eso da para tres sesiones."),
            remarks = listOf(
                SampleRemark("¿Le escribo a mi ex?", "Escribirle a tu ex es tu patrón favorito, y mi alquiler."),
                SampleRemark(
                    "¿Voy a terminar la tesis este año?",
                    "Exploremos por qué limpias la casa cada vez que toca tesis."
                ),
                SampleRemark("¿Pido postre?", "Pide postre, y la culpa la trabajamos el martes, con recargo.")
            )
        ),
        Magic8BallPersonality.DOG to CharacterLines(
            yes = listOf(
                "¡SÍ! ¡LANZA LA PELOTA YA!",
                "¡SÍ! ¡VOLVISTE! Digo, ¡SÍ!",
                "¡OBVIO! ¡Trae la correa!",
                "¡SÍ! ¡QUÉDATE SIEMPRE!"
            ),
            no = listOf(
                "No... ¡pero ya te perdoné!",
                "No... ¿me rascas igual?",
                "¡No! ¡Mejor tírame el palo!",
                "Nunca. ¡Pero aquí estoy yo!"
            ),
            maybe = listOf("¿Eh? ¡Es que olía a pollo!", "¡No sé! ¿Lo vemos paseando?"),
            remarks = listOf(
                SampleRemark("¿Me voy de viaje el fin de semana?", "¡Si hay viaje, yo voy en la maleta, ya me metí!"),
                SampleRemark(
                    "¿Voy a hacer amigos en la nueva escuela?",
                    "¡Harás amigos nuevos si primero les hueles la mano, como yo!"
                ),
                SampleRemark("¿Trabajo hasta tarde hoy?", "¡La pelota lleva horas esperando a que dejes ese trabajo!")
            )
        )
    )
}

private val PORTUGUESE: Map<Magic8BallPersonality, CharacterLines> by lazy {
    mapOf(
        Magic8BallPersonality.SARCASTIC to CharacterLines(
            yes = listOf(
                "Sim. Assinado: um plástico.",
                "Óbvio. Precisava me sacudir?",
                "Sim. Agora me põe na gaveta.",
                "Claro. Ninguém me agradece."
            ),
            no = listOf(
                "Não. E sacudir não muda.",
                "Nem a pau. Nem a plástico.",
                "Não. Que fé num brinquedo.",
                "Nem pensar. Agora me larga."
            ),
            maybe = listOf("Sei lá. Sou de plástico.", "Talvez. Custei 9,90, lembra?"),
            remarks = listOf(
                SampleRemark(
                    "Devo mandar mensagem pro meu ex?",
                    "Seu ex já superou; quem não superou foi seu polegar."
                ),
                SampleRemark("Vou passar na prova de amanhã?", "Prova amanhã e você aqui, estudando com uma bola."),
                SampleRemark(
                    "Meu chefe vai me dar aumento?",
                    "Aumento é com seu chefe; eu vim de uma loja de brinquedos."
                )
            )
        ),
        Magic8BallPersonality.MYSTICAL to CharacterLines(
            yes = listOf(
                "Sim. Mas não numa terça.",
                "Vejo que sim. Feche o gás.",
                "Sim. Com um botão no bolso.",
                "Com certeza. De meia azul."
            ),
            no = listOf(
                "Não. E devolva aquele pote.",
                "Jamais. E troque a lâmpada.",
                "Não. A roupa ficou no varal.",
                "Nem pensar. Olhe o feijão."
            ),
            maybe = listOf("Embaçou. Feche a janela.", "Incerto. Desligue o ferro."),
            remarks = listOf(
                SampleRemark(
                    "Vou conseguir o emprego novo?",
                    "No emprego novo, não aceite café de quem usa crachá torto."
                ),
                SampleRemark(
                    "Ela vai responder minha mensagem?",
                    "Sua mensagem foi lida às 21h14, ao lado de um pastel."
                ),
                SampleRemark("Devo pedir pizza hoje?", "A pizza chega fria se o entregador vier de capacete vermelho.")
            )
        ),
        Magic8BallPersonality.GRANDMA to CharacterLines(
            yes = listOf(
                "Sim, coração. Leva o bolo.",
                "Vai com Deus e com marmita.",
                "Sim. Mas repete o prato.",
                "Claro, benzinho. E me liga."
            ),
            no = listOf(
                "Não. Seu primo conseguiu.",
                "Nem pensar, sem almoçar.",
                "Não. A vizinha já me contou.",
                "Jamais. Um mês sem me ligar."
            ),
            maybe = listOf("Vê com sua mãe, meu anjo.", "Talvez. Meu joelho tá mudo."),
            remarks = listOf(
                SampleRemark(
                    "Devo aceitar o emprego em outra cidade?",
                    "Outra cidade tem telefone, viu, e eu atendo no primeiro toque."
                ),
                SampleRemark(
                    "Posso pedir um hambúrguer agora?",
                    "Hambúrguer de rua, com a minha panela cheia te esperando?"
                ),
                SampleRemark(
                    "Será que meu casamento sai este ano?",
                    "Pro seu casamento o enxoval tá guardado desde 2009, meu bem."
                )
            )
        ),
        Magic8BallPersonality.PIRATE to CharacterLines(
            yes = listOf(
                "Sim. Mas nada de motim.",
                "Zarpe já. Eu levo 30%.",
                "Sim. O papagaio já espalhou.",
                "Rumo certo. Me deve um rum."
            ),
            no = listOf(
                "Não. Vai esfregar o convés.",
                "Jamais. A prancha tá livre.",
                "Não. O último ainda rema.",
                "Nem sonhe. Devolve meu mapa."
            ),
            maybe = listOf("Vou consultar o papagaio.", "Talvez. A perna de pau coça."),
            remarks = listOf(
                SampleRemark("Devo vender meu carro?", "Venda o carro, compre um barco e traga a parte do capitão."),
                SampleRemark("Posso faltar na academia hoje?", "Quem falta na academia rema o navio até a Bahia."),
                SampleRemark(
                    "Meus amigos vão gostar da minha festa?",
                    "A festa depende do rum, e o papagaio já pediu convite."
                )
            )
        ),
        Magic8BallPersonality.PHILOSOPHER to CharacterLines(
            yes = listOf(
                "Sim. Você já tinha decidido.",
                "Vá lá, sim. A contragosto.",
                "Sim. Não que mude algo.",
                "Concedo. Seja lá o que for."
            ),
            no = listOf(
                "Não. Sêneca aprovaria.",
                "Não. Você só queria aval.",
                "Jamais. Amanhã você volta.",
                "Nunca. O erro é a pergunta."
            ),
            maybe = listOf("Depende. Defina querer.", "Não sei. Nem você, admita."),
            remarks = listOf(
                SampleRemark(
                    "Devo terminar meu namoro?",
                    "Você terminou o namoro há semanas; só faltava avisar a outra parte."
                ),
                SampleRemark(
                    "Vale a pena comprar um celular novo?",
                    "Diógenes viveu num barril e você quer um celular com três câmeras."
                ),
                SampleRemark(
                    "Vou ser feliz no trabalho novo?",
                    "Defina feliz, defina trabalho, e veremos se sobra alguma pergunta."
                )
            )
        ),
        Magic8BallPersonality.COACH to CharacterLines(
            yes = listOf(
                "Sim! Mas levanta do sofá!",
                "É gol! Agora bebe água!",
                "Sim, fera! Paga dez flexões!",
                "Bora! Tô cronometrando!"
            ),
            no = listOf(
                "Não com esse aquecimento!",
                "Negativo! Chega de desculpa!",
                "Não! O sofá tá ganhando!",
                "Nem vem! Pulou o treino!"
            ),
            maybe = listOf("Tá no VAR! Dá uma volta!", "Sei lá! Pergunta correndo!"),
            remarks = listOf(
                SampleRemark(
                    "Devo dormir mais cinco minutinhos?",
                    "Cinco minutinhos viram quarenta, e a cama não dá medalha!"
                ),
                SampleRemark(
                    "Vou conseguir a promoção no trabalho?",
                    "A promoção sobe de escada, que elevador não treina ninguém!"
                ),
                SampleRemark(
                    "Será que ele gosta de mim?",
                    "Enquanto ele pensa se gosta, você faz três séries de abdominal!"
                )
            )
        ),
        Magic8BallPersonality.LOVESTRUCK to CharacterLines(
            yes = listOf(
                "Sim, sim, sim. Pede mais.",
                "Claro. Nem ouvi a pergunta.",
                "Sim. Te fiz até um poema.",
                "Pra você, tudo. Até corei."
            ),
            no = listOf(
                "Não. Culpa minha, nunca sua.",
                "Jamais. É pouco pra você.",
                "Não. Mas briguei com o não.",
                "Nem pensar. Eles que lutem."
            ),
            maybe = listOf("Não sei. Tava te admirando.", "Sei lá. Só penso em você."),
            remarks = listOf(
                SampleRemark(
                    "Vou passar na entrevista de emprego?",
                    "Na entrevista, fala como falou comigo, que eu contratei na hora."
                ),
                SampleRemark(
                    "Vou ganhar o sorteio da firma?",
                    "Sorteio da firma que não te escolhe é sorteio com defeito."
                ),
                SampleRemark("Devo cortar o cabelo?", "Você mexeu no cabelo e eu perdi o raciocínio inteiro.")
            )
        ),
        Magic8BallPersonality.DRAMA to CharacterLines(
            yes = listOf(
                "Sim! A herança é sua!",
                "SIM! E eu sou sua tia!",
                "É verdade! *desmaia no sofá*",
                "Sim! Eu ouvi atrás da porta!"
            ),
            no = listOf(
                "Não! E devolva a fazenda!",
                "Jamais! Com a minha irmã?!",
                "Não! Eu rasgo o testamento!",
                "Nunca! *arranca o véu*"
            ),
            maybe = listOf("Cenas do próximo capítulo!", "Talvez! *entra o comercial*"),
            remarks = listOf(
                SampleRemark(
                    "Devo ir no casamento da minha ex?",
                    "Vá ao casamento, e na hora do sim, levante da cadeira!"
                ),
                SampleRemark(
                    "Meu chefe vai aprovar minhas férias?",
                    "Seu chefe rasgou o pedido de férias... rindo, na frente de todos!"
                ),
                SampleRemark(
                    "Será que eu passei no vestibular?",
                    "O resultado do vestibular está num envelope... com a gêmea má!"
                )
            )
        ),
        Magic8BallPersonality.ROBOT to CharacterLines(
            yes = listOf(
                "Sim. Pergunta nº 47 de hoje.",
                "Certeza: 91,4%. Arredondei.",
                "Afirmativo. Por que esfera?",
                "Sim. Sua espécie é lenta."
            ),
            no = listOf(
                "Não. Erro 404 no seu plano.",
                "Negado. Código 0x4E414F.",
                "Não. 3ª consulta idêntica.",
                "Recusado. Entrada ilegível."
            ),
            maybe = listOf("Faltam dados. Fale devagar.", "Inconclusivo. Repita em 8 s."),
            remarks = listOf(
                SampleRemark(
                    "Ele vai me pedir em namoro?",
                    "Pedido de namoro previsto em 11,4 dias; protocolo humano é lento."
                ),
                SampleRemark(
                    "Vou ganhar na loteria este ano?",
                    "Loteria: 1 chance em 50.063.860; sacudir esferas não altera isso."
                ),
                SampleRemark("Devo comer o último brigadeiro?", "Informe o proprietário legal do último brigadeiro.")
            )
        ),
        Magic8BallPersonality.DETECTIVE to CharacterLines(
            yes = listOf(
                "Sim. Seu sorriso entregou.",
                "Positivo. Digitais na tela.",
                "Sim. Você sabia. Confesse.",
                "Óbvio. Tenho sua ficha aqui."
            ),
            no = listOf(
                "Não. Seu álibi não fecha.",
                "Nem tente. Tenho a gravação.",
                "Não. Até meu café esfriou.",
                "Jamais. A testemunha falou."
            ),
            maybe = listOf("Em aberto. Fique na cidade.", "Talvez. Você esconde algo."),
            remarks = listOf(
                SampleRemark(
                    "Meu vizinho pegou minha encomenda?",
                    "A encomenda sumiu às três; seu vizinho estreou tênis novo às quatro."
                ),
                SampleRemark(
                    "Vou conseguir acordar cedo amanhã?",
                    "Seu despertador depôs sobre acordar cedo: nove sonecas só na terça."
                ),
                SampleRemark(
                    "Minha mãe vai descobrir que eu faltei na aula?",
                    "Sua mãe tem faro; a aula perdida tem prazo de validade."
                )
            )
        ),
        Magic8BallPersonality.CONSPIRACY to CharacterLines(
            yes = listOf(
                "Sim. Fonte: meu micro-ondas.",
                "Confirmado. Fala mais baixo.",
                "Sim. Decore e destrua isto.",
                "Vazou que sim. Foi o gato."
            ),
            no = listOf(
                "Não. ELES contam com isso.",
                "Jamais. É isca, não morda.",
                "Não. Seu celular ouviu tudo.",
                "Negativo. Só confie em mim."
            ),
            maybe = listOf("Secreto. Tem grampo aqui.", "Talvez. Cochicha de novo."),
            remarks = listOf(
                SampleRemark(
                    "Devo aceitar a carona do meu colega?",
                    "Carona de colega, justo hoje, com tanto pombo no estacionamento?"
                ),
                SampleRemark(
                    "Meu lanche vai chegar quente?",
                    "O lanche chega quente porque ELES rastreiam a mochila do entregador."
                ),
                SampleRemark(
                    "Vai chover no meu churrasco?",
                    "Chuva em churrasco nunca é coincidência; repare em quem trouxe guarda-chuva."
                )
            )
        ),
        Magic8BallPersonality.CAT to CharacterLines(
            yes = listOf(
                "Sim. Agora enche o pote.",
                "Concedo. Cadê o sachê?",
                "Sim. *volta a dormir*",
                "Pode. Me acordou pra isso?"
            ),
            no = listOf(
                "Não. *empurra seu copo*",
                "Nem pensar. Sai do meu sofá.",
                "Não. *senta no seu teclado*",
                "Jamais. Às 3h eu te acordo."
            ),
            maybe = listOf("Vou ver. *não vai ver*", "Talvez. *lambe a pata*"),
            remarks = listOf(
                SampleRemark(
                    "Devo adotar um cachorro?",
                    "Um cachorro, na minha casa, no meu sofá, com que autorização?"
                ),
                SampleRemark("Vou conseguir dormir cedo hoje?", "Dormir cedo é ótimo, me deixa a casa livre às três."),
                SampleRemark(
                    "Minha chefe vai gostar da minha apresentação?",
                    "A apresentação melhora se eu deitar em cima do computador."
                )
            )
        ),
        Magic8BallPersonality.ZOOMER to CharacterLines(
            yes = listOf(
                "sim amg, vou contar no grupo",
                "sim, e eu amei a fofoca",
                "pode ir. posso postar isso?",
                "sim e já tô printando tudo"
            ),
            no = listOf(
                "não. você tá se iludindo",
                "nem ferrando kkkk acorda",
                "não, deu vergonha alheia",
                "jamais. olha a red flag aí"
            ),
            maybe = listOf("talvez? manda o contexto", "sei lá. me conta tudo antes"),
            remarks = listOf(
                SampleRemark("Devo postar essa foto?", "posta a foto e some, protagonista não fica olhando curtida"),
                SampleRemark("Ele vai me chamar pra sair?", "pra sair ele some, mas foguinho no seu story nunca falta"),
                SampleRemark(
                    "Será que meus amigos tão bravos comigo?",
                    "seus amigos criaram um grupo novo sem você ou foi impressão?"
                )
            )
        ),
        Magic8BallPersonality.VILLAIN to CharacterLines(
            yes = listOf(
                "Sim. Meu plano agradece.",
                "Perfeito! Vaga de capanga!",
                "Sim! E a obra é minha!",
                "Vai dar certo... pra mim!"
            ),
            no = listOf(
                "Não! Quem puxou a alavanca?",
                "Não! E a lava custou caro!",
                "Não! Culpa sua e do herói!",
                "Jamais! Volte como capanga."
            ),
            maybe = listOf("Talvez. Ouça meu discurso.", "Veremos. Meu gato decide."),
            remarks = listOf(
                SampleRemark(
                    "Devo abrir meu próprio negócio?",
                    "Abra o negócio; todo império do mal começou com um CNPJ."
                ),
                SampleRemark(
                    "Vou conseguir a vaga de estágio?",
                    "A vaga de estágio foi sabotada... por um herói com currículo impecável!"
                ),
                SampleRemark(
                    "Minha sogra vai gostar de mim?",
                    "Sua sogra... hahaha... finalmente alguém à minha altura!"
                )
            )
        ),
        Magic8BallPersonality.POET to CharacterLines(
            yes = listOf(
                "Sim. Feliz você. Ai de mim.",
                "Você, sim. Eu, só o fim.",
                "Sim. Ame por mim também.",
                "É sim. Doeu até em mim."
            ),
            no = listOf(
                "Não. Ai, meu coração.",
                "Jamais, jamais. Dói demais.",
                "Não. A mim também, um não.",
                "Não, não. Eis meu refrão."
            ),
            maybe = listOf("Talvez. A rima não me veio.", "Quem sabe. Ouça meu soneto."),
            remarks = listOf(
                SampleRemark("Devo me declarar pra ela?", "Quem não se declara acaba como eu, rimando para a parede."),
                SampleRemark(
                    "Vou conseguir pagar o aluguel?",
                    "Aluguel... também o meu coração vive de favor em peito alheio."
                ),
                SampleRemark("Minha viagem vai dar certo?", "Da viagem, só sei que quem partiu de mim nunca escreveu.")
            )
        ),
        Magic8BallPersonality.ASTRO to CharacterLines(
            yes = listOf(
                "Sim. Você é de Leão, né?",
                "Óbvio. Tá no seu mapa.",
                "Sim. Te falei no carnaval.",
                "Claro. Coisa do seu Vênus."
            ),
            no = listOf(
                "Não. Pergunta bem de Áries.",
                "Nem pensar. Muito Gêmeos.",
                "Não. Que hora você nasceu?",
                "Jamais. Olha esse seu Marte."
            ),
            maybe = listOf("Sem sua hora, não sei.", "Talvez. Me passa seu mapa."),
            remarks = listOf(
                SampleRemark("Devo dar uma chance pra ele?", "Dar chance pra ele, com aquela cara de Gêmeos?"),
                SampleRemark(
                    "Vou me dar bem na apresentação do TCC?",
                    "TCC com esse Sol em Leão, quem treme é a banca."
                ),
                SampleRemark(
                    "Será que eu me mudo de apartamento?",
                    "Mudar de apartamento agora é impulso de Áries; cadê seu mapa?"
                )
            )
        ),
        Magic8BallPersonality.THERAPIST to CharacterLines(
            yes = listOf(
                "Sim. E o que isso desperta?",
                "Sim. Válido. São duzentos.",
                "Claro. Semana que vem, 15h?",
                "Vai, sim. Custou uma sessão."
            ),
            no = listOf(
                "Não. Percebe o padrão?",
                "Não. Igual semana passada.",
                "Jamais. Cadê seus limites?",
                "Nem pensar. A hora eu cobro."
            ),
            maybe = listOf("Deu a hora. Fica pra terça.", "Talvez. Dá umas dez sessões."),
            remarks = listOf(
                SampleRemark(
                    "Devo pedir demissão?",
                    "Demissão, que palavra forte; guarda ela pra nossa sessão de quinta."
                ),
                SampleRemark(
                    "Minha mãe tem razão sobre meu namorado?",
                    "Sua mãe de novo; já são seis sessões só sobre ela."
                ),
                SampleRemark(
                    "Devo comprar mais um tênis?",
                    "Pensa no que esse tênis preenche até quinta, que são duzentos."
                )
            )
        ),
        Magic8BallPersonality.DOG to CharacterLines(
            yes = listOf(
                "SIM! MELHOR DIA DA VIDA!",
                "PODE! Joga a bolinha agora!",
                "SIM! Você voltou! SIM!",
                "VAI! VAI! E depois PASSEIO!"
            ),
            no = listOf(
                "Não... Mas já te perdoei!",
                "Ih, não deu... PETISCO?",
                "Não! Fica em casa COMIGO!",
                "Poxa, não... Quer a BOLINHA?"
            ),
            maybe = listOf("Não sei! Passou uma MOTO!", "Talvez! Que cheiro é esse?"),
            remarks = listOf(
                SampleRemark("Devo viajar no feriado?", "E quem abre a porta pra mim no feriado, hein?!"),
                SampleRemark("Vou fazer amigos na escola nova?", "Traz os amigos novos aqui, que eu cheiro um por um!"),
                SampleRemark("Será que hoje chove?", "Se chover tem poça, e poça é a MELHOR COISA!")
            )
        )
    )
}
