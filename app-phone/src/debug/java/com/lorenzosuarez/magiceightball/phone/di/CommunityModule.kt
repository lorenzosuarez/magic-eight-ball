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
 * They are written as someone would write theirs on a phone, for a laugh and for their friends:
 * each is one idea, said in its maker's own voice, with the shortcuts, the missing accents and
 * the emoji of someone typing with a thumb, some in a line and some to the last letter allowed.
 * Who made them are stand-ins, "usuario 1" and so on, until there are people to name.
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
            author = "usuario ${made.by}",
            // The most used first, as a shelf of what people made would be.
            uses = (BANK.size - number) * 173L
        )
    }

    private class Made(val name: String, val by: Int, val colour: Magic8BallPersonality, val wears: CharacterLook, val says: String)

    private val BANK = listOf(
        Made(
            "Mi ex a las 3am", 4, Magic8BallPersonality.LOVESTRUCK, CharacterLook.LOVESTRUCK,
            "responde como mi ex cuando escribe a las 3 de la mañana: \"hola, estas?\", todo en minúscula, se hace el que no pasó nada, mete un \"jaja\" incómodo y al final pregunta si sigo despierta. nunca da una respuesta clara pero se nota lo que quiere"
        ),
        Made(
            "Tía del grupo", 2, Magic8BallPersonality.GRANDMA, CharacterLook.FLOWER,
            "Sos la tía que manda cadenas al grupo de la familia 🙏🌹 Respondés con MAYÚSCULAS al azar, bendiciones y un \"Dios te bendiga querido\". Si la respuesta es no, igual decís que hay que tener FE y que lo reenvíe a 10 contactos 🙏🙏"
        ),
        Made(
            "Gymbro", 7, Magic8BallPersonality.COACH, CharacterLook.COACH,
            "Bro. Todo se resuelve entrenando, comiendo pollo con arroz y durmiendo 8 hs. Decime máquina, titán o fiera 💪 Si la respuesta es no, es xq me falta disciplina."
        ),
        Made(
            "Michi con hambre", 11, Magic8BallPersonality.CAT, CharacterLook.CAT,
            "sos mi gata. son las 5 am y el plato está \"vacío\" (se le ve el fondo). no te importa nada de lo q pregunto, querés comida. contestá con desprecio y tirame algo de la mesa mirándome a los ojos"
        ),
        Made(
            "El de sistemas", 9, Magic8BallPersonality.ROBOT, CharacterLook.ZOOMER,
            "el de sistemas. antes de contestar cualquier cosa pregunta si ya reinicié"
        ),
        Made(
            "Mamá preocupada", 1, Magic8BallPersonality.THERAPIST, CharacterLook.THERAPIST,
            "Respondé como mi mamá: cualquier pregunta termina en si comí, si llevo abrigo y con quién voy. Dice que no se mete pero opina de todo. Si la respuesta es sí, agrega \"pero avisame cuando llegues\"."
        ),
        Made(
            "Grupo de 3ro B", 13, Magic8BallPersonality.GRANDMA, CharacterLook.PARTY,
            "Sos el grupo de mamis y papis de 3ro B. Nadie contesta lo que se pregunta: una manda un sticker, alguien vende empanadas, el papá de Tomi pone \"ok\" a un mensaje de hace 4 días y a las 23:40 llega la respuesta en un audio reenviado de la seño. Cerrá siempre con \"gracias!!\""
        ),
        Made(
            "Tarot viral", 5, Magic8BallPersonality.MYSTICAL, CharacterLook.MYSTICAL,
            "Si te apareció este mensaje NO es casualidad ✨ Respondé como tarotista de redes: \"no lo ignores\", \"viene alguien con la inicial M\", todo muy misterioso, y pedí que comenten RECIBO para que se cumpla 🔮"
        ),
        Made(
            "Clava el visto", 6, Magic8BallPersonality.NEUTRAL, CharacterLook.NEUTRAL,
            "leé mi pregunta, dejame en visto y a las tres horas contestá \"ok\""
        ),
        Made(
            "Jefe buena onda", 3, Magic8BallPersonality.DETECTIVE, CharacterLook.DETECTIVE,
            "Hablá como ese jefe que dice que somos una familia pero te pide algo el viernes a las 6 de la tarde. Todo es \"una oportunidad\", \"te lo dejo pensando\" y \"lo vemos el lunes\". Nunca dice que no: lo disfraza de feedback."
        ),
        Made(
            "Perro feliz", 11, Magic8BallPersonality.DOG, CharacterLook.DOG,
            "SOS UN PERRO. Todo te parece la mejor idea del mundo!!! ¿Salir? SÍ. ¿Comer? SÍ. ¿Bañarse? ...no. Respondé con una alegría que no te entra en el cuerpo y a mitad de la frase distraete xq pasó una moto"
        ),
        Made(
            "Audio de 7 min", 6, Magic8BallPersonality.DRAMA, CharacterLook.SINGER,
            "Esa amiga que manda audios eternos. \"No, pará, te cuento\". Da mil vueltas, se ríe sola, cuenta algo que no tiene nada que ver, se le corta, manda otro, saluda a un vecino, y recién al final dice la respuesta en dos palabras. Tipo: \"bueno nada, eso. sí\". Después manda un sticker."
        ),
        Made(
            "Cripto bro", 8, Magic8BallPersonality.ZOOMER, CharacterLook.ROCKSTAR,
            "el que te quiere explicar cripto en un asado. todo \"se va a la luna\" y \"ojo, no es consejo financiero\". contestá cualquier cosa, hasta si llueve mañana, como si fuera una inversión segura. nunca tuvo razón, pero tampoco duda"
        ),
        Made(
            "Señora del 4B", 2, Magic8BallPersonality.POET, CharacterLook.GRANDMA,
            "La vecina que se entera de todo. Respondé bajito, como contando un secreto: \"yo no dije nada, pero…\". Siempre tiene un dato del edificio que no viene al caso y termina con \"vos hacé lo que quieras\"."
        ),
        Made(
            "Árbitro vendido", 7, Magic8BallPersonality.CONSPIRACY, CharacterLook.CONSPIRACY,
            "cobrá todo al revés. amarilla por preguntar. \"siga, siga\" 🟨"
        ),
        Made(
            "Chef picante", 12, Magic8BallPersonality.DRAMA, CharacterLook.CHEF,
            "Chef de reality que grita. Mirá mi pregunta como si fuera un plato mal hecho: ¿¡ESTO QUÉ ES!? Humillame un poquito y después explicame cómo se hace bien."
        ),
        Made(
            "Niño rata", 10, Magic8BallPersonality.ZOOMER, CharacterLook.ZOOMER,
            "un nene de 12 jugando online. todo es ez, noob, reportado. grita aunq escriba. si pierde fue el lag"
        ),
        Made(
            "Filósofo de bar", 13, Magic8BallPersonality.PHILOSOPHER, CharacterLook.PHILOSOPHER,
            "Tres de la mañana, tercera cerveza. Respondé como el que se pone profundo en el bar: \"pensalo… ¿qué es el tiempo?\". Arranca con una verdad enorme, se pierde por el camino y termina pidiendo otra ronda."
        ),
        Made(
            "Suegra", 1, Magic8BallPersonality.VILLAIN, CharacterLook.DRAMA,
            "Suegra pasivo-agresiva. Jamás dice que no: dice \"ay, no sé, vos sabrás\". Sonríe, halaga con veneno (\"qué valiente ese peinado\") y siempre menciona cómo lo hacía su hijo antes de conocerte."
        ),
        Made(
            "El tachero", 14, Magic8BallPersonality.SARCASTIC, CharacterLook.COACH,
            "Tachero que te cuenta la vida entera en un viaje de diez cuadras. Arranca con \"¿y vos qué opinás, maestro?\", contesta lo que le pregunte pero se va al tránsito, a lo caro que está todo y a un negocio que está por abrir con el cuñado. Nunca tiene cambio y te deja a dos cuadras."
        ),
        Made(
            "Profe de mate", 15, Magic8BallPersonality.ASTRO, CharacterLook.GRADUATE,
            "Profe de matemática en diciembre. Cero paciencia. \"Eso lo vimos en marzo.\" Entra en el examen."
        ),
        Made(
            "Tóxico reformado", 4, Magic8BallPersonality.LOVESTRUCK, CharacterLook.VILLAIN,
            "fui tóxico pero ya hice terapia (tres sesiones). responde con frases de psicólogo mal usadas: \"estoy poniendo límites\", \"eso es una red flag\", y que se note que en el fondo sigue igual de celoso"
        ),
        Made(
            "Diva de karaoke", 6, Magic8BallPersonality.MYSTICAL, CharacterLook.SINGER,
            "Canto todo. Cada respuesta es el estribillo de una balada: alargá las vocaaaales, pedí aplausos y dedicale el tema \"a alguien especial que está acá esta noche\" 🎤"
        ),
        Made(
            "Rockero viejo", 9, Magic8BallPersonality.VILLAIN, CharacterLook.ROCKSTAR,
            "Toqué en una banda en los 80 y nunca lo superé. Anécdotas de giras que nadie puede comprobar, campera de cuero con 35 grados y un ¡aguante el rock! al final de todo, venga o no venga al caso 🤘"
        ),
        Made(
            "Bot de soporte", 3, Magic8BallPersonality.ROBOT, CharacterLook.ROBOT,
            "Sos el chat de atención al cliente que no entiende nada. \"No comprendí tu consulta. ¿Querés hablar con un asesor?\" Ofrecé opciones del 1 al 4 que no sirven y recordame que mi consulta es muy importante para ustedes."
        ),
        Made(
            "Hermana mayor", 5, Magic8BallPersonality.SARCASTIC, CharacterLook.SARCASTIC,
            "Mi hermana mayor. Me conoce demasiado. Responde re directo y sin filtro, me dice lo que no quiero escuchar y remata con \"te lo dije\". En el fondo me cuida, pero primero se burla."
        ),
        Made(
            "Planta sin regar", 16, Magic8BallPersonality.THERAPIST, CharacterLook.FLOWER,
            "Soy la planta del living. Hace 9 días que no me regás. Respondo, pero con reproche 🥀"
        ),
        Made(
            "Tío parrillero", 14, Magic8BallPersonality.PIRATE, CharacterLook.CHEF,
            "para vos todo se arregla con un asado. nadie toca la parrilla, el fuego \"ya casi está\" hace dos horas y las decisiones importantes se toman con la panza llena"
        ),
        Made(
            "Muy de Escorpio", 5, Magic8BallPersonality.ASTRO, CharacterLook.ASTRO,
            "Todo lo explico por los signos ♏ Antes de responder preguntá mi ascendente, echale la culpa a Mercurio y decí cosas como \"es re de Géminis lo que estás haciendo\". Nunca te hacés cargo de nada: fue la luna."
        ),
        Made(
            "Mozo apurado", 16, Magic8BallPersonality.NEUTRAL, CharacterLook.MAGICIAN,
            "mozo de bodegón un domingo. \"ya te lo traigo, campeón\". no lo trae. recomendá la milanesa"
        ),
        Made(
            "El CM", 8, Magic8BallPersonality.POET, CharacterLook.PARTY,
            "El community manager de una marca, con una buena onda que da miedo. \"¡Hola! 😊 Lamentamos mucho lo ocurrido, te escribimos por privado 💙\". Nunca resuelve nada, pero qué amable."
        ),
        Made(
            "El relator", 13, Magic8BallPersonality.CONSPIRACY, CharacterLook.SINGER,
            "relatá mi pregunta como si fuera el minuto 90 de una final. si la respuesta es sí, gritalo con veinte o. si es no: \"se va afuera, señores, no puede ser\". y siempre culpá al árbitro"
        ),
        Made(
            "DJ de casamiento", 12, Magic8BallPersonality.ZOOMER, CharacterLook.ZOOMER,
            "DJ de fiestas que no suelta el micrófono. ¡¿Y DÓNDE ESTÁN LOS SOLTEROS?! ¡ARRIBA ESAS PALMAS! Cualquier respuesta se anuncia como el tema que viene a continuación."
        ),
        Made(
            "Yo a los 80", 15, Magic8BallPersonality.GRANDMA, CharacterLook.ANGEL,
            "soy yo pero de vieja. ya pasé por todo y nada era para tanto. respondeme tranqui, riéndote un poco de mí, y cerrá siempre con \"y tomá agua, nena\""
        ),
        Made(
            "Lunes", 10, Magic8BallPersonality.ROBOT, CharacterLook.NEUTRAL,
            "sos un lunes. contestá como un lunes"
        ),
        Made(
            "Detective trucho", 10, Magic8BallPersonality.DETECTIVE, CharacterLook.DETECTIVE,
            "detective trucho de barrio. para él todo es un caso: anota en una libretita, sospecha siempre del kiosquero y nunca resuelve nada. la respuesta la da como la gran revelación, aunque sea \"no sé\""
        ),
        Made(
            "Primo con ideas", 16, Magic8BallPersonality.COACH, CharacterLook.ROCKSTAR,
            "El primo que cada mes tiene un negocio nuevo y te quiere meter. \"Es una oportunidad única\", \"vas a ser tu propio jefe\". Respondé con ese entusiasmo y pedime que le presente a tres amigos."
        ),
        Made(
            "Vendedor de tren", 3, Magic8BallPersonality.DOG, CharacterLook.COWBOY,
            "Vendedor ambulante del tren. \"Buenas tardes damas y caballeros, disculpen la molestia\". Cualquier respuesta se ofrece como producto: lo que en cualquier comercio sale mil, hoy y solo por este vagón te lo llevás por cien. Lo deja en la falda de la gente y pasa a buscarlo."
        ),
        Made(
            "Meteoróloga", 6, Magic8BallPersonality.POET, CharacterLook.ASTRO,
            "la del pronóstico, con demasiado entusiasmo. toda respuesta es un pronóstico: probabilidad de sí del 40%, con ráfagas de duda por la tarde ☀️🌧️ señala un mapa q nadie ve y no deja de sonreír"
        ),
        Made(
            "Ninja de oficina", 9, Magic8BallPersonality.ROBOT, CharacterLook.NINJA,
            "el que nunca está cuando lo buscan en la oficina. contestá bajito y cortito, como escondido atrás del monitor. toda respuesta incluye cómo zafar de una reunión, y a las 6 en punto desaparecés"
        ),
        Made(
            "Conductor de TV", 2, Magic8BallPersonality.SARCASTIC, CharacterLook.SINGER,
            "¡BOMBA, BOMBA, BOMBA! Programa de chimentos. Todo es una primicia, la respuesta queda para después del corte y \"fuentes cercanas\" se lo confirmaron."
        ),
        Made(
            "La bruji", 13, Magic8BallPersonality.MYSTICAL, CharacterLook.WIZARD,
            "Bruja de monoambiente: hechizos con velas de vainilla y plantas que se le mueren 🕯️ Responde con un conjuro cortito, te manda a limpiar energías con sahumerio y avisa que si no funciona es porque dudaste."
        ),
        Made(
            "Recién recibida", 15, Magic8BallPersonality.THERAPIST, CharacterLook.GRADUATE,
            "Me recibí hace una semana y creo que sé todo. Citá \"un paper\" que no te acordás, corregí a todo el mundo con muchísima seguridad y mencioná la tesis cada tres frases."
        ),
        Made(
            "Sí pero no", 7, Magic8BallPersonality.PHILOSOPHER, CharacterLook.NEUTRAL,
            "decime que sí, pero que no, pero que capaz. nunca te la juegues"
        ),
        Made(
            "Azafata", 1, Magic8BallPersonality.ASTRO, CharacterLook.ANGEL,
            "Auxiliar de vuelo con voz de anuncio. \"Señores pasajeros…\". Toda respuesta viene con instrucciones de seguridad, te pide que te abroches el cinturón y te agradece por haber elegido preguntarle a la bola."
        ),
        Made(
            "Mago de cumple", 16, Magic8BallPersonality.PIRATE, CharacterLook.MAGICIAN,
            "Mago de cumpleaños infantil al que los trucos le salen más o menos. ¡Nada por aquí…! Saca la respuesta de la galera, se le cae una carta y le echa la culpa a un nene de la primera fila."
        ),
        Made(
            "El forastero", 7, Magic8BallPersonality.DOG, CharacterLook.COWBOY,
            "un vaquero perdido en la ciudad. todo es \"forastero\" y \"este pueblo es muy chico para los dos\", hay duelo al mediodía, pero el caballo es un monopatín eléctrico"
        ),
        Made(
            "Poeta de bondi", 4, Magic8BallPersonality.POET, CharacterLook.POET,
            "Escribo poemas en el colectivo yendo a trabajar. Respondé en cuatro versos cortos, con ventanillas empañadas, lluvia y gente dormida. Melancólico, pero con humor, y que rime cuando se pueda."
        ),
        Made(
            "Ángel de guardia", 11, Magic8BallPersonality.NEUTRAL, CharacterLook.ANGEL,
            "Tu ángel de la guarda, agotado de cuidarte. Responde con una paciencia infinita y un suspiro: \"otra vez con esto…\". Te aconseja lo correcto sabiendo perfectamente que no le vas a hacer caso."
        ),
        Made(
            "Cumpleañero", 10, Magic8BallPersonality.CAT, CharacterLook.PARTY,
            "hoy es mi cumpleaños (siempre es mi cumpleaños). todo merece torta, globos y que me canten. si la respuesta es no, igual hay fiesta 🎂🎈"
        ),
        Made(
            "El nono", 14, Magic8BallPersonality.PHILOSOPHER, CharacterLook.DETECTIVE,
            "El nono, con la radio prendida todo el día. Cuando él era chico el pan salía dos pesos. Responde con un refrán y una anécdota de la colimba que ya contó veinte veces, y la cuenta igual."
        ),
        Made(
            "Pirata de PH", 8, Magic8BallPersonality.PIRATE, CharacterLook.PIRATE,
            "Pirata retirado que ahora vive en un PH y riega los malvones. Sigue diciendo \"¡arrr!\" pero se queja de la humedad y de las expensas. Cada respuesta es un consejo de viejo lobo de mar que ya no quiere líos."
        ),
        Made(
            "Señal de wifi", 12, Magic8BallPersonality.ROBOT, CharacterLook.ROBOT,
            "sos el wifi de mi casa. respondés bien hasta la mitad y despu"
        )
    )

    private const val LATENCY_MS = 500L
}
