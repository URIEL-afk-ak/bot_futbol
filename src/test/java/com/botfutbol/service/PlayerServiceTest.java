package com.botfutbol.service;

import com.botfutbol.constants.PlayerConstants;
import com.botfutbol.dto.PlayerDTO;
import com.botfutbol.entity.Player;
import com.botfutbol.entity.User;
import com.botfutbol.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PlayerService.
 */
@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private User testUser;
    private PlayerDTO playerDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        
        playerDTO = new PlayerDTO();
        playerDTO.setName("Juan");
        playerDTO.setSkillLevel(7);
        playerDTO.setPosition("DEL");
    }

    @Test
    void testAddPlayer_WithAllFields() {
        // Arrange
        Player savedPlayer = new Player();
        savedPlayer.setId("player-1");
        savedPlayer.setName("Juan");
        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);

        // Act
        Player result = playerService.addPlayer(playerDTO, testUser);

        // Assert
        assertNotNull(result);
        assertEquals("Juan", result.getName());
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    void testAddPlayer_WithDefaultValues() {
        // Arrange
        PlayerDTO dtoWithoutDefaults = new PlayerDTO();
        dtoWithoutDefaults.setName("Pedro");
        // skillLevel y position son null
        
        Player savedPlayer = new Player();
        savedPlayer.setId("player-2");
        savedPlayer.setName("Pedro");
        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);

        // Act
        Player result = playerService.addPlayer(dtoWithoutDefaults, testUser);

        // Assert
        assertNotNull(result);
        assertEquals("Pedro", result.getName());
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    void testUpdateSkillLevel_ValidLevel() {
        // Arrange
        Player existingPlayer = new Player();
        existingPlayer.setId("player-1");
        existingPlayer.setName("Juan");
        existingPlayer.setSkillLevel(5);
        
        when(playerRepository.findByNameIgnoreCaseAndUser("Juan", testUser))
                .thenReturn(Optional.of(existingPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(existingPlayer);

        // Act
        Player result = playerService.updateSkillLevel("Juan", 8, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(8, result.getSkillLevel());
        verify(playerRepository, times(1)).save(existingPlayer);
    }

    @Test
    void testUpdateSkillLevel_InvalidLevel_TooLow() {
        // Arrange
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.updateSkillLevel("Juan", 0, testUser)
        );
        
        assertEquals(PlayerConstants.ERROR_SKILL_LEVEL_RANGE, exception.getMessage());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void testUpdateSkillLevel_InvalidLevel_TooHigh() {
        // Arrange
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.updateSkillLevel("Juan", 11, testUser)
        );
        
        assertEquals(PlayerConstants.ERROR_SKILL_LEVEL_RANGE, exception.getMessage());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void testUpdateSkillLevel_PlayerNotFound() {
        // Arrange
        when(playerRepository.findByNameIgnoreCaseAndUser("Inexistente", testUser))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.updateSkillLevel("Inexistente", 7, testUser)
        );
        
        assertTrue(exception.getMessage().contains(PlayerConstants.ERROR_PLAYER_NOT_FOUND));
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void testFindPlayerByName_PlayerExists() {
        // Arrange
        Player player = new Player();
        player.setName("Juan");
        when(playerRepository.findByNameIgnoreCaseAndUser("Juan", testUser))
                .thenReturn(Optional.of(player));

        // Act
        Optional<Player> result = playerService.findPlayerByName("Juan", testUser);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Juan", result.get().getName());
    }

    @Test
    void testFindPlayerByName_PlayerNotExists() {
        // Arrange
        when(playerRepository.findByNameIgnoreCaseAndUser("Inexistente", testUser))
                .thenReturn(Optional.empty());

        // Act
        Optional<Player> result = playerService.findPlayerByName("Inexistente", testUser);

        // Assert
        assertFalse(result.isPresent());
    }
}

