package com.warofwavesbattlearena;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;

/**
 * Enemy class - Represents a hostile NPC that chases and attacks the player.
 *
 * ============================================================
 * COURSE OUTCOME MAPPING
 * ============================================================
 * CO1 (BTL-4): Analyze algorithmic efficiency and searching algorithms.
 *   - update(): Linear search O(k) through building bounds for collision.
 *   - Direction calculation uses vector subtraction and normalization — O(1).
 *   - Wall-sliding algorithm: tries X-axis then Z-axis movement (greedy approach).
 *
 * CO2 (BTL-4): Design and implement Abstract Data Types (ADTs).
 *   - Enemy is an ADT with encapsulated state (position, health, speed)
 *     and operations (update, takeDamage, isDead, removeFromScene).
 *   - Receives ArrayList<BuildingBounds> for collision — demonstrates
 *     passing ADTs between modules.
 *
 * CO3 (BTL-3): Apply stacks and queues to model real-world workflows.
 *   - attackCooldown acts as a timer-based queue: attack → cooldown → ready.
 *   - Attack animation follows a sequential state workflow.
 *
 * CO5 (BTL-4): Practical application of Linear Data Structures.
 *   - Enemy objects are stored in ArrayList<Enemy> and managed with
 *     insert/delete/traverse operations during gameplay.
 *
 * CO6 (BTL-5): Develop complete programs and applications.
 *   - Full enemy AI with movement, collision, attack, and animation.
 * ============================================================
 */
public class Enemy {
    // CO2: ADT internal state — encapsulated enemy properties
    private Vector3f position;
    private float speed = 5f;
    private int health = 50;
    // CO3: Timer-based cooldown — models attack queue (attack → wait → ready)
    private float attackCooldown = 0f;
    private final float ATTACK_DELAY = 1.5f;  // CO1: Constant — O(1)
    private final float ATTACK_RANGE = 2.2f;
    
    // CO3: Attack animation state machine (idle → attacking → idle)
    private boolean isAttacking = false;
    private float attackAnimTime = 0f;
    private final float ATTACK_ANIM_DURATION = 0.4f;
    private Geometry rightArm;  // Store reference for animation
    
    // 3D model - humanoid made of multiple parts
    private Node enemyNode;
    private Node parentNode;
    private AssetManager assetManager;
    
    public Enemy(Vector3f startPosition, AssetManager assetManager, Node parent) {
        this.position = startPosition.clone();
        this.parentNode = parent;
        this.assetManager = assetManager;
        
        // Create node to hold all body parts
        enemyNode = new Node("Enemy");
        
        // Skin color (tan/brown for human look)
        ColorRGBA skinColor = new ColorRGBA(0.8f, 0.6f, 0.5f, 1f);
        ColorRGBA shirtColor = new ColorRGBA(0.3f, 0.3f, 0.6f, 1f);  // Blue shirt
        ColorRGBA pantsColor = new ColorRGBA(0.2f, 0.2f, 0.2f, 1f);  // Dark pants
        
        // HEAD - sphere
        Sphere headShape = new Sphere(12, 12, 0.25f);
        Geometry head = new Geometry("Head", headShape);
        head.setMaterial(createMaterial(skinColor));
        head.setLocalTranslation(0, 1.75f, 0);
        enemyNode.attachChild(head);
        
        // BODY/TORSO - box
        Box bodyShape = new Box(0.3f, 0.4f, 0.15f);
        Geometry body = new Geometry("Body", bodyShape);
        body.setMaterial(createMaterial(shirtColor));
        body.setLocalTranslation(0, 1.2f, 0);
        enemyNode.attachChild(body);
        
        // LEFT ARM
        Box armShape = new Box(0.1f, 0.35f, 0.1f);
        Geometry leftArm = new Geometry("LeftArm", armShape);
        leftArm.setMaterial(createMaterial(skinColor));
        leftArm.setLocalTranslation(-0.45f, 1.15f, 0);
        enemyNode.attachChild(leftArm);
        
        // RIGHT ARM
        Geometry rightArmGeo = new Geometry("RightArm", armShape);
        rightArmGeo.setMaterial(createMaterial(skinColor));
        rightArmGeo.setLocalTranslation(0.45f, 1.15f, 0);
        enemyNode.attachChild(rightArmGeo);
        this.rightArm = rightArmGeo;  // Store for animation
        
        // LEFT LEG
        Box legShape = new Box(0.12f, 0.4f, 0.12f);
        Geometry leftLeg = new Geometry("LeftLeg", legShape);
        leftLeg.setMaterial(createMaterial(pantsColor));
        leftLeg.setLocalTranslation(-0.15f, 0.4f, 0);
        enemyNode.attachChild(leftLeg);
        
        // RIGHT LEG
        Geometry rightLeg = new Geometry("RightLeg", legShape);
        rightLeg.setMaterial(createMaterial(pantsColor));
        rightLeg.setLocalTranslation(0.15f, 0.4f, 0);
        enemyNode.attachChild(rightLeg);
        
        // Set position
        enemyNode.setLocalTranslation(position.x, position.y, position.z);
        
        // Add to scene
        parent.attachChild(enemyNode);
    }

    private Material createMaterial(ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", color);
        mat.setColor("Ambient", color.mult(0.4f));
        mat.setColor("Specular", ColorRGBA.White.mult(0.2f));  // Add subtle shine
        mat.setFloat("Shininess", 16f);  // Moderate shininess
        mat.setBoolean("UseMaterialColors", true);
        return mat;
    }

    /**
     * CO1: Enemy AI update — linear search O(k) for building collision.
     * CO2: Receives ArrayList<BuildingBounds> ADT for spatial queries.
     * CO3: Manages cooldown and animation state workflows.
     */
    public void update(float tpf, Vector3f playerPosition, ArrayList<GameApp.BuildingBounds> buildings) {
        // CO3: Decrement cooldown timer — queue-like processing
        if (attackCooldown > 0) {
            attackCooldown -= tpf;
        }
        
        // Update attack animation
        if (isAttacking) {
            attackAnimTime += tpf;
            
            // Animate right arm swinging forward
            float progress = attackAnimTime / ATTACK_ANIM_DURATION;
            if (progress < 0.5f) {
                // Swing forward
                float angle = progress * 2f * 1.2f;  // Swing 70 degrees forward
                rightArm.setLocalTranslation(0.45f + (float)Math.sin(angle) * 0.3f, 1.15f, (float)Math.cos(angle) * 0.3f);
            } else {
                // Swing back to rest
                float angle = (1f - progress) * 2f * 1.2f;
                rightArm.setLocalTranslation(0.45f + (float)Math.sin(angle) * 0.3f, 1.15f, (float)Math.cos(angle) * 0.3f);
            }
            
            if (attackAnimTime >= ATTACK_ANIM_DURATION) {
                isAttacking = false;
                attackAnimTime = 0f;
                rightArm.setLocalTranslation(0.45f, 1.15f, 0);  // Reset position
            }
        }
        
        // Calculate direction and distance to player
        Vector3f direction = playerPosition.subtract(position);
        direction.y = 0; // Stay on ground
        float distanceToPlayer = direction.length();
        direction.normalizeLocal();
        
        // Only move if farther than attack range
        if (distanceToPlayer > ATTACK_RANGE) {
            // Store old position for collision detection
            Vector3f oldPosition = position.clone();
            
            // Calculate new position
            Vector3f newPosition = position.add(direction.mult(speed * tpf));
            
            // CO1: Linear search O(k) through building bounds for collision
            // Each AABB check is O(1), total: O(k) where k = number of buildings
            float enemyRadius = 0.6f;
            boolean colliding = false;
            
            for (GameApp.BuildingBounds building : buildings) {
                if (building.intersects(newPosition.x, newPosition.z, enemyRadius)) {
                    colliding = true;
                    break;  // CO1: Early termination — best case O(1)
                }
            }
            
            if (!colliding) {
                // Move to new position
                position.set(newPosition);
            } else {
                // Try sliding along walls
                Vector3f slideX = new Vector3f(newPosition.x, position.y, oldPosition.z);
                Vector3f slideZ = new Vector3f(oldPosition.x, position.y, newPosition.z);
                
                boolean canSlideX = true;
                boolean canSlideZ = true;
                
                for (GameApp.BuildingBounds building : buildings) {
                    if (building.intersects(slideX.x, slideX.z, enemyRadius)) {
                        canSlideX = false;
                    }
                    if (building.intersects(slideZ.x, slideZ.z, enemyRadius)) {
                        canSlideZ = false;
                    }
                }
                
                if (canSlideX) {
                    position.set(slideX);
                } else if (canSlideZ) {
                    position.set(slideZ);
                }
                // else stay at old position (blocked)
            }
        }
        
        // Update 3D model position
        enemyNode.setLocalTranslation(position.x, position.y, position.z);
    }
    public Vector3f getPosition() {
        return position;
    }

    public void takeDamage(int damage) {
        health -= damage;
    }
    

    public boolean isDead() {
        return health <= 0;
    }
    public boolean canAttack() {
        return attackCooldown <= 0;
    }
    public void resetAttackCooldown() {
        attackCooldown = ATTACK_DELAY;
    }

    public void playAttackAnimation() {
        isAttacking = true;
        attackAnimTime = 0f;
    }
    public void removeFromScene() {
        parentNode.detachChild(enemyNode);
    }
    public Vector3f getDeathPosition() {
        return position.clone().add(0, 1f, 0);
    }
}
