# Solicitud de acceso a producción

Para enviar el 27 de noviembre de 2026, cuando se cumplan los 14 días con 12 testers. Las preguntas son las del formulario de Play Console (Panel → "Apply for production"), leídas el 17 de noviembre de 2026; las de las partes 2 y 3 salen de la página de ayuda de Google ([answer/14151465](https://support.google.com/googleplay/android-developer/answer/14151465)), porque el formulario solo muestra la primera parte hasta que se rellena. Cada respuesta escrita admite 300 caracteres y no se publica en Google Play.

Google rechaza sobre todo por poca participación de los testers: pide que hayan usado las funciones de la app y que se guarde constancia de sus comentarios.

## Datos de la prueba al 17 de noviembre

- **Testers:** 12 inscritos desde el 13 de noviembre. Si uno se da de baja, el contador de 14 días vuelve a empezar.
- **Firebase Analytics, últimos 28 días** (solo quien dio su consentimiento, así que es un mínimo): 28 usuarios; 27 hicieron preguntas, 375 en total; 13 min 32 s de uso medio; 3,5 sesiones con interacción por usuario; 100 % de usuarios sin fallos.
- **Funciones usadas:** elegir personaje, 3 personas; compartir una respuesta, 3; ajustes, 4; modo fiesta, 2; veredicto del día, 2; valorar una respuesta, 1; compartir la app, 2. Una desinstalación.
- **Uso diario:** bajando; 3 usuarios el 16 de noviembre.
- **Comentarios privados en Play** (Calificaciones y reseñas → Testing feedback): ninguno.

## Antes del 27

1. **Invitar a 3 o 4 testers más**, para no depender de que ninguno de los 12 se dé de baja.
2. **Pedir a los testers que usen la 1.3.0** en cuanto Google la apruebe, y guardar sus respuestas (capturas del grupo, correos). Mensaje propuesto:

> ¡Hola! Gracias por probar Magic AI Ball. En unos días os llega la versión 1.3.0 y necesito vuestra ayuda, 5 minutos:
> 1. Elige otro personaje y hazle una pregunta.
> 2. Inicia sesión con Google y crea tu propio personaje.
> 3. Prueba el modo fiesta con alguien.
> 4. Comparte una respuesta.
>
> Después contadme qué os gusta y qué no, por aquí o en Google Play ("Enviar comentarios privados" en la ficha de la app). Y por favor, no salgáis del programa de pruebas hasta el 27 de noviembre.

3. **Volver a leer los números** el 26 de noviembre y actualizar la respuesta sobre la participación.

## 1. About your closed test

**How did you recruit users for your closed test? For example, did you ask friends and family, or use a paid testing provider?**

> FALTA: solo lo sabe el dueño. Plantilla: "We invited friends, family and colleagues personally, by WhatsApp and email, with the Play opt-in link, looking for adults who use Android every day. N joined, and we kept them in a group to send updates and collect feedback."

**How easy was it to recruit testers for your app?** (Very difficult, Difficult, Neither difficult or easy, Easy, Very easy)

> FALTA: según cómo lo vivió el dueño.

**Describe the engagement you received from testers during your closed test.** Include whether or not testers utilized all of the features in your app, and whether tester usage was consistent with how you would expect a real user to use your app. If not, describe the differences you would expect to see.

> Analytics (consenting users only) counted 28 users in 28 days: 27 asked questions (375 in all), 13.5 min average engagement, 3.5 engaged sessions each, 100% crash-free. Only 2 to 4 people tried characters, sharing or party mode; real users should explore more, so 1.3.0 puts who answers up front.

**Provide a summary of the feedback that you received from testers. Include how you collected the feedback.**

> FALTA: qué dijeron y por dónde. Plantilla: "Feedback came through [a WhatsApp group / in person / email]: [the two or three main points]. Play's private feedback channel received none. We fixed [what], and the AI answers got a Report button so testers could flag a bad one."

## 2. About your app

**Target audience** (lo más concreto posible):

> Adults (18+) in Spain and Latin America, and English and Portuguese speakers, who like quick, light-hearted yes/no answers: friends at a party, people who enjoy horoscope or tarot-style fun, and Wear OS users who want an answer on the wrist with a tap or a shake.

**How your app provides value to users:**

> A playful yes/no oracle with character: ask by voice, tap or shake and get an answer in one of 20 voices (sarcastic, grandma, pirate...), written by AI for your question or drawn offline. Party mode, a daily verdict, shareable answer cards and a Wear OS tile make it quick and social.

**Estimated installs in the first year:** el rango que contenga 20.000 a 50.000 (por ejemplo, 10K–100K). Sale del escenario razonable del estudio de mercado, `docs/research/mercado/README.md`: 2.000 a 5.000 instalaciones al mes con la ficha orientada a "sí o no" y vídeos.

## 3. Your production readiness

**Changes made based on what you learned from your closed test:**

> Testers mostly asked questions and rarely changed who answers, so the character now leads the ball's page and its colour lights the ball. We also added optional Google sign-in with in-app deletion, a Report button on AI answers, safe replies to harmful questions, and no AI or ads under 18.

CONFIRMAR: que esos cambios respondieron a lo visto en la prueba. Si alguno vino de comentarios concretos de testers, decirlo en su lugar.

**How you determined that your app was ready for production:**

> 416 app, 77 logic and 61 server tests pass; no crashes in Crashlytics during the test; the subscription cycle (buy, renew, cancel, expire) was run end to end with license testers; account deletion is offered in the app and on the web; privacy policy, terms and Play declarations are up to date.
