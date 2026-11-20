# Rediseño de la bola: especificación

Estado: propuesta B aprobada el 18 nov 2026 y componentes de la variante Luz elegidos el 19 nov 2026
([ADR 0004](adr/0004-componentes-luz.md)). Los valores salen de la página «Diseño» del lienzo de
diseño.

Todas las medidas de la bola están en un lienzo de 200 × 210 unidades, con el centro del cuerpo en
(100, 104). En la app se escalan al tamaño de la bola en pantalla: una unidad equivale al ancho
disponible dividido entre 200.

## 1. Anatomía

| Parte | Forma | Valores |
|---|---|---|
| Cuerpo | Círculo | Centro (100, 104), radio 82. Degradado radial con centro en (0,36; 0,3) y radio 0,78: `#4B5166` → `#1C1F2B` (45 %) → `#0A0B10` |
| Borde de luz | Aro | Radio 81, trazo de 2,4, color del oráculo al 70 % |
| Brillo | Elipse | Centro (66, 56), radios 22 × 11, girada −34°, blanco al 14 % |
| Aura | Círculo detrás del cuerpo | Radio 106. Degradado radial del color del oráculo: 24 % hasta el 76 % del radio y 0 % en el borde. Es lo único que brilla en la pantalla |
| Sombra | Elipse bajo la bola | Centro (100, 200), radios 58 × 6, negro al 30 % |
| Sombra del borde | Círculo sobre el cuerpo | Radio 82. Degradado radial negro con centro en (96, 98) y radio 90: 0 % hasta el 60 % y 55 % en el borde. Va encima de la cara y la ventana y debajo del borde de luz, el brillo y el accesorio. En reposo no toca la cara; al girar, oscurece lo que se acerca al borde |
| Cara | Círculo | Centro (100, 106), radio 44. Degradado mate con centro en (0,45; 0,38) y radio 0,72: `#FFFFFF` → `#F1EEEB` (70 %) → `#DFDBD7` |
| Ojos | Dos cápsulas | 7,6 × 16, radio de esquina 3,8, en x = 83,2 y x = 109,2, y = 96. Color `#16171D`. Sin boca y sin brillos |
| Ventana | Círculo, en la cara opuesta | Centro (100, 106), radio 46. Líquido con degradado radial: `#23308A` → `#0E1550` (60 %) → `#05081C`. Aro interior de radio 44, negro al 35 %, trazo de 4 |
| Burbujas | Círculos | Radios 1,3–2, `#8FA0FF` al 35 % |
| Triángulo | Triángulo hacia arriba | Vértices (100, 68), (137, 132) y (63, 132). Degradado vertical del color del oráculo. Contorno de 1,2 al 60 % |
| Texto de la respuesta | Dentro del triángulo | Inter Bold, tamaño 9, espaciado 0,2, mayúsculas, centrado en x = 100. Dos líneas como máximo, con las bases en y = 106 y 119. Si una palabra no cabe, el tamaño baja por pasos hasta 7,6 |

La bola se inclina unos grados según el estado (entre −16° y 10°). Los accesorios y la cara giran
con ella.

## 2. Tokens: color, tono, tipo, espacio y forma

Variante Luz ([ADR 0004](adr/0004-componentes-luz.md)): un único acento, el cobalto; el resto del
color lo ponen los oráculos. Las medidas van en dp, la letra en sp y los tiempos en ms, salvo que se
diga otra cosa.

**Dónde viven.** Todos los tokens están en `:core:designsystem`, en Kotlin común
([ADR 0002](adr/0002-diseno-listo-para-kmp.md)). Son objetos inmutables (`@Immutable`) que
`LuzTheme { }` provee con `staticCompositionLocalOf`, y se leen así: `LuzTheme.colors.accent`,
`LuzTheme.type.rowTitle`. Ninguna pantalla escribe un color, un tamaño de letra, una medida, un
tiempo ni una curva. Si falta uno, se añade aquí.

| Objeto | Qué lleva | Cambia con la app abierta |
|---|---|---|
| `LuzColors` | Primitivas y roles de color | No |
| `OracleColors` | Los roles del personaje que responde | Al cambiar de personaje |
| `LuzTypography` | La escala de letra | No |
| `LuzSpacing`, `LuzShapes`, `LuzSizes` | Espaciado, radios y tamaños | No |
| `LuzMotion` | Duraciones, curvas y si se reduce el movimiento | Al cambiar «Reducir movimiento» |
| `LuzHaptics` | La vibración, como interfaz común que implementa cada plataforma | No |

### Color: primitivas

| Familia | Pasos |
|---|---|
| Cobalto | 900 `#152240` · 800 `#1D2C4E` · 700 `#263A66` · 600 `#3B55D1` · 500 `#4361EE` · 300 `#7C96FF` · 200 `#A9BCFF` · 100 `#C9D6FF` |
| Noche | 990 `#01060C` · 950 `#030B14` · 900 `#0D1824` · 800 `#16222F` · 750 `#1E2C3B` · 700 `#263647` (nuevo) |
| Niebla | 50 `#F2F5F8` · 200 `#C9D1DA` · 300 `#9AA7B4` · 500 `#808D9A` · 600 `#6B7886` · 700 `#56626F` |
| Ámbar | 500 `#FFB547` · 600 `#E9A43F` · 900 `#2A1A00` · 950 `#21190C` |
| Coral | 400 `#FF8A8A` · 900 `#3A1C22` · 950 `#2A1418` |
| Sueltos | Blanco `#FFFFFF` · Negro `#000000` · Tinta `#16171D` · Asa `#2A3644` |

Las pantallas y los componentes no usan primitivas: usan roles.

### Color: roles

| Rol | Valor | Uso |
|---|---|---|
| `bg` | Noche 950 | Fondo de las pantallas y de la barra superior. Nivel 0 |
| `surface` | Noche 900 | Grupos sobre el fondo, hojas y diálogos. Nivel 1 |
| `control` | Noche 800 | Controles; grupos dentro de una hoja o un diálogo. Nivel 2 |
| `surfaceOverlay` | Noche 750 | Pulsado del nivel 2, aviso breve y etiqueta emergente. Nivel 3. Antes `pressed` |
| `surfaceHighest` | Noche 700 | Pulsado del nivel 3. Nivel 4 |
| `text` | Niebla 50 | Texto principal |
| `contentMedium` | Niebla 200 | Iconos, cuerpo de diálogos y avisos, ayuda de la bola, muestras en Fraunces. Sustituye a `icon`, que ya se usaba como texto |
| `textSecondary` | Niebla 300 | Texto secundario, cabeceras de grupo, contadores, › y ↗ |
| `disabled` | Niebla 500 | Contenido desactivado y marcador de los campos |
| `outline` | Niebla 600 | Contorno de los campos y del interruptor apagado |
| `outlineVariant` | Blanco al 8 % | Separadores, contorno de los controles y de los diálogos, borde superior de las hojas (antes al 6 %) |
| `pageDot` | Niebla 700 | Puntos de las páginas que no se ven |
| `scrim` | Noche 990 al 72 % | Velo detrás de hojas y diálogos |
| `accent` | Cobalto 500 | Botón principal e interruptor encendido. El único acento |
| `accentPressed` | Cobalto 600 | Botón principal pulsado |
| `onAccent` | Blanco | Texto e indicador sobre el cobalto |
| `accentHighlight` | Blanco al 14 % | Línea interior de 1 en el borde superior del botón principal |
| `check` | Cobalto 300 | ✓ de lo elegido, pulgar marcado y anillo de la muestra elegida |
| `selected` | Cobalto 900 | Fondo de lo elegido |
| `selectedPressed` | Cobalto 800 | Lo elegido, pulsado |
| `selectedOutline` | Cobalto 300 al 40 % | Contorno de 1 de lo elegido |
| `link` | Cobalto 200 | Botones de texto |
| `focusRing` | Cobalto 200 | Anillo de foco y contorno del campo con foco |
| `cursor` | Cobalto 200 | Cursor y asas de selección de los campos |
| `textSelection` | Cobalto 200 al 40 % | Fondo del texto seleccionado |
| `tonal` / `onTonal` | Cobalto 800 / Cobalto 100 | Botón tonal |
| `tonalPressed` | Cobalto 700 | Botón tonal pulsado |
| `warning` / `onWarning` | Ámbar 500 / Ámbar 900 | Icono de los avisos, botón ámbar y acción del aviso compacto |
| `warningPressed` | Ámbar 600 | Botón ámbar pulsado |
| `warningContainer` | Ámbar 950 | Fondo de los avisos que piden atención |
| `warningOutline` | Ámbar 500 al 22 % | Contorno del aviso |
| `error` | Coral 400 | El error de un campo y lo que borra |
| `errorContainer` | Coral 950 | Fondo del botón de borrar |
| `errorPressed` | Coral 900 | Botón de borrar pulsado |
| `errorOutline` | Coral 400 al 22 % | Contorno del botón de borrar |
| `handle` | Asa | Asa de las hojas y contorno del interruptor desactivado |
| `ink` | Tinta | Ojos y 8 de la bola, ✓ sobre una muestra de color. Antes `eye` |
| `watchBg` | Negro | Fondo del reloj |

- Un color con alfa se compone sobre el fondo real; en Compose, `Color.White.copy(alpha = 0.08f)`.
- Toda combinación de texto y fondo da 4,5:1 o más. La sección 8 da cada par medido.
- El botón «Continuar con Google» lleva los colores de la guía de Google (`#131314`, `#8E918F` y
  `#E3E3E3`). No son roles y no se usan en nada más.

### Tono

No hay sombras: la altura se cuenta con el tono. Cuanto más arriba, más claro.

| Nivel | Rol | Qué va en él |
|---|---|---|
| 0 | `bg` | La pantalla y la barra superior |
| 1 | `surface` | Grupos, hojas y diálogos |
| 2 | `control` | Controles; grupos dentro de una hoja o un diálogo |
| 3 | `surfaceOverlay` | Lo pulsado del nivel 2; aviso breve; etiqueta emergente |
| 4 | `surfaceHighest` | Lo pulsado del nivel 3 |

- Un control va un nivel por encima de su contenedor y nunca por debajo del 2: sobre el fondo, un
  grupo, una hoja o un diálogo es `control`; dentro de un grupo de una hoja, `surfaceOverlay`.
- Pulsar sube un nivel. Las superficies se aclaran al pulsar; no se oscurecen.
- Una fila, un botón de texto y un icono de barra son controles transparentes: en reposo no enseñan
  su nivel y, pulsados, toman el siguiente. En la pantalla y sus grupos, `surfaceOverlay`; en un
  grupo de una hoja, `surfaceHighest`.
- Las sombras que dibuja el lienzo (diálogo 0 20 40 negro al 50 %, aviso breve 0 8 24 negro al 45 %)
  no se implementan.

**Capas**, de abajo arriba: contenido 0, barra superior 1, aviso breve 2, velo 3, hoja o diálogo 4.

### Colores de cada personaje: `OracleColors`

Cada personaje recibe sus roles a partir de su luz (sección 3), con una fórmula. Nada de esta tabla
cambia un color de la sección 3.

| Rol | Fórmula | Uso |
|---|---|---|
| `light` | La luz de la sección 3 | Origen de los demás |
| `label` | La luz, si da 4,5:1 sobre `bg` y `surface`; si no, se sube su L* en pasos de 0,5 hasta darlo | «▲ Vidente añade · IA» |
| `ring` | La luz | Anillo de 2 de los avatares |
| `aura` | La luz al 24 %, que baja a 0 en el borde | Aura (sección 1) |
| `rim` | La luz al 70 % | Borde de luz (sección 1) |
| `tintSurface` | La luz al 14 % sobre `surface` | Fondo teñido, como la vista previa del editor |
| `tintOutline` | La luz al 24 % | Contorno de 1 del fondo teñido |
| `triangleTop`, `triangleBottom`, `triangleText` | Los de la sección 3 | Triángulo de la ventana |
| `shareBackground`, `shareInk` | Pendiente de decidir (abajo) | Tarjeta de compartir |

- Hoy los ocho dan 4,5:1 con su luz, así que `label` es la luz. El más justo es el Pirata: 5,25:1
  sobre `bg` y 4,76:1 sobre `surface`.
- La etiqueta en color solo va sobre `bg` o `surface`. Sobre `control`, `selected`, el tinte o en el
  reloj, el nombre va en `text` y el color lo ponen el ▲ y el anillo: allí el Pirata daría 4,2:1.
- Un test unitario calcula estos roles para cada personaje y comprueba el 4,5:1.

**Pendiente de decidir** (va en una propuesta aparte para el dueño):
- Los cuatro colores de la sección 3 que no llegan a ΔE00 18 frente a la interfaz. Opciones ya
  medidas:
  - Genio: (a) se queda `#F2B84B`, a 4 del ámbar; (b) `#E8D27A`, a 14,4 del ámbar, que no llega a 18
    y queda a 16 del Robot; (c) `#ECDC51`, a 18 del ámbar pero a 12 del Robot.
  - Vidente `#CC60FB` (18,1), Astróloga `#F692CE` (18,2) o `#F28FD0` (19,2) y Filósofo `#DFBFAA`
    (18,2). Todos dan 6,2:1 o más sobre `bg`.
- El fondo y la tinta de la tarjeta de compartir: (a) `#E4DBFF` fijo, que solo sirve a la Vidente
  (la etiqueta en el color del personaje da 1,72:1 con el Genio, 1,83:1 con el Mago y 1,48:1 con el
  Robot); (b) la luz al 18 % sobre `#FBF9F7`, con la etiqueta en `ink`.

### Letra

La escala es cerrada: un estilo que no está en la tabla no existe. Dentro de un estilo solo cambia
el color.

| Token | Letra | Tamaño / interlineado | Uso |
|---|---|---|---|
| `screenTitle` | Outfit 700 | 28/34 | Título de pantalla; título de Premium y de la ficha |
| `sheetTitle` | Outfit 700 | 21/28 | Título de hojas, diálogos y estado vacío |
| `verdict` | Outfit 600 | 20/26 | El veredicto bajo la bola, «Sí, sin duda.» |
| `barTitle` | Outfit 600 | 18/24 | Título pequeño de la barra plegada |
| `button` | Outfit 600 | 16/20 | Botones y nombre del selector |
| `chip` | Outfit 600 | 15/20 | Chips; cabeceras de lista del reloj |
| `transcript` | Inter 400 | 18/26 | Lo que la bola va oyendo |
| `body` | Inter 400 | 16/23 | Ayuda de la bola, texto de los campos, ventajas de Premium |
| `bodySmall` | Inter 400 | 15/22 | Cuerpo de diálogos y del estado vacío; la pregunta, en el título |
| `supporting` | Inter 400 | 14/20 | Segunda línea de las filas; cuerpo de los avisos |
| `caption` | Inter 400 | 12/16 | Ayuda y contador de los campos, notas y pies |
| `emphasisLarge` | Inter 500 | 18/26 | «No lo veo claro. Pregúntame otra vez.» |
| `emphasis` | Inter 500 | 15/20 | Aviso breve y aviso compacto; opciones del reloj |
| `emphasisSmall` | Inter 500 | 13/18 | Racha y etiqueta emergente |
| `rowTitle` | Inter 600 | 16/22 | Título de las filas |
| `noticeTitle` | Inter 600 | 15/21 | Título de los avisos y de grupos de texto como «Qué se envía a Gemini, de Google» |
| `sectionLabel` | Inter 600 | 13/18 | Etiqueta de campo, cabecera de grupo, «Te escucho…» |
| `tag` | Inter 600 | 12/16 | Etiquetas, «▲ Vidente añade · IA» y «Responde como», que pasa de 11 a 12 |
| `oracleLarge` | Fraunces 500 | 24/30 | La frase bajo la bola |
| `oracleMedium` | Fraunces 500 | 20/26 | Respuesta larga, ficha, tarjeta de compartir y vista previa del editor |
| `oracleSmall` | Fraunces 500 | 16/22 | Recientes |
| `oracleXSmall` | Fraunces 500 | 15/20 | Muestras en Personajes |
| `shareDisplay` | Outfit 700 | 44/46, espaciado −0,5 | Solo la respuesta de la tarjeta de compartir |

- Todos los estilos llevan `letterSpacing = 0.sp`, salvo `shareDisplay`, y
  `lineHeightStyle = LineHeightStyle(Alignment.Center, Trim.None)`.
- **Trampa de Material 3:** el `Text` de M3 hereda `LocalTextStyle`, que por defecto es `bodyLarge`,
  con 0,5 sp de espaciado; `bodyMedium` lleva 0,25. Los componentes Luz pasan siempre su `TextStyle`
  entero y nunca leen `MaterialTheme.typography`.
- **Archivos**, en `composeResources/font`, con solo los caracteres latinos:
  - Inter 400, 500 y 600; Outfit 600 y 700.
  - Fraunces 500 en un corte estático con sus ejes fijados (wght 500, SOFT 30, opsz 24, WONK 0),
    hecho con `fonttools varLib.instancer`. Así se ve como en el lienzo, que usa
    `'SOFT' 30, 'opsz' 24`.
  - Google Sans Medium, solo para el botón de Google.
- Las líneas de títulos y frases se equilibran con `lineBreak = LineBreak.Heading`.
- El texto del triángulo (Inter Bold de 9 unidades) se dibuja en la bola y sigue la sección 1, no
  esta escala.
- La tarjeta de compartir se dibuja aparte (sección 9). Además de `shareDisplay` y `oracleMedium`,
  lleva dos estilos suyos, fuera de esta escala: la pregunta en Inter 400 15/20 y «▲ Vidente» en
  Inter 600 14/18 (sección 7).

### Espacio

Escala: 2, 4, 6, 8, 12, 16, 20, 24, 32 y 40 (`LuzSpacing.s2` … `s40`). No hay otros valores. Donde
el lienzo usa 3, 10, 14 o 66, mandan esta escala y la medida que da cada componente en la sección 9.

| Constante | Valor |
|---|---|
| Margen lateral de la pantalla | 16 |
| Título de pantalla, cabeceras de grupo y título de las hojas | Texto a 20 del borde de la pantalla o de la hoja; en los diálogos, a 24 del borde del diálogo |
| Relleno de una fila | 12 arriba y abajo, 16 a los lados |
| Separación entre las piezas de una fila | 16 |
| Texto de una fila con icono | A 56 del borde del grupo y a 72 del de la pantalla, como en los avisos |
| Texto de una fila con avatar de 40 | A 72 del borde del grupo y a 88 del de la pantalla |
| Entre dos grupos | 12; con cabecera, 20 hasta la cabecera y 8 hasta el grupo |
| Diálogo | A 24 de los bordes de la pantalla |
| Acciones de la bola | 20 por encima de la barra de navegación; 16 en pantallas bajas (sección 8) |
| Hojas | 16 a los lados; abajo, 16 por encima de la barra de navegación |

### Forma

| Radio | Dónde |
|---|---|
| 12 | Chips y etiqueta emergente |
| 16 | Botones, botones de icono, campos, aviso breve, aviso compacto y losas de accesorio |
| 24 | Grupos, avisos que piden atención, salvo el compacto, y losa del estado vacío |
| 28 | Diálogos |
| 32 | Hojas, solo en las esquinas de arriba |
| Completo | Interruptor, etiquetas, avatares, muestras de color, puntos de página, asa y la bola |

- El anillo de foco lleva el radio del control más 4: 16 → 20, 12 → 16. Un círculo sigue siendo un
  círculo.
- Las filas no tienen radio propio: el grupo recorta las esquinas de la primera y la última.

### Tamaños

| Pieza | Medida |
|---|---|
| Botones con relleno | 52 de alto |
| Botones de texto y de icono | 48; los de icono de la fila de la respuesta, 52 |
| Campo | 56 |
| Chip | 36, con objetivo táctil de 48 |
| Etiqueta | 24 |
| Objetivo táctil | 48 × 48 como mínimo |
| Filas | 56 con una línea; 66 con dos, lo que da su contenido (12 + 22 + 20 + 12); 72 con avatar de 40 |
| Barra superior | 56 |
| Título de pantalla | 64 |
| Asa de la hoja | 36 × 4, en una zona de 48 × 24 |
| Bola | 250 en el móvil, que baja a 160 si no cabe (sección 8); 112 en el reloj, 104 sin respuesta y 88 sin el móvil (sección 7) |
| Muestra de color | 40, con objetivo de 48 |
| Losa de accesorio | 52 |
| Avatares | 28 (reloj), 32 (selector, botón de avatar y personas del modo fiesta), 36 (losas), 40 (Recientes, filas de personaje y fila de la cuenta en Opciones), 64 (cabecera de Premium e inicial de la cuenta) y 88 (ficha) |
| Indicador circular | 18 dentro de un botón o al final de una fila; 24 suelto o en el aviso compacto |
| Punto de página | 6 |

Todas las alturas son mínimas (`heightIn(min = …)`): crecen con la letra (sección 8).

### Iconos

Una sola familia, Lucide, de trazo. Tamaños: 14, 16, 18, 22 y 24. El trazo mide 2 a cualquier tamaño
(lo que Lucide llama `absoluteStrokeWidth`): el `ImageVector`, dibujado sobre 24, lleva un trazo de
2 × 24 / tamaño. La única excepción es el icono de 32 de la losa del estado vacío. El logotipo «G»
de Google no es de Lucide: mide 20 y no se cambia. La › del final de las filas, que el lienzo dibuja
a 20, pasa a 18, como la ↗.

| Tamaño | Dónde |
|---|---|
| 14 | Insignias: ✓ en la esquina de una losa, icono de una etiqueta, chispa de la racha |
| 16 | En línea con el texto: ✓ del chip, icono y ✕ del chip de nombre, flecha del selector, bandera de «Respuesta reportada» |
| 18 | Botones con icono, › y ↗ al final de las filas, ✓ de la muestra de color y del reloj |
| 22 | Barra superior, botones de icono, inicio de las filas y ✓ de las filas |
| 24 | Icono de los avisos que piden atención |

| Uso | Icono |
|---|---|
| Recientes · Opciones | `history` · `sliders-horizontal` |
| Atrás, Volver a la bola · abre una pantalla · selector | `chevron-left` o `chevron-right` · `chevron-right` · `chevron-down` |
| Cerrar, Cancelar, Quitar · Añadir, Crear un personaje | `x` · `plus` |
| Leer en voz alta · Detener la lectura | `volume-2` · `square` |
| Compartir, Compartir la app · Incluir la pregunta · Buena y mala respuesta · Respuesta reportada, Reportar personaje | `share-2` · `message-circle` · `thumbs-up`, `thumbs-down` · `flag` |
| Borrar respuesta, Eliminar cuenta | `trash-2` |
| Premium · Respuestas con IA · Respuestas más largas · racha | `crown` · `sparkles` · `text-align-start` · `sparkle` |
| Voz · aviso del micrófono | `mic` · `mic-off` |
| Modo fiesta · «¿Quién del grupo…?» · Elegir otra vez | `party-popper` · `users` · `repeat` |
| Notificaciones · aviso de notificaciones · límite de IA · error · aviso del pago | `bell` · `bell-off` · `clock` · `circle-alert` · `credit-card` |
| Idioma · Privacidad de anuncios · Política de privacidad · Términos de uso | `globe` · `shield` · `lock` · `file-text` |
| Licencias · Contacto · sale de la app | `info` · `mail` · `external-link` |
| Tu cuenta, Tu perfil · Cerrar sesión | `user` · `log-out` |
| Más opciones · Editar · Bloquear a @… | `ellipsis-vertical` · `pencil` · `ban` |
| Sin anuncios · Tus propios personajes · El tono que describas | `megaphone-off` · `palette` · `quote` |
| Lo elegido | `check` |

### Opacidades

Las de la interfaz son estas; las de la bola están en la sección 1. 0,08 (líneas), 0,14 (brillo del
botón principal y tinte), 0,22 (contornos ámbar y coral), 0,24 (aura y contorno del tinte), 0,30
(pista del indicador), 0,40 (contorno de lo elegido y selección de texto), 0,70 (borde de luz) y
0,72 (velo). Lo desactivado no usa opacidad: cambia de color.

### Movimiento: `LuzMotion`

| Duración | ms | Uso |
|---|---|---|
| `short1` | 90 | Entrada del pulsado |
| `short2` | 150 | Salida del pulsado, interruptor, chips, barra al desplazar, puntos de página y salida de diálogo y aviso breve |
| `medium1` | 200 | Entrada de diálogo y aviso breve; fundidos de «Reducir movimiento» |
| `medium2` | 250 | Salida de la hoja y fila que se borra |
| `long1` | 350 | Entrada de la hoja |

| Curva | Valores | Uso |
|---|---|---|
| `standard` | `CubicBezierEasing(0.2f, 0f, 0f, 1f)` | Lo que cambia sin entrar ni salir: pulsado, interruptor, chips, fila borrada, barra, puntos |
| `emphasizedDecelerate` | `CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)` | Lo que entra: hoja, diálogo y aviso breve; la bola al ir a su sitio (sección 6) |
| `emphasizedAccelerate` | `CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)` | Lo que sale: hoja, diálogo y aviso breve |
| `smoothstep` | u² (3 − 2u), igual que `CubicBezierEasing(1/3, 0, 2/3, 1)` | Ida y vuelta: flotar y latir de la bola, pulso de las filas esqueleto |
| `easeOutCubic` | 1 − (1 − u)³, igual que `CubicBezierEasing(1/3, 1, 2/3, 1)` | Lo que llega y se frena en la bola (sección 6) |

`smoothstep` y `easeOutCubic` son las curvas que usa el lienzo para la bola; en código se escriben
como `Easing { u -> … }`, con su fórmula exacta.

| Pieza | Entra | Sale |
|---|---|---|
| Pulsado | 90 `standard`: tono siguiente y escala 0,98 | 150 `standard` |
| Hoja y su velo | 350 `emphasizedDecelerate`: sube desde abajo | 250 `emphasizedAccelerate` |
| Diálogo y su velo | 200 `emphasizedDecelerate`: fundido y escala de 0,96 a 1 | 150 `emphasizedAccelerate`: fundido |
| Aviso breve | 200 `emphasizedDecelerate`: fundido y 8 hacia arriba | 150 `emphasizedAccelerate`: fundido |
| Interruptor | 150 `standard`: el círculo se desliza y cambia de tamaño; los colores se cruzan | Igual |
| Chip elegido | 150 `standard`: entra la ✓ y cambia el ancho | Igual |
| Fila borrada | — | 250 `standard`: se funde y se pliega su alto |
| Barra superior al desplazar | 150 `standard`: fondo opaco, línea y título pequeño, con fundido | Igual |
| Puntos de página | 150 `standard`: cruce de color | — |
| Indicador circular | Una vuelta por segundo, lineal | — |
| Filas esqueleto | Opacidad de 0,6 a 1 en 600 y de vuelta en 600, `smoothstep` | — |
| Anillo de foco | Sin animación | — |
| Toda la bola | Sección 6 | — |

Tokens propios de la bola, que usa la sección 6: el halo sube en 50 y baja en 200; el parpadeo
cierra en 50 y abre en 70; el cambio entre dos gestos de ojos cerrados dura 300; los fundidos de
«Reducir movimiento», 200.

**Con «Reducir movimiento»** (cómo se detecta, en la sección 8) no hay escala, ni desplazamiento, ni
giro: solo fundidos.

| Pieza | Con «Reducir movimiento» |
|---|---|
| Pulsado | Solo el tono, sin escala |
| Hoja | Fundido de 200 al entrar y de 150 al salir, sin subir |
| Diálogo | Fundido de 200, sin escala |
| Aviso breve | Fundido, sin desplazamiento |
| Interruptor | El círculo cambia de sitio sin deslizarse; los colores, con fundido de 150 |
| Chip | La ✓ y el ancho cambian de golpe |
| Fila borrada | Fundido de 150; las de debajo suben sin animación |
| Filas esqueleto | Quietas, al 100 % |
| Paginador | Los botones de la barra saltan de página (`scrollToPage`) en lugar de deslizar; el deslizamiento con el dedo sigue igual |
| Indicador circular | Sigue girando: es lo que dice que algo está pasando |
| La bola | Sección 6 |

### Háptica: `LuzHaptics`

Es una interfaz común que implementa cada plataforma (ADR 0002). En Android usa
`View.performHapticFeedback`, que ya respeta la respuesta táctil del sistema.

| Momento | Android | Antes de la API que pide |
|---|---|---|
| Tocar la bola o despertarla | `CLOCK_TICK` | — |
| Empezar a escuchar | `LONG_PRESS` | — |
| Llega el triángulo; cae el accesorio nuevo; cada caída de la celebración | `CONFIRM` (API 30) | `VIRTUAL_KEY` |
| Error de un campo al guardar | `REJECT` (API 30) | `VIRTUAL_KEY` |
| Encender o apagar un interruptor | `TOGGLE_ON` / `TOGGLE_OFF` (API 34) | `VIRTUAL_KEY` |
| Cada sacudida al agitar | Composición de `PRIMITIVE_TICK` (API 30) | `CLOCK_TICK` |

- La composición del agitado usa `Vibrator` con `USAGE_TOUCH`, para que el sistema la silencie
  cuando la respuesta táctil está apagada.
- Nada más vibra: ni botones, ni filas, ni chips.

### Ventana

| Ancho de la ventana | Qué cambia |
|---|---|
| Menos de 600 | Las pantallas tal como están dibujadas |
| 600 o más | La columna de contenido se centra con 560 como máximo; los diálogos miden 400 como máximo y las hojas 640; la bola sigue en 250 |

- La clase se lee con `currentWindowAdaptiveInfo().windowSizeClass`.
- Con `targetSdk` 36, Android 16 no respeta el bloqueo de orientación en pantallas de 600 o más. El
  diseño en horizontal está pendiente (sección 7).

### Si se envuelve un componente de Material 3

Los componentes Luz no leen `MaterialTheme`. Si uno envuelve un componente de M3 (`Switch`,
`ModalBottomSheet`, `SnackbarHost`, `TooltipBox`), lo pone dentro de `LuzMaterialTheme`:

- Sin color dinámico: nunca `dynamicDarkColorScheme`.
- Sin ripple: `LocalRippleConfiguration provides null` y `LocalIndication provides LuzIndication`,
  un `IndicationNodeFactory` que dibuja el pulsado y el foco de Luz.
- Con `typography` hecha de estilos Luz, todos con espaciado 0: `bodyLarge` → `body`, `bodyMedium` →
  `supporting`, `bodySmall` → `caption`, `labelLarge` → `button`, `titleLarge` → `sheetTitle`,
  `titleMedium` → `rowTitle`.
- Con este `ColorScheme`. Es solo una red de seguridad: cada envoltorio pasa además sus colores
  explícitos (`SwitchDefaults.colors(…)`, `containerColor`, `scrimColor`).

| M3 | Luz |
|---|---|
| `primary` / `onPrimary` | `accent` / `onAccent` |
| `primaryContainer` / `onPrimaryContainer` | `selected` / `text` |
| `secondaryContainer` / `onSecondaryContainer` | `tonal` / `onTonal` |
| `background`, `surface`, `surfaceContainerLowest` | `bg` |
| `surfaceContainerLow`, `surfaceContainer` | `surface` |
| `surfaceContainerHigh`, `surfaceVariant` | `control` |
| `surfaceContainerHighest` | `surfaceOverlay` |
| `onBackground`, `onSurface` / `onSurfaceVariant` | `text` / `textSecondary` |
| `outline` / `outlineVariant` | `outline` / `outlineVariant` |
| `inverseSurface` / `inverseOnSurface` / `inversePrimary` | `surfaceOverlay` / `text` / `link`, que usa el aviso breve de M3 |
| `error` / `errorContainer` / `onErrorContainer` | `error` / `errorContainer` / `error` |
| `scrim` | Noche 990; el 72 % lo pone `scrimColor` |

## 3. Oráculos

| Oráculo (es / en) | Luz | Triángulo (texto) | Accesorio | Mirada | Carácter | Frase de ejemplo |
|---|---|---|---|---|---|---|
| Vidente / Psychic | `#9B7BFF` | `#8E73F0` → `#5B3FD1` (blanco) | Turbante `#6E4FD8` con pliegues `#8E73F0` y gema `#FFD166` | Ojos cerrados con calma | Misteriosa y segura | «Las estrellas dicen que sí.» |
| Genio / Genie | `#F2B84B` | `#FFD27A` → `#E0A030` (`#2A1A00`) | Moño y banda dorados `#F2B84B`, joya `#FF8FA3` y humo bajo la bola | Ojos felices | Generoso y teatral | «Concedido: es un sí.» |
| Bruja / Witch | `#5BC8A0` | `#23A374` → `#0F6447` (blanco) | Sombrero puntiagudo `#1E7A58`, ala `#1A6A4C` y cinta `#A8F0D4` | Borde de arriba inclinado, de pícara | Pícara y directa | «Ni lo sueñes, cariño.» |
| Mago / Wizard | `#34D1DB` | `#7DE6EC` → `#2FB7C2` (`#032A2E`) | Sombrero `#1F8C99`, ala `#18737E` y estrellas `#FFD166` | Cápsulas atentas | Sabio y amable | «Mis cálculos dicen que sí.» |
| Filósofo / Philosopher | `#C4B59F` | `#9A8B74` → `#4F4436` (blanco) | Gafas redondas negras | Medio cerrados | Seco y cansado | «Define “sí”.» |
| Astróloga / Astrologer | `#FF8FA3` | `#E0607E` → `#A82C4C` (blanco) | Luna creciente `#FFD1DB` y estrellas `#FFD166` | Mirando arriba | Soñadora | «Con Venus a favor, sí.» |
| Robot / Robot | `#C6F03C` | `#DDF58A` → `#9CC827` (`#1B2A06`) | Antena `#8B9AAD` con bola `#C6F03C` sobre una arandela `#5F6E80`, y una junta remachada | Pantallas de 10 × 7 | Literal y exacto | «Afirmativo. Bip.» |
| Pirata / Pirate | `#EC4A2A` | `#E8584A` → `#A12A21` (blanco) | Pañuelo `#B8262A` de borde inclinado, con lunares `#F4EDE4` y nudo al lado, y un parche negro con cinta | Un ojo tapado y el otro de pícaro | Gruñón y teatral | «Ni hablar, grumete.» |

Nombres que se evitan: sibila y augur, porque casi nadie los conoce; «gitana», por estereotipo;
médium y chamán.

Los colores de los oráculos no pueden coincidir con los de la interfaz. Se comprueba con una
distancia de color ΔE00 de 18 o más frente al cobalto, el ámbar, el coral y los grises de texto.
Por eso el Mago es turquesa y el Filósofo, piedra. Hoy no lo cumplen el Genio (4 frente al
ámbar), la Astróloga (7 frente al coral), la Vidente (12 frente al azul del ✓ y 15 frente al
cobalto) y el Filósofo (17 frente al gris de los iconos); está pendiente de decidir. Robot y Pirata
sí: 31 y 18,5.

Robot y Pirata ya son estilos de respuesta de la app; son los primeros que reciben su aspecto.

Pendiente de decidir, en el tablero «Propuestas para decidir»:

- **Colores:** Vidente `#BC4BFF`, Genio `#F5CB45`, Mago `#45D3F2`, Filósofo `#B0A08A` y Astróloga `#FF7EC8`, con el
  ámbar de los avisos en `#FF9F43`. Así todos quedan a ΔE00 18 o más de la interfaz y a 19,6 o más entre ellos.
- **Siluetas y miradas que se lean a 48 px:**
  - el moño dorado del Genio en lugar de la gota, y sin humo;
  - la calma de la Vidente, más curva;
  - la Bruja mirando de reojo;
  - las gafas de pasta del Filósofo;
  - la luna pegada a la Astróloga, que la mira;
  - el Mago sin ala y la Bruja con ala ancha.

## 4. Catálogo de expresiones

Todas las expresiones se dibujan solo con los ojos. Cada ojo mide 7,6 de ancho y su centro está en
x = 87 y x = 113.

| Expresión | Forma |
|---|---|
| Reposo | Dos cápsulas de 7,6 × 16 |
| Escuchando | Cápsulas más altas, de 8,6 × 21 |
| Pensando | Cápsulas desplazadas hacia arriba y a la derecha: y = 86, x + 4,8 |
| Agitando | `>` y `<`, con trazo de 4 y extremos redondos |
| Sí | Arcos felices, con trazo de 4,2 y extremos redondos: `M81 107 Q87 96 93 107` |
| Quizá | Un ojo medio cerrado (mitad inferior, borde de arriba plano) y el otro abierto |
| No | Cápsulas caídas: el borde de arriba se inclina hacia fuera y los ojos bajan |
| Cerrados con calma | Arcos hacia abajo, trazo de 3,8: `M82 103 Q87 109 92 103` |
| Medio cerrados | Mitad inferior de la cápsula, con el borde de arriba plano en y = 104 |
| Pícara | Borde de arriba inclinado hacia el centro |
| Parpadeo | Cápsulas aplastadas al 12 % de su alto durante unos 100 ms |
| Pantallas (Robot) | Rectángulos de 10 × 7 con esquinas de 2; su «sí» son dos ángulos rellenos de 11 × 8,5 |
| Parche (Pirata) | Tapa el ojo izquierdo; el derecho lleva la mirada de pícaro |

| Sin respuesta | Dos puntos de 5,5 × 5,5: no es la mirada de ningún veredicto |
| Pantallas bajas (Robot) | Barras de 10 × 3; bajadas 3 para decir que no |

Cada personaje tiene su versión de cada mirada. Está dibujada en el tablero «Miradas de cada personaje»:

| Personaje | Reposo | Al agitarse | Sí | No | Quizá |
|---|---|---|---|---|---|
| Vidente | Cerrados con calma | `> <` | Felices | Caídos | Quizá |
| Genio | Felices | `> <` | Felices | Caídos | Quizá |
| Bruja | Pícara | `> <` | Felices | Pícara | Quizá |
| Mago | Abiertos | `> <` | Felices | Caídos | Quizá |
| Filósofo | Medio cerrados | `> <` | Felices | Caídos | Quizá |
| Astróloga | Mirando arriba | `> <` | Felices | Caídos | Quizá |
| Robot | Pantallas | Pantallas bajas | Ángulos | Pantallas bajas y caídas | Una pantalla y una baja |
| Pirata | Pícaro, con un ojo | Cerrado con calma | Feliz | Pícaro | Medio cerrado |

- **Robot:** nunca pasa a ojos humanos.
- **Pirata:** lo dice todo con el ojo que no tapa el parche; su `<` al agitarse se leería como un guiño.
- **Mientras gira, todos cierran los ojos con calma:** el Robot apaga sus pantallas.

## 5. La ventana de respuesta

- La bola gira sobre su eje vertical, así que el accesorio sigue arriba y la ventana queda a la
  vista. El adorno del accesorio (la gema de la Vidente, la joya del Genio…) se repite por detrás,
  así que con la ventana delante se ve igual que con la cara.
- En la ventana van de una a tres palabras, en mayúsculas y en dos líneas como máximo: «SÍ, SIN
  DUDA», «CONCEDIDO», «NI LO SUEÑES».
- La frase con más personalidad va debajo de la bola, en el texto de la pantalla, después del veredicto
  escrito (§7).
- El triángulo es opaco. Sube tapado por una niebla de `#0E1550` al 85 % que se aclara en 450 ms, así que nunca
  se ve un triángulo traslúcido. Hacia los 450 ms toca el cristal (sube 1,2 de más) y se asienta en 150 ms; ahí
  vibra. Al hundirse, la niebla vuelve.
- Mientras se lee, todo se calma: la bola flota ±1,5 y el triángulo ±0,4, sin girar.
- El triángulo toma el color del oráculo. Su texto cumple 4,5:1 sobre ese color; en el Genio, el Mago
  y el Robot por eso es oscuro.
- Cada línea cabe dentro del triángulo con 2,5 de margen a cada lado, medido en lo alto de sus
  mayúsculas, donde el triángulo mide de medio ancho 37 · (base − 0,73 · tamaño − 68) / 64. Si no
  cabe, el tamaño baja por pasos hasta 7,6.

## 6. Animación de la respuesta

Ciclo de referencia del lienzo, de 7,5 s:

| Fase | Tiempo | Qué pasa |
|---|---|---|
| Reposo | 0–0,9 s | Flota ±3 unidades, tres ondas por ciclo, con la mirada del oráculo. Si la mirada tiene los ojos abiertos, parpadea a 0,45 s |
| Escuchando | 0,9–1,7 s | Los ojos se abren en un parpadeo y pasan a 8,6 × 21, la bola se inclina de −5° a −12° y el halo late suave: un 18 % más, a 2,4 Hz |
| Se agita | 1,7–2,3 s | 3,5 sacudidas que se apagan. El cuerpo va ±5 unidades y se mece ±4°; la cara le sigue 25 ms después, deslizándose ±3,9° sobre la bola, y los ojos, con su mirada de agitarse (§4), un poco más tarde. Las puntas de los sombreros, la antena y el moño se balancean |
| Gira | 2,3–3,3 s | Cierra los ojos con calma. Media vuelta sobre su eje vertical: 5° hacia el otro lado en 0,12 s y un muelle (ζ 0,8, rigidez 90) que pasa por 180° a los 0,44 s, se pasa 2,7° y se asienta a los 0,86 s sin pararse. La bola sube 1,8 mientras gira |
| La respuesta | 3,1–5,2 s | El triángulo sube 26 unidades en 0,6 s tapado por una niebla que se aclara en 450 ms, y toca el cristal. El halo se duplica y la bola flota ±1,5. Antes de volver se hunde en 0,26 s y la niebla vuelve |
| Vuelve | 5,2–6,0 s | La otra media vuelta, en el mismo sentido y con el mismo muelle: la ventana sale por la izquierda y la cara entra por la derecha con su mirada de reposo |
| Gesto | 6,0–7,0 s | Te mira y parpadea a la mirada del veredicto (§4) mientras anticipa. Sí: se agacha 3 y se aplasta un 4 % en 110 ms, salta 10 estirándose un 2 %, se aplasta un 3 % al caer y rebota 1,5; el Genio salta 13 y se queda arriba 0,08 s, y el Robot cabecea dos veces. No: niega dos veces, 0 → +12° → −12° → +6° → 0 en 0,8 s. Quizá: sube 5 en 140 ms, se queda 220 ms y baja en 300 ms, ladeada 9° y mirando a un lado. El Filósofo reacciona 0,15 s tarde y al 70 % |
| Reposo | 7,0–7,5 s | El gesto se relaja en la mirada de reposo |

Cómo se dibuja el giro:

- La bola es una esfera de radio 82 que gira sobre su eje vertical, con la cara delante y la ventana
  detrás. Cada ciclo da una vuelta entera, siempre en el mismo sentido.
- La cara, los ojos y la ventana van pegados a la superficie. Un punto (x, y) del dibujo de frente,
  medido desde el centro del cuerpo, está a la profundidad z = √(82² − x² − y²). Tras girar un
  ángulo ψ queda en x′ = x·cos ψ − z·sen ψ, con profundidad z′ = x·sen ψ + z·cos ψ; la y no cambia.
  La ventana usa ψ − 180°. Para la sacudida y el cabeceo la cara gira además un ángulo pequeño θ
  sobre el eje horizontal.
- Lo que queda detrás (z′ < 0) se pega al contorno: la cara se va por el borde y no se encoge hacia
  el centro.
- Las piezas pequeñas (ojos, burbujas, adorno) van a su punto girado y se estrechan en horizontal
  por z′/z.
- La luz no gira: el degradado del cuerpo, el brillo y la sombra del borde se quedan quietos, y el
  degradado de la cara también. El del líquido viaja con la ventana.
- Lo redondo del accesorio no cambia al girar. Lo que no es redondo (la punta torcida de un sombrero,
  el borde inclinado del pañuelo) se mezcla con su imagen en espejo según cos ψ, y lo que sobresale
  (el nudo del Pirata) da la vuelta alrededor del eje y pasa por detrás de la bola.
- Los adornos se repiten por detrás y se recortan con la silueta del accesorio: en cada media vuelta
  uno se va por la izquierda y el otro entra por la derecha.
- El dibujo necesita 34 unidades libres por encima de la bola: en el salto, la punta del sombrero del
  Mago sube hasta y = −33.
- Los contornos se calculan una vez; en cada fotograma solo se transforman, sin crear memoria
  nueva.

En la app, las fases las disparan los estados y no un reloj fijo:

| Estado de la app | Animación |
|---|---|
| `Idle` | Reposo |
| `Listening` (mantener pulsado y hablar) | Escuchando. El halo sigue el nivel del micrófono en dB: ignora lo que está por debajo de −50 dBFS y lleva de −50 a −10 dBFS a un 0–18 % más de aura, en escala logarítmica; sube en 90 ms y baja en 300 ms |
| `Thinking` | Se agita y gira. Si la respuesta se pide agitando el móvil, la sacudida sigue al acelerómetro. Si la respuesta no ha llegado, la ventana queda a la vista con el líquido en movimiento y sin triángulo, hasta el máximo actual de 6 s |
| `Answered` | Se divide en dos. `Revelada`: la ventana con el triángulo, al menos 2,5 s y mientras TalkBack la lee; es lo que enseñan el widget y el reloj. `Reaccionada`: la cara con la mirada del veredicto, que se queda hasta que se pide otra respuesta |
| Fallo («No lo veo claro. Pregúntame otra vez.») | El triángulo no aparece; la bola vuelve con mirada de duda |

Interrupciones:

- La bola responde en 100 ms como máximo a cualquier toque.
- Con la cara delante, el agitado sale de la pose en que esté, mezclando en 80 ms.
- Con la ventana delante, pedir otra respuesta hunde el triángulo en 200 ms, agita el líquido 400 ms y sube
  el siguiente, sin dar otra vuelta.
- El giro solo se deshace antes de llegar a 90°; pasado ese punto, termina.
- Si la respuesta llega a mitad del giro, el triángulo espera a que la ventana esté delante.
- Cancelar la escucha: parpadeo al reposo en 120 ms y vuelta a −5° en 250 ms.
- Otra respuesta pedida antes de 20 s usa el ritual corto: 0,4 s de agitado y la cara de cristal.

Reglas:

- Con «Quitar animaciones» la escala de animación de Android es 0 y Compose acabaría cualquier animación en
  el siguiente fotograma. La app lee el ajuste y ofrece además el suyo (§8). En ese modo no hay giro, ni
  agitado, ni saltos: se usa la cara de cristal con fundidos de 200 ms que la app controla.
- El bucle de la bola se para cuando no se ve, cuando la app va en segundo plano y con el ahorro de batería.
- Un gesto pasa a otro dentro de un parpadeo: los ojos se cierran en 50 ms hasta una cápsula de
  7,6 × 1,9, que es igual para todos los gestos, cambian de forma y se abren en 70 ms. Los trazos
  conservan su grosor; no se aplastan con una escala. Entre dos gestos de ojos cerrados, como los
  ojos felices y los cerrados con calma, la forma cambia directamente en 0,3 s.
- Los valores animados se leen solo en la fase de dibujo, sin recomponer la pantalla en cada
  fotograma, y no se crea memoria nueva en cada fotograma.
- Todos los tiempos y curvas son tokens del sistema de diseño, nunca números sueltos en una pantalla.
- La vibración acompaña al agitado (pulsos cortos) y a la llegada del triángulo (un toque).

### En prueba: cara de cristal

En lugar de girar, la bola enseña la respuesta donde tiene la cara: la cara se vuelve de cristal. El
tablero «Prueba: ¿gira o su cara se vuelve ventana?» pone las dos versiones a la vez. Hasta que se
decida, manda la versión que gira. La recomendación de la auditoría es usar las dos: «Gira»
en el móvil, porque en la bola 8 de verdad la ventana está detrás, y «Cara de cristal» como versión rápida, en
el reloj, con «Quitar animaciones», al pedir otra respuesta antes de 20 s y en una respuesta abierta desde
Recientes.

| Fase | Tiempo | Qué pasa |
|---|---|---|
| Cierra los ojos | 2,28–2,58 s | Tras la sacudida, los ojos se cierran tranquilos (las pantallas del Robot se apagan) y la bola toma aire: sube 3,5 y se estira un 2 %. Luego los ojos y lo que lleva la cara, como las gafas o el parche, se desvanecen |
| La cara se aclara | 2,52–2,8 s | Un destello cruza el blanco y la cara se vuelve transparente en 0,22 s sobre la ventana, que toma al final su tamaño y su bisel. El triángulo ya sube detrás desde 2,62 s |
| La respuesta | Hasta 5,25 s | El triángulo flota y, desde 4,8 s, se hunde en 0,45 s con una curva suave |
| Se cierra | 5,0–5,3 s | El blanco vuelve con otro destello y reaparecen los ojos cerrados |
| Abre los ojos | 5,85 s | Se abren con la mirada del gesto, que empieza a los 6,05 s, como en el giro |

Reglas de la prueba:

- La cara nunca tiene un agujero: un círculo oscuro que crece dentro del blanco parece un ojo enorme.
- La cara no cambia de color: roja o verde se lee como herida, enfado o mareo.
- No se dice «se le abre la cara», que en español es una herida.
- El accesorio no se mueve, y el modo «reducir movimiento» queda casi igual que el normal, porque ya
  cambia la cara por la ventana con un fundido.

### El 8 y los ojos

La cara está donde la bola 8 de siempre tiene su 8, y el personaje lo usa para contar en qué estado está:

- **El 8 es la bola como objeto:** dormida o trabajando.
- **Los ojos son la bola despierta:** el personaje.
- **Al abrir la app,** el 8 se vuelve ojos: despierta.
- **Mientras algo carga,** los ojos vuelven a ser el 8, de pie, y una luz lo recorre: trabaja.
- **Tras un rato sin uso,** cierra los ojos y vuelve a ser el 8: descansa. Al tocarla, el 8 se vuelve ojos.
- **Tumbado (∞), el 8 solo se ve de paso:** en la cara, dos aros tumbados parecen unos ojos saltones.

El 8 no es una letra de la fuente. Se dibuja con dos aros de trazo grueso que se funden en la cintura, con el
peso de Outfit 800 a ese tamaño, para poder convertirse en ojos:

| Aro | Centro | Radio | Hueco |
|---|---|---|---|
| Arriba | (0, −10,5) | 11 | 4,2 |
| Abajo | (0, 12) | 12,8 | 5,2 |

- Las medidas son desde el centro del cuerpo y la tinta es `#16171D`, la de los ojos.
- Para tumbarse, el 8 gira un cuarto de vuelta a la izquierda: el aro de arriba acaba en el ojo izquierdo.
- Para volverse ojos, cada aro se estrecha hasta el ojo abierto (7,6 × 16, en x = ±13) y su hueco se cierra en
  el primer 60 % del cambio. Para dormirse, acaba en la cápsula cerrada de 7,6 × 1,9.
- Las formas tienen los mismos comandos en todo el recorrido (el hueco llega a radio 0, no desaparece), así que
  el cambio se puede hacer con `pathData` en un AnimatedVectorDrawable.

### Inicio de la app

En el arranque en frío se ve que el personaje es una bola 8: empieza la bola de siempre y su 8 se vuelve los
ojos. Está en los tableros «Así se abre la app» y «El inicio, paso a paso». El icono del lanzador sigue siendo
el 8 de siempre, así que la historia empieza en él.

| Fase | Tiempo | Qué pasa |
|---|---|---|
| La bola 8 | 0–0,25 s | El splash del sistema enseña la bola sin accesorio, con su 8 y la luz de la marca, `#7C96FF` |
| El 8 se tumba | 0,25–0,5 s | Gira un cuarto de vuelta y queda en ∞. Justo antes, la bola baja 1 |
| Abre los ojos | 0,45–0,65 s | Los aros se estrechan hasta los ojos abiertos; sus huecos se cierran en el primer 35 % |
| Parpadea | 0,72 s | Primer parpadeo y sube 2: está despierta. Aquí acaba el icono animado (0,8 s) |
| Entra en su sitio | 1,0–1,45 s | La bola pasa del centro, al 78 %, a su sitio en la pantalla principal con desaceleración enfática (0,05, 0,7, 0,1, 1). La pantalla entra alrededor: la barra a 1,15 s, la ayuda a 1,23 s y lo de abajo a 1,31 s, cada parte con un fundido de 250 ms y 8 dp de desplazamiento |
| Se viste | 1,05–1,61 s | La luz pasa a la del personaje en 0,5 s. Desde 1,25 s su accesorio cae 18 unidades con aceleración de caída y, al llegar, aplasta la bola un 2,5 % |
| Lista | 1,8 s | Parpadea a su mirada de reposo y empieza a flotar |

En Android 12 o posterior:

- `windowSplashScreenBackground` es `#030B14` de día y de noche. Hoy el día es `#FAF9F9` y da un destello blanco.
- `windowSplashScreenAnimatedIcon` es un AnimatedVectorDrawable en `drawable-v31` de 0,8 s, sin esperar más de
  166 ms para empezar. El icono no lleva aura, o la bola mide 148 dp como máximo para que quepa en el círculo
  de 192 dp.
- El sistema puede quitar el splash en cualquier fotograma, así que los ojos se leen a los 0,65 s y el último
  fotograma, al que salta con «Quitar animaciones», es la cara con ojos.
- `windowSplashScreenBehavior` es `icon_preferred`, para que salga también al abrir desde la notificación.
- `setKeepOnScreenCondition` solo dura lo que tarda la carga real.
- En `setOnExitAnimationListener`, la vista del icono se cambia por la bola de Compose en la misma posición,
  tamaño y fotograma (`iconAnimationStartMillis`), y desde ahí corre la entrada.
- De Android 8 a 11 el icono no se anima: la pantalla principal empieza con el 8 y hace el cambio a ojos en la
  entrada.
- En el arranque en caliente no hay splash: la bola parpadea una vez al volver.
- Sin sonido ni vibración.
- En el reloj el splash es el icono quieto; el cambio del 8 a los ojos va dentro de la app, en 400 ms como
  máximo, y no se hace en modo ambiente ni con «Quitar animaciones».
- Con «Quitar animaciones», la bola aparece ya en su sitio con los ojos, y el accesorio y la pantalla entran
  con un fundido de 200 ms.

### El reloj en modo ambiente

Con targetSdk 36 en Wear OS 6, la app sigue a la vista en modo ambiente. La pantalla queda negra en un 85 % como
mínimo, sin rellenos blancos ni animaciones:

- el cuerpo negro, sin aura ni brillo, con el borde de 1,5 dp al 60 % del color del personaje;
- la cara, un aro de 1,5 dp en `#C9D1DA` sin relleno; los ojos pueden ir rellenos, porque ocupan poco;
- el accesorio, solo en contorno, en la misma posición y sin flotar;
- se actualiza una vez por minuto y vuelve a la versión normal con un fundido de 250 ms.

### Catálogo de animaciones

Todas están en el tablero «Catálogo de animaciones», cada una en bucle de reposo a reposo. Todo lo cuentan el
cuerpo, los ojos y la luz; nada se dibuja alrededor de la bola.

| Animación | Cuándo | Qué hace | Duración | Vibración | Con «reducir movimiento» |
|---|---|---|---|---|---|
| Inicio | Arranque en frío | El 8 se vuelve ojos, la bola entra en su sitio y se viste (ver arriba) | 2,1 s | Ninguna | Fundidos de 200 ms |
| Reposo | Siempre que espera | Flota ±3 en 3,2 s y parpadea cada 3–6 s al azar | En bucle | Ninguna | Quieta; solo parpadea |
| Mira alrededor | Cada 8–12 s en reposo, al azar | Los ojos se mueven 3,5 a un lado en 160 ms, luego al otro, y vuelven. La cabeza los sigue 80 ms después, 2,2° por unidad | 3,6 s | Ninguna | No se hace |
| Toque | Al pulsar la bola, antes de que escuche o responda | Se hunde un 4 % al pulsar, rebota 3 al soltar y abre un poco más los ojos | 0,45 s | Un toque suave al pulsar | Solo el parpadeo |
| Se duerme y despierta | Tras 2 min sin tocarla con la app delante; despierta al tocarla o al agitar el móvil | Cierra los ojos en 0,9 s, respira más despacio (±1,2) y su luz baja al 60 %. Luego sus ojos cerrados se juntan y se levantan en el 8. Al despertar, el 8 se tumba y se vuelve ojos en 0,6 s, se estira un 3 % y parpadea | 2,2 s dormirse; 0,6 s despertar | Un toque suave al despertar | Fundidos de 200 ms entre los ojos y el 8 |
| Sin conexión | Mientras la app avisa de que no hay red | Ojos a media asta mirando abajo, inclinada −8°, flota ±1,2 y su luz al 60 % | En bucle | Ninguna | Quieta en esa postura |
| Escucha | Mientras mantienes pulsado | Abre los ojos a 8,6 × 21, se inclina a −12° y el halo sigue tu voz en dB (ver `Listening`) | Lo que dure | `GESTURE_START`; si no lo hay, pulsación larga | Sin inclinarse; el halo, un 15 % más, fijo |
| Se agita | Al soltar, al tocar o al agitar el móvil | 3,5 sacudidas: el cuerpo, la cara 25 ms después y los ojos `> <` más tarde | 0,6 s | Pulsos cortos | Un parpadeo |
| Gira y responde | Con la respuesta | Media vuelta, sube el triángulo y vuelve la otra media vuelta con el gesto | 2,9 s | Un toque al llegar el triángulo | Fundido de 200 ms entre la cara y la ventana |
| Sí | Veredicto sí | Te mira, parpadea a ojos felices y salta (ver «Gesto») | 0,6 s | `CONFIRM` al caer | Solo los ojos felices y `CONFIRM` |
| No | Veredicto no | Niega dos veces, ±12°, en 0,8 s | 0,8 s | `REJECT` | Solo la mirada y `REJECT` |
| Quizá | Veredicto quizá | Sube 5, se queda 220 ms y baja, ladeada 9° y mirando a un lado | 0,66 s | Un `PRIMITIVE_TICK` | Solo la mirada |
| Sí del Robot | Su veredicto sí | Cabecea dos veces, como un servo: −7° y baja 2,5 | 0,42 s | Ninguna | Solo la mirada |
| Sin respuesta | Si la respuesta falla («No lo veo claro. Pregúntame otra vez.») o tarda más de 6 s | Vuelve sin triángulo, con dos puntos en blanco por ojos; se hunde 3 y su luz baja al 40 % en 400 ms, y vuelve un 20 % más despacio | 1,5 s | Dos `PRIMITIVE_LOW_TICK`, nunca `REJECT` | Los puntos y la luz |
| Esperando | Cuando una carga pasa de 0,6 s: los personajes, la cuenta, una compra, la imagen para compartir | Los ojos se juntan en ∞ y el ∞ se levanta en el 8 (0,6 s); una luz blanca de radio 1,7 lo recorre cada 0,9 s; al acabar vuelve a ojos y parpadea | Lo que dure; como mínimo un recorrido | Ninguna | El 8 sin la luz que corre, con fundidos |
| Cambio de personaje | Al volver a la bola tras elegir otro | El accesorio sube 16 y se desvanece en 0,32 s, la luz pasa al color nuevo en 0,5 s, parpadea a la mirada nueva y cae el accesorio nuevo desde 18 en 0,36 s, aplastando la bola un 2,5 % | 1,2 s | Un toque al caer | Fundido cruzado de 200 ms |
| Celebración | Al abrir la app el día que se cumple una racha de 3, 7 o 30 días | Ojos felices, dos saltos de 6 y 9 con aplastamiento al caer, y su luz late una vez | 1,3 s | Un toque en cada caída | Ojos felices y la luz late, sin saltos |
| Gesto propio (sin tablero) | Cada 20–40 s de reposo, al azar | El Genio mueve el moño, la Bruja mira de reojo, el Filósofo suspira y el Pirata se balancea como en un barco | 0,8–1,5 s | Ninguna | No se hace |
| Leer en voz alta (sin tablero) | Mientras lee la respuesta | El halo sigue la voz, hasta un 12 % más | Lo que dure | Ninguna | El halo, fijo |
| Sorteo del modo fiesta (sin tablero) | Al elegir a quién le toca | Da dos o tres vueltas que frenan en 1,6 s y para en la ventana con el nombre | 1,6 s | Un toque en cada media vuelta y `CONFIRM` al parar | El nombre con un fundido |
| Cara de cristal | Prueba frente a «Gira» | Ver «En prueba: cara de cristal» | 2,9 s | Un toque al llegar el triángulo | Fundido de 200 ms |

Reglas del catálogo:

- **Prioridad:** cuando dos animaciones quieren los ojos a la vez, gana la de más arriba en esta lista: responder
  (escucha, agitado, giro y gesto), toque, cambio de personaje, celebración, esperando, mira alrededor, reposo.
- **Interrupciones:** dormida se interrumpe con cualquier toque. Si se le pide una respuesta mientras duerme,
  primero despierta (0,6 s) y luego responde.
- **Azar:** las de reposo (el parpadeo y mirar alrededor) caen al azar dentro de su ventana, para que no se note
  el bucle.
- **Esperas breves:** «Esperando» solo aparece si la carga pasa de 0,6 s y, una vez empieza, dura al menos un
  recorrido completo, para no parpadear.
- **La espera de la respuesta es otra:** mientras llega la respuesta, se ve la ventana con el líquido en
  movimiento (la tabla de estados de arriba).
- **Vibración:** el toque suave es `CLOCK_TICK`, la pulsación larga `LONG_PRESS`, el toque `CONFIRM` y los pulsos
  cortos una composición de `PRIMITIVE_TICK`. Si el sistema tiene la vibración táctil apagada, no se vibra.

## 7. Pantallas

Todas están en la página «Diseño» del lienzo, en las secciones «Pantallas: la bola», «Recientes y
Opciones», «Personajes y cuenta», «Premium, compartir y sistema» y «Reloj». Los tableros de
componentes van antes, en «Componentes Luz». Cada estado da el nombre de su tablero, que es su
archivo sin `.dc.html`; lo que aún no está dibujado dice «sin tablero».

Los componentes se llaman como en la sección 9, y los colores y la letra, con los tokens de la
sección 2. Si este texto y un tablero no coinciden, manda este texto.

**Barras del sistema.** Todas las pantallas del móvil las llevan dibujadas, como dice la sección 9
(«Borde a borde»), y el contenido va entre las dos. El velo, las hojas y el teclado llegan al borde y
las barras van encima, sin oscurecerse. La notificación y el widget no las llevan: son superficies
del sistema.

### La bola

La bola es la página central de tres que se pasan deslizando. Recientes queda a la izquierda y
Opciones a la derecha.

**Huecos**, de arriba abajo, en un móvil de 390 × 844 con barras de 24. Los altos son mínimos y la
bola es elástica: si no cabe, se encoge (abajo).

| Hueco | Alto | Qué lleva |
|---|---|---|
| Barra de estado | 24 | Del sistema |
| Barra superior | 56 | Iconos de barra |
| Margen superior | El 40 % del espacio libre en reposo: 50 en 844 | Nada; en el modo fiesta, el chip «Modo fiesta» |
| Título | 96, con el contenido abajo | La ayuda o la pregunta, en tres líneas como máximo, con «…» |
| Bola | 250 de ancho como máximo y 292 de alto, a 8 del título: de y = 234 a 526 | La bola con su aura |
| Leyenda | 120 como mínimo, con el contenido arriba | La racha; el veredicto, la etiqueta y la frase; o un aviso |
| Hueco libre | Lo que quede | Nada |
| Acciones | 80, con 20 por encima de la barra de navegación; los botones, de y = 734 a 786 | Botones |
| Barra de navegación | 24 con gestos; 48 con tres botones | Del sistema |

- El margen superior se calcula una vez y no cambia entre estados. Si la leyenda crece, solo ocupa
  el hueco libre, así que la bola no se mueve.
- **Tamaño de la bola.** Con el veredicto bajo la bola, la leyenda no baja de 120 ni en un móvil
  bajo:

  `bola = clamp(160, (alto útil − 368) / 1,168, 250)`

  368 son los huecos al 100 % en un móvil bajo: barra 56, título 96, separación 8, leyenda 120,
  acciones 72 y margen inferior 16. La bola mide 250 con un alto útil de 660 o más. En 360 × 640
  mide 192 con navegación por gestos (alto útil de 592) y 171 con tres botones (568). Con la leyenda
  en 96 mediría 212 en ese móvil con gestos, pero el fundido de la respuesta larga taparía casi toda
  la frase. El resto de la regla, en la sección 8.
- Los avisos breves van 12 por encima de los botones de acción: de y = 666 a 722 en 844.

**Barra superior:**
- En reposo: «Recientes» a la izquierda, los puntos de página en el centro y «Opciones» a la
  derecha.
- Mientras escucha o piensa: solo ✕, «Cancelar».
- Con una respuesta: ✕, «Cerrar», y a la derecha «Leer la respuesta en voz alta». Mientras lee, ese
  botón es «Detener la lectura» y la para.
- Con una respuesta abierta desde Recientes: ‹, «Volver a Recientes», en lugar de ✕.

**Ayuda.** En reposo, el título dice «Toca o agita para una respuesta.» y, debajo, «Mantén
presionado y pregunta en voz alta.», en `body` y `contentMedium`. Tocar o agitar da una respuesta
sin pregunta, así que nunca se llama «preguntar»: preguntar es mantener y hablar. Una vez hecha, la
pregunta se queda en el título, entre «», en `bodySmall` y `textSecondary`.

**Preguntar y responder:**

| Estado | Tablero | Título | Bola | Leyenda | Acciones |
|---|---|---|---|---|---|
| En reposo | `LP01Reposo` | La ayuda | Cara en reposo | La racha: «3 días seguidos» | El selector de oráculo, «Responde como · Vidente» |
| Escuchando, antes de hablar | `LP40Escuchar` | «Te escucho…» en `sectionLabel` y `textSecondary` y, donde irá lo que oye, «Haz una pregunta de sí o no.» en `transcript` y `textSecondary` | Escuchando: ojos de 8,6 × 21, inclinada −12° y con el halo latiendo | «Suelta para preguntar · Desliza fuera para cancelar» en `emphasisSmall` y `textSecondary`; si no cabe, se parte en el «·» | Nada |
| Escuchando | `LP02Escuchando` | «Te escucho…» y, debajo, lo que va oyendo en `transcript` y `text`: sus dos últimas líneas; las anteriores salen por arriba | Escuchando | Como antes de hablar | Nada |
| Pensando | `LP03Pensando` | La pregunta | La ventana con el líquido | Nada; TalkBack dice «Pensando…» | Nada |
| Respuesta | `LP04Respuesta` | La pregunta | La ventana con la respuesta | El veredicto, «Sí, sin duda.», en `verdict` y `text`; a 8, la etiqueta «▲ Vidente añade · IA»; a 8, la frase en `oracleLarge` (sección 9, «Veredicto y frase del oráculo») | «Compartir» y los dos pulgares |
| Respuesta larga, con «Respuestas más largas» | `LP31RespuestaLarga` | La pregunta | La ventana con la respuesta | El veredicto y la etiqueta, fijos. La frase, en `oracleMedium`, se desplaza en el resto de la leyenda hasta las acciones, con un fundido de 24 sobre ellas y otro arriba en cuanto se ha desplazado. El tablero lleva 210 caracteres; la IA llega a 240 | Como en la respuesta, siempre a la vista |
| Respuesta sin pregunta | `LP46SinPregunta` | «Una pregunta en silencio» en `bodySmall` y `textSecondary`, sin «» | Como en la respuesta | Como en la respuesta | Como en la respuesta |
| Volver a preguntar | `LP53OtraVez` | La pregunta | La cara, con su mirada de reposo | Como en la respuesta | Encima, «¿Otra pregunta? Mantén presionada la bola.» en `emphasisSmall` y `textSecondary` |
| Abierta desde Recientes | `LP54DesdeRecientes` | «Hace 5 min» en `sectionLabel` y `textSecondary`, sobre la pregunta | La ventana con la respuesta, quieta | Como en la respuesta | Como en la respuesta, con los pulgares como se dejaron |
| Sin respuesta | `LP05SinRespuesta` | La pregunta | Cara de duda | «No lo veo claro. Pregúntame otra vez.» en `emphasisLarge` y `text` | «Toca o agita para una respuesta», como texto |

- `LP40Escuchar` sale en cada escucha hasta que llega la primera palabra; luego, la transcripción de
  `LP02Escuchando`. Salir 24 del círculo con el dedo cancela.
- La primera vez que se mantiene la bola, Android pide el micrófono con su diálogo, que no se
  dibuja. Con permiso, la siguiente pulsación ya escucha; sin él, `LP41SinMicrofono`.
- `LP46SinPregunta` sale tras tocar o agitar. Recientes pone la misma línea donde va la pregunta.
  Sin el artículo, «Pregunta en silencio» se leería como una orden.
- En `LP53OtraVez`, la línea entra con un fundido de 250 ms al acabar el gesto (a los 7 s del ciclo
  de la sección 6). Mantener la bola pregunta otra vez sin cerrar; tocarla da una respuesta sin
  pregunta. No hay botones nuevos.
- En `LP54DesdeRecientes`, la respuesta entra con un fundido de 200 ms, sin agitar ni girar. ‹ y el
  atrás del sistema, también el predictivo, vuelven a Recientes, a la misma fila, y la bola queda en
  reposo. Si se mantiene la bola, la respuesta nueva es normal, con ✕, que cierra al reposo.
- `LP05SinRespuesta` sale si la respuesta falla o tarda más de 6 s (sección 6).

**Aviso compacto.** El aviso que pide atención de la bola es más bajo que el de la sección 9, que
aún no lo recoge:
- 56 de alto como mínimo, radio 16, `warningContainer` y línea de 1 `warningOutline`.
- El icono de 24 en `warning`, a 16 del borde del aviso. El texto en `emphasis` y `text`, en dos
  líneas como máximo y equilibradas, a 72 del borde de la pantalla, como en el aviso grande.
- La acción, un botón de texto de 48 con la etiqueta en `warning`, a 4 del borde.
- Es una región viva educada (`liveRegion = Polite`). Con la letra grande crece en alto y no se
  corta.
- Va en la leyenda, donde la racha; con una respuesta, donde el aviso breve.

**Voz y micrófono:**

| Estado | Tablero | Título | Bola | Leyenda | Acciones |
|---|---|---|---|---|---|
| Sin micrófono | `LP41SinMicrofono` | «Toca o agita para una respuesta.», sin la línea de la voz | En reposo | El aviso compacto: `mic-off`, «Sin micrófono, la bola no te oye.» y «Permitir» | El selector |
| Micrófono bloqueado | `LP42MicrofonoBloqueado` | Como sin micrófono | En reposo | El mismo aviso, con «Abrir ajustes» | El selector |
| No te he oído | `LP43NoTeOi` | Vacío | Duda: ladeada 8°, un ojo medio cerrado y la luz al 60 % | «No te he oído.» en `emphasisLarge` y `text` y, debajo, «Mantén presionado y habla.» en `bodySmall` y `textSecondary` | El selector |
| Sin reconocimiento de voz | `LP44SinVoz` | «Toca o agita para una respuesta.» | En reposo | `mic-off` de 16 y «Este móvil no puede escuchar preguntas.» en `emphasis` y `textSecondary`. Sin ámbar: no hay nada que hacer | El selector |

- `LP41SinMicrofono` sale tras negar el permiso y se queda en el reposo hasta que lo hay.
  «Permitir» y mantener la bola vuelven a lanzar el diálogo de Android.
- `LP42MicrofonoBloqueado` sale cuando Android ya no enseña el diálogo
  (`shouldShowRequestPermissionRationale` es falso tras negar). «Abrir ajustes» abre la ficha de la
  app en Ajustes. Al volver con permiso, se va el aviso y vuelve la ayuda entera.
- `LP43NoTeOi` sale a los 5 s de mantener sin voz: la escucha se para y soltar después no pregunta.
  Dura hasta el siguiente toque. Soltar antes de los 5 s sin haber hablado cuenta como un toque.
- `LP44SinVoz` sale si el móvil no tiene reconocimiento de voz. La ayuda pierde la línea de la voz
  para siempre. La nota sale en el primer uso y cada vez que se mantiene la bola.

**Sin IA y pregunta delicada:**

| Estado | Tablero | Título | Bola | Leyenda | Acciones |
|---|---|---|---|---|---|
| Límite de IA | `LP47LimiteIA` | La pregunta | Como en la respuesta | El veredicto, «▲ Vidente añade», sin «· IA», y una frase suya de siempre | Encima, el aviso compacto: `clock`, «Ya usaste las de hoy. Vuelven mañana.» y «Ver anuncio». Debajo, «Compartir» y el botón de texto «Hazte Premium», con `crown` |
| Sin conexión | `LP48SinConexion` | La pregunta | Como en la respuesta, con el aura al 60 %, la luz de «Sin conexión» (sección 6) | El veredicto, «▲ Vidente · sin conexión» y una frase suya de siempre. «· sin conexión» va en `textSecondary`, como «· IA» | Solo «Compartir» |
| Pregunta delicada | `LP45Delicada` | La pregunta | Sin giro, sin triángulo y sin gesto: las cápsulas de reposo, quietas y a 0°, con el aura al 50 % | Sin etiqueta: «Esto no lo puede responder un juguete.» en `emphasisLarge` y `text`, y «Habla con alguien de confianza o llama al 112.» en `body` y `contentMedium` | El botón secundario «Dónde pedir ayuda» |

- Sin IA no hay pulgares: valoran y reportan lo que escribe la IA.
- `LP47LimiteIA`: si la frase ocupa dos líneas, pasa a `oracleMedium` para dejar 12 sobre el aviso.
  Si «Compartir» y «Hazte Premium» no caben en una fila, «Hazte Premium» baja. Sin anuncio con
  recompensa (Remote Config), la acción del aviso es «Hazte Premium» y no se repite debajo. Con
  Premium no hay acción: la línea va en `textSecondary`, sin ámbar.
- `LP48SinConexion` no lleva ámbar, porque no hay acción. Con red, la respuesta siguiente vuelve a
  ser con IA, sin avisar.
- `LP45Delicada` lleva en la barra ✕ y «Leer la respuesta en voz alta». No hay «Compartir», ni
  pulgares, ni etiqueta, y no se guarda en Recientes. El 112 cambia según el país. TalkBack lee las
  dos líneas.

**Modo fiesta:**

| Estado | Tablero | Título | Bola | Leyenda | Acciones |
|---|---|---|---|---|---|
| Turno de Ana | `LP77FiestaTurno`, que sustituye a `LP06Fiesta` | «Turno de Ana» en `screenTitle` y, debajo, la ayuda en `bodySmall` y `textSecondary` | Cara en reposo | La racha | El botón de avatar del modo fiesta y el secundario «¿Quién del grupo…?», con `users` |
| Le toca a Leo | `LP78FiestaElegido` | «¿Quién del grupo…?» en `bodySmall` y `textSecondary` | Gira y la ventana dice «LEO» | «Le toca a Leo» en `verdict` y `text` | El secundario «Elegir otra vez», con el icono de repetir |

- En el margen superior, centrado, va el chip «Modo fiesta»: un chip de nombre (sección 9) con
  `party-popper` de 16 delante del texto. Se llama «Salir del modo fiesta». Lo apaga y sale el aviso
  breve «Modo fiesta desactivado» con «Deshacer». La bola no se mueve. Mientras la bola escucha,
  piensa o responde, el chip no se ve.
- Tras cada respuesta, el turno pasa al siguiente de la lista.
- `LP78FiestaElegido` sale al tocar «¿Quién del grupo…?», con el sorteo de la sección 6. La barra
  lleva ✕, «Cerrar», y «Leer la respuesta en voz alta». ✕ vuelve al turno: elegir no lo cambia. En
  la ventana, los nombres largos bajan de 9 a 7 unidades, sin espaciado; «Le toca a…» los dice
  enteros.

**Primer uso:**

| Estado | Tablero | Título | Bola | Leyenda | Acciones |
|---|---|---|---|---|---|
| Primer uso | `LP51PrimerUso` | La ayuda | En reposo | Nada: aún no hay racha | El selector |
| Consentimiento de anuncios | `LP52Consentimiento` | La ayuda, bajo el velo | En reposo, bajo el velo `scrim` | Nada | El selector, bajo el velo |

- La racha aparece cuando llega al mínimo (`streakMinDaysShown`); hasta entonces, la leyenda queda
  vacía.
- El formulario de consentimiento (Google UMP) sale al acabar la entrada (2,1 s, sección 6), sobre
  la bola en reposo y solo donde Google lo pide; nunca mientras escucha o piensa. La forma y los
  textos son de Google, y en el móvil suele ser un diálogo casi a pantalla completa: el tablero solo
  marca el momento y el sitio, con un borde discontinuo. Al cerrarlo, `LP51PrimerUso`. «Privacidad
  de anuncios», en Opciones, lo vuelve a abrir.

**Anuncios:**

| Momento | Tablero | Qué se ve |
|---|---|---|
| Cargando un anuncio, si tarda más de 0,6 s | `LP49Recompensa` | El aviso compacto cambia el icono por el indicador de 24 en `warning` y dice «Cargando un anuncio…»; «Ver anuncio» se desactiva |
| No hay anuncio, a los 8 s | `LP49Recompensa` | `clock`, «No hay anuncios ahora. Prueba en un rato.» y «Ver anuncio», activo para volver a probar. «Hazte Premium» sigue debajo |
| Recompensa | `LP49Recompensa` | Visto el anuncio entero, se van el aviso y «Hazte Premium», y sale el aviso breve «Listo: la IA vuelve a responder.», sin acción. La pregunta siguiente ya va con IA |
| Al cerrar la respuesta | `LP50Intersticial` | ✕ → la bola en reposo, ya asentada, con la ayuda, la racha y el selector → fundido de 300 ms a `bg`, la bola también → el anuncio de AdMob → al cerrarlo, fundido de 300 ms desde `bg` y la bola parpadea una vez |

- El anuncio con recompensa sale del aviso del límite de IA (`LP47LimiteIA`). Lo muestra AdMob a
  pantalla completa y no se dibuja. Si se cierra antes de la recompensa, no hay aviso breve y vuelve
  el aviso con «Ver anuncio». Nunca se dan cifras.
- El anuncio al cerrar solo sale con la bola en reposo, nunca mientras escucha, piensa o responde.
  Si se toca la bola o se cambia de página antes de que acabe el fundido, esa vez no hay anuncio. El
  ritmo es el de hoy.

**Valorar y reportar:**
- El pulgar arriba marca la respuesta: el glifo pasa a relleno en `check`, sobre `selected` con
  línea `selectedOutline` (sección 9), y un corte de 2 del color del fondo separa el puño. Es la
  excepción a la ✓: el pulgar ya es su propio icono.
- El pulgar abajo se marca igual y abre la hoja «¿Qué tiene de malo esta respuesta?»
  (`LP25Reportar`), con «Elige una opción y se envía» y dos grupos de filas que actúan al tocar:
  - el primero, solo con «No me gustó»;
  - el segundo, bajo la cabecera «Reportar»: «Es ofensiva», «Es sexual o inapropiada», «Puede hacer
    daño», «No tiene sentido» y «Otra cosa».
- «No me gustó» no es una denuncia. Deja el pulgar marcado y cierra la hoja; sale el aviso breve
  «Gracias por decírnoslo», sin «Deshacer», porque la marca se quita tocando otra vez el pulgar. La
  frase y «Compartir» no cambian.
- Un motivo del segundo grupo envía la denuncia (`LP26Reportada`):
  - el veredicto se queda;
  - la etiqueta y la frase se cambian por la bandera de 16 y «Respuesta reportada», en `emphasis` y
    `textSecondary`;
  - «Compartir» se desactiva;
  - sale el aviso breve «Reportada. Gracias.» con «Deshacer».
- «Deshacer» retira la denuncia: vuelven la frase y «Compartir», y el pulgar sigue marcado.

### Recientes

- La barra lleva los puntos de página y, a la derecha, «Volver a la bola». El título «Recientes» va
  solo (`LP07Recientes`).
- Un grupo con una fila de Recientes (sección 9) por respuesta, de 72 como mínimo:
  - el avatar de 40, sin anillo, arriba; el texto empieza a 88 del borde de la pantalla;
  - la respuesta en `rowTitle`;
  - la frase del personaje en `oracleSmall` y `contentMedium`, en tres líneas como máximo, con «…»;
  - la pregunta, entre «», en `supporting` y `textSecondary`, en dos líneas como máximo; si la
    respuesta llegó sin pregunta, «Una pregunta en silencio», sin «»;
  - «Hace 5 min · Vidente · IA» en `caption` y `textSecondary`; las del día anterior, «Ayer»;
  - al final, arriba, la papelera de 48 «Borrar respuesta» (sección 9; pendiente, abajo).
- Al borrar una, la fila se va en 250 ms y sale el aviso breve «Respuesta borrada» con «Deshacer».
  Con TalkBack, la fila se lee entera, su clic es «ver en la bola» y lleva la acción «Borrar
  respuesta» (sección 8).
- La última fila del grupo es «Borrar todo», una fila que borra (sección 9): el título y la papelera
  de 22 en `error`, la papelera en el hueco del avatar. Abre el diálogo «¿Borrar todas las
  respuestas?» (`LP09BorrarTodo`): «Se borrarán de este dispositivo todas las preguntas y respuestas
  guardadas. No se puede deshacer.», «Borrar todo» en el botón de borrar y «Cancelar».
- Debajo del grupo va «Se guarda solo en este dispositivo».
- Tocar una fila abre la respuesta en la bola (`LP54DesdeRecientes`).
- Vacía (`LP08RecientesVacio`): el estado vacío de la sección 9, con el reloj en la losa, «Aún no hay
  respuestas», «Pregúntale a la bola y tus respuestas aparecerán aquí, guardadas solo en este
  dispositivo» y el botón secundario «Volver a la bola».

### Opciones

- La barra lleva «Volver a la bola» y los puntos de página. Debajo, el título «Opciones»
  (`LP10Opciones`).
- Sin cuenta, un grupo con una fila de 72: un avatar de 40 (un círculo `control` con línea
  `outlineVariant` y `user` de 22 en `contentMedium`), «Entrar», «Los personajes sabrán quién
  pregunta» y ›. Abre la elección de cuenta de Google. A 390 de ancho, el apoyo ocupa dos líneas y
  la fila mide 86. Con cuenta, la fila lleva el avatar de inicial de 40, el nombre y el correo,
  también de 72, y abre «Tu cuenta».
- La fila de Premium, con `crown`: «Premium», «Sin anuncios y más respuestas con IA» y el botón
  tonal «Obtener». Sus otros estados, en «Premium, compartir y sistema».
- Tres grupos:
  - «Respuestas»: Respuestas con IA, Voz, Modo fiesta y Menos movimiento;
  - «General»: Notificaciones, Idioma y Compartir la app;
  - «Información»: Privacidad de anuncios, Política de privacidad, Términos de uso, Licencias de
    código abierto y Contacto.
- Cada fila lleva un icono de 22 y su texto empieza a 72 del borde de la pantalla.
  - Abren una pantalla y llevan ›: Respuestas con IA, Voz, Modo fiesta, Notificaciones, Idioma y
    Licencias de código abierto.
  - Salen de la app y llevan ↗: Política de privacidad, Términos de uso y Contacto.
  - Compartir la app y Privacidad de anuncios actúan al tocar, sin final. Privacidad de anuncios
    vuelve a abrir el formulario de consentimiento (`LP52Consentimiento`).
- **Menos movimiento** (sin tablero): una fila con interruptor y el subtítulo fijo «Sin giros ni
  saltos: la bola cambia con fundidos». Es el interruptor propio de la app de la sección 8:
  `LuzMotion.reduceMotion` se enciende con él o con «Quitar animaciones» de Android. Su icono aún no
  está en la sección 2.
- Los resúmenes dicen el estado de verdad, en `textSecondary` y sin ámbar:

| Fila | Resumen |
|---|---|
| Respuestas con IA | «Activadas» o «Ya usaste las de hoy» |
| Voz | «Preguntas y respuestas» o «Sin micrófono» |
| Modo fiesta | «Desactivado»; encendido, sin tablero |
| Notificaciones | «Activadas» o «Bloqueadas en Android» |
| Idioma | El idioma elegido: «Español» |

- Al pie, a 24 del grupo: la nota de privacidad en `caption`, «El servicio de voz de tu dispositivo
  convierte tu pregunta en texto, que se envía a Gemini, de Google, para responderla. Tus preguntas
  y respuestas se guardan en Recientes, solo en este dispositivo.»; la versión, «Magic AI Ball ·
  v1.3.1»; y «Con la tecnología de Gemini» (antes «Powered by Gemini»).

Pantallas que se abren desde Opciones, todas con «Atrás» y su título:

| Pantalla | Tablero | Contenido | Aviso cuando falta algo |
|---|---|---|---|
| Respuestas con IA | `LP11IA` | Los interruptores «Respuestas con IA» («Escritas al momento») y «Respuestas más largas» («La bola se explica en unas frases») | «Ya usaste las de hoy», «Vuelven mañana. Mientras, la bola responde con sus frases de siempre.», «Ver un anuncio» y la nota «Un anuncio corto y sigues con la IA.» |
| Voz | `LP12Voz` | «Preguntas por voz» («Mantén presionada la bola y habla») y «Respuestas en voz alta» («La bola también dice su respuesta»). «Agitar para preguntar», de la sección 8, sin tablero | «Necesita acceso al micrófono», «Sin él, solo puedes tocar o agitar.» y «Permitir micrófono» |
| Notificaciones | `LP13Notificaciones` | «Veredicto del día» («Una notificación al día, de tu personaje») | «Las notificaciones están desactivadas para esta app», «Sin ellas no te llega el veredicto del día.», «Permitir notificaciones» y la nota «Abre los ajustes de notificaciones de la app.» |
| Idioma | `LP14Idioma` | English, Español y Português; el elegido, con fondo `selected` y la ✓ de 22 | Ninguno |
| Licencias de código abierto | `LP15Licencias` | Lucide («Iconos»), Google Sans («La letra del botón de Google»), Fraunces («La letra de lo que dicen los personajes»), Outfit («La letra de los títulos y los botones»), Inter («La letra del resto del texto») y «Bibliotecas» («AndroidX, Jetpack Compose, Kotlin, Dagger Hilt y Firebase»), cada una con › | Ninguno |
| Modo fiesta | `LP76Fiesta` | Abajo | Ninguno |

- Cada aviso va debajo del interruptor al que afecta, con su acción dentro.
- Con menos de 18 años (`LP75PerfilTeclado`), el interruptor «Respuestas con IA» queda apagado y
  desactivado, con su subtítulo de siempre, y debajo va la nota «Las respuestas con IA son para
  mayores de 18. La bola te responde con sus frases de siempre.» (sin tablero).

**Modo fiesta** es una pantalla propia (`LP76Fiesta`), que sustituye al diálogo
`LP16FiestaDialogo`:
- un grupo con el interruptor «Modo fiesta», con `party-popper` y el subtítulo fijo «La bola anuncia
  el turno de cada persona»;
- bajo la cabecera «Personas», filas de 56: el avatar de inicial de 32, el nombre en `rowTitle` y,
  al final, el botón de icono ✕ «Quitar a Leo». Los separadores empiezan a 64;
- la última fila, «Añadir persona», con + en un círculo de 32 de línea `link` al 40 % y el texto en
  `rowTitle` y `link`. Al tocarla se convierte en el campo «Nombre», con el teclado; Intro añade el
  nombre y deja el campo listo para otro. Nombres de hasta 10 caracteres. Al llegar al máximo, la
  fila desaparece: nunca se dice cuántas caben;
- al quitar a alguien, el aviso breve «Ana quitada» con «Deshacer».

### Personajes y cuenta

**Personajes** (`LP17Personajes`):
- La barra lleva ✕, «Cerrar», y +, «Crear un personaje». Debajo, el título «Personajes».
- Los chips de filtro «Todos», «Originales», «Comunidad» y «Tuyos», en una fila que se desliza
  (sección 9). A 390 caben los cuatro.
- «Todos» enseña los ocho en el grupo «Originales», con filas de elegir personaje: Vidente,
  Filósofo, Astróloga, Bruja, Genio, Mago, Robot («Afirmativo. Bip.») y Pirata («Ni hablar,
  grumete.»). Debajo irán «Comunidad» y «Tuyos».
- El actual lleva fondo `selected`, su anillo y la ✓. Tocar una fila abre su ficha: es la opción (b)
  de la sección 9, aún pendiente.
- Mientras cargan, filas esqueleto. Si no cargan, el aviso «No se pudieron cargar los personajes»,
  «Mientras, sigues con la Vidente.» y «Reintentar». Los dos están en `LP30Estados`.

| Filtro | Tablero | Qué se ve |
|---|---|---|
| Comunidad | `LP61PersonajesComunidad` | Filas de 72 que abren la ficha: el avatar de 40, el nombre en `rowTitle`, la muestra en `oracleXSmall` y `contentMedium`, y «De @javitorres_» en `caption` y `textSecondary`, en una línea, con «…». Debajo, en `caption` y `textSecondary`, «Los crean otras personas. Desde su ficha puedes reportarlos o bloquear a quien los creó.» Si no cargan, el mismo aviso que Personajes |
| Tuyos, vacío | `LP60PersonajesTuyos` | El estado vacío (sección 9): la paleta de 32, «Aún no has creado ninguno», «Elige su color, su accesorio y cómo responde.» y «Crear un personaje», con +. Lleva al mismo recorrido que el + de la barra |

- Los de Comunidad del tablero son de prueba: alias como los de las redes (en minúsculas, sin
  tildes y sin resumir el perfil) y bolas hechas con el editor, sin caras. En producción no se
  inventan usuarios.
- Crear un personaje: `LP19CuentaNecesaria` si no hay cuenta → Google → `LP22Perfil` → Premium si
  hace falta → el editor.

**Ficha de un personaje**, en una hoja:
- el avatar de 88 y el nombre en `screenTitle`;
- debajo, quién lo hizo;
- bajo la cabecera «Cómo responde», un grupo con la descripción y una muestra en `oracleMedium`;
- el botón principal «Usar este personaje».

| Ficha | Tablero | Bajo el nombre | Arriba a la derecha | Botón |
|---|---|---|---|---|
| Original | `LP18Ficha` | «Original de Magic AI Ball» | Nada | «Usar este personaje». En la del actual, desactivado: «Es el que responde ahora» |
| De Comunidad | `LP62FichaComunidad` | «De @javitorres_» | El botón de icono ⋮ «Más opciones» | «Usar este personaje» |
| Tuya | `LP63TuPersonaje` | «Creado por ti · En Comunidad»; si no se comparte, «Creado por ti» | Los botones de icono «Editar» (lápiz) y «Borrar» (`trash-2`) | «Usar este personaje» |

- Descripciones de los originales, por ejemplo: Vidente, «Misteriosa y segura. Ve con los ojos
  cerrados.»; Bruja, «Pícara y directa. Te dice lo que no quieres oír y se ríe un poco mientras lo
  dice.».
- En la de Comunidad, la descripción va tal como la escribió su autor. «Más opciones» ofrece dos
  acciones, ninguna en coral porque no borran. El lienzo las dibuja en un menú (pendiente, abajo):
  - «Reportar personaje», con la bandera, abre la hoja de reportar con «¿Qué pasa con este
    personaje?», «Elige una opción y se envía» y seis filas: «Es ofensivo», «Es sexual o
    inapropiado», «Puede hacer daño», «Se hace pasar por alguien», «Es spam o engaño» y «Otra cosa».
    Al tocar una, se envía, la hoja se cierra y sale «Reportado. Gracias.» con «Deshacer».
  - «Bloquear a @javitorres_» abre el diálogo «¿Bloquear a @javitorres_?», «Dejarás de ver sus
    personajes en Comunidad.», el botón principal «Bloquear» y «Cancelar». Después sale
    «@javitorres_ bloqueado» con «Deshacer».
- En la tuya, «Editar» abre el editor con el título «Editar personaje» y lo guardado. «Borrar» no
  pregunta: cierra la ficha y sale «Capitán Gruñón borrado» con «Deshacer». Cuando el aviso se va, se
  borra también de Comunidad. Como se puede deshacer, la papelera es neutra, en `contentMedium`
  (sección 9).

**Crear sin cuenta** (`LP19CuentaNecesaria`), en un diálogo: «Para crear personajes necesitas una
cuenta», «Entra con Google para crear el tuyo. La app sigue funcionando igual sin cuenta.»,
«Continuar con Google» y «Ahora no».

**Entrar con Google**, desde la fila «Entrar» de Opciones o desde ese diálogo:

| Estado | Tablero | Qué se ve |
|---|---|---|
| Entrando | `LP66EntrarCargando` | Tras elegir la cuenta en la ventana de Google, el › de la fila «Entrar» se cambia por el indicador de 18 en `textSecondary`. La fila no se puede volver a tocar y nada se mueve. TalkBack dice «Entrando con Google» |
| Cancelado | Sin tablero | Si se cierra la ventana de Google, no sale ningún aviso: la pantalla queda como estaba y el foco vuelve a «Entrar» o a «Continuar con Google» |
| Error | `LP67EntrarError` | Un aviso que pide atención ocupa el sitio de la fila «Entrar»: `circle-alert`, «No se pudo entrar con Google», «No hay conexión. Todo sigue como estaba.» y «Reintentar». Si falla Google: «Google no responde. Todo sigue como estaba.» |

**¿Quién pregunta?** es una hoja sin asa que sale sobre la bola al entrar con Google (`LP22Perfil`):
- «Dos datos para respuestas a tu medida»;
- el campo «Nombre» y el campo de fecha (sección 9) bajo «Fecha de nacimiento», con «Día», «Mes» y
  «Año» encima de cada casilla y la ayuda «12 de marzo de 1990»;
- un grupo con «Qué se envía a Gemini, de Google»: «Tu nombre y tu signo. El día de tu cumpleaños,
  también eso. Cada personaje recibe solo lo que usa. Si creas un personaje, también su nombre y tu
  descripción. Nunca tu fecha de nacimiento, tu correo ni tu foto.», y la fila «Política de
  privacidad», de 56, con ↗;
- «Aceptar y continuar» y «Ahora no» (antes «Continuar sin cuenta»).

| Estado | Tablero | Qué se ve |
|---|---|---|
| Con el teclado | `LP75PerfilTeclado` | La hoja se apoya en el teclado y su cuerpo se desplaza. Los botones quedan fijos justo encima, con una línea `outlineVariant` arriba. El teclado del sistema solo se dibuja en los tableros: un bloque neutro de 250 de alto |
| Menores de 18 | `LP75PerfilTeclado` | En cuanto la fecha está completa, el grupo «Qué se envía a Gemini» se cambia por una nota en un grupo, con `info`: «Las respuestas con IA son para mayores de 18. La bola te responde con sus frases de siempre.» El botón dice «Continuar»: no se envía nada. Sin ámbar ni coral, porque no hay nada que arreglar |
| Fecha que no existe | `LP30Estados` | El error del campo de fecha (sección 9; el texto, pendiente) |

**Nuevo personaje** es una pantalla completa (`LP23Editor`), porque el formulario no cabe en una
hoja:
- la barra con ✕, «Cancelar», y «Guardar» como botón de texto;
- la vista previa, de 150 de alto como mínimo, que crece con la letra: arriba la etiqueta «▲ Capitán
  Gruñón» y debajo «Con tus palabras» en `oracleMedium`, como bajo la bola. Fondo `tintSurface` con
  contorno `tintOutline`, del color elegido. Sobre el tinte, el nombre va en `text` y el color lo
  pone el ▲ (sección 2);
- los campos «Nombre», de hasta 16 caracteres, y «¿Cómo responde?», de hasta 500, con su contador;
- «Color · Orquídea»: nueve muestras de color (sección 9) en una fila que llega al borde; la
  etiqueta dice el nombre del elegido;
- «Accesorio · Sombrero de mago»: seis losas de accesorio, nombradas por el objeto: «Ninguno»,
  «Sombrero de mago», «Turbante», «Sombrero de bruja», «Diadema» y «Gafas»;
- el grupo «Compartir en Comunidad», con `users`, el subtítulo fijo «Otras personas podrán verlo y
  usarlo» y el interruptor apagado por defecto (`LP64EditorRechazo`);
- la nota «Se envía a Gemini, de Google, para responder. No pongas datos personales.».

La paleta del tablero está pendiente con la de los personajes (sección 9). Sus colores quedan a
ΔE00 18 o más de la interfaz y dan 6:1 o más sobre `bg`. Los nombres en inglés y portugués, por
confirmar:

| Color | es | en | pt |
|---|---|---|---|
| `#E3733B` | Mandarina | Tangerine | Tangerina |
| `#EDE0AB` | Vainilla | Vanilla | Baunilha |
| `#BFF2A6` | Pistacho | Pistachio | Pistache |
| `#63C15C` | Hierba | Grass | Grama |
| `#5CC1B4` | Aguamarina | Aquamarine | Água-marinha |
| `#4CCCFA` | Celeste | Sky | Celeste |
| `#D84CFA` | Orquídea | Orchid | Orquídea |
| `#FC9CFC` | Chicle | Bubblegum | Chiclete |
| `#CD7AB1` | Malva | Mauve | Malva |

| Estado | Tablero | Qué se ve |
|---|---|---|
| Descripción rechazada | `LP64EditorRechazo` | Tras «Guardar», la pantalla baja hasta el campo: el título grande se ha ido y «Nuevo personaje» pasa a la barra, que lleva su línea inferior. «¿Cómo responde?» en error: «Esta descripción no se puede usar.», con el contador a la derecha. No se guarda nada. Para el nombre, «Este nombre no se puede usar.» |
| ¿Descartar el personaje? | `LP65EditorDescartar` | Al tocar ✕ o atrás con algo escrito: el diálogo «¿Descartar el personaje?», «Se pierde lo que has escrito.», el botón de borrar «Descartar» y el de texto «Seguir editando». Sin nada escrito, ✕ cierra sin preguntar. Al editar uno guardado, «¿Descartar los cambios?» |

**Tu cuenta**, en una hoja (`LP20Cuenta`):
- el avatar de inicial de 64, el nombre, el correo y la etiqueta «Tienes Premium», con la corona;
- «Tu perfil», «Ana · 12 de marzo de 1990», con ›;
- «Gestionar suscripción», «Cancélala o cámbiala en Google Play», con ↗;
- «Cerrar sesión», «Tu cuenta sigue ahí para cuando vuelvas»;
- «Eliminar cuenta», «Se borran tu perfil y tus personajes», una fila que borra.

**¿Eliminar tu cuenta?** es un diálogo centrado entre las barras (`LP21EliminarCuenta`):
- «Se borran tu perfil y tus personajes. Tus respuestas siguen en este móvil. No se puede deshacer.»
- Con Premium, debajo, «Premium se sigue cobrando hasta que lo canceles.» en `noticeTitle` y
  `text`, y las acciones en este orden: «Gestionar en Google Play» (secundario), «Eliminar cuenta»
  (de borrar) y «Cancelar» (de texto).
- Sin Premium, ni esa línea ni «Gestionar en Google Play».

| Estado | Tablero | Qué se ve |
|---|---|---|
| Eliminando | `LP73Eliminando` | «Eliminar cuenta» en su estado «Cargando» (sección 9) y «Cancelar» desactivado. Atrás y tocar fuera no cierran el diálogo. Con Premium, igual. Si falla, vuelve «Eliminar cuenta» y sale el aviso breve «No se pudo eliminar la cuenta. Prueba otra vez.» |
| Cuenta eliminada | `LP74CuentaEliminada` | Opciones sin cuenta, con la fila «Entrar», y el aviso breve «Cuenta eliminada», sin «Deshacer» |

### Premium, compartir y sistema

**Magic AI Ball Premium**, en una hoja (`LP24Premium`):
- tres personajes propios de ejemplo y el título en `screenTitle`, centrado;
- las cuatro ventajas, en filas de 56 con su icono: «Sin anuncios», «Más respuestas con IA cada
  día», «Tus propios personajes» y «Cada personaje responde con el tono que describas»;
- «La app sigue siendo gratis, con anuncios.»;
- las condiciones: «0,99 € al mes, cobrado por Google Play. Se renueva automáticamente hasta que
  canceles.» y «Cancela cuando quieras en Google Play.»;
- a 8, «Términos de uso» y «Política de privacidad», botones de texto de 48, uno junto al otro;
- «Suscribirme por 0,99 € al mes» y «Ahora no». Con la letra grande, los botones de texto pasan a
  dos líneas y «Suscribirme…» crece sin pisarlos.

| Estado | Tablero | Qué se ve |
|---|---|---|
| Buscando el precio | `LP30Estados` | «Suscribirme» en su estado «Cargando» (sección 9) y, debajo, «Buscando el precio en Google Play…» |
| Sin precio, a los 8 s | `LP72PremiumSinPrecio` | El precio, las condiciones y «Suscribirme» se cambian por el aviso «No se pudo cargar el precio», «Google Play no responde. Sin el precio no puedes suscribirte.» y «Reintentar». Quedan «Términos de uso», «Política de privacidad» y «Ahora no». Al reintentar vuelve la espera |
| Ya tienes Premium | `LP69PremiumExito` | Una hoja sobre Opciones al volver de Google Play: los tres personajes de ejemplo, «Ya tienes Premium» en `screenTitle`, «Ya no verás anuncios y tienes más respuestas con IA. Ahora puedes crear tus propios personajes.» en `bodySmall` y `contentMedium`, «Crear un personaje» (principal) y «Ahora no». Sin cuenta, «Crear un personaje» sigue el recorrido de `LP19CuentaNecesaria` |

**La fila de Premium en Opciones:**

| Estado | Tablero | La fila | Final |
|---|---|---|---|
| Sin Premium | `LP10Opciones` | «Sin anuncios y más respuestas con IA» | El botón tonal «Obtener», que abre Premium |
| Activo | `LP20Cuenta`, `LP69PremiumExito` | «Activo · Gestionar en Google Play» | ↗, a Google Play |
| Pago pendiente | `LP68PremiumPendiente` | «Pago pendiente. Se activa cuando Google Play lo confirme.» Sin ámbar: no hay nada que hacer | ↗, a la suscripción en Google Play |
| Cancelado | `LP71PremiumCancelado` | «Hasta el 17 de diciembre. No se renovará.», sin partir «No se renovará». Sin ámbar | ↗, a Google Play, para reactivarla |
| Problema con el pago, en el periodo de gracia | `LP70PremiumPago` | En su sitio, un aviso que pide atención con la tarjeta de pago: «No se pudo cobrar Premium», «Actualiza el pago antes del 27 de noviembre o perderás Premium.» y «Arreglar en Google Play» | — |
| Cobro retenido: Premium en pausa | Sin tablero | El mismo aviso: «Premium está en pausa», «Actualiza el pago en Google Play para recuperarlo.» y «Arreglar en Google Play» | — |

**Compartir.** «Compartir», en la respuesta, abre una hoja (`LP79CompartirHoja`):
- el título «Compartir» y la tarjeta al 60 % (216 × 270), con radio 12 y línea `outlineVariant`;
- debajo, dos chips de filtro: «Publicación» (4:5, 1080 × 1350) e «Historia» (9:16, 1080 × 1920).
  En 9:16 el contenido es el mismo y la bola, anclada a la esquina, pasa de 290 a 340;
- el interruptor «Incluir la pregunta», con el icono de bocadillo y sin subtítulo. Sin pregunta, la
  tarjeta empieza por la respuesta;
- el botón principal «Compartir», con `share-2`, que abre la hoja de Android. Mientras se genera la
  imagen, está en su estado «Cargando».

Por defecto, «Publicación» y con la pregunta. Se recuerda la última elección.

**Tarjeta de compartir** (`LP27Compartir`), de 360 × 450. Se dibuja aparte, con sus propios tamaños
(sección 9):
- Fondo y tinta: pendientes (`shareBackground` y `shareInk`, sección 2). El tablero usa la luz del
  personaje al 18 % sobre `#FBF9F7` (con la Vidente, `#EAE2F8`) y la tinta `#2A2440`.
- La pregunta, en un bocadillo blanco con relleno de 12 × 16, en Inter 15/20, en dos líneas como
  máximo, con «…».
- La respuesta en `shareDisplay`, en una columna de 200 como máximo y en dos líneas. Si no cabe,
  baja de 2 en 2 hasta 30/32. Si una sola palabra no cabe a 30, la columna se ensancha lo justo,
  hasta 304: a esa altura no hay bola. Las expresiones que no deben partirse llevan espacio duro:
  «Sí, / sin duda.».
- La frase en `oracleMedium`, en una columna de 200, en dos líneas con «…».
- «▲ Vidente», en Inter 600 14/18 y `ink`, con el ▲ del mismo color.
- 16 tras la pregunta y 12 entre lo demás. El texto nunca llega a la bola: se ha comprobado con una
  pregunta de 21 palabras, «Definitivamente, sin duda.», «Probablemente.», «Ni lo sueñes.» y una
  frase de 110 caracteres.
- La marca, «Magic AI Ball», con su glifo; el hueco del glifo lleva el color del fondo.
- La bola entra por la esquina inferior, inclinada unos 18°, con el disco blanco descentrado como
  cara.

**Notificación del veredicto** (`LP28Notificacion`): el glifo de la bola en monocromo, «Magic AI
Ball · ahora», «Veredicto del día», la frase y, a la derecha, el avatar del personaje.

**Widget** (`LP29Widget`): dos de 156 × 156 con el radio del sistema. Uno lleva la bola en reposo y
el otro la ventana con la última respuesta (sección 9).

### Reloj

Wear OS, pantalla redonda de 227 (sección 9, «Reloj»). La hora va arriba en todas las pantallas.

| Estado | Tablero | Qué se ve |
|---|---|---|
| En reposo | `LPW1Reposo` | La bola de 112 y «Toca o agita.» / «Mantén presionado para hablar.» en `supporting` y `contentMedium`, en tres líneas |
| Escuchando | `LPW6Escuchando` | La bola de 112 con la cara de escuchar (8,6 × 21, a −12°) y el halo latiendo, sin ondas ni marcas. «Te escucho…» en `contentMedium`. Cuando oye palabras, esa línea las enseña: una sola, recortada por el principio |
| Pensando | `LPW7Pensando` | La bola de 112 con la ventana y el líquido, y la pregunta entre «» en `textSecondary`, en dos líneas como máximo |
| La respuesta | `LPW2Respuesta` | La ventana de 120, para que se lea. Debajo, «Otra vez», la acción principal: una píldora de 160 × 52 en `accent`. Más abajo, al desplazar, «Reportar» en una píldora `control`, que en reposo solo asoma por el borde |
| Sin respuesta | `LPW8Fallo` | La bola de 104 con la mirada de duda y «No lo veo claro. Pregúntame otra vez.» en `contentMedium`. Tocar o agitar da una respuesta; mantener y hablar pregunta |
| Sin el móvil | `LPW9SinMovil` | La bola de 88 en reposo, «Móvil desconectado» en `text` y «Responde con sus frases de siempre» en `textSecondary`. Sin ámbar: no hay nada que hacer. El aviso se va solo al volver la conexión |
| Sin micrófono | `LPW10Microfono` | `mic` de 24 en `warning`, «Sin micrófono solo puedes tocar o agitar.» en `text` y el botón ámbar «Permitir», en píldora de 52. Si se denegó para siempre, «Abrir ajustes» |
| Modo ambiente | `LPW11Ambiente`, `LPW12AmbienteMago` | La bola de ambiente (sección 6): sin aura, brillo, sombra ni degradados; el cuerpo negro con el borde de 1,5 al 60 % de la luz del personaje; la cara, un aro de 1,5 en `contentMedium`, sin relleno, con los ojos de reposo en `contentMedium`; el accesorio, en contorno de 1,5 del color del borde. Sin texto, solo la hora. Se desplaza unos píxeles cada minuto |

- En ambiente, el borde de la Vidente da 2,9:1 sobre negro: es decorativo, y la forma la da el aro
  de la cara, a 13,6:1. El del Mago da 4,3:1.
- Idioma (`LPW3Idioma`), Personaje (`LPW4Personaje`) y Respuestas con IA (`LPW5IA`, «Sí» o «No») son
  listas de opciones (sección 9): píldoras de 160 × 52 con relleno de 16. La elegida lleva fondo
  `selected`, línea `selectedOutline` y ✓ de 18 en `check`. La que llega al borde se encoge al 86 % y
  se apaga al 70 %.
- En Personaje, cada píldora lleva el avatar de 28, con 12 de relleno a la izquierda, y el nombre en
  `text`. El anillo del color del personaje solo va en el elegido.
- Con el límite de IA gastado, Respuestas con IA dice «Ya usaste las de hoy. Vuelven mañana.» en
  `caption` y `textSecondary`, sin ámbar: en el reloj no hay acción que ofrecer.

### Estados de los componentes

Los tableros de «Componentes Luz», antes de las pantallas, enseñan los componentes de la sección 9:

| Tablero | Qué enseña |
|---|---|
| `LuzBase` | Color, letra y luz. «El color es de los personajes», con los ocho: Robot y Pirata, dibujados como los otros seis. «Pulsar y foco» |
| `LuzContenido` | La racha, las etiquetas, los avisos y la fila de Premium |
| `LuzBotones` | Principal, secundario, tonal, ámbar, de borrar, de texto y de icono, en normal, pulsado, desactivado y cargando |
| `LuzSeleccion` | El selector de oráculo, también con foco; el interruptor, también desactivado; elegir personaje; los chips de filtro elegido, normal y desactivado; las filas que actúan al tocar, una con foco y otra desactivada |
| `LP30Estados` | Los estados que no salen en las pantallas (abajo) |

`LP30Estados` lleva:
- los botones normal, pulsado, con foco, desactivado, cargando y de texto pulsado;
- el ámbar y el de borrar, normales, pulsados y desactivados;
- el campo normal, con foco, con el error «Ponle un nombre» y desactivado;
- la fecha que no existe, con las tres casillas en error;
- Premium mientras busca el precio;
- Personajes sin conexión y cargando;
- el pulgar marcado y la lectura en voz alta;
- «Añadir» sin nombre escrito, del diálogo `LP16FiestaDialogo`: con `LP76Fiesta` ya no se usa.

### Textos nuevos

Hay que añadirlos o cambiarlos en los tres idiomas:

| Dónde | Textos |
|---|---|
| La bola | La ayuda, «Toca o agita para una respuesta.» y «Mantén presionado y pregunta en voz alta.» (antes «Toca o agita para preguntar. Mantén presionado para hablar.»); «Suelta para preguntar · Desliza fuera para cancelar»; «Haz una pregunta de sí o no.»; «No lo veo claro. Pregúntame otra vez.» (antes «Prueba otra vez luego.»); «Una pregunta en silencio»; «¿Otra pregunta? Mantén presionada la bola.»; «Volver a Recientes»; «Detener la lectura» |
| Voz y micrófono | «Sin micrófono, la bola no te oye.», «Permitir», «Abrir ajustes», «No te he oído.», «Mantén presionado y habla.» y «Este móvil no puede escuchar preguntas.» |
| Sin IA y pregunta delicada | «Ya usaste las de hoy. Vuelven mañana.», «Ver anuncio», «Hazte Premium», «· sin conexión», «Esto no lo puede responder un juguete.», «Habla con alguien de confianza o llama al 112.» y «Dónde pedir ayuda» |
| Anuncios | «Cargando un anuncio…», «No hay anuncios ahora. Prueba en un rato.» y «Listo: la IA vuelve a responder.» |
| Modo fiesta | «Turno de Ana» (antes «Ana, te toca»), «Salir del modo fiesta», «Modo fiesta desactivado», «Le toca a Leo», «Elegir otra vez», «La bola anuncia el turno de cada persona», «Personas», «Añadir persona», «Quitar a Leo» y «Ana quitada» |
| Valorar y reportar | «No me gustó», la cabecera «Reportar» y «Gracias por decírnoslo» |
| Recientes | «Ayer» y «Respuesta borrada» |
| Opciones | «Entrar» y «Los personajes sabrán quién pregunta» (antes la tarjeta «Respuestas a tu medida»); los resúmenes «Activadas», «Ya usaste las de hoy», «Preguntas y respuestas», «Sin micrófono», «Desactivado» y «Bloqueadas en Android»; «Menos movimiento» y «Sin giros ni saltos: la bola cambia con fundidos»; «Con la tecnología de Gemini» (antes «Powered by Gemini») |
| Ajustes | El aviso de Respuestas con IA: «Vuelven mañana. Mientras, la bola responde con sus frases de siempre.», «Ver un anuncio» y «Un anuncio corto y sigues con la IA.». El de Voz: «Sin él, solo puedes tocar o agitar.». La nota de Notificaciones, ahora con punto. En Licencias, «Bibliotecas» y las descripciones de Fraunces, Outfit e Inter |
| Personajes | «Es el que responde ahora»; la descripción de la Bruja, sin la coma; «Aún no has creado ninguno» y «Elige su color, su accesorio y cómo responde.»; «De @…» y la nota de Comunidad; «Más opciones», «Reportar personaje», «¿Qué pasa con este personaje?», sus seis motivos y «Reportado. Gracias.»; «Bloquear a @…», «¿Bloquear a @…?», «Dejarás de ver sus personajes en Comunidad.», «Bloquear» y «@… bloqueado»; «Creado por ti», «En Comunidad», «Editar», «Borrar» y «… borrado» |
| Editor | «Editar personaje», «Color · …», «Accesorio · …», los nombres de los colores y de los accesorios, «Compartir en Comunidad», «Otras personas podrán verlo y usarlo», «Esta descripción no se puede usar.», «Este nombre no se puede usar.», «¿Descartar el personaje?», «¿Descartar los cambios?», «Se pierde lo que has escrito.», «Descartar», «Seguir editando» y la nota «Se envía a Gemini, de Google, para responder. No pongas datos personales.» |
| Cuenta | «Entrando con Google», «No se pudo entrar con Google», «No hay conexión. Todo sigue como estaba.» y «Google no responde. Todo sigue como estaba.»; el texto de «Qué se envía a Gemini, de Google»; «Día», «Mes» y «Año»; «Ahora no» (antes «Continuar sin cuenta»); la nota de menores y «Continuar»; los apoyos de Tu cuenta; el cuerpo de «¿Eliminar tu cuenta?» y «Premium se sigue cobrando hasta que lo canceles.»; «No se pudo eliminar la cuenta. Prueba otra vez.» y «Cuenta eliminada» |
| Premium | Las cuatro ventajas y las condiciones; «No se pudo cargar el precio» y «Google Play no responde. Sin el precio no puedes suscribirte.»; «Ya tienes Premium» y su texto; «Pago pendiente. Se activa cuando Google Play lo confirme.»; «Hasta el … No se renovará.»; «No se pudo cobrar Premium», «Actualiza el pago antes del … o perderás Premium.» y «Arreglar en Google Play»; «Premium está en pausa» y «Actualiza el pago en Google Play para recuperarlo.» |
| Compartir | «Publicación», «Historia» e «Incluir la pregunta» |
| Reloj | «Otra vez», «Móvil desconectado», «Responde con sus frases de siempre» y «Sin micrófono solo puedes tocar o agitar.» |

### Pendiente de decidir

**La bola**
- Sin pregunta: «Una pregunta en silencio» en el título y en Recientes, o el hueco vacío.
- Cuándo sale el aviso del límite de IA: solo en la primera respuesta sin IA del día (propuesta) o
  en todas.
- La pista de `LP53OtraVez`: siempre, o solo hasta que se pregunta así una vez (propuesta).
- `LP44SinVoz`: mantener la bola enseña la nota y no pregunta (propuesta), o cuenta como un toque,
  como hoy.
- `LP40Escuchar`: con el dedo fuera, que la línea diga «Suelta para cancelar» y el halo deje de
  seguir la voz (propuesta).
- `LP54DesdeRecientes`: «Hace 5 min» encima de la pregunta (propuesta; se puede quitar).

**Pregunta delicada**
- Qué abre «Dónde pedir ayuda» (una hoja con teléfonos por país o una web) y qué número de
  emergencias sale en cada país en lugar del 112.
- Dependencia de producto: para enseñar `LP45Delicada`, la respuesta de la IA tiene que marcar la
  pregunta como delicada. Hoy solo lo sabe la IA (`DefaultPromptPolicy`), que cambia el texto.

**Edad y menores**
- Dónde se pregunta la edad a quien usa la IA sin cuenta, que hoy solo se pregunta en «¿Quién
  pregunta?»: al dar la primera respuesta con IA, en el primer uso o dejando la IA solo para quien
  entra.
- Qué pueden hacer los menores: ver Comunidad, crear personajes (que necesitan la IA) y compartir.

**Comunidad**
- Quién publica: según la edad, la cuenta o Premium.
- Si un personaje se revisa antes de verse o después.
- Qué alias se muestra: la app no tiene alias, y «De @autor» necesita uno elegido al compartir por
  primera vez, nunca el nombre de Google.
- Qué pasa con lo publicado al eliminar la cuenta.
- Cuántos reportes ocultan un personaje y cómo se recurre.
- Dónde se desbloquea a alguien (propuesta: «Tu cuenta › Personas bloqueadas»; sin cuenta, en este
  móvil).
- Dónde van «Reportar personaje» y «Bloquear a @…»: en el menú ⋮ de `LP62FichaComunidad`, que la
  sección 9 no construye («Qué no se construye»), o en filas de una hoja.
- Si los personajes de Comunidad salen solo de la paleta del editor: Abuela Remedios y El Cuñao
  llevan los colores de los ejemplos de Premium, que no están en ella.

**Valorar y reportar**
- «Reportar» o «Denunciar».
- Si «No tiene sentido» es una denuncia o solo una valoración, como «No me gustó».

**Personajes**
- Qué hace tocar una fila de personaje: la opción (a) o la (b) de la sección 9. El lienzo dibuja la
  (b).
- `LP17Personajes`: un grupo «Originales» con los ocho, o los grupos «Los de siempre» y «Con
  actitud», que suman 668 en 640 libres y dejan al Pirata bajo la barra de gestos.
- El dibujo del avatar de cada personaje (sección 9).
- Las losas «Sombrero de bruja», «Diadema» y «Gafas» van en los colores de su personaje. Lo
  coherente sería el color elegido, pero este trabajo no redibuja accesorios.
- La paleta del editor y los nombres de sus colores en inglés y portugués.

**Recientes**
- Cómo se borra una respuesta: con la papelera de 48 de la sección 9, o deslizando la fila hacia la
  izquierda, como dibuja `LP07Recientes`, con `surfaceOverlay` y la papelera detrás.

**Compartir**
- «Publicación» e «Historia», o «4:5» y «9:16».
- El fondo y la tinta de la tarjeta para cada personaje (`shareBackground` y `shareInk`, sección 2).

**Reloj**
- Si el reloj puede responder con IA sin el móvil: `LPW9SinMovil` supone que solo lo hace a través
  del móvil.
- El contorno del accesorio en modo ambiente: el color del personaje al 60 % (propuesta) o
  `contentMedium`.

**Textos**
- «Ya usaste las de hoy» es de Latinoamérica; en España se dice «Ya has usado las de hoy». Afecta a
  la bola, a Opciones, a Respuestas con IA y al reloj.
- El aviso del micrófono en la bola: «Sin micrófono, la bola no te oye.» o «Sin micrófono solo
  puedes tocar o agitar.», que repite la ayuda y es el del reloj.
- «Ver un anuncio» (`LP11IA`) o «Ver anuncio» (`LP47LimiteIA`, `LP49Recompensa`): uno solo para los
  dos sitios.
- El error de la fecha: «Fecha no válida» (sección 9) o «Esa fecha no existe» (`LP30Estados`).
- El nombre del interruptor «Agitar para preguntar» (sección 8): agitar da una respuesta sin
  pregunta, como tocar.

**Pantallas**
- El diseño en horizontal (sección 2, «Ventana»).

**Tableros que faltan**
- La carga de Comunidad.
- El alias al compartir en Comunidad por primera vez, cuando se decida.
- El teclado abierto en el editor (`LP23Editor`).
- La letra al 200 % en `LP21EliminarCuenta`, `LP22Perfil` y `LP24Premium`, con el cuerpo desplazable
  y las acciones fijas.
- «Menos movimiento» y «Agitar para preguntar» en Opciones, la nota de menores en Respuestas con IA
  y Premium en pausa.

## 8. Accesibilidad

El objetivo es WCAG 2.2 AA en el móvil y en el reloj. Cada PR de interfaz lo comprueba como dice el
[plan](plan.md): TalkBack, contraste, objetivos de 48 y capturas en oscuro con la letra al 100, 130
y 200 %.

Lo esencial:
- La bola tiene una descripción que cambia con el estado; por ejemplo, «La Vidente responde: sí, sin
  duda».
- La respuesta también se lee fuera de la bola: el veredicto va en texto bajo ella, para TalkBack y
  para la letra grande.
- Objetivos táctiles de 48 como mínimo.
- Lo elegido se marca con ✓ y fondo, nunca solo con color.
- Un aviso que pide atención va en ámbar y siempre da una salida.
- Todo lo que se hace con un gesto (mantener, agitar, deslizar, arrastrar) tiene otro camino.

### Semántica de cada componente

Los componentes reciben sus textos de accesibilidad como parámetros, en los tres idiomas
([ADR 0002](adr/0002-diseno-listo-para-kmp.md)).

| Componente | En Compose | TalkBack lee, por ejemplo |
|---|---|---|
| Botón | `Role.Button` con su etiqueta; desactivado, `disabled()`; cargando, `stateDescription` «Cargando» y los toques se ignoran sin desactivarlo | «Suscribirme, botón, cargando» |
| Botón de icono | `contentDescription` con lo que hace (tabla de nombres, abajo); el icono, `null` | «Recientes, botón» |
| Fila que abre o actúa | `semantics(mergeDescendants = true)` y `clickable(onClickLabel = …)`, sin `contentDescription`: se lee su texto. ›, ↗ y el icono de inicio no se leen | «Voz, Preguntas y respuestas, botón. Toca dos veces para abrir» |
| Fila que sale de la app | La misma, con `onClickLabel` «abrir en el navegador» | «Términos de uso, botón. Toca dos veces para abrir en el navegador» |
| Fila de Recientes | Fusionada; `onClickLabel` «ver en la bola»; la papelera sale del recorrido y pasa a `customActions` «Borrar respuesta» | «Sí, sin duda. Las estrellas lo tenían claro…» y, en acciones, «Borrar respuesta» |
| Fila con interruptor | `toggleable(value, role = Role.Switch)` en toda la fila y `Switch(onCheckedChange = null)` dentro | «Veredicto del día, Una notificación al día…, interruptor, activado» |
| Fila que elige (Idioma) | `selectable(selected, role = Role.RadioButton)` dentro de `selectableGroup()` | «Español, botón de opción, seleccionado, 2 de 3» |
| Cabecera de grupo y título de pantalla | `heading()` | «Respuestas, encabezado» |
| Chip de filtro | `selectable(role = Role.RadioButton)` en un `selectableGroup()` | «Comunidad, botón de opción, no seleccionado» |
| Chip de nombre | `Role.Button`, con lo que hace como nombre; el icono y la ✕ no se leen | «Salir del modo fiesta, botón» |
| Muestra de color | `selectable(role = Role.RadioButton)` con el nombre del color en `contentDescription` | «Orquídea, botón de opción, seleccionado» |
| Losa de accesorio | Igual, con el nombre del objeto | «Sombrero de mago, botón de opción, no seleccionado» |
| Pulgares | `toggleable(value, role = Role.Checkbox)` | «Buena respuesta, casilla de verificación, marcada» |
| Selector de oráculo y botón de avatar | `Role.Button`; nombre «Responde como Vidente»; `onClickLabel` «elegir quién responde» | «Responde como Vidente, botón. Toca dos veces para elegir quién responde» |
| Campo de texto | La etiqueta y el campo se fusionan; el error, con `error(…)`; el contador se lee como «14 de 16 caracteres» | «Nombre, cuadro de edición, Capitán Gruñón, 14 de 16 caracteres» |
| Campo de fecha | Tres campos llamados «Día», «Mes» y «Año» bajo el encabezado «Fecha de nacimiento»; el error, con `error(…)` en los tres | «Día, cuadro de edición, 12» |
| Paginador | Cada página lleva `paneTitle` («La bola», «Recientes», «Opciones»); los puntos, un nodo con `stateDescription` «Página 2 de 3» | Al cambiar: «Recientes» |
| Hoja y diálogo | `paneTitle` con su título; el título, `heading()`; el asa, la acción «Cerrar» | Al abrirse: «¿Borrar todas las respuestas?» |
| Aviso breve | `liveRegion = LiveRegionMode.Polite`; su acción, alcanzable con el foco | «Reportada. Gracias. Deshacer» |
| Aviso que pide atención | Un grupo (`isTraversalGroup`); si aparece con la pantalla abierta, `liveRegion = Polite` en su título | «Necesita acceso al micrófono…» |
| Aviso compacto | `liveRegion = Polite` en todo el aviso; su acción, alcanzable con el foco | «Sin micrófono, la bola no te oye. Permitir, botón» |
| Indicador suelto | `progressBarRangeInfo = Indeterminate` y «Cargando» | «Cargando» |
| Filas esqueleto | Las filas no se leen; el grupo, un solo nodo con lo que carga | «Cargando personajes» |
| Etiqueta, racha y avatares | Texto normal; los iconos y los avatares junto a un nombre, `null` | «3 días seguidos» |
| Veredicto y frase | Un nodo fusionado; el ▲ se dibuja, no es un carácter, y «·» se lee como coma | «Sí, sin duda. Vidente añade, con IA: Las estrellas lo tenían claro.» |
| La bola | Ver abajo | «Bola mágica, responde la Vidente» |

**La bola (`OracleBall`):**
- `contentDescription` «Bola mágica» y un `stateDescription` por estado, que TalkBack anuncia porque
  la bola es `liveRegion = Polite`:

  | Estado | `stateDescription` |
  |---|---|
  | Reposo | «Responde la Vidente» |
  | Escuchando | «Te escucho» |
  | Pensando | «Pensando…» |
  | Respuesta | «La Vidente responde: sí, sin duda» |
  | Sin respuesta, a los 6 s | «Sin respuesta» |
  | Dormida · sin conexión | «Dormida» · «Sin conexión» |

- `onClick(label = "pedir una respuesta")`: tocar pide una respuesta sin pregunta, así que no se
  llama «preguntar». TalkBack dice «Toca dos veces para pedir una respuesta».
- `customActions` «Preguntar en voz alta»: empieza a escuchar sin mantener y para sola cuando la
  persona se calla. «Cancelar», en la barra, la para antes.

**Nombres de los botones de icono.** Dicen lo que hacen, sin la palabra «botón» (TalkBack ya dice el
rol) y sin el estado (va en `stateDescription`). Si una lista repite un botón, el nombre lleva el
elemento: «Quitar a Ana», «Ver ficha de Bruja». Nunca dos elementos de una lista con el mismo
nombre.

| Dónde | Icono | Nombre |
|---|---|---|
| Barra de la bola | `history` · `sliders-horizontal` | «Recientes» · «Opciones» |
| Recientes y Opciones | `chevron-right` o `chevron-left` | «Volver a la bola» |
| Pantallas que se abren desde Opciones | `chevron-left` | «Atrás» |
| Bola escuchando o pensando · bola con la respuesta | `x` | «Cancelar» · «Cerrar» |
| Bola con la respuesta | `volume-2` · `square` | «Leer la respuesta en voz alta» · «Detener la lectura» |
| Bola con la respuesta | `thumbs-up` · `thumbs-down` | «Buena respuesta» · «Mala respuesta» |
| Bola abierta desde Recientes | `chevron-left` | «Volver a Recientes» |
| Recientes | `trash-2` | «Borrar respuesta», como acción de la fila |
| Modo fiesta, en cada persona | `x` | «Quitar a Leo» |
| Personajes | `x` · `plus` | «Cerrar» · «Crear un personaje» |
| Ficha de Comunidad | `ellipsis-vertical` | «Más opciones» |
| Tu personaje | `pencil` · `trash-2` | «Editar» · «Borrar» |
| Nuevo personaje | `x` | «Cancelar» |

### Orden del foco

- El de lectura: la barra de izquierda a derecha, el título, el contenido de arriba abajo, las
  acciones y, si lo hay, el aviso breve. En la bola: barra, ayuda o pregunta, bola, veredicto y
  frase, acciones.
- `traversalIndex` solo donde el orden visual no sirve; nunca para saltar contenido.
- Al abrirse una hoja o un diálogo, el foco va a su título y no sale de ella mientras está abierta.
  Al cerrarse, vuelve al control que la abrió.
- Al borrar una fila, el foco pasa a la siguiente, o a la anterior si era la última. Con la lista
  vacía, al título.
- Con teclado físico: Tab recorre los controles, Intro y Espacio los activan y Escape cierra la hoja
  o el diálogo, como atrás.

**Anillo de foco.** Solo con foco de teclado o de mando, nunca al tocar:
- Controles sueltos: anillo de 2 `focusRing`, separado 2 del control, con el hueco transparente (no
  `bg`, que en una hoja parece un marco) y el radio del control más 4.
- Filas: anillo interior de 2 dentro de la fila, para que el grupo no lo recorte; en la primera y la
  última sigue las esquinas de 24 del grupo.
- Interruptor: el anillo va en su fila, que es la que conmuta.
- Campo: su contorno de 2 `focusRing` es el foco; no lleva otro anillo.
- Muestra de color: el anillo de selección se dibuja en `focusRing`.
- La bola: un círculo 4 por fuera del cuerpo.

### Objetivos táctiles

- 48 × 48 como mínimo en todo lo que se toca, con `minimumInteractiveComponentSize()`.
- Se dibujan más pequeños, con su zona de 48: chips de 36, muestras de color de 40 y el asa de la
  hoja (zona de 48 × 24; con TalkBack, su acción «Cerrar» se activa con doble toque).
- Las zonas de dos controles vecinos no se pisan: las muestras van a 48 de centro a centro (40 + 8).
- Los puntos de página, la racha y las etiquetas no se tocan.

### Contraste

- Texto: 4,5:1 sobre su fondo real, también pulsado, elegido o desactivado, y también con letra
  grande.
- Lo que no es texto pero hace falta para entender un control (iconos, contorno de campos e
  interruptor, ✓, anillo de foco): 3:1 frente a lo que tiene al lado.
- Lo desactivado va siempre sobre `control`: `disabled` sobre `surfaceOverlay` daría 4,19:1. Por eso
  lo desactivado no sube de tono en hojas ni diálogos.
- Un test unitario recorre los pares de esta tabla con los valores de `LuzColors`.

| Texto o pieza | Sobre | Contraste |
|---|---|---|
| `text` | `bg` · `surface` · `control` · `surfaceOverlay` · `surfaceHighest` | 18,07 · 16,36 · 14,72 · 12,98 · 11,28 |
| `text` | `selected` · `warningContainer` · `errorContainer` | 14,38 · 15,87 · 15,84 |
| `contentMedium` | `bg` · `surface` · `control` · `surfaceOverlay` · `warningContainer` | 12,82 · 11,61 · 10,44 · 9,21 · 11,26 |
| `textSecondary` | `bg` · `surface` · `control` · `surfaceOverlay` · `selected` | 8,06 · 7,30 · 6,56 · 5,79 · 6,41 |
| `disabled` | `bg` · `surface` · `control` · `warningContainer` | 5,83 · 5,28 · 4,75 · 5,12 |
| `link` | `bg` · `surface` · `control` · `surfaceOverlay` (pulsado) | 10,66 · 9,65 · 8,68 · 7,66 |
| `onAccent` | `accent` · `accentPressed` | 5,02 · 6,17 |
| `onTonal` | `tonal` · `tonalPressed` | 9,55 · 7,74 |
| `onWarning` | `warning` · `warningPressed` | 9,59 · 7,90 |
| `error` | `errorContainer` · `errorPressed` · `bg` · `control` | 7,64 · 6,76 · 8,72 · 7,10 |
| `warning`, icono y acción del aviso compacto | `warningContainer` · `surfaceOverlay` (pulsado) | 9,89 · 8,08 |
| `check` | `selected` · `surface` · `control` | 5,74 · 6,53 · 5,88 |
| `outline` | `control` · `surface` · `bg` | 3,57 · 3,97 · 4,38 |
| `accent`, pista del interruptor | `surface` · `control` | 3,57 · 3,21 |
| `focusRing` | `control` | 8,68 |
| `#E3E3E3`, botón de Google | `#131314` | 14,47 |

### Nunca solo con color

- Lo elegido: ✓ y fondo `selected`.
- El pulgar marcado: cambia la forma, no solo el color. El glifo pasa a relleno en `check`, sobre
  `selected` con `selectedOutline`. Es la excepción escrita a la ✓.
- El error de un campo: contorno de 2 y un mensaje que dice qué pasa.
- El interruptor: el círculo cambia de lado y de tamaño (24 encendido, 16 apagado).
- Un aviso: icono, un título que dice qué pasa y su acción.
- Los enlaces son botones de texto o filas con ↗; nunca un trozo de párrafo en color.

### Tamaño de letra

- Toda la letra va en sp, salvo la de los dibujos que no deben crecer: la inicial de los avatares y
  la tarjeta de compartir. Todas las alturas son mínimas (`heightIn(min = …)`), nunca `height(…)`:
  botones, filas, campos, chips, etiquetas, aviso breve y títulos crecen con su texto.
- Los botones tienen 12 de relleno vertical por encima del mínimo y su etiqueta puede ocupar dos
  líneas. Nunca se corta con «…».
- Con la escala no lineal de Android 14, al 200 % un texto de 16 sp se dibuja a unos 28 y uno de 13,
  a unos 25.

| Escala | Qué pasa |
|---|---|
| 100 % | Las pantallas como están dibujadas |
| 130 % | Nada cambia de sitio; filas, botones y huecos crecen lo que pida su texto. En 390 × 844 la bola sigue en 250 |
| 150 % o más | El selector de oráculo pasa a una línea: avatar y nombre, y «Responde como» queda solo en su nombre accesible. La acción de un título baja bajo él. Si dos botones no caben uno al lado del otro, se apilan |
| 200 % | Lo anterior, y además: hojas y diálogos desplazan su cuerpo con las acciones fijas abajo; los chips siguen en una línea que se desliza; la bola se encoge (abajo) |

**La bola cuando no cabe.** Su tamaño se calcula con lo que queda libre:

`bola = clamp(160, (alto útil − huecos) / 1,168, 250)`

- **Alto útil** es la ventana sin las barras del sistema ni el recorte de la cámara
  (`WindowInsets.safeDrawing`, arriba y abajo).
- **Huecos** es la suma de lo que va encima y debajo de la bola, medido con la letra actual. Al
  100 % en un móvil bajo (alto útil de menos de 700) es 368: barra 56, título 96, separación 8,
  leyenda 120, acciones 72 y margen inferior 16. La leyenda no baja de 120, para que quepan el
  veredicto, la etiqueta y la frase (sección 7).
- **1,168** es el alto del dibujo entre su ancho (292 / 250), porque la bola necesita sitio encima
  para saltar.
- Con un alto útil de 660 o más, la bola mide 250. En 360 × 640, el alto útil es 592 con navegación
  por gestos y la bola mide 192; con tres botones, 568 y 171.
- Si ni con 160 cabe, la bola se queda en 160 y toda la columna se desplaza.
- Los huecos se miden una vez por tamaño de ventana y de letra, con lo más alto que puede llevar
  cada uno (por ejemplo, la pregunta en tres líneas), para que la bola no se mueva al cambiar de
  estado.

### Reducir movimiento

- **Se lee el sistema.** Android no tiene un ajuste propio: «Quitar animaciones» (Accesibilidad ›
  Color y movimiento) pone a 0 la escala de animaciones, `Settings.Global.ANIMATOR_DURATION_SCALE`.
  La app la lee al arrancar y al volver a primer plano, mediante una interfaz común que implementa
  Android (ADR 0002).
- **La app tiene además su interruptor,** «Menos movimiento», con el subtítulo fijo «Sin giros ni
  saltos: la bola cambia con fundidos», para quien solo quiere menos movimiento aquí. Va en
  Opciones, en el grupo «Respuestas» (sección 7).
- `LuzMotion.reduceMotion` = escala del sistema a 0, o el interruptor encendido. En esta
  especificación, «Reducir movimiento» es este modo, no el nombre de un control.
- **La trampa:** con la escala a 0, Compose termina cada animación de golpe, así que un fundido de
  200 ms no se vería. Los fundidos que sustituyen al movimiento corren con su propia escala:
  `withContext(object : MotionDurationScale { override val scaleFactor = 1f }) { … }`. Todo lo demás
  respeta la del sistema.
- Qué cambia en cada pieza: sección 2 («Movimiento») y, en la bola, sección 6.

### Tiempos

- Nada de la app caduca salvo el aviso breve.
- Aviso breve con acción («Deshacer»): dura
  `calculateRecommendedTimeoutMillis(10_000, containsIcons = false, containsText = true, containsControls = true)`,
  de `LocalAccessibilityManager`, que en Android respeta el «Tiempo para actuar» del sistema. Sin
  acción, la base es de 4.000 ms.
- Con TalkBack activo, un aviso breve con acción no se va solo: se queda hasta que se usa o se
  descarta. Si TalkBack está activo lo dice la interfaz común de la plataforma.
- El tiempo se para mientras el aviso está pulsado o tiene el foco.

### Otro camino para cada gesto

| Gesto | Otro camino |
|---|---|
| Mantener la bola y hablar | La acción «Preguntar en voz alta» de la bola, que sirve con TalkBack y con Accesibilidad con interruptores |
| Deslizar fuera de la bola para cancelar | «Cancelar», en la barra |
| Agitar el móvil | Tocar la bola. Agitar se puede apagar con el interruptor «Agitar para preguntar» (Opciones › Voz) |
| Deslizar entre las páginas de la bola | Los botones de la barra: «Recientes», «Opciones» y «Volver a la bola» |
| Arrastrar una hoja para cerrarla | Atrás, tocar el velo o la acción «Cerrar» del asa |
| Deslizar un aviso breve | Se va solo o con atrás; su acción sigue en el foco |
| Mantener un botón de icono | Solo enseña su nombre; nada se hace únicamente manteniendo |

## 9. Componentes

Variante Luz ([ADR 0004](adr/0004-componentes-luz.md)), con los botones de radio 16 del
[ADR 0005](adr/0005-botones-radio-16.md). El lienzo muestra los componentes y todas las pantallas en
la página «Diseño».

Cada componente vive en `:core:designsystem` con el prefijo `Luz` (`LuzButton`, `LuzListItem`…) y
recibe sus textos como parámetros ([ADR 0002](adr/0002-diseno-listo-para-kmp.md)). Los valores salen
de los tokens de la sección 2 y la semántica, de la sección 8. Un estado que no aparece en su
componente no existe.

### Reglas comunes

- **Al pulsar** sube un tono (sección 2): las superficies se aclaran (`control` → `surfaceOverlay`);
  el cobalto pasa a `accentPressed`, el tonal a `tonalPressed`, el ámbar a `warningPressed`, el de
  borrar a `errorPressed` y lo elegido a `selectedPressed`. El botón de texto y los iconos sin fondo
  enseñan un fondo `surfaceOverlay`. Los controles sueltos se encogen además al 98 %; las filas, los
  grupos y los campos, no. Entra en 90 y sale en 150, `standard`.
- **Sin ripple:** `LocalRippleConfiguration provides null`. El pulsado y el foco los dibuja
  `LuzIndication`, un `IndicationNodeFactory`.
- **Al enfocar** con teclado: anillo de 2 `focusRing`, separado 2, con el hueco transparente y el
  radio del control más 4 (sección 8).
- **Desactivado:** contenido `disabled`; contenedor `control`, sin línea, sin escala y sin opacidad,
  también en hojas y diálogos. La semántica lleva `disabled()`, que ya pone
  `clickable(enabled = false)`.
- **Cargando:** un solo aspecto para todos los botones (abajo, «Cargando»).
- **Borrar:** lo que no se puede deshacer va en coral y pide confirmación en un diálogo con el botón
  de borrar. Lo que se puede deshacer se borra con un icono neutro y el aviso breve con «Deshacer».
  Nunca se borra con un botón de texto cobalto.
- **Brillo y sombras:** ninguno. Solo brilla la bola.
- **Alturas:** todas mínimas; crecen con la letra.
- **Textos:** español de España, comillas «» y frases cortas. Las etiquetas (botones, títulos,
  filas, chips, cabeceras y campos) no llevan punto final; las frases (respuestas y cuerpos de
  avisos y diálogos), sí. Un aviso breve de una sola parte va sin punto, como una etiqueta
  («Respuesta borrada»); con dos, termina en punto («Reportada. Gracias.», «Listo: la IA vuelve a
  responder.»). Un título dice una sola cosa. Ningún texto da cifras internas: cuántas respuestas
  quedan, cuotas o cómo está hecho.

### Estados

| Componente | Reposo | Pulsado | Foco | Desactivado | Cargando | Elegido o error |
|---|---|---|---|---|---|---|
| Botón principal | `accent`, línea superior `accentHighlight`, etiqueta `onAccent` | `accentPressed`, 0,98 | Anillo de radio 20 | `control`, `disabled` | Indicador `onAccent` | — |
| Botón secundario | `control` con línea `outlineVariant`, etiqueta `text` | `surfaceOverlay`, 0,98 | Anillo de radio 20 | `control` sin línea, `disabled` | Indicador `text` | — |
| Botón tonal | `tonal`, etiqueta `onTonal` | `tonalPressed`, 0,98 | Anillo de radio 20 | `control`, `disabled` | Indicador `onTonal` | — |
| Botón ámbar | `warning`, etiqueta `onWarning` | `warningPressed`, 0,98 | Anillo de radio 20 | `control`, `disabled` | Indicador `onWarning` | — |
| Botón de borrar | `errorContainer` con línea `errorOutline`, etiqueta `error` | `errorPressed`, 0,98 | Anillo de radio 20 | `control`, `disabled` | Indicador `error` | — |
| Botón de texto | Sin fondo, etiqueta `link`; en el aviso compacto, `warning` | Fondo `surfaceOverlay`, 0,98 | Anillo de radio 20 | Etiqueta `disabled` | No tiene | — |
| Botón de icono | `control` con línea `outlineVariant`, icono `contentMedium` | `surfaceOverlay`, 0,98 | Anillo de radio 20 | `control`, icono `disabled` | No tiene | Pulgar marcado: `selected`, línea `selectedOutline` y glifo relleno `check` |
| Icono de barra | Sin fondo, icono `contentMedium` | Fondo `surfaceOverlay` | Anillo de radio 20 | Icono `disabled` | No tiene | — |
| Chip de filtro | `control` con línea `outlineVariant`, texto `contentMedium` | `surfaceOverlay`, 0,98 | Anillo de radio 16 | `control` sin línea, `disabled` | — | `selected`, línea `selectedOutline`, ✓ 16 `check`, texto `text` |
| Chip de nombre | `control` con línea `outlineVariant`, texto `text`, icono `contentMedium`, ✕ `textSecondary` | `surfaceOverlay`, 0,98 | Anillo de radio 16 | — | — | — |
| Selector de oráculo | Como el secundario | `surfaceOverlay`, 0,98 | Anillo de radio 20 | — | — | — |
| Fila | Transparente sobre su grupo | `surfaceOverlay`; en un grupo de una hoja, `surfaceHighest`. Sin escala | Anillo interior | Texto e icono `disabled` | Filas esqueleto; si espera, el indicador de 18 en su final | `selected` y ✓ 22 `check`; pulsada, `selectedPressed` |
| Interruptor | Encendido: pista `accent`, círculo 24 `onAccent`. Apagado: pista `control`, contorno 2 `outline`, círculo 16 `outline` | El círculo pasa a 28 | En su fila | Pista `control`, contorno 2 `handle`, círculo `disabled` | — | — |
| Campo | `control`, contorno 1 `outline` | — | Contorno 2 `focusRing` | Contorno 1 `handle`, texto `disabled` | — | Error: contorno 2 `error` y mensaje `error` |
| Muestra de color | Su color | 0,98 | Su anillo, en `focusRing` | — | — | Anillo 2 `check` a 3 y ✓ 18 `ink` |
| Losa de accesorio | `control` | `surfaceOverlay`, 0,98 | Anillo de radio 20 | — | — | `selected`, línea `selectedOutline`, ✓ 14 `check` en la esquina |
| Botón de Google | `#131314`, contorno 1 `#8E918F`, etiqueta `#E3E3E3` | Solo 0,98: sus colores no cambian | Anillo de radio 20 | — | Indicador `#E3E3E3` | — |

«—» quiere decir que ese estado no existe en ese componente. El anillo de foco solo sale con teclado
o mando (sección 8).

### Botones

| Botón | Alto | Relleno lateral | Ancho | Uso |
|---|---|---|---|---|
| Principal | 52 | 20 | Su texto, o todo el ancho de su columna | La acción principal. Uno por pantalla, hoja o diálogo |
| Secundario | 52 | 20 | Su texto o todo el ancho | Acciones de apoyo |
| Tonal | 52 | 20 | Su texto | Solo al final de una fila, como «Obtener» en la de Premium |
| Ámbar | 52 | 20 | Su texto | Solo dentro de un aviso que pide atención |
| Borrar | 52 | 20 | Todo el ancho | Confirmar algo que no se puede deshacer, en su diálogo |
| Texto | 48 | 12 | Su texto o todo el ancho | Salidas, «Cancelar», «Ahora no» y la acción de un título |
| Icono | 48; 52 en la fila de la respuesta | — | Cuadrado | Acciones con un icono claro; siempre con nombre accesible |
| Icono de barra | 48 | — | Cuadrado | Barra superior, sin fondo |

- Etiqueta en `button`, centrada, con 12 de relleno arriba y abajo: con una línea manda el alto
  mínimo y con dos el botón crece. Nunca se corta con «…».
- El botón de icono lleva un icono de 22.
- **Con icono:** de 18, al principio y a 8 de la etiqueta. El relleno del lado del icono baja 4: 16
  en los de relleno, 8 en los de texto. Ejemplos: «Compartir» (`share-2`) y «Hazte Premium»
  (`crown`, de texto).
- **Cargando:**
  - El contenedor y la etiqueta no cambian. Delante de la etiqueta, a 8, aparece el indicador de 18
    con trazo de 2 y pista al 30 %, en el color de la etiqueta. Si el botón tiene icono, el
    indicador ocupa su sitio.
  - Los toques se ignoran, pero el botón no se desactiva: sigue con el foco y lleva
    `stateDescription` «Cargando».
  - El ancho no cambia. A todo el ancho, el indicador y la etiqueta se centran juntos. Un botón sin
    icono que se ajusta a su texto y puede cargar (los ámbar de los avisos) reserva desde el reposo
    los 26 del indicador (18 + 8), repartidos a los dos lados de la etiqueta.
  - Nunca «Un momento…» en lugar de la etiqueta, ni un contenedor desactivado mientras carga.
- **Contenido:** un verbo o la acción exacta, en dos o tres palabras: «Suscribirme», «Permitir
  micrófono». El botón que confirma repite el verbo del título del diálogo: «¿Borrar todas las
  respuestas?» → «Borrar todo», nunca «Aceptar».
- **Sí:** un solo botón principal y «Cancelar» o «Ahora no» como botón de texto. **No:** dos botones
  cobalto, ni «Cancelar» con relleno junto al principal.

**Botón de avatar del modo fiesta.** 52 × 52, radio 16, `control` con línea `outlineVariant`, y en
el centro el avatar de 32 con su anillo de 2 `ring`. Pulsado, `surfaceOverlay` y 0,98. Se llama
«Responde como Vidente» y abre la lista para elegir quién responde, como el selector. No lleva
texto, así que la letra grande no lo cambia.

**Pulgares.** Dos botones de icono de 52, «Buena respuesta» (`thumbs-up`) y «Mala respuesta»
(`thumbs-down`), a 12 de «Compartir». Marcado: fondo `selected`, línea `selectedOutline` y el glifo
relleno en `check`. Son `toggleable` con `Role.Checkbox`. Marcar el de abajo abre la hoja para
reportar (sección 7).

**Continuar con Google.** Sigue la guía de marca de Google (consultada el 20 nov 2026):
- 52 de alto y radio 16, a todo el ancho; fondo `#131314` y contorno interior de 1 `#8E918F`.
- La «G» de 20, con sus colores, y a 10, como pide la guía, la etiqueta «Continuar con Google» en
  Google Sans Medium 16/20, color `#E3E3E3`. El grupo va centrado.
- La letra es la que pide la guía, Google Sans Medium, no Roboto. Su tamaño de 14 es para el botón
  de 40 de alto de la guía: con 52, se usa el 16 de los demás botones.
- Pulsado: solo la escala de 0,98. Cargando: el indicador, de `#E3E3E3`, ocupa el sitio de la «G».

### Selección

**Chip de filtro** (Personajes: «Todos», «Originales», «Comunidad», «Tuyos»):
- 36 de alto con objetivo de 48, radio 12, letra `chip`.
- Relleno de 12 a los lados. Elegido: 8 delante, la ✓ de 16, 6 hasta el texto y 12 detrás.
- Van en un `LazyRow` con 16 de margen a los lados y 8 entre chips, que se desliza hasta el borde.
  Nunca pasan a dos líneas.
- Siempre hay uno elegido: es un grupo de opciones (`Role.RadioButton`).
- Una palabra por chip.

**Chip de nombre** (el chip «Modo fiesta» de la bola): 36 de alto, radio 12, `control` con línea
`outlineVariant`, el texto en `chip` y una ✕ de 16 en `textSecondary` al final. Relleno de 12
delante y 8 detrás. Puede llevar delante un icono de 16 en `contentMedium`, a 6 del texto; entonces
el relleno de delante baja a 8, como en el chip de filtro elegido. Todo el chip es un botón y se
llama por lo que hace: «Salir del modo fiesta».

**Interruptor:**
- Pista de 52 × 32, como el de Material 3; se usa el `Switch` de M3 con los colores de la tabla de
  estados (`SwitchDefaults.colors(…)`), sin iconos dentro.
- Toda la fila conmuta: el `Switch` va con `onCheckedChange = null` (sección 8).
- El círculo mide 24 encendido y 16 apagado, y 28 mientras se pulsa. Se mueve en 150 con `standard`.
- Vibra con `TOGGLE_ON` o `TOGGLE_OFF`.
- Desactivado encendido: el círculo de 24 en `disabled`, a la derecha, sobre la pista `control` con
  contorno `handle`. Desactivado apagado: igual, con el círculo de 16 a la izquierda.
- El subtítulo de la fila no cambia con el estado.

**Muestra de color** (Nuevo personaje):
- Círculo de 40 con objetivo de 48, a 8 de la siguiente, en un `LazyRow` con 16 a los lados y 6
  arriba y abajo para que quepa el anillo. Se desliza hasta el borde.
- Elegida: anillo de 2 `check` separado 3, con el hueco transparente, y ✓ de 18 en `ink` en el
  centro.
- Con foco, ese mismo anillo se dibuja en `focusRing`, esté elegida o no.
- Cada muestra tiene un nombre propio en los tres idiomas, que es su nombre accesible; nunca
  «Color».
- La paleta y sus nombres están pendientes con los colores de los personajes (sección 2). Un test
  exige ΔE00 de 18 o más frente a la interfaz y 3:1 de la ✓ sobre cada color.

**Losa de accesorio** (Nuevo personaje):
- 52 × 52, radio 16, `control`, con el avatar de 36 que lleva ese accesorio; a 8 de la siguiente.
- Elegida: `selected`, línea `selectedOutline` y ✓ de 14 en `check`, a 4 de la esquina superior
  derecha.
- Se llama como el objeto («Sombrero de mago»), no como un personaje.

**Selector de oráculo** («Responde como · Vidente», en las acciones de la bola):
- 52 de alto, radio 16, `control` con línea `outlineVariant`.
- A 8 del borde izquierdo, el avatar de 32 con su anillo de 2 `ring`. A 8, dos líneas: «Responde
  como» en `tag` y `textSecondary`, y el nombre en `button` y `text`. A 8, la flecha `chevron-down`
  de 16 en `textSecondary`, y 12 hasta el borde derecho.
- Con la letra al 150 % o más, una línea: avatar, nombre y flecha.
- Abre la lista para elegir quién responde.

### Listas

**Grupo:**
- Radio 24, sin contorno, a 16 de los bordes de la pantalla. `surface` sobre el fondo; `control`
  dentro de una hoja o un diálogo.
- Recorta las esquinas de la primera y la última fila.
- Entre dos grupos, 12. Un grupo con contenido libre, como «Qué se envía a Gemini, de Google», lleva
  16 de relleno y 12 entre sus piezas.

**Cabecera de grupo:** `sectionLabel` en `textSecondary`, a 20 del borde de la pantalla, 20 por
encima y 8 hasta el grupo. Es un encabezado (`heading()`). Una o dos palabras, sin punto.

**Fila de lista: `LuzListItem`.** Parámetros: `leading`, `headline`, `supporting`, `trailing`,
`onClick`, `selected`, `enabled` y `destructive`.

| Pieza | Valores |
|---|---|
| Alto mínimo | 56 con una línea; 72 con avatar de 40. Con dos líneas mide 66, lo que da su contenido |
| Relleno | 12 arriba y abajo y 16 a los lados; 4 a la derecha si termina en un botón de icono de 48, para que su icono quede donde los demás finales. El botón no hace crecer la fila: en una de 56 le quedan 4 arriba y abajo |
| Separación | 16 entre el inicio, el texto y el final |
| Inicio | Un icono de 22 en una caja de 24, en `contentMedium` (en una de 40 si las demás filas del grupo llevan avatar, como «Borrar todo»); un avatar de 40; el avatar de inicial de 32 o de 40; el círculo de 32 de la fila que añade; o nada |
| Título | `rowTitle` en `text`. Se parte en líneas; nunca se corta |
| Apoyo | `supporting` en `textSecondary`, justo debajo |
| Final | › o ↗ de 18 en `textSecondary`; un interruptor; un botón tonal; un botón de icono de 48; la ✓ de 22 en `check`; mientras espera, el indicador de 18 en `textSecondary`; o nada |
| Separador | 1 de `outlineVariant`, desde donde empieza el texto hasta el borde derecho del grupo: a 16 sin inicio, a 56 con icono, a 64 con avatar de 32 y a 72 con uno de 40. No hay tras la última fila |
| Alineación | Inicio y final centrados en vertical; con tres líneas o más, arriba, a 12 del borde |

Variantes:
- **Abre una pantalla:** termina en ›. **Sale de la app:** termina en ↗.
- **Ajuste:** termina en un interruptor y toda la fila conmuta. El apoyo explica el ajuste y no
  cambia con el estado.
- **Elige:** la elegida lleva fondo `selected` y la ✓; las demás, nada.
- **Actúa al tocar** (antes «Filas de acción», como los motivos de «¿Qué tiene de malo esta
  respuesta?»): sin inicio ni final y sin marca de elegido; tocar ya actúa. Título en `rowTitle`: el
  lienzo dibuja estas filas y las de Idioma en Inter 500, y pasan a 600.
- **Borra:** título e icono en `error`. Abre siempre un diálogo, como «Eliminar cuenta».
- **Con botón tonal:** como «Obtener» en la fila de Premium; la fila crece a 76 (12 + 52 + 12).
- **Añade** («Añadir persona»): en el inicio, + de 18 en `link` dentro de un círculo de 32 con línea
  de 1 `link` al 40 %, y el título en `link`, sin final. Al tocarla se convierte en un campo
  (sección 7).
- **Espera** (la fila «Entrar», `LP66EntrarCargando`): mientras se completa lo que abrió, el final
  se cambia por el indicador de 18 en `textSecondary`. Nada más cambia ni se mueve, y la fila no se
  puede volver a tocar. TalkBack dice lo que pasa: «Entrando con Google».
- **Recientes:** avatar arriba y cuatro líneas, a 2 entre sí y a 4 antes de la última: la respuesta
  en `rowTitle`; la frase en `oracleSmall` y `contentMedium`; la pregunta, entre «», en `supporting`
  y `textSecondary`; y «Hace 5 min · Vidente · IA» en `caption` y `textSecondary`. Al final, arriba,
  la papelera de 48 «Borrar respuesta».

- Pulsada: `surfaceOverlay`, o `surfaceHighest` en un grupo de una hoja; sin escala (sección 2,
  «Tono»).
- **Contenido:** título corto, sin punto. El apoyo, una frase corta que no repite el título.
- **Sí:** toda la fila es un solo toque. **No:** un objetivo pequeño dentro de la fila que hace otra
  cosa, salvo el botón de icono de 48 del final.

**Elegir personaje:**
- Filas de 72 en un grupo: avatar de 40, el nombre en `rowTitle` y una muestra de cómo responde,
  entre «», en `oracleXSmall` y `contentMedium`.
- La elegida: fondo `selected`, anillo de 2 `ring` en su avatar y ✓ de 22. Las demás, sin anillo.
- En Comunidad, una tercera línea con el autor, «De @…», en `caption` y `textSecondary`, en una
  línea con «…».
- **Pendiente de decidir** qué hace el toque:
  - (a) Tocar la fila elige y cierra. Un botón de icono de 48 al final, `info`, «Ver ficha de
    Bruja», abre la ficha. La fila es `selectable(role = Role.RadioButton)`.
  - (b) Tocar la fila abre la ficha y la ✓ marca el actual. La fila es un botón con `onClickLabel`
    «ver ficha» y la elegida lleva `stateDescription` «Elegido».

**Filas esqueleto** (Personajes y Comunidad, mientras cargan):
- Filas de 72 en su grupo: un círculo de 40 y dos barras de 12 de alto, redondas, del 60 % y el 40 %
  del ancho del texto, separadas 8. Todo en `control`.
- Tantas como llenen el primer grupo de la pantalla.
- Laten de 0,6 a 1 de opacidad en 1,2 s, con `smoothstep`. Con «Reducir movimiento», quietas.
- No se leen una a una: el grupo dice «Cargando personajes».

**Estado vacío** (Recientes sin respuestas, «Tuyos» sin personajes):
- Centrado, con 320 de ancho como máximo y 40 de margen a los lados.
- Una losa de 72, radio 24 y `control`, con un icono de 32 en `contentMedium`.
- A 20, el título en `sheetTitle`; a 12, el texto en `bodySmall` y `textSecondary`; a 24, un botón
  secundario que lleva a llenarlo.
- El título dice qué falta («Aún no hay respuestas»), el texto cómo llenarlo y el botón lleva allí.

### Campos

**Campo de texto:**

| Pieza | Valores |
|---|---|
| Etiqueta | `sectionLabel` en `textSecondary`, encima, a 4 del borde del campo y a 6 de él |
| Contenedor | 56 de alto mínimo, radio 16, `control` y contorno de 1 `outline` |
| Texto | `body` en `text`, con 16 a los lados y centrado en vertical |
| Marcador | `body` en `disabled`. Solo un ejemplo; nunca repite la etiqueta |
| Ayuda y contador | `caption` en `textSecondary`, a 6 debajo y a 4 de los bordes: la ayuda a la izquierda y el contador («14/16») a la derecha |
| Cursor y selección | Cursor de 2 en `cursor`; selección en `textSelection` y asas en `cursor` (`TextSelectionColors`) |

- **Con foco:** contorno de 2 `focusRing`.
- **Error:** contorno de 2 `error` y el mensaje en `caption` y `error`, en el sitio de la ayuda;
  vibra con `REJECT`. Aparece al guardar y se va al corregirlo. El mensaje dice qué hacer: «Ponle un
  nombre».
- **Desactivado:** contorno de 1 `handle` y el texto en `disabled`.
- **Límite:** el campo no deja escribir más del máximo y el contador lo enseña.
- **Varias líneas** («¿Cómo responde?»): de 3 a 6 líneas, de 101 a 170 de alto, con 16 arriba y
  abajo. Pasadas 6, se desplaza dentro. Lleva siempre contador.
- **Teclado:** `capitalization = Sentences`, o `Words` en los nombres; `imeAction = Next` entre
  campos y `Done` en el último, que cierra el teclado.
- Preguntar es solo por voz: ningún campo sirve para escribir la pregunta.

**Campo de fecha: `DateField`** (fecha de nacimiento en «¿Quién pregunta?»):
- Tres casillas de 56 separadas 8: el día y el mes de 72 de ancho; el año, el resto.
- Se llaman «Día», «Mes» y «Año»; encima va la etiqueta «Fecha de nacimiento».
- El orden es un parámetro que da la pantalla según el idioma: día, mes y año en español y
  portugués; mes, día y año en inglés de Estados Unidos.
- Teclado numérico (`KeyboardType.Number`). Con 2, 2 y 4 cifras salta a la siguiente casilla; borrar
  en una vacía vuelve a la anterior.
- Marcadores «DD», «MM» y «AAAA» en `disabled`. Debajo, como ayuda, la fecha en letra: «12 de marzo
  de 1990».
- Un solo error para las tres, «Fecha no válida», con las tres casillas en contorno `error`.
- No se usa el `DatePicker` de M3: no hay calendario.

### Barras, hojas y avisos

**Barra superior y título de pantalla:**
- La barra mide 56, más la barra de estado encima (`WindowInsets.statusBars`). Tiene 4 de relleno a
  los lados y dos zonas de 104, a izquierda y derecha, con hasta dos botones de icono de 48, a 4 uno
  de otro. En el centro van los puntos de página o el título pequeño.
- En reposo es transparente sobre `bg`. Cuando el contenido pasa por debajo, toma `bg` opaco y una
  línea inferior de 1 `outlineVariant`, con un fundido de 150.
- En las pantallas con «Atrás», el título pequeño (`barTitle`, una línea, centrado) entra con ese
  fundido cuando el título grande queda bajo la barra. Es el comportamiento de `exitUntilCollapsed`
  de M3, hecho a mano.
- El título de pantalla va en el contenido y se desplaza con él: 64 de alto mínimo, `screenTitle`, a
  20 del borde, con 8 de relleno abajo y a la derecha. A su derecha puede llevar un botón de texto;
  con la letra al 150 % o más, ese botón baja bajo el título.
- Solo el título grande es encabezado; el pequeño no se lee (sección 8).

**Paginador** (las tres páginas de la bola):
- Tres puntos de 6 separados 6, en el centro de la barra: `text` el de la página que se ve y
  `pageDot` los demás. Cambian de color en 150.
- Los puntos no se tocan. Las páginas se pasan deslizando o con los botones de la barra.
- Cada página anuncia su nombre con `paneTitle`; los puntos dicen «Página 2 de 3».

**Hoja:**
- Se construye sobre `ModalBottomSheet` de M3, con
  `rememberModalBottomSheetState(skipPartiallyExpanded = true)`: abre siempre entera.
- Radio 32 arriba, `surface` y una línea superior de 1 `outlineVariant`. Detrás, el velo `scrim`.
- 16 a los lados y, abajo, 16 por encima de la barra de navegación. Ancho máximo 640, centrada.
- Alto: el de su contenido, como mucho la ventana menos la barra de estado y 48. Si no cabe, el
  contenido se desplaza y el botón principal se queda fijo abajo, con una línea de 1
  `outlineVariant` encima mientras haya contenido debajo.
- Título en `sheetTitle`, a 20 del borde. Premium y la ficha de un personaje lo llevan en
  `screenTitle`, centrado.
- Los grupos de dentro van en `control` y sus filas, pulsadas, en `surfaceHighest`.

| | Con asa | Sin asa («¿Quién pregunta?») |
|---|---|---|
| Arriba | Asa de 36 × 4, `handle`, a 8 del borde, en una zona de 48 × 24; el contenido empieza 16 más abajo | El contenido empieza a 24 del borde |
| Se cierra | Arrastrando, tocando el velo, con atrás o con la acción «Cerrar» del asa | Solo con sus botones: no se arrastra (`sheetGesturesEnabled = false`), el velo no la cierra y atrás hace lo mismo que «Ahora no» |

**Diálogo:**
- Se construye sobre `Dialog` con `usePlatformDefaultWidth = false`.
- De 280 a 400 de ancho, a 24 de los bordes de la pantalla. Radio 28, `surface` y contorno de 1
  `outlineVariant`. Detrás, el velo `scrim`.
- Relleno de 24 arriba, 20 a los lados y 16 abajo; 12 entre las piezas.
- Título en `sheetTitle` y cuerpo en `bodySmall` y `contentMedium`, los dos con 4 más de margen (a
  24 del borde).
- El cuerpo se desplaza si no cabe: el diálogo mide como mucho la ventana menos 96.
- Acciones apiladas, a 24 del cuerpo (12 + 12): arriba el botón de 52, principal o de borrar, a todo
  el ancho; si hace falta otro de 52, secundario, va encima, a 8 (como «Gestionar en Google Play» en
  «¿Eliminar tu cuenta?»); debajo, los de texto de 48, separados 4.
- Tocar el velo o atrás hacen lo que «Cancelar». Con un campo dentro, `imePadding()`.
- **Contenido:** el título pregunta por la acción («¿Borrar todas las respuestas?»); el cuerpo dice
  qué se pierde y si se puede deshacer, en dos o tres frases cortas; el botón repite el verbo.

**Aviso breve:**
- Se construye con `SnackbarHost` y el contenido de Luz.
- 56 de alto mínimo, radio 16, `surfaceOverlay` y contorno de 1 `outlineVariant`, sin sombra.
- El texto en `emphasis` y `text`, dos líneas como mucho, con 16 a la izquierda. La acción es un
  botón de texto de 48 dentro, con 4 hasta el borde.
- Va 12 por encima de las acciones de la bola; en las demás pantallas, 16 por encima de la barra de
  navegación o del botón fijo de abajo. 16 a los lados; ancho máximo 560.
- Uno cada vez: uno nuevo sustituye al que hay.
- Se descarta deslizándolo a un lado. Su duración, la de la sección 8: el tiempo se para mientras se
  toca y, con TalkBack, el que tiene acción no se va solo.
- **Contenido:** qué ha pasado, en una parte o en dos («Respuesta borrada», «Reportada. Gracias.»),
  y como acción un verbo («Deshacer»). Sin icono.

**Aviso que pide atención:**
- Un grupo de radio 24 en `warningContainer`, con contorno de 1 `warningOutline` y 16 de relleno.
- El icono de 24 en `warning`, sin caja; a 16, el texto, que empieza a 72 del borde de la pantalla,
  como el de las filas.
- Título en `noticeTitle` y `text`; a 4, el cuerpo en `supporting` y `contentMedium`; a 12, el botón
  ámbar, que se ajusta a su texto; si hace falta, a 8, una nota en `caption` y `contentMedium`.
- Va debajo del ajuste al que afecta, o en el sitio de lo que ha fallado (como la fila «Entrar» en
  `LP67EntrarError`), a 12 del grupo siguiente.
- **Contenido:** el título dice qué pasa, el cuerpo qué se pierde y el botón lo arregla: «Las
  notificaciones están desactivadas para esta app», «Sin ellas no te llega el veredicto del día.»,
  «Permitir notificaciones».
- Sin una acción no hay ámbar: el aviso pasa a texto en `textSecondary`.

**Aviso compacto** (el que pide atención en la bola, sección 7):
- 56 de alto mínimo, radio 16, `warningContainer` y contorno de 1 `warningOutline`. A 16 de los
  bordes de la pantalla.
- El icono de 24 en `warning`, a 16 del borde del aviso; a 16, el texto en `emphasis` y `text`, en
  dos líneas como mucho y equilibradas (`LineBreak.Heading`), que empieza a 72 del borde de la
  pantalla, como en el aviso que pide atención.
- La acción es un botón de texto de 48 dentro, con la etiqueta en `warning` y 4 hasta el borde.
- Mientras carga lo que pide su acción, el icono se cambia por el indicador de 24 en `warning` y la
  acción se desactiva (`LP49Recompensa`).
- Va en la leyenda, donde la racha; con una respuesta, 12 por encima de las acciones, donde el aviso
  breve.
- Es una región viva educada (`liveRegion = Polite`) y crece con la letra: nunca se corta.
- Sin una acción no hay ámbar ni contenedor: queda el icono de 16 y, a 8, el texto en `emphasis`,
  los dos en `textSecondary` (`LP44SinVoz`).

**Etiqueta emergente** (los botones de icono, al mantenerlos):
- Es el `TooltipBox` de M3 con un `PlainTooltip` propio. Dice el nombre accesible del botón.
- `emphasisSmall` en `text`, sobre `surfaceOverlay`, radio 12, 8 de relleno a los lados y 6 arriba y
  abajo.
- Sale a 8 del botón, encima o debajo según el sitio, y se va 1,5 s después de soltar.
- TalkBack no la lee: el botón ya tiene ese nombre.

### Contenido

**Etiqueta:** 24 de alto, redonda del todo y sin borde. `tag` en `contentMedium`, sobre `control`.
Puede llevar delante un icono de 14 a 6 del texto. Relleno de 8 en el lado del icono y 12 en el del
texto. No se toca. Una o dos palabras: «Tienes Premium».

**Racha:** una línea en `emphasisSmall` y `textSecondary`, con la chispa (`sparkle`) de 14 delante,
a 6: «3 días seguidos». Va bajo la bola, en reposo y en el modo fiesta. No es un contador en la
barra.

**Veredicto y frase del oráculo** (bajo la bola, centrados, sin bocadillo):
- El veredicto en `verdict` y `text` (por ejemplo, «Sí, sin duda.»). Así se lee fuera del triángulo
  y sigue el tamaño de letra.
- A 8, la etiqueta en `tag` y en el color `label` del personaje: el ▲ de 10 dibujado (no es un
  carácter), a 8, y «Vidente añade · IA».
- A 8, la frase en `oracleLarge` y `text`, con las líneas equilibradas (`LineBreak.Heading`).
- Con «Respuestas más largas», la frase pasa a `oracleMedium` y la leyenda crece hacia abajo sin
  mover la bola; si no cabe, la frase se desplaza hasta las acciones, con un fundido de 24
  (sección 7).
- Una respuesta reportada cambia la etiqueta y la frase por la bandera de 16 y
  «Respuesta reportada», en `emphasis` y `textSecondary`; el veredicto se queda.

**Avatares:**
- **De personaje:** círculo de 28, 32, 36, 40, 64 u 88. La bola grande sale por la esquina inferior,
  inclinada unos 18°, con el disco blanco descentrado como cara. El dibujo de cada uno está
  pendiente (sección 7).
- Lleva un anillo de 2 `ring` solo cuando marca al personaje actual: selector, botón de avatar, fila
  elegida y ficha. Nunca un anillo vacío.
- **De inicial:** círculo `control` con contorno interior de 1 `outlineVariant`, y la inicial en
  Outfit 700 y `contentMedium`, a 0,42 del tamaño (27 en uno de 64). La letra se calcula en dp, así
  que no crece con el tamaño de letra.
- Junto a un nombre, el avatar no se lee.

**Indicador circular:**
- 18 dentro de un botón o al final de una fila; 24 suelto o en el aviso compacto. Trazo de 2 con
  extremos redondos.
- Una pista entera al 30 % del color del indicador y un arco de 90° que da una vuelta por segundo.
- Color: el del contenido de su contenedor (`onAccent`, `text`, `onTonal`, `onWarning` o `error`);
  al final de una fila, `textSecondary`; en el aviso compacto, `warning`; suelto, `contentMedium`.
- Solo indeterminado: nunca un porcentaje.

### La bola: `OracleBall`

El componente dibuja la bola de las secciones 1 a 6
([ADR 0001](adr/0001-bola-dibujada-con-compose.md)).

| Parámetro | Valores |
|---|---|
| `state` | Reposo, escuchando (con el nivel del micrófono), pensando, respuesta (con el veredicto y el texto de la ventana; quieta si se abre desde Recientes), sin respuesta, no te he oído, delicada, sorteo (con el nombre en la ventana), dormida, sin conexión, esperando y ambiente (reloj). Cómo es cada uno, en las secciones 6 y 7 |
| `oracle` | Luz, accesorio y mirada del personaje, con sus `OracleColors` |
| `size` | El ancho: de 160 a 250 en el móvil (sección 8); en el reloj, 112, 104 sin respuesta y 88 sin el móvil (sección 7). El alto es el ancho × 1,168, por el sitio para saltar |
| `reduceMotion` | El de `LuzMotion` |

- **Tocar:** pide una respuesta sin pregunta, con la vibración `CLOCK_TICK`.
- **Mantener** el tiempo de pulsación larga del sistema (`longPressTimeoutMillis`): empieza a
  escuchar, vibra con `LONG_PRESS` y escucha mientras se mantiene. Al soltar, pregunta.
- Un toque que empieza en la bola no pasa de página: la bola se queda con el gesto.
- **Agitar** el móvil lo detecta `:core:motion` y hace lo mismo que tocar, salvo con el interruptor
  «Agitar para preguntar» apagado.
- **Semántica:** la de la sección 8, con la acción «Preguntar en voz alta».

### Fuera de la app

**Tarjeta de compartir:**
- 360 × 450 dp en «Publicación» y 360 × 640 dp en «Historia», que se exportan a 1080 × 1350 y
  1080 × 1920 px (× 3) en PNG. Se dibuja aparte, con la escala de letra a 1, para que la letra
  grande no rompa la composición.
- Contenido y colores, en la sección 7; el fondo y la tinta por personaje están pendientes
  (`shareBackground` y `shareInk`, sección 2).
- Se comparte con la hoja del sistema (`ACTION_SEND`, `image/png` y un URI de `FileProvider` con
  permiso de lectura).
- En la app, su imagen lleva como descripción el texto entero de la tarjeta.
- Mientras se genera, «Compartir» está en su estado de carga.

**Widget:**
- Con Glance. Un tamaño de 2 × 2, 156 × 156, con el radio del sistema
  (`system_app_widget_background_radius`; antes de Android 12, 16).
- Fondo `surface` con una línea de 1 `outlineVariant`, como forma XML.
- La bola va como imagen: Glance no dibuja con el Canvas de Compose, así que se pinta un mapa de
  bits con el dibujo de `OracleBall` al tamaño del widget. Uno lleva la bola en reposo y el otro la
  ventana con la última respuesta.
- Sin texto. La imagen lleva su descripción: «Bola mágica» o «La Vidente responde: sí, sin duda».
- Tocarlo abre la app en la bola.

**Notificación:**
- Icono pequeño: el glifo de la bola, de 24, de un solo color (blanco sobre transparente), como pide
  Android.
- Icono grande: el avatar del personaje.
- El texto lo da la sección 7.

### Reloj

Wear OS con Compose Material 3 para Wear, que ya usa la app del reloj. Se prueba en pantallas de
192, 227 y 240.

| Pieza | Componente | Valores |
|---|---|---|
| Marco | `AppScaffold` y `ScreenScaffold`, con `TimeText` arriba | Fondo `watchBg`, negro, como pide Wear OS para las pantallas OLED |
| Lista | `TransformingLazyColumn` con `ListHeader` y `ScrollIndicator` | La corona o el bisel la desplazan. Márgenes en porcentaje del ancho: 5,2 %, unos 12 en 227. La opción que llega al borde se encoge y se apaga |
| Cabecera | `ListHeader` | `chip` en `text` |
| Opción | `Button` de 52, en píldora, dentro de un `selectableGroup()` y con `Role.RadioButton` | `emphasis` en `text`. Elegida: `selected`, línea `selectedOutline` y ✓ de 18 al final de la etiqueta. Los nombres de los personajes, en `text` |
| Acción principal | `Button` de 52, en píldora | 160 de ancho, en `accent`, bajo la ventana. La acción de la pantalla (sección 7) |
| La bola | `OracleBall` | 112; 104 sin respuesta y 88 sin el móvil. En la respuesta, solo su ventana, de 120 (sección 7) |
| Aviso | Texto en `textSecondary` si no tiene acción; con acción, un `Button` ámbar | — |

- La letra mínima es 12 sp y sigue el tamaño de letra del reloj.
- El ámbar solo va con una acción, igual que en el móvil.

### Borde a borde, barras del sistema y teclado

Con `targetSdk` 35 o más, Android dibuja la app detrás de sus barras. La app llama a
`enableEdgeToEdge()`, con las barras transparentes e iconos claros, y cada superficie aparta lo
suyo:

| Superficie | Qué aparta |
|---|---|
| Barra superior | La barra de estado (`statusBars`) y, en horizontal, el recorte de la cámara (`displayCutout`) |
| Listas que se desplazan | La barra de navegación, como relleno inferior del `LazyColumn` más 16: la última fila nunca queda debajo |
| Acciones de la bola | 20 más la barra de navegación (`navigationBars`) |
| Aviso breve | 12 sobre las acciones de la bola; en otras pantallas, 16 sobre la barra de navegación |
| Hoja | Abajo, 16 más la barra de navegación; arriba, no pasa de la barra de estado |
| Diálogo | Se centra en lo que queda libre (`safeDrawing`) |
| La bola | Su tamaño sale del alto útil, sin las barras (sección 8) |

- En el lienzo, las pantallas llevan dibujadas sus barras: la de estado de 24 arriba, con la hora a
  la izquierda y los iconos a la derecha en `text`; la de navegación por gestos de 24 abajo, con su
  píldora de 108 × 4 en `text` al 60 %. Con tres botones mide 48.
- **Teclado:**
  - `windowSoftInputMode="adjustResize"` en la actividad.
  - Los formularios llevan `imePadding()` y se desplazan. El campo con foco se trae a la vista con
    `BringIntoViewRequester`.
  - La acción principal siempre se ve: en una hoja, su botón queda fijo encima del teclado y el
    contenido se desplaza por detrás; en una pantalla completa, como Nuevo personaje, «Guardar» va
    en la barra.

### Carga

| Qué espera | Cómo se ve |
|---|---|
| Una acción de un botón: comprar, «Continuar con Google», ver un anuncio en Respuestas con IA, borrar la cuenta | El estado «Cargando» del botón |
| Entrar desde la fila «Entrar» de Opciones | El indicador de 18 en lugar del › (`LP66EntrarCargando`) |
| Un anuncio desde el aviso compacto de la bola | El indicador de 24 en lugar del icono y «Cargando un anuncio…» (`LP49Recompensa`) |
| El precio de Premium | «Suscribirme» en su estado «Cargando» y, debajo, «Buscando el precio en Google Play…» |
| Una lista (Personajes, Comunidad) | Filas esqueleto |
| La respuesta de la bola | La ventana con el líquido (sección 6). TalkBack dice «Pensando…» y, a los 6 s, «Sin respuesta» |
| Algo largo con la bola a la vista | La animación «Esperando» (sección 6) |
| La imagen para compartir | «Compartir» en su estado «Cargando» |

- Nunca un indicador a pantalla completa ni un velo que bloquee la pantalla mientras carga.

### Qué no se construye

- **Menús desplegables:** las acciones de un elemento van en las filas de una hoja. El ⋮
  «Más opciones» de la ficha de Comunidad abre una hoja con dos filas, «Reportar personaje» y
  «Bloquear a @…», no un menú.
- **Insignias:** ni números ni puntos sobre los iconos.
- **Control segmentado:** para filtrar se usan los chips.
- **Deslizar para recargar:** un fallo se arregla con «Reintentar» en su aviso.
- **Progreso lineal:** solo el indicador circular.
- **Selector de fecha con calendario:** la fecha se escribe en el campo de fecha.
- **Tarjetas altas:** filas compactas en grupos, como en Grok.
