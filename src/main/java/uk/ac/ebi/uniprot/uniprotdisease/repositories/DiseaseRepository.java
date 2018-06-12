package uk.ac.ebi.uniprot.uniprotdisease.repositories;

import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;

import java.util.Collection;
import java.util.regex.Pattern;
import org.bson.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DiseaseRepository extends MongoRepository<Disease, String> {

    Disease findByAccession(String accession);

    Disease findByIdentifierIgnoreCase(String identifier);

    Disease findByAcronymIgnoreCase(String acronym);

    Collection<Disease> findByIdentifierIgnoreCaseLike(String identifier);

    Collection<Disease>
    findByIdentifierIgnoreCaseLikeOrAccessionIgnoreCaseLikeOrAcronymIgnoreCaseLikeOrAlternativeNamesIgnoreCaseLikeOrDefinitionIgnoreCaseLike(
            String identifier, String accession, String acronym, String alternativeNames, String definition);

    Collection<Disease>
    findByIdentifierRegexOrAccessionRegexOrAcronymRegexOrAlternativeNamesRegexOrDefinitionRegex(
            Pattern identifier, Pattern accession, Pattern acronym, Pattern alternativeNames, Pattern definition);

    Collection<Disease> findByIdentifierRegex(Pattern regex);

    @Query("?0")
    Collection<Disease> findByJsonDocumentQuery(Document query);
}
