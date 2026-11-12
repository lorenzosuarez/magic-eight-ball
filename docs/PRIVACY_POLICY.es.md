# Política de privacidad de Magic AI Ball (Wear OS y teléfonos y tabletas Android)

**Fecha de entrada en vigor:** 12 de noviembre de 2026

Esta política explica cómo la app Magic AI Ball (paquete `com.lorenzosuarez.magiceightball`), desarrollada por Lorenzo Suarez, trata la información. Abarca las dos versiones de la app: la de relojes Wear OS y la de teléfonos y tabletas Android. Cuando hay diferencias entre ellas, esta política lo indica.

## Resumen
- Sin cuenta ni inicio de sesión. En teléfonos y tabletas, y solo con tu consentimiento cuando la ley lo exige, la app cuenta cómo se usa (nunca lo que preguntas ni lo que responde), como se describe en "Medición de uso". La app del reloj no mide nada. Si la app falla, se envía al desarrollador un informe de fallos técnico, como se describe en "Informes de fallos". La app del reloj no muestra anuncios. La app para teléfonos y tabletas puede mostrar un anuncio de vez en cuando, como se describe en "Anuncios"; antes se te pide tu consentimiento cuando la ley lo exige.
- Lo único que la app guarda en tu dispositivo son tus elecciones en la app (idioma, estilo de respuesta, si se usan las respuestas con IA y, en teléfonos y tabletas, si se usan las preguntas por voz y las respuestas habladas, y los nombres que escribes para el modo fiesta) y, en teléfonos y tabletas, una lista de tus últimas preguntas y respuestas ("Recientes"). El desarrollador no opera ningún servidor que reciba tus datos.
- Una respuesta sale de la app hacia otra app o persona solo cuando eliges "Compartir" debajo de ella, como se describe en "Compartir una respuesta".
- Puedes desactivar las respuestas con IA en la app en cualquier momento.
- La app se conecta a servicios de Google, todos descritos más abajo: Firebase Remote Config, para consultar su configuración; la Gemini API, cuando las respuestas con IA están activadas; Firebase Crashlytics, cuando la app falla; y, en teléfonos y tabletas, Firebase Cloud Messaging para las notificaciones, Google Analytics for Firebase cuando tu consentimiento lo permite y Google AdMob mientras los anuncios están activados.
- **Preguntar en voz alta:** en las dos versiones puedes mantener presionado y decir tu pregunta. La app usa el micrófono solo mientras escucha una pregunta que tú iniciaste, el servicio de reconocimiento de voz de tu dispositivo convierte en texto lo que dices, y ese texto se envía a la Gemini API solo cuando las respuestas con IA están activadas. La app nunca almacena tu voz. En teléfonos y tabletas, el texto de tu pregunta se guarda, con su respuesta, solo en la lista "Recientes" de tu dispositivo, que puedes borrar en cualquier momento; el reloj no guarda nada. La app funciona sin el micrófono.
- **En el reloj**, mantener presionado para preguntar se ofrece solo mientras las respuestas con IA están en uso, porque solo el servicio de IA puede responder una pregunta; en los demás casos, el reloj responde cuando lo agitas y nunca escucha.

## Qué envía la app y a quién
**Firebase Remote Config.** Cuando la app se inicia, y como máximo aproximadamente una vez por hora mientras la usas, le pide al servicio Firebase Remote Config de Google su configuración actual, por ejemplo si las respuestas con IA están activadas. Esta solicitud incluye un identificador de instalación aleatorio creado por Firebase para esta copia de la app, e información básica de la app y del dispositivo (por ejemplo, versión de la app, idioma, país y versión del sistema operativo). No incluye tu nombre, tu cuenta ni tu ubicación. Google trata esta información conforme a los [términos de privacidad y seguridad de Firebase](https://firebase.google.com/support/privacy).

**Gemini API.** Las respuestas con IA se usan solo cuando tanto tú como el desarrollador las tienen activadas: tú eliges con "Respuestas con IA" en las opciones de la app (activadas de forma predeterminada), y el desarrollador puede activarlas o desactivarlas para todo el mundo. Cuando el desarrollador las tiene desactivadas, esa página las muestra como no disponibles. Mientras las respuestas con IA están desactivadas, por tu elección o la del desarrollador, la app muestra respuestas de una lista incluida en la app y no envía nada a la Gemini API. Lo mismo ocurre mientras está elegido el estilo de respuesta "Clásico": sus respuestas son siempre las clásicas incluidas en la app, sin importar qué más esté activado. Mientras están activadas, preguntarle a la bola envía una solicitud, a través de una conexión HTTPS cifrada, a la Gemini API de Google (`generativelanguage.googleapis.com`). Esa solicitud contiene únicamente:
- una instrucción escrita por el desarrollador, que refleja el estilo de respuesta y el idioma de las respuestas (inglés, español o portugués) que elegiste en la app, junto con lo que la app elige al azar para cada respuesta de modo que no haya dos iguales: si debe ser un sí, un no o una respuesta indecisa, y un tema y un giro expresivo tomados de listas incluidas en la app. Nada de su contenido proviene de ti;
- cuando agitas, o tocas en un teléfono o una tableta: la palabra fija "shake";
- cuando preguntaste en voz alta y tu pregunta fue reconocida: el texto de esa pregunta (200 caracteres como máximo) en lugar de la palabra fija, para que la respuesta responda a ella;
- parámetros técnicos de generación y la clave de API del desarrollador.

En las dos versiones, la solicitud nunca incluye audio, y no incluye tu nombre, los nombres escritos para el modo fiesta, tu cuenta, tus contactos, tu ubicación, identificadores del dispositivo ni lecturas de sensores. Por favor, no digas en una pregunta nada que no quieras enviar a Google. Como ocurre con cualquier solicitud por internet, Google recibe la dirección IP de tu dispositivo e información estándar de la conexión. Google trata estas solicitudes conforme a las [Condiciones del Servicio Adicionales de la Gemini API](https://ai.google.dev/gemini-api/terms) y la [Política de Privacidad de Google](https://policies.google.com/privacy). Según cómo se acceda a la API, Google puede conservar estas solicitudes durante un tiempo y usarlas para prestar y mejorar sus servicios.

Si la solicitud falla o tu dispositivo no tiene conexión, la app muestra en su lugar una respuesta de la lista incluida. Lo mismo ocurre cuando un dispositivo ya pidió su cupo diario de respuestas con IA; para llevar esa cuenta, la app guarda en tu dispositivo la fecha de hoy y un número, y nada más.

Cada solicitud también indica qué app la hace (el nombre de su paquete y la huella digital de su certificado de firma), para que Google pueda rechazar las solicitudes que no provienen de esta app.

## Informes de fallos
Si la app deja de funcionar de forma inesperada, envía un informe de fallos al servicio Firebase Crashlytics de Google la próxima vez que se abre, para que el desarrollador pueda encontrar y corregir el fallo. Las dos versiones de la app lo hacen.

- **Qué contiene un informe.** La traza técnica del fallo (en qué parte del código de la app ocurrió), la versión de la app, el modelo del dispositivo, la versión del sistema operativo, el idioma y la orientación del dispositivo, la memoria y el almacenamiento libres en ese momento, la fecha y la hora, y un identificador de instalación aleatorio creado por Crashlytics para esta copia de la app.
- **Qué no contiene nunca.** Tus preguntas, las respuestas de la bola, tu lista "Recientes", audio, tu nombre, tu cuenta ni tu ubicación. La app no agrega nada propio a un informe de fallos. (Una respuesta que eliges reportar se envía por separado: consulta "Reportar una respuesta").
- **Quién lo recibe.** Google, por cuenta del desarrollador, conforme a los [términos de privacidad y seguridad de Firebase](https://firebase.google.com/support/privacy). Los informes se conservan durante 90 días y luego se eliminan. Se usan solo para corregir la app, nunca para anuncios.
- **Cuando la app no ha fallado.** Cada vez que la app se abre, envía a Crashlytics un breve registro de sesión (versión de la app, modelo del dispositivo, versión del sistema operativo y el mismo identificador de instalación aleatorio), que es lo que le permite al desarrollador ver cuántas sesiones terminan sin un fallo. No contiene nada sobre lo que haces en la app.

## Medición de uso (solo teléfonos y tabletas)
Para saber qué partes de la app se usan y si un cambio la mejoró, la app para teléfonos y tabletas cuenta algunas cosas que haces en ella, con Google Analytics for Firebase. La app del reloj no lo hace.

- **Qué se cuenta.** Que la bola respondió, y sobre esa respuesta: si tocaste, agitaste, hablaste o usaste "¿Quién de ustedes…?", el estilo de respuesta en uso, si la respuesta fue escrita por IA o vino de la lista incluida, y si tenía una segunda línea. Que compartiste una respuesta o la app. Que elegiste un estilo de respuesta o cambiaste uno de los interruptores de las opciones, y cuál. Que se configuró el modo fiesta, y para cuántas personas.
- **Qué no se cuenta nunca.** Tus preguntas, las respuestas de la bola, los nombres escritos para el modo fiesta, tu lista "Recientes", audio ni ninguna otra cosa que hayas dicho o escrito. La app no tiene forma de incluirlos en lo que cuenta.
- **Qué lo acompaña.** Google Analytics agrega lo que recopila para todas las apps: un identificador aleatorio creado para esta copia de la app, la versión de la app, el modelo y el sistema operativo del dispositivo, el idioma y el país, y cuándo y durante cuánto tiempo se usó la app. La app desactivó la recopilación que hace Google Analytics de tu ID de publicidad y del ID propio del dispositivo.
- **Tu consentimiento.** Cuando la ley lo exige (por ejemplo, en el Espacio Económico Europeo, el Reino Unido y Suiza), no se cuenta nada a menos que, en el mensaje de consentimiento, hayas aceptado el almacenamiento de información en tu dispositivo, la medición y la mejora del servicio. Mientras ese mensaje no se haya respondido, tampoco se cuenta nada. Puedes cambiar tu elección en cualquier momento en "Privacidad de anuncios", en las opciones, y el conteo se ajusta a ella.
- **No para publicidad.** La app le indica a Google Analytics que no use estos conteos para publicidad ni para personalizar anuncios.
- **Quién lo recibe.** Google, por cuenta del desarrollador, conforme a los [términos de privacidad y seguridad de Firebase](https://firebase.google.com/support/privacy). Los conteos se conservan durante 14 meses como máximo.
- **Enlaces compartidos.** El enlace de Google Play que la app pone en lo que compartes indica que se compartió desde la app, para que el desarrollador pueda ver cuántas instalaciones provienen de lo compartido. No dice nada sobre ti.

## Notificaciones (solo teléfonos y tabletas)
El desarrollador puede enviar de vez en cuando una notificación a quienes usan la app, sobre alguna novedad de la app o como recordatorio para volver, a través de Firebase Cloud Messaging de Google. La app del reloj no recibe ninguna.

- **El veredicto del día.** Una vez al día, la app puede mostrar una frase con la voz del estilo de respuesta que elegiste. Se crea en tu dispositivo a partir de las respuestas incluidas en la app: no se envía ni se descarga nada para ello, y no se muestra un día en que ya le preguntaste a la bola. Puedes desactivarlo en "Veredicto del día", en las opciones.
- **Tu permiso.** En Android 13 y versiones posteriores, la app no muestra ninguna notificación a menos que lo permitas. Lo pregunta una sola vez, y solo después de que hayas usado la app en varios días. Puedes cambiar tu respuesta en cualquier momento en los ajustes de tu dispositivo, en las notificaciones de la app.
- **Cómo te llega un mensaje.** Los mensajes se envían a temas, no a personas: cada copia de la app sigue un tema común a todas y otro para el idioma en que está configurada, de modo que un mensaje pueda escribirse en tu idioma. Para entregarlos, Firebase Cloud Messaging le da a esta copia de la app un token de registro, vinculado a un identificador de instalación aleatorio creado por Firebase. Ninguno de los dos dice quién eres, y la app no envía con ellos nada de tus preguntas, respuestas o elecciones, aparte del idioma.
- **Quién lo recibe.** Google, por cuenta del desarrollador, conforme a los [términos de privacidad y seguridad de Firebase](https://firebase.google.com/support/privacy).

## Anuncios (solo teléfonos y tabletas)
La app para teléfonos y tabletas puede mostrar de vez en cuando un anuncio a pantalla completa, solo después de que hayas cerrado una respuesta y nunca mientras preguntas o lees una. El desarrollador puede activar y desactivar los anuncios para todo el mundo; mientras están desactivados, no ocurre nada de lo descrito en esta sección. La app del reloj nunca muestra anuncios.

- **Un anuncio que tú eliges ver.** Una vez que hayas usado las respuestas con IA del día, las opciones pueden ofrecerte más a cambio de ver un anuncio. Se muestra solo si lo pides, y se solicita y se gestiona igual que los demás anuncios descritos aquí.
- **Quién los muestra.** Los anuncios los proporciona Google AdMob. Para solicitar y mostrar un anuncio, el software de publicidad de Google incluido en la app recopila y usa información como el ID de publicidad de tu dispositivo, la dirección IP, la ubicación aproximada que se deriva de ella, información del dispositivo y de la app, y cómo interactúas con los anuncios. Google la trata conforme a [su política sobre cómo usa la información de las apps que utilizan sus servicios](https://policies.google.com/technologies/partner-sites) y la [Política de Privacidad de Google](https://policies.google.com/privacy).
- **Tu consentimiento.** Cuando la ley lo exige (por ejemplo, en el Espacio Económico Europeo, el Reino Unido y Suiza), la app te pide tu elección antes de solicitar cualquier anuncio, mediante el mensaje de consentimiento de Google. Puedes cambiar esa elección en cualquier momento en "Privacidad de anuncios", en las opciones de la app. También puedes restablecer o eliminar tu ID de publicidad, o desactivar la personalización de anuncios, en los ajustes de tu dispositivo.
- **Qué no reciben nunca los anuncios.** Tus preguntas, las respuestas de la bola y tu lista "Recientes" nunca se usan para anuncios, ni la app las comparte con el servicio de anuncios.
- **Permiso.** El software de publicidad agrega el permiso `com.google.android.gms.permission.AD_ID` a la app para teléfonos y tabletas.

## Preguntas por voz
En un teléfono o una tableta, mantienes presionada la bola y dices tu pregunta; en el reloj, mantienes presionada la pantalla. Esto es opcional. En un teléfono o una tableta puedes desactivar "Preguntas por voz" en las opciones; en cualquiera de los dispositivos puedes rechazar el permiso del micrófono, y entonces la app responde solo cuando tocas o agitas. El reloj lo ofrece solo mientras las respuestas con IA están en uso.

- **Cuándo se usa el micrófono.** Solo mientras mantienes presionado para preguntar (o, en un teléfono o una tableta con un servicio de accesibilidad, entre que eliges "Preguntar por voz" y el final de tu pregunta), y solo mientras la app está en pantalla. La app no escucha en ningún otro momento y nunca en segundo plano.
- **Quién convierte tu voz en texto.** La app no procesa ni graba audio por sí misma. Delega la escucha en el servicio de reconocimiento de voz de tu dispositivo, mediante la función estándar de reconocimiento de voz de Android. Cuando tu dispositivo ofrece reconocimiento en el propio dispositivo para el idioma de la app (Android 12 y versiones posteriores), la app lo usa y el audio se procesa en el dispositivo. De lo contrario, la app usa el proveedor de reconocimiento de voz predeterminado de tu dispositivo, que normalmente es Google y que puede enviar el audio a sus propios servidores para reconocerlo. Ese proveedor trata el audio conforme a su propia política de privacidad (en el caso de Google, la [Política de Privacidad de Google](https://policies.google.com/privacy)); puedes ver o cambiar el proveedor en los ajustes de tu dispositivo.
- **Qué pasa con el texto.** La pregunta reconocida se muestra en pantalla, escrita como pregunta: un reconocedor oye las palabras y no el tono en que se dijeron, así que la app agrega por su cuenta los signos de interrogación. Si las respuestas con IA están activadas, se envía a la Gemini API como se describió más arriba; si están desactivadas o no disponibles, la app no la envía a ningún lado (y el reloj no escucha en absoluto).
- **Qué se conserva.** El audio nunca se guarda. En teléfonos y tabletas, la pregunta reconocida se guarda, junto con su respuesta, en la lista "Recientes" descrita más abajo, solo en tu dispositivo. El reloj conserva la pregunta en memoria solo hasta que se ha mostrado la respuesta.

## Respuestas habladas (solo teléfonos y tabletas)
La app para teléfonos y tabletas puede leer cada respuesta en voz alta. Esto está desactivado hasta que actives "Respuestas habladas" en las opciones, y también puedes hacer que se lea una sola respuesta con el botón del altavoz que se muestra mientras está en pantalla.

- **Quién habla.** La app no produce la voz por sí misma. Entrega el texto de la respuesta, y nada más, al servicio de texto a voz de tu dispositivo, mediante la función estándar de Android. Ese servicio normalmente habla en el propio dispositivo; según la voz instalada, puede enviar el texto a los servidores de su proveedor para producir el audio. El proveedor (normalmente Google) lo trata conforme a su propia política de privacidad, y puedes verlo o cambiarlo en los ajustes de tu dispositivo.
- **Qué no se entrega nunca.** Tu pregunta, tu lista "Recientes" ni los nombres escritos para el modo fiesta. Una respuesta que sea uno de esos nombres, como se describe en "Modo fiesta", se lee en voz alta como cualquier otra respuesta.
- **Qué se conserva.** Nada: la app no guarda ningún audio.

## Modo fiesta (solo teléfonos y tabletas)
En las opciones puedes escribir los nombres de las personas que juegan, de dos a ocho. La app muestra entonces a quién le toca y ofrece "¿Quién de ustedes…?", que responde con uno de esos nombres elegido al azar en el dispositivo.

- **Dónde están los nombres.** En el archivo de configuración de la app en tu dispositivo, y en "Recientes" cuando un nombre fue una respuesta. No se envían al desarrollador, a la Gemini API ni a nadie más, y "¿Quién de ustedes…?" no se comunica con ningún servicio.
- **Tu control.** "Desactivar", en el cuadro de diálogo del modo fiesta, elimina los nombres. Borrar los datos de la app o desinstalarla también lo hace.
- Por favor, escribe solo nombres de pila o apodos, y solo de personas que estén jugando contigo.

## Compartir una respuesta (solo teléfonos y tabletas)
Debajo de una respuesta, "Compartir" crea una imagen de ella y la entrega a la app que elijas en el menú para compartir de tu dispositivo. No se comparte nada a menos que tú lo elijas, cada vez.

- **Qué hay en la imagen.** El nombre de la app, tu pregunta si hiciste una en voz alta, la respuesta con la línea que el personaje agregó debajo, y el nombre del estilo de respuesta que la dio.
- **Qué la acompaña.** Un texto breve con el enlace de la app en Google Play.
- **A dónde va.** Solo a la app que elijas, que la trata conforme a sus propios términos. El desarrollador no recibe nada, y la app no se entera de qué app elegiste.
- **Qué se conserva.** La imagen se guarda como un único archivo en el almacenamiento temporal de la app para que la otra app pueda leerla, y es reemplazada por la siguiente que compartas. Se elimina cuando borras la caché o los datos de la app o desinstalas la app, y tu dispositivo puede eliminarla por su cuenta.

## Reportar una respuesta (solo teléfonos y tabletas)

Nadie lee las respuestas escritas por IA antes de que tú las veas. Debajo de una respuesta de ese tipo hay un botón "Reportar", para cuando una no debería haberse dado.

- **Qué se envía.** La respuesta tal como se escribió, con la línea que el personaje agregó debajo, el estilo de respuesta y el idioma. Tu pregunta no se envía, y la app no agrega nada que te identifique.
- **Quién lo recibe.** El desarrollador, a través de Firebase Crashlytics, el mismo servicio de Google que recibe los informes de fallos, y con los mismos términos y la misma conservación que se describen en "Informes de fallos". Se usa solo para revisar lo que escribió la IA y para corregir las instrucciones que se le dan.
- **Tu elección.** No se envía nada a menos que presiones "Reportar". Un reporte no se puede retirar una vez enviado.

## Widget de la pantalla de inicio y tarjeta del reloj
La app para teléfonos y tabletas ofrece un widget para la pantalla de inicio, y la app del reloj una tarjeta, que muestran una respuesta al tocarlos. Sus respuestas siempre provienen de la lista incluida en la app: no envían nada a la Gemini API, no usan el micrófono y no se guardan en "Recientes".

## Recientes: tus últimas preguntas y respuestas (solo teléfonos y tabletas)
La app para teléfonos y tabletas conserva una lista de las últimas respuestas que te dio la bola, para que puedas volver a leerlas. La app del reloj no conserva ninguna lista de ese tipo.

- **Qué se guarda.** Para cada respuesta: el texto de tu pregunta si preguntaste en voz alta (nada si tocaste o agitaste; para "¿Quién de ustedes…?" en el modo fiesta, esas palabras y el nombre que se eligió), la respuesta, el estilo de respuesta que la dio, la fecha y la hora, y si la respuesta fue escrita por IA o vino de la lista incluida en la app.
- **Dónde.** En un archivo privado dentro del almacenamiento propio de la app en tu dispositivo. No se envía al desarrollador, a Google ni a nadie más, no se sincroniza entre dispositivos y no se incluye en las copias de seguridad del dispositivo ni en las transferencias de un dispositivo a otro.
- **Cuánto.** Como máximo, las últimas 50 respuestas; las más antiguas se eliminan automáticamente a medida que llegan nuevas.
- **Tu control.** Puedes borrar cualquier entrada por separado (con su botón de borrar; "Deshacer" permite recuperarla durante unos segundos), o todas con "Borrar todo", desde la propia lista. Borrar los datos de la app o desinstalarla también elimina la lista.
- **Compartir.** La lista en sí nunca se envía a ningún lado. Puedes compartir una respuesta que esté en pantalla como se describe en "Compartir una respuesta". "Compartir la app", en las opciones, entrega únicamente un texto breve con el enlace de la app en Google Play a la app que elijas.

## Sensores y permisos
- **Sensor de movimiento (acelerómetro):** se usa solo en tu dispositivo para detectar cuando lo agitas y, en teléfonos y tabletas, para mover ligeramente la bola a medida que mueves el dispositivo. Las lecturas del sensor no se almacenan ni se envían a ningún lado. No se requiere ningún permiso.
- **Internet y estado de la red** (`INTERNET`, `ACCESS_NETWORK_STATE`): se usan para comunicarse con Firebase Remote Config, la Gemini API, Firebase Crashlytics después de un fallo y, en teléfonos y tabletas cuando tu consentimiento lo permite, Google Analytics for Firebase.
- **Otras apps en tu dispositivo:** la app le pregunta a Android qué servicios de reconocimiento de voz y de texto a voz están instalados, solo para poder escuchar una pregunta y leer respuestas en voz alta. No lee nada más sobre tus apps.
- **Notificaciones** (`POST_NOTIFICATIONS`): se pide una sola vez, como se describe en "Notificaciones". La app funciona igual sin ese permiso.
- **Micrófono** (`RECORD_AUDIO`): se solicita la primera vez que mantienes presionado para preguntar en voz alta, y se usa solo como se describe en "Preguntas por voz". Puedes rechazarlo o retirarlo en los ajustes de tu dispositivo en cualquier momento; la app sigue funcionando al tocar y agitar.

Ninguna de las dos versiones solicita ubicación, cámara, contactos, sensores corporales ni ningún otro permiso sensible.

## Información almacenada en tu dispositivo
La app guarda tus elecciones (idioma, estilo de respuesta, si se usan las respuestas con IA y, en teléfonos y tabletas, si se usan las preguntas por voz y las respuestas habladas, y los nombres escritos para el modo fiesta) en un pequeño archivo de configuración en tu dispositivo, para que sigan ahí la próxima vez que abras la app. Permanecen en el dispositivo, no se envían por sí solas a ningún lado y se eliminan cuando borras los datos de la app o la desinstalas. En teléfonos y tabletas, la app también guarda la lista "Recientes" descrita más arriba (como máximo, tus últimas 50 preguntas y respuestas), que se elimina de la misma manera o desde la propia lista. En teléfonos y tabletas, la app también conserva cuántos días seguidos le has preguntado a la bola, en forma de dos números y una fecha, que nunca salen del dispositivo. En teléfonos y tabletas, la imagen de la última respuesta que compartiste espera en el almacenamiento temporal, como se describe en "Compartir una respuesta". En el reloj, la última respuesta se conserva solo en memoria. La app no almacena por su cuenta nada más. Firebase almacena en el dispositivo sus identificadores de instalación y la configuración más reciente, y un informe de fallos espera ahí hasta que se ha enviado. Nada se incluye en las copias de seguridad del dispositivo.

## Conservación y eliminación
Aparte de los informes de fallos descritos más arriba, que Google conserva para el desarrollador durante 90 días, y de los conteos de uso descritos en "Medición de uso", que conserva durante 14 meses como máximo, el desarrollador no recopila ni almacena tu información. Lo que la app guarda en tu dispositivo permanece ahí hasta que lo borres, como se describió más arriba. La información que reciben Google, el proveedor de reconocimiento de voz o de texto a voz de tu dispositivo, o una app con la que compartes una respuesta, se trata conforme a sus políticas, como se describió más arriba.

## Niños
La app no está dirigida a niños, y el desarrollador no recopila a sabiendas datos personales de niños.

## Cambios en esta política
Si esta política cambia, la versión actualizada se publicará aquí con una nueva fecha de entrada en vigor.

## Idiomas
Esta política se publica en inglés, español y portugués. Si las versiones difieren, se aplica la versión en inglés.

## Contacto
Si tienes preguntas sobre esta política, comunícate con el desarrollador mediante los datos de contacto que aparecen en la ficha de la app en Google Play.
