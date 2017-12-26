package com.playtika.sales.client.web;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.playtika.sales.client.domain.dto.CarSaleDto;
import com.playtika.sales.client.service.CarSalesExtractorService;
import com.playtika.sales.client.service.external.http.CarServiceClient;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(MockitoJUnitRunner.class)
public class CarServiceClientControllerRestTest {

    MockMvc mockMvc;

    @Mock
    CarSalesExtractorService extractor;

    @Rule
    public WireMockRule wm = new WireMockRule(options().port(8091));

    @Before
    public void setUp() throws Exception {
        CarServiceClient feignClient = Feign.builder()
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(CarServiceClient.class, "http://localhost:8091");
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CarServiceClientController(extractor, feignClient))
                .build();
    }

    @Test
    public void successfulRegistrationCarSales() throws Exception {
        mockExtractorService(asList(generateTestDto("AA3295", "Vyshnivskyi"), generateTestDto("AA3296", "Vyshnivskyi")));
        generateStubCarSaleService("AA3295", ok("1"));
        generateStubCarSaleService("AA3296", ok("2"));

        long result = Long.valueOf(
                mockMvc.perform(MockMvcRequestBuilders.post("/cars")
                        .contentType("text/plain;charset=UTF-8")
                        .content(String.valueOf(new URL("http://test.csv"))))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse()
                        .getContentAsString());
        assertThat(result, Matchers.is(0L));
    }

    @Test
    public void unSuccessfulCauseConflictRegistrationPartOfCarSales() throws Exception {
        mockExtractorService(asList(generateTestDto("AA3295", "Vyshnivskyi"), generateTestDto("AA3296", "Vyshnivskyi")));
        generateStubCarSaleService("AA3295", ok("1"));
        generateStubCarSaleService("AA3296", aResponse().withStatus(CONFLICT.value()));

        long result = Long.valueOf(
                mockMvc.perform(MockMvcRequestBuilders.post("/cars")
                        .contentType("text/plain;charset=UTF-8")
                        .content(String.valueOf(new URL("http://test.csv"))))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse()
                        .getContentAsString());
        assertThat(result, Matchers.is(1L));
    }

    @Test
    public void unSuccessfulCauseBadRequestRegistrationPartOfCarSales() throws Exception {
        mockExtractorService(asList(generateTestDto("AA3295", "Vyshnivskyi"), generateTestDto("AA3296", "Vyshnivskyi")));
        generateStubCarSaleService("AA3295", ok("1"));
        generateStubCarSaleService("AA3296", aResponse().withStatus(BAD_REQUEST.value()));

        long result = Long.valueOf(
                mockMvc.perform(MockMvcRequestBuilders.post("/cars")
                        .contentType("text/plain;charset=UTF-8")
                        .content(String.valueOf(new URL("http://test.csv"))))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                        .andReturn()
                        .getResponse()
                        .getContentAsString());
        assertThat(result, Matchers.is(1L));
    }

    @Test
    public void exceptionAppearsWhenCanNotExtractCarSalesFromUrl() throws Exception {
        when(extractor.extractAllCarSales(new URL("http://test.csv"))).thenThrow(IOException.class);
        Exception resolved = mockMvc.perform(MockMvcRequestBuilders.post("/cars")
                .contentType("text/plain;charset=UTF-8")
                .content(String.valueOf(new URL("http://test.csv"))))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertThat(resolved.getClass(), typeCompatibleWith(CarServiceClientController.NotPossibleToDownloadFileException.class));
    }

    void generateStubCarSaleService(String number, ResponseDefinitionBuilder response) {
        stubFor(WireMock.post("/cars?price=2003.1&firstName=Roma&phone=380960000000&lastName=Vyshnivskyi")
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson(getCarJSON(number)))
                .willReturn(response));
    }

    void mockExtractorService(List<CarSaleDto> carSales) throws IOException {
        when(extractor.extractAllCarSales(new URL("http://test.csv"))).thenReturn(carSales);
    }

    String getCarJSON(final String number) {
        return "{\"brand\":\"BMW\",\"color\":\"red\",\"year\":2001,\"number\":\"" + number + "\"}";
    }

    CarSaleDto generateTestDto(String number, String lastName) {
        CarSaleDto testDto = new CarSaleDto();
        testDto.setBrand("BMW");
        testDto.setColor("red");
        testDto.setYear(2001);
        testDto.setPrice(2003.1);
        testDto.setOwnerFirstName("Roma");
        testDto.setOwnerLastName(lastName);
        testDto.setOwnerPhoneNumber("380960000000");
        testDto.setNumber(number);
        return testDto;
    }
}
