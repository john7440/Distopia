package fr.fms.Distopia.web;


import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.Town;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.TownService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

@ExtendWith(MockitoExtension.class)
class TownControllerTest {
    @Mock
    private TownService townService;

    @Mock
    private TownRepository townRepository;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @InjectMocks
    private TownController townController;

    private User adminUser;
    private User regularUser;
    private Town town;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setRole(Role.USER);

        town = new Town();
        town.setId(1L);
        town.setName("Dax");
    }
}
