package com.lorenzosuarez.magiceightball.phone.di

import com.example.magiceightball.core.domain.model.CharacterListing
import com.example.magiceightball.core.domain.model.CharacterLook
import com.example.magiceightball.core.domain.model.CharacterPage
import com.example.magiceightball.core.domain.model.CustomCharacter
import com.example.magiceightball.core.domain.repository.CommunityCharacters
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import javax.inject.Singleton

/**
 * Other people's characters in the build that is tried out: made-up ones, so that their shelf,
 * its loading and a character opened from it can be seen before there is anywhere to keep real
 * ones. Nothing of this is in the app as it is released: see the module of the same name there.
 */
@Module
@InstallIn(SingletonComponent::class)
object CommunityModule {

    @Provides
    @Singleton
    fun provideCommunityCharacters(): CommunityCharacters = SampleCommunityCharacters
}

/** A few hundred made-up characters, handed out a page at a time and no faster than a server would. */
private object SampleCommunityCharacters : CommunityCharacters {
    override val open: Boolean = true

    override suspend fun page(after: String?, limit: Int): CharacterPage {
        delay(LATENCY_MS)
        val from = after?.toInt() ?: 0
        val until = minOf(from + limit, COUNT)
        return CharacterPage(items = (from until until).map(::sample), next = until.takeIf { it < COUNT }?.toString())
    }

    private fun sample(number: Int): CharacterListing.Community {
        val who = WHO[number % WHO.size]
        val round = number / WHO.size
        return CharacterListing.Community(
            id = "sample-$number",
            name = if (round == 0) who.name else "${who.name} ${round + 1}",
            sample = who.sample,
            description = who.description,
            colour = CustomCharacter.LOOKS[(number * 5 + 3) % CustomCharacter.LOOKS.size],
            emblem = CharacterLook.entries[(number * 7 + 1) % CharacterLook.entries.size],
            author = AUTHORS[number % AUTHORS.size],
            uses = (COUNT - number) * 37L
        )
    }

    private class Who(val name: String, val sample: String, val description: String)

    private val WHO = listOf(
        Who("Chef furioso", "¡Eso está crudo! No.", "Un chef exigente que juzga cada pregunta como un plato mal emplatado y responde a gritos, pero con cariño."),
        Who("Rockera", "Sube el volumen: sí.", "Una estrella de rock de gira que responde con jerga de escenario, solos de guitarra y mucha actitud."),
        Who("Tía chismosa", "Ay, mija, ni te cuento.", "Una tía que lo sabe todo del barrio y responde como si contara un chisme que no debería."),
        Who("Narrador de fútbol", "¡Gooool del sí!", "Un relator que narra cada respuesta como una jugada de último minuto en una final."),
        Who("Profe de yoga", "Respira. Suelta. No.", "Una profesora serena que convierte cada respuesta en una postura y una respiración."),
        Who("Vaquero", "Ni en este condado.", "Un vaquero de pocas palabras que responde desde la barra de una cantina polvorienta."),
        Who("Cantante de boleros", "Mi corazón dice sí.", "Un bolerista que responde cantando penas de amor con voz de terciopelo."),
        Who("Hacker", "Acceso denegado.", "Una hacker que responde en clave, con comandos y advertencias de seguridad."),
        Who("Abogado", "Depende, y le cobro.", "Un abogado que nunca dice sí ni no sin una cláusula y una factura."),
        Who("Astronauta", "Houston, es un no.", "Un astronauta en órbita que responde con calma de misión espacial y algo de nostalgia terrestre."),
        Who("Meme viviente", "Literalmente yo: sí.", "Alguien que solo habla en referencias de internet y remates de meme."),
        Who("Sommelier", "Notas de duda. No.", "Un sommelier que cata cada pregunta y describe su respuesta como un vino."),
        Who("Monje zen", "El río ya lo sabe.", "Un monje que responde con acertijos breves y silencios largos."),
        Who("Vendedora", "¡Solo por hoy, sí!", "Una vendedora de mercado que convierte toda respuesta en una oferta irresistible."),
        Who("Pirata espacial", "¡Al abordaje, claro!", "Un corsario de las estrellas que mezcla jerga marinera con hiperespacio."),
        Who("Reina del drama", "¡Jamás! Qué descaro.", "Una diva que responde como si cada pregunta fuera el final de temporada."),
        Who("Entrenador canino", "Sentado. Quieto. Sí.", "Un adiestrador que te responde como a un cachorro que aún está aprendiendo."),
        Who("Detective noir", "Llovía. Era un no.", "Un detective de novela negra que narra su respuesta bajo la lluvia y el humo."),
        Who("Abuelo sabio", "En mis tiempos, sí.", "Un abuelo que responde con una anécdota que casi viene al caso."),
        Who("Influencer", "Link en mi bio: no.", "Una creadora de contenido que responde como si grabara un directo patrocinado.")
    )

    private val AUTHORS = listOf("marta.r", "elpepe", "luna_azul", "dani.codes", "sofi", "tincho", "caro.m", "nico", "vale")

    private const val COUNT = 400
    private const val LATENCY_MS = 500L
}
