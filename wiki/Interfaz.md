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
| **Trascendencia** | celeste (Éter) | Éter | tener 50 Ascensiones en el ciclo |
| | | Mutaciones | Trascender por primera vez |

Un sistema descubierto no vuelve a esconderse, ni al Ascender ni al Trascender.

## Lo nuevo se marca

- Al aparecer una pestaña, un aviso arriba dice **"✦ Nuevo: …"** con el color de su capa.
- La pestaña y su capa llevan un **punto dorado** hasta que la abres.
- Al final de la lista de Investigación, una línea con candado dice **cuál es el próximo descubrimiento** y qué falta (solo uno a la vez).
- Al actualizar desde una versión anterior, lo que ya tenías no se marca como nuevo.

## Qué hay en cada pestaña de Ascensión y Trascendencia

- **Cordura:** tu Cordura Residual, lo que daría Ascender ahora y accesos al **Mapa de Locura**, a la **Atención** (desde la primera Ascensión) y a la **Automatización** (con el hito de la Ascensión 5).
- **Panteón:** los dioses y sus pactos.
- **Pruebas:** las Pruebas de Locura, su avance y el botón para abandonar la actual.
- **Runas:** el Altar de Runas, con los espacios, las runas grabadas y la maestría.
- **Éter:** el Éter, el avance del ciclo, qué se pierde y qué se conserva, las mejoras eternas y el botón **TRASCENDER** (pide tocar dos veces).
- **Mutaciones:** el catálogo de las 20 mutaciones. Las que todavía no elegiste se ven como **???**; la activa lleva la marca **ACTIVA**.

Con la capa Ascensión descubierta, abajo del menú queda siempre la **barra de Ascender**: tu Cordura, el pacto y la mutación de esta vida, y el botón **ASCENDER**.

## Otras cosas que aparecen con el progreso

- **Multiplicadores de compra** (x1 a x100, HITO y MAX): al tener 5 edificios en total, o desde la primera Ascensión.
- **Estadísticas** (icono de la barra superior): con el Grimorio.
- **Logros** (botón de la barra superior): con el primer logro.

## Runas más claras

- Al aparecer, cada runa muestra una **placa** con su nivel y el gesto que pide (Común · desliza, Rara · mantén pulsado, Mítica · traza el sigilo), su **nombre** y **lo que da**, calculado de sus valores reales y de su maestría. Una barra muestra el tiempo que queda para atraparla.
- Al capturarla, un aviso dice **"RUNA CAPTURADA · nombre"** y lo que acaba de dar.
- Cada potenciador activo es un chip con el **color y el nombre de su runa**, lo que da, los segundos que quedan y una barra que se vacía.
