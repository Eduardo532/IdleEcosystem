# Ascensión y Mapa de Locura

## Ascender

Ascender **reinicia la partida** a cambio de **Cordura Residual**, una moneda permanente.

- **Requisito:** haber generado al menos **1M de Conocimiento en esta vida** (desde la última Ascensión).
- **Cordura que ganas:** `⌊ √(Conocimiento generado en esta vida ÷ 1.000.000) ⌋`, multiplicada por los bonos de Cordura del Mapa de Locura.

| Generado en esta vida | Cordura base |
|---|---|
| 1M | 1 |
| 4M | 2 |
| 9M | 3 |
| 100M | 10 |
| 1B | 31 |

### La confirmación de Ascender *(v0.19.1)*

Muestra arriba la Cordura que ganas y, debajo, **una fila por cada elección de la próxima vida**: **Pacto**, **Prueba**, **Mutación** y **Senda** (cada una aparece solo cuando existe). Tocar una fila abre su selector, con todas las opciones a la vista en varias filas *(v0.19.3)*; al pulsar **LISTO** vuelves al resumen. Lo demás (qué se pierde y qué se conserva, el ritmo de la Cordura y los hitos) está plegado en **Qué se pierde, qué se conserva e hitos**.

Como es una raíz cuadrada, **duplicar la Cordura exige 4 veces más Conocimiento**. La confirmación de Ascender *(v0.14)* muestra cuándo se duplicaría la Cordura; en **Qué se pierde, qué se conserva e hitos** está lo generado en esta vida y cuándo llega el siguiente punto. El panel de Cordura también indica cuándo se duplica.

Al Ascender también eliges el **pacto** de tu próxima vida (ver [Panteón](Panteon)) y, si recorres una **Senda**, puedes abandonarla (ver [Sendas](#sendas-v0162)). Desde la tercera Ascensión también puedes elegir una **Prueba de Locura** (ver [Pruebas de Locura](Pruebas-de-Locura)).

### Qué se pierde y qué se conserva

| Se pierde | Se conserva |
|---|---|
| Conocimiento | Cordura Residual |
| Edificios | Nodos del Mapa de Locura |
| Mejoras del Grimorio (incluidas las sinergias) | Logros (y sus conjuntos) |
| | Ecos |
| | Estadísticas |

### Hitos de Ascensión *(v0.15)*

Según cuántas veces hayas Ascendido en toda la partida, cada vida nueva empieza con algo, conserva algo o suma una automatización:

| Ascensiones | Hito |
|---|---|
| 1 | Empiezas cada vida con 10 Sinapsis |
| 3 | Conservas las mejoras de clic del Grimorio |
| 5 | Autocomprador de edificios |
| 10 | Autocomprador del Grimorio y Ascensión automática |
| 25 | Conservas las sinergias compradas |
| 50 | Un ritual elegido se activa solo |

La confirmación de Ascender muestra la lista en **Qué se pierde, qué se conserva e hitos**: ✓ los que ya tienes y ★ el que desbloquea la Ascensión que estás por hacer.

**Autocompradores:** se encienden y apagan con el botón **AUTO** (sobre la lista de edificios, junto a "Comprar todas" para el Grimorio y en cada ritual para el ritual automático).

- Por defecto nunca gastan más de la **mitad** de tu Conocimiento (se cambia en el panel).
- El de edificios compra **una unidad de cada edificio por segundo**, del más caro al más barato.
- El del Grimorio compra las mejoras que entran en ese presupuesto, de la más barata a la más cara.
- El ritual automático se activa cuando está listo y te alcanzan los Ecos.

**Panel de automatización** *(v0.15.1)*: el engranaje al lado del botón AUTO (o la tarjeta **Automatización** de la pestaña Cordura) abre un panel donde eliges el **presupuesto** (10 %, 25 %, 50 %, 75 % o 100 % del Conocimiento), **qué edificios** compra el autocomprador, el autocomprador del Grimorio, el ritual automático y la Ascensión automática. Las secciones bloqueadas dicen con cuántas Ascensiones se abren. La configuración se guarda en el teléfono (no viaja en el código de exportación).

**Ascensión automática** *(desde 10 Ascensiones, apagada por defecto)*: el juego Asciende solo cuando la Cordura que ganarías **iguala o supera** la que ya tienes, es decir, cuando la duplica. Un aviso muestra cuánta ganaste.

### Qué da la Cordura

Cada punto de Cordura Residual que tienes da **+10 % a toda la producción**: con 25 puntos, x3,5.

## Mapa de Locura

Se abre desde la pestaña **Cordura** (capa Ascensión) → **Mapa de Locura**. Es la tienda de Cordura: desde la v0.19.2, **un cielo con siete constelaciones** alrededor de **El Despertar**, con **35 estrellas** (28 nodos y 7 Sendas). Cada nodo se sube de nivel varias veces y cada nivel cuesta más que el anterior (`costo base × crecimiento^nivel`); algunos tienen un nivel máximo.

> Gastar Cordura en el Mapa **la descuenta de tu Cordura Residual**, así que también baja su +10 % por punto. Vale la pena cuando el nodo da más que lo que pierdes.

### Cómo se usa *(v0.19.2)*

- **Cielo:** pellizca para acercar y arrastra para moverte. Con zoom aparecen los nombres de las estrellas. Toca una estrella para ver su ficha.
- **Constelaciones reales** *(v0.19.3)*: cada sección es una figura del cielo, con sus estrellas de adorno y sus líneas. La primera estrella que se compra es la más cercana a El Despertar (de ahí sale su hilo), y la Senda está dentro de la figura, unida a su estrella más cercana. La ficha dice el nombre real de cada estrella (por ejemplo, ★ Rigel).

| Sección | Constelación | Estrellas del Mapa, en orden de compra | Senda |
|---|---|---|---|
| La Mano | Orión | Rigel, Mintaka, Bellatrix, Betelgeuse | Alnilam, en el cinturón |
| El Engranaje | Osa Mayor | Phecda, Megrez, Dubhe | en el centro del cazo |
| El Sueño | Las Pléyades | Atlas, Alcyone, Merope | en el centro del cúmulo |
| La Mente | Casiopea | Segin, Ruchbah, Navi, Schedar, Caph | bajo la W |
| El Vacío | Pez Austral | Epsilon, Fomalhaut, Delta | en el centro del pez |
| El Culto | Tauro | Elnath, Ain, Gamma, Aldebarán | dentro de la V de las Híades |
| Lo Arcano | Perseo | Eta, Gamma, Mirfak, Algol, Rho | Delta Persei |
- **Lista:** las mismas estrellas agrupadas por constelación, con su efecto y su costo.
- **Constelaciones:** los botones de arriba acercan el cielo a una constelación (o filtran la lista) y muestran cuántas estrellas tiene encendidas.
- **Colores:** gris, todavía no se alcanza; contorno del color de la constelación, se puede comprar (brilla más si te alcanza la Cordura); relleno, comprada; dorado, al máximo o Senda recorrida; rojo con cruz, Senda sellada.
- **Ficha:** qué da ahora, qué daría el siguiente nivel, el efecto neto de gastar Cordura y el botón **MEJORAR**.

Dentro de cada constelación las estrellas se compran **en cadena**: la primera pide El Despertar y cada una pide la anterior. Dentro de un nodo, los niveles se suman (3 niveles de +10 % = +30 %); entre nodos distintos, los bonos se multiplican.

### Las constelaciones

| Constelación | Nodo | Efecto por nivel | Costo base | Crecimiento | Máx. |
|---|---|---|---|---|---|
| (centro) | El Despertar | Producción +10 % | 1 | x1.5 | — |
| **La Mano** | Dedos Inquietos | Clic +50 % | 5 | x1.5 | — |
| | Furia Táctil | Clic +100 % | 25 | x1.6 | — |
| | Tormenta Psiónica | Clic +200 % | 150 | x1.8 | — |
| | La Mano de Dios | Clic +500 % | 1000 | x2.5 | — |
| **El Engranaje** | Engranajes Mentales | Seis primeros edificios +30 % *(antes 20 %)* | 10 | x1.5 | — |
| | Sincronía | Todos los edificios +25 % *(antes 15 %)* | 50 | x1.6 | — |
| | Planos Imposibles *(nuevo)* | Edificios −3 % de costo | 150 | x1.8 | 10 |
| **El Sueño** | Sueño Lúcido | Sin conexión +50 % | 200 | x1.8 | — |
| | Eternidad en un Segundo | Sin conexión +200 % | 1500 | x2.5 | — |
| | Visiones Nocturnas *(nuevo)* | Eventos +15 % más seguido y +10 % de fuerza | 300 | x2.0 | 5 |
| **La Mente** | Concentración | Producción +10 % *(antes 5 %)* | 15 | x1.4 | — |
| | Lógica Alienígena | Producción +25 % | 80 | x1.7 | — |
| | Iluminación Oscura | Producción +50 % | 400 | x2.0 | — |
| | Omnisciencia | Producción +150 % | 2500 | x3.0 | — |
| | Ascensión Cósmica | Producción +100 % | 5000 | x5.0 | — |
| **El Vacío** | Llamada del Vacío | Cordura +25 % *(antes 10 %)* | 100 | x2.0 | — |
| | Abismo | Cordura +40 % *(antes 20 %)* | 500 | x2.5 | — |
| | Singularidad | Cordura +100 % *(antes 50 %)* | 5000 | x4.0 | — |
| **El Culto** | Legado del Culto | +5 Notas Desordenadas al empezar | 3 | x2.0 | — |
| | Biblioteca Heredada *(nuevo)* | +5 Pergaminos Prohibidos al empezar | 20 | x2.0 | 10 |
| | Reliquias de Familia *(nuevo)* | +3 Artefactos Susurrantes al empezar | 120 | x2.2 | 10 |
| | Iniciados Fieles *(nuevo)* | +2 Sectarios Novatos al empezar | 600 | x2.5 | 10 |
| **Lo Arcano** | Ojos del Vacío *(nuevo)* | Runas +10 % más seguido | 40 | x1.8 | 5 |
| | Liturgia Breve *(nuevo)* | Rituales −5 % de costo y de recarga | 150 | x2.0 | 6 |
| | Resonancia *(nuevo)* | Probabilidad de Eco +10 % | 400 | x2.2 | 5 |
| | Ojo Interior *(nuevo)* | +1 punto de Atención | 1000 | x4.0 | 3 |
| | Calma Antigua *(nuevo)* | La Locura sube 8 % menos | 2500 | x3.0 | 5 |

Desde la v0.19.2, El Sueño y El Vacío ya no cuelgan de otras ramas: empiezan en El Despertar. La Prueba que anula la Cordura y el Mapa también anula los edificios iniciales y los efectos de Lo Arcano.

### Sendas *(v0.16.2; siete desde la v0.19.2)*

Cada constelación tiene en su **centro** una Senda con doble anillo, que se abre cuando **todas sus estrellas tienen al menos un nivel**. Son **excluyentes**: en cuanto una tiene un nivel, las demás quedan **selladas** (con una cruz roja) y no se pueden comprar. Todas cuestan 2000 de Cordura y crecen x2.5.

| Senda | Efecto por nivel |
|---|---|
| Senda de la Mano | Clic +300 % |
| Senda del Engranaje | Todos los edificios +100 % |
| Senda del Sueño | Sin conexión +300 % |
| Senda de la Mente *(nueva)* | Producción +50 % |
| Senda del Vacío *(nueva)* | Cordura ganada +50 % |
| Senda del Culto *(nueva)* | +10 de cada uno de los cinco primeros edificios de producción al empezar |
| Senda Arcana *(nueva)* | Runas +25 % más seguido, Eco +25 % y rituales −10 % de costo |

Si ya recorrías una Senda antes de la v0.19.2, la conservas y puedes seguir subiéndola aunque su constelación no esté completa.

**Cambiar de Senda:** solo al Ascender. En la confirmación, toca la fila **Senda** para ver la casilla "Abandonar la Senda…": si la marcas, la Senda vuelve a nivel 0 y **recuperas toda la Cordura** que pusiste en ella (por ejemplo, 2 niveles = 2000 + 5000 = 7000). Después puedes elegir otra.

## Progreso sin conexión

Si cierras el juego más de 10 segundos, al volver recibes:

`producción base por segundo × segundos fuera × bono sin conexión del Mapa`

La producción base no incluye los potenciadores temporales (runas, rituales, eventos), que no se extienden al tiempo fuera.
