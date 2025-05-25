package com.infsus.dz3_md.controller;

import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    /**
     * Main endpoint for projects - handles both listing and creation
     * GET: Shows the project list or form
     * POST: Creates a new project
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public String handleProject(@RequestParam(required = false) String q,
                              @PageableDefault(size = 10) Pageable pageable,
                              @Valid @ModelAttribute(required = false) Project project,
                              BindingResult br,
                              Model model,
                              RedirectAttributes ra) {
        
        // Handle POST request (project creation)
        if (project != null) {
            if (br.hasErrors()) {
                model.addAttribute("project", project);
                model.addAttribute("error", "Please correct the errors below.");
                return "project-form";
            }
            
            try {
                service.save(project);
                ra.addFlashAttribute("msg", "Project saved successfully!");
                return "redirect:/projects";
            } catch (Exception e) {
                model.addAttribute("project", project);
                model.addAttribute("error", e.getMessage());
                return "project-form";
            }
        }
        
        // Handle GET request (project listing)
        model.addAttribute("page", service.search(q, pageable));
        model.addAttribute("q", q);
        return "project-list";
    }

    /**
     * Handles project deletion
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("msg", "Project deleted successfully!");
        return "redirect:/projects";
    }
}
