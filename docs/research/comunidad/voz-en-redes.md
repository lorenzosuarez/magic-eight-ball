# Cómo escriben y bromean en redes los hispanohablantes (2024-2026): investigación para reescribir el banco de la Comunidad

Material leído: el banco actual (`app-phone/src/debug/java/com/lorenzosuarez/magiceightball/phone/di/CommunityModule.kt`, 128 personajes) y la investigación anterior (`docs/research/prompts-de-comunidad.md`). Las fuentes están parafraseadas y no cito frases textuales. Los ejemplos son míos e imitan formatos que circulan. Cuando algo sale de observación mía y no de una fuente, lo marco como **(obs.)**.

## Resumen

1. El banco no falla por la ortografía. Falla por la materia prima. Son tipos de cuaderno de guionista contados desde afuera: 27 de los 128 nombres son solo un oficio (Verdulero, Plomero, Kiosquero, Colectivero…). Además, solo 1 de los 128 usa alguno de unos 20 formatos o palabras de internet actuales (POV, red flag, aura, NPC, lore, era, cringe…). Lo medí yo sobre `CommunityModule.kt`.
2. Lo que la gente comparte tiene cuatro rasgos: se reconoce, es simple, sorprende y es específico. Casi siempre trata de uno mismo, de un vínculo (mi ex, mi gata, mi jefe, el grupo de la familia) o de una situación mínima de todos los días.
3. El autodesprecio es el humor más frecuente en comentarios de TikTok. Lo gracioso es una transgresión inofensiva: pincha sin lastimar.
4. Los formatos vivos sirven como esqueletos de personaje: POV, "nadie: / yo:", banderitas, tier list, "estoy en mi era de", lore, NPC, aura, main character, imperio romano. Caducan rápido: TikTok ya dio por terminados *delulu* y *romantizar la vida* para 2026.
5. La región se nota más en la gramática (voseo, "tú tenés", vosotros, "cachái") que en el vocabulario. Juntar muchos modismos es la caricatura más fácil de detectar. Y la jerga que no encaja con quien habla le quita credibilidad.
6. A la IA la delata el conjunto más que cada texto. Un modelo puede escribir un chiste tan bueno como el de una persona, pero cien chistes suyos se parecen entre sí. El banco reparte 13 "manos" entre 40 autores, así que el mismo tic aparece en usuarios distintos.
7. La investigación anterior acertó con la uniformidad, las tríadas y el punto final. Se equivocó al tratar "lo humano" como errores de tipeo, y no estudió qué contenido hace la gente.
8. Las 20 reglas del final se resumen así: un vínculo o una escena, un detalle reconocible, un solo formato, una muletilla de tres palabras como mucho, el rastro de quién lo hizo y la gramática de su país. Sin tríadas y sin órdenes al modelo.

**Límites.** No encontré un corpus de descripciones de personajes escritas por usuarios en español. Muchos glosarios de jerga son periodísticos: confirman que una palabra circula, no cuánto se usa. No pude documentar "el de atrás" ni "no porque es tan real" como formatos. Validación sugerida: mostrar 10 descripciones a 5 personas por país y hacerles dos preguntas: si lo escribió una persona o una IA, y si lo probarían.

---

## 1. Qué hace gracioso y compartible un texto corto

### 1.1 Lo que dicen las fuentes

- **El humor es lo que más se consume.** En Chile, el 75,3 % de 400 usuarios de Instagram y TikTok eligió humor y memes como su contenido favorito. Quedó por encima de consejos (68,4 %), tutoriales (60 %) y noticias (48,6 %) ([The Clinic, 2024](https://www.theclinic.cl/2024/01/09/chile-memes-estudio-revela-humor-contenido-favorito-de-chilenos-en-redes-sociales-radiografia-digital/)).
- **Qué se viraliza.** Investigadores consultados coinciden en que circula lo identificable, simple y sorpresivo, con gente común y situaciones universales. El momento importa. Los chistes internos circulan menos ([Gizmodo, 2024](https://gizmodo.com/why-does-a-meme-go-viral-and-can-we-predict-it-2000526357)).
- **Especificidad, sí, pero que se entienda de un vistazo.** Para guionistas de comedia, el detalle específico es la clave ([Lit Hub sobre Jen Spyra, 2021](https://lithub.com/this-former-late-show-with-stephen-colbert-writer-has-some-tips-for-writing-humor)). En redes, en cambio, gana lo que se entiende en un segundo: una referencia que obliga a pensar hace que la gente siga de largo ([White Label Comedy, 2024](https://whitelabelcomedy.com/blog/clarity-beats-specificity-jokes)). Lo que funciona es lo específico que se reconoce al instante.
- **Lo hiperespecífico da pertenencia.** Funciona como chiste interno: quien lo entiende siente que pertenece ([Mozilla, Brozovsky, 2025](https://blog.mozilla.org/en/internet-culture/secret-language-of-memes-iykyk/)). La tensión con el punto anterior se resuelve con "el chiste interno que todos tenemos": el audio de 7 minutos, el "ok." con punto.
- **Autodesprecio e ironía mandan.** En 236 comentarios de TikTok sobre temas duros, el 38 % fue autodespreciativo, el 26 % irónico o meta, el 21 % de apoyo y el 15 % absurdo o nihilista. Los comentarios de la Gen Z son cortos, crudos y con emojis. Los de millennials son más narrativos, sarcásticos y con referencias ([UCLA, 2025](https://languagedlife.ucla.edu/?p=3523)).
- **Transgresión inofensiva.** Algo hace reír cuando a la vez está mal y no daña ([McGraw y Warren, 2010](https://leeds-faculty.colorado.edu/mcgrawp/pdf/mcgraw.warren.2010.pdf)). Por eso el "roast" funciona si pincha algo que uno eligió (releer el chat) y no lo que uno es. Lo segundo además está prohibido en la app.
- **Lo cotidiano es el material.** TikTok observa que su público convierte lo mundano en humor compartido y que las historias caóticas (el *lore* de un viaje que salió mal) pegan más que la fantasía. Para 2026 da por terminados #delulu, #romanticizing y el escapismo digital. En alza pone #lockedin, #joblife, el domingo a la tarde (#sundayscaries) y los comentarios con fotos de reacción ([TikTok Next 2026](https://ads.tiktok.com/business/library/TikTok_Next_2026_Trend_Report.pdf)).
- **Lo parasocial.** Fue la palabra del año 2025 de Cambridge e incluye el vínculo con personajes y con chatbots ([Cambridge, 2025](https://www.cam.ac.uk/news/cambridge-dictionary-reveals-word-of-the-year-2025)). Una bola que "te contesta" vive dentro de ese chiste.
- **El micro-diálogo es el formato estrella en español.** En perfiles de humor de Facebook, X e Instagram es el más frecuente entre los más compartidos. Hace reír por sorpresa, exponiendo torpeza o incomprensión, o por pensamiento lateral ([Portillo-Fernández, 2022](https://doi.org/10.15517/rfl.v48i2.50512)).

### 1.2 Los mecanismos, con ejemplos

| Mecanismo | Genérico (no pega) | Como lo escribe la gente (ejemplo mío) |
|---|---|---|
| Especificidad | mi jefe es pesado | mi jefe que contesta "ok." con punto a las 23:58 |
| "Me siento atacado" | el impuntual | el que dice "ya salgo" y todavía no se bañó |
| Escalada absurda | la heladera vacía | abro la heladera, nada, la cierro, la vuelvo a abrir por si se repuso sola, ahora le hablo |
| Autodesprecio | un personaje que da consejos | yo preguntándole a una bola mágica porque mis amigas ya me bloquearon el tema |
| Parasocial | una fan | el streamer dijo "buenas noches chat" y me lo tomé personal |
| Situación hiperespecífica | el que manda audios | el audio de 4:59 que arranca con "no, nada, una boludez" |
| Registro equivocado | vecino quejoso | comunicado oficial del consorcio, pero sobre quién se comió mi yogur |
| Objeto o mascota con rencor | una planta | la planta del living que ya sabe que la vas a dejar morir como a la anterior |

### 1.3 De qué hablan

Vínculos (ex, *situationship*, crush), trabajo (#joblife, #corporatemaxxing, el domingo a la tarde), el grupo de la familia, mascotas (las *beige flags* ya se aplican a gatos y amigos, [Time, 2023](https://time.com/6286130/tiktok-beige-flags)), batería, sueño, gimnasio y "lock in", astrología y tarot, fandoms sin nombres, nostalgia de internet viejo y chisme ("té", [El Español, 2024](https://elespanol.com/reportajes/20240518/diccionario-actualizado-boomers-hablar-entender-hijo-sin-flopear-dar-cringe/855164723_0.html)). No hablan de oficios vistos desde afuera **(obs.)**.

### 1.4 Cómo cambia según la plataforma

- **Comentarios de TikTok:** muy cortos, con emojis, de acuerdo ("real", "literal"), etiquetando a una amiga ([UCLA, 2025](https://languagedlife.ucla.edu/?p=3523)). Ahora también se comenta con fotos ([TikTok Next 2026](https://ads.tiktok.com/business/library/TikTok_Next_2026_Trend_Report.pdf)).
- **X:** frases sueltas, "nadie: / yo:", ironía, juegos de palabras **(obs.)**.
- **Instagram:** pie corto, minúscula, emoji al final **(obs.)**.
- **WhatsApp:** sin saludo, audios, stickers, cadenas reenviadas en el grupo de la familia **(obs.)**. El punto final se lee frío ([Gunraj et al., 2016](https://researchconnect.suny.edu/en/publications/texting-insincerely-the-role-of-the-period-in-text-messaging/)).
- **Reddit:** párrafos más largos, mejor ortografía, "edit:" al final **(obs.)**.
- **Chat de Twitch:** W, L y F en masa, emotes, copypastas ([Migaku](https://migaku.com/es/blog/language-fun/jerga-de-internet-en-ingles)).
- **Comentarios de YouTube:** minuto exacto más la reacción ("2:14 me mató") **(obs.)**.
- **Una descripción de personaje de la Comunidad** se lee como una mezcla de bio de Instagram y comentario de TikTok: corta, en primera persona y con un guiño a quien la lee **(obs.)**.

---

## 2. Formatos de humor en uso y cómo volverlos personaje

Para cada formato doy qué es, un ejemplo mío y un personaje posible. El nombre del personaje cabe en 16 unidades (medido). Indico también cómo diría sí y no en 6 palabras o menos.

**POV:** abre con "POV:" y pone al lector dentro de una escena ([Know Your Meme](https://knowyourmeme.com/memes/pov-memes)). Su sentido se fue desdibujando con el uso ([Bustle](https://www.bustle.com/life/what-does-pov-mean-on-tiktok)).
- Ejemplo (MX): *pov: son las 5:52 de un viernes, le preguntas si ya te puedes ir y te dice "lo vemos el lunes"*
- Personaje: **Jefe un viernes**. Sí: "va, pero mándame un correo". No: "lo vemos el lunes".
- Vigencia: estable, pero gastado como chiste solo. Sirve como primera línea.

**Nadie: / Yo:** estructura para observaciones que se burla de lo que nadie pidió. Know Your Meme la describe como un armado simple para chistes de observación ([KYM](https://knowyourmeme.com/editorials/memescape-enabler-formats)). Nació en Twitter en 2018 ([espejo de KYM](https://meme.com/memes/no-one-nobody)).
- Ejemplo (ES): *nadie: / mi gato a las 4am: carrera por el pasillo y luego me mira como si la rara fuese yo*
- Personaje: **Gato 4am**. Sí: tira algo de la mesa. No: te da la espalda.
- Vigencia: viejo, pero se entiende al instante. Es un micro-diálogo.

**Red flag / green flag / beige flag:** señal de alerta, señal buena, y la beige, que es una manía rara e inofensiva. Ejemplo clásico de beige: poner un temporizador de 6 horas en vez de una alarma ([Merriam-Webster](https://www.merriam-webster.com/slang/green-flag); [Time, 2023](https://time.com/6286130/tiktok-beige-flags)).
- Ejemplo (CO): *mi amiga que todo lo clasifica en banderitas, hasta el almuerzo. le cuentas algo y antes de terminar ya te dijo "beige flag pero lo permito"*
- Personaje: **Red flag o no**. Sí: "green flag, dale". No: "red flag amiga 🚩".
- Vigencia: roja y verde están en todos los glosarios. La beige da material más fresco.

**"Es tan real" / "literal" / "real" / "factos" / "x2":** comentarios de acuerdo total. En España, "literal" significa "estoy de acuerdo" ([El Español, 2024](https://elespanol.com/reportajes/20240518/diccionario-actualizado-boomers-hablar-entender-hijo-sin-flopear-dar-cringe/855164723_0.html)). "Real" se usa como "de verdad" ([Ara, 2026](https://es.ara.cat/lengua/charca-tienes-mil-aura-10-expresiones-generacion-z-no-entiendes_1_5666005.html)). "Factos" ([Tunota, 2026](https://www.tunota.com/entretenimiento/palabras-chaviza-significado-expresiones-redes-sociales-2026-10-03)). "x2" es "yo también" ([ENTER.CO, 2025](https://www.enter.co/cultura-digital/el-popurri/tambien-dices-aesthetic-asi-esta-cambiando-nuestra-forma-de-hablar-en-2025-hablamos-peor-o-solo-hablamos-distinto/)). La frase completa "no porque es tan real" no la encontré documentada. El arranque "no porque literal…" lo veo en comentarios **(obs.)**.
- Ejemplo (PE): *mi amiga que comenta "es tan real" hasta en una foto de mi pared*
- Personaje: **La que te apoya**. Sí: "factos". No: "no, pero te entiendo 😭".

**Delulu:** viene de *delusional*. Nació en fandoms de K-pop a comienzos de los 2010 ([Merriam-Webster](https://www.merriam-webster.com/slang/delulu)). TikTok lo puso en "lo que ya fue" para 2026 ([TikTok Next 2026](https://ads.tiktok.com/business/library/TikTok_Next_2026_Trend_Report.pdf)).
- Ejemplo (AR): *te vio en línea = está pensando en vos. likeó una historia de hace 3 semanas = casamiento*
- Personaje: **Amiga delulu**. Sí: "es una señal". No: "todavía no, pero es señal".
- Vigencia: ponlo en un autor de 28 a 35 años un poco atrasado. Eso también es realismo.

**Main character:** vivir como protagonista de tu propia película. Se usa para la confianza dramática o la autoimportancia ([Merriam-Webster](https://www.merriam-webster.com/dictionary/MCE); [Dictionary.com](https://www.dictionary.com/e/pop-culture/main-character/)). En España, *main character* es el egocéntrico y lo opuesto a NPC ([Ara, 2026](https://es.ara.cat/lengua/charca-tienes-mil-aura-10-expresiones-generacion-z-no-entiendes_1_5666005.html)).
- Ejemplo (ES): *narra tu pregunta como una peli donde tú eres la prota y los demás son extras. y entonces, bajo la lluvia, supo que sí*
- Personaje: **Protagonista**. Sí y no con voz de tráiler.

**"El de atrás":** no encontré uso documentado. No lo usaría hasta que alguien confirme qué significa.

**Literal yo:** tomar un personaje ajeno como autorretrato ([espejo de KYM](https://meme.com/memes/literal-me)).
- Ejemplo (CL): *soy yo a la 1am con 4% de batería preguntándole cosas a una bola mágica en vez de dormir. literal yo wn, no me juzguen*
- Personaje: **Literal yo**. Sí: "dale, total mañana duermo". No: "no, y ándate a dormir".

**"Me sentí atacada":** el comentario a un post que te describe demasiado bien. En inglés está registrado como meme autoconsciente en el archivo de folclore de USC (https://folklore.usc.edu/?p=32354; solo pude leer el resumen de búsqueda). El uso en español es **(obs.)**.
- Ejemplo (AR): *un horóscopo que te conoce demasiado. no predice, te expone: "sí, pero dejá de releer el chat"*
- Personaje: **Horóscopo tóxico**. El pinchazo es por algo elegido, nunca por el cuerpo ni la identidad.

**"Cuando…" / "Yo cuando…":** una situación más la reacción **(obs.)**. Encaja con el domingo a la tarde que TikTok marca en alza ([TikTok Next 2026](https://ads.tiktok.com/business/library/TikTok_Next_2026_Trend_Report.pdf)).
- Ejemplo (MX): *yo un domingo a las 7pm cuando me acuerdo que existe el lunes. todo lo que me preguntes me da bajón*
- Personaje: **Yo un domingo**.

**Tier list:** ranking de S a F. Nació en comunidades de juegos de pelea a mediados de los 2000 y después se aplicó a todo ([Inverse](https://inverse.com/gaming/tier-list-meaning-definition-origins-video-games); [KYM](https://knowyourmeme.com/memes/tier-lists)).
- Ejemplo (AR): *no te dice sí o no, te dice en qué tier está tu pregunta. S tier: hacelo ya. F tier: ni en pedo*
- Personaje: **Rankea todo**.

**-core:** sufijo que viene de *hardcore* y convierte cualquier palabra en una estética: cottagecore, goblincore, normcore ([Dictionary.com](https://www.dictionary.com/e/core-words-cottagecore/)).
- Ejemplo (UY): *la estética señora de 60: barre la vereda a las 8 y antes de contestar te dice ay m'hija, tú sabés que…*
- Personaje: **Señora-core**.

**"Estoy en mi era de…":** una etapa con nombre, inicio y fin. Llegó al español en 2023 y 2024. Dicha sobre uno mismo es autocrítica con humor ([Cultura Colectiva](https://culturacolectiva.com/arte/letras/que-significa-estar-en-mi-era/)).
- Ejemplo (CO): *estoy en mi era de cuidar mi paz, entonces todo es no. salvo que haya torta, ahí lo hablamos*
- Personaje: **En mi era de no**.

**Lore / evento canónico:** "lore" ya es cualquier historia de fondo. "Evento canónico" es algo que te toca vivir sí o sí ([El Español, 2024](https://elespanol.com/reportajes/20240518/diccionario-actualizado-boomers-hablar-entender-hijo-sin-flopear-dar-cringe/855164723_0.html)).
- Ejemplo (ES): *todo lo que te pasa es un evento canónico: "tienes que vivirlo tía, es parte de tu arco"*
- Ejemplo (VE): *chamo, todo lo explica con el lore de su familia: el tío y la parrilla del 2016, la abuela y el tupper. nadie entiende pero es burda de importante*
- Personajes: **Evento canónico**, **El lore familiar**.

**NPC:** personaje no jugable. Se dice de quien repite lo mismo sin iniciativa ([Tunota, 2026](https://www.tunota.com/entretenimiento/palabras-chaviza-significado-expresiones-redes-sociales-2026-10-03)).
- Ejemplo (PE): *es un npc de supermercado con dos líneas: "¿algo más?" y "pase por la otra caja". si le insistes te las repite igualito*
- Personaje: **NPC del súper**.

**Aura (+1000 / -500):** puntos imaginarios de carisma ("farmear aura", "perdió aura"). En Perú se dice "más mil de aura" ([UDEP, 2025](https://www.udep.edu.pe/castellanoactual/?p=23438); [Ara, 2026](https://es.ara.cat/lengua/charca-tienes-mil-aura-10-expresiones-generacion-z-no-entiendes_1_5666005.html)).
- Ejemplo (CL): *a cada pregunta le suma o le resta aura. +500 si es que sí, -1000 si preguntaste algo que ya sabíai*
- Personaje: **Contador de aura**.
- Riesgo: es jerga muy adolescente y su ola viral salió de videos con chicos. No hay que mencionar ninguno. En un adulto suena irónico y caduca rápido.

**"Mi imperio romano":** algo en lo que pensás todos los días. Viene del trend sobre cuánto piensan los hombres en el Imperio romano ([El Español, 2024](https://elespanol.com/reportajes/20240518/diccionario-actualizado-boomers-hablar-entender-hijo-sin-flopear-dar-cringe/855164723_0.html)).
- Ejemplo (ES): *su imperio romano es el táper que su abuela le prestó a la vecina en 2019. le preguntes lo que le preguntes acaba ahí*
- Personaje: **Imperio romano**.

**Parasocial** ([Cambridge, 2025](https://www.cam.ac.uk/news/cambridge-dictionary-reveals-word-of-the-year-2025)):
- Ejemplo (MX): *está segura de que el cantante le contestó una historia (fue un corazón automático). habla de él como si fueran compadres*
- Personaje: **Fan número uno**. Nunca con el nombre de alguien real.

**Micro-diálogo** ([Portillo-Fernández, 2022](https://doi.org/10.15517/rfl.v48i2.50512)):
- Ejemplo (CO): *-ma, tengo una pregunta -¿ya comiste? -tengo 34 años -¿y eso qué tiene?*
- Personaje: **Mamá en el chat**. Con guion corto: en el celular nadie escribe la raya larga.

**Chat de stream:** W es victoria y L es derrota ([Migaku](https://migaku.com/es/blog/language-fun/jerga-de-internet-en-ingles)). Que se escriban en masa como aplauso o abucheo es **(obs.)**.
- Ejemplo: *contesta como el chat de un stream con 6 viewers: W, L, F, alguien pidiendo saludo y otro que llegó tarde preguntando qué pasó*
- Personaje: **Chat de stream**.

**Charca:** quien se quedó en una moda pasada sin darse cuenta ([Ara, 2026](https://es.ara.cat/lengua/charca-tienes-mil-aura-10-expresiones-generacion-z-no-entiendes_1_5666005.html)). Convierte la jerga vieja en el chiste.
- Ejemplo (ES): *se quedó en 2016: dice "ola k ase" y pide like si lloraste, sin ironía*
- Personaje: **La charca**.

---

## 3. Marcadores de escritura por región

### Lo que vale para todos

- **Anglicismos compartidos:** cringe, random, red flag, POV, NPC, mood, aesthetic, crush, slay, x2. Se propagan a la vez en México, Colombia y España por los algoritmos ([ENTER.CO, 2025](https://www.enter.co/cultura-digital/el-popurri/tambien-dices-aesthetic-asi-esta-cambiando-nuestra-forma-de-hablar-en-2025-hablamos-peor-o-solo-hablamos-distinto/); [UDEP, 2025](https://www.udep.edu.pe/castellanoactual/?p=23438)).
- **Emoji y edad:**
  - En 2025, 😭 fue el emoji más usado del mundo (más del 6 % de los usos), seguido de 🤣 y 😂, sobre 665 millones de emojis de Gboard y sin datos de edad. Los jóvenes usan 🙏 en lugar de 👍 y 🔥 en lugar de 💯. 💀 tuvo su pico en 2022 y 2023 y después bajó ([Deseret, resumiendo al NYT](https://www.deseret.com/lifestyle/2026/08/13/the-most-popular-emojis/)).
  - Algunos jóvenes leen 👍 como frío. Otros significados: 🫠 vergüenza o agotamiento, 🙂 seco o irónico, 🤡 "hice el ridículo", 🧍 "me quedé duro" ([Publimetro, 2026](https://www.publimetro.co/tecnologia/2026/08/25/el-diccionario-oculto-de-los-emojis-que-la-generacion-z-usa-y-que-probablemente-tu-ni-siquiera-conoces/)).
  - ✨ rodeando una palabra marca ironía **(obs.)**.
- **Puntuación:**
  - El punto final se percibe como menos sincero ([Gunraj et al., 2016](https://researchconnect.suny.edu/en/publications/texting-insincerely-the-role-of-the-period-in-text-messaging/)).
  - Casi nadie abre con ¿ ni ¡, y la minúscula se lee casual **(obs.)**.
- **Risa:**
  - "jajaja" es universal, y la intensidad se marca con el largo y las mayúsculas **(obs.)**.
  - "JAJSJAJS" es el teclazo de la carcajada real **(obs.)**.
  - Los usuarios más jóvenes ven "xd" como viejo ([Xataka Móvil, 2025](https://www.xatakamovil.com/movil-y-sociedad/misterio-xd-whatsapp-significa-dios-equisde)), aunque sigue vivo en Chile, Perú y entre gamers **(obs.)**.
- **Fuera del banco:** "skibidi" y "six seven" figuran en diccionarios para padres de 2026 como jerga infantil ([El Rancagüino, 2026](https://www.elrancaguino.cl/2026/09/19/cringe-npc-delulu-y-ghostear-el-diccionario-para-entender-la-jerga-digital-de-ninos-y-adolescentes/)). Remiten a menores.
- **Cómo se abre y se cierra un mensaje:** casi nunca con saludo. Se arranca con un vocativo o directo al tema, y se cierra con risa, un emoji o nada **(obs.)**.

### México

1. Tuteo, con "usted" para mayores o para marcar distancia **(obs.)**.
2. "wey": la Academia Mexicana prefiere "güey", pero en lo digital domina "wey". Es coloquial y un poco vulgar ([UnoTV](https://www.unotv.com/cultura/guey-o-wey-como-se-escribe-y-cual-es-su-verdadero-significado/)).
3. "neta" y "¿neta?": verdad, ¿en serio? ([Mexico News Daily, 2024](https://mexiconewsdaily.com/mexico-living/get-la-neta-with-our-guide-to-mexican-slang)).
4. "bien" como intensificador ("bien cansada"), "qué oso", "no manches" (o "no mames", más vulgar), "ahorita" **(obs.)**.
5. Siglas: "x2", "ntp", "nmms" y "alv" (vulgar) **(obs.)**.
6. "chamba" y "chambear"; "chambeadora" es muy trabajadora ([Cultura Colectiva](https://culturacolectiva.com/arte/letras/que-significa-chambear/)).
7. 💀 equivale a "me muero de risa", con variantes locales como "me mató" o "ya me llevó". Repetirlo intensifica ([Cultura Colectiva, 2026](https://culturacolectiva.com/arte/letras/que-significa-el-emoji-de-calavera/)).
8. Risa: "jajaja"; "jsjsjs" entre jóvenes; "JAJSJAJS" para la carcajada **(obs.)**.
9. Abre con "oye", "amiga" o "wey". Cierra con "va", "sale", "bye", "tqm" o sin cierre **(obs.)**.

**Caricatura:** "órale", "ándale" y "híjole" juntos, más "wey" cada tres palabras, suena a película gringa. "Chido" ya suena algo mayor **(obs.)**.

### Argentina

1. Voseo siempre: "vos tenés", "contame", "fijate". "Tú tienes" suena a doblaje **(obs.)**.
2. "re" como intensificador, documentado en X en Buenos Aires, Rosario, Mar del Plata y Santa Fe ([tesis de Utrecht](https://studenttheses.uu.nl/handle/20.500.12932/36058)).
3. "boludo/a" como trato de confianza, que convive con el insulto ([Šmídová, 2017](https://dokumenty.osu.cz/ff/journals/studiaromanistica/17-1/SR_1_2017_11_Smidova.pdf)).
4. "che", "posta", "nada" ("bueno nada", "nada, eso"), "tipo", "onda" **(obs.)**.
5. Intensificadores jóvenes: "mal" ("me encantó mal"), "una banda", "alto plan" **(obs.)**.
6. "ahre" marca que algo es en broma (la función es **obs.**). Está documentado en adolescentes de Buenos Aires ([De Luca, 2021](https://dialnet.unirioja.es/descarga/articulo/9127762.pdf)), así que en un adulto suena joven: una vez como mucho.
7. Risa: "jajaja", "JAJAJAJ" en mayúsculas, "jsjs"; "xd" más de gamer **(obs.)**.
8. En un estudio de WhatsApp, los argentinos usaron más emojis distintos que los españoles ([Cantamutto y Vela Delfa, 2019](https://revistas.ucm.es/index.php/CLAC/article/download/63282/4564456549278/4564456562476)).
9. Abre con "che", "amigo/a", "holis" o directo. Cierra con "beso", "besis", "abrazo", "dale" **(obs.)**.

**Caricatura:** "che", "boludo" y "posta" en la misma línea. El banco ya lo hace: "posta" aparece en 7 entradas de 3 autores y "bueno nada" en 6 de 3. "Tachero", "PH" y "expensas" fijan a todos en la misma ciudad y la misma edad.

### Colombia

1. "Usted" también entre amigos y con cariño (Bogotá, Antioquia); tuteo en la costa **(obs.)**.
2. "parce" o "parcero": amigo **(obs.)**.
3. "qué chimba": muy bueno. La recoge la Academia Colombiana y su sentido varía según la región ([Infobae, 2025](https://www.infobae.com/colombia/2025/10/19/desde-ser-chimba-a-estar-chimbo-esta-es-la-palabra-que-los-colombianos-usan-para-todo/)).
4. Entre las expresiones colombianas más buscadas en Google están "qué chimba", "dar papaya", "qué paila", "qué vaina", "ponte pilas" y "hacer un dos" ([La FM](https://www.lafm.com.co/tecnologia/parche-y-melo-entre-las-expresiones-tipicas-colombianas-mas-buscadas-en-google)).
5. "parche" (plan, grupo), "melo", "bacano", "berraco", "llave" **(obs.)**.
6. "qué pena" por "perdón", "uy no", "pues" al final en Antioquia **(obs.)**.
7. Risa: "jajaja", "jajsjs"; 😂 sigue fuerte en adultos **(obs.)**.
8. Abre con "parce", "amiga", "qué más". Cierra con "listo", "dale", "hablamos" **(obs.)**.

**Caricatura:** "mk" o "marica" como vocativo entre jóvenes es también un insulto homófobo: fuera del banco. Tampoco referencias narco, ni "dar papaya" en boca de turista.

### España

1. Vosotros y "os" ("¿qué hacéis?", "os leo") son la marca escrita más fuerte. "Ustedes" suena latinoamericano **(obs.)**.
2. "tío/tía" y "bro" como vocativos. "Bro" aparece en [El Español, 2024](https://elespanol.com/reportajes/20240518/diccionario-actualizado-boomers-hablar-entender-hijo-sin-flopear-dar-cringe/855164723_0.html).
3. "en plan" como muletilla **(obs.)**.
4. "me renta", "júramelo", "literal" (de acuerdo), "de locos", "lache" (vergüenza ajena), "lore", "evento canónico" ([El Español, 2024](https://elespanol.com/reportajes/20240518/diccionario-actualizado-boomers-hablar-entender-hijo-sin-flopear-dar-cringe/855164723_0.html)).
5. "aura" y su contrario "Laura", "charca", "no puedo más", "basado", "bruh" ([Ara, 2026](https://es.ara.cat/lengua/charca-tienes-mil-aura-10-expresiones-generacion-z-no-entiendes_1_5666005.html)).
6. Tacos suaves: "joder", "hostia", "qué fuerte" **(obs.)**.
7. Risa: "jajaja", "jajajaj", "JAJAJA"; "xD" es de foro de los 2010 **(obs.)**.
8. Abre con "holaaa", "tíaaa", "buenas". Cierra con "venga", "un besito", "bss" **(obs.)**.

**Caricatura:** "mola" y "guay" suenan a 2005 o a padre. "Potaxie" suena a 2021. "Tío" en cada frase **(obs.)**.

### Chile

1. Voseo verbal con "tú": "¿cachái?", "¿cómo estái?", "¿qué querís?" **(obs.)**.
2. "weón" o "wn" como vocativo y marcador. Se escribe así en canales informales ([Helincks, 2015](https://www.redalyc.org/pdf/1345/134544960004.pdf)) y funciona como marcador gramaticalizado ([Rojas, 2012](https://revistahumanidades.unab.cl/index.php/revista-de-humanidades/en/article/view/303)).
3. "po" ("ya po", "sí po") también escrito en el chat ([Chilevisión](https://www.chilevision.cl/noticias/viral/mexicana-se-hace-viral-al-hablar-sobre-palabras-sin-sentido-que-usamos)).
4. "cachai", "altiro" y "fome" (este último solo chileno, según Darío Rojas) ([Marca Chile, 2025](https://www.marcachile.cl/en/el-habla-chilena/)).
5. "bacán", "la raja", "cuático", "filo", "pucha" **(obs.)**.
6. Risa: "jajaja", "JAJAJA" y mucho "xd" en minúscula **(obs., sin estudio)**.
7. Abre con "oye" o "wn". Cierra con "ya po", "chao", "nos vimos" **(obs.)**.

**Caricatura:** "wn" cada tres palabras. "Conchetumare" es fuerte: fuera. El chileno es la variedad que peor reconocen los modelos ([Kawasaki, 2026](https://arxiv.org/abs/2602.09346)), así que conviene que lo revise un chileno.

### Perú

1. Tuteo, con "usted" para mayores **(obs.)**.
2. "causa", "pata", "chochera", "yunta": amigo ([La República](https://larepublica.pe/tendencias/actualidad/2022/06/04/que-significa-floro-y-que-otras-jergas-se-usan-comunmente-en-peru-causa-palta-netflix-viral)).
3. "chamba"; "jato" (casa, del quechua) ([Matador](https://matadornetwork.com/es/guia-de-expresiones-peruanas-e-iberoamericanas-una-conversacion-con-willy/)).
4. Jerga joven de 2025: "qué robo" (qué injusto o qué suerte), "fachero", "canear", "más mil de aura", "la queso", "devorar/ayunar", "campear", "mítico" ([UDEP, 2025](https://www.udep.edu.pe/castellanoactual/?p=23438)).
5. "pe" ("ya pe", "habla pe"), "oe" para abrir, "asu", "ya fue" **(obs.)**.
6. Risa: "jajaja" y mucho "xd" **(obs.)**.
7. Abre con "oe" o "habla causa". Cierra con "ya pe" o "nos vemos" **(obs.)**.

**Caricatura:** "pe" al final de todo, o "causa", "jato" y "chamba" juntos. "csm" es grosero fuerte: fuera.

### Venezuela

1. Tuteo **(obs.)**.
2. "chamo/chama" como vocativo, "pana" (amigo), "burda" (mucho, muy), "qué ladilla" (qué fastidio), "na'guará" (asombro). Fuente de divulgación: [Psicología y Mente](https://psicologiaymente.com/reflexiones/palabras-expresiones-venezolanas) y [Lingopie](https://lingopie.com/blog/venezuelan-slang/).
3. "chévere", "vale", "épale", "fino" **(obs.)**.
4. La diáspora mezcla con el país donde vive: "chamo, cachai" o "chamo, qué chimba". Es muy creíble **(obs.)**.
5. Risa: "jajaja", "jajsjs" **(obs.)**.
6. Abre con "chamo" o "épale". Cierra con "dale" o "fino" **(obs.)**.

**Caricatura:** "marico" o "mrc" es también un insulto: fuera. "Vale" al final de cada frase es estereotipo de telenovela. "Na'guará" es sobre todo de Lara **(obs.)**.

### Uruguay

1. "tú tenés": pronombre de tuteo con verbo de voseo, una combinación única en el mundo hispano. Conviven "vos tenés" y "tú tienes" según la edad y la confianza ([estudio de Maldonado](https://libra.unine.ch/bitstreams/1e1b42d0-f4ca-4468-8f91-e6bd5dc2e708/download); [Shively](https://scholarworks.iu.edu/journals/index.php/iulcwp/article/download/26226/31847/61927)).
2. "bo" como vocativo, parecido al "che"; "tá" como sí u ok ([Babbel](https://www.babbel.com/learn-spanish/advanced/uruguayan-spanish); [Culture Trip](https://theculturetrip.com/south-america/uruguay/articles/all-the-uruguayan-slang-terms-you-need-to-know)).
3. "salado": difícil, increíble u horrible, según el tono (mismas fuentes).
4. "re", "bárbaro", "de una", "nabo" **(obs.)**.
5. Risa: "jajaja", "JAJAJA" **(obs.)**.
6. Abre con "bo". Cierra con "tá", "beso", "abrazo" **(obs.)**.

**Caricatura:** el banco pone "bo" en 13 entradas y "tá" en 8, lo que lo vuelve un sketch. Mucha gente no usa ninguna de las dos. "Gurí" y "botija" significan niño: evitarlas.

### Latinos en EE. UU. (Spanglish)

1. En 8 TikToks de humor, el 77 % de los cambios de idioma ocurrió dentro de la misma frase. Los videos que mezclaban cambios dentro y entre frases tuvieron más del doble de likes por vista. El chiste suele ser familiar: padres hispanohablantes con hijos bilingües ([UCLA, 2025](https://languagedlife.ucla.edu/?p=3610)).
2. "No sabo kids": en 95 TikToks del hashtag se discute si hablar poco español o mezclar idiomas resta autenticidad ([Callesano, 2023](https://mdpi.com/2226-471X/8/3/206)). Es un chiste interno: nunca burlarse de quien pregunta.
3. Jerga en inglés sin traducir: "lowkey", "ngl", "fr", "bro", "like", "it's giving" **(obs.)**.
4. Calcos: "parquear", "textear", "aplicar" **(obs.)**.
5. Mezcla según el origen: "mija" (mexicoamericano), "wepa" y "janguear" (boricua), "klk" (dominicano), "qué bolá" (cubano) **(obs.)**. Creadores boricuas usan el humor contra la idea de un español neutro ([JLACR](https://journal.pubalaic.org/index.php/jlacr/article/view/170)).
6. Risa: "jajaja" y "lmaoo" en el mismo mensaje **(obs.)**.
7. Abre con "bro", "girl" o "ma". Cierra con "ok bye" o "love u" **(obs.)**.

**Caricatura:** alternar idioma cada dos palabras de forma mecánica. La "mamá con la chancla" está gastada.

---

## 4. Qué delata a un texto escrito por IA

### 4.1 Fuentes

- **Wikipedia, "Signs of AI writing"** ([enlace](https://en.wikipedia.org/wiki/Wikipedia:Signs_of_AI_writing)):
  - Señales vigentes: regla de tres (§3.5), "no es X, es Y" y "no solo X sino Y" (§3.3), vocabulario típico de IA (§3.1) y evitar el simple "es/tiene" (§3.2).
  - El guion largo pasó a "indicador histórico" (2022 a septiembre de 2026), igual que el tono promocional.
  - No sirven como prueba: la gramática perfecta, la mezcla de registros ni la prosa sosa o rebuscada.
  - Las personas usan más "es/tiene", sinónimos simples, superlativos tajantes, matices y frases con palabras de más.
  - Cada modelo tiene su estilo. Gemini y Claude tienden a la concisión.
  - Las personas detectan mal el texto de IA.
- **Español (RAEL, Complutense, 2024):** en 17 de 41 rasgos, GPT-3.5 y GPT-4 difieren de los humanos. Los modelos usan más adjetivos y más puntos (más oraciones). Las personas usan más pronombres, más adverbios y más paréntesis, comas, comillas y dos puntos ([Alonso Simón et al.](https://doi.org/10.58859/rael.v23i1.666)). Ojo: el estudio usa artículos, noticias y reseñas, no chat.
- **Repetición:** más del 90 % de 1.008 chistes de ChatGPT eran los mismos 25 ([Jentzsch y Kersting, 2023](https://arxiv.org/abs/2306.04563)).
- **Bueno solo, igual en conjunto:** cada chiste puede ser igual de gracioso que uno humano ([Gorenz y Schwarz, 2024](https://doi.org/10.1371/journal.pone.0305364)). Las historias hechas con IA salen mejor una por una pero más parecidas entre sí ([Doshi y Hauser, 2024](https://pmc.ncbi.nlm.nih.gov/articles/PMC11244532)).
- **Variedades del español:** los modelos reconocen mejor España, México y Centroamérica y el Río de la Plata, y peor Chile ([Kawasaki, 2026](https://arxiv.org/abs/2602.09346)). Muchos modelos confunden variedades ([dataset UPM, 2025](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC12504998/)).
- **Jerga que no encaja:** la jerga baja la credibilidad salvo que coincida con la personalidad de quien habla. Se le perdona más a un creador que a una marca ([Iowa, 2025](https://tippie.uiowa.edu/node/9516)).
- **Rechazo cultural:** "slop", el contenido de IA de baja calidad hecho en cantidad, fue la palabra del año 2025 de Merriam-Webster ([Deseret](https://www.deseret.com/entertainment/2025/12/15/2025-words-of-the-year)).
- **Listas de palabras:** existen listas de palabras de IA, como el auge de "delve"/"ahondar" ([Genbeta, 2024](https://www.genbeta.com/inteligencia-artificial/hay-manera-facil-detectar-texto-esta-hecho-chatgpt-ninguna-herramienta)). En descripciones de 100 caracteres casi no aparecen.

### 4.2 Cómo se ve en una descripción corta de comunidad (síntesis)

1. **El mismo esqueleto** en todas: tipo + tic + remate, con el mismo ritmo y la misma calidad.
2. **Arquetipos de oficio** en vez de vínculos: 27 de 128 nombres son un oficio.
3. **El humor cae siempre sobre otros.** Nunca es autorretrato, aunque el autodesprecio es lo más común.
4. **La jerga como disfraz:**
   - todas las marcas de un país en una línea;
   - jerga de diccionario para padres (LOL, OMG, fail) dicha en serio;
   - jerga ajena a la edad del autor.
5. **Español neutro o de la variedad equivocada:** tuteo en una voz argentina, España sin vosotros, un chileno inventado.
6. **Lenguaje de especificación:** "toda respuesta…", "cerrá siempre con…". Aparece en 9 entradas.
7. **Tríadas, antítesis redondas y moraleja.**
8. **Imperfección uniforme.** Typos inventados que se repiten ("pra" en 4 entradas de 2 autores). Son de un tipo que el autocorrector suele arreglar: lo real son las tildes que faltan en pares ambiguos (esta/está, si/sí, tu/tú) y las palabras cambiadas por el corrector **(obs.)**.
9. **Emoji ilustrativo** al final (🎂 junto a "cumple") en vez de emoji de reacción.
10. **Muletillas "humanizadoras" clonadas:** "(tipo siempre)" y "o sea" en los autores 7, 20 y 31; "bueno nada" en los 5, 22 y 37; "posta" en los 14, 23 y 33. El banco tiene 40 autores pero solo 13 manos.
11. **Adjetivos en vez de acciones**, y el chiste explicado.
12. **Sin rastro social:** nadie dice para quién lo hizo, nadie pide perdón por el largo, no hay chistes internos a medias **(obs.)**.

### 4.3 La investigación anterior

**Acertó en:**
- la uniformidad como señal principal;
- la regla de tres y el "no X sino Y";
- el punto final como marca formal;
- el detalle concreto por encima del adjetivo;
- los largos desparejos y subir el tope a 500 (ya hecho);
- sacar menores y cripto;
- reconocer que no había corpus.

**Erró o le faltó:**
- **Tomó lo humano como ortografía.** Wikipedia aclara que la gramática no prueba nada, y un typo no hace gracioso a nadie. La queja del dueño es de voz y de contenido.
- **Sus 13 "manos" se volvieron plantillas** clonadas entre autores. Fue una uniformidad nueva.
- **Una sola ciudad y una sola edad:** costumbrismo porteño y montevideano de 40 a 60 años. Solo 1 de 128 entradas usa un formato actual. No hay voces de México, Colombia, España, Chile ni Perú.
- **Aconsejó dejar chistes sin cerrar y meter entradas flojas.** Pero la estantería se ordena por usos, así que lo de arriba tiene que ser lo mejor. En una plataforma real, el 1,6 % de los personajes concentra más del 80 % de las interacciones ([estudio de Character.AI](https://arxiv.org/html/2505.13354v1)).
- **Inventó typos poco plausibles** en un celular ("pronsotico", "ahroa", "reclaulando").
- **Olvidó que la descripción también es el prompt.** Una entrada de 463 caracteres de divagación no se puede decir en 6 palabras.
- **Olvidó por qué la gente crea personajes:** para molestar a una amiga, como autorretrato o como chiste interno. Esos creadores suelen hablarle a "vos" en una escena (mismo estudio).
- **Al margen, sobre figuras históricas:**
  - "Einstein": la Universidad Hebrea licencia su nombre e imagen y ha litigado ([Chronicle](https://www.chronicle.com/article/Company-Sues-for-Right-to-Sell/125953/)), aunque un tribunal de EE. UU. dio por vencido ese derecho en 2012 ([CDAS](https://cdas.com/einstein-publicity-rights-deemed-expired-by-california-federal-court/)).
  - "Tesla" coincide con una marca registrada **(obs.)**.

---

## 5. Veinte reglas para una descripción que parezca escrita en un celular y den ganas de probarla

1. **Empieza por un vínculo o una escena, no por un oficio.** "Mi gata", "mi jefe un viernes", "yo a las 3am" funcionan; "un plomero que…" no.
2. **El chiste tiene que estar en lo primero que se lee** (unos 60 a 80 caracteres) **(obs.)**.
3. **Un detalle específico que se reconozca al instante:** una hora rara, un número, un objeto, una frase textual. Que no sea obscuro.
4. **Dale al modelo una muletilla de tres palabras o menos, y di cómo responde sí y cómo responde no.** Si la voz no cabe en 6 palabras, no existe.
5. **Escribe como quien lo hizo y se lo cuenta a un amigo, no como quien lo vende.** Nada de "un personaje ideal para…". Primera o segunda persona; tercera a veces ("es el que…").
6. **En una de cada cuatro o cinco, deja rastro de por qué existe:** "lo hice para mi hermana", "basado en mi ex (real)", "perdón me quedó largo", "no me funen" **(obs.)**.
7. **Un solo formato por personaje, y comprueba que siga vivo.** Delulu y romantizar ya fueron. Lo pasado de moda sirve solo si el chiste es justamente ese (la charca).
8. **Jerga solo la que diría esa persona, y poca:** una o dos marcas. La que no encaja resta credibilidad.
9. **La región entra por la gramática:** voseo (AR), "tú tenés" (UY), vosotros (ES), "cachái" (CL), "usted" cariñoso (CO). Con una palabra regional alcanza.
10. **El blanco del chiste es quien lo creó o la situación.** A quien pregunta se lo puede pinchar por algo que eligió, nunca por su cuerpo ni su identidad.
11. **Describe costumbres, no des órdenes:** "te deja en visto y a las 3 horas manda ok", no "toda respuesta debe…". Igual la app ignora las instrucciones.
12. **Nada de tríadas parejas, "no es X, es Y", moraleja final ni chiste explicado.**
13. **Verbos y pronombres antes que adjetivos:** "te mira, suspira y te manda un sticker" le gana a "sarcástico y dramático".
14. **Puntuación de celular, coherente con el autor:**
    - casi nunca punto final;
    - casi nunca ¿ ni ¡ de apertura;
    - las tildes que faltan son las de pares ambiguos, porque el teclado pone el resto;
    - nada de letras cruzadas inventadas.
15. **Emoji de reacción y de la edad del autor:** de 0 a 3, en medio o después del chiste. 😭 es general y joven; 😂 y 🤣 son más de adultos. Nunca un emoji que ilustre lo que dice el texto.
16. **La risa también tiene dueño:** "jajaja" es de cualquiera; "JAJSJAJS", de alguien joven en plena carcajada; "jsjs", tímida o joven; "xd", de Chile, Perú o gamer. Una por descripción como mucho.
17. **Largo desparejo, pero que lo pida la idea:** muchas de una línea; las largas, solo para autores charlatanes, y también con el gancho arriba. Ninguna muletilla repetida tres veces.
18. **Un banco variado, sin manos clonadas:**
    - países y edades de 20 a 60;
    - tipos: autorretrato, mascota, familia, trabajo, citas, objetos, formatos de internet, historia y mito;
    - ningún tic compartido entre dos autores;
    - el nick y el avatar, coherentes con la voz.
19. **Lo mejor arriba:** las diez primeras deben ser las más claras y graciosas. Lo flojo va en la cola larga.
20. **Límites duros:**
    - nadie real vivo, ni por nombre ni por rasgos reconocibles (describe el tipo);
    - ninguna marca en el nombre, y genérico en la descripción ("la app de delivery");
    - ningún personaje de franquicia;
    - nada de menores ni de escuela, ni "six seven";
    - nada sexual;
    - ningún vocativo que también sea insulto a un grupo ("marica", "marico");
    - nada de plata, apuestas, cripto, salud ni autolesión;
    - figuras históricas, sí, pero revisa las que tienen licencias.

**Prueba final:** ¿lo mandarías al grupo con un 😭? Si hay que explicarlo, no va.

**Nombres (16 unidades):** minúscula o como salga, sin mayúscula de catálogo. Posesivos y artículos que cuentan una relación: "mi gata juzgona" (15), "la tía del grupo" (16). Una aclaración entre paréntesis: "ex tóxico (real)" (16). Un emoji ocupa 2 o más unidades, así que úsalo solo si suma: "yo a las 3am 😭" (15). Nunca un oficio solo ni el nombre de un famoso.

---

## Fuentes principales

- **Humor y circulación:** [The Clinic 2024](https://www.theclinic.cl/2024/01/09/chile-memes-estudio-revela-humor-contenido-favorito-de-chilenos-en-redes-sociales-radiografia-digital/) · [Gizmodo 2024](https://gizmodo.com/why-does-a-meme-go-viral-and-can-we-predict-it-2000526357) · [Lit Hub 2021](https://lithub.com/this-former-late-show-with-stephen-colbert-writer-has-some-tips-for-writing-humor) · [White Label Comedy 2024](https://whitelabelcomedy.com/blog/clarity-beats-specificity-jokes) · [Mozilla 2025](https://blog.mozilla.org/en/internet-culture/secret-language-of-memes-iykyk/) · [UCLA 2025, humor negro](https://languagedlife.ucla.edu/?p=3523) · [McGraw y Warren 2010](https://leeds-faculty.colorado.edu/mcgrawp/pdf/mcgraw.warren.2010.pdf) · [TikTok Next 2026](https://ads.tiktok.com/business/library/TikTok_Next_2026_Trend_Report.pdf) · [Cambridge 2025](https://www.cam.ac.uk/news/cambridge-dictionary-reveals-word-of-the-year-2025) · [Portillo-Fernández 2022](https://doi.org/10.15517/rfl.v48i2.50512)
- **Formatos:** [KYM POV](https://knowyourmeme.com/memes/pov-memes) · [KYM Nobody](https://knowyourmeme.com/editorials/memescape-enabler-formats) · [Time 2023](https://time.com/6286130/tiktok-beige-flags) · [MW delulu](https://www.merriam-webster.com/slang/delulu) · [MW MCE](https://www.merriam-webster.com/dictionary/MCE) · [Dictionary.com -core](https://www.dictionary.com/e/core-words-cottagecore/) · [Inverse, tier lists](https://inverse.com/gaming/tier-list-meaning-definition-origins-video-games) · [Cultura Colectiva, era](https://culturacolectiva.com/arte/letras/que-significa-estar-en-mi-era/)
- **Regiones:** [El Español 2024](https://elespanol.com/reportajes/20240518/diccionario-actualizado-boomers-hablar-entender-hijo-sin-flopear-dar-cringe/855164723_0.html) · [Ara 2026](https://es.ara.cat/lengua/charca-tienes-mil-aura-10-expresiones-generacion-z-no-entiendes_1_5666005.html) · [UDEP 2025](https://www.udep.edu.pe/castellanoactual/?p=23438) · [ENTER.CO 2025](https://www.enter.co/cultura-digital/el-popurri/tambien-dices-aesthetic-asi-esta-cambiando-nuestra-forma-de-hablar-en-2025-hablamos-peor-o-solo-hablamos-distinto/) · [Marca Chile 2025](https://www.marcachile.cl/en/el-habla-chilena/) · [Helincks 2015](https://www.redalyc.org/pdf/1345/134544960004.pdf) · [De Luca 2021](https://dialnet.unirioja.es/descarga/articulo/9127762.pdf) · [UCLA 2025, Spanglish](https://languagedlife.ucla.edu/?p=3610) · [Callesano 2023](https://mdpi.com/2226-471X/8/3/206) · [Deseret/NYT 2026](https://www.deseret.com/lifestyle/2026/08/13/the-most-popular-emojis/) · [Publimetro 2026](https://www.publimetro.co/tecnologia/2026/08/25/el-diccionario-oculto-de-los-emojis-que-la-generacion-z-usa-y-que-probablemente-tu-ni-siquiera-conoces/)
- **IA:** [Wikipedia](https://en.wikipedia.org/wiki/Wikipedia:Signs_of_AI_writing) · [RAEL 2024](https://doi.org/10.58859/rael.v23i1.666) · [Jentzsch y Kersting 2023](https://arxiv.org/abs/2306.04563) · [Doshi y Hauser 2024](https://pmc.ncbi.nlm.nih.gov/articles/PMC11244532) · [Gorenz y Schwarz 2024](https://doi.org/10.1371/journal.pone.0305364) · [Kawasaki 2026](https://arxiv.org/abs/2602.09346) · [Iowa 2025](https://tippie.uiowa.edu/node/9516) · [Character.AI, arXiv 2025](https://arxiv.org/html/2505.13354v1)
