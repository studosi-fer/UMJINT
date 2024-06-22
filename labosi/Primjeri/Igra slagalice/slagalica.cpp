/* Sveučilište u Zagrebu, Fakultet elektrotehnike i računarstva
   Umjetna inteligencija
 
   Pretraživanje prostora stanja: Igra slagalice 
   v1.0
 
   Copyright: (c) 2011 Frane Šarić <frane.saric@fer.hr>
 
   Zadatak:
   Napišite program za rješavanje slagalice proizvoljnih dimenzija 
   uporabom algoritma A* i odgovarajuće heurističke funkcije. Početna 
   pozicija slagalice neka se učitava iz datoteke (definirajte dva primjera 
   nad kojima ćete demonstirati rad svog programa). Počevši od zadane 
   pozicije, program treba pronaći i ispisati niz poteza koji dovodi do 
   rješenja, odnosno prekinuti rad ako rješenje nije pronađeno ili ako su 
   iscrpljeni računalni resursi. Dopuštenu potrošnju računalnih resursa 
   (prostornih i vremenskih, npr. broj pohranjenih čvorova i broj ukupno 
   proširenih čvorova) neka definira korisnik pri pokretanju programa.
   Detaljnije: http://www.cut-the-knot.org/pythagoras/fifteen.shtml

   Vremenska složenost opisanog rješenja je O(k * n log n), a prostorna
   O(k * n), gdje je k broj elemenata slagalice, a n broj elemenata u stablu
   pretrage.
*/
#include <iostream>
#include <vector>
#include <algorithm>
#include <map>
#include <set>
#include <iomanip>
#include <unordered_map>

using namespace std;

/* Ovo je struktura u kojoj se pamti stanje neke slagalice.
 * Pamtimo širinu i visinu slagalice te sve elemente slagalice.
 * Element sa vrijednosti 0 označava prazno polje.
 * Oko pravih elemenata slagalice dodajemo još brojeve -1 koji nam
 * olakšavaju provjeru valjanosti poteza.
 *
 * Npr. slagalica:
 * 4 _ 3
 * 2 1 5
 * pretvorila bi se u ove elemente:
 * -1 -1 -1 -1 -1
 * -1  4  0  3 -1
 * -1  2  1  5 -1
 * -1 -1 -1 -1 -1
 *
 * U samom polju e svi su elementi zapisani linearno:
 * -1, -1, -1, -1, -1, -1, 4, 0, 3, -1, -1, 2, 1, 5, -1, -1, -1, -1, -1, -1
 */
struct slagalica {
  int W, H;
  vector<int> e;
  /* Budući da se kasnije koristi asocijativno polje (struktura map) čiji je
   * ključ slagalica, a struktura map traži da svi elementi budu poredani,
   * potrebno je definirati operator <.
   *
   * U idealnom slučaju koristili bi asocijativno polje sa raspršenim
   * adresiranjem zvano unordered_map koje bi nam pružilo nešto bolje
   * performanse (O(1) umjesto O(log n)), ali malo je kompliciranije za
   * korištenje (potrebno je definirati i operator usporedbe == i vlastitu
   * "hash" funkciju).
   */
  bool operator<(const slagalica &b) const {
    return e < b.e;
  }
  /* Ispis slagalice u neki "stream". U C++-u ostream može biti npr.
   * standardni izlaz zvan cout ili npr. neka datoteka (ofstream).
   */
  friend ostream &operator<<(ostream &os, const slagalica &s) {
    for (int y = 1; y <= s.H; ++y, os << '\n') {
      for (int x = 1; x <= s.W; ++x) {
        if (int v = s.e[x + y * (s.W + 2)]) {
          cout << ' ' << setw(2) << v;
        } else {
          cout << " __";
        }
      }
    }
    return os;
  }
};

/* Malo složenija heuristička funkcija - vraća zbroj Manhattan udaljenosti
 * svakog elementa slagalice od završne pozicije tog elementa.
 */
int heuristika(const slagalica &s) {
  int rez = 0;
  for (int y = 1; y <= s.H; ++y) {
    for (int x = 1; x <= s.W; ++x) {
      if (int v = s.e[y * (s.W + 2) + x]) {
        rez += abs((v - 1) % s.W + 1 - x) + abs((v - 1) / s.W + 1 - y);
      }
    }
  }
  return rez;
}

/* Struktura u koju se spremaju svi čvorovi koji su obiđeni algoritmom A*.
 * Svi čvorovi stabla označeni su brojevima 0..n-1 (n je broj čvorova
 * stabla).
 * Svakom čvoru odgovara slagalica u tom čvoru.
 * Za svaki čvor pamti se njegov roditelj i vrijednosti funkcija F i G.
 *
 * Osim što je potrebno (za efikasnu implementaciju algoritma A*) pronaći brzo
 * koja slagalica odgovara kojem čvoru, potreban je i drugi smjer. Pomoću
 * strukture slagalica_u_id može se za zadanu slagalicu pronaći čvor u kojem
 * je ta slagalica prije viđena u vremenu O(log n).
 * Heuristička funkcija koja se koristi je monotona, pa nikada neće
 * postojati dva različita čvora u stablu sa istom slagalicom.
 */
struct stablo_pretrage {
  map<slagalica, int> slagalica_u_id;
  vector<map<slagalica, int>::iterator> id_u_slagalicu;
  unordered_map<int, int> prethodnik_cvora;
  vector<int> F, G;
};

/* Funkcija koja popuni stablo pretrage i vrati čvor u kojem je pronađeno
 * rješenje (ili -1 ako nema rješenja).
 */
int a_zvjezdica(const slagalica &pocetna, stablo_pretrage *sp) {
  /* Popis otvorenih čvorova drži se u strukturi set koja je interno
   * implementirana kao balansirano stablo. U strukturi set traženje,
   * ubacivanje i brisanje elementa operacija je koja traje O(log n)
   * vremena. Moguće je pristupiti i najmanjem elementu u vremenu O(log n).
   *
   * Ovdje se definira funkcija za usporedbu koja kaže da jedan otvoreni
   * čvor dolazi prije drugog ako je vrijednost funkcije F tog čvora manja.
   * Ukoliko su vrijednosti funkcije F jednake svejedno je potrebno
   * razlikovati elemente, pa se uzima da je manji onaj koji je prije viđen
   * (ima manji indeks).
   */
  auto usporedba = [sp](int a, int b) {
    return sp->F[a] != sp->F[b] ? sp->F[a] < sp->F[b] : a < b;
  };
  set<int, decltype(usporedba)> otvoreni(usporedba);

  /* Početna slagalica stavlja se u korijen stabla. Računaju se vrijednosti
   * funkcija F i G za početnu slagalicu.
   */
  sp->F.push_back(heuristika(pocetna));
  sp->G.push_back(0);
  /* Istovremeno se pune strukture slagalica_u_id i id_u_slagalicu. */
  sp->id_u_slagalicu.push_back(sp->slagalica_u_id.insert({pocetna, 0}).first);
  /* Tek sada je moguće ubaciti čvor 0 u listu otvorenih čvorova. */
  otvoreni.insert(0);

  while (!otvoreni.empty()) {
    /* Uzima se najmanji čvor iz liste otvorenih, tj. onaj s najmanjom
     * vrijednosti funkcije F.
     */
    int s_id = *otvoreni.begin();
    otvoreni.erase(otvoreni.begin());
    const slagalica &s = sp->id_u_slagalicu[s_id]->first;
    /* Jedino je za ciljni čvor vrijednost heuristike 0, pa ako je to slučaj
     * pronađeno je rješenje.
     */
    if (!heuristika(s)) {
      return s_id;
    }
    /* p0 označava poziciju rupe (elementa s vrijednosti 0) */
    int p0 = find(s.e.begin(), s.e.end(), 0) - s.e.begin();
    /* Pokušava se rupa zamijeniti sa jednim od četiri susjedna elementa. */
    for (int pomak : {1, -1, s.W + 2, -(s.W + 2)}) {
      /* Provjerava se da se susjedni element nalazi unutar slagalice. */
      if (s.e[p0 + pomak] >= 0) {
        slagalica nova = s;
        swap(nova.e[p0 + pomak], nova.e[p0]);

        int novi_g = sp->G[s_id] + 1, novi_f = heuristika(nova) + novi_g;
        /* Pokuša se nova slagalica ubaciti u stablo (složenost O(log n)).
         * Ako se ista slagalica već nalazi u stablu onda će varijabla i
         * pokazivati na čvor sa već postojećom slagalicom. Inače će
         * pokazivati na novoubačeni čvor.
         */
        auto i = sp->slagalica_u_id.insert({nova, sp->id_u_slagalicu.size()});
        /* Id čvora koji sadrži slagalicu "nova". */
        int nova_id = i.first->second;
        if (i.second) {
          /* Samo ako slagalica "nova" nije postojala u stablu dopunjava se
           * struktura stabla.
           */
          sp->F.push_back(0);
          sp->G.push_back(0);
          sp->id_u_slagalicu.push_back(i.first);
        } else if (novi_f < sp->F[nova_id]) {
          /* Pronađen je bolji put do postojećeg čvora u stablu, pa je
           * potrebno promijeniti vrijednost F u tom čvoru.
           * Promjena vrijednosti elementa vrši se u tri koraka (budući da
           * nije direktno podržana sturkutrom set):
           * 1. Briše se element iz liste otvorenih čvorova (O(log n)).
           * 2. Mijenja se vrijednost elementa (O(1)).
           * 3. Element se vraća u listu otvorenih čvorova (O(log n)).
           */
          otvoreni.erase(nova_id);
        } else {
          continue;
        }
        /* Spremaju se vrijednosti funkcija F i G za novi čvor koji se zatim
         * ubacuje u listu otvorenih.
         */
        sp->F[nova_id] = novi_f, sp->G[nova_id] = novi_g;
        otvoreni.insert(nova_id);
        sp->prethodnik_cvora[nova_id] = s_id;
      }
    }
  }
  return -1;
}

/* U strukturi stablo_pretrage pamte se samo roditelji, tj. prethodnici
 * čvorova. Zato je potrebno pratiti put od rješenja prema početnom čvoru i
 * ispisati ga u obrnutom redoslijedu.
 */
void ispisi_rjesenje(int id_cvora, stablo_pretrage &sp) {
  if (sp.prethodnik_cvora.count(id_cvora)) {
    ispisi_rjesenje(sp.prethodnik_cvora[id_cvora], sp);
    cout << '\n';
  }
  cout << sp.id_u_slagalicu[id_cvora]->first;
}

int main() {
  /* Učitaj slagalicu (širinu, visinu i vrijednosti svih elemenata). */
  slagalica pocetna;
  cin >> pocetna.H >> pocetna.W;
  /* Širina i visina uvećaju se za 2 i rubovi slagalice pune se
   * vrijednostima -1.
   */
  pocetna.e.resize((pocetna.H + 2) * (pocetna.W + 2), -1);
  for (int y = 1; y <= pocetna.H; ++y) {
    for (int x = 1; x <= pocetna.W; ++x) {
      cin >> pocetna.e[x + y * (pocetna.W + 2)];
    }
  }

  stablo_pretrage sp;
  int id_zavrsnog_cvora = a_zvjezdica(pocetna, &sp);
  if (id_zavrsnog_cvora >= 0) {
    cerr << "Velicina stabla pretrage: " << sp.id_u_slagalicu.size() << '\n';
    cerr << "Duljina rjesenja: " << sp.G[id_zavrsnog_cvora] << '\n';
    ispisi_rjesenje(id_zavrsnog_cvora, sp);
  }
}
