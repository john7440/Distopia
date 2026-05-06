package fr.fms.Distopia.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class TmdbVideoDto {

    private String key;
    private String site;
    private String type;
}
