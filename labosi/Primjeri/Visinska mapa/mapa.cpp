/* Sveučilište u Zagrebu, Fakultet elektrotehnike i računarstva
   Umjetna inteligencija
 
   Pretraživanje prostora stanja: Pronalaženje puta na visinskoj mapi
   v1.0
 
   Copyright: (c) 2011 Frane Šarić <frane.saric@fer.hr>
 
   Zadatak:

   Napišite program koji će korištenjem algoritama A*, pretraživanja s
   jednolikom cijenom i pohlepnim pretraživanjem "najbolji prvi"
   izračunavati najjeftiniji put između dvije točke na zadanoj visinskoj
   mapi. Mapa se sastoji od diskretnih polja poredanih u m redaka i n
   stupaca. Polje na koordinatama (redak, stupac) nalazi se na visini
   v(redak, stupac). Dozvoljen je pomak od polja na koordinatama (r1,s1) na
   polje na koordinatama (r2,s2) ukoliko je |r1-r2|<=1,|s1-s2|<=1 i
   abs(v(r1,s1)-v(r2,s2))<=maks_skok. Cijena puta od polja (r1,s1) do polja
   (r2,s2) je
   sqrt((r2-r1)^2+(s2-s1)^2)+(sgn(v(r2,s2)-v(r1,s1))/2+1)*abs(v(r1,s1)-v(r2,s2)).
   Ovime se ostvaruje da je cijena uspinjanja nešto veća od cijene silaska
   (funkcija sgn vraća -1, 0 ili 1 ovisno o tome je li argument manji,
   jednak ili veći od nule). Mapu i parametre program treba učitati iz
   zadane tekstne datoteke. Program treba u grafičkome sučelju (ili na
   generiranoj slici) prikazati visinsku mapu, sve zatvorene čvorove, sve
   otvorene čvorove, pronađeni put, duljinu pronađenog puta i broj koraka
   algoritma. Potrebno je osmisliti barem tri različite heuristike i
   isprobati rad algoritama s tim heuristikama.
 
   Primjer ulazne datoteke:
   http://www.fer.hr/_download/repository/ui_lab_zad1-4_ulaz%5B1%5D.txt

   Primjer prikaza visinske mape i rješenja:
   http://www.fer.hr/_download/repository/ui_lab_zad1-4_mapa.png
   http://www.fer.hr/_download/repository/ui_lab_zad1-4_rezultat.png
*/
#include <cstdio>
#include <vector>
#include <algorithm>
#include <set>
#include <unordered_map>
#include <unordered_set>
#include <cmath>

using namespace std;

int main() {
    // Učitajmo datoteku (nije podržano učitavanje datoteka sa znakom #).
    int h, w, diffmax;
    int sy, sx, ty, tx;
    scanf("%d%d%d%d%d%d%d", &h, &w, &diffmax, &sy, &sx, &ty, &tx);
    w += 2; // Mapu širimo za jedno polje sa svake strane.
    h += 2; // Polja na rubu mape odmah će biti označena kao nedostupna
    vector<int> vmap(w * h); // visine
    vector<char> closed(w * h, 1); // podatak je li čvor zatvoren ili ne
    vector<double> f_map(w * h, 1e7); // funkcija f

    auto coords = [w](int i, int j) { return i + 1 + (j + 1) * w; };

    int vmax = 0; // najveća visina (treba nam za generiranje slike)
    for (int j = 0; j < h - 2; ++j)
        for (int i = 0; i < w - 2; ++i) {
            scanf("%d", &vmap[coords(i, j)]);
            vmax = max(vmap[coords(i, j)], vmax);
            closed[coords(i, j)] = 0;
        }

    struct node { // Za svaki čvor pamtimo vrijednost funkcije g
        const double g, &f; // i pokazivač na vrijednost funkcije f
        bool operator <(const node &b) const { // poredak čvorova
            return f != b.f ? f < b.f : &f < &b.f;
        }
    };

    int trgidx = coords(tx, ty); // cilj
    // koristimo strukturu set jer podržava pronalaženje najmanjeg čvora u
    // O(log N) (podržava brisanje i dodavanje isto u O(log N)).
    set<node> open({ { 0, f_map[coords(sx, sy)] } });
    // polje backtrack koristimo za rekonstrukciju puta
    // Pristup i promjena elementa u strukturi unordered_map je O(1).
    unordered_map<int, int> backtrack;

    int num_steps = 0; // broj koraka algoritma
    while (!open.empty()) { // dok je skup otvorenih čvorova neprazan
        node cur = *open.begin(); // uzmi najmanji element u O(log N).
        open.erase(open.begin()); // izbriši ga
        int idx = &cur.f - &f_map[0]; // izračunaj indeks čvora
        closed[idx] = true; // stavi ga u listu zatvorenih

        ++num_steps;
        if (idx == trgidx) // pronađen je cilj
            break; // gotovi smo jer je heuristika optimistična i monotona

        for (int dy = -1; dy <= 1; ++dy) { // pokušaj otvoriti svako
            for (int dx = -1; dx <= 1; ++dx) { // susjedno polje
                int new_idx = idx + dx + dy * w; // novi čvor
                if (closed[new_idx]) // preskoči zatvoreno polje
                    continue;
                double new_g = cur.g + hypot(dx, dy); // izračunaj novi g
                int hdiff = vmap[new_idx] - vmap[idx];
                if (hdiff >= 0) {
                    new_g += 1.5 * hdiff;
                    if (hdiff > diffmax)
                        continue;
                } else {
                    new_g -= 0.5 * hdiff;
                    if (-hdiff > diffmax)
                        continue;
                }
                double new_f = new_g; // počni računati f = g + h
                // uračunaj zračnu udaljenost
                new_f += hypot(tx - new_idx % w + 1, ty - new_idx / w + 1);
                // zatim dodaj visinsku cijenu (pazeći na smjer)
                int diff2 = vmap[trgidx] - vmap[new_idx];
                new_f += diff2 >= 0 ? diff2 * 1.5 : -diff2 * 0.5;
                if (new_f < f_map[new_idx]) { // smanjili smo f
                    // pobriši čvor iz liste otvorenih u O(log N) vremenu.
                    open.erase(node({ 0, f_map[new_idx] }));
                    f_map[new_idx] = new_f; // promijeni vrijednost f
                    backtrack[new_idx] = idx; // zapamti odakle smo došli
                    // dodaj čvor u listu otvorenih u O(log N) vremenu.
                    open.insert(node({ new_g, f_map[new_idx] }));
                }
            }
        }
    }
    fprintf(stderr, "Cijena: %.2f\n", f_map[coords(tx, ty)]);
    fprintf(stderr, "Broj koraka: %d\n", num_steps);

    unordered_set<int> solution({ trgidx }); // rekonstruiraj rješenje
    while (backtrack.count(trgidx)) {
        trgidx = backtrack[trgidx];
        solution.insert(trgidx);
    }
    printf("P3 %d %d 255\n", w - 2, h - 2); // zapiši sliku u PNM formatu
    for (int j = 0; j < h - 2; ++j) {
        for (int i = 0; i < w - 2; ++i) {
            float h = vmap[coords(i, j)] / (float) vmax;
            float vis = closed[coords(i, j)] * .5;
            int r = (h * (1 - vis) + vis * .5) * 255;
            int g = (h * (1 - vis) + vis * .5) * 255;
            int b = (h * (1 - vis) + vis * .1) * 255;
            if (solution.count(coords(i, j)))
                r = 255, g = 0, b = 0;
            printf("%d %d %d\n", r, g, b); // pišemo trojku R G B
        }
    }
}
