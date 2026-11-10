# Ecos y Rituales

## Ecos del Vacío

Los **Ecos** son la segunda moneda del juego y solo sirven para los Rituales.

- **Cómo se consiguen:** cada clic en INVESTIGAR tiene un **1 %** de probabilidad de encontrar un Eco. Algunas runas suben esa probabilidad o dan Ecos directamente (ver [Runas](Runas)).
- **No se pierden al Ascender.**
- Encontrar el primer Eco descubre la pestaña de Rituales *(v0.14)*.

## Rituales

Un ritual se paga con Ecos y da un efecto temporal. Después de usarlo hay que esperar su recarga.

| Ritual | Costo | Efecto | Duración | Recarga |
|---|---|---|---|---|
| Frenesí de Sangre | 5 Ecos | x5 al clic | 15 s | 1 min |
| Cántico del Vacío | 15 Ecos | x3 a la producción | 30 s | 2 min |
| Ofrenda de Conocimiento | 25 Ecos | 2 minutos de producción al instante | inmediato | 10 min |
| Grieta Temporal | 50 Ecos | x20 a la producción | 15 s | 5 min |
| Convergencia *(v0.16.2)* | 75 Ecos | los potenciadores se multiplican (tope x1.000) | 1 min | 30 min |
| Vigilia Eterna | 100 Ecos | x6 a la producción | 2 min | 20 min |

## Cómo se combinan los efectos temporales

Los potenciadores temporales de rituales, runas y eventos **se suman entre sí** en vez de multiplicarse. Por ejemplo, un ritual x3 y una runa x10 activos a la vez dan x12 (1 + 2 + 9), no x30. Los bonos permanentes (mejoras, Cordura, logros) sí se multiplican.

Esta regla evita que juntar varios efectos dispare la producción a valores absurdos.

### Convergencia *(v0.16.2)*

Mientras la **Convergencia** está activa, los potenciadores temporales **se multiplican** en lugar de sumarse, con un tope de **x1.000**. Por ejemplo, Grieta Temporal (x20), Vigilia Eterna (x6) y Cántico del Vacío (x3) dan x27 sumados y **x360** con la Convergencia. Nunca rinde menos que la suma, así que con un solo potenciador da lo mismo: conviene invocarla cuando ya tienes runas y rituales activos a la vez.

## Valor por Eco

Los precios de los rituales de producción están ajustados para que **pagar más Ecos nunca rinda menos por Eco**: un ritual más caro entrega al menos tanta producción por Eco gastado como uno más barato. Ver [Reglas de balance](Reglas-de-balance#rituales).
