"""Plantilla común de las ilustraciones del Bestiario (v0.19.11).

Estilo: carta coleccionable oscura + grabado antiguo en tinta. Cada criatura es un
archivo SVG propio (src/<id>.svg) con solo su dibujo; esta plantilla añade el fondo de
carta, el papel, el halo del Dominio, los rayados de grabado y los estilos de trazo.

Clases para dibujar (viewBox 0 0 200 200):
  b   cuerpo: aguada del color del Dominio, contorno de tinta
  b2  cuerpo secundario (más oscuro)
  sh  sombra rayada (grabado) sobre una forma del cuerpo
  sx  sombra con rayado cruzado (más oscura)
  dt  punteado (texturas: piel, piedra)
  ln  línea de tinta suelta (pliegues, escamas, arrugas)
  hl  luz: trazo marfil fino
  dk  relleno de tinta (huecos, bocas, interiores)
  bn  hueso / dientes / garras (marfil)
  ey  ojo que brilla (color de la rareza)
  pu  pupila
  ac  acento de color (joyas, fuego, veneno) en el color secundario del Dominio
  mt  metal / oro
"""

DOMAIN = {
    'deep':   ('#4DB6AC', '#1E5E57', '#B2FFF4'),
    'under':  ('#A1887F', '#4E3B33', '#E8D5C4'),
    'sky':    ('#9575CD', '#3F2E66', '#D9CCFF'),
    'cosmos': ('#FFD54F', '#7A5B12', '#FFF4C2'),
    'dream':  ('#7986CB', '#2C3470', '#D3DAFF'),
    'eras':   ('#4DD0E1', '#145F69', '#C9F6FC'),
    'outer':  ('#CE93D8', '#5E2F68', '#F6D9FA'),
    'arcane': ('#FFB74D', '#7A4A10', '#FFE2B8'),
}
RARITY = {'C': '#CFD8DC', 'U': '#A5D6A7', 'R': '#90CAF9', 'M': '#E1BEE7', 'P': '#FFE082'}

INK = '#120E0B'
IVORY = '#EDE3CF'


def mix(a: str, b: str, t: float) -> str:
    """Mezcla dos colores #RRGGBB (t = cuánto de b)."""
    ca = [int(a[i:i + 2], 16) for i in (1, 3, 5)]
    cb = [int(b[i:i + 2], 16) for i in (1, 3, 5)]
    return '#' + ''.join(f'{round(x + (y - x) * t):02X}' for x, y in zip(ca, cb))


def wrap(body: str, domain: str, rarity: str, seed: int = 3) -> str:
    base, dark0, light0 = DOMAIN[domain]
    # Aguada de grabado: el color del Dominio apagado hacia el sepia del pergamino
    c = mix(base, '#7A6A52', 0.42)
    dark = mix(dark0, '#17110C', 0.45)
    light = mix(light0, '#DCCBA6', 0.5)
    eye = RARITY[rarity]
    return f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200">
<defs>
  <radialGradient id="card" cx="50%" cy="42%" r="70%">
    <stop offset="0" stop-color="{dark}" stop-opacity="0.55"/>
    <stop offset="0.55" stop-color="#15121A"/>
    <stop offset="1" stop-color="#07060A"/>
  </radialGradient>
  <radialGradient id="halo" cx="50%" cy="50%" r="50%">
    <stop offset="0" stop-color="{base}" stop-opacity="0.28"/>
    <stop offset="1" stop-color="{base}" stop-opacity="0"/>
  </radialGradient>
  <linearGradient id="wash" x1="0.2" y1="0" x2="0.8" y2="1">
    <stop offset="0" stop-color="{light}"/>
    <stop offset="0.45" stop-color="{c}"/>
    <stop offset="1" stop-color="{dark}"/>
  </linearGradient>
  <linearGradient id="wash2" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="{c}"/>
    <stop offset="1" stop-color="{dark}"/>
  </linearGradient>
  <linearGradient id="bone" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="#F6EEDC"/>
    <stop offset="1" stop-color="#B9A98A"/>
  </linearGradient>
  <linearGradient id="gold" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0" stop-color="#FFF1B0"/>
    <stop offset="0.5" stop-color="#E0A526"/>
    <stop offset="1" stop-color="#7A4E06"/>
  </linearGradient>
  <radialGradient id="eyeg">
    <stop offset="0" stop-color="#FFFFFF"/>
    <stop offset="0.35" stop-color="{eye}"/>
    <stop offset="1" stop-color="{eye}" stop-opacity="0"/>
  </radialGradient>
  <radialGradient id="iris">
    <stop offset="0" stop-color="#FFFFFF"/>
    <stop offset="0.45" stop-color="{eye}"/>
    <stop offset="1" stop-color="{mix(eye, '#120E0B', 0.55)}"/>
  </radialGradient>
  <pattern id="hatch" width="3.2" height="3.2" patternUnits="userSpaceOnUse" patternTransform="rotate(38)">
    <line x1="0" y1="0" x2="0" y2="3.2" stroke="{INK}" stroke-width="1.05"/>
  </pattern>
  <pattern id="cross" width="3.2" height="3.2" patternUnits="userSpaceOnUse" patternTransform="rotate(38)">
    <path d="M0 0V3.2M0 0H3.2" stroke="{INK}" stroke-width="0.95"/>
  </pattern>
  <pattern id="dots" width="4" height="4" patternUnits="userSpaceOnUse">
    <circle cx="1" cy="1" r="0.55" fill="{INK}"/><circle cx="3" cy="3" r="0.45" fill="{INK}"/>
  </pattern>
  <filter id="paper" x="0" y="0" width="100%" height="100%">
    <feTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves="3" seed="{seed}" result="n"/>
    <feColorMatrix type="matrix" values="0 0 0 0 0.9  0 0 0 0 0.85  0 0 0 0 0.75  0 0 0 0.09 0"/>
  </filter>
  <filter id="ink" x="-10%" y="-10%" width="120%" height="120%">
    <feTurbulence type="fractalNoise" baseFrequency="0.06" numOctaves="2" seed="{seed}" result="w"/>
    <feDisplacementMap in="SourceGraphic" in2="w" scale="1.6" xChannelSelector="R" yChannelSelector="G"/>
  </filter>
  <filter id="rim" x="-20%" y="-20%" width="140%" height="140%">
    <feMorphology in="SourceAlpha" operator="dilate" radius="2.2" result="d"/>
    <feGaussianBlur in="d" stdDeviation="3.2" result="b"/>
    <feFlood flood-color="{base}" flood-opacity="0.45"/>
    <feComposite in2="b" operator="in" result="r"/>
    <feMerge><feMergeNode in="r"/><feMergeNode in="SourceGraphic"/></feMerge>
  </filter>
  <filter id="glow" x="-100%" y="-100%" width="300%" height="300%">
    <feGaussianBlur stdDeviation="1.6" result="g"/>
    <feMerge><feMergeNode in="g"/><feMergeNode in="g"/><feMergeNode in="SourceGraphic"/></feMerge>
  </filter>
  <style>
    .b{{fill:url(#wash);stroke:{INK};stroke-width:2.2;stroke-linejoin:round;stroke-linecap:round}}
    .b2{{fill:url(#wash2);stroke:{INK};stroke-width:2;stroke-linejoin:round;stroke-linecap:round}}
    .sh{{fill:url(#hatch);stroke:none;opacity:.85}}
    .sx{{fill:url(#cross);stroke:none;opacity:.9}}
    .dt{{fill:url(#dots);stroke:none;opacity:.8}}
    .ln{{fill:none;stroke:{INK};stroke-width:1.3;stroke-linecap:round;stroke-linejoin:round}}
    .lnt{{fill:none;stroke:{INK};stroke-width:0.8;stroke-linecap:round}}
    .hl{{fill:none;stroke:{IVORY};stroke-width:0.9;stroke-linecap:round;opacity:.75}}
    .dk{{fill:{INK};stroke:{INK};stroke-width:1.2;stroke-linejoin:round}}
    .bn{{fill:url(#bone);stroke:{INK};stroke-width:1.2;stroke-linejoin:round}}
    .ey{{fill:url(#eyeg);filter:url(#glow)}}
    .pu{{fill:{INK}}}
    .ac{{fill:{light};stroke:{INK};stroke-width:1.1;filter:url(#glow)}}
    .mt{{fill:url(#gold);stroke:{INK};stroke-width:1.2;stroke-linejoin:round}}
    .ink{{fill:none;stroke:{INK};stroke-width:2.2;stroke-linecap:round;stroke-linejoin:round}}
  </style>
</defs>
<rect width="200" height="200" fill="url(#card)"/>
<rect width="200" height="200" filter="url(#paper)"/>
<circle cx="100" cy="102" r="86" fill="url(#halo)"/>
<g opacity="0.07"><rect width="200" height="200" fill="url(#hatch)"/></g>
<g filter="url(#rim)"><g filter="url(#ink)">
{body}
</g></g>
<rect x="3" y="3" width="194" height="194" rx="10" fill="none" stroke="{base}" stroke-opacity="0.35" stroke-width="1.2"/>
</svg>
'''


def eye(x, y, r=4.0, slit=False):
    """Un ojo hundido que brilla: cuenca oscura, iris de luz del color de la rareza y pupila."""
    pupil = (f'<ellipse class="pu" cx="{x}" cy="{y}" rx="{r*0.18:.2f}" ry="{r*0.62:.2f}"/>' if slit
             else f'<circle class="pu" cx="{x}" cy="{y}" r="{r*0.3:.2f}"/>')
    return (f'<circle cx="{x}" cy="{y}" r="{r*1.15:.2f}" fill="#120E0B"/>'
            f'<circle class="ey" cx="{x}" cy="{y}" r="{r*1.25:.2f}"/>'
            f'<circle cx="{x}" cy="{y}" r="{r*0.78:.2f}" fill="url(#iris)"/>{pupil}'
            f'<circle cx="{x - r*0.3:.2f}" cy="{y - r*0.3:.2f}" r="{r*0.16:.2f}" fill="#FFFFFF" opacity="0.85"/>')
