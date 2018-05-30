package uk.ac.ebi.uniprot.uniprotdisease.repositories;

import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;
import uk.ac.ebi.uniprot.uniprotdisease.import_data.CombineDiseaseAndRefCount;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
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
        final Disease retObj = repo.findByAcronym(acronym);
        assertObject(retObj);
    }

    /**
     * Test of findByIdentifier method, of class DiseaseRepository.
     */
    @ParameterizedTest
    @ValueSource(strings = "Wiskott-Aldrich syndrome")
    void findByIdentifier(final String identifier) {
        final Disease retObj = repo.findByIdentifier(identifier);
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
    @ValueSource(strings = "*TT*")
    void identifierIgnoringCase(final String id) {
        final Collection<Disease> result = repo.findByIdentifierIgnoreCaseLike(id);
        assertThat(result).isNotNull().hasSize(3);
        assertThat(result).extracting(ACCESSION_PROP).doesNotContain("DI-01150");
    }

    @ParameterizedTest
    @ValueSource(strings = "wiskott-aldrich syndrome")
    void identifierCaseChangeWillNotWork(final String identifier) {
        final Disease retObj = repo.findByIdentifier(identifier);
        assertThat(retObj).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = "WaS")
    void acronymCaseChangeWillNotWork(final String acronym) {
        final Disease retObj = repo.findByAcronym(acronym);
        assertThat(retObj).isNull();
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

}
