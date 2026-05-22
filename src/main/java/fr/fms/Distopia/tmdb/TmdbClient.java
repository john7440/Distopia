package fr.fms.Distopia.tmdb;


import fr.fms.Distopia.tmdb.dto.TmdbMovieDto;
import fr.fms.Distopia.tmdb.dto.TmdbSearchResponse;
import fr.fms.Distopia.tmdb.dto.TmdbVideosResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;


@Service
public class TmdbClient {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate =  new RestTemplate();

    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    public static final String IMG_BASE= "https://image.tmdb.org/t/p/w500";

    //----------Recherche par titre-------------------
    public List<TmdbMovieDto> search(String query){
        String encoded =  URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = BASE_URL + "/search/movie?api_key=" + apiKey + "&query=" + encoded + "&language=fr-FR&page=1";
        TmdbSearchResponse response = restTemplate.getForObject(url, TmdbSearchResponse.class);
        return response !=null ? response.getResults() : List.of();
    }

    //--------------Détail d'un film------------------------------
    public TmdbMovieDto getDetail(Long tmdbId){
        String url =  BASE_URL + "/movie/" + tmdbId + "?api_key=" + apiKey + "&language=fr-FR";
        return restTemplate.getForObject(url, TmdbMovieDto.class);
    }

    //--------------Trailer film------------------------------
    /**
     * Retrieves the YouTube trailer URL for a TMDB movie
     * <p>
     * The method calls the TMDB videos endpoint and searches for
     * the first video where the site is YouTube and the type is Trailer
     *
     * @param tmdbId the TMDB movie identifier
     * @return the embeddable YouTube trailer URL, or null if no trailer is found
     */
    public String getTrailerUrl(Long tmdbId){
        String url = BASE_URL + "/movie/" + tmdbId + "/videos?api_key=" + apiKey;
        TmdbVideosResponse response = restTemplate.getForObject(url, TmdbVideosResponse.class);
        if (response == null || response.getResults() == null){
            return null;
        }
        return response.getResults().stream()
                .filter(v -> "YouTube".equals(v.getSite()) && "Trailer".equals(v.getType()))
                .findFirst()
                .map(v -> "https://www.youtube.com/embed/" + v.getKey())
                .orElse(null);
    }

    //-------------Films a l'affiche en France-----------------------------------
    /**
     * Retrieves movies currently playing in French cinemas from TMDB
     *
     * @return a list of movies currently playing in France, or an empty list if no response is returned
     */
    public List<TmdbMovieDto> getNowPlaying(){
        String url = BASE_URL + "/movie/now_playing?api_key=" + apiKey +  "&language=fr-FR&region=FR&page=1";
        TmdbSearchResponse response = restTemplate.getForObject(url, TmdbSearchResponse.class);
        return response !=null ? response.getResults() : List.of();
    }

    // --------- Films sortis cette semaine ----------------------------
    /**
     * Retrieves movies released during the last seven days
     * <p>
     * This method filters the movies returned by {@link #getNowPlaying()}
     * using their release date
     *
     * @return a list of movies released this week, limited to 10 results
     */
    public List<TmdbMovieDto> getThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        return getNowPlaying().stream()
                .filter(m -> {
                    if (m.getReleaseDate() == null || m.getReleaseDate().isBlank()) return false;
                    try {
                        LocalDate d = LocalDate.parse(m.getReleaseDate());
                        return !d.isBefore(weekAgo) && !d.isAfter(today);
                    } catch (Exception e) { return false; }
                })
                .limit(10)
                .toList();
    }

    //------------- les prochaines sorties---------------------------------
    /**
     * Retrieves upcoming movies in France from TMDB
     *
     * @return a list of upcoming movies, or an empty list if no response is returned
     */
    public List<TmdbMovieDto> getUpcoming() {
        String url = BASE_URL + "movie/upcoming?api_key=" + apiKey
                + "&language=fr-FR&region=FR&page=1";
        TmdbSearchResponse response = restTemplate.getForObject(url, TmdbSearchResponse.class);
        return response != null ? response.getResults() : List.of();
    }


}
