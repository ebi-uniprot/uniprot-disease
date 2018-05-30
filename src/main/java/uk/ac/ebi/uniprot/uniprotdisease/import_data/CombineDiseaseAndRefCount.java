package uk.ac.ebi.uniprot.uniprotdisease.import_data;

import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;
import uk.ac.ebi.uniprot.uniprotdisease.dto.DiseaseReferenceCount;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CombineDiseaseAndRefCount {

    private static final Logger LOG = LoggerFactory.getLogger(CombineDiseaseAndRefCount.class);

    public List<Disease> readFileImportAndCombine(String diseaseFilePath, String referenceCountFilePath) {
        diseaseFilePath = diseaseFilePath == null ? "" : diseaseFilePath.trim();
        referenceCountFilePath = referenceCountFilePath == null ? "" : referenceCountFilePath.trim();

        List<String> allLines = null;
        LOG.debug("File: {} to import disease data set ", diseaseFilePath);
        try {
            allLines = Files.readAllLines(Paths.get(diseaseFilePath));
        } catch (IOException e) {
            LOG.error("Exception Handle gracefully: Failed to read file {} ", diseaseFilePath, e);
            allLines = Collections.emptyList();
        }
        LOG.debug("total {} lines found in file ", allLines.size());

        final ParseDiseaseLines parser = new ParseDiseaseLines();
        List<Disease> diseaseList = parser.parseLines(allLines);
        LOG.info("total {} entries found in file ", diseaseList.size());

        LOG.debug("File: {} to import disease reference count ", referenceCountFilePath);
        final ParseReferenceCountLines refParser = new ParseReferenceCountLines();
        Collection<DiseaseReferenceCount> referenceCountList;
        try {
            referenceCountList = refParser.parseLinesFromReader(new FileReader(referenceCountFilePath));
        } catch (IOException e) {
            LOG.error("Exception Handle gracefully: Failed to read file {} ", referenceCountFilePath, e);
            referenceCountList = Collections.emptyList();
        }
        LOG.info("total {} Reference count found in file ", referenceCountList.size());

        updateDiseasesWithReferenceCount(diseaseList, referenceCountList);

        return diseaseList;
    }

    private void updateDiseasesWithReferenceCount(List<Disease> diseaseList,
            Collection<DiseaseReferenceCount> referenceCountList) {
        // Loop on reference count list
        referenceCountList.forEach(
                // Find the disease from disease list
                rc -> diseaseList.stream().filter(d -> d.getIdentifier().equalsIgnoreCase(rc.getIdentifier()))
                        .findFirst()
                        .ifPresent(
                                //Disease found from list
                                disease -> {
                                    //Updating keyword or category count from reference count object
                                    disease.setSwissProtCount(rc.getSwissProtCount());
                                    disease.setTremblCount(rc.getTremblCount());
                                }
                        )

        );
    }
}
