package br.com.alura.screenMatch.repository;

import br.com.alura.screenMatch.models.Categoria;
import br.com.alura.screenMatch.models.Episodio;
import br.com.alura.screenMatch.models.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {
    Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);

    List<Serie> findByAtoresContainingIgnoreCase(String nomeAtor);

    List<Serie> findTop5ByOrderByAvaliacaoDesc();

    List<Serie> findByGenero(Categoria categoria);

    List<Serie> findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(int totalTemporadas, double avaliacao);

    List<Serie> findTop5ByOrderByEpisodiosDataLancamentoDesc();

    @Query("select s from Serie s where s.totalTemporadas <= :totalTemporadas and s.avaliacao >= :avaliacao")
    List<Serie> FiltrarPorTemporadaEAvaliacao(Integer totalTemporadas, Double avaliacao);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE e.titulo ILIKE %:epBuscado%")
    List<Episodio> BuscarEpPorTrecho(String epBuscado);

    @Query("SELECT e FROM Serie s JOIN s.episodios e ORDER BY e.avaliacao DESC LIMIT 5")
    List<Episodio> Top5EpPorSerie(Serie serie);

    @Query("select e from Serie s join s.episodios e where s = :serie and year(e.dataLancamento) >= :ano")
    List<Episodio> EpisodiosAPartirDoAno(Serie serie, Integer ano);

    @Query("SELECT s FROM Serie s " +
            "JOIN s.episodios e " +
            "GROUP BY s " +
            "ORDER BY MAX(e.dataLancamento) DESC LIMIT 5")
    List<Serie> lancamentosMaisRecentes();

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s.id = :id AND e.temporada = :numero")
    List<Episodio> obterEpisodiosPorTemporada(Long id, Long numero);
}
