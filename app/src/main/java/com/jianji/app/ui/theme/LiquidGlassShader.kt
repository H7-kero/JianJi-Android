package com.jianji.app.ui.theme

import android.graphics.RuntimeShader
import android.os.Build

object LiquidGlassShader {

    val source = """
        uniform shader composable;
        uniform float2 resolution;
        uniform float refraction;
        uniform float dispersion;
        uniform float fresnelPower;
        uniform float edgeWidth;
        uniform float highlightIntensity;
        uniform float2 lensCenter;
        uniform float cornerRadius;
        uniform float specularIntensity;

        float sdfRoundedBox(float2 p, float2 b, float r) {
            float2 q = abs(p) - b + r;
            return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / resolution;
            float2 center = lensCenter;
            float2 fromCenter = uv - center;

            float dist = length(fromCenter);
            float2 dir = normalize(fromCenter + 0.0001);

            float refractedDist = refraction * smoothstep(0.0, 0.5, dist);
            float2 refractedUv = uv + dir * refractedDist * 0.02;

            float aberration = dispersion * 0.003;
            half4 r = composable.eval(refractedUv + float2(aberration, 0.0) * resolution);
            half4 g = composable.eval(refractedUv);
            half4 b = composable.eval(refractedUv - float2(aberration, 0.0) * resolution);

            half4 color = half4(r.r, g.g, b.b, r.a);

            float2 pixelCoord = fragCoord;
            float2 halfRes = resolution * 0.5;
            float2 p = pixelCoord - halfRes;
            float2 boxHalf = halfRes - cornerRadius;
            float sdf = sdfRoundedBox(p, boxHalf, cornerRadius);
            float normalizedSdf = sdf / max(resolution.x, resolution.y);

            // Directional Fresnel - stronger on bottom/right edges (light from top-left)
            float2 normalDir = normalize(p + 0.0001);
            float directionalBias = max(0.0, dot(normalDir, normalize(float2(-0.3, -0.7))));
            float fresnel = pow(1.0 - clamp(abs(normalizedSdf) * 8.0, 0.0, 1.0), fresnelPower);
            fresnel *= mix(0.7, 1.3, directionalBias);
            color = mix(color, half4(1.0), fresnel * 0.25);

            // Edge glow with top bias
            float edgeSdf = abs(sdf);
            float edgeGlow = 1.0 - smoothstep(0.0, edgeWidth, edgeSdf);
            float topBias = smoothstep(0.0, 0.3, 1.0 - uv.y);
            color = mix(color, half4(1.0), edgeGlow * topBias * highlightIntensity * 0.4);

            // Top highlight band
            float topHighlight = smoothstep(edgeWidth * 2.0, 0.0, sdf) *
                                 smoothstep(0.0, 0.15, uv.y) *
                                 smoothstep(0.15, 0.0, uv.y);
            color = mix(color, half4(1.0), topHighlight * highlightIntensity * 0.5);

            // Specular hot-spot - elliptical highlight simulating point light from top-left
            float2 specularCenter = float2(0.35, 0.22);
            float2 specularAxes = float2(0.35, 0.2);
            float2 specUV = (uv - specularCenter) / specularAxes;
            float specDist = length(specUV);
            float specular = exp(-specDist * specDist * 4.0) * specularIntensity;
            color = mix(color, half4(1.0), specular);

            // Subtle inner shadow at top edge
            float innerShadow = smoothstep(edgeWidth * 3.0, 0.0, sdf) *
                                smoothstep(0.0, 0.05, 1.0 - uv.y);
            color = mix(color, half4(0.0, 0.0, 0.0, 1.0), innerShadow * 0.06);

            return color;
        }
    """

    fun createShader(): RuntimeShader? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
        return try {
            RuntimeShader(source)
        } catch (_: Exception) {
            null
        }
    }

    fun applyShaderParams(
        shader: RuntimeShader,
        width: Float,
        height: Float,
        refraction: Float = 0.3f,
        dispersion: Float = 0.3f,
        fresnelPower: Float = 3.0f,
        edgeWidth: Float = 3.0f,
        highlightIntensity: Float = 0.8f,
        cornerRadius: Float = 20f,
        specularIntensity: Float = 0.35f
    ) {
        shader.setFloatUniform("resolution", width, height)
        shader.setFloatUniform("refraction", refraction)
        shader.setFloatUniform("dispersion", dispersion)
        shader.setFloatUniform("fresnelPower", fresnelPower)
        shader.setFloatUniform("edgeWidth", edgeWidth)
        shader.setFloatUniform("highlightIntensity", highlightIntensity)
        shader.setFloatUniform("lensCenter", width * 0.5f, height * 0.5f)
        shader.setFloatUniform("cornerRadius", cornerRadius)
        shader.setFloatUniform("specularIntensity", specularIntensity)
    }
}
