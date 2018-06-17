package uk.ac.ebi.uniprot.uniprotdisease.controllers;

import uk.ac.ebi.uniprot.uniprotdisease.domains.Disease;
import uk.ac.ebi.uniprot.uniprotdisease.dto.DiseaseAutoComplete;
import uk.ac.ebi.uniprot.uniprotdisease.services.DiseaseService;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DefaultController.class)
@ExtendWith(SpringExtension.class)
class DefaultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiseaseService diseaseService;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final Disease l = new Disease("Citrullinemia 2", "DI-00310","CTLN2");
        final Disease o = new Disease("CK syndrome", "DI-03007", "CKS");
        final Disease t = new Disease("Cleft palate isolated", "DI-01837", "CPI");

        given(diseaseService.findByAccession("DI-00310")).willReturn(l);

        given(diseaseService.findByIdentifier("CK syndrome")).willReturn(o);

        given(diseaseService.findByAcronym("CPI")).willReturn(t);

        given(diseaseService.findByIdentifierIgnoreCaseLike("singleWord"))
                .willReturn(Arrays.asList(l, o, t));

        given(diseaseService.findAllByKeyWordSearch("any string OR any"))
                .willReturn(Arrays.asList(l, o, t));

    }

    @Test
    void testAccessionEndPoint() throws Exception {

        MvcResult rawRes = mockMvc.perform(get("/accession/{accession}", "DI-00310"))
                .andExpect(status().isOk())
                .andReturn();

        Disease response = mapper.readValue(rawRes.getResponse().getContentAsString(), Disease.class);
        assertThat(response.getAcronym()).isEqualTo("CTLN2");
        assertThat(response.getIdentifier()).isEqualTo("Citrullinemia 2");
    }

    @Test
    void testIdentifierEndPoint() throws Exception {

        MvcResult rawRes = mockMvc.perform(get("/identifier/{identifier}", "CK syndrome"))
                .andExpect(status().isOk())
                .andReturn();

        Disease response = mapper.readValue(rawRes.getResponse().getContentAsString(), Disease.class);
        assertThat(response.getAcronym()).isEqualTo("CKS");
        assertThat(response.getAccession()).isNotNull();
    }

    @Test
    void testAcronymEndPoint() throws Exception {

        MvcResult rawRes = mockMvc.perform(get("/acronym/{acronym}", "CPI"))
                .andExpect(status().isOk())
                .andReturn();

        Disease response = mapper.readValue(rawRes.getResponse().getContentAsString(), Disease.class);
        assertThat(response.getIdentifier()).isNotNull();
        assertThat(response.getAccession()).isNotNull();
    }

    @Test
    void testIdentifierAllEndPoint() throws Exception {

        MvcResult rawRes = mockMvc.perform(get("/identifier/all/{singleWord}", "singleWord"))
                .andExpect(status().isOk())
                .andReturn();

        List<Disease> retList = mapper.readValue(rawRes.getResponse().getContentAsString(),
                mapper.getTypeFactory().constructCollectionType(List.class, Disease.class));
        assertThat(retList.size()).isEqualTo(3);
        List<String> accessions = Arrays.asList("DI-00310", "DI-03007", "DI-01837");
        assertThat(retList.get(0).getAccession()).isIn(accessions);
        assertThat(retList.get(1).getAccession()).isIn(accessions);
        assertThat(retList.get(2).getAccession()).isIn(accessions);
    }

    @Test
    public void testSearchEndPoint() throws Exception {

        MvcResult rawRes = mockMvc.perform(get("/search/{wordSeperatedBySpace}", "any string OR any"))
                .andExpect(status().isOk())
                .andReturn();

        List<Disease> retList = mapper.readValue(rawRes.getResponse().getContentAsString(),
                mapper.getTypeFactory().constructCollectionType(List.class, Disease.class));

        assertThat(retList.size()).isEqualTo(3);
    }

    @Test
    public void likeEndPointForAutoCompleteWithOutSize() throws Exception {
        given(diseaseService.autoCompleteSearch("abc def", null)).willReturn(Collections.emptyList());

        MvcResult rawRes = mockMvc.perform(get("/like/{wordCanBeSeperatedBySpace}", "abc def"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void likeEndPointForAutoCompleteWithSize() throws Exception {

        given(diseaseService.autoCompleteSearch("abc", 5)).willReturn(Arrays.asList(
                new DiseaseAutoComplete() {
                    @Override public String getIdentifier() {
                        return "syndrome";
                    }

                    @Override public String getAccession() {
                        return "D-123";
                    }

                    @Override public String getAcronym() {
                        return "SDN";
                    }
                }
        ));

        MvcResult rawRes = mockMvc.perform(get("/like/abc?size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].identifier").value("syndrome"))
                .andExpect(jsonPath("$[0].accession").value("D-123"))
                .andExpect(jsonPath("$[0].acronym").value("SDN"))
                .andReturn();
    }
}
