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
| `disabled` | `#808D9A` | Texto desactivado; sobre `#16222F` llega a 4,5:1 |
| `outline` | `#6B7886` | Contorno del interruptor apagado y de los campos |
| `pageDot` | `#56626F` | Puntos de las páginas que no se ven |
| `scrim` | `#01060C` al 72 % | Velo detrás de hojas y diálogos |
| `accent` | `#4361EE` | Botón principal y acento único |
| `accentPressed` | `#3B55D1` | Botón principal pulsado |
| `check` | `#7C96FF` | ✓ de lo elegido |
| `link` | `#A9BCFF` | Enlaces y anillo de foco |
| `selected` | `#152240` | Fondo de lo elegido |
| `tonal` / `onTonal` | `#1D2C4E` / `#C9D6FF` | Botón tonal dentro de filas |
| `warning` | `#FFB547` sobre `#21190C` | Solo avisos que piden atención, con su acción dentro |
| `error` | `#FF8A8A`; `#2A1418` en el botón de borrar | Solo el error de un campo y las acciones que borran |
| `eye` | `#16171D` | Ojos |

Tipografía:

| Estilo | Letra | Uso |
|---|---|---|
| Título | Outfit 700, 28 | Título de pantalla. En hojas y diálogos, 21, salvo Premium y la ficha de un personaje, que llevan 28 |
| Botón | Outfit 600, 16 | Botones y selector de oráculo |
| Texto | Inter 400 y 600, 16 | Filas y texto |
| Secundario | Inter 400 y 500, 14 | Descripciones y ayuda |
| Nota | Inter 600, 12 | Etiquetas y pies |
| Oráculo | Fraunces 500 | Solo lo que dicen los personajes: 24/30 bajo la bola, 20/26 en las respuestas largas, la ficha y la tarjeta de compartir, 16/22 en Recientes y 15/20 en las muestras de Personajes |

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

Todas están dibujadas en la página «Pantallas · Luz» del lienzo, en el orden de esta sección. Los
componentes que se nombran aquí están en la sección 9.

### La bola

La bola es la página central de tres que se pasan deslizando. Recientes queda a la izquierda y
Opciones a la derecha.

**Huecos fijos**, de arriba abajo:

| Hueco | Alto | Qué lleva |
|---|---|---|
| Barra superior | 56 | Botones de icono sin fondo |
| Margen superior | El 40 % del espacio libre en reposo: 68 en un teléfono de 844 | Nada |
| Título | 96, con el contenido abajo | La ayuda o la pregunta |
| Bola | 250 de ancho, a 8 del título | La bola con su aura |
| Leyenda | 120 como mínimo, con el contenido arriba | La racha o la frase del personaje |
| Hueco libre | Lo que quede | Nada |
| Acciones | 80, con 20 de margen inferior | Botones |

El margen superior se calcula una vez y no cambia entre estados. Si la leyenda crece, solo ocupa
el hueco libre, así que la bola no se mueve nunca.

**Barra superior:**
- En reposo: Recientes a la izquierda, los tres puntos de página en el centro y Opciones a la
  derecha.
- Mientras escucha o piensa: solo ✕, «Cancelar».
- Con la respuesta: ✕, «Cerrar», y a la derecha «Leer la respuesta en voz alta». Mientras lee, ese
  botón muestra «parar» y la detiene.

**Estados.** Una vez hecha, la pregunta se queda en el título, entre «», en Inter 15 `#9AA7B4`.

| Estado | Título | Bola | Leyenda | Acciones |
|---|---|---|---|---|
| En reposo | «Toca o agita para preguntar. Mantén presionado para hablar.» | Cara en reposo | La racha: «3 días seguidos» | Selector «Responde como · Vidente» |
| Escuchando | «Te escucho…» en Inter 600 13 y, debajo, lo que va oyendo en Inter 18 `#F2F5F8` | Escuchando, con ondas | Nada | Nada |
| Pensando | La pregunta | La ventana con el líquido | Nada | Nada |
| Respuesta | La pregunta | La ventana con la respuesta | «▲ Vidente añade · IA» y la frase en Fraunces 24/30 | «Compartir» y los dos pulgares |
| Respuesta larga, con «Respuestas más largas» | La pregunta | La ventana con la respuesta | La frase en Fraunces 20/26; la leyenda crece hacia abajo | Como en la respuesta |
| Sin respuesta | La pregunta | Cara de duda | «Prueba otra vez luego.» en Inter 500 18 | «Toca o agita para preguntar», como texto |
| Modo fiesta | «Ana, te toca» en Outfit 28 y, debajo, «Toca o agita para preguntar» | Cara en reposo | La racha | El avatar del personaje y «¿Quién del grupo…?» |

**Valorar y reportar:**
- El pulgar arriba marca la respuesta: fondo `#152240` e icono `#7C96FF`.
- El pulgar abajo se marca igual y abre la hoja «¿Qué tiene de malo esta respuesta?», con «Elige una
  opción y se envía» y seis filas de acción:
  - «No me gustó»;
  - «Es ofensiva»;
  - «Es sexual o inapropiada»;
  - «Puede hacer daño»;
  - «No tiene sentido»;
  - «Otra cosa».
- Al tocar una, se envía. La frase del personaje se cambia por «Respuesta reportada», con una
  bandera, y sobre las acciones sale el aviso «Reportada. Gracias.» con «Deshacer».

### Recientes

- La barra lleva los puntos de página y, a la derecha, «Volver a la bola». El título «Recientes»
  lleva a su derecha «Borrar todo», como botón de texto.
- Un grupo con una fila por respuesta:
  - el avatar de 36, sin anillo;
  - la respuesta en Inter 600 16;
  - la frase del personaje en Fraunces 16/22;
  - la pregunta entre «», en Inter 14 `#9AA7B4`;
  - «Hace 5 min · Vidente · IA», en Inter 12;
  - a la derecha, la papelera, «Borrar respuesta».
- Tocar una fila la abre en la bola. Debajo del grupo va «Se guarda solo en este dispositivo».
- Vacía: el reloj en una losa de 72, «Aún no hay respuestas», «Pregúntale a la bola y tus
  respuestas aparecerán aquí, guardadas solo en este dispositivo» y «Volver a la bola».
- «Borrar todo» abre el diálogo «¿Borrar todas las respuestas?», con «Borrar todo» en el botón de
  borrar y «Cancelar».

### Opciones

- La barra lleva «Volver a la bola» y los puntos de página. Debajo, el título «Opciones».
- Sin cuenta, una tarjeta con «Respuestas a tu medida», «Entra y los personajes sabrán quién
  pregunta» y «Continuar con Google». Con cuenta, una fila con la inicial, el nombre y el correo,
  que abre «Tu cuenta».
- La fila de Premium lleva la corona, «Premium», «Sin anuncios y más respuestas con IA» y
  «Obtener». Con Premium dice «Activo · Gestionar en Google Play» y sale a Google Play.
- Tres grupos:
  - «Respuestas»: Respuestas con IA, Voz y Modo fiesta;
  - «General»: Notificaciones, Idioma y Compartir la app;
  - «Información»: Privacidad de anuncios, Política de privacidad, Términos de uso, Licencias de
    código abierto y Contacto.
- Cada fila lleva un icono de 22, y su texto empieza a 72 del borde, como el de los avisos.
  - La fila que abre una pantalla lleva ›.
  - La que sale de la app lleva el icono de enlace externo.
- Al pie van la nota de privacidad, la versión y «Powered by Gemini».

Pantallas que se abren desde Opciones, todas con «Atrás» y su título:

| Pantalla | Contenido | Aviso cuando falta algo |
|---|---|---|
| Respuestas con IA | Los interruptores «Respuestas con IA» y «Respuestas más largas» | «Ya usaste las de hoy. Vuelven mañana», «Mira un anuncio corto para seguir» y «Ver un anuncio» |
| Voz | «Preguntas por voz» y «Respuestas en voz alta» | «Necesita acceso al micrófono», «Para preguntar en voz alta» y «Permitir micrófono» |
| Notificaciones | «Veredicto del día» | «Las notificaciones están desactivadas para esta app», «Sin ellas no te llega el veredicto del día.», «Permitir notificaciones» y la nota «Abre los ajustes de notificaciones de la app» |
| Idioma | English, Español y Português, con ✓ en el elegido | Ninguno |
| Licencias de código abierto | Lucide, Google Sans, Fraunces, Outfit, Inter y «Bibliotecas» | Ninguno |

Cada aviso va debajo del interruptor al que afecta, con su acción dentro.

**Modo fiesta** se abre en un diálogo con:
- la nota «La bola anuncia el turno de cada persona y puede elegir a alguien del grupo.»;
- el campo «Nombre» y «Añadir», un botón de texto con +, desactivado mientras no hay nombre;
- los nombres en chips; tocar uno lo quita, «Quitar a Ana»;
- «3 de 8 personas»;
- «Guardar» y «Cancelar».

### Personajes y cuenta

**Personajes:**
- La barra lleva ✕ y «Crear un personaje».
- Debajo van los chips «Todos», «Originales», «Comunidad» y «Tuyos».
- Los grupos «Los de siempre» y «Con actitud» tienen filas de elegir personaje.
- Si no cargan, sale el aviso «No se pudieron cargar los personajes» con «Reintentar».

**Ficha de un personaje**, en una hoja:
- el avatar de 88 y el nombre en Outfit 28;
- «Original de Magic AI Ball»;
- «Cómo responde», con la descripción y una muestra en Fraunces 20/26;
- «Usar este personaje».

**Crear sin cuenta**, en un diálogo: «Para crear personajes necesitas una cuenta», «Entra con
Google para crear el tuyo. La app sigue funcionando igual sin cuenta.», «Continuar con Google» y
«Ahora no».

**Nuevo personaje** es una pantalla completa, porque el formulario no cabe en una hoja:
- la barra con ✕, «Cancelar», y «Guardar» como botón de texto;
- una vista previa con «Con tus palabras» en Fraunces 20/26 y «▲ Capitán Gruñón» en el color
  elegido;
- los campos «Nombre», de hasta 16 caracteres, y «¿Cómo responde?», de hasta 500;
- los colores, en círculos de 40 en una fila que se desliza hasta el borde de la pantalla;
- los accesorios, en losas de 52;
- en los colores y en los accesorios, ✓ en el elegido;
- la nota «Se envía a Gemini, de Google, para responder. Nada personal.».

**Tu cuenta**, en una hoja:
- la inicial, el nombre, el correo y la etiqueta «Tienes Premium»;
- «Tu perfil»;
- «Gestionar suscripción», que sale a Google Play;
- «Cerrar sesión»;
- «Eliminar cuenta», en coral.

**¿Eliminar tu cuenta?**, en un diálogo. Con Premium usa el texto que avisa de que la suscripción
no se cancela, y ofrece «Eliminar cuenta», «Gestionar en Google Play» y «Cancelar».

**¿Quién pregunta?** es una hoja sin asa que sale sobre la bola al entrar con Google:
- «Dos datos para respuestas a tu medida»;
- el campo «Nombre» y la fecha de nacimiento en tres campos;
- la tarjeta «Qué se envía a Gemini, de Google», con un enlace a la política de privacidad;
- «Aceptar y continuar» y «Continuar sin cuenta».

### Premium, compartir y sistema

**Magic AI Ball Premium**, en una hoja:
- tres personajes propios de ejemplo y el título en Outfit 28;
- las cuatro ventajas, cada una con su icono: sin anuncios, más respuestas con IA, personajes
  propios y el tono que describas;
- «La app sigue siendo gratis, con anuncios.» y las condiciones de Google Play;
- «Términos de uso» y «Política de privacidad», como botones de texto de 48;
- «Suscribirme por 0,99 € al mes» y «Ahora no».

Mientras no llega el precio, «Suscribirme» está desactivado, con su indicador, y debajo dice
«Buscando el precio en Google Play…».

**Tarjeta de compartir**, de 360 × 450:
- fondo `#E4DBFF`;
- la pregunta en un bocadillo;
- la respuesta en Outfit 44 y la frase en Fraunces 20/26;
- «▲ Vidente»;
- la marca con su glifo.

Los avatares y esta tarjeta siguen pendientes de decisión. La bola entra por la esquina inferior,
inclinada unos 18°, con el círculo blanco y los ojos como cara.

**Notificación del veredicto:** el glifo de la bola en monocromo, «Magic AI Ball · ahora»,
«Veredicto del día», la frase y, a la derecha, el avatar del personaje.

**Widget:** dos de 156 × 156 con el radio del sistema (`system_app_widget_background_radius`). Uno
lleva la bola en reposo y el otro la ventana con la respuesta.

### Reloj

Wear OS, pantalla redonda de 227 dp:
- La hora va arriba en todas las pantallas.
- En reposo: la bola de 112 y «Toca, agita o mantén presionado».
- La respuesta se ve en la ventana de cerca, para que se lea, con «Reportar» en una píldora de 52.
- Idioma, Personaje y Respuestas con IA son listas que se desplazan:
  - la cabecera va arriba;
  - las opciones van en píldoras de 52, con ✓ en la elegida;
  - la que llega al borde se encoge y se apaga.
- Con el límite de IA gastado, «Ya usaste las de hoy. Vuelven mañana» va en ámbar.

### Estados de los componentes

La página termina con un tablero de estados:
- los botones normal, pulsado, con foco, desactivado y cargando;
- el campo normal, con foco y con el error «Ponle un nombre»;
- Premium sin precio;
- Personajes sin conexión;
- el pulgar marcado y la lectura en voz alta;
- «Añadir» sin nombre escrito.

### Textos nuevos

Hay que añadirlos en los tres idiomas:
- «No me gustó», en el reporte;
- «Ver un anuncio», en el aviso del límite de IA;
- «Detener la lectura», la descripción del botón que para la lectura;
- en Licencias, la fila «Bibliotecas» y las descripciones de Fraunces, Outfit e Inter.

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

Variante Luz ([ADR 0004](adr/0004-componentes-luz.md)), con los botones de radio 16 del
[ADR 0005](adr/0005-botones-radio-16.md). El lienzo muestra los componentes en la página
«Componentes · Luz», todavía con botones en píldora, y todas las pantallas en «Pantallas · Luz».

Reglas comunes:
- **Al pulsar:** el control se oscurece y se encoge al 98 %; entra en 90 ms y sale en 150 ms.
- **Al enfocar:** anillo de 2 px `#A9BCFF`, separado 2 px del control.
- **Brillo:** ningún componente brilla.
- **Forma:** radio 16 en botones, campos y botones de icono; 12 en los chips; 24 en los grupos; 28
  en los diálogos; 32 en las hojas. Solo son redondos del todo el interruptor, las etiquetas, los
  avatares y la bola.
- **Tamaño táctil:** 48 dp como mínimo; la altura no se fija, para que la letra grande no se recorte.

| Componente | Forma y medidas | Estados y uso |
|---|---|---|
| Botón principal | 52 de alto, radio 16 y 20 de relleno lateral. Fondo `#4361EE` con una línea interior superior de blanco al 14 %, texto blanco | Pulsado `#3B55D1`. Desactivado `#16222F` con texto `#808D9A`. Cargando: indicador y «Un momento…». Uno por pantalla |
| Botón secundario | 52 de alto y radio 16, fondo `#16222F` con línea de blanco al 8 % | Pulsado `#1E2C3B`. Desactivado `#0D1824` |
| Botón tonal | 52 de alto y radio 16, solo dentro de filas, `#1D2C4E` con texto `#C9D6FF` | Pulsado `#263A66`. Ejemplo: «Obtener» en la fila de Premium |
| Botón de texto | 48 de alto y radio 16, texto `#A9BCFF` | Pulsado con fondo `#1E2C3B` |
| Botón de icono | 48 de alto y radio 16, fondo `#16222F`, icono de 22 con trazo de 2. En la fila de la respuesta mide 52; en la barra superior va sin fondo | Mientras lee en voz alta, el de la barra muestra «parar», un cuadrado de contorno |
| Botón de borrar | 52 de alto y radio 16, fondo `#2A1418` con línea coral al 22 % y texto `#FF8A8A` | Solo para acciones que borran, como «Borrar todo» y «Eliminar cuenta» |
| Selector de oráculo | 52 de alto y radio 16, con avatar de 32, anillo de 2 px del color del oráculo, nombre y flecha | Abre la lista para elegir personaje |
| Campo de texto | 56 de alto y radio 16, fondo `#16222F` con contorno de 1 px `#6B7886`. La etiqueta va encima, en Inter 600 13 | Con foco: contorno de 2 px `#A9BCFF`. Con error: contorno de 2 px coral y el mensaje debajo, en coral |
| Chip de filtro | 36 de alto y radio 12, en Outfit 600 15 | Elegido: fondo `#152240`, línea `#7C96FF` al 40 % y ✓ `#7C96FF` |
| Chip de nombre | 36 de alto y radio 12, `#16222F` con línea de blanco al 8 %, Outfit 600 15 y una ✕ de 16 | Todo el chip quita a la persona: «Quitar a Ana» |
| Barra superior | 56 de alto, con botones de icono de 48 sin fondo | En las páginas de la bola lleva en el centro los puntos de página: `#F2F5F8` el de la página que se ve y `#56626F` los demás |
| Título de pantalla | 64 de alto como mínimo, Outfit 700 28/34, a 20 del borde | A la derecha puede llevar un botón de texto, como «Borrar todo» |
| Hoja | Radio 32 arriba, `#0D1824`, asa de 36 × 4 `#2A3644` y 16 de margen lateral | Su alto es el de su contenido. Título en Outfit 700 21/28. Los grupos de dentro van en `#16222F`. Detrás, el velo `scrim` |
| Diálogo | Radio 28, `#0D1824` con línea de blanco al 8 %, a 24 de los bordes | Título en Outfit 700 21/28 y texto en Inter 15. La acción principal arriba y «Cancelar» o «Ahora no» como botón de texto. Detrás, el velo `scrim` |
| Elegir personaje | Filas de 72 en un grupo `#0D1824` de radio 24: avatar de 40, nombre en Inter 600 16 y una muestra de cómo responde en Fraunces 15/20 | Elegida: fondo `#152240`, anillo de su color y ✓ `#7C96FF`. Sin anillos vacíos en las demás |
| Filas de acción | Filas de 56 sin control, separadas por líneas de blanco al 8 % | Pulsada `#1E2C3B`. Tocar ya actúa |
| Interruptor | 52 × 32, como el de Material 3. Toda la fila conmuta | Encendido: cobalto con círculo blanco de 24. Apagado: contorno de 2 px `#6B7886` y círculo de 16. El subtítulo no cambia con el estado |
| Etiqueta | 24 de alto, redonda del todo, sin borde, Inter 600 12, `#16222F` con texto `#C9D1DA` | Ejemplo: «Tienes Premium», en Tu cuenta |
| Aviso de atención | Grupo de radio 24, `#21190C`, línea ámbar al 22 %. Icono ámbar de 24, sin caja, y el texto a 72 del borde | Título, qué se pierde, la acción dentro en un botón ámbar de 52 y, si hace falta, una nota. Sirve para el límite de IA, el micrófono, las notificaciones y los personajes que no cargan («Reintentar») |
| Aviso breve | 56 de alto y radio 16, opaco, `#1E2C3B` | Se va solo. Tras reportar dice «Reportada. Gracias.» y ofrece «Deshacer» durante 5 s |
| Racha | Línea en Inter 500 13 `#9AA7B4`, con una chispa de trazo de 14 | Bajo la bola, en reposo y en modo fiesta; la ayuda va encima de la bola. No es un contador en la barra |
| Frase del oráculo | Etiqueta en Inter 600 12 con ▲ en el color del oráculo, y la frase en Fraunces 500 24/30 | Líneas equilibradas. Con «Respuestas más largas», 20/26, y la leyenda crece hacia abajo sin mover la bola |
