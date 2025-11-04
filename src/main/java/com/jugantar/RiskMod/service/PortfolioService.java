package com.jugantar.RiskMod.service;
import com.jugantar.RiskMod.dto.PortfolioRequest;
import com.jugantar.RiskMod.dto.PortfolioResponse;
import com.jugantar.RiskMod.model.Portfolio;
import com.jugantar.RiskMod.model.User;
import com.jugantar.RiskMod.repository.PortfolioRepository;
import com.jugantar.RiskMod.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    @Autowired
    public PortfolioService(PortfolioRepository portfolioRepository, UserRepository userRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new portfolio for the given user.
     * @param request The DTO containing the portfolio's name.
     * @param username The username of the currently authenticated user.
     * @return A DTO representing the newly created portfolio.
     */
    public PortfolioResponse createPortfolio(PortfolioRequest request, String username) {

        // 1. Find the user in the database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Create the new Portfolio entity
        Portfolio portfolio = new Portfolio();
        portfolio.setName(request.getName());

        // 3. Link the portfolio to the user
        portfolio.setUser(user);

        // 4. Save the new portfolio to the database
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        // 5. Return the response DTO
        return new PortfolioResponse(
                savedPortfolio.getId(),
                savedPortfolio.getName(),
                user.getId()
        );
    }

    /**
     * Finds all portfolios for a given user.
     * @param username The username of the currently authenticated user.
     * @return A list of PortfolioResponse DTOs.
     */
    public List<PortfolioResponse> getPortfoliosByUsername(String username) {

        // 1. Find the user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Find all portfolios using the user's ID
        // (We already created this findByUserId method in our PortfolioRepository)
        List<Portfolio> portfolios = portfolioRepository.findByUserId(user.getId());

        // 3. Convert the list of Portfolio (Entities) to a list of PortfolioResponse (DTOs)
        return portfolios.stream()
                .map(portfolio -> new PortfolioResponse(
                        portfolio.getId(),
                        portfolio.getName(),
                        user.getId() // or portfolio.getUser().getId()
                ))
                .collect(Collectors.toList());
    }
}