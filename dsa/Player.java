package com.warofwavesbattlearena;

import com.jme3.math.Vector3f;

/**
 * Player class - Represents the player.
 * Stores health, position, and movement speed.
 *
 * ============================================================
 * COURSE OUTCOME MAPPING
 * ============================================================
 * CO2 (BTL-4): Design and implement Abstract Data Types (ADTs).
 *   - Player is an ADT encapsulating state (health, position, speed)
 *     with defined operations (takeDamage, jump, reset, toggleScope).
 *   - Demonstrates data abstraction — internal fields are private
 *     with getter/setter access (information hiding).
 *
 * CO3 (BTL-3): Apply stacks and queues to model real-world workflows.
 *   - Jump mechanics model a state machine (ground → airborne → ground),
 *     analogous to stack push/pop for managing nested states.
 *   - Scope toggle uses boolean state flip — binary queue-like alternation.
 *
 * CO5 (BTL-4): Design, develop and evaluate practical applications.
 *   - Player ADT is a core component of the game application,
 *     demonstrating how data structures underpin real systems.
 *
 * CO6 (BTL-5): Skill students to develop programs and applications.
 *   - Complete class design with constructor, getters, setters, and
 *     domain-specific methods showcasing OOP and DS principles.
 * ============================================================
 */
public class Player {
    
    // CO2: ADT internal state — encapsulated with private access
    private int health;
    private int maxHealth;
    private float speed;
    private Vector3f position;
    
    // CO3: Jump mechanics — state-based workflow (ground ↔ airborne)
    private float jumpVelocity = 0f;
    private boolean isOnGround = true;
    private final float JUMP_STRENGTH = 8f;  // CO1: Constant — O(1) lookup
    private final float GRAVITY = 20f;
    
    // CO3: Scope mechanics — toggle-based state management
    private boolean isScoping = false;
    
    /**
     * Constructor - Create a new player
     */
    public Player() {
        this.maxHealth = 100;
        this.health = maxHealth;
        this.speed = 10f;
        this.position = new Vector3f(0, 2, 0);
    }
    
    /**
     * Get current health
     */
    public int getHealth() {
        return health;
    }
    
    /**
     * Get movement speed
     */
    public float getSpeed() {
        return speed;
    }
    
    /**
     * Get current position
     */
    public Vector3f getPosition() {
        return position;
    }
    
    /**
     * Set player position
     */
    public void setPosition(Vector3f newPos) {
        this.position = newPos.clone();
    }
    
    /**
     * Take damage from enemy.
     * CO2: ADT operation — modify internal state with bounds checking.
     * CO1: O(1) constant time operation.
     */
    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) {
            health = 0;
        }
    }
    
    /**
     * Reset player to starting state
     */
    public void reset() {
        health = maxHealth;
        position = new Vector3f(0, 2, 0);
        jumpVelocity = 0f;
        isOnGround = true;
        isScoping = false;
    }
    
    /**
     * Jump if on ground.
     * CO3: State transition modeling — push from ground state to airborne.
     * Analogous to stack push (entering a new state).
     */
    public void jump() {
        if (isOnGround) {
            jumpVelocity = JUMP_STRENGTH;
            isOnGround = false;  // CO3: State change — workflow transition
        }
    }
    
    /**
     * Update jump physics each frame.
     * CO3: Continuous state update — gravity pulls back to ground state
     * (analogous to stack pop when landing).
     * CO1: O(1) per frame — constant time physics calculation.
     */
    public void updateJump(float tpf) {
        if (!isOnGround) {
            jumpVelocity -= GRAVITY * tpf;
        }
    }
    
    /**
     * Get jump velocity
     */
    public float getJumpVelocity() {
        return jumpVelocity;
    }
    
    /**
     * Set ground state
     */
    public void setOnGround(boolean onGround) {
        this.isOnGround = onGround;
        if (onGround) {
            jumpVelocity = 0f;
        }
    }
    
    /**
     * Check if on ground
     */
    public boolean isOnGround() {
        return isOnGround;
    }
    
    /**
     * Toggle scope
     */
    public void toggleScope() {
        isScoping = !isScoping;
    }
    
    /**
     * Check if scoping
     */
    public boolean isScoping() {
        return isScoping;
    }
    
    /**
     * Get movement speed (slower when scoping)
     */
    public float getEffectiveSpeed() {
        return isScoping ? speed * 0.5f : speed;
    }
}
