# Prompts de la Comunidad: cómo escribir ejemplos que suenen a gente

Documento para el dueño de Magic AI Ball. Cubre el banco de muestra de la estantería Comunidad (`CommunityModule.kt`, 51 entradas), por qué se lee como hecho a máquina, cómo escribe la gente de verdad, una guía de estilo con "manos" fijas por autor, y un banco nuevo de 53 entradas listo para pegar.

Límites de la app (de `CustomCharacter.kt`): nombre hasta 16 y descripción hasta 280, contados en unidades UTF-16 (`String.length` de Kotlin). Un emoji cuenta 2 o más. Todas las medidas de este documento están hechas con esa regla.

## Resumen en diez líneas

1. El banco actual no suena a máquina por una sola cosa sino por la suma: todas las entradas son un chiste cerrado (planteo, escalada, remate), con la puntuación y los acentos exactos, y los "errores" son siempre los mismos y siempre aplicados al 100%.
2. La gente real escribe en el celular con descuido irregular: se olvida unas tildes y otras no, no abre signos, no pone punto final, se corta, mete una aclaración que no hace falta, repite la letra, se le escapa un typo.
3. Detectar texto de máquina en textos cortos e informales es poco fiable para los detectores automáticos. Lo que importa acá no es engañar a un detector sino a un lector: y al lector lo delatan la uniformidad y la prolijidad.
4. Cada "usuario N" tiene que escribir siempre con la misma mano (13 manos definidas abajo), con fugas: el que no usa tildes se le cuela alguna.
5. Los largos tienen que ser desparejos: algunas de 30 a 50 unidades y varias entre 260 y 280 que parezcan cortadas por el contador, no cerradas con moño.
6. 280 es bajo para lo que la gente escribe cuando arma un personaje en apps parecidas, y alcanza para un chiste. Recomendación: 500.
7. El banco nuevo tiene 53 entradas; todas verificadas por conteo, sin comillas dobles, sin `$` ni barras, así que van tal cual dentro de un string de Kotlin.
8. Se tiraron o reescribieron las que rozaban lo financiero (cripto), lo menor (niño, nene, 3ro B) o eran duplicadas.
9. Hay una lista de control de 12 puntos al final para juzgar entradas futuras.
10. Límite de la evidencia: no encontré un corpus público de descripciones reales de personajes de Character.AI o Talkie (las páginas piden sesión), ni un estudio académico del español rioplatense escrito en el celular con estos rasgos. Eso está dicho donde corresponde y lo que es observación mía va marcado como tal.

---

## 1. Diagnóstico: qué hace que el banco actual parezca de máquina

Medí las 51 entradas del banco actual (largo en UTF-16, mayúscula inicial, punto final, signos, emoji, tildes). Los números van entre paréntesis.

**Tell 1. Todas son un chiste terminado: planteo, escalada, remate.** Cada entrada resuelve. El remate suele ser una frase corta que cierra con antítesis: en "El CM" termina con "No arregla nada, pero qué amable."; en "Cripto bro" con "nunca tuvo razon, pero tampoco duda"; en "Jefe buena onda" con "El no lo disfraza de feedback." Una persona real a veces ni llega al remate, o lo explica de más, o lo deja a medias. Ninguna de las 51 queda abierta, ninguna pregunta, ninguna se corrige. (La excepción que funciona es "Señal de wifi", que se corta en "despu": es la más humana del banco, y se recomienda copiar esa idea.)

**Tell 2. Regla de tres y enumeraciones parejas.** "Tarot viral": "no lo ignores", "viene alguien con la inicial M", "todo muy misterioso". "Gymbro": entrenando, comiendo pollo con arroz y durmiendo 8 hs. "Chef picante" y "Mago de cumple" repiten el esquema de tres tareas. La regla de tres figura entre las señales listadas en la guía de Wikipedia sobre escritura de IA (sección 2.1.1; ver fuentes). Los humanos también enumeran, pero desparejo: cuatro cosas, o dos y media, o seis que se pierden.

**Tell 3. Puntuación y signos exactos.** "Chef picante" escribe "¿¡ESTO QUÉ ES!?" y "DJ de casamiento" escribe "¡¿Y DÓNDE ESTÁN LOS SOLTEROS?! ¡ARRIBA ESAS PALMAS!": signos de apertura y cierre perfectos, con tilde en mayúscula, en el celular, donde casi nadie los pone. 9 de 51 usan ¿ o ¡, y en esas 9 cada signo abierto está cerrado y ninguno falta. Los únicos "?" sin apertura están en "Mi ex a las 3am" y "Perro feliz", ambas del registro descuidado; en el registro cuidado nunca falta un signo. Además 18 de 51 terminan con punto final, y 34 de 51 traen una frase entre comillas dobles bien abierta y cerrada, el recurso de "ejemplo de diálogo" que se ve en guías de prompts.

**Tell 4. Los "errores" son sistemáticos.** 20 entradas están en minúscula "descuidada" y en las 20 no hay una sola vocal con tilde: el modelo de error es "sacar todas las tildes", aplicado al 100%, y sin un solo typo de dedos. Una persona real omite algunas tildes (la que le cuesta, la que el teclado no corrige) y deja otras ("qué" puesta, "está" sin tilde), y se equivoca de letra. En el banco: "q" aparece en 8 entradas, "xq" en 2 y "aunq" en 2; "x" por "por" y "tmb" nunca, "jaja" en una sola de 51 y ninguna risa tipo "jsjs". Hay una sola vocal alargada ("vocaaaales") y ningún typo de dedos.

**Tell 5. Dos registros nítidos, no una mezcla.** 31 entradas son "cuidadas" (mayúscula, puntos, tildes) y 20 son "descuidadas" (todo minúscula, sin tildes). Nadie en el medio. Una persona real oscila: arranca en minúscula y después mete una mayúscula, o hace lo contrario; abre una coma y no la cierra; pone punto en la primera frase y nada en la segunda. Los dos registros son disfraces consistentes, lo cual los delata.

**Tell 6. Todas arrancan como instrucción de prompt.** 12 abren con una fórmula de rol o de orden ("Respondé como", "Hablá como", "Sos", "Soy", "Contestá", "Ponete", "Seguí"). "Mamá preocupada": "Respondé como mi mamá. Cualquier pregunta termina en si comí...". Es lo que escribe quien sabe cómo se le habla a un modelo. La gente real, cuando describe a alguien para un juego, mezcla: "es el que...", "mi tía que...", "soy yo cuando...", "ponele que sos...". Y no usa vocabulario de especificación ("cualquier pregunta termina en", "toda respuesta incluye", "cerrá siempre con").

**Tell 7. Cadencia publicitaria y construcciones balanceadas.** "Vendedor de tren": "Lo que en cualquier comercio sale mil, hoy y solo por este vagón te lo llevás por cien." "Azafata": "Gracias por elegirnos." "Planta sin regar": "Respondo, pero con reproche". La estructura "no X, sino Y" y las antítesis redondas están en la lista de señales de Wikipedia (sección 3.3, paralelismos negativos). El humor de cada autor suena igual de pulido.

**Tell 8. El largo forma una campana.** Rango 36 a 278 (media 157, mediana 163, desvío 61), pero 46 de 51 están entre 57 y 228 y 35 de 51 caen entre 100 y 230. Ninguna entrada es de dos o tres palabras, y las 4 más largas (271, 272, 277, 278: "Vendedor de tren", "Grupo de 3ro B", "El tachero", "Audio de 7 min") terminan en un cierre limpio (tres con punto, una con la comilla de cierre): quien escribe cerca del tope no suele caer en un cierre perfecto, o se pasa y lo corta el contador, o deja de escribir antes. Esto es "escrito para caber".

**Tell 9. Emojis temáticos y bien ubicados.** 11 de 51 llevan emoji y casi todos son literales y al final de la frase o de la entrada: "Tía del grupo" cierra con 🙏🙏 y "Rockero viejo" con 🤘; "Meteoróloga" pone ☀️🌧️ al lado del pronóstico, "Tarot viral" 🔮 y "Cumpleañero" 🎂 en su tema. Wikipedia lista el emoji como señal cuando aparece en lugares formales como adornos de lista (sección "Style"); en chat, el rasgo útil es otro: la gente pone emojis de reacción (😭 💀 🙃) que no ilustran lo que se dice, a mitad de frase, y repetidos.

**Tell 10. Un solo tipo de humor, el mismo pulido, para 16 autores.** Todo es la misma observación costumbrista de la Argentina urbana (el tachero, el consorcio, el grupo de padres, el bondi), calibrada igual de bien. Una comunidad real trae cosas torcidas, mal explicadas, con chistes internos que no se entienden, malas ideas, ideas repetidas (es esperable que varios hagan el michi con hambre) y personajes poco graciosos. También ayuda a la credibilidad que algunas sean flojas.

Además, tres problemas de contenido a corregir: "Niño rata" (un pibe jugando online) y "Grupo de 3ro B" (madres y padres de chicos de escuela) rozan a menores; "Cripto bro" hace chiste con asesoramiento financiero; "Mago de cumple" nombra a un nene.

---

## 2. Cómo escribe la gente de verdad: hallazgos y fuentes

### 2.1 Cómo se escribe un personaje o una bio en apps de consumo

- **Character.AI**: según su centro de ayuda, el campo público "Description" admite hasta 500 caracteres, escrito con la voz del personaje, el "Tagline" 50, el saludo hasta 4096 y la "Definition" hasta 32000. Las guías de la comunidad dicen que casi nadie usa los 32000, y que la parte que realmente pesa es la primera. https://support.character.ai/hc/en-us/articles/50609592926235-9-Templates-and-Examples (límites vistos en un resumen de búsqueda; la página devolvió 403 al leerla) y https://support.character.ai/hc/en-us/articles/36429196456475-Community-Update-April-2025 (saludo a 4096).
- **SillyTavern** (el cliente de código abierto con el que se arman la mayoría de las "cards"): la descripción "could be of any length (be it 200 or 2000 tokens)", y el primer mensaje es lo que más pega el estilo y largo de las respuestas. https://docs.sillytavern.app/usage/core-concepts/characterdesign
- **JanitorAI**: el campo "Personality" recomienda quedarse por debajo de 2500 tokens permanentes; la bio pública es opcional y sin largo fijado. https://help.janitorai.com/en/article/the-basics-the-character-creation-page-overview-15xevon/
- **Talkie, Poe, Discord**: no encontré documentación oficial con un tope de descripción de persona citable. Las guías de terceros para bots de Discord insisten en la voz y en dar ejemplos más que adjetivos; no hay colección pública de prompts de usuarios reales que se pueda leer sin sesión.
- **Bios sociales** (tope, no uso real): X 160, Instagram 150, TikTok 80 según tablas de terceros que replican las páginas de ayuda. https://metricool.com/characters-social-media/
- **Lo que no pude verificar**: ejemplos reales de descripciones de usuarios de esas apps. Lo que sigue sobre cómo escriben viene de estudios de escritura informal en chat y de los límites que fijan las plataformas, no de un corpus de personajes.

### 2.2 Qué se sabe de distinguir escritura humana informal de escritura de modelo

- **Uniformidad frente a variabilidad.** En un estudio con 4346 ensayos, la escritura generada por IA mostró "striking stylistic uniformity" y la humana más variabilidad e individualidad (resumen en Digital Scholarship in the Humanities). https://academic.oup.com/dsh/advance-article/doi/10.1093/llc/fqag064/8714041 (solo leí el resumen).
- **Estandarización gramatical y puntuación.** Un estudio de estilometría sobre textos cortos encontró que los modelos usan más estandarización gramatical (frecuencias de categorías gramaticales más parejas que las de Wikipedia, escrita por muchos autores), distinto uso de puntos, y sobreuso de ciertas palabras. Aviso honesto: sobre textos informales fuera del dominio de entrenamiento (tuits, reseñas) la clasificación bajó a macro-F1 0.48 a 0.54, o sea que los detectores automáticos fallan en texto corto informal. https://arxiv.org/html/2507.00838v2
- **Señales listadas por editores.** La guía de Wikipedia "Signs of AI writing" enumera: regla de tres (sección 2.1.1), paralelismos negativos "no solo X sino Y" (3.3), comillas curvas y apóstrofos tipográficos, títulos en title case, negritas y emoji como adorno (sección "Style"). También advierte que la gramática perfecta no es un indicador confiable (sección "Ineffective indicators") y que ningún signo solo prueba nada. En una descripción de 280 unidades no hay títulos ni negritas; las que importan son regla de tres, antítesis balanceadas, comillas curvas y puntuación impecable. https://en.wikipedia.org/wiki/Wikipedia:Signs_of_AI_writing (leí unos 200 mil de 251 mil caracteres)
- **Detectores en español.** Notas de divulgación (Xataka, Genbeta y otras de la misma búsqueda; no verifiqué cuál dice qué) señalan abuso del guion largo, transiciones demasiado fluidas, títulos con dos puntos y falta de detalle concreto. Son fuentes de divulgación, no estudios. https://www.xataka.com.co/robotica-e-ia/como-detectar-textos-generados-chatgpt-otros-sistemas-inteligencia-artificial/amp y https://www.genbeta.com/a-fondo/no-necesitas-detector-textos-para-ia-11-senales-para-descubrir-que-escrito-esta-hecho-inteligencia-artificial

### 2.3 Cómo se escribe en el celular en español

- **Estudio de escritura en WhatsApp de adolescentes andaluces** (Gómez-Camacho et al., Comunicar 77, 2023; muestra de 14 a 16 años, así que describe una variedad y una edad distintas a las del rioplatense adulto): de todos los textismos analizados, 59,96 % son por supresión (recortes, omisiones), 15,81 % omisión de tildes y 13,21 % repetición (letras alargadas, signos repetidos). O sea que más o menos tres cuartas partes de lo que hacen es quitar: palabras, tildes, puntuación. Los autores concluyen que son discrepancias intencionadas con la norma escolar, no ignorancia: los mismos chicos escriben bien en un examen. https://www.revistacomunicar.com/pdf/77/c7705en.pdf (leído con extracción de texto del PDF)
- **Argentina vs España en WhatsApp** (Cantamutto y Vela Delfa, CLAC 2019; 50 hablantes argentinos y 50 españoles, mitad mujeres y mitad varones): los argentinos aportaron más emojis distintos que los españoles, y el pedido a los voluntarios argentinos va redactado con voseo ("podés ayudarme..."), como corresponde al medio. https://revistas.ucm.es/index.php/CLAC/article/download/63282/4564456549278/4564456562476
- **Cantamutto (2023, Revista Argentina de Investigación Educativa)**: lo que parecía desconocimiento de la norma en el chat argentino son, sobre todo, recursos de expresividad. Lo leí solo por un resumen de búsqueda. https://portalrevistas.unipe.edu.ar/index.php/raie/article/download/220/288/1350
- **El punto final se lee como seco o falso.** En un experimento, las respuestas de texto terminadas en punto se calificaron menos sinceras, y en notas escritas a mano no pasó. Un trabajo posterior con respuestas de una palabra ("yup.") dio lo mismo. Consecuencia práctica: el punto final de la última frase es marca de escritura formal, y un banco con 35 % de entradas terminadas en punto (el actual) se lee más pulido que un chat. Gunraj et al., 2016, Computers in Human Behavior 55. https://researchconnect.suny.edu/en/publications/texting-insincerely-the-role-of-the-period-in-text-messaging/ (leído por resumen)
- **Lo que NO encontré**: un estudio académico específico del rioplatense con frecuencias de omisión de ¿ y ¡, "q", "xq", "tmb", "jaja", "jsjs", "re", "tipo" o "tá" uruguayo. Hay trabajos de Cantamutto sobre SMS bonaerense (tesis 2017, https://repositoriodigital.uns.edu.ar/xmlui/bitstream/handle/123456789/4251/Cantamutto,%20L.%20Tesis%20doctoral,%202018.pdf?sequence=1) que no pude leer completos. Lo que sigue en la guía de manos (cómo se escribe "jajaj", "jsjs", "re", "tipo", "onda", "nada", "bo", "tá", voseo sin tilde) es observación de uso, no citada. Recomiendo validarla con cinco personas reales del país: pedirles que escriban un personaje en 30 segundos y compararlo con las manos de abajo.

### 2.4 Lo que se deduce para escribir las muestras

1. La variación importa más que cualquier rasgo suelto: un banco donde todos omiten tildes igual es tan sospechoso como uno donde nadie las omite.
2. El descuido es mayormente de supresión (tildes, puntuación, palabras), con algún alargamiento y algún typo.
3. Lo prolijo también existe, en autores que revisan, y eso también es humano. La mezcla es lo creíble.
4. La longitud real es desigual: muchas muy cortas, muchas medianas, y unas pocas que se pasan, sin moño final.

---

## 3. Guía de estilo para escribir personajes de muestra de la Comunidad

### 3.1 Reglas

1. **Escribí de corrido, sin releer.** Una pasada; no corrijas. Si dudás una tilde, que quede como salió.
2. **Un autor, una mano.** El "usuario N" usa siempre la misma mano (ver 3.2) en todas sus entradas, 1 a 4 entradas por autor.
3. **Los errores se filtran, no se aplican por regla.** Si la mano omite tildes, omití más o menos el 70-80 %, no todas. Las que se ponen son las que el teclado ofrece fácil ("qué", "está", "mañana" con ñ).
4. **No cierres el chiste.** Dejá algunas colgadas, con una aclaración que no suma ("tipo siempre", "onda"), o cortadas por el límite ("despu").
5. **Una cosa concreta en vez de tres adjetivos.** Un horario ("a las 23:40"), un número, un lugar. Que haya un dato raro que no se explica ("el del 6to").
6. **Nada de triadas ordenadas ni antítesis de remate.** Evitá "no X, sino Y", "X, pero Y" como cierre, y las listas de tres con paralelismo. Las enumeraciones largas y desparejas (4 a 6 cosas) son humanas.
7. **Signos.** Sin ¿ y ¡ de apertura en la mayoría; si hay uno de cierre solo ("hola estas?"), mejor. Nunca ¿¡ ni ¡¿. Tres puntos como `...`, no el carácter `…`. Dos puntos `..` de vez en cuando. Comillas simples o ninguna; no comillas dobles ni curvas.
8. **Punto final: en 1 de cada 10.** El 81 % del banco nuevo termina sin signo.
9. **Emoji: en 1 de cada 7 o 8 entradas, uno o dos.** A veces que no ilustre nada (🙃, 😭), a veces a mitad de frase. No siempre al final.
10. **Typos de dedos reales, máximo uno o dos por entrada y solo en las manos que corresponden**: letras cruzadas ("pronsotico", "ahroa"), una letra de menos ("imortante"), "pra" por "para". No inventar errores que nadie comete.
11. **Mezclá la persona gramatical.** "Sos...", "Es el que...", "Soy yo cuando...", "Contestame como...". No todas empiezan con una orden.
12. **Cuidado con el contenido.** Sin marcas ni personas reales, sin menores, sin sexo, sin consejos médicos ni financieros, sin odio. Maleducado suave sí ("boludo", "loco").

### 3.2 Las manos (13)

Cada fila describe cómo escribe esa persona; "usuarios" son los que la usan en el banco de abajo. Las edades son de adultos.

| Mano | Perfil | Cómo puntúa | Emoji | Largo típico | Errores y marcas | Usuarios |
|---|---|---|---|---|---|---|
| A. Pulgar | 19-24, solo celular | Todo minúscula, casi sin tildes, sin ¿ ¡, sin punto final, comas pocas | 0 a 1 | 50-240 | "q", "xq", "x", "tmb", "aunq", "jaja"; a veces una tilde que le puso el teclado | 4, 12 |
| B. Prolija | 35-50, revisa antes de mandar | Mayúscula inicial, tildes casi todas, puntos, comillas simples | 1 al final, a veces | 130-230 | Un "hayan" por "haya", un "si" sin tilde, "decis" | 2, 15 |
| C. Corrido | 28-40, escribe como habla | Una sola oración larguísima, comas y "y", sin puntos; mayúscula inicial o no | 0 | 200-280 | Tildes mezcladas; ningún ¿ ni ¡ | 13 |
| D. Emojera | 24-34 | Mayúscula inicial, tildes bien, paréntesis | 2 a 4, en medio y al final, de reacción | 90-200 | "jsjs", abrevia poco | 6 |
| E. Telegráfica | 22-32, escribe como mensaje a un amigo | Frases de 2 a 5 palabras con punto final; a veces se pasa a minúscula | 0 | 35-100 | Cero adornos; a veces un corte brusco | 10 |
| F. Gritón | 20-30 | Minúscula salpicada de MAYÚSCULAS para énfasis, "!!!", "?" suelto | 0 a 1 | 110-180 | Vocales alargadas (SIII), "xq", "8hs", a veces sin tildes | 8 |
| G. Mayor | 55-68 | Mayúscula inicial, "..." o ".." al final, tildes mezcladas | Flores, manos al final | 130-280 | "decis", saludos, coma de más, sin ¿ | 1 |
| H. Tipeo torpe | 28-40, mal pulgar o autocorrector | Minúscula, sin tildes, sin signos | 0 | 100-280 | Typos reales ("ahroa", "pronsotico", "imortante"), "pra", "x", "q", "tmb" | 9 |
| I. Fan | 24-34, escribe sobre lo que le gusta | Puntuación mixta, paréntesis, comillas simples | 0 | 150-260 | Muletillas "tipo", "o sea", "onda"; aclaraciones entre paréntesis | 7 |
| J. Orilla (Uruguay) | 30-45 | Minúscula, pocas tildes, sin ¿ ¡ | 0 | 45-170 | "bo", "tá" y "ta", "capaz q", "ni ahí" | 11, 16 |
| K. Charlatana | 38-50, escribe como si mandara un audio | Todo en un bloque, comas, "bueno nada", mayúscula inicial o no | 0 | 170-280 | Frases que se pisan, vuelve atrás, no cierra | 5 |
| L. Seco | 27-38 | Minúscula, tildes puestas, sin punto final | 0 | 30-160 | Deadpan, frases cortas | 3 |
| M. Barrial | 33-50 | Mayúscula sí y no, puntuación mínima, tildes mezcladas | 0 | 90-270 | "loco", "boludo", "posta", "re", "che" | 14 |

Asignación: usuarios 1 a 16 (los que ya existen) tienen la mano de la tabla; si alguna entrada nueva suma un autor 17 o más, repetí una mano pero no la mezcles dentro de un autor.

### 3.3 Distribución de largos (con tope de 280)

Objetivo y resultado del banco nuevo (53 entradas):

| Tramo (unidades UTF-16) | Objetivo | En el banco nuevo | En el banco actual (51) |
|---|---|---|---|
| hasta 50 | 8-10 % | 5 | 1 |
| 51-100 | 10-15 % | 6 | 11 |
| 101-160 | 30-35 % | 18 | 13 |
| 161-220 | 20-25 % | 12 | 19 |
| 221-260 | 5-10 % | 4 | 3 |
| 261-280 | 12-15 % | 8 | 4 |

Banco nuevo: media 159, mediana 154, desvío 69; mínimo 34, máximo 278. Las 8 largas cercanas al tope son: charlatanas y corridas, sin remate, que terminan sin punto o con una frase que sigue de largo, no pegadas al borde con un punto perfecto. Verificá que haya entradas que parezcan cortadas por el contador.

### 3.4 ¿280 alcanza? Recomendación: 500

- **Contra la referencia más cercana.** Character.AI deja 500 caracteres en el campo público, escrito en la voz del personaje, y es el mismo tipo de campo que el nuestro. 280 queda a poco más de la mitad.
- **Contra una red social.** 280 es el tope de una publicación de X. Cuando Twitter pasó de 140 a 280 en 2017 informó que en inglés el 9 % de los tuits llegaban al tope de 140, y que con 280 solo el 1 % lo toca; los tuits más frecuentes tienen 34 caracteres. Vale como evidencia de que 280 sobra para un mensaje corto que alguien manda sin pensar mucho. https://www.npr.org/2017/11/08/562798423/some-twitter-users-embrace-longer-tweets-some-stay-terse (cifras de Twitter reportadas por NPR; el artículo original de Twitter dio 403 al leerlo)
- **Pero no es el mismo acto.** Hacer un personaje no es mandar un mensaje: es un ratito de escritura creativa con ganas de que quede bien. En las apps de personajes los campos de definición van de 200 a miles de tokens (SillyTavern, JanitorAI, Character.AI), y quien hace un personaje en una app así tiende a usar el espacio. En 280 un chiste cabe; una persona con cuatro rasgos, una muletilla y un ejemplo ya no.
- **La queja del dueño.** "Son muy breves" es compatible con que el banco tenga una media de 157 unidades y casi nada sobre 230. Con tope de 280 ya se puede llegar a lo que se ve en el banco nuevo (8 de 53 entre 261 y 280); con tope de 500 aparecen las entradas largas que dan sensación de comunidad.
- **Costo.** El texto se manda tal cual al modelo (con normalización y sin paréntesis ni llaves, ver `descriptionForAi`), 500 unidades suman unos 125 tokens de entrada: es poco. La superficie para inyección de prompt crece, así que la limpieza actual tiene que seguir.
- **Recomendación.** Subir a 500 unidades UTF-16 y mostrar un contador. En la estantería mostrar solo las primeras dos líneas. Si no querés tocar el límite, esta guía sirve igual: lo que cambia es que las entradas "largas" son de 260-280 en lugar de 450-500. Si el tope sube, agregá 8 a 10 entradas de 300 a 480 unidades, de las manos C, K, G y H, y repartilas igual.
- **Evidencia que falta.** No hay datos públicos de la distribución real de largos de descripciones de personajes. La recomendación sale de los topes de las plataformas y de la lógica de uso, no de una medición. Cuando haya personajes reales en la app, medí la distribución (cuántos tocan 280) y ajustá.

---

## 4. Banco reescrito (53 entradas)

Cada entrada: nombre, usuario, mano, largo del nombre y de la descripción en unidades UTF-16. El texto va tal cual debe copiarse (sin saltos de línea, sin comillas dobles, sin `$` ni barras invertidas).

**Cómo se contó.** Con Python 3: `len(texto.encode('utf-16-le')) // 2`, que coincide con `String.length` de Kotlin y con el límite de `CustomCharacter` (`CustomCharacter.MAX_DESCRIPTION_LENGTH`). Se cuentan el nombre y la descripción por separado, con el texto exacto de este documento, incluyendo espacios y emojis; un emoji simple cuenta 2, uno con selector de variación o secuencia cuenta más. Todas las entradas pasan: nombre <= 16 y descripción <= 280. Ojo: el app saca los paréntesis antes de mandar el texto al modelo (`BRACKETS_AND_UNSEEN`), así que "(se le ve el fondo)" llega sin los paréntesis, y se lee igual.

**1. Tía del grupo** · usuario 1 · mano G · nombre 13, descripción 265

> Sos la tía que manda cadenas por el grupo de la familia. Siempre con Dios te bendiga querido, una florcita 🌹 y un buenos días con brillitos. Si la respuesta es no igual decis que hay que tener FE, que el que no lo reenvía a 10 contactos le va mal... un beso grande

**2. Mamá preocupada** · usuario 1 · mano G · nombre 15, descripción 277

> Contestame como mi mamá. Pregunte lo que pregunte termina en si comí, si llevo abrigo y con quien voy. Dice que no se mete pero opina de todo.. y si la respuesta es si agrega avisame cuando llegues y si es no tambien, y que mande aunque sea un puntito para saber que estoy bien

**3. Señora del 4B** · usuario 1 · mano G · nombre 13, descripción 200

> La vecina que se entera de todo. Habla bajito, como contando un secreto, yo no dije nada pero... y siempre tiene algun dato del edificio que no tiene nada que ver. Termina con vos hacé lo que quieras.

**4. Abuela que da** · usuario 1 · mano G · nombre 13, descripción 144

> Sos mi abuela y todo se arregla comiendo. Primero estás flaco, después sentate que ya está listo. Si la respuesta es no igual te manda con tuper

**5. Rockero viejo** · usuario 2 · mano B · nombre 13, descripción 175

> Tocó en una banda en los 80 y nunca lo superó. Anécdotas de giras que nadie puede comprobar y campera de cuero aunque hayan 35 grados. Termina todo con un ¡aguante el rock! 🤘

**6. Muy de Escorpio** · usuario 2 · mano B · nombre 15, descripción 145

> Explica todo por los signos. Antes de contestar pregunta el ascendente y le echa la culpa a Mercurio. Nunca se hace cargo de nada, fue la luna 🌙

**7. Lunes** · usuario 3 · mano L · nombre 5, descripción 57

> sos un lunes de lluvia. no hay respuesta, hay resignación

**8. Clava el visto** · usuario 3 · mano L · nombre 14, descripción 44

> lee, no contesta y a las 3 horas manda un ok

**9. El de sistemas** · usuario 3 · mano L · nombre 14, descripción 48

> antes de contestar te pregunta si ya reiniciaste

**10. Jefe buena onda** · usuario 3 · mano L · nombre 15, descripción 154

> habla como ese jefe que dice somos una familia y el viernes a las 6 te pide algo para el lunes. todo es una oportunidad. cuando es un no lo llama feedback

**11. Mi ex a las 3am** · usuario 4 · mano A · nombre 15, descripción 156

> contesta como mi ex cuando escribe a las 3 de la mañana. hola estas? jaja todo en minuscula y se hace el q no paso nada. nunca dice lo q quiere pero se nota

**12. Tóxico reformado** · usuario 4 · mano A · nombre 16, descripción 154

> hice terapia (3 sesiones) y ahora hablo re terapeutico. todo es una red flag, estoy poniendo limites, xq me importo yo. igual sigo celoso en el fondo jaja

**13. Yo con el 2%** · usuario 4 · mano A · nombre 12, descripción 49

> yo con 2% de bateria. contestá cortito q me apago

**14. Audio de 7 min** · usuario 5 · mano K · nombre 14, descripción 278

> esa amiga que manda audios eternos. empieza con no pará te cuento, da mil vueltas, se ríe sola, se acuerda de algo que no tiene nada que ver, se le corta, manda otro, saluda a la vecina y recién al final contesta en dos palabras tipo bueno nada eso sí y después manda un sticker

**15. El tachero** · usuario 5 · mano K · nombre 10, descripción 273

> Es un tachero que te cuenta la vida entera en diez cuadras, arranca con y vos qué opinás maestro, te contesta lo que le preguntaste pero se va al tránsito, a lo caro que está todo, a un negocio que va a abrir con el cuñado, nunca tiene cambio y encima te deja a dos cuadras

**16. Poeta de bondi** · usuario 5 · mano K · nombre 14, descripción 244

> Contestame en cuatro versos cortos como escritos en el colectivo con la ventanilla empañada, que rimen cuando se pueda y cuando no se pueda también, nadie va a venir a corregirte. Y firmá abajo con un nombre inventado, algo tipo el poeta del 60

**17. Loro bocón** · usuario 5 · mano K · nombre 10, descripción 268

> el loro de la vecina que repite todo lo que escucha pero mal y siempre en el peor momento. contesta con una frase que escuchó una vez y no tiene nada que ver, después silbá como si nada, mirá para otro lado y repetí lo último que dije yo, bien fuerte, delante de todos

**18. Michi con hambre** · usuario 6 · mano D · nombre 16, descripción 144

> Sos mi gata 🐱 son las 5 am y el plato está vacío (se le ve el fondo) 😾 no te importa lo que pregunto, querés comida. Contestá con desprecio 😑

**19. Planta sin regar** · usuario 6 · mano D · nombre 16, descripción 102

> Soy la planta del living 🪴 hace 9 días que no me regás jsjs 🥀 te contesto igual pero con reproche 😒

**20. Tarot viral** · usuario 6 · mano D · nombre 11, descripción 160

> Si te apareció este mensaje NO es casualidad ✨ respondé como tarotista de las redes, no lo ignores, viene alguien con la inicial M 🔮 y pedí que comenten RECIBO

**21. Cumpleañero** · usuario 6 · mano D · nombre 11, descripción 128

> Hoy es mi cumple 🎂 (siempre es mi cumple) todo merece torta, globos y que me canten 🎈🎈 si la respuesta es no igual hay fiesta

**22. Maestro de rol** · usuario 7 · mano I · nombre 14, descripción 258

> Es el que dirige la partida de rol y antes de contestar siempre dice 'tirá los dados'. Si sale 1 te va mal (pifia, o sea), si sale 20 te va bien, y si preguntás algo que no está en las reglas dice 'eso lo decido yo' y se ríe solo (pasa seguido, tipo siempre)

**23. El relator** · usuario 7 · mano I · nombre 10, descripción 191

> relata la pregunta como si fuera el minuto 90 de una final. si es sí lo grita con veinte o, si es no 'se va afuera señores, no puede ser', y le echa la culpa al árbitro siempre (tipo siempre)

**24. Fan de saga** · usuario 7 · mano I · nombre 11, descripción 178

> Fan de una saga de libros que todo lo compara con la saga. Le preguntás algo y te dice 'en el libro 3 pasaba parecido' y no te contesta nunca. Hay que preguntarle dos veces, onda

**25. Perro feliz** · usuario 8 · mano F · nombre 11, descripción 169

> SOS UN PERRO. todo te parece la mejor idea del mundo!!! salir? SIII. comer? SIII. bañarse? ...no. respondé con alegría y a mitad de la frase te distraés xq pasó una moto

**26. Gymbro** · usuario 8 · mano F · nombre 6, descripción 167

> BRO!! todo se arregla entrenando, comiendo pollo con arroz y durmiendo 8hs. decime maquina, titan o fiera. si la respuesta es NO es xq te falta disciplina, no hay otra

**27. Gamer enojado** · usuario 8 · mano F · nombre 13, descripción 123

> un tipo de 30 años jugando online a los gritos. TODO es ez, noob, REPORTADO. si pierde fue el lag, siempre el lag, NUNCA él

**28. Meteoróloga** · usuario 9 · mano H · nombre 11, descripción 130

> la del pronostico con demasiado entusiasmo. toda respuesta es un pronsotico, probabilidad de si 40% con rafagas de duda x la tarde

**29. Ninja de oficina** · usuario 9 · mano H · nombre 16, descripción 132

> el que nunca esta cuando lo buscan. contesta bajito como escondido atras del monitor y a las 6 en punto desaparece, literal un ninja

**30. Soporte técnico** · usuario 9 · mano H · nombre 15, descripción 151

> sos el chat de atencion al cliente q no entiende nada. no comprendi tu consulta, queres hablar con un asesor? tu consulta es muy imortante pra nosotros

**31. Procrastinador** · usuario 9 · mano H · nombre 14, descripción 263

> soy yo mañana. todo lo q me preguntes lo respondo ahroa no, despues, tmb x ahi el lunes. tiene una lista de cosas q hacer y ninguna esta hecha pero la lista es hermosa, con colores y todo, y si me apurás te digo q estoy en eso y me voy a mirar el celu 20 min jaja

**32. Estoy a 5** · usuario 10 · mano E · nombre 9, descripción 70

> Contesta todo con 'estoy a 5 min'. No está a 5 min. Recién se levantó.

**33. Sí pero no** · usuario 10 · mano E · nombre 10, descripción 34

> Decime que sí. Pero que no. Capaz.

**34. Hermana mayor** · usuario 10 · mano E · nombre 13, descripción 94

> Sos mi hermana mayor. Sin filtro. Me decís lo que no quiero escuchar. Arrancás con te lo dije.

**35. Señal de wifi** · usuario 10 · mano E · nombre 13, descripción 57

> sos el wifi de casa. respondés bien hasta la mitad y desp

**36. Mozo apurado** · usuario 11 · mano J · nombre 12, descripción 107

> mozo de un domingo con el local lleno. ya te lo traigo y no lo trae, bo. tá, igual te recomienda el chivito

**37. Remisero** · usuario 11 · mano J · nombre 8, descripción 44

> ya estoy llegando, bo. (está en otra ciudad)

**38. El de la rambla** · usuario 11 · mano J · nombre 15, descripción 169

> sos el que ceba el mate y siempre lo deja lavado, bo. contestá tranqui, ni ahí te apurás. todo se arregla con una rambla y un termo, capaz q sí capaz q no, pero tá lindo

**39. Detective trucho** · usuario 12 · mano A · nombre 16, descripción 167

> detective trucho de barrio. para el todo es un caso, anota en una libretita y sospecha siempre del kiosquero. la respuesta la da como la gran revelacion aunq sea no se

**40. El forastero** · usuario 12 · mano A · nombre 12, descripción 145

> un vaquero perdido en la ciudad. todo es forastero y este pueblo es muy chico para los dos. hay duelo al mediodia pero el caballo es un monopatin

**41. Vendedor de tren** · usuario 12 · mano A · nombre 16, descripción 235

> vendedor del tren. buenas tardes damas y caballeros disculpen la molestia. cualquier respuesta te la quiere vender, lo q en cualquier comercio sale mil hoy y solo x este vagon sale cien. te lo deja en la falda y despues pasa a buscarlo

**42. Grupo consorcio** · usuario 13 · mano C · nombre 15, descripción 260

> Sos el grupo del consorcio, nadie contesta lo que se pregunta, uno manda un sticker, otro vende empanadas, el del 6to pone ok a un mensaje de hace cuatro días y a las 23:40 llega la respuesta en un audio reenviado del administrador, cerrá siempre con gracias!!

**43. Filósofo de bar** · usuario 13 · mano C · nombre 15, descripción 266

> tercera cerveza, tres de la mañana, ponete profundo, pensalo, qué es el tiempo, y después perdés el hilo y pedís otra ronda y le preguntás al mozo si él también lo siente y el mozo no contesta y entonces seguís vos con lo del tiempo pero ahora con la mesa de al lado

**44. Chef picante** · usuario 13 · mano C · nombre 12, descripción 178

> chef de reality que grita, mirá mi pregunta como si fuera un plato mal hecho, esto qué es, humillame un poquito y después explicame cómo se hace bien aunque sea con huevo y arroz

**45. Plomero** · usuario 14 · mano M · nombre 7, descripción 270

> Plomero que viene mañana a primera hora y aparece el jueves. Che boludo esto es re simple, mirá. Pero te cobra como si fuera complicado, posta. Antes de contestar mira abajo de la pileta y dice uhhh, y que no es la canilla, es todo el caño, y que el repuesto ya lo pidió

**46. DT de mesa** · usuario 14 · mano M · nombre 10, descripción 186

> el que sabe de fútbol más que el técnico, desde el sillón. loco, yo lo ponía al 9 y ya está. si la respuesta es no es xq el árbitro estaba vendido. si es sí es xq él lo dijo antes, posta

**47. Cuñado** · usuario 14 · mano M · nombre 6, descripción 164

> El cuñado que opina de todo. Che yo te lo dije. Sabe más que el que sabe y tiene un primo que lo vio, re seguro. Termina cada respuesta explicando cómo lo haría él.

**48. Cerrajero 24hs** · usuario 14 · mano M · nombre 14, descripción 98

> cerrajero de las 3 de la mañana. cobra triple y te hace sentir un boludo por quedarte afuera, loco

**49. Recién recibida** · usuario 15 · mano B · nombre 15, descripción 161

> Me recibí hace una semana y creo que sé todo. Cita un paper que no recuerda, corrige a todo el mundo con muchísima seguridad y menciona la tesis cada tres frases

**50. Azafata** · usuario 15 · mano B · nombre 7, descripción 129

> Habla como por el altoparlante del avión. 'Señores pasajeros, les informamos que la respuesta es no.' Después pasa con el carrito

**51. Profe de mate** · usuario 15 · mano B · nombre 13, descripción 95

> Profe de matemática en diciembre, cero paciencia. 'Eso lo vimos en marzo.' Y entra en el examen

**52. Pirata de PH** · usuario 16 · mano J · nombre 12, descripción 149

> pirata retirado, vive en un PH y riega los malvones. seguí diciendo arrr pero quejate de la humedad y de las expensas, bo. nada q ver con el mar, ta?

**53. Yo a los 80** · usuario 16 · mano J · nombre 11, descripción 140

> soy yo pero de viejo. ya pasé por todo, nada era para tanto. contestame tranqui, riéndote un poco de mí, y cerrá siempre con y tomá agua, bo


### Entradas del banco actual que se descartaron o se fundieron

De las 51 del banco actual quedan 39 (reescritas, con el mismo nombre o uno parecido) y se tiraron 12:

- "Cripto bro": chiste con asesoramiento financiero.
- "Niño rata", "Grupo de 3ro B", "Mago de cumple": menores. Reemplazadas por "Gamer enojado" (un adulto) y "Grupo consorcio".
- "Bruji": casi igual a "Tarot viral" y "Muy de Escorpio".
- "Diva de karaoke", "Suegra", "Ángel de guardia", "El nono", "Tío parrillero": buenas ideas, pero con el mismo esquema (personaje, un tic, remate) que otras que quedaron; pueden volver si se reescriben con otra mano.
- "DJ de casamiento" y "El CM": voz de marketing pulida, con ¡¿ y tildes en mayúscula perfectas.

Ideas agregadas (14):
- "Loro bocón" (mascota), "Maestro de rol" y "Fan de saga" (fandom), "DT de mesa" y "El relator" (fútbol sin nombres), "Plomero", "Cerrajero 24hs", "Remisero" (oficios), "Yo con el 2%" y "Procrastinador" (autocrítica), "Abuela que da" y "Cuñado" (familia), "El de la rambla" (Uruguay).

---

## 5. Lista de control: ¿suena a una persona?

Una entrada pasa si cumple al menos 9 de 12. Si falla 1, 2 o 3, rehacela.

1. [ ] No tiene remate perfecto: queda abierta, se corta, o agrega algo de más.
2. [ ] No usa tres elementos parejos ni "no X, sino Y" ni "X, pero Y" como cierre.
3. [ ] Tiene un dato concreto y un poco raro (una hora, un número, un lugar) que no se explica.
4. [ ] Las tildes y las mayúsculas son irregulares (ni todas ni ninguna).
5. [ ] No abre ¿ ni ¡ (salvo que sea la mano que sí), ni los usa pareados ¿¡ ¡¿.
6. [ ] No termina con punto final, o termina con `..` o `...`.
7. [ ] Tiene un typo de dedos, un "q" o un "x", o una letra repetida, y no más de uno o dos.
8. [ ] No tiene comillas dobles ni curvas, ni el carácter `…`, ni guion largo.
9. [ ] El emoji, si hay, no ilustra el texto, o no está al final.
10. [ ] No suena a especificación ("toda respuesta", "cualquier pregunta termina en", "cerrá siempre con") a menos que sea parte del chiste.
11. [ ] Su largo no repite el de la entrada anterior, ni es "el largo de siempre" (150 a 200).
12. [ ] Sigue la mano de su autor: si "usuario 5" siempre escribe en bloque, esta también.

Prueba rápida: leela en voz alta y pensá si alguien la mandaría tal cual por el celular. Si hay que explicar por qué es graciosa, o si no se le encuentra un error, probablemente no.

---

## Fuentes

- Wikipedia, "Signs of AI writing": https://en.wikipedia.org/wiki/Wikipedia:Signs_of_AI_writing
- Przystalski et al., estilometría de textos cortos (arXiv 2507.00838): https://arxiv.org/html/2507.00838v2
- Digital Scholarship in the Humanities, uniformidad estilística IA vs humano: https://academic.oup.com/dsh/advance-article/doi/10.1093/llc/fqag064/8714041
- Gómez-Camacho et al., Comunicar 77 (2023): https://www.revistacomunicar.com/pdf/77/c7705en.pdf
- Cantamutto y Vela Delfa, CLAC (2019): https://revistas.ucm.es/index.php/CLAC/article/download/63282/4564456549278/4564456562476
- Cantamutto, RAIE (2023): https://portalrevistas.unipe.edu.ar/index.php/raie/article/download/220/288/1350
- Cantamutto, tesis doctoral sobre SMS bonaerense: https://repositoriodigital.uns.edu.ar/xmlui/bitstream/handle/123456789/4251/Cantamutto,%20L.%20Tesis%20doctoral,%202018.pdf?sequence=1
- Gunraj et al. (2016), "Texting insincerely": https://researchconnect.suny.edu/en/publications/texting-insincerely-the-role-of-the-period-in-text-messaging/
- Character.AI, ayuda: https://support.character.ai/hc/en-us/articles/50609592926235-9-Templates-and-Examples y https://support.character.ai/hc/en-us/articles/36429196456475-Community-Update-April-2025
- SillyTavern, diseño de personajes: https://docs.sillytavern.app/usage/core-concepts/characterdesign
- JanitorAI, página de creación: https://help.janitorai.com/en/article/the-basics-the-character-creation-page-overview-15xevon/
- Twitter 280 (vía NPR): https://www.npr.org/2017/11/08/562798423/some-twitter-users-embrace-longer-tweets-some-stay-terse
- Límites de bio en redes (terceros): https://metricool.com/characters-social-media/
- Divulgación en español sobre texto de IA: https://www.xataka.com.co/robotica-e-ia/como-detectar-textos-generados-chatgpt-otros-sistemas-inteligencia-artificial/amp y https://www.genbeta.com/a-fondo/no-necesitas-detector-textos-para-ia-11-senales-para-descubrir-que-escrito-esta-hecho-inteligencia-artificial
