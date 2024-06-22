package ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.*;

public class Solution {

	public static File putanjaDoDatotekeUcenja;
	public static File putanjaDoDatotekeTestiranja;
	public static String[] nazivZnacajki;

	public static void main(String ... args) throws FileNotFoundException {
		int dubinaStabla = -1;
		if (args.length > 2){
			dubinaStabla = Integer.parseInt(args[2]);
		}
		putanjaDoDatotekeUcenja = new File(args[0]);
		Scanner sc = new Scanner(putanjaDoDatotekeUcenja);
		String line;
		line = sc.nextLine();
		nazivZnacajki = line.split(",");
		int redovi = 0;
		while (sc.hasNextLine()){
			sc.nextLine();
			redovi++;
		}
		String[][] vrijednostiZnacajkaUcenja = new String[redovi][nazivZnacajki.length];
		sc = new Scanner(putanjaDoDatotekeUcenja);
		int splitVar=0;
		sc.nextLine();
		while (sc.hasNextLine()){
			line = sc.nextLine();
			vrijednostiZnacajkaUcenja[splitVar] = line.split(",");
			splitVar++;
		}
		//DO OVDJE SAM UČITAO DATOTEKU ZA UCENJE
		//SAD DALJE
		putanjaDoDatotekeTestiranja = new File(args[1]);
		sc = new Scanner(putanjaDoDatotekeTestiranja);
		sc.nextLine();
		redovi = 0;
		while (sc.hasNextLine()){
			sc.nextLine();
			redovi++;
		}
		String[][] vrijednostiZnacajkaTestiranja = new String[redovi][nazivZnacajki.length];
		sc = new Scanner(putanjaDoDatotekeTestiranja);
		splitVar=0;
		sc.nextLine();
		while (sc.hasNextLine()){
			line = sc.nextLine();
			vrijednostiZnacajkaTestiranja[splitVar] = line.split(",");
			splitVar++;
		}
		//DO OVDJE SAM UČITAO DATOTEKU ZA TESTIRANJE
		//SAD DALJE


		Cvor glavniCvor = new Cvor(vrijednostiZnacajkaUcenja, nazivZnacajki, dubinaStabla);
		System.out.println("[BRANCHES]:");
		glavniCvor.ispisGrana("", 1);

		System.out.print("[PREDICTIONS]:");
		TreeSet<String> setKlasa = brojKlasa(vrijednostiZnacajkaTestiranja);
		String[][] stvarnaVsPredvidjenaKlasa = new String[vrijednostiZnacajkaTestiranja.length][2];
		TreeMap<String, TreeMap<String, Integer>> matricaKonfuzije = new TreeMap<>();
		int tocno = 0;
		int total = 0;
		for (int i=0; i< vrijednostiZnacajkaTestiranja.length; i++){
			String predikcijaKlase = glavniCvor.predikcija(vrijednostiZnacajkaTestiranja[i]);
			setKlasa.add(predikcijaKlase.toLowerCase());
			stvarnaVsPredvidjenaKlasa[i][0] = vrijednostiZnacajkaTestiranja[i][nazivZnacajki.length-1].toLowerCase();
			stvarnaVsPredvidjenaKlasa[i][1] = predikcijaKlase.toLowerCase();

			System.out.print(" " + predikcijaKlase);
			total++;
			if (predikcijaKlase.equals(vrijednostiZnacajkaTestiranja[i][nazivZnacajki.length-1])){
				tocno++;
			}
		}
		System.out.println();
		System.out.println("[ACCURACY]: " + String.format(Locale.ROOT, "%.5f", ((double)tocno/(double)total)));

		for (String stvarneKlase : setKlasa){
			TreeMap<String, Integer> pom = new TreeMap<>();
			for (String predvidjeneKlase : setKlasa){
				pom.put(predvidjeneKlase, 0);
			}
			matricaKonfuzije.put(stvarneKlase, pom);
		}
		for (String[] stvarnaVsPredvidjena : stvarnaVsPredvidjenaKlasa){
			matricaKonfuzije.get(stvarnaVsPredvidjena[0]).put(stvarnaVsPredvidjena[1], matricaKonfuzije.get(stvarnaVsPredvidjena[0]).get(stvarnaVsPredvidjena[1])+1);
		}
		System.out.println("[CONFUSION_MATRIX]:");
		for (Map.Entry<String, TreeMap<String, Integer>> entryStvarna : matricaKonfuzije.entrySet()) {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, Integer> entryPredvidjena : entryStvarna.getValue().entrySet()) {
				sb.append(entryPredvidjena.getValue() + " ");
			}
			System.out.println(sb.toString().trim());
		}


	}

	public static TreeSet<String> brojKlasa(String[][] vrijednostiZnacajkaNaTestu){
		TreeSet<String> klase = new TreeSet<>();
		int mjestoKlase = nazivZnacajki.length-1;
		for (String[] row : vrijednostiZnacajkaNaTestu) {
			klase.add(row[mjestoKlase].toLowerCase());
		}
		return klase;
	}
}
