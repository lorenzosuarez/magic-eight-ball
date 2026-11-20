# ADR 0007: doce personajes, con sus colores y sus siluetas

Estado: aceptada. 20 nov 2026. Sustituye el ámbar del punto 2 y los colores del Mago y del Filósofo
del punto 7 del [ADR 0004](0004-componentes-luz.md).

## Contexto

- Había ocho personajes: Vidente, Genio, Bruja, Mago, Filósofo, Astróloga, Robot y Pirata.
- Las auditorías midieron que cuatro luces no llegaban a ΔE00 18 frente a la interfaz: el Genio
  quedaba a 4 del ámbar; la Astróloga, a 7 del coral; la Vidente, a 12 del azul del ✓ y a 15 del
  cobalto; y el Filósofo, a 17 del gris de los iconos.
- Varias siluetas no se distinguían a 48 px. Por ejemplo, la Bruja y el Mago llevaban dos sombreros
  puntiagudos con ala.
- El tablero «Propuestas para decidir» reunía los colores, las siluetas y las miradas que proponían
  las auditorías.
- Con esos colores, solo quedaban cuatro huecos de color para un personaje más: un verde vivo, un
  dorado viejo, un rosa pálido y un verde salvia.

## Decisión

1. **Colores aprobados:** Vidente `#BC4BFF`, Genio `#F5CB45`, Mago `#45D3F2`, Filósofo `#B0A08A` y
   Astróloga `#FF7EC8`. La Bruja, el Robot y el Pirata no cambian. Los triángulos, sus bordes y los
   colores de los accesorios están en la sección 3 de [especificacion.md](../especificacion.md).
2. **El ámbar de los avisos pasa a `#FF9F43`,** y a `#E9913D` al pulsar, para dejar sitio al Genio.
   `onWarning` (`#2A1A00`) da 8,3:1 sobre él y 6,9:1 pulsado.
3. **Siluetas y miradas:**
   - el Genio lleva un moño dorado con anillo, sin humo;
   - la Vidente cierra los ojos con una calma más honda;
   - la Bruja lleva ala ancha y un cono corto y doblado, con cinta y hebilla, y mira de reojo;
   - el Filósofo lleva gafas de pasta, y la barra de arriba de cada aro le hace de párpado;
   - la Astróloga lleva la luna pegada a la bola y la mira, arriba a la izquierda; sin estrellas;
   - el Mago no lleva ala: un cono alto y estrecho que cae hacia atrás, con su estrella.
4. **Cuatro personajes nuevos,** dos con humor y dos más tiernos (es / en / pt; los nombres en
   inglés y portugués, por confirmar):
   - **Gata / Cat / Gata,** `#2DC511`: una gata negra cuya cabeza es la bola, con ojos de rendija.
     Dice que sí con un parpadeo lento y que no apartando la cara. Presumida y perezosa.
   - **Comentarista / Commentator / Narrador,** `#B08C19`: cascos con micrófono y ojos muy
     abiertos. Su sí es un gol y su quizá va al VAR. Eufórico y exagerado.
   - **Repostera / Pastry chef / Confeiteira,** `#F5D1FB`: gorro de cocina con una guinda que bota y
     ojos que sonríen. Antes de decidir, lo prueba. Dulce y perfeccionista.
   - **Dino / Dino / Dino,** `#819865`: una cresta de cinco placas y ojos pequeños. Reacciona tarde
     y su sí es un pisotón. Antiquísimo y bonachón.
5. **En Personajes, dos grupos:** «Los de siempre», con los seis clásicos, y «Con actitud», con el
   Robot, el Pirata y los cuatro nuevos.
6. **Presupuesto de color:** las doce luces quedan a ΔE00 18 o más del cobalto, del ámbar
   `#FF9F43`, del coral y de los grises, y a 17,9 o más entre sí. Los pares más cercanos son Genio y
   Comentarista (17,9), Robot y Gata (18,0), Bruja y Dino (18,3) y Filósofo y Dino (18,3). No quedan
   huecos: un personaje más tendrá que compartir familia de color con otro y distinguirse por su
   silueta.

## Alternativas descartadas

- **Quedarse con los colores de antes:** cuatro se confundían con la interfaz.
- **Mover el Genio y no el ámbar:** `#E8D27A` quedaba a 14,4 del ámbar de antes y a 16 del Robot, y
  `#ECDC51`, a 18 del ámbar pero a 12 del Robot.
- **Las otras opciones medidas** para la Vidente (`#CC60FB`), la Astróloga (`#F692CE` o `#F28FD0`)
  y el Filósofo (`#DFBFAA`): con los doce, la Vidente quedaría a 17,3 de la Astróloga, la Astróloga
  a 15,5 o 15,8 de la Repostera y el Filósofo a 17,9 del ámbar nuevo.

## Consecuencias

- **Especificación:** cambian el ámbar (sección 2), la tabla de personajes (sección 3), las miradas
  (sección 4), los gestos (sección 6), la lista de Personajes con `LP80PersonajesConActitud` y el
  reloj (sección 7), y los contrastes del ámbar (sección 8). `OracleColors` gana `triangleEdge`, el
  borde del triángulo.
- **Contraste:** las doce luces dan 4,5:1 o más sobre `bg` y `surface`, así que la etiqueta sigue
  siendo la luz. Las más justas son el Pirata y la Vidente, con 5,25 y 5,26:1 sobre `bg`. El texto
  de los triángulos nuevos da 4,5:1 o más donde va.
- **Textos nuevos en los tres idiomas:** los nombres, el carácter y las frases de los cuatro nuevos,
  y las cabeceras de los dos grupos. Su voz para Gemini y sus frases sin conexión llegan en la fase 4
  del [plan](../plan.md).
- **Pendiente** (sección 7 de la especificación): si los cuatro nuevos son gratis o de Premium (la
  recomendación es gratis: traen a la gente de vuelta, y Premium vende las respuestas con IA y los
  personajes propios); sus nombres en inglés y portugués; y los colores de su tarjeta de compartir.
- **Ningún personaje se presenta por una edad:** la app es para adolescentes y adultos, con anuncios
  y respuestas de IA, y una app dirigida a menores tendría que cumplir la política de familias de
  Google Play. La Repostera y el Dino son tiernos, nada más.
- **ADR 0004:** su ámbar y los colores del Mago y del Filósofo quedan sustituidos; ver su
  actualización.
- **Lienzo:** «Propuestas para decidir» se quitó, porque ya está decidido. «Cuatro personajes
  nuevos» va junto a los personajes, y los tableros de personajes enseñan los doce.
