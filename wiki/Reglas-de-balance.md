# Reglas de balance

Estas son las reglas que mantienen el juego equilibrado. Casi todas tienen un **test unitario** que falla si un valor nuevo se sale de ellas: antes de cambiar un número, revisa la regla que le corresponde.

## Unidad de medida: segundos de producción

Para comparar bonos distintos, se miden en **segundos de producción base equivalentes**:

- un potenciador **xM durante D segundos** aporta `(M − 1) × D` segundos extra;
- un **salto de tiempo** aporta exactamente sus segundos.

Ejemplo: x10 durante 15 s = 9 × 15 = **135 s** de producción.

## Potenciadores temporales: se suman

Runas, rituales y eventos combinan sus multiplicadores **sumando**: `1 + Σ(Mᵢ − 1)`. Los permanentes (mejoras, Cordura, logros, Mapa de Locura) se multiplican.

Así, x10 + x20 activos a la vez dan x29, no x200. *(Código: `BuffMath`.)*

## Runas

Cada nivel tiene un rango fijo de valor:

| Nivel | Runas de producción | Runas de clic (en clics equivalentes) |
|---|---|---|
| 1 | 90 – 180 s | 300 – 600 |
| 2 | 360 – 600 s | 1.200 – 2.400 |
| 3 | 1.200 – 2.400 s | 4.000 – 8.000 |

- Clics equivalentes de una runa de clic = `clics × multiplicador de clic × multiplicador de producción`.
- El autoclic de las runas hace **10 clics/s**.
- *(Código: `RuneEffects`; test: `RuneEffectsTest` recorre todo el catálogo.)*

## Rituales

Un ritual más caro **nunca rinde menos por Eco** que uno más barato:

| Ritual | Costo | Segundos de producción | Por Eco |
|---|---|---|---|
| Cántico del Vacío | 15 | (3 − 1) × 30 = 60 | 4,0 |
| Ofrenda de Conocimiento | 25 | 120 | 4,8 |
| Grieta Temporal | 50 | (20 − 1) × 15 = 285 | 5,7 |
| Vigilia Eterna | 100 | (6 − 1) × 120 = 600 | 6,0 |

*(Código: `Ritual.productionSeconds`.)*

**Convergencia:** mientras está activa, los potenciadores temporales se multiplican: `min(∏ Mᵢ, 1.000)`, y nunca menos que la suma. *(Código: `BuffMath.converged`.)*

## Edificios

- Costo de la unidad n: `costo base × 1,15ⁿ`.
- Cada edificio se desbloquea al tener 1 del anterior.

## Grimorio

- **Escalera:** escalones en 25, 50 y 100; cada uno x2. Costo `costo base × 1,15^escalón × 10`, redondeado a 2 cifras significativas. *(Código: `GrimoireLadder`.)*
- **Sinergias:** una por par de edificios vecinos (15). Aparecen con 50 de ambos, cuestan lo mismo que el escalón de 50 del edificio objetivo y dan `1 + 0,01 × unidades del edificio fuente`. Sin tope. *(Código: `BuildingSynergies`.)*

## Logros

- El bono global de logros es el **producto** de los bonos de los logros desbloqueados (redondeado a 16 cifras).
- **Conjuntos:** todos los logros de cantidad no secretos de un edificio → x2 a ese edificio. *(Código: `AchievementSets`.)*
- **Próxima meta:** el logro no secreto con mayor progreso (a igual progreso, el primero de la lista). *(Código: `NextGoalFinder`.)*
- **Logros de las mecánicas** *(v0.17)*: +1 % cada uno, +2 % el último escalón de una serie y los combinados, +5 % los secretos; en promedio, cada logro nuevo no da más de x1,02. Cada mecánica tiene al menos 3 y cada dios al menos 2 propios. *(Código: `MechanicAchievements`; test: `MechanicAchievementsTest`.)*
- **Contadores:** lo que miden los logros nuevos se guarda en la estadística `counters`; los de la vida actual (`run.*`) se borran al Ascender. *(Código: `AchievementStat`, `StatisticsManager.counters`.)*

## Ascensión

- Cordura = `⌊√(generado en esta vida ÷ 10⁶)⌋ × bono de Cordura`. Duplicarla exige 4 veces más Conocimiento.
- Cada punto de Cordura Residual: +10 % de producción global (se suma: `1 + 0,1 × Cordura`).

## Hitos y automatización

- Hitos en 1, 3, 5, 10, 25 y 50 Ascensiones totales. *(Código: `AscensionMilestone`.)*
- Los autocompradores gastan como máximo un porcentaje del Conocimiento (**50 %** por defecto; 10–100 % en el panel): una unidad por edificio por segundo y las mejoras que entren en el presupuesto. *(Código: `AutomationSettings.budgetFraction`.)*
- Ascensión automática: cuando `Cordura ganada ≥ max(Cordura Residual, 1)`. *(Código: `AutomationSettings.shouldAutoAscend`.)*
- Efecto neto de un nodo global: `(1 + e·(L+1)) / (1 + e·L) × (1 + 0,1·(S − costo)) / (1 + 0,1·S)`. *(Código: `SanitySpending`.)*

## Pruebas de Locura

- Se superan al generar su meta de Conocimiento en la vida en que se eligieron; la regla se levanta al superarla.
- Las recompensas se multiplican entre sí y son permanentes. Cuatro adelantan a un Dios Exterior. *(Código: `Trial`; test: `TrialTest`.)*

## Espacios de Runa

- Grabar una runa pide atraparla 10 veces (nivel 1), 5 (nivel 2) o 3 (nivel 3). Hay hasta 4 espacios, uno por cada Prueba que los da.
- Cada runa grabada da un bono pasivo propio que se multiplica con el pacto. *(Código: `RuneEngraving`; test: `RuneEngravingTest`.)*

## Medidor de Locura

- Sube: clic +0,06, runa +2/+4/+8, ritual +5, Convergencia +20. Baja por segundo `0,05 + 0,007 × Locura` (fórmula cerrada, igual sin conexión); la Letanía de Calma, 40. Mantener pulsado se estabiliza en ~61 %. *(Test: `MadnessMeterTest`.)*
- Bono: `1 + 0,01 × Locura`, como potenciador temporal. Presagios desde el 50 %: probabilidad por segundo `(Locura − 50) / 500`.
- Colapso al 100 %: se pierden los potenciadores y la producción queda x0,5 durante 60 s. Nunca se pierden recursos. *(Código: `MadnessMeter`.)*

## Fases de la Marea

- Cada fase dura 600 s de juego, en orden Alta, Baja, Muerta. Se calcula del tiempo jugado. *(Código: `TidePhase`; test: `TidePhaseTest`.)*
- Alta: runas y eventos x1,5, Locura sube x1,5. Baja: producción x1,25, Locura baja x2. Muerta: rituales x0,7 de costo y recarga, Ecos x1,5.

## Trascender

- Pide 50 Ascensiones en el ciclo. Éter = `⌊√(Ascensiones del ciclo × log10(1 + Cordura del ciclo)) × 1,25^Eco⌋`. *(Código: `Transcendence`; test: `TranscendenceTest`.)*
- Mejoras eternas: costo `base × crecimiento^nivel`, redondeado hacia arriba. Fuego Eterno x2, Cordura Primordial x1,5 por nivel.
- Los logros de Ascensiones cuentan las Ascensiones de todos los ciclos (`ascensions_past`).

## Consagrar edificios

- Nivel n+1: tener `100 × (n+1)` unidades en la vida y pagar `n+1` de Éter. Máximo V. *(Código: `Consecration`; test: `ConsecrationTest`.)*
- Producción del edificio x3 por nivel (x243 en el V). Eterno: no se pierde al Ascender ni al Trascender.

## Maestría de runas

- Niveles por capturas de siempre: nivel 1 de runa 25/100/250/500/1.000; nivel 2 10/40/100/200/400; nivel 3 5/15/40/80/150.
- Cada nivel: duración del potenciador `x(1 + 0,1·n)` y bono grabado escalado `1 + (b − 1)·(1 + 0,2·n)`. *(Código: `RuneEngraving`; test: `RuneEngravingTest`.)*

## Mutaciones

- Tras la primera Trascendencia. Oferta: 3 de 20 sin repetir, barajadas con la semilla `Ascensiones totales × 7919 + 17` (determinista: no se puede volver a tirar). *(Código: `Mutation.offer`; test: `MutationTest`.)*
- Las de edificio: ese edificio x10 y otro x0,5 (o un precio global de x0,5–x1,5). Las globales: entre x1,5 y x5 de beneficio con un precio de x0,75–x0,9.
- Dura una vida (contador `run.mutation.<id>`); se combina con el pacto, las runas grabadas y la Marea mediante `PactEffects.combinedWith`.

## Sendas

- Tres nodos excluyentes: solo uno puede tener niveles. *(Código: `MadnessPaths`.)*
- Abandonar una Senda (solo al Ascender) devuelve `Σ costo base × crecimiento^i` para i < nivel.

## Atención

- Puntos: `0` sin Ascensiones; luego `min(10, 1 + Ascensiones ÷ 3)`. *(Código: `Attention.available`.)*
- Cada punto: +25 % al destino (edificio, Sueño o Vigilia); los puntos se suman. El reparto se ajusta siempre a los puntos disponibles. *(Código: `Attention.clamp`.)*

## Clic

- Clic = `1 + producción total de la Sinapsis`.
- Mantener pulsado: **8 clics/s** a partir de los 0,3 s, por debajo del autoclic de las runas (10/s) para no quitarles valor.
- Probabilidad de Eco por clic: 1 % + bonos de runas.

## Revelación de sistemas

| Sistema | Condición |
|---|---|
| Grimorio | 250 de Conocimiento histórico, o alguna mejora comprada, o haber Ascendido |
| Rituales | Algún Eco encontrado, o haber Ascendido |
| Ascensión | Poder Ascender, tener Cordura, o haber Ascendido |

Usa estadísticas históricas: un sistema descubierto no vuelve a esconderse salvo con el reinicio total. *(Código: `SystemReveal`.)*

## Reglas técnicas

- La tabla principal del guardado se llama **`game_save`**. Es un estándar del proyecto y **no se cambia**.
- El esquema de Room se guarda en `app/schemas/` y la CI falla si cambia sin actualizarse.
- La CI también comprueba que los campos del guardado no se renombren al minificar.
- El motor corre en un solo hilo; la interfaz solo recibe copias.
- Todo texto de contenido nuevo necesita su traducción al inglés: `ContentTranslationsTest` falla si falta alguna.
