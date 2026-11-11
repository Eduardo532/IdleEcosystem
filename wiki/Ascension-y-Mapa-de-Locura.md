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

Como es una raíz cuadrada, **duplicar la Cordura exige 4 veces más Conocimiento**. La confirmación de Ascender *(v0.14)* muestra lo generado en esta vida, cuándo llega el siguiente punto y cuánto falta para duplicar. El panel de Cordura también indica cuándo se duplica.

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

La confirmación de Ascender muestra la lista: ✓ los que ya tienes y ★ el que desbloquea la Ascensión que estás por hacer.

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

Se abre desde la pestaña **Cordura** (capa Ascensión) → **Mapa de Locura** *(v0.19.0)*. La tienda de Cordura: una constelación de **21 nodos** (18 nodos más las 3 Sendas). Cada nodo se sube de nivel varias veces y cada nivel cuesta más que el anterior (`costo base × crecimiento^nivel`). Un nodo solo se puede comprar si su nodo anterior tiene al menos un nivel.

> Gastar Cordura en el Mapa **la descuenta de tu Cordura Residual**, así que también baja su +10 % por punto. Vale la pena cuando el nodo da más que lo que pierdes.

**Efecto neto** *(v0.15)*: al elegir un nodo, el panel muestra cuánto sube el nodo, cuánto baja el bono de tu Cordura y, en los nodos de producción global, el resultado neto. Si el neto es menor que x1, ese nivel baja tu producción.

| Nodo | Efecto | Por nivel | Costo base | Crecimiento | Requiere |
|---|---|---|---|---|---|
| El Despertar | Producción global | +10 % | 1 | x1.5 | — |
| Dedos Inquietos | Clic | +50 % | 5 | x1.5 | El Despertar |
| Furia Táctil | Clic | +100 % | 25 | x1.6 | Dedos Inquietos |
| Tormenta Psiónica | Clic | +200 % | 150 | x1.8 | Furia Táctil |
| La Mano de Dios | Clic | +500 % | 1000 | x2.5 | Tormenta Psiónica |
| Engranajes Mentales | Edificios 1 a 6 | +20 % | 10 | x1.5 | El Despertar |
| Sincronía | Todos los edificios | +15 % | 50 | x1.6 | Engranajes Mentales |
| Sueño Lúcido | Ganancia sin conexión | +50 % | 200 | x1.8 | Sincronía |
| Eternidad en un Segundo | Ganancia sin conexión | +200 % | 1500 | x2.5 | Sueño Lúcido |
| Concentración | Producción global | +5 % | 15 | x1.4 | El Despertar |
| Lógica Alienígena | Producción global | +25 % | 80 | x1.7 | Concentración |
| Iluminación Oscura | Producción global | +50 % | 400 | x2.0 | Lógica Alienígena |
| Omnisciencia | Producción global | +150 % | 2500 | x3.0 | Iluminación Oscura |
| Llamada del Vacío | Cordura ganada al Ascender | +10 % | 100 | x2.0 | Lógica Alienígena |
| Abismo | Cordura ganada al Ascender | +20 % | 500 | x2.5 | Llamada del Vacío |
| Singularidad | Cordura ganada al Ascender | +50 % | 5000 | x4.0 | Abismo |
| Ascensión Cósmica | Producción global | +100 % | 5000 | x5.0 | Omnisciencia |

Dentro de un nodo, los niveles se suman (3 niveles de +10 % = +30 %). Entre nodos distintos, los bonos se multiplican.

### Legado del Culto *(v0.17.4)*

Nodo nuevo junto a El Despertar: **cada vida empieza con 5 Notas Desordenadas por nivel**. Cuesta 3 de Cordura y se duplica por nivel. Sirve para arrancar más rápido y para las vidas en las que el clic no produce.

### Sendas *(v0.16.2)*

Al final de las ramas de clic, edificios y sueño hay tres nodos con anillo dorado: las **Sendas**. Son **excluyentes**: en cuanto una tiene un nivel, las otras dos quedan **selladas** (con una cruz roja) y no se pueden comprar.

| Senda | Efecto | Por nivel | Costo base | Crecimiento | Requiere |
|---|---|---|---|---|---|
| Senda de la Mano | Clic | +300 % | 2000 | x2.5 | La Mano de Dios |
| Senda del Engranaje | Todos los edificios | +100 % | 2000 | x2.5 | Sincronía |
| Senda del Sueño | Ganancia sin conexión | +300 % | 2000 | x2.5 | Eternidad en un Segundo |

**Cambiar de Senda:** solo al Ascender. La confirmación muestra la casilla "Abandonar la Senda…": si la marcas, la Senda vuelve a nivel 0 y **recuperas toda la Cordura** que pusiste en ella (por ejemplo, 2 niveles = 2000 + 5000 = 7000). Después puedes elegir otra.

## Progreso sin conexión

Si cierras el juego más de 10 segundos, al volver recibes:

`producción base por segundo × segundos fuera × bono sin conexión del Mapa`

La producción base no incluye los potenciadores temporales (runas, rituales, eventos), que no se extienden al tiempo fuera.
