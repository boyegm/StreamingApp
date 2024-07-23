package br.com.alura.screenMatch.services;

import br.com.alura.screenMatch.dto.EpisodioDTO;
import br.com.alura.screenMatch.dto.SerieDTO;
import br.com.alura.screenMatch.models.Categoria;
import br.com.alura.screenMatch.models.Serie;
import br.com.alura.screenMatch.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SerieService {
    @Autowired
    SerieRepository repository;

    public List<SerieDTO> obterSeries()
    {
        return Converte(repository.findAll());
    }

    public List<SerieDTO> ObterTop5() {
        return Converte(repository.findTop5ByOrderByAvaliacaoDesc());
    }

    private List<SerieDTO> Converte(List<Serie> series) {
        return series.stream()
                .map(s -> new SerieDTO(s.getId(),
                        s.getTitulo(),
                        s.getTotalTemporadas(),
                        s.getAvaliacao(),
                        s.getGenero(),
                        s.getAtores(),
                        s.getPoster(),
                        s.getSinopse()))
                .collect(Collectors.toList());
    }


    public SerieDTO obterPorId(Long Id) {
        Optional<Serie> serie = repository.findById(Id);

        if (serie.isPresent()) {
            Serie s = serie.get();
            return new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse());
        }
        return null;
    }

    public List<SerieDTO> obterLancamentos() {
        return Converte(repository.lancamentosMaisRecentes());
    }

    public List<EpisodioDTO> obterTodasTemporadas(Long id) {
        Optional<Serie> serie = repository.findById(id);

        if (serie.isPresent()) {
            Serie s = serie.get();
            return s.getEpisodios().stream()
                    .map(e -> new EpisodioDTO(e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public List<EpisodioDTO> obterTemporadasPorNumero(Long id, Long numero) {
        return repository.obterEpisodiosPorTemporada(id, numero)
                .stream()
                .map(e -> new EpisodioDTO(e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo()))
                .collect(Collectors.toList());
    }


    public List<SerieDTO> obterSeriesPorCategoria(String nomeGenero) {
        Categoria categoria = Categoria.fromPortuguese(nomeGenero);
        return Converte(repository.findByGenero(categoria));
    }
}
