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

Se abre desde la pestaña **Cordura** (capa Ascensión) → **Mapa de Locura**. Es la tienda de Cordura: desde la v0.19.2, **un cielo con siete constelaciones** alrededor de **El Despertar**. Desde la v0.19.4, **cada estrella de cada figura es un nodo**: 54 nodos (contando El Despertar) y 7 Sendas. Cada nodo se sube de nivel varias veces y cada nivel cuesta más que el anterior (`costo base × crecimiento^nivel`); algunos tienen un nivel máximo.

> Gastar Cordura en el Mapa **la descuenta de tu Cordura Residual**, así que también baja su +10 % por punto. Vale la pena cuando el nodo da más que lo que pierdes.

### Cómo se usa *(v0.19.2)*

- **Cielo:** pellizca para acercar y arrastra para moverte. Con zoom aparecen los nombres de las estrellas. Toca una estrella para ver su ficha.
- **Constelaciones reales** *(v0.19.3)*: cada sección es una figura del cielo, con sus estrellas de adorno y sus líneas. La primera estrella que se compra es la más cercana a El Despertar (de ahí sale su hilo), y la Senda está dentro de la figura, unida a su estrella más cercana. La ficha dice el nombre real de cada estrella (por ejemplo, ★ Rigel).

| Sección | Constelación | Primera estrella | Senda |
|---|---|---|---|
| La Mano | Orión | Rigel | en el hueco del centro de la figura |
| El Engranaje | Osa Mayor | Alkaid | en el hueco del centro de la figura |
| El Sueño | Las Pléyades | Pleione | en el hueco del centro de la figura |
| La Mente | Casiopea | Segin | en el hueco del centro de la figura |
| El Vacío | Pez Austral | Epsilon | en el hueco del centro de la figura |
| El Culto | Tauro | Elnath | en el hueco del centro de la figura |
| Lo Arcano | Perseo | Eta | en el hueco del centro de la figura |

Dentro de cada constelación las estrellas se encienden **siguiendo las líneas de la figura**: la primera (la más cercana a El Despertar) pide El Despertar, y cada una de las demás pide una estrella vecina ya encendida. Dentro de un nodo, los niveles se suman (3 niveles de +10 % = +30 %); entre nodos distintos, los bonos se multiplican.

### Las constelaciones

| Constelación | Estrella | Nodo | Efecto por nivel | Costo base | Crecimiento | Máx. |
|---|---|---|---|---|---|---|
| (centro) | — | El Despertar | Aumenta la Producción Global. | 1 | x1.5 | — |
| **La Mano · Orión** | Rigel | Dedos Inquietos | Multiplica el poder de clic. | 5 | x1.5 | — |
|  | Mintaka | Furia Táctil | Clics más potentes. | 25 | x1.6 | — |
|  | Bellatrix | Tormenta Psiónica | Clics devastadores. | 150 | x1.8 | — |
|  | Betelgeuse | La Mano de Dios | Poder de clic definitivo. | 1000 | x2.5 | — |
|  | Alnilam | Cinturón del Cazador | Multiplica el poder de clic. | 60 | x1.6 | — |
|  | Alnitak | Nudillos de Piedra | Clics todavía más fuertes. | 300 | x1.8 | — |
|  | Saiph | Golpe Resonante | Un 5% más de probabilidad de Eco por nivel. | 800 | x2.2 | 5 |
|  | Meissa | Cabeza del Cazador | Clics brutales. | 3000 | x2.6 | — |
| **El Engranaje · Osa Mayor** | Alkaid | Engranajes Mentales | Multiplica los seis primeros edificios. | 10 | x1.5 | — |
|  | Mizar | Sincronía | Multiplica TODOS los edificios. | 50 | x1.6 | — |
|  | Alioth | Planos Imposibles | Los edificios cuestan un 3% menos por nivel. | 150 | x1.8 | 10 |
|  | Megrez | Ruedas Dentadas | Multiplica los edificios 1 a 6. | 80 | x1.7 | — |
|  | Phecda | Engranajes Dobles | Multiplica los edificios 7 a 11. | 400 | x1.9 | — |
|  | Merak | Motor Primordial | Multiplica los edificios 12 a 16. | 2500 | x2.2 | — |
|  | Dubhe | Aleación Impía | Los edificios cuestan un 2% menos por nivel. | 600 | x2.0 | 10 |
| **El Sueño · Las Pléyades** | Pleione | Sueño Lúcido | Multiplica ganancia Offline. | 200 | x1.8 | — |
|  | Atlas | Eternidad en un Segundo | Ganancia Offline masiva. | 1500 | x2.5 | — |
|  | Alcyone | Visiones Nocturnas | Más eventos, y sus potenciadores más fuertes. | 300 | x2.0 | 5 |
|  | Merope | Sueño Compartido | Multiplica la ganancia sin conexión. | 250 | x1.8 | — |
|  | Electra | Pesadilla Útil | Los potenciadores de los eventos, un 15% más fuertes por nivel. | 500 | x2.0 | 5 |
|  | Maia | Siete Hermanas | Los eventos llegan un 10% más seguido por nivel. | 900 | x2.2 | 5 |
|  | Taygeta | Desvelo Infinito | Ganancia sin conexión masiva. | 3000 | x2.5 | — |
| **La Mente · Casiopea** | Segin | Concentración | Multiplicador Global. | 15 | x1.4 | — |
|  | Ruchbah | Lógica Alienígena | Aumento Global considerable. | 80 | x1.7 | — |
|  | Navi | Iluminación Oscura | Potenciador Global avanzado. | 400 | x2.0 | — |
|  | Schedar | Omnisciencia | Multiplicador Global divino. | 2500 | x3.0 | — |
|  | Caph | Ascensión Cósmica | El Multiplicador Supremo. | 5000 | x5.0 | — |
| **El Vacío · Pez Austral** | Epsilon | Llamada del Vacío | Multiplica la Cordura obtenida. | 100 | x2.0 | — |
|  | Fomalhaut | Abismo | Gran bono de Cordura. | 500 | x2.5 | — |
|  | Delta | Singularidad | Multiplicador masivo de Cordura. | 5000 | x4.0 | — |
|  | Gamma | Marea Negra | Un 10% más de Cordura por nivel. | 800 | x2.0 | — |
|  | Beta | Agua sin Luz | Un 15% más de Cordura por nivel. | 2000 | x2.3 | — |
|  | Theta | Boca del Pez | Un 10% más de probabilidad de Eco por nivel. | 3500 | x2.4 | 5 |
|  | Iota | Escamas de Sombra | Un 20% más de Cordura por nivel. | 8000 | x2.6 | — |
|  | Mu | Ojo de Fomalhaut | Un 30% más de Cordura por nivel. | 20000 | x3.0 | — |
| **El Culto · Tauro** | Elnath | Legado del Culto | Cada vida empieza con 5 Notas Desordenadas por nivel. | 3 | x2.0 | — |
|  | Ain | Biblioteca Heredada | Cada vida empieza con 5 Pergaminos Prohibidos por nivel. | 20 | x2.0 | 10 |
|  | Delta | Reliquias de Familia | Cada vida empieza con 3 Artefactos Susurrantes por nivel. | 120 | x2.2 | 10 |
|  | Gamma | Iniciados Fieles | Cada vida empieza con 2 Sectarios Novatos por nivel. | 600 | x2.5 | 10 |
|  | Theta | Diezmo de Sangre | Cada vida empieza con 1 Sacerdote Demente por nivel. | 1500 | x2.5 | 10 |
|  | Aldebaran | Ojo del Toro | Cada vida empieza con 1 Santuario Subterráneo por nivel. | 4000 | x2.7 | 10 |
|  | Zeta | Cuerno Sagrado | Cada vida empieza con 1 Altar de Hueso por nivel. | 10000 | x3.0 | 10 |
|  | Lambda | Ofrendas | Los rituales cuestan un 5% menos por nivel. | 700 | x2.0 | 6 |
|  | Omicron | Rebaño Fiel | Cada vida empieza con 5 Sinapsis Arcanas por nivel. | 1200 | x2.2 | 10 |
| **Lo Arcano · Perseo** | Eta | Ojos del Vacío | Las runas aparecen un 10% más seguido por nivel. | 40 | x1.8 | 5 |
|  | Gamma | Liturgia Breve | Los rituales cuestan y tardan en recargar un 5% menos por nivel. | 150 | x2.0 | 6 |
|  | Mirfak | Resonancia | Un 10% más de probabilidad de Eco por nivel. | 400 | x2.2 | 5 |
|  | Kappa | Ojo Interior | Un punto más de Atención por nivel. | 1000 | x4.0 | 3 |
|  | Algol | Calma Antigua | La Locura sube un 8% menos por nivel. | 2500 | x3.0 | 5 |
|  | Rho | Mirada de Medusa | Las runas aparecen un 8% más seguido por nivel. | 4000 | x2.5 | 5 |
|  | Delta | Rito Prolongado | Los rituales duran un 10% más por nivel. | 600 | x2.0 | 5 |
|  | Epsilon | Eco Persistente | Un 8% más de probabilidad de Eco por nivel. | 1500 | x2.2 | 5 |
|  | Zeta | Llama Errante | Los rituales recargan un 5% más rápido por nivel. | 3500 | x2.4 | 6 |

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
