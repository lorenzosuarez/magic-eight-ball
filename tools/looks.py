"""The looks of the characters, drawn once: this file is the drawing board.

    python3 tools/looks.py sheet out.png [light] [names,...]   a contact sheet, rendered through Chrome
    python3 tools/looks.py kotlin                              writes LookArtData.kt, which the app draws from

A look is drawn on a ball that stands upright in a box of 100 by 100: a circle of radius 28 about (50, 66), with
its crown at (50, 38). That is the board; it is never shown so. What is shown is the figure: the ball large and
tipped towards the words, coming into the round from its lower corner, its white disc off to one side as a face,
and what it wears seated on the tipped crown, a size down. See FIGURE below, and `drawFigure` in the app.

The rules, so that thirty looks read as one family:
- filled shapes in two colours, the ink and white, each shape a path of its own (they are drawn one by one, so
  two that overlap never cut holes in each other; a ring is one path with its hole);
- nothing white thinner than 4.5, which is 2dp at the size of a row's face;
- the ball runs off the round at its lower corner, and what stands beside the ball may run off with it; what is
  worn on the crown is whole, and no nearer than 4 to the round's edge;
- what is worn overlaps the crown by 5 or more, or floats 3 or more over it: never a touch;
- ears and horns grow from behind the ball; hats and what is held sit over it, with a rim of the ground round them.
"""
import math
import subprocess
import sys

INK = '#12110D'
BX, BY, BR = 50.0, 66.0, 28.0
RIM = 5.0

# The figure, in a round of 100 across: where the ball's middle is, how large the ball is, how far it is tipped,
# and how large what it wears is drawn beside the board, about the crown. The white disc is off the ball's middle
# by these parts of its radius, and is so wide and so tall.
FIGURE = dict(x=64.0, y=78.0, r=36.0, tilt=-18.0, worn=0.8)
# A ball that wears nothing has the round to itself: larger, and further in.
BARE = dict(x=59.0, y=73.0, r=41.0)
FACE = dict(x=-0.23, y=-0.18, wide=0.37, tall=0.42)


def f(x):
    return ('%.2f' % x).rstrip('0').rstrip('.')


def circ(cx, cy, r, cw=True):
    s = 1 if cw else 0
    return 'M%s %s a%s %s 0 1 %d %s 0 a%s %s 0 1 %d %s 0Z' % (f(cx - r), f(cy), f(r), f(r), s, f(2 * r), f(r), f(r), s, f(-2 * r))


def ring(cx, cy, ro, ri):
    return circ(cx, cy, ro) + ' ' + circ(cx, cy, ri, cw=False)


def bar(x1, y1, x2, y2, w, cap=True):
    """A straight bar from one point to another, round at its ends unless told not to be."""
    dx, dy = x2 - x1, y2 - y1
    n = math.hypot(dx, dy)
    px, py = dy / n * w / 2, -dx / n * w / 2
    a, b, c, d = (x1 + px, y1 + py), (x2 + px, y2 + py), (x2 - px, y2 - py), (x1 - px, y1 - py)
    if not cap:
        return poly([a, b, c, d])
    r = f(w / 2)
    return 'M%s %s L%s %s A%s %s 0 0 1 %s %s L%s %s A%s %s 0 0 1 %s %sZ' % (
        f(a[0]), f(a[1]), f(b[0]), f(b[1]), r, r, f(c[0]), f(c[1]), f(d[0]), f(d[1]), r, r, f(a[0]), f(a[1]))


def rrect(x, y, w, h, r):
    return 'M%s %s h%s a%s %s 0 0 1 %s %s v%s a%s %s 0 0 1 %s %s h%s a%s %s 0 0 1 %s %s v%s a%s %s 0 0 1 %s %sZ' % (
        f(x + r), f(y), f(w - 2 * r), f(r), f(r), f(r), f(r), f(h - 2 * r), f(r), f(r), f(-r), f(r),
        f(-(w - 2 * r)), f(r), f(r), f(-r), f(-r), f(-(h - 2 * r)), f(r), f(r), f(r), f(-r))


def poly(pts):
    return 'M' + ' L'.join('%s %s' % (f(x), f(y)) for x, y in pts) + 'Z'


def ellipse_points(cx, cy, rx, ry, rot, t0, t1, n):
    c, s = math.cos(math.radians(rot)), math.sin(math.radians(rot))
    pts = []
    for k in range(n + 1):
        t = math.radians(t0 + (t1 - t0) * k / n)
        x, y = rx * math.cos(t), ry * math.sin(t)
        pts.append((cx + x * c - y * s, cy + x * s + y * c))
    return pts


def ell_ring(cx, cy, rx, ry, w, rot=0, t0=0, t1=360, n=72):
    """A band that follows an ellipse, [w] thick, whole or between two angles."""
    outer = ellipse_points(cx, cy, rx + w / 2, ry + w / 2, rot, t0, t1, n)
    inner = ellipse_points(cx, cy, rx - w / 2, ry - w / 2, rot, t0, t1, n)
    if t1 - t0 >= 360:
        return poly(outer[:-1]) + ' ' + poly(list(reversed(inner[:-1])))
    return poly(outer + list(reversed(inner)))


def arc_band(cx, cy, r_in, r_out, a0, a1, n=48):
    return ell_ring(cx, cy, (r_in + r_out) / 2, (r_in + r_out) / 2, r_out - r_in, 0, a0, a1, n)


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


def star4(cx, cy, r, waist=0.36):
    w = r * waist
    return poly([(cx, cy - r), (cx + w, cy - w), (cx + r, cy), (cx + w, cy + w), (cx, cy + r), (cx - w, cy + w), (cx - r, cy), (cx - w, cy - w)])


def mirror(d):
    """A path as its mirror image about the ball's axis: only for paths of M, L, Q, C and Z with absolute numbers."""
    out, k = [], 0
    for token in d.replace(',', ' ').split():
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


def both(d):
    return [d, mirror(d)]


def quill(nib, tip, widest=7.4, shaft=0.24):
    """A feather to write with: a bare shaft from its nib, then the vane, notched on one edge, to its tip."""
    (nx, ny), (tx, ty) = nib, tip
    dx, dy = tx - nx, ty - ny
    n = math.hypot(dx, dy)
    ux, uy = dx / n, dy / n
    px, py = -uy, ux

    def width(u):
        return widest * math.sin(math.pi * min(1.0, u * 0.92 + 0.08)) ** 0.7

    def notch(u):
        for centre in (0.36, 0.6):
            gap = abs(u - centre)
            if gap < 0.05:
                return 0.25 + 0.75 * gap / 0.05
        return 1.0

    left, right = [], []
    steps = 64
    for k in range(steps + 1):
        u = k / steps
        along = n * (shaft + (1 - shaft) * u)
        cx, cy = nx + ux * along, ny + uy * along
        left.append((cx + px * width(u), cy + py * width(u)))
        right.append((cx - px * width(u) * notch(u), cy - py * width(u) * notch(u)))
    start = (nx + ux * n * shaft, ny + uy * n * shaft)
    return [poly(left + list(reversed(right))), bar(nx, ny, start[0] + ux * 3, start[1] + uy * 3, 3.6)]


def taper(x1, y1, w1, x2, y2, w2):
    """A handle: wider at one end than at the other."""
    dx, dy = x2 - x1, y2 - y1
    n = math.hypot(dx, dy)
    px, py = dy / n, -dx / n
    return poly([(x1 + px * w1 / 2, y1 + py * w1 / 2), (x2 + px * w2 / 2, y2 + py * w2 / 2), (x2 - px * w2 / 2, y2 - py * w2 / 2), (x1 - px * w1 / 2, y1 - py * w1 / 2)])


QUOTE = 'M%s 16 a8.2 8.2 0 1 1 16.4 0 q0 12 -12 18.6 q4.4 -6.6 2.8 -11 a8.2 8.2 0 0 1 -7.2 -7.6Z'
CLOUD = [(37, 20, 9), (47, 14.5, 10), (58.5, 15.5, 9.5), (64.5, 22.5, 8.5), (53, 24, 9.5), (43, 25, 8.5)]
PETALS = [(59 + 9.6 * math.cos(math.radians(a)), 29 + 9.6 * math.sin(math.radians(a))) for a in range(-90, 270, 60)]

# name: (colour for the sheet, {layer: [paths]})
# Layers, bottom to top: behind (ink, under the ball), ink (over the ball, with a rim of the ground round it),
# white, dot (ink again, over the white). plain: nothing worn, and the ball takes the round. lift: moved up by.
LOOKS = {
    'neutral': ('#F2EEE3', {}),
    'sarcastic': ('#FFDD33', {'ink': [QUOTE % '27', QUOTE % '55.6']}),
    'mystical': ('#C9B6FF', {'ink': [crescent((44, 19.5), 14.5, (52, 15), 12), star4(69, 17, 8, 0.38)]}),
    'grandma': ('#FFC2D1', {
        # A ball of wool with the needles in it: she knits.
        'ink': [bar(50, 30, 70.5, 9.5, 4), circ(71.5, 8.5, 4), bar(57, 35, 77.5, 15.5, 4), circ(78.5, 14.5, 4), circ(46, 28, 13.5)],
        'white': ['M34.5 24.5 Q46 16 57.5 24.5 L57.5 29.1 Q46 20.6 34.5 29.1Z', 'M35.5 32 Q46 24.5 56.5 32 L55.5 36.4 Q46 29.2 36.5 36.4Z'],
    }),
    'pirate': ('#5FD6C9', {
        'ink': ['M9 35 Q14 41 26 39.5 Q50 34 74 39.5 Q86 41 91 35 Q85 33 77 24 Q65 8 50 7 Q35 8 23 24 Q15 33 9 35Z'],
        'white': [circ(50, 20, 7.2), rrect(45.6, 24, 8.8, 6.5, 2.2)],
        'dot': [circ(47.1, 20, 2.1), circ(52.9, 20, 2.1)],
    }),
    'philosopher': ('#E3D5B8', {
        'ink': [circ(x, y, r) for x, y, r in CLOUD] + [circ(33.5, 37, 3.6)],
        'white': [circ(41, 19.5, 3), circ(51, 19.5, 3), circ(61, 19.5, 3)],
    }),
    'coach': ('#FF8A3D', {
        'ink': ['M29 46 Q27 17 51 17 Q74 17 72 46 Q51 38 29 46Z', 'M34 36 Q18 37 8 43 Q5 49 12 50 Q24 46.5 37 45.5Z', circ(51, 16.5, 3.2)],
        'white': ['M37 33.5 Q52 29 69.5 34.5 L70.2 39.4 Q52 33.4 36 38.4Z'],
    }),
    'lovestruck': ('#FF7A9C', {'ink': ['M50 43 C22 25 30 2 50 15.5 C70 2 78 25 50 43Z']}),
    'drama': ('#FF5A4F', {
        'ink': ['M30 45 L26 19.5 L39.5 30 L50 11 L60.5 30 L74 19.5 L70 45 Q50 37.5 30 45Z', circ(26, 17.5, 4), circ(50, 9.5, 4.2), circ(74, 17.5, 4)],
        'white': [circ(50, 32, 3.6), circ(37, 37.5, 2.6), circ(63, 37.5, 2.6)],
    }),
    'robot': ('#B8C4D0', {
        'beside': [rrect(13, 56, 10.5, 20, 4.5), rrect(76.5, 56, 10.5, 20, 4.5)],
        'ink': [bar(50, 41, 50, 21, 5.5, cap=False), circ(50, 15.5, 7.5)],
        'white': [circ(50, 15.5, 3.2)],
    }),
    'detective': ('#D6C68A', {
        'ink': ['M18 42 Q50 30 82 42 Q84.5 46.5 79.5 47 Q50 37.5 20.5 47 Q15.5 46.5 18 42Z', 'M30 40 Q27.5 15 40 13 Q50 18.5 60 13 Q72.5 15 70 40 Q50 34 30 40Z'],
        'white': ['M30.2 29.5 Q50 25.5 69.8 29.5 L70 35.4 Q50 31.2 30 35.4Z'],
    }),
    'conspiracy': ('#9BE564', {
        'ink': ['M30.7 33 Q26.5 33 29 29.1 L47.4 6.9 Q50 4.1 52.6 6.9 L71 29.1 Q73.5 33 69.3 33Z'],
        'white': ['M38.5 24.3 Q50 14.1 61.5 24.3 Q50 32.5 38.5 24.3Z'],
        'dot': [circ(50, 23.8, 3.6)],
    }),
    'cat': ('#FFB86B', {
        'behind': both('M22 56 L28.5 16.5 Q43 26 50 44Z'),
        'white': both('M30 37 L32.3 25.5 Q38.5 29.5 42 36Z'),
    }),
    'zoomer': ('#D4FF4F', {
        'fitted': True,
        'ink': [arc_band(50, 66, 31.5, 36.3, 195, 345), rrect(10, 54, 12.5, 22, 5.5), rrect(77.5, 54, 12.5, 22, 5.5)],
    }),
    'villain': ('#B58CFF', {'behind': both('M23 54 Q7 31 33 9 Q21 30 35 45Z')}),
    'poet': ('#BFD9FF', {'ink': quill((45, 41), (69.5, 9.5))}),
    'astro': ('#8FA8FF', {
        'ink': [circ(50, 21, 13), ell_ring(50, 21, 24, 6.8, 5, rot=-18)],
        'white': [ell_ring(50, 21, 24, 6.8, 4.6, rot=-18, t0=57, t1=123, n=24)],
    }),
    'therapist': ('#BFE8D2', {
        'ink': [ring(37, 31, 12.5, 7), ring(63, 31, 12.5, 7), bar(47.5, 29.5, 52.5, 29.5, 4.5, cap=False), bar(25.5, 28.5, 18.5, 25.5, 4.5), bar(74.5, 28.5, 81.5, 25.5, 4.5)],
    }),
    'dog': ('#F0B878', {
        'behind': both('M37 42 Q29 33 21 38 Q10 48 13 63 Q15 70 21 68 Q28 59 31 48Z'),
    }),
    # No character of the app's wears these: they are there to be picked.
    'singer': ('#FFC2D1', {
        'ink': [circ(62, 19, 10), taper(53.9, 28.1, 9, 43.1, 40.3, 6.5)],
        'white': [bar(56.4, 14, 67.6, 24, 4.6, cap=False)],
    }),
    'rockstar': ('#B58CFF', {'ink': ['M58 6 L33 29 L47 29 L40.5 45 L67.5 20 L53.5 20Z']}),
    'chef': ('#F2EEE3', {
        'ink': [circ(38.6, 21.6, 9.7), circ(50, 17.2, 11), circ(61.4, 21.6, 9.7), rrect(35, 24.3, 30, 17.6, 2.6)],
        'white': [rrect(37.2, 34.2, 25.6, 5, 2.5)],
    }),
    'wizard': ('#8FA8FF', {
        'ink': ['M31 41 L48.5 9 Q52 4.5 57 8.5 L53.5 15 L69 41Z', 'M16 41 Q50 30 84 41 Q86.5 46 81.5 46.5 Q50 37 18.5 46.5 Q13.5 46 16 41Z'],
        'white': [star4(50, 28, 7)],
    }),
    'cowboy': ('#F0B878', {
        'ink': ['M33.5 39 Q30 11 41 9.5 Q50 16 59 9.5 Q70 11 66.5 39Z',
                'M11.5 31 Q10.5 25 14 25.5 Q19 37 33 36 Q50 33 67 36 Q81 37 86 25.5 Q89.5 25 88.5 31 Q84 45.5 69 43.5 Q50 39.5 31 43.5 Q16 45.5 11.5 31Z'],
    }),
    'magician': ('#B8C4D0', {
        'ink': ['M35.5 39 L33 9.5 Q33 6 36.5 6 L63.5 6 Q67 6 67 9.5 L64.5 39Z', rrect(20, 36, 60, 7.2, 3.6)],
        'white': ['M34.6 26.5 L65.4 26.5 L64.9 32.6 L35.1 32.6Z'],
    }),
    'ninja': ('#FF5A4F', {
        # Tied on the side that is in sight, its ends blown up and back.
        'fitted': True,
        'white': ['M29.5 47 Q50 38 70.5 47 L74.6 53.5 Q50 44 25.4 53.5Z'],
        'ink': [circ(24, 50, 4.6), 'M24 47.5 Q17 43 14 34.5 L9.8 37 Q12.5 47.5 21 52.5Z', 'M25.5 47 Q19.5 39 21.5 28.5 L26.3 30 Q25.5 39 29.5 44.5Z'],
    }),
    'angel': ('#BFD9FF', {'ink': [ell_ring(50, 21, 21, 8, 5.6)]}),
    'graduate': ('#BFE8D2', {
        'ink': ['M31.5 30 L31.5 41 Q50 47 68.5 41 L68.5 30 L50 38Z', 'M50 9 L86 24 L50 39 L14 24Z', bar(77.5, 25.5, 77.5, 40, 3.6), rrect(74.3, 38.5, 6.4, 9, 2)],
        'white': [circ(50, 24, 3.6), bar(50, 24, 76, 24.8, 3.6)],
    }),
    'flower': ('#FFB86B', {
        'ink': [circ(x, y, 6.4) for x, y in PETALS] + [circ(59, 29, 6)],
        'white': [circ(59, 29, 5.2)],
    }),
    'party': ('#D4FF4F', {
        'ink': ['M31 42 L48.5 14 Q50 11.5 51.5 14 L69 42 Q50 36.5 31 42Z', circ(50, 11, 5.5)],
        'white': ['M43.9 22 L56.1 22 L59.3 27 L40.7 27Z', 'M37.8 31.5 L62.2 31.5 L65.5 36.5 L34.5 36.5Z'],
    }),
}

ORIGINALS = ['neutral', 'sarcastic', 'mystical', 'grandma', 'pirate', 'philosopher', 'coach', 'lovestruck', 'drama', 'robot',
             'detective', 'conspiracy', 'cat', 'zoomer', 'villain', 'poet', 'astro', 'therapist', 'dog']


def eight(cx, cy, k=1.0, squeeze=1.0):
    """The number, as two rings, narrowed as the disc it stands on is."""
    def one(y, ro, ri):
        outer = ellipse_points(cx, y, ro * squeeze, ro, 0, 0, 360, 40)[:-1]
        inner = ellipse_points(cx, y, ri * squeeze, ri, 0, 0, 360, 24)[:-1]
        return poly(outer) + ' ' + poly(list(reversed(inner)))
    return one(cy - 3.7 * k, 3.9 * k, 1.35 * k) + ' ' + one(cy + 3.6 * k, 4.7 * k, 2.0 * k)


FACE_X, FACE_Y = BX + FACE['x'] * BR, BY + FACE['y'] * BR
EIGHT = eight(FACE_X, FACE_Y, 0.95, FACE['wide'] / FACE['tall'])


def lerp_hex(a, b, t):
    pa = [int(a[i:i + 2], 16) for i in (1, 3, 5)]
    pb = [int(b[i:i + 2], 16) for i in (1, 3, 5)]
    return '#%02X%02X%02X' % tuple(round(x + (y - x) * t) for x, y in zip(pa, pb))


def svg(name, size, ground, uid, guide=False):
    layers = LOOKS[name][1]
    place = FIGURE if layers else BARE
    figure = 'translate(%s %s) rotate(%s) scale(%s) translate(%s %s)' % (
        f(place['x']), f(place['y']), f(FIGURE['tilt']), f(place['r'] / BR), f(-BX), f(-BY))
    worn = 'translate(50 38) scale(%s) translate(-50 -38)' % f(1.0 if layers.get('fitted') else FIGURE['worn'])
    out = ['<svg viewBox="0 0 100 100" width="%d" height="%d"><defs><clipPath id="c%s"><circle cx="50" cy="50" r="50"/></clipPath></defs><g clip-path="url(#c%s)">' % (size, size, uid, uid)]
    out.append('<rect width="100" height="100" fill="%s"/>' % ground)
    out.append('<g transform="%s">' % figure)
    for d in layers.get('beside', []):
        out.append('<path d="%s" fill="%s"/>' % (d, INK))
    out.append('<g transform="%s">' % worn)
    for d in layers.get('behind', []):
        out.append('<path d="%s" fill="%s"/>' % (d, INK))
    out.append('</g>')
    out.append('<circle cx="%s" cy="%s" r="%s" fill="%s"/>' % (f(BX), f(BY), f(BR), INK))
    out.append('<ellipse cx="%s" cy="%s" rx="%s" ry="%s" fill="#fff"/>' % (f(FACE_X), f(FACE_Y), f(FACE['wide'] * BR), f(FACE['tall'] * BR)))
    out.append('<path d="%s" fill="%s"/>' % (EIGHT, INK))
    out.append('<g transform="%s">' % worn)
    for d in layers.get('ink', []):
        out.append('<path d="%s" fill="none" stroke="%s" stroke-width="%s" stroke-linejoin="round"/>' % (d, ground, f(RIM)))
    for d in layers.get('ink', []):
        out.append('<path d="%s" fill="%s"/>' % (d, INK))
    for d in layers.get('white', []):
        out.append('<path d="%s" fill="#fff"/>' % d)
    for d in layers.get('dot', []):
        out.append('<path d="%s" fill="%s"/>' % (d, INK))
    out.append('</g></g>')
    if guide:
        out.append('<circle cx="50" cy="50" r="46" fill="none" stroke="#f0f" stroke-width="0.4"/>')
    out.append('</g></svg>')
    return ''.join(out)


def sheet(path, names, dark=True, guide=False):
    page = '#0E141B' if dark else '#F1F3F7'
    card = '#1B222B' if dark else '#FFFFFF'
    neutral = '#AAB2BD' if dark else '#C6CCD4'
    text = '#E8ECF1' if dark else '#12110D'
    cells = []
    for k, name in enumerate(names):
        colour = LOOKS[name][0]
        quiet, loud = lerp_hex(colour, neutral, 0.7), lerp_hex(colour, neutral, 0.3)
        cells.append('<div class="cell"><div class="big">%s</div><div class="row">%s%s%s</div><div class="name">%s</div></div>' % (
            svg(name, 150, loud, 'a%d' % k, guide), svg(name, 28, quiet, 'b%d' % k), svg(name, 40, loud, 'c%d' % k), svg(name, 88, loud, 'd%d' % k), name))
    html = '''<html><body style="margin:0;background:%s;font-family:Helvetica;color:%s">
<style>.grid{display:grid;grid-template-columns:repeat(6,1fr);gap:14px;padding:18px}.cell{background:%s;border-radius:18px;padding:12px;text-align:center}
.row{display:flex;gap:8px;justify-content:center;align-items:center;margin-top:8px}.name{font-size:13px;margin-top:6px}svg{display:block}.big{display:flex;justify-content:center}</style>
<div class="grid">%s</div></body></html>''' % (page, text, card, ''.join(cells))
    page_file = path.rsplit('.', 1)[0] + '.html'
    open(page_file, 'w').write(html)
    rows = (len(names) + 5) // 6
    subprocess.run(['/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', '--headless=new', '--disable-gpu', '--hide-scrollbars',
                    '--force-device-scale-factor=1', '--screenshot=' + path, '--window-size=%d,%d' % (1420, 36 + rows * 322), 'file://' + page_file],
                   capture_output=True)


def kotlin():
    """The drawings as the app reads them."""
    out = ['package com.lorenzosuarez.magiceightball.phone.ui', '', 'import com.example.magiceightball.core.domain.model.CharacterLook', '',
           '/**',
           ' * What each look is drawn from: paths on a ball that stands upright in a box of 100 by 100, a',
           ' * circle of radius 28 about (50, 66) with its crown at (50, 38). See [LookArt] for the layers,',
           ' * and [drawFigure] for how that ball is shown.',
           ' *',
           ' * Drawn on one board to one set of rules, so that they read as one family. The numbers come',
           ' * from that board, `tools/looks.py`: a look is corrected there and written out again, not',
           ' * edited here.',
           ' */',
           'internal fun lookArt(look: CharacterLook): LookArt = when (look) {']
    for name, (_, layers) in LOOKS.items():
        args = []
        for key in ('beside', 'behind', 'ink', 'white', 'dot'):
            if key in layers:
                args.append('        %s = listOf(\n%s\n        )' % (key, ',\n'.join('            "%s"' % d for d in layers[key])))
        if layers.get('fitted'):
            args.append('        fitted = true')
        if args:
            out.append('    CharacterLook.%s -> LookArt(' % name.upper())
            out.append(',\n'.join(args))
            out.append('    )')
        else:
            out.append('    CharacterLook.%s -> LookArt()' % name.upper())
    out += ['}', '',
            '/** The number on the ball\'s white disc, as two rings, narrowed as the disc is. */',
            'internal const val LOOK_EIGHT = "%s"' % EIGHT, '',
            '/** The figure: where the ball\'s middle is in a round of 100 across, how large the ball is, how far it is tipped, and how large what it wears is drawn about the crown. */',
            'internal const val FIGURE_X = %sf' % f(FIGURE['x']),
            'internal const val FIGURE_Y = %sf' % f(FIGURE['y']),
            'internal const val FIGURE_RADIUS = %sf' % f(FIGURE['r']),
            'internal const val FIGURE_TILT = %sf' % f(FIGURE['tilt']),
            'internal const val FIGURE_WORN = %sf' % f(FIGURE['worn']), '',
            '/** The same for a ball that wears nothing, which has the round to itself: larger, and further in. */',
            'internal const val BARE_X = %sf' % f(BARE['x']),
            'internal const val BARE_Y = %sf' % f(BARE['y']),
            'internal const val BARE_RADIUS = %sf' % f(BARE['r']), '',
            '/** The white disc, in parts of the ball\'s radius: off its middle and narrowed, as on a sphere. */',
            'internal const val FACE_X = %sf' % f(FACE['x']),
            'internal const val FACE_Y = %sf' % f(FACE['y']),
            'internal const val FACE_WIDE = %sf' % f(FACE['wide']),
            'internal const val FACE_TALL = %sf' % f(FACE['tall']), '']
    return '\n'.join(out)


if __name__ == '__main__':
    if sys.argv[1] == 'kotlin':
        target = 'app-phone/src/main/java/com/lorenzosuarez/magiceightball/phone/ui/LookArtData.kt'
        open(target, 'w').write(kotlin())
        print('written', target)
    else:
        names = list(LOOKS)
        for extra in sys.argv[3:]:
            if extra not in ('light', 'guide'):
                names = extra.split(',')
        sheet(sys.argv[2], names, dark='light' not in sys.argv, guide='guide' in sys.argv)
        print('ok', len(names))
