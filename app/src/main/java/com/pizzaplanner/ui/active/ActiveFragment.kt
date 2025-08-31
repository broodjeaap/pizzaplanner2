package com.pizzaplanner.ui.active

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pizzaplanner.databinding.FragmentActiveBinding

class ActiveFragment : Fragment() {

    private var _binding: FragmentActiveBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // TODO: Implement active recipe tracking
        // This will include:
        // - Display current active recipe
        // - Show current step with description
        // - Progress indicator
        // - Next step preview
        // - Manual step completion
        // - Time remaining display
        // - Pause/resume functionality
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
