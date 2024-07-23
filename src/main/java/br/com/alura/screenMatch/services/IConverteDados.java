package br.com.alura.screenMatch.services;

public interface IConverteDados {
    <T> T obterDados(String json, Class<T> classe);
}
