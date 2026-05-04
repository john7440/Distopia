package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.Town;
import fr.fms.Distopia.service.TownService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexControllerTest {
    @Mock
    private TownService townService;
    @Mock
    private Model model;
    @InjectMocks
    private IndexController indexController;

    List<Town> towns;

    @BeforeEach
    void setUp() {
        Town paris = new Town();
        paris.setId(1L);
        paris.setName("Paris");

        Town lyon =  new Town();
        lyon.setId(2L);
        lyon.setName("Lyon");

        towns = List.of(paris, lyon);
    }

    //------------------tests for index()---------------------------------
    @Test
    @DisplayName("index() - returns view name 'index")
    void index_ShouldReturnIndexView() {
        when(townService.getAll()).thenReturn(towns);

        String view = indexController.index(model);

        assertThat(view).isEqualTo("index");
    }

    @Test
    @DisplayName("index() - adds all towns to the model")
    void index_ShouldAddAllTownsToTheModel() {
        when(townService.getAll()).thenReturn(towns);

        indexController.index(model);

        verify(model).addAttribute("towns", towns);
    }

    @Test
    @DisplayName("index() - calls townService.getAll() exactly once")
    void index_ShouldCallTownServiceTownService() {
        when(townService.getAll()).thenReturn(towns);

        indexController.index(model);

        verify(townService, times(1)).getAll();
    }

}
