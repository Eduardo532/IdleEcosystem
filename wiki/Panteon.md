# Panteón *(v0.16)*

Al **Ascender** eliges un **pacto** para tu próxima vida. El pacto dura hasta la siguiente Ascensión y no se puede cambiar antes. Cada dios da ventajas y cobra un precio según su mito.

La pestaña **Panteón** (se descubre junto con la Ascensión) muestra a todos los dioses con su sigilo. Los que aún no puedes elegir aparecen como silueta, con las Ascensiones que faltan. Al tocar uno se abre su ficha, con su mito y su pacto.

## Sin dioses

| Pacto | Desde | Ventajas | Precio |
|---|---|---|---|
| **Sin Pacto**, la Mente Propia | Siempre | Cordura ganada al Ascender x1,25 | — |

## Grandes Antiguos

| Dios | Desde | Ventajas | Precio |
|---|---|---|---|
| **Cthulhu**, el Durmiente de R'lyeh | 1 | Progreso sin conexión x3 · Templo de R'lyeh x2 | Runas x0,5 |
| **Hastur**, el Rey de Amarillo | 1 | Producción x2 | Cordura ganada x0,75 |
| **Tulzscha**, la Llama Verde | 1 | Duración de los rituales x2 | Probabilidad de Eco x0,5 |
| **Ubbo-Sathla**, la Fuente Primigenia | 3 | Notas, Pergaminos, Artefacto y Sectario x5 | Del Monolito en adelante x0,5 |
| **Abhoth**, el Padre de la Inmundicia | 3 | Costo de los edificios x0,6 | Producción x0,8 |
| **Nug**, el Primogénito | 5 | Sectario, Sacerdote, Santuario y Altar x4 | Clic x0,5 |
| **Yeb**, el de las Nieblas Susurrantes | 5 | Clic x4 · Probabilidad de Eco x1,5 | Producción x0,75 |
| **Daoloth**, el que Rasga los Velos | 10 | Runas x2 · Eventos x1,5 | Producción x0,75 |
| **Ghroth**, el Heraldo | 10 | Eventos x2 · Potenciadores de eventos x2 | Cordura ganada x0,8 |

## Dioses Exteriores

| Dios | Desde | Ventajas | Precio |
|---|---|---|---|
| **Shub-Niggurath**, la Cabra Negra de los Mil Retoños | 20 | Producción x3 · Costo de los edificios x0,85 | Costo de los rituales x2 · Clic x0,5 |
| **Yog-Sothoth**, la Llave y la Puerta | 30 | Progreso sin conexión x5 · Recarga de los rituales x0,5 · Cordura ganada x1,2 | Runas x0,5 |
| **Nyarlathotep**, el Caos Reptante | 40 | Clic x10 · Runas x2 · Eventos x1,5 | Sin progreso sin conexión |
| **Azathoth**, el Sultán Demoníaco | 50 | Producción al azar entre x1 y x20 cada 30 s · Cordura ganada x1,5 | Costo de los rituales x2 |

"Desde" es la Ascensión en la que el pacto ya se puede elegir. Por ejemplo, Cthulhu se puede elegir al hacer tu primera Ascensión.

## Reglas

- La Cordura que ganas al Ascender usa el pacto de la vida **que termina**.
- "Runas x2" significa el doble de seguido; "Potenciadores de eventos x2" convierte un x5 en un x9 (`1 + (5 − 1) × 2`).
- Los multiplicadores del pacto se aplican como los demás bonos permanentes: se multiplican con mejoras, Cordura y logros.
- El pacto se guarda con la partida (columna `activePact` de `game_save`) y viaja en el código de exportación.
