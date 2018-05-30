package uk.ac.ebi.uniprot.uniprotdisease.domains;

public class Keyword {
    private String accession;
    private String identifier;

    private Keyword(){}

    public Keyword(String accession, String identifier) {
        this.accession = accession;
        this.identifier = identifier;
    }

    public String getAccession() {
        return accession;
    }

    public String getIdentifier() {
        return identifier;
    }
}
