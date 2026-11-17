# Comunidad: diagnóstico del banco de 128 personajes

Revisé las 128 entradas de `CommunityModule.kt` (40 autores de muestra) junto con la investigación anterior, `prompts-de-comunidad.md`. Los conteos salen de un script que recorre el archivo y mide los largos en unidades UTF-16, igual que `String.length`. Todo lo demás sale de leer cada entrada. El número entre paréntesis es la posición en el banco: la 1 es la primera de la estantería y la "más usada".

## Resumen

1. **El arreglo anterior tocó solo la superficie y dejó marcas nuevas.** La guía previa pedía tildes salteadas, nada de punto final, un dato concreto raro y una mano por autor. Se aplicó como plantilla. Resultado: 25 entradas traen una hora o una cifra (cinco nombres llevan "am"), 7 meten "tipo siempre" u "onda siempre" y en 10 un "bo." cierra la primera frase.
2. **Son fichas de casting.** 68 de 128 abren con una etiqueta ("Es el que...", "La vecina que...") o con una fórmula de prompt ("Sos", "Soy", "Contestá", "Habla como"). Cuentan el chiste en vez de hacerlo.
3. **El chiste es el primero que se le ocurre a cualquiera:** el plomero que no viene, Drácula y el ajo, Einstein y "todo es relativo", el gymbro y el pollo con arroz.
4. **Pocos chistes, repetidos muchas veces.** "Dice que llega y no llega" aparece 16 veces. "El aparato que falla", 10. Hay 22 oficios o servicios que atienden mal o hablan de más.
5. **Es el mundo de un adulto porteño, no el de alguien de 18 a 35 de cualquier país.** 57 entradas usan voseo y ninguna está escrita de tú, aunque la app habla de tú. Sobran colectivos, termotanques, ñoquis del 29 y la tele del almuerzo. Faltan citas, historias, roomies, entrevistas y memes.
6. **Lo que funciona pasa en el celular, y es lo que menos hay.** Hay 18 entradas sobre la vida en el teléfono (el ex de las 3am, el visto, el audio eterno, el autocorrector, el tarot de redes): de esas se conserva la idea de 12. De las otras 110, de 26.
7. **Hay riesgos reales.**
   - 7 entradas retratan a personas vivas reconocibles sin nombrarlas. El propio código lo buscó así: "a few of today by what they are known to say in public and never by name".
   - Una describe a un personaje de videojuego que tiene dueño.
   - Carl Sagan, Einstein y Dalí tienen herederos o instituciones que defienden su nombre.
8. **Veredicto:** conservar la idea en 38, reescribir 30 y descartar 60.

## 1. Patrones que se repiten

### 1.1 Cómo arrancan
- **Con una etiqueta en tercera persona (37).** Casi siempre lleva un "que" detrás: "Es el que dirige la partida de rol" (9), "es el verdulero que te dice" (14), "esa amiga que manda audios eternos" (35), "La vecina que se entera de todo" (80), "El cuñado que opina de todo" (85). Es la forma de una definición de diccionario o de una lista de "tipos de personas".
- **Con "Sos" o "Soy" (23).** Es la fórmula de quien sabe cómo se le habla a un modelo: "Sos mi gata" (2), "Soy Cleopatra" (4), "soy el despertador" (7), "soy el termotanque" (79), "soy el ventilador de techo" (101).
- **Con "Habla como..." o "Contesta/Contestame..." (8).**
- En total son 68 (53 %). Además, 70 repiten el nombre o su palabra clave en los primeros 40 caracteres, que es lo único que se lee en la fila (ver 1.9).

### 1.2 Cómo están armadas
Casi todas siguen el mismo molde: quién es, dos a cuatro conductas, una regla para el no y un remate.
- **Reglas para el resultado (18):** "si la respuesta es no...", "si es sí...", "si sale 1".
  - La cláusula "si la respuesta es no igual..." aparece textual en Tía del grupo, Abuela que da, Cumpleañero y Cocinero tano. "Si es no igual" aparece en El del Siuu y en Hincha popular. Son seis "personas" distintas con la misma frase.
  - Como la app sortea el sí o el no, a la IA esa regla no le sirve.
- **Lenguaje de especificación:** "siempre" en 28 entradas. En otras 28 se dice cómo arranca o termina cada respuesta ("arranca todo con... y al final se despide con", "cerrá siempre con", "Termina cada respuesta explicando..."). En 51 se dice cómo contesta.
- **Regla de tres en miniatura:** siete entradas son exactamente tres frases cortas: "Hierve. Clic. Silencio." (75), "No hay cambio. Ya cierro. No cierra." (122), "Todo es relativo. Hasta la respuesta. Pelo loco." (39). Pasa lo mismo en 29, 34, 66 y 90.
- **Acotaciones de escenario.** Sirven para un sketch filmado, pero la bola contesta seis palabras de texto, así que la IA las ignora o gasta las palabras en describirlas. Ejemplos:
  - "contesta de espaldas y con anteojos de sol" (6)
  - "contesta con aullidos" (95)
  - "contesta tirándose al piso" (100)
  - "contesta desde la puerta con la ventana abierta y cambiando de carril" (128)
  - "con el bombo... todo con disfraz" (92)
  - "Escanea la pregunta y la pasa por la caja" (109)
  - "tu respuesta llega con volumen y un poquito de spray" (125)
- **Frases de cabecera que no entran en seis palabras:**
  - "Señores pasajeros, les informamos que la respuesta es no" (61, nueve palabras)
  - "buenas tardes querido a todos" más "un beso grande para todos los que están en casa" (11)
  - "somos polvo de estrellas, o sea..." (13)
  - "buenas tardes damas y caballeros disculpen la molestia" (78)
  - "cuatro versos cortos" (97)

### 1.3 Cómo terminan
- **Con un remate de narrador que la bola no puede decir.** Ejemplos: "y al final te los regala" (14), "te contesta con una bolsa llena" (31), "y encima te deja a dos cuadras" (71), "y cobra el viaje" (74), "Y entra en el examen" (90), "Después pasa con el carrito" (61), "y al otro día lo mismo" (83). El chiste está escrito para quien mira la estantería, como cierre de sketch.
- **Con un tic de cierre:** "tipo siempre" u "onda siempre". 4 entradas terminan así y otras 3 lo meten en el medio. Son de tres autores distintos (usuarios 7, 20 y 31).
- **Con un emoji que ilustra lo que ya dice el texto.** 20 entradas llevan emoji: 🐱 en la gata, 🦇 en Drácula, 🍝 en los ñoquis, 🧉 en el termo, 🌹 en las señoras. Cuatro terminan en ✨.

### 1.4 El tipo de chiste
- **El primero posible.** Las 19 celebridades muertas, míticas o de libro se reducen a su dato de manual:
  - Cleopatra y el Nilo, Zeus y el rayo, Medusa y la mirada.
  - Sócrates y "no sabía nada", César y los idus de marzo, Einstein y "todo es relativo".
  - Newton y la manzana, Hamlet y la calavera, Dalí y los relojes.
  - Con los oficios pasa igual: el plomero tarda, el cerrajero cobra triple, el albañil dice "mañana arranco". Y con los arquetipos: la profe de yoga dice namaste, el gymbro come pollo con arroz.
- **Las pocas que dan un segundo paso, un detalle inesperado, son de lo mejor del banco:**
  - el pirata jubilado que se queja de las expensas (30)
  - "hice terapia (3 sesiones)" (65)
  - "dijiste llamar a mamá y puso una canción triste" (62)
  - "pedí que comenten RECIBO" (77)
- **Pocos chistes, repetidos:**
  - "Dice que lo hace, o que llega, y no": 16 entradas. Son Plomero, Albañil, Estoy a 5, Amiga puntual, Remisero, Repartidor, Ómnibus 7am, Mozo apurado, Compañero de TP, Amigo del dale, Eterno alumno, Procrastinador, Despertador, Hermano prestado, Kiosquero y Ulises.
  - "El aparato que falla": 10. Son Recalculando, Despertador, Aire split, Autocorrector, Termo vacío, Pava eléctrica, Termotanque, Ventilador, Señal de wifi y Heladera 2am.
  - "El oficio que atiende mal o habla de más": 22.
  - "Lo épico vuelto doméstico", con la misma humedad: Pirata de PH y Fantasma de casa.
- **Cuentan el chiste en vez de actuarlo.** Papá de chistes (56) explica que hace chistes malos y no trae ninguno. Chef picante (82) pide "humillame un poquito" sin una sola línea del chef.
- **Casi ninguno quiere algo de quien pregunta.** De esa idea parten los personajes de la propia app ("A style is not a vocabulary: it is someone who wants...", en `Characters.kt`).
  - Las entradas que funcionan la cumplen sin saberlo: la mamá quiere que avises, el vendedor quiere venderte, la hermana quiere que no te equivoques, el jefe quiere el informe el lunes.
  - Las demás son escenas u objetos sin deseo: Aire split, Pava eléctrica, Torta de oficina, Fútbol de jueves, Vecino taladro.
- **Un solo registro:** costumbrismo de sobremesa, del tipo "viste cuando...". Faltan los registros que más se comparten en redes: el roast, el absurdo, la autocrítica, el drama exagerado, la ironía y lo tierno.

### 1.5 Vocabulario
- **La jerga está puesta como sal, siempre en el mismo lugar.**
  - "bo / tá / ni ahí / capaz q" aparece en 13 entradas. En 10, "bo." cierra la primera frase: "el lobizón, bo.", "un duende de la casa, bo.", "el zorzal, bo.".
  - "che / loco / posta / boludo" aparece en 12. "Che boludo" está textual en tres entradas del mismo autor (17, 120 y 126), y "posta" en 7.
- **Muletillas en serie:**
  - "bueno nada" en 6 entradas, tres veces dentro de Verdulero.
  - "jsjs" en 6, siempre junto a un paréntesis o un emoji.
  - "8hs" en tres entradas gritonas, aunque no signifique nada ("8hs así", 58).
  - "querido / con cariño / un beso grande" en 7.
- **Lo que no está: el vocabulario de 18 a 35 en redes.** "red flag" aparece una vez y "bro" otra. No hay ningún "literal", "crush", "casi algo", "ghosting", "match", "historias", "POV", "cringe", "random" ni "delulu". Nadie mezcla inglés, que en ese rango es lo normal.

### 1.6 Largos
- El tope es 500. El mínimo es 23 (Pava eléctrica), la mediana 162, la media 176 y el máximo 463 (Verdulero).
- 74 de 128 caen entre 101 y 220, y solo 12 pasan de 280.
- **Las largas crecen enumerando** ("y que..., y que..., bueno nada, y que..."), sin sumar voz, una frase de ejemplo o el porqué del personaje: Verdulero 463, Eterno alumno 462, Diva de almuerzo 381, Don Quijote 367, Nikola Tesla 360, Albañil 351.
- **Las 12 de menos de 50 son tuits:** Kiosquero, Sí pero no, Aire split, Pava eléctrica, Fútbol de jueves, entre otras. El nombre es todo el chiste y a la IA no le dicen cómo suena el personaje.

### 1.7 Errores plantados y minúsculas
- **Los typos están en tres manos y son de los que el teclado corrige solo:** "reclaulando" (5), "pronsotico" (23), "imortante" (89), "ahroa" (108).
  - "pra" aparece 4 veces (7, 79, 89, 101) y es portugués coloquial. En español se acorta "pa".
  - En el celular, lo que sobrevive suele ser otra palabra real que puso el autocorrector, o una palabra que falta. Esto es observación propia.
- **Las minúsculas contradicen al propio editor.** 66 descripciones empiezan en minúscula y 62 no tienen ninguna mayúscula.
  - El editor de la app sugiere mayúscula al empezar cada oración de la descripción y en cada palabra del nombre (`KeyboardCapitalization.Sentences` y `.Words` en `CharacterEditor.kt`). Escribir todo en minúscula ahí es un gesto deliberado de pocos, no el descuido de la mitad.
  - Los 128 nombres están con mayúscula solo al principio ("Michi con hambre"), aunque el teclado los propone con mayúscula en cada palabra ("Michi Con Hambre"). Un banco real mezclaría las dos formas, y tendría alguno en mayúsculas o en minúscula.

### 1.8 Cuarenta autores, trece manos
Los 40 "usuarios" comparten 13 moldes, y varios autores distintos escriben con la misma plantilla:
- **6, 18 y 32:** "Soy X [emoji] dato jsjs (aparte) [emoji] regla ✨". Cleopatra, Dalí, Planta sin regar, Drácula, Amiga puntual y Primo en España.
- **22 y 37:** la enumeración con "bueno nada". Verdulero, Panadero 5am, Paraguas, Compañero de TP y Vecino taladro.
- **7, 20 y 31:** el paréntesis "(el de..., o sea...)" y el "tipo siempre". Maestro de rol, Carl Sagan, Nikola Tesla, Sherlock, Don Quijote, Gurú del éxito y Mago de barba.
- **11, 16 y 27:** la postal uruguaya con "bo, tá, ni ahí".
- **14, 23 y 33:** "che, loco, posta".

Además, cada autor escribe un solo tipo de cosa: el 34, cinco genios muertos; el 36, cuatro aparatos; el 11, seis postales uruguayas; el 1, seis señoras. Una persona real mezcla: quien hace a su gata también hace a su jefa y a su ex.

### 1.9 Lo que se ve en la estantería
- **La fila muestra el nombre y una sola línea de la descripción**, unos 40 caracteres (`maxLines = 1` en `CharactersScreen.kt`). 70 de 128 gastan esa línea en repetir el nombre: "Plomero" / "Plomero que viene mañana...", "Medusa" / "medusa, la de las serpientes...", "Colectivero" / "Colectivero del 60...".
- **Los nombres son rótulos de categoría.** 41 son de una sola palabra (Plomero, Cartero, Kiosquero, Termotanque, Duende, Newton). Ninguno usa un nombre propio inventado o un apodo, y ninguno lleva emoji. Los que enganchan cuentan una situación propia: "Mi ex a las 3am", "Yo con el 2%", "Tóxico reformado", "Clava el visto", "Audio de 7 min".
- **36 entradas llevan exactamente el color y el accesorio de un personaje de la app.** Michi con hambre es el Gato; Gymbro, DT de mesa, DT tranquilo, Gurú del éxito y Fútbol de jueves son el Coach. En la lista parecen copias.
- **Los usos bajan de 22.144 a 173 en pasos exactos de 173.** Una estantería real tiene unos pocos personajes muy usados y una cola larga casi sin uso.

## 2. Qué ideas valen y cuáles son relleno

**Criterio de la tabla.** Una idea se conserva si cumple cuatro condiciones:
- quien la lee la vivió;
- el personaje quiere algo de quien pregunta o trae un formato verbal reconocible (un anuncio de cabina, un pronóstico, un relato de gol);
- su voz entra en seis palabras;
- se entiende fuera del Río de la Plata.

Aun las que se conservan necesitan redacción nueva.

### 2.1 Con potencial real
- **Vida en el celular:** Mi ex a las 3am, Clava el visto, Audio de 7 min, Autocorrector, Señal de wifi, Yo con el 2%, Tarot viral, Receta en video, Playlist de 4am, El de sistemas, Recalculando.
- **Familia y amigos que existen en todos los países:** Tía del grupo, Mamá preocupada, Hermana mayor, Cuñado (meme fuerte también en España), Papá de chistes, Amigo del dale, Primo en España, Señora del 4B, Loro bocón.
- **Identidad de 18 a 35:** Tóxico reformado, Procrastinador, Recién recibida, Jefe buena onda, Gurú del éxito, Gymbro.
- **Voces que ya traen su sí y su no:**
  - Meteoróloga (porcentajes)
  - El relator (gol o afuera)
  - Azafata (anuncio de cabina)
  - Vendedor de tren (la oferta)
  - Maestro de rol (el dado de 20)
  - Guardavidas, una vez reescrito (bandera roja o verde)

### 2.2 Premisa sin chiste
El nombre lo dice todo y no hay voz en: Lunes, Aire split, Pava eléctrica, Kiosquero, Cartero, Fútbol de jueves, Hermano prestado, Capitán callado, Ninja de oficina, Torta de oficina, Cerrajero 24hs, Ñoquis del 29, Tortuga vieja, Vecino taladro, Paraguas, El de la rambla, Cantautor de tá y Sí pero no.

Lunes es justo el chiste que la ficha del Sarcástico prohíbe por genérico ("generic jokes about Mondays, coffee or wifi", en `Characters.kt`). Señal de wifi se salva solo por la forma, porque se corta a mitad de palabra.

### 2.3 Copias de personajes de la app
Perro feliz (Perro), Michi con hambre (Gato), Detective trucho y Sherlock (Detective), Gamer enojado (Gamer), Abuela que da (Abuela), Sócrates (Filósofo), Poeta de bondi (Poeta), Mago de barba (Místico) y Muy de Escorpio (Horóscopo). Siete de ellas llevan además el color y el accesorio del original.

### 2.4 Solo se entienden en el Río de la Plata (25)
- **Cultura y folclore local:** Gardel, Cantautor de tá, Murguista, Lobizón, El de la rambla, Ñoquis del 29, Termo vacío.
- **Vivienda y oficios con palabras locales:**
  - Pirata de PH (PH, expensas, malvones) y Grupo consorcio.
  - El tachero, Remisero y Kiosquero.
  - Colectivero ("del 60", "pasá para el fondo") y Ómnibus 7am.
  - Mozo apurado (chivito), Panadero 5am (facturas, medialunas de grasa) y Termotanque.
- **Fútbol local:** Hincha popular, Arquero potrero, y los tres que retratan a la selección argentina: Capitán callado, Zurdo humilde y DT tranquilo.
- **Tele y escenario argentinos:** Diva de almuerzo, Cocinero tano, Pop de estadio ("buenos aireeees").

**Falsos amigos que fuera de ahí cambian el sentido:**
- En México, "torta" es un sándwich (87).
- "Facturas" son las que se pagan (31).
- "Pava" es mala suerte en Colombia y Venezuela (75).
- "Heladera", "pileta", "canilla", "palta", "celu" y "living" se dicen de otra forma.
- "Aire de enero" (34) y "guardavidas en febrero" (124) son verano solo en el hemisferio sur. Lo mismo pasa con los exámenes de diciembre (90).

### 2.5 Chocan con cómo funciona la bola
- **Ordenan el resultado, que en realidad sortea la app:** "Decime que sí. Pero que no. Capaz." (66).
- **Contestan sin sí ni no:**
  - Sócrates "contesta todo con otra pregunta" (33).
  - Hamlet "a los veinte minutos te dice capaz" (123).
  - Clava el visto "no contesta" (59); se puede salvar con una voz seca.
- **Piden más de seis palabras:** Poeta de bondi (cuatro versos), y Azafata, Diva de almuerzo y Carl Sagan (frases de cabecera largas).

## 3. Qué sobra y qué falta para 18 a 35 de varios países

### Sobra
- **Río de la Plata.** 57 entradas usan voseo y ninguna está escrita de tú. El único "tú" es el Shakespeare que "se confunde entre el vos y el tú". El único español de España es el "vale y tío" que imita Primo en España. La app, en cambio, habla de tú ("Toca", "Pregunta", "Tienes" en `values-es/strings.xml`).
- **Temas repetidos:**
  - oficios y servicios: 22;
  - aparatos: 10;
  - llegar tarde: 16;
  - fútbol: 9, cuatro de ellos personas vivas.
- **Celebridades muertas, míticas o de libro: 19, y 17 son hombres.** Las dos mujeres son la vanidad (Cleopatra) y el peligro (Medusa).
- **Gente mayor vista desde afuera:** tía, mamá, abuela, la señora del 4B, la peluquera, el cuñado, el rockero de los 80, la diva de la tele, el DT.
  - Las mujeres aparecen sobre todo como madre, tía, abuela, vecina, peluquera, cajera o azafata.
  - Como amigas o pares de quien pregunta hay apenas cuatro: Audio de 7 min, Amiga puntual, Hermana mayor y Recién recibida.

### Falta
- **Otros países y el tú.** No hay ninguna voz de México, Colombia, Chile, Perú, Venezuela, España, el Caribe o Centroamérica. Hay ideas locales que viajan bien:
  - el "ahorita" mexicano, que puede ser en cinco minutos o nunca;
  - el "de una" colombiano para todo;
  - la tía que en cada reunión pregunta "¿y el novio pa' cuándo?";
  - el primo exitoso con el que te comparan;
  - la villana de telenovela;
  - el Chupacabras;
  - el vendedor ambulante del metro o del bus, sacado del vagón porteño.
- **Vida amorosa de 18 a 35:** el casi algo, el crush que contesta con un emoji, el ex que mira tus historias pero no escribe, la amiga que a todo dice "bloquéalo", el match de la app de citas que solo habla de sí mismo.
- **Cómo se usan las redes:**
  - la que decide todo con encuestas en historias (calza perfecto con una bola);
  - el que manda memes en vez de contestar;
  - la influencer de 300 seguidores que "les cuenta";
  - el POV y el storytime.
- **Vida adulta temprana:**
  - el roomie que se come tu comida y el dueño del depto que nunca arregla nada;
  - la de recursos humanos ("¿dónde te ves en cinco años?") y la reunión que podía ser un mail;
  - renunciar, mudarse, irse del país (solo está Primo en España).
- **Humor nativo de internet:**
  - animales de meme: el gato naranja de una sola neurona, la capibara que no se altera, el mapache que vive en tu basura, la paloma nerviosa;
  - el absurdo, la autocrítica y el drama exagerado.
- **Actitudes que atraen:** la amiga que te hypea en todo ("sí, reina, hazlo"), el hater cariñoso, el ángel y el diablito, tu conciencia a las 3am.
- **La voz de quien lo hizo:**
  - Ninguna de las 128 dice por qué existe o para quién es ("lo hice por mi gata", "para cuando no sabes si mandar el audio").
  - Ninguna le habla al lector ("tu ex", "tu jefa"), que es como suelen titularse los personajes en apps de este tipo.
  - Casi ninguna trae una frase de ejemplo con la voz.
  - Esto es observación propia, sin un corpus detrás. Conviene contrastarlo con la investigación de redes.

## 4. Riesgos

| # | Entrada | Riesgo | Nivel |
|---|---|---|---|
| 21 | El del Siuu | El festejo identifica a Cristiano Ronaldo, que está vivo. Una nota deportiva dice que tiene derechos sobre el festejo, pero no lo encontré en un registro oficial. | Alto |
| 25, 40 | Capitán callado, Zurdo humilde | Leídas juntas, describen a Lionel Messi. | Alto |
| 27 | DT tranquilo | Un DT argentino que habla despacio, defiende "el grupo" y toma mate se lee como Lionel Scaloni. "Partido a partido" es el lema de Diego Simeone. Los dos están vivos. | Alto |
| 11 | Diva de almuerzo | "La señora de la tele de los sábados, con la mesa larga" es Mirtha Legrand, viva a fines de octubre de 2026 según la prensa. | Alto |
| 19 | Voz de selva | "El abuelo inglés de los documentales de naturaleza" es David Attenborough, que cumplió 100 años en mayo de 2026. El formato de narrador funciona igual sin esa pista. | Medio |
| 118 | Cocinero tano | El cocinero italiano de la tele argentina que dice "mamma mia" se puede leer como una persona concreta y viva. Además se ríe del acento. | Medio |
| 63 | Cantautor de tá | Un cantautor uruguayo de voz suave que agradece "gracias gracias" puede señalar a alguien concreto. | Bajo-medio |
| 13 | Carl Sagan | La empresa de su viuda tiene la marca CARL SAGAN en EE. UU. En agosto de 2026 demandó a una empresa de IA por usar su voz en un anuncio. Para una app de IA es el peor candidato posible. | Alto |
| 39 | Einstein | La Universidad Hebrea de Jerusalén tiene registradas EINSTEIN y ALBERT EINSTEIN, licencia el nombre y se opone a registros ajenos (tiene una oposición abierta en EE. UU. desde 2024). | Medio-alto |
| 20 | Dalí | La Orden CLT/1079/2024 encarga a la Fundació Gala-Salvador Dalí administrar sus derechos de propiedad intelectual e industrial, marcas incluidas, y los de imagen. Los tribunales españoles discutieron hasta dónde llega el derecho de imagen. | Medio |
| 50 | Sherlock | Es dominio público en EE. UU. desde 2023 y en el Reino Unido desde hace más de dos décadas. Aun así, el legado de Conan Doyle sigue reclamando marcas y pidiendo licencias. | Bajo-medio |
| 18 | Nikola Tesla | El nombre es libre, pero la descripción alude a la marca de autos ("no el auto, aclara siempre"). | Bajo |
| 58 | Plomero saltarín | Bigote, gorra, saltos, monedas y pozos describen a un personaje de videojuego de Nintendo, pensado para que se reconozca. | Alto |
| 106, 81, 9 | Mago de barba, Fan de saga, Maestro de rol | Son genéricos. El riesgo es que la IA complete con un mago, una saga o un juego de rol que tiene dueño. Conviene escribir "una saga" o "el juego". | Bajo |
| 1 | Mi ex a las 3am | El "hola estas?" de madrugada es el mensaje para sexo casual, y "nunca dice lo q quiere pero se nota" lo insinúa. Hay que cambiar el motivo: quiere su buzo de vuelta, o te extraña. | Medio |
| 12 | Zeus | Su meme real son las infidelidades y los raptos, uno de ellos de un muchacho (Ganimedes). Mejor no desarrollarlo. | Medio |
| 123 | Hamlet | "Ser o no ser" es un monólogo sobre quitarse la vida: mala base para un personaje que ayuda a decidir. | Medio |
| 6 | Medusa | Si se reescribe, hay que quedarse en la mirada que petrifica. Su lectura como víctima lleva a la agresión sexual que cuenta Ovidio. | Bajo |
| 90 | Profe de mate | Diciembre y "entra en el examen" suenan a secundaria en el Cono Sur, o sea, a menores. Hay que pasarlo a la facultad. | Bajo-medio |
| 86 | Gurú del éxito | "Un curso a mitad de precio" queda al lado del consejo financiero y de la estafa. Que no hable de plata, ingresos ni inversiones. | Bajo-medio |
| 73, 37, 26, 63, 92, 104 | Las que contestan cantando | Invitan a la IA a citar letras reales con derechos. Hay que pedir canciones inventadas. | Bajo-medio |
| 48 | Filósofo de bar | Alcohol entre adultos, aceptable, siempre que no empuje a tomar ("pedís otra ronda"). El accesorio de botella también lo llevan Cuñado (85) y Primo en España (116), aunque sus textos no hablan de alcohol. | Bajo |
| 53, 102, 82 | Gymbro, Abuela que da, Chef picante | Pueden terminar opinando del cuerpo ("estás flaco", "te falta disciplina") o humillando a la persona. El blanco tiene que ser la pregunta. | Bajo |
| 65 | Tóxico reformado | Que el chiste sea la terapia mal usada y no los celos. | Bajo |
| 71 | El tachero | "Lo caro que está todo" lleva a hablar de política y economía. | Bajo |

No hay marcas en los nombres ni contenido de odio, drogas, apuestas o cripto. "Boludo" y "loco" entran en lo aceptado.

## 5. Las 128 entradas

Qué significa cada veredicto:
- **Conservar la idea:** la premisa sirve y se reescribe la voz.
- **Reescribir:** hace falta otro ángulo u otro chiste, o sacarle lo local o lo riesgoso.
- **Descartar:** es relleno, una copia, algo local sin arreglo o un riesgo.

| # | Nombre | Veredicto | Por qué |
|---|---|---|---|
| 1 | Mi ex a las 3am | conservar la idea | Gancho fuerte para 18-35; quitar la insinuación sexual del 'se nota' |
| 2 | Michi con hambre | reescribir | Copia al Gato de la app; el plato vacío es obvio |
| 3 | Detective trucho | descartar | Duplica al Detective de la app; 'trucho' y 'kiosquero' no viajan |
| 4 | Cleopatra | reescribir | Trivia de manual; su ángulo: los amores con César y Antonio |
| 5 | Recalculando | conservar la idea | Voz de GPS que todos reconocen; le falta un chiste propio |
| 6 | Medusa | reescribir | Solo el dato del mito y acotaciones físicas que no caben |
| 7 | Despertador | descartar | Otro 'después'; Procrastinador y Estoy a 5 ya lo cubren |
| 8 | Tía del grupo | conservar la idea | Existe en cada familia hispana; mostrarla con sus propias cadenas |
| 9 | Maestro de rol | conservar la idea | Nicho real de 18-35; el dado de 20 calza con sí/no |
| 10 | Grupo consorcio | reescribir | El caos del grupo de vecinos es universal; 'consorcio' no |
| 11 | Diva de almuerzo | descartar | Retrata a una conductora argentina viva y reconocible: límite duro |
| 12 | Zeus | descartar | Solo grita y tira rayos; su meme real roza lo sexual |
| 13 | Carl Sagan | descartar | Sus herederos tienen la marca y demandaron a una IA (2026) |
| 14 | Verdulero | reescribir | La voz de puesto ('¿qué le damos, reina?') viaja; hoy es relleno |
| 15 | Rockero viejo | conservar la idea | El tío rockero existe en todos los países; darle anécdotas propias |
| 16 | Perro feliz | reescribir | Copia al Perro de la app; mejor un chihuahua furioso |
| 17 | Plomero | reescribir | Su diagnóstico ('es todo el caño') sirve; sobra el 'che boludo posta' |
| 18 | Nikola Tesla | reescribir | Nombra la marca de autos; mejor su rencor con Edison |
| 19 | Voz de selva | reescribir | Formato de meme muy bueno; 'abuelo inglés' señala a alguien vivo |
| 20 | Dalí | descartar | Su fundación administra nombre e imagen; chiste de relojes y bigote |
| 21 | El del Siuu | descartar | Identifica a un futbolista vivo por su festejo: límite duro |
| 22 | Lunes | descartar | Chiste de lunes: el genérico que la ficha del Sarcástico prohíbe |
| 23 | Meteoróloga | conservar la idea | Habla en probabilidades: calza con sí/no en cualquier país |
| 24 | Recién recibida | conservar la idea | Cercana a 22-30; mostrarla citando su tesis, no describirla |
| 25 | Capitán callado | descartar | Evoca a un capitán vivo; un rasgo y ningún chiste |
| 26 | Gardel | descartar | Solo rioplatense; el chiste es la disputa por su cuna |
| 27 | DT tranquilo | descartar | Evoca a técnicos vivos ('partido a partido'); repite a Capitán callado |
| 28 | Ñoquis del 29 | descartar | Costumbre rioplatense; describe un ritual, no una voz |
| 29 | Estoy a 5 | conservar la idea | Universal y corto; que sea el único de 'llego tarde' |
| 30 | Pirata de PH | reescribir | Buen giro épico a doméstico; 'PH' y 'expensas' no viajan |
| 31 | Panadero 5am | descartar | Relleno sin chiste; fuera del Plata 'facturas' son otra cosa |
| 32 | Ulises | reescribir | La excusa épica para llegar tarde sirve; hoy resume la Odisea |
| 33 | Sócrates | descartar | Duplica al Filósofo; 'contesta con otra pregunta' choca con sí/no |
| 34 | Aire split | descartar | Tres frases sin voz; enero es verano solo en el sur |
| 35 | Audio de 7 min | conservar la idea | Los audios eternos se mandan en toda Latinoamérica y España |
| 36 | Mozo apurado | descartar | Otro 'promete y no cumple'; chivito y 'bo, tá' de decorado |
| 37 | Pop de estadio | reescribir | Floja así; mejor si siempre saluda a la ciudad equivocada |
| 38 | Julio César | reescribir | Citas de manual; su ángulo es la traición: '¿tú también, Bruto?' |
| 39 | Einstein | descartar | Nombre registrado por la Universidad Hebrea; chiste de taza |
| 40 | Zurdo humilde | descartar | Evoca a un futbolista vivo, el mismo de Capitán callado |
| 41 | Planta sin regar | conservar la idea | Planta pasivo-agresiva rinde en 18-35; cambiar la plantilla 'Soy X jsjs' |
| 42 | El forastero | reescribir | La voz de western sirve ('este pueblo es chico'); falta conflicto |
| 43 | Drácula | reescribir | Ajo, castillo y noche: lo primero que pensaría cualquiera |
| 44 | Chofer de app | reescribir | Universal; fundirlo con lo mejor de El tachero |
| 45 | Autocorrector | conservar la idea | Chiste nativo del texto: funciona dentro de seis palabras |
| 46 | Mamá preocupada | conservar la idea | 'Avisame cuando llegues' lo entiende cualquier hispanohablante |
| 47 | El relator | conservar la idea | Sirve en todos los países: el sí es un GOOOL |
| 48 | Filósofo de bar | conservar la idea | El borracho profundo de las 3am es universal y gracioso |
| 49 | Tortuga vieja | descartar | Lenta y sabia: el cliché exacto, sin chiste |
| 50 | Sherlock | descartar | Duplica al Detective; el legado aún reclama marcas y licencias |
| 51 | Compañero de TP | reescribir | El que no hace nada en grupo es universal; 'TP' no |
| 52 | Muy de Escorpio | reescribir | Repite al Horóscopo; mejor la intensidad escorpiana que Mercurio |
| 53 | Gymbro | conservar la idea | Arquetipo de redes; que no opine del cuerpo de quien pregunta |
| 54 | DT de mesa | descartar | El mismo sabelotodo que Cuñado; El relator ya cubre fútbol |
| 55 | Don Quijote | reescribir | Ícono de todo el idioma; hoy resume el libro, no habla |
| 56 | Papá de chistes | conservar la idea | Chistes malos de papá: universales; mostrar uno, no describirlo |
| 57 | Amiga puntual | descartar | Mismo chiste que Estoy a 5, con la plantilla 'jsjs ✨' |
| 58 | Plomero saltarín | descartar | Describe a un personaje de videojuego con dueño, para reconocerlo |
| 59 | Clava el visto | conservar la idea | El visto es universal; responde seco: 'ok', 'vemos' |
| 60 | Ninja de oficina | descartar | Un solo rasgo; el chiste se agota en el nombre |
| 61 | Azafata | conservar la idea | Voz universal: el anuncio de cabina calza con sí/no |
| 62 | Asistente de voz | descartar | Una anécdota buena, no una voz; fundir con Soporte técnico |
| 63 | Cantautor de tá | descartar | Solo uruguayo, puede señalar a un cantautor vivo, sin chiste |
| 64 | Termo vacío | descartar | Rioplatense y otro objeto que falla; no hay chiste |
| 65 | Tóxico reformado | conservar la idea | La jerga de terapia mal usada es meme vivo en 18-35 |
| 66 | Sí pero no | descartar | Da órdenes sobre el resultado, que la app ignora; sin voz |
| 67 | Yo a los 80 | conservar la idea | Tu yo de viejo relativizando todo: tierno y universal |
| 68 | Paraguas | descartar | Relleno con 'bueno nada'; ni chiste ni voz |
| 69 | Eterno alumno | descartar | Otro 'ya casi'; 462 caracteres de enumeración |
| 70 | Leonardo | reescribir | El genio que no termina nada es buen ángulo; sobra trivia |
| 71 | El tachero | descartar | 'Tachero' es porteño; lo bueno pasa a Chofer de app |
| 72 | Remisero | descartar | Copia de Estoy a 5; 'remisero' solo se dice en Argentina |
| 73 | Playlist de 4am | conservar la idea | Despecho de madrugada atrae a 18-35; que no cite letras reales |
| 74 | Albañil | descartar | El mismo chiste que Plomero: promete y no viene |
| 75 | Pava eléctrica | descartar | Tres palabras sin voz; 'pava' es mala suerte en Colombia |
| 76 | Hermano prestado | descartar | Lista de tres sin voz; otro 'ya te lo devuelvo' |
| 77 | Tarot viral | conservar la idea | Parodia exacta del tarot de redes; muy 18-35 en todos lados |
| 78 | Vendedor de tren | conservar la idea | El vendedor del vagón o del bus existe en toda Latinoamérica |
| 79 | Termotanque | descartar | Otro aparato que falla; 'termotanque' casi no se dice afuera |
| 80 | Señora del 4B | conservar la idea | Vecina chismosa universal; 'yo no dije nada pero' ya es voz |
| 81 | Fan de saga | reescribir | El fandom es muy 18-35; cuidar que no nombre franquicias |
| 82 | Chef picante | conservar la idea | El jurado que destroza tu plato atrae; sin nombrar programa ni chef |
| 83 | Vecino taladro | descartar | Es un ruido, no una voz; relleno con 'bueno nada' |
| 84 | Gamer enojado | descartar | Duplica al Gamer que la app ya trae |
| 85 | Cuñado | conservar la idea | El cuñado sabelotodo es meme en España y en Argentina |
| 86 | Gurú del éxito | conservar la idea | La parodia del gurú de redes rinde; que no hable de dinero |
| 87 | Torta de oficina | descartar | Es una escena, no una voz; en México 'torta' es sándwich |
| 88 | El de sistemas | conservar la idea | '¿Probaste reiniciar?' es universal y entra en seis palabras |
| 89 | Soporte técnico | reescribir | El bot de atención sirve; sacar typos plantados como 'imortante' |
| 90 | Profe de mate | reescribir | Diciembre y examen sugieren secundaria; pasarlo a la facultad |
| 91 | Cartero | descartar | Premisa sin chiste ni voz |
| 92 | Murguista | descartar | Solo uruguayo; acotaciones de escenario que no caben en texto |
| 93 | Yo con el 2% | conservar la idea | Universal; el apuro de batería encaja con respuestas cortas |
| 94 | Hermana mayor | conservar la idea | La verdad sin filtro de una hermana sirve para cualquier pregunta |
| 95 | Lobizón | descartar | Folclore rioplatense; 'contesta con aullidos' no da voz |
| 96 | Shakespeare | conservar la idea | Tragedia para preguntas triviales es buen motor; sobra Hamlet |
| 97 | Poeta de bondi | descartar | Pide cuatro versos, imposibles en seis palabras; duplica al Poeta |
| 98 | El de la rambla | descartar | Postal uruguaya sin chiste; 'bo, tá' de decorado |
| 99 | Cumpleañero | reescribir | Mejor como quien festeja 'su mes de cumple' en redes |
| 100 | Arquero potrero | descartar | Acotación física, no voz; el fútbol ya está cubierto |
| 101 | Ventilador | descartar | Otro aparato que falla, igual que otros tres del autor |
| 102 | Abuela que da | descartar | Duplica a la Abuela de la app con su mismo chiste |
| 103 | Profe de yoga | reescribir | Namaste y campanita es lo obvio; mejor su jerga de 'soltar' |
| 104 | Hincha popular | descartar | Fútbol repetido; 'la popular' es argentino y cantar no cabe |
| 105 | Cerrajero 24hs | descartar | Un rasgo; otro oficio que cobra de más |
| 106 | Mago de barba | descartar | Duplica al Místico; roza a magos de franquicias conocidas |
| 107 | Jefe buena onda | conservar la idea | Jerga corporativa universal para 22-35; buen motor de sí/no |
| 108 | Procrastinador | conservar la idea | Procrastinar es meme central de 18-35; quitar el typo 'ahroa' |
| 109 | Cajera de súper | descartar | Guion de caja sin actitud; otro oficio de servicio |
| 110 | Repartidor | descartar | Otro 'estoy llegando': repite a Estoy a 5 |
| 111 | Señal de wifi | conservar la idea | El corte a mitad de palabra es chiste nativo del texto |
| 112 | Fantasma de casa | reescribir | Fantasma que pide perdón es tierno; no repetir la humedad del pirata |
| 113 | Newton | descartar | Chiste de la manzana; una de cinco celebridades del mismo autor |
| 114 | Loro bocón | conservar la idea | El loro que repite lo peor es clásico en Latinoamérica |
| 115 | Duende | reescribir | El duende que esconde cosas viaja; darle voz de 'yo no fui' |
| 116 | Primo en España | conservar la idea | El que emigró y ya habla como allá es meme latinoamericano |
| 117 | Amigo del dale | conservar la idea | El que dice 'sí voy' y no va existe en todos lados |
| 118 | Cocinero tano | descartar | Puede señalar a un cocinero de TV vivo; estereotipo de acento |
| 119 | Receta en video | conservar la idea | El 'antes de empezar, suscribite' es humor de internet universal |
| 120 | Napoleón | reescribir | 'Retirada estratégica' sirve para el no; sobra el 'che boludo posta' |
| 121 | Fútbol de jueves | descartar | Una regla de fútbol cinco, no un personaje |
| 122 | Kiosquero | descartar | Tres frases sin voz; 'kiosquero' es rioplatense |
| 123 | Hamlet | descartar | 'Ser o no ser' trata del suicidio; además choca con sí/no |
| 124 | Guardavidas | reescribir | Banderas roja y verde calzan con sí/no; quitar el 'bo, tá' |
| 125 | Peluquera | descartar | Repite el chisme de Señora del 4B, del mismo usuario |
| 126 | Colectivero | descartar | Línea 60 y 'pasá para el fondo': solo porteño |
| 127 | Heladera 2am | reescribir | Meme universal sin voz; que la heladera te juzgue |
| 128 | Ómnibus 7am | descartar | Otro 'ya viene', solo uruguayo y con acotaciones físicas |

## Fuentes de la sección 4

- Einstein: [oposición de la Universidad Hebrea ante la USPTO](https://ttabvue.uspto.gov/ttabvue/ttabvue-91292758-OPP-1.pdf), [registro ALBERT EINSTEIN](https://trademarkdir.com/trademark/albert-einstein-78273213)
- Dalí: [BOE, Orden CLT/1079/2024](https://www.boe.es/boe/dias/2024/10/08/pdfs/BOE-A-2024-20386.pdf), [Artnet sobre el Supremo y el derecho de imagen](https://news.artnet.com/art-world/supreme-court-rules-dali-image-rights-535910)
- Carl Sagan: [marca CARL SAGAN](https://gleanmark.com/trademark/uspto-87311295), [demanda contra Luma AI, agosto de 2026](https://www.lawcommentary.com/articles/carl-sagan-rights-holder-sues-luma-ai-over-cosmos-voice-clip-in-ad)
- Sherlock: [CITMA](https://www.citma.org.uk/resources/sherlock-homes-has-entered-the-public-domain-what-does-it-mean-blog.html), [Slashfilm sobre el pleito por Enola Holmes](https://www.slashfilm.com/1539794/weird-lawsuit-netflix-enola-holmes-explained)
- Mirtha Legrand: [Última Hora, octubre de 2026](https://www.ultimahora.com/mirtha-legrand)
- David Attenborough: [OPB, 8 de mayo de 2026](https://www.opb.org/article/2026/05/08/david-attenborough-turns-100/)
- Festejo "Siu": [NSS Sports](https://www.nss-sports.com/en/news/42886/cold-palmer-trademark)

## Archivos

- Banco: `app-phone/src/debug/java/com/lorenzosuarez/magiceightball/phone/di/CommunityModule.kt`
- Investigación anterior: `docs/research/prompts-de-comunidad.md`
- Fila de la estantería (nombre y una línea, `maxLines = 1`, líneas 543 a 556): `app-phone/src/main/java/com/lorenzosuarez/magiceightball/phone/ui/CharactersScreen.kt`
- Mayúsculas del editor (líneas 350-351 y 387-388): `app-phone/src/main/java/com/lorenzosuarez/magiceightball/phone/ui/CharacterEditor.kt`
- Fichas de la app ("someone who wants", línea 6; "jokes about Mondays, coffee or wifi", línea 61): `core/domain/src/main/java/com/example/magiceightball/core/domain/policy/Characters.kt`
- Español de la app, escrito de tú: `app-phone/src/main/res/values-es/strings.xml`
