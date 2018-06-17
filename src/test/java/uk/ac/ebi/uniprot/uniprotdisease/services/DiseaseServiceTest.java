package uk.ac.ebi.uniprot.uniprotdisease.services;

import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;
import uk.ac.ebi.uniprot.uniprotdisease.dto.DiseaseAutoComplete;
import uk.ac.ebi.uniprot.uniprotdisease.repositories.DiseaseRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

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
        when(repo.findByIdentifierIgnoreCase(anyString())).thenReturn(data.get(1));

        Disease found = diseaseService.findByIdentifier("CK syndrome");
        assertNotNull(found);
        assertEquals("CKS", found.getAcronym());
    }

    @Test
    void findByAcronymShouldReturnSingleResultFromRepo() {
        when(repo.findByAcronymIgnoreCase(anyString())).thenReturn(data.get(2));

        Disease found = diseaseService.findByAcronym("CPI");
        assertNotNull(found);
        assertEquals("DI-01837", found.getAccession());
    }

    @Test
    void findByIdentifierIgnoreCaseLikeShouldAddStarInParam() {
        Pattern p = Pattern.compile("\\bi\\b", Pattern.CASE_INSENSITIVE);
        when(repo.findByIdentifierRegex(refEq(p))).thenReturn(data);
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
    void testKeywordSearch() {
        Pattern input = Pattern.compile("\\binner\\b", Pattern.CASE_INSENSITIVE);
        doReturn(data.subList(0, 1)).when(repo)
                .findByIdentifierRegexOrAccessionRegexOrAcronymRegexOrAlternativeNamesRegexOrDefinitionRegex(
                        refEq(input), refEq(input), refEq(input), refEq(input), refEq(input));

        input = Pattern.compile("\\bman\\b", Pattern.CASE_INSENSITIVE);
        doReturn(data.subList(1, 2)).when(repo)
                .findByIdentifierRegexOrAccessionRegexOrAcronymRegexOrAlternativeNamesRegexOrDefinitionRegex(
                        refEq(input), refEq(input), refEq(input), refEq(input), refEq(input));

        input = Pattern.compile("\\bouter\\b", Pattern.CASE_INSENSITIVE);
        doReturn(data.subList(2, 3)).when(repo)
                .findByIdentifierRegexOrAccessionRegexOrAcronymRegexOrAlternativeNamesRegexOrDefinitionRegex(
                        refEq(input), refEq(input), refEq(input), refEq(input), refEq(input));

        input = Pattern.compile("\\bnot\\b", Pattern.CASE_INSENSITIVE);
        doReturn(data.subList(0, 1)).when(repo)
                .findByIdentifierRegexOrAccessionRegexOrAcronymRegexOrAlternativeNamesRegexOrDefinitionRegex(
                        refEq(input), refEq(input), refEq(input), refEq(input), refEq(input));

        Collection<Disease> retCol = diseaseService.findAllByKeyWordSearch("inNer Outer INNER man noT");
        assertNotNull(retCol);
        assertEquals(3, retCol.size());

        verify(repo, times(4))
                .findByIdentifierRegexOrAccessionRegexOrAcronymRegexOrAlternativeNamesRegexOrDefinitionRegex(
                        any(Pattern.class), any(Pattern.class), any(Pattern.class), any(Pattern.class), any(Pattern.class));
    }

    @Test
    void autoCompleteShouldCallPaginationWithDefault10WhenPassingSizeNull(){
        when(repo.findProjectedByIdentifierIgnoreCaseLike(anyString(), refEq(PageRequest.of(0,10)))).thenReturn
                (Collections.emptyList());
        final List<DiseaseAutoComplete> retList = diseaseService.autoCompleteSearch("s", null);
    }

    @Test
    void autoCompleteShouldCallPaginationWithDefault10WhenPassingSize0(){
        when(repo.findProjectedByIdentifierIgnoreCaseLike(anyString(), refEq(PageRequest.of(0,10)))).thenReturn
                (Collections.emptyList());
        final List<DiseaseAutoComplete> retList = diseaseService.autoCompleteSearch("s", 0);
    }

    @Test
    void autoCompleteShouldCallPaginationWithIntegerMaxWhenPassingSizeMinusValue(){
        when(repo.findProjectedByIdentifierIgnoreCaseLike(anyString(), refEq(PageRequest.of(0,Integer.MAX_VALUE)))).thenReturn
                (Collections.emptyList());
        final List<DiseaseAutoComplete> retList = diseaseService.autoCompleteSearch("s", -1);
    }

    @Test
    void autoCompleteWhenPassingPositiveValue(){
        when(repo.findProjectedByIdentifierIgnoreCaseLike(eq("s"), refEq(PageRequest.of(0,5)))).thenReturn
                (Collections.emptyList());
        final List<DiseaseAutoComplete> retList = diseaseService.autoCompleteSearch("s", 5);
    }
}
