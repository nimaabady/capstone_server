package com.capstone.server.auth.dto;

import java.util.UUID;

public record UpdateResponseDto(

        UUID id,
        String email,
        String username

) { }
