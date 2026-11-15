# Expediciones y Reliquias *(v0.21.3)*

Es el segundo minijuego del **Despertar**, inspirado en el minijuego de ataque de Revolution Idle. Se abre con el **primer Despertar**, en la pestaña **Expedición** de la capa Despertar.

## La Expedición

- Un **círculo** golpea cada segundo al horror del nivel en curso. Cada horror es una criatura del Bestiario y se ve en el círculo.
- **Daño por segundo** = `(1 + log10(1 + producción por segundo))² × bono`. Por ejemplo, con 1e30 por segundo son 961.
- **Vida** del horror de nivel n = `100 × 1,25^n`. Crece más rápido que el daño, así que el nivel se frena solo.
- El botón **GOLPEAR** hace una décima del daño por segundo en el acto.
- Cada horror vencido **sube el nivel**, que **no tiene tope**. El daño que sobra pasa al siguiente.
- La Expedición sigue golpeando **sin conexión**.

## Oro

Al **Despertar**, la Expedición paga **⌊nivel^1,3⌋ de Oro** (nivel 20: 49; nivel 100: 398) y vuelve al nivel 0. El **mejor nivel** queda guardado y abre las Reliquias.

## Reliquias

Se compran con Oro y valen para siempre:

| Reliquia | Se abre en el nivel | Efecto por nivel | Máximo | Costo |
|---|---|---|---|---|
| El Trapezoedro Resplandeciente | 10 | Producción x5 | 10 | 10 · 2ⁿ |
| La Corona de Yig | 25 | Éter ganado +50 % | 10 | 25 · 2ⁿ |
| El Espejo de Nitocris | 50 | Daño de Expedición x2 y Visiones +10 % | 5 | 50 · 2,5ⁿ |

## Logros

Las Expediciones traen **14 logros**:
- vencer 10, 100 y 1.000 horrores;
- llegar a los niveles 25, 50 y 100;
- ganar 100 y 1.000 de Oro;
- tener las tres Reliquias;
- vencer horrores con el pacto de Tulzscha o con el de Cthulhu;
- vencer horrores en la Senda de la Mano o en la del Vacío;
- uno secreto.
