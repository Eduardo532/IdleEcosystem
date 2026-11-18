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
| 6 | v0.19 | Completar la segunda capa | Romper el Velo ✅, Hitos de Trascendencia ✅, Cuando las Estrellas Estén en Posición ✅, Bestiario ✅, Laboratorio de Miskatonic ✅ | Las 10 Pruebas tienen un objetivo final y Trascender se automatiza |
| 7 | v0.20 | Profundidad de Trascender | El Sueño Profundo ✅, Cadena de Invocación ✅, Letargo ✅, Pruebas de Trascendencia (I–V) ✅ | Un ciclo de Trascender tiene un desafío largo propio |
| 8 | v0.21 – v0.23 | Tercera capa: el Despertar | Despertar (Pruebas combinadas) ✅, Signos Antiguos y planetas ✅, Expediciones y Reliquias ✅, Cristales de Yuggoth ✅, los Cuatro Elementales ✅ | El Despertar abre minijuegos distintos que alimentan a las capas anteriores |
| 9 | v0.24 → v1.0 | El arco final | Cartas del Rey de Amarillo ✅ (v0.24.0), Artefactos ✅ (v0.24.1), El Sueño de Azathoth ✅ (v0.25.0), Despertar a Azathoth ✅ (v0.25.1), actos con un final visible e interfaz narrativa ✅ (la Crónica, v0.26.0); eventos con memoria y contenido oculto ✅ (v0.26.1) | El juego tiene un final visible |

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
| **Romper el Velo** ✅ *v0.19.6* | Romper el Infinito | Tope suave en 1e25 de Conocimiento por vida (producción ÷(K/Velo)²). Superar las 10 Pruebas lo rompe para siempre: sin tope y producción x100. Ver [El Velo](El-Velo). |
| **Hitos de Trascendencia** ✅ *v0.19.5* | Hitos de Eternidad | Seis ventajas por número de Trascendencias: automatización desde el inicio del ciclo, primeras estrellas encendidas, Senda Eterna, Trascendencia automática, Atención con todas las Ascensiones y Trascender con 40. Ver [Trascender](Trascender). |
| **Cuando las Estrellas Estén en Posición** ✅ *v0.19.7* | Estrellas y Polvo Estelar | Tras Trascender nacen Estrellas (también sin conexión) que dan Polvo Estelar para seis mejoras estelares. Cada media hora se alinean y el Polvo cae x5. Ver [Estrellas](Estrellas). |
| **Bestiario** ✅ *v0.19.8 · v0.19.10 · v0.19.11* | Zoológico y Animales | 312 criaturas ilustradas en 8 Dominios. Se consiguen invocando (gachapón) con Sellos Antiguos, una moneda que nunca se reinicia; las repetidas suben de nivel y dan Fragmentos al máximo. Ver [Bestiario](Bestiario). |
| **Laboratorio de Miskatonic** ✅ *v0.19.9* | Laboratorio | Seis experimentos pagados con Éter que suben exponentes: producción, clic, Cordura y Éter, además de potenciar el Bestiario y alejar el Velo. Ver [Laboratorio](Laboratorio). |

### Etapa 7: profundidad de Trascender

| Mecánica | Inspiración | Idea |
|---|---|---|
| **El Sueño Profundo** ✅ *v0.20.0* | Dilatación | Desde la 3ª Trascendencia, un ciclo soñado donde producción, clic y Cordura se elevan a ^0,75. Al Trascender paga Puntos de Sueño para un árbol de 8 nodos con reinicio libre. Ver [El Sueño Profundo](El-Sueno-Profundo). |
| **Cadena de Invocación** ✅ *v0.20.1* | Generadores | Desde la 4ª Trascendencia, ocho eslabones comprados con Conocimiento: cada uno produce al de abajo y el primero, Poder de Invocación, que multiplica la producción. Se deshace al Trascender. Ver [Cadena de Invocación](Cadena-de-Invocacion). |
| **Letargo** ✅ *v0.20.2* | Ralentización | Desde la 5ª Trascendencia: producción x0,1 a cambio de Sopor, que multiplica la Cordura al Ascender y hace que cada hora la Ascensión cuente una vez más (hasta 4). Ver [Letargo](Letargo). |
| **Pruebas de Trascendencia** ✅ *v0.20.3* | Desafíos de Eternidad | Desde la 6ª Trascendencia, cuatro Pruebas de ciclo entero (Hambre, Cordura Quebrada, Soledad y Silencio), cada una con niveles I a V: más meta y mejor recompensa permanente. Ver [Pruebas de Trascendencia](Pruebas-de-Trascendencia). |

### Etapa 8: el Despertar

| Mecánica | Inspiración | Idea |
|---|---|---|
| **Despertar** ✅ *v0.21.0 – v0.21.1* | Unidad | La tercera capa: con 10 Trascendencias y 5 niveles de Pruebas de Trascendencia reinicia Trascender y da Visiones para un árbol de 9 nodos, con 5 hitos. Los **Juramentos** combinan las Pruebas de todas las capas: se juran para el próximo Despertar, rigen todo el Despertar y multiplican las Visiones. Ver [Despertar](Despertar). |
| **Signos Antiguos y planetas** ✅ *v0.21.2* | Astrología | Desde el primer Despertar, Signos con rareza y calidad (1 por Trascendencia y 3 por Despertar) equipados en Yuggoth, Saturno, Xoth y Kythanil, cada planeta con su afinidad; suben de nivel con Polvo Astral y al venderlos lo dan. Ver [Signos Antiguos](Signos-Antiguos). |
| **Expediciones** ✅ *v0.21.3* | Ataque | Desde el primer Despertar, un círculo golpea horrores del Bestiario con daño basado en la producción; el nivel no tiene tope y da Oro al Despertar. Ver [Expediciones y Reliquias](Expediciones). |
| **Reliquias** ✅ *v0.21.3* | Reliquias | Compradas con Oro y desbloqueadas por nivel de Expedición: Trapezoedro Resplandeciente (producción), Corona de Yig (Éter), Espejo de Nitocris (daño y Visiones). |
| **Cristales de Yuggoth** ✅ *v0.22.0 – v0.22.1* | Minerales | Desde el segundo Despertar, una cuadrícula de 4 × 4 donde dos cristales iguales se fusionan en uno mayor y su Poder multiplica la producción. Dos prestigios propios: Pulir (Lustre) y Refinar (Esencia de los Mi-Go), con un árbol de refinado de 8 nodos. Ver [Cristales de Yuggoth](Cristales-de-Yuggoth). |
| **Los Cuatro Elementales** ✅ *v0.23.0* | Elementos | Desde el tercer Despertar, Cthugha (fuego), Ithaqua (viento), Cthulhu (agua) y Nyarlathotep (tierra), cada uno con su árbol de 4 nodos comprado con su Favor; solo uno invocado a la vez y solo su árbol hace efecto. Ver [Los Cuatro Elementales](Los-Cuatro-Elementales). |

### Etapa 9: el arco final

| Mecánica | Inspiración | Idea |
|---|---|---|
| **Cartas del Rey de Amarillo** ✅ *v0.24.0* | Tarot | Desde el quinto Despertar con los cuatro Elementales: ocho Cartas (El Loco, La Torre, La Luna… El Rey de Amarillo), cada una con Juramentos que rigen un Despertar entero, una meta de Trascendencias, un bono permanente, Fragmentos Amarillos y un capítulo de la historia del acto final. Ver [Cartas del Rey de Amarillo](Cartas-del-Rey-de-Amarillo). |
| **Artefactos** ✅ *v0.24.1* | Artefactos | Siete Artefactos que se compran con los Fragmentos Amarillos de las Cartas, valen para siempre y aceleran sistemas concretos: Favor, Sueño, la Mina, el Polvo Estelar, la Expedición, las Visiones y los propios Fragmentos. Ver [Cartas del Rey de Amarillo](Cartas-del-Rey-de-Amarillo). |
| **El Sueño de Azathoth** ✅ *v0.25.0 – v0.25.1* | Singularidad | La última capa: Descender reinicia el Despertar y da Caos, que compra el árbol del Caos (7 nodos). Despertar a Azathoth es el final visible: epílogo, resumen de la partida y el juego sigue con el Ojo abierto. Ver [El Sueño de Azathoth](El-Sueno-de-Azathoth). |
| **La Crónica** ✅ *v0.26.0* | Narrativa | Los actos con un final visible y la interfaz narrativa: la partida contada en cinco actos (uno por capa) y veinte capítulos que se escriben solos al llegar a hitos, con una barra hacia el final y avisos de cada capítulo nuevo. Ver [La Crónica](La-Cronica). |
| **Eventos con memoria y contenido oculto** ✅ *v0.26.1* | Narrativa | Tres historias de eventos que recuerdan para siempre tus decisiones y cambian según ellas, de la Ascensión al Despertar; al cerrarlas se escriben páginas ocultas en el Apéndice de la Crónica, y hay un encuentro secreto para quien cierre las tres. Ver [Eventos](Eventos). |

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
