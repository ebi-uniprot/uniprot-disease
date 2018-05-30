package uk.ac.ebi.uniprot.uniprotdisease.dto;

import java.util.Objects;

public class DiseaseReferenceCount {
    private final String identifier;
    private int swissProtCount;
    private int tremblCount;

    public DiseaseReferenceCount(String identifier) {
        this.identifier = identifier;
        this.swissProtCount = 0;
        this.tremblCount = 0;
    }

    public String getIdentifier() {
        return identifier;
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

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DiseaseReferenceCount)) {
            return false;
        }
        DiseaseReferenceCount that = (DiseaseReferenceCount) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override public int hashCode() {

        return Objects.hash(identifier);
    }
}
