package com.velentium.android.platformv.uicontrols.common.views

import android.content.Context
import android.content.res.ColorStateList
import android.text.InputFilter
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.velentium.android.platformv.uicontrols.R

/**
 * Custom Edit Text field that encapsulates [TextInputLayout], and [TextInputEditText] controls.
 */
class CustomEditText : LinearLayout {

    //region Properties

    private val editTextField: TextInputEditText
    private val editTextLayout: TextInputLayout

    /**
     * Simple enum to encapsulate the enter key func for EditText field
     */
    enum class EnterKeyAction(val imeOptions: Int) {
        NEXT(EditorInfo.IME_ACTION_NEXT),
        DONE(EditorInfo.IME_ACTION_DONE);

        companion object {
            val tag: String = EnterKeyAction::class.java.simpleName
        }
    }

    /**
     * Enum to wrap InputType, in order to constrain/handle possible types
     */
    enum class EditTextInputType(val inputType: Int) {
        TEXT(InputType.TYPE_CLASS_TEXT),
        EMAIL(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS),
        PASSWORD(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        companion object {
            val tag: String = EditTextInputType::class.java.simpleName
        }
    }

    val editText: TextInputEditText?
        get() = editTextField

    val textLayout: TextInputLayout?
        get() = editTextLayout

    var text: String
        get() = editTextField.text?.toString() ?: ""
        set(value) {
            editTextField.text = SpannableStringBuilder(value)
        }

    /**
     * Set input filters for the EditText field
     */
    var inputFilters: List<InputFilter>? = null
        set(value) {
            field = value
            editTextField.filters = field?.toTypedArray()
        }

    var enterKeyAction: EnterKeyAction = EnterKeyAction.NEXT
        set(value) {
            field = value
            editTextField.imeOptions = field.imeOptions
        }

    var onEditorActionListener: TextView.OnEditorActionListener? = null
        set(value) {
            field = value
            editTextField.setOnEditorActionListener(field)
        }

    var onTextChangeListener: TextWatcher? = null
        set(value) {
            value?.let {
                editTextField.addTextChangedListener(it)
                field = value
            } ?: run {
                field?.let {
                    editTextField.removeTextChangedListener(it)
                }
                field = null
            }
        }

    var onEditTextFocusChangeListener: OnFocusChangeListener? = null
        set(value) {
            field = value
        }

    /**
     * Sets/get the input type on the edit text field
     */
    var editTextInputType: EditTextInputType = EditTextInputType.TEXT
        set(value) {
            field = value
            editTextField.inputType = field.inputType
        }

    /**
     * Clear the field button
     */
    var hasClearButton: Boolean
        get() = editTextLayout.endIconMode == TextInputLayout.END_ICON_CLEAR_TEXT && editTextLayout.isEndIconVisible
        set(value) {
            if (value) {
                // these two are mutually exclusive
                hasShowPasswordButton = false

            }
            with(editTextLayout) {
                endIconMode =
                    if (value) TextInputLayout.END_ICON_CLEAR_TEXT else TextInputLayout.END_ICON_NONE
                isEndIconVisible = value
            }
        }

    /**
     * Shows a show password button in password fields
     */
    var hasShowPasswordButton: Boolean
        get() = editTextLayout.endIconMode == TextInputLayout.END_ICON_PASSWORD_TOGGLE && editTextLayout.isEndIconVisible
        set(value) {
            if (value) {
                // these two are mutually exclusive
                hasClearButton = false
            }
            with(editTextLayout) {
                endIconMode =
                    if (value) TextInputLayout.END_ICON_PASSWORD_TOGGLE else TextInputLayout.END_ICON_NONE
                isEndIconVisible = value
            }
        }

    /**
     * true if the errors label is showing, false if hidden
     */
    val isErrorShowing: Boolean
        get() = editTextLayout.isHelperTextEnabled

    // keeps track of errors since TextInputLayout has strange behavior around nulling error
    private var _errorText: String? = null

    //endregion


    //region Lifecycle

    init {
        View.inflate(context, R.layout.custom_edittext, this)
        editTextField = findViewById(R.id.edit_text_field)
        editTextLayout = findViewById(R.id.edit_text_layout)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        attrs?.let {
            initializeWithAttributes(it)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initializeUI()
    }

    //endregion

    //region Public Methods

    /**
     * Calls requestFocus on the EditText
     */
    fun requestEditTextFocus() {
        editTextField.requestFocus()
    }

    /**
     * Hides the errors for this field
     */
    fun hideError() {
        toggleError(false)
    }

    /**
     * Shows the errors for this field.
     */
    fun showError() {
        toggleError(true)
    }

    /**
     * Sets the errors label via string resources
     * @param errorTextResId
     * @param textColorResId
     */
    fun setError(
        @StringRes errorTextResId: Int,
        @ColorRes textColorResId: Int = android.R.color.holo_red_dark
    ) {
        setError(
            errorText = resources.getString(errorTextResId),
            textColor = ContextCompat.getColor(context, textColorResId)
        )
    }

    /**
     * Sets the errors label to [errorText]
     * @param errorText
     * @param textColor
     */
    @JvmOverloads
    fun setError(
        errorText: String,
        @ColorInt textColor: Int = ContextCompat.getColor(context, android.R.color.holo_red_dark)
    ) {
        _errorText = errorText
        with(editTextLayout) {
            error = _errorText
            setErrorTextColor(ColorStateList.valueOf(textColor))
            helperText = null
        }
    }

    //endregion

    //region Private Methods

    /**
     * Toggles the errors label visibility, with animation
     */
    private fun toggleError(visible: Boolean) {
        with(editTextLayout) {
            isHelperTextEnabled = false
            isErrorEnabled = visible
            error = if (visible) _errorText else null
        }
    }

    /**
     * Init. UI
     */
    private fun initializeUI() {
        with(editTextField) {
            setOnFocusChangeListener { view, hasFocus ->
                onEditTextFocusChangeListener?.onFocusChange(view, hasFocus)
            }
        }
        with(editTextLayout) {
            isHelperTextEnabled = false
            helperText = null
            isErrorEnabled = false
        }
    }

    /**
     * Init. with attributes from xml
     * @param attrs
     */
    private fun initializeWithAttributes(attrs: AttributeSet) {
        val typedAttrs = context.obtainStyledAttributes(attrs, R.styleable.CustomEditText)
        try {
            with(editTextField) {
                imeOptions = typedAttrs.getInt(
                    R.styleable.CustomEditText_android_imeOptions,
                    EditorInfo.IME_ACTION_NEXT
                )
                val editTextInputType = typedAttrs.getInt(
                    R.styleable.CustomEditText_android_inputType,
                    InputType.TYPE_CLASS_TEXT
                )
                inputType = editTextInputType
                val selectAllOnFocus = typedAttrs.getBoolean(
                    R.styleable.CustomEditText_android_selectAllOnFocus,
                    false
                )
                setSelectAllOnFocus(selectAllOnFocus)
                contentDescription =
                    typedAttrs.getString(R.styleable.CustomEditText_textAccessibilityId)
                        ?: hint

                val match = editTextInputType.and(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                if (match == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                    hasShowPasswordButton = true
                } else {
                    hasClearButton = true
                }
            }

            with(editTextLayout) {
                hint = typedAttrs.getString(R.styleable.CustomEditText_hintText)
                    ?: hint
                _errorText = typedAttrs.getString(R.styleable.CustomEditText_errorText)
                error = _errorText
                val errorTextColor = typedAttrs.getColor(
                    R.styleable.CustomEditText_errorTextColor,
                    ContextCompat.getColor(context, android.R.color.holo_red_dark)
                )
                setErrorTextColor(ColorStateList.valueOf(errorTextColor))
            }
        } catch (ex: Throwable) {
            Log.e(TAG, "Failed to read attrs", ex)
        } finally {
            typedAttrs.recycle()
        }
    }
    //endregion

    //region Companion

    companion object {
        val TAG: String = CustomEditText::class.java.simpleName
    }

    //endregion
}