package com.adoptapp.adoptionservice.controller;

import com.adoptapp.adoptionservice.dto.AdoptionCommand;
import com.adoptapp.adoptionservice.dto.AdoptionRequest;
import com.adoptapp.adoptionservice.dto.AdoptionResponse;
import com.adoptapp.adoptionservice.dto.AdoptionResult;
import com.adoptapp.adoptionservice.model.Adoption;
import com.adoptapp.adoptionservice.service.AdoptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.adoptapp.adoptionservice.dto.ErrorResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/adoptions")
public class AdoptionController {
    private final AdoptionService service;

    public AdoptionController(AdoptionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Adoption>> getAllAdoptions(
            @RequestParam(required = false) String status){

        List<AdoptionResult> results = status != null
                ? this.service.getAdoptions(status)
                : this.service.getAdoptions();
        List<AdoptionResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();
        return new ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<AdoptionResponse> getAdoptionById(@PathVariable Long id){
        return this.service.getById(id)
                .map(result -> toResponse(result))
                .map(ResponseEntity::ok)
        .ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Object> createAdoption(@Valid @RequestBody AdoptionRequest request){
        try{
            AdoptionCommand command = toCommand(request);
            AdoptionResult result = this.service.create(command);
            AdoptionResponse response = toResponse(result);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ErrorResponse(e.getMessage()),
                            HttpStatus.CONFLICT.value(),
                            LocalDateTime.now()
                    ));
        }catch(IllegalArgumentException e){
          return ResponseEntity.status(HttpStatus.CONFLICT)
                  .body(new ErrorResponse(e.getMessage(),
                          HttpStatus.CONFLICT.value(),
                          LocalDateTime.now()
                  ));
        }
    }

    @PutMapping
    public ResponseEntity<Object> updateAdoptionById(
            @PathVariable Long id,
            @Valid @RequestBody AdoptionRequest request){
        try {
            AdoptionCommand command = toCommand(request);
            Optional<AdoptionResult> result = this.service.updateById(id, command);
            if (result.isPresent()) {
                return ResponseEntity.ok(toResponse(result.get()));
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
           return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage(),
                   HttpStatus.CONFLICT.value(),
                   LocalDateTime.now()
           ));
        }
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteAdoptionById(@PathVariable Long id){}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException()
}
