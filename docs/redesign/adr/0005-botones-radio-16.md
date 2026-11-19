# ADR 0005: botones con radio 16 en lugar de píldora

Estado: aceptada. 19 nov 2026. Sustituye la forma de los botones del punto 4 del
[ADR 0004](0004-componentes-luz.md).

## Contexto

- El ADR 0004 dejó los botones en píldora y los botones de icono en círculo.
- Al dibujar todas las pantallas en Luz se probó la redondez de Duolingo: esquinas de radio 16 en
  lugar de píldora. Está en la página «Pantallas · Luz» del lienzo, con 36 pantallas.
- Se toma solo la redondez. No vuelve el borde inferior de 4 px de la versión Canto, que fue lo
  que la acercó a Duolingo.
- Dos revisiones de diseño independientes recomiendan quedarse con el radio 16:
  - encaja en las hojas: una hoja de radio 32 con 16 de margen deja un radio concéntrico de 16;
  - coincide con los campos y los botones de icono, que ya usan 16;
  - los botones anchos de las hojas se ven más amables que una píldora.
- Con radio 16, un botón de 44 de alto parece media píldora, y había cuatro alturas de botón
  (44, 48, 52 y 56).

## Decisión

1. **Los botones con relleno miden 52 de alto y tienen radio 16.**
   - Son el principal, el secundario, el tonal, el ámbar y el de borrar.
   - Llevan 20 de relleno lateral.
2. **Los botones de texto miden 48 de alto y tienen radio 16.** El fondo solo se ve al pulsar, en
   `#1E2C3B`.
3. **Los botones de icono miden 48, y 52 en la fila de la respuesta, con radio 16.** En la barra
   superior van sin fondo.
4. **Los campos de texto miden 56 de alto y tienen radio 16.** Los chips miden 36 de alto y tienen
   radio 12.
5. **Lo demás no cambia.**
   - Los grupos tienen radio 24, los diálogos 28 y las hojas 32.
   - Siguen redondos del todo el interruptor, las etiquetas, los avatares y la bola.
   - En el reloj, los botones y las opciones siguen en píldora de 52, como es habitual en Wear OS.

Los valores exactos están en la sección 9 de [especificacion.md](../especificacion.md).

## Alternativas descartadas

- **Píldora, como decía el ADR 0004:** mezcla dos formas en la misma pantalla, porque los campos y
  los botones de icono ya tienen radio 16.
- **Mantener en 44 los botones tonales y ámbar:** a esa altura, el radio 16 los hace parecer media
  píldora junto a los de 52.

## Consecuencias

- **Sistema de diseño:** tiene un token de forma de radio 16 para botones, campos y botones de
  icono, y otro de radio 12 para los chips.
- **Altura:** la fila de Premium y los avisos crecen 8 dp, porque «Obtener» y los botones ámbar
  pasan de 44 a 52.
- **Lienzo desactualizado:** la página «Componentes · Luz» todavía dibuja los botones en píldora.
  Donde no coincidan, mandan este ADR y la especificación.
