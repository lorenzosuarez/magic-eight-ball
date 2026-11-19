# Rediseño de la bola: especificación

Estado: propuesta B aprobada el 18 nov 2026 y componentes de la variante Luz elegidos el 19 nov 2026
([ADR 0004](adr/0004-componentes-luz.md)). Los valores salen del lienzo de diseño: páginas «Ojos en
el círculo» y «Componentes · Luz».

Todas las medidas de la bola están en un lienzo de 200 × 210 unidades, con el centro del cuerpo en
(100, 104). En la app se escalan al tamaño de la bola en pantalla: una unidad equivale al ancho
disponible dividido entre 200.

## 1. Anatomía

| Parte | Forma | Valores |
|---|---|---|
| Cuerpo | Círculo | Centro (100, 104), radio 82. Degradado radial con centro en (0,36; 0,3) y radio 0,78: `#4B5166` → `#1C1F2B` (45 %) → `#0A0B10` |
| Borde de luz | Aro | Radio 81, trazo de 2, color del oráculo al 45–70 % |
| Brillo | Elipse | Centro (66, 56), radios 22 × 11, girada −34°, blanco al 16 % |
| Aura | Círculo detrás del cuerpo | Radio 106. Degradado radial del color del oráculo: 24 % hasta el 76 % del radio y 0 % en el borde. Es lo único que brilla en la pantalla |
| Sombra | Elipse bajo la bola | Centro (100, 200), radios 58 × 6, negro al 30 % |
| Cara | Círculo | Centro (100, 106), radio 44. Degradado mate con centro en (0,45; 0,38) y radio 0,72: `#FFFFFF` → `#F1EEEB` (70 %) → `#DFDBD7` |
| Ojos | Dos cápsulas | 7,6 × 16, radio de esquina 3,8, en x = 83,2 y x = 109,2, y = 96. Color `#16171D`. Sin boca y sin brillos |
| Ventana | Círculo, en la cara opuesta | Centro (100, 106), radio 46. Líquido con degradado radial: `#23308A` → `#0E1550` (60 %) → `#05081C`. Aro interior de radio 44, negro al 35 %, trazo de 4 |
| Burbujas | Círculos | Radios 1,3–2, `#8FA0FF` al 35 % |
| Triángulo | Triángulo hacia arriba | Vértices (100, 68), (137, 132) y (63, 132). Degradado vertical del color del oráculo. Contorno de 1,2 al 60 % |
| Texto de la respuesta | Dentro del triángulo | Inter Bold, tamaño 9, espaciado 0,2, mayúsculas, centrado en x = 100. Dos líneas como máximo, con las bases en y = 106 y 119. Si una palabra no cabe, el tamaño baja por pasos hasta 7,6 |

La bola se inclina unos grados según el estado (entre −14° y 10°). Los accesorios y la cara giran
con ella.

## 2. Colores de la interfaz

Variante Luz ([ADR 0004](adr/0004-componentes-luz.md)): un único acento, el cobalto; el resto del
color lo ponen los oráculos.

| Token | Valor | Uso |
|---|---|---|
| `bg` | `#030B14` | Fondo de las pantallas |
| `surface` | `#0D1824` | Grupos de filas y hojas |
| `control` | `#16222F` | Botones secundarios, selector, botones de icono |
| `pressed` | `#1E2C3B` | Controles y filas pulsados |
| `line` | Blanco al 8 % | Separadores y contorno de los controles |
| `text` | `#F2F5F8` | Texto principal |
| `icon` | `#C9D1DA` | Iconos y notas sobre avisos |
| `textSecondary` | `#9AA7B4` | Texto secundario |
| `disabled` | `#7A8794` | Texto desactivado |
| `outline` | `#6B7886` | Contorno del interruptor apagado |
| `accent` | `#4361EE` | Botón principal y acento único |
| `accentPressed` | `#3B55D1` | Botón principal pulsado |
| `check` | `#7C96FF` | ✓ de lo elegido |
| `link` | `#A9BCFF` | Enlaces y anillo de foco |
| `selected` | `#152240` | Fondo de lo elegido |
| `tonal` / `onTonal` | `#1D2C4E` / `#C9D6FF` | Botón tonal dentro de filas |
| `warning` | `#FFB547` sobre `#21190C` | Solo avisos que piden atención |
| `error` | `#FF8A8A` sobre `#241316` | Solo errores |
| `eye` | `#16171D` | Ojos |

Tipografía:

| Estilo | Letra | Uso |
|---|---|---|
| Título | Outfit 700, 28 | Título de pantalla; 21 en hojas |
| Barra | Outfit 600, 18 | «Magic AI Ball» en la barra superior |
| Botón | Outfit 600, 16 | Botones y selector de oráculo |
| Texto | Inter 400 y 600, 16 | Filas y texto |
| Secundario | Inter 400 y 500, 14 | Descripciones y ayuda |
| Nota | Inter 600, 12 | Etiquetas y pies |
| Oráculo | Fraunces 500, 24/30 | Solo la frase del oráculo |

Toda combinación de texto y fondo cumple 4,5:1.

## 3. Oráculos

| Oráculo (es / en) | Luz | Triángulo (texto) | Accesorio | Mirada | Carácter | Frase de ejemplo |
|---|---|---|---|---|---|---|
| Vidente / Psychic | `#9B7BFF` | `#8E73F0` → `#5B3FD1` (blanco) | Turbante `#6E4FD8` con pliegues `#8E73F0` y gema `#FFD166` | Ojos cerrados con calma | Misteriosa y segura | «Las estrellas dicen que sí.» |
| Genio / Genie | `#F2B84B` | `#FFD27A` → `#E0A030` (`#2A1A00`) | Moño y banda dorados `#F2B84B`, joya `#FF8FA3` y humo bajo la bola | Ojos felices | Generoso y teatral | «Concedido: es un sí.» |
| Bruja / Witch | `#5BC8A0` | `#23A374` → `#0F6447` (blanco) | Sombrero puntiagudo `#1E7A58`, ala `#1A6A4C` y cinta `#A8F0D4` | Borde de arriba inclinado, de pícara | Pícara y directa | «Ni lo sueñes, cariño.» |
| Mago / Wizard | `#34D1DB` | `#7DE6EC` → `#2FB7C2` (`#032A2E`) | Sombrero `#1F8C99`, ala `#18737E` y estrellas `#FFD166` | Cápsulas atentas | Sabio y amable | «Mis cálculos dicen que sí.» |
| Filósofo / Philosopher | `#C4B59F` | `#9A8B74` → `#4F4436` (blanco) | Gafas redondas negras | Medio cerrados | Seco y cansado | «Define "sí".» |
| Astróloga / Astrologer | `#FF8FA3` | `#E0607E` → `#A82C4C` (blanco) | Luna creciente `#FFD1DB` y estrellas `#FFD166` | Mirando arriba | Soñadora | «Con Venus a favor, sí.» |

Nombres que se evitan: sibila y augur, porque casi nadie los conoce; «gitana», por estereotipo;
médium y chamán.

Los colores de los oráculos no pueden coincidir con los de la interfaz. Se comprueba con una
distancia de color ΔE00 de 18 o más frente al cobalto, el ámbar, el coral y los grises de texto.
Por eso el Mago es turquesa y el Filósofo, piedra.

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

## 5. La ventana de respuesta

- La bola gira sobre su eje vertical, así que el accesorio sigue arriba y la ventana queda a la
  vista.
- En la ventana van de una a tres palabras, en mayúsculas y en dos líneas como máximo: «SÍ, SIN
  DUDA», «CONCEDIDO», «NI LO SUEÑES».
- La frase con más personalidad va debajo de la bola, en el texto de la pantalla.
- El triángulo toma el color del oráculo. Su texto cumple 4,5:1 sobre ese color; en el Genio por eso
  es oscuro.

## 6. Animación de la respuesta

Ciclo de referencia del lienzo, de 10 s:

| Fase | Tiempo | Qué pasa |
|---|---|---|
| Reposo | 0–1,8 s | Flota ±3 unidades y parpadea a 1,0 s |
| Escuchando | 1,8–2,9 s | Los ojos se estiran un 30 %, la bola se inclina −12° y aparecen ondas de sonido |
| Se agita | 2,9–3,6 s | Oscila entre −12° y 10°, con ojos `> <` y líneas de movimiento |
| Gira | 3,6–4,3 s | La cara se comprime hacia la izquierda (escala horizontal hasta 0, desplazamiento −34) y la ventana entra por la derecha; la gema del accesorio se oculta |
| La respuesta | 4,3–7,1 s | El triángulo sube 26 unidades y aparece en 0,9 s, flota y el halo se duplica |
| Vuelve | 7,1–7,8 s | La ventana sale por la izquierda y la cara entra por la derecha con el gesto de la respuesta |
| Gesto | 7,8–9,4 s | Un saltito de 10 unidades y vuelta al reposo |

En la app, las fases las disparan los estados y no un reloj fijo:

| Estado de la app | Animación |
|---|---|
| `Idle` | Reposo |
| `Listening` (mantener pulsado y hablar) | Escuchando |
| `Thinking` | Se agita y gira. Si la respuesta no ha llegado, la ventana queda a la vista con el líquido en movimiento y sin triángulo, hasta el máximo actual de 6 s |
| `Answered` | Sube el triángulo, se mantiene unos 2,5 s y la bola vuelve con el gesto del `Verdict`: sí, no o quizá |
| Fallo («Prueba otra vez luego.») | El triángulo no aparece; la bola vuelve con mirada de duda |

Reglas:

- Con «reducir movimiento» del sistema activado no hay giro, ni agitado, ni saltos: la cara y la
  ventana se cambian con un fundido de 200 ms.
- Los valores animados se leen solo en la fase de dibujo, sin recomponer la pantalla en cada
  fotograma, y no se crea memoria nueva en cada fotograma.
- Todos los tiempos y curvas son tokens del sistema de diseño, nunca números sueltos en una pantalla.
- La vibración acompaña al agitado (pulsos cortos) y a la llegada del triángulo (un toque).

## 7. Pantallas

**Principal.**
- Barra superior con «Magic AI Ball» y los botones de Recientes y Opciones.
- Debajo, el selector de oráculo, la bola con su aura y una fila de oráculos con miniaturas de 52 px;
  la miniatura del elegido lleva el anillo de su color.
- Al pie, la racha y la ayuda «Toca o agita para preguntar. Mantén presionado para hablar.».

**Con la respuesta.**
- La bola de espaldas con la ventana.
- La etiqueta «▲ Vidente añade» y la frase del personaje en Fraunces 24/30.
- Tres botones de icono con su nombre debajo: Escuchar, Compartir y Reportar.

**Reportar.**
- Una hoja con «¿Qué tiene de malo esta respuesta?» y «Elige una opción y se envía».
- Cinco filas de acción.
- Al tocar una, se envía y aparece el aviso «Reportada. Gracias.» con «Deshacer».

**Premium.**
- Tres personajes propios de ejemplo.
- «Magic AI Ball Premium» y «Tú eliges el nombre, el color y el accesorio».
- Las cuatro ventajas con ✓ y «La app sigue siendo gratis, con anuncios.».
- El botón «Suscribirme por 0,99 € al mes» y las condiciones de Google Play.

**Avatares y tarjeta de compartir** (decisión pendiente): la bola grande entrando por la esquina
inferior, inclinada unos 18°, con el círculo blanco y los ojos como cara.

## 8. Accesibilidad

- La bola tiene una descripción que cambia con el estado; por ejemplo, «La Vidente responde: sí, sin
  duda».
- Objetivos táctiles de 48 dp como mínimo.
- El texto de la respuesta se lee también fuera de la bola, en el texto de la pantalla, para
  TalkBack y para el tamaño de letra grande.
- «Reducir movimiento» se respeta como dice la sección 6.
- Lo elegido se marca con ✓ y fondo, nunca solo con color.
- Un aviso que pide atención va en ámbar y siempre da una salida.

## 9. Componentes

Variante Luz ([ADR 0004](adr/0004-componentes-luz.md)). El lienzo los muestra en la página
«Componentes · Luz», junto a la comparación con la versión descartada.

Reglas comunes:
- **Al pulsar:** el control se oscurece y se encoge al 98 %; entra en 90 ms y sale en 150 ms.
- **Al enfocar:** anillo de 2 px `#A9BCFF`, separado 2 px del control.
- **Brillo:** ningún componente brilla.
- **Tamaño táctil:** 48 dp como mínimo; la altura no se fija, para que la letra grande no se recorte.

| Componente | Forma y medidas | Estados y uso |
|---|---|---|
| Botón principal | Píldora de 52 de alto con 24 de relleno lateral. Fondo `#4361EE` con una línea interior superior de blanco al 14 %, texto blanco | Pulsado `#3B55D1`. Desactivado `#16222F` con texto `#7A8794`. Cargando: indicador y «Un momento…». Uno por pantalla |
| Botón secundario | Píldora de 52, fondo `#16222F` con línea de blanco al 8 % | Pulsado `#1E2C3B`. Desactivado `#0D1824` |
| Botón tonal | Píldora de 44, solo dentro de filas, `#1D2C4E` con texto `#C9D6FF` | Pulsado `#263A66`. Ejemplo: «Obtener» en la fila de Premium |
| Botón de texto | 44 de alto, texto `#A9BCFF` | Pulsado con fondo `#152240` |
| Botón de icono | Círculo de 48, fondo `#16222F`, icono de 22 con trazo de 2 | Mientras lee en voz alta muestra «parar» (■) |
| Selector de oráculo | Píldora de 44 con avatar de 32, anillo de 2 px del color del oráculo, nombre y flecha | Abre la lista para elegir personaje |
| Elegir personaje | Filas de 72 en un grupo `#0D1824` de radio 24: avatar de 40, nombre en Inter 600 16, descripción en 14 | Elegida: fondo `#152240`, anillo de su color y ✓ `#7C96FF`. Sin anillos vacíos en las demás |
| Filas de acción | Filas de 56 sin control, separadas por líneas de blanco al 8 % | Pulsada `#1E2C3B`. Tocar ya actúa |
| Interruptor | 52 × 32, como el de Material 3. Toda la fila conmuta | Encendido: cobalto con círculo blanco de 24. Apagado: contorno de 2 px `#6B7886` y círculo de 16. El subtítulo no cambia con el estado |
| Etiqueta | 22 de alto, sin borde, Inter 600 12 | Premium: `#152240` con texto `#A9BCFF`. Neutra: `#16222F` con `#C9D1DA` |
| Aviso de atención | Grupo de radio 24, `#21190C`, línea ámbar al 22 % | Título, qué se pierde, botón ámbar de 44 y nota de ayuda |
| Error | Grupo de radio 24, `#241316`, línea coral al 24 % | Texto y botón «Reintentar» |
| Aviso breve | Píldora opaca de 56, `#1E2C3B` | Se va solo. Tras reportar dice «Reportada. Gracias.» y ofrece «Deshacer» durante 5 s |
| Racha | Línea en Inter 500 14 `#9AA7B4`, con una chispa de trazo | Bajo la bola y a 24 de la ayuda; no es un contador en la barra |
| Frase del oráculo | Etiqueta en Inter 600 12 con ▲ en el color del oráculo, y la frase en Fraunces 500 24/30 | Líneas equilibradas, tres como máximo; a 20 px si pasa de dos |
