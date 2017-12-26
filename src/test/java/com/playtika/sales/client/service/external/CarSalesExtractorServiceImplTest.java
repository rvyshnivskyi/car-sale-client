package com.playtika.sales.client.service.external;

import com.playtika.sales.client.domain.dto.CarSaleDto;
import com.playtika.sales.client.service.CarSalesExtractorService;
import com.playtika.sales.client.service.CarSalesExtractorServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public class CarSalesExtractorServiceImplTest {
    CarSalesExtractorService extractor = new CarSalesExtractorServiceImpl();

    @Test
    public void extractAllCarSalesFromFile() throws Exception {
        List<CarSaleDto> result = extractor.extractAllCarSales(new File("src/test/java/resources/cars.csv").toURL());
        assertThat(result, hasSize(2));
        assertThat(result, hasItems(is(generateTestDto("AA3295", "Vyshnivskyi")), is(generateTestDto("AA3296", "Vyshnivskyi"))));
    }

    @Test
    public void extractAllCarSalesFromFileWithoutUnMandatoryFields() throws Exception {
        List<CarSaleDto> result = extractor.extractAllCarSales(new File("src/test/java/resources/carsWithoutOwnerLastName.csv").toURL());
        assertThat(result, hasSize(2));
        assertThat(result, hasItems(is(generateTestDto("AA3295", null)), is(generateTestDto("AA3296", null))));
    }

    @Test(expected = RuntimeException.class)
    public void extractCarSalesFromFileWithoutMandatoryFieldsThrowsException() throws Exception {
        List<CarSaleDto> result = extractor.extractAllCarSales(new File("src/test/java/resources/carsWithoutBrand.csv").toURL());
    }


    @Test(expected = RuntimeException.class)
    public void extractCarSalesFromFileWithNullValueOfMandatoryFieldThrowsException() throws Exception {
        List<CarSaleDto> result = extractor.extractAllCarSales(new File("src/test/java/resources/carsWithNullValueOfBrand.csv").toURL());
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
