package com.example.userservice.controller;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@Validated
@Tag(name = "User API", description = "API для управления пользователями")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CircuitBreakerFactory circuitBreakerFactory;

    @Operation(summary = "Тестовый endpoint для Circuit Breaker", description = "Имитирует задержку для тестирования Circuit Breaker")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ"),
            @ApiResponse(responseCode = "500", description = "Имитированная ошибка")
    })
    @GetMapping("/test/circuit-breaker")
    public ResponseEntity<String> testCircuitBreaker(
            @RequestParam(required = false, defaultValue = "0") long delay,
            @RequestParam(required = false, defaultValue = "false") boolean error,
            @RequestParam(required = false, defaultValue = "false") boolean success) {

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("testCircuitBreaker");

        String result = circuitBreaker.run(() -> {
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during delay");
                }
            }

            if (error) {
                throw new RuntimeException("Имитированная ошибка для тестирования Circuit Breaker");
            }

            if (success) {
                return "Circuit Breaker Test - SUCCESS. Delay: " + delay + "ms";
            }

            return "Circuit Breaker Test - OK. Delay: " + delay + "ms";
        }, throwable -> {
            return "Fallback: Service is temporarily unavailable. Original error: " + throwable.getMessage();
        });

        return ResponseEntity.ok(result);
    }



    @Operation(summary = "Создать нового пользователя", description = "Создает нового пользователя в системе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные пользователя"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует")
    })
    @PostMapping
    public ResponseEntity<EntityModel<UserResponse>> createUser(
            @Parameter(description = "Данные пользователя")
            @Valid @RequestBody UserRequest userRequest) {

        UserResponse user = userService.createUser(userRequest);
        EntityModel<UserResponse> resource = EntityModel.of(user);

        resource.add(linkTo(methodOn(UserController.class).getUserById(user.getUserId())).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));

        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @Operation(summary = "Получить пользователя по ID", description = "Возвращает пользователя по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponse>> getUserById(
            @Parameter(description = "ID пользователя")
            @PathVariable Long id) {

        UserResponse user = userService.getUserById(id);
        EntityModel<UserResponse> resource = EntityModel.of(user);

        resource.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей с поддержкой HATEOAS")
    @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();

        List<EntityModel<UserResponse>> userResources = users.stream()
                .map(user -> {
                    EntityModel<UserResponse> resource = EntityModel.of(user);
                    resource.add(linkTo(methodOn(UserController.class).getUserById(user.getUserId())).withSelfRel());
                    return resource;
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<UserResponse>> resources = CollectionModel.of(userResources);

        resources.add(linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
        resources.add(linkTo(methodOn(UserController.class).createUser(new UserRequest())).withRel("create-user"));

        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Обновить пользователя", description = "Обновляет данные пользователя по указанному ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Email уже используется другим пользователем")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponse>> updateUser(
            @Parameter(description = "ID пользователя")
            @PathVariable Long id,
            @Parameter(description = "Обновленные данные пользователя")
            @Valid @RequestBody UserRequest userRequest) {

        UserResponse user = userService.updateUser(id, userRequest);
        EntityModel<UserResponse> resource = EntityModel.of(user);

        resource.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по указанному ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя")
            @PathVariable Long id) {

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test/circuit-breaker/status")
    public ResponseEntity<String> getCircuitBreakerStatus() {
        return ResponseEntity.ok("Circuit Breaker endpoints are available. Use /test/circuit-breaker with parameters: delay, error, success");
    }

    @Operation(summary = "Получить количество пользователей", description = "Возвращает общее количество пользователей в системе")
    @ApiResponse(responseCode = "200", description = "Количество пользователей успешно получено")
    @GetMapping("/count")
    public ResponseEntity<Long> getUserCount() {
        long count = userService.getUserCount();
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Проверить существование email", description = "Проверяет, существует ли пользователь с указанным email")
    @ApiResponse(responseCode = "200", description = "Результат проверки email")
    @GetMapping("/check-email/{email}")
    public ResponseEntity<EntityModel<Boolean>> checkEmailExists(
            @Parameter(description = "Email для проверки")
            @PathVariable String email) {

        boolean exists = userService.existsByEmail(email);

        EntityModel<Boolean> resource = EntityModel.of(exists);
        resource.add(linkTo(methodOn(UserController.class).checkEmailExists(email)).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));

        return ResponseEntity.ok(resource);
    }
}