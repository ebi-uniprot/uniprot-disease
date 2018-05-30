package uk.ac.ebi.uniprot.uniprotdisease.services;

import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;
import uk.ac.ebi.uniprot.uniprotdisease.import_data.CombineDiseaseAndRefCount;
import uk.ac.ebi.uniprot.uniprotdisease.repositories.DiseaseRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiseaseService {

    private static final Logger LOG = LoggerFactory.getLogger(DiseaseService.class);
    private final DiseaseRepository diseaseRepository;

    public DiseaseService(DiseaseRepository diseaseRepository) {this.diseaseRepository = diseaseRepository;}

    public void importDiseaseEntriesFromFileIntoDb(String pathToDiseaseFile) {
        importDiseaseAndReferenceCountFromFilesIntoDb(pathToDiseaseFile, "reference count file not given");
    }

    @Transactional
    public void importDiseaseAndReferenceCountFromFilesIntoDb(String pathToDiseaseFile, String pathToRefCountFile) {
        CombineDiseaseAndRefCount obj = new CombineDiseaseAndRefCount();
        final List<Disease> diseases = obj.readFileImportAndCombine(pathToDiseaseFile, pathToRefCountFile);
        diseaseRepository.saveAll(diseases);
        LOG.info("{} diseases saved into database", diseases.size());
    }

    @Transactional(readOnly = true)
    public Disease findByAccession(final String accession) {
        return diseaseRepository.findByAccession(accession);
    }

    @Transactional(readOnly = true)
    public Disease findByIdentifier(final String identifier) {
        return diseaseRepository.findByIdentifier(identifier);
    }

    @Transactional(readOnly = true)
    public Disease findByAcronym(final String acronym) {
        return diseaseRepository.findByAcronym(acronym);
    }

    @Transactional(readOnly = true)
    public Collection<Disease> findByIdentifierIgnoreCaseLike(final String identifier) {
        return diseaseRepository.findByIdentifierIgnoreCaseLike("*" + identifier + "*");
    }

    @Transactional(readOnly = true)
    public Collection<Disease> findAllByKeyWordSearch(final String input) {
        Set<String> words =
                Stream.of(input.split("\\s+")).map(s -> "*" + s.toLowerCase() + "*").collect(Collectors.toSet());
        // Database will be embedded so we can query multiple times with minimum performance hit
        // We could build dynamic query to save DB hits, but that will increase code also load on DB to scan for huge
        // set
        return words.stream()
                .flatMap(i -> diseaseRepository
                        .findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrAcronymIgnoreCaseLikeOrAlternativeNamesIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
                                i, i, i, i, i).stream()).collect(Collectors.toSet());
    }
}
