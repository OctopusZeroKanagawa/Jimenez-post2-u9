package com.universidad.seguridad.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    /**
     * Spring Security redirige aquí cuando AccessDeniedException es lanzada,
     * tanto por reglas de URL como por @PreAuthorize en la capa de servicio.
     */
    @GetMapping("/error/403")
    public String accesoDenegado() {
        return "error/403";
    }
}
