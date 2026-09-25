package es.masanz;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GreetingController {

    @GetMapping("/")
    public String greeting(
            @RequestParam(name = "name", required = false, defaultValue = "Andoni") String name,
            Model model) {

        // El atributo recibirá el valor que pongas en la URL (poner?)
        model.addAttribute("name", name);


        return "index";
    }
}
