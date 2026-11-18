package cl.duoc.ms_vidasalud_audit.controller;

import cl.duoc.ms_vidasalud_audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AuditService auditService;

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/v1/audit")).andExpect(status().isUnauthorized());
    }

    @Test
    void elOperadorYElClienteNoVenLaAuditoria() throws Exception {
        mockMvc.perform(get("/api/v1/audit").with(rol("Operador"))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/audit").with(rol("Cliente"))).andExpect(status().isForbidden());
    }

    @Test
    void elAuditorConsulta() throws Exception {
        when(auditService.buscar(any(), any(), any(), any(), any())).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/audit").param("usuario", "olga").param("from", "2026-11-01").with(rol("Auditor")))
                .andExpect(status().isOk());
    }

    @Test
    void esSoloLectura() throws Exception {
        mockMvc.perform(post("/api/v1/audit").with(rol("Admin"))).andExpect(status().isMethodNotAllowed());
    }

    private static RequestPostProcessor rol(String rol) {
        return jwt().authorities(new SimpleGrantedAuthority("APPROLE_" + rol));
    }
}
