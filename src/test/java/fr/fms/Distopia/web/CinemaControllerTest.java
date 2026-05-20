package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.service.CinemaCsvImporter;
import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.TownService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CinemaController.class)
class CinemaControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CinemaService cinemaService;
    @MockitoBean
    private TownService townService;
    @MockitoBean
    private CinemaCsvImporter cinemaCsvImporter;


    //------------------------tests for cinemas--------------------
    @Test
    @DisplayName("/cinemas - Should display cinemas page")
    void cinemas_shouldDisplayCinemasPage() throws Exception {

        Cinema cinema = new Cinema();
        cinema.setName("Mega CGR");

        Page<Cinema> cinemaPage = new PageImpl<>(List.of(cinema));

        given(cinemaService.searchPublic(any(), any(), any(), anyInt()))
                .willReturn(cinemaPage);

        given(cinemaService.getAllDepartments())
                .willReturn(List.of("64", "75"));

        given(townService.getAll())
                .willReturn(List.of());

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

        given(cinemaService.searchPublic(eq("pathe"), eq(1L), eq("64"), eq(0)))
                .willReturn(cinemaPage);

        given(cinemaService.getAllDepartments())
                .willReturn(List.of());

        given(townService.getAll())
                .willReturn(List.of());

        mockMvc.perform(get("/cinemas")
                        .with(user("user").roles("USER"))
                        .param("keyword", "pathe")
                        .param("townId", "1")
                        .param("department", "64"))
                .andExpect(status().isOk())
                .andExpect(view().name("cinemas"))
                .andExpect(model().attribute("selectedTownId", 1L))
                .andExpect(model().attribute("selectedDepartment", "64"))
                .andExpect(model().attribute("keyword", "pathe"));
    }

    //------------------------test for import-cinemas------------------
    @Test
    @DisplayName("/admin/import-cinemas - Should import cinemas successfully")
    void adminImportCinemas_shouldImportCinemasSuccessfully() throws Exception {

        CinemaCsvImporter.ImportResult result =
                new CinemaCsvImporter.ImportResult(10, 2);

        given(cinemaCsvImporter.importFromCsv())
                .willReturn(result);

        mockMvc.perform(get("/admin/import-cinemas")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cinemas"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("/admin/import-cinemas - Should handle import exception")
    void adminImportCinemas_ShouldHandleImportException() throws Exception {

        given(cinemaCsvImporter.importFromCsv())
                .willThrow(new RuntimeException("CSV error"));

        mockMvc.perform(get("/admin/import-cinemas")
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

        given(cinemaService.searchAdmin(any(), any(), any(), anyInt()))
                .willReturn(cinemaPage);

        given(townService.getAll())
                .willReturn(List.of());

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
        cinema.setName("Pathé");

        Page<Cinema> cinemaPage = new PageImpl<>(List.of());

        given(cinemaService.searchAdmin(any(), any(), any(), anyInt()))
                .willReturn(cinemaPage);

        given(cinemaService.findById(1L))
                .willReturn(Optional.of(cinema));

        given(townService.getAll())
                .willReturn(List.of());

        mockMvc.perform(get("/admin/cinemas")
                        .with(user("admin").roles("ADMIN"))
                        .param("editId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-cinemas"))
                .andExpect(model().attributeExists("cinemaToEdit"));
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

    //----------test for delete()----------------------------------
    @Test
    @DisplayName("/admin/deleteCinema - Should delete cinema")
    void adminDeleteCinema_shouldDeleteCinema() throws Exception {

        mockMvc.perform(get("/admin/deleteCinema")
                        .with(user("admin").roles("ADMIN"))
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cinemas"));

        verify(cinemaService).delete(1L);
    }
}