# Premium: precio, mercado y la meta de 500 € al mes

Estudio del 17 de noviembre de 2026, con Premium a 0,99 € al mes (IVA incluido) y la app a punto de salir de la prueba cerrada. Las cifras salen de los tres informes de esta carpeta, que enlazan cada fuente con su fecha:

- [competidores.md](competidores.md): quién vende qué y a qué precio en bola 8, decisión, tarot con IA y personajes de IA.
- [conversion-y-anuncios.md](conversion-y-anuncios.md): cuánta gente paga, cuánto dura un suscriptor y cuánto dan los anuncios.
- [captacion.md](captacion.md): qué cuesta una instalación, qué se busca, y cómo consiguen usuarios las apps pequeñas.

Lo marcado como estimación es cálculo propio sobre esas cifras, no un dato publicado.

Las propuestas que salen de este estudio, con el estado de cada paso de la salida a 18 de noviembre y la idea de pasar a personajes, están en [propuestas.pdf](propuestas.pdf), que se imprime con Chrome desde [propuestas.html](propuestas.html).

## Veredicto

- **500 € netos al mes en los primeros meses no es realista** solo con la suscripción a 0,99 € y sin audiencia. Hacen falta unos 715 suscriptores activos, que piden entre 7.000 y 22.000 instalaciones nuevas cada mes, sostenidas. La app que lidera «magic 8 ball» en Google Play recibe unas 5.600 al mes.
- **Es alcanzable en 6 a 12 meses si pasan dos cosas**: que un vídeo corto de gente usando el modo fiesta pegue, y que a la suscripción se sumen los anuncios y un plan anual o un pago único.
- **Comprar instalaciones no se paga**: a este precio se recupera entre el 2 % y el 35 % de lo gastado.
- **0,99 € es un precio de entrada correcto** para España y Latinoamérica: es lo que cobran las dos apps de decisión grandes que tienen suscripción. En Estados Unidos esas mismas cobran 1,99 USD.

## La cuenta

Lo que queda de cada pago en España, después del IVA (21 %) y de la comisión de Google Play (15 % en suscripciones):

| Plan | Precio | Neto | Suscriptores para 500 € al mes |
|---|---|---|---|
| Mensual | 0,99 € | 0,70 € | 715 |
| Mensual | 1,99 € | 1,40 € | 358 |
| Mensual | 2,99 € | 2,10 € | 238 |
| Anual | 6,99 € | 4,91 € al año | 1.222 al año (102 altas al mes) |

Un plan mensual barato pierde gente rápido: renuevan el 60 %, luego el 75 % y el 81 % de los que quedan, y a los 12 meses sigue el 10,8 %. Un suscriptor paga de media unas 4 a 5 veces: a 0,99 € deja unos 2,8 a 3,4 € netos en total.

Para sostener 715 suscriptores hacen falta unas 146 altas al mes. Según cuánta gente pague tras instalar:

| Conversión de instalación a pago | De dónde sale | Instalaciones al mes |
|---|---|---|
| 0,66 % | mediana de entretenimiento en Google Play | 22.000 |
| 1,0 % | mediana de usuarios de Latinoamérica | 14.600 |
| 2,0 % | cuartil alto de entretenimiento en Google Play | 7.200 |

Los anuncios, con un intersticial cada varias respuestas: entre 1 y 5 USD al día por cada 1.000 usuarios diarios en España y Latinoamérica (más en España, menos en Latinoamérica). Solo con anuncios, 500 € al mes pedirían unos 3.800 usuarios diarios en España o 6.300 en México.

## Tres escenarios (estimación)

| Escenario | Qué tendría que pasar | Instalaciones al mes | A los 6 meses |
|---|---|---|---|
| Pesimista | Solo la búsqueda de «bola 8 mágica», sin vídeos | 300 a 400 | unos 10 suscriptores: menos de 15 € al mes |
| Razonable | Ficha orientada a «sí o no», vídeos cada semana, alguno con buena salida | 2.000 a 5.000 | 80 a 200 suscriptores más anuncios: 75 a 200 € al mes |
| Bueno | Un vídeo del modo fiesta con millones de visualizaciones y una cola después | 20.000 o más en el mes del pico | 500 € al mes posibles desde el tercer mes, si la retención acompaña |

El 84,5 % de las apps publicadas en Play en 2026 sigue por debajo de 1.000 instalaciones. Si va a funcionar, suele verse en los dos o tres primeros meses.

## Precio: qué cambiar y cuándo

1. **Dejar 0,99 € al mes en España y Latinoamérica.** Bajarlo no trae más gente y subirlo ahora, sin reseñas, quita la única ventaja frente a las apps de IA de 9,99 €.
2. **Subir Estados Unidos, Reino Unido y el norte de Europa a 1,99.** Es lo que cobra el nicho allí, y se cambia en Play Console sin tocar la app.
3. **Añadir un plan anual de 6,99 €** (el precio de siete meses). Cobra por adelantado a quien, con el mensual, se iría en la primera renovación. Pide un cambio en la app: hoy solo vende el plan `monthly`.
4. **Valorar un pago único "para siempre" de 12,99 a 14,99 €.** En bola 8 y decisión lo que más se vende es anuncios más un pago único; la suscripción es rara. También pide cambio en la app y en el servidor, que ya sabe guardar un Premium sin fecha de fin.
5. **Prueba gratis de 3 a 7 días** solo si, pasado el primer mes, paga menos del 1 % de quienes instalan.

Para el pago único conviene inscribirse antes en el programa de comisión del 15 % de Google Play (Play Console lo ofrece en sus avisos): sin él, las compras únicas pagan el 30 %.

## Campañas

No para ganar: para medir. Un presupuesto de 300 a 500 €, dado por gastado.

- **Cuándo:** del 26 de diciembre a mediados de enero, con la app ya en producción.
- **Dónde:** Google App Campaigns, solo instalaciones, Android, México más Colombia o Argentina: 15 a 20 € al día durante 14 días, unas 700 a 1.500 instalaciones.
- **Qué medir:** coste real por instalación, cuántos vuelven al día siguiente y a la semana (referencia: 20 % y 5 a 6 %), cuántos se suscriben, cuántos comparten una respuesta, qué personaje engancha.
- **Regla:** si se suscribe menos del 2 al 3 % y a la semana vuelve menos del 8 %, no poner más dinero.

Antes, lo que no cuesta dinero:

1. **La ficha de Play.** Casi nadie busca «bola 8 mágica» en español ni en portugués. La demanda está en «sí o no», «ruleta», «tarot sí o no» y «oráculo»: título, descripción corta y capturas deben decirlo.
2. **Vídeo corto del modo fiesta**, grabado en grupo, vertical: primero en TikTok, el mismo en Reels y Shorts. Es lo que dio millones de instalaciones al juego del impostor entre diciembre de 2025 y enero de 2026. Diez mil instalaciones son entre 1 y 10 millones de visualizaciones.
3. **Que cada respuesta compartida lleve el nombre de la app** bien visible, y que se pueda compartir como vídeo vertical además de como imagen.
4. **El veredicto diario**, con frases que den ganas de capturar y enviar: así creció Co-Star sin publicidad.
5. **Regalar Premium o un personaje a medida** a creadores pequeños, en lugar de pagarles: un vídeo de 100 € necesitaría 400.000 visualizaciones para devolverlos.

## Si la app está lista

Como producto, sí: la compra, la confirmación en el servidor, la cancelación y el vencimiento se probaron de punta a punta el 17 de noviembre. Para que dé ingresos le falta, por orden de impacto:

1. **Los anuncios encendidos** (`ads_enabled` está apagado) con un ritmo que no eche a nadie: son la mitad del ingreso en este nicho.
2. **El plan anual** y, después, el pago único.
3. **Vender en el momento justo:** al agotarse las respuestas con IA del día y al querer crear un personaje.
4. **La ficha de Play** reescrita para lo que la gente busca.
5. **Medir el embudo desde el primer día:** instalaciones por origen, vuelta al día 1 y al día 7, preguntas por usuario, cuántos agotan la IA, cuántos ven la hoja de Premium, cuántos compran y cuántos renuevan la primera vez.

Números que indicarían cambiar de plan: menos del 0,5 % de pago al mes de instalar (revisar la oferta o probar la prueba gratis); primera renovación por debajo del 50 % (Premium no da lo bastante); menos del 15 % vuelve al día siguiente (el problema es la app, no el precio).

## Lo que quedó sin verificar

- Ningún informe publica conversión ni retención para un precio de 0,99: las cifras son de "precio bajo" en general.
- El precio exacto de cada plan de los competidores en Google Play (la tienda solo publica el rango); los mensuales se leyeron en el App Store.
- Los ritmos de instalación de otras apps son estimaciones de un tercero (AppGoblin), no datos de Google.
- No se midió cuánto recorta el alcance la clasificación para mayores de 18.
