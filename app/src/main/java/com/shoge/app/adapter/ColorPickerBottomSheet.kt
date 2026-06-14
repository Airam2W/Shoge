package com.shoge.app.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shoge.app.R
import com.shoge.app.adapter.ColorSwatchAdapter
import com.shoge.app.databinding.BottomSheetColorPickerBinding

class ColorPickerBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetColorPickerBinding? = null
    private val binding get() = _binding!!

    var onColorSelected: ((Int) -> Unit)? = null
    private var currentColor = DEFAULT_COLOR

    companion object {
        const val TAG = "ColorPickerBottomSheet"
        private const val ARG_COLOR = "arg_color"
        private val DEFAULT_COLOR = Color.parseColor("#FF6B00")

        // 16 curated preset colors (4×4 grid)
        val PRESET_COLORS = listOf(
            0xFFFF6B00.toInt(),  // Orange — brand
            0xFFEF4444.toInt(),  // Red
            0xFFEC4899.toInt(),  // Pink
            0xFF8B5CF6.toInt(),  // Purple
            0xFF6366F1.toInt(),  // Indigo
            0xFF3B82F6.toInt(),  // Blue
            0xFF06B6D4.toInt(),  // Cyan
            0xFF14B8A6.toInt(),  // Teal
            0xFF22C55E.toInt(),  // Green
            0xFF84CC16.toInt(),  // Lime
            0xFFEAB308.toInt(),  // Yellow
            0xFFF59E0B.toInt(),  // Amber
            0xFF1A1A1A.toInt(),  // Near-black
            0xFF6B7280.toInt(),  // Gray
            0xFF475569.toInt(),  // Slate
            0xFFF8FAFC.toInt()   // Off-white
        )

        fun newInstance(initialColor: Int) = ColorPickerBottomSheet().apply {
            arguments = Bundle().apply { putInt(ARG_COLOR, initialColor) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentColor = arguments?.getInt(ARG_COLOR) ?: DEFAULT_COLOR
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetColorPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwatchGrid()
        setupHexInput()
        setupPreview()

        binding.btnColorDone.setOnClickListener {
            onColorSelected?.invoke(currentColor)
            dismiss()
        }
    }

    private fun setupSwatchGrid() {
        val adapter = ColorSwatchAdapter(PRESET_COLORS, currentColor) { color ->
            currentColor = color
            // Sync hex field without re-triggering the text watcher loop
            val hex = "#%06X".format(0xFFFFFF and color)
            if (binding.etHexColor.text.toString() != hex) {
                binding.etHexColor.setText(hex)
            }
            refreshPreview()
        }
        binding.rvColorSwatches.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            this.adapter = adapter
            itemAnimator = null    // prevent flicker on swatch redraws
        }
    }

    private fun setupHexInput() {
        // Seed the field with the initial color
        binding.etHexColor.setText("#%06X".format(0xFFFFFF and currentColor))

        binding.etHexColor.addTextChangedListener { editable ->
            val input = editable.toString().trim()
            // Accept only full 7-char hex strings (#RRGGBB)
            if (input.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                try {
                    currentColor = Color.parseColor(input)
                    refreshPreview()
                } catch (_: IllegalArgumentException) { /* ignore malformed */ }
            }
        }
    }

    private fun setupPreview() = refreshPreview()

    private fun refreshPreview() {
        // Repaint the live preview circle
        binding.viewColorPreview.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(currentColor)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}