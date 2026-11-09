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

### Qué se pierde y qué se conserva

| Se pierde | Se conserva |
|---|---|
| Conocimiento | Cordura Residual |
| Edificios | Nodos del Mapa de Locura |
| Mejoras del Grimorio (incluidas las sinergias) | Logros (y sus conjuntos) |
| | Ecos |
| | Estadísticas |

### Qué da la Cordura

Cada punto de Cordura Residual que tienes da **+10 % a toda la producción**: con 25 puntos, x3,5.

## Mapa de Locura

La tienda de Cordura: una constelación de **17 nodos**. Cada nodo se sube de nivel varias veces y cada nivel cuesta más que el anterior (`costo base × crecimiento^nivel`). Un nodo solo se puede comprar si su nodo anterior tiene al menos un nivel.

> Gastar Cordura en el Mapa **la descuenta de tu Cordura Residual**, así que también baja su +10 % por punto. Vale la pena cuando el nodo da más que lo que pierdes.

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

## Progreso sin conexión

Si cierras el juego más de 10 segundos, al volver recibes:

`producción base por segundo × segundos fuera × bono sin conexión del Mapa`

La producción base no incluye los potenciadores temporales (runas, rituales, eventos), que no se extienden al tiempo fuera.
