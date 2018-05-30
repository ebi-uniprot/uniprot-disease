package uk.ac.ebi.uniprot.uniprotdisease.repositories;

import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;

import java.util.Collection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiseaseRepository extends MongoRepository<Disease, String> {

    Disease findByAccession(String accession);

    Disease findByIdentifier(String identifier);

    Disease findByAcronym(String acronym);

    Collection<Disease> findByIdentifierIgnoreCaseLike(String identifier);

    Collection<Disease>
    findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrAcronymIgnoreCaseLikeOrAlternativeNamesIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
            String identifier, String accession, String acronym, String alternativeNames, String definition);
}
