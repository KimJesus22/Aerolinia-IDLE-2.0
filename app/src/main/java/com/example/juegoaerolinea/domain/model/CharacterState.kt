package com.example.juegoaerolinea.domain.model

enum class CharacterDirection {
    UP, DOWN, LEFT, RIGHT
}

data class CharacterState(
    val x: Float = 0f,
    val y: Float = 0f,
    val targetX: Float = 0f,
    val targetY: Float = 0f,
    val isMoving: Boolean = false,
    val direction: CharacterDirection = CharacterDirection.DOWN,
    val animationFrame: Int = 0 
)
