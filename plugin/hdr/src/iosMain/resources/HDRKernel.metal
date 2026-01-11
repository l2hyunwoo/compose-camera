//
//  HDRKernel.metal
//  Compose Camera HDR Plugin
//
//  Copyright (C) 2025 l2hyunwoo
//  Licensed under the Apache License, Version 2.0
//

#include <metal_stdlib>
using namespace metal;

/**
 * Calculate exposure weight for HDR compositing.
 * Based on Mertens et al. exposure fusion algorithm.
 *
 * Weight factors:
 * - Well-exposedness: Gaussian weight centered at 0.5
 * - Saturation: Higher saturation gets higher weight
 * - Contrast (optional): Local Laplacian could be added
 */
kernel void calculateExposureWeight(
    texture2d<float, access::read> input [[texture(0)]],
    texture2d<float, access::write> output [[texture(1)]],
    uint2 gid [[thread_position_in_grid]]
) {
    // Validate both input and output texture bounds
    if (gid.x >= input.get_width() || gid.y >= input.get_height() ||
        gid.x >= output.get_width() || gid.y >= output.get_height()) {
        return;
    }
    
    float4 pixel = input.read(gid);
    
    // Well-exposedness: Gaussian weight centered at 0.5
    // exp(-((x - 0.5)^2) / (2 * sigma^2)) where sigma = 0.2
    float sigma2 = 0.08; // 2 * 0.2^2
    float wellExposedR = exp(-pow(pixel.r - 0.5, 2.0) / sigma2);
    float wellExposedG = exp(-pow(pixel.g - 0.5, 2.0) / sigma2);
    float wellExposedB = exp(-pow(pixel.b - 0.5, 2.0) / sigma2);
    float wellExposed = wellExposedR * wellExposedG * wellExposedB;
    
    // Saturation weight
    float mean = (pixel.r + pixel.g + pixel.b) / 3.0;
    float saturation = sqrt((pow(pixel.r - mean, 2.0) + 
                             pow(pixel.g - mean, 2.0) + 
                             pow(pixel.b - mean, 2.0)) / 3.0);
    
    // Combine weights (adding small epsilon to avoid zero weights)
    float weight = (wellExposed + 0.001) * (saturation + 0.001);
    
    output.write(float4(weight, weight, weight, 1.0), gid);
}

/**
 * Blend multiple weighted images.
 * Takes weighted sum of images normalized by total weights.
 */
kernel void blendWeightedImages(
    texture2d<float, access::read> image1 [[texture(0)]],
    texture2d<float, access::read> weight1 [[texture(1)]],
    texture2d<float, access::read> image2 [[texture(2)]],
    texture2d<float, access::read> weight2 [[texture(3)]],
    texture2d<float, access::read> image3 [[texture(4)]],
    texture2d<float, access::read> weight3 [[texture(5)]],
    texture2d<float, access::write> output [[texture(6)]],
    uint2 gid [[thread_position_in_grid]]
) {
    // Validate all texture dimensions to prevent OOB access
    if (gid.x >= output.get_width() || gid.y >= output.get_height() ||
        gid.x >= image1.get_width() || gid.y >= image1.get_height() ||
        gid.x >= weight1.get_width() || gid.y >= weight1.get_height() ||
        gid.x >= image2.get_width() || gid.y >= image2.get_height() ||
        gid.x >= weight2.get_width() || gid.y >= weight2.get_height() ||
        gid.x >= image3.get_width() || gid.y >= image3.get_height() ||
        gid.x >= weight3.get_width() || gid.y >= weight3.get_height()) {
        return;
    }

    float4 c1 = image1.read(gid);
    float w1 = weight1.read(gid).r;
    
    float4 c2 = image2.read(gid);
    float w2 = weight2.read(gid).r;
    
    float4 c3 = image3.read(gid);
    float w3 = weight3.read(gid).r;
    
    float totalWeight = w1 + w2 + w3 + 0.0001; // Epsilon to avoid division by zero
    
    float4 blended = (c1 * w1 + c2 * w2 + c3 * w3) / totalWeight;
    blended.a = 1.0;
    
    output.write(blended, gid);
}

/**
 * Reinhard tone mapping operator.
 * Maps HDR values to displayable range: L / (1 + L)
 * Then applies gamma correction.
 */
kernel void reinhardToneMap(
    texture2d<float, access::read> input [[texture(0)]],
    texture2d<float, access::write> output [[texture(1)]],
    constant float& gamma [[buffer(0)]],
    uint2 gid [[thread_position_in_grid]]
) {
    // Validate both input and output texture bounds
    if (gid.x >= input.get_width() || gid.y >= input.get_height() ||
        gid.x >= output.get_width() || gid.y >= output.get_height()) {
        return;
    }

    float4 hdr = input.read(gid);

    // Reinhard operator: L / (1 + L)
    float3 mapped = hdr.rgb / (1.0 + hdr.rgb);

    // Gamma correction with safety clamp to prevent divide-by-zero
    float safeGamma = max(gamma, 0.001);
    float invGamma = 1.0 / safeGamma;
    mapped = pow(mapped, float3(invGamma));
    
    // Clamp to valid range
    mapped = clamp(mapped, 0.0, 1.0);
    
    output.write(float4(mapped, 1.0), gid);
}

/**
 * Simple exposure blend without tone mapping.
 * Useful as a fallback or for real-time preview.
 */
kernel void simpleExposureBlend(
    texture2d<float, access::read> lowExposure [[texture(0)]],
    texture2d<float, access::read> normalExposure [[texture(1)]],
    texture2d<float, access::read> highExposure [[texture(2)]],
    texture2d<float, access::write> output [[texture(3)]],
    uint2 gid [[thread_position_in_grid]]
) {
    // Validate all texture dimensions to prevent OOB access
    if (gid.x >= output.get_width() || gid.y >= output.get_height() ||
        gid.x >= lowExposure.get_width() || gid.y >= lowExposure.get_height() ||
        gid.x >= normalExposure.get_width() || gid.y >= normalExposure.get_height() ||
        gid.x >= highExposure.get_width() || gid.y >= highExposure.get_height()) {
        return;
    }

    float4 low = lowExposure.read(gid);
    float4 normal = normalExposure.read(gid);
    float4 high = highExposure.read(gid);
    
    // Calculate luminance
    float lumNormal = dot(normal.rgb, float3(0.2126, 0.7152, 0.0722));
    
    // Use high exposure in dark areas, low exposure in bright areas
    float darkWeight = smoothstep(0.0, 0.4, lumNormal);
    float brightWeight = smoothstep(0.6, 1.0, lumNormal);
    
    // Blend based on luminance
    float4 result = normal;
    result.rgb = mix(high.rgb, result.rgb, darkWeight);
    result.rgb = mix(low.rgb, result.rgb, 1.0 - brightWeight);
    result.a = 1.0;
    
    output.write(result, gid);
}
