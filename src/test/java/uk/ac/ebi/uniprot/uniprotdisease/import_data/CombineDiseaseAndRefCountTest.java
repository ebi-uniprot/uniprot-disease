package uk.ac.ebi.uniprot.uniprotdisease.import_data;

import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class CombineDiseaseAndRefCountTest {

    private static final String IDENTIFIER_PROP = "identifier";
    private static final String SWISS_PROT_COUNT_PROP = "swissProtCount";
    private static final String TREMBL_COUNT_PROP = "tremblCount";
    private final CombineDiseaseAndRefCount obj = new CombineDiseaseAndRefCount();

    @Test
    void shouldIgnoreExceptionsWhenDiseaseAndReferenceFilesNotExist() {
        final List<Disease> retList = obj.readFileImportAndCombine("abc", "def");
        assertNotNull(retList);
        assertThat(retList).hasSize(0);
    }

    @Test
    void shouldReturnNothingWhenDiseaseFileMissingAndReferenceFileAvailable() {
        final String referenceCountFilePath = ClassLoader.getSystemResource("sample-reference.txt").getPath();
        final List<Disease> retList = obj.readFileImportAndCombine(null, referenceCountFilePath);
        assertNotNull(retList);
        assertThat(retList).hasSize(0);
    }

    @Test
    void countsShouldZeroWhenRefCountFileNotValid() {
        final String diseaseFilePath = ClassLoader.getSystemResource("sample-diseases.txt").getPath();
        final List<Disease> retList = obj.readFileImportAndCombine(diseaseFilePath, "tmp");

        assertAll(
                () -> assertNotNull(retList),
                () -> assertThat(retList).hasSize(4),
                () -> assertThat(retList.get(0)).extracting(IDENTIFIER_PROP, SWISS_PROT_COUNT_PROP, TREMBL_COUNT_PROP)
                        .containsExactly("Wiskott-Aldrich syndrome", 0, 0),
                () -> assertThat(retList.get(3)).extracting(IDENTIFIER_PROP, SWISS_PROT_COUNT_PROP, TREMBL_COUNT_PROP)
                        .containsExactly("Wolff-Parkinson-White syndrome", 0, 0)
        );
    }

    @Test
    void countShouldValidWhenRefFileContainEntires() {
        final String diseaseFilePath = ClassLoader.getSystemResource("sample-diseases.txt").getPath();
        final String referenceCountFilePath = ClassLoader.getSystemResource("sample-reference.txt").getPath();
        final List<Disease> retList = obj.readFileImportAndCombine(diseaseFilePath, referenceCountFilePath);

        assertAll(
                () -> assertNotNull(retList),
                () -> assertThat(retList).hasSize(4),
                () -> assertThat(retList.get(1)).extracting(IDENTIFIER_PROP, SWISS_PROT_COUNT_PROP, TREMBL_COUNT_PROP)
                        .containsExactly("Wiskott-Aldrich syndrome 2", 1, 3),
                () -> assertThat(retList.get(2)).extracting(IDENTIFIER_PROP, SWISS_PROT_COUNT_PROP, TREMBL_COUNT_PROP)
                        .containsExactly("Wolcott-Rallison syndrome", 2, 4)
        );
    }
}
