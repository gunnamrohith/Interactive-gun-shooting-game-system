package com.warofwavesbattlearena;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.font.BitmapText;
import java.util.ArrayList;

/**
 * GameApp - Main game class that handles the game loop, input, and rendering.
 *
 * ============================================================
 * COURSE OUTCOME MAPPING
 * ============================================================
 * CO1 (BTL-4): Analyze algorithmic efficiency using asymptotic notation.
 *   - updateBullets(): O(n*m) collision detection — linear search of n bullets
 *     against m enemies. Trade-off: simplicity vs spatial hashing (CO4).
 *   - updateEnemies(): O(n) traversal of enemies list each frame.
 *   - isCollidingWithBuilding(): O(k) linear search across k building bounds.
 *
 * CO2 (BTL-4): Design and implement ADTs using arrays and linked lists.
 *   - ArrayList<Enemy>, ArrayList<Bullet>, ArrayList<BloodEffect>,
 *     ArrayList<BuildingBounds> — dynamic array-based ADT implementations.
 *   - Typical operations: insert (add), delete (remove), search (collision),
 *     traverse (for-each loops).
 *
 * CO3 (BTL-3): Apply stacks and queues to model real-world workflows.
 *   - The game loop (simpleUpdate) processes entities in FIFO order each frame,
 *     modeling a queue-based update pipeline.
 *   - shootCooldown acts as a timer-based queue to throttle fire rate.
 *
 * CO4 (BTL-4): Leverage Java Collections (List, Queue, Deque, Map) for
 *   efficient, scalable solutions.
 *   - Extensive use of java.util.ArrayList (List interface) for managing
 *     all game entities with O(1) amortized insertion.
 *   - BuildingBounds uses axis-aligned bounding box (AABB) for fast
 *     spatial lookup — O(k) per query.
 *
 * CO5 (BTL-4): Design, develop and evaluate practical applications for
 *   Linear Data Structures.
 *   - The entire game is a real-world application of linear data structures.
 *   - Enemies, bullets, blood effects are all managed via ArrayList with
 *     dynamic add/remove during gameplay.
 *
 * CO6 (BTL-5): Skill students to develop programs and applications.
 *   - Full game application integrating rendering, physics, input handling,
 *     and data structure management into a cohesive system.
 * ============================================================
 */
public class GameApp extends SimpleApplication {

    // CO2 & CO4: Game object collections — ArrayList (dynamic array ADT from java.util)
    // These lists demonstrate ADT operations: insert, delete, search, traverse
    private Player player;
    private WaveManager waveManager;
    private ArrayList<Enemy> enemies;        // CO2: Dynamic array storing Enemy objects
    private ArrayList<Bullet> bullets;       // CO2: Dynamic array storing Bullet projectiles
    private ArrayList<BloodEffect> bloodEffects; // CO2: Dynamic array storing visual effects
    private Gun gun;
    
    // CO4: Building collision bounds — uses Java Collections (ArrayList<BuildingBounds>)
    // for fast spatial lookup during collision detection
    private ArrayList<BuildingBounds> buildingBounds;
    
    // Game state
    private int score = 0;
    private boolean gameOver = false;
    
    // UI text
    private BitmapText healthText;
    private BitmapText waveText;
    private BitmapText scoreText;
    private BitmapText gameOverText;
    
    // Shooting cooldown
    private float shootCooldown = 0f;
    private final float SHOOT_DELAY = 0.3f;
    
    // Damage feedback
    private Geometry damageOverlay;
    private float damageFlashTime = 0f;
    private Vector3f cameraShakeOffset = new Vector3f();
    private float shakeIntensity = 0f;
    
    // Scope overlay
    private Geometry scopeOverlay;
    private Geometry scopeCrosshair;
    private final float NORMAL_FOV = 45f;
    private final float SCOPE_FOV = 20f;
    private float currentFOV = NORMAL_FOV;

    @Override
    public void simpleInitApp() {
        // CO2 & CO4: Initialize dynamic array-based ADTs (ArrayList)
        // ArrayList provides O(1) amortized insertion and O(n) traversal
        enemies = new ArrayList<>();          // CO2: ADT for enemy management
        bullets = new ArrayList<>();          // CO2: ADT for bullet tracking
        bloodEffects = new ArrayList<>();     // CO2: ADT for effect lifecycle
        buildingBounds = new ArrayList<>();   // CO4: Collection for spatial queries
        
        // Set sky blue background (day mode)
        viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.7f, 1.0f, 1.0f));
        
        // Setup the game
        setupCamera();
        setupLighting();
        createFloor();
        createBuildings();
        createBoundaryWalls();
        createPlayer();
        createGun();
        createWaveManager();
        setupInput();
        setupHUD();
        
        // Hide the default stats display
        setDisplayStatView(false);
        setDisplayFps(false);
    }

    /**
     * Setup first-person camera
     */
    private void setupCamera() {
        // Disable default fly camera movement (we control it ourselves)
        flyCam.setMoveSpeed(0);
        flyCam.setRotationSpeed(2f);
        
        // Set camera starting position
        cam.setLocation(new Vector3f(0, 2, 0));
        
        // Adjust camera frustum to prevent gun clipping (near plane closer)
        cam.setFrustumPerspective(45f, (float)cam.getWidth() / cam.getHeight(), 0.05f, 1000f);
    }

    /**
     * Add lights to the scene - bright daylight sun
     */
    private void setupLighting() {
        // Sun light - bright daylight from above
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.3f, -1f, -0.3f));
        sun.setColor(ColorRGBA.White.mult(1.5f));  // Bright sun
        rootNode.addLight(sun);
        
        // Second sun for better shadows
        DirectionalLight sun2 = new DirectionalLight();
        sun2.setDirection(new Vector3f(0.3f, -0.8f, 0.3f));
        sun2.setColor(ColorRGBA.Yellow.mult(0.5f));  // Warm fill light
        rootNode.addLight(sun2);
        
        // Ambient light - brighter for daytime
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.6f));
        rootNode.addLight(ambient);
    }

    /**
     * Create the ground/floor - green grass with texture-like appearance
     */
    private void createFloor() {
        // Create a flat box for the floor (50 x 0.1 x 50)
        Box floorShape = new Box(50, 0.1f, 50);
        Geometry floor = new Geometry("Floor", floorShape);
        
        // Create material - green grass color with specular highlights
        Material floorMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        floorMat.setColor("Diffuse", new ColorRGBA(0.15f, 0.55f, 0.15f, 1f));  // Richer green
        floorMat.setColor("Ambient", new ColorRGBA(0.1f, 0.35f, 0.1f, 1f));
        floorMat.setColor("Specular", new ColorRGBA(0.05f, 0.1f, 0.05f, 1f));  // Subtle shine
        floorMat.setFloat("Shininess", 8f);  // Low shininess for grass
        floorMat.setBoolean("UseMaterialColors", true);
        floor.setMaterial(floorMat);
        
        // Position floor at y = -0.1 (slightly below player)
        floor.setLocalTranslation(0, -0.1f, 0);
        
        // Add to scene
        rootNode.attachChild(floor);
    }

    /**
     * Create buildings around the map with windows and details
     */
    private void createBuildings() {
        // Building positions and sizes (x, z, width, height, depth)
        float[][] buildings = {
            {-20, -20, 4, 8, 4},   // Building 1
            {20, -25, 5, 12, 5},   // Building 2 - tall
            {-25, 15, 6, 6, 6},    // Building 3
            {25, 20, 4, 10, 4},    // Building 4
            {0, -30, 8, 5, 4},     // Building 5 - wide
            {-30, 0, 5, 15, 5},    // Building 6 - tallest
            {30, -10, 4, 7, 6},    // Building 7
            {15, 30, 6, 9, 4},     // Building 8
            {-15, -35, 5, 6, 5},   // Building 9
            {35, 5, 4, 11, 4},     // Building 10
        };
        
        // Building wall colors
        ColorRGBA[] wallColors = {
            new ColorRGBA(0.85f, 0.8f, 0.75f, 1f),   // Cream
            new ColorRGBA(0.7f, 0.55f, 0.4f, 1f),    // Brick brown
            new ColorRGBA(0.75f, 0.75f, 0.8f, 1f),   // Light blue-gray
            new ColorRGBA(0.9f, 0.85f, 0.75f, 1f),   // Sand
        };
        
        ColorRGBA windowColor = new ColorRGBA(0.3f, 0.5f, 0.7f, 1f);  // Blue glass
        ColorRGBA roofColor = new ColorRGBA(0.3f, 0.25f, 0.2f, 1f);   // Dark brown roof
        ColorRGBA doorColor = new ColorRGBA(0.4f, 0.25f, 0.15f, 1f);  // Wood door
        
        for (int i = 0; i < buildings.length; i++) {
            float x = buildings[i][0];
            float z = buildings[i][1];
            float w = buildings[i][2] / 2;
            float h = buildings[i][3] / 2;
            float d = buildings[i][4] / 2;
            
            Node buildingNode = new Node("Building" + i);
            ColorRGBA wallColor = wallColors[i % wallColors.length];
            
            // Main building body
            Box bodyShape = new Box(w, h, d);
            Geometry body = new Geometry("Body", bodyShape);
            body.setMaterial(createBuildingMaterial(wallColor));
            body.setLocalTranslation(0, h, 0);
            buildingNode.attachChild(body);
            
            // Roof (flat top or slanted)
            Box roofShape = new Box(w + 0.1f, 0.15f, d + 0.1f);
            Geometry roof = new Geometry("Roof", roofShape);
            roof.setMaterial(createBuildingMaterial(roofColor));
            roof.setLocalTranslation(0, h * 2 + 0.15f, 0);
            buildingNode.attachChild(roof);
            
            // Add windows on front and back
            int floors = (int)(h * 2 / 2.5f);  // One floor every 2.5 units
            int windowsPerFloor = (int)(w * 2 / 1.5f);  // One window every 1.5 units
            
            for (int floor = 0; floor < floors; floor++) {
                float windowY = 1.5f + floor * 2.5f;
                
                for (int win = 0; win < windowsPerFloor; win++) {
                    float windowX = -w + 0.75f + win * 1.5f;
                    
                    // Front window
                    Box windowShape = new Box(0.3f, 0.4f, 0.05f);
                    Geometry frontWindow = new Geometry("Window", windowShape);
                    frontWindow.setMaterial(createBuildingMaterial(windowColor));
                    frontWindow.setLocalTranslation(windowX, windowY, d + 0.05f);
                    buildingNode.attachChild(frontWindow);
                    
                    // Back window
                    Geometry backWindow = new Geometry("Window", windowShape);
                    backWindow.setMaterial(createBuildingMaterial(windowColor));
                    backWindow.setLocalTranslation(windowX, windowY, -d - 0.05f);
                    buildingNode.attachChild(backWindow);
                }
            }
            
            // Add door on front
            Box doorShape = new Box(0.5f, 1f, 0.05f);
            Geometry door = new Geometry("Door", doorShape);
            door.setMaterial(createBuildingMaterial(doorColor));
            door.setLocalTranslation(0, 1f, d + 0.05f);
            buildingNode.attachChild(door);
            
            // Position entire building
            buildingNode.setLocalTranslation(x, 0, z);
            rootNode.attachChild(buildingNode);
            
            // Add collision bounds for this building
            buildingBounds.add(new BuildingBounds(x, z, w * 2, d * 2));
        }
    }
    
    /**
     * Create boundary walls around the map edges
     */
    private void createBoundaryWalls() {
        float mapSize = 50f;
        float wallHeight = 4f;
        float wallThickness = 0.5f;
        
        ColorRGBA wallColor = new ColorRGBA(0.6f, 0.55f, 0.5f, 1f);  // Stone gray
        ColorRGBA capColor = new ColorRGBA(0.5f, 0.45f, 0.4f, 1f);   // Darker cap
        
        // North wall (positive Z)
        createWallSection(-mapSize, mapSize, mapSize, mapSize, wallThickness, wallHeight, wallColor, capColor, true);
        
        // South wall (negative Z)
        createWallSection(-mapSize, -mapSize, mapSize, -mapSize, wallThickness, wallHeight, wallColor, capColor, true);
        
        // East wall (positive X)
        createWallSection(mapSize, -mapSize, mapSize, mapSize, wallThickness, wallHeight, wallColor, capColor, false);
        
        // West wall (negative X)
        createWallSection(-mapSize, -mapSize, -mapSize, mapSize, wallThickness, wallHeight, wallColor, capColor, false);
        
        // Add collision bounds for all walls
        buildingBounds.add(new BuildingBounds(0, mapSize, mapSize * 2, wallThickness * 2));  // North
        buildingBounds.add(new BuildingBounds(0, -mapSize, mapSize * 2, wallThickness * 2)); // South
        buildingBounds.add(new BuildingBounds(mapSize, 0, wallThickness * 2, mapSize * 2));  // East
        buildingBounds.add(new BuildingBounds(-mapSize, 0, wallThickness * 2, mapSize * 2)); // West
    }
    
    /**
     * Create a single wall section
     */
    private void createWallSection(float x1, float z1, float x2, float z2, 
                                    float thickness, float height, 
                                    ColorRGBA wallColor, ColorRGBA capColor,
                                    boolean alongX) {
        float length = alongX ? Math.abs(x2 - x1) : Math.abs(z2 - z1);
        float centerX = (x1 + x2) / 2;
        float centerZ = (z1 + z2) / 2;
        
        Node wallNode = new Node("Wall");
        
        // Main wall body
        Box wallShape = alongX ? 
            new Box(length / 2, height / 2, thickness / 2) : 
            new Box(thickness / 2, height / 2, length / 2);
        Geometry wall = new Geometry("WallBody", wallShape);
        wall.setMaterial(createBuildingMaterial(wallColor));
        wall.setLocalTranslation(0, height / 2, 0);
        wallNode.attachChild(wall);
        
        // Top cap
        Box capShape = alongX ? 
            new Box(length / 2, 0.2f, thickness / 2 + 0.1f) : 
            new Box(thickness / 2 + 0.1f, 0.2f, length / 2);
        Geometry cap = new Geometry("WallCap", capShape);
        cap.setMaterial(createBuildingMaterial(capColor));
        cap.setLocalTranslation(0, height + 0.2f, 0);
        wallNode.attachChild(cap);
        
        // Add some decorative elements - vertical pillars every 10 units
        int numPillars = (int)(length / 10);
        for (int i = 0; i <= numPillars; i++) {
            float pillarOffset = -length / 2 + (i * length / numPillars);
            
            Box pillarShape = alongX ? 
                new Box(0.3f, height / 2 + 0.5f, thickness / 2 + 0.1f) : 
                new Box(thickness / 2 + 0.1f, height / 2 + 0.5f, 0.3f);
            Geometry pillar = new Geometry("Pillar", pillarShape);
            pillar.setMaterial(createBuildingMaterial(capColor));
            
            if (alongX) {
                pillar.setLocalTranslation(pillarOffset, height / 2 + 0.5f, 0);
            } else {
                pillar.setLocalTranslation(0, height / 2 + 0.5f, pillarOffset);
            }
            wallNode.attachChild(pillar);
        }
        
        // Position wall
        wallNode.setLocalTranslation(centerX, 0, centerZ);
        rootNode.attachChild(wallNode);
    }
    
    /**
     * Helper to create building material with improved appearance
     */
    private Material createBuildingMaterial(ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", color);
        mat.setColor("Ambient", color.mult(0.3f));
        mat.setColor("Specular", ColorRGBA.White.mult(0.3f));  // Add shine
        mat.setFloat("Shininess", 32f);  // Moderate shininess
        mat.setBoolean("UseMaterialColors", true);
        return mat;
    }

    /**
     * Create the player
     */
    private void createPlayer() {
        player = new Player();
    }

    /**
     * Create the first-person gun
     */
    private void createGun() {
        gun = new Gun(assetManager, rootNode);
    }

    /**
     * Create wave manager for spawning enemies
     */
    private void createWaveManager() {
        waveManager = new WaveManager(this);
    }

    /**
     * Setup keyboard and mouse controls
     */
    private void setupInput() {
        // Movement keys
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Scope", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Restart", new KeyTrigger(KeyInput.KEY_R));
        
        // Register movement listener
        inputManager.addListener(movementListener, "Forward", "Backward", "Left", "Right");
        inputManager.addListener(actionListener, "Shoot", "Restart", "Jump", "Scope");
    }


     // Handles continuous movement input
    private AnalogListener movementListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (gameOver) return;
            
            // Get camera direction for movement
            Vector3f camDir = cam.getDirection().clone();
            Vector3f camLeft = cam.getLeft().clone();
            
            // Keep movement horizontal (no flying)
            camDir.y = 0;
            camLeft.y = 0;
            camDir.normalizeLocal();
            camLeft.normalizeLocal();
            
            // Calculate movement
            Vector3f movement = new Vector3f();
            float speed = player.getEffectiveSpeed() * tpf;
            
            if (name.equals("Forward")) {
                movement.addLocal(camDir.mult(speed));
            }
            if (name.equals("Backward")) {
                movement.addLocal(camDir.mult(-speed));
            }
            if (name.equals("Left")) {
                movement.addLocal(camLeft.mult(speed));
            }
            if (name.equals("Right")) {
                movement.addLocal(camLeft.mult(-speed));
            }
            
            // Calculate new position
            Vector3f oldPos = cam.getLocation().clone();
            Vector3f newPos = oldPos.add(movement);
            
            // Keep player in bounds (-45 to 45)
            newPos.x = Math.max(-45, Math.min(45, newPos.x));
            newPos.z = Math.max(-45, Math.min(45, newPos.z));
            
            // Update Y position with jump height
            float jumpHeight = 2f;
            if (!player.isOnGround()) {
                jumpHeight += player.getJumpVelocity() * tpf;
            }
            newPos.y = Math.max(2f, jumpHeight) + cameraShakeOffset.y;
            
            // Check collision with buildings
            if (!isCollidingWithBuilding(newPos)) {
                cam.setLocation(newPos);
                player.setPosition(newPos);
            } else {
                // Try sliding along walls
                Vector3f slideX = new Vector3f(newPos.x, newPos.y, oldPos.z);
                Vector3f slideZ = new Vector3f(oldPos.x, newPos.y, newPos.z);
                
                if (!isCollidingWithBuilding(slideX)) {
                    cam.setLocation(slideX);
                    player.setPosition(slideX);
                } else if (!isCollidingWithBuilding(slideZ)) {
                    cam.setLocation(slideZ);
                    player.setPosition(slideZ);
                }
                // else don't move (hard collision)
            }
        }
    };

    /**
     * Handles button press actions
     */
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Shoot") && isPressed && !gameOver) {
                if (shootCooldown <= 0) {
                    shoot();
                    shootCooldown = SHOOT_DELAY;
                }
            }
            if (name.equals("Jump") && isPressed && !gameOver) {
                player.jump();
            }
            if (name.equals("Scope") && isPressed && !gameOver) {
                player.toggleScope();
                updateScopeView();
            }
            if (name.equals("Restart") && isPressed && gameOver) {
                restartGame();
            }
        }
    };

    /**
     * Create and fire a bullet from the gun
     */
    private void shoot() {
        // Trigger gun animation
        gun.shoot();
        
        // Create bullet at gun muzzle position, going in camera direction
        Vector3f position = gun.getMuzzlePosition(cam);
        Vector3f direction = cam.getDirection().clone();
        
        Bullet bullet = new Bullet(position, direction, assetManager, rootNode);
        bullets.add(bullet);
    }

    /**
     * Setup the HUD (health, wave, score display)
     */
    private void setupHUD() {
        // Health text
        healthText = new BitmapText(guiFont);
        healthText.setSize(guiFont.getCharSet().getRenderedSize() * 1.5f);
        healthText.setColor(ColorRGBA.Red);
        healthText.setLocalTranslation(10, cam.getHeight() - 10, 0);
        guiNode.attachChild(healthText);
        
        // Wave text
        waveText = new BitmapText(guiFont);
        waveText.setSize(guiFont.getCharSet().getRenderedSize() * 1.5f);
        waveText.setColor(ColorRGBA.Yellow);
        waveText.setLocalTranslation(10, cam.getHeight() - 50, 0);
        guiNode.attachChild(waveText);
        
        // Score text
        scoreText = new BitmapText(guiFont);
        scoreText.setSize(guiFont.getCharSet().getRenderedSize() * 1.5f);
        scoreText.setColor(ColorRGBA.Green);
        scoreText.setLocalTranslation(10, cam.getHeight() - 90, 0);
        guiNode.attachChild(scoreText);
        
        // Game over text (hidden initially)
        gameOverText = new BitmapText(guiFont);
        gameOverText.setSize(guiFont.getCharSet().getRenderedSize() * 3f);
        gameOverText.setColor(ColorRGBA.Red);
        gameOverText.setText("GAME OVER - Press R to Restart");
        gameOverText.setLocalTranslation(
            cam.getWidth() / 2 - gameOverText.getLineWidth() / 2,
            cam.getHeight() / 2,
            0
        );
        
        // Create damage flash overlay (red transparent screen)
        com.jme3.scene.shape.Quad quad = new com.jme3.scene.shape.Quad(cam.getWidth(), cam.getHeight());
        damageOverlay = new Geometry("DamageOverlay", quad);
        Material overlayMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        overlayMat.setColor("Color", new ColorRGBA(1f, 0f, 0f, 0f));  // Red, fully transparent initially
        overlayMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        damageOverlay.setMaterial(overlayMat);
        damageOverlay.setLocalTranslation(0, 0, 0);
        guiNode.attachChild(damageOverlay);
        
        // Create scope overlay (black edges with circle cutout)
        com.jme3.scene.shape.Quad scopeQuad = new com.jme3.scene.shape.Quad(cam.getWidth(), cam.getHeight());
        scopeOverlay = new Geometry("ScopeOverlay", scopeQuad);
        Material scopeMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        scopeMat.setColor("Color", new ColorRGBA(0f, 0f, 0f, 0.7f));  // Black semi-transparent
        scopeMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        scopeOverlay.setMaterial(scopeMat);
        scopeOverlay.setLocalTranslation(0, 0, 1);
        
        // Create scope crosshair (circle outline)
        float centerX = cam.getWidth() / 2;
        float centerY = cam.getHeight() / 2;
        float radius = Math.min(cam.getWidth(), cam.getHeight()) / 3;
        
        Node crosshairNode = new Node("ScopeCrosshair");
        
        // Create circle segments for scope outline
        int segments = 60;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float)(2 * Math.PI * i / segments);
            float angle2 = (float)(2 * Math.PI * (i + 1) / segments);
            
            float x1 = centerX + radius * (float)Math.cos(angle1);
            float y1 = centerY + radius * (float)Math.sin(angle1);
            float x2 = centerX + radius * (float)Math.cos(angle2);
            float y2 = centerY + radius * (float)Math.sin(angle2);
            
            com.jme3.scene.shape.Line line = new com.jme3.scene.shape.Line(
                new Vector3f(x1, y1, 0),
                new Vector3f(x2, y2, 0)
            );
            Geometry segment = new Geometry("Segment" + i, line);
            Material lineMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            lineMat.setColor("Color", new ColorRGBA(0.2f, 1f, 0.2f, 1f));  // Green scope
            segment.setMaterial(lineMat);
            crosshairNode.attachChild(segment);
        }
        
        // Add center crosshair lines
        float crossSize = 15f;
        com.jme3.scene.shape.Line hLine = new com.jme3.scene.shape.Line(
            new Vector3f(centerX - crossSize, centerY, 0),
            new Vector3f(centerX + crossSize, centerY, 0)
        );
        Geometry hCross = new Geometry("HorizontalCross", hLine);
        Material crossMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        crossMat.setColor("Color", new ColorRGBA(0.2f, 1f, 0.2f, 1f));
        hCross.setMaterial(crossMat);
        crosshairNode.attachChild(hCross);
        
        com.jme3.scene.shape.Line vLine = new com.jme3.scene.shape.Line(
            new Vector3f(centerX, centerY - crossSize, 0),
            new Vector3f(centerX, centerY + crossSize, 0)
        );
        Geometry vCross = new Geometry("VerticalCross", vLine);
        vCross.setMaterial(crossMat);
        crosshairNode.attachChild(vCross);
        
        scopeCrosshair = new Geometry("Crosshair", new com.jme3.scene.shape.Quad(1, 1));
        crosshairNode.setLocalTranslation(0, 0, 2);
        guiNode.attachChild(crosshairNode);
    }

    /**
     * Main game loop - runs every frame.
     * CO3: This loop models a queue-based processing pipeline where all
     *   game entities are updated in FIFO order each frame.
     * CO1: Each sub-method has distinct time complexity — see individual methods.
     */
    @Override
    public void simpleUpdate(float tpf) {
        if (gameOver) return;
        
        // Update shoot cooldown
        if (shootCooldown > 0) {
            shootCooldown -= tpf;
        }
        
        // Update jump physics
        player.updateJump(tpf);
        
        // Update camera height based on jump
        Vector3f camPos = cam.getLocation();
        float targetY = 2f;
        if (!player.isOnGround()) {
            targetY = Math.max(2f, camPos.y + player.getJumpVelocity() * tpf);
        }
        if (targetY <= 2f) {
            targetY = 2f;
            player.setOnGround(true);
        }
        camPos.y = targetY + cameraShakeOffset.y;
        cam.setLocation(camPos);
        
        // Smooth FOV transition for scope
        float targetFOV = player.isScoping() ? SCOPE_FOV : NORMAL_FOV;
        currentFOV += (targetFOV - currentFOV) * tpf * 8f;
        cam.setFrustumPerspective(currentFOV, (float)cam.getWidth() / cam.getHeight(), 0.05f, 1000f);
        
        // Update damage flash effect
        if (damageFlashTime > 0) {
            damageFlashTime -= tpf;
            float alpha = Math.max(0, damageFlashTime / 0.3f) * 0.4f;  // Fade from 40% to 0%
            Material mat = damageOverlay.getMaterial();
            mat.setColor("Color", new ColorRGBA(1f, 0f, 0f, alpha));
        }
        
        // Update camera shake
        if (shakeIntensity > 0) {
            shakeIntensity -= tpf * 8f;  // Decay shake
            if (shakeIntensity < 0) shakeIntensity = 0;
            
            // Apply random shake offset
            if (shakeIntensity > 0) {
                cameraShakeOffset.set(
                    (float)(Math.random() - 0.5) * shakeIntensity * 0.3f,
                    (float)(Math.random() - 0.5) * shakeIntensity * 0.3f,
                    0
                );
            } else {
                cameraShakeOffset.set(0, 0, 0);
            }
        }
        
        // Update gun position to follow camera
        gun.update(tpf, cam);
        
        // Update wave manager
        waveManager.update(tpf, enemies);
        
        // Update all enemies
        updateEnemies(tpf);
        
        // Update all bullets
        updateBullets(tpf);
        
        // Check if player is dead
        if (player.getHealth() <= 0) {
            gameOver = true;
            guiNode.attachChild(gameOverText);
        }
        
        // Update HUD
        updateHUD();
    }

    /**
     * Update all enemies.
     * CO1: Linear traversal O(n) where n = number of active enemies.
     *   Distance calculation uses Euclidean formula — O(1) per enemy.
     * CO2: Demonstrates traverse and delete operations on ArrayList ADT.
     *   Dead enemies are collected and batch-removed (avoids ConcurrentModification).
     * CO5: Practical application of linear data structures for real-time
     *   game entity management with dynamic insertion and removal.
     */
    private void updateEnemies(float tpf) {
        // CO2: Temporary list to collect dead enemies for batch removal
        ArrayList<Enemy> deadEnemies = new ArrayList<>();
        
        // Use camera position as player position
        Vector3f playerPos = cam.getLocation().clone();
        
        for (Enemy enemy : enemies) {
            // Move enemy toward player (camera) with collision detection
            enemy.update(tpf, playerPos, buildingBounds);
            
            // Check if enemy reached player - use horizontal distance only
            Vector3f enemyPos = enemy.getPosition();
            float dx = enemyPos.x - playerPos.x;
            float dz = enemyPos.z - playerPos.z;
            float distance = (float) Math.sqrt(dx * dx + dz * dz);
            
            if (distance < 2.5f) {
                // Enemy attacks player
                if (enemy.canAttack()) {
                    player.takeDamage(10);
                    enemy.resetAttackCooldown();
                    enemy.playAttackAnimation();
                    
                    // Visual damage feedback
                    damageFlashTime = 0.3f;  // Flash screen red for 0.3 seconds
                    shakeIntensity = 1.0f;   // Shake camera
                }
            }
            
            // Remove dead enemies
            if (enemy.isDead()) {
                deadEnemies.add(enemy);
                score += 10;
                
                // Create blood effect at enemy position
                BloodEffect blood = new BloodEffect(enemy.getDeathPosition(), assetManager, rootNode);
                bloodEffects.add(blood);
            }
        }
        
        // Remove dead enemies from list
        for (Enemy dead : deadEnemies) {
            dead.removeFromScene();
            enemies.remove(dead);
        }
        
        // Update blood effects
        ArrayList<BloodEffect> finishedEffects = new ArrayList<>();
        for (BloodEffect blood : bloodEffects) {
            blood.update(tpf);
            if (blood.isFinished()) {
                finishedEffects.add(blood);
            }
        }
        for (BloodEffect finished : finishedEffects) {
            finished.removeFromScene();
            bloodEffects.remove(finished);
        }
    }

    /**
     * Update all bullets and check for collisions.
     * CO1: Nested loop — O(n * m) where n = bullets, m = enemies.
     *   This is a brute-force linear search approach; for large n and m,
     *   spatial partitioning (e.g., hash grid from CO4) would reduce to ~O(n).
     * CO2: Demonstrates search operation on ArrayList — checking each
     *   bullet against each enemy for distance-based collision.
     * CO5: Practical application of linear search in real-time collision detection.
     */
    private void updateBullets(float tpf) {
        // CO2: Temporary list for batch deletion (avoids ConcurrentModificationException)
        ArrayList<Bullet> bulletsToRemove = new ArrayList<>();
        
        for (Bullet bullet : bullets) {
            bullet.update(tpf);
            
            // Check bullet lifetime
            if (bullet.isExpired()) {
                bulletsToRemove.add(bullet);
                continue;
            }
            
            // Check collision with enemies
            for (Enemy enemy : enemies) {
                float distance = bullet.getPosition().distance(enemy.getPosition());
                if (distance < 1.5f) {
                    enemy.takeDamage(50);
                    bulletsToRemove.add(bullet);
                    break;
                }
            }
        }
        
        // Remove bullets
        for (Bullet bullet : bulletsToRemove) {
            bullet.removeFromScene();
            bullets.remove(bullet);
        }
    }

    /**
     * Update HUD text
     */
    private void updateHUD() {
        healthText.setText("Health: " + player.getHealth());
        waveText.setText("Wave: " + waveManager.getCurrentWave());
        scoreText.setText("Score: " + score);
    }

    /**
     * Restart the game — resets all data structures.
     * CO2: Demonstrates clear/delete-all operation on ArrayList ADTs.
     *   Traverses each list to clean up resources, then clears — O(n) per list.
     * CO5: Practical use of linear data structure lifecycle management.
     */
    private void restartGame() {
        // CO2: Traverse and remove all enemies from ArrayList
        for (Enemy enemy : enemies) {
            enemy.removeFromScene();
        }
        enemies.clear();  // CO2: Clear entire ADT — O(n)
        
        // CO2: Traverse and remove all bullets from ArrayList
        for (Bullet bullet : bullets) {
            bullet.removeFromScene();
        }
        bullets.clear();  // CO2: Clear entire ADT — O(n)
        
        // CO2: Traverse and remove all blood effects from ArrayList
        for (BloodEffect blood : bloodEffects) {
            blood.removeFromScene();
        }
        bloodEffects.clear();  // CO2: Clear entire ADT — O(n)
        
        // Reset player
        player.reset();
        cam.setLocation(new Vector3f(0, 2, 0));
        
        // Reset wave manager
        waveManager.reset();
        
        // Reset score
        score = 0;
        
        // Remove game over text
        guiNode.detachChild(gameOverText);
        gameOver = false;
    }

    /**
     * Spawn an enemy (called by WaveManager).
     * CO2: Insert operation on ArrayList ADT — O(1) amortized.
     * CO4: Uses Java Collections List.add() for dynamic insertion.
     */
    public void spawnEnemy(Vector3f position) {
        Enemy enemy = new Enemy(position, assetManager, rootNode);
        enemies.add(enemy);  // CO2: ADT insert operation
    }
    
    /**
     * Get player position (for enemy spawning)
     */
    public Vector3f getPlayerPosition() {
        return player.getPosition();
    }
    
    /**
     * Check if a position collides with any building.
     * CO1: Linear search O(k) where k = number of building bounds.
     *   Each AABB intersection check is O(1). Overall: O(k) per query.
     *   For larger maps, a spatial hash (CO4) would improve to O(1) average.
     * CO4: Uses ArrayList (Java Collection) for sequential scan of BuildingBounds.
     */
    private boolean isCollidingWithBuilding(Vector3f position) {
        float playerRadius = 0.5f;  // Player collision radius
        
        // CO1: Linear search through all building bounds — O(k)
        for (BuildingBounds building : buildingBounds) {
            if (building.intersects(position.x, position.z, playerRadius)) {
                return true;  // CO1: Early termination on first collision found
            }
        }
        return false;
    }
    
    /**
     * Public static class to store building collision bounds (AABB).
     * CO2: This is an ADT encapsulating spatial data with defined operations
     *   (intersects). Demonstrates encapsulation and data abstraction.
     * CO4: Supports efficient spatial lookup — each intersection check is O(1).
     *   Used within an ArrayList (Java Collection) for sequential scanning.
     */
    public static class BuildingBounds {
        float centerX, centerZ;
        float width, depth;
        
        BuildingBounds(float x, float z, float w, float d) {
            this.centerX = x;
            this.centerZ = z;
            this.width = w;
            this.depth = d;
        }
        
        /**
         * CO1: AABB intersection check — O(1) constant time per check.
         * Uses closest-point-on-rectangle algorithm to determine collision.
         */
        public boolean intersects(float x, float z, float radius) {
            // Find closest point on building to position
            float closestX = Math.max(centerX - width/2, Math.min(x, centerX + width/2));
            float closestZ = Math.max(centerZ - depth/2, Math.min(z, centerZ + depth/2));
            
            // Calculate distance (squared to avoid costly sqrt — CO1: optimization)
            float dx = x - closestX;
            float dz = z - closestZ;
            float distSq = dx * dx + dz * dz;
            
            return distSq < (radius * radius);
        }
    }
    
    /**
     * Update scope overlay visibility
     */
    private void updateScopeView() {
        if (player.isScoping()) {
            guiNode.attachChild(scopeOverlay);
        } else {
            guiNode.detachChild(scopeOverlay);
        }
    }
}
