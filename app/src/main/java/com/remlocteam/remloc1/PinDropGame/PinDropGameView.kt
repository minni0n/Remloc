package com.remlocteam.remloc1.PinDropGame

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.remlocteam.remloc1.R
import java.util.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pin Drop - a one-tap arcade game.
 *
 * A map pin swings like a pendulum at the top of a scrolling city map.
 * Tap to drop it onto the moving target zones below. The closer to the
 * bullseye, the more points. Consecutive bullseyes build a combo
 * multiplier. Three misses and the game is over.
 */
class PinDropGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private enum class State { READY, AIMING, FALLING, GAME_OVER }

    private data class Target(
        var x: Float,
        var y: Float,
        var radius: Float,
        var hit: Boolean = false,
        var hitOffsetX: Float = 0f
    )

    private data class Ripple(var x: Float, var y: Float, var age: Float, var maxRadius: Float)

    private data class FloatingText(
        val text: String,
        var x: Float,
        var y: Float,
        var age: Float,
        val color: Int
    )

    private val density = resources.displayMetrics.density
    private fun dp(v: Float): Float = v * density

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Colors from the app palette
    private val colorBackground = ContextCompat.getColor(context, R.color.apple_background)
    private val colorGrid = ContextCompat.getColor(context, R.color.pastel_gray)
    private val colorBlock = ContextCompat.getColor(context, R.color.slight_gray)
    private val colorPin = ContextCompat.getColor(context, R.color.app_main)
    private val colorTargetOuter = ContextCompat.getColor(context, R.color.apple_blue_light)
    private val colorTargetInner = ContextCompat.getColor(context, R.color.apple_blue)
    private val colorText = ContextCompat.getColor(context, R.color.apple_text)
    private val colorTextSecondary = ContextCompat.getColor(context, R.color.apple_text_secondary)
    private val colorGood = ContextCompat.getColor(context, R.color.apple_success)
    private val colorBad = ContextCompat.getColor(context, R.color.red)

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val random = Random()

    private var state = State.READY
    private var running = false
    private var lastFrameNanos = 0L

    // World
    private var gridOffset = 0f
    private val targets = mutableListOf<Target>()
    private val ripples = mutableListOf<Ripple>()
    private val floatingTexts = mutableListOf<FloatingText>()

    // Pin
    private var swingPhase = 0f
    private var pinX = 0f
    private var pinY = 0f
    private var pinVelocityY = 0f

    // Progress
    private var score = 0
    private var bestScore = prefs.getInt(KEY_HIGH_SCORE, 0)
    private var lives = MAX_LIVES
    private var combo = 0
    private var newBest = false
    private var gameOverAt = 0L

    private fun swingTop(): Float = dp(96f)
    private fun swingAmplitude(): Float = width / 2f - dp(48f)
    private fun scrollSpeed(): Float = dp(85f) + min(dp(1.4f) * score, dp(150f))
    private fun swingOmega(): Float = 2.1f + min(0.045f * score, 2.6f)
    private fun targetRadius(): Float = max(dp(46f) - dp(0.35f) * score, dp(26f))
    private fun gravity(): Float = dp(3200f)

    // region lifecycle

    fun resumeGame() {
        running = true
        lastFrameNanos = 0L
        postInvalidateOnAnimation()
    }

    fun pauseGame() {
        running = false
    }

    // endregion

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && targets.isEmpty()) {
            resetRound(fullReset = true)
        }
    }

    private fun resetRound(fullReset: Boolean) {
        if (fullReset) {
            score = 0
            lives = MAX_LIVES
            combo = 0
            newBest = false
            targets.clear()
            ripples.clear()
            floatingTexts.clear()
            var y = height * 0.55f
            while (y < height + dp(120f)) {
                spawnTarget(y)
                y += spawnGap()
            }
        }
        swingPhase = random.nextFloat() * TWO_PI
        pinY = swingTop()
        pinVelocityY = 0f
    }

    private fun spawnGap(): Float = dp(230f) + random.nextFloat() * dp(170f)

    private fun spawnTarget(y: Float) {
        val r = targetRadius()
        val margin = r + dp(24f)
        val x = margin + random.nextFloat() * (width - 2f * margin)
        targets.add(Target(x, y, r))
    }

    // region input

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true
        when (state) {
            State.READY -> {
                state = State.AIMING
            }
            State.AIMING -> {
                state = State.FALLING
                pinVelocityY = dp(220f)
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
            State.FALLING -> Unit
            State.GAME_OVER -> {
                // Small lockout so the game-over tap doesn't instantly restart
                if (System.currentTimeMillis() - gameOverAt > 600) {
                    resetRound(fullReset = true)
                    state = State.AIMING
                }
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    // endregion

    // region update

    private fun update(dt: Float) {
        val scrolling = state != State.GAME_OVER
        if (scrolling) {
            val speed = scrollSpeed()
            gridOffset = (gridOffset + speed * dt) % dp(GRID_CELL_DP)

            val iterator = targets.listIterator()
            while (iterator.hasNext()) {
                val t = iterator.next()
                t.y -= speed * dt
                if (t.y < -t.radius) iterator.remove()
            }
            val lowest = targets.maxByOrNull { it.y }
            if (lowest == null || lowest.y < height + dp(60f) - spawnGap()) {
                spawnTarget((lowest?.y ?: height * 0.7f) + spawnGap())
            }
        }

        when (state) {
            State.READY, State.AIMING, State.GAME_OVER -> {
                swingPhase = (swingPhase + swingOmega() * dt) % TWO_PI
                pinX = width / 2f + swingAmplitude() * sin(swingPhase)
                pinY = swingTop()
            }
            State.FALLING -> {
                pinVelocityY += gravity() * dt
                pinY += pinVelocityY * dt
                val hitTarget = targets.firstOrNull { t ->
                    !t.hit && distance(pinX, pinY, t.x, t.y) <= t.radius
                }
                if (hitTarget != null) {
                    onTargetHit(hitTarget)
                } else if (pinY > height + dp(40f)) {
                    onMiss()
                }
            }
        }

        ripples.forEach { it.age += dt }
        ripples.removeAll { it.age >= RIPPLE_LIFETIME }
        floatingTexts.forEach {
            it.age += dt
            it.y -= dp(38f) * dt
        }
        floatingTexts.removeAll { it.age >= TEXT_LIFETIME }
    }

    private fun onTargetHit(target: Target) {
        val dist = distance(pinX, pinY, target.x, target.y)
        val ratio = dist / target.radius
        val points: Int
        when {
            ratio <= 0.35f -> {
                points = 3
                combo++
            }
            ratio <= 0.7f -> {
                points = 2
                combo = 0
            }
            else -> {
                points = 1
                combo = 0
            }
        }
        val multiplier = max(1, combo)
        val gained = points * multiplier
        score += gained
        if (score > bestScore) {
            bestScore = score
            newBest = true
            prefs.edit().putInt(KEY_HIGH_SCORE, bestScore).apply()
        }

        target.hit = true
        target.hitOffsetX = pinX - target.x
        ripples.add(Ripple(target.x, target.y, 0f, target.radius * 2.2f))
        val label = if (multiplier > 1) "+$gained x$multiplier" else "+$gained"
        floatingTexts.add(FloatingText(label, target.x, target.y - target.radius, 0f, colorGood))
        performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)

        state = State.AIMING
        resetRound(fullReset = false)
    }

    private fun onMiss() {
        lives--
        combo = 0
        floatingTexts.add(
            FloatingText(
                context.getString(R.string.pin_drop_miss),
                pinX.coerceIn(dp(60f), width - dp(60f)),
                height - dp(140f),
                0f,
                colorBad
            )
        )
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        if (lives <= 0) {
            state = State.GAME_OVER
            gameOverAt = System.currentTimeMillis()
        } else {
            state = State.AIMING
            resetRound(fullReset = false)
        }
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    // endregion

    // region drawing

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var dt = 0f
        val now = System.nanoTime()
        if (lastFrameNanos != 0L) {
            dt = min((now - lastFrameNanos) / 1_000_000_000f, MAX_FRAME_DT)
        }
        lastFrameNanos = now
        if (running && width > 0) update(dt)

        drawMap(canvas)
        targets.forEach { drawTarget(canvas, it) }
        drawRipples(canvas)
        drawPin(canvas, pinX, pinY, dp(16f))
        drawFloatingTexts(canvas)
        drawHud(canvas)

        when (state) {
            State.READY -> drawCenteredOverlay(
                canvas,
                context.getString(R.string.pin_drop),
                context.getString(R.string.pin_drop_how_to),
                context.getString(R.string.pin_drop_tap_to_start)
            )
            State.GAME_OVER -> drawGameOver(canvas)
            else -> Unit
        }

        if (running) postInvalidateOnAnimation()
    }

    private fun drawMap(canvas: Canvas) {
        canvas.drawColor(colorBackground)

        val cell = dp(GRID_CELL_DP)
        // City blocks: rounded squares inside the street grid
        fillPaint.color = colorBlock
        val inset = dp(10f)
        var y = -cell + (cell - gridOffset % cell)
        while (y < height + cell) {
            var x = 0f
            while (x < width + cell) {
                val rect = RectF(x + inset, y + inset, x + cell - inset, y + cell - inset)
                canvas.drawRoundRect(rect, dp(6f), dp(6f), fillPaint)
                x += cell
            }
            y += cell
        }

        // Street lines
        strokePaint.color = colorGrid
        strokePaint.strokeWidth = dp(2f)
        y = -cell + (cell - gridOffset % cell)
        while (y < height + cell) {
            canvas.drawLine(0f, y, width.toFloat(), y, strokePaint)
            y += cell
        }
        var x = 0f
        while (x < width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), strokePaint)
            x += cell
        }
    }

    private fun drawTarget(canvas: Canvas, target: Target) {
        if (target.hit) {
            // A softer, "completed" zone with the stuck pin
            fillPaint.color = withAlpha(colorGood, 60)
            canvas.drawCircle(target.x, target.y, target.radius, fillPaint)
            strokePaint.color = colorGood
            strokePaint.strokeWidth = dp(2f)
            canvas.drawCircle(target.x, target.y, target.radius, strokePaint)
            drawPin(canvas, target.x + target.hitOffsetX, target.y, dp(12f))
            return
        }

        fillPaint.color = withAlpha(colorTargetOuter, 170)
        canvas.drawCircle(target.x, target.y, target.radius, fillPaint)

        fillPaint.color = withAlpha(colorTargetInner, 90)
        canvas.drawCircle(target.x, target.y, target.radius * 0.7f, fillPaint)

        fillPaint.color = colorTargetInner
        canvas.drawCircle(target.x, target.y, target.radius * 0.35f, fillPaint)

        strokePaint.color = colorTargetInner
        strokePaint.strokeWidth = dp(2f)
        canvas.drawCircle(target.x, target.y, target.radius, strokePaint)
    }

    private fun drawRipples(canvas: Canvas) {
        ripples.forEach { r ->
            val progress = r.age / RIPPLE_LIFETIME
            strokePaint.color = withAlpha(colorGood, ((1f - progress) * 200).toInt())
            strokePaint.strokeWidth = dp(3f)
            canvas.drawCircle(r.x, r.y, r.maxRadius * progress, strokePaint)
        }
    }

    /** Draws a classic map pin whose tip is at (tipX, tipY). */
    private fun drawPin(canvas: Canvas, tipX: Float, tipY: Float, headRadius: Float) {
        val headCenterY = tipY - headRadius * 2.2f

        fillPaint.color = colorPin
        val path = Path()
        path.moveTo(tipX, tipY)
        path.lineTo(tipX - headRadius * 0.85f, headCenterY + headRadius * 0.5f)
        path.lineTo(tipX + headRadius * 0.85f, headCenterY + headRadius * 0.5f)
        path.close()
        canvas.drawPath(path, fillPaint)
        canvas.drawCircle(tipX, headCenterY, headRadius, fillPaint)

        fillPaint.color = colorBackground
        canvas.drawCircle(tipX, headCenterY, headRadius * 0.42f, fillPaint)
    }

    private fun drawHud(canvas: Canvas) {
        textPaint.color = colorText
        textPaint.textSize = dp(22f)
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(
            "${context.getString(R.string.pin_drop_score)}: $score",
            dp(16f),
            dp(34f),
            textPaint
        )

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = colorTextSecondary
        canvas.drawText(
            "${context.getString(R.string.pin_drop_best)}: $bestScore",
            width - dp(16f),
            dp(34f),
            textPaint
        )

        // Lives as small pins
        for (i in 0 until MAX_LIVES) {
            val alpha = if (i < lives) 255 else 50
            fillPaint.color = withAlpha(colorPin, alpha)
            val cx = width / 2f + (i - 1) * dp(28f)
            canvas.drawCircle(cx, dp(26f), dp(7f), fillPaint)
            val tail = Path()
            tail.moveTo(cx, dp(26f) + dp(14f))
            tail.lineTo(cx - dp(5f), dp(29f))
            tail.lineTo(cx + dp(5f), dp(29f))
            tail.close()
            canvas.drawPath(tail, fillPaint)
        }

        if (combo > 1 && state != State.GAME_OVER) {
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.color = colorGood
            textPaint.textSize = dp(18f)
            canvas.drawText(
                "${context.getString(R.string.pin_drop_combo)} x$combo",
                width / 2f,
                dp(64f),
                textPaint
            )
        }
    }

    private fun drawFloatingTexts(canvas: Canvas) {
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = dp(20f)
        floatingTexts.forEach { t ->
            val progress = t.age / TEXT_LIFETIME
            textPaint.color = withAlpha(t.color, ((1f - progress) * 255).toInt())
            canvas.drawText(t.text, t.x, t.y, textPaint)
        }
    }

    private fun drawCenteredOverlay(canvas: Canvas, title: String, line1: String, line2: String) {
        fillPaint.color = withAlpha(Color.WHITE, 215)
        val panel = RectF(dp(24f), height * 0.36f, width - dp(24f), height * 0.62f)
        canvas.drawRoundRect(panel, dp(20f), dp(20f), fillPaint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = colorPin
        textPaint.textSize = dp(34f)
        canvas.drawText(title, width / 2f, panel.top + dp(56f), textPaint)

        textPaint.color = colorText
        textPaint.textSize = dp(16f)
        canvas.drawText(line1, width / 2f, panel.top + dp(96f), textPaint)

        textPaint.color = colorTextSecondary
        canvas.drawText(line2, width / 2f, panel.bottom - dp(28f), textPaint)
    }

    private fun drawGameOver(canvas: Canvas) {
        fillPaint.color = withAlpha(Color.BLACK, 110)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fillPaint)

        fillPaint.color = withAlpha(Color.WHITE, 235)
        val panel = RectF(dp(24f), height * 0.32f, width - dp(24f), height * 0.66f)
        canvas.drawRoundRect(panel, dp(20f), dp(20f), fillPaint)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = colorBad
        textPaint.textSize = dp(30f)
        canvas.drawText(
            context.getString(R.string.pin_drop_game_over),
            width / 2f,
            panel.top + dp(52f),
            textPaint
        )

        textPaint.color = colorText
        textPaint.textSize = dp(22f)
        canvas.drawText(
            "${context.getString(R.string.pin_drop_score)}: $score",
            width / 2f,
            panel.top + dp(96f),
            textPaint
        )
        textPaint.color = colorTextSecondary
        textPaint.textSize = dp(18f)
        canvas.drawText(
            "${context.getString(R.string.pin_drop_best)}: $bestScore",
            width / 2f,
            panel.top + dp(128f),
            textPaint
        )

        if (newBest) {
            textPaint.color = colorGood
            textPaint.textSize = dp(18f)
            canvas.drawText(
                context.getString(R.string.pin_drop_new_best),
                width / 2f,
                panel.top + dp(158f),
                textPaint
            )
        }

        textPaint.color = colorTextSecondary
        textPaint.textSize = dp(16f)
        canvas.drawText(
            context.getString(R.string.pin_drop_tap_to_retry),
            width / 2f,
            panel.bottom - dp(28f),
            textPaint
        )
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))

    // endregion

    companion object {
        private const val PREFS_NAME = "PinDropGame"
        private const val KEY_HIGH_SCORE = "highScore"
        private const val MAX_LIVES = 3
        private const val TWO_PI = 6.2831855f
        private const val GRID_CELL_DP = 96f
        private const val RIPPLE_LIFETIME = 0.6f
        private const val TEXT_LIFETIME = 0.9f
        private const val MAX_FRAME_DT = 1f / 30f
    }
}
