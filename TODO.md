# TODO: Procedural Terrain Generation in Java (3D) with Custom Noise

## Project Overview
Create a Java application that generates 3D procedural terrain using a custom seedable noise function, displays it within a circular visible area, and allows mouse-based navigation to explore the terrain in a 3D environment (not first-person).

## 1. Project Setup
- [x] Choose and set up a 3D graphics library.
    - Use LWJGL for OpenGL rendering, window management, and input handling.
    - Configure LWJGL dependencies (e.g., via Maven or Gradle).
- [x] Create a basic Java project structure.
    - Set up a main class to initialize the application.
    - Configure build tools for dependency management.
- [x] Initialize a window for rendering.
    - Use GLFW (via LWJGL) to create a resizable window.
    - Set up an OpenGL context for 3D rendering.

## 2. Custom Noise Generation
- [x] Design a custom Perlin-like noise function.
    - Research Perlin or Simplex noise principles (gradient-based noise for smooth transitions).
    - Plan for 2D noise for heightmaps, with potential extension to 3D for volumetric features.
- [x] Implement a seedable permutation system.
    - Create a permutation table (e.g., 0–255 array) initialized with a user-provided seed.
    - Shuffle the table deterministically using the seed for reproducible results.
    - Double the table for wraparound to handle out-of-bounds indices.
- [x] Develop core noise logic.
    - Assign random gradient vectors at grid points using the permutation table.
    - Implement fade (smoothstep), linear interpolation, and gradient dot products for smooth noise.
    - Compute noise values for any (x, y) coordinate, scaled by a user-defined factor.
- [x] Add layered noise (Fractal Brownian Motion).
    - Combine multiple noise layers (octaves) with varying frequency and amplitude.
    - Add parameters for scale, octaves, persistence, and lacunarity to control terrain detail.
    - Normalize output to a consistent range (e.g., 0 to 1) for heightmap use.
- [x] Test noise implementation.
    - Verify smoothness and randomness by outputting noise values for a small grid.
    - Check that different seeds produce varied but reproducible patterns.

## 3. Terrain Generation
- [x] Design a chunk-based terrain system.
    - Divide the world into chunks (e.g., 16x16x16 blocks) for efficient memory and rendering.
    - Use the custom noise function to generate heightmaps for each chunk.
- [x] Define terrain features.
    - Create biomes (e.g., plains, mountains) based on noise values (height, optional moisture layer).
    - Add variation (e.g., hills, cliffs) using layered noise.
- [x] Store terrain data.
    - Use a 3D array or hashmap to store block types (e.g., grass, stone) per chunk.

## 4. Rendering the Terrain
- [x] Set up a 3D camera.
    - Implement a camera class for position, and perspective projection, a fixed 45 degree angle
    - Enable mouse input for camera rotation (zoom).
    - Add optional keyboard input (WASD) for camera movement.
- [x] Render terrain chunks.
    - Convert chunk data into 3D meshes (voxel-based cubes).
    - Optimize by rendering only visible block faces (face culling).
- [x] Implement a circular visibility area.
    - Use a fragment shader to apply a circular mask centered on the camera’s view.
    - Adjust the radius dynamically based on camera distance or user input.
- [x] Add basic lighting and texturing.
    - Implement directional lighting for 3D depth.
    - Apply textures to blocks using texture atlasing.

## 6. Optimization
- [x] Optimize terrain rendering.
    - Use frustum culling to render only visible chunks.
    - Implement level-of-detail (LOD) for distant chunks.
- [ ] Optimize noise calculations.
    - Cache permutation tables and precompute gradients where possible.
    - Use background threads for noise generation to avoid lag.
- [x] Manage memory.
    - Load/unload chunks dynamically based on camera position.

## 7. Seed System
- [x] Integrate seed with noise.
    - Use a user-provided seed (e.g., integer or string hash) to initialize the permutation table.
    - Ensure identical seeds produce identical terrain.
- [x] Allow seed input.
    - Add a simple UI (ImGui text field) for seed input.
    - Default to a random seed if none provided.

## 8. Testing and Debugging
- [x] Test noise and terrain.
    - Verify noise smoothness and seed consistency across multiple runs.
    - Ensure chunk transitions and biome variations are seamless.
- [x] Test rendering.
    - Confirm circular visibility area follows camera and renders correctly.
    - Check performance with large terrains.
- [x] Test navigation.
    - Ensure mouse controls are smooth and intuitive.
    - Debug camera issues (e.g., jitter, clipping).

## 9. Polish and Enhancements
- [ ] Enhance terrain details.
    - Add features like trees or rivers using additional noise layers.
    - Vary block types based on height and biome.
- [x] Improve visuals.
    - Add fog for distant terrain.
    - Implement basic shadows or ambient occlusion.
- [x] Add UI elements.
    - Display current seed and camera coordinates.
    - Add toggle for visibility mask (debugging).

## 10. Documentation and Finalization
- [ ] Document the code.
    - Comment noise implementation, explaining gradient and interpolation logic.
    - Include README with setup instructions and controls.
- [ ] Package the application.
    - Ensure build compatibility across systems.
    - Test on different hardware.

## Tools and Libraries
- **LWJGL**: For OpenGL rendering, window management, and input.
- **Custom Noise**: Implement Perlin-like noise from scratch (no external libraries).
- **ImGui (optional)**: For UI (seed input, debug info).
- **Maven/Gradle**: For dependency management.

## Milestones
1. **Basic Setup**: Window and OpenGL context working.
2. **Custom Noise**: Seedable Perlin-like noise generating heightmaps.
3. **Terrain Generation**: Chunks generated using custom noise.
4. **Rendering**: Terrain displayed with circular visibility.
5. **Navigation**: Mouse-based camera controls implemented.
6. **Optimization**: Smooth performance with chunk and noise optimizations.
7. **Polish**: Add biomes, textures, and UI.