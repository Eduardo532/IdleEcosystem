# Grimorio

El Grimorio es la tienda de **mejoras**. Se paga con Conocimiento y cada mejora se compra una sola vez por vida: al [Ascender](Ascension-y-Mapa-de-Locura) se pierden todas.

- Solo se ven las mejoras que ya puedes alcanzar (las que tienen requisito aparecen al cumplirlo).
- **Comprar todas** compra de una vez todas las que te alcanzan, de la más barata a la más cara.
- El interruptor **Ver Comprados** vuelve a mostrar las que ya tienes.

Hay tres familias de mejoras.

## 1. Mejoras originales

21 mejoras con nombre propio, siempre visibles:

- **De edificio:** x2 o x3 a la producción de un edificio (por ejemplo, *Tinta de Calamar*: x2 a las Notas, 250).
- **De clic:** *Pluma de Cuervo* (x3) y *Escritura Automática* (x5).
- **Globales:** *Alineación Menor* (x1,5) y *Sincronicidad Cósmica* (x2) a toda la producción.

## 2. Escalera del Grimorio

Tres mejoras por edificio (48 en total), que aparecen al tener **25, 50 y 100** unidades:

| Escalón | Nombre | Efecto |
|---|---|---|
| 25 | *Edificio*: Devoción | x2 a ese edificio |
| 50 | *Edificio*: Obsesión | x2 a ese edificio |
| 100 | *Edificio*: Trascendencia | x2 a ese edificio |

En la Sinapsis, la escalera duplica el clic.

**Costo:** 10 veces lo que cuesta la unidad número 25, 50 o 100 de ese edificio, redondeado a dos cifras (`costo base × 1,15^escalón × 10`). Así cada escalón cuesta lo mismo en relación con su edificio, del primero al último.

## 3. Sinergias *(v0.14)*

Una sinergia conecta dos edificios vecinos. Una vez comprada, **cada unidad del edificio anterior suma un +1 % a la producción del siguiente**.

- **Aparecen** al tener al menos **50 de ambos** edificios.
- **Cuestan** lo mismo que el escalón de 50 del edificio que reciben el bono.
- **El bono crece** con lo que tengas en cada momento: 50 unidades dan x1,5; 100, x2; 200, x3; 600, x7.
- **Solo van hacia adelante:** las Notas potencian a los Pergaminos, no al revés.

| Sinergia | Da el bono → lo recibe | Costo aprox. |
|---|---|---|
| Mente y Mano | Sinapsis → Notas | 163 K |
| Notas al Margen | Notas → Pergaminos | 1.08 M |
| Inscripciones Rituales | Pergaminos → Artefacto | 11.9 M |
| Reliquias de Culto | Artefacto → Sectario | 130 M |
| Jerarquía del Culto | Sectario → Sacerdote | 1.41 B |
| Procesión Subterránea | Sacerdote → Santuario | 15.2 B |
| Cámara de Sacrificios | Santuario → Altar | 217 B |
| Sangre en la Piedra | Altar → Monolito | 3.58 T |
| Cimientos Ciclópeos | Monolito → Templo | 55.3 T |
| Geometría No Euclidiana | Templo → Portal | 813 T |
| Mareas del Otro Lado | Portal → Shoggoths | 10.8 Qa |
| Pesadillas Vivientes | Shoggoths → Sueño | 152 Qa |
| Soñar con las Estrellas | Sueño → Alineación Estelar | 1.84 Qi |
| Cuando las Estrellas Estén en Posición | Alineación Estelar → Avatar | 22.8 Qi |
| El Heraldo del Trono | Avatar → Trono | 282 Qi |

**Para qué sirven:** en un idle, el edificio más nuevo suele producir tanto que los anteriores dejan de importar. Con las sinergias, seguir comprando Notas o Sectarios mejora al edificio siguiente.
