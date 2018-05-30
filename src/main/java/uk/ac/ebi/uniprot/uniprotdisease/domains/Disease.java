package uk.ac.ebi.uniprot.uniprotdisease.domains;

import java.util.List;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Disease {
    @Id
    private String identifier;
    @Indexed
    private String accession;
    @Indexed
    private String acronym;
    private String definition;
    private List<String> alternativeNames;
    private List<CrossReference> crossReferences;
    private List<Keyword> associatedkeywords;

    private int swissProtCount;
    private int tremblCount;

    private Disease() {
    }

    public Disease(String identifier, String accession, String acronym) {
        this.identifier = identifier;
        this.accession = accession;
        this.acronym = acronym;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public List<String> getAlternativeNames() {
        return alternativeNames;
    }

    public void setAlternativeNames(List<String> alternativeNames) {
        this.alternativeNames = alternativeNames;
    }

    public List<CrossReference> getCrossReferences() {
        return crossReferences;
    }

    public void setCrossReferences(List<CrossReference> crossReferences) {
        this.crossReferences = crossReferences;
    }

    public List<Keyword> getAssociatedkeywords() {
        return associatedkeywords;
    }

    public void setAssociatedkeywords(List<Keyword> associatedkeywords) {
        this.associatedkeywords = associatedkeywords;
    }

    public int getSwissProtCount() {
        return swissProtCount;
    }

    public void setSwissProtCount(int swissProtCount) {
        this.swissProtCount = swissProtCount;
    }

    public int getTremblCount() {
        return tremblCount;
    }

    public void setTremblCount(int tremblCount) {
        this.tremblCount = tremblCount;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getAccession() {
        return accession;
    }

    public String getAcronym() {
        return acronym;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Disease)) {
            return false;
        }
        Disease disease = (Disease) o;
        return Objects.equals(identifier, disease.identifier) &&
                Objects.equals(accession, disease.accession) &&
                Objects.equals(acronym, disease.acronym);
    }

    @Override public int hashCode() {

        return Objects.hash(identifier, accession, acronym);
    }
}