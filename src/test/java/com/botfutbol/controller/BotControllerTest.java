package com.botfutbol.controller;

import com.botfutbol.dto.PlayerDTO;
import com.botfutbol.entity.Player;
import com.botfutbol.entity.User;
import com.botfutbol.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests de integración para BotController.
 */
@ExtendWith(MockitoExtension.class)
class BotControllerTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private TeamService teamService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private MatchService matchService;

    @Mock
    private ChatParsingService chatParsingService;

    @Mock
    private GroupService groupService;

    @Mock
    private GameEventService gameEventService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private BotController botController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
    }

    @Test
    void testGetAllPlayers_Success() {
        // Arrange
        List<Player> players = new ArrayList<>();
        Player player = new Player();
        player.setName("Juan");
        players.add(player);
        
        when(userService.findById(1L)).thenReturn(testUser);
        when(playerService.getAllPlayers(testUser)).thenReturn(players);
        when(request.getHeader("X-User-Id")).thenReturn("1");

        // Act
        ResponseEntity<List<Player>> response = botController.getAllPlayers(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(playerService, times(1)).getAllPlayers(testUser);
    }

    @Test
    void testGetAllPlayers_Unauthorized() {
        // Arrange
        when(request.getHeader("X-User-Id")).thenReturn(null);

        // Act
        ResponseEntity<List<Player>> response = botController.getAllPlayers(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(playerService, never()).getAllPlayers(any());
    }

    @Test
    void testAddPlayer_Success() {
        // Arrange
        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setName("Juan");
        playerDTO.setSkillLevel(7);
        
        Player savedPlayer = new Player();
        savedPlayer.setName("Juan");
        
        when(userService.findById(1L)).thenReturn(testUser);
        when(playerService.addPlayer(any(PlayerDTO.class), eq(testUser))).thenReturn(savedPlayer);
        when(request.getHeader("X-User-Id")).thenReturn("1");

        // Act
        ResponseEntity<?> response = botController.addPlayerRest(playerDTO, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Juan"));
        verify(playerService, times(1)).addPlayer(any(PlayerDTO.class), eq(testUser));
    }

    @Test
    void testAddPlayer_InvalidUser() {
        // Arrange
        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setName("Juan");
        
        when(request.getHeader("X-User-Id")).thenReturn("999");
        when(userService.findById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> botController.addPlayerRest(playerDTO, request));
    }
}

