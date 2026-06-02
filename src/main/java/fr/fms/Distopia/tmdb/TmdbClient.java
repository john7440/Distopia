package fr.fms.Distopia.tmdb;


import fr.fms.Distopia.tmdb.dto.TmdbMovieDto;
import fr.fms.Distopia.tmdb.dto.TmdbSearchResponse;
import fr.fms.Distopia.tmdb.dto.TmdbVideosResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Client responsible for communicating with the TMDB API
 * <p>
 * This service provides methods for:
 * <ul>
 *     <li>searching movies</li>
 *     <li>retrieving movie details</li>
 *     <li>retrieving trailers</li>
 *     <li>retrieving now-playing and upcoming movies</li>
 * </ul>
 */
@Service
public class TmdbClient {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate =  new RestTemplate();

    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    public static final String IMG_BASE= "https://image.tmdb.org/t/p/w500";

    //----------Recherche par titre-------------------
    /**
     * Searches movies on TMDB using a title keyword<p>
     * Results are retrieved in French language
     * @param query the movie title keyword
     * @return the list of matching TMDB movies
     */
    public List<TmdbMovieDto> search(String query){
        String encoded =  URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = BASE_URL + "search/movie?api_key=" + apiKey + "&query=" + encoded + "&language=fr-FR&page=1";
        TmdbSearchResponse response = restTemplate.getForObject(url, TmdbSearchResponse.class);
        return response !=null ? response.getResults() : List.of();
    }

    //--------------Détail d'un film------------------------------
    /**
     * Retrieves detailed information for a TMDB movie<p>
     * Returned information may include:
     * <ul>
     *     <li>title</li>
     *     <li>overview</li>
     *     <li>runtime</li>
     *     <li>genres</li>
     *     <li>poster path</li>
     *     <li>release date</li>
     * </ul>
     * @param tmdbId the TMDB movie identifier
     * @return the detailed TMDB movie information
     */
    public TmdbMovieDto getDetail(Long tmdbId){
        String url =  BASE_URL + "movie/" + tmdbId + "?api_key=" + apiKey + "&language=fr-FR";
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
        String url = BASE_URL + "movie/" + tmdbId + "/videos?api_key=" + apiKey;
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
        String url = BASE_URL + "movie/now_playing?api_key=" + apiKey +  "&language=fr-FR&region=FR&page=1";
        TmdbSearchResponse response = restTemplate.getForObject(url, TmdbSearchResponse.class);
        return response !=null ? response.getResults() : List.of();
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

    /**
     * Enriches TMDB movie summaries with detailed movie information<p>
     * This method fetches each movie detail to complete the
     * displayed data
     *
     * @param movies the TMDB movie summaries to enrich
     * @return the enriched movie list
     */
    public List<TmdbMovieDto> enrichWithDetails(List<TmdbMovieDto> movies) {
        if (movies == null || movies.isEmpty()) {
            return List.of();
        }

        return movies.stream()
                .map(movie -> {
                    try {
                        TmdbMovieDto detail = getDetail(movie.getId());
                        movie.setRuntime(detail.getRuntime());
                        movie.setGenres(detail.getGenres());
                        if (movie.getOverview() == null || movie.getOverview().isBlank()) {
                            movie.setOverview(detail.getOverview());
                        }
                        if (movie.getPosterPath() == null || movie.getPosterPath().isBlank()) {
                            movie.setPosterPath(detail.getPosterPath());
                        }

                        return movie;

                    } catch (Exception e) {
                        return movie;
                    }}).toList();
    }
}
