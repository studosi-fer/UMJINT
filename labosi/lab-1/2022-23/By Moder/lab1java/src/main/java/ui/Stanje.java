package ui;

import java.util.List;

//POCETNO STANJE je roditelj == null
public class Stanje implements Comparable<Stanje>{
    public String imeStanja;
    public Stanje roditelj;
    public Double costToHere;
    public int pathLength;
    public Double costPlusHeuristic;

    public Stanje(String imeStanja, Stanje roditelj, int pathLength, Double costToHere){
        this.imeStanja = imeStanja;
        this.roditelj = roditelj;
        this.pathLength = pathLength;
        this.costToHere = costToHere;
        this.costPlusHeuristic = costToHere;
    }

    public Stanje(String imeStanja, int pathLength, Double costToHere){
        this.imeStanja = imeStanja;
        this.pathLength = pathLength;
        this.costToHere = costToHere;
        this.costPlusHeuristic = costToHere;
    }

    public Stanje(String imeStanja, Stanje roditelj, int pathLength, Double costToHere, Double costPlusHeuristic){
        this.imeStanja = imeStanja;
        this.roditelj = roditelj;
        this.pathLength = pathLength;
        this.costToHere = costToHere;
        this.costPlusHeuristic = costPlusHeuristic;
    }

    public Stanje(String imeStanja, int pathLength, Double costToHere, Double costPlusHeuristic){
        this.imeStanja = imeStanja;
        this.pathLength = pathLength;
        this.costToHere = costToHere;
        this.costPlusHeuristic = costPlusHeuristic;
    }

    @Override
    public int compareTo(Stanje drugoStanje) {
        if (!costPlusHeuristic.equals(drugoStanje.costPlusHeuristic)){
            return Double.compare(costPlusHeuristic, drugoStanje.costPlusHeuristic);
        }else{
            return imeStanja.compareToIgnoreCase(drugoStanje.imeStanja);
        }
    }

    /*@Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Stanje))
            return false;
        Stanje other = (Stanje) o;
        return (this.imeStanja.equals(other.imeStanja));
    }*/
}
