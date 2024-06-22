package ui;

import java.util.*;

public class Cvor {

    final class entropijaPlusBrojRedovaSTomVrijednosti
    {
        public int brojRedova;
        public double entropija;

        public entropijaPlusBrojRedovaSTomVrijednosti (int brojRedova, double entropija)
        {
            this.brojRedova = brojRedova;
            this.entropija = entropija;
        }
    }

    //svaka znacajka ima svoje vrijednosti
    //svaka vrijednost ima svoje klase (neke finalne rezultate/klasifikacije)
    //finalnih klasa/rezultata ima neki broj
    public Map<String, HashSet<String>> popisVrijednostiZnacajkiKojeMoguBitiUOvomCvoru = new HashMap<>();
    public Map<String, Double> informacijskaDobitZaSvakuZnacajku;
    public Double entropijaOvogCvora;
    public Map<String, Cvor> slijedeciCvorPoNazivuVrijednostiZnacajke;
    public String klasa = "";
    public String znacajkaOvogCvora;
    public static String[][] vrijednostiZnacajkaUcenja;
    public static String[] nazivZnacajki;
    public String[][] vrijednostiZnacajkaUcenjaZaPretrazivanjeZaOvajCvor;
    public static int maxDubina;
    public int dubinaOvogCvora;


    public Cvor(String[][] vrijednostiZnacajkaUcenjaParam, String[] nazivZnacajkiParam, int maxDubinaParam){
        if (maxDubinaParam == -1){
            maxDubina = Integer.MAX_VALUE;
        }else{
            maxDubina = maxDubinaParam;
        }
        vrijednostiZnacajkaUcenja = vrijednostiZnacajkaUcenjaParam;
        nazivZnacajki = nazivZnacajkiParam;
        vrijednostiZnacajkaUcenjaZaPretrazivanjeZaOvajCvor = Arrays.stream(vrijednostiZnacajkaUcenjaParam).map(String[]::clone).toArray(String[][]::new);
        //za svaku znacajku
        for (int k = 0; k< nazivZnacajki.length-1; k++) {
            popisVrijednostiZnacajkiKojeMoguBitiUOvomCvoru.put(nazivZnacajki[k], new HashSet<String>());
            //za svaki red
            for (int j = 0; j< vrijednostiZnacajkaUcenja.length; j++) {
                //System.out.print(vrijednostiZnacajkaUcenja[k][j] + " ");
                popisVrijednostiZnacajkiKojeMoguBitiUOvomCvoru.get(nazivZnacajki[k]).add(vrijednostiZnacajkaUcenja[j][k]);
            }
        }
        entropijaOvogCvora = entropijaZaOvajCvor();
        informacijskaDobitZaSvakuZnacajku = informacijskaDobitZaSvakuZnacajku();
        znacajkaOvogCvora = vratiNajboljuZnacajkuPoInformacijskojDobiti();
        //System.out.println();
        slijedeciCvorPoNazivuVrijednostiZnacajke = vratiSljedeceCvorove();

    }

    public Cvor(String[][] vrijednostiZnacajkaUcenjaZaPretrazivanjeZaOvajCvor, Map<String, HashSet<String>> popisVrijednostiZnacajkiKojeMoguBitiUOvomCvoru, int dubinaOvogCvoraParam){
        this.vrijednostiZnacajkaUcenjaZaPretrazivanjeZaOvajCvor = vrijednostiZnacajkaUcenjaZaPretrazivanjeZaOvajCvor;
        this.popisVrijednostiZnacajkiKojeMoguBitiUOvomCvoru = popisVrijednostiZnacajkiKojeMoguBitiUOvomCvoru;
        dubinaOvogCvora = dubinaOvogCvoraParam;

        entropijaOvogCvora = entropijaZaOvajCvor();
        informacijskaDobitZaSvakuZnacajku = informacijskaDobitZaSvakuZnacajku();
        znacajkaOvogCvora = vratiNajboljuZnacajkuPoInformacijskojDobiti();
        if (entropijaOvogCvora.equals(0.0)){
            klasa = vrijednostiZnacajkaUcenjaZaPretrazivanjeZaOvajCvor[0][nazivZnacajki.length-1];
        }
        else if (dubinaOvogCvora >= maxDubina){
            Map<String, Integer> racunanje = new HashMap<>();
            int mjestoKlase = nazivZnacajki.length-1;
            int maxPojavljanje = 0;
            String maxPojavljanjeKlasa = "";
            for (String[] row : vrijednostiZnacajkaUcenjaZaPretrazivanjeZaOvajCvor) {
                if (racunanje.get(row[mjestoKlase]) == null){
                    racunanje.put(row[mjestoKlase], 1);
                }else{
                    racunanje.put(row[mjestoKlase], racunanje.get(row[mjestoKlase])+1);
                }
            }
            for (Map.Entry<String,Integer> entry : racunanje.entrySet()){
                if (entry.getValue() > maxPojavljanje){
                    maxPojavljanjeKlasa = entry.getKey();
                    maxPojavljanje = entry.getValue();
                }else if (entry.getValue().equals(maxPojavljanje)){
                    if (entry.getKey().compareToIgnoreCase(maxPojavljanjeKlasa) < 0){
                        maxPojavljanjeKlasa = entry.getKey();
                    }
                }
            }
            klasa = maxPojavljanjeKlasa;
        }
        else{
            slijedeciCvorPoNazivuVrijednostiZnacajke = vratiSljedeceCvorove();
        }
    }

    public void ispisGrana(String dosadasnjeGrane, int grana){
        if (!klasa.equals("")){
            System.out.print(dosadasnjeGrane + klasa + "\n");
        }
        else {
            for (Map.Entry<String, Cvor> entry : slijedeciCvorPoNazivuVrijednostiZnacajke.entrySet()) {
                entry.getValue().ispisGrana(dosadasnjeGrane + grana + ":" + znacajkaOvogCvora + "=" + entry.getKey() + " ", grana+1);
            }
        }
    }

    public String predikcija(String[] redTestiranja){
        if (!klasa.equals("")){
            return klasa;
        }else{
            int position = returnNazivZnacajkePosition(znacajkaOvogCvora);
            for (Map.Entry<String, Cvor> entry : slijedeciCvorPoNazivuVrijednostiZnacajke.entrySet()) {
                if (entry.getKey().equals(redTestiranja[position])){
                    return entry.getValue().predikcija(redTestiranja);
                }
            }
            //TU MORA
            //ITI JEDNA ITERACIJA
            //DA SE ZRAČUNA ČEGA IMA
            //NAJVIŠE
            Map<String, Integer> racunanje = new HashMap<>();
            int mjestoKlase = nazivZnacajki.length-1;
            int maxPojavljanje = 0;
            String maxPojavljanjeKlasa = "";
            for (String[] row : vrijednostiZnacajkaUcenjaZaPretrazivanjeZaOvajCvor) {
                if (racunanje.get(row[mjestoKlase]) == null){
                    racunanje.put(row[mjestoKlase], 1);
                }else{
                    racunanje.put(row[mjestoKlase], racunanje.get(row[mjestoKlase])+1);
                }
            }
            for (Map.Entry<String,Integer> entry : racunanje.entrySet()){
                if (entry.getValue() > maxPojavljanje){
                    maxPojavljanjeKlasa = entry.getKey();
                    maxPojavljanje = entry.getValue();
                }else if (entry.getValue().equals(maxPojavljanje)){
                    if (entry.getKey().compareToIgnoreCase(maxPojavljanjeKlasa) < 0){
                        maxPojavljanjeKlasa = entry.getKey();
                    }
                }
            }
            return maxPojavljanjeKlasa;
        }
    }

    public Map<String, Cvor> vratiSljedeceCvorove(){
        Map<String, Cvor> rtrMap = new HashMap<>();
        Map<String, HashSet<String>> popisVrijednostiZnacajkiZaSljedeceCvorove = new HashMap<>(popisVrijednostiZnacajkiKojeMoguBitiUOvomCvoru);
        popisVrijednostiZnacajkiZaSljedeceCvorove.remove(znacajkaOvogCvora);
        //EKSPERIMENTALNO
        //EKSPERIMENTALNO
        /*for (Map.Entry<String, HashSet<String>> entry : popisVrijednostiZnacajkiZaSljedeceCvorove.entrySet()) {
            entry.getValue().clear();
        }*/

        //EKSPERIMENTALNO
        //EKSPERIMENTALNO
        int positionZnacajke = returnNazivZnacajkePosition(znacajkaOvogCvora);

        for (String vrijednost : popisVrijednostiZnacajkiKojeMoguBitiUOvomCvoru.get(znacajkaOvogCvora)){
            ArrayList<ArrayList<String>> vrijednostiZnacajkaUcenjaZaPretrazivanjeZaSljedeciCvorList = new ArrayList<>();

            for (int i=0; i< vrijednostiZnacajkaUcenjaZaPretrazivanjeZaOvajCvor.length; i++){
                if (vrijednostiZnacajkaUcenjaZaPretrazivanjeZaOvajCvor[i][positionZnacajke].equals(vrijednost)){
                    vrijednostiZnacajkaUcenjaZaPretrazivanjeZaSljedeciCvorList.add(new ArrayList<>(Arrays.asList(vrijednostiZnacajkaUcenjaZaPretrazivanjeZaOvajCvor[i])));
                }
            }
            String[][] vrijednostiZnacajkaUcenjaZaPretrazivanjeZaSljedeciCvor = new String[vrijednostiZnacajkaUcenjaZaPretrazivanjeZaSljedeciCvorList.size()][];
            for (int i = 0; i < vrijednostiZnacajkaUcenjaZaPretrazivanjeZaSljedeciCvorList.size(); i++) {
                ArrayList<String> row = vrijednostiZnacajkaUcenjaZaPretrazivanjeZaSljedeciCvorList.get(i);
                vrijednostiZnacajkaUcenjaZaPretrazivanjeZaSljedeciCvor[i] = row.toArray(new String[row.size()]);
            }
            //EXPERIMENTALNO
            /*for (Map.Entry<String, HashSet<String>> entry : popisVrijednostiZnacajkiKojeMoguBitiUOvomCvoru.entrySet()) {
                if (entry.getKey().equals(znacajkaOvogCvora)){
                    continue;
                }
                int pozicijaZnacajkePom = returnNazivZnacajkePosition(entry.getKey());
                for (String[] vrijednosti : vrijednostiZnacajkaUcenjaZaPretrazivanjeZaSljedeciCvor) {
                    //ne mogu po svim znacajkama
                    //moram po onim od roditelja minus njegova
                    popisVrijednostiZnacajkiZaSljedeceCvorove.get(nazivZnacajki[pozicijaZnacajkePom]).add(vrijednosti[pozicijaZnacajkePom]);
                }
            }*/
            //EXPERIMENTALNO
            if (vrijednostiZnacajkaUcenjaZaPretrazivanjeZaSljedeciCvor.length != 0) {
                rtrMap.put(vrijednost, new Cvor(vrijednostiZnacajkaUcenjaZaPretrazivanjeZaSljedeciCvor, popisVrijednostiZnacajkiZaSljedeceCvorove, dubinaOvogCvora + 1));
            }
        }
        return rtrMap;
    }

    public Double entropijaZaOvajCvor(){
        Map<String, Integer> racunanje = new HashMap<>();
        int mjestoKlase = nazivZnacajki.length-1;
        int total = 0;
        double rezultat = 0;
        for (String[] row : vrijednostiZnacajkaUcenjaZaPretrazivanjeZaOvajCvor) {
            if (racunanje.get(row[mjestoKlase]) == null){
                racunanje.put(row[mjestoKlase], 1);
                total++;
            }else{
                racunanje.put(row[mjestoKlase], racunanje.get(row[mjestoKlase])+1);
                total++;
            }
        }
        for (Map.Entry<String,Integer> entry : racunanje.entrySet()){
            rezultat = rezultat + (- (entry.getValue()/(double)total) * log2(entry.getValue()/(double)total));
        }
        return rezultat;
    }

    public entropijaPlusBrojRedovaSTomVrijednosti entropijaZaOdabranuVrijednost(String vrijednost, int pozicija){
        Map<String, Integer> racunanje = new HashMap<>();
        int mjestoKlase = nazivZnacajki.length-1;
        int total = 0;
        double rezultat = 0;
        for (String[] row : vrijednostiZnacajkaUcenjaZaPretrazivanjeZaOvajCvor) {
            if (row[pozicija].equals(vrijednost)) {
                if (racunanje.get(row[mjestoKlase]) == null) {
                    racunanje.put(row[mjestoKlase], 1);
                    total++;
                } else {
                    racunanje.put(row[mjestoKlase], racunanje.get(row[mjestoKlase]) + 1);
                    total++;
                }
            }
        }
        for (Map.Entry<String,Integer> entry : racunanje.entrySet()){
            rezultat = rezultat + (- (entry.getValue()/(double)total) * log2(entry.getValue()/(double)total));
        }
        return new entropijaPlusBrojRedovaSTomVrijednosti(total, rezultat);
    }

    public Map<String, Double> informacijskaDobitZaSvakuZnacajku() {
        Map<String, Double> informacijskaDobitZaSvakuZnacajkuUFunkciji = new HashMap<>();
        for (Map.Entry<String, HashSet<String>> entry : popisVrijednostiZnacajkiKojeMoguBitiUOvomCvoru.entrySet()) {
            Double informacijskaDobitTrenutneIteriraneZnacajke = entropijaOvogCvora;
            int pozicijaVrijednosti = returnNazivZnacajkePosition(entry.getKey());
            for (String vrijednost : entry.getValue()) {
                entropijaPlusBrojRedovaSTomVrijednosti rez = entropijaZaOdabranuVrijednost(vrijednost, pozicijaVrijednosti);
                informacijskaDobitTrenutneIteriraneZnacajke = informacijskaDobitTrenutneIteriraneZnacajke - (((double)rez.brojRedova/ vrijednostiZnacajkaUcenjaZaPretrazivanjeZaOvajCvor.length) * rez.entropija);
            }
            informacijskaDobitZaSvakuZnacajkuUFunkciji.put(entry.getKey(), informacijskaDobitTrenutneIteriraneZnacajke);
        }
        return informacijskaDobitZaSvakuZnacajkuUFunkciji;
    }


    public static double log2(double v) {
        return Math.log(v) / Math.log(2.0);
    }

    public static int returnNazivZnacajkePosition(String naziv){
        for (int i = 0; i< nazivZnacajki.length; i++){
            if (nazivZnacajki[i].equals(naziv)){
                return i;
            }
        }
        return -1;
    }

    public String vratiNajboljuZnacajkuPoInformacijskojDobiti(){
        Double najveca = 0.0;
        String znacajka = "";
        for (Map.Entry<String, Double> entry : informacijskaDobitZaSvakuZnacajku.entrySet()) {
            if (entry.getValue() > najveca){
                znacajka = entry.getKey();
                najveca = entry.getValue();
            }else if (entry.getValue().equals(najveca)){
                if (entry.getKey().compareToIgnoreCase(znacajka) < 0){
                    znacajka = entry.getKey();
                }
            }
        }
        return znacajka;
    }
}