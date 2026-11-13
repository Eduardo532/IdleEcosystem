#!/usr/bin/env python3
"""Ilustraciones del Bestiario (v0.19.11).

Cada criatura tiene una imagen generada con la IA de Figma a partir de su descripción en
prompts.py (estilo común: grabado antiguo en tinta + carta coleccionable oscura).

  ai/<id>.png          original de 1024 px descargado de Figma (no se sube al repo)
  app/.../drawable-nodpi/beast_<id>.webp    la ilustración grande (640 px): tarjeta y visor con zoom
  app/.../drawable-nodpi/beast_t_<id>.webp  la miniatura (160 px): colección, invocación y encuentros

Desde la v0.19.12 la imagen grande del juego es la referencia: las miniaturas salen de ella, y
solo se regenera desde ai/ si el original está.

Uso:
  python3 build.py            convierte todo lo nuevo de ai/ y regenera BeastArt.kt
  python3 build.py --sheet    además deja out/sheet.png con todas las ilustraciones
"""
import glob, os, re, sys
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
RES = os.path.join(ROOT, 'app/src/main/res/drawable-nodpi')
CATALOG = os.path.join(ROOT, 'app/src/main/java/com/EGomez/idleecosystem/domain/logic/BeastCatalog.kt')
APP_SIZE, APP_Q = 640, 70
THUMB_SIZE, THUMB_Q = 160, 80


def catalog():
    s = open(CATALOG, encoding='utf-8').read()
    return [m.group(1) for m in re.finditer(r'Beast\("([^"]+)"', s)]


def convert():
    os.makedirs(RES, exist_ok=True)
    for p in sorted(glob.glob(os.path.join(HERE, 'ai', '*.png'))):
        bid = os.path.basename(p)[:-4]
        out = os.path.join(RES, f'beast_{bid}.webp')
        if not os.path.exists(out) or os.path.getmtime(out) < os.path.getmtime(p):
            Image.open(p).convert('RGB').resize((APP_SIZE, APP_SIZE), Image.LANCZOS).save(out, 'WEBP', quality=APP_Q, method=6)
    for big in sorted(glob.glob(os.path.join(RES, 'beast_*.webp'))):
        name = os.path.basename(big)
        if name.startswith('beast_t_'):
            continue
        thumb = os.path.join(RES, 'beast_t_' + name[6:])
        if not os.path.exists(thumb) or os.path.getmtime(thumb) < os.path.getmtime(big):
            Image.open(big).convert('RGB').resize((THUMB_SIZE, THUMB_SIZE), Image.LANCZOS).save(thumb, 'WEBP', quality=THUMB_Q, method=6)


def big_ids():
    return sorted(os.path.basename(p)[6:-5] for p in glob.glob(f'{RES}/beast_*.webp') if not os.path.basename(p).startswith('beast_t_'))


def write_index():
    """BeastArt.kt: el recurso de cada criatura que ya tiene ilustración (referencias
    directas a R.drawable para que el reductor de recursos no las quite)."""
    known = set(catalog())
    ids = [i for i in big_ids() if i in known]
    lines = ['package com.EGomez.idleecosystem.ui.components', '', 'import com.EGomez.idleecosystem.R', '',
             '// Generado por tools/bestiary-art/build.py: no editar a mano.', '',
             '/** La ilustración de cada criatura del Bestiario (v0.19.11), grande y en miniatura (v0.19.12). */',
             'object BeastArt {', '    /** La ilustración grande (640 px), para la tarjeta y el visor con zoom. */',
             '    val drawables: Map<String, Int> = mapOf(']
    lines += [f'        "{i}" to R.drawable.beast_{i},' for i in ids]
    if ids:
        lines[-1] = lines[-1].rstrip(',')
    lines += ['    )', '', '    /** La miniatura (160 px), para la colección, la invocación y los encuentros. */',
              '    val thumbs: Map<String, Int> = mapOf(']
    lines += [f'        "{i}" to R.drawable.beast_t_{i},' for i in ids]
    if ids:
        lines[-1] = lines[-1].rstrip(',')
    lines += ['    )', '}', '']
    open(os.path.join(ROOT, 'app/src/main/java/com/EGomez/idleecosystem/ui/components/BeastArt.kt'), 'w', encoding='utf-8').write('\n'.join(lines))
    return ids


def sheet(ids, out, cols=8, cell=160):
    rows = (len(ids) + cols - 1) // cols
    W = Image.new('RGB', (cols * cell, rows * (cell + 14)), (10, 10, 12))
    d = ImageDraw.Draw(W)
    for k, bid in enumerate(ids):
        im = Image.open(os.path.join(RES, f'beast_{bid}.webp')).resize((cell - 4, cell - 4))
        x, y = (k % cols) * cell, (k // cols) * (cell + 14)
        W.paste(im, (x + 2, y + 2))
        d.text((x + 3, y + cell), bid, fill=(200, 190, 170))
    os.makedirs(os.path.dirname(out), exist_ok=True)
    W.save(out)


if __name__ == '__main__':
    convert()
    ids = write_index()
    missing = [i for i in catalog() if i not in set(ids)]
    print(f'{len(ids)} con ilustración, {len(missing)} pendientes')
    if '--sheet' in sys.argv:
        sheet(ids, os.path.join(HERE, 'out', 'sheet.png'))
