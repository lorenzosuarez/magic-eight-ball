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
import re
import subprocess
import sys

INK = '#12110D'
BX, BY, BR = 50.0, 66.0, 28.0
RIM = 5.0

# The figure, in a round of 100 across: where the ball's middle is, how large the ball is, how far it is tipped,
# and how large what it wears is drawn beside the board, about the crown. The white disc is off the ball's middle
# by these parts of its radius, and is so wide and so tall.
FIGURE = dict(x=64.0, y=78.0, r=36.0, tilt=-18.0, worn=0.8)
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


def turned(d, cx, cy, degrees, scale=1.0):
    """A path turned about a point, and made larger or smaller about it: only for paths of M, L, Q, C and Z with absolute numbers."""
    c, s = math.cos(math.radians(degrees)) * scale, math.sin(math.radians(degrees)) * scale
    out, pair, letter = [], [], ''
    for token in re.findall(r'[A-Za-z]|-?[0-9.]+', d):
        if token.isalpha():
            assert token in 'MLQCZ', d
            if token == 'Z':
                out[-1] += 'Z'
            else:
                letter = token
            continue
        pair.append(float(token))
        if len(pair) == 2:
            x, y = pair[0] - cx, pair[1] - cy
            out.append('%s%s %s' % (letter, f(cx + x * c - y * s), f(cy + x * s + y * c)))
            pair, letter = [], ''
    return ' '.join(out)


def seen(d):
    """A path drawn where it is seen, in the round of 100 across, taken back to the board: for what is held at
    the ball's face or carried behind it, which is placed by where the face ends up and not by the crown. Only
    for paths of M, L, Q, C and Z with absolute numbers, on a look that is fitted."""
    scale = FIGURE['r'] / BR
    c, s = math.cos(math.radians(-FIGURE['tilt'])), math.sin(math.radians(-FIGURE['tilt']))
    out, pair, letter = [], [], ''
    for token in re.findall(r'[A-Za-z]|-?[0-9.]+', d):
        if token.isalpha():
            assert token in 'MLQCZ', d
            if token == 'Z':
                out[-1] += 'Z'
            else:
                letter = token
            continue
        pair.append(float(token))
        if len(pair) == 2:
            x, y = (pair[0] - FIGURE['x']) / scale, (pair[1] - FIGURE['y']) / scale
            out.append('%s%s %s' % (letter, f(BX + x * c - y * s), f(BY + x * s + y * c)))
            pair, letter = [], ''
    return ' '.join(out)


def shuriken(cx, cy, reach, waist, lean, turn):
    """A throwing star of four blades, each leaning the way it spins, with the hole in its middle."""
    pts = []
    for k in range(4):
        a, b = math.radians(turn + 90 * k), math.radians(turn + 90 * k + lean)
        pts.append((cx + reach * math.sin(a), cy - reach * math.cos(a)))
        pts.append((cx + waist * math.sin(b), cy - waist * math.cos(b)))
    return poly(pts) + ' ' + circ(cx, cy, 3.6, cw=False)


def disc(cx, cy, r):
    """A circle as points, for where a circle has to be turned or taken back to the board."""
    return poly(ellipse_points(cx, cy, r, r, 0, 0, 360, 40)[:-1])


def curve(points, n=24):
    """Points along a curve drawn through control points: two make a line, three a quadratic, four a cubic."""
    out = []
    for k in range(n + 1):
        t = k / n
        level = list(points)
        while len(level) > 1:
            level = [(a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t) for a, b in zip(level, level[1:])]
        out.append(level[0])
    return out


def ribbon(points, w0, w1=None, caps=(True, True)):
    """A strip that follows points, as wide as [w0] at its start and [w1] at its end, round at the ends that are capped."""
    w1 = w0 if w1 is None else w1
    left, right = [], []
    last = len(points) - 1
    for k, (x, y) in enumerate(points):
        ax, ay = points[max(k - 1, 0)]
        bx, by = points[min(k + 1, last)]
        n = math.hypot(bx - ax, by - ay)
        px, py = (by - ay) / n, -(bx - ax) / n
        w = (w0 + (w1 - w0) * k / last) / 2
        left.append((x + px * w, y + py * w))
        right.append((x - px * w, y - py * w))

    def cap(a, b):
        """Half a turn from one edge of an end to the other, bulging away from the strip."""
        mx, my = (a[0] + b[0]) / 2, (a[1] + b[1]) / 2
        r = math.hypot(a[0] - b[0], a[1] - b[1]) / 2
        start = math.atan2(a[1] - my, a[0] - mx)
        return [(mx + r * math.cos(start + math.pi * k / 10), my + r * math.sin(start + math.pi * k / 10)) for k in range(1, 10)]

    out = list(left)
    if caps[1]:
        out += cap(left[-1], right[-1])
    out += list(reversed(right))
    if caps[0]:
        out += cap(right[0], left[0])
    return poly(out)


def pill(cx, cy, w, h, degrees=0.0):
    """A rounded bar about its middle, [w] across and [h] long, turned."""
    half = (h - w) / 2
    c, s = math.cos(math.radians(degrees)), math.sin(math.radians(degrees))
    return ribbon([(cx + s * half * (1 - 2 * k / 6), cy - c * half * (1 - 2 * k / 6)) for k in range(7)], w)


def heart(cx, cy, w, h, degrees=0.0):
    """A heart about its middle, [w] across and [h] tall, leaning by [degrees]: round lobes and a soft dip."""
    pts = []
    for k in range(64):
        t = 2 * math.pi * k / 64
        pts.append((16 * math.sin(t) ** 3, -(13 * math.cos(t) - 5 * math.cos(2 * t) - 2 * math.cos(3 * t) - math.cos(4 * t))))
    top, bottom = min(y for _, y in pts), max(y for _, y in pts)
    c, s = math.cos(math.radians(degrees)), math.sin(math.radians(degrees))
    out = []
    for x, y in pts:
        x, y = x / 32 * w, (y - (top + bottom) / 2) / (bottom - top) * h
        out.append((cx + x * c - y * s, cy + x * s + y * c))
    return poly(out)


def on_ball(angle, out=0.0):
    """A point on the ball's edge, [angle] degrees from its crown towards the right, and [out] past the edge."""
    a = math.radians(angle)
    return (BX + (BR + out) * math.sin(a), BY - (BR + out) * math.cos(a))


def band_on_ball(top, thick, bow, inset=1.6, n=36):
    """A band across the ball, from edge to edge and kept [inset] within it: its top edge at [top] on the ball's axis and [bow] lower by the sides."""
    reach = BR - inset

    def edge(level):
        pts = []
        for k in range(n + 1):
            x = BX - reach + 2 * reach * k / n
            y = level + bow * ((x - BX) / reach) ** 2
            if (x - BX) ** 2 + (y - BY) ** 2 <= reach * reach:
                pts.append((x, y))
        # Where the curve meets the circle it is kept within, found by halving.
        def meet(sign):
            lo, hi = 0.0, reach
            for _ in range(40):
                mid = (lo + hi) / 2
                y = level + bow * (mid / reach) ** 2
                if mid * mid + (y - BY) ** 2 <= reach * reach:
                    lo = mid
                else:
                    hi = mid
            return (BX + sign * lo, level + bow * (lo / reach) ** 2)
        return [meet(-1)] + pts + [meet(1)]

    upper, lower = edge(top), edge(top + thick)

    def along(a, b):
        """The circle's own edge between two points on it, the short way."""
        a0, a1 = math.atan2(a[1] - BY, a[0] - BX), math.atan2(b[1] - BY, b[0] - BX)
        return [(BX + reach * math.cos(a0 + (a1 - a0) * k / 6), BY + reach * math.sin(a0 + (a1 - a0) * k / 6)) for k in range(1, 6)]

    return poly(upper + along(upper[-1], lower[-1]) + list(reversed(lower)) + along(lower[0], upper[0]))


QUOTE = 'M%s 16 a8.2 8.2 0 1 1 16.4 0 q0 12 -12 18.6 q4.4 -6.6 2.8 -11 a8.2 8.2 0 0 1 -7.2 -7.6Z'
CLOUD = [(37, 20, 9), (47, 14.5, 10), (58.5, 15.5, 9.5), (64.5, 22.5, 8.5), (53, 24, 9.5), (43, 25, 8.5)]
# The crest a punk wears: spikes from brow to nape, tallest in the middle, their feet behind the ball.
CREST = ([on_ball(-46, -5)] +
         [point for k, (tip, tall) in enumerate([(-34, 12), (-17, 18.5), (0, 22), (17, 18.5), (34, 12)])
          for point in ([on_ball(tip - 8.5, 2.6)] if k else []) + [on_ball(tip, tall)]] +
         [on_ball(46, -5)] + [on_ball(angle, -7) for angle in range(40, -41, -10)])
PETALS = [(59 + 9.6 * math.cos(math.radians(a)), 29 + 9.6 * math.sin(math.radians(a))) for a in range(-90, 270, 60)]

# name: (colour for the sheet, {layer: [paths]})
# Layers, bottom to top: behind (ink, under the ball), ink (over the ball, with a rim of the ground round it),
# white, dot (ink again, over the white). fitted: made to the ball's own measure, and so not drawn a size down.
LOOKS = {
    'neutral': ('#F2EEE3', {}),
    'sarcastic': ('#FFDD33', {'ink': [QUOTE % '27', QUOTE % '55.6']}),
    'mystical': ('#C9B6FF', {'ink': [crescent((44, 19.5), 14.5, (52, 15), 12), star4(69, 17, 8, 0.38)]}),
    'grandma': ('#FFC2D1', {
        # A ball of wool with the needles in it, sunk well into the crown: she knits.
        'ink': [bar(50, 32.5, 66.5, 16, 4), circ(67.3, 15.2, 3.4), bar(55, 36.5, 71.5, 21, 4), circ(72.3, 20.2, 3.4), circ(46, 30.5, 13.5)],
        'white': ['M35.23 26.48 Q46 19.02 56.77 26.48 L57.45 31.56 Q46 23.14 34.55 31.56Z', 'M35.28 34.66 Q46 26.84 56.72 34.66 L54.54 38.21 Q46 32.39 37.46 38.21Z'],
    }),
    'pirate': ('#5FD6C9', {
        # Pulled down onto the crown: floating over it, its rim cut the ball flat.
        'ink': ['M11.9 41 Q16.5 47 27.7 45.5 Q50 40 72.3 45.5 Q83.5 47 88.1 41 Q82.5 39 75.1 30 Q64 14 50 13 Q36 14 24.9 30 Q17.5 39 11.9 41Z'],
        'white': [circ(50, 26, 7.2), rrect(45.6, 30, 8.8, 6.5, 2.2)],
        'dot': [circ(47.1, 26, 2.1), circ(52.9, 26, 2.1)],
    }),
    'philosopher': ('#E3D5B8', {
        'ink': [circ(x, y, r) for x, y, r in CLOUD] + [circ(29.5, 35.5, 3.6)],
        'white': [circ(41, 19.5, 3), circ(51, 19.5, 3), circ(61, 19.5, 3)],
    }),
    'coach': ('#FF8A3D', {
        # A cap, its peak towards the words: the crown, the button on it, and one stripe that runs with its
        # lower edge, as far from one side of it as from the other. The peak's underside runs into that edge.
        'ink': ['M29 45.5 Q27 18.5 51 18.5 Q75 18.5 73.6 45 Q51 37.5 29 45.5Z',
                'M34.5 38.5 Q21 38 9.5 43.5 Q6.5 47 10.5 48.8 Q20 46.4 29 45.5 L36 42Z', circ(51, 17.6, 2.8)],
        'white': [ribbon(curve([(36, 37.4), (51, 31.2), (66, 37.4)]), 4.6)],
    }),
    # Hearts in the air over it, a large one and a small one, each leaning its own way.
    'lovestruck': ('#FF7A9C', {'ink': [heart(46.5, 22.5, 26, 23.5, -6), heart(68.5, 13.5, 12, 10.9, 16)]}),
    'drama': ('#FF5A4F', {
        'ink': ['M30 45 L26 19.5 L39.5 30 L50 11 L60.5 30 L74 19.5 L70 45 Q50 37.5 30 45Z', circ(26, 17.5, 4), circ(50, 9.5, 4.2), circ(74, 17.5, 4)],
        'white': [circ(50, 32, 3.6), circ(37, 37.5, 2.6), circ(63, 37.5, 2.6)],
    }),
    'robot': ('#B8C4D0', {
        # An aerial that grows from behind the ball, as ears do, broad where it leaves it: nothing stuck into it.
        'behind': ['M40.5 42 Q47.6 37.5 47.5 27 L52.5 27 Q52.4 37.5 59.5 42Z', bar(50, 29, 50, 19, 5, cap=False), circ(50, 13, 7.4)],
        'white': [circ(50, 13, 3.2)],
    }),
    'detective': ('#D6C68A', {
        # The brim round at its ends, the pinch of the crown soft.
        'ink': [ribbon(curve([(18.5, 44.6), (50, 33.4), (81.5, 44.6)]), 5.8),
                'M30 40 Q27.8 17.5 37.5 13.6 Q40 12.7 42.2 14 Q50 18 57.8 14 Q60 12.7 62.5 13.6 Q72.2 17.5 70 40 Q50 34 30 40Z'],
        'white': ['M29.66 29.5 Q50 25.5 70.34 29.5 L70.35 35.4 Q50 31.2 29.65 35.4Z'],
    }),
    'conspiracy': ('#9BE564', {
        # A pyramid worn as a hat, tipped with the ball as every hat is, its foot past the ball at both sides
        # like a brim, and the eye in it.
        'ink': ['M23 46.2 L47.4 12.6 Q50 9.6 52.6 12.6 L77 46.2 Q50 37.3 23 46.2Z'],
        'white': ['M38.5 31.2 Q50 21.4 61.5 31.2 Q50 39 38.5 31.2Z'],
        'dot': [circ(50, 30.6, 4.7)],
    }),
    'cat': ('#FFB86B', {
        'behind': both('M22 56 L28.5 16.5 Q43 26 50 44Z'),
        'white': both('M30 37 L32.3 25.5 Q38.5 29.5 42 36Z'),
    }),
    'zoomer': ('#D4FF4F', {
        # Made to the ball: the band over its crown and a cup at each side, lying along it where ears would
        # be and clear of the face. The far one runs off with the ball.
        'fitted': True,
        'ink': [ribbon([on_ball(a, 3.6) for a in range(-58, 59, 4)], 4.2, caps=(False, False)),
                pill(*on_ball(-64, 1.4), 9.5, 19, 26), pill(*on_ball(64, 1.4), 9.5, 19, 154)],
        # The pad of each cup: what tells a cup from the ball where it lies over it.
        'white': [pill(*on_ball(-64, 1.4), 4.5, 11.5, 26), pill(*on_ball(64, 1.4), 4.5, 11.5, 154)],
    }),
    'villain': ('#B58CFF', {'behind': both('M23 54 Q7 31 33 9 Q21 30 35 45Z')}),
    # Tucked behind the ball and broad: planted on it, its shaft stood in a socket.
    'poet': ('#BFD9FF', {'behind': quill((43.5, 45), (70, 8.5), widest=10.5, shaft=0.34)}),
    'astro': ('#8FA8FF', {
        # A planet and its ring: the ring is ink where it is past the planet and white where it crosses in
        # front of it, cut by the planet's own edge.
        'ink': [circ(50, 21, 12.5), ell_ring(50, 21, 24, 6.8, 4.8, rot=-20)],
        'white': [poly(ellipse_points(50, 21, 26.4, 9.2, -20, 70.5, 109.5, 20) + list(reversed(ellipse_points(50, 21, 21.6, 4.4, -20, 59, 121, 20))))],
    }),
    'therapist': ('#BFE8D2', {
        # Glasses pushed up onto the crown, their arms short and level.
        'ink': [ring(37, 32.5, 12.5, 7.2), ring(63, 32.5, 12.5, 7.2), bar(47.5, 31, 52.5, 31, 4.4, cap=False), bar(25.5, 31, 19.5, 31, 4.4), bar(74.5, 31, 80.5, 31, 4.4)],
    }),
    # A bone in the air over it: what says dog at any size, where ears on a ball said bear.
    'dog': ('#F0B878', {
        'ink': [ribbon([(32, 26), (68, 26)], 8, caps=(False, False)), circ(31, 21.5, 6), circ(31, 30.5, 6), circ(69, 21.5, 6), circ(69, 30.5, 6)],
    }),
    # No character of the app's wears these: they are there to be picked.
    'singer': ('#FFC2D1', {
        # Held up to its face and leaning towards it, as one sings into it: the head beside the white disc,
        # its collar cut by the head's own edge, the handle round at its end and clear of the ball. The handle
        # is as long as it can be with 3.6 left to the ball and to the round's edge: longer, it touches one.
        'fitted': True,
        'ink': [seen(disc(27, 62, 9.4)), seen(ribbon([(24.44, 65.94), (16.6, 78.02)], 8.6, 5.6, caps=(False, True)))],
        'white': [seen(turned(poly(ellipse_points(27, 62, 9.35, 9.35, 0, 208.2, 165.7, 8) + ellipse_points(27, 62, 9.35, 9.35, 0, 90.3, 47.8, 8)), 27, 62, -5.1))],
    }),
    # In the air over it and broad: stuck into the ball, its point was a spike in a socket.
    'rockstar': ('#B58CFF', {'ink': ['M59.5 2.5 L33.5 22.5 L47 22.5 L40.5 34.5 L66.5 14.5 L53 14.5Z']}),
    'chef': ('#F2EEE3', {
        'ink': [circ(38.6, 21.6, 9.7), circ(50, 17.2, 11), circ(61.4, 21.6, 9.7), rrect(35, 24.3, 30, 17.6, 2.6)],
        'white': [rrect(37.2, 34.2, 25.6, 5, 2.5)],
    }),
    'wizard': ('#8FA8FF', {
        # Its point flops over; its brim is round at the ends.
        'ink': ['M31 41 Q41.5 27 46 12 Q48.5 4.5 55.5 6 Q61.5 7.5 64 14 Q59 11.5 55.5 15.5 Q60.5 29 69 41Z',
                ribbon(curve([(16.5, 43.8), (50, 32.4), (83.5, 43.8)]), 6)],
        'white': [star4(50, 28, 7)],
    }),
    'cowboy': ('#F0B878', {
        'ink': ['M33.5 39 Q30 11 41 9.5 Q50 16 59 9.5 Q70 11 66.5 39Z',
                'M14.2 31 Q13.3 25 16.5 25.5 Q21.2 37 34.2 36 Q50 33 65.8 36 Q78.8 37 83.5 25.5 Q86.7 25 85.8 31 Q81.6 45.5 67.7 43.5 Q50 39.5 32.3 43.5 Q18.4 45.5 14.2 31Z'],
    }),
    'magician': ('#B8C4D0', {
        'ink': ['M35.5 39 L33 9.5 Q33 6 36.5 6 L63.5 6 Q67 6 67 9.5 L64.5 39Z', rrect(20, 36, 60, 7.2, 3.6)],
        'white': ['M34.6 26.5 L65.4 26.5 L64.9 32.6 L35.1 32.6Z'],
    }),
    'ninja': ('#FF5A4F', {
        # A band tied across its brow, straight from side to side and clear of its face, knotted at the back
        # with its two ends flying: the ball itself is the ninja. Along the ball's edge the band was taken
        # for a shine, and a star in the air for a sparkle.
        'fitted': True,
        'behind': [seen(ribbon(curve([(77.7, 43.6), (74, 35.5), (81.5, 29.5), (76.5, 19.5)], 16), 4, 6.2, caps=(True, False))),
                   seen(ribbon(curve([(77.7, 43.6), (84, 42.5), (83.5, 35), (89, 32.5)], 16), 4, 5.8, caps=(True, False))),
                   circ(*on_ball(40.2, 0.6), 4.8)],
        'white': [band_on_ball(41.3, 5.4, 0.6, inset=0)],
    }),
    'angel': ('#BFD9FF', {'ink': [ell_ring(50, 21, 21, 8, 5.6)]}),
    'graduate': ('#BFE8D2', {
        'ink': ['M31.5 30 L31.5 41 Q50 47 68.5 41 L68.5 30 L50 38Z', 'M50 9 L86 24 L50 39 L14 24Z', bar(77.5, 25.5, 77.5, 34.5, 3.6), rrect(74.3, 32.5, 6.4, 9, 2)],
        'white': [circ(50, 24, 3.6), bar(50, 24, 75.2, 24.8, 4.6)],
    }),
    'flower': ('#FFB86B', {
        'ink': [circ(x, y, 6.4) for x, y in PETALS] + [circ(59, 29, 6)],
        'white': [circ(59, 29, 5.2)],
    }),
    'party': ('#D4FF4F', {
        'ink': ['M31 42 L48.5 14 Q50 11.5 51.5 14 L69 42 Q50 36.5 31 42Z', circ(50, 11, 5.5)],
        'white': ['M43.9 22 L56.1 22 L59.3 27 L40.7 27Z', 'M37.8 31.5 L62.2 31.5 L65.5 36.5 L34.5 36.5Z'],
    }),
    # More to pick among, each worn or grown and none in the air.
    # A gourd of mate held up beside its face and leaning towards it: round, with a neck, the metal round
    # its mouth in white under a lip of its own, and the straw, thin, with its flat end. It overlaps the
    # ball enough to be told from it by its rim. Sipped, the straw into the face made a pipe of it.
    'mate': ('#9BE564', {
        'fitted': True,
        'ink': [seen(turned(poly([(14.9, 53.5), (30.1, 53.5)] + ellipse_points(22.5, 66.37, 11, 9.4, 0, -41.8, 221.8, 40)), 22.5, 53.5, 6)),
                seen(turned(bar(24, 55.5, 32.5, 38.5, 3.8, cap=False), 22.5, 53.5, 6)),
                seen(turned(ribbon([(30.9, 41.7), (32.8, 38)], 5.4), 22.5, 53.5, 6))],
        'white': [seen(turned(poly([(14.7, 55.7), (30.3, 55.7), (30.74, 60.5), (14.26, 60.5)]), 22.5, 53.5, 6))],
    }),
    # A bottle that has all but fallen over on its crown, and what rises from it, larger as it rises.
    'bottle': ('#BFE8D2', {
        'ink': [turned('M42.1 43.2 Q39.5 43.2 39.5 40.6 L39.5 25.5 Q39.5 20.9 44 19.4 L44 11.2 L51 11.2 L51 19.4 Q55.5 20.9 55.5 25.5 L55.5 40.6 Q55.5 43.2 52.9 43.2Z', 47.5, 43.2, -20),
                turned(poly([(42.6, 9.2), (52.4, 9.2), (52.4, 13.2), (42.6, 13.2)]), 47.5, 43.2, -20),
                circ(41.4, 4.5, 2.8), circ(50.6, 3.8, 3.7)],
        'white': [turned(poly([(42.1, 27), (52.9, 27), (52.9, 35.7), (42.1, 35.7)]), 47.5, 43.2, -20)],
    }),
    # A fin: behind the ball, and so part of its outline.
    'shark': ('#8FA8FF', {'behind': ['M38 48 Q42 26 64 12 Q57 28 65.5 46Z']}),
    # A crest of spikes from brow to nape, made to the ball.
    'punk': ('#FF7A9C', {'fitted': True, 'behind': [poly(CREST)]}),
    # A helmet with its two horns and the band round its edge.
    'viking': ('#D6C68A', {
        'ink': ['M30 45 Q28 20.5 50 20.5 Q72 20.5 70 45 Q50 37.5 30 45Z'] + both('M34 37 Q14 38 21.5 15 Q23.5 27 35 28Z'),
        'white': [ribbon(curve([(36.5, 38.2), (50, 33.4), (63.5, 38.2)]), 4.6)],
    }),
    # A mouse's ears: ovals that lean outwards, the near one to the side and the far one nearer the crown,
    # as a head turned a little shows them, the inside of each in white.
    'mouse': ('#B8C4D0', {
        'fitted': True,
        'behind': [poly(ellipse_points(*on_ball(-45, 7.5), 12.2, 14.2, -45, 0, 360, 40)[:-1]), poly(ellipse_points(*on_ball(32, 6.5), 11.6, 13.4, 32, 0, 360, 40)[:-1])],
        'white': [poly(ellipse_points(*on_ball(-45, 6.2), 6.2, 8, -45, 0, 360, 40)[:-1]), poly(ellipse_points(*on_ball(32, 5.4), 5.8, 7.4, 32, 0, 360, 40)[:-1])],
    }),
    # A rabbit's ears: long, behind the ball, each with its inside in white.
    'bunny': ('#FFC2D1', {
        'fitted': True,
        'behind': both('M36.5 46 Q26.5 28 32.8 13.5 Q45.5 23 47.5 43Z'),
        'white': both('M38.2 37.5 Q33.8 27.5 35.6 20.5 Q41.4 27.5 43 38Z'),
    }),
    # A bird that has settled on its crown, looking where the ball looks: its beak, its wing, its tail up.
    'bird': ('#BFD9FF', {
        'ink': [poly(ellipse_points(53, 34.5, 13.5, 10.4, -6, 0, 360, 40)[:-1]), circ(41, 27.5, 7.8), poly([(35, 23.6), (24.5, 28), (35.2, 31.4)]),
                ribbon([(60, 33), (74, 19.5)], 8.6, 6, caps=(False, False))],
        'white': [circ(40, 26.5, 2.6), ribbon(curve([(47.5, 33.5), (54, 40.5), (60.5, 34.5)]), 4.6)],
    }),
    # A cap with a propeller of two blades on it.
    'propeller': ('#D4FF4F', {
        'ink': ['M31 45.5 Q30 24.5 50 24.5 Q70 24.5 69 45.5 Q50 38 31 45.5Z', bar(50, 26, 50, 15, 4.2, cap=False),
                poly(ellipse_points(39.5, 11.4, 10.5, 4.4, 12, 0, 360, 40)[:-1]), poly(ellipse_points(60.5, 15.6, 10.5, 4.4, 12, 0, 360, 40)[:-1]), circ(50, 13.5, 4.2)],
        'white': [ribbon(curve([(38, 38.6), (50, 34), (62, 38.6)]), 4.6)],
    }),
    # A ribbon tied in a bow on its crown.
    'bow': ('#FF7A9C', {
        'ink': both('M48 33 Q38 19.5 28 21.5 Q25.5 33 28 44.5 Q38 46.5 48 33Z') + [circ(50, 33, 6)],
    }),
    # The cap of a mushroom, with its spots.
    'mushroom': ('#FF5A4F', {
        'ink': ['M16 40.5 Q13 14.5 50 14.5 Q87 14.5 84 40.5 Q81.5 45.5 75 44.3 Q50 38.5 25 44.3 Q18.5 45.5 16 40.5Z'],
        'white': [circ(38, 28, 5.8), circ(59.5, 23.5, 4.4), circ(71, 34, 3.4)],
    }),
    # An apple on its crown with an arrow through it.
    'apple': ('#9BE564', {
        'ink': ['M50 22 Q55 17.5 61 20 Q67.5 23.5 66 33 Q64 44.5 56 46.5 Q52.5 47.2 50 45.6 Q47.5 47.2 44 46.5 Q36 44.5 34 33 Q32.5 23.5 39 20 Q45 17.5 50 22Z',
                bar(50, 23, 52.5, 14.5, 3.6), 'M53 16.5 Q58 9.5 66 11.5 Q62 18.5 53 16.5Z',
                bar(22, 33, 79, 33, 3.6, cap=False), poly([(27.5, 26.8), (14.5, 33), (27.5, 39.2)]),
                poly([(72.5, 33), (77, 27), (85.5, 27), (81.5, 33), (85.5, 39), (77, 39)])],
    }),
    # A gamepad held up beside its face, with its two sticks: half over the ball, told from it by its rim.
    'gamer': ('#4DD9FF', {
        'fitted': True,
        'ink': [seen(turned('M12 60.5 Q12 56.5 16.5 56.5 L32.5 56.5 Q37 56.5 37 60.5 L38 70.5 Q38.4 75 34.8 75 Q32.4 75 30.9 71.8 L29.6 69 L19.4 69 L18.1 71.8 Q16.6 75 14.2 75 Q10.6 75 11 70.5Z', 24.5, 65, 8))],
        'white': [seen(turned(disc(18.6, 62.6, 2.9), 24.5, 65, 8)), seen(turned(disc(30.4, 62.6, 2.9), 24.5, 65, 8))],
    }),
}

ORIGINALS = ['neutral', 'sarcastic', 'mystical', 'grandma', 'pirate', 'philosopher', 'coach', 'lovestruck', 'drama', 'robot',
             'detective', 'conspiracy', 'cat', 'zoomer', 'villain', 'poet', 'astro', 'therapist', 'dog', 'gamer']


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
    figure = 'translate(%s %s) rotate(%s) scale(%s) translate(%s %s)' % (
        f(FIGURE['x']), f(FIGURE['y']), f(FIGURE['tilt']), f(FIGURE['r'] / BR), f(-BX), f(-BY))
    worn = 'translate(50 38) scale(%s) translate(-50 -38)' % f(1.0 if layers.get('fitted') else FIGURE['worn'])
    out = ['<svg viewBox="0 0 100 100" width="%d" height="%d"><defs><clipPath id="c%s"><circle cx="50" cy="50" r="50"/></clipPath></defs><g clip-path="url(#c%s)">' % (size, size, uid, uid)]
    out.append('<rect width="100" height="100" fill="%s"/>' % ground)
    out.append('<g transform="%s">' % figure)
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
        for key in ('behind', 'ink', 'white', 'dot'):
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
