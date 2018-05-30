package uk.ac.ebi.uniprot.uniprotdisease.integration_test;

import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;
import uk.ac.ebi.uniprot.uniprotdisease.services.DiseaseService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.commons.io.IOUtils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@TestInstance (TestInstance.Lifecycle.PER_CLASS)
class DefaultControllerIT {

    private static final String TEMP_FILE_NAME = "disease-data-temp.txt";
    private ObjectMapper mapper;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private DiseaseService service;

    @BeforeAll
    void downloadAndSaveFile() throws IOException {
        URL url = new URL(
                "ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/docs/humdisease.txt");
        URLConnection connection = url.openConnection();
        InputStream initialStream = connection.getInputStream();
        File targetFile = new File(TEMP_FILE_NAME);

        Files.copy(
                initialStream,
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        IOUtils.closeQuietly(initialStream);

        service.importDiseaseEntriesFromFileIntoDb(new File(TEMP_FILE_NAME).getPath());
        mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @AfterAll
    void deleteFile() throws IOException {
        Files.deleteIfExists(new File(TEMP_FILE_NAME).toPath());
    }


    @Test
    void testAccessionEndPoint() throws IOException {
        ResponseEntity<String> rawRes = rest.getForEntity("/accession/{accession}", String.class, "DI-00312");

        assertEquals(HttpStatus.OK, rawRes.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_UTF8, rawRes.getHeaders().getContentType());

        Disease response = mapper.readValue(rawRes.getBody(), Disease.class);
        assertThat(response.getIdentifier()).isEqualTo("Cockayne syndrome B");
        assertThat(response.getAcronym()).isEqualTo("CSB");
    }

    @Test
    void testAccessionAllEndPoint() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("identifier", "q10");
        ResponseEntity<String> rawRes = rest.getForEntity("/identifier/all/{identifier}", String.class, params);

        assertEquals(HttpStatus.OK, rawRes.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_UTF8, rawRes.getHeaders().getContentType());

        List<Disease> retCol = mapper.readValue(rawRes.getBody(), new TypeReference<List<Disease>>() {});
        assertEquals(8, retCol.size());
    }

    @Test
    void testSearchEndPoint() throws IOException {
        ResponseEntity<String> rawRes = rest.getForEntity("/search/{keywords}", String.class, "insipidus");

        assertEquals(HttpStatus.OK, rawRes.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_UTF8, rawRes.getHeaders().getContentType());

        Disease[] retArr = mapper.readValue(rawRes.getBody(), Disease[].class);
        assertEquals(5, retArr.length);
    }
}