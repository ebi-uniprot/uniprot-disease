package uk.ac.ebi.uniprot.uniprotdisease.import_data;

import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParseDiseaseLinesTest {
    private static final ParseDiseaseLines obj = new ParseDiseaseLines();

    @Test
    @DisplayName("Only simple disease with cross references")
    void simpleDiseaseParse() {

        final List<String> input =
                Arrays.asList("_______________________________",
                        "ID   Roberts syndrome.",
                        "AC   DI-02272",
                        "AR   RBS.",
                        "DE   Rare autosomal recessive disorder characterized",
                        "DR   MIM; 268300; phenotype.",
                        "//");

        final List<Disease> retList = obj.parseLines(input);
        assertAll("Disease parse result",
                () -> assertNotNull(retList),
                () -> assertEquals(1, retList.size(), "should have one object return"),
                () -> assertEquals("Roberts syndrome", retList.get(0).getIdentifier()),
                () -> assertEquals("DI-02272", retList.get(0).getAccession()),
                () -> assertEquals("RBS", retList.get(0).getAcronym()),
                () -> assertEquals("Rare autosomal recessive disorder characterized", retList.get(0).getDefinition()),
                () -> assertEquals("MIM", retList.get(0).getCrossReferences().get(0).getAbbreviation()),
                () -> assertEquals("268300", retList.get(0).getCrossReferences().get(0).getIdentifier()),
                () -> assertEquals("phenotype", retList.get(0).getCrossReferences().get(0).getInformations().get(0)),
                () -> assertNull(retList.get(0).getAlternativeNames()),
                () -> assertNull(retList.get(0).getAssociatedkeywords())
        );
    }

    @Test
    @DisplayName("Should return keywords when parsing disease")
    void parseDiseaseAndCheckKeywords() {

        final List<String> input =
                Arrays.asList("______________________________",
                        "ID   Unilateral palmoplantar verrucous nevus.",
                        "AC   DI-01111",
                        "AR   UPVN.",
                        "DE   UPVN is characterized by a localized epidermolytic hyperkeratosis in",
                        "DE   parts of the right palm and the right sole, following the lines of",
                        "DE   Blaschko.",
                        "DR   MIM; 144200; phenotype.",
                        "DR   MeSH; D053546.",
                        "KW   KW-1007:Palmoplantar keratoderma.",
                        "//");

        final List<Disease> retList = obj.parseLines(input);

        assertAll("Disease parse result",
                () -> assertNotNull(retList.get(0).getAssociatedkeywords()),
                () -> assertEquals(2, retList.get(0).getCrossReferences().size(), "should have two CR return"),
                () -> assertEquals("KW-1007", retList.get(0).getAssociatedkeywords().get(0).getAccession()),
                () -> assertEquals("Palmoplantar keratoderma", retList.get(0).getAssociatedkeywords().get(0)
                        .getIdentifier(), "should be without dot")
        );

    }

    @Test
    @DisplayName("Should return alternative names when parsing disease")
    void parseDiseaseAndCheckAlternativeNames() {
        final List<String> input =
                Arrays.asList("_____________________________",
                        "ID   SC phocomelia syndrome.",
                        "AC   DI-02280",
                        "AR   SCPS.",
                        "SY   SC pseudothalidomide syndrome.",
                        "//");
        final List<Disease> retList = obj.parseLines(input);

        assertNotNull(retList.get(0).getAlternativeNames());
        assertEquals(1, retList.get(0).getAlternativeNames().size());
        assertEquals("SC pseudothalidomide syndrome", retList.get(0).getAlternativeNames().get(0));

        assertNull(retList.get(0).getDefinition());
        assertNull(retList.get(0).getCrossReferences());
    }

    @Test
    @DisplayName("should return all attributes not null when parsing complete entry")
    void parseLargeDisease() {
        final List<String> input =
                Arrays.asList("____________________________",
                        "ID   Salt and pepper developmental regression syndrome",
                        "AC   DI-00096",
                        "AR   SPDRS.",
                        "DE   A rare autosomal recessive disorder characterized by infantile onset",
                        "SY   AIES.",
                        "SY   Amish infantile epilepsy syndrome.",
                        "SY   Epilepsy syndrome infantile-onset symptomatic.",
                        "SY   GM3 synthase deficiency.",
                        "SY   Salt and pepper mental retardation syndrome.",
                        "DR   MIM; 609056; phenotype.",
                        "DR   MedGen; C1836824.",
                        "DR   MeSH; D004827.",
                        "KW   KW-0887:Epilepsy.",
                        "KW   KW-0991:Mental retardation.",
                        "//");
        final List<Disease> retList = obj.parseLines(input);
        final Disease di = retList.get(0);
        assertNotNull(di);
        assertNotNull(di.getIdentifier());
        assertNotNull(di.getAccession());
        assertNotNull(di.getAcronym());
        assertNotNull(di.getDefinition());
        assertEquals(5, di.getAlternativeNames().size());
        assertEquals(3, di.getCrossReferences().size());
        assertEquals(2, di.getAssociatedkeywords().size());
    }

}
