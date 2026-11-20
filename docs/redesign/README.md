# Rediseño: la bola como personaje

Estado: diseño aprobado (propuesta B, 18 nov 2026) y componentes elegidos (variante Luz con
botones de radio 16, 19 nov 2026). La implementación no ha empezado.

La bola 8 pasa a ser un personaje. Donde iba el 8, en el círculo blanco, tiene dos ojos pequeños
en forma de cápsula, sin boca y sin brillos, y toda su expresión sale de la forma de esos ojos.
Cada oráculo (Vidente, Genio, Bruja…) es esa misma bola con otro accesorio, otro color de luz y
otra mirada. Para responder, la bola gira sobre sí misma y enseña su ventana: un triángulo sube del
líquido con la respuesta, como en la bola 8 de siempre. Después vuelve a mirarte con el gesto que
corresponde a esa respuesta.

## Documentos

| Documento | Qué contiene |
|---|---|
| [plan.md](plan.md) | Principios, fases con su entregable y su criterio de hecho, forma de trabajo y riesgos |
| [especificacion.md](especificacion.md) | Anatomía de la bola, colores, oráculos, catálogo de expresiones, ventana de respuesta, animación con tiempos, pantallas y accesibilidad |
| [adr/0001-bola-dibujada-con-compose.md](adr/0001-bola-dibujada-con-compose.md) | Por qué la bola se dibuja con Compose y no con Lottie, Rive ni vídeo |
| [adr/0002-diseno-listo-para-kmp.md](adr/0002-diseno-listo-para-kmp.md) | Reglas para que el sistema de diseño y la bola pasen a Kotlin Multiplatform sin reescribirse |
| [adr/0003-lanzamiento-con-interruptor.md](adr/0003-lanzamiento-con-interruptor.md) | Cómo se lanza sin poner en riesgo la versión de producción |
| [adr/0004-componentes-luz.md](adr/0004-componentes-luz.md) | Por qué los componentes siguen la variante Luz y no la versión con canto, inspirada en Duolingo |
| [adr/0005-botones-radio-16.md](adr/0005-botones-radio-16.md) | Por qué los botones tienen radio 16 y no forma de píldora |
| [../plans/migracion-kmp.pdf](../plans/migracion-kmp.pdf) | Plan de migración a Kotlin Multiplatform e iOS, que va después del rediseño |

## Lienzo de diseño

Los diseños están en el lienzo «Rediseño de la bola» de Claude:
https://claude.ai/artifact/HT9sVVKHCiHjhWFJLgdc2E. Es privado y solo lo abre su dueño.

- Página «Diseño»: todo lo elegido, en una sola página y en este orden.
  - La bola: la portada, los estados, los oráculos y la ventana de cada uno.
  - La respuesta, en movimiento: la animación en bucle, los ocho personajes (los seis oráculos y
    Robot y Pirata, que ya son estilos de respuesta de la app) y los cinco pasos. Los tableros de los
    oráculos, de las ventanas y de los pasos se dibujan a partir de la animación, así que coinciden
    con ella.
  - La prueba «¿Gira o se le abre la cara?»: la versión que gira junto a otra en la que la cara se
    vuelve la ventana. Está en la sección 6 de la especificación.
  - Los componentes Luz, con los botones de radio 16 del ADR 0005.
  - Todas las pantallas: la bola en cada estado, Recientes, Opciones, los personajes, la cuenta,
    Premium, reportar, la tarjeta de compartir, la notificación, el widget y el reloj. La sección 7
    de la especificación las describe una a una.
- Página «Descartado · Canto»: la primera versión de los componentes, descartada por parecerse
  demasiado a Duolingo, con la comparación frente a Luz.

La propuesta A, con la cara dentro de la ventana, se borró del lienzo el 20 nov 2026.

Si este documento y el lienzo no coinciden, manda este documento: aquí están los valores exactos.

## Decisiones clave

1. Propuesta B: ojos en el círculo blanco, sin boca y sin brillos (18 nov 2026).
2. La respuesta se ve al girar la bola, en la ventana con el triángulo. En la ventana van de una a
   tres palabras; la frase del personaje va debajo de la bola.
3. La bola se dibuja con Compose (ADR 0001).
4. El sistema de diseño y la bola se escriben listos para KMP desde el primer día (ADR 0002).
5. Todo se lanza detrás de un interruptor de Remote Config, sin bloquear la salida a producción del
   27 nov (ADR 0003).
6. Componentes de la variante Luz (19 nov 2026, ADR 0004):
   - solo brilla la bola;
   - el cobalto es el único acento de la interfaz;
   - formas tomadas de la bola;
   - lo elegido se marca con ✓;
   - el Mago pasa a turquesa y el Filósofo a piedra.
7. Botones con radio 16 en lugar de píldora (19 nov 2026, ADR 0005):
   - los botones con relleno miden 52 de alto;
   - los de texto miden 48.
