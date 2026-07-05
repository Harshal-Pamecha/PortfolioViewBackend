package com.portfolio.controller;

import com.portfolio.entity.PortfolioSnapshot;
import com.portfolio.entity.User;
import com.portfolio.repository.UserRepository;
import com.portfolio.security.UserContext;
import com.portfolio.service.PortfolioSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio/performance")
@RequiredArgsConstructor
public class PortfolioSnapshotController {

    private final PortfolioSnapshotService service;
    private final UserRepository userRepository;

    @GetMapping
    public List<PortfolioSnapshot> getPerformanceHistory(@RequestParam(defaultValue = "30") int days) {
        Integer userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User is not authenticated");
        }
        User user = userRepository.findById(userId).orElseThrow();
        return service.getOrSeedHistory(user, days);
    }
}
