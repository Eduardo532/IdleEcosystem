# Edificios

Los edificios son la base de la economía. Cada unidad produce Conocimiento por segundo (la Sinapsis, en cambio, potencia el clic).

## Lista

| # | Edificio | Tipo | Costo base | Producción base por unidad |
|---|---|---|---|---|
| 1 | Sinapsis Arcana | Clic | 10 | +1 al clic |
| 2 | Notas Desordenadas | Producción | 15 | 0.5/s |
| 3 | Pergaminos Prohibidos | Producción | 100 | 4/s |
| 4 | Artefacto Susurrante | Producción | 1.1 K | 12/s |
| 5 | Sectario Novato | Producción | 12 K | 45/s |
| 6 | Sacerdote Demente | Producción | 130 K | 150/s |
| 7 | Santuario Subterráneo | Producción | 1.4 M | 600/s |
| 8 | Altar de Hueso | Producción | 20 M | 2.5 K/s |
| 9 | Monolito de Obsidiana | Producción | 330 M | 12 K/s |
| 10 | Templo de R'lyeh | Producción | 5.1 B | 65 K/s |
| 11 | Portal Dimensional | Producción | 75 B | 400 K/s |
| 12 | Criadero de Shoggoths | Producción | 1 T | 2.5 M/s |
| 13 | Fragmento de Sueño | Producción | 14 T | 15 M/s |
| 14 | Alineación Estelar | Producción | 170 T | 100 M/s |
| 15 | Avatar Primigenio | Producción | 2.1 Qa | 850 M/s |
| 16 | Trono del Vacío | Producción | 26 Qa | 10 B/s |

- **Desbloqueo:** la Sinapsis y las Notas están desde el inicio. Cada edificio siguiente aparece cuando tienes al menos uno del anterior.
- **Costo:** cada unidad cuesta un **15 % más** que la anterior (`costo = costo base × 1,15^unidades que tienes`).
- **El clic:** cada clic da `1 + producción total de la Sinapsis`. Por eso las mejoras de clic y de la Sinapsis suben el clic, no la producción por segundo.

## Cantidad de compra

El menú de edificios tiene un selector: **x1, x5, x10, x100, HITO y MAX**.

- **MAX** compra todo lo que alcanza tu Conocimiento.
- **HITO** *(v0.14)* compra justo lo que falta para el **siguiente hito** del edificio.

## Hitos *(v0.14)*

Un hito es una cantidad donde pasa algo:

- los **escalones del Grimorio** (25, 50 y 100), que hacen aparecer una mejora x2 (ver [Grimorio](Grimorio));
- los **logros de cantidad** de ese edificio (por ejemplo, 10, 50, 100, 300 y 600 Notas). Los logros secretos no cuentan como hito.

Cada tarjeta muestra una barra con el siguiente hito y cuántos faltan ("Hito 25 · faltan 6"). Pasado el último hito, cada centena cuenta como uno.

## Qué multiplica a un edificio

La producción de un edificio se multiplica por todo esto (cada factor multiplica a los demás):

- sus mejoras del Grimorio y la escalera (x2 cada una);
- las [sinergias](Grimorio#3-sinergias-v014) que lo potencian;
- su [conjunto de logros](Logros#conjuntos-de-logros-v014) completo (x2);
- los nodos del [Mapa de Locura](Ascension-y-Mapa-de-Locura) que afectan a edificios;
- el multiplicador global: mejoras globales, Cordura Residual, logros y potenciadores temporales de runas, rituales y eventos.
