package uk.ac.ebi.uniprot.uniprotdisease.controllers;

import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;
import uk.ac.ebi.uniprot.uniprotdisease.services.DiseaseService;

import java.util.Collection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class DefaultController {

    private final DiseaseService diseaseService;

    public DefaultController(DiseaseService diseaseService) {
        this.diseaseService = diseaseService;
    }

    @GetMapping("accession/{accession}")
    public Disease findByAccession(@PathVariable String accession) {
        return diseaseService.findByAccession(accession);
    }

    @GetMapping("acronym/{acronym}")
    public Disease findByAcronym(@PathVariable String acronym) { return diseaseService.findByAcronym(acronym); }

    @GetMapping("identifier/{identifier}")
    public Disease findByIdentifier(@PathVariable String identifier) {
        return diseaseService.findByIdentifier(identifier);
    }

    @GetMapping("identifier/all/{identifier}")
    public Collection<Disease> findByidentifierLikeIgnoreCase(@PathVariable String identifier) {
        return diseaseService.findByIdentifierIgnoreCaseLike(identifier);
    }

    @GetMapping("search/{wordSeperatedBySpace}")
    public Collection<Disease> search(@PathVariable String wordSeperatedBySpace) {
        return diseaseService.findAllByKeyWordSearch(wordSeperatedBySpace);
    }
}