package LifeValuable.Library.controller;

import LifeValuable.Library.dto.auth.LoginRequestDTO;
import LifeValuable.Library.dto.auth.LoginResponseDTO;
import LifeValuable.Library.dto.auth.LogoutResponseDTO;
import LifeValuable.Library.dto.reader.ReaderDetailDTO;
import LifeValuable.Library.service.ReaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Аутентификация", description = "Вход и выход из системы")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final ReaderService readerService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, ReaderService readerService) {
        this.authenticationManager = authenticationManager;
        this.readerService = readerService;
    }

    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя по email и паролю")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход"),
            @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для входа", required = true)
            @Valid @RequestBody LoginRequestDTO loginRequest,
            HttpServletRequest request) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        ReaderDetailDTO readerDetailDTO = readerService.findByEmail(loginRequest.email());

        return ResponseEntity.ok(new LoginResponseDTO(
                "Успешный вход в систему",
                readerDetailDTO.role(),
                readerDetailDTO.firstName(),
                readerDetailDTO.lastName(),
                readerDetailDTO.email()
        ));
    }


    @Operation(summary = "Выход из системы", description = "Завершение пользовательской сессии")
    @ApiResponse(responseCode = "200", description = "Успешный выход")
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDTO> logout(HttpServletRequest request) {

        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        LogoutResponseDTO response = new LogoutResponseDTO("Успешный выход из системы");
        return ResponseEntity.ok(response);
    }
}
