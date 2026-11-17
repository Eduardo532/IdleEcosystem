# Los Cuatro Elementales *(v0.23.0)*

Es el cuarto minijuego del **Despertar** y cierra la etapa 8 del Roadmap. Se abre con el **tercer Despertar**, en la pestaña **Elementales** de la capa Despertar.

## Invocar

- Hay cuatro Elementales: **Cthugha** (fuego), **Ithaqua** (viento), **Cthulhu** (agua) y **Nyarlathotep** (tierra).
- Se invoca **uno solo a la vez**. Tocar una tarjeta muestra su árbol, y el botón **INVOCAR** lo llama. **Despedir** lo deja sin ninguno.
- Cambiar de Elemental es gratis y no se pierde nada: el Favor y los nodos de cada uno se guardan.

## Favor

- El Elemental invocado junta **Favor** cada segundo, también sin conexión:
  `Favor por segundo = (1 + log10(1 + producción por segundo)) / 300`.
  Con 1e100 por segundo son unos 20 por minuto.
- Cada Elemental tiene su propio Favor, que solo crece mientras está invocado, y solo compra nodos de su árbol.
- Nada de esto se pierde al Ascender, al Trascender ni al Despertar.

## Los árboles

Cada árbol tiene cuatro nodos. El primero no pide nada; los demás piden un nivel del indicado. **Solo el árbol del Elemental invocado hace efecto**: los otros quedan dormidos.

### Cthugha, el fuego

| Nodo | Pide | Efecto por nivel | Máximo | Costo |
|---|---|---|---|---|
| Brasa de Fomalhaut | — | Producción x5 | 10 | 2 · 2ⁿ |
| Pira Viviente | Brasa | Poder de clic x10 | 5 | 3 · 2ⁿ |
| Llamarada | Brasa | Daño de Expedición x3 | 5 | 4 · 2,5ⁿ |
| Fuego Estelar | Llamarada | Producción ^1,02 | 3 | 20 · 3ⁿ |

### Ithaqua, el viento

| Nodo | Pide | Efecto por nivel | Máximo | Costo |
|---|---|---|---|---|
| Viento Helado | — | Ganancias sin conexión x3 | 10 | 2 · 2ⁿ |
| Caminante del Viento | Viento Helado | La Mina de Cristales va x1,5 más rápido | 5 | 3 · 2ⁿ |
| Torbellino Estelar | Viento Helado | Polvo Estelar x2 | 5 | 4 · 2,5ⁿ |
| Norte Blanco | Torbellino | Favor x1,5 | 3 | 20 · 3ⁿ |

### Cthulhu, el agua

| Nodo | Pide | Efecto por nivel | Máximo | Costo |
|---|---|---|---|---|
| Marea de R'lyeh | — | Cordura ganada x5 | 10 | 2 · 2ⁿ |
| Sueño Hundido | Marea | Puntos de Sueño x2 | 5 | 3 · 2ⁿ |
| Fosa Abisal | Marea | Lustre al Pulir +50 % | 5 | 4 · 2,5ⁿ |
| Las Torres de R'lyeh | Fosa | Visiones ganadas +20 % | 3 | 20 · 3ⁿ |

### Nyarlathotep, la tierra

| Nodo | Pide | Efecto por nivel | Máximo | Costo |
|---|---|---|---|---|
| Piedra Negra | — | Éter ganado x2 | 10 | 2 · 2ⁿ |
| Oro de Khem | Piedra Negra | Oro de Expedición +25 % | 5 | 3 · 2ⁿ |
| El Hombre Negro | Piedra Negra | Esencia de los Mi-Go al Refinar +25 % | 5 | 4 · 2,5ⁿ |
| El Faraón Negro | El Hombre Negro | Éter x(1 + 0,5 × Despertares) | 3 | 20 · 3ⁿ |

## Cuál invocar

Cada Elemental ayuda a una parte distinta del juego:
- Cthugha conviene para la producción y la Expedición.
- Ithaqua conviene para jugar sin conexión, la Mina y las Estrellas.
- Cthulhu conviene para Ascender, el Sueño, los Cristales y las Visiones.
- Nyarlathotep conviene para Trascender y el Despertar.

## Logros

Los Elementales traen **15 logros**:
- ganar 100, 1.000 y 10.000 de Favor;
- tener 10 y 50 niveles en los árboles;
- invocar 10 veces;
- comprar el último nodo de cada Elemental (cuatro logros);
- invocar con el pacto de Ghroth o con el de Shub-Niggurath;
- invocar en la Senda del Vacío o en la del Sueño;
- uno secreto.
