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

    @Test
    @DisplayName("buildMapsUrl() - returns encoded address url when coordinates do not exist")
    void buildMapsUrl_ShouldReturnEncodedAddressUrlWhenCoordinatesDoNotExist() {
        Town town = new Town();
        town.setName("Dax");

        cinema.setAddress("10 rue de la paix");
        cinema.setTown(town);

        String result = cinema.buildMapsUrl();

        assertThat(result).contains("https://www.google.com/maps/search/?api=1&query=");
        assertThat(result).contains("10+rue+de+la+paix+Dax");
    }
}
