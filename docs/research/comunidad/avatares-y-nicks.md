# Avatares y nicks creíbles para la Comunidad: investigación (17 nov 2026)

## Resumen

- **Build de prueba, ya mismo:** para mascotas, comida, paisajes y selfies sin cara, lo mejor son fotos propias hechas con el celular, y si faltan, Unsplash, Pexels o Pixabay. Para los avatares dibujados sirven los estilos CC0 de DiceBear, Fluent Emoji (MIT) o Boring Avatars (MIT). Uno de cada cinco creadores va sin foto. No va ninguna cara reconocible, ni de stock ni hecha con IA.
- **Descartados:** This Person Does Not Exist (el dominio está en venta y StyleGAN no admite uso comercial), Generated Photos (el plan gratis es no comercial y sus términos prohíben engañar) y las fotos de randomuser.me (caras sin licencia declarada).
- **En producción no se inventan usuarios ni conteos.** Lo que se hace es una cuenta oficial con su etiqueta y los primeros creadores reales. Inventar choca con la directiva europea 2005/29/CE (anexo I, puntos 22 y 23c), con la regla de la FTC (16 CFR 465), con el art. 37 del CDC de Brasil y el art. 32 de la LFPC de México, y en Play con las políticas de Misrepresentation, Deceptive Behavior y Metadata.
- **Los nicks** van en minúscula y sin tildes ni ñ, porque las plataformas no las admiten. Llevan puntos o guiones bajos, y números que no son años recientes. Abajo hay 40 ejemplos repartidos por país.

Las marcas son estas: **(obs. propia)** es algo que observé yo, **(inferencia)** es algo que deduzco de una fuente, y **(vía búsqueda)** es un dato que saqué del resumen del buscador porque la página bloqueó la lectura directa.

---

## A. Fuentes de imágenes con aspecto de perfil social

### A.1 Fotos de stock gratuitas

**Unsplash.** Tiene fotos de calidad de todo lo que hace falta: perros, gatos, atardeceres, comida, gente de espaldas y siluetas.
- **Licencia:** es gratis, se puede usar comercialmente y modificar, y no exige atribución. Hay dos límites: no se puede vender sin una modificación significativa ni juntar fotos para montar un servicio parecido o competidor ([licencia](https://unsplash.com/license)). Los términos agregan que no se pueden usar para entrenar IA ni para hacer scraping fuera de la API.
- **Personas:** la licencia no cubre marcas, logos, personas reconocibles ni obras de arte, y según el uso puede hacer falta el permiso del retratado. No hay model releases ([términos §5 y §8](https://unsplash.com/terms)). Presentar a alguien como "usuario" de la app es justo el caso que pediría ese permiso (inferencia).
- **API:** obliga a enlazar las imágenes desde las URLs que ella misma devuelve, a dar crédito a Unsplash y al fotógrafo con enlace y a avisar cada descarga. Puede haber cuotas, y a los negocios con uso intenso les puede cobrar una tarifa ([API terms §6, §9 y §12](https://unsplash.com/api-terms)). Para 50 avatares es más simple descargarlos a mano bajo la licencia, que no trae esas obligaciones (inferencia).

**Pexels.** Ofrece lo mismo que Unsplash.
- **Licencia:** gratis y comercial, la atribución es opcional y se puede editar. No se permite mostrar a personas identificables de forma negativa u ofensiva, **sugerir que la gente o las marcas de la foto respaldan tu producto**, vender copias sin alterar, redistribuir en otros sitios de stock o fondos de pantalla, ni usar las fotos como marca ([licencia](https://www.pexels.com/license/)).
- **API:** es gratis, con 200 pedidos por hora y 20.000 al mes, ampliables gratis si se cumplen los términos. Exige un enlace visible a Pexels y crédito al fotógrafo cuando se pueda, y no permite replicar lo central de Pexels ([documentación](https://www.pexels.com/api/documentation/)).

**Pixabay.**
- **Licencia:** gratis, sin atribución, y se puede modificar.
- **Prohibiciones:** vender el contenido tal cual, usar comercialmente fotos con marcas o logos, darles un uso inmoral o ilegal (sobre todo si aparecen personas reconocibles) y **darles un uso engañoso, como sugerir que una persona, marca u organización de la foto te respalda o está afiliada**. Tampoco se pueden usar como marca ([resumen](https://pixabay.com/service/license-summary/), [términos §5](https://pixabay.com/service/terms/)). El §8 prohíbe además juntar fotos para un servicio competidor, la copia masiva o sistemática y el scraping para machine learning. Conseguir el consentimiento de terceros corre por tu cuenta.
- **API:** gratis, 100 pedidos por minuto, hay que cachear 24 horas y no se puede enlazar en forma permanente: hay que descargar las imágenes a un servidor propio. En los resultados hay que decir de dónde vienen las imágenes, y la descarga masiva está prohibida ([docs](https://pixabay.com/api/docs/)).

**Lorem Picsum.** Es un servicio de imágenes de relleno que saca sus fotos de Unsplash. Se piden por URL: `/id/{id}`, `/seed/{seed}`, `/v2/list` y `/id/{id}/info`, que devuelve el autor ([picsum.photos](https://picsum.photos/)). La página no declara licencia; por el origen, entiendo que vale la de Unsplash (inferencia). No tiene búsqueda por tema, así que sirve para paisajes al azar. Conviene descargar las fotos y no depender de un tercero.

### A.2 Buscadores de obras con licencia libre

**Openverse.** Busca obras con licencias CC y de dominio público.
- **Licencias:** Openverse **no verifica** las licencias, y comprobar que se puede usar cada obra queda a cargo de quien la usa ([términos §3](https://docs.openverse.org/terms_of_service.html)).
- **API:** tiene límites y registro, prohíbe scrapear el catálogo y obliga a atribuir las obras CC. Un producto hecho con la API tiene que decir que usa Openverse y que Openverse no lo respalda (§2 y §4). Puede cobrar por usos comerciales intensos (§5).
- **Filtro:** hay que quedarse con obras que permitan uso comercial y modificación, y descartar las NC y ND. Las CC BY necesitan una pantalla de créditos.

**Wikimedia Commons.**
- **Licencias:** cambian de un archivo a otro (CC BY, BY-SA, dominio público), y la Fundación no garantiza el estado de cada uno ([reutilización](https://commons.wikimedia.org/wiki/Commons:Reusing_content_outside_Wikimedia)).
- **Personas:** la licencia libre no cubre los derechos del retratado, y el uso comercial de fotos de personas puede necesitar su consentimiento ([personas identificables](https://commons.wikimedia.org/wiki/Commons:Photographs_of_identifiable_people)).
- **API:** tiene la API MediaWiki, `api.php` ([Commons:API](https://commons.wikimedia.org/wiki/Commons:API)).
- **Estética:** es fuerte en animales y paisajes y floja en aspecto de foto de celular (obs. propia).

### A.3 Avatares dibujados, generados por código y emoji

**DiceBear.** Genera avatares SVG a partir de una semilla, así que el mismo nick da siempre el mismo avatar. El código es MIT, pero **cada estilo tiene su licencia** ([licencias](https://www.dicebear.com/licenses/)):

| Licencia | Estilos |
|---|---|
| **CC0, sin atribución** | Todos los de DiceBear (Thumbs, Pixel Art, Shapes, Glass, Identicon, Initials, Rings, Croodles no), más Lorelei, Notionists y Open Peeps |
| **CC BY 4.0, con atribución** | Adventurer, Big Ears, Big Smile, Croodles, Dylan, Fun Emoji, Glyphs, Micah, Miniavs, Personas, Toon Head |
| **MIT** | Icons |
| **Términos del artista** ("gratis personal y comercial", hay que revisarlos en su fuente) | Avataaars, Bottts |

- **API HTTP:** es **gratis solo para uso no comercial**; para uso comercial hay que alojarla uno mismo. Admite 50 pedidos por segundo en SVG y 10 en PNG, JPG, WebP y AVIF, y los formatos raster llegan hasta 256 px ([HTTP API](https://www.dicebear.com/how-to-use/http-api/)).
- **Uso aquí:** conviene pre-renderizarlos con la librería y estilos CC0 (inferencia).

**Boring Avatars.** Es una librería React con licencia MIT y seis variantes: marble (la predeterminada), beam, pixel, sunset, ring y bauhaus. Tiene además una API por suscripción paga ([GitHub](https://github.com/boringdesigners/boring-avatars), [sitio](https://boringavatars.com/)). Son abstractos y parecen el avatar por defecto de una app, no el de una persona (obs. propia).

**Emoji como avatar.** Un emoji grande sobre un fondo de color es un avatar muy común (obs. propia).
- Fluent UI Emoji de Microsoft tiene licencia MIT ([GitHub](https://github.com/microsoft/fluentui-emoji)) e incluye versiones 3D (obs. propia).
- En Noto Emoji, las imágenes son Apache 2.0, las fuentes OFL 1.1 y las banderas de dominio público ([GitHub](https://github.com/googlefonts/noto-emoji)).

### A.4 Caras sintéticas o de relleno (todas descartadas)

**Generated Photos.** Son caras hechas con IA ([términos](https://intercom.help/generated-photos/en/articles/7872726-generated-photos-terms-and-conditions)).
- **Planes:** el gratis es solo para uso personal no comercial y exige un enlace de atribución visible. Los pagos dan licencia comercial para un proyecto. Hay una API por suscripción que permite uso comercial solo mientras la suscripción está activa.
- **Prohibiciones:** presentar falsamente que algo lo creó uno u otra persona, y crear o difundir contenido falso o engañoso o pensado para engañar. Usarlas como usuarios de la app choca con eso, y la app es comercial.

**This Person Does Not Exist y StyleGAN.**
- Hoy el dominio muestra que está en venta y no dice licencia ni modelo ([sitio](https://thispersondoesnotexist.com/)).
- StyleGAN v1 es CC BY-NC 4.0 ([repo](https://github.com/NVlabs/stylegan)). StyleGAN2 y StyleGAN3 usan la NVIDIA Source Code License-NC, que solo permite investigación o evaluación ([v2](https://github.com/NVlabs/stylegan2/blob/master/LICENSE.txt), [v3](https://github.com/NVlabs/stylegan3/blob/main/LICENSE.txt)). Ninguna licencia aclara qué pasa con las imágenes generadas.
- FFHQ, el set con el que se entrenó, es CC BY-NC-SA 4.0 en conjunto. No se puede usar para reconocimiento facial, y las personas que aparecen pueden pedir que las quiten ([FFHQ](https://github.com/NVlabs/ffhq-dataset)).

**randomuser.me.** Es una API gratis y de código abierto que genera usuarios falsos ([sitio](https://randomuser.me/)). Sus 200 fotos son caras elegidas de la sección "autorizada" de UI Faces, sin licencia declarada; la página remite a una FAQ archivada que no pude abrir ([fotos](https://randomuser.me/photos)). Sirve, como mucho, para una maqueta interna.

### A.5 Generar las imágenes con un modelo

**Google: AI Studio, Gemini API y Firebase AI Logic.**
- **Propiedad:** Google no reclama la propiedad de lo generado, aunque puede generar lo mismo para otra persona.
- **Condiciones:** el servicio está pensado para uso empresarial y exige ser mayor de 18. En el uso no pago, Google usa los prompts y las respuestas para mejorar sus productos, con revisión humana ([términos](https://aistudio.google.com/docs/terms)).
- **Uso prohibido:** su política prohíbe fraudes, suplantar a alguien para engañar sin avisarlo y **hacer pasar contenido de IA como si lo hubiera hecho solo una persona, para engañar** ([política](https://policies.google.com/terms/generative-ai/use-policy)).
- **Modelos:** hoy los modelos de imagen son la familia Nano Banana: `gemini-2.5-flash-image` (el antiguo), `gemini-3-pro-image` y `gemini-3.1-flash-image`. **Imagen ya no está disponible** en la Gemini API ([image-generation](https://aistudio.google.com/docs/image-generation), [imagen](https://aistudio.google.com/docs/imagen)).
- **SynthID:** todas las imágenes que se generan llevan SynthID. Es una marca invisible, resiste recortes, filtros y compresión, y se puede comprobar en la app Gemini, en Search o Chrome, o en synthid.com ([SynthID](https://deepmind.google/models/synthid/)).
- **Firebase AI Logic:** los modelos Nano Banana piden el plan Blaze y la función está en Preview, sin SLA ([docs](https://firebase.google.com/docs/ai-logic/generate-images-gemini)). El proyecto ya incluye `firebase-ai` (en `gradle/libs.versions.toml`), pero para generar 50 imágenes una sola vez basta con AI Studio (obs. propia).

**OpenAI.** Lo generado es del usuario y OpenAI le cede sus derechos, aunque el resultado puede no ser único ([términos](https://openai.com/policies/row-terms-of-use/), vía búsqueda porque la página me devolvió 403).

**Midjourney.** Lo generado es de quien lo crea; las empresas que facturan más de USD 1 M al año necesitan el plan Pro o Mega, y las imágenes son públicas salvo que se use el modo Stealth ([términos](https://docs.midjourney.com/hc/en-us/articles/32083055291277-Terms-of-Service), vía búsqueda).

**Derechos de autor.** La Oficina de Copyright de EE. UU. concluyó en enero de 2025 que escribir el prompt no hace autor a nadie y que lo generado puramente por IA no tiene copyright ([USCO](https://www.copyright.gov/newsnet/2025/1060.html)). La consecuencia es que no se puede impedir que otros copien esos avatares, cosa que aquí da igual (inferencia).

### A.6 Lo que no tiene fuente porque se hace desde cero (obs. propia)

- **Fotos propias de celular** (mascotas, comida, atardeceres, la espalda de un amigo que dio permiso): son las más creíbles y no tienen ningún problema de licencia.
- **Memes:** hay que hacerlos desde cero. Muchas plantillas conocidas son fotos o fotogramas con dueño.
- **Personajes de franquicias:** no van, por copyright y por marca.

---

## B. ¿Inventar usuarios en producción choca con Play o con la ley?

### B.1 Google Play

- **Deceptive Behavior:** la regla general es que la app no engañe y funcione como el usuario espera razonablemente. **No tiene ningún punto específico sobre usuarios falsos ni interacción inventada**; lo verifiqué en la página ([política](https://support.google.com/googleplay/android-developer/answer/9888077)).
- **Misrepresentation:** prohíbe ocultar quién está detrás de la app y coordinarse para engañar a los usuarios o esconder el origen del contenido, con foco en política y temas sociales ([política](https://support.google.com/googleplay/android-developer/answer/9888689)). Firmar con "usuarios" inventados lo que escribe el propio equipo es esconder el origen de ese contenido (mi lectura).
- **Metadata:** la ficha no puede inducir a error, y eso incluye las capturas. Los testimonios de usuarios en la descripción tienen que llevar autor y no pueden ser anónimos ([política](https://support.google.com/googleplay/android-developer/answer/9898842)).
- **Capturas:** tienen que mostrar la experiencia real dentro de la app ([guía de recursos](https://support.google.com/googleplay/android-developer/answer/9866151)).
- **Contenido generado por usuarios:** la estantería de la Comunidad lo es. Hacen falta términos que el usuario acepte antes de publicar, una lista de contenido prohibido, moderación constante y, como es pública, **poder denunciar y bloquear** desde la app ([política](https://support.google.com/googleplay/android-developer/answer/9876937)).
- **Contenido generado con IA:** hace falta un botón dentro de la app para denunciar lo ofensivo que genere la IA ([política](https://support.google.com/googleplay/android-developer/answer/13985936)).

### B.2 Ley de protección del consumidor

- **Unión Europea, directiva 2005/29/CE, anexo I** (prácticas desleales siempre, sin importar sus efectos):
  - **Punto 22:** prohíbe que una empresa se haga pasar por consumidor o dé a entender que no actúa como empresa ([texto adoptado](https://www.legislation.gov.uk/eudr/2005/29/annex/I/adopted)).
  - **Puntos 23b y 23c**, vigentes desde el 28 de mayo de 2022: prohíben afirmar que las reseñas son de usuarios reales sin verificarlo, publicar o encargar reseñas o recomendaciones falsas y **tergiversar reseñas o recomendaciones sociales** para promocionar un producto ([resumen](https://www.makeinfluence.com/en/academy/fake-and-manipulated-reviews-what-the-eu-omnibus-directive-bans); fechas en la [Comisión](https://commission.europa.eu/law/law-topic/consumer-protection-law/unfair-commercial-practices-law/unfair-commercial-practices-directive_en)). Mi herramienta no pudo leer EUR-Lex.
  - Creadores inventados más "usado N veces" inventado es prueba social que no existe, y encaja en el espíritu de los puntos 22 y 23c (mi lectura).
- **Estados Unidos, regla de la FTC (16 CFR 465):**
  - Define los indicadores de influencia en redes (seguidores, me gusta, vistas) y considera **falsos** los que no reflejan la actividad real de personas ([465.1](https://www.law.cornell.edu/cfr/text/16/465.1)).
  - El §465.8 prohíbe venderlos o comprarlos con fines comerciales ([465.8](https://www.law.cornell.edu/cfr/text/16/465.8)).
  - Inventarse un contador propio no es comprarlo, pero entra en la prohibición general de engaño de la sección 5 de la FTC Act (conocimiento general, no lo verifiqué hoy).
- **Brasil, CDC art. 37 §1:** es engañosa la información total o parcialmente falsa, o la que induce a error aunque sea por omisión ([reproducción](https://revistas.unievangelica.edu.br/index.php/raizesnodireito/article/download/7058/4942/28678), vía búsqueda).
- **México, LFPC art. 32:** es engañosa la información, verdadera o no, que induce o puede inducir a error ([mley](https://mley.mx/LFPC/articulo/32/), vía búsqueda).

No es asesoría legal.

### B.3 Según el escenario

| Escenario | ¿Se puede inventar creadores, avatares y conteos? | Cuidado con |
|---|---|---|
| **Build de prueba y capturas para revisión interna** | Sí: no se distribuye a nadie | Que las licencias no sean "no comerciales" (DiceBear HTTP, Generated Photos gratis, StyleGAN) |
| **Capturas de la ficha de Play** | No, si parecen reales | Son publicidad: aplican Metadata y la ley de consumo. Mostrar contenido oficial etiquetado o contenido real, sin conteos inventados ni caras de stock (Pexels y Pixabay prohíben sugerir respaldo) |
| **Producción** | No | Cuenta oficial, creadores reales, conteos reales y todo lo que pide la política de contenido generado por usuarios |

### B.4 Lo que hacen apps serias

- **Contenido oficial con su etiqueta:** Spotify marca sus listas editoriales con su logo en la línea de autor ([Spotify](https://support.spotify.com/us/artists/article/types-of-spotify-playlists/)). En la GPT Store, los GPT de OpenAI aparecen como "By ChatGPT" (obs. propia).
- **Primeros creadores reales:** Pinterest reclutó a mano a blogueros de diseño ([YC](https://ycombinator.com/blog/ben-silbermann-at-startup-school)). Stack Exchange arranca cada sitio con una beta privada de usuarios comprometidos ([Area 51 FAQ, en una copia espejo](https://backiee.wasmer.app/https_area51_stackexchange_com/faq)). Su blog desaconseja sembrar preguntas inventadas porque **salen genéricas**, que es justo la queja que dio pie a este trabajo ([SO blog](https://stackoverflow.blog/2010/07/07/area-51-asking-the-first-questions/)).
- **Contraejemplo:** los fundadores de Reddit arrancaron con cuentas falsas, se supo después, y Huffman duda de que funcionara otra vez ([Daily Dot](https://dailydot.com/steve-huffman-built-reddit-fake-accounts)).

---

## C. Nicks: cómo son de verdad

### C.1 Las reglas de cada plataforma moldean los handles

| Plataforma | Handle | Nombre visible | Fuente |
|---|---|---|---|
| Instagram | Si el nombre está tomado, Instagram sugiere agregar puntos, números, guiones bajos o abreviaturas. Las guías de terceros coinciden en letras, números, punto y guion bajo, hasta 30, pero no lo encontré en la ayuda oficial | — | [ayuda](https://help.instagram.com/513717858639392) |
| TikTok | Letras, números, guion bajo y punto; no puede terminar en punto; se cambia cada 30 días | Va aparte | [soporte](https://www.tiktok.com/support/faq_detail?id=7096681481912752642), vía búsqueda |
| X | Hasta 15: letras, números y guion bajo | Hasta 50 | [ayuda](https://help.x.com/en/managing-your-account/x-username-rules), vía búsqueda |
| Twitch | 4 a 25: letras, números y guion bajo (fuentes de terceros, sin verificar) | Solo cambia mayúsculas | (obs. propia) |
| Discord | 2 a 32, en minúscula obligatoria: letras latinas, números, `_` y `.`, sin dos puntos seguidos | Casi libre: espacios, emojis, mayúsculas | [soporte](https://support.discord.com/hc/articles/12620128861463), vía búsqueda |

Lo que se deduce de esas reglas (inferencia):
- **El handle no lleva tildes ni ñ.** Feña se escribe `fena` y Muñoz se escribe `munoz`. En el nombre visible, en cambio, sí van tildes, ñ y emojis.
- Un handle con puntos suena a Instagram, TikTok o Discord; uno solo con guiones bajos suena a X o Twitch.

### C.2 Los patrones

- **Nombres de pila y variaciones para que sea único:**
  - El nombre de pila es frecuente en muchos sitios. La exclusividad se consigue sumando números, letras o caracteres especiales ([Hämäläinen 2022, reseña de 103 estudios](https://ans-names.pitt.edu/ans/article/view/2364), §3.7).
  - En las redes donde la gente se muestra con su identidad real abundan los nombres oficiales (ídem, §4).
  - El inglés pesa aunque la comunidad hable otro idioma, de ahí el spanglish (ídem, §3.7).
  - La cultura popular inspira muchos nicks (ídem, §3.7). Aquí la reemplazamos por jerga, comida y lugares, sin franquicias.
  - Al conversar, la gente acorta los nicks: `Jeppe-82` pasa a ser "Jeppe" (ídem, §4).
- **Nombre más inicial o apellido abreviado:** en México se abrevia el apellido así: Mtz, Glz, Hdz, Rdz (obs. propia).
- **Artículo delante del nombre ("la Juli", "el Nacho"):** suena afectivo o expresivo según el ambiente ([Castellano Actual, UDEP](https://www.udep.edu.pe/castellanoactual/duda-resuelta-articulo-con-el-sustantivo-propio/)). Se usa mucho en Chile, Argentina y Cataluña (obs. propia).
- **Otros recursos** (obs. propia, sin fuente formal):
  - Sufijos de archivo: `.jpg`, `.exe`.
  - El prefijo `soy`.
  - Letras estiradas: `agusss`.
  - El relleno `xX_…_Xx`, propio de los gamers de los 2000 y hoy usado con ironía.
  - Todo en minúscula.
  - Emojis al final del nombre visible.
  - Banderas.
  - Códigos de área o de ciudad: `81`, `787`, `_bcn`.
- **Números:** no se usan años posteriores a 2007, y conviene evitar del `08` al `26`, que se leen como años recientes. Es mejor un año de los 80 o 90, un código de área o un número de la suerte.

### C.3 Señas por región (obs. propia)

| Zona | Señas |
|---|---|
| México | Wera (güera), chilango, regio, el apellido abreviado, chilaquil |
| Argentina | Tincho, Agus, Juli, Flor, Pato; barrios; mate |
| Colombia | Mona (rubia), rolo o rola (de Bogotá), parce o parcero, costeña |
| España | Lore, Javi, Pili, Nacho; yaya; `_bcn`, `_mad`; nombres locales de ciudades como Cai |
| Chile | Cata, Feña, Coni, Benja; Valpo; completo |
| Perú | Pao, causa, chalaca, Misti |
| Venezuela | Chama o chamo, pana, maracucha; la diáspora |
| Uruguay | Bo, Facu, Mati; la rambla |
| Latinos de EE. UU. | Mija, tía, códigos de área, barrios dichos en inglés |

### C.4 Cuarenta ejemplos inventados (sin verificar si existen en alguna plataforma)

| # | Handle (para "creado por") | Nombre visible | Zona | Patrón |
|---|---|---|---|---|
| 1 | `fer.glz81` | Fer Glz | MX, Monterrey | Nombre + apellido abreviado + código de área |
| 2 | `la.wera.del.depa` | la wera 🏢 | MX | Artículo + apodo sin ü + chiste doméstico |
| 3 | `reina.del.chilaquil` | Ale 🌶️ | MX | Apodo exagerado + comida |
| 4 | `siempre_es_el_lag` | Rodri 🎮 | MX | Excusa gamer con guiones bajos |
| 5 | `andy_hdz94` | Andy Hdz | MX | Nombre + apellido abreviado + año 1994 |
| 6 | `juli.de.caballito` | Juli 🦋 | AR, CABA | Diminutivo + barrio |
| 7 | `tincho.con.mate` | Tincho 🧉 | AR | Apodo + costumbre |
| 8 | `agusss.exe` | Agus 🫠 | AR | Letra estirada + `.exe` |
| 9 | `flor.en.modo.avion` | Flor ✈️ | AR | Nombre + autoironía |
| 10 | `xX_el_pato_Xx` | Pato 🦆 | AR | Relleno xX…Xx irónico |
| 11 | `la.mona.de.envigado` | la mona 🌻 | CO | Artículo + apodo regional + municipio |
| 12 | `parcero.sin.datos` | Santi 📵 | CO | Jerga + queja universal |
| 13 | `vale.rolita` | Vale 🌧️ | CO, Bogotá | Diminutivo + gentilicio cariñoso |
| 14 | `costena.en.el.frio` | Kelly 🌴 | CO | Gentilicio sin ñ + situación |
| 15 | `pepe.el.gato.jpg` | Pepe 🐈 | CO | La cuenta "es" del gato + `.jpg` |
| 16 | `lorena.de.cai` | Lore 🌊 | ES, Cádiz | Nombre + nombre local de la ciudad |
| 17 | `javi.que.no.madruga` | Javi ☕ | ES | Nombre + autoironía |
| 18 | `pili.y.sus.plantas` | Pili 🪴 | ES | Diminutivo + afición |
| 19 | `elnachoo_bcn` | Nacho 🛵 | ES | Artículo pegado + vocal estirada + ciudad |
| 20 | `yaya.carmen.mad` | Carmen 👵🏻 | ES | Rol familiar + nombre + ciudad |
| 21 | `la.cata.de.valpo` | Cata 🌊 | CL | Artículo + diminutivo + ciudad abreviada |
| 22 | `fena.sin.ene` | Feña 🌸 | CL | Chiste sobre la ñ que no se puede poner |
| 23 | `benja.desde.temuco` | Benja 🌧️ | CL | Diminutivo + ciudad |
| 24 | `coni_y_el_completo` | Coni 🌭 | CL | Diminutivo + comida |
| 25 | `soylapao_` | Pao 🌼 | PE | `soy` + artículo + `_` porque el nombre limpio estaba tomado |
| 26 | `causa.sin.limon` | Diego 🍋 | PE | Doble sentido (amigo y plato) |
| 27 | `chalaca.de.corazon` | Mile 💙 | PE, Callao | Gentilicio + frase |
| 28 | `renzo.y.el.misti` | Renzo 🌋 | PE, Arequipa | Nombre + paisaje |
| 29 | `chama.en.madrid` | Gaby 🇻🇪✈️ | VE, diáspora | Jerga + ciudad donde vive ahora |
| 30 | `pana.del.cafecito` | Luisa ☕ | VE | Jerga + diminutivo |
| 31 | `maracucha.de.verdad` | Yoli 🔥 | VE | Orgullo regional |
| 32 | `el.chamo.de.las.arepas` | Jose 🫓 | VE | Artículo + jerga + comida |
| 33 | `facu.desde.pando` | Facu 🧉 | UY | Diminutivo + ciudad |
| 34 | `mati.de.la.rambla` | Mati 🌅 | UY | Diminutivo + lugar |
| 35 | `bo.que.calor` | Seba ☀️ | UY | Interjección regional + queja |
| 36 | `xx.lu.xx` | Lu 🌙 | UY | Relleno de x en minúscula |
| 37 | `mija.no.contesta` | Vero 💅 | EE. UU. | Frase de mamá latina |
| 38 | `tia.lupe.tx` | Tía Lupe 🌮 | EE. UU., Texas | Rol familiar + nombre + estado |
| 39 | `yari_787` | Yari 🇵🇷 | Puerto Rico y Orlando | Nombre + código de área |
| 40 | `marco.de.east.la` | Marco 🌴 | EE. UU., Los Ángeles | Nombre + barrio en inglés |

Todos empiezan con una letra. Ninguno tiene años recientes, marcas, nada sexual ni drogas.

### C.5 Cómo comprobar que no son handles de alguien

1. Buscar el handle exacto entre comillas, solo y junto a "instagram" o "tiktok". Hacer lo mismo con el nombre visible.
2. Abrir `instagram.com/<handle>`, `tiktok.com/@<handle>`, `x.com/<handle>` y `twitch.tv/<handle>`. Que diga "no disponible" no prueba que esté libre: Instagram mantiene reservados los nombres de cuentas inactivas ([ayuda](https://help.instagram.com/513717858639392)).
3. Pasarlo por whatsmyname.app, que usa la base WhatsMyName. Esa base tiene licencia CC BY-SA 4.0, quedó archivada el 9 de noviembre de 2026 con 691 sitios y sus resultados son pistas, no hechos ([repo](https://github.com/WebBreacher/WhatsMyName)).
4. Si el handle es de una persona, cambiar un elemento entero (la ciudad, la mascota, la palabra), no solo un dígito. No usar nada que se parezca a una cuenta famosa.

---

## D. Recomendación

### D.1 La mezcla para unos 50 creadores (los 40 de arriba y 10 de reserva)

| Tipo | % | Cuántos | Build de prueba, ahora | Producción |
|---|---|---|---|---|
| Sin foto: la letra sobre un color, que la app ya dibuja | 20 | 10 | No hay que conseguir nada | Es lo predeterminado para todos |
| Mascotas | 20 | 10 | Fotos propias o de amigos con permiso; si faltan, Unsplash, Pexels o Pixabay | Galería de la app o foto subida por el usuario, que obliga a moderar |
| Comida, bebida, objetos, plantas, sin logos | 14 | 7 | Las mismas fuentes | Galería |
| Paisaje o atardecer | 10 | 5 | Fotos propias, Unsplash o Pexels, Picsum por id | Galería |
| Dibujado, 3D o emoji | 16 | 8 | DiceBear CC0 pre-renderizado con el nick como semilla, Fluent 3D (MIT), Noto (Apache), Boring Avatars (MIT) | **Galería de la app**: no hace falta moderar imágenes |
| Selfie sin cara: de espaldas, silueta, una mano con una taza | 12 | 6 | Fotos propias con consentimiento o stock de siluetas; Gemini solo para pruebas | Solo si lo sube el usuario |
| Garabato o meme hecho de cero | 8 | 4 | Dibujado a mano, o con IA solo para pruebas | — |
| Caras realistas o personajes de franquicias | 0 | 0 | Descartados | Descartados |

**Que el avatar cuadre con lo que crea cada uno** (obs. propia): `pepe.el.gato.jpg` hizo "Michi con hambre" y su avatar es el gato.

### D.2 Para el build de prueba, ahora

- Asignar los 40 handles a "usuario 1" hasta "usuario 40", sin olvidar que quien hizo varios personajes escribe con la misma mano.
- El cargador solo acepta `https://` (en `Avatars.load` de `Avatar.kt`). Hay dos salidas: subir los archivos a un hosting propio con HTTPS, o hacer un cambio pequeño para que acepte recursos empaquetados en debug.
- **Un detalle del código:** `Avatar.kt:73` usa `name.trim().take(1)`. Si un nombre empieza con un emoji, la letra de respaldo muestra medio emoji roto. Los 40 nicks de arriba empiezan con letra; si no, habría que tomar el primer carácter completo en vez del primer código.

### D.3 Para producción, después

1. Crear una cuenta "Magic AI Ball" con el sello "Oficial" y el ícono de la app como avatar. Los personajes que escribe el equipo se publican con ese autor.
2. Los primeros creadores reales salen de los testers de la prueba cerrada. Cada uno elige su nick y su avatar y da su consentimiento. No hay que publicar automáticamente el nombre ni la foto de su cuenta de Google.
3. Mostrar solo conteos reales, y poner "Nuevo" mientras no lleguen a un mínimo.
4. Que el usuario acepte términos antes de publicar, que se pueda denunciar y bloquear en la estantería y que haya moderación, como pide la política de contenido generado por usuarios.
5. En las capturas de Play, solo contenido oficial etiquetado o contenido real.

Los nicks para portugués de Brasil quedan fuera de esta tanda.
