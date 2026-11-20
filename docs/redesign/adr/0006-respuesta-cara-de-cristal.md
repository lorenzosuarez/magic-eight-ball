# ADR 0006: la bola responde con la cara de cristal

Estado: aceptada. 20 nov 2026. Sustituye el giro como respuesta por defecto, que venía de la
propuesta B y del [ADR 0001](0001-bola-dibujada-con-compose.md).

## Contexto

- La propuesta B respondía girando: la bola daba media vuelta, enseñaba la ventana de detrás con el
  triángulo y volvía con la otra media vuelta y el gesto. Es lo que hace la bola 8 de juguete.
- Después se probó otra versión, la cara de cristal: la bola cierra los ojos, su cara se vuelve de
  cristal y el triángulo sube detrás, donde estaba la cara. El tablero «Prueba: ¿gira o su cara se
  vuelve ventana?» ponía las dos a la vez, con el mismo ciclo de 7,5 s.
- La auditoría recomendaba usar las dos: «Gira» en el móvil, porque en la bola 8 de verdad la
  ventana está detrás, y la cara de cristal como versión rápida, en el reloj, con «Quitar
  animaciones», al pedir otra respuesta antes de 20 s y en una respuesta abierta desde Recientes.
- El dueño prefirió que el personaje no le dé nunca la espalda.
- Además, la cara de cristal responde medio segundo antes: su triángulo sube a 2,62 s, y el de
  «Gira», a 3,12 s.

## Decisión

1. **La cara de cristal es la única respuesta por defecto, en todas partes:** en el móvil, en el
   reloj, con «Reducir movimiento», en el ritual corto y en el modo fiesta. Sus tiempos, en la
   sección 6 de [especificacion.md](../especificacion.md):
   - cierra los ojos a 2,28–2,3 s y toma aire a 2,3–2,85 s: sube 3,5 y se estira un 2 %;
   - lo que lleva la cara se desvanece a 2,46–2,58 s, y un destello la cruza mientras se aclara, a
     2,52–2,74 s; la ventana toma su tamaño y su bisel a 2,7–2,8 s;
   - el triángulo sube desde 2,62 s bajo una niebla que pasa del 85 % a 0 en 0,45 s;
   - la cara se empaña con otro destello a 5,0–5,22 s, mientras el triángulo se hunde a 4,8–5,25 s,
     y los ojos cerrados vuelven a 5,2–5,3 s;
   - abre los ojos con la mirada del veredicto a 5,85 s, y el gesto empieza a 6,05 s.
2. **«Gira» queda como secundaria, apartada.** No sale por defecto. Su línea de tiempo y su dibujo
   se guardan en la especificación, en «Secundaria: gira».
3. **«Giro clásico»:** si Opciones la ofrece está pendiente de decidir. La recomendación es no
   ofrecerla en el lanzamiento.
4. **El sorteo del modo fiesta** conserva su ruleta, dos o tres vueltas que frenan en 1,6 s. Para de
   cara a ti y su cara se vuelve de cristal con el nombre: no hay ventana detrás.
5. **Mientras llega la respuesta,** la cara de cristal deja ver el líquido en movimiento
   (`LP03Pensando`).
6. **La cara nunca tiene un agujero ni cambia de color,** y no se dice «se le abre la cara».

## Alternativas descartadas

- **«Gira» como respuesta principal:** el personaje te da la espalda justo al responder, y responde
  medio segundo más tarde.
- **Las dos según el caso, como recomendaba la auditoría:** el mismo personaje respondería de dos
  maneras; en el móvil seguiría dando la espalda; y habría dos animaciones que construir y probar.

## Consecuencias

- **Interrupciones:** no hay giro que deshacer. Si la respuesta llega mientras la cara se aclara, el
  triángulo sube ya, bajo la niebla. Otra respuesta antes de 20 s sigue con el ritual corto, de
  0,4 s de agitado. Con «Menos movimiento», la cara de cristal cambia con fundidos de 200 ms, sin
  respiración, ni destello, ni saltos.
- **Ventana:** está detrás de la cara, en su mismo sitio. Mientras la cara se aclara mide 44, el
  radio de la cara, y crece a 46 con su bisel.
- **Código:** `OracleBall` no tiene que girar la bola para responder. La proyección de la esfera se
  sigue construyendo para la sacudida, los gestos que giran la cabeza y el sorteo, y baja el riesgo
  del plan con los móviles modestos.
- **Gestos:** no cambian; empiezan a 6,05 s, como antes.
- **Lienzo:** la prueba pasa a llamarse «La principal y la secundaria», con la cara de cristal a la
  izquierda y «Gira» a la derecha. La respuesta en cinco pasos y la respuesta de cada personaje
  enseñan la cara de cristal. En el catálogo, «Responde» es la cara de cristal y «Gira · secundaria»
  va la última.
- **ADR 0001:** decía que la bola gira para enseñar la ventana; su actualización apunta aquí.
