# Rediseño: la bola como personaje

Estado: diseño aprobado (propuesta B, 18 nov 2026) y componentes elegidos (variante Luz, 19 nov
2026). La implementación no ha empezado.

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
| [../plans/migracion-kmp.pdf](../plans/migracion-kmp.pdf) | Plan de migración a Kotlin Multiplatform e iOS, que va después del rediseño |

## Lienzo de diseño

Los diseños están en el lienzo «Rediseño de la bola» de Claude:
https://claude.ai/artifact/HT9sVVKHCiHjhWFJLgdc2E. Es privado y solo lo abre su dueño.

- Página «Ojos en el círculo»: la propuesta aprobada. Incluye la portada, la pantalla principal, la
  pantalla con la respuesta, los estados, los oráculos, la secuencia de respuesta, la ventana de
  cada oráculo y una animación en bucle.
- Página «Cara en la ventana»: la propuesta A, descartada, que se guarda como referencia.
- Página «Componentes · Luz»: el sistema de componentes elegido. Incluye la base, los botones, la
  selección, los avisos, tres pantallas y la comparación con la versión con canto.
- Página «Componentes · Canto»: la primera versión de los componentes, descartada por parecerse
  demasiado a Duolingo.

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
