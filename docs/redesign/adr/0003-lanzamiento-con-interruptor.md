# ADR 0003: el rediseño se lanza con un interruptor

Estado: aceptada. 18 nov 2026.

## Contexto

La app sale a producción a finales de noviembre de 2026 con la interfaz actual. El rediseño dura
varias semanas y no puede bloquear esa salida ni las versiones que vengan después.

## Decisión

- Un parámetro de Remote Config, `redesign_enabled` (booleano, `false` por defecto), elige la
  interfaz nueva o la actual al abrir la pantalla principal. Se documenta en `docs/REMOTE_CONFIG.md`
  cuando se cree.
- El código del rediseño se fusiona en `master` en PRs pequeños, apagado, y viaja en las versiones
  normales sin que nadie lo vea.
- Se enciende por fases con condiciones de Remote Config: primero los dispositivos de prueba,
  después el 10 %, el 50 % y el 100 % de los usuarios.
- Cada salto se decide con las métricas del plan (fase 6). Si algo empeora, se apaga sin publicar
  una versión nueva.
- Cuando el 100 % lleve dos semanas sin problemas, se borra la interfaz antigua y el interruptor en
  un PR propio.

## Consecuencias

- Producción nunca depende de que el rediseño esté terminado.
- Durante unas semanas conviven las dos interfaces en el código. Ese coste es temporal y termina con
  el último PR.
- Las métricas tienen que poder separarse por interfaz: el evento `ask` gana un parámetro con la
  interfaz en uso. Como cambia lo que se mide, se actualiza también la política de privacidad.
