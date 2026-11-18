# ADR 0002: el sistema de diseño y la bola, listos para KMP

Estado: aceptada. 18 nov 2026.

## Contexto

Después del rediseño, la app pasará a Kotlin Multiplatform con Compose Multiplatform para salir en
iOS (`../../plans/migracion-kmp.pdf`). Si el sistema de diseño y la bola nacen con dependencias de
Android, habrá que reescribirlos entonces.

## Decisión

`:core:designsystem` y `:core:domain` se escriben como si ya fueran código común:

- Sin imports de `android.*` ni de `androidx` exclusivos de Android. Se permiten `compose.ui`,
  `compose.foundation`, `compose.animation`, `compose.runtime` y Material 3, que existen en Compose
  Multiplatform.
- Sin `R`, `Context`, `LocalContext` ni recursos de Android dentro de los componentes. Los textos y
  las descripciones de accesibilidad se reciben como parámetros. Al migrar se pasan a los recursos
  de Compose Multiplatform.
- Sin `java.time` ni otras APIs exclusivas de la JVM en el dominio nuevo: se usa
  `kotlinx-datetime`.
- Lo que solo existe en una plataforma (vibración, sensor de agitado, sonido) se expone con una
  interfaz en el código común, y cada plataforma la implementa por su cuenta.
- Una prueba de arquitectura con Konsist, en el CI, falla si alguno de estos módulos importa
  `android.*`, `java.time` o `R`.

## Consecuencias

- Al migrar, estos módulos pasan a `commonMain` casi sin cambios. La estimación del plan KMP
  depende de que esta regla se cumpla.
- Algunas cosas cómodas de Android (por ejemplo `stringResource` dentro de un componente) quedan
  fuera de estos módulos y pasan a la pantalla que los usa.
- Hilt sigue en la app de Android. El sistema de diseño y la bola no dependen de inyección.
