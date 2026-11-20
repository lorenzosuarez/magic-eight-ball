# ADR 0004: componentes con lenguaje propio, la variante Luz

Estado: aceptada. 19 nov 2026. La forma de los botones (punto 4) la sustituye el
[ADR 0005](0005-botones-radio-16.md). Actualizada el 20 nov 2026: ver «Actualización», al final.

## Contexto

- Se pidió una sección de componentes inspirada en Duolingo, sin copiarla y en azul.
- La primera versión, «Canto» (página «Componentes · Canto» del lienzo), se parecía demasiado a
  Duolingo:
  - un borde inferior sólido de 4 px bajo cada control;
  - opciones en losas con borde, relleno y canto azules;
  - bocadillo con pico;
  - contador de racha en la barra superior;
  - letra redonda y gruesa.
- Una revisión de diseño independiente puntuó su parecido con Duolingo en 7 sobre 10.
- Un primer intento de Luz quitó el canto, pero llenó la pantalla de brillos de color. Se parecía a
  cualquier app de IA oscura. Además, en Android las sombras de color solo existen desde la API 28 y
  se repintan en cada fotograma.

## Decisión

Los componentes siguen la variante Luz, en la página «Componentes · Luz» del lienzo:

1. **Solo brilla la bola,** con un aura radial en el color del oráculo. Ningún otro elemento lleva
   sombra de color ni halo.
2. **La interfaz tiene un único acento,** el cobalto `#4361EE`, y el color lo ponen los oráculos.
   - El ámbar `#FFB547` es solo para avisos y el coral `#FF8A8A` solo para errores.
   - Premium usa el cobalto.
3. **Pulsar oscurece el control y lo encoge al 98 %.**
   - El cobalto pasa a `#3B55D1` y las superficies a `#1E2C3B`.
   - Entra en 90 ms y sale en 150 ms.
   - El foco es un anillo de 2 px `#A9BCFF`.
4. **Formas de la bola.**
   - Botones en píldora e iconos en círculo. Sustituido por el ADR 0005: radio 16.
   - Radio 24 en los grupos y 32 en las hojas.
   - Superficies separadas por tono, sin bordes gruesos; separadores de blanco al 8 %.
5. **Lo elegido se marca con ✓ cobalto y fondo `#152240`, nunca solo con color.** Las listas que
   actúan al tocar no llevan marca.
6. **La frase del oráculo va sin bocadillo.**
   - Se escribe en Fraunces 500, 24/30, con la etiqueta «▲ Vidente añade» en el color del oráculo.
   - Títulos y botones van en Outfit; el resto, en Inter.
7. **El Mago pasa a turquesa `#34D1DB` y el Filósofo a piedra `#C4B59F`.** Sus colores anteriores
   coincidían con el acento y con el gris de lo desactivado.

Los valores exactos están en la sección 9 de [especificacion.md](../especificacion.md).

## Alternativas descartadas

- **Canto:** demasiado cerca de Duolingo.
- **Luz con brillo en todos los controles:** ruido visual, aspecto de plantilla y coste de dibujo.
- **Mago en azul cielo `#56C8F5`:** está a ΔE00 9 del azul de Duolingo.

## Consecuencias

- **Fuentes:** Fraunces y Outfit se incluyen en la app; las dos son OFL. De Fraunces basta un corte
  estático de peso 500 con los caracteres latinos.
- **El aura es un degradado radial:** funciona desde la API 26 y no cuesta en móviles de gama media.
- **Textos nuevos en los tres idiomas:**
  - «Sin ellas no te llega el veredicto del día.», en el aviso de notificaciones;
  - «Reintentar», en el error;
  - «Deshacer», en el aviso que sale tras reportar.
- **Función nueva:** deshacer un reporte, durante 5 s.
- **Lienzo desactualizado:** la página «Ojos en el círculo» conserva los colores antiguos del Mago y
  del Filósofo. Donde no coincidan, manda la especificación.

## Actualización (20 nov 2026)

La revisión de componentes del 20 nov 2026 corrigió cuatro puntos. La decisión no cambia; los valores
están en las secciones 2, 8 y 9 de [especificacion.md](../especificacion.md).

- **Coral (punto 2):** el coral `#FF8A8A` es para el error de un campo y para lo que borra algo que
  no se puede deshacer: el botón de borrar y «Eliminar cuenta», siempre con su diálogo. Lo que se
  puede deshacer se borra con un icono neutro y el aviso breve con «Deshacer».
- **Pulsar (punto 3):** las superficies no se oscurecen al pulsar: suben un tono y se aclaran
  (`#16222F` → `#1E2C3B`). El cobalto sí pasa a `#3B55D1`. Solo los controles sueltos se encogen al
  98 %; las filas, no. El anillo de foco deja transparente el hueco de 2 px.
- **Deshacer un reporte (Consecuencias):** no dura 5 s fijos. El aviso dura lo que diga
  `getRecommendedTimeoutMillis` sobre 10 s y, con TalkBack, no se va solo.
- **Fraunces (Consecuencias):** el corte estático fija también sus ejes, SOFT 30 y opsz 24, como el
  lienzo.

El mismo día, el [ADR 0007](0007-doce-personajes.md) cambió dos valores de este ADR: el ámbar de
los avisos pasa de `#FFB547` a `#FF9F43` (punto 2), y el Mago y el Filósofo, a `#45D3F2` y
`#B0A08A` (punto 7).
