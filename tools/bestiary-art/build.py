"""Compila las ilustraciones del Bestiario (v0.19.11).

    python3 tools/bestiary-art/build.py [id ...]

src/<id>.svg tiene solo el dibujo de cada criatura (hecho a mano, un archivo por
criatura). Este script lo envuelve en la carta común (frame.py), lo dibuja con Chromium
(Playwright) y guarda app/src/main/res/drawable-nodpi/beast_<id>.webp. Las criaturas sin
SVG propio pueden traer su imagen ya hecha en ai/<id>.png (ilustraciones generadas).
"""
import sys, os, re, glob, subprocess, json

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
sys.path.insert(0, HERE)
from frame import wrap, eye

CATALOG = os.path.join(ROOT, 'app/src/main/java/com/EGomez/idleecosystem/domain/logic/BeastCatalog.kt')
RES = os.path.join(ROOT, 'app/src/main/res/drawable-nodpi')
OUT = os.path.join(HERE, 'out')
RARITY = {'COMMON': 'C', 'UNCOMMON': 'U', 'RARE': 'R', 'MYTHIC': 'M', 'PRIMORDIAL': 'P'}
INFO = {m.group(1): (m.group(3), RARITY[m.group(4)], m.group(2)) for m in re.finditer(
    r'Beast\("([^"]+)", "([^"]+)", "(?:[^"\\]|\\.)*", "([^"]+)", BeastRarity\.(\w+)', open(CATALOG, encoding='utf-8').read())}
EYE = re.compile(r'<eye\s+cx="([\d.]+)"\s+cy="([\d.]+)"(?:\s+r="([\d.]+)")?(\s+slit="1")?\s*/>')
SIZE_APP = 256


def expand(body):
    return EYE.sub(lambda m: eye(float(m.group(1)), float(m.group(2)), float(m.group(3) or 4), bool(m.group(4))), body)


def seed_of(bid):
    return sum(ord(c) * (i + 1) for i, c in enumerate(bid)) % 97


def render(ids=None, size=512):
    os.makedirs(f'{OUT}/svg', exist_ok=True)
    os.makedirs(f'{OUT}/png', exist_ok=True)
    jobs = []
    for f in sorted(glob.glob(f'{HERE}/src/*.svg')):
        bid = os.path.basename(f)[:-4]
        if ids and bid not in ids:
            continue
        domain, rarity, _ = INFO[bid]
        out = f'{OUT}/svg/{bid}.svg'
        open(out, 'w', encoding='utf-8').write(wrap(expand(open(f, encoding='utf-8').read()), domain, rarity, seed_of(bid)))
        jobs.append((out, f'{OUT}/png/{bid}.png'))
    json.dump({'size': size, 'jobs': jobs}, open(f'{OUT}/jobs.json', 'w'))
    npm_root = subprocess.check_output(['npm', 'root', '-g'], text=True).strip()
    subprocess.run(['node', f'{HERE}/render_many.js', f'{OUT}/jobs.json'], check=True, env={**os.environ, 'NODE_PATH': npm_root})
    return [j[1] for j in jobs]


def export(pngs):
    """Guarda cada ilustración como WebP de 256 px en los recursos de la app."""
    from PIL import Image
    os.makedirs(RES, exist_ok=True)
    for p in pngs:
        bid = os.path.basename(p)[:-4]
        Image.open(p).convert('RGB').resize((SIZE_APP, SIZE_APP), Image.LANCZOS).save(f'{RES}/beast_{bid}.webp', 'WEBP', quality=86, method=6)


def write_index():
    """BeastArt.kt: el recurso de cada criatura que ya tiene ilustración (referencias
    directas a R.drawable para que el reductor de recursos no las quite)."""
    ids = sorted(os.path.basename(p)[6:-5] for p in glob.glob(f'{RES}/beast_*.webp'))
    lines = ['package com.EGomez.idleecosystem.ui.components', '', 'import com.EGomez.idleecosystem.R', '',
             '// Generado por tools/bestiary-art/build.py: no editar a mano.', '',
             '/** La ilustración de cada criatura del Bestiario (v0.19.11). */',
             'object BeastArt {', '    val drawables: Map<String, Int> = mapOf(']
    lines += [f'        "{i}" to R.drawable.beast_{i},' for i in ids]
    if ids:
        lines[-1] = lines[-1].rstrip(',')
    lines += ['    )', '}', '']
    open(os.path.join(ROOT, 'app/src/main/java/com/EGomez/idleecosystem/ui/components/BeastArt.kt'), 'w', encoding='utf-8').write('\n'.join(lines))


def sheet(pngs, out, cols=6, cell=200):
    from PIL import Image, ImageDraw
    rows = (len(pngs) + cols - 1) // cols
    W = Image.new('RGB', (cols * cell, rows * (cell + 16)), (10, 10, 12))
    d = ImageDraw.Draw(W)
    for i, p in enumerate(pngs):
        x, y = (i % cols) * cell, (i // cols) * (cell + 16)
        W.paste(Image.open(p).resize((cell, cell)), (x, y))
        d.text((x + 4, y + cell + 2), INFO[os.path.basename(p)[:-4]][2], fill=(220, 210, 190))
    W.save(out)


if __name__ == '__main__':
    ids = sys.argv[1:] or None
    pngs = render(ids)
    export(pngs)
    # Ilustraciones ya hechas (generadas): ai/<id>.png
    from PIL import Image
    for p in sorted(glob.glob(f'{HERE}/ai/*.png')):
        bid = os.path.basename(p)[:-4]
        if not ids or bid in ids:
            Image.open(p).convert('RGB').resize((SIZE_APP, SIZE_APP), Image.LANCZOS).save(f'{RES}/beast_{bid}.webp', 'WEBP', quality=86, method=6)
    write_index()
    sheet(pngs, f'{OUT}/sheet.png')
    print(len(pngs), 'ilustraciones')
