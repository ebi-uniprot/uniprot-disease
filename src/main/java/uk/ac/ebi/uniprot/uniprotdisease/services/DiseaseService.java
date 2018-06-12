package uk.ac.ebi.uniprot.uniprotdisease.services;

import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;
import uk.ac.ebi.uniprot.uniprotdisease.import_data.CombineDiseaseAndRefCount;
import uk.ac.ebi.uniprot.uniprotdisease.repositories.DiseaseRepository;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.regex.Pattern.*;

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
        return diseaseRepository.findByIdentifierIgnoreCase(identifier);
    }

    @Transactional(readOnly = true)
    public Disease findByAcronym(final String acronym) {
        return diseaseRepository.findByAcronymIgnoreCase(acronym);
    }

    @Transactional(readOnly = true)
    public Collection<Disease> findByIdentifierIgnoreCaseLike(final String identifier) {
        final Pattern input = compile("\\b" + identifier + "\\b", CASE_INSENSITIVE);
        return diseaseRepository.findByIdentifierRegex(input);
    }

    @Transactional(readOnly = true)
    public Collection<Disease> findAllByKeyWordSearch(final String input) {
        //Java don't override equals for Patthern
        Comparator<Pattern> comp = (p1, p2) -> p1.pattern().compareTo(p2.pattern()) + (p1.flags()-p2.flags());

        //Make only unique quries
        Set<Pattern> words =
                Stream.of(input.split("\\s+")).map(String::toLowerCase).map(s -> compile("\\b" + s + "\\b", CASE_INSENSITIVE))
                        .collect(Collectors.toCollection(()->new TreeSet<>(comp)));

        // Database will be embedded so we can query multiple times with minimum performance hit
        // We could build dynamic query to save DB hits, but that will increase code also load on DB to scan for huge
        // set
        return words.stream()
                .flatMap(i -> diseaseRepository
                        .findByIdentifierRegexOrAccessionRegexOrAcronymRegexOrAlternativeNamesRegexOrDefinitionRegex(
                                i, i, i, i, i).stream()).collect(Collectors.toSet());
    }
}
