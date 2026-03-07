package com.warofwavesbattlearena;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 * Bullet class - Projectile fired by player.
 *
 * ============================================================
 * COURSE OUTCOME MAPPING
 * ============================================================
 * CO2 (BTL-4): Design and implement Abstract Data Types (ADTs).
 *   - Bullet is an ADT with encapsulated state (position, direction,
 *     speed, lifetime) and operations (update, isExpired, removeFromScene).
 *   - Stored in ArrayList<Bullet> — demonstrates ADT within a linear DS.
 *
 * CO1 (BTL-4): Analyze algorithmic efficiency.
 *   - update(): O(1) per frame — constant time position calculation.
 *   - Collision checked externally via linear search in GameApp.updateBullets().
 *
 * CO5 (BTL-4): Practical application of Linear Data Structures.
 *   - Bullets are dynamically added/removed from ArrayList during gameplay,
 *     demonstrating real-time insert and delete on a linear ADT.
 * ============================================================
 */
public class Bullet {
    
    // CO2: ADT internal state — encapsulated bullet properties
    private Vector3f position;
    private Vector3f direction;
    private float speed = 50f;       // CO1: Constant — O(1) access
    private float lifetime = 3f;     // CO3: Timer-based expiry (auto-dequeue after 3s)
    
    // 3D model
    private Geometry model;
    private Node parentNode;
    
    /**
     * Constructor - Create bullet at position going in direction
     */
    public Bullet(Vector3f startPosition, Vector3f shootDirection, AssetManager assetManager, Node parent) {
        this.position = startPosition.clone();
        this.direction = shootDirection.normalize();
        this.parentNode = parent;
        
        // Create small sphere for bullet with trail effect
        Sphere bulletShape = new Sphere(10, 10, 0.12f);
        model = new Geometry("Bullet", bulletShape);
        
        // Create bright glowing material with lighting
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", new ColorRGBA(1f, 0.9f, 0.2f, 1f));  // Bright yellow-orange
        mat.setColor("Ambient", new ColorRGBA(1f, 0.8f, 0.1f, 1f));  // Bright ambient
        mat.setColor("Specular", ColorRGBA.White.mult(1.5f));  // Very bright specular
        mat.setFloat("Shininess", 128f);  // Very shiny
        mat.setBoolean("UseMaterialColors", true);
        model.setMaterial(mat);
        
        // Set starting position
        model.setLocalTranslation(position);
        
        // Add to scene
        parent.attachChild(model);
    }
    
    /**
     * Update bullet each frame.
     * CO1: O(1) constant time — simple vector arithmetic per frame.
     * CO3: Lifetime countdown models a self-expiring queue element.
     */
    public void update(float tpf) {
        // CO1: O(1) vector addition for movement
        position.addLocal(direction.mult(speed * tpf));
        
        // Update 3D model position
        model.setLocalTranslation(position);
        
        // CO3: Countdown timer — bullet auto-expires (dequeue trigger)
        lifetime -= tpf;
    }
    
    /**
     * Get current position
     */
    public Vector3f getPosition() {
        return position;
    }
    
    /**
     * Check if bullet should be removed
     */
    public boolean isExpired() {
        return lifetime <= 0;
    }
    
    /**
     * Remove bullet from scene
     */
    public void removeFromScene() {
        parentNode.detachChild(model);
    }
}
