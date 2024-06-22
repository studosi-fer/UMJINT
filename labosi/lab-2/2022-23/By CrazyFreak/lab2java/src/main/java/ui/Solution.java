package ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Solution {

	private static LinkedList<String> computeIngredients(LinkedList<String> baseIngredients, LinkedList<String> recipes) {
		LinkedList<String> ingredients = new LinkedList<>();
		ingredients.addAll(baseIngredients);

		for (int i = 0; i < recipes.size(); i++) {
			LinkedList<String> neededIngredients = new LinkedList<>();

			neededIngredients.addAll(List.of(recipes.get(i).split(" v ")));
			String outputIngredient = neededIngredients.remove(neededIngredients.size() - 1);
//			System.out.println(neededIngredients + " = " + outputIngredient);

			if (ingredients.contains(outputIngredient)) {
				continue;
			} else {
				// check if i have all ingredient to make the output
				boolean haveAllIngredients = true;
				for (String neededIngredient : neededIngredients) {
					neededIngredient = neededIngredient.substring(1);
					if (!ingredients.contains(neededIngredient)) {
						haveAllIngredients = false;
						break;
					}
				}

				if (haveAllIngredients) {
//					System.out.println("Can make " + outputIngredient);

					ingredients.add(outputIngredient);
					i = -1; // so it goes through all the recipes again, myb not needed
				}
				/*else {
					System.out.println("Cannot make " + outputIngredient);
				}*/
			}
		}

		return ingredients;
	}

	private static void refutationResolution(List<String> clauses) {
		// Split the input clauses into premises and goal
		List<String> premises = new ArrayList<>(clauses.subList(0, clauses.size() - 1));
		String goal = clauses.get(clauses.size() - 1);

		// Initialize the set-of-support (SOS) with the negated goal
		List<String> sos = new ArrayList<>();
		sos.add(negate(goal));

		// Initialize the list of derived clauses
		List<String> derived = new ArrayList<>();
		boolean derivedNil = false;

		// While there are clauses in the SOS and the NIL clause has not been derived
		while (!sos.isEmpty() && !derivedNil) {
			// Remove the first clause from the SOS
			String current = sos.remove(0);

			// Attempt to resolve the current clause with each premise
			for (String premise : premises) {
				String resolvent = resolve(current, premise);

				// If a resolvent is found
				if (resolvent != null) {
					// If the resolvent is the empty clause (NIL), the goal has been derived
					if (resolvent.isEmpty()) {
						derivedNil = true;
						derived.add(current);
						break;
					} else {
						// Otherwise, add the resolvent to the SOS
						sos.add(resolvent);
					}
				}
			}

			// Add the current clause to the list of derived clauses and the premises
			if (!derived.contains(current))
				derived.add(current);
//			premises.add(current);
		}

		// Print the resolution steps and the final conclusion
		printResolutionSteps(premises, derived, goal, derivedNil);
	}


	private static String negate(String literal) {
		return literal.startsWith("~") ? literal.substring(1) : "~" + literal;
	}

	private static String resolve(String clause1, String clause2) {
		String[] literals1 = clause1.split(" ");
		String[] literals2 = clause2.split(" ");
		Set<String> resolvent = new HashSet<>();
		boolean resolved = false;

		// Iterate over the literals in the two input clauses
		for (String literal1 : literals1) {
			for (String literal2 : literals2) {
				// If a complementary literal pair is found
				if (literal1.equals(negate(literal2))) {
					// If this is the first complementary pair found, initialize the resolvent
					if (!resolved) {
						resolved = true;
						for (String literal : literals1) {
							if (!literal.equals(literal1)) {
								resolvent.add(literal);
							}
						}
						for (String literal : literals2) {
							if (!literal.equals(literal2)) {
								resolvent.add(literal);
							}
						}
					} else {
						// If another complementary pair is found, the resolvent is invalid
						return null;
					}
				}
			}
		}

		// Factorize the resolvent by converting the set of literals back to a space-separated string
		return String.join(" ", resolvent);
	}


	private static void printResolutionSteps(List<String> premises, List<String> derived, String goal, boolean derivedNil) {
		int index = 1;

		// Print premises
		for (String premise : premises) {
			System.out.println(index + ". " + premise);
			index++;
		}

		// Print separator
		System.out.println("===============");

		// Print derived clauses
		for (String clause : derived) {
			System.out.println(index + ". " + clause);
			index++;
		}

		// Print separator
		System.out.println("===============");

		// Print conclusion
		if (derivedNil) {
			// If the empty clause (NIL) was derived, the goal is true
			System.out.println("[CONCLUSION]: " + goal + " is true");
		} else {
			// If the empty clause (NIL) was not derived, the truth of the goal is unknown
			System.out.println("[CONCLUSION]: " + goal + " is unknown");
		}
	}



	public static void main(String ... args) throws FileNotFoundException {
		// parse arguments
		/*for(String arg : args) {
			System.out.printf("[ARGUMENT]: %s%n", arg);
		}*/

		if (args[0].equals("cooking")) {
//			TODO: show steps
			LinkedList<String> cookbookLines = new LinkedList<>();

			/*File cookbookFile = new File("D:\\#Data\\Office & PDF Things\\Education & Work" +
				"\\FER - Fakultet elektrotehnike i računarstva\\FER 6. semestar" +
				"\\UUUI - Uvod u umjetnu inteligenciju\\[ENG]\\Laboratory exercises" +
				"\\autograder\\data\\lab2\\files\\cooking_heldout_large_chain.txt");*/
			File cookbookFile = new File(args[1]);
			Scanner cookbookfs = new Scanner(cookbookFile);
			while (cookbookfs.hasNextLine()) {
				String line = cookbookfs.nextLine().replace("V", "v").toLowerCase();
				if(!line.startsWith("#"))
					cookbookLines.add(line);
			}
			cookbookfs.close();

			LinkedList<String> inputLines = new LinkedList<>();

			/*File inputFile = new File("D:\\#Data\\Office & PDF Things\\Education & Work" +
					"\\FER - Fakultet elektrotehnike i računarstva\\FER 6. semestar" +
					"\\UUUI - Uvod u umjetnu inteligenciju\\[ENG]\\Laboratory exercises" +
					"\\autograder\\data\\lab2\\files\\cooking_heldout_large_chain_input.txt");*/
			File inputFile = new File(args[2]);
			Scanner inputfs = new Scanner(inputFile);
			while (inputfs.hasNextLine()) {
				String line = inputfs.nextLine().replace("V", "v").toLowerCase();
				if(!line.startsWith("#"))
					inputLines.add(line);
			}
			inputfs.close();


			LinkedList<String> baseIngredients = new LinkedList<>();
			LinkedList<String> recipes = new LinkedList<>();

			for (String line: cookbookLines) {
//            System.out.println(line);

				if (line.contains(" v ")) {
					recipes.add(line);
				} else {
					baseIngredients.add(line);
				}
			}

//		System.out.println(baseIngredients);
//		System.out.println(recipes);

			LinkedList<String> ingredients = new LinkedList<>();

			for (String line: inputLines) {
//            System.out.println(line);

				String clause = line.substring(0, line.length() - 2);
				String action = line.substring(line.length() - 1);
//			System.out.println(clause + " | " + action);

				if (action.equals("?")) {
//				System.out.println("Recompute and check if ingredients has the clause: " + clause);

					ingredients = computeIngredients(baseIngredients, recipes);
//				System.out.println(baseIngredients);
//				System.out.println(ingredients);

					System.out.println("[CONCLUSION]: " + clause + " is " +
							(ingredients.contains(clause) ? "true" : "unknown") + "\n");
				} else if (action.equals("+")) {
					if (clause.contains(" v ") && !recipes.contains(clause)) {
						System.out.println("[ADD] recipe: " + clause);
						recipes.add(clause);
					} else if (!clause.contains(" v ") && !baseIngredients.contains(clause)) {
						System.out.println("[ADD] ingredient: " + clause);
						baseIngredients.add(clause);
					}
				} else if (action.equals("-")) {
					if (clause.contains(" v ") && recipes.contains(clause)) {
						System.out.println("[REMOVE] recipe: " + clause);
						recipes.remove(clause);
					} else if (!clause.contains(" v ") && baseIngredients.contains(clause)) {
						System.out.println("[REMOVE] ingredient: " + clause);
						baseIngredients.remove(clause);
					}
				}
			}

		} else if (args[0].equals("resolution")) {
			LinkedList<String> resolutionLines = new LinkedList<>();

			/*File filePath = new File("D:\\#Data\\Office & PDF Things\\Education & Work" +
				"\\FER - Fakultet elektrotehnike i računarstva\\FER 6. semestar" +
				"\\UUUI - Uvod u umjetnu inteligenciju\\[ENG]\\Laboratory exercises" +
				"\\autograder\\data\\lab2\\files\\resolution_small_example.txt");*/
			File filePath = new File(args[1]);
			Scanner resolutionfs = new Scanner(filePath);
			while (resolutionfs.hasNextLine()) {
				String line = resolutionfs.nextLine().replace("V", "v").toLowerCase();
				if(!line.startsWith("#"))
					resolutionLines.add(line);
			}
			resolutionfs.close();

			refutationResolution(resolutionLines);
		}

	}
}
