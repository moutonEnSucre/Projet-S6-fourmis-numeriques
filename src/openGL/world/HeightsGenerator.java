package openGL.world;

import configs.Configs;
import openGL.utils.Maths;
import org.lwjgl.util.vector.Vector2f;

import java.util.Random;

public class HeightsGenerator {

    private static final float AMPLITUDE = 4f;
    private static final int OCTAVES = 3;
    private static final float ROUGHNESS = 0.75f;

    private Random random = new Random();
    private int seed;
    private float[][] heightMap;

    public HeightsGenerator(int xSize, int zSize) {
        this.seed = random.nextInt(1000000000);
        heightMap = new float[xSize][zSize];
    }

    public HeightsGenerator(int seed, int xSize, int zSize) {
        this.seed = seed;
        heightMap = new float[xSize][zSize];
    }

    public float getHeightInChunk(int gridX, int gridZ, float x, float z) {
        return getInterpolatedHeight(x + gridX * (Chunk.VERTEX_COUNT - 1), z + gridZ * (Chunk.VERTEX_COUNT - 1));
    }

    public int getSeed() {
        return seed;
    }

    public float getHeight(float x, float z) {
        return getInterpolatedHeight(x * (Chunk.VERTEX_COUNT - 1) + 1, z * (Chunk.VERTEX_COUNT - 1) + 1);
    }

    public void generateHeight(Vector2f worldCenter) {
        for (int x = 0; x < heightMap.length; x++) {
            for (int z = 0; z < heightMap[x].length; z++) {
                float total = 0;
                float d = (float) Math.pow(2, OCTAVES-1);
                for(int i = 0; i < OCTAVES; i++){
                    float freq = (float) (Math.pow(2, i) / d) / 3;
                    float amp = (float) Math.pow(ROUGHNESS, i) * AMPLITUDE;
                    total += getInterpolatedNoise(x * freq, z * freq) * amp;
                }
                int gridX = (int) Math.floor(x);
                int gridZ = (int) Math.floor(z);
                float distance = Vector2f.sub(worldCenter, new Vector2f((float)x / (Chunk.VERTEX_COUNT - 1), (float)z / (Chunk.VERTEX_COUNT - 1)), null).length();
                float attenuation = (float) Math.exp(-Math.pow((distance * Configs.FOG_DENSITY), .3f*Configs.FOG_GRADIENT));
                heightMap[x][z] = total * Maths.clamp(attenuation, 0, 1);
            }
        }
    }

    private float getInterpolatedNoise(float x, float z){
        int intX = (int) x;
        int intZ = (int) z;
        float fracX = x - intX;
        float fracZ = z - intZ;

        float v1 = getSmoothNoise(intX, intZ);
        float v2 = getSmoothNoise(intX + 1, intZ);
        float v3 = getSmoothNoise(intX, intZ + 1);
        float v4 = getSmoothNoise(intX + 1, intZ + 1);
        float i1 = interpolate(v1, v2, fracX);
        float i2 = interpolate(v3, v4, fracX);
        return interpolate(i1, i2, fracZ);
    }

    private float getInterpolatedHeight(float x, float z){
        int intX = (int) x;
        int intZ = (int) z;
        float fracX = x - intX;
        float fracZ = z - intZ;
        if (intX < 0)
            intX = 0;
        if (intZ < 0)
            intZ = 0;
        if (intX >= heightMap.length)
            intX = heightMap.length - 1;
        if (intZ >= heightMap[intX].length)
            intZ = heightMap[intX].length - 1;

        float v1 = heightMap[intX][intZ];
        float v2 = heightMap[intX >= heightMap.length - 1 ? heightMap.length - 1 : intX + 1][intZ];
        float v3 = heightMap[intX][intZ >= heightMap[intX].length - 1 ? heightMap[intX].length - 1 : intZ + 1];
        float v4 = heightMap[intX >= heightMap.length - 1 ? heightMap.length - 1 : intX + 1][intZ >= heightMap[intX].length - 1 ? heightMap[intX].length - 1 : intZ + 1];
        float i1 = interpolate(v1, v2, fracX);
        float i2 = interpolate(v3, v4, fracX);
        return interpolate(i1, i2, fracZ);
    }

    private float interpolate(float a, float b, float blend){
        double theta = blend * Math.PI;
        float f = (float)(1f - Math.cos(theta)) * 0.5f;
        return a * (1f - f) + b * f;
    }

    private float getSmoothNoise(int x, int z) {
        float corners = (getNoise(x - 1, z - 1) + getNoise(x + 1, z - 1) + getNoise(x - 1, z + 1)
                + getNoise(x + 1, z + 1)) / 16f;
        float sides = (getNoise(x - 1, z) + getNoise(x + 1, z) + getNoise(x, z - 1)
                + getNoise(x, z + 1)) / 8f;
        float center = getNoise(x, z) / 4f;
        return corners + sides + center;
    }

    private float getNoise(int x, int z) {
        random.setSeed(x * 49632 + z * 325176 + seed);
        return random.nextFloat() * 2f - 1f;
    }

}
