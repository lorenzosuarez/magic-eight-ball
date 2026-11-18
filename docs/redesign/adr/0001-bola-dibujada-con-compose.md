# ADR 0001: la bola se dibuja con Compose

Estado: aceptada. 18 nov 2026.

## Contexto

La bola del rediseño cambia de mirada, de color y de accesorio según el oráculo y el estado. Además
gira para enseñar la ventana, el triángulo sube del líquido con un texto que cambia en cada
respuesta, y todo tiene que ir fluido en el móvil y en el reloj, y más adelante en iOS mediante
Kotlin Multiplatform. La bola actual ya se dibuja con Canvas de Compose (`ui/EightBall.kt`).

## Decisión

La bola es un componente de Compose, `OracleBall`, dibujado con Canvas (círculos, trayectorias,
degradados y texto) y animado con las APIs de animación de Compose (`updateTransition`,
`Animatable`, `keyframes`, muelles). Vive en `:core:designsystem`, sin dependencias de Android.

## Alternativas descartadas

| Alternativa | Por qué no |
|---|---|
| Lottie | Cada combinación de oráculo, mirada y respuesta sería otro archivo o una capa que cambiar en tiempo de ejecución. El texto de la respuesta es dinámico. No tiene versión oficial para Compose Multiplatform |
| Rive | Tiene máquina de estados, pero cada plataforma usa su propio motor (Android, iOS), sin integración oficial en Compose Multiplatform. Añade una dependencia y un editor externo al flujo |
| Vídeo o GIF | No se adapta a cada oráculo ni a cada respuesta, pesa y no se puede interrumpir |
| Imágenes por capas | Escalan mal entre el reloj y el móvil y multiplican los recursos por oráculo |

## Consecuencias

- Un único código para el móvil, el reloj y, con KMP, iOS.
- Cada oráculo y cada expresión es un dato (colores, forma de los ojos, accesorio), no un recurso
  gráfico.
- La animación responde a gestos y se puede interrumpir, por ejemplo si el usuario vuelve a
  preguntar a mitad.
- El texto de la respuesta se dibuja con la misma tipografía que el resto de la app.
- El coste es que hay que escribir el dibujo a mano y cuidar el rendimiento. Se mitiga leyendo los
  valores animados solo en la fase de dibujo, guardando en caché las trayectorias con
  `drawWithCache` y midiendo con Macrobenchmark en la fase 2.
