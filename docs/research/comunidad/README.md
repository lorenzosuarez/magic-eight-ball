# Comunidad: cómo escribe la gente, avatares y nicks

Investigación de noviembre de 2026 para que los personajes de muestra de la Comunidad se lean como hechos por personas en su celular, y para decidir qué avatares y qué nicks son creíbles. El banco de muestra solo existe en la build de prueba (`app-phone/src/debug/.../di/CommunityModule.kt`); la app publicada no lista personajes de la Comunidad hasta que haya una base de datos.

## Archivos

- [voz-en-redes.md](voz-en-redes.md): qué hace gracioso y compartible un texto corto, los formatos de humor que siguen vivos, las marcas de escritura de cada país y qué delata un texto escrito en serie.
- [personajes-de-usuarios.md](personajes-de-usuarios.md): qué personajes hechos por usuarios se vuelven populares en otras plataformas, cómo se nombran y se describen, y sus riesgos legales.
- [avatares-y-nicks.md](avatares-y-nicks.md): fuentes de imágenes y sus licencias, si se pueden inventar usuarios en producción y cómo son los nicks de verdad.
- [diagnostico-del-banco-anterior.md](diagnostico-del-banco-anterior.md): qué fallaba en el banco anterior, entrada por entrada.
- [guia-de-escritura.md](guia-de-escritura.md): la guía con la que se reescribió el banco.
- La investigación anterior, [../prompts-de-comunidad.md](../prompts-de-comunidad.md), sigue valiendo en lo que dice de la uniformidad, las tríadas y el punto final.

## Lo esencial

1. El banco anterior no fallaba por la ortografía sino por la materia prima: 27 de los 128 nombres eran un oficio visto desde afuera, casi ninguno usaba un formato de internet actual, y 40 autores escribían con 13 manos.
2. Lo que la gente comparte se reconoce al instante, es específico y casi siempre trata de uno mismo, de un vínculo (mi ex, mi gata, mi jefe, el grupo de la familia) o de una escena mínima de todos los días. El autodesprecio es el humor más frecuente.
3. Cada descripción tiene un gancho en los primeros 40 caracteres, porque la estantería muestra el nombre y una sola línea, y una frase de ejemplo con la voz en lugar de reglas: la app ya sortea el sí o el no y corta la respuesta.
4. La región entra por la gramática (voseo, vosotros, el usted cariñoso) y por una palabra, no por cinco. Juntar modismos es la caricatura que más rápido se detecta.
5. A un banco escrito en serie lo delata el conjunto: los largos pegados al techo de cada tramo, el mismo remate, la misma muletilla en todas las entradas de un autor, las premisas repetidas. Una última pasada corrigió eso con medidas sobre el banco entero.
6. Nada de personas vivas ni reconocibles, marcas, franquicias, menores, sexo, drogas, apuestas ni consejos de plata o salud. Mitos y figuras históricas sí, sin su parte trágica.

## Los creadores

Cada uno tiene país, edad, una mano (registro, risa, emoji o ninguno, una muletilla que no comparte con nadie, cómo escribe los nombres, cuánto se alarga) e intereses de los que salen sus personajes. Los nicks se inventaron para la build de prueba y no se comprobó si existen en alguna plataforma.

| Nick | País | Edad | Personajes | Avatar |
|---|---|---|---|---|
| `ferchis.mtz` | México (CDMX) | 27 | 9 | Su perrita salchicha con suéter rosa, un poco movida (foto propia de celular; si falta, Unsplash o Pexels sin personas) |
| `tincho.tiradados` | Argentina (Córdoba) | 33 | 7 | Un dado de 20 caras sobre su mesa de rol con luz amarilla (foto propia; si no, Pexels) |
| `nuria.lee` | España (Madrid, de Cádiz) | 29 | 7 | De espaldas mirando el mar en Cádiz, sin cara (foto propia de móvil) |
| `valen.astral` | Colombia (Medellín) | 24 | 6 | Luna y estrellas en 3D sobre fondo lila (Fluent Emoji 3D, MIT) |
| `dany.mp4` | México (Guadalajara) | 22 | 5 | Garabato propio de su gato naranja en estilo anime (dibujado a mano) |
| `elnicof` | Argentina (Buenos Aires) | 38 | 4 | Sin foto: la inicial sobre color (la dibuja la app) |
| `el_rudo_91` | México (Monterrey) | 35 | 4 | Una máscara de luchador sin logos colgada en la pared de su cuarto (foto propia; si no, Pixabay) |
| `cata.sinfiltro` | Chile (Santiago) | 30 | 4 | Su mano con una taza de té y las uñas pintadas, sin cara (foto propia o Unsplash) |
| `gabo.vzla` | Venezuela (Caracas, vive en Madrid) | 32 | 4 | El loro verde de su abuela en la jaula del patio (foto propia de celular) |
| `laucardenas_` | Colombia (Bogotá) | 29 | 4 | Su plantica de escritorio junto al computador, sin logos (foto propia o Pexels) |
| `renzito_404` | Perú (Lima) | 23 | 4 | Avatar pixel art generado con su nick como semilla (DiceBear Pixel Art, CC0, prerenderizado) |
| `javi_zgz` | España (Zaragoza) | 41 | 4 | Sin foto: la inicial sobre color |
| `luuwera` | México (CDMX) | 26 | 3 | Tacos al pastor en plato de plástico (foto propia o Unsplash) |
| `fede.manana` | Argentina (Rosario) | 28 | 2 | Sin foto: la inicial sobre color |
| `gus_80` | Uruguay (Montevideo) | 46 | 2 | La rambla al atardecer (foto propia de celular o Unsplash) |
| `itsyaz.la` | EE. UU. (Los Ángeles, familia de Michoacán) | 25 | 3 | Uñas pintadas en 3D sobre fondo rosa (Fluent Emoji 3D, MIT) |
| `benjaa_` | Chile (Santiago) | 27 | 3 | Sin foto: la inicial sobre color |
| `chuy_poblano` | México (Puebla) | 41 | 3 | Carretera con un volcán al fondo desde el parabrisas (foto propia o Pexels) |
| `laiadibuixa` | España (Barcelona) | 34 | 3 | Garabato propio de una paloma con cara de susto |
| `andres.curramba` | Colombia (Barranquilla) | 31 | 2 | Un sombrero vueltiao sobre una silla de plástico (foto propia o Pixabay) |
| `rodri.legday` | México (Querétaro) | 27 | 2 | Selfie de espejo en el gym con el celular tapándole la cara (foto propia) |
| `cacho_1969` | Argentina (Buenos Aires) | 57 | 1 | Su perro mestizo durmiendo en la vereda de la ferretería (foto propia o Unsplash) |
| `ing.marco.aqp` | Perú (Arequipa) | 35 | 2 | El volcán Misti desde una azotea (foto propia o Pexels) |
| `ale.y.ya` | México (CDMX) | 30 | 2 | Piñata de estrella en 3D (Fluent Emoji 🪅, MIT) |
| `isaocampo_` | Colombia (Cali) | 26 | 2 | De espaldas con la toga de graduación mirando un auditorio, sin cara (foto propia) |
| `yolimar.c` | Venezuela (Maracaibo, vive en Lima) | 44 | 2 | Una torta tres leches recién hecha (foto propia o Unsplash) |
| `seba_cr` | Chile (Valparaíso) | 29 | 2 | Su gata negra acostada sobre el teclado (foto propia) |
| `jandro_787` | EE. UU. (Chicago, familia boricua) | 34 | 2 | Un tocadiscos con un vinilo girando (foto propia o Pexels) |
| `irenita.svq` | España (Sevilla) | 24 | 2 | Una pila de libros en el suelo con su gato encima (foto propia) |
| `user5839201` | México (Tijuana) | 21 | 2 | Sin foto: la inicial sobre color (la cuenta por defecto) |
| `flor.escorpiana` | Argentina (Mendoza) | 24 | 2 | Garabato propio de un escorpión en el margen de un cuaderno |
| `pepegtz` | México (Toluca) | 33 | 1 | Sin foto: la inicial sobre color |
| `jhonmono_` | Colombia (Pereira) | 37 | 1 | Un manojo de llaves sobre una mesa de madera (foto propia o Pixabay) |
| `karlitaa.pech` | México (Mérida) | 28 | 1 | Un pastel que le salió chueco (foto propia) |
| `diego.aji` | Perú (Lima) | 30 | 1 | Un ají amarillo sobre una tabla de picar (foto propia o Pexels) |
| `coniii.v` | Chile (Puerto Montt) | 32 | 1 | Lluvia en la ventana con cerros verdes (foto propia) |
| `caro.mvd` | Uruguay (Montevideo) | 29 | 1 | Sus pies con medias gruesas sobre una frazada a cuadros, sin cara (foto propia) |
| `lalo.y.panchita` | México (León) | 24 | 1 | Panchita, su chihuahua, enojada y con suéter (foto propia) |
| `beaa.gm` | España (Valencia) | 27 | 1 | Una monstera con una hoja amarilla junto a la ventana (foto propia) |
| `julietaa.r` | Argentina (Buenos Aires, Caballito) | 31 | 1 | La vista desde su balcón del séptimo piso (foto propia) |
| `santi_rmz` | Colombia (Bucaramanga) | 22 | 1 | Avatar dibujado generado con su nick como semilla (DiceBear Thumbs, CC0) |
| `majo.jpg` | México (Hermosillo) | 19 | 1 | Avatar abstracto con su nick como semilla (Boring Avatars 'beam', MIT) |
| `toni_mf` | España (Teruel, vive en Valencia) | 38 | 1 | El pueblo desde la carretera al atardecer (foto propia) |
| `fio.rq` | Perú (Lima) | 27 | 1 | Pulseras de hilo en su muñeca, sin cara (foto propia) |
| `andreina.vp` | Venezuela (Valencia, vive en Panamá) | 26 | 1 | Nubes desde la ventanilla del avión (foto propia o Unsplash) |
| `solee.ibarra` | Argentina (La Plata) | 35 | 1 | Su mano sosteniendo el celular mientras graba un audio (foto propia) |
| `dario.lanus` | Argentina (Lanús) | 43 | 1 | Sin foto: la inicial sobre color |
| `sofii.rdz` | México (Saltillo) | 25 | 1 | Un golden retriever con la lengua afuera en el parque (foto propia o Unsplash) |
| `mafe.rg` | Colombia (Bogotá) | 31 | 1 | Avatar abstracto con su nick como semilla (Boring Avatars 'sunset', MIT) |
| `wilkin.pz` | República Dominicana (Santiago de los Caballeros) | 31 | 2 | La silla de su barbería con la capa colgada, sin nadie sentado (foto propia de celular) |
| `osmany.rdgz` | Cuba (La Habana) | 54 | 2 | Fichas de dominó sobre la mesa del portal (foto propia de celular) |
| `dome.slzr` | Ecuador (Quito) | 26 | 1 | Un plato de encebollado con chifles en la mesa de un mercado (foto propia de celular; si no, Unsplash) |
| `fabi_ulate` | Costa Rica (Puntarenas) | 39 | 1 | El muelle de Puntarenas al atardecer (foto propia de celular) |

## Avatares

La estantería ya dibuja, para quien no tiene foto, la inicial sobre un color propio de cada nombre. Para que se parezca a una red social:

| Tipo | Parte | De dónde sale en la build de prueba |
|---|---|---|
| Sin foto: la inicial sobre color | 1 de cada 5 | La dibuja la app |
| Mascotas | 1 de cada 5 | Fotos propias o de amigos con permiso; si faltan, Unsplash, Pexels o Pixabay |
| Comida, objetos, plantas, paisajes | 1 de cada 4 | Fotos propias, Unsplash, Pexels o Pixabay, sin logos |
| Selfie sin cara: de espaldas, una mano, un reflejo | 1 de cada 8 | Fotos propias con consentimiento |
| Dibujado, 3D o emoji | 1 de cada 6 | DiceBear (solo los estilos CC0, generados de antemano con el nick como semilla), Fluent Emoji 3D (MIT), Boring Avatars (MIT), Noto Emoji (Apache 2.0) |
| Garabato hecho a mano | 1 de cada 12 | Dibujado a mano |
| Caras realistas, de stock o hechas con IA, y personajes de franquicias | ninguno | Descartados |

- Unsplash, Pexels y Pixabay permiten uso comercial sin atribución, pero no cubren a personas reconocibles ni permiten sugerir que alguien de la foto respalda la app: por eso no van caras.
- La API HTTP de DiceBear es gratis solo para uso no comercial: los avatares se generan con la librería y se guardan como archivos.
- This Person Does Not Exist, Generated Photos y las fotos de randomuser.me quedan fuera por licencia o por sus términos.
- El cargador de fotos solo acepta direcciones `https://` (`Avatars.load`). Para ver avatares en la build de prueba hay que alojarlos en un hosting propio o dejar que acepte imágenes empaquetadas en debug.
- La inicial ahora es siempre una letra entera: un nombre que empieza con un emoji o un ✨ muestra su primera letra y nunca medio emoji.

## Nicks

- Minúsculas, sin tildes ni ñ (las plataformas no las aceptan), con puntos o guiones bajos, y como mucho 30 caracteres.
- Un nombre de pila con algo que lo vuelva único: letras dobladas (`sofii`), apellido abreviado (`mtz`, `rdz`), un número de la suerte o un código de área (`787`). Nada de años recientes: del 2008 en adelante se lee como la edad de un menor.
- No resumen el perfil (`karla.cocina.mal`) ni dicen siempre la ciudad: eso es una etiqueta para quien lee, no un nick. Algunos sí lo hacen, como pasa de verdad (`javi_zgz`, `caro.mvd`).
- Alguien nunca cambió el que le dio la app (`user5839201`).

## En producción

Inventar usuarios, avatares o conteos de uso en la app publicada es prueba social falsa: choca con las políticas de Google Play (Deceptive Behavior, Misrepresentation, Metadata) y con leyes de consumo como la directiva europea 2005/29/CE (anexo I, puntos 22 y 23c). Lo que hacen las apps serias:

1. Una cuenta oficial con su sello para lo que escribe el equipo, como las listas editoriales de Spotify.
2. Los primeros creadores reales salen de los testers de la prueba cerrada, que eligen su nick y su avatar y dan su consentimiento.
3. Conteos reales, con "Nuevo" mientras no lleguen a un mínimo.
4. Términos aceptados antes de publicar, denuncia y bloqueo en la estantería y moderación, como pide la política de contenido generado por usuarios.
5. En las capturas de la ficha de Play, solo contenido oficial etiquetado o real.

No es asesoría legal.
