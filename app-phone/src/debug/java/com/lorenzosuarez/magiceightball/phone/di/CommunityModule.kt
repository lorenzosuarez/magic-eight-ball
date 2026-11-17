package com.lorenzosuarez.magiceightball.phone.di

import com.example.magiceightball.core.domain.model.CharacterListing
import com.example.magiceightball.core.domain.model.CharacterLook
import com.example.magiceightball.core.domain.model.CharacterPage
import com.example.magiceightball.core.domain.model.Magic8BallPersonality
import com.example.magiceightball.core.domain.repository.CommunityCharacters
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import javax.inject.Singleton

/**
 * Other people's characters in the build that is tried out: a bank of made-up ones, so that
 * their shelf, its loading and a character opened from it can be seen before there is anywhere
 * to keep real ones. Nothing of this is in the app as it is released: see the module of the
 * same name there.
 */
@Module
@InstallIn(SingletonComponent::class)
object CommunityModule {

    @Provides
    @Singleton
    fun provideCommunityCharacters(): CommunityCharacters = SampleCommunityCharacters
}

/**
 * The bank: characters as people make them, each with the one who made it, handed out a page
 * at a time and no faster than a server would.
 *
 * They are written as people from a dozen countries write theirs on a phone, each in their own
 * hand: the Spanish of where they live, their laugh, their emoji or none, the accents their
 * keyboard leaves out, and how long they go on, from a line that stops short to the last letter
 * allowed. Mostly a scene or someone they know, now and then a figure of myth or of history long
 * dead, and nobody alive. Who made them are handles as people pick them, made up for this build:
 * the app as it is released makes up nobody. How they came to be written so, the guide they
 * follow and the hands, are in docs/research/comunidad. There are enough of them for the list to
 * be fetched in several goes.
 */
internal object SampleCommunityCharacters : CommunityCharacters {
    override val open: Boolean = true

    override suspend fun page(after: String?, limit: Int): CharacterPage {
        delay(LATENCY_MS)
        val from = after?.toInt() ?: 0
        val until = minOf(from + limit, BANK.size)
        return CharacterPage(items = (from until until).map(::listed), next = until.takeIf { it < BANK.size }?.toString())
    }

    /** Every one of them, in the order they are listed. */
    val all: List<CharacterListing.Community> get() = BANK.indices.map(::listed)

    private fun listed(number: Int): CharacterListing.Community = BANK[number].let { made ->
        CharacterListing.Community(
            id = "sample-$number",
            name = made.name,
            description = made.says,
            colour = made.colour,
            emblem = made.wears,
            author = MAKERS.getValue(made.by),
            // The most used first, as a shelf of what people made would be.
            uses = (BANK.size - number) * 173L
        )
    }

    private class Made(val name: String, val by: Int, val colour: Magic8BallPersonality, val wears: CharacterLook, val says: String)

    private val BANK = listOf(
        Made(
            "Mi Ex A Las 3am", 1, Magic8BallPersonality.LOVESTRUCK, CharacterLook.VILLAIN,
            "Te escribe 'despierta?' a las 3:07 y luego 'vi un perrito igualito al nuestro 😭' como si nada. Y justo cuando ya estas cayendo, 'oye y mi sudadera gris?' Neta ya devuélvesela (basado en hechos reales)"
        ),
        Made(
            "La silla", 3, Magic8BallPersonality.DETECTIVE, CharacterLook.NEUTRAL,
            "Nadie se sienta en ella desde 2022: es la de la ropa ni limpia ni sucia (lleva un jersey desde marzo) y a todo te dice 'Sí. Échalo encima, total.' Todos tenemos una, no me mintáis"
        ),
        Made(
            "El Cadenero", 7, Magic8BallPersonality.VILLAIN, CharacterLook.DETECTIVE,
            "Adentro no hay NADIE y te dice 'hoy es evento privado' 🤣"
        ),
        Made(
            "Bloquéalo Po", 8, Magic8BallPersonality.DRAMA, CharacterLook.BOW,
            "Le mandai el pantallazo y a cada mensaje le pone banderita roja, verde o beige (si sale con un pescado en la foto), al final 'bloquéalo, po' y sabe que igual le vai a responder 🙄"
        ),
        Made(
            "gato naranja 🧡", 5, Magic8BallPersonality.CAT, CharacterLook.PROPELLER,
            "hoy no le toca la neurona a cheto 💀 dice 'si obvio' bien seguro o te tira el vaso viendote fijo"
        ),
        Made(
            "Mi tía Coromoto", 9, Magic8BallPersonality.GRANDMA, CharacterLook.FLOWER,
            "Su rosa con brillitos llega al grupo a las 11 de aquí, las 6 en Caracas: 'feliz y bendecido martes familia'. La bendición te la manda en sticker 😂 y su 'sí mi vida, Dios te bendiga' viene reenviado muchas veces. Nadie le responde y ella feliz igual"
        ),
        Made(
            "el de sistemas", 2, Magic8BallPersonality.ROBOT, CharacterLook.THERAPIST,
            "'probaste apagar y prender?', despues 'a mi me anda' y si insistis es de redes"
        ),
        Made(
            "La casamentera", 3, Magic8BallPersonality.LOVESTRUCK, CharacterLook.GRANDMA,
            "La Paqui, mi madrina de Cádiz, casó a medio barrio en los ochenta y dice que las apps de citas la han cambiado por un algoritmo. Sigue emparejando por costumbre, sin mirar, en plan 'a ver si cae alguien' (la semana pasada cayó un señor de la cola del pescado). Te pilla en el portal: 'Sí, y te lo presento el domingo, que tiene piso propio'. Va por una amiga, tres meses deslizando y ni un café."
        ),
        Made(
            "Mamá en el chat", 10, Magic8BallPersonality.THERAPIST, CharacterLook.GRANDMA,
            "Así escribe la mía: 'Sí.' '¿Y sumercé ya almorzó?' '¿Con quién va?' 'Avíseme cuando llegue.' El no sí llega solito, 'No.', y a los dos minutos '¿Está brava?'. Copiado tal cual de los chats de mi mamá, qué pena con ella 🙏"
        ),
        Made(
            "Jefe Buena Onda", 1, Magic8BallPersonality.ROBOT, CharacterLook.MAGICIAN,
            "Viernes 5:52, tu ya con la mochila puesta y se asoma por encima del cubículo: 'algo rapidito para el lunes, sin presión eh' 😭 le pides aumento y 'lo vemos el lunes', y neta ese lunes nunca llega"
        ),
        Made(
            "El Del Wifi ✨", 4, Magic8BallPersonality.PHILOSOPHER, CharacterLook.ZOOMER,
            "Pidió un tinto a las 8 y a las 2 sigue ahí, el cargador del portátil en el único enchufe y la taza vacía de adorno. Usted va a recogerla y 'sí, ya casi me voy... me regala otra vez la clave?' 🤍"
        ),
        Made(
            "mi ex en terapia", 14, Magic8BallPersonality.THERAPIST, CharacterLook.VILLAIN,
            "fue a tres sesiones y ya habla como su psicóloga, todo es 'mi proceso' y hasta la muzza la pide 'desde un lugar sano' 🙃 te valida la pregunta y después te pone un límite, re tranquilo ahre"
        ),
        Made(
            "Audio De 7 Min", 46, Magic8BallPersonality.DRAMA, CharacterLook.SINGER,
            "No, pará, te cuento, esto lo armé porque las chicas me cargan, el 'sí, obvio' te lo da al final de todo, como en mis audios, porque antes te tiene que contar que lo mismo le preguntó la Vane el martes en la pelu mientras le hacía las mechas, y que la Vane está con un tema con el novio que no te puedo contar pero te lo cuento, hola Marta, el jueves a las 4 te anoto, perdón, donde estaba, ah, lo de la Vane, no, pará, primero lo de mi hermana que es más importante, que anoche se hizo sola el flequi"
        ),
        Made(
            "La piñata", 24, Magic8BallPersonality.LOVESTRUCK, CharacterLook.PARTY,
            "Hay fiesta y le tiemblan los picos: 'si... pero quien trae el palo?' Osea obvio lo traigo yo 🤡"
        ),
        Made(
            "El Casi Algo", 1, Magic8BallPersonality.POET, CharacterLook.LOVESTRUCK,
            "No son nada pero ve tu historia a los 4 segundos y te pregunta quién es ese. Era tu primo 😭 te contesta con su 'sin etiquetas, va?' y desaparece tres días. Y tu ahí. Vamos viendo, dice"
        ),
        Made(
            "mi taquero", 13, Magic8BallPersonality.COACH, CharacterLook.CHEF,
            "el de los tacos de la esquina 'con todo, werita?' y la salsa 'no pica' (si pica wey) 🫠"
        ),
        Made(
            "Bolsa De Bolsas", 26, Magic8BallPersonality.GRANDMA, CharacterLook.NEUTRAL,
            "Esa no la botes que sirve!! La bolsa de bolsas de debajo del fregadero, igualita a mi mamá, todo te lo contesta doblando una en triangulito, por si acaso. Yo tengo tres mi amor, una en la cocina, otra en el clóset y otra en la maleta del carro, y el día que a mi esposo se le rompió la del mercado con las papas rodando por toda la calle quién lo salvó?? Yo con mis bolsas!! Ahora el que dobla en triangulito es él, qué molleja 🙌"
        ),
        Made(
            "Parte 2 ✨", 4, Magic8BallPersonality.DRAMA, CharacterLook.ZOOMER,
            "Cara pegada a la cámara y voz de chisme: 'bueno, resulta que sí...' y corta justo ahí, la ✨parte 2✨ la sube si esto llega a mil likes. Así cuenta mi prima hasta lo de su ex y la parte 2 todavía no sale"
        ),
        Made(
            "Mi Cerebro", 8, Magic8BallPersonality.ASTRO, CharacterLook.PHILOSOPHER,
            "Son las 3am, casi dormida y mi cerebro: oye te acordai cuando le cortaste al jefe con un 'ya, chao, te quiero'? Ya, ahora si duérmete tranquila"
        ),
        Made(
            "Jaja Si Obvio", 1, Magic8BallPersonality.SARCASTIC, CharacterLook.ZOOMER,
            "Tu cuando el del taller te explica que es la junta de la... del... y tu 'jaja si obvio' 😭 asintiendo como si supieras qué es un balero"
        ),
        Made(
            "El Narrador", 50, Magic8BallPersonality.COACH, CharacterLook.SINGER,
            "La radio de mi barbería con la pelota todo el día y el narrador te canta el si de una, como jonrón, 'SI señores, esa bola va pa Moca!' y el de la silla brinca con la capa puesta, eso si, en el noveno no se pela a nadie, ni con cita ajajaja"
        ),
        Made(
            "Panchita 🐾", 38, Magic8BallPersonality.DOG, CharacterLook.BOW,
            "Revisa cada caja de zapatos que traigo del trabajo como si fuera la aduana, la huele y SI GUAU GUAU, pasa. Pesa 2 kilos y tiembla hasta dormida pero es mi perra y aquí manda ella"
        ),
        Made(
            "+1000 de aura", 11, Magic8BallPersonality.ZOOMER, CharacterLook.ANGEL,
            "preguntarle a una bola si te cortas el cerquillo tu solo -9999 de aura"
        ),
        Made(
            "Casandra", 3, Magic8BallPersonality.MYSTICAL, CharacterLook.SARCASTIC,
            "¿Os acordáis de la de Troya que avisó lo del caballo y nadie le hizo ni caso? Esa. Te mira con cara de 'te lo dije en marzo, tía' y ya ni insiste, porque sabe que vas a hacer lo que te dé la gana (lleva tres mil años así, no la culpo). En agosto avisé a mis amigas de que la casa rural no tenía aire y allí que fuimos, a cuarenta grados jajajaj"
        ),
        Made(
            "mi roomie 🫠", 13, Magic8BallPersonality.THERAPIST, CharacterLook.MOUSE,
            "se acabó mi yogurt griego, dejo el bote vacío en el refri y todavía me pregunta 'era tuyo?' con la cuchara en la boca. te contesta lo que sea jurando que mañana lo repone y mañana nunca llega wey 🫠 mi roomie no sabe que existe esto JAJSJAJS"
        ),
        Made(
            "Don Chema", 18, Magic8BallPersonality.PHILOSOPHER, CharacterLook.COWBOY,
            "Mi papá es ranchero y nunca acaba un refrán. Camarón que se duerme... y se queda pensando. Bueno, si. Va para mis hermanos, que lo quieren traer en el celular ja ja 👍 Y cuando no sabe: ahorita le digo. Y ya no le dice"
        ),
        Made(
            "la bestie 💅", 16, Magic8BallPersonality.LOVESTRUCK, CharacterLook.DRAMA,
            "girl obvio que si y al que te diga que no pasame su @ que yo me encargo lmaoo"
        ),
        Made(
            "Modo Tiburón", 32, Magic8BallPersonality.COACH, CharacterLook.SHARK,
            "Mi cuñado se levanta a las 4:30 y se baña con agua fría. Te lo cuenta sin que nadie pregunte. Desde que es coach de vida todo es mentalidad. Te contesta con un campeón y una frase de taza. Modo tiburón. Sal de tu zona de confort. En el cumpleaños de mi suegra nos puso a escribir tres metas antes del pastel. Mientras tú duermes alguien más está entrenando, eso también lo dice. Se lo enseñé. Le encantó, que es lo más triste"
        ),
        Made(
            "Recalculando", 18, Magic8BallPersonality.GAMER, CharacterLook.ROBOT,
            "Tres vueltas a la misma glorieta y el GPS muy tranquilo: en 200 metros gire a la derecha... Recalculando... Si. El mío una vez me mandó por terraceria a las 2 de la mañana, fíjese, y ni le cuento lo que vi. Ja ja 👍"
        ),
        Made(
            "El Universo ✨", 4, Magic8BallPersonality.ASTRO, CharacterLook.MYSTICAL,
            "Usted ✨manifestando✨ y el universo como yo en la barra un sábado a las 10: 'sí, a nombre de quién?', le escribe mal el nombre en el vaso y le entrega el de otro. Yo pedí un novio en enero y me llegó un gato jajsjs, se llama Enero"
        ),
        Made(
            "La Hermana Mayor", 8, Magic8BallPersonality.CAT, CharacterLook.SARCASTIC,
            "27 años tiene mi hermano chico y todavía me llama pa preguntarme cuanto rato se deja el arroz o si le escribe a la ex. Y yo: si, y no me hagai repetirlo, preguntale a la mamá po 🙄"
        ),
        Made(
            "El Cuñao", 12, Magic8BallPersonality.SARCASTIC, CharacterLook.DETECTIVE,
            "Sobremesa, palillo en la boca y salta él: yo conozco a uno que... Que si te dan el piso? Te lo dije. Que no? Eso es que no has sabido negociar, macho. Va por mi cuñado. Él dice que va por mí"
        ),
        Made(
            "mis 6 viewers", 2, Magic8BallPersonality.ZOOMER, CharacterLook.GAMER,
            "'W' 'q paso? recien llego', eso es mi chat cuando streameo, son 6 contando a mi vieja que entra solo a poner 'bien hijo', con el delay la W llega cuando ya paso todo, y el rober posta pide saludo todas las noches, nadie sabe quien es el rober"
        ),
        Made(
            "Tía En Navidad", 1, Magic8BallPersonality.GRANDMA, CharacterLook.PARTY,
            "Todavía ni te sirves los romeritos y ya: 'y la tesis?', 'y el novio pa cuando?' Te hace más preguntas que el de migración, todo con su 'yo nomás pregunto' 😭 y neta lo que le digas, para el recalentado ya lo sabe toda la familia"
        ),
        Made(
            "Revisando Jugada", 7, Magic8BallPersonality.GAMER, CharacterLook.DETECTIVE,
            "Te hace la pantallita con los dedos, ve la repetición en cámara lenta y 'SI, es penal!!!' jajajajajaja 🤣 como mi compa el Beto en la cascarita, pide revisión y no hay ni cámara"
        ),
        Made(
            "yo con 2%", 30, Magic8BallPersonality.DRAMA, CharacterLook.ROBOT,
            "contesto rapidito porque el cargador esta en la sala y ya me estoy apag"
        ),
        Made(
            "La Comadre", 1, Magic8BallPersonality.DRAMA, CharacterLook.FLOWER,
            "Vas con tu mamá por las tortillas y ahí esta. Le cuentas que te cambiaste de trabajo y 'ay si, igualito que mi Xime, nomás que ella ya es gerente' y que hasta duerme sus 8 horas 😭 y tu mamá voltea a verte"
        ),
        Made(
            "Chupacabras", 18, Magic8BallPersonality.CONSPIRACY, CharacterLook.PUNK,
            "Sale borroso en todas las fotos desde el 96 y ya está cansado, fíjese. Nadie le cree, ni su compadre el nahual. Ahora se peina, se para bien quietecito a la orilla de la carretera y pide 'si, pero ahorita enfoqueme bien'. Quiere una foto decente para mandarle a su mamá. Mi papá jura que lo vio en el rancho y tampoco traía cámara 👍"
        ),
        Made(
            "el refri", 13, Magic8BallPersonality.POET, CharacterLook.CHEF,
            "lo abres por quinta vez y nada: medio limón, tres bolsitas de salsa de los tacos y un frasco de octubre. 'si hay tortillas, duras pero hay' 🫠 solo quiere que ya cierres la puerta"
        ),
        Made(
            "Mi compi de piso", 3, Magic8BallPersonality.SARCASTIC, CharacterLook.BOW,
            "Pósit en la nevera con letra redondita: 'Sí. PD: la sartén lleva tres días en remojo' y una carita que da más miedo que la PD. La sartén es mía. Plastifica el cuadrante de limpieza y pone punto y coma en un pósit, que eso ya es violencia."
        ),
        Made(
            "Novio Golden 🥹", 48, Magic8BallPersonality.DOG, CharacterLook.LOVESTRUCK,
            "'SIIII amor, que buena idea!!' y su no es 'no amor... pero te traje pan' aww es mi novio 🥹"
        ),
        Made(
            "Chiste de papá", 10, Magic8BallPersonality.NEUTRAL, CharacterLook.COACH,
            "¿Sabe cuál es el café más peligroso? El ex-preso. Mi papá lleva toda mi vida contándolo y se ríe solo. Le cuento que me voy a vivir sola y: 'Sí, mija. ¿Y el ex-preso?' 🙏"
        ),
        Made(
            "master de rol", 2, Magic8BallPersonality.MYSTICAL, CharacterLook.WIZARD,
            "'a ver', tira el d20 atras de la pantalla, lo mira un rato largo y te dice que si, posta nunca vas a saber que salio, y si discutis 'eso lo decide el master', se lo digo todos los sabados a los de mi mesa, la mica siempre quiere hablar con el dragon y el gonza le roba a todos los npc, hasta al tabernero"
        ),
        Made(
            "angel y diablito", 30, Magic8BallPersonality.VILLAIN, CharacterLook.ANGEL,
            "en mis hombros uno dice que no el otro que si y el angelito ya ni pelea nmms"
        ),
        Made(
            "La Del 4B", 8, Magic8BallPersonality.GRANDMA, CharacterLook.BIRD,
            "Te la topai en el ascensor, te cuenta que el del 6A debe tres meses de gastos comunes y bajando la voz 'yo no dije nada' 🙄 y como el conserje le cuenta todo, sabe que pediste delivery tres veces esta semana"
        ),
        Made(
            "Pudo ser un mail", 10, Magic8BallPersonality.DETECTIVE, CharacterLook.THERAPIST,
            "Le agenda una reunión con nueve personas para contestarle si puede salir a almorzar. Alguien dice 'para no quitarles más tiempo' y habla veinte minutos, y otro quiere complementar y dice lo mismo. Al final: 'Sí, pero socialicémoslo en el comité'. Quedo atenta 🙏"
        ),
        Made(
            "Ya mismo", 52, Magic8BallPersonality.ZOOMER, CharacterLook.PARTY,
            "Mi ñaño a las 10 de la noche: 'ñaña, ya mismo salgo', y sigue en pijama 🤭 su 'sí, ya mismo' es en cinco minutos o en marzo"
        ),
        Made(
            "Levante", 3, Magic8BallPersonality.DRAMA, CharacterLook.PROPELLER,
            "Tres días soplando y en Cádiz nadie responde de sus actos. Ni ha oído la pregunta, 'Sí. ¿Qué? Que SÍ', y tu sombrilla ya va por Tarifa. En mi casa todo es culpa del levante (hasta el pelo de mi madre)."
        ),
        Made(
            "Abue Tarotista ✨", 4, Magic8BallPersonality.MYSTICAL, CharacterLook.GRANDMA,
            "Le lee las cartas a todo el que se siente en la mesa de la cocina, hasta al domiciliario, con la baraja española y el tinto servido, y en la misma tirada le cuenta el chisme de la cuadra: 'salió el caballo de copas mi reina, eso es un sí, y la del segundo piso se separó' jajsjs. Es mi abuela de verdad, le conté que la había puesto en una app y se puso los aretes pues 🤍"
        ),
        Made(
            "Ahí Sí Hay Señal", 36, Magic8BallPersonality.ASTRO, CharacterLook.ROBOT,
            "Pucha, arriba de la taza del baño con el brazo estirado, 'ya, pero no te muevas'"
        ),
        Made(
            "El Sonidero", 7, Magic8BallPersonality.GAMER, CharacterLook.ZOOMER,
            "Agarra el micro con eco y a todo volumen: 'SIII se puede!!! Y un saludo pa la familia Treviño de la Indepe y pa los compas que nos escuchan hasta Houston!!!' Así era el sonidero de la boda de mi primo jajajajajaja 🤣"
        ),
        Made(
            "Primo en España", 9, Magic8BallPersonality.PIRATE, CharacterLook.BIRD,
            "Ocho meses en Madrid y ya te suelta 'vale chamo, vas a flipar burda'. En diciembre llega a Caracas pidiendo un bocata"
        ),
        Made(
            "La Del Karaoke", 1, Magic8BallPersonality.DRAMA, CharacterLook.PARTY,
            "Tu amiga que trae el micro desde las 9 y ya te anotó en un dueto que no te sabes 😭 te pasa el otro micro y grita 'siii, esta me la seee' aunque tampoco se la sabe"
        ),
        Made(
            "nunca la pasa", 2, Magic8BallPersonality.DOG, CharacterLook.SHARK,
            "'dejala, dejala que voy yo' y sale gambeteando del arco, ni la ensalada te pasa en el asado"
        ),
        Made(
            "El Último Pedazo", 17, Magic8BallPersonality.CAT, CharacterLook.PARTY,
            "Lleva desde el cumple de las 11 en la mesa de la pega y nadie se atreve wn, dice piola 'si... si nadie mas quiere' y a las 6 se lo lleva el guardia"
        ),
        Made(
            "Lata De Galletas", 20, Magic8BallPersonality.POET, CharacterLook.MAGICIAN,
            "La azul de mi mamá, dice que sí y son puros botones erda!! Dice que no y ahí sí hay galletas 🔥"
        ),
        Made(
            "Mis Perrhijos", 1, Magic8BallPersonality.DOG, CharacterLook.BOW,
            "Lo consulté con Frijol y Canela y ya movieron la colita 😭"
        ),
        Made(
            "El Documental", 19, Magic8BallPersonality.DETECTIVE, CharacterLook.FLOWER,
            "Te mira como a un ñu a punto de cruzar el río. 'Aquí vemos al humano adulto, en su hábitat, preguntándole a una bola si debería comprarse una freidora de aire a las dos de la mañana. Lo está gestionando fatal.' La voz de los documentales de después de comer, me salió un domingo con resaca viendo uno de pingüinos. Probadlo a la hora de la siesta, es de locos 🙂"
        ),
        Made(
            "Playlist 4am ✨", 4, Magic8BallPersonality.POET, CharacterLook.SINGER,
            "Puro ✨despecho✨, 4am: 'siiii... pero ya está con otraaa' a la tercera usted ya llora 🤍"
        ),
        Made(
            "Siempre De Una", 20, Magic8BallPersonality.COACH, CharacterLook.COWBOY,
            "Mudanza un domingo a las 7am? 'De una mi llave!!' 🔥 se le mide a todo, hasta su no es 'hoy no, el sábado de una' y el sábado llega primero, con neverita y todo!!"
        ),
        Made(
            "jefe final", 2, Magic8BallPersonality.VILLAIN, CharacterLook.VIKING,
            "'insensato, creiste que era tan facil?' y se le rompe la armadura y aparece otra barra de vida, te dice que si y cuando ya estas festejando 'esta no era mi forma final', uno asi me mato 14 veces en stream"
        ),
        Made(
            "La Mantita", 37, Magic8BallPersonality.POET, CharacterLook.BUNNY,
            "Domingo de lluvia, vos en el sillón con la serie y cada vez que te querés levantar te dice 'tá, pero quedate un ratito más'. No te deja ni ir a calentar el agua del mate y cuando te das cuenta son las siete, ya es de noche y mañana es lunes"
        ),
        Made(
            "Loro bocón", 9, Magic8BallPersonality.CONSPIRACY, CharacterLook.BIRD,
            "Vive en la cocina de mi abuela en Maracaibo y apenas entra la visita suelta lo que ella dijo antes de abrir la puerta: 'sí, sí... y cuando se van?' JAJAJA y mi abuela con la bandeja del café, roja de la pena"
        ),
        Made(
            "muy de escorpio", 31, Magic8BallPersonality.VILLAIN, CharacterLook.MYSTICAL,
            "primero te pregunta por que queres saber, y despues 'si. y me lo voy a acordar' 🫶 tipo te lo saca en tres años en una pelea por los platos que no tenia nada que ver"
        ),
        Made(
            "El lore familiar", 9, Magic8BallPersonality.PHILOSOPHER, CharacterLook.WIZARD,
            "Para todo tiene un 'igualito que esa vez' y tu ni estabas. Esa vez es la parrilla de mi tío Nelson en 2016, cuando se quemó el toldo del vecino (y de eso no se habla). Si insistes te sale con lo de la olla de las hallacas que mi abuela le prestó a la señora Carmen y nunca volvió. No vas a entender nada pero es burda de importante. Al final te pregunta si estás con mi mamá o con mi tía Morella, que no se hablan desde la boda de mi prima, así que piénsalo bien chamo JAJAJA"
        ),
        Made(
            "Me Oyes?", 51, Magic8BallPersonality.NEUTRAL, CharacterLook.ZOOMER,
            "Domingo, por fin entra la videollamada con mi hija en Miami y yo enseguida: 'sí, sí... me oyes?' Ahí mismo me congelo con la cara pegada a medio estornudo 😆 ay chico, y ella del otro lado haciéndome captura"
        ),
        Made(
            "Estoy A 5", 1, Magic8BallPersonality.ZOOMER, CharacterLook.PROPELLER,
            "Tu amiga desde la cama: 'ya casi llego' 😭"
        ),
        Made(
            "La Montse", 19, Magic8BallPersonality.ROBOT, CharacterLook.BIRD,
            "La paloma de mi plaza, la he dibujado mil veces. Va fatal de los nervios: 'sí sí sí, no, qué ha sido eso?, sí', sale volando por un patinete y vuelve a por tu bocadillo"
        ),
        Made(
            "Yo Ni La Toco", 21, Magic8BallPersonality.COACH, CharacterLook.VIKING,
            "Te grita 'tu solo bro, yo ni la toco' con las dos manos en la barra 💪 cuando ya no sale te la regresa al rack x tu bien y te choca la mano igual, asi le hago con los que entreno"
        ),
        Made(
            "Receta En Video", 35, Magic8BallPersonality.CAT, CharacterLook.CHEF,
            "Oe, el video dura doce minutos y la receta es de su abuela en Huancayo, te pide que te suscribas, sale a comprar el culantro que le faltaba y recién en el último minuto te dice 'sí, a ojo, un chorrito' y que lo dejes reposar. Asu, yo solo quería saber cuanto ají le echo"
        ),
        Made(
            "Camarero Borde", 12, Magic8BallPersonality.PHILOSOPHER, CharacterLook.SARCASTIC,
            "Trapo al hombro y 'sí o no? Venga, que tengo otra mesa'. Si dudas te pone lo de siempre sin preguntar y encima acierta. Es el de mi bar de toda la vida, tal cual, macho. Borde como él solo pero la tapa más grande te la pone a ti jeje"
        ),
        Made(
            "la shipper", 5, Magic8BallPersonality.GRANDMA, CharacterLook.LOVESTRUCK,
            "con el chavo de la tiendita, con el que nomas te dijo buenos dias en el elevador una vez, ya les hizo fan art y todo 💀 'si, y son endgame' y va en el capitulo 14, el de la boda"
        ),
        Made(
            "Te Deja En Visto", 17, Magic8BallPersonality.VILLAIN, CharacterLook.NEUTRAL,
            "Te pone 'ok' a las 3 horas y entremedio subió 6 historias wn"
        ),
        Made(
            "yo mañana", 14, Magic8BallPersonality.PHILOSOPHER, CharacterLook.MATE,
            "decime que esta bien dejarlo para mañana. yo con mi lista re linda de colores sin nada tachado y logo_final_final_ahora_si_v7 abierto desde agosto: 'si, mañana arranco' 🙃 y al cliente 'te lo paso a la tarde' sin aclarar de que día. update: sigue en v7"
        ),
        Made(
            "Ajedrez de plaza", 6, Magic8BallPersonality.NEUTRAL, CharacterLook.PHILOSOPHER,
            "Te sentás frente al viejo de boina de la plaza, termo entre los pies, y todavía estás acomodando las piezas cuando ya te contestó pegándole al reloj sin levantar la vista, 'sí, y te toca', en cinco minutos te liquida como a todos, ja, yo le gané una vez, en 2009, y todavía anda diciendo que estaba resfriado"
        ),
        Made(
            "Yo No Fui, Mijo", 33, Magic8BallPersonality.CONSPIRACY, CharacterLook.MUSHROOM,
            "El duende esconde las llaves, decía mi abuela, y toca dejarle un pedacito de panela. 'Sí, mijo, pero yo no fui' y aparecen en el bolsillo que ya revisé. Ome, cada semana en el taller"
        ),
        Made(
            "Recién Graduada", 25, Magic8BallPersonality.THERAPIST, CharacterLook.GRADUATE,
            "Me gradué hace una semana: 'según mi capítulo 3, sí'. Mi tesis fue sobre cómo decide un grupo de amigas qué pedir de comer un viernes. Conclusión: nadie decide, piden lo de siempre. Lo que no salió en la muestra queda para futuras investigaciones 🥲 Si querés te explico la metodología, son solo cuarenta páginas"
        ),
        Made(
            "Pirata Jubilado", 53, Magic8BallPersonality.PHILOSOPHER, CharacterLook.PIRATE,
            "Vendió el barco y vive cerca del Paseo de los Turistas, el parche es para la siesta y cada tarde su churchill. 'Arr, sí, grumete' 😎 y si le da chance, mae, le cuenta dónde enterró el tesoro, siempre en la Isla del Coco y cada vez en otra punta"
        ),
        Made(
            "El del bus", 10, Magic8BallPersonality.CAT, CharacterLook.COACH,
            "Se sube a las 6 en la Séptima con una bolsa de bocadillos: 'qué pena con ustedes, disculpen la molestia'. Le deja el sí en la pierna a mil pesitos, dos por mil quinientos, y el no se lo regala. Si no lo quiere, lo recoge y le desea un feliz resto de día"
        ),
        Made(
            "mi nona modista", 31, Magic8BallPersonality.GRANDMA, CharacterLook.GRANDMA,
            "con los alfileres en la boca y el centimetro al cuello, marcandote el ruedo. yo estudio diseño de indumentaria pero la que sabe coser es mi nona 🫶 te dice 'si, pero quedate quieta' y te pincha igual"
        ),
        Made(
            "Gracias Bogotá!!", 26, Magic8BallPersonality.LOVESTRUCK, CharacterLook.SINGER,
            "Primera fila en Lima después de 6 horas de cola con mi prima, yo con mi cartel de LIMA TE AMA en escarcha, y sale la muchacha, agarra el micrófono: GRACIAS BOGOTÁ!! Silencio total!! Un señor atrás gritando ES LIMAAA y ella feliz tirando besos. Con todo el amor del mundo y a la ciudad equivocada: 'Sí Bogotá, los amo!!' 🙌 Yo mas nunca hago cartel, pero la entrada si la tengo guardada por si acaso"
        ),
        Made(
            "5 estrellitas", 11, Magic8BallPersonality.PIRATE, CharacterLook.COACH,
            "su atajo es javier prado a las 6, te cuenta que tambien estudio sistemas y al final 'me pones 5 estrellitas causa?' xd"
        ),
        Made(
            "El Colmadero", 50, Magic8BallPersonality.CONSPIRACY, CharacterLook.POET,
            "Don Toño de la esquina con el lápiz en la oreja y el cuaderno del fiao abierto: un salami, un chin de arroz, 'ahí te lo apunto, manito'. Ya va por la hoja 80 ajajaja y la mitad es mía"
        ),
        Made(
            "Evento canónico", 3, Magic8BallPersonality.DRAMA, CharacterLook.ASTRO,
            "Mi compañera de piso lleva en plan tres años volviendo con el mismo chico y todo lo vive como una peli. '¿Que te ha dejado por audio? Es parte de tu arco', y te lo dice abrazada al cojín que le regaló él"
        ),
        Made(
            "EL RUDO", 7, Magic8BallPersonality.DRAMA, CharacterLook.NINJA,
            "En la tercera cuerda: 'NO!!! Y que me chiflen!!!' 🤣 le avientan palomitas y el feliz"
        ),
        Made(
            "Drácula En Valpo", 27, Magic8BallPersonality.VILLAIN, CharacterLook.MAGICIAN,
            "Llegó en barco al puerto como en el libro, subió el cerro a pata y ahora está en tu puerta, sin invitación no entra: 'si, me invitas a pasar?' ojo que si lo invitas se queda a vivir"
        ),
        Made(
            "El Rojo Más Rojo", 15, Magic8BallPersonality.DRAMA, CharacterLook.THERAPIST,
            "Mira la prueba dos minutos, la da vuelta y te dice 'bárbaro, pero el rojo más rojo', que en la pantalla se veía distinto.. Veinte años en la imprenta y el cliente es siempre el mismo. Y tú tenés que sonreír"
        ),
        Made(
            "el asador", 2, Magic8BallPersonality.DOG, CharacterLook.CHEF,
            "'falta', con la pinza en la mano, y si le decis que ya esta te mira mal, como si le hubieras tocado el fuego, y cuando por fin sale la carne, un aplauso para el asador"
        ),
        Made(
            "Meteoróloga", 23, Magic8BallPersonality.POET, CharacterLook.ASTRO,
            "En Arequipa hay sol el 82% de los días y la del clima se aburrió. Ahora le pone porcentaje a todo: '73% de sí, despejado como siempre'. Nunca se compromete al 100%, ni con el Misti sin una nube. Su sección duraba once segundos"
        ),
        Made(
            "Me Voy A Máncora", 44, Magic8BallPersonality.PIRATE, CharacterLook.FLOWER,
            "Yo en la chamba cuando el cliente me grita por tercera vez: 'ya fue, me voy a Máncora' a vender pulseras en la playa. Las pulseras ya las hago los domingos, solo me falta irme"
        ),
        Made(
            "tsundere 😤", 5, Magic8BallPersonality.ZOOMER, CharacterLook.BOW,
            "le pides un favor a mi compañera de practicas y te dice que ni loca, 'n-no me importa... pero si', y a los 10 min ya te llego el archivo x correo con todo y portada 💀"
        ),
        Made(
            "En Mi Era De No", 25, Magic8BallPersonality.MYSTICAL, CharacterLook.FLOWER,
            "Estudié psicología para aprender a decir que no, mor 🥲 si hay torta sí, ve, tampoco soy de piedra"
        ),
        Made(
            "Les Informamos", 45, Magic8BallPersonality.ASTRO, CharacterLook.ANGEL,
            "Sonrisa de turno doble y aviso de cabina: 'señores pasajeros, les informamos que no'"
        ),
        Made(
            "Llegó Mi Mes ✨", 4, Magic8BallPersonality.CAT, CharacterLook.PARTY,
            "Cumplo el 28 y celebro desde el 1, parce 🤍"
        ),
        Made(
            "Filósofo de bar", 6, Magic8BallPersonality.PHILOSOPHER, CharacterLook.BOTTLE,
            "A la tercera cerveza el de la punta de la barra se pone profundo, le preguntás si llueve y te dice 'sí, como todo, hermano', pero qué es la lluvia si no el cielo que no se aguanta más, y que el ajedrez es la vida porque el rey casi no se mueve y los peones se la bancan, y cuando parece que llega a algo se olvida qué le preguntaste y me pide un vaso de agua, lo más sensato que dijo en toda la noche"
        ),
        Made(
            "Chef Reality V2", 34, Magic8BallPersonality.DRAMA, CharacterLook.CHEF,
            "Prueba con la cuchara, la deja caer en el plato y te mira: 'le falta sal'. Ay no, mi cochinita pibil la mandó a eliminación igual. La v1 me hizo llorar 😅"
        ),
        Made(
            "fan numero uno", 5, Magic8BallPersonality.ZOOMER, CharacterLook.BUNNY,
            "mi cantante favorito me dio corazon en una historia y desde ahi ya nos conocemos jsjsjs (tambien le dio a otras 40 mil personas pero eso no importa) le digo mi esposo y hablo de el como si fuera mi vecino, 'anda bien cansado se le nota en la foto', y se que ama los chilaquiles verdes x un live de hace dos años 💀 te digo lo que diria el, 'si, mi esposo lo aprueba', y luego vota x el en los premios porfa son 10 votos diarios"
        ),
        Made(
            "El Carpincho", 15, Magic8BallPersonality.DOG, CharacterLook.BIRD,
            "Lo quiero de presidente en la reunión de copropietarios.. Todos a los gritos por la cuota extra de la fachada y él con un pájaro parado en la cabeza, ni pestañea.. Se ceba un mate y te dice 'Sí, bo. Sentate un rato'"
        ),
        Made(
            "Mi Gata Escribe", 27, Magic8BallPersonality.CAT, CharacterLook.CAT,
            "La Mora se sube al teclado y contesta ella, a un cliente le mandó 'sssssiiiiii77ñ' y el cliente respondió altiro 'perfecto, quedo atento', cuático"
        ),
        Made(
            "La De Ventanilla", 47, Magic8BallPersonality.PHILOSOPHER, CharacterLook.THERAPIST,
            "Atiende de 8 a 12 y a las 11:40 ya no atiende. Llevás todo y 'sí, pero falta la fotocopia'. La traés y 'eso es en la ventanilla 4'. Trabajo al lado de ella hace once años, qué querés que te diga"
        ),
        Made(
            "el tutorial", 2, Magic8BallPersonality.GAMER, CharacterLook.GRADUATE,
            "'parece que estas perdido, presiona A' y vos con 300 horas de juego"
        ),
        Made(
            "el spoilero", 5, Magic8BallPersonality.CONSPIRACY, CharacterLook.VILLAIN,
            "mi primo se avento solo el anime que ibamos a ver juntos y ahora me escribe 'si, el gemelo era el malo' sin que nadie le pregunte 💀 bien odioso"
        ),
        Made(
            "Pasaje En Promo", 49, Magic8BallPersonality.COACH, CharacterLook.FLOWER,
            "Quieres renunciar? 'Amiga, hay promo a San Andrés, es una señal'. Te cortaron mal el pelo? Cartagena, cuatro noches con desayuno. Y yo llevo ocho meses mirando al del gimnasio 🙈 y a ese no le vendo nada"
        ),
        Made(
            "Sherezade", 3, Magic8BallPersonality.VILLAIN, CharacterLook.MYSTICAL,
            "¿Mil y una noches dejando al sultán con la intriga? Eso es oficio, tía (y le funcionó casi tres años, chapó). Aquí hace lo mismo: 'Sí. ¿El porqué? Mañana'. Así me cuenta mi madre los cotilleos del barrio, por entregas y por teléfono, y siempre me cuelga justito en lo mejor jajajaj. Esta noche os toca lo del mercader de Basora, que se asomó a un pozo a buscar su anillo y desde el fondo una voz le dijo, tan tranquila: 'Hassan, ya sé quién se comió los dátiles del califa, y no fuiste tú, fue tu sue"
        ),
        Made(
            "npc del super", 11, Magic8BallPersonality.GAMER, CharacterLook.NEUTRAL,
            "yo en el turno noche del super, 'boleta o factura?'"
        ),
        Made(
            "Mi Monstera", 39, Magic8BallPersonality.THERAPIST, CharacterLook.FLOWER,
            "Es la cuarta de este año y lo sabe. Paso por delante y suelta otra hoja amarilla. 'Si, pero riégame, que ya toca'. Madre mía, a vosotros también se os mueren todas? 😬"
        ),
        Made(
            "El Casero", 12, Magic8BallPersonality.DETECTIVE, CharacterLook.MAGICIAN,
            "La gotera del pasillo, según el, le da carácter al piso. El alquiler, eso sí, el día 1. El termo roto desde marzo y 'sí, sí, ya mando a alguien'. Ocho meses duchándome en el gimnasio"
        ),
        Made(
            "Mi Yayo", 43, Magic8BallPersonality.DOG, CharacterLook.DETECTIVE,
            "A sus 91 escribe en el grupo de la familia TODO EN MAYÚSCULAS y firma cada mensaje por si no sabemos quién es: 'SÍ. UN BESO. EL YAYO'. Le configuré yo el móvil y no hay manera de quitarle las mayúsculas, ostras, ni él quiere. Pregunta cada mañana si ha llovido por ahí y cuándo subes al pueblo, que en agosto son las fiestas. Hasta para decirte que no es majo: 'NO. ABRÍGATE. EL YAYO'"
        ),
        Made(
            "Grupo Del Edif", 40, Magic8BallPersonality.DETECTIVE, CharacterLook.SARCASTIC,
            "Preguntás si mañana cortan el agua y te contestan de quién es el llavero de delfín que apareció en el palier, así es todo, la respuesta esta pero perdida entre un audio del encargado y la del 5to aclarando que ella no fue, y a los cuatro días alguien pone 'ok' y nadie sabe a qué, me hicieron admin sin preguntarme y no me puedo ir porque se ofenden, bueno nada"
        ),
        Made(
            "una partida mas", 11, Magic8BallPersonality.VILLAIN, CharacterLook.GAMER,
            "yo a las 3:40 'si causa, una ranked y ya' y a las 4 estoy pidiendo pollo a la brasa xd"
        ),
        Made(
            "El director", 6, Magic8BallPersonality.DRAMA, CharacterLook.POET,
            "Che, tres meses de ensayo para seis funciones en una sala de cuarenta butacas, y en sus obras no se muere nadie porque no hay presupuesto para tanto, ja, y te contesta con una nota de actuación, 'sí, pero dámelo con más verdad, desde el estómago', y vos ahí, un martes a las once de la noche en un sótano de Boedo, buscando el estómago..."
        ),
        Made(
            "Autocorrector", 29, Magic8BallPersonality.ZOOMER, CharacterLook.ROBOT,
            "Escribo 'illo' y el pesao me pone 'hilo', tan tranquilo. A ti te dice 'sin' y luego '*sí' jajajjaj"
        ),
        Made(
            "tio hawaiano", 16, Magic8BallPersonality.SARCASTIC, CharacterLook.PARTY,
            "en cada party mi tio pide hawaiana con jalapeño pa todos sin preguntar, 'simon, con piña'"
        ),
        Made(
            "Martes 13", 17, Magic8BallPersonality.NEUTRAL, CharacterLook.CAT,
            "Mi mamá hoy: ni te cases ni te embarques. Voy a la pega"
        ),
        Made(
            "el que no aporto", 42, Magic8BallPersonality.ROBOT, CharacterLook.NINJA,
            "3 semanas en visto en el grupo y el dia de la expo en la uni, camisa planchada: 'si, yo lo explico'. si eres tu ntp 💗 pero todos te vimos decir 'como dijo mi compañera' y repetir mi parte"
        ),
        Made(
            "La Gira Del 86", 51, Magic8BallPersonality.SARCASTIC, CharacterLook.SINGER,
            "Mi papá tocaba el tres en un conjunto de son y para todo tiene una de la gira del 86. Te mira por encima de los espejuelos: 'sí, como en Santiago en el 86'. Pruebas? 😆 Las fotos las tiene el trompetista, que vive en Matanzas y no tiene teléfono. Y él toda la tarde en el portal del dominó, en la silla que cojea aunque le traigo otra, pidiendo otro cafecito"
        ),
        Made(
            "la micro influ", 16, Magic8BallPersonality.ZOOMER, CharacterLook.BOW,
            "holaaa chicos les cuento 💅 y ahi va la encuesta en historias: 'ustedes votaron, si 72%' (votaron 18 y 5 son cuentas de su mama). es mi prima y la amo pero girl, le escribio una marca pa una colab, era el salon de uñas de nuestra tia, y lo anuncio con cuenta regresiva lmaoo"
        ),
        Made(
            "El Año Que Viene", 22, Magic8BallPersonality.PIRATE, CharacterLook.MATE,
            "Desde el 78 esperando ganar una final... Perdimos cinco finales, una con gol en contra en el descuento, no me hagan acordar 😂 Sí querido, pero por penales y en el minuto 94... El año que viene, el año que viene. La bufanda es la misma desde entonces y no se lava, por cábala 👏👏 Y no te muevas de donde estas que da suerte"
        ),
        Made(
            "Metalero Tierno", 28, Magic8BallPersonality.LOVESTRUCK, CharacterLook.VILLAIN,
            "El más grande del pit, 6'5 y todo de negro, te contesta con un growl, 'GRAAAH SÍ', y enseguida bajito 'Dios te bendiga, cuídate mucho'. En el último show me ayudó a buscar las llaves en el piso con la linterna del cel haha 🤘"
        ),
        Made(
            "Like Si Lloraste", 19, Magic8BallPersonality.GAMER, CharacterLook.FLOWER,
            "Se quedó en 2013, en su foto de espejo con flash. 'Ola k ase', 'zi', y te pide like aunque le hayas preguntado por el alquiler. Tengo una amiga así y no pienso decírselo"
        ),
        Made(
            "El De La Maquina", 21, Magic8BallPersonality.PIRATE, CharacterLook.ZOOMER,
            "Lleva 40 min en la prensa con el cel, 'ya es la ultima' 💪"
        ),
        Made(
            "Míster De Barra", 12, Magic8BallPersonality.SARCASTIC, CharacterLook.COACH,
            "Te arregla la alineación en una servilleta mientras ve el partido de pie con el cortado. Hemos ganado 4 a 0 y te suelta un 'sí, yo ahí metía dos puntas'. Como no le des la razón pide otra servilleta. Es el del bar de abajo, macho"
        ),
        Made(
            "Profe de cálculo", 23, Magic8BallPersonality.ASTRO, CharacterLook.GRADUATE,
            "Yo, según mis alumnos de ingeniería de las 7am: 'sí, pero muéstrame el procedimiento'. El semestre pasado aprobó el 64% y fue récord. Ya pues, lo demás queda como ejercicio"
        ),
        Made(
            "Cleopatra", 24, Magic8BallPersonality.CAT, CharacterLook.DRAMA,
            "Le cuentas de tu crush y te pregunta si es romano. Ya tuvo dos. 'Ojo con los romanos', dice la que llegó enrollada en una alfombra. En mi grupo me piden consejos de amor a mi 🤡 miren mi historial"
        ),
        Made(
            "El Del Lado B", 28, Magic8BallPersonality.CAT, CharacterLook.ZOOMER,
            "No te vende un disco hasta que le escuches el lado B entero con los audífonos que solo suenan de un lado. Huele los discos como si fueran vino. 'Nah, ese no es pa ti todavía' 🤘 Inspirado en mi boss"
        ),
        Made(
            "Dante", 6, Magic8BallPersonality.COACH, CharacterLook.VILLAIN,
            "Che, visita guiada por el infierno, paraguas en alto para que nadie se pierda, y te contesta sin frenar, 'sí, siga por el séptimo, ese es el de los que contestan audios con texto', el de los que abren un caramelo en el silencio del segundo acto queda más abajo, y que no se separen del grupo, que él hace este recorrido desde el 1300, con Virgilio atrás contando cabezas..."
        ),
        Made(
            "Profe, Una Duda", 41, Magic8BallPersonality.CONSPIRACY, CharacterLook.GRADUATE,
            "7:55pm en la u, todos con la maleta al hombro y levanta la mano: 'otra cosita, profe'. Qué boleta"
        ),
        Made(
            "El lector pesao", 29, Magic8BallPersonality.POET, CharacterLook.THERAPIST,
            "Te pausa la peli cada diez minutos pa decirte que en el libro no era así. 'Sí, pero en el libro llovía', y se sube las gafas a la cabeza para contarte el capítulo que se saltaron. Acaba prestándote el libro, eso sí, como le dobles una esquina te deja de hablar. Trabajo en una librería y me viene uno así cada sábado"
        )
    )

    /** Who made them, by the number each character names its maker by. */
    private val MAKERS = mapOf(
        1 to "ferchis.mtz",
        2 to "tincho.tiradados",
        3 to "nuria.lee",
        4 to "valen.astral",
        5 to "dany.mp4",
        6 to "elnicof",
        7 to "el_rudo_91",
        8 to "cata.sinfiltro",
        9 to "gabo.vzla",
        10 to "laucardenas_",
        11 to "renzito_404",
        12 to "javi_zgz",
        13 to "luuwera",
        14 to "fede.manana",
        15 to "gus_80",
        16 to "itsyaz.la",
        17 to "benjaa_",
        18 to "chuy_poblano",
        19 to "laiadibuixa",
        20 to "andres.curramba",
        21 to "rodri.legday",
        22 to "cacho_1969",
        23 to "ing.marco.aqp",
        24 to "ale.y.ya",
        25 to "isaocampo_",
        26 to "yolimar.c",
        27 to "seba_cr",
        28 to "jandro_787",
        29 to "irenita.svq",
        30 to "user5839201",
        31 to "flor.escorpiana",
        32 to "pepegtz",
        33 to "jhonmono_",
        34 to "karlitaa.pech",
        35 to "diego.aji",
        36 to "coniii.v",
        37 to "caro.mvd",
        38 to "lalo.y.panchita",
        39 to "beaa.gm",
        40 to "julietaa.r",
        41 to "santi_rmz",
        42 to "majo.jpg",
        43 to "toni_mf",
        44 to "fio.rq",
        45 to "andreina.vp",
        46 to "solee.ibarra",
        47 to "dario.lanus",
        48 to "sofii.rdz",
        49 to "mafe.rg",
        50 to "wilkin.pz",
        51 to "osmany.rdgz",
        52 to "dome.slzr",
        53 to "fabi_ulate"
    )

    private const val LATENCY_MS = 500L
}
