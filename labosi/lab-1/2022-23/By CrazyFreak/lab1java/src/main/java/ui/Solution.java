package ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


class Node {
	public String stateName;
	public Map<String, Integer> successors;
	public Node parent = null;
	public float costFromStart = Integer.MAX_VALUE;
	public float heuristicCostToGoal = Integer.MAX_VALUE;
	public float trueCostToGoal = Integer.MAX_VALUE;

	public Node(String stateName, Map<String, Integer> successors) {
		this.stateName = stateName;
		this.successors = successors;
	}

	@Override
	public String toString() {
		return "\n\tNode{" +
				"stateName='" + stateName + '\'' +
				", successors=" + successors +
				", parent=" + parent +
				'}';
	}

//	@Override
//	public String toString() {
//		return "\n\tNode{" +
//				"stateName='" + stateName + '\'' +
//				", successors=" + successors +
//				", parent=" + parent +
//				", costFromStart=" + costFromStart +
//				", actualCostToGoal=" + actualCostToGoal +
//				", heuristicCostToGoal=" + heuristicCostToGoal +
//				'}';
//	}
}


public class Solution {
	public static void main(String ... args) throws FileNotFoundException {
		// parse args
		String alg = "";
		String inputFilePath = "";
		String heuristicFilePath = "";
		boolean checkIfOptimistic = false;
		boolean checkIfConsistent = false;

		for (int i = 0; i < args.length; i++) {
			if ("--alg".equals(args[i])) {
				if (i + 1 < args.length) {
					alg = args[++i];

					System.out.println("# " + alg.toUpperCase());
				}
			} else if ("--ss".equals(args[i])) {
				if (i + 1 < args.length) {
					inputFilePath = args[++i];

//					System.out.println(stateSpaceFilePath);
				}
			} else if ("--h".equals(args[i])) {
				if (i + 1 < args.length) {
					heuristicFilePath = args[++i];

//					System.out.println(heuristicFilePath);
				}
			} else if ("--check-optimistic".equals(args[i])) {
				checkIfOptimistic = true;
			} else if ("--check-consistent".equals(args[i])) {
				checkIfConsistent = true;
			}
		}

		// hardcoded args for testing
//		inputFilePath = "C:\\Users\\Skull mini\\Desktop\\autograder\\data\\lab1\\files\\ai.txt";
//		heuristicFilePath = "C:\\Users\\Skull mini\\Desktop\\autograder\\data\\lab1\\files\\ai_fail.txt";
//		alg = "astar";
//		checkIfOptimistic = true;
//		checkIfConsistent = true;

		// parse files
		File inputFile = new File(inputFilePath);

		LinkedList<String> inputLines = new LinkedList<>();
		Scanner iScanner = new Scanner(inputFile);
		while (iScanner.hasNextLine()) {
			String line = iScanner.nextLine();
			if(!line.startsWith("#"))
				inputLines.add(line);
		}
		iScanner.close();

//		for (String inputLine : inputLines) {
//			System.out.println(inputLine);
//		}

		LinkedList<String> heuristicLines = new LinkedList<>();
		if (!heuristicFilePath.isEmpty()) {
			File heuristicFile = new File(heuristicFilePath);

			Scanner hScanner = new Scanner(heuristicFile);
			while (hScanner.hasNextLine()) {
				String line = hScanner.nextLine();
				if (!line.startsWith("#")) {
					heuristicLines.add(line);
				}
			}
			hScanner.close();
		}


		// set initial values
		String startState = inputLines.remove(0);
		List<String> goalStates = List.of(inputLines.remove(0).split(" "));

		Map<String, Node> graph = new HashMap<>();
		parseInput(graph, inputLines);
		parseHeuristic(graph, heuristicLines);

//		for (Node s : graph.values()) {
//			System.out.println(s);
//		}

		Node startNode = graph.get(startState);
		startNode.costFromStart = 0;

		Node solutionNode = null;
		Set<String> visited = new HashSet<>();


		// start state space search using desired alg
		if (alg.equals("bfs")) {
			solutionNode = BFS(graph, startNode, goalStates, visited);
		} else if (alg.equals("ucs")) {
			solutionNode = UCS(graph, startNode, goalStates, visited);
		} else if (alg.equals("astar")) {
			solutionNode = Astar(graph, startNode, goalStates, visited);
		}

		if (!alg.equals("")) {
			printSolutionDetails(solutionNode, visited, graph);
		}

		updateTrueCostToGoal(graph, reverseGraph(graph), goalStates);

		if (checkIfOptimistic) {
			checkOptimistic(graph, heuristicFilePath);
		}
		if (checkIfConsistent) {
			checkConsistent(graph, heuristicFilePath);
		}

	}

	private static void parseInput(Map<String, Node> graph, List<String> inputLines) {
		for (String inputLine : inputLines) {
//			System.out.println(inputLine);
			String[] parts = inputLine.split(":");
			String stateName = parts[0];

			Map<String, Integer> successorsMap = new HashMap<>();
			if (parts.length > 1) {
				List<String> successorsList = List.of(parts[1].substring(1).split(" "));
//				System.out.println(successorsList);

				for (String successor : successorsList) {
//					System.out.println(successor);

					String[] successorParts = successor.split(",");
					String successorState = successorParts[0];
					int distance = Integer.parseInt(successorParts[1]);
					successorsMap.put(successorState, distance);
				}
			}

//			System.out.println(successorsMap);

			graph.put(stateName, new Node(stateName, successorsMap));
		}

	}

	private static void parseHeuristic(Map<String, Node> graph, List<String> heuristicLines) {
		if (!heuristicLines.isEmpty()) {
			for (String heuristicLine : heuristicLines) {
				String[] parts = heuristicLine.split(": ");

				Node n = graph.get(parts[0]);
				n.heuristicCostToGoal = Integer.parseInt(parts[1]);
//				System.out.println(n);
			}
		}
	}

	private static void printSolutionDetails(Node solutionNode, Set<String> visited, Map<String, Node> graph) {
		if (solutionNode != null) {
			List<String> path = new ArrayList<>();
			for (Node n = solutionNode; n != null; n = n.parent) {
				path.add(n.stateName);
			}
			Collections.reverse(path);

			System.out.println("[FOUND_SOLUTION]: yes");
			System.out.println("[STATES_VISITED]: " + (visited.size() + 1));
			System.out.println("[PATH_LENGTH]: " + path.size());

			float totalCost = 0;
			for (int i = 0; i < path.size() - 1; i++) {
				String from = path.get(i);
				String to = path.get(i + 1);
				totalCost += graph.get(from).successors.get(to);
			}
			System.out.println("[TOTAL_COST]: " + totalCost);

			System.out.print("[PATH]: ");
			if (!path.isEmpty()) {
				System.out.print(path.remove(0)); // Print and remove the first element
				for (String s : path) {
					System.out.print(" => " + s); // Print the rest of the path
				}
			}
		} else {
			System.out.println("[FOUND_SOLUTION]: no");
		}
	}

	private static Node BFS(Map<String, Node> graph, Node startNode, List<String> goalStates, Set<String> visited) {
		Queue<Node> open = new LinkedList<>();
		open.add(startNode);

		while (!open.isEmpty()) {
			Node currentNode = open.poll();
//			System.out.println("OPEN: " + open);
//			System.out.println(currentNode.stateName);

			if (goalStates.contains(currentNode.stateName)) {
				return currentNode;
			}

			visited.add(currentNode.stateName);
//			System.out.println("VISITED: " + visited);

			// sort alphabetically
			List<String> sortedSuccessors = new ArrayList<>(currentNode.successors.keySet());
			Collections.sort(sortedSuccessors);
//			System.out.println(sortedSuccessors);

			for (String successorName : sortedSuccessors) {
				Node successorNode = graph.get(successorName);

				if (!visited.contains(successorName) && !open.contains(successorNode)) {
					// successor not yet visited and not yet added to queue

					// save parent
					successorNode.parent = currentNode;

					// add to open queue
					open.add(successorNode);
				}
			}
		}

		return null; // No solution found
	}

	private static Node UCS(Map<String, Node> graph, Node startNode, List<String> goalStates, Set<String> visited) {
		PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.costFromStart));
		open.add(startNode);

		while (!open.isEmpty()) {
			Node currentNode = open.poll();
			if (goalStates.contains(currentNode.stateName)) {
				return currentNode;
			}

			visited.add(currentNode.stateName);

			for (Map.Entry<String, Integer> entry : currentNode.successors.entrySet()) {
				Node successor = graph.get(entry.getKey());

				if (successor != null && !visited.contains(successor.stateName)) {
					float newCost = currentNode.costFromStart + entry.getValue();
					if (newCost < successor.costFromStart) {
						// found a cheaper path to node

						// change values
						successor.costFromStart = newCost;
						successor.parent = currentNode;

						// update order in priority queue
						open.remove(successor);
						open.add(successor);
					}
				}
			}
		}

		return null; // No solution found
	}

	private static Node Astar(Map<String, Node> graph, Node startNode, List<String> goalStates, Set<String> visited) {
		PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.costFromStart + n.heuristicCostToGoal));
		open.add(startNode);

		while (!open.isEmpty()) {
			Node currentNode = open.poll();
			if (goalStates.contains(currentNode.stateName)) {
				return currentNode;
			}

			visited.add(currentNode.stateName);

			for (Map.Entry<String, Integer> entry : currentNode.successors.entrySet()) {
				Node successor = graph.get(entry.getKey());
				float newCost = currentNode.costFromStart + entry.getValue();

				if (visited.contains(successor.stateName) || open.contains(successor)) {
					if (successor.costFromStart <= newCost) {
						// new path not cheaper
						continue;
					}
				}

				successor.costFromStart = newCost;
				successor.parent = currentNode;

				// update order in priority queue
				open.remove(successor);
				open.add(successor);
			}

		}

		return null; // No solution found
	}

	private static void checkConsistent(Map<String, Node> graph, String heuristicFilePath) {
		System.out.println("\n# HEURISTIC-CONSISTENT " + heuristicFilePath);

		boolean heuristicIsConsistent = true;

		for (Node n : graph.values()) {
			Map<String, Integer> successors = n.successors;

			for (Map.Entry<String, Integer> successorEntry : successors.entrySet()) {
				// get successor for his heuristic value
				String successorStateName = successorEntry.getKey();
				Node successor = graph.get(successorStateName);

				// get cost of transition to successor from parent node
				float costToSuccessor = successorEntry.getValue();

				System.out.print("[CONDITION]: ");

				// if h(n) > h(s) + c => h is not consistent
				if (n.heuristicCostToGoal > successor.heuristicCostToGoal + costToSuccessor) {
					heuristicIsConsistent = false;

					System.out.println("[ERR] h(" + n.stateName + ") <= h(" + successorStateName + ") + c: " +
							n.heuristicCostToGoal + " <= " +
							successor.heuristicCostToGoal + " + " + costToSuccessor);
				} else {
					System.out.println("[OK] h(" + n.stateName + ") <= h(" + successorStateName + ") + c: " +
							n.heuristicCostToGoal + " <= " +
							successor.heuristicCostToGoal + " + " + costToSuccessor);
				}
			}
		}

		System.out.print("[CONCLUSION]: ");
		if (heuristicIsConsistent) {
			System.out.println("Heuristic is consistent.");
		} else {
			System.out.println("Heuristic is not consistent.");
		}
	}

	private static void checkOptimistic(Map<String, Node> graph, String heuristicFilePath) {
		System.out.println("\n# HEURISTIC-OPTIMISTIC " + heuristicFilePath);

		boolean heuristicIsOptimistic = true;

		for (Node n : graph.values()) {
			System.out.print("[CONDITION]: ");

			if (n.heuristicCostToGoal <= n.trueCostToGoal) {
				System.out.print("[OK] ");
			} else {
				System.out.print("[ERR] ");
				heuristicIsOptimistic = false;
			}
			System.out.println("h(" + n.stateName + ") <= h*: " + n.heuristicCostToGoal + " <= " + n.trueCostToGoal);
		}

		System.out.print("[CONCLUSION]: ");
		if (heuristicIsOptimistic) {
			System.out.println("Heuristic is optimistic.");
		} else {
			System.out.println("Heuristic is not optimistic.");
		}
	}

	private static Map<String, Node> reverseGraph(Map<String, Node> originalGraph) {
		Map<String, Node> reversedGraph = new HashMap<>();

		// add all nodes to reversed graph
		for (String stateName : originalGraph.keySet()) {
			reversedGraph.put(stateName, new Node(stateName, new HashMap<>()));
		}

		// reverse edges
		for (Node originalNode : originalGraph.values()) {
			for (Map.Entry<String, Integer> entry : originalNode.successors.entrySet()) {
				String successorName = entry.getKey();
				int cost = entry.getValue();
				reversedGraph.get(successorName).successors.put(originalNode.stateName, cost);
			}
		}
		return reversedGraph;
	}

	private static void updateTrueCostToGoal(Map<String, Node> originalGraph, Map<String, Node> reversedGraph, List<String> goalStates) {
		for (String goalState : goalStates) {
			PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.costFromStart));
			Set<String> visited = new HashSet<>();

			// reset costs for all nodes
			for (Node node : reversedGraph.values()) {
				node.costFromStart = Integer.MAX_VALUE;
			}

			Node startNode = reversedGraph.get(goalState);
			startNode.costFromStart = 0;
			open.add(startNode);

			while (!open.isEmpty()) {
				Node currentNode = open.poll();

				if (!visited.contains(currentNode.stateName)) {
					visited.add(currentNode.stateName);

					for (Map.Entry<String, Integer> entry : currentNode.successors.entrySet()) {
						Node successor = reversedGraph.get(entry.getKey());
						float newCost = currentNode.costFromStart + entry.getValue();

						if (newCost < successor.costFromStart) {
							successor.costFromStart = newCost;

							if (!visited.contains(successor.stateName)) {
								open.add(successor);
							}
						}
					}
				}
			}

			// update true costfrom every node to goal in the origigi graph
			for (Map.Entry<String, Node> entry : reversedGraph.entrySet()) {
				String nodeName = entry.getKey();
				float cost = entry.getValue().costFromStart;
					originalGraph.get(nodeName).trueCostToGoal =
							Math.min(originalGraph.get(nodeName).trueCostToGoal, cost);
			}
		}
	}
}
