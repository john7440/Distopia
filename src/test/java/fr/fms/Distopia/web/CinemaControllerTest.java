package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.service.CinemaCsvImporter;
import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.TownService;
import fr.fms.Distopia.config.SecurityConfig;
import fr.fms.Distopia.service.DistopiaUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CinemaController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {"spring.autoconfigure.exclude= org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"})
class CinemaControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CinemaService cinemaService;
    @MockitoBean
    private TownService townService;
    @MockitoBean
    private CinemaCsvImporter cinemaCsvImporter;
    @MockitoBean
    private DistopiaUserDetailsService userDetailsService;


    //------------------------tests for cinemas--------------------
    @Test
    @DisplayName("/cinemas - Should display cinemas page")
    void cinemas_shouldDisplayCinemasPage() throws Exception {

        Cinema cinema = new Cinema();
        cinema.setName("Mega CGR");

        Page<Cinema> cinemaPage = new PageImpl<>(List.of(cinema));

        when(cinemaService.searchPublic(any(), any(), any(), anyInt())).thenReturn(cinemaPage);
        when(cinemaService.getAllDepartments()).thenReturn(List.of("64", "75"));
        when(townService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/cinemas")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("cinemas"))
                .andExpect(model().attributeExists("cinemas"))
                .andExpect(model().attributeExists("cinemaPage"))
                .andExpect(model().attributeExists("towns"))
                .andExpect(model().attributeExists("departments"));
    }

    @Test
    @DisplayName("/cinemas - Should filter cinemas")
    void cinemas_shouldFilterCinemas() throws Exception {

        Page<Cinema> cinemaPage = new PageImpl<>(List.of());

        when(cinemaService.searchPublic("Ciné", 1L, "64", 0))
                .thenReturn(cinemaPage);
        when(cinemaService.getAllDepartments()).thenReturn(List.of());
        when(townService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/cinemas")
                        .with(user("user").roles("USER"))
                        .param("keyword", "Ciné")
                        .param("townId", "1")
                        .param("department", "64"))
                .andExpect(status().isOk())
                .andExpect(view().name("cinemas"))
                .andExpect(model().attribute("selectedTownId", 1L))
                .andExpect(model().attribute("selectedDepartment", "64"))
                .andExpect(model().attribute("keyword", "Ciné"));
    }

    //------------------------test for import-cinemas------------------
    @Test
    @DisplayName("/admin/import-cinemas - Should import cinemas successfully")
    void adminImportCinemas_shouldImportCinemasSuccessfully() throws Exception {

        CinemaCsvImporter.ImportResult result =
                new CinemaCsvImporter.ImportResult(10, 2);

        when(cinemaCsvImporter.importFromCsv()).thenReturn(result);

        mockMvc.perform(post("/admin/import-cinemas")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cinemas"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("/admin/import-cinemas - Should handle import exception")
    void adminImportCinemas_ShouldHandleImportException() throws Exception {

        when(cinemaCsvImporter.importFromCsv()).thenThrow(new RuntimeException("CSV error"));

        mockMvc.perform(post("/admin/import-cinemas")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cinemas"))
                .andExpect(flash().attributeExists("error"));
    }

    //------------------tests for admin-cinema-------------------------
    @Test
    @DisplayName("/admin/cinemas - Should display admin cinemas page")
    void adminCinemas_shouldDisplayAdminCinemasPage() throws Exception {
        Page<Cinema> cinemaPage = new PageImpl<>(List.of());
        when(cinemaService.searchAdmin(any(), any(), any(), anyInt(), eq(false))).thenReturn(cinemaPage);
        when(townService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/cinemas")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-cinemas"))
                .andExpect(model().attributeExists("cinemaPage"))
                .andExpect(model().attributeExists("cinemas"))
                .andExpect(model().attributeExists("cinemaToEdit"));
    }

    @Test
    @DisplayName("/admin/cinemas - Should load cinema to edit")
    void adminCinemas_ShouldLoadCinemaToEdit() throws Exception {

        Cinema cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Ciné");

        Page<Cinema> cinemaPage = new PageImpl<>(List.of());
        when(cinemaService.searchAdmin(any(), any(), any(), anyInt(), eq(false))).thenReturn(cinemaPage);
        when(cinemaService.findById(1L)).thenReturn(Optional.of(cinema));
        when(townService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/cinemas").with(user("admin").roles("ADMIN")).param("editId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-cinemas"))
                .andExpect(model().attributeExists("cinemaToEdit"));
    }


    @Test
    @DisplayName("/admin/cinemas - includes deleted cinemas when requested")
    void adminCinemas_ShouldIncludeDeletedWhenRequested() throws Exception {
        Cinema deletedCinema = new Cinema();
        deletedCinema.setId(7L);
        deletedCinema.setName("Cinéma supprimé");
        deletedCinema.setDeleted(true);

        Page<Cinema> page = new PageImpl<>(List.of(deletedCinema));

        when(cinemaService.searchAdmin("Cinéma", "name", "desc", 0, true)).thenReturn(page);

        mockMvc.perform(get("/admin/cinemas").with(user("admin").roles("ADMIN"))
                        .param("keyword", "Cinéma")
                        .param("sortField", "name")
                        .param("sortDir", "desc")
                        .param("includeDeleted", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-cinemas"))
                .andExpect(model().attribute("includeDeleted", true))
                .andExpect(model().attribute("cinemas", List.of(deletedCinema)));

        verify(cinemaService).searchAdmin("Cinéma", "name", "desc", 0, true);
    }

    @Test
    @DisplayName("/admin/cinemas - excludes deleted cinemas by default")
    void adminCinemas_ShouldExcludeDeletedByDefault() throws Exception {
        Page<Cinema> page = new PageImpl<>(List.of());

        when(cinemaService.searchAdmin(null, "name", "asc", 0, false)).thenReturn(page);

        mockMvc.perform(get("/admin/cinemas").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-cinemas"))
                .andExpect(model().attribute("includeDeleted", false));

        verify(cinemaService).searchAdmin(null, "name", "asc", 0, false);
    }



    //----------------------------test for admin-saveCinema-----------------------------
    @Test
    @DisplayName("/admin/saveCinema - Should save cinema")
    void adminSaveCinemas_shouldSaveCinema() throws Exception {

        mockMvc.perform(post("/admin/saveCinema")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .param("name", "UGC")
                        .param("address", "26 rue du test")
                        .param("townId", "1")
                        .param("website", "https://ugc.fr")
                        .param("latitude", "43.48")
                        .param("longitude", "-1.57")
                        .param("imageUrl", "image.jpg")
                        .param("department", "64"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cinemas"));

        verify(cinemaService).save(isNull(), eq("UGC"), eq("26 rue du test"),
                eq(1L), eq("https://ugc.fr"), eq(43.48), eq(-1.57),
                eq("image.jpg"), eq("64")
        );
    }

    @Test
    @DisplayName("/admin/saveCinema - Should redirect with error when form is invalid")
    void adminSaveCinema_ShouldRedirectWithError_WhenFormIsInvalid() throws Exception {

        mockMvc.perform(post("/admin/saveCinema")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .param("name", "")
                        .param("address", "26 rue du test")
                        .param("townId", "1")
                        .param("website", "https://ugc.fr")
                        .param("latitude", "43.48")
                        .param("longitude", "-1.57")
                        .param("imageUrl", "image.jpg")
                        .param("department", "64"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cinemas"))
                .andExpect(flash().attributeExists("error"));

        verify(cinemaService, never()).save(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    //----------tests for delete()----------------------------------
    @Test
    @DisplayName("/admin/deleteCinema - Should delete cinema")
    void adminDeleteCinema_shouldDeleteCinema() throws Exception {

        mockMvc.perform(post("/admin/deleteCinema")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cinemas"))
                .andExpect(flash().attribute("message", "Cinéma supprimé avec succès"));

        verify(cinemaService).delete(1L);
    }

    @Test
    @DisplayName("/admin/deleteCinema - Should redirect with error when cinema has seances")
    void adminDeleteCinema_ShouldRedirectWithError_WhenCinemaHasSeances() throws Exception {
        doThrow(new IllegalStateException("Impossible de supprimer ce cinéma : des séances y sont encore associées"))
                .when(cinemaService).delete(1L);

        mockMvc.perform(post("/admin/deleteCinema")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cinemas"))
                .andExpect(flash().attributeExists("error"));

        verify(cinemaService).delete(1L);
    }

    @Test
    @DisplayName("POST /admin/restoreCinema - restores deleted cinema" )
    void restoreCinema_ShouldRestoreDeletedCinema() throws Exception {
        when(cinemaService.restore(1L)).thenReturn(true);

        mockMvc.perform(post("/admin/restoreCinema").with(csrf()).with(user("admin").roles("ADMIN")).param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cinemas"))
                .andExpect(flash().attribute("message", "Cinéma restauré avec succès"));

        verify(cinemaService).restore(1L);
    }

    @Test
    @DisplayName("POST /admin/restoreCinema - handles active cinema")
    void restoreCinema_ShouldHandleAlreadyActiveCinema() throws Exception {
        when(cinemaService.restore(1L)).thenReturn(false);

        mockMvc.perform(post("/admin/restoreCinema").with(csrf()).with(user("admin").roles("ADMIN")).param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cinemas"))
                .andExpect(flash().attribute("message", "Cinéma déjà actif"));

        verify(cinemaService).restore(1L);
    }


    @Test
    @DisplayName("POST /admin/restoreCinema - handles unknown cinema")
    void restoreCinema_ShouldHandleUnknownCinema() throws Exception {
        when(cinemaService.restore(99L)).thenThrow(new NoSuchElementException());

        mockMvc.perform(post("/admin/restoreCinema").with(csrf()).with(user("admin").roles("ADMIN")).param("id", "99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cinemas"))
                .andExpect(flash().attribute("error", "Cinéma introuvable"));

        verify(cinemaService).restore(99L);
    }


    @Test
    @DisplayName("POST /admin/restoreCinema - forbids regular user")
    void restoreCinema_ShouldForbidRegularUser() throws Exception {
        mockMvc.perform(post("/admin/restoreCinema").with(csrf()).with(user("user").roles("USER"))
                        .param("id", "1")).andExpect(status().isForbidden());

        verifyNoInteractions(cinemaService);
    }


    @Test
    @DisplayName("POST /admin/restoreCinema - requires CSRF ")
    void restoreCinema_ShouldRequireCsrf() throws Exception {
        mockMvc.perform(post("/admin/restoreCinema").with(user("admin").roles("ADMIN"))
                        .param("id", "1")).andExpect(status().isForbidden());

        verifyNoInteractions(cinemaService);
    }

}