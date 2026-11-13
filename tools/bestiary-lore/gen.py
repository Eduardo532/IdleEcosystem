#!/usr/bin/env python3
"""Genera la historia de cada criatura del Bestiario (v0.19.12).

lore_<dominio>.py tiene, para cada criatura, su historia en español y en inglés. Este script
escribe BeastLore.kt (el texto en español, por id) y BeastLoreTranslations.kt (español -> inglés).

Uso: python3 tools/bestiary-lore/gen.py
"""
import importlib, os, re, sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
JAVA = os.path.join(ROOT, 'app/src/main/java/com/EGomez/idleecosystem')
sys.path.insert(0, HERE)
DOMAINS = ['deep', 'under', 'sky', 'cosmos', 'dream', 'eras', 'outer', 'arcane']


def k(s):
    return '"' + s.replace('\\', '\\\\').replace('"', '\\"').replace('$', '\\$') + '"'


def main():
    lore = {}
    for d in DOMAINS:
        lore.update(importlib.import_module('lore_' + d).LORE)
    catalog = re.findall(r'Beast\("([^"]+)"', open(os.path.join(JAVA, 'domain/logic/BeastCatalog.kt'), encoding='utf-8').read())
    missing = [i for i in catalog if i not in lore]
    extra = [i for i in lore if i not in catalog]
    if missing or extra:
        sys.exit(f'faltan: {missing} sobran: {extra}')
    out = ['package com.EGomez.idleecosystem.domain.logic', '',
           '// Generado por tools/bestiary-lore/gen.py: no editar a mano.', '',
           '/** La historia de cada criatura del Bestiario (v0.19.12), en español; la traducción va en BeastLoreTranslations. */',
           'object BeastLore {', '    private val histories: Map<String, String> = mapOf(']
    out += [f'        {k(i)} to {k(lore[i][0])},' for i in catalog]
    out[-1] = out[-1].rstrip(',')
    out += ['    )', '', '    /** La historia de la criatura [id], o null si no tiene. */',
            '    fun history(id: String): String? = histories[id]', '}', '']
    open(os.path.join(JAVA, 'domain/logic/BeastLore.kt'), 'w', encoding='utf-8').write('\n'.join(out))
    tr = ['package com.EGomez.idleecosystem.ui.i18n', '',
          '// Generado por tools/bestiary-lore/gen.py: no editar a mano.', '',
          '/** La historia de cada criatura del Bestiario, en inglés (v0.19.12). */',
          'internal val BEAST_LORE_EN: Map<String, String> = mapOf(']
    tr += [f'    {k(lore[i][0])} to {k(lore[i][1])},' for i in catalog]
    tr[-1] = tr[-1].rstrip(',')
    tr += [')', '']
    open(os.path.join(JAVA, 'ui/i18n/BeastLoreTranslations.kt'), 'w', encoding='utf-8').write('\n'.join(tr))
    words = sum(len(v[0].split()) for v in lore.values())
    print(f'{len(catalog)} historias, {words} palabras en español')


if __name__ == '__main__':
    main()
