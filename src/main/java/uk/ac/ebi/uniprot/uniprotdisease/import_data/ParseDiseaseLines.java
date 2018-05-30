package uk.ac.ebi.uniprot.uniprotdisease.import_data;

import uk.ac.ebi.uniprot.uniprotdisease.domains.CrossReference;
import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;
import uk.ac.ebi.uniprot.uniprotdisease.domains.Keyword;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseDiseaseLines {

    private static final String SPLIT_SPACES = "   ";
    private static final String COLON = ":";
    private static final String SEMICOLON = ";";
    private static final Logger LOG = LoggerFactory.getLogger(ParseDiseaseLines.class);

    public List<Disease> parseLines(List<String> lines) {
        return convertLinesIntoInMemoryObjectList(lines).stream().map(this::parseDiseaseFileEntry)
                .collect(Collectors.toList());
    }

    private Disease parseDiseaseFileEntry(DiseaseFileEntry entry) {
        final Disease retObj = new Disease(trimSpacesAndRemoveLastDot(entry.id), entry.ac, trimSpacesAndRemoveLastDot
                (entry.ar));

        // definition
        String def = String.join(" ", entry.de);
        retObj.setDefinition(def.isEmpty() ? null : def);

        // Synonyms or Alternative Names
        List<String> synList =
                entry.sy.stream()
                        .map(this::trimSpacesAndRemoveLastDot)
                        .collect(Collectors.toList());
        retObj.setAlternativeNames(synList.isEmpty() ? null : synList);

        // Cross-reference(s)
        List<CrossReference> crList = entry.dr.stream().map(this::parseCrossReference).collect(Collectors.toList());
        retObj.setCrossReferences(crList.isEmpty() ? null : crList);

        // keyword(s)
        List<Keyword> kwList = entry.kw.stream().map(this::parseKeyword).collect(Collectors.toList());
        retObj.setAssociatedkeywords(kwList.isEmpty() ? null : kwList);

        return retObj;
    }

    private Keyword parseKeyword(String kw) {
        final String[] tokens = kw.split(COLON);
        return new Keyword(tokens[0], trimSpacesAndRemoveLastDot(tokens[1]));
    }

    private CrossReference parseCrossReference(String cr) {
        final String[] tokens = cr.split(SEMICOLON);
        final String type = trimSpacesAndRemoveLastDot(tokens[0]);
        final String id = trimSpacesAndRemoveLastDot(tokens[1]);
        final List<String> des =
                Stream.of(Arrays.copyOfRange(tokens, 2, tokens.length)).map(this::trimSpacesAndRemoveLastDot)
                        .collect(Collectors.toList());

        return new CrossReference(id, type, des.isEmpty() ? null : des);
    }

    private String trimSpacesAndRemoveLastDot(String str) {
        if (str == null) {
            return null;
        }
        str = str.trim();
        return str.endsWith(".") ? str.substring(0, str.length() - 1) : str;
    }

    private List<DiseaseFileEntry> convertLinesIntoInMemoryObjectList(List<String> lines) {
        // At the time of writing code there was 5047 entries in file
        List<DiseaseFileEntry> retList = new ArrayList<>(5200);

        int i = 0;

        // Ignore the header lines and information
        for (; i < lines.size(); i++) {
            String lineIgnore = lines.get(i);
            if (lineIgnore.startsWith("______")) {
                break;
            }
        }

        // Ignore underscore ___ line
        i++;

        // reached entries lines
        DiseaseFileEntry entry = new DiseaseFileEntry();

        // create in memory list of objects
        while (i < lines.size()) {
            String line = lines.get(i);

            // For terminating line no need to complete loop
            if (line.equals("//")) {
                retList.add(entry);
                entry = new DiseaseFileEntry();
                i++;
                continue;
            }

            String[] tokens = line.split(SPLIT_SPACES);
            switch (tokens[0]) {
                case "ID":
                    entry.id = tokens[1];
                    break;
                case "AR":
                    entry.ar = tokens[1];
                    break;
                case "AC":
                    entry.ac = tokens[1];
                    break;
                case "DE":
                    entry.de.add(tokens[1]);
                    break;
                case "SY":
                    entry.sy.add(tokens[1]);
                    break;
                case "DR":
                    entry.dr.add(tokens[1]);
                    break;
                case "KW":
                    entry.kw.add(tokens[1]);
                    break;
                default:
                    LOG.info("Unhandle line found while parsing file: {}", line);

            }

            // read and save next line
            i++;
        }
        return retList;
    }

    private class DiseaseFileEntry {
        String id;
        String ac;
        String ar;
        List<String> de;
        List<String> sy;
        List<String> dr;
        List<String> kw;

        DiseaseFileEntry() {
            de = new ArrayList<>();
            sy = new ArrayList<>();
            dr = new ArrayList<>();
            kw = new ArrayList<>();
        }
    }
}
