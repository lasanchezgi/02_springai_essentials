package com.eazybytes.springai.model;

import java.util.List;

/**
 * Modelo de salida estructurada: un país y sus ciudades.
 * Un {@code record} es ideal: inmutable y con los campos que Spring AI usa para generar el esquema JSON.
 *
 * @param country nombre del país
 * @param cities  ciudades del país
 */
public record CountryCities(String country, List<String> cities) {
}