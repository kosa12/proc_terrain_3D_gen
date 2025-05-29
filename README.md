# Procedural Terrain Generation with LWJGL in Java

A Java-based 3D procedural terrain generator built using LWJGL (Lightweight Java Game Library) and OpenGL, leveraging Perlin noise with fractal Brownian motion for realistic terrain generation. This project supports customizable terrain through seed and metric inputs, features an ImGUI interface for real-time parameter adjustments, and is optimized with chunk culling and texture atlasing. The terrain includes grass and sand biomes, with a WASD movement system and an interactive cursor mode for adjusting metrics.

![OpenGL](https://img.shields.io/badge/OpenGL-%23FFFFFF.svg?style=for-the-badge&logo=opengl)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)
![Arch](https://img.shields.io/badge/Arch%20Linux-1793D1?logo=arch-linux&logoColor=fff&style=for-the-badge)

## Features
- **Procedural Terrain**: Generates 3D terrain using Perlin noise with fractal Brownian motion layering for natural-looking landscapes.
- **Customizable Input**: Supports seed values and adjustable metrics (e.g., terrain height, scale) via an ImGUI interface.
- **Biomes**: Includes grass and sand biomes with texture atlasing for efficient rendering.
- **Optimization**: Implements chunk-based rendering (16x16 chunks) with culling to improve performance.
- **Input System**:
    - WASD for camera movement.
    - Press `Enter` to toggle a visible cursor for interacting with ImGUI metric sliders.
- **Tech Stack**:
    - Java 21
    - LWJGL for OpenGL rendering
    - Built with Gradle
- **Rendering**: Uses texture atlasing for efficient biome texture application.

## Configurable metrics

- **Scale**:
    - _Default value_: `0.078` (float, range: 0.01–0.2)
    - _Description_: Determines the spatial frequency of Perlin noise by scaling the input coordinates `(x * scale, z * scale)`. A smaller value creates larger, more spread-out terrain features (e.g., broad hills or mountains), while a larger value produces smaller, denser features (e.g., tightly packed ridges).
- **Octaves**:
    - _Default value_: `5` (integer, range: 1–10)
    - _Description_: Specifies the number of Perlin noise layers combined in Fractal Brownian Motion. Each octave adds finer details by increasing the frequency and reducing the amplitude of the noise.
- **Persistence**:
    - _Default value_: `0.5` (float, range: 1.0–4.0)
    - _Description_: Controls the amplitude reduction of each successive octave in FBM. Higher values give smaller-scale details more influence, making the terrain rougher and more jagged.
- **Lacunarity**:
    - _Default value_: `2.6`  (float, range: 1.0–4.0)
    - _Description_: Determines the frequency multiplier for each octave in FBM. Higher values increase the frequency of details, adding finer features to the terrain.
- **Height Scale**:
    - _Default value_: `8.7` (float, range: 1.0–16.0)
    - _Description_: Scales the Perlin noise output to determine the terrain's vertical height. The noise value (0–1) is multiplied by `heightScale` and added to `baseHeight` to compute block heights.
- **Base Height**:
    - _Default value_: `3.1` (float, range: 0.0–8.0)
    - _Description_: Sets the minimum height of the terrain by adding a constant offset to the scaled noise value. Ensures a baseline elevation for all terrain.
- **Sand Height Threshold**:
    - _Default value_: `6` (integer, range: 0–16)
    - _Description_: Defines the maximum y-coordinate (height) below which blocks are set as sand (block type 3). Blocks at or below this height, if solid, become sand instead of grass or stone.
- **Seed**:
    - _Default value_: `67890` (long)
    - _Description_: Initializes the random number generator for Perlin noise, ensuring consistent terrain generation for the same seed. Different seeds produce unique landscapes.
- **Noise Type**:
    - _Default value_: `"Standard"` (string, options: Standard, Ridged, Billowy, Hybrid)
    - _Description_: Specifies the Perlin noise variant used for terrain generation:
        - **Standard**: Classic Perlin noise, producing smooth, natural hills and valleys.
        - **Ridged**: Emphasizes sharp ridges and creases, ideal for mountainous terrain.
        - **Billowy**: Creates soft, rolling hills with a cloud-like appearance.
        - **Hybrid**: Combines Standard and Ridged for varied terrain with both smooth and sharp features.


## Prerequisites
- **Java 21**: Ensure you have JDK 21 installed.
- **Gradle**: Required for building and managing dependencies.
- **LWJGL**: Configured via Gradle for OpenGL rendering.
- A compatible GPU with OpenGL 4.1+ support.

## Tested on

- **Arch Linux**: `Linux 6.12.30-1-lts`
- **JDK**: `openjdk 21.0.7 2025-04-15`
- **Gradle**: `8.14.1`
- **OpenGL**: `4.6.0`

## Setup Instructions
1. **Clone the Repository**:
   ```bash
    git clone https://github.com/kosa12/proc_terrain_3D_gen
    cd proc_terrain_3D_gen
   ```
2. **Build the Project**:
   ```bash
    gradle build
    ```
3. **Run the Application**:
    ```bash
    gradle run
    ```
## Usage

- **Navigation**: Use WASD keys to move the camera around the 3D terrain.
- **Metric Adjustments**: Press Enter to toggle the cursor and interact with the ImGUI interface. Adjust sliders to modify terrain parameters like height, scale, or seed.
- **Biomes**: Explore grass and sand biomes, rendered with texture atlasing for smooth visuals.
- **Performance**: The terrain is divided into 16x16 chunks with culling to ensure efficient rendering.

## Project Structure

```bash
src
└── main
    ├── java
    │   └── edu
    │       └── kosa
    │           └── terrainproject
    │               ├── app
    │               │   └── Main.java
    │               ├── graphics
    │               │   ├── Camera.java
    │               │   ├── Mesh.java
    │               │   ├── Renderer.java
    │               │   ├── ShaderProgram.java
    │               │   ├── TextureLoader.java
    │               │   └── WindowManager.java
    │               ├── input
    │               │   └── InputHandler.java
    │               ├── noise
    │               │   └── PerlinNoise.java
    │               └── terrain
    │                   ├── Chunk.java
    │                   ├── ChunkPos.java
    │                   ├── TerrainConfig.java
    │                   └── World.java
    └── resources
        └── textures
            └── atlas.png
```

## Contributing

Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m 'Add your feature'`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.
6. I'll take a look at your so-called _**feature**_.

## Acknowledgments

- Thanks Grok for making this `README` and understanding the math behind the Perlin noise.
- Thanks Kebab for the initial inspiration: [Kebab's Youtube video](https://www.youtube.com/watch?v=kbVn7Jdhl3Y)