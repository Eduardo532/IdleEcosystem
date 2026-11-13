"""Lista las criaturas que todavía no tienen ilustración (ni SVG ni imagen generada)."""
import os, re, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from build import INFO, CATALOG
lore = {m.group(1): m.group(2) for m in re.finditer(r'Beast\("([^"]+)", "[^"]+", "((?:[^"\\]|\\.)*)"', open(CATALOG, encoding='utf-8').read())}
domain = sys.argv[1] if len(sys.argv) > 1 else None
done = {f[:-4] for f in os.listdir(f'{HERE}/src')} | {f[:-4] for f in os.listdir(f'{HERE}/ai')} if os.path.isdir(f'{HERE}/ai') else {f[:-4] for f in os.listdir(f'{HERE}/src')}
left = [(b, i) for b, i in INFO.items() if b not in done and i[1] in 'CUR' and (not domain or i[0] == domain)]
for b, i in left:
    print(f'{b} | {i[1]} | {i[2]} | {lore[b]}')
print(len(left), 'pendientes')
