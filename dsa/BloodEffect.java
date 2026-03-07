package com.warofwavesbattlearena;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import java.util.Random;

/**
 * BloodEffect class - Particle-based death effect using arrays.
 *
 * ============================================================
 * COURSE OUTCOME MAPPING
 * ============================================================
 * CO2 (BTL-4): Design and implement ADTs using arrays.
 *   - Uses fixed-size arrays (Geometry[], Vector3f[]) — array-based ADT.
 *   - Demonstrates array traversal and per-element update operations.
 *   - Trade-off: Fixed array (O(1) access) vs ArrayList (dynamic sizing).
 *
 * CO1 (BTL-4): Analyze algorithmic efficiency.
 *   - update(): O(PARTICLE_COUNT) = O(15) per frame — linear traversal.
 *   - removeFromScene(): O(PARTICLE_COUNT) — detach each particle.
 *   - Space complexity: O(PARTICLE_COUNT) for positions and velocities.
 *
 * CO3 (BTL-3): Apply time-based workflows.
 *   - Lifetime countdown models self-expiring effect (auto-cleanup).
 *   - Particles follow physics workflow: burst → gravity → stop at ground.
 *
 * CO5 (BTL-4): Practical application of Linear Data Structures.
 *   - Arrays used for real-time particle simulation in a game.
 *   - BloodEffect objects stored in ArrayList<BloodEffect> in GameApp.
 * ============================================================
 */
public class BloodEffect {
    
    // CO2: Array-based ADT — fixed-size arrays for particle data
    // Trade-off vs ArrayList: O(1) indexed access, no resizing overhead,
    // but fixed capacity (suitable here since particle count is constant)
    private Geometry[] particles;    // CO2: Array storing particle geometries
    private Vector3f[] velocities;   // CO2: Parallel array for velocity vectors
    private float lifetime;          // CO3: Timer for auto-expiry workflow
    private Node parentNode;
    private boolean finished = false;
    
    private static final int PARTICLE_COUNT = 15;   // CO1: Constant — defines array size
    private static final float MAX_LIFETIME = 1.5f;  // CO3: Expiry duration
    
    /**
     * Constructor - Create blood effect at position
     */
    public BloodEffect(Vector3f position, AssetManager assetManager, Node parent) {
        this.parentNode = parent;
        this.lifetime = MAX_LIFETIME;
        this.particles = new Geometry[PARTICLE_COUNT];
        this.velocities = new Vector3f[PARTICLE_COUNT];
        
        Random random = new Random();
        
        // Create blood particles with improved appearance
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            // Small sphere for blood drop with varied sizes
            Sphere dropShape = new Sphere(8, 8, 0.06f + random.nextFloat() * 0.06f);
            particles[i] = new Geometry("Blood" + i, dropShape);
            
            // Realistic blood material with lighting
            Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            mat.setColor("Diffuse", new ColorRGBA(0.7f, 0.05f, 0.05f, 1f));  // Bright dark red
            mat.setColor("Ambient", new ColorRGBA(0.4f, 0.02f, 0.02f, 1f));  // Dark ambient
            mat.setColor("Specular", new ColorRGBA(0.3f, 0.05f, 0.05f, 1f));  // Wet shine
            mat.setFloat("Shininess", 32f);  // Moderate shininess for wet look
            mat.setBoolean("UseMaterialColors", true);
            particles[i].setMaterial(mat);
            
            // Set starting position
            particles[i].setLocalTranslation(position.clone());
            
            // Random velocity - spread outward
            float vx = (random.nextFloat() - 0.5f) * 6f;
            float vy = random.nextFloat() * 4f + 2f;  // Upward burst
            float vz = (random.nextFloat() - 0.5f) * 6f;
            velocities[i] = new Vector3f(vx, vy, vz);
            
            parent.attachChild(particles[i]);
        }
    }
    
    /**
     * Update blood particles each frame.
     * CO1: O(PARTICLE_COUNT) = O(15) linear traversal per frame.
     * CO2: Array traversal operation on fixed-size ADT.
     * CO3: Lifetime countdown — triggers cleanup when expired.
     */
    public void update(float tpf) {
        // CO3: Timer decrement — auto-expiry workflow
        lifetime -= tpf;
        
        if (lifetime <= 0) {
            finished = true;
            return;
        }
        
        // CO2: Traverse array — update each particle's position and velocity
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            // Apply gravity
            velocities[i].y -= 15f * tpf;
            
            // Move particle
            Vector3f pos = particles[i].getLocalTranslation();
            pos.addLocal(velocities[i].mult(tpf));
            
            // Stop at ground
            if (pos.y < 0.05f) {
                pos.y = 0.05f;
                velocities[i].set(0, 0, 0);
            }
            
            particles[i].setLocalTranslation(pos);
            
            // Fade out based on lifetime
            float alpha = lifetime / MAX_LIFETIME;
            float scale = 0.5f + alpha * 0.5f;
            particles[i].setLocalScale(scale);
        }
    }
    
    /**
     * Check if effect is finished
     */
    public boolean isFinished() {
        return finished;
    }
    
    /**
     * Remove particles from scene.
     * CO2: Array traversal for cleanup — O(PARTICLE_COUNT).
     * CO5: Resource cleanup in practical application lifecycle.
     */
    public void removeFromScene() {
        // CO2: Traverse array and detach each element
        for (Geometry particle : particles) {
            parentNode.detachChild(particle);
        }
    }
}
