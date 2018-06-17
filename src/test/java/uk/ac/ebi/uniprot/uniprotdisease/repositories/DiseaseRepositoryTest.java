package uk.ac.ebi.uniprot.uniprotdisease.repositories;

import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;
import uk.ac.ebi.uniprot.uniprotdisease.dto.DiseaseAutoComplete;
import uk.ac.ebi.uniprot.uniprotdisease.import_data.CombineDiseaseAndRefCount;

import java.util.regex.Pattern;
import org.assertj.core.util.Arrays;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@DataMongoTest
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DiseaseRepositoryTest {
    private static final String ACCESSION_PROP = "accession";
    private static final String IDENTIFIER_PROP = "identifier";
    private static final String ABBREVIATION_PROP = "abbreviation";

    @Autowired
    private DiseaseRepository repo;

    @BeforeAll
    void setupAll() {
        final CombineDiseaseAndRefCount obj = new CombineDiseaseAndRefCount();
        final String sampleDataFilePath = ClassLoader.getSystemResource("sample-diseases.txt").getPath();
        final String sampleRefFilePath = ClassLoader.getSystemResource("sample-reference.txt").getPath();
        final List<Disease> keywordList = obj.readFileImportAndCombine(sampleDataFilePath, sampleRefFilePath);
        repo.saveAll(keywordList);
    }

    /**
     * Test of findByAccession method, of class DiseaseRepository.
     */
    @ParameterizedTest
    @ValueSource(strings = "DI-01147")
    void findByAccession(final String accession) {
        final Disease retObj = repo.findByAccession(accession);
        assertObject(retObj);
    }

    /**
     * Test of findByAcronym method, of class DiseaseRepository.
     */
    @ParameterizedTest
    @ValueSource(strings = "WAS")
    void findByAcronym(final String acronym) {
        final Disease retObj = repo.findByAcronymIgnoreCase(acronym);
        assertObject(retObj);
    }

    /**
     * Test of findByIdentifier method, of class DiseaseRepository.
     */
    @ParameterizedTest
    @ValueSource(strings = "Wiskott-Aldrich syndrome")
    void findByIdentifier(final String identifier) {
        final Disease retObj = repo.findByIdentifierIgnoreCase(identifier);
        assertObject(retObj);
    }

    private void assertObject(final Disease retObj){
        assertAll(
                () -> {
                    assertNotNull(retObj);
                    assertAll(
                            "Checking the alternative names return from repo",
                            () -> assertThat(retObj.getAlternativeNames()).isNotNull().hasSize(6),
                            () -> assertThat(retObj.getAlternativeNames())
                                    .contains("Aldrich syndrome","Eczema-thrombocytopenia-immunodeficiency syndrome",
                                            "IMD2","Immunodeficiency 2","WAS1","Wiskott-Aldrich syndrome 1")
                    );
                    assertAll(
                            "Checking cross references returned from repo",
                            () -> assertThat(retObj.getCrossReferences()).isNotNull().hasSize(3),
                            () -> assertThat(retObj.getCrossReferences()).extracting(ABBREVIATION_PROP)
                                    .contains("MIM", "MedGen","MeSH"),
                            () -> assertThat(retObj.getCrossReferences()).extracting(IDENTIFIER_PROP)
                                    .contains("301000", "C0043194","D014923")
                    );
                    assertAll(
                            "Checking other remaining properties returned from repo",
                            () -> assertThat(retObj.getIdentifier()).isEqualTo("Wiskott-Aldrich syndrome"),
                            () -> assertThat(retObj.getAccession()).isEqualTo("DI-01147"),
                            () -> assertThat(retObj.getAcronym()).isEqualTo("WAS"),
                            () -> assertThat(retObj.getDefinition()).isEqualTo("An X-linked recessive " +
                                    "immunodeficiency characterized by eczema, thrombocytopenia, recurrent " +
                                    "infections, and bloody diarrhea. Death usually occurs before age 10.")
                    );
                }
        );
    }



    @ParameterizedTest
    @ValueSource(strings = "TT")
    void identifierIgnoringCase(final String id) {
        final Collection<Disease> result = repo.findByIdentifierIgnoreCaseLike(id);
        assertThat(result).isNotNull().hasSize(3);
        assertThat(result).extracting(ACCESSION_PROP).doesNotContain("DI-01150");
    }

    @ParameterizedTest
    @ValueSource(strings = "wiskott-aldrich syndrome")
    void identifierCaseChangeWillWork(final String identifier) {
        final Disease retObj = repo.findByIdentifierIgnoreCase(identifier);
        assertThat(retObj).isNotNull();
        assertObject(retObj);
    }

    @ParameterizedTest
    @ValueSource(strings = "WaS")
    void acronymCaseChangeWillWork(final String acronym) {
        final Disease retObj = repo.findByAcronymIgnoreCase(acronym);
        assertThat(retObj).isNotNull();
        assertObject(retObj);
    }

    @ParameterizedTest
    @ValueSource(strings = "dI-01147")
    void accessionCaseChangeWillNotWork(final String accession) {
        final Disease retObj = repo.findByAccession(accession);
        assertThat(retObj).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = "*syndrome*")
    void searchInIdentifierAccessionAcronymAlternativeNameDefinition(final String input) {
        final Collection<Disease> retCol = repo
                .findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrAcronymIgnoreCaseLikeOrAlternativeNamesIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
                        input, input, input, input, input);
        assertNotNull(retCol);
        assertEquals(4, retCol.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"wiskott-aldrich", "aldrich", "wiskott"})
    void findByidentifierWholeWordCaseInsensitive(final String id) {
        final Pattern input = Pattern.compile("\\b"+id+"\\b", Pattern.CASE_INSENSITIVE);
        final Collection<Disease> result = repo.findByIdentifierRegex(input);
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result).extracting(ACCESSION_PROP).doesNotContain("DI-01149","DI-01150");
    }

    @ParameterizedTest
    @ValueSource(strings = {"wiskott-aldric", "iskott-aldrich", "tt-al"})
    void findByidentifierWholeWord(final String id) {
        final Pattern input = Pattern.compile("\\b"+id+"\\b", Pattern.CASE_INSENSITIVE);
        final Collection<Disease> result = repo.findByIdentifierRegex(input);
        assertThat(result).isNotNull().hasSize(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-"})
    void dashWillConsiderCompleteWord(final String id) {
        final Pattern input = Pattern.compile("\\b"+id+"\\b", Pattern.CASE_INSENSITIVE);
        final Collection<Disease> result = repo.findByIdentifierRegex(input);
        assertThat(result).isNotNull().hasSize(4);
    }

    @ParameterizedTest
    @ValueSource(strings = "immunodefiCiency")
    void searchInIdentifierAccessionAcronymAlternativeNameDefinitionWholeWordIgnoreCase(final String id) {
        final Pattern i = Pattern.compile(id, Pattern.CASE_INSENSITIVE);

        final Collection<Disease> result = repo
                .findByIdentifierRegexOrAccessionRegexOrAcronymRegexOrAlternativeNamesRegexOrDefinitionRegex(i,i,i,i,
                        i);
        assertThat(result).isNotNull().hasSize(2);
    }

    @Test
    void passingDocumentDiaynamicQueryShouldFulfilCriteriaAndTest(){
        final Document cri1 = new Document("accession", "DI-01150");
        final Document cri2 = new Document("acronym","WPWS");
        final Document query = new Document("$and", Arrays.array(cri1, cri2));

        final Collection<Disease> result = repo.findByJsonDocumentQuery(query);
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result).extracting(ACCESSION_PROP).contains("DI-01150");
    }

    @Test
    void passingDocumentDiaynamicQueryShouldFulfilCriteriaOrTest(){
        final Document cri1 = new Document("accession", "DI-01150");
        final Document cri2 = new Document("acronym","WRS");
        final Document query = new Document("$or", Arrays.array(cri1, cri2));

        final Collection<Disease> result = repo.findByJsonDocumentQuery(query);
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result).extracting(ACCESSION_PROP).contains("DI-01150","DI-01149");
    }

    @ParameterizedTest
    @ValueSource(strings = "n")
    void autoCompleteAndPaginationChecks(final String id) {
        List<DiseaseAutoComplete> result = repo.findProjectedByIdentifierIgnoreCaseLike(id, PageRequest.of(0, 1));
        assertThat(result).isNotNull().hasSize(1);

        result = repo.findProjectedByIdentifierIgnoreCaseLike(id, PageRequest.of(0, 10));
        assertThat(result).isNotNull().hasSize(4);

        assertThat(result).extracting(IDENTIFIER_PROP).contains("Wolff-Parkinson-White syndrome", "Wolcott-Rallison " +
                "syndrome", "Wiskott-Aldrich syndrome 2", "Wiskott-Aldrich syndrome");
    }

}
