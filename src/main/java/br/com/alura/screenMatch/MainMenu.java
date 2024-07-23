package br.com.alura.screenMatch;

import br.com.alura.screenMatch.models.*;
import br.com.alura.screenMatch.repository.SerieRepository;
import br.com.alura.screenMatch.services.ConsumoAPI;
import br.com.alura.screenMatch.services.ConverteDados;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class MainMenu {
    private Scanner scan = new Scanner(System.in);
    private final String API_KEY = "&apikey=13f8f5c1";
    private final String ENDERECO = "http://www.omdbapi.com/?t=";
    private ConsumoAPI consumo = new ConsumoAPI();
    private ConverteDados converte = new ConverteDados();
    private SerieRepository repository;
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serieBusca;

    public MainMenu(SerieRepository repositorio) {
        this.repository = repositorio;
    }

    public MainMenu() {

    }

    public void ExibeMenu() {
        var opcao = -1;
        while(opcao != 0) {
            var menu = """
                    \n
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por título
                    5 - Buscar séries por ator
                    6 - Top 5 Séries
                    7 - Busca por categoria
                    8 - Busca por maximo de temporadas e minimo de avaliação
                    9 - Busca por trecho
                    10 - Top 5 episódios série
                    11 - Procurar episódios a partir de uma data
                    
                    0 - Sair
                    """;

            System.out.println(menu);
            opcao = scan.nextInt();
            scan.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    Top5Series();
                    break;
                case 7:
                    ProcurarPorGenero();
                    break;
                case 8:
                    FiltrarSeriePorTemporadaeAvaliacao();
                    break;
                case 9:
                    BuscarEpPorTrecho();
                    break;
                case 10:
                    Top5Episodios();
                    break;
                case 11:
                    ProcurarEpAPartirDoAno();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }




    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        System.out.println(serie);
        repository.save(serie);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = scan.nextLine();
        var serieEncoded = URLEncoder.encode(nomeSerie, StandardCharsets.UTF_8);
        System.out.println(ENDERECO + serieEncoded + API_KEY);
        var json = consumo.obterJson(ENDERECO + serieEncoded + API_KEY);
        DadosSerie dados = converte.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = scan.nextLine();
        serieBusca = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieBusca.isPresent()) {
            var serieEncontrada = serieBusca.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterJson(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = converte.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repository.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void listarSeriesBuscadas(){
        series = repository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = scan.nextLine();
        serieBusca = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()) {
            System.out.println("serie encontrada!\n" + serieBusca.get());
        }
        else {
            System.out.println("serie nao encontrada!");
        }
    }

    private void buscarSeriesPorAtor() {
        System.out.println("Qual o nome para a busca?");
        var nomeAtor = scan.nextLine();
        List<Serie> seriesEncontradas = repository.findByAtoresContainingIgnoreCase(nomeAtor);
        seriesEncontradas.forEach(System.out::println);
    }

    private void Top5Series() {
        List<Serie> series = repository.findTop5ByOrderByAvaliacaoDesc();
        series.forEach(System.out::println);
    }

    private void ProcurarPorGenero() {
        System.out.println("Qual a categoria buscada?");
        var categoria = scan.nextLine();
        Categoria cat = Categoria.fromPortuguese(categoria);
        List<Serie> series = repository.findByGenero(cat);
        series.forEach(System.out::println);
    }

    private void FiltrarSeriePorTemporadaeAvaliacao() {
        System.out.println("Qual o número maximo de temporadas?");
        var maxTemp = scan.nextInt();
        System.out.println("Qual o número minimo para avaliação?");
        var minAvaliacao = scan.nextDouble();
        List<Serie> series = repository.FiltrarPorTemporadaEAvaliacao(maxTemp, minAvaliacao);
        series.forEach(System.out::println);
    }

    private void BuscarEpPorTrecho() {
        System.out.println("Qual o trecho do episódio buscado?");
        var epBuscado = scan.nextLine();
        List<Episodio> episodios = repository.BuscarEpPorTrecho(epBuscado);
        episodios.forEach(e -> System.out.printf("Série: %s, Episódio: %s, avaliacão: %.1f", e.getSerie().getTitulo(),
                e.getTitulo(), e.getAvaliacao()));
    }

    private void Top5Episodios() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            List<Episodio> episodiosTop5 = repository.Top5EpPorSerie(serie);
            episodiosTop5.forEach(System.out::println);
        }
    }

    private void ProcurarEpAPartirDoAno() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            System.out.println("A partir de que ano são os episódios que voce quer?");
            var anoEpisodio = scan.nextInt();
            scan.nextLine();
            List<Episodio> episodiosPorAno = repository.EpisodiosAPartirDoAno(serie, anoEpisodio);
            episodiosPorAno.forEach(System.out::println);
        }
    }
}
