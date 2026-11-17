# Benchmarks para prever ingresos de Magic AI Ball (suscripción y anuncios)

Consulta: 17-nov-2026. Las cifras de RevenueCat son percentiles leídos de los datos de sus gráficos (P25 / mediana / P75). Lo marcado como "estimación propia" es cálculo mío, no dato publicado.

## 1. Lo esencial

- **Conversión en Google Play:** la mediana de instalación a pago en 35 días es 0,90 %, y 0,66 % en Media & Entertainment. En App Store es 2,58 %.
- **LatAm convierte menos:** 1,03 % frente a 1,81 % en Europa occidental y 2,83 % en Norteamérica.
- **Ingreso por instalación a 60 días en Google Play (mediana):** 0,06 USD en LatAm, 0,23 USD en Europa occidental, 0,29 USD en Norteamérica.
- **Plan mensual barato:** renuevan 60 %, 75 % y 81 % en las tres primeras renovaciones; a 12 meses sigue el 10,8 %.
- **Precio:** 0,99 queda muy por debajo de la mediana mensual en Play (4,65 USD en LatAm, 9,36 USD en Europa occidental).
- **Comisión de Play:** 15 % en total para suscripciones. El neto de 0,70 EUR en España es coherente con las fuentes.
- **Anuncios:** un intersticial en Android rinde entre 1 y 5 USD al día por cada 1.000 usuarios diarios en España y LatAm (datos de 2024).

Fuente de los cinco primeros puntos: [RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026, datos de 2025, más de 115.000 apps. Su "LatAm" es Brasil, México, Argentina, Chile, Colombia y Perú; "Europa occidental" incluye España.

## 2. Conversión de instalación a pago (35 días)

Fuente: [RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026.

| Segmento | P25 | Mediana | P75 |
|---|---|---|---|
| Todas las apps | 0,68 % | 1,99 % | 4,60 % |
| Google Play, todas las categorías | 0,26 % | 0,90 % | 2,55 % |
| Google Play, Media & Entertainment | 0,20 % | 0,66 % | 2,03 % |
| Google Play, juegos | 0,08 % | 0,37 % | 1,19 % |
| App Store, todas | 1,07 % | 2,58 % | 5,39 % |
| Usuarios de LatAm | 0,29 % | 1,03 % | 2,61 % |
| Usuarios de Europa occidental | 0,67 % | 1,81 % | 4,04 % |
| Usuarios de Norteamérica | 0,99 % | 2,83 % | 6,04 % |
| Precio bajo | 0,42 % | 1,44 % | 3,67 % |
| Precio medio | 0,72 % | 1,96 % | 4,35 % |
| Precio alto | 1,04 % | 2,81 % | 6,07 % |
| Freemium | 0,83 % | 2,13 % | 4,50 % |
| Muro de pago obligatorio | 6,53 % | 10,70 % | 20,04 % |

- "Precio bajo, medio y alto" es relativo al conjunto de apps; RevenueCat no publica umbrales. No hay dato para 0,99.
- No se publica el cruce Google Play × LatAm para conversión.
- El resumen "Apple vs. Google" del informe dice 2,9 % frente a 2,6 % y 0,23 USD. Los datos del gráfico, que uso aquí, dicen 2,58 % frente a 0,90 % y 0,16 USD.

**Con y sin prueba gratuita**

| Mediana | Global | LatAm | Europa occ. | Norteamérica | Precio bajo | Media & Ent. |
|---|---|---|---|---|---|---|
| Descarga a prueba (30 días) | 5,79 % | 3,70 % | 5,00 % | 7,14 % | 4,43 % | 3,98 % |
| Prueba a pago | 32,15 % | 23,53 % | 29,72 % | 34,19 % | n/d | 32,22 % |

Fuente: [RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026.

- **Por tienda:** prueba a pago 32,47 % en Google Play y 32,04 % en App Store; en Media & Entertainment sobre Google Play, 34,90 %. La diferencia entre tiendas está antes de la prueba, no después. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)
- **Duración de la prueba:** 25,46 % con 4 días o menos, 37,39 % con 5 a 9 días, 42,47 % con 17 a 32 días. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)
- **Sin prueba:** el 37,2 % de las apps de Media & Entertainment no ofrece prueba. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)
- **Adapty (medias, base dominada por iOS):** instalación a prueba 10,9 %, prueba a pago 25,6 % (Entertainment 19,1 %), compra directa sin prueba 3,71 %. ([Adapty](https://adapty.io/blog/how-much-does-a-subscription-app-make/), 22-mar-2026; [informe](https://adapty.io/state-of-in-app-subscriptions-2026), 2026)
- **iOS frente a Android según Adapty:** iOS convierte unas 3 veces más; en planes semanales, 1,55 % frente a 0,26 %. ([Adapty](https://adapty.io/blog/how-much-does-a-subscription-app-make/), 22-mar-2026)
- **Cuándo pagan:** en LatAm el 53,7 % de las conversiones ocurre el día 0 y el 18,9 % desde la semana 6; en Europa occidental, 50,7 % y 21,2 %. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)

## 3. Retención de planes mensuales

Renovaciones (mediana). Fuente: [RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026.

| Segmento | 1.ª | 2.ª | 3.ª |
|---|---|---|---|
| Precio bajo | 60,1 % | 75,0 % | 80,6 % |
| Precio medio | 55,3 % | 72,0 % | 77,9 % |
| Precio alto | 51,2 % | 68,0 % | 75,0 % |
| Google Play | 57,6 % | 71,4 % | 77,6 % |
| App Store | 54,1 % | 72,0 % | 78,0 % |
| LatAm | 53,6 % | 70,4 % | 77,0 % |
| Europa occidental | 55,2 % | 72,2 % | 78,1 % |
| Norteamérica | 54,9 % | 72,3 % | 78,2 % |
| Media & Entertainment | 57,8 % | 74,5 % | 79,7 % |

Suscriptores mensuales que siguen (P25 / mediana / P75). Misma fuente.

| Segmento | A 6 meses | A 12 meses |
|---|---|---|
| Todas | 10,0 / 18,3 / 30,2 % | 3,6 / 8,2 / 16,4 % |
| Media & Entertainment | 11,1 / 22,0 / 37,2 % | 4,1 / 10,5 / 22,8 % |
| Precio bajo | n/d | 4,6 / 10,8 / 21,8 % |
| Precio alto | n/d | 2,6 / 6,1 / 11,4 % |
| Apps de IA | n/d | 3,3 / 6,1 / 11,8 % |
| Apps sin IA | n/d | 4,7 / 9,5 / 18,5 % |

- **Tendencia:** la retención mensual a 12 meses bajó de 9,6 % a 8,2 % entre cohortes. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)
- **Bajas en Google Play:** 66,3 % voluntarias y 32,3 % por error de cobro (App Store 15,2 %). En la edición anterior eran 28,2 %. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026; [RevenueCat 2025](https://www.revenuecat.com/state-of-subscription-apps-2025/), marzo 2025)
- **Reembolsos (mediana):** 2,7 % en precio bajo, 6,1 % en LatAm, 4,1 % en Europa occidental, 3,5 % en Norteamérica. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)
- **Reactivación en 12 meses de bajas mensuales:** 19,7 % en LatAm, 21,7 % en Europa occidental, 15,4 % en precio bajo. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)
- **Adapty 2025 (medias):** plan mensual, 43 % retenido al día 90 y 17 % activo al año. ([Adapty](https://adapty.io/blog/state-of-in-app-subscriptions-2025-in-10-minutes/), 16-jul-2025)
- **Estimación propia:** con 60 / 75 / 81 % y 10,8 % a 12 meses, cada renovación posterior ronda el 87 %. Eso da unos 4,1 pagos por suscriptor nuevo el primer año y unos 4,9 en toda su vida.

## 4. Mensual frente a anual

- **Planes vendidos (semanal / mensual / anual):** LatAm 29,0 / 40,6 / 29,1 %; Europa occidental 21,7 / 41,0 / 35,3 %; Norteamérica 23,0 / 36,2 / 39,7 %. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)
- **Media & Entertainment, reparto de ingresos:** 15,6 % semanal, 35,5 % mensual, 33,3 % anual, 15,7 % otros. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)
- **Adapty, ingresos de 2025:** 55,5 % semanal, 11,7 % mensual, 22,5 % anual, 10,3 % pago único. ([Adapty](https://adapty.io/state-of-in-app-subscriptions-2026), 2026)
- **Retención a 12 meses:** 27,5 % en plan anual (36,1 % en precio bajo) frente a 8,2 % en mensual. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)
- **Precio mediano en Google Play (mensual / anual):** LatAm 4,65 / 19,99 USD; Europa occidental 9,36 / 36,10 USD; Norteamérica 9,99 / 39,99 USD. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)

## 5. Ingreso por instalación

USD, P25 / mediana / P75. Fuente: [RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026.

| Segmento | Día 14 | Día 60 |
|---|---|---|
| Todas | 0,07 / 0,23 / 0,65 | 0,10 / 0,34 / 0,93 |
| Media & Entertainment | 0,04 / 0,15 / 0,45 | 0,06 / 0,23 / 0,65 |
| Precio bajo | 0,02 / 0,08 / 0,24 | 0,03 / 0,11 / 0,33 |
| LatAm, todas las tiendas | 0,03 / 0,11 / 0,32 | 0,05 / 0,16 / 0,45 |
| Europa occidental | 0,08 / 0,25 / 0,64 | 0,12 / 0,36 / 0,89 |
| Norteamérica | 0,12 / 0,38 / 0,95 | 0,19 / 0,55 / 1,32 |
| Google Play, global | n/d | 0,04 / 0,16 / 0,58 |
| Google Play, LatAm | n/d | 0,02 / 0,06 / 0,19 |
| Google Play, Europa occidental | n/d | 0,07 / 0,23 / 0,70 |
| Google Play, Norteamérica | n/d | 0,08 / 0,29 / 0,87 |
| App Store, global | n/d | 0,15 / 0,42 / 1,07 |

- **Día 30:** no se publica. Estimación propia: unas 1,2 veces el día 14, porque el día 60 es unas 1,5 veces el día 14.
- **Día 365:** RevenueCat no publica ingreso por instalación anual. El dato más cercano es el valor por pagador a 1 año (mediana): 21,62 USD en Google Play, 13,37 en Google Play LatAm, 27,08 en Google Play Europa occidental, 10,69 en precio bajo y 21,01 en Media & Entertainment. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)
- **Adapty, ingreso por instalación a 12 meses:** 0,59 USD en Entertainment y 0,84 USD de mediana general, sobre una base con el 85 % de ingresos en iOS. ([Adapty](https://adapty.io/blog/how-much-does-a-subscription-app-make/), 22-mar-2026)
- **Edición 2025:** día 60 global de 0,38 USD en App Store y 0,14 en Google Play; valor por pagador a 1 año en precio bajo, 8,08 USD. ([RevenueCat 2025](https://www.revenuecat.com/state-of-subscription-apps-2025/), marzo 2025)

## 6. Probabilidad de alcanzar ingresos

- **Ingreso mensual al año del lanzamiento (mediana / P75 / P90):** 72 / 429 / 2.574 USD; en Media & Entertainment, 87 / 517 / 3.126 USD. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)
- **Apps nuevas que llegan a 1.000 USD al mes en 2 años:** 17,3 % (19,9 % en Media & Entertainment). A 2.500 USD llega el 10,7 % y a 10.000 USD el 4,6 %. Las que llegan a 1.000 tardan 58 días de mediana. ([RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)
- **Adapty:** el 57,7 % de las apps nuevas ingresa menos de 1.000 USD en su primer año y el 10 % superior se queda el 94,5 % de los ingresos. ([Adapty](https://adapty.io/blog/mobile-app-monetization-2026/), 12-mar-2026)
- **Competencia:** en enero de 2026 se lanzaron unas 14.700 apps de suscripción, unas 3.300 en Android. Las lanzadas desde 2025 suman el 3 % de los ingresos. (datos de Appfigures en [RevenueCat 2026](https://www.revenuecat.com/state-of-subscription-apps-2026/), marzo 2026)
- **Mercado global 2025:** 167.000 millones USD en compras dentro de apps (+10,6 %) y casi 150.000 millones de descargas (+0,8 %). ([Sensor Tower](https://sensortower.com/blog/state-of-mobile-2026), enero 2026)

## 7. Anuncios en Android

Categoría Entertainment, septiembre de 2024, que es el último periodo que ofrece la herramienta. eCPM en USD. Fuente: [Appodeal Benchmarks](https://appodeal.com/benchmarks/), consultado el 17-nov-2026.

| País | Intersticial eCPM | Intersticial imp./DAU | Recompensado eCPM | Recompensado imp./DAU | Banner eCPM | Banner imp./DAU |
|---|---|---|---|---|---|---|
| España | 3,18 | 1,52 | 4,67 | 0,67 | 0,13 | 8,57 |
| México | 2,58 | 1,12 | 2,28 | 0,80 | 0,13 | 4,27 |
| Argentina | 0,90 | 1,15 | 0,97 | 0,51 | 0,04 | 6,74 |
| Colombia | 1,20 | 1,07 | 1,23 | 0,51 | 0,04 | 4,71 |
| Brasil | 2,59 | 1,12 | 2,56 | 0,63 | 0,20 | 3,27 |
| EE. UU. | 11,95 | 1,22 | 17,24 | 0,42 | 0,78 | 8,51 |

Ingreso por cada 1.000 usuarios diarios (estimación propia: eCPM × impresiones por DAU):

| País | Solo intersticial, USD/día | Solo intersticial, USD/mes | Con recompensado, USD/día |
|---|---|---|---|
| España | 4,83 | 145 | 7,96 |
| México | 2,89 | 87 | 4,71 |
| Argentina | 1,04 | 31 | 1,53 |
| Colombia | 1,28 | 39 | 1,91 |
| Brasil | 2,90 | 87 | 4,51 |
| EE. UU. | 14,58 | 437 | 21,82 |

- **Límites del dato:** Appodeal es una mediación con más de 80 redes, no solo AdMob, y el periodo es 2024. AdMob no publica eCPM por país; el nivel de 2026 no está verificado.
- **Contraste regional, 4.º trimestre de 2024 (aproximado, leído de gráficos):** intersticial Android de unos 1,3 USD en LatAm, 3,6 en Europa y 9,6 en Norteamérica. ([Playio](https://blog.playio.co/mobile-game-ecpm-benchmarks-2026), 5-oct-2026)
- **Rangos de MonetizeMore (estimaciones suyas):** intersticial Android 1,50 a 5,00 USD; CPM en Brasil 0,80 a 2,20 USD. ([MonetizeMore](https://www.monetizemore.com/blog/how-much-ad-revenue-can-apps-generate/), 20-ene-2026)
- **Peso de AdMob:** 22 % de los ingresos publicitarios en Android en el 3.er trimestre de 2026, frente a 29 % de AppLovin. Brasil pesa el 5 % y México el 4 %. ([Tenjin](https://tenjin.com/blog/ad-mon-gaming-2026/), actualizado el 21-oct-2026)

## 8. Comisión de Google Play e IVA

- **Espacio Económico Europeo (incluida España), Reino Unido y EE. UU.:** desde el 30-jun-2026 las suscripciones pagan 10 % de servicio más 5 % de facturación si se usa Google Play Billing. Total: 15 %. ([Ayuda de Play Console](https://support.google.com/googleplay/android-developer/answer/112622?hl=en), consultado el 17-nov-2026; [blog de Android](https://android-developers.googleblog.com/2026/03/a-new-era-for-choice-and-openness.html), 4-mar-2026)
- **Resto de mercados, incluida toda LatAm:** 15 % para suscripciones, hasta el despliegue global previsto para el 30-sep-2027. (mismas fuentes)
- **Precio en España:** incluye IVA, y Google lo calcula, cobra e ingresa para compradores de la UE. Si el desarrollador está en la UE fuera de Irlanda, Google no le repercute IVA local. ([Ayuda de Play Console, impuestos](https://support.google.com/googleplay/android-developer/answer/138000?hl=en), consultado el 17-nov-2026)
- **Tipo general en España:** 21 %. ([Comisión Europea](https://europa.eu/youreurope/business/taxation/vat/vat-rules-rates/index_en.htm), revisado el 13-jul-2026)
- **Cálculo del neto:** 0,99 ÷ 1,21 × 0,85 = 0,70 EUR. Google solo dice de forma expresa que la comisión se calcula sobre el precio sin IVA en los países donde el desarrollador liquida el impuesto. Para la UE lo apoyan fuentes de terceros ([documentación de RevenueCat](https://www.revenuecat.com/docs/dashboard-and-metrics/taxes-and-commissions), sin fecha), no un texto oficial.

Neto por suscriptor y mes (estimación propia; cambio de [open.er-api.com](https://open.er-api.com/v6/latest/EUR), 17-nov-2026). Tratamiento fiscal según la [ayuda de Play Console](https://support.google.com/googleplay/android-developer/answer/138000?hl=en).

| Mercado | Precio | Tratamiento fiscal | Neto aprox. |
|---|---|---|---|
| España | 0,99 EUR | Google liquida el IVA del 21 % | 0,70 EUR |
| EE. UU. | 0,99 USD | El impuesto de ventas se añade aparte | 0,76 EUR |
| México | 19 MXN | Google liquida el IVA del 16 % | 0,68 EUR |
| Colombia | 3.000 COP | Google no lo liquida; el IVA del 19 % es obligación del desarrollador | 0,59 EUR tras IVA |
| Argentina | 1.400 ARS | No lo cubre la página de Google | 0,58 a 0,70 EUR |
| Brasil | 4,99 BRL | Retención de IRRF y CIDE sobre el pago | 0,48 a 0,67 EUR |

- **Colombia y Perú:** la página de Google dice que el desarrollador de fuera sigue siendo responsable de cobrar, ingresar y declarar el IVA. Conviene revisarlo con un asesor.
- **Brasil:** Google confirma la retención pero no publica el porcentaje. ([Ayuda de Play Console, retenciones](https://support.google.com/googleplay/android-developer/answer/9384608?hl=en), consultado el 17-nov-2026) El "hasta 25 % del precio" viene de un aviso de marzo de 2025 reproducido por un tercero, que solo vi en un resultado de búsqueda.

## 9. Cálculos derivados (estimación propia)

Combinan medianas de distribuciones distintas, así que son órdenes de magnitud. No incluyen el coste de la IA, y los anuncios solo los ven los usuarios gratuitos.

- **Solo suscripción:** 500 EUR netos al mes son unos 715 suscriptores activos a 0,70 EUR.
- **Altas necesarias:** unas 146 al mes en régimen estable, con 4,9 pagos de vida media.
- **Instalaciones necesarias al mes:** unas 22.000 con la conversión mediana de Media & Entertainment en Google Play (0,66 %), o unas 7.200 con la del cuartil superior (2,03 %).
- **Ingreso de suscripción por instalación:** unos 0,023 EUR con conversión mediana y 0,070 EUR con la del cuartil superior.
- **Solo intersticiales:** 500 EUR al mes exigen unos 3.800 usuarios diarios en España, 6.300 en México o Brasil, 14.300 en Colombia, 17.700 en Argentina o 1.260 en EE. UU.

## 10. Lo que no pude verificar

- **Precio de 0,99:** ningún informe publica conversión o retención para ese precio.
- **Sensor Tower, Appfigures y data.ai:** el resumen público de Sensor Tower no trae cifras de LatAm ni de España. De Appfigures solo hay los lanzamientos citados por RevenueCat. No encontré informe público de data.ai de 2025 o 2026.
- **Impresiones por usuario diario:** no hay otra referencia neutral que la de Appodeal.
- **Coste por instalación:** quedaba fuera de este encargo. La única cifra que leí es que en Android "rara vez supera 3 a 3,50 USD", sin desglose por país. ([Mapendo](https://mapendo.co/blog/mobile-apps-cpi-2026-the-ultimate-report-for-app-marketers), 1.er semestre de 2026)
- **Método:** no inicié sesión en ningún sitio. En Appodeal rechacé las cookies y usé su filtro público.
