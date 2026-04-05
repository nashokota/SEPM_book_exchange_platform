package com.team.book_exchange.controller.web;

import com.team.book_exchange.dto.view.MilestoneView;
import com.team.book_exchange.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    @GetMapping("/milestones")
    public String milestones(Model model) {
        model.addAttribute("milestones", milestoneService.getMilestones());
        return "milestones/index";
    }

    @GetMapping("/api/milestones")
    @ResponseBody
    public List<MilestoneView> milestonesApi() {
        return milestoneService.getMilestones();
    }
}
