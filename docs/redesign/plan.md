# Rediseño de la bola: plan

Estado: aprobado para empezar por la fase 0. 18 nov 2026.

## Objetivo

Llevar a la app la propuesta B del lienzo: la bola como personaje con ojos en el círculo blanco,
oráculos con accesorio, color y mirada propios, y la respuesta que aparece al girar. Se hace con
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
| 2 · La bola | `OracleBall`: cuerpo, borde de color, cara con ojos, ventana con líquido y triángulo, accesorios, catálogo de expresiones y la máquina de estados de la animación | Una captura por oráculo y por expresión; tests de la máquina de estados; sin fotogramas lentos en el Pixel 8 Pro (Macrobenchmark); con «reducir movimiento» no gira | 4–6 días |
| 3 · Pantalla principal | La nueva bola en la pantalla, la respuesta en la ventana, la frase del personaje debajo y la vibración sincronizada con agitar y girar | Con el interruptor encendido, todo el ciclo de pregunta funciona en el móvil, incluidos los casos de fallo (`ai_miss`) | 3–5 días |
| 4 · Oráculos | Cada estilo actual recibe su aspecto (accesorio, color y mirada); los personajes propios también; el selector y la lista de Personajes | Todos los estilos y los personajes propios se ven con su aspecto, con capturas | 3–4 días |
| 5 · Resto de pantallas | Compartir, historial, ajustes y reloj con el nuevo sistema | Ninguna pantalla mezcla el estilo viejo y el nuevo | 3–5 días |
| 6 · Lanzamiento | Prueba interna; luego el 10 %, el 50 % y el 100 % con el interruptor | Las métricas no empeoran frente a la interfaz actual | 2–3 semanas de calendario |

En total, unas 4–5 semanas de trabajo antes del lanzamiento gradual. Es una estimación.

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
| La animación va lenta en móviles modestos | Se mide en la fase 2 con un móvil de gama baja además del Pixel. Si hace falta, el giro pasa a un efecto 2D más simple |
| Las frases incluidas en la app no dicen si son sí, no o quizá, y la bola no sabría qué gesto poner | Etiquetar las listas de `magic_8_ball_responses.json` en la fase 3. Las respuestas de la IA ya llevan su `Verdict` |
| Los 20 estilos actuales son muchos para diseñar uno a uno | Fase 4: primero los 6 del lienzo; el resto hereda color y mirada, y su accesorio llega por tandas |
| Cambiar la interfaz rompe el hábito de los usuarios actuales | Lanzamiento gradual con el interruptor, que se puede apagar en minutos |
| El rediseño se cruza con la salida a producción | El interruptor sigue apagado hasta la fase 6, y la versión de producción no depende de él |

## Decisiones pendientes

- Qué estilos actuales pasan a llamarse como los oráculos del lienzo (Vidente, Genio, Bruja, Mago,
  Filósofo, Astróloga) y cuáles conservan su nombre.
- Si la ventana de respuesta lleva sonido.
- Si los avatares y la tarjeta de compartir mantienen la composición de la bola entrando por la
  esquina, inclinada unos 18°, ahora con los ojos en el círculo blanco (preferencia del 16 nov).
