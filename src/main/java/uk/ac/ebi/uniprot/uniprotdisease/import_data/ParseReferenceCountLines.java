package uk.ac.ebi.uniprot.uniprotdisease.import_data;

import uk.ac.ebi.uniprot.uniprotdisease.dto.DiseaseReferenceCount;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseReferenceCountLines {

    private static final int SWISSPROT = 0;
    private static final int TREMBL = 1;
    private static final Logger LOG = LoggerFactory.getLogger(ParseReferenceCountLines.class);

    public Collection<DiseaseReferenceCount> parseLinesFromReader(final Reader reader) throws IOException {

        //Default is RFC4180
        final CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        //initialize the CSVParser object
        final CSVParser parser = new CSVParser(reader, format);

        final Map<String, DiseaseReferenceCount> tempMap = new HashMap<>(5200, 1);

        for (CSVRecord record : parser) {
            if (isValidRecord(record)) {
                final String identifier = record.get(0).trim();
                final int type = Integer.parseInt(record.get(1).trim());
                final int count = Integer.valueOf(record.get(2).trim());

                tempMap.merge(identifier, getDiseaseRefCountObject(identifier, type, count), this::merge);
            } else {
                LOG.info("Ignoring Record/line {} while parsing", record);
            }
        }

        return tempMap.values();
    }

    private DiseaseReferenceCount merge(DiseaseReferenceCount old, DiseaseReferenceCount val) {
        final DiseaseReferenceCount retObj = new DiseaseReferenceCount(val.getIdentifier());
        retObj.setSwissProtCount(Math.max(old.getSwissProtCount(), val.getSwissProtCount()));
        retObj.setTremblCount(Math.max(old.getTremblCount(), val.getTremblCount()));
        return retObj;
    }

    private DiseaseReferenceCount getDiseaseRefCountObject(final String identifier, final int type, final int count) {
        final DiseaseReferenceCount retObj = new DiseaseReferenceCount(identifier);
        if (type == SWISSPROT) {
            retObj.setSwissProtCount(count);
        } else if (type == TREMBL) {
            retObj.setTremblCount(count);
        }
        return retObj;
    }

    private boolean isValidRecord(CSVRecord record) {

        if (record.size() < 3) {
            return false;
        }

        if (record.get(0).trim().isEmpty()) {
            return false;
        } else if (record.get(1).trim().isEmpty() || !("0".equals(record.get(1).trim()) || "1".equals(record.get(1)
                .trim()))) {
            return false;
        } else {
            try {
                Integer.valueOf(record.get(2).trim());
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }
}
