package com.huazhi.changsha.compositeexperiment.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

@SuppressLint("AppCompatCustomView")
public class CustomEditText extends EditText implements OnFocusChangeListener,
        TextWatcher {

    private boolean hasFocus;
    public Context mContext;
    private Drawable mDrawable; // 右侧删除图标

    public CustomEditText(Context mContext) {
        super(mContext);
        initView();
    }

    public CustomEditText(Context mContext, AttributeSet attr) {
        super(mContext, attr);
        initView();
    }

    public CustomEditText(Context mContext, AttributeSet attr, int defStyle) {
        super(mContext, attr, defStyle);
        initView();
    }

    public void initView() {
        // 取得右侧删除图标
        // 即我们在布局文件中设置的android:drawableRight
        Drawable[] drawables = this.getCompoundDrawables();
        mDrawable = drawables[2];
        if (mDrawable == null) {// 如果drawableRight没有设置,那么使用默认的图标
            mDrawable = getResources().getDrawable(android.R.drawable.ic_delete);
        }
        mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth()/2,
                mDrawable.getIntrinsicHeight()/2);
        setClearIconVisible(false);// 默认隐藏删除图标
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }

    /**
     * 因为我们不能直接给EditText设置点击事件，所以我们用记住我们按下的位置来模拟点击事件 当我们按下的位置 在 EditText的宽度 -
     * 图标到控件右边的间距 - 图标的宽度 和 EditText的宽度 - 图标到控件右边的间距之间我们就算点击了图标
     */
    @SuppressLint("ClickableViewAccessibility") @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {

                boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight())
                        && (event.getX() < ((getWidth() - getPaddingRight())));
                if (touchable) {
                    this.setText("");
                }
            }
        }

        return super.onTouchEvent(event);
    }

    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // TODO Auto-generated method stub

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (hasFocus) {
            setClearIconVisible(s.length() > 0);
        }
    }

    public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub

    }

    /**
     * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
     */
    public void onFocusChange(View v, boolean hasFocus) {
        this.hasFocus = hasFocus;
        if (hasFocus) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

    /**
     * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     *
     * @param visible
     */
    protected void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

}
