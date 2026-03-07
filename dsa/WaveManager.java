package com.warofwavesbattlearena;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.Random;

/**
 * WaveManager - Handles spawning waves of enemies.
 * Each wave has more enemies than the last.
 *
 * ============================================================
 * COURSE OUTCOME MAPPING
 * ============================================================
 * CO1 (BTL-4): Analyze algorithmic efficiency.
 *   - startNextWave(): Enemy count formula 3 + (wave-1)*2 — O(1) calculation.
 *   - Spawning loop: O(e) where e = enemies per wave.
 *   - getRandomSpawnPosition(): O(1) — single random position generation.
 *
 * CO3 (BTL-3): Apply stacks and queues to model real-world workflows.
 *   - Wave management follows a sequential queue pattern:
 *     Wave N completes → delay timer → Wave N+1 starts.
 *   - Timer-based state machine: waveInProgress → waiting → next wave.
 *
 * CO4 (BTL-4): Leverage Java Collections for efficient solutions.
 *   - Receives ArrayList<Enemy> to check wave completion status.
 *   - Uses java.util.Random for spawn position generation.
 *
 * CO5 (BTL-4): Practical application of Linear Data Structures.
 *   - Wave system dynamically populates the enemies ArrayList,
 *     demonstrating real-time data structure growth.
 *
 * CO6 (BTL-5): Develop complete programs and applications.
 *   - Wave-based difficulty scaling is a key game design pattern.
 * ============================================================
 */
public class WaveManager {
    
    // CO3: Wave state machine — models sequential processing queue
    private int currentWave = 0;
    private int enemiesRemaining = 0;
    private float waveDelay = 2f;        // CO3: Delay between waves (queue wait time)
    private float timer = 0f;
    private boolean waveInProgress = false; // CO3: State flag for wave workflow
    
    // Reference to game for spawning
    private GameApp game;
    private Random random;
    
    /**
     * Constructor
     */
    public WaveManager(GameApp game) {
        this.game = game;
        this.random = new Random();
    }
    
    /**
     * Update wave manager each frame.
     * CO3: State machine — checks wave completion (queue empty?) then transitions.
     * CO4: Uses ArrayList.isEmpty() from Java Collections for O(1) check.
     */
    public void update(float tpf, ArrayList<Enemy> enemies) {
        if (waveInProgress) {
            // CO4: ArrayList.isEmpty() — O(1) check if all enemies defeated
            if (enemies.isEmpty()) {
                waveInProgress = false;  // CO3: Transition → waiting state
                timer = waveDelay;
            }
        } else {
            // CO3: Timer countdown — queue delay between waves
            timer -= tpf;
            if (timer <= 0) {
                startNextWave();  // CO3: Dequeue next wave
            }
        }
    }
    
    /**
     * Start the next wave.
     * CO1: Enemy count uses arithmetic formula — O(1) calculation.
     *   Spawning loop runs O(e) times where e = 3 + (wave-1)*2.
     * CO2: Each spawn inserts into ArrayList<Enemy> — O(1) amortized per insert.
     * CO5: Demonstrates dynamic growth of a linear data structure.
     */
    private void startNextWave() {
        currentWave++;
        
        // CO1: Arithmetic progression — O(1) calculation
        // Wave 1 = 3, Wave 2 = 5, Wave 3 = 7, ...
        int enemyCount = 3 + (currentWave - 1) * 2;
        
        // CO2 & CO5: Populate ArrayList with new enemies — O(e) total insertions
        for (int i = 0; i < enemyCount; i++) {
            Vector3f spawnPos = getRandomSpawnPosition();
            game.spawnEnemy(spawnPos);  // CO2: ADT insert operation
        }
        
        waveInProgress = true;
    }
    
    /**
     * Get random position around player (15-25 units away).
     * CO1: O(1) — constant time; uses trigonometric functions and clamping.
     *   No searching required since position is computed mathematically.
     */
    private Vector3f getRandomSpawnPosition() {
        Vector3f playerPos = game.getPlayerPosition();
        
        // Random angle (0 to 360 degrees) — O(1)
        float angle = random.nextFloat() * 360f;
        float radians = (float) Math.toRadians(angle);
        
        // Random distance (15 to 25 units) — O(1)
        float distance = 15f + random.nextFloat() * 10f;
        
        // CO1: Direct computation — no iteration needed
        float x = playerPos.x + (float) Math.cos(radians) * distance;
        float z = playerPos.z + (float) Math.sin(radians) * distance;
        
        // Clamp within map bounds — O(1)
        x = Math.max(-45, Math.min(45, x));
        z = Math.max(-45, Math.min(45, z));
        
        return new Vector3f(x, 0, z);
    }
    
    /**
     * Get current wave number
     */
    public int getCurrentWave() {
        return currentWave;
    }
    
    /**
     * Reset wave manager
     */
    public void reset() {
        currentWave = 0;
        waveInProgress = false;
        timer = 1f;  // Short delay before first wave
    }
}
