package com.retrocode.newcustomkeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by Đorđe Milutinović on 9/6/17.
 * Copyright (c) 2017 retrocode. All rights reserved.
 */

public class CustomKeyboardView extends KeyboardView implements KeyboardView.OnKeyboardActionListener {

    public interface CustomKeyboardCallback {
        void okPressed();
    }

    private static final int BUTTON_RADIUS = 5;

    private static final int KEY_TEXT_VERTICAL_OFFSET = 25;
    private static final int KEY_TEXT_HORIZONTAL_OFFSET = 5;
    private static final int KEY_BACKGROUND_OFFSET = 10;

    private static final int STROKE_WIDTH = 6;
    private static final int STROKE_OFFSET = 2;

    private static final int KEY_TEXT_SIZE = 40;
    private static final int KEY_STROKE_WIDTH = 2;

    public static final int CODE_DELETE = -5;
    public static final int CODE_DOT = 46;
    public static final int CODE_ZERO = 48;
    public static final int CODE_ONE = 55001;
    public static final int CODE_FIVE = 55002;
    public static final int CODE_TEN = 55003;
    public static final int CODE_TWENTY_FIVE = 55004;
    public static final int CODE_FIFTY = 55005;
    public static final int CODE_OK = 55006;

    private CustomKeyboardCallback callback;

    public void setCallback(CustomKeyboardCallback callback) {
        this.callback = callback;
    }

    public CustomKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setKeyboard(new Keyboard(context, R.xml.custom_keyboard));
    }

    public CustomKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setKeyboard(new Keyboard(context, R.xml.custom_keyboard));
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        setOnKeyboardActionListener(this);
        Drawable dr = ContextCompat.getDrawable(getContext(), R.color.yellow);

        for (Keyboard.Key key : getKeyboard().getKeys()) {
            if (isKeySpecial(key.codes[0])) {
                dr.setBounds(key.x + KEY_BACKGROUND_OFFSET, key.y + KEY_BACKGROUND_OFFSET,
                        key.x + key.width, key.y + key.height + KEY_BACKGROUND_OFFSET);
                int keyNumberHeight = key.y + (key.height / 2) + KEY_TEXT_VERTICAL_OFFSET;

                dr.draw(canvas);
                canvas.drawRoundRect(getStrokeRectForSpecialButton(key),
                        BUTTON_RADIUS, BUTTON_RADIUS, paintKeyBorder());

                int keyWidth = getSpecialKeyNumberWidth(key);
                canvas.drawText(key.label.toString(), keyWidth, keyNumberHeight, paintKeyNumber());
            }
        }
    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int i) {
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        final Editable editable = getEditable();

        if (editable == null) {
            return;
        }

        String text = editable.toString();
        int start = getSelectionStart();

        if (isKeySpecial(primaryCode)) {
            editable.clear();
            editable.insert(0, getSpecialKeyLabel(primaryCode));
            hideCustomKeyboard();
            return;
        }

        switch (primaryCode) {
            case CODE_DELETE:
                if (start > 0) {
                    editable.delete(start - 1, start);
                }
                break;
            case CODE_ZERO:
                if (text.equals("0")) {
                    return;
                }

                editable.insert(start, Character.toString((char) primaryCode));
                break;
            case CODE_DOT:
                if (text.contains(".")) {
                    return;
                }

                if (start > 0) {
                    editable.insert(start, Character.toString((char) primaryCode));
                }
                break;
            case CODE_OK:
                if (callback != null) {
                    callback.okPressed();
                }
                hideCustomKeyboard();
                break;
            default:
                editable.insert(start, Character.toString((char) primaryCode));
                break;
        }
    }

    @Override
    public void onText(CharSequence charSequence) {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeUp() {
    }

    public void registerEditText(final EditText edittext) {
        edittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showCustomKeyboard(v);
                } else {
                    hideCustomKeyboard();
                }
            }
        });

        edittext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showCustomKeyboard(v);
            }
        });

        edittext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocus();

                setCursorPosition(event, edittext);

                showCustomKeyboard(v);
                return true;
            }
        });

        edittext.setInputType(edittext.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

    public void showCustomKeyboard(View v) {
        setVisibility(View.VISIBLE);
        setEnabled(true);
        setPreviewEnabled(false);

        if (v != null) {
            ((InputMethodManager) getContext().getSystemService(MainActivity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public void hideCustomKeyboard() {
        setVisibility(View.GONE);
        setEnabled(false);
    }

    public boolean isCustomKeyboardVisible() {
        return getVisibility() == View.VISIBLE;
    }

    private boolean isKeySpecial(int code) {
        return code == CODE_ONE || code == CODE_FIVE || code == CODE_TEN || code == CODE_TWENTY_FIVE || code == CODE_FIFTY;
    }

    private int getSpecialKeyNumberWidth(Keyboard.Key key) {
        if (key.codes[0] == CODE_ONE || key.codes[0] == CODE_FIVE) {
            return key.x + (key.width / 2) + (KEY_TEXT_HORIZONTAL_OFFSET * 2);
        }

        return key.x + (key.width / 2) + KEY_TEXT_HORIZONTAL_OFFSET;
    }

    private RectF getStrokeRectForSpecialButton(Keyboard.Key key) {
        int horizontalStartPosition = key.x + STROKE_OFFSET;
        int strokeLeft = horizontalStartPosition + KEY_TEXT_HORIZONTAL_OFFSET;
        int strokeRight = horizontalStartPosition + key.width;

        int strokeTop = key.y + KEY_BACKGROUND_OFFSET;
        int strokeBottom = strokeTop + key.height + STROKE_OFFSET;

        return new RectF(strokeLeft, strokeTop, strokeRight, strokeBottom);
    }

    private void setCursorPosition(MotionEvent event, EditText edittext) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Layout layout = edittext.getLayout();
            float x = event.getX() + edittext.getScrollX();
            int offset = layout.getOffsetForHorizontal(0, x);

            if (offset > 0)
                if (x > layout.getLineMax(0)) {
                    edittext.setSelection(offset);
                } else {
                    edittext.setSelection(offset - 1);
                }
        }
    }

    private Editable getEditable() {
        EditText editText = getFocusedEditText();

        if (editText == null) {
            return null;
        }

        return editText.getText();
    }

    private int getSelectionStart() {
        EditText editText = getFocusedEditText();

        if (editText == null) {
            return 0;
        }

        return editText.getSelectionStart();
    }

    private EditText getFocusedEditText() {
        View focusCurrent = ((MainActivity) getContext()).getWindow().getCurrentFocus();
        return (EditText) focusCurrent;
    }

    private Paint paintKeyNumber() {
        Paint paintKeyNumber = new Paint();
        paintKeyNumber.setTextAlign(Paint.Align.CENTER);
        paintKeyNumber.setTextSize(KEY_TEXT_SIZE);
        paintKeyNumber.setColor(Color.BLACK);
        paintKeyNumber.setStyle(Paint.Style.FILL_AND_STROKE);
        paintKeyNumber.setStrokeWidth(KEY_STROKE_WIDTH);
        paintKeyNumber.setAntiAlias(true);
        return paintKeyNumber;
    }

    private Paint paintKeyBorder() {
        Paint paintKeyBorder = new Paint();
        paintKeyBorder.setStyle(Paint.Style.STROKE);
        paintKeyBorder.setColor(Color.parseColor("#d8b54b"));
        paintKeyBorder.setStrokeWidth(STROKE_WIDTH);
        paintKeyBorder.setAntiAlias(true);
        return paintKeyBorder;
    }

    private String getSpecialKeyLabel(int primaryCode) {
        String specialTitle = "";

        for (Keyboard.Key key : getKeyboard().getKeys()) {
            if (key.codes[0] == primaryCode && isKeySpecial(primaryCode)) {
                specialTitle = key.label.toString().trim();
                break;
            }
        }

        return specialTitle;
    }

}
