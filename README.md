# 3D Procedural Terrain Generation in Java

This project aims to create a Java application that generates and renders 3D procedural terrain using a custom-built seedable noise function. The terrain is displayed within a circular visible area, and the user can navigate the environment using mouse-based controls.

## Project Overview

The goal is to build a complete system for procedural terrain generation and exploration, starting from a blank slate and implementing core components like noise generation, terrain chunking, rendering with OpenGL (via LWJGL), and user interaction.

## Progress

Here's a breakdown of the completed tasks and the current status of the project:

### 1. Project Setup
- [x] Chosen and set up **LWJGL** for OpenGL rendering, window management, and input handling.
- [x] Created a basic Java project structure with a main class.
- [x] Configured build tools (e.g., Maven or Gradle) for dependency management.
- [x] Initialized a resizable window using **GLFW** (via LWJGL) and set up an **OpenGL context** for 3D rendering.

### 2. Custom Noise Generation
- [x] Designed and implemented a **custom Perlin-like noise function** for generating heightmaps.
- [x] Created a **seedable permutation system** to ensure reproducible terrain generation.
- [x] Developed the core noise logic including gradient dot products, interpolation, and fading.
- [x] Added **layered noise (Fractal Brownian Motion)** with configurable parameters (scale, octaves, persistence, lacunarity).
- [x] Implemented **normalization** of the noise output to a consistent range.
- [x] Conducted **initial testing** of the noise implementation to verify smoothness and seed consistency.

### 3. Terrain Generation
- [x] Designed a **chunk-based terrain system** to divide the world into manageable blocks.
- [x] Began defining terrain features and variations using the custom noise function.
- [x] Set up a system to store terrain data (e.g., block types) within chunks.

### 4. Rendering the Terrain
- [x] Set up a **3D camera** with a fixed 45-degree perspective projection.
- [x] Implemented **mouse input** for camera rotation (zoom).
- [x] Developed logic to **render terrain chunks** by converting block data into 3D meshes.
- [x] Implemented **face culling** to optimize rendering by drawing only visible block faces.
- [x] Used a **fragment shader** to create a **circular visibility area** centered on the camera's view.
- [x] Added **basic directional lighting** to provide depth.
- [x] Implemented **basic texturing** using texture atlasing.

### 6. Optimization
- [x] Implemented **frustum culling** to render only chunks within the camera's view.

### 7. Seed System
- [x] Integrated the **user-provided seed** with the custom noise function to initialize the permutation table and ensure identical seeds produce identical terrain.

## Remaining Tasks (Partial List)

- Implement keyboard input for camera movement.
- Optimize noise calculations (caching, multithreading).
- Manage memory more dynamically by loading/unloading chunks.
- Add a UI for seed input.
- Conduct comprehensive testing and debugging for noise, terrain, rendering, and navigation.
- Enhance terrain details with features like biomes, trees, or rivers.
- Improve visuals with fog, shadows, or ambient occlusion.
- Add more UI elements for debugging and information display.
- Document the code and package the application for distribution.

## Getting Started

**(Instructions for cloning, building, and running the project will go here once ready.)**

## Built With

*   **LWJGL** - Lightweight Java Game Library (OpenGL, GLFW, etc.)
*   **Custom Noise** - Perlin-like noise implemented from scratch

## Contributing

**(Information on how to contribute to the project will go here.)**

## License

**(License information will go here.)**

## Acknowledgments

**(Any acknowledgments will go here.)**
