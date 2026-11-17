# La interfaz por capas *(v0.19.0)*

El juego empieza simple: una sola pestaña de edificios y el botón **INVESTIGAR**. Cada sistema aparece recién cuando llegas a él, igual que en los idle por capas como Revolution Idle. Así un jugador nuevo no ve de golpe todo lo que queda.

## Capas y pestañas

El menú de abajo agrupa las pestañas en **capas**, una por cada nivel de prestigio. La fila de capas solo aparece cuando hay más de una, y la fila de pestañas solo cuando la capa tiene más de una pestaña.

| Capa | Color | Pestaña | Aparece al... |
|---|---|---|---|
| **Vida** | violeta | Investigación | desde el inicio |
| | | Grimorio | reunir 250 de Conocimiento en total |
| | | Rituales | encontrar tu primer Eco |
| **Ascensión** | rosa (Cordura) | Cordura | poder Ascender por primera vez (1M de Conocimiento en una vida) |
| | | Panteón | Ascender por primera vez |
| | | Pruebas | llegar a la Ascensión 3 |
| | | Runas | ganar tu primer Espacio de Runa o tu primera Trascendencia |
| | | Bestiario | Ascender por primera vez |
| **Trascendencia** | celeste (Éter) | Éter | tener 50 Ascensiones en el ciclo |
| | | ↳ Éter › Mutaciones | Trascender por primera vez |
| | | Estrellas | Trascender por primera vez |
| | | Laboratorio | Trascender por primera vez |
| | | Ciclos | Trascender por tercera vez (con su primer desafío) |
| | | ↳ Ciclos › Sueño | Trascender por tercera vez |
| | | ↳ Ciclos › Cadena | Trascender por cuarta vez |
| | | ↳ Ciclos › Letargo | Trascender por quinta vez |
| | | ↳ Ciclos › Desafíos | Trascender por sexta vez |
| **Despertar** | amarillo pálido (Visiones) | Despertar | llegar a 10 Trascendencias en total *(v0.21.0)* |
| | | Visiones | ídem |
| | | Signos | el primer Despertar *(v0.21.2)* |
| | | Expedición | el primer Despertar *(v0.21.3)* |
| | | Cristales | el segundo Despertar *(v0.22.0)* |
| | | Elementales | el tercer Despertar *(v0.23.0)* |
| | | Cartas | el quinto Despertar, con los cuatro Elementales invocados *(v0.24.0)* |

**Grupos *(v0.20.4)*:** para que la capa Trascendencia no tenga ocho pestañas apretadas, arriba se ven solo cuatro: **Éter**, **Estrellas**, **Laboratorio** y **Ciclos**. Mutaciones va dentro de Éter, y Sueño, Cadena, Letargo y Desafíos van dentro de Ciclos. Debajo de la fila de pestañas aparece una fila de chips con las pestañas del grupo. Ciclos no tiene contenido propio: abre la primera pestaña con algo nuevo, o la primera del grupo. El punto dorado de algo nuevo se ve en el chip y también en la pestaña del grupo.

Un sistema descubierto no vuelve a esconderse, ni al Ascender ni al Trascender.

## Lo nuevo se marca

- Al aparecer una pestaña, un aviso arriba dice **"✦ Nuevo: …"** con el color de su capa.
- La pestaña y su capa llevan un **punto dorado** hasta que la abres.
- Al final de la lista de Investigación, una línea con candado dice **cuál es el próximo descubrimiento** y qué falta (solo uno a la vez).
- Al actualizar desde una versión anterior, lo que ya tenías no se marca como nuevo.
- **Criaturas nuevas *(v0.20.4)*:** una criatura conseguida lleva la etiqueta **NUEVA** y borde dorado hasta que abres su tarjeta. Su Dominio lleva un punto dorado, el panel dice cuántas nuevas hay y la pestaña Bestiario sigue marcada mientras quede alguna sin mirar. El Bestiario se abre en el primer Dominio con criaturas nuevas.

## Qué hay en cada pestaña de Ascensión y Trascendencia

- **Cordura:** tu Cordura Residual, lo que daría Ascender ahora y accesos al **Mapa de Locura**, a la **Atención** (desde la primera Ascensión) y a la **Automatización** (con el hito de la Ascensión 5).
- **Panteón:** los dioses y sus pactos.
- **Pruebas:** las Pruebas de Locura, su avance y el botón para abandonar la actual.
- **Runas:** el Altar de Runas, con los espacios, las runas grabadas y la maestría.
- **Bestiario:** los Sellos Antiguos y los Fragmentos, el Altar de Invocación (x1 y x10, con garantías), la compra de Sellos con Conocimiento y la colección por Dominio, con una fila por criatura (miniatura, nombre, rareza y nivel). Tocar una abre su tarjeta, con su historia y su ilustración, que se puede ver a pantalla completa y ampliar. Los encuentros flotan arriba de la pantalla, en cualquier pestaña. Ver [Bestiario](Bestiario).
- **Éter:** el Éter, el avance del ciclo, qué se pierde y qué se conserva, las mejoras eternas y el botón **TRASCENDER** (pide tocar dos veces).
- **Mutaciones:** el catálogo de las 20 mutaciones. Las que todavía no elegiste se ven como **???**; la activa lleva la marca **ACTIVA**.
- **Estrellas:** las Estrellas, el Polvo Estelar, la alineación (con su cuenta atrás) y las seis mejoras estelares. Ver [Estrellas](Estrellas).
- **Laboratorio:** los seis experimentos de Miskatonic, con su exponente actual y su costo en Éter. Ver [Laboratorio](Laboratorio).
- **Sueño:** elegir si el próximo ciclo será un Sueño Profundo, despertar antes de tiempo, los Puntos de Sueño y el Árbol del Sueño. Mientras se sueña, un aviso lunar en Investigación lo recuerda. Ver [El Sueño Profundo](El-Sueno-Profundo).
- **Cadena:** el Poder de Invocación y su multiplicador, el botón para comprar todo lo posible y los ocho eslabones, cada uno con sus unidades, sus compras, su multiplicador y su precio en Conocimiento. Ver [Cadena de Invocación](Cadena-de-Invocacion).
- **Letargo:** el interruptor para caer en Letargo o salir, el Sopor de la vida, la Cordura que multiplicará, las Ascensiones extra (y cuánto falta para la próxima) y el aviso de que salir lo pierde. Mientras dura, un aviso con reloj de arena aparece en Investigación. Ver [Letargo](Letargo).
- **Desafíos:** la Prueba de Trascendencia en curso (meta, mejor vida y si ya se alcanzó; botón para abandonarla) y las cuatro Pruebas con sus niveles superados, su restricción, su recompensa, la meta siguiente y el botón para elegirla para el próximo ciclo. Al superar una, el aviso de Trascender lo dice. Ver [Pruebas de Trascendencia](Pruebas-de-Trascendencia).
- **Despertar:** los dos requisitos con su progreso, las Visiones que darías, el botón DESPERTAR (pide confirmación y lista lo que se pierde y lo que se conserva) y los cinco Hitos del Despertar. Al Despertar, un aviso dice cuántas Visiones ganaste. Ver [Despertar](Despertar).
- **Visiones:** el Árbol de Visiones, de nueve nodos, con su reinicio gratis.
- **Signos *(v0.21.2)*:** el Polvo Astral, los cuatro planetas (bloqueados, vacíos o con su Signo y su efecto, con botón para quitarlo) y el inventario ordenado por rareza. Cada Signo tiene botones para equiparlo en cada planeta abierto (la afinidad se marca en amarillo), mejorarlo y venderlo. Hay un botón para vender todos los comunes. Ver [Signos Antiguos](Signos-Antiguos).
- **Expedición *(v0.21.3)*:** el Oro, el nivel y el mejor nivel, el círculo con el horror en curso (retrato del Bestiario), su barra de vida, el daño por segundo, el botón GOLPEAR, el Oro que daría Despertar y las tres Reliquias con su nivel para abrirse. Ver [Expediciones y Reliquias](Expediciones).
- **Cristales *(v0.22.0)*:** el Lustre, el Poder y el multiplicador de producción, cuánto falta para el próximo cristal, la cuadrícula de 4 × 4 (cada cristal con su grado y su color; el elegido en blanco y sus parejas en violeta), los botones Fusionar todo y PULIR. Debajo, *(v0.22.1)* la tarjeta de Refinar (Esencia, botón REFINAR con confirmación) y el árbol de refinado, de ocho nodos, con su reinicio gratis. Ver [Cristales de Yuggoth](Cristales-de-Yuggoth).
- **Elementales *(v0.23.0)*:** el Elemental invocado y su Favor por minuto, las cuatro tarjetas (nombre, elemento, Favor y si está invocado), y el árbol del que se mira con su botón INVOCAR o Despedir; si no es el invocado, un aviso de que está dormido. Ver [Los Cuatro Elementales](Los-Cuatro-Elementales).
- **Cartas *(v0.24.0)*:** los Fragmentos Amarillos; la Carta en juego con sus Trascendencias en este Despertar, una barra hasta la meta y el botón Abandonar; y el mazo, donde cada Carta muestra sus Juramentos, su meta, su premio, el botón ELEGIR (o «Elegida para el próximo Despertar») y, si ya se completó, Leer el capítulo. Al completar una Carta por primera vez aparece un aviso con su capítulo. Ver [Cartas del Rey de Amarillo](Cartas-del-Rey-de-Amarillo).
- **Juramentos *(v0.21.1)*:** en la pestaña Despertar, debajo del botón, una lista de las 14 Pruebas para jurar (con casilla y peso de Visiones). Arriba se ven los Juramentos vigentes y los jurados para el próximo Despertar, cada grupo con su multiplicador.

Con la capa Ascensión descubierta, abajo del menú queda siempre la **barra de Ascender**: tu Cordura, el pacto y la mutación de esta vida, y el botón **ASCENDER**.

## Otras cosas que aparecen con el progreso

- **Multiplicadores de compra** (x1 a x100, HITO y MAX): al tener 5 edificios en total, o desde la primera Ascensión.
- **Estadísticas** (icono de la barra superior): con el Grimorio.
- **Logros** (botón de la barra superior): con el primer logro.

## Runas más claras

- Al aparecer, cada runa muestra una **placa** con su nivel y el gesto que pide (Común · desliza, Rara · mantén pulsado, Mítica · traza el sigilo), su **nombre** y **lo que da**, calculado de sus valores reales y de su maestría. Una barra muestra el tiempo que queda para atraparla.
- Al capturarla, un aviso dice **"RUNA CAPTURADA · nombre"** y lo que acaba de dar.
- Cada potenciador activo es un chip con el **color y el nombre de su runa**, lo que da, los segundos que quedan y una barra que se vacía.

## Sin emojis *(v0.19.10)*

El juego no usa emojis: cambian de aspecto según el teléfono. La Locura, la Marea, las Pruebas, el Velo, el Laboratorio y los candados del Mapa usan iconos dibujados con la estética del juego. Un test lo comprueba.
