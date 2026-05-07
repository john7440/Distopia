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
    public List<TmdbMovieDto> getNowPlaying(){
        String url = BASE_URL + "/movie/now_playing?api_key=" + apiKey +  "&language=fr-FR&region=FR&page=1";
        TmdbSearchResponse response = restTemplate.getForObject(url, TmdbSearchResponse.class);
        return response !=null ? response.getResults() : List.of();
    }


}
