package ui;

import java.util.HashSet;
import java.util.Set;

//POCETNO STANJE je roditelj == null
public class klauzula{
    public Set<String> varijableKlauzule = new HashSet<>();
    public int brojKlauzule;
    public klauzula roditelj1;
    public klauzula roditelj2;

    public klauzula(Set<String> varijableKlauzule, int brojKlauzule, klauzula roditelj1, klauzula roditelj2){
        this.varijableKlauzule = varijableKlauzule;
        this.brojKlauzule = brojKlauzule;
        this.roditelj1 = roditelj1;
        this.roditelj2 = roditelj2;
    }

    public klauzula(Set<String> varijableKlauzule, int brojKlauzule){
        this.varijableKlauzule = varijableKlauzule;
        this.brojKlauzule = brojKlauzule;
    }

    public klauzula(){
    }


    @Override
    public boolean equals(Object o) {
        klauzula druga = (klauzula) o;
        return this.varijableKlauzule.equals(druga.varijableKlauzule);
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (String str : this.varijableKlauzule) {
            hashCode = hashCode + str.hashCode();
        }
        return hashCode;
    }

}
