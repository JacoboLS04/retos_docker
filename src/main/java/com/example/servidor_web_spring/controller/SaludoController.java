
package main.java.com.example.servidor_web_spring.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SaludoController {

    @GetMapping("/saludo")
    public ResponseEntity<String> saludar(@RequestParam(required = false) String nombre) {
        if (nombre != null && !nombre.isEmpty()) {
            return new ResponseEntity<>("Hola " + nombre, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Solicitud no valida: El nombre es obligatorio", HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping("*")
    public ResponseEntity<String> manejarRutasNoEncontradas() {
        return new ResponseEntity<>("Recurso no encontrado", HttpStatus.NOT_FOUND);
    }
}


