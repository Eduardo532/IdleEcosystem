# Los Signos Antiguos *(v0.21.2)*

Es el primer minijuego del **Despertar**, inspirado en la astrología de Revolution Idle. Se abre con el **primer Despertar**, en la pestaña **Signos** de la capa Despertar.

## Conseguir Signos

- Cada **Trascendencia** da **1 Signo** al azar, y cada **Despertar** da **3**.
- Cada Signo tiene:
  - un **tipo**, que dice qué potencia: Llama (producción), Mano (clic), Mente (Cordura), Éter (Éter) u Ojo (Visiones);
  - una **rareza**: Común 50 %, Raro 30 %, Épico 14 %, Legendario 5 % o Mítico 1 %;
  - una **calidad** de 1 a 100 %;
  - un **nivel** de 1 a 10.
- El inventario guarda **30 Signos**. Si está lleno, el Signo nuevo se vende solo.

## Bono de un Signo

`bono = base del tipo × rareza × (0,5 + calidad) × (1 + 0,5 por cada nivel de más)`

| Tipo | Potencia | Base |
|---|---|---|
| Signo de la Llama | Producción | 2 |
| Signo de la Mano | Poder de clic | 3 |
| Signo de la Mente | Cordura ganada | 0,5 |
| Signo del Éter | Éter ganado | 0,25 |
| Signo del Ojo | Visiones ganadas | 0,1 |

La rareza multiplica el bono: x1 (Común), x2 (Raro), x4 (Épico), x8 (Legendario) y x16 (Mítico).

## Planetas

Un Signo solo cuenta **equipado en un planeta**, y su efecto es `x(1 + bono)`. Si el planeta tiene la **afinidad** del Signo, el bono se **duplica**. Los planetas se abren con Despertares:

| Planeta | Despertares | Afinidad |
|---|---|---|
| Yuggoth | 1 | Llama (producción) |
| Saturno | 2 | Mente (Cordura) |
| Xoth | 3 | Éter |
| Kythanil | 5 | Ojo (Visiones) |

## Polvo Astral

- **Subir de nivel** un Signo cuesta `10 × rareza × 2^(nivel − 1)` de Polvo Astral.
- **Venderlo** da `5 × rareza × (0,5 + calidad) × nivel` de Polvo; un Signo equipado se quita al venderlo.
- El botón **Vender comunes** vende todos los Signos comunes que no estén equipados.
- Los Signos, los planetas y el Polvo **no se pierden** al Trascender ni al Despertar.

## Logros

Los Signos Antiguos traen **14 logros**:
- conseguir 1, 25 y 100 Signos;
- conseguir un Signo Legendario y uno Mítico;
- subir un Signo a nivel 5 y a nivel 10;
- equipar los cuatro planetas;
- vender 20 Signos;
- conseguir Signos con el pacto de Nug o con el de Yeb;
- conseguir Signos en la Senda del Engranaje o en la del Culto;
- uno secreto.
