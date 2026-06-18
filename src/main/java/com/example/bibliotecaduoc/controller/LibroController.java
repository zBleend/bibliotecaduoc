package com.example.bibliotecaduoc.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.bibliotecaduoc.dto.CreateLibroRequest;
import com.example.bibliotecaduoc.dto.PokemonResponse;
import com.example.bibliotecaduoc.dto.UpdateLibroRequest;
import com.example.bibliotecaduoc.exception.ResourceNotFoundException;
import com.example.bibliotecaduoc.mapper.LibroMapper;
import com.example.bibliotecaduoc.model.Libro;
import com.example.bibliotecaduoc.service.LibroService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


/**
 * Controller REST modernizado para Java 21 LTS y Spring Boot 3.3+ 100% REST compliant
 */
@RestController
@RequestMapping("/api/v1/libros")
@Tag(name = "Libros", description = "Operaciones relacionadas con los libros en la biblioteca") // Para Swagger/OpenAPI grouping
public class LibroController {

        private final LibroService libroService;
        private final WebClient pokeApiWebClient;

        // Constructor injection (mejor práctica 2026)
        public LibroController(LibroService libroService, WebClient pokeApiWebClient) {
                this.libroService = libroService;
                this.pokeApiWebClient = pokeApiWebClient;
        }

        @GetMapping
        @Operation(summary = "Listar todos los libros", description = "Obtiene una lista de todos los libros disponibles en la biblioteca")
        @ApiResponse(responseCode = "200", description = "Libros obtenidos exitosamente")
        public ResponseEntity<List<Libro>> listarLibros() {
                List<Libro> libros = libroService.getLibros();
                return ResponseEntity.ok(libros);
        }

        @PostMapping
        @Operation(summary = "Agregar un nuevo libro", description = "Crea un nuevo libro en la biblioteca con los datos proporcionados")
        @ApiResponse(responseCode = "201", description = "Libro creado exitosamente")
        public ResponseEntity<Libro> agregarLibro(@Valid @RequestBody CreateLibroRequest request) {
                // @Valid ejecuta validaciones Jakarta automáticamente
                // Si falla → GlobalExceptionHandler.handleValidationErrors() retorna 400

                Libro nuevoLibro = libroService.saveLibro(LibroMapper.toModel(request));
                return ResponseEntity.status(HttpStatus.CREATED).body(nuevoLibro);
        }

        @GetMapping("{id}")
        @Operation(summary = "Buscar un libro por ID", description = "Obtiene los detalles de un libro específico por su ID")
        @ApiResponse(responseCode = "200", description = "Libro encontrado exitosamente")
        public ResponseEntity<Libro> buscarLibro(@PathVariable int id) {
                Libro libro = libroService.getLibroId(id);

                if (libro == null) {

                        throw new ResourceNotFoundException("Libro no encontrado para id: " + id);
                }

                return ResponseEntity.ok(libro);
        }

        @PutMapping("{id}")
        @Operation(summary = "Actualizar un libro por ID", description = "Actualiza los datos de un libro específico por su ID")
        @ApiResponse(responseCode = "200", description = "Libro actualizado exitosamente")
        public ResponseEntity<Libro> actualizarLibro(@PathVariable int id,
                        @Valid @RequestBody UpdateLibroRequest request) {
                // El ID viene del path, no del body → evita ambigüedad
                Libro libroActualizado = libroService.updateLibro(LibroMapper.toModel(id, request));
                return ResponseEntity.ok(libroActualizado);
        }

        @DeleteMapping("{id}")
        @Operation(summary = "Eliminar un libro por ID", description = "Elimina un libro específico de la biblioteca por su ID")
        @ApiResponse(responseCode = "204", description = "Libro eliminado exitosamente")
        public ResponseEntity<Void> eliminarLibro(@PathVariable int id) {
                libroService.deleteLibro(id);
                return ResponseEntity.noContent().build(); // 204 No Content (estándar REST)
        }

        @GetMapping("/total")
        @Operation(summary = "Obtener el total de libros", description = "Retorna el número total de libros disponibles en la biblioteca")
        @ApiResponse(responseCode = "200", description = "Total de libros obtenido exitosamente")
        public ResponseEntity<Integer> totalLibros() {
                int total = libroService.totalLibrosV2();
                return ResponseEntity.ok(total);
        }

        @GetMapping("/editorial/{editorial}")
        @Operation(summary = "Buscar libros por editorial", description = "Obtiene una lista de libros filtrados por su editorial")
        public List<Libro> getporEditorial(@PathVariable String editorial) {
                return libroService.obtenerPorEditorial(editorial);
        }

        @GetMapping("/editorial")
        @Operation(summary = "Buscar libros por editorial (query param)", description = "Obtiene una lista de libros filtrados por su editorial usando query parameter")
        @ApiResponse(responseCode = "200", description = "Libros obtenidos exitosamente")
        public List<Libro> getporEditorial2(@RequestParam String editorial) {
                return libroService.obtenerPorEditorial(editorial);
        }

        /**
         * Endpoint demostrativo de WebClient consumiendo PokeAPI GET
         * /api/v1/libros/pokeapi?nombre=pikachu
         */
        @GetMapping("/pokeapi")
        @Operation(summary = "Consultar información de un Pokémon", description = "Ejemplo de consumo de API externa usando WebClient para obtener información de un Pokémon por su nombre")
        @ApiResponse(responseCode = "200", description = "Información del Pokémon obtenida exitosamente")
        public ResponseEntity<PokemonResponse> consultarPokemon(
                        @RequestParam(name = "nombre") String nombre) {

                PokemonResponse pokemon = pokeApiWebClient.get()
                                .uri("/pokemon-species/{nombre}", nombre) // Endpoint más simple
                                .retrieve().bodyToMono(PokemonResponse.class).block();

                return ResponseEntity.ok(pokemon);
        }

}
