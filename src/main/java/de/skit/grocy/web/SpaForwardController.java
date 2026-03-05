package de.skit.grocy.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController {

    @RequestMapping({
            "/{p1:[^\\.]*}",
            "/{p1:[^\\.]*}/{p2:[^\\.]*}",
            "/{p1:[^\\.]*}/{p2:[^\\.]*}/{p3:[^\\.]*}",
            "/{p1:[^\\.]*}/{p2:[^\\.]*}/{p3:[^\\.]*}/{p4:[^\\.]*}",
            "/{p1:[^\\.]*}/{p2:[^\\.]*}/{p3:[^\\.]*}/{p4:[^\\.]*}/{p5:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}