package com.chenhongxin.menu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单按钮：上下左右 上中间 下中间 左中间 右中间
 */
public class ArcMenu extends ViewGroup {

    /**
     * 半径
     */
    private int radius = 100;

    /**
     * 小菜单默认顺时针方向分布
     */
    private ArcMenu.Direction direction = ArcMenu.Direction.Clockwise;

    /**
     * 小菜单分布的时候的方向
     * 顺时针
     * 逆时针
     */
    public enum Direction {
        Clockwise, AntiClockwise
    }

    /**
     * 主菜单的方向
     */
    private ArcMenu.MenuPostion menuPostion = ArcMenu.MenuPostion.Top_Center;

    /**
     * 中心菜单的位置
     */
    public enum MenuPostion {
        Left_Center, Right_Center, Top_Center, Buttom_Center, LeftTop, LeftButtom, RightTop, RightButtom, Center
    }

    /**
     * 存放所有孩子的位置信息
     */
    private List<Rect> rectList = new ArrayList<Rect>();

    /**
     * 主菜单的View所在的位置的中点,如果是一个圆就是圆点
     */
    private Point menuPoint = new Point();
    // 中间空间
    private View centerMenu;
    // 可以旋转
    private boolean canRotation;

    /**
     * 开始的角度
     */
    private int startAngle;

    /**
     * 结束的角度
     */
    private int endAngle;

    /**
     * 每一个小菜单之间的差的角度,可以是正的可以是负的
     */
    private int eachAngle;

    /**
     * 缩放范围
     */
    private float scaleSize = 2.0f;

    /**
     * 动画时间
     */
    private int duration = 300;

    /**
     * 菜单状态
     */
    private Status currentStatus = Status.CLOSE;

    enum Status{
        OPEN, CLOSE
    }

    private OnMenuClickListener onMenuClickListener;

    public ArcMenu(Context context) {
        super(context);
    }

    public ArcMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ArcMenu);

        //获取自定义属性
        radius = (int) a.getDimensionPixelSize(R.styleable.ArcMenu_arc_radius, 200);
        int directionAttr = a.getInt(R.styleable.ArcMenu_arc_direction, 0);
        direction = ArcMenu.Direction.values()[directionAttr];

        int menuPositionAttr = a.getInt(R.styleable.ArcMenu_arc_menu_position, 0);
        menuPostion = ArcMenu.MenuPostion.values()[menuPositionAttr];
        scaleSize = a.getFloat(R.styleable.ArcMenu_arc_scaleSize, scaleSize);
        duration = a.getInteger(R.styleable.ArcMenu_arc_duration, duration);
        canRotation = a.getBoolean(R.styleable.ArcMenu_arc_canRotation, canRotation);

        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed) {
            int count = getChildCount();
            layoutCenter();
            layoutChild(count);
            for (int i = 0; i < count; i++) {
                View view = getChildAt(i);
                Rect rect = rectList.get(i);
                view.layout(rect.left, rect.top, rect.right, rect.bottom);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 布局中间菜单
     */
    void layoutCenter(){
        rectList.clear();
        centerMenu = getChildAt(0);
        // 判断方位
        if (menuPostion == MenuPostion.LeftTop) { // 左上角
            menuPoint.x = 0 + centerMenu.getMeasuredWidth() / 2;
            menuPoint.y = 0 + centerMenu.getMeasuredHeight() / 2;
            startAngle = 0;
            endAngle = -90;
        }

        if (menuPostion == MenuPostion.Left_Center) { // 正左方方位
            menuPoint.x = 0 + centerMenu.getMeasuredWidth() / 2;
            menuPoint.y = getHeight() / 2;
            startAngle = 90;
            endAngle = -90;
        }

        if (menuPostion == MenuPostion.LeftButtom) { // 左下角
            menuPoint.x = 0 + centerMenu.getMeasuredWidth() / 2;
            menuPoint.y = getHeight() - centerMenu.getMeasuredHeight() / 2;
            startAngle = 90;
            endAngle = 0;
        }

        if (menuPostion == MenuPostion.Top_Center) { // 正上方方位
            menuPoint.x = getWidth() / 2;
            menuPoint.y = 0 + centerMenu.getMeasuredHeight() / 2;
            startAngle = 0;
            endAngle = -180;
        }

        if (menuPostion == MenuPostion.Buttom_Center) { // 正下方方位
            menuPoint.x = getWidth() / 2;
            menuPoint.y = getHeight() - centerMenu.getMeasuredHeight() / 2;
            startAngle = 180;
            endAngle = 0;
        }

        if (menuPostion == MenuPostion.RightTop) { // 右上方方位
            menuPoint.x = getWidth() - centerMenu.getMeasuredWidth() / 2;
            menuPoint.y = 0 + centerMenu.getMeasuredHeight() / 2;
            startAngle = 270;
            endAngle = 180;
        }

        if (menuPostion == MenuPostion.Right_Center) { // 正右方方位
            menuPoint.x = getWidth() - centerMenu.getMeasuredWidth() / 2;
            menuPoint.y = getHeight() / 2;
            startAngle = 270;
            endAngle = 90;
        }

        if (menuPostion == MenuPostion.RightButtom) { // 右下方方位
            menuPoint.x = getWidth() - centerMenu.getMeasuredWidth() / 2;
            menuPoint.y = getHeight() - centerMenu.getMeasuredHeight() / 2;
            startAngle = 180;
            endAngle = 90;
        }
        if (menuPostion == MenuPostion.Center) { // 正中间位置
            menuPoint.x = getWidth() / 2;
            menuPoint.y = getHeight() / 2;
            startAngle = 0;
            endAngle = 360;
        }

        rectList.add(new Rect(menuPoint.x - centerMenu.getMeasuredWidth() / 2, menuPoint.y - centerMenu.getMeasuredHeight() / 2,
                menuPoint.x + centerMenu.getMeasuredWidth() / 2, menuPoint.y + centerMenu.getMeasuredHeight() / 2));
        centerMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canRotation) {
                    rotationCenter(duration);
                }
                toggleMenu(duration);
                if(onMenuClickListener != null){
                    onMenuClickListener.open(v, isOpen());
                }
            }
        });
    }

    /**
     * 旋转中间
     * @param duration
     */
    private void rotationCenter(int duration) {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(duration);
        centerMenu.startAnimation(rotateAnimation);
    }

    /**
     * 切换菜单
     * @param duration 时间
     */
    public void toggleMenu(int duration) {
        int count = getChildCount();
        //如果显示的情况下就让所有的孩子消失
        for (int i = 0; i < count - 1; i++){
            final View view = getChildAt(i + 1);
            view.setVisibility(VISIBLE);
            Rect rect = rectList.get(i + 1);
            int left = menuPoint.x - (rect.left + rect.right) / 2;
            int top = menuPoint.y - (rect.top + rect.bottom) / 2;
            AnimationSet animSet = new AnimationSet(true);
            TranslateAnimation tranAnim = null;
            if(currentStatus == Status.OPEN) {
                tranAnim = new TranslateAnimation(0.0F, (float)left, 0.0F, (float)top);
                view.setClickable(false);
                view.setFocusable(false);
            } else{
                tranAnim = new TranslateAnimation((float)left, 0.0F, (float)top, 0.0F);
                view.setClickable(true);
                view.setFocusable(true);
            }
            tranAnim.setDuration((long)duration);
            tranAnim.setFillAfter(true);
            tranAnim.setStartOffset(i * 100 / count);
            tranAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(currentStatus == Status.CLOSE){
                        view.setVisibility(GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            RotateAnimation rotateAnimation = new RotateAnimation(0, 1440, Animation.RELATIVE_TO_SELF,
                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(duration);
            animSet.addAnimation(rotateAnimation);
            animSet.addAnimation(tranAnim);
            animSet.setDuration(duration);
            view.startAnimation(animSet);
        }
        changeStatus();
    }

    /**
     * 测量孩子的位置
     * @param count 数量
     */
    void layoutChild(int count){
        if(menuPostion == MenuPostion.LeftTop){ // 左上方
            startAngle = 360;
            endAngle = 270;
        }
        if(menuPostion == MenuPostion.Top_Center){ // 正上方
            startAngle = 360;
            endAngle = 180;
        }
        if(menuPostion == MenuPostion.RightTop){ // 右上方
            startAngle = 270;
            endAngle = 180;
        }
        if(menuPostion == MenuPostion.Right_Center){ // 正右方
            startAngle = 270;
            endAngle = 90;
        }
        if(menuPostion == MenuPostion.RightButtom){ // 右下方
            startAngle = 180;
            endAngle = 90;
        }
        if(menuPostion == MenuPostion.Buttom_Center){ // 正下方
            startAngle = 180;
            endAngle = 0;
        }
        if(menuPostion == MenuPostion.LeftButtom){ // 左下方
            startAngle = 90;
            endAngle = 0;
        }
        if(menuPostion == MenuPostion.Left_Center){ // 正左方
            startAngle = 90;
            endAngle = -90;
        }
        if(menuPostion == MenuPostion.Center){ // 正中间
            startAngle = 0;
            endAngle = 360;
        }
        // 方位交换
        if(direction == Direction.Clockwise){
            startAngle = startAngle + endAngle;
            endAngle = startAngle - endAngle;
            startAngle = startAngle - endAngle;
        }
        // 计算每个孩子之间的角度
        eachAngle = (endAngle - startAngle) / (count - 2);
        if(menuPostion == MenuPostion.Center){
            eachAngle = (endAngle - startAngle) / (count - 1);
        }
        for(int i = 0; i < count - 1; i++){
            View view = getChildAt(i + 1);
            view.setVisibility(GONE);
            int angle = startAngle + eachAngle * i;
            int x = (int) (menuPoint.x + radius * Math.cos(Math.toRadians(angle)));
            int y = (int) (menuPoint.y - radius * Math.sin(Math.toRadians(angle)));
            rectList.add(new Rect(x - view.getMeasuredWidth() / 2, y - view.getMeasuredHeight() / 2, x + view.getMeasuredWidth() / 2, y + view.getMeasuredHeight() / 2));
            view.setTag(i);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (Integer) v.getTag();
                    if(onMenuClickListener != null){
                        onMenuClickListener.onClick(v, pos);
                    }
                    changeStatus();
                    selectChild(pos);
                }
            });
        }
    }

    /**
     * 选中的孩子
     * @param position 索引
     */
    void selectChild(int position){
        int count = getChildCount();
        for(int i = 0; i < count -1; i++){
            View view = getChildAt(i + 1);
            if(i == position){
                view.startAnimation(scaleBitAnim(duration));
            }else{
                view.startAnimation(scaleSmallAnim(duration));
            }
            view.setClickable(false);
            view.setFocusable(false);
        }
    }

    /**
     * 变小动画
     * @param duration
     * @return
     */
    private Animation scaleSmallAnim(int duration) {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0f, 1.0f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setFillAfter(true);
        animationSet.setDuration(duration);
        return animationSet;
    }

    /**
     * 变大效果
     * @param duration
     * @return
     */
    private Animation scaleBitAnim(int duration) {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, scaleSize, 1.0f, scaleSize, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setFillAfter(true);
        animationSet.setDuration(duration);
        return animationSet;
    }

    /**
     * 改变状态
     */
    void changeStatus() {
        currentStatus = currentStatus == Status.CLOSE ? Status.OPEN : Status.CLOSE;
    }

    /**
     * @return 动画时间
     */
    public int getDuration() {
        return duration;
    }

    public boolean isOpen(){
        return currentStatus == Status.OPEN;
    }

    public void setOnMenuClickListener(OnMenuClickListener onMenuClickListener) {
        this.onMenuClickListener = onMenuClickListener;
    }

    public interface OnMenuClickListener{
        void onClick(View view, int position);
        void open(View view, boolean isOpen);
    }

}
