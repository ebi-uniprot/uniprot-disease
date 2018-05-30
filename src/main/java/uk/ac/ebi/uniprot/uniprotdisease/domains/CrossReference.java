package uk.ac.ebi.uniprot.uniprotdisease.domains;

import java.util.List;

public class CrossReference {
    private String identifier;
    private String abbreviation;
    private List<String> informations;

    private CrossReference(){}

    public CrossReference(String identifier, String abbreviation, List<String> informations) {
        this.identifier = identifier;
        this.abbreviation = abbreviation;
        this.informations = informations;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public List<String> getInformations() {
        return informations;
    }
}