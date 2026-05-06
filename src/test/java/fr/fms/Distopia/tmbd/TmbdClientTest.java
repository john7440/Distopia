package fr.fms.Distopia.tmbd;

import fr.fms.Distopia.tmdb.TmdbClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class TmbdClientTest {
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private TmdbClient tmdbClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tmdbClient, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(tmdbClient, "apiKey", "FAKE_KEY_TEST");
    }
}
