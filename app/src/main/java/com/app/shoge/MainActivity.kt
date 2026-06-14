package com.shoge.app

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.webkit.URLUtil
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.shoge.app.adapter.BrowserDropdownAdapter
import com.shoge.app.databinding.ActivityMainBinding
import com.shoge.app.databinding.LayoutFormBinding
import com.shoge.app.model.BrowserInfo
import com.shoge.app.model.FormState
import com.shoge.app.model.IconType
import com.shoge.app.ui.ColorPickerBottomSheet
import com.shoge.app.util.BrowserDetector
import com.shoge.app.generator.GeneratorResult
import com.shoge.app.generator.ShortcutGenerator
import android.graphics.Color as Color1
import android.net.Uri as Uri1

class MainActivity : AppCompatActivity() {

    // ── ViewBinding ───────────────────────────────────────────────────────────

    private lateinit var binding: ActivityMainBinding
    private lateinit var form: LayoutFormBinding   // the form card contents

    // ── State ─────────────────────────────────────────────────────────────────

    private var state = FormState()
    private var browsers: List<BrowserInfo> = emptyList()

    // ── Image picker ──────────────────────────────────────────────────────────

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri1? -> uri?.let { applyPickedImage(it) } }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        inflateForm()
        loadBrowsers()
        setupIconToggle()
        setupTextWatchers()
        setupGenerateButton()
    }

    // ── Form inflation ────────────────────────────────────────────────────────

    private fun inflateForm() {
        // Attach layout_form.xml into the card's FrameLayout
        form = LayoutFormBinding.inflate(layoutInflater, binding.formContainer, true)
        // Paint the preview circle with the default brand orange
        paintIconColor(state.iconColor)
    }

    // ── Browser dropdown ──────────────────────────────────────────────────────

    private fun loadBrowsers() {
        browsers = BrowserDetector.getInstalledBrowsers(this)

        if (browsers.isEmpty()) {
            snack(getString(R.string.msg_no_browsers))
            form.tilBrowser.isEnabled = false
            return
        }

        val adapter = BrowserDropdownAdapter(this, browsers)
        form.actvBrowser.setAdapter(adapter)

        form.actvBrowser.setOnItemClickListener { _, _, position, _ ->
            state = state.copy(selectedBrowser = browsers[position])
            form.tilBrowser.error = null
            // Tint the start icon orange to signal a selection was made
            form.tilBrowser.setStartIconTintList(
                android.content.res.ColorStateList.valueOf(
                    getColor(R.color.orange_primary)
                )
            )
        }
    }

    // ── Icon toggle ───────────────────────────────────────────────────────────

    private fun setupIconToggle() {
        // Start in color mode
        form.toggleIconType.check(R.id.btn_toggle_color)

        form.toggleIconType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btn_toggle_color -> activateColorMode()
                R.id.btn_toggle_image -> activateImageMode()
            }
        }

        form.btnIconAction.setOnClickListener {
            when (state.iconType) {
                IconType.COLOR -> openColorPicker()
                IconType.IMAGE -> pickImage.launch("image/*")
            }
        }
    }

    private fun activateColorMode() {
        state = state.copy(iconType = IconType.COLOR)
        form.btnIconAction.text = getString(R.string.btn_pick_color)
        form.tvIconHint.text    = "Tap to choose a color for your icon"
        form.ivIconImage.visibility    = android.view.View.GONE
        form.tvIconInitial.visibility  = android.view.View.VISIBLE
        form.viewIconBg.visibility     = android.view.View.VISIBLE
        paintIconColor(state.iconColor)
    }

    private fun activateImageMode() {
        state = state.copy(iconType = IconType.IMAGE)
        form.btnIconAction.text = getString(R.string.btn_pick_image)
        form.tvIconHint.text    = "Tap to pick an image from your gallery"

        if (state.iconBitmap != null) {
            // Already picked an image — keep showing it
            form.ivIconImage.visibility   = android.view.View.VISIBLE
            form.tvIconInitial.visibility = android.view.View.GONE
            form.viewIconBg.visibility    = android.view.View.GONE
        }
    }

    private fun openColorPicker() {
        val sheet = ColorPickerBottomSheet.newInstance(state.iconColor)
        sheet.onColorSelected = { color ->
            state = state.copy(iconColor = color)
            paintIconColor(color)
        }
        sheet.show(supportFragmentManager, ColorPickerBottomSheet.TAG)
    }

    /** Redraws the icon preview oval with a new solid color. */
    private fun paintIconColor(color: Int) {
        form.viewIconBg.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }

    private fun applyPickedImage(uri: Uri1) {
        try {
            val stream = contentResolver.openInputStream(uri)
            val bmp    = BitmapFactory.decodeStream(stream)
            stream?.close()

            state = state.copy(iconBitmap = bmp, iconType = IconType.IMAGE)
            form.ivIconImage.setImageBitmap(bmp)
            form.ivIconImage.visibility   = android.view.View.VISIBLE
            form.tvIconInitial.visibility = android.view.View.GONE
            form.viewIconBg.visibility    = android.view.View.GONE
        } catch (e: Exception) {
            snack("Could not load image. Please try another.")
        }
    }

    // ── Text watchers ─────────────────────────────────────────────────────────

    private fun setupTextWatchers() {
        form.etLink.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                state = state.copy(url = s.toString().trim())
                form.tilLink.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        form.etName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val name = s.toString().trim()
                state = state.copy(shortcutName = name)
                form.tilName.error = null
                // Mirror the first letter into the icon preview
                form.tvIconInitial.text =
                    if (name.isNotEmpty()) name[0].uppercaseChar().toString() else "S"
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ── Generate button ───────────────────────────────────────────────────────

    private fun setupGenerateButton() {
        binding.btnGenerate.setOnClickListener {
            if (!validateForm()) return@setOnClickListener

            // Show a loading state so the user knows something is happening
            binding.btnGenerate.isEnabled = false
            binding.btnGenerate.text = "Generating…"

            val result = ShortcutGenerator.generate(this, state)
            handleGeneratorResult(result)

            // Restore button immediately — the system dialog takes over from here
            binding.btnGenerate.isEnabled = true
            binding.btnGenerate.text = getString(R.string.btn_generate)
        }
    }

    private fun handleGeneratorResult(result: GeneratorResult) {
        when (result) {
            is GeneratorResult.DialogShown -> {
                snack(getString(R.string.msg_dialog_shown))
            }

            is GeneratorResult.PinningNotSupported -> {
                // Some custom launchers (certain OEM ROMs) don't support pinned shortcuts.
                // Show a persistent Snackbar with a link to the docs so the user understands why.
                Snackbar.make(
                    binding.root,
                    getString(R.string.msg_pinning_not_supported),
                    Snackbar.LENGTH_LONG
                )
                    .setBackgroundTint(Color1.parseColor("#1A1A1A"))
                    .setTextColor(Color1.WHITE)
                    .setActionTextColor(Color1.parseColor("#FF6B00"))
                    .setAction("Why?") {
                        openDocsLink()
                    }
                    .show()
            }

            is GeneratorResult.Error -> {
                snack(getString(R.string.msg_generation_error, result.cause))
            }
        }
    }

    private fun openDocsLink() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri1.parse("https://developer.android.com/guide/topics/ui/shortcuts/creating-shortcuts")
        )
        startActivity(intent)
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private fun validateForm(): Boolean {
        var ok = true

        // URL
        val rawUrl = state.url
        when {
            rawUrl.isEmpty() -> {
                form.tilLink.error = "A URL is required"
                ok = false
            }
            !rawUrl.startsWith("http://") && !rawUrl.startsWith("https://") -> {
                // Auto-prefix and re-validate
                val fixed = "https://$rawUrl"
                if (URLUtil.isValidUrl(fixed)) {
                    form.etLink.setText(fixed)
                    state = state.copy(url = fixed)
                    form.tilLink.error = null
                } else {
                    form.tilLink.error = getString(R.string.msg_invalid_url)
                    ok = false
                }
            }
            !URLUtil.isValidUrl(rawUrl) -> {
                form.tilLink.error = getString(R.string.msg_invalid_url)
                ok = false
            }
            else -> form.tilLink.error = null
        }

        // Name
        if (state.shortcutName.isEmpty()) {
            form.tilName.error = "A shortcut name is required"
            ok = false
        }

        // Browser
        if (state.selectedBrowser == null) {
            form.tilBrowser.error = "Please select a browser"
            ok = false
        }

        // Icon image
        if (state.iconType == IconType.IMAGE && state.iconBitmap == null) {
            snack("Please select an image for the icon")
            ok = false
        }

        if (!ok) snack(getString(R.string.msg_fill_required))
        return ok
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun snack(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(Color1.parseColor("#1A1A1A"))
            .setTextColor(Color1.WHITE)
            .show()
    }
}