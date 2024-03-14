package ru.netology.nmedia.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import ru.netology.nmedia.R
import ru.netology.nmedia.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()
    private val backgroundColor = resources.getColor(R.color.divider_color)

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    private val dotPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    var data: List<Float?> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    var sum: Float = data.filterNotNull().sum()
        set(value) {
            if (value >= data.filterNotNull().sum()) {
                field = value
            }
            invalidate()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }
        canvas.drawCircle(
            center.x,
            center.y,
            radius,
            paint.apply { color = backgroundColor })

        var startFrom = -90F
        for ((index, datum) in data.withIndex()) {
            if (datum != null) {
                val angle = 360F * datum / sum
                paint.color = colors.getOrNull(index) ?: randomColor()
                canvas.drawArc(oval, startFrom, angle, false, paint)
                startFrom += angle
            }
        }

        drawDot(canvas)

        canvas.drawText(
            "%.2f%%".format(data.filterNotNull().sum() / sum * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())

    private fun drawDot(canvas: Canvas) {
        val index = data.indexOfFirst { it != 0F }
        val color = colors[index]
        dotPaint.color = color
        canvas.drawCircle(center.x, center.y - radius, (lineWidth + 1F) / 2, dotPaint)
    }
}