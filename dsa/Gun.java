package com.warofwavesbattlearena;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;

/**
 * Gun class - First-person gun model that follows the camera.
 *
 * ============================================================
 * COURSE OUTCOME MAPPING
 * ============================================================
 * CO2 (BTL-4): Design and implement Abstract Data Types (ADTs).
 *   - Gun is an ADT with internal state (recoil, muzzle flash, offsets)
 *     and operations (shoot, update, getMuzzlePosition).
 *   - Demonstrates encapsulation and data hiding.
 *
 * CO3 (BTL-3): Apply queues/stacks to model real-world workflows.
 *   - Recoil animation uses a decay-based workflow (shoot → recoil → recover).
 *   - Muzzle flash follows a timed state: show → timer → hide.
 *
 * CO6 (BTL-5): Skill students to develop programs and applications.
 *   - Complex 3D model construction with multiple parts demonstrates
 *     building sophisticated application components.
 * ============================================================
 */
public class Gun {
    
    // CO2: ADT internal state — gun properties
    private Node gunNode;
    private AssetManager assetManager;
    
    // Gun position offset from camera
    private Vector3f baseOffset = new Vector3f(0.4f, -0.3f, 0.8f);
    
    // CO3: Recoil workflow — shoot triggers recoil, then gradual recovery
    private float recoilAmount = 0f;
    private float recoilRecovery = 10f;
    
    // CO3: Muzzle flash state — timed show/hide workflow
    private Geometry muzzleFlash;
    private float muzzleFlashTime = 0f;
    
    /**
     * Constructor - Create the gun model
     */
    public Gun(AssetManager assetManager, Node parent) {
        this.assetManager = assetManager;
        
        gunNode = new Node("Gun");
        
        // Colors
        ColorRGBA gunMetal = new ColorRGBA(0.2f, 0.2f, 0.25f, 1f);
        ColorRGBA gunGrip = new ColorRGBA(0.15f, 0.1f, 0.05f, 1f);
        ColorRGBA gunAccent = new ColorRGBA(0.3f, 0.3f, 0.35f, 1f);
        
        // BARREL - main long part
        Box barrelShape = new Box(0.03f, 0.04f, 0.25f);
        Geometry barrel = new Geometry("Barrel", barrelShape);
        barrel.setMaterial(createMaterial(gunMetal));
        barrel.setLocalTranslation(0, 0, 0.15f);
        gunNode.attachChild(barrel);
        
        // BARREL TOP RAIL
        Box railShape = new Box(0.025f, 0.015f, 0.2f);
        Geometry rail = new Geometry("Rail", railShape);
        rail.setMaterial(createMaterial(gunAccent));
        rail.setLocalTranslation(0, 0.055f, 0.1f);
        gunNode.attachChild(rail);
        
        // RECEIVER - middle body
        Box receiverShape = new Box(0.04f, 0.06f, 0.12f);
        Geometry receiver = new Geometry("Receiver", receiverShape);
        receiver.setMaterial(createMaterial(gunMetal));
        receiver.setLocalTranslation(0, 0, -0.05f);
        gunNode.attachChild(receiver);
        
        // GRIP - handle
        Box gripShape = new Box(0.025f, 0.08f, 0.04f);
        Geometry grip = new Geometry("Grip", gripShape);
        grip.setMaterial(createMaterial(gunGrip));
        // Rotate grip to angle back
        grip.setLocalTranslation(0, -0.1f, -0.1f);
        Quaternion gripRot = new Quaternion();
        gripRot.fromAngles(0.3f, 0, 0);
        grip.setLocalRotation(gripRot);
        gunNode.attachChild(grip);
        
        // TRIGGER GUARD
        Box guardShape = new Box(0.015f, 0.025f, 0.03f);
        Geometry guard = new Geometry("Guard", guardShape);
        guard.setMaterial(createMaterial(gunMetal));
        guard.setLocalTranslation(0, -0.04f, -0.05f);
        gunNode.attachChild(guard);
        
        // MAGAZINE
        Box magShape = new Box(0.02f, 0.06f, 0.03f);
        Geometry magazine = new Geometry("Magazine", magShape);
        magazine.setMaterial(createMaterial(gunAccent));
        magazine.setLocalTranslation(0, -0.08f, -0.02f);
        gunNode.attachChild(magazine);
        
        // FRONT SIGHT
        Box frontSightShape = new Box(0.008f, 0.02f, 0.008f);
        Geometry frontSight = new Geometry("FrontSight", frontSightShape);
        frontSight.setMaterial(createMaterial(gunMetal));
        frontSight.setLocalTranslation(0, 0.065f, 0.35f);
        gunNode.attachChild(frontSight);
        
        // REAR SIGHT
        Box rearSightShape = new Box(0.015f, 0.015f, 0.008f);
        Geometry rearSight = new Geometry("RearSight", rearSightShape);
        rearSight.setMaterial(createMaterial(gunMetal));
        rearSight.setLocalTranslation(0, 0.065f, 0.05f);
        gunNode.attachChild(rearSight);
        
        // MUZZLE FLASH (hidden by default)
        Box flashShape = new Box(0.08f, 0.08f, 0.02f);
        muzzleFlash = new Geometry("MuzzleFlash", flashShape);
        Material flashMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        flashMat.setColor("Color", new ColorRGBA(1f, 0.8f, 0.2f, 1f));
        muzzleFlash.setMaterial(flashMat);
        muzzleFlash.setLocalTranslation(0, 0, 0.42f);
        muzzleFlash.setCullHint(Geometry.CullHint.Always);  // Hidden
        gunNode.attachChild(muzzleFlash);
        
        // Scale gun to appropriate size
        gunNode.setLocalScale(1.2f);
        
        parent.attachChild(gunNode);
    }
    
    /**
     * Create material for gun parts with improved appearance
     */
    private Material createMaterial(ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", color);
        mat.setColor("Ambient", color.mult(0.25f));
        mat.setColor("Specular", ColorRGBA.White.mult(0.5f));  // Metallic shine
        mat.setFloat("Shininess", 64f);  // High shininess for metal
        mat.setBoolean("UseMaterialColors", true);
        return mat;
    }
    
    /**
     * Update gun position to follow camera
     */
    public void update(float tpf, Camera cam) {
        // Recover from recoil
        if (recoilAmount > 0) {
            recoilAmount -= recoilRecovery * tpf;
            if (recoilAmount < 0) recoilAmount = 0;
        }
        
        // Update muzzle flash timer
        if (muzzleFlashTime > 0) {
            muzzleFlashTime -= tpf;
            if (muzzleFlashTime <= 0) {
                muzzleFlash.setCullHint(Geometry.CullHint.Always);  // Hide flash
            }
        }
        
        // Calculate gun position relative to camera
        Vector3f camDir = cam.getDirection().clone();
        Vector3f camLeft = cam.getLeft().clone();
        Vector3f camUp = cam.getUp().clone();
        
        // Base position
        Vector3f gunPos = cam.getLocation().clone();
        
        // Offset right
        gunPos.addLocal(camLeft.mult(-baseOffset.x));
        // Offset down
        gunPos.addLocal(camUp.mult(baseOffset.y));
        // Offset forward (minus recoil)
        gunPos.addLocal(camDir.mult(baseOffset.z - recoilAmount * 0.1f));
        
        gunNode.setLocalTranslation(gunPos);
        
        // Rotate gun to match camera direction
        gunNode.setLocalRotation(cam.getRotation());
    }
    
    /**
     * Called when player shoots - trigger recoil and muzzle flash.
     * CO3: Initiates the shoot workflow — sets recoil state and flash timer.
     * CO2: ADT operation modifying internal state.
     */
    public void shoot() {
        recoilAmount = 1f;         // CO3: Begin recoil state
        muzzleFlashTime = 0.05f;   // CO3: Begin flash timer
        muzzleFlash.setCullHint(Geometry.CullHint.Never);  // Show flash
    }
    
    /**
     * Get the muzzle position for bullet spawn
     */
    public Vector3f getMuzzlePosition(Camera cam) {
        Vector3f muzzlePos = cam.getLocation().clone();
        muzzlePos.addLocal(cam.getDirection().mult(1.0f));
        return muzzlePos;
    }
}
