package uk.ac.ebi.uniprot.uniprotdisease.services;

import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;
import uk.ac.ebi.uniprot.uniprotdisease.repositories.DiseaseRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiseaseServiceTest {
    @Mock
    private DiseaseRepository repo;
    private DiseaseService diseaseService;
    private static List<Disease> data;

    @BeforeAll
    static void data() {
        final Disease l = new Disease("Citrullinemia 2", "DI-00310","CTLN2");
        final Disease o = new Disease("CK syndrome", "DI-03007", "CKS");
        final Disease t = new Disease("Cleft palate isolated", "DI-01837", "CPI");
        data = Arrays.asList(l, o, t);
    }

    @BeforeEach
    void setup() {
        diseaseService = new DiseaseService(repo);
    }

    @Test
    void findByAccessionShouldReturnSingleResultFromRepo() {
        when(repo.findByAccession(anyString())).thenReturn(data.get(0));
        Disease found = diseaseService.findByAccession("DI-00310");
        assertNotNull(found);
        assertEquals("Citrullinemia 2", found.getIdentifier());
    }

    @Test
    void findByIdentifierShouldReturnSingleResultFromRepo() {
        when(repo.findByIdentifier(anyString())).thenReturn(data.get(1));

        Disease found = diseaseService.findByIdentifier("CK syndrome");
        assertNotNull(found);
        assertEquals("CKS", found.getAcronym());
    }

    @Test
    void findByAcronymShouldReturnSingleResultFromRepo() {
        when(repo.findByAcronym(anyString())).thenReturn(data.get(2));

        Disease found = diseaseService.findByAcronym("CPI");
        assertNotNull(found);
        assertEquals("DI-01837", found.getAccession());
    }

    @Test
    void findByIdentifierIgnoreCaseLikeShouldAddStarInParam() {
        when(repo.findByIdentifierIgnoreCaseLike(eq("*i*"))).thenReturn(data);
        Collection<Disease> retCol = diseaseService.findByIdentifierIgnoreCaseLike("i");
        assertNotNull(retCol);
        assertEquals(3, retCol.size());
    }

    @Test
    void importEntriesPassingNullWillNotThrowExceptionWillHandleGracefully() {
        doAnswer(returnsFirstArg()).when(repo).saveAll(anyCollection());
        diseaseService.importDiseaseEntriesFromFileIntoDb(null);
    }

    @Test
    public void testKeywordSearch() {
        String input = "*inner*";
        doReturn(data.subList(0, 1)).when(repo)
                .findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrAcronymIgnoreCaseLikeOrAlternativeNamesIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
                        eq(input), eq(input), eq(input), eq(input), eq(input));

        input = "*man*";
        doReturn(data.subList(1, 2)).when(repo)
                .findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrAcronymIgnoreCaseLikeOrAlternativeNamesIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
                        eq(input), eq(input), eq(input), eq(input), eq(input));

        input = "*outer*";
        doReturn(data.subList(2, 3)).when(repo)
                .findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrAcronymIgnoreCaseLikeOrAlternativeNamesIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
                        eq(input), eq(input), eq(input), eq(input), eq(input));

        input = "*not*";
        doReturn(data.subList(0, 1)).when(repo)
                .findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrAcronymIgnoreCaseLikeOrAlternativeNamesIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
                        eq(input), eq(input), eq(input), eq(input), eq(input));

        Collection<Disease> retCol = diseaseService.findAllByKeyWordSearch("inNer Outer INNER man noT");
        assertNotNull(retCol);
        assertEquals(3, retCol.size());

        verify(repo, times(4))
                .findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrAcronymIgnoreCaseLikeOrAlternativeNamesIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
                        anyString(), anyString(), anyString(), anyString(), anyString());
    }
}
