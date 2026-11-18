# Rediseño de la bola: especificación

Estado: propuesta B aprobada. 18 nov 2026. Los valores salen del lienzo de diseño (página
«Ojos en el círculo»).

Todas las medidas de la bola están en un lienzo de 200 × 210 unidades, con el centro del cuerpo en
(100, 104). En la app se escalan al tamaño de la bola en pantalla: una unidad equivale al ancho
disponible dividido entre 200.

## 1. Anatomía

| Parte | Forma | Valores |
|---|---|---|
| Cuerpo | Círculo | Centro (100, 104), radio 82. Degradado radial con centro en (0,36; 0,3) y radio 0,78: `#4B5166` → `#1C1F2B` (45 %) → `#0A0B10` |
| Borde de luz | Aro | Radio 81, trazo de 2, color del oráculo al 45–70 % |
| Brillo | Elipse | Centro (66, 56), radios 22 × 11, girada −34°, blanco al 16 % |
| Halo | Círculo detrás del cuerpo | Radio 97–99, color del oráculo al 10–20 % según el estado |
| Sombra | Elipse bajo la bola | Centro (100, 200), radios 58–62 × 6–7, negro al 40 % |
| Cara | Círculo | Centro (100, 106), radio 44. Degradado mate con centro en (0,45; 0,38) y radio 0,72: `#FFFFFF` → `#F1EEEB` (70 %) → `#DFDBD7` |
| Ojos | Dos cápsulas | 7,6 × 16, radio de esquina 3,8, en x = 83,2 y x = 109,2, y = 96. Color `#16171D`. Sin boca y sin brillos |
| Ventana | Círculo, en la cara opuesta | Centro (100, 106), radio 46. Líquido con degradado radial: `#23308A` → `#0E1550` (60 %) → `#05081C`. Aro interior de radio 44, negro al 35 %, trazo de 4 |
| Burbujas | Círculos | Radios 1,3–2, `#8FA0FF` al 35 % |
| Triángulo | Triángulo hacia arriba | Vértices (100, 68), (137, 132) y (63, 132). Degradado vertical del color del oráculo. Contorno de 1,2 al 60 % |
| Texto de la respuesta | Dentro del triángulo | Inter Bold, tamaño 7,6, espaciado 0,3, mayúsculas, centrado en x = 100. Dos líneas como máximo, con las bases en y = 108 y 118 (o 112 y 122 si la palabra larga va arriba) |

La bola se inclina unos grados según el estado (entre −14° y 10°). Los accesorios y la cara giran
con ella.

## 2. Colores de la interfaz

| Token | Valor | Uso |
|---|---|---|
| `bg` | `#0E1016` | Fondo de las pantallas |
| `surface` | `#171A23` | Pastillas, tarjetas y cajas de frase |
| `line` | `#262A36` / `#2A2E3B` | Bordes de pastillas y botones |
| `divider` | `#1F2330` | Separadores |
| `text` | `#F2F4F8` | Texto principal |
| `textSecondary` | `#A3AABD` | Texto secundario |
| `textTertiary` | `#7D8497` | Notas |
| `accent` | `#7C96FF` | Acento de la marca y color de la bola sin oráculo |
| `eye` | `#16171D` | Ojos |

Tipografía: Inter (400, 500, 600, 700). Títulos de 24–40 px en 700, texto de 13–16 px. Toda
combinación de texto y fondo cumple 4,5:1.

## 3. Oráculos

| Oráculo (es / en) | Luz | Triángulo (texto) | Accesorio | Mirada | Carácter | Frase de ejemplo |
|---|---|---|---|---|---|---|
| Vidente / Psychic | `#9B7BFF` | `#8E73F0` → `#5B3FD1` (blanco) | Turbante `#6E4FD8` con pliegues `#8E73F0` y gema `#FFD166` | Ojos cerrados con calma | Misteriosa y segura | «Las estrellas dicen que sí.» |
| Genio / Genie | `#F2B84B` | `#FFD27A` → `#E0A030` (`#2A1A00`) | Moño y banda dorados `#F2B84B`, joya `#FF8FA3` y humo bajo la bola | Ojos felices | Generoso y teatral | «Concedido: es un sí.» |
| Bruja / Witch | `#5BC8A0` | `#23A374` → `#0F6447` (blanco) | Sombrero puntiagudo `#1E7A58`, ala `#1A6A4C` y cinta `#A8F0D4` | Borde de arriba inclinado, de pícara | Pícara y directa | «Ni lo sueñes, cariño.» |
| Mago / Wizard | `#7C96FF` | `#7C96FF` → `#4558D6` (blanco) | Sombrero `#4558D6`, ala `#3446B8` y estrellas `#FFD166` | Cápsulas atentas | Sabio y amable | «Mis cálculos dicen que sí.» |
| Filósofo / Philosopher | `#A7B0C0` | `#8A93A8` → `#4A5164` (blanco) | Gafas redondas negras | Medio cerrados | Seco y cansado | «Define "sí".» |
| Astróloga / Astrologer | `#FF8FA3` | `#E0607E` → `#A82C4C` (blanco) | Luna creciente `#FFD1DB` y estrellas `#FFD166` | Mirando arriba | Soñadora | «Con Venus a favor, sí.» |

Nombres que se evitan: sibila y augur, porque casi nadie los conoce; «gitana», por estereotipo;
médium y chamán.

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

**Principal.** Barra superior con «Magic AI Ball» y los botones de Recientes y Opciones. Debajo, el
selector de oráculo en una pastilla con su color, la bola, la respuesta en texto grande con la frase
del personaje debajo, el aviso «Mantén pulsado y pregunta en voz alta» y una fila de oráculos con
miniaturas de 52 px, con un aro de color en el elegido.

**Con la respuesta.** La bola de espaldas con la ventana, la etiqueta «Vidente añade», la frase del
personaje en 20 px y un botón «Compartir».

**Avatares y tarjeta de compartir** (decisión pendiente): la bola grande entrando por la esquina
inferior, inclinada unos 18°, con el círculo blanco y los ojos como cara.

## 8. Accesibilidad

- La bola tiene una descripción que cambia con el estado; por ejemplo, «La Vidente responde: sí, sin
  duda».
- Objetivos táctiles de 48 dp como mínimo.
- El texto de la respuesta se lee también fuera de la bola, en el texto de la pantalla, para
  TalkBack y para el tamaño de letra grande.
- «Reducir movimiento» se respeta como dice la sección 6.
