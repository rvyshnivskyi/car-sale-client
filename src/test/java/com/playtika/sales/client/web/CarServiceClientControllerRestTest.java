package com.playtika.sales.client.web;

import com.playtika.sales.client.domain.Car;
import com.playtika.sales.client.domain.dto.CarSaleDto;
import com.playtika.sales.client.exception.NotPossibleToDownloadFileException;
import com.playtika.sales.client.exception.feign.CarIsAlreadyExistException;
import com.playtika.sales.client.exception.feign.CarSalesClientException;
import com.playtika.sales.client.exception.feign.CarSalesServerException;
import com.playtika.sales.client.service.CarSalesExtractorService;
import com.playtika.sales.client.service.external.http.CarServiceClient;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarServiceClientController.class)
@RunWith(SpringRunner.class)
public class CarServiceClientControllerRestTest {

    @MockBean
    CarSalesExtractorService extractor;

    @MockBean
    CarServiceClient client;

    @Autowired
    MockMvc mockMvc;

    @Test
    public void successfulRegistrationCarSales() throws Exception {
        mockExtractorService(asList(generateTestDto("AA3295", "Vyshnivskyi"), generateTestDto("AA3296", "Vyshnivskyi")));
        mockFeignClient("AA3295").thenReturn(1L);
        mockFeignClient("AA3296").thenReturn(2L);

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
        mockFeignClient("AA3295").thenReturn(1L);
        mockFeignClient("AA3296").thenThrow(CarIsAlreadyExistException.class);

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
        mockFeignClient("AA3295").thenReturn(1L);
        mockFeignClient("AA3296").thenThrow(CarSalesClientException.class);

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
    public void unSuccessfulRegistrationPartOfCarSalesCauseTroublesOnCarSalesServer() throws Exception {
        mockExtractorService(asList(generateTestDto("AA3295", "Vyshnivskyi"), generateTestDto("AA3296", "Vyshnivskyi")));
        mockFeignClient("AA3295").thenReturn(1L);
        mockFeignClient("AA3296").thenThrow(CarSalesServerException.class);

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
        when(extractor.extractAllCarSales("http://test.csv")).thenThrow(NotPossibleToDownloadFileException.class);
        Exception resolved = mockMvc.perform(MockMvcRequestBuilders.post("/cars")
                .contentType("text/plain;charset=UTF-8")
                .content(String.valueOf(new URL("http://test.csv"))))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertThat(resolved.getClass(), typeCompatibleWith(NotPossibleToDownloadFileException.class));
    }

    OngoingStubbing<Long> mockFeignClient(String number) {
        return when(client.addCarWithSaleDetails(2003.1, "Roma", "380960000000", "Vyshnivskyi", generateCar(number)));
    }

    void mockExtractorService(List<CarSaleDto> carSales) throws IOException {
        when(extractor.extractAllCarSales("http://test.csv")).thenReturn(carSales);
    }

    Car generateCar(String number) {
        return Car.builder()
                .number(number)
                .year(2001)
                .color("red")
                .brand("BMW")
                .build();
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
