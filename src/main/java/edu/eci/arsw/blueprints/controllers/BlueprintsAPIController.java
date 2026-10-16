package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
@Tag(name = "Blueprints", description = "Consulta y creación de planos y sus puntos")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    @GetMapping
    @Operation(summary = "Listar todos los planos", description = "Retorna todos los planos registrados.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(200, "execute ok", services.getAllBlueprints()));
    }

    @GetMapping("/{author}")
    @Operation(summary = "Listar los planos de un autor", description = "Retorna todos los planos del autor indicado.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "El autor no tiene planos",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<Set<Blueprint>>> byAuthor(@PathVariable String author)
            throws BlueprintNotFoundException {
        return ResponseEntity.ok(new ApiResponse<>(200, "execute ok", services.getBlueprintsByAuthor(author)));
    }

    @GetMapping("/{author}/{bpname}")
    @Operation(summary = "Consultar un plano", description = "Retorna el plano identificado por autor y nombre.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "El plano no existe",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<Blueprint>> byAuthorAndName(@PathVariable String author,
                                                                  @PathVariable String bpname)
            throws BlueprintNotFoundException {
        return ResponseEntity.ok(new ApiResponse<>(200, "execute ok", services.getBlueprint(author, bpname)));
    }

    @PostMapping
    @Operation(summary = "Crear un plano", description = "Crea un nuevo plano con su lista de puntos.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Plano creado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
            description = "Datos inválidos o el plano ya existe",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<Blueprint>> add(@Valid @RequestBody NewBlueprintRequest req)
            throws BlueprintPersistenceException {
        Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
        services.addNewBlueprint(bp);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "blueprint created", bp));
    }

    @PutMapping("/{author}/{bpname}/points")
    @Operation(summary = "Agregar un punto a un plano", description = "Agrega un punto (x, y) al plano indicado.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Punto agregado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "El plano no existe",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<Blueprint>> addPoint(@PathVariable String author,
                                                           @PathVariable String bpname,
                                                           @Valid @RequestBody Point p)
            throws BlueprintNotFoundException {
        services.addPoint(author, bpname, p.x(), p.y());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ApiResponse<>(202, "point added", services.getBlueprint(author, bpname)));
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid List<Point> points
    ) { }
}