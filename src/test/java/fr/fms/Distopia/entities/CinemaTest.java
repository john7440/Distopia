package fr.fms.Distopia.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CinemaTest {

    @InjectMocks
    private Cinema cinema;

    //-------------------test for buildMapsUrl() ----------------------------
    @Test
    @DisplayName("buildMapsUrl() - returns coordinates urls when latitude and longitude exist")
    void buildMapsUrl_ShouldReturnCoordinatesUrlWhenLatitudeAndLongitudeExist() {
        cinema.setLatitude(57.7102);
        cinema.setLongitude(-1.2345);

        String result = cinema.buildMapsUrl();

        assertThat(result).isEqualTo("https://www.google.com/maps?q=57.7102,-1.2345");
    }

}
