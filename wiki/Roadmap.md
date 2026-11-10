# Roadmap

El plan sale de una investigación sobre las mecánicas de retención de juegos idle como Cookie Clicker, Antimatter Dimensions, Melvor Idle, NGU Idle y Realm Grinder. Está ordenado por dependencias: primero mejoras baratas sobre lo que existe, luego quitar la repetición de Ascender, después dar identidad a cada partida y recién entonces una segunda capa de prestigio.

Cada etapa termina en una **puerta**: una condición que debería cumplirse antes de seguir. Las versiones son una referencia y se pueden reagrupar.

| Etapa | Versión | Objetivo | Mecánicas | Puerta |
|---|---|---|---|---|
| 1 | v0.14 | Pulido de la base | Próxima meta ✅, hasta el hito ✅, ritmo del prestigio ✅, conjuntos de logros ✅, sinergias ✅, mantener pulsado ✅, revelación gradual ✅ | Cada sesión muestra una meta concreta |
| 2 | v0.15 | Ascender sin repetir | Hitos de Ascensión ✅, autocompradores ✅, efecto neto de la Cordura ✅, panel de automatización ✅ | La décima Ascensión ya no repite el inicio a mano |
| 3 | v0.16 | Identidad de partida | Pactos (Panteón) ✅, Atención ✅, ramas excluyentes en el Mapa de Locura ✅, combos controlados (Convergencia) ✅ | Dos partidas seguidas se juegan distinto |
| 4 | v0.17 | Desafíos y riesgo | 162 logros de las mecánicas ✅, Pruebas de Locura ✅, espacios de runa ✅, medidor de Locura ✅, fases de la Marea ✅ | Las pruebas desbloquean pactos y espacios de runa |
| 5 | v0.18 – v0.19 | Segunda capa | Trascender (nueva capa de prestigio), mutaciones, maestría de runas, consagrar edificios | Trascender abre sistemas, no solo multiplica |
| 6 | hacia v1.0 | El arco final | Actos con un final visible, interfaz narrativa, eventos con memoria, contenido oculto | — |

## Criterios para cada etapa

- **Un sistema nuevo por versión como máximo**, con su desbloqueo y su tutorial, para no saturar al jugador.
- **Cada mecánica nueva trae una serie de varios logros** (al menos 3 y variados: cantidades, retos con un pacto o una Senda, hazañas, secretos). `MechanicAchievementsTest` lo comprueba.
- **Una sola migración de base de datos por etapa**, con su prueba. La tabla principal sigue siendo `game_save`.
- **Cada valor nuevo con su regla medible** y un test que falle si se sale de ella (ver [Reglas de balance](Reglas-de-balance)).
- **Probar en un dispositivo antes de la versión oficial.**
- Evitar mecánicas que conviertan el juego en una **obligación diaria**.
