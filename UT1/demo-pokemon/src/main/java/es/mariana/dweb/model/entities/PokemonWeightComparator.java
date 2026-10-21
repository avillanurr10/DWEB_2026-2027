package es.mariana.dweb.model.entities;

import java.util.Comparator;

public class PokemonWeightComparator implements Comparator<Pokemon> {

    @Override
    public int compare(Pokemon p1, Pokemon p2) {
        if (p1.getWeight() < p2.getWeight()) return -1;
        else if (p1.getWeight() > p2.getWeight()) return 1;
        else return 0;
    }
}
