# Cristales de Yuggoth *(v0.22.0 – v0.22.1)*

Es el tercer minijuego del **Despertar**, inspirado en los minerales de Revolution Idle. Se abre con el **segundo Despertar**, en la pestaña **Cristales** de la capa Despertar.

## La Mina y la cuadrícula

- La Mina deja un **cristal de grado 1** en una casilla vacía al azar de una cuadrícula de **4 × 4**, cada **10 segundos** (más rápido con Lustre).
- Si la cuadrícula está llena, la Mina espera: hay que fusionar para hacer lugar.
- La Mina sigue trabajando **sin conexión**, hasta llenar la cuadrícula.
- Nada de esto se pierde al Ascender, al Trascender ni al Despertar.

## Fusionar

- **A mano:** toca un cristal y después otro **del mismo grado**. El segundo sube un grado y el primero desaparece. Mientras hay uno elegido, se marcan los que hacen pareja con él.
- **Fusionar todo:** fusiona todas las parejas, de los grados bajos a los altos, hasta que no quede ninguna.

## Poder

- El **Poder** de un cristal de grado g es `3^(g − 1)`: un grado 5 vale 81 y un grado 10, 19.683.
- El Poder de la cuadrícula es la suma de sus cristales, **+10 % por cada Lustre** (y x2 por nivel de Facetas Infinitas).
- La producción se multiplica por **x(1 + Poder)** (elevado a ^1,05 por nivel de Núcleo del Planeta).

## Pulir

- Con un cristal de **grado 5** o más, el botón **PULIR** vacía la cuadrícula y da **Lustre** = `⌊√(Poder sin Lustre / 10)⌋`.
  - Por ejemplo, un grado 6 solo (Poder 243) da 4 de Lustre; un grado 8 (2.187) da 14.
- El Lustre no se gasta y vale para siempre:
  - **+10 % de Poder** por Lustre;
  - **la Mina va un 5 % más rápido** por Lustre: `10 / (1 + 0,05 × Lustre)` segundos por cristal.
- Pulir conviene cuando el Lustre que da mejora más de lo que se pierde al vaciar la cuadrícula.

## Refinar *(v0.22.1)*

- Con **25 de Lustre** o más, el botón **REFINAR** (pide confirmación) vuelve el **Lustre a 0** y vacía la cuadrícula a cambio de **Esencia de los Mi-Go** = `⌊√(Lustre / 5)⌋`.
  - 25 de Lustre dan 2 de Esencia; 125 dan 5; 500 dan 10.
- La Esencia compra el **árbol de refinado**, que vale para siempre y se puede **reiniciar gratis** (devuelve toda la Esencia).

## El árbol de refinado

Cada nodo pide tener al menos un nivel del anterior.

| Nodo | Pide | Efecto por nivel | Máximo | Costo |
|---|---|---|---|---|
| Taladros de los Mi-Go | — | La Mina va x1,5 más rápido | 5 | 1 · 2ⁿ |
| Facetas Infinitas | Taladros | Poder x2 | 10 | 1 · 2ⁿ |
| Semilla de Cuarzo | Taladros | Los cristales de la Mina nacen un grado más alto | 3 | 3 · 3ⁿ |
| Pulidor Fungoide | Facetas | Lustre al Pulir +50 % | 5 | 2 · 2ⁿ |
| Lente de Yuggoth | Facetas | Éter x(1 + 0,1 × log10(1 + Poder)) | 5 | 4 · 2,5ⁿ |
| Veta Profunda | Semilla | La Mina deja un cristal más cada vez | 2 | 5 · 3ⁿ |
| Eco del Cristal | Pulidor | Pulir conserva el cristal de mayor grado, dos grados más bajo | 1 | 8 |
| Núcleo del Planeta | Lente | El multiplicador de los cristales se eleva a ^1,05 | 3 | 15 · 3ⁿ |

## Logros

Los Cristales traen **13 logros**:
- conseguir un cristal de grado 5, 10 y 15;
- hacer 100 y 1.000 fusiones;
- Pulir 1 y 10 veces;
- ganar 100 de Lustre;
- fusionar con el pacto de Abhoth o con el de Daoloth;
- fusionar en la Senda Arcana o en la de la Mente;
- uno secreto.

Refinar trae **12 logros** más *(v0.22.1)*:
- Refinar 1, 5 y 25 veces;
- ganar 10 y 100 de Esencia;
- tener 4 y 8 nodos del árbol de refinado;
- Refinar con el pacto de Yeb o con el de Nug;
- Refinar en la Senda del Engranaje o en la de la Mano;
- uno secreto.
