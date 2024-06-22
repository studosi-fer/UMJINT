package ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Solution {
	public static String algoritam = "";
	public static File putanjaDoKlauzula;
	public static File putanjaDoKorisnickihNaredbi;
	public static Set<klauzula> pocetneKlauzule = new HashSet<>();
	public static klauzula pomUcitavanaKlauzula;
	public static klauzula ciljnaKlauzula;
	public static Set<klauzula> ciljneKlauzule = new HashSet<>();
	public static Set<klauzula> ciljneKlauzuleNegated = new HashSet<>();
	public static int brojIteration = 0;
	public static boolean notFound = true;
	public static klauzula pomocnaZaSearchKl = new klauzula();

	public static void main(String ... args) throws FileNotFoundException {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("resolution")) {
				algoritam = "resolution";
				putanjaDoKlauzula = new File(args[i + 1]);
				break;
			}else if (args[i].equals("cooking")) {
				algoritam = "cooking";
				putanjaDoKlauzula = new File(args[i + 1]);
				putanjaDoKorisnickihNaredbi = new File(args[i+2]);
				break;
			}
		}

		if (algoritam.equals("resolution")){
			Scanner sc = new Scanner(putanjaDoKlauzula);
			String line;
			while (sc.hasNextLine()) {
				++brojIteration;
				line = sc.nextLine();
				if (line.charAt(0) == '#') {
					continue;
				}
				//tu koristim ciljna tako da samo zadnja lijepo ostane u ciljnoj, a ostalo u setu
				pomUcitavanaKlauzula = new klauzula();
				ciljnaKlauzula = new klauzula();
				String[] varijable = line.split(" v ");
				String[] pomVar2 = line.split(" V ");
				if (pomVar2.length > varijable.length){
					varijable = pomVar2;
				}
				if (!sc.hasNextLine()){
					for (String var : varijable) {
						if (var.contains("~")){
							ciljnaKlauzula.varijableKlauzule.add(var.trim().toLowerCase());
							Set<String> ciljnaPomNegated = new HashSet<>();
							ciljnaPomNegated.add(var.trim().substring(1).toLowerCase());
							ciljneKlauzuleNegated.add(new klauzula(ciljnaPomNegated, brojIteration++));
						}else{
							ciljnaKlauzula.varijableKlauzule.add(var.trim().toLowerCase());
							Set<String> ciljnaPomNegated = new HashSet<>();
							ciljnaPomNegated.add("~" + var.trim().toLowerCase());
							ciljneKlauzuleNegated.add(new klauzula(ciljnaPomNegated, brojIteration++));
						}
					}
					break;
				}
				for (String var : varijable) {
					pomUcitavanaKlauzula.varijableKlauzule.add(var.trim().toLowerCase());
				}
				pomUcitavanaKlauzula.brojKlauzule = brojIteration;
				pocetneKlauzule.add(pomUcitavanaKlauzula);
			}
			resolution();

		}
		else if (algoritam.equals("cooking")){
			Scanner naredbeSc = new Scanner(putanjaDoKorisnickihNaredbi);
			Scanner sc = new Scanner(putanjaDoKlauzula);
			String line;
			while (sc.hasNextLine()) {
				++brojIteration;
				line = sc.nextLine();
				if (line.charAt(0) == '#') {
					continue;
				}
				//tu koristim ciljna tako da samo zadnja lijepo ostane u ciljnoj, a ostalo u setu
				pomUcitavanaKlauzula = new klauzula();
				ciljnaKlauzula = new klauzula();
				String[] varijable = line.split(" v ");
				String[] pomVar2 = line.split(" V ");
				if (pomVar2.length > varijable.length){
					varijable = pomVar2;
				}
				/*if (!sc.hasNextLine()){
					for (String var : varijable) {
						if (var.contains("~")){
							ciljnaKlauzula.varijableKlauzule.add(var.trim().toLowerCase());
							Set<String> ciljnaPomNegated = new HashSet<>();
							ciljnaPomNegated.add(var.trim().substring(1).toLowerCase());
							ciljneKlauzuleNegated.add(new klauzula(ciljnaPomNegated, brojIteration++));
						}else{
							ciljnaKlauzula.varijableKlauzule.add(var.trim().toLowerCase());
							Set<String> ciljnaPomNegated = new HashSet<>();
							ciljnaPomNegated.add("~" + var.trim().toLowerCase());
							ciljneKlauzuleNegated.add(new klauzula(ciljnaPomNegated, brojIteration++));
						}
					}
					break;
				}*/
				for (String var : varijable) {
					pomUcitavanaKlauzula.varijableKlauzule.add(var.trim().toLowerCase());
				}
				pomUcitavanaKlauzula.brojKlauzule = brojIteration;
				pocetneKlauzule.add(pomUcitavanaKlauzula);
			}
			//NAREDBE
			//NAREDBE
			//NAREDBE
			while (naredbeSc.hasNextLine()){
				line = naredbeSc.nextLine();
				if (line.charAt(0) == '#') {
					continue;
				}
				System.out.println("\nUser’s command: " + line);

				if (line.endsWith("?")){
					pomUcitavanaKlauzula = new klauzula();
					ciljnaKlauzula = new klauzula();
					ciljneKlauzuleNegated.clear();
					String[] varijable = line.substring(0,line.length()-2).split(" v ");
					String[] pomVar2 = line.substring(0,line.length()-2).split(" V ");
					if (pomVar2.length > varijable.length){
						varijable = pomVar2;
					}
					for (String var : varijable) {
						if (var.contains("~")) {
							ciljnaKlauzula.varijableKlauzule.add(var.trim().toLowerCase());
							Set<String> ciljnaPomNegated = new HashSet<>();
							ciljnaPomNegated.add(var.trim().substring(1).toLowerCase());
							ciljneKlauzuleNegated.add(new klauzula(ciljnaPomNegated, ++brojIteration));
						} else {
							ciljnaKlauzula.varijableKlauzule.add(var.trim().toLowerCase());
							Set<String> ciljnaPomNegated = new HashSet<>();
							ciljnaPomNegated.add("~" + var.trim().toLowerCase());
							ciljneKlauzuleNegated.add(new klauzula(ciljnaPomNegated, ++brojIteration));
						}
					}
					resolution();
					System.out.println("\n");
				}else{
					pomUcitavanaKlauzula = new klauzula();
					String[] varijable = line.substring(0,line.length()-2).split(" v ");
					String[] pomVar2 = line.substring(0, line.length()-2).split(" V ");
					if (pomVar2.length > varijable.length){
						varijable = pomVar2;
					}

					if (line.endsWith("-")){
						for (String var : varijable) {
							pomUcitavanaKlauzula.varijableKlauzule.add(var.trim().toLowerCase());
						}
						pocetneKlauzule.remove(pomUcitavanaKlauzula);
					}
					else if (line.endsWith("+")){
						for (String var : varijable) {
							pomUcitavanaKlauzula.varijableKlauzule.add(var.trim().toLowerCase());
						}
						pomUcitavanaKlauzula.brojKlauzule = ++brojIteration;
						pocetneKlauzule.add(pomUcitavanaKlauzula);
					}
				}
			}

		}

	}

	public static void resolution(){
		Set <klauzula> startPlusOldNew = new HashSet<>();
		startPlusOldNew.addAll(pocetneKlauzule);
		Set <klauzula> newKlauzule = new HashSet<>();
		Set <klauzula> newNewestKlauzule = new HashSet<>();
		Set <klauzula> pom;
		newKlauzule.addAll(ciljneKlauzuleNegated);
		deleteRedundant(startPlusOldNew);
		deleteUnimportant(startPlusOldNew);
		boolean thisIterationAddedNew = true;
		while (thisIterationAddedNew){
			thisIterationAddedNew = false;
			for (klauzula sos : newKlauzule){
				for (klauzula staraKlauzula : startPlusOldNew){
					if ((pom = resolveNew(sos, staraKlauzula)) != null)
						if (!startPlusOldNew.contains(pom) && !newKlauzule.contains(pom)) {
							newNewestKlauzule.addAll(pom);
							thisIterationAddedNew = true;
						}
				}
				startPlusOldNew.add(sos);
			}
				deleteUnimportant(newNewestKlauzule);
				deleteRedundant(newNewestKlauzule);
				newKlauzule.clear();
				newKlauzule.addAll(newNewestKlauzule);
				newNewestKlauzule.clear();
				deleteUnimportant(startPlusOldNew);
				deleteRedundant(startPlusOldNew);

				Set <String> pomocnaZaSearch = new HashSet<>();
				pomocnaZaSearch.add("NIL");
				pomocnaZaSearchKl = new klauzula();
				pomocnaZaSearchKl.varijableKlauzule = pomocnaZaSearch;
				if (newKlauzule.contains(pomocnaZaSearchKl)){
					break;
				}
		}
		if (newKlauzule.contains(pomocnaZaSearchKl)){
			//onda ispiši onak kak treba
			//cijeli ovaj dio dolje je za poredanje i ispis
			Queue<klauzula> priorityListaRoditelja = new PriorityQueue<>(Comparator.comparingInt((klauzula o) -> o.brojKlauzule));
			Set<klauzula> noviRoditelji = new HashSet<>();
			Set<klauzula> noveKlauzule = new HashSet<>();
			klauzula nilKlauzula = new klauzula();
			for (klauzula izvadiVan : newKlauzule){
				if (izvadiVan.varijableKlauzule.contains("NIL")){
					nilKlauzula = izvadiVan;
					break;
				}
			}
			noveKlauzule.add(nilKlauzula);
			while (!noveKlauzule.isEmpty()){
				for (klauzula kl : noveKlauzule){
					if (kl.roditelj1 != null && kl.roditelj2 != null){
						priorityListaRoditelja.add(kl);
					}
					if (kl.roditelj1.roditelj1 != null && kl.roditelj1.roditelj2 != null){
						noviRoditelji.add(kl.roditelj1);
					}
					if (kl.roditelj2.roditelj1 != null && kl.roditelj2.roditelj2 != null){
						noviRoditelji.add(kl.roditelj2);
					}
				}
				noveKlauzule.clear();
				noveKlauzule.addAll(noviRoditelji);
				noviRoditelji.clear();
			}

			//OVO JE DRUGI ISPIS
			//
			/*while (!noveKlauzule.isEmpty()){
				for (klauzula kl : noveKlauzule){
					if (kl != null){
						priorityListaRoditelja.add(kl);
					}
					if (kl.roditelj1 != null){
						noviRoditelji.add(kl.roditelj1);
					}
					if (kl.roditelj2 != null){
						noviRoditelji.add(kl.roditelj2);
					}
				}
				noveKlauzule.clear();
				noveKlauzule.addAll(noviRoditelji);
				noviRoditelji.clear();
			}*/
			//
			//DO OVDJE JE DRUGI ISPIS

			priorityListaRoditelja.addAll(pocetneKlauzule);
			priorityListaRoditelja.addAll(ciljneKlauzuleNegated);
			int brojac = 1;
			Queue<klauzula> priorityListaRoditeljaPom = new PriorityQueue<>(Comparator.comparingInt((klauzula o) -> o.brojKlauzule));
			while (!priorityListaRoditelja.isEmpty()){
				klauzula pomocna = priorityListaRoditelja.poll();
				pomocna.brojKlauzule = brojac++;
				priorityListaRoditeljaPom.add(pomocna);
			}
			priorityListaRoditelja = priorityListaRoditeljaPom;

			for (int i=1; i<pocetneKlauzule.size()+ciljneKlauzuleNegated.size()+1; i++){
				System.out.println(i + ". " + cnfIspis(priorityListaRoditelja.poll().varijableKlauzule));
			}
			klauzula ispis = new klauzula();

			//OVO JE DRUGI ISPIS
			//
			/*while (!priorityListaRoditelja.isEmpty()){
			ispis = priorityListaRoditelja.poll();
			if (ispis.roditelj1 != null || ispis.roditelj2 != null)
				break;
			System.out.println(ispis.brojKlauzule + ". " + cnfIspis(ispis.varijableKlauzule));
			}*/
			//
			//OVO JE DRUGI ISPIS

			System.out.println("===============");

			//OVO SE TIČE DRUGAČIJEG ISPISA
			//
			//System.out.println(ispis.brojKlauzule + ". " + cnfIspis(ispis.varijableKlauzule) + " (" + ispis.roditelj1.brojKlauzule + ", " + ispis.roditelj2.brojKlauzule + ")");
			//
			//OVO SE TIČE DRUGAČIJEG ISPISA

			while (!priorityListaRoditelja.isEmpty()){
				ispis = priorityListaRoditelja.poll();
				System.out.println(ispis.brojKlauzule + ". " + cnfIspis(ispis.varijableKlauzule) + " (" + ispis.roditelj1.brojKlauzule + ", " + ispis.roditelj2.brojKlauzule + ")");
			}
			System.out.println("===============");
			System.out.println("[CONCLUSION]: " + cnfIspis(ciljnaKlauzula.varijableKlauzule) + " is true");

		}
		else{
			Queue<klauzula> priorityListaRoditelja = new PriorityQueue<>(Comparator.comparingInt((klauzula o) -> o.brojKlauzule));
			priorityListaRoditelja.addAll(pocetneKlauzule);
			priorityListaRoditelja.addAll(ciljneKlauzuleNegated);
			for (int i=1; i<pocetneKlauzule.size()+ciljneKlauzuleNegated.size()+1; i++){
				System.out.println(i + ". " + cnfIspis(priorityListaRoditelja.poll().varijableKlauzule));
			}
			System.out.println("===============");
			while (!priorityListaRoditelja.isEmpty()){
				klauzula ispis = priorityListaRoditelja.poll();
				System.out.println(ispis.brojKlauzule + ". " + cnfIspis(ispis.varijableKlauzule) + " (" + ispis.roditelj1.brojKlauzule + ", " + ispis.roditelj2.brojKlauzule + ")");
			}
			System.out.println("===============");

			//AKO ŽELIM SAMO ISPIS UNKNOWN
			//ONDA OVO GORE ZAKOMENTIRAM
			System.out.println("[CONCLUSION]: " + cnfIspis(ciljnaKlauzula.varijableKlauzule) + " is unknown");
		}
	}

	public static void deleteRedundant(Set<klauzula> klauzule){
		Set<klauzula> redundant = new HashSet<>();
		for (klauzula it1 : klauzule){
			for (klauzula it2 : klauzule){
				if (!(it1.equals(it2))){
					if (it2.varijableKlauzule.containsAll(it1.varijableKlauzule)){
						redundant.add(it2);
					}
				}
			}
		}
		klauzule.removeAll(redundant);
	}

	public static void deleteUnimportant(Set<klauzula> klauzule){
		Set<klauzula> unimportant = new HashSet<>();
		for (klauzula kl : klauzule){
			for (String str : kl.varijableKlauzule){
				if (str.startsWith("~")){
					if (kl.varijableKlauzule.contains(str.substring(1))){
						unimportant.add(kl);
						break;
					}
				}else{
					if (kl.varijableKlauzule.contains("~" + str)){
						unimportant.add(kl);
						break;
					}
				}
			}
		}
		klauzule.removeAll(unimportant);
	}

	public static Set<klauzula> resolveNew(klauzula clause1, klauzula clause2){
		Set<klauzula> nova = new HashSet<>();
		boolean napravljenaNova = false;

		for (String kl1 : clause1.varijableKlauzule) {
			for (String kl2 : clause2.varijableKlauzule){
				if (kl1.equals(kl2.substring(1)) || kl2.equals(kl1.substring(1))){
					Set<String> pom = new HashSet<>();
					pom.addAll(clause1.varijableKlauzule);
					pom.addAll(clause2.varijableKlauzule);
					pom.remove(kl1);
					pom.remove(kl2);
					if (pom.isEmpty()){
						pom.add("NIL");
						nova.add(new klauzula(pom,++brojIteration, clause1, clause2));
					}else {
						nova.add(new klauzula(pom, ++brojIteration, clause1, clause2));
					}
					napravljenaNova = true;
				}
			}
		}
		if (napravljenaNova){
			return nova;
		}else{
			return null;
		}
	}

	public static String cnfIspis(Set<String> klauzula){
		StringBuilder sb = new StringBuilder();
		for (String str : klauzula){
			sb.append(str + " v ");
		}
		sb.delete(sb.length()-3, sb.length());
		return sb.toString();
	}
}
