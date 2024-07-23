package br.com.alura.screenMatch.dto;

import br.com.alura.screenMatch.models.Categoria;

public record SerieDTO(
         Long id,
         String titulo,
         Integer totalTemporadas,
         Double avaliacao,
         Categoria genero,
         String atores,
         String poster,
         String sinopse) {
}
