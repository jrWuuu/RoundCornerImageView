package wuchen.com.viewdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

@SuppressLint("AppCompatCustomView")
public class RoundCornerImageView extends ImageView {

    private static final String TAG = "RoundCornerImageView";

    /**
     * 圆形
     */
    private static final int TYPE_CIRCLE = 0;

    /**
     * 圆角矩形
     */
    private static final int TYPE_ROUND_RECT = 1;

    private int mRadius = 10;

    private int mType = 0;

    private final Paint mPaint = new Paint();

    /**
     * 叠加模式
     */
    private Xfermode mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);

    public RoundCornerImageView(Context context) {
        super(context);
        initView(context, null);
    }

    public RoundCornerImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public RoundCornerImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, @Nullable AttributeSet attrs) {
        if (context == null || attrs == null) {
            return;
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundCornerImageView);
        if (typedArray != null) {
            int radius = typedArray.getDimensionPixelOffset(R.styleable.RoundCornerImageView_my_radius, 10);
            int type = typedArray.getInt(R.styleable.RoundCornerImageView_my_type, 0);
            this.mRadius = radius;
            this.mType = type;
            Log.d(TAG, "mRadius:" + mRadius + ", mType:" + mType);
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 如果类型是圆形，但是使用者长宽设置不一致，那么需要进行处理，以最小的值为准
        if (mType != TYPE_CIRCLE) {
            return;
        }
        int finalSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
        Log.i(TAG, "onMeasure getMeasuredWidth: " + getMeasuredWidth() + ", getMeasuredHeight:"
                + getMeasuredHeight() + ", finalSize:" + finalSize);
        setMeasuredDimension(finalSize, finalSize);
    }

    /*
    绘制圆角矩形，使用的是paint自带的叠加属性。也就是Paint.setXfermode()
    那么，首先我们要在图上画出原图，然后在设置好XferMode，在画出圆角矩形图，即可
    */

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw");
        Drawable drawable = getDrawable();

        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        // 图片的原始宽度
        int intrinsicWidth = drawable.getIntrinsicWidth();
        // 图片的原始高度
        int intrinsicHeight = drawable.getIntrinsicHeight();

        // 控件的当前宽度
        int viewWidth = getWidth();
        // 控件的当前高度
        int viewHeight = getHeight();

        Log.i(TAG, "onDraw intrinsicWidth: " + intrinsicWidth + ", intrinsicHeight:"
                + intrinsicHeight + ", viewWidth:" + viewWidth + ", viewHeight:" + viewHeight
                + ", getWidth:" + bitmap.getWidth() + ", getHeight:" + bitmap.getHeight());

        // 原始图片和当前图片的宽高比较。
        // 宽高比不一致，因为需要出现圆角效果，不能出现空白区域，那么对比控件/原图的宽高比，用较大的一个对图片进行缩放。
        float widthScale = viewWidth * 1.0f / intrinsicWidth;
        float heightScale = viewHeight * 1.0f / intrinsicHeight;
        final float scale = Math.max(widthScale, heightScale);

        Log.i(TAG, "onDraw widthScale: " + widthScale + ", heightScale:"
                + heightScale + ", scale:" + scale);

        // 缩放比计算完毕后，我们需要根据控件当前的宽度生成一个bitmap作为一个画布的底板，
        // 在上面画好图，
        // 最后在控件的canvas上画出

        Bitmap resBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        // 生成bitmap并且和画布绑定
        Canvas tempCanvas = new Canvas(resBitmap);
        // 对原始图片进行缩放
        drawable.setBounds(0, 0, (int) (intrinsicWidth * scale), (int) (intrinsicHeight * scale));
        // 临时画板上绘制原图
        drawable.draw(tempCanvas);
        Log.d(TAG, "onDraw done draw drawable" );

        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        Bitmap roundBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        // 在另外一块画布上对roundBitmap订制为圆角
        Canvas roundCanvas = new Canvas(roundBitmap);
        if (mType == TYPE_ROUND_RECT) {
            Log.d(TAG, "onDraw TYPE_ROUND_RECT");
            roundCanvas.drawRoundRect(0, 0, viewWidth, viewHeight, mRadius, mRadius, mPaint);
        }
        if (mType == TYPE_CIRCLE) {
            Log.d(TAG, "onDraw TYPE_CIRCLE");
                roundCanvas.drawCircle(viewWidth / 2, viewHeight / 2, viewWidth / 2, mPaint);
        }

        // 重叠的方式，画好圆角矩形
        mPaint.setXfermode(mXfermode);
        // 临时画板上叠加绘制圆角图
        tempCanvas.drawBitmap(roundBitmap, 0, 0, mPaint);
        Log.d(TAG, "onDraw done draw round");

        mPaint.setXfermode(null);
        canvas.drawBitmap(resBitmap, 0, 0, mPaint);
        Log.d(TAG, "onDraw done");
    }

}
