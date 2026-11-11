# Roadmap

El plan sale de una investigación sobre las mecánicas de retención de juegos idle como Cookie Clicker, Antimatter Dimensions, Melvor Idle, NGU Idle y Realm Grinder. Está ordenado por dependencias: primero mejoras baratas sobre lo que existe, luego quitar la repetición de Ascender, después dar identidad a cada partida y recién entonces una segunda capa de prestigio. Desde la etapa 6, el plan toma como referencia a **Revolution Idle**, un idle con cuatro capas de prestigio donde cada capa abre un minijuego que alimenta a los anteriores.

Cada etapa termina en una **puerta**: una condición que debería cumplirse antes de seguir. Las versiones son una referencia y se pueden reagrupar.

| Etapa | Versión | Objetivo | Mecánicas | Puerta |
|---|---|---|---|---|
| 1 | v0.14 | Pulido de la base | Próxima meta ✅, hasta el hito ✅, ritmo del prestigio ✅, conjuntos de logros ✅, sinergias ✅, mantener pulsado ✅, revelación gradual ✅ | Cada sesión muestra una meta concreta |
| 2 | v0.15 | Ascender sin repetir | Hitos de Ascensión ✅, autocompradores ✅, efecto neto de la Cordura ✅, panel de automatización ✅ | La décima Ascensión ya no repite el inicio a mano |
| 3 | v0.16 | Identidad de partida | Pactos (Panteón) ✅, Atención ✅, ramas excluyentes en el Mapa de Locura ✅, combos controlados (Convergencia) ✅ | Dos partidas seguidas se juegan distinto |
| 4 | v0.17 | Desafíos y riesgo | 162 logros de las mecánicas ✅, Pruebas de Locura ✅, espacios de runa ✅, medidor de Locura ✅, fases de la Marea ✅ | Las pruebas desbloquean pactos y espacios de runa |
| 5 | v0.18 – v0.19 | Segunda capa | Trascender (nueva capa de prestigio) ✅, mutaciones ✅, maestría de runas ✅, consagrar edificios ✅ | Trascender abre sistemas, no solo multiplica |
| 6 | v0.19 | Completar la segunda capa | Romper el Velo, Hitos de Trascendencia, Cuando las Estrellas Estén en Posición, Bestiario, Laboratorio de Miskatonic | Las 10 Pruebas tienen un objetivo final y Trascender se automatiza |
| 7 | v0.20 | Profundidad de Trascender | Las Tierras del Sueño, Cadena de Invocación, Letargo, Pruebas de Trascendencia (I–V) | Un ciclo de Trascender tiene un desafío largo propio |
| 8 | v0.21 – v0.23 | Tercera capa: el Despertar | Despertar (Pruebas combinadas), Signos Antiguos y planetas, Expediciones y Reliquias, Cristales de Yuggoth, los Cuatro Elementales | El Despertar abre minijuegos distintos que alimentan a las capas anteriores |
| 9 | v0.24 → v1.0 | El arco final | Cartas del Rey de Amarillo, Artefactos, El Sueño de Azathoth; actos con un final visible, interfaz narrativa, eventos con memoria, contenido oculto | El juego tiene un final visible |

## Las capas como actos

Cada capa de prestigio es un acto de la historia, con su narrativa y su cierre:

| Acto | Capa | Moneda | Qué abre |
|---|---|---|---|
| I | Ascender | Cordura | Mapa de Locura, Sendas, Pactos, Pruebas, Locura, Marea |
| II | Trascender | Éter | Mejoras eternas, Consagración, Maestría, Mutaciones y la etapa 6–7 |
| III | Despertar | Visiones | Signos y planetas, Expediciones, Cristales, Elementales |
| Final | El Sueño de Azathoth | Caos | Cartas, Artefactos y el final del juego |

## Mecánicas planeadas

### Etapa 6: completar la segunda capa

| Mecánica | Inspiración | Idea |
|---|---|---|
| **Romper el Velo** | Romper el Infinito | El Conocimiento tiene un tope, el Velo, que obliga a Ascender. Superar las 10 Pruebas lo rompe y deja crecer sin límite. |
| **Hitos de Trascendencia** | Hitos de Eternidad | Ventajas por número de Trascendencias: conservar la automatización, Ascensión automática, autocompra del Mapa de Locura. |
| **Cuando las Estrellas Estén en Posición** | Estrellas y Polvo Estelar | Tras Trascender se generan Estrellas lentamente; dan Polvo Estelar para un grupo de mejoras propio. |
| **Bestiario** | Zoológico y Animales | Criaturas invocadas con Esencia de las Pruebas (Gato de Ulthar, Shoggoth, Byakhee, Profundo, Mi-Go, Ghoul), cada una con un multiplicador propio. |
| **Laboratorio de Miskatonic** | Laboratorio | Con Éter se suben exponentes (producción ^1,01 por nivel) en lugar de multiplicadores. |

### Etapa 7: profundidad de Trascender

| Mecánica | Inspiración | Idea |
|---|---|---|
| **Las Tierras del Sueño** | Dilatación | Una vida especial donde todo se eleva a ^0,75. Produce Puntos de Sueño y da puntos para un árbol de Sueño con reinicio libre. |
| **Cadena de Invocación** | Generadores | Invocaciones de 8 niveles: la 1 produce Conocimiento y cada una produce a la de abajo. |
| **Letargo** | Ralentización | Frenar la producción a cambio de Ascensiones o Cordura extra, para vidas largas. |
| **Pruebas de Trascendencia** | Desafíos de Eternidad | Las Pruebas con niveles de dificultad I a V, cada uno con más restricciones y mejor recompensa. |

### Etapa 8: el Despertar

| Mecánica | Inspiración | Idea |
|---|---|---|
| **Despertar** | Unidad | Reinicia Trascender y da Visiones. Las Pruebas de todas las capas se combinan y siguen activas al reiniciar. |
| **Signos Antiguos y planetas** | Astrología | Signos con rareza y calidad equipados en planetas (Yuggoth, Saturno, Xoth, Kythanil); suben de nivel y al venderlos dan Polvo Astral. |
| **Expediciones** | Ataque | Un círculo que golpea horrores con daño basado en el Conocimiento; el nivel no tiene tope y da Oro al Despertar. |
| **Reliquias** | Reliquias | Compradas con Oro y desbloqueadas por nivel de Expedición: Trapezoedro Resplandeciente, Corona de Yig, Espejo de Nitocris. |
| **Cristales de Yuggoth** | Minerales | Una cuadrícula donde dos cristales iguales se fusionan en uno mayor; prestigios propios Pulir y Refinar con un árbol de refinado. |
| **Los Cuatro Elementales** | Elementos | Cthugha (fuego), Ithaqua (viento), Cthulhu (agua) y Nyarlathotep (tierra), cada uno con su árbol; solo uno activo a la vez. |

### Etapa 9: el arco final

| Mecánica | Inspiración | Idea |
|---|---|---|
| **Cartas del Rey de Amarillo** | Tarot | Desafíos de cartas (La Torre, La Luna, El Rey), cada uno con una regla y un premio; cuentan la historia del acto final. |
| **Artefactos** | Artefactos | Convierten los recursos de las Cartas en bonos permanentes y aceleran sistemas concretos. |
| **El Sueño de Azathoth** | Singularidad | La última capa: reinicia el Despertar y da Caos con un árbol propio. Despertar a Azathoth es el final visible del juego. |

### Mecánicas generales

- **Tiempo Robado** (Time Flux): el tiempo sin conexión se guarda y se gasta después acelerando el juego x2 o x3.
- **Editor de reglas de automatización** (macros): por ejemplo, "Ascender cuando la Cordura se duplique".
- **Velocidad local:** acelerar solo un sistema sin tocar el resto.

### Descartado

- Recompensas diarias: convierten el juego en una obligación diaria.
- Moneda premium y tienda de pago.
- Ranking global y sincronización en la nube: necesitan un servidor.

## Criterios para cada etapa

- **Un sistema nuevo por versión como máximo**, con su desbloqueo y su tutorial, para no saturar al jugador.
- **Cada mecánica nueva trae una serie de varios logros** (al menos 3 y variados: cantidades, retos con un pacto o una Senda, hazañas, secretos). `MechanicAchievementsTest` lo comprueba.
- **Una sola migración de base de datos por etapa**, con su prueba. La tabla principal sigue siendo `game_save`.
- **Cada valor nuevo con su regla medible** y un test que falle si se sale de ella (ver [Reglas de balance](Reglas-de-balance)).
- **Probar en un dispositivo antes de la versión oficial.**
- Evitar mecánicas que conviertan el juego en una **obligación diaria**.
