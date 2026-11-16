# Magic AI Ball: preparación para producción en Google Play

Revisión del 16 de noviembre de 2026 sobre la rama `feature/looks-held-and-minimal` (commit `b4191e1`): teléfono 1.2.1 (código 4), reloj 1.0.1 (código 103), en prueba interna y cerrada. Cada punto se contrastó con el código, con los paquetes de release que hay compilados en `build/` y con las páginas de Google citadas al final, leídas ese mismo día. Los números de línea son los de ese commit; de los archivos que ese día tenían cambios sin confirmar (textos y accesorios de los personajes) se cita el nombre del texto o de la entrada. No es asesoría legal.

Veredictos: **Cumple** (el código o un artefacto compilado lo demuestra), **Falta** (brecha comprobada), **No verificable** (depende de Play Console, Firebase o AdMob; se dice dónde mirar).

## Veredicto

Todavía no puede salir a producción, y lo que lo impide no está en el código de la app. El flujo de la suscripción cumple la política tal como está escrita (precio, periodo, renovación y forma de cancelar a la vista antes del botón; "Gestionar suscripción" con el enlace de Google Play; aviso de que borrar la cuenta no cancela; estado verificado en servidor), y también cumplen el nivel de API, las páginas de 16 KB, la versión de la biblioteca de pagos, el borrado de cuenta dentro de la app y el reporte de respuestas de IA. Lo que bloquea es lo publicado y lo configurado: la política de privacidad que está en línea describe una app sin cuenta ni compras y la página para eliminar la cuenta devuelve 404; el proyecto de Firebase está en el plan gratuito, lo que deja a Gemini en el servicio no pago (sus términos no lo permiten para usuarios del EEE, el Reino Unido y Suiza, y piden no enviarle información personal) y deja sin servidor a Premium; y quedan por cumplir o comprobar el requisito de 12 testers durante 14 días y las declaraciones de Play Console. Si la prueba cerrada empezó el 14 de noviembre, los 14 días se cumplen el 28 y la revisión de la solicitud suele tardar hasta 7 días más: producción no llega antes de la primera semana de diciembre.

## Bloqueantes

### 1. La política publicada no describe este build y la página para eliminar la cuenta no existe: **Falta**

- **Qué.** `https://magic-ai-8-ball.web.app/privacy` (vigente desde el 12 de noviembre de 2026) dice "No account or sign-in", no tiene las secciones de cuenta ni de Premium y no dice que la app es para mayores de 18. `/delete-account`, `/es/delete-account` y `/pt/delete-account` devuelven 404. El build sí trae inicio de sesión, perfil, personajes en Firestore y compras (`app-phone/build.gradle.kts:112-118`), y la app enlaza a esa política (`ui/EightBallScreen.kt:1690-1697`).
- **Norma.** User Data: la política debe describir lo que la app recoge; una app que permite crear cuentas debe ofrecer el borrado dentro de la app y en un recurso web que funcione, cuyo enlace se declara en Play Console. El formulario de Seguridad de los datos ya es exigible en prueba cerrada.
- **Dónde.** `docs/PRIVACY_POLICY.md:3`, `docs/PRIVACY_POLICY.es.md:3`, `docs/PRIVACY_POLICY.pt.md:3` siguen con `RELEASE-DATE`, y `site/build.py:191-192` se niega a publicar así.
- **Arreglo mínimo.** Después del punto 2 (la política nueva afirma en `docs/PRIVACY_POLICY.md:30` que se usa el servicio de pago): poner la fecha en las tres políticas, `firebase deploy --only hosting`, abrir las seis direcciones y comprobar que responden. Debe estar hecho antes de que `account_enabled` esté encendido para alguien fuera de la prueba interna.

- **Publicada el 16 de noviembre de 2026**, antes del punto 2, con la frase que ya tenía la política anterior en lugar de la del servicio de pago: "Según cómo se acceda a la API, Google puede conservar estas solicitudes durante un tiempo y usarlas para prestar y mejorar sus servicios", que es cierta con cualquiera de los dos planes. Con el proyecto ya en Blaze: volver a la frase del servicio de pago en las tres políticas, poner fecha nueva y desplegar otra vez.

### 2. El proyecto de Firebase está en el plan gratuito: **Falta** mientras siga así (se ve en Firebase console → Uso y facturación)

- **Qué.** Sin cuenta de facturación, la Gemini Developer API que usa Firebase AI Logic es el nivel gratuito. Los términos de la Gemini API (vigentes desde el 23 de marzo de 2026) solo permiten los servicios de pago para apps disponibles en el EEE, Suiza o el Reino Unido; en el servicio no pago Google usa lo enviado para mejorar sus productos, puede leerlo un revisor humano, y se pide no enviar información personal. La app envía el texto de la pregunta dicha por voz de cualquier usuario y, con cuenta, el nombre, el signo y la descripción de los personajes. Además, Cloud Functions no se puede desplegar sin el plan Blaze, así que Premium no tiene servidor.
- **Dónde.** El propio repositorio lo exige: `docs/REMOTE_CONFIG.md:31` y `:48`, `docs/plans/personajes-cuenta-premium.md:34-37`.
- **Arreglo mínimo.** Pasar el proyecto a Blaze con un presupuesto y alertas en Google Cloud. Si no se hace antes de salir: `gemini_enabled` y `account_enabled` apagados, o excluir el EEE, el Reino Unido y Suiza en los países de distribución, y no publicar la frase del servicio de pago.

### 3. Premium no se puede vender todavía: **Falta** (solo bloquea si Premium sale con el lanzamiento)

- **Qué.** Las cinco funciones de `functions/src/index.ts` (`claimPremium:170`, `playNotified:199`, `endLapsed:214`, `revokeVoided:227`, `forgetAccount:248`) no están desplegadas. Con `premium_enabled` encendido y sin ellas, la compra se cobra, la app no puede confirmarla (`account/Shop.kt:350-372` termina en "no confirmado") y Google Play la devuelve a los tres días por no haber sido reconocida.
- **Dónde.** Los siete pasos están en `docs/REMOTE_CONFIG.md:45-55`: Blaze, API de Google Play Developer y permiso de la cuenta de servicio, `firebase deploy --only functions,firestore:rules`, tema `play-notifications` con las notificaciones en tiempo real, suscripción `premium_subscription` con el plan `monthly`, política y ficha.
- **Arreglo mínimo.** Dejar `premium_enabled` apagado (es el valor por defecto, `config/AppConfig.kt:48`) hasta completar esos pasos y probar con un tester de licencia el ciclo entero: compra, renovación, cancelación (Premium sigue hasta el fin del periodo), vencimiento, pago rechazado, reembolso, borrado de la cuenta con suscripción activa y reclamo desde otra cuenta de la app.

### 4. Acceso a producción: 12 testers durante 14 días seguidos: **No verificable**

- **Qué.** Las cuentas personales creadas después del 13 de noviembre de 2023 necesitan una prueba cerrada con al menos 12 testers inscritos 14 días continuos antes de poder pedir producción; quien se da de baja y vuelve reinicia su cuenta. Después se envía un cuestionario cuya revisión suele tardar hasta 7 días.
- **Dónde.** Play Console → Panel → "Solicitar acceso a producción": cuántos testers cuentan y desde qué día.

### 5. Declaraciones de Play Console acordes a este build: **No verificable**

Sin ellas la revisión rechaza o la ficha queda en infracción. La lista exacta está en "No se puede saber desde el repositorio"; lo que no puede faltar: Seguridad de los datos con cuenta, compras y la URL de borrado; público objetivo solo "18 y más"; clasificación de contenido rehecha; instrucciones de acceso para el revisor, con una cuenta que ya tenga Premium.

### Orden sugerido

1. Blaze, presupuesto y cuotas. 2. Desplegar reglas y funciones. 3. Fecha en las políticas y desplegar el sitio. 4. Declaraciones de Play Console. 5. Producto, notificaciones y prueba con tester de licencia. 6. Encender `account_enabled` y `premium_enabled`. 7. Pedir producción cuando se cumplan los 14 días.

## Conviene antes de salir

Por orden de valor. Ninguno impide publicar hoy, y los tres primeros deberían ir en el mismo release.

Arreglados después de la revisión, y dejados escritos como se encontraron:

- el 1: los filtros se indican para las cuatro categorías (`FirebaseAiGeminiClient.kt`);
- el 2: la fila está en Opciones mientras se venda una suscripción (`ui/SettingsSheet.kt`);
- el 4: el sitio, el README y el Clásico ya no llevan el nombre ni las veinte respuestas del juguete; las instrucciones que solo lee el servicio de IA lo siguen nombrando;
- el 5: ni se piden ni se muestran anuncios en un dispositivo marcado como menor, ni antes de saber si lo está (`ads/AdMobAdsController.kt`);
- el 6: `site/static/app-ads.txt`, publicado con el sitio el 16 de noviembre de 2026;
- el 8: que falte la concesión en una copia que todavía no oyó al servidor no le quita Premium a nadie (`account/Entitlements.kt`, `heldBy`);
- el 10, en parte: con las animaciones del sistema quitadas, la bola no se mece (`ui/DesignedMotion.kt`, `motionRemoved`); lo que dice dónde está la app se sigue moviendo, que es decisión de producto;
- el 11: la página de licencias nombra las bibliotecas bajo la Apache License 2.0 y la da entera, sin dependencia nueva.

Quedan el 3, el 7 y el 9.

1. **Filtros de seguridad de Gemini explícitos: Falta.** `core/data/.../gateway/FirebaseAiGeminiClient.kt:53-64` crea el modelo sin `safetySettings`, y la documentación de Firebase AI Logic dice que las categorías no indicadas quedan apagadas (`OFF`). Hoy la única barrera contra acoso, odio, contenido sexual o peligroso es el texto del prompt (`core/domain/.../policy/DefaultPromptPolicy.kt:138-161`), con descripciones de personajes escritas por el usuario y respuestas largas activables. La política de contenido generado por IA hace responsable a la app de impedirlo. El resto ya está: una respuesta bloqueada se convierte en `GeminiReply.Blocked` (`:82-84`, `:95`). Arreglo: pasar `safetySettings` con `HarmBlockThreshold.MEDIUM_AND_ABOVE` para `HATE_SPEECH`, `SEXUALLY_EXPLICIT` y `DANGEROUS_CONTENT`, y `ONLY_HIGH` para `HARASSMENT` (los personajes pinchan a propósito), y volver a pasar las pruebas de personajes.
2. **"Gestionar suscripción" siempre alcanzable: Falta parcial.** La fila solo aparece cuando el servidor tiene anotada una suscripción vigente (`ui/AccountSection.kt:196`, `ui/SettingsSheet.kt:212-214`, `account/Entitlements.kt:93-94`). No la ve quien tiene el pago retenido o la suscripción en pausa (el servidor borra la concesión, `functions/src/index.ts:162`), quien pagó y quedó sin confirmar, quien la tiene en otra cuenta de la app, ni quien borró la cuenta de la app y sigue pagando. La política pide acceso en la app a una forma sencilla de cancelar. Arreglo: una fila fija en Opciones → Acerca de mientras `premiumEnabled` esté encendido, y el mismo enlace en la hoja de Premium cuando termina en "en otra cuenta" o "sin confirmar", al centro general `https://play.google.com/store/account/subscriptions`.
3. **Términos de uso y contacto: Falta.** Opciones → Acerca de solo tiene opciones de privacidad, política y licencias (`ui/SettingsSheet.kt:272-299`); no hay términos ni forma de escribir a soporte en la app. Google exige desde noviembre de 2026 declarar la condición de comerciante (límite: 1 de febrero de 2027); los criterios que cita (cobrar de forma habitual por compras en la app o por anuncios) apuntan a comerciante para quien distribuye en el EEE o el Reino Unido, y al comerciante se le pide un enlace a los términos de cada app y se le publican teléfono y dirección. La política de contenido de usuarios los exigirá además el día que la Comunidad liste algo. Arreglo: `docs/TERMS.md` en los tres idiomas añadido a `TEXTS` en `site/build.py`, y dos filas en Acerca de: "Términos de uso" y "Contacto" (correo).
4. **"Magic 8 Ball" es marca registrada de Mattel (confirmarlo en el registro de marcas): riesgo.** La portada del sitio dice "A Magic 8 Ball with character" (`site/build.py:39-40`, publicado), y el personaje Clásico trae las veinte respuestas del juguete (`core/data/src/main/assets/magic_8_ball_responses.json`, `en.neutral`). Los textos de la app no usan la marca. Arreglo: cambiar ese lema por una descripción genérica y no usar la marca en el título, la descripción ni las capturas de la ficha.
5. **Dispositivo marcado como menor de 18: sigue viendo anuncios.** La marca corta la IA (`EightBallViewModel.kt:166`, `:791`) pero `ads/AdMobAdsController.kt` no la consulta. Arreglo: en `mayLoad()` (`:142-149`), no pedir anuncios si el dispositivo está marcado.
6. **`app-ads.txt`: Falta.** `https://magic-ai-8-ball.web.app/app-ads.txt` devuelve 404. AdMob limita los anuncios de las apps nuevas hasta verificarlas con ese archivo en el sitio web del desarrollador que figure en la ficha. Arreglo: `site/static/app-ads.txt` (el build copia esa carpeta, `site/build.py:196`).
7. **Reloj.** (a) No hay enlace ni dirección de la política dentro de la app del reloj (ninguna mención en `feature/chat` ni en `app-wear`), y la norma pide el enlace "dentro de la app": añadir una línea con la dirección en `feature/chat/.../components/SettingsMenu.kt`. (b) La vista previa del tile es el icono de la app (`app-wear/src/main/AndroidManifest.xml:57-59`); la guía WO-V10 pide una imagen de lo que muestra el tile. (c) `release_assets/screenshots/4.png` mide 794×790 y está recortada en círculo sobre fondo gris; WO-G5 pide capturas 1:1, solo de la interfaz, sin máscaras ni marcos.
8. **Premium sin conexión.** `account/Entitlements.kt:106` toma "el documento no existe" como "no tiene Premium" aunque el dato venga de la caché, que es de memoria y arranca vacía (`account/AccountStore.kt:33-34`): quien paga y abre la app sin red ve la oferta de Premium y pierde la fila de gestión. Arreglo: tratar ese caso como "aún no se sabe", como ya hace `account/FirestoreProfiles.kt:68-76`.
9. **Con Premium ya encendido.** `ENFORCE_APP_CHECK = true` (`functions/src/index.ts:40`); una regla que exija `grants/premium` para crear un personaje (`firestore/firestore.rules:81-84`; hoy solo lo impide la app, `docs/REMOTE_CONFIG.md:59`); y `showInAppMessages()` en `account/Shop.kt` para que Google Play avise del pago rechazado dentro de la app.
10. **Accesibilidad.** `ui/DesignedMotion.kt:23-25` fija la escala de animación en 1 e ignora "quitar animaciones" del sistema, y el vaivén de la bola solo se apaga para todos desde Remote Config. Respetar la escala 0 del sistema al menos para el vaivén y los deslizamientos. Lo demás está cuidado: roles, descripciones, regiones vivas, filas de 56 dp o más, textos en sp con disposición alternativa a tamaños grandes (`ui/SettingsSheet.kt:505`).
11. **Licencias.** La pantalla lista Lucide y Google Sans (`ui/SettingsSheet.kt:590-602`). AndroidX, Kotlin, Hilt, Firebase y Play services son Apache 2.0, que pide entregar la licencia: el plugin `oss-licenses` lo resuelve.

## Ya cumple

| Tema | Evidencia |
|---|---|
| Términos de la oferta antes de comprar, sin acción adicional | Precio y periodo tal como los da Google Play, renovación automática y cómo cancelar, sobre el botón: `ui/PremiumSheet.kt:119`, `:251-265`; texto `premium_terms` en `res/values/strings.xml`, `values-es` y `values-pt`. El botón no responde sin precio: `ui/PremiumSheet.kt:321`. Qué queda gratis: textos `premium_free_note` y `premium_all_note` |
| Sin prueba gratis ni descuentos | Solo se vende el plan base con una fase que se renueva: `account/Shop.kt:105-109` |
| Gestionar y cancelar desde la app | Fila "Gestionar suscripción" y tarjeta Premium en Opciones, con el enlace que documenta Google (`?sku=…&package=…`): `account/Shop.kt:115-116`, `ui/AccountSection.kt:251-257`, `:503-511`, `ui/SettingsSheet.kt:212-214` |
| Borrar la cuenta no cancela la suscripción | Diálogo propio para quien está suscrito: `ui/AccountSection.kt:384-386`, texto `account_delete_text_subscribed`; también en `docs/DELETE_ACCOUNT.md:28` y `docs/PRIVACY_POLICY.md:47`, `:54` |
| Restaurar compras | Automático al abrir con sesión y antes de cada compra: `account/Shop.kt:225`, `:253`, `:309-320` |
| Estados de la suscripción | Activa, periodo de gracia y cancelada con tiempo pagado conservan Premium; retenida, en pausa, vencida o reembolsada lo quitan: `functions/src/claim.ts:61-69`, `functions/src/index.ts:152-168`, `:214-241`. Reconocimiento ante Google Play: `index.ts:120-135`. La app no puede escribirse Premium: `firestore/firestore.rules:91-93`, `:117-122` |
| Pagos solo por Google Play, precio igual al de Play | Billing Library 9.1.0 (`gradle/libs.versions.toml:29`; desde el 31 de agosto de 2026 se exige la 8 o superior); el precio mostrado es `formattedPrice` (`account/Shop.kt:395-404`) |
| Borrado de cuenta dentro de la app | Opciones → cuenta → Eliminar cuenta → confirmar: `ui/AccountSection.kt:380-405`, `:520-527`; borra perfil y personajes en una transacción y luego la cuenta: `account/Membership.kt:113-116`, `:230-237`, `account/FirestoreProfiles.kt:102-112`, `account/FirebaseAccount.kt:90-105`. El servidor limpia concesión y compras: `functions/src/index.ts:248-257` |
| Reporte de respuestas de IA sin salir de la app | Teléfono: `ui/EightBallScreen.kt:852-861`, `ui/ReportSheet.kt`; reloj: `feature/chat/.../ChatScreen.kt:420-440`, `ChatViewModel.kt:198-204`; envío: `usage/FirebaseAnswerReports.kt:34-36`. Las respuestas de IA van marcadas "IA": `ui/EightBallScreen.kt:1361-1379` |
| Contenido de otros usuarios | No aplica aún: en release la Comunidad no lista nada (`app-phone/src/release/.../di/CommunityModule.kt:18`, y su pestaña solo dice `characters_community_soon` en `ui/CharactersScreen.kt`) y los personajes propios son privados (`firestore/firestore.rules:67-85`). Las parodias de personas reales solo existen en `src/debug` |
| Voz | La app no graba ni envía audio: usa el reconocedor del sistema y prefiere el del dispositivo (`core/data/.../speech/AndroidSpeechListener.kt:114-119`); no hay `AudioRecord` ni `MediaRecorder` |
| Analítica y publicidad | Analítica apagada hasta el consentimiento, sin ID de publicidad y nunca para anuncios: `app-phone/src/main/AndroidManifest.xml:65-76`, `usage/FirebaseUsageTracker.kt:59-75`. Consentimiento antes de pedir anuncios: `ads/AdMobAdsController.kt:74-87`, `:142-149`. Intersticial solo al cerrar una respuesta, tras 4 respuestas y 3 minutos (`config/AppConfig.kt:38-39`); el recompensado se elige; Premium no ve ninguno. Opciones de privacidad accesibles: `ui/SettingsSheet.kt:273-285` |
| Consentimiento del perfil | La pantalla dice qué se envía a Gemini y el botón es el acuerdo: `ui/ProfileSheet.kt:385-402`; queda fechado por el servidor: `firestore/firestore.rules:43-55` |
| Solo adultos | 18 años en la app y en las reglas: `account/Profile.kt:86`, `:108`, `firestore/firestore.rules:23-26`, `:41`. Fecha sin rellenar de antemano: `ui/ProfileSheet.kt:145`. Menor: cuenta borrada y dispositivo sin IA: `account/Membership.kt:129-139` |
| Permisos | Micrófono opcional y pedido al usarlo (`AndroidManifest.xml:8`, `:21-23`; `ui/EightBallScreen.kt:213-217`); notificaciones una vez y tras días de uso (`push/Notifications.kt:98-100`, `:135-144`); sin alarmas exactas (trabajo periódico, `push/DailyVerdict.kt:54`); sin servicios en primer plano con tipo, sin `QUERY_ALL_PACKAGES`, sin ubicación. Nada requiere formulario de permisos |
| Nivel de API | `targetSdk = 36` en teléfono y reloj (`app-phone/build.gradle.kts:48`, `app-wear/build.gradle.kts:26`); se exige 36 en teléfono y 35 en Wear OS desde el 31 de agosto de 2026 |
| Páginas de 16 KB y 64 bits | Las dos bibliotecas nativas de los AAB de release (`libandroidx.graphics.path.so`, `libdatastore_shared_counter.so`) tienen los segmentos alineados a 0x4000 en las cuatro arquitecturas, en teléfono y reloj; AGP 8.11.2 |
| Optimización de código (umbral de febrero de 2027) | R8 activo en ambos: `app-phone/build.gradle.kts:59`, `app-wear/build.gradle.kts:34` |
| Wear OS | Independiente del teléfono (`app-wear/src/main/AndroidManifest.xml:33-35`), fondo negro y pantalla de inicio (`app-wear/src/main/res/values/styles.xml:5`, `:9-12`), modo ambiente (`app-wear/.../MainActivity.kt:24-36`), cierre deslizando (paginador vertical, `ChatScreen.kt:161`), objetivo táctil de 48 dp (`ChatScreen.kt:558`), sin inicio de sesión en el reloj, mismo paquete y firma (`gradle/release-signing.gradle`) |
| Suplantación y propiedad intelectual en la app | Las orejas de ratón son óvalos ladeados con el interior blanco, vistos de lado y rotulados "Ratón" (entrada `'mouse'` de `tools/looks.py`): no es la silueta de tres círculos negros. Sin nombres de personas reales en release |
| Operación | Crashlytics en teléfono y reloj; cuenta, Premium, IA y anuncios se apagan desde Remote Config y nacen apagados (`config/AppConfig.kt:47-48`, `core/data/.../RemoteConfigGeminiSettingsSource.kt:71`, `ads/AdMobAdsController.kt:194-195`), con cambios en vivo (`config/AppConfig.kt:152-161`); sin red o sin cupo responde la lista incluida (`core/data/.../Magic8BallRepositoryImpl.kt:63-82`); tope diario por dispositivo de 20 y 40 (`core/data/.../GeminiSettings.kt:43`, `:49`), 256 tokens de salida, App Check con Play Integrity (`core/data/.../appcheck/AppCheck.kt:55`), funciones con 10 instancias como máximo (`functions/src/index.ts:33`) |
| Anuncios reales en release | El manifiesto de release compilado no lleva el identificador de prueba de Google; sin `ADMOB_REWARDED_ID` el recompensado no se ofrece (`ads/AdMobAdsController.kt:201-203`) |
| Enlace a la política y copias de seguridad | Opciones → Política de privacidad (`ui/SettingsSheet.kt:286-291`); nada se respalda (`AndroidManifest.xml:38-40`) |

## No se puede saber desde el repositorio

### Play Console

- **Panel → producción.** Cuántos de los 12 testers cuentan y desde cuándo (bloqueante 4).
- **Política y programas → Contenido de la app.**
  - Política de privacidad: `https://magic-ai-8-ball.web.app/privacy`, ya con la versión nueva.
  - Seguridad de los datos: contrastar con las tablas de `docs/play-console-checklist.md` (sección 2), que coinciden con el código: nombre, correo, ID de usuario, fecha de nacimiento, texto de personajes y de preguntas por voz, historial de compras, interacciones, fallos, diagnósticos, identificadores del dispositivo y ubicación aproximada (estos últimos compartidos por AdMob). Voz o grabaciones: no. Creación de cuenta: sí. URL de borrado: `https://magic-ai-8-ball.web.app/delete-account`.
  - Público objetivo: solo "18 y más". Los términos de Gemini prohíben apps dirigidas a menores de 18 o con acceso probable de ellos; "Restringir el acceso de menores" es opcional para esta categoría y encaja con ese motivo.
  - Clasificación de contenido: rehacer el cuestionario con cuenta, datos personales, compras digitales y texto generado por IA. El accesorio "Botella" (entrada `'bottle'` de `tools/looks.py`, con burbujas) cuenta como referencia a bebidas si se pregunta. Las apps sin clasificar no se admiten.
  - Acceso a la app: instrucciones para entrar con cualquier cuenta de Google, completar el perfil con fecha de adulto y llegar a "Crear". Para lo de pago, una cuenta de prueba a la que se le dé Premium con `npm --prefix functions run staff -- give …` (`docs/REMOTE_CONFIG.md:117-129`); la guía de Wear OS (WO-G8) también lo pide.
  - Anuncios: sí. ID de publicidad: sí, para publicidad. Funciones financieras: ninguna. Salud: ninguna. Noticias: no. Gobierno: no. Las dos primeras de "ninguna" hay que enviarlas igualmente.
- **Ficha.** Que diga que Premium es una suscripción y qué queda gratis; que no use "Magic 8 Ball"; que las capturas no salgan de un build de depuración con nombres de personas reales (Carl Sagan, Einstein, Dalí y otros en `app-phone/src/debug/.../di/CommunityModule.kt`); capturas de Wear OS 1:1 y sin marcos; casilla de "generado con IA" en las imágenes que lo sean; sitio web del desarrollador igual al que aloje `app-ads.txt`.
- **Monetización.** Perfil de pagos; suscripción `premium_subscription` con plan base `monthly` activo y sin ofertas; el producto único `premium` desactivado; duración del periodo de gracia y de la retención de cuenta; notificaciones en tiempo real con el tema `play-notifications` y el mensaje de prueba entregado; testers de licencia. Si llega la invitación de autorización para ventas a Japón, completarla antes del 1 de diciembre de 2026: sin ella Google retiene los pagos de todas las regiones.
- **Cuenta de desarrollador.** Condición de comerciante (Administrar cuenta → Detalles de la cuenta → Acerca de ti): se declara desde noviembre de 2026 y a más tardar el 1 de febrero de 2027. Países de distribución, si Gemini sigue en el servicio no pago.
- **Integridad de la app.** API de Play Integrity vinculada al proyecto de Google Cloud, y las huellas SHA-1 y SHA-256 de la clave de firma de Play registradas en Firebase: sin ellas fallan el inicio de sesión con Google y App Check en la app instalada desde Play.
- **Explorador de paquetes.** Porcentaje de optimización DEX (umbral del 25 % desde febrero de 2027 para apps con más de 10 MB de DEX).

### Firebase y Google Cloud

- **Plan** del proyecto y **presupuesto** con alertas.
- **Remote Config.** Valores actuales de `account_enabled`, `premium_enabled`, `ads_enabled`, `gemini_enabled`, `gemini_model`, `gemini_backend`. Si siguen `gemini_api_key`, `gemini_use_paid_key` o `gemini_paid_api_key` (`docs/REMOTE_CONFIG.md:3`): la app nunca estuvo en producción, así que pueden borrarse ya y revocarse esas claves.
- **Firestore.** Que las reglas desplegadas sean las del repositorio (`firebase emulators:exec … "python3 firestore/rules_test.py"` y `firebase deploy --only firestore:rules`), y que la base esté en `eur3`, como dice la política.
- **App Check.** Las dos apps registradas con Play Integrity y la exigencia activada para AI Logic y Firestore: el tope diario de la app se borra con los datos (`core/data/.../AiAnswerQuota.kt:30-31`), el límite real es este y las cuotas.
- **AI Logic.** Cuota "Generate content requests" por usuario (100 por minuto por defecto; un juguete necesita muchas menos) y tope diario del proyecto en la API de Gemini.
- **Authentication.** Proveedor de Google activo; usuarios sin documento en `users/` de más de un día, que se borran a mano (`docs/REMOTE_CONFIG.md:35`).
- **Crashlytics.** Quién mira los reportes de respuestas: todos caen en un mismo problema no fatal (`ReportedAnswer`), sin aviso propio, y se borran a los 90 días. La política espera que los reportes sirvan para ajustar los filtros.
- **Analytics.** Si el proyecto está vinculado a Google Play, Analytics registra compras por su cuenta y la política dice que no (`docs/play-console-checklist.md:65`).
- **Pantalla de consentimiento de OAuth.** Nombre de la app y dirección de la política.

### AdMob

- Mensajes de consentimiento publicados en Privacidad y mensajes: el europeo y el de los estados de EE. UU. Sin mensajes publicados la biblioteca de consentimiento no tiene qué preguntar: según el caso, eso deja a Europa sin un consentimiento válido o a la app sin anuncios (`ads/AdMobAdsController.kt:142-149` depende de `canRequestAds()`).
- App vinculada a la ficha de Play, `app-ads.txt` verificado y clasificación máxima del contenido de los anuncios acorde a la de la app.

### Cuestiones legales que este documento no resuelve

- Leyes de verificación de edad (varios estados de EE. UU. y otras jurisdicciones). Google ofrece Play Age Signals y dice que no obliga a usarla y que cada desarrollador decide cómo le aplican.
- Si la actividad convierte al desarrollador en comerciante a efectos del EEE y el Reino Unido, con la publicación de teléfono y dirección que eso implica.

## Fuentes

Páginas de Google consultadas el 16 de noviembre de 2026.

- Suscripciones: https://support.google.com/googleplay/android-developer/answer/9900533
- Pagos: https://support.google.com/googleplay/android-developer/answer/9858738
- Datos de usuario: https://support.google.com/googleplay/android-developer/answer/10144311
- Sección de Seguridad de los datos: https://support.google.com/googleplay/android-developer/answer/10787469
- Requisitos de eliminación de cuentas: https://support.google.com/googleplay/android-developer/answer/13327111
- Contenido generado por IA: https://support.google.com/googleplay/android-developer/answer/13985936
- Contenido generado por usuarios: https://support.google.com/googleplay/android-developer/answer/9876937
- Anuncios: https://support.google.com/googleplay/android-developer/answer/9857753
- ID de publicidad: https://support.google.com/googleplay/android-developer/answer/6048248
- Público objetivo y contenido de la app: https://support.google.com/googleplay/android-developer/answer/9867159
- Contenido y funciones con restricción de edad: https://support.google.com/googleplay/android-developer/answer/16302250
- Clasificaciones de contenido: https://support.google.com/googleplay/android-developer/answer/9859655
- Permisos y API que acceden a información sensible: https://support.google.com/googleplay/android-developer/answer/9888170
- Nivel de API objetivo: https://support.google.com/googleplay/android-developer/answer/11926878 y https://developer.android.com/google/play/requirements/target-sdk
- Páginas de memoria de 16 KB: https://developer.android.com/guide/practices/page-sizes
- Requisitos de calidad de apps (memoria y optimización de código): https://support.google.com/googleplay/android-developer/answer/17492799
- Calidad de apps para Wear OS: https://developer.android.com/docs/quality-guidelines/wear-app-quality
- Suplantación de identidad: https://support.google.com/googleplay/android-developer/answer/9888374
- Propiedad intelectual: https://support.google.com/googleplay/android-developer/answer/9888072
- Metadatos de la ficha: https://support.google.com/googleplay/android-developer/answer/9898842
- Declaración de recursos generados con IA en la ficha: https://support.google.com/googleplay/android-developer/answer/17262077
- Requisitos de prueba para cuentas personales nuevas: https://support.google.com/googleplay/android-developer/answer/14151465
- Preparar la app para revisión: https://support.google.com/googleplay/android-developer/answer/9859455
- Declaración de funciones financieras: https://support.google.com/googleplay/android-developer/answer/13849271
- Declaración de apps de salud: https://support.google.com/googleplay/android-developer/answer/14738291
- Declaración de la condición de comerciante: https://support.google.com/googleplay/android-developer/answer/17594752
- Anuncio de políticas del 29 de octubre de 2026: https://support.google.com/googleplay/android-developer/answer/18100766
- Anuncio de políticas del 15 de julio de 2026: https://support.google.com/googleplay/android-developer/answer/17134731
- Leyes estatales de EE. UU. sobre edad y tiendas de apps: https://support.google.com/googleplay/android-developer/answer/16569691
- Autorización de pagos para Japón: https://support.google.com/paymentscenter/answer/17595997
- Versiones admitidas de Play Billing Library: https://developer.android.com/google/play/billing/deprecation-faq
- Suscripciones, enlaces de gestión y mensajes en la app: https://developer.android.com/google/play/billing/subscriptions
- Ciclo de vida de las suscripciones: https://developer.android.com/google/play/billing/lifecycle/subscriptions
- Términos adicionales de la Gemini API: https://ai.google.dev/gemini-api/terms (redirige a https://aistudio.google.com/docs/terms)
- Firebase AI Logic, precios: https://firebase.google.com/docs/ai-logic/pricing
- Firebase AI Logic, filtros de seguridad: https://firebase.google.com/docs/ai-logic/safety-settings
- Firebase AI Logic, cuotas: https://firebase.google.com/docs/ai-logic/quotas
- AdMob, verificación con app-ads.txt: https://support.google.com/admob/answer/14538460
