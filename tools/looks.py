"""The looks of the characters, drawn once: this file is the drawing board. It renders a contact sheet through
Chrome and writes the Kotlin that the app draws them from.

A look is drawn in a box of 100 by 100, in which the ball is a circle of radius 30 about (50, 68): its crown is
at (50, 38). Everything is filled shapes in two colours, the ink and white, with nothing thinner than 3.
"""
import math
import subprocess
import sys

INK = '#12110D'
BALL = (50.0, 68.0, 30.0)


def f(x):
    return ('%.2f' % x).rstrip('0').rstrip('.')


def circ(cx, cy, r, cw=True):
    s = 1 if cw else 0
    return 'M%s %s a%s %s 0 1 %d %s 0 a%s %s 0 1 %d %s 0Z' % (f(cx - r), f(cy), f(r), f(r), s, f(2 * r), f(r), f(r), s, f(-2 * r))


def ring(cx, cy, ro, ri):
    return circ(cx, cy, ro) + ' ' + circ(cx, cy, ri, cw=False)


def bar(x1, y1, x2, y2, w, cap=True):
    """A straight bar from one point to another, round at its ends."""
    dx, dy = x2 - x1, y2 - y1
    n = math.hypot(dx, dy)
    px, py = -dy / n * w / 2, dx / n * w / 2
    if not cap:
        return 'M%s %s L%s %s L%s %s L%s %sZ' % (f(x1 + px), f(y1 + py), f(x2 + px), f(y2 + py), f(x2 - px), f(y2 - py), f(x1 - px), f(y1 - py))
    r = f(w / 2)
    return 'M%s %s L%s %s A%s %s 0 0 1 %s %s L%s %s A%s %s 0 0 1 %s %sZ' % (
        f(x1 + px), f(y1 + py), f(x2 + px), f(y2 + py), r, r, f(x2 - px), f(y2 - py),
        f(x1 - px), f(y1 - py), r, r, f(x1 + px), f(y1 + py))


def rrect(x, y, w, h, r):
    return 'M%s %s h%s a%s %s 0 0 1 %s %s v%s a%s %s 0 0 1 %s %s h%s a%s %s 0 0 1 %s %s v%s a%s %s 0 0 1 %s %sZ' % (
        f(x + r), f(y), f(w - 2 * r), f(r), f(r), f(r), f(r), f(h - 2 * r), f(r), f(r), f(-r), f(r),
        f(-(w - 2 * r)), f(r), f(r), f(-r), f(-r), f(-(h - 2 * r)), f(r), f(r), f(r), f(-r))


def ellipse_points(cx, cy, rx, ry, rot, t0, t1, n=28):
    c, s = math.cos(math.radians(rot)), math.sin(math.radians(rot))
    pts = []
    for k in range(n + 1):
        t = math.radians(t0 + (t1 - t0) * k / n)
        x, y = rx * math.cos(t), ry * math.sin(t)
        pts.append((cx + x * c - y * s, cy + x * s + y * c))
    return pts


def poly(pts):
    return 'M' + ' L'.join('%s %s' % (f(x), f(y)) for x, y in pts) + 'Z'


def ell_ring(cx, cy, rx, ry, w, rot=0, t0=0, t1=360):
    """A band that follows an ellipse, [w] thick, whole or between two angles."""
    outer = ellipse_points(cx, cy, rx + w / 2, ry + w / 2, rot, t0, t1)
    inner = ellipse_points(cx, cy, rx - w / 2, ry - w / 2, rot, t0, t1)
    if t1 - t0 >= 360:
        return poly(outer) + ' ' + poly(list(reversed(inner)))
    return poly(outer + list(reversed(inner)))


def crescent(c1, r1, c2, r2):
    """What is left of a circle when another takes a bite out of it."""
    (x1, y1), (x2, y2) = c1, c2
    d = math.hypot(x2 - x1, y2 - y1)
    a = (r1 * r1 - r2 * r2 + d * d) / (2 * d)
    h = math.sqrt(r1 * r1 - a * a)
    mx, my = x1 + a * (x2 - x1) / d, y1 + a * (y2 - y1) / d
    p = (mx + h * (y2 - y1) / d, my - h * (x2 - x1) / d)
    q = (mx - h * (y2 - y1) / d, my + h * (x2 - x1) / d)
    return 'M%s %s A%s %s 0 1 0 %s %s A%s %s 0 1 1 %s %sZ' % (f(p[0]), f(p[1]), f(r1), f(r1), f(q[0]), f(q[1]), f(r2), f(r2), f(p[0]), f(p[1]))


def star4(cx, cy, r, waist=0.3):
    w = r * waist
    return 'M%s %s L%s %s L%s %s L%s %s L%s %s L%s %s L%s %s L%s %sZ' % (
        f(cx), f(cy - r), f(cx + w), f(cy - w), f(cx + r), f(cy), f(cx + w), f(cy + w),
        f(cx), f(cy + r), f(cx - w), f(cy + w), f(cx - r), f(cy), f(cx - w), f(cy - w))


def mirror(d):
    """A path as its mirror image about the ball's axis: only for paths of M, L, Q, C and Z with absolute numbers."""
    out, tokens, k = [], d.replace(',', ' ').split(), 0
    for token in tokens:
        letter = token[0] if token[0].isalpha() else ''
        number = token[1:] if letter else token
        if letter:
            assert letter in 'MLQCZ', d
            k = 0
        if number:
            out.append(letter + (f(100 - float(number)) if k % 2 == 0 else number))
            k += 1
        else:
            out.append(letter)
    return ' '.join(out)


def join(*parts):
    return ' '.join(parts)


def band_between(c, r_in, r_out, a0, a1, n=30):
    cx, cy = c
    outer = [(cx + r_out * math.cos(math.radians(a0 + (a1 - a0) * k / n)), cy + r_out * math.sin(math.radians(a0 + (a1 - a0) * k / n))) for k in range(n + 1)]
    inner = [(cx + r_in * math.cos(math.radians(a0 + (a1 - a0) * k / n)), cy + r_in * math.sin(math.radians(a0 + (a1 - a0) * k / n))) for k in range(n + 1)]
    return poly(outer + list(reversed(inner)))


QUOTE = 'M%s 17 a7.5 7.5 0 1 1 15 0 q0 11 -11 17 q4 -6 2.6 -10 a7.5 7.5 0 0 1 -6.6 -7Z'
CAT_EAR = 'M24 52 Q20 30 26 13 Q41 23 49 41Z'
CAT_INNER = 'M30 43 Q28.5 31 31 23 Q38 28 42.5 37Z'
HORN = 'M31 48 Q11 37 20 11 Q26 28 42 39Z'
DOG_EAR = 'M35 42 Q16 33 8 51 Q5 68 17 73 Q27 63 28 50Z'

# name: (colour for the sheet, {layer: path})
# Layers, bottom to top: behind (ink, under the ball), ink (over the ball, with a rim of the ground round it),
# white, dot (ink again, over the white). plain: nothing worn, and the ball takes the round. lift: moved up by.
LOOKS = {
    'neutral': ('#F2EEE3', {'plain': True}),
    'sarcastic': ('#FFDD33', {'ink': join(QUOTE % '29', QUOTE % '55')}),
    'mystical': ('#C9B6FF', {'ink': join(crescent((44, 22), 14.5, (52, 17.5), 12), star4(71, 13, 7.5))}),
    'grandma': ('#FFC2D1', {
        'ink': join(circ(47, 27, 13.5), bar(50, 30, 71, 7, 3.6), circ(71.5, 6.5, 3.8), bar(57, 35, 79, 13, 3.6), circ(79.5, 12.5, 3.8)),
        'white': join('M36.5 23.5 Q47 15.5 57.5 23.5 L57.5 27.2 Q47 19.2 36.5 27.2Z', 'M35.5 31 Q47 22.5 58.5 31 L58.5 34.7 Q47 26.2 35.5 34.7Z'),
    }),
    'pirate': ('#5FD6C9', {
        'ink': 'M11 42 Q17 31 30 32 Q33 10 50 8 Q67 10 70 32 Q83 31 89 42 Q69 35.5 50 35.5 Q31 35.5 11 42Z',
        'white': join(bar(41, 27.5, 59, 17.5, 3), bar(41, 17.5, 59, 27.5, 3), circ(50, 20.5, 5.6), rrect(46.6, 23.5, 6.8, 5.4, 1.6)),
        'dot': join(circ(47.9, 20.4, 1.3), circ(52.1, 20.4, 1.3)),
    }),
    'philosopher': ('#E3D5B8', {
        'ink': join(rrect(27, 5, 46, 23, 11.5), circ(40, 33.5, 4), circ(45.5, 40, 2.6)),
        'white': join(circ(39.5, 16.5, 3), circ(50, 16.5, 3), circ(60.5, 16.5, 3)),
    }),
    'coach': ('#FF8A3D', {
        'ink': join('M29 46 Q27 17 51 17 Q74 17 72 46 Q51 38 29 46Z', 'M34 36 Q18 37 7 43 Q4 49.5 12 50 Q24 46.5 37 45.5Z', circ(51, 16.5, 3.2)),
        'white': 'M37 33.5 Q52 29 69.5 34.5 L70.2 39.4 Q52 33.4 36 38.4Z',
    }),
    'lovestruck': ('#FF7A9C', {'ink': 'M50 37 C27 22 33 3 50 14 C67 3 73 22 50 37Z'}),
    'drama': ('#FF5A4F', {
        'ink': join('M29 45 L24 18 L39 30 L50 10 L61 30 L76 18 L71 45 Q50 37.5 29 45Z', circ(24, 15.5, 4.4), circ(50, 8.5, 4.4), circ(76, 15.5, 4.4)),
        'white': join(circ(50, 32, 3.6), circ(36.5, 37.5, 2.4), circ(63.5, 37.5, 2.4)),
    }),
    'robot': ('#B8C4D0', {
        'behind': join(rrect(10, 57, 11, 22, 4.5), rrect(79, 57, 11, 22, 4.5)),
        'ink': join(bar(50, 41, 50, 20, 4.4, cap=False), circ(50, 15, 7)),
        'white': circ(50, 15, 2.8),
    }),
    'detective': ('#D6C68A', {
        'ink': join('M15 42 Q50 29 85 42 Q87.5 46.5 82.5 47 Q50 37 17.5 47 Q12.5 46.5 15 42Z', 'M30 40 Q27.5 15 40 13 Q50 18.5 60 13 Q72.5 15 70 40 Q50 34 30 40Z'),
        'white': 'M30.2 29.5 Q50 25.5 69.8 29.5 L70 35.4 Q50 31.2 30 35.4Z',
    }),
    'conspiracy': ('#9BE564', {
        'ink': 'M29 33 Q24.5 33 27.2 28.8 L47.2 4.6 Q50 1.6 52.8 4.6 L72.8 28.8 Q75.5 33 71 33Z',
        'white': 'M37.5 23.5 Q50 12.5 62.5 23.5 Q50 32.5 37.5 23.5Z',
        'dot': circ(50, 23, 3.8),
    }),
    'cat': ('#FFB86B', {
        'ink': join(CAT_EAR, mirror(CAT_EAR)),
        'white': join(CAT_INNER, mirror(CAT_INNER)),
    }),
    'zoomer': ('#D4FF4F', {
        'ink': join(band_between((50, 68), 34.5, 39.5, 200, 340), rrect(6, 56, 14, 25, 5.5), rrect(80, 56, 14, 25, 5.5)),
    }),
    'villain': ('#B58CFF', {'ink': join(HORN, mirror(HORN))}),
    'poet': ('#BFD9FF', {
        'ink': 'M53 44 C45.5 38.5 42.5 32 43.5 25.5 L50.5 28.5 L46.5 19.5 C51 11 60.5 5 73 2 C80 12.5 80.5 21 76.5 27.5 L69 25.5 L72 33.5 C68 39 61 42.5 53 44Z',
        'white': bar(55, 41, 70.5, 9, 2.8),
    }),
    'astro': ('#8FA8FF', {
        'ink': join(circ(50, 21, 11.5), ell_ring(50, 21, 22, 6.2, 4, rot=-18)),
        'white': ell_ring(50, 21, 22, 6.2, 3.4, rot=-18, t0=56, t1=124),
    }),
    'therapist': ('#BFE8D2', {
        'ink': join(ring(37.5, 31, 11.5, 7.3), ring(62.5, 31, 11.5, 7.3), bar(47, 29.5, 53, 29.5, 3.4, cap=False), bar(27, 28.5, 19.5, 25, 3.4), bar(73, 28.5, 80.5, 25, 3.4)),
    }),
    'dog': ('#F0B878', {'ink': join(DOG_EAR, mirror(DOG_EAR))}),
    # New ones.
    'singer': ('#FFC2D1', {
        'ink': join(circ(64, 16.5, 11), 'M54.5 20.5 L60.5 26.5 L41.5 44 L37 39.5Z'),
        'white': join(bar(55.5, 12.5, 71.5, 22, 2.8), bar(60.5, 7.8, 74.5, 16, 2.6)),
    }),
    'rockstar': ('#B58CFF', {'ink': 'M58 2 L32 27 L47 27 L39 44 L68 17 L53 17Z'}),
    'chef': ('#F2EEE3', {
        'ink': join(circ(37, 19, 11), circ(50, 14, 12.5), circ(63, 19, 11), rrect(33, 22, 34, 20, 3)),
        'white': rrect(35.5, 33.5, 29, 4.4, 2.2),
    }),
    'wizard': ('#8FA8FF', {
        'ink': join('M31 41 L48.5 6 Q52 1.5 57 5.5 L53.5 12 L69 41Z', 'M16 41 Q50 30 84 41 Q86.5 46 81.5 46.5 Q50 37 18.5 46.5 Q13.5 46 16 41Z'),
        'white': star4(50, 26, 6.5),
    }),
    'cowboy': ('#F0B878', {
        'ink': join('M33 39 Q30.5 15 41 13 Q50 19.5 59 13 Q69.5 15 67 39Z',
                    'M8.5 33 Q13 45.5 30 43 Q50 39 70 43 Q87 45.5 91.5 33 Q92.5 29.5 89 30 Q80 37.5 68 36 Q50 33 32 36 Q20 37.5 11 30 Q7.5 29.5 8.5 33Z'),
        'white': 'M33 30 Q50 27 67 30 L67 35 Q50 32 33 35Z',
    }),
    'magician': ('#B8C4D0', {
        'ink': join('M35.5 39 L33 9.5 Q33 6 36.5 6 L63.5 6 Q67 6 67 9.5 L64.5 39Z', rrect(20, 36, 60, 7.2, 3.6)),
        'white': 'M34.6 27 L65.4 27 L64.9 32.6 L35.1 32.6Z',
    }),
    'ninja': ('#FF5A4F', {
        'lift': -9,
        'white': 'M23.5 53.5 Q50 42.5 76.5 53.5 L78.6 59.5 Q50 48.5 21.4 59.5Z',
        'ink': join(circ(79, 56.5, 4.4), 'M79 53.5 Q88 46 92 35 L97 38.5 Q92 51 82 58.5Z', 'M80.5 57 Q91 57 99 51 L100 57 Q91 63.5 81 61.5Z'),
    }),
    'angel': ('#BFD9FF', {'ink': ell_ring(50, 21, 21, 8, 5.6)}),
    'graduate': ('#BFE8D2', {
        'ink': join('M31.5 30 L31.5 41 Q50 47 68.5 41 L68.5 30 L50 38Z', 'M50 9 L86 24 L50 39 L14 24Z', bar(79, 26, 79, 41, 3), rrect(76, 39, 6, 8.5, 2)),
        'white': join(circ(50, 24, 3), bar(50, 24, 79, 25.5, 2.4)),
    }),
    'flower': ('#FFB86B', {
        'ink': join(*[circ(50 + 10.5 * math.cos(math.radians(a)), 21 + 10.5 * math.sin(math.radians(a)), 7) for a in range(-90, 270, 60)], bar(50, 30, 50, 41, 3.6, cap=False)),
        'white': circ(50, 21, 5.6),
    }),
    'party': ('#D4FF4F', {
        'ink': join('M33 42 L48.5 11 Q50 8.5 51.5 11 L67 42 Q50 36.5 33 42Z', circ(50, 8, 6)),
        'white': join('M43.4 21 L56.6 21 L58.9 25.6 L41.1 25.6Z', 'M38.6 30.6 L61.4 30.6 L63.6 35.2 L36.4 35.2Z'),
    }),
}

ORIGINALS = ['neutral', 'sarcastic', 'mystical', 'grandma', 'pirate', 'philosopher', 'coach', 'lovestruck', 'drama', 'robot',
             'detective', 'conspiracy', 'cat', 'zoomer', 'villain', 'poet', 'astro', 'therapist', 'dog']


def eight(cx, cy, k=1.0):
    return join(ring(cx, cy - 3.7 * k, 3.7 * k, 1.5 * k), ring(cx, cy + 3.6 * k, 4.5 * k, 2.0 * k))


def lerp_hex(a, b, t):
    pa = [int(a[i:i + 2], 16) for i in (1, 3, 5)]
    pb = [int(b[i:i + 2], 16) for i in (1, 3, 5)]
    return '#%02X%02X%02X' % tuple(round(x + (y - x) * t) for x, y in zip(pa, pb))


def svg(name, size, ground, uid):
    colour, layers = LOOKS[name]
    out = ['<svg viewBox="0 0 100 100" width="%d" height="%d"><defs><clipPath id="c%s"><circle cx="50" cy="50" r="50"/></clipPath></defs><g clip-path="url(#c%s)">' % (size, size, uid, uid)]
    out.append('<rect width="100" height="100" fill="%s"/>' % ground)
    if layers.get('plain'):
        out.append('<circle cx="50" cy="52" r="35" fill="%s"/>' % INK)
        out.append('<ellipse cx="50" cy="49" rx="15" ry="16.5" fill="#fff"/>')
        out.append('<path d="%s" fill="%s"/>' % (eight(50, 49, 1.55), INK))
    else:
        out.append('<g transform="translate(0 %s)">' % layers.get('lift', 0))
        if 'behind' in layers:
            out.append('<path d="%s" fill="%s"/>' % (layers['behind'], INK))
        out.append('<circle cx="50" cy="68" r="30" fill="%s"/>' % INK)
        out.append('<ellipse cx="50" cy="66.5" rx="10.5" ry="11.5" fill="#fff"/>')
        out.append('<path d="%s" fill="%s"/>' % (eight(50, 66.5), INK))
        if 'ink' in layers:
            out.append('<path d="%s" fill="%s" stroke="%s" stroke-width="3.4" stroke-linejoin="round" paint-order="stroke"/>' % (layers['ink'], INK, ground))
        if 'white' in layers:
            out.append('<path d="%s" fill="#fff"/>' % layers['white'])
        if 'dot' in layers:
            out.append('<path d="%s" fill="%s"/>' % (layers['dot'], INK))
        out.append('</g>')
    out.append('</g></svg>')
    return ''.join(out)


def sheet(path, names, dark=True, muting=0.7):
    page = '#0E141B' if dark else '#F1F3F7'
    card = '#1B222B' if dark else '#FFFFFF'
    neutral = '#AAB2BD' if dark else '#C6CCD4'
    text = '#E8ECF1' if dark else '#12110D'
    cells = []
    for k, name in enumerate(names):
        colour = LOOKS[name][0]
        quiet = lerp_hex(colour, neutral, muting)
        loud = lerp_hex(colour, neutral, 0.3)
        cells.append(
            '<div class="cell"><div class="big">%s</div><div class="row">%s%s%s</div><div class="name">%s</div></div>' % (
                svg(name, 150, loud, 'a%d' % k), svg(name, 40, quiet, 'b%d' % k), svg(name, 40, loud, 'c%d' % k), svg(name, 88, loud, 'd%d' % k), name))
    html = '''<html><body style="margin:0;background:%s;font-family:Helvetica;color:%s">
<style>.grid{display:grid;grid-template-columns:repeat(6,1fr);gap:14px;padding:18px}.cell{background:%s;border-radius:18px;padding:12px;text-align:center}
.row{display:flex;gap:8px;justify-content:center;align-items:center;margin-top:8px}.name{font-size:13px;margin-top:6px}svg{display:block}.big{display:flex;justify-content:center}</style>
<div class="grid">%s</div></body></html>''' % (page, text, card, ''.join(cells))
    open(path + '.html', 'w').write(html)
    rows = (len(names) + 5) // 6
    subprocess.run(['/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', '--headless=new', '--disable-gpu', '--hide-scrollbars',
                    '--force-device-scale-factor=1', '--screenshot=' + path + '.png', '--window-size=%d,%d' % (1320, 36 + rows * 322), 'file://' + path + '.html'],
                   capture_output=True)


if __name__ == '__main__' and not (len(sys.argv) > 1 and sys.argv[1] == 'kotlin'):
    here = __file__.rsplit('/', 1)[0]
    which = sys.argv[1] if len(sys.argv) > 1 else 'all'
    names = list(LOOKS)
    if which == 'old':
        names = ORIGINALS
    elif which == 'new':
        names = [n for n in LOOKS if n not in ORIGINALS]
    elif which != 'all':
        names = which.split(',')
    sheet(here + '/sheet-' + which.replace(',', '-')[:40], names, dark='light' not in sys.argv)
    print('ok', len(names))


def kotlin():
    """The drawings as the app reads them."""
    out = ['package com.lorenzosuarez.magiceightball.phone.ui', '', 'import com.example.magiceightball.core.domain.model.CharacterLook', '',
           '/**',
           ' * What each look is drawn from: paths in a box of 100 by 100 in which the ball is a circle of',
           ' * radius 30 about (50, 68), so that its crown is at (50, 38). See [LookArt] for the layers.',
           ' *',
           ' * Drawn on one board to one set of rules, so that they read as one family: filled shapes in two',
           ' * colours, nothing thinner than 3 of those 100, each about as heavy as the next. The numbers',
           ' * come from that board, `tools/looks.py`: a look is corrected there and written out again, not edited here.',
           ' */',
           'internal fun lookArt(look: CharacterLook): LookArt? = when (look) {']
    for name, (_, layers) in LOOKS.items():
        if layers.get('plain'):
            out.append('    CharacterLook.%s -> null' % name.upper())
            continue
        out.append('    CharacterLook.%s -> LookArt(' % name.upper())
        args = []
        for key in ('behind', 'ink', 'white', 'dot'):
            if key in layers:
                args.append('        %s = "%s"' % (key, layers[key]))
        if 'lift' in layers:
            args.append('        lift = %sf' % f(layers['lift']))
        out.append(',\n'.join(args))
        out.append('    )')
    out.append('}')
    out.append('')
    out.append('/** The number on the ball, under what it wears and on a ball that wears nothing, as two rings each. */')
    out.append('internal const val LOOK_EIGHT = "%s"' % eight(50, 66.5))
    out.append('internal const val LOOK_EIGHT_PLAIN = "%s"' % eight(50, 49, 1.55))
    out.append('')
    return '\n'.join(out)


if __name__ == '__main__' and len(sys.argv) > 1 and sys.argv[1] == 'kotlin':
    open(sys.argv[2], 'w').write(kotlin())
    print('written', len(kotlin()))
