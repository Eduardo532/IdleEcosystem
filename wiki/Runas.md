# Runas

Las runas son símbolos que aparecen de vez en cuando sobre la pantalla. Si las reclamas a tiempo, dan un bono. Si no, desaparecen.

## Cuándo aparecen

- Después de cada runa hay una espera mínima de **40 segundos**.
- Pasada esa espera, cada segundo hay alrededor de un **1 %** de probabilidad de que aparezca otra (en promedio, una cada dos minutos y algo).

## Los tres niveles

| Nivel | Probabilidad | Cómo se reclama |
|---|---|---|
| 1 | 80 % | **Cortar:** desliza el dedo a través de la runa |
| 2 | 17 % | **Estabilizar:** mantén el dedo sobre la runa |
| 3 | 3 % | **Inscribir:** traza la figura que se muestra |

Dentro de cada nivel, unas runas son más comunes que otras.

## Catálogo

### Nivel 1

| Runa | Efecto |
|---|---|
| Runa de Sangre | x10 producción (15 s) |
| Runa de Ceniza | x8 producción (20 s) |
| Runa de Sal | x15 producción (10 s) |
| Runa de Hueso | +2 minutos de producción al instante |
| Runa de Sombra | x5 producción (30 s) |
| Runa de Susurro | x2 clic + autoclic (20 s) |
| Runa de Óxido | x9 producción (18 s) |
| Runa de Escarcha | Congela los demás efectos (5 s) |
| Runa de Plaga | x12 producción (12 s) |
| Runa de Grieta | +3 minutos de producción al instante |
| Runa de Vacío Menor | +1 % de probabilidad de Eco |

### Nivel 2

| Runa | Efecto |
|---|---|
| Runa del Tiempo | x5 clic + autoclic (30 s) |
| Runa de Gravedad | x25 producción (20 s) |
| Runa de Barrera | −15 % a los costos (30 s) |
| Runa de Marea | x50 producción (10 s) |
| Runa de Mente | +5 % de Cordura Residual |
| Runa de Visión | Invoca un evento |
| Runa de Entropía | Reinicia la espera de runas |

### Nivel 3

| Runa | Efecto |
|---|---|
| Sigilo de Cthulhu | x30 producción (60 s) + 1 Eco |
| El Caos Reptante | x25 producción (60 s) + autoclic (20 s) |
| Sigilo de Azathoth | +50 Ecos |

## Detalles

- **Autoclic:** hace 10 clics por segundo mientras dura.
- **Efectos activos:** aparecen como etiquetas bajo el contador, con su tiempo restante.
- Los efectos de las runas **se suman** con los de rituales y eventos (ver [Ecos y Rituales](Ecos-y-Rituales#cómo-se-combinan-los-efectos-temporales)).
- Cada nivel tiene un **valor fijo**, medido en segundos de producción. Ver [Reglas de balance](Reglas-de-balance#runas).

## Espacios de Runa *(v0.17.2)*

Una runa que ya atrapaste varias veces se puede **grabar** en un espacio del **Altar de Runas** (pestaña Panteón → vista **Runas**). Mientras está grabada da un **bono pasivo permanente**, distinto para cada runa.

- **Espacios:** se ganan superando Pruebas de Locura. Ceguera, Silencio, Amnesia y Mortalidad dan uno cada una (hasta **4**).
- **Para grabar una runa** hay que haberla atrapado: **10 veces** las de nivel 1, **5** las de nivel 2 y **3** los sigilos de nivel 3. Las capturas se cuentan desde la v0.17.2.
- **Cambiar es gratis:** quita una runa y graba otra cuando quieras.
- Los bonos de las runas grabadas se **multiplican** con el pacto de la vida actual y entre sí.

| Runa | Bono grabada |
|---|---|
| Runa de Sangre | Producción x1,15 |
| Runa de Ceniza | Progreso sin conexión x1,3 |
| Runa de Sal | Duración de los rituales x1,2 |
| Runa de Hueso | Costo de los edificios x0,95 |
| Runa de Sombra | Frecuencia de las runas x1,15 |
| Runa de Susurro | Clic x1,5 |
| Runa de Óxido | Costo de los rituales x0,9 |
| Runa de Escarcha | Recarga de los rituales x0,9 |
| Runa de Plaga | Frecuencia de los eventos x1,2 |
| Runa de Grieta | Progreso sin conexión x1,5 |
| Runa de Vacío Menor | Probabilidad de Eco x1,2 |
| Runa del Tiempo | Clic x2 |
| Runa de Gravedad | Producción x1,3 |
| Runa de Barrera | Costo de los edificios x0,9 |
| Runa de Marea | Fuerza de los potenciadores de eventos x1,5 |
| Runa de Mente | Cordura ganada al Ascender x1,1 |
| Runa de Visión | Frecuencia de los eventos x1,4 |
| Runa de Entropía | Frecuencia de las runas x1,3 |
| Sigilo de Cthulhu | Producción x1,5 |
| El Caos Reptante | Clic x4 |
| Sigilo de Azathoth | Probabilidad de Eco x2 |

Con los Espacios de Runa llegan **16 logros** (categoría Arcanos): espacios ganados, runas grabadas, atrapar las 21 runas distintas, grabar sigilos y retos con un dios (por ejemplo, tener grabado el Sigilo de Cthulhu durante una vida con Cthulhu).

## Maestría de runas *(v0.18.3)*

Tras la **primera Trascendencia**, cada runa gana **maestría** según cuántas veces la atrapaste **desde siempre** (las capturas de maestría nunca se reinician, ni al Trascender). Se ve como estrellas en la vista **Runas** del Panteón.

| Nivel | Nivel 1 (capturas) | Nivel 2 (capturas) | Nivel 3 (capturas) |
|---|---|---|---|
| ★ | 25 | 10 | 5 |
| ★★ | 100 | 40 | 15 |
| ★★★ | 250 | 100 | 40 |
| ★★★★ | 500 | 200 | 80 |
| ★★★★★ | 1.000 | 400 | 150 |

Cada estrella:
- **alarga un 10 %** el potenciador de la runa al atraparla (x1,5 con 5 estrellas);
- hace **un 20 % más fuerte** su bono si está grabada (con 5 estrellas, el doble de fuerte: la Runa de Sangre pasa de x1,15 a x1,30).

La maestría trae **9 logros** (Arcanos): niveles de maestría en total, una estrella en las 21 runas, una runa a 5 estrellas, retos con Nyarlathotep, Cthulhu y Daoloth, y uno secreto.
