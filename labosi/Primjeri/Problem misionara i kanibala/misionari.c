/* Sveučilište u Zagrebu, Fakultet elektrotehnike i računarstva
   Umjetna inteligencija
 
   Pretraživanje prostora stanja: Problem misionara i kanibala
   v1.0
 
   Copyright: (c) 2011 Frane Šarić <frane.saric@fer.hr>
 
   Zadatak:
   Napišite program koji će pretraživanjem u širinu pronaći optimalno rješenje
   problema misionara i kanibala. Problem je opisan na sljedeći način: tri misionara
   i tri kanibala potrebno je jednim čamcem prevesti s jedne strane obale rijeke na
   drugu, pri čemu se niti u jednom trenutku na jednoj strani obale ne smije naći
   više kanibala nego misionara. Čamac može prevesti najviše dvije osobe i ne
   može ploviti prazan. Optimalno rješenje je ono s najmanjim brojem koraka.
   Program treba ispisati rješenje u obliku niza operatora i stanja koja dovode do
   ciljnog stanja.

   Primjer: http://www.learn4good.com/games/puzzle/boat.htm
*/
#include <stdio.h>

typedef struct { /* Pamtimo broj k. i m. na ovoj i suprotnoj obali. */
    int k[2], m[2], o; /* o = 0 je lijeva obala, o = 1 je desna. */
} stanje;
stanje mk_stanje(int k1, int m1, int k2, int m2, int o) {
    stanje rez = { { k1, k2 }, { m1, m2 }, o };
    return rez;
}
stanje prebaci(stanje *s, int nk, int nm) { /* Promijeni stranu rijeke. */
    return mk_stanje(s->k[1] + nk, s->m[1] + nm, s->k[0] - nk, s->m[0] - nm,
                     1 - s->o);
}
int moze(stanje *s) { /* Je li dobro stanje (neće se nitko pojesti)? */
    return (!s->m[0] || s->m[0] >= s->k[0]) &&
           (!s->m[1] || s->m[1] >= s->k[1]);
}

void ispisi(stanje *q, int *proslo, int qpos) {
    stanje *s = &q[qpos]; /*  Trenutno stanje. */
    if (proslo[qpos] >= 0) { /* Samo ako ovo nije početno stanje onda: */
        stanje *prev = &q[proslo[qpos]]; /* uzmi prethodno stanje, */
        ispisi(q, proslo, proslo[qpos]); /* ispiši korake do tog stanja, */
        printf("%5c--- K:%d M:%d ---%c\n", "< "[s->o], s->k[0] - prev->k[1],
                s->m[0] - prev->m[1], " >"[s->o]); /* ispiši operaciju. */
    }
    printf("K:%d M:%d ......... K:%d M:%d\n", /* Ispiši trenutno stanje. */
            s->k[s->o], s->m[s->o], s->k[1 - s->o], s->m[1 - s->o]);
}

int main(void) {
    static stanje q[1000]; /* Mana ovog rješenja je fiksna veličina */
    static int proslo[1000]; /* reda, ali 1000 je i više nego dovoljno. */
    static char visited[4][4][4][4][2]; /* Koja stanja su posjećena? */
    int qsize = 0, qstart, k, m;
    q[qsize] = mk_stanje(3, 3, 0, 0, 0), proslo[qsize++] = -1;
    for (qstart = 0; qstart < qsize; ++qstart) {
        stanje s = q[qstart]; /* Uzmi stanje s početka reda. */
        if (s.o == 1 && s.k[0] + s.m[0] == 6) { /* Našli smo rješenje? */
            ispisi(q, proslo, qstart); /* Ispiši ga i prekini traženje. */
            break;
        }
        for (k = 0; k <= s.k[0]; ++k) /* Probaj staviti kanibale. */
            for (m = 0; m <= s.m[0]; ++m) { /* Probaj staviti misionare. */
                stanje n = prebaci(&s, k, m); /* Konstruiraj novo stanje. */
                char *vis = &visited[n.k[0]][n.k[1]][n.m[0]][n.m[1]][n.o];
                if (m + k > 0 && m + k <= 2 && moze(&n) && !*vis) {
                    q[qsize] = n, proslo[qsize++] = qstart; /* Dodaj u red */
                    *vis = 1; /* Označi stanje kao posjećeno. */
                }
            }
    }
    return 0;
}
