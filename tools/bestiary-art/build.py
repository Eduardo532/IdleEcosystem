#!/usr/bin/env python3
"""Ilustraciones del Bestiario (v0.19.11).

Cada criatura tiene una imagen generada con la IA de Figma a partir de su descripción en
prompts.py (estilo común: grabado antiguo en tinta + carta coleccionable oscura).

  ai/<id>.png          original de 1024 px descargado de Figma (no se sube al repo)
  master/<id>.webp     copia maestra de 512 px que sí se guarda en el repo
  app/.../drawable-nodpi/beast_<id>.webp   lo que usa el juego (384 px)

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
MASTER_SIZE, MASTER_Q = 512, 88
APP_SIZE, APP_Q = 384, 75


def catalog():
    s = open(CATALOG, encoding='utf-8').read()
    return [m.group(1) for m in re.finditer(r'Beast\("([^"]+)"', s)]


def convert():
    os.makedirs(os.path.join(HERE, 'master'), exist_ok=True)
    os.makedirs(RES, exist_ok=True)
    for p in sorted(glob.glob(os.path.join(HERE, 'ai', '*.png'))):
        bid = os.path.basename(p)[:-4]
        m = os.path.join(HERE, 'master', bid + '.webp')
        if not os.path.exists(m) or os.path.getmtime(m) < os.path.getmtime(p):
            Image.open(p).convert('RGB').resize((MASTER_SIZE, MASTER_SIZE), Image.LANCZOS).save(m, 'WEBP', quality=MASTER_Q, method=6)
    for m in sorted(glob.glob(os.path.join(HERE, 'master', '*.webp'))):
        bid = os.path.basename(m)[:-5]
        out = os.path.join(RES, f'beast_{bid}.webp')
        if not os.path.exists(out) or os.path.getmtime(out) < os.path.getmtime(m):
            Image.open(m).convert('RGB').resize((APP_SIZE, APP_SIZE), Image.LANCZOS).save(out, 'WEBP', quality=APP_Q, method=6)


def write_index():
    """BeastArt.kt: el recurso de cada criatura que ya tiene ilustración (referencias
    directas a R.drawable para que el reductor de recursos no las quite)."""
    known = set(catalog())
    ids = sorted(i for i in (os.path.basename(p)[6:-5] for p in glob.glob(f'{RES}/beast_*.webp')) if i in known)
    lines = ['package com.EGomez.idleecosystem.ui.components', '', 'import com.EGomez.idleecosystem.R', '',
             '// Generado por tools/bestiary-art/build.py: no editar a mano.', '',
             '/** La ilustración de cada criatura del Bestiario (v0.19.11). */',
             'object BeastArt {', '    val drawables: Map<String, Int> = mapOf(']
    lines += [f'        "{i}" to R.drawable.beast_{i},' for i in ids]
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
        im = Image.open(os.path.join(HERE, 'master', bid + '.webp')).resize((cell - 4, cell - 4))
        x, y = (k % cols) * cell, (k // cols) * (cell + 14)
        W.paste(im, (x + 2, y + 2))
        d.text((x + 3, y + cell), bid, fill=(200, 190, 170))
    os.makedirs(os.path.dirname(out), exist_ok=True)
    W.save(out)


if __name__ == '__main__':
    # quita las ilustraciones viejas (SVG) que ya no tienen maestra
    masters = {os.path.basename(m)[:-5] for m in glob.glob(os.path.join(HERE, 'master', '*.webp'))}
    convert()
    masters = {os.path.basename(m)[:-5] for m in glob.glob(os.path.join(HERE, 'master', '*.webp'))}
    for f in glob.glob(f'{RES}/beast_*.webp'):
        if os.path.basename(f)[6:-5] not in masters:
            os.remove(f)
    ids = write_index()
    missing = [i for i in catalog() if i not in set(ids)]
    print(f'{len(ids)} con ilustración, {len(missing)} pendientes')
    if '--sheet' in sys.argv:
        sheet(ids, os.path.join(HERE, 'out', 'sheet.png'))
