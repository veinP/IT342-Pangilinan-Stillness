package edu.cit.pangilinan.stillness.features.landing

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class RippleLogoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = context.resources.displayMetrics.density

    // Callbacks
    var onBoxComplete: (() -> Unit)? = null

    // Timeline state
    private var progress = 0f // 0 to 3500

    // Paints
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3b6fd4")
        style = Paint.Style.FILL
    }

    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3b6fd4")
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
    }

    private val boxStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3b6fd4")
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }

    private val boxFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Colors
    private val rippleAlphaMax = 120 // ~47%

    // Pre-calculated metrics
    private val boxSize = 110f * density
    private val boxRadius = 24f * density

    // Ellipse targets
    private val targetRadii = arrayOf(
        PointF(43f * density, 13f * density),
        PointF(32f * density, 9.5f * density),
        PointF(20f * density, 6f * density),
        PointF(9f * density, 2.8f * density)
    )

    private val maxRadii = arrayOf(
        PointF(115f * density, 38f * density),
        PointF(85f * density, 28f * density),
        PointF(55f * density, 18f * density)
    )

    private val boxPath = Path()
    private val boxPathMeasure = PathMeasure()
    private var boxPathLength = 0f

    private var animator: ValueAnimator? = null
    private var boxCompleteTriggered = false

    init {
        // Gradient for box background
        val gradient = LinearGradient(
            0f, 0f, 0f, boxSize,
            Color.parseColor("#ffffff"),
            Color.parseColor("#f0f4fd"),
            Shader.TileMode.CLAMP
        )
        boxFillPaint.shader = gradient
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cx = w / 2f
        val cy = h / 2f

        boxPath.reset()
        boxPath.addRoundRect(
            cx - boxSize / 2f, cy - boxSize / 2f,
            cx + boxSize / 2f, cy + boxSize / 2f,
            boxRadius, boxRadius,
            Path.Direction.CW
        )
        boxPathMeasure.setPath(boxPath, false)
        boxPathLength = boxPathMeasure.length
    }

    fun startAnimation() {
        animator?.cancel()
        boxCompleteTriggered = false
        animator = ValueAnimator.ofFloat(0f, 3500f).apply {
            duration = 3500
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                
                if (progress >= 2600f && !boxCompleteTriggered) {
                    boxCompleteTriggered = true
                    onBoxComplete?.invoke()
                }
                
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f

        // Phase 1: Dot fades in and grows (0 - 400ms)
        if (progress > 0) {
            val dotP = (progress / 400f).coerceIn(0f, 1f)
            val r = dotP * 4f * density
            dotPaint.alpha = (dotP * 255).toInt()
            canvas.drawCircle(cx, cy, r, dotPaint)
        }

        // Phase 2 & 3: Ripples expand then shrink (200 - 2000ms)
        // Draw ripples inside box clipping if phase 4 is active
        val phase4P = ((progress - 1900f) / 700f).coerceIn(0f, 1f)
        val drawBoxBackground = phase4P > 0
        
        canvas.save()
        if (drawBoxBackground) {
            // Fill box background with alpha based on phase 4 progress
            boxFillPaint.alpha = (phase4P * 255).toInt()
            canvas.drawPath(boxPath, boxFillPaint)
            
            // Clip ripples to the box
            canvas.clipPath(boxPath)
        }

        // Draw ripples
        for (i in 0 until 3) {
            val startExpand = 200f + i * 160f
            if (progress >= startExpand) {
                // Expansion: startExpand to startExpand + 1100
                val expandP = ((progress - startExpand) / 1100f).coerceIn(0f, 1f)
                
                // Shrinking: 1300 to 2000
                val shrinkP = ((progress - 1300f) / 700f).coerceIn(0f, 1f)

                val currentRx: Float
                val currentRy: Float
                val currentAlpha: Int

                if (shrinkP > 0) {
                    // Interpolate from max size to target logo size
                    currentRx = lerp(maxRadii[i].x, targetRadii[i].x, shrinkP)
                    currentRy = lerp(maxRadii[i].y, targetRadii[i].y, shrinkP)
                    // Fade back in slightly during shrink
                    currentAlpha = lerpAlpha(rippleAlphaMax / 2, rippleAlphaMax, shrinkP)
                } else {
                    // Interpolate from dot to max size
                    currentRx = lerp(4f * density, maxRadii[i].x, expandP)
                    currentRy = lerp(4f * density, maxRadii[i].y, expandP)
                    // Fade out slightly during expand
                    currentAlpha = lerpAlpha(rippleAlphaMax, rippleAlphaMax / 2, expandP)
                }

                ripplePaint.alpha = currentAlpha
                canvas.drawOval(
                    cx - currentRx, cy - currentRy,
                    cx + currentRx, cy + currentRy,
                    ripplePaint
                )
            }
        }
        
        // Final logo state - draw the innermost dot/ellipse
        if (progress >= 1300f) {
            val shrinkP = ((progress - 1300f) / 700f).coerceIn(0f, 1f)
            val rx = lerp(4f * density, targetRadii[3].x, shrinkP)
            val ry = lerp(4f * density, targetRadii[3].y, shrinkP)
            ripplePaint.alpha = (shrinkP * rippleAlphaMax).toInt()
            canvas.drawOval(cx - rx, cy - ry, cx + rx, cy + ry, ripplePaint)
        }
        
        canvas.restore()

        // Phase 4: Draw box border (1900 - 2600ms)
        if (progress >= 1900f) {
            val drawP = ((progress - 1900f) / 700f).coerceIn(0f, 1f)
            val drawLength = drawP * boxPathLength
            
            boxStrokePaint.pathEffect = DashPathEffect(
                floatArrayOf(drawLength, boxPathLength), 0f
            )
            // Fade in stroke
            boxStrokePaint.alpha = (drawP * 255).toInt()
            canvas.drawPath(boxPath, boxStrokePaint)
        }
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction
    }

    private fun lerpAlpha(start: Int, end: Int, fraction: Float): Int {
        return (start + (end - start) * fraction).toInt()
    }
}
