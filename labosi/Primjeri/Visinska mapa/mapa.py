# coding=utf-8
# Sveučilište u Zagrebu, Fakultet elektrotehnike i računarstva
# Umjetna inteligencija
#
# Pretraživanje prostora stanja: Pronalaženje puta na visinskoj mapi
# v1.0
#
# Copyright: (c) 2011 Frane Šarić <frane.saric@fer.hr>
#
# Zadatak:
#
# Napišite program koji će korištenjem algoritama A*, pretraživanja s
# jednolikom cijenom i pohlepnim pretraživanjem "najbolji prvi"
# izračunavati najjeftiniji put između dvije točke na zadanoj visinskoj
# mapi. Mapa se sastoji od diskretnih polja poredanih u m redaka i n
# stupaca. Polje na koordinatama (redak, stupac) nalazi se na visini
# v(redak, stupac). Dozvoljen je pomak od polja na koordinatama (r1,s1) na
# polje na koordinatama (r2,s2) ukoliko je |r1-r2|<=1,|s1-s2|<=1 i
# abs(v(r1,s1)-v(r2,s2))<=maks_skok. Cijena puta od polja (r1,s1) do polja
# (r2,s2) je
# sqrt((r2-r1)^2+(s2-s1)^2)+(sgn(v(r2,s2)-v(r1,s1))/2+1)*abs(v(r1,s1)-v(r2,s2)).
# Ovime se ostvaruje da je cijena uspinjanja nešto veća od cijene silaska
# (funkcija sgn vraća -1, 0 ili 1 ovisno o tome je li argument manji,
# jednak ili veći od nule). Mapu i parametre program treba učitati iz
# zadane tekstne datoteke. Program treba u grafičkome sučelju (ili na
# generiranoj slici) prikazati visinsku mapu, sve zatvorene čvorove, sve
# otvorene čvorove, pronađeni put, duljinu pronađenog puta i broj koraka
# algoritma. Potrebno je osmisliti barem tri različite heuristike i
# isprobati rad algoritama s tim heuristikama.
#
# Primjer ulazne datoteke:
# http://www.fer.hr/_download/repository/ui_lab_zad1-4_ulaz%5B1%5D.txt
#
# Primjer prikaza visinske mape i rješenja:
# http://www.fer.hr/_download/repository/ui_lab_zad1-4_mapa.png
# http://www.fer.hr/_download/repository/ui_lab_zad1-4_rezultat.png

import sys
import numpy
import math
from heapq import *

def load_map(path):
    data = map(int, open(path).read().split())
    (height, width) = data[:2]
    diffmax = data[2]
    src = tuple(data[3:5])
    trg = tuple(data[5:7])
    array = numpy.array(data[7:]).reshape((height, width))
    return array, src, trg, diffmax

def astar(m, src, trg, diffmax):
    in_bounds = numpy.zeros((m.shape[0] + 2, m.shape[1] + 2), dtype=numpy.int8)
    in_bounds[1:-1, 1:-1] = 1

    opened = numpy.zeros(m.shape, dtype=numpy.int32)
    closed = numpy.zeros(m.shape, dtype=numpy.int32)
    backtrack = {}

    hdists = numpy.zeros(m.shape, dtype=numpy.float32)

    row_dist, col_dist = numpy.mgrid[0:m.shape[0], 0:m.shape[1]]
    # zračna udaljenost
    h = numpy.hypot(row_dist - trg[0], col_dist - trg[1])

    # slabija visinska heuristika
    #h += numpy.abs(m - m[trg]) * 0.5

    # stroža visinska heuristika
    h += numpy.abs(m - m[trg]) * (numpy.sign(m[trg] - m) * 0.5 + 1)

    # bez heuristike
    h[:, :] = 0

    open = []
    # sporija varijanta:
    # open.append((h[src], 0, src))

    # brža varijanta:
    heappush(open, (h[src], 0, src))

    steps = 0
    while len(open) > 0:
        # sporija varijanta:
        # minelem = min(open)
        # open.remove(minelem)
        # hval, dist, coord = minelem

        # brža varijanta:
        hval, dist, coord = heappop(open)

        if closed[coord]:
            continue

        closed[coord] = 1
        steps += 1

        if coord == trg:
            break

        h1 = m[coord]
        for dy in xrange(-1, 2):
            for dx in xrange(-1, 2):
                ncoord = (coord[0] + dy, coord[1] + dx)
                if not in_bounds[ncoord[0] + 1, ncoord[1] + 1]:
                    continue
                if closed[ncoord]:
                    continue
                h2 = m[ncoord]
                hdist = abs(h2 - h1)
                if hdist > diffmax:
                    continue
                dcost = math.hypot(dx, dy)
                if h2 > h1:
                    dcost += hdist * 1.5
                else:
                    dcost += hdist * 0.5
                ndist = dist + dcost
                hnew = h[ncoord] + ndist
                if not opened[ncoord] or hdists[ncoord] > hnew:
                    opened[ncoord] = 1
                    hdists[ncoord] = hnew

                    # sporija varijanta:
                    # open.append((hnew, ndist, ncoord))
                    # brža varijanta:
                    heappush(open, (hnew, ndist, ncoord))

                    backtrack[ncoord] = coord
    path = numpy.zeros(m.shape, dtype=numpy.int32)
    last_coord = trg
    while last_coord in backtrack:
        path[last_coord] = 1
        last_coord = backtrack[last_coord]

    return dist, path, closed, h, steps

m, src, trg, diffmax = load_map(sys.argv[1])

pathcost, path, closed, h, steps = astar(m, src, trg, diffmax)

print "Cijena puta: %.2f" % pathcost
print "Broj koraka: %d" % steps

import Image, ImageDraw

def to_image(m):
    im = Image.new("L", (m.shape[1], m.shape[0]))
    a = im.load()
    for r in xrange(m.shape[0]):
        for c in xrange(m.shape[1]):
            a[c, r] = m[r, c]
    return im

im_h = to_image(h * 255 / max(h.max(), 1))
im_m = to_image(m)

im_closed = to_image(closed * 255)
im_path = to_image(path * 255)

final = Image.new("RGB", (m.shape[1], m.shape[0]))
import ImageOps, ImageChops
final = ImageChops.composite(
            ImageOps.colorize(im_closed, (0, 0, 0), (200, 200, 100)),
            ImageOps.colorize(im_m, (0, 0, 0), (255, 255, 255)),
            to_image(closed * 128)
            )
final.paste(ImageOps.colorize(im_path, (0, 0, 0), (255, 0, 0)),
            None,
            im_path)
final.save("slika.png", "PNG")
im_m.save("mapa.png", "PNG")
im_h.save("heuristika.png", "PNG")
