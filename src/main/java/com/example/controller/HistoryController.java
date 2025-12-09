package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.repository.SearchHistoryRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HistoryController {

    private final SearchHistoryRepository searchHistoryRepository;

    @GetMapping("/history")
    public String history(@RequestParam Integer userId, Model model) {
        model.addAttribute("history", searchHistoryRepository.findByUserUserIdOrderBySearchedAtDesc(userId));
        return "history";
    }
}
