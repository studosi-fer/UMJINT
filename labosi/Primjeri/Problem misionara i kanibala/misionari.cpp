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
#include <vector>
#include <cstdio> // U C++-u treba koristiti cstdio umjesto stdio.h, itd.

using namespace std; // Da ne moramo pisati std::vector, itd.

struct stanje { // Pamtimo broj k. i m. na ovoj i suprotnoj obali.
    int k[2], m[2], o; // o = 0 je lijeva obala, o = 1 je desna.
    stanje(int k1, int m1, int k2, int m2, int o_) : o(o_) {
        k[0] = k1, k[1] = k2, m[0] = m1, m[1] = m2;
    }
    stanje prebaci(int nk, int nm) const { // Promijeni stranu rijeke.
        return stanje(k[1] + nk, m[1] + nm, k[0] - nk, m[0] - nm, 1 - o);
    }
    bool moze() const { // Je li dobro stanje (neće se nitko pojesti)?
        return (!m[0] || m[0] >= k[0]) && (!m[1] || m[1] >= k[1]);
    }
};

// Prvi parametar je referenca jer ga ne želimo kopirati svaki put kad se
// zove funkcija (poziv je "by value"), a const je jer ga ne mijenjamo.
void ispisi(const vector<pair<stanje, int> > &q, int qpos) {
    const stanje &s = q[qpos].first; // Trenutno stanje.
    if (q[qpos].second >= 0) { // Samo ako ovo nije početno stanje onda:
        ispisi(q, q[qpos].second); // ispiši korake do prethodnog stanja,
        const stanje &prev = q[q[qpos].second].first;
        printf("%5c--- K:%d M:%d ---%c\n", "< "[s.o], s.k[0] - prev.k[1],
                s.m[0] - prev.m[1], " >"[s.o]); // ispiši operaciju.
    }
    printf("K:%d M:%d ......... K:%d M:%d\n", // Ispiši trenutno stanje.
            s.k[s.o], s.m[s.o], s.k[1 - s.o], s.m[1 - s.o]);
}

int main() {
    static char visited[4][4][4][4][2]; // Koja stanja su posjećena?
    vector<pair<stanje, int> > q; // Pamtimo stanje i indeks prethodnog.
    q.push_back(make_pair(stanje(3, 3, 0, 0, 0), -1)); // Početno stanje.

    for (unsigned qstart = 0; qstart < q.size(); ++qstart) {
        stanje s = q[qstart].first; // Uzmi stanje s početka reda.
        if (s.o == 1 && s.k[0] + s.m[0] == 6) { // Našli smo rješenje?
            ispisi(q, qstart); // Ispiši ga i prekini traženje.
            break;
        }
        for (int k = 0; k <= s.k[0]; ++k) // Probaj staviti kanibale.
            for (int m = 0; m <= s.m[0]; ++m) { // Probaj staviti misionare.
                stanje n = s.prebaci(k, m); // Konstruiraj novo stanje.
                char &vis = visited[n.k[0]][n.k[1]][n.m[0]][n.m[1]][n.o];
                if (m + k > 0 && m + k <= 2 && n.moze() && !vis) {
                    q.push_back(make_pair(n, qstart)); // Dodaj u red.
                    vis = 1; // Označi stanje kao posjećeno.
                }
            }
    }
}
