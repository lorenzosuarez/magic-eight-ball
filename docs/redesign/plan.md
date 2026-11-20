# Rediseño de la bola: plan

Estado: aprobado para empezar por la fase 0. 18 nov 2026. Actualizado el 20 nov 2026 con la cara de
cristal y los doce personajes: ver «Cambios del 20 nov 2026».

## Objetivo

Llevar a la app la propuesta B del lienzo: la bola como personaje con ojos en el círculo blanco,
doce oráculos con accesorio, color y mirada propios, y la respuesta que aparece cuando su cara se
vuelve de cristal. Se hace con
estándares de producto de empresa: cada paso se mide, se prueba y se revisa antes de fusionarse, y
el código queda listo para compartirse con iOS mediante Kotlin Multiplatform.

## Fuera de alcance

- La migración a KMP y la app de iOS: tienen su propio plan, `../plans/migracion-kmp.pdf`, y van
  después.
- Cambios en la IA, en el cobro o en la cuenta.
- Preguntar escribiendo: se sigue preguntando solo por voz, con un toque o agitando.

## Punto de partida (medido el 18 nov 2026)

| Pieza | Estado |
|---|---|
| Bola actual | `app-phone/.../ui/EightBall.kt`, 914 líneas, dibujada con Canvas de Compose |
| Pantalla principal | `ui/EightBallScreen.kt`, 1.947 líneas |
| Interfaz del móvil | 11.280 líneas en `app-phone/.../ui` |
| Sistema de diseño | `:core:designsystem` tiene 29 líneas; el tema vive en `app-phone/.../ui/Theme.kt` |
| Compose | BOM `2024.09.00`, que hay que actualizar; Kotlin `2.3.21` |
| Pruebas de pantalla | Roborazzi ya se usa, por ejemplo en `EightBallScreenshotTest` |
| Fases de la bola | `BallPhase`: `Idle`, `Listening`, `Thinking`, `Answered` |
| Tipo de respuesta | `Verdict` en `core:domain` dice si la respuesta de la IA es sí, no o indecisa |
| Reloj | `app-wear`, con su propia interfaz |

## Principios

1. **Una sola fuente del diseño.** Los tokens (colores, tono, tipografía, espaciado, radios, tiempos
   de animación y vibración) y los componentes viven en `:core:designsystem`. Ninguna pantalla define
   colores ni tiempos propios.
2. **Listo para KMP desde el primer día.** El sistema de diseño, la bola y el dominio se escriben en
   Kotlin y Compose comunes, sin APIs de Android (ADR 0002). Una regla automática lo comprueba en
   el CI.
3. **La bola se dibuja con Compose** (ADR 0001): es vectorial, se adapta a cada oráculo y a cada
   gesto, y es el mismo código en el móvil, en el reloj y en iOS.
4. **Producción no se toca.** Todo va detrás de `redesign_enabled` (ADR 0003). La salida del 27 nov
   sigue con la interfaz actual.
5. **Nada se fusiona sin pruebas ni revisión.** Cada PR lleva capturas, tests y el CI en verde, y
   Lorenzo da el sí a cada fusión.

## Fases

| Fase | Qué se entrega | Hecho cuando | Estimación |
|---|---|---|---|
| 0 · Cimientos | Compose actualizado; el tema movido a `:core:designsystem`; la regla de arquitectura (Konsist) en el CI; el interruptor `redesign_enabled` apagado; estos documentos | La app se ve igual, todos los tests pasan y la regla falla si alguien usa `android.*` en los módulos comunes | 1–2 días |
| 1 · Sistema de diseño | Los tokens de la sección 2 de `especificacion.md`, los componentes de la sección 9 (botones, filas de lista, chips, campos, hojas, diálogos, avisos y selector de oráculo) y una galería interna para verlos | Cada componente tiene su captura de referencia en oscuro, con la letra al 100, 130 y 200 % | 2–3 días |
| 2 · La bola | `OracleBall`: cuerpo, borde de color, cara con ojos, la cara de cristal sobre la ventana con líquido y triángulo, accesorios, catálogo de expresiones y gestos, y la máquina de estados de la animación | Una captura por oráculo y por expresión; tests de la máquina de estados; sin fotogramas lentos en el Pixel 8 Pro (Macrobenchmark); con «reducir movimiento», solo fundidos | 4–6 días |
| 3 · Pantalla principal | La nueva bola en la pantalla, la respuesta en la cara de cristal, la frase del personaje debajo y la vibración sincronizada con agitar y con la llegada del triángulo | Con el interruptor encendido, todo el ciclo de pregunta funciona en el móvil, incluidos los casos de fallo (`ai_miss`) | 3–5 días |
| 4 · Oráculos | Cada estilo actual recibe su aspecto (accesorio, color y mirada); los cuatro personajes nuevos; los personajes propios también; el selector y la lista de Personajes, con sus dos grupos | Todos los estilos, los doce del lienzo y los personajes propios se ven con su aspecto, con capturas | 3–4 días, por revisar con los cuatro nuevos |
| 5 · Resto de pantallas | Compartir, historial, ajustes y reloj con el nuevo sistema | Ninguna pantalla mezcla el estilo viejo y el nuevo | 3–5 días |
| 6 · Lanzamiento | Prueba interna; luego el 10 %, el 50 % y el 100 % con el interruptor | Las métricas no empeoran frente a la interfaz actual | 2–3 semanas de calendario |

En total, unas 4–5 semanas de trabajo antes del lanzamiento gradual. Es una estimación.

## Cambios del 20 nov 2026

La cara de cristal sustituye al giro como respuesta ([ADR 0006](adr/0006-respuesta-cara-de-cristal.md))
y los personajes pasan a doce ([ADR 0007](adr/0007-doce-personajes.md)). Los valores salen de las
secciones 3, 4 y 6 de `especificacion.md`, que copian el motor de animación del lienzo. En la app,
con Jetpack Compose:

| Paso | Fase | Qué se hace | Hecho cuando |
|---|---|---|---|
| 1 · La cara de cristal | 2 | `OracleBall` dibuja la ventana detrás de la cara y anima, con los tiempos de la sección 6 como tokens de `LuzMotion`: los ojos que se cierran, la respiración (sube 3,5 y se estira un 2 %), el fundido de la cara y de lo que lleva, el destello, el bisel, el triángulo bajo la niebla y la cara que se empaña. Los valores se leen en la fase de dibujo (`drawWithCache`, `graphicsLayer` con lambdas). «Gira» no se construye; la proyección de la esfera sí, para la sacudida, los gestos que giran la cabeza y las vueltas del sorteo del modo fiesta | Capturas de Roborazzi en 2,3, 2,6, 3,3, 5,1 y 5,9 s del ciclo, con una mirada de trazo (Vidente) y una rellena (Mago); las interrupciones de la sección 6, con tests de la máquina de estados; con «reducir movimiento», solo fundidos de 200 ms |
| 2 · Miradas y gestos nuevos | 2 | Las miradas de reojo, mirando arriba a la izquierda, cerrados con calma hondos, rendijas, muy abiertos, sonrientes y pequeños, como datos (forma y medidas de la sección 4); los gestos parpadeo lento, aparta la cara, ¡gol!, al VAR, pisotón y lo prueba, con el retraso, la amplitud y el tempo de cada personaje (el Filósofo, 0,15 s tarde y al 70 %; el Dino, 0,08 s tarde y 1,25 veces más lento); las orejas de la Gata | Un test por gesto compara sus valores en varios instantes con los del motor del lienzo; una captura por mirada |
| 3 · Los cuatro personajes | 4 | Para la Gata, el Comentarista, la Repostera y el Dino: sus `OracleColors` (luz, triángulo, texto y borde); su nombre, su carácter y sus frases de ejemplo en es, en y pt; su estilo de respuesta para Gemini, una `CharacterSheet` en `Characters.kt` con la voz de la sección 3 (en inglés y sin muletillas literales, como las demás); sus `CharacterLines` y su banco de frases sin conexión en `magic_8_ball_responses.json`, en los tres idiomas y etiquetado con sí, no o quizá. La app ya tiene un estilo `CAT` («Gato») con ficha y frases: puede ser la base de la Gata (pendiente, abajo). Los otros tres son estilos nuevos de `Magic8BallPersonality` y `CharacterLook`, que se guardan por su nombre | El test de `OracleColors` pasa con los doce: 4,5:1 de la etiqueta y del texto del triángulo, y ΔE00 de 18 o más frente a la interfaz; `ShippedPromptsTest` recoge los estilos nuevos; textos en los tres idiomas |
| 4 · Avatares | 4 | El avatar de los doce, con la composición de la sección 9 cuando se decida | Una captura por personaje a 28, 40 y 88 |
| 5 · Personajes y `LP80` | 4 | La lista de Personajes con los grupos «Los de siempre» y «Con actitud» (`LP17Personajes`, y `LP80PersonajesConActitud` desplazada hasta el segundo), y los doce en el reloj (`LPW4Personaje`), con los cuatro nuevos al final | Capturas de las dos pantallas, también con la letra al 200 % |

- La fase 4 crece con los cuatro nuevos: su estimación está por revisar.
- La app es para adolescentes y adultos: ninguna ficha para Gemini ni ningún texto presenta a un
  personaje como pensado para una edad. La Repostera y el Dino son tiernos, nada más.

## Forma de trabajo

- Una rama por PR, que nunca empieza por `claude/`. Ningún PR supera unas 400 líneas de cambios
  salvo que sea código movido.
- Cada PR de interfaz incluye capturas de antes y después.
- Criterio de hecho de cada PR:
  - tests unitarios y de capturas en verde, y lint sin avisos nuevos;
  - la regla de arquitectura pasa;
  - accesibilidad revisada: TalkBack, contraste de 4,5:1, objetivos táctiles de 48 dp y tamaño de
    letra hasta el 200 %;
  - textos en español, inglés y portugués;
  - sin cambios en lo que se mide o se envía, o, si los hay, la política de privacidad actualizada
    en el mismo PR.
- Las decisiones de arquitectura nuevas se escriben como ADR en `adr/`.
- Lorenzo aprueba cada fusión. Nada se publica en Play sin su sí.

## Métricas del lanzamiento (fase 6)

Se comparan la interfaz nueva y la actual, con el mismo periodo y el mismo tipo de usuario:

- preguntas por usuario y día (evento `ask`);
- vuelta al día siguiente y a los 7 días;
- respuestas compartidas;
- fallos de la IA (`ai_miss`) y respuestas «Prueba otra vez luego.» (`source=try_again`);
- cierres de la app (Crashlytics) y fotogramas lentos (Android vitals).

## Riesgos

| Riesgo | Qué haríamos |
|---|---|
| La animación va lenta en móviles modestos | Se mide en la fase 2 con un móvil de gama baja además del Pixel. La respuesta ya no gira; si hace falta, los gestos que giran la cabeza y el sorteo pasan a un efecto 2D más simple |
| Las frases incluidas en la app no dicen si son sí, no o quizá, y la bola no sabría qué gesto poner | Etiquetar las listas de `magic_8_ball_responses.json` en la fase 3. Las respuestas de la IA ya llevan su `Verdict` |
| Los 20 estilos actuales son muchos para diseñar uno a uno | Fase 4: primero los 12 del lienzo; el resto hereda color y mirada, y su accesorio llega por tandas |
| Cambiar la interfaz rompe el hábito de los usuarios actuales | Lanzamiento gradual con el interruptor, que se puede apagar en minutos |
| El rediseño se cruza con la salida a producción | El interruptor sigue apagado hasta la fase 6, y la versión de producción no depende de él |

## Decisiones pendientes

- Qué estilos actuales pasan a llamarse como los oráculos del lienzo (Vidente, Genio, Bruja, Mago,
  Filósofo, Astróloga) y cuáles conservan su nombre.
- Si la Gata es el estilo «Gato» de hoy (`CAT`), que ya tiene ficha y frases, o un personaje aparte.
- Las del 20 nov 2026, en la sección 7 de `especificacion.md`: «Giro clásico» en Opciones; si los
  cuatro nuevos son gratis o de Premium; sus nombres en inglés y portugués; los colores de su
  tarjeta de compartir; y el sitio para saltar encima de la bola.
- Si la ventana de respuesta lleva sonido.
- Si los avatares y la tarjeta de compartir mantienen la composición de la bola entrando por la
  esquina, inclinada unos 18°, ahora con los ojos en el círculo blanco (preferencia del 16 nov).
