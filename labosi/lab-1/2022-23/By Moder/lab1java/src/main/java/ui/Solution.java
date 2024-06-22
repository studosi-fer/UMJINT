package ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Solution {
	public static String algoritam = "";
	public static File putanjaDoOpisnikaProstoraStanja;
	public static File putanjaDoOpisnikaHeuristike;
	public static String checkHeuristic = "";
	public static String pocetnoStanje;
	public static List<String> ciljnaStanja = new ArrayList<>();
	public static Map<String, Double> heuristikaVrijednosti = new TreeMap<>();
	public static Map<String, TreeMap<String, Double>> funkcijePrijelaza = new TreeMap<>();

	public static void main(String... args) throws FileNotFoundException {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--alg")) {
				algoritam = args[i + 1];
			}
			if (args[i].equals("--ss")) {
				putanjaDoOpisnikaProstoraStanja = new File(args[i + 1]);
				Scanner sc = new Scanner(putanjaDoOpisnikaProstoraStanja);
				String line;
				boolean procitanPrviRed = false;
				boolean procitanDrugiRed = false;
				while (sc.hasNextLine()) {
					line = sc.nextLine();
					if (line.charAt(0) == '#') {
						continue;
					}
					if (!procitanPrviRed) {
						pocetnoStanje = line;
						procitanPrviRed = true;
						continue;
					} else if (!procitanDrugiRed) {
						ciljnaStanja = Arrays.asList(line.split(" "));
						procitanDrugiRed = true;
						continue;
					}
					//ovo je malo komplicirano, ali basicly dodajem u mapu stanje
					//i u value mapu susjeda sa value-ima
					String[] split = line.split(":");
					String[] susjednaStanjaSaPodacima = new String[0];
					if (split.length > 1) {
						susjednaStanjaSaPodacima = split[1].trim().split(" ");
					}
					TreeMap<String, Double> temp = new TreeMap<>();
					for (String susjed : susjednaStanjaSaPodacima) {
						temp.put(susjed.split(",")[0], Double.parseDouble(susjed.split(",")[1]));
					}
					if (!temp.isEmpty()) {
						funkcijePrijelaza.put(split[0], temp);
					}
				}
			}
			if (args[i].equals("--h")) {
				putanjaDoOpisnikaHeuristike = new File(args[i + 1]);
				Scanner sc = new Scanner(putanjaDoOpisnikaHeuristike);
				String line;
				while (sc.hasNextLine()) {
					line = sc.nextLine();
					if (line.charAt(0) == '#') {
						continue;
					}
					heuristikaVrijednosti.put(line.split(":")[0], Double.parseDouble(line.split(":")[1].trim()));
				}
			}
			if (args[i].equals("--check-optimistic")) {
				checkHeuristic = args[i];
			}
			if (args[i].equals("--check-consistent")) {
				checkHeuristic = args[i];
			}
		}
		if (algoritam.equals("bfs")) {
			bfs();
		} else if (algoritam.equals("ucs")) {
			ucs();
		} else if (algoritam.equals("astar")) {
			astar();
		} else if (checkHeuristic.equals("--check-consistent")) {
			constistentCheck();
		} else if (checkHeuristic.equals("--check-optimistic")) {
			optimisticCheck();
		}
	}

	public static void bfs() {
		System.out.println("# BFS");
		Queue<Stanje> open = new LinkedList<>();
		Set<String> closed = new HashSet<>();
		Stanje trenutnoStanje = new Stanje(pocetnoStanje, 1, 0.0);
		open.add(trenutnoStanje);
		int statesVisited = 0;
		boolean foundSolution = false;
		while (!open.isEmpty()) {
			trenutnoStanje = open.remove();
			closed.add(trenutnoStanje.imeStanja);
			statesVisited++;
			if (ciljnaStanja.contains(trenutnoStanje.imeStanja)) {
				//OVDJE SE MORAM VAN ISPETLJATI I ISPISAT KAJ TREBA
				foundSolution = true;
				break;
			}
			if (funkcijePrijelaza.get(trenutnoStanje.imeStanja) == null) {
				continue;
			}
			for (Map.Entry<String, Double> susjedi : funkcijePrijelaza.get(trenutnoStanje.imeStanja).entrySet()) {
				//OVDJE EKSPANDIRAM
				if (!closed.contains(susjedi.getKey())) {
					open.add(new Stanje(susjedi.getKey(), trenutnoStanje, trenutnoStanje.pathLength + 1, trenutnoStanje.costToHere + susjedi.getValue()));
				}
			}
		}
		if (foundSolution == true) {
			System.out.println("[FOUND_SOLUTION]: yes");
			System.out.println("[STATES_VISITED]: " + statesVisited);
			System.out.println("[PATH_LENGTH]: " + trenutnoStanje.pathLength);
			System.out.println("[TOTAL_COST]: " + trenutnoStanje.costToHere);
			Stack<String> path = new Stack<>();
			while (trenutnoStanje != null) {
				path.push(trenutnoStanje.imeStanja);
				trenutnoStanje = trenutnoStanje.roditelj;
			}
			System.out.print("[PATH]: " + path.pop());
			while (path.size() != 0) {
				System.out.print(" => " + path.pop());
			}
			System.out.print("\n");
		} else if (foundSolution == false) {
			System.out.println("[FOUND_SOLUTION]: no");
		}
	}

	public static void ucs(String... args) {
		System.out.println("# UCS");
		Queue<Stanje> open = new PriorityQueue<>();
		Set<String> closed = new HashSet<>();
		Stanje trenutnoStanje = new Stanje(pocetnoStanje, 1, 0.0);
		open.add(trenutnoStanje);
		int statesVisited = 0;
		boolean foundSolution = false;
		while (!open.isEmpty()) {
			trenutnoStanje = open.remove();
			closed.add(trenutnoStanje.imeStanja);
			statesVisited++;
			if (ciljnaStanja.contains(trenutnoStanje.imeStanja)) {
				//OVDJE SE MORAM VAN ISPETLJATI I ISPISAT KAJ TREBA
				foundSolution = true;
				break;
			}
			if (funkcijePrijelaza.get(trenutnoStanje.imeStanja) == null) {
				continue;
			}
			for (Map.Entry<String, Double> susjedi : funkcijePrijelaza.get(trenutnoStanje.imeStanja).entrySet()) {
				//OVDJE EKSPANDIRAM
				if (!closed.contains(susjedi.getKey())) {
					open.add(new Stanje(susjedi.getKey(), trenutnoStanje, trenutnoStanje.pathLength + 1, trenutnoStanje.costToHere + susjedi.getValue()));
				}
			}
		}
		if (foundSolution == true) {
			System.out.println("[FOUND_SOLUTION]: yes");
			System.out.println("[STATES_VISITED]: " + statesVisited);
			System.out.println("[PATH_LENGTH]: " + trenutnoStanje.pathLength);
			System.out.println("[TOTAL_COST]: " + trenutnoStanje.costToHere);
			Stack<String> path = new Stack<>();
			while (trenutnoStanje != null) {
				path.push(trenutnoStanje.imeStanja);
				trenutnoStanje = trenutnoStanje.roditelj;
			}
			System.out.print("[PATH]: " + path.pop());
			while (path.size() != 0) {
				System.out.print(" => " + path.pop());
			}
			System.out.print("\n");
		} else if (foundSolution == false) {
			System.out.println("[FOUND_SOLUTION]: no");
		}
	}

	//TREBA ASTAR POPRAVITI
	public static void astar(String... args) {
		System.out.println("# A-STAR " + putanjaDoOpisnikaHeuristike.getPath());
		Queue<Stanje> open = new PriorityQueue<>();
		Set<String> closed = new HashSet<>();
		Stanje trenutnoStanje = new Stanje(pocetnoStanje, 1, 0.0, 0.0 + heuristikaVrijednosti.get(pocetnoStanje));
		open.add(trenutnoStanje);
		int statesVisited = 0;
		boolean foundSolution = false;
		while (!open.isEmpty()) {
			trenutnoStanje = open.remove();
			closed.add(trenutnoStanje.imeStanja);
			statesVisited++;
			if (ciljnaStanja.contains(trenutnoStanje.imeStanja)) {
				//OVDJE SE MORAM VAN ISPETLJATI I ISPISAT KAJ TREBA
				foundSolution = true;
				break;
			}
			if (funkcijePrijelaza.get(trenutnoStanje.imeStanja) == null) {
				continue;
			}
			for (Map.Entry<String, Double> susjedi : funkcijePrijelaza.get(trenutnoStanje.imeStanja).entrySet()) {
				//OVDJE EKSPANDIRAM
				Stanje zaCompareStanje = new Stanje(susjedi.getKey(), trenutnoStanje, trenutnoStanje.pathLength + 1,
						trenutnoStanje.costToHere + susjedi.getValue(),
						trenutnoStanje.costToHere + susjedi.getValue() + heuristikaVrijednosti.get(susjedi.getKey()));

				/*if (/*closed.contains(zaCompareStanje) || open.contains(zaCompareStanje)) {
					//OK OVO TU SAM SE JAKO DOBRO SNASEL, BASICLY JER SAM PROMIJENIL EQUAL SAM ZAKAKAL
					//PA SAD GLEDAM AK NEŠ REMOVAM PO LOGICI DA JE G(m) VECI ILI JEDNAKI
					//ZNACI DA OVOG TRENUTNOG MORAM DODATI
					//INACE SAMO CONTINUE I NE DODAJEM NIŠ
					if (open.removeIf(stanje -> (stanje.costPlusHeuristic >= zaCompareStanje.costPlusHeuristic))/* || closed.removeIf(stanje -> (stanje.costToHere >= zaCompareStanje.costToHere))) {

					} else {
						continue;
					}
				}*/
				if (!closed.contains(susjedi.getKey()))
					open.add(zaCompareStanje);
			}
		}
		if (foundSolution == true) {
			System.out.println("[FOUND_SOLUTION]: yes");
			System.out.println("[STATES_VISITED]: " + statesVisited);
			System.out.println("[PATH_LENGTH]: " + trenutnoStanje.pathLength);
			System.out.println("[TOTAL_COST]: " + trenutnoStanje.costToHere);
			Stack<String> path = new Stack<>();
			while (trenutnoStanje != null) {
				path.push(trenutnoStanje.imeStanja);
				trenutnoStanje = trenutnoStanje.roditelj;
			}
			System.out.print("[PATH]: " + path.pop());
			while (path.size() != 0) {
				System.out.print(" => " + path.pop());
			}
			System.out.print("\n");
		} else if (foundSolution == false) {
			System.out.println("[FOUND_SOLUTION]: no");
		}
	}

	public static void constistentCheck(String... args) {
		System.out.println("# HEURISTIC-CONSISTENT " + putanjaDoOpisnikaHeuristike.getPath());
		boolean consistent = true;
		for (Map.Entry<String, TreeMap<String, Double>> trenutni : funkcijePrijelaza.entrySet()) {
			for (Map.Entry<String, Double> susjed : trenutni.getValue().entrySet()){
			if (heuristikaVrijednosti.get(trenutni.getKey()) <= heuristikaVrijednosti.get(susjed.getKey()) + susjed.getValue()) {
				System.out.println("[CONDITION]: [OK] h(" + trenutni.getKey() + ") <= h(" + susjed.getKey() + ") + c: " + heuristikaVrijednosti.get(trenutni.getKey()) + " <= " + heuristikaVrijednosti.get(susjed.getKey()) + " + " + susjed.getValue());
			} else {
				System.out.println("[CONDITION]: [ERR] h(" + trenutni.getKey() + ") <= h(" + susjed.getKey() + ") + c: " + heuristikaVrijednosti.get(trenutni.getKey()) + " <= " + heuristikaVrijednosti.get(susjed.getKey()) + " + " + susjed.getValue());
				consistent = false;
			}
		}
	}
		if (consistent){
			System.out.println("[CONCLUSION]: Heuristic is consistent.");
		}
		else{
			System.out.println("[CONCLUSION]: Heuristic is not consistent.");
		}
	}

	public static void optimisticCheck(String... args) {
		System.out.println("# HEURISTIC-OPTIMISTIC " + putanjaDoOpisnikaHeuristike.getPath());
		boolean optimistic = true;
		for (Map.Entry<String, Double> heuristika : heuristikaVrijednosti.entrySet()) {
			pocetnoStanje = heuristika.getKey();
			Double realCost = ucsForHeuristic();
			if (heuristika.getValue() <= realCost){
				System.out.println("[CONDITION]: [OK] h(" + pocetnoStanje + ") <= h*: " + heuristika.getValue() + " <= " + realCost);
			}
			else{
				System.out.println("[CONDITION]: [ERR] h(" + pocetnoStanje + ") <= h*: " + heuristika.getValue() + " <= " + realCost);
				optimistic = false;
			}
		}
		if (optimistic){
			System.out.println("[CONCLUSION]: Heuristic is optimistic.");
		}
		else{
			System.out.println("[CONCLUSION]: Heuristic is not optimistic.");
		}
	}

	public static Double ucsForHeuristic() {
		Queue<Stanje> open = new PriorityQueue<>();
		List<String> closed = new LinkedList<>();
		Stanje trenutnoStanje = new Stanje(pocetnoStanje, 1, 0.0);
		open.add(trenutnoStanje);
		int statesVisited = 0;
		boolean foundSolution = false;
		while (!open.isEmpty()) {
			trenutnoStanje = open.remove();
			closed.add(trenutnoStanje.imeStanja);
			statesVisited++;
			if (ciljnaStanja.contains(trenutnoStanje.imeStanja)) {
				//OVDJE SE MORAM VAN ISPETLJATI I ISPISAT KAJ TREBA
				foundSolution = true;
				break;
			}
			if (funkcijePrijelaza.get(trenutnoStanje.imeStanja) == null) {
				continue;
			}
			for (Map.Entry<String, Double> susjedi : funkcijePrijelaza.get(trenutnoStanje.imeStanja).entrySet()) {
				//OVDJE EKSPANDIRAM
				if (!closed.contains(susjedi.getKey())) {
					open.add(new Stanje(susjedi.getKey(), trenutnoStanje, trenutnoStanje.pathLength + 1, trenutnoStanje.costToHere + susjedi.getValue()));
				}
			}
		}
		return trenutnoStanje.costToHere;
	}
}
