package com.warofwavesbattlearena;

/**
 * Main class - Entry point of the game
 * This starts our game application.
 *
 * ============================================================
 * COURSE OUTCOME MAPPING
 * ============================================================
 * CO6 (BTL-5): Skill the students to develop and create programs
 *   and applications in Data Structures.
 *   - This class demonstrates the entry point design pattern for
 *     a complete Java application built using data structures.
 *   - The main() method instantiates the GameApp and delegates
 *     control, showcasing modular application architecture.
 *
 * CO5 (BTL-4): Design, develop and evaluate common practical
 *   applications for Linear Data Structures.
 *   - The entire War of Waves Battle Arena game is a real-world practical
 *     application that relies on linear data structures
 *     (ArrayList) for managing enemies, bullets, and effects.
 * ============================================================
 */
public class Main {
    
    /**
    * Entry point of the War of Waves Battle Arena application.
     * CO6: Demonstrates creating a full-fledged game application
     * that integrates multiple data structure concepts.
     */
    public static void main(String[] args) {
        // CO6: Instantiate and launch the game - application development skill
        GameApp game = new GameApp();
        game.start();
    }
}
