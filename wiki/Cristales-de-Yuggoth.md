# Cristales de Yuggoth *(v0.22.0)*

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
- El Poder de la cuadrícula es la suma de sus cristales, **+10 % por cada Lustre**.
- La producción se multiplica por **x(1 + Poder)**.

## Pulir

- Con un cristal de **grado 5** o más, el botón **PULIR** vacía la cuadrícula y da **Lustre** = `⌊√(Poder sin Lustre / 10)⌋`.
  - Por ejemplo, un grado 6 solo (Poder 243) da 4 de Lustre; un grado 8 (2.187) da 14.
- El Lustre no se gasta y vale para siempre:
  - **+10 % de Poder** por Lustre;
  - **la Mina va un 5 % más rápido** por Lustre: `10 / (1 + 0,05 × Lustre)` segundos por cristal.
- Pulir conviene cuando el Lustre que da mejora más de lo que se pierde al vaciar la cuadrícula.

*Refinar*, el segundo prestigio de los Cristales, y su **árbol de refinado** llegan en la v0.22.1.

## Logros

Los Cristales traen **13 logros**:
- conseguir un cristal de grado 5, 10 y 15;
- hacer 100 y 1.000 fusiones;
- Pulir 1 y 10 veces;
- ganar 100 de Lustre;
- fusionar con el pacto de Abhoth o con el de Daoloth;
- fusionar en la Senda Arcana o en la de la Mente;
- uno secreto.
